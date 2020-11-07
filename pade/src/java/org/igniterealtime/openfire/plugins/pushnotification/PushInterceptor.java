/*
 * Copyright (C) 2019 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.igniterealtime.openfire.plugins.pushnotification;

import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import nl.martijndwars.webpush.*;
import org.jivesoftware.util.*;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;

public class PushInterceptor implements PacketInterceptor, OfflineMessageListener
{
    private static final Logger Log = LoggerFactory.getLogger( PushInterceptor.class );
    public static final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<>();

    /**
     * Invokes the interceptor on the specified packet. The interceptor can either modify
     * the packet, or throw a PacketRejectedException to block it from being sent or processed
     * (when read).<p>
     * <p>
     * An exception can only be thrown when <tt>processed</tt> is false which means that the read
     * packet has not been processed yet or the packet was not sent yet. If the exception is thrown
     * with a "read" packet then the sender of the packet will receive an answer with an error. But
     * if the exception is thrown with a "sent" packet then nothing will happen.<p>
     * <p>
     * Note that for each packet, every interceptor will be called twice: once before processing
     * is complete (<tt>processing==true</tt>) and once after processing is complete. Typically,
     * an interceptor will want to ignore one or the other case.
     *
     * @param packet    the packet to take action on.
     * @param session   the session that received or is sending the packet.
     * @param incoming  flag that indicates if the packet was read by the server or sent from
     *                  the server.
     * @param processed flag that indicates if the action (read/send) was performed. (PRE vs. POST).
     * @throws PacketRejectedException if the packet should be prevented from being processed.
     */
    @Override
    public void interceptPacket( final Packet packet, final Session session, final boolean incoming, final boolean processed ) throws PacketRejectedException
    {
        if ( incoming ) {
            return;
        }

        if ( !processed ) {
            return;
        }

        if ( !(packet instanceof Message)) {
            return;
        }

        final String body = ((Message) packet).getBody();
        if ( body == null || body.isEmpty() )
        {
            return;
        }

        if (!(session instanceof ClientSession)) {
            return;
        }

        final User user;
        try
        {
            user = XMPPServer.getInstance().getUserManager().getUser( packet.getTo().getNode() );
        }
        catch ( UserNotFoundException e )
        {
            Log.debug( "Not a recognized user.", e );
            return;
        }

        Log.debug( "If user '{}' has push services configured, pushes need to be sent for a message that just arrived.", user );
        tryPushNotification( user, body, packet.getFrom(), ((Message) packet).getType() );
    }

    private void tryPushNotification( User user, String body, JID jid, Message.Type msgtype )
    {
        if (XMPPServer.getInstance().getPresenceManager().isAvailable( user ))
        {
            return; // dont notify if user is online and available. let client handle that
        }

        webPush(user, body, jid, msgtype, null);
    }
    /**
     * Notification message indicating that a message was not stored offline but bounced
     * back to the sender.
     *
     * @param message the message that was bounced.
     */
    @Override
    public void messageBounced( final Message message )
    {}

    /**
     * Notification message indicating that a message was stored offline since the target entity
     * was not online at the moment.
     *
     * @param message the message that was stored offline.
     */
    @Override
    public void messageStored( final Message message )
    {
        if ( message.getBody() == null || message.getBody().isEmpty() )
        {
            return;
        }

        Log.debug( "Message stored to offline storage. Try to send push notification." );
        final User user;
        try
        {
            user = XMPPServer.getInstance().getUserManager().getUser( message.getTo().getNode() );
            tryPushNotification( user, message.getBody(), message.getFrom(), message.getType() );
        }
        catch ( UserNotFoundException e )
        {
            Log.error( "Unable to find local user '{}'.", message.getTo().getNode(), e );
        }
    }
    /**
     * Push a payload to a subscribed web push user
     *
     *
     * @param user being pushed to.
     * @param publishOptions web push data stored.
     * @param body web push payload.
     */
    public void webPush( final User user, final String body, JID jid, Message.Type msgtype, String nickname )
    {
        try {
            for (String key : user.getProperties().keySet())
            {
                if (key.startsWith("webpush.subscribe."))
                {
                    String publicKey = user.getProperties().get("vapid.public.key");
                    String privateKey = user.getProperties().get("vapid.private.key");

                    if (publicKey == null) publicKey = JiveGlobals.getProperty("vapid.public.key", null);
                    if (privateKey == null) privateKey = JiveGlobals.getProperty("vapid.private.key", null);

                    if (publicKey != null && privateKey != null)
                    {
                        PushService pushService = new PushService()
                            .setPublicKey(publicKey)
                            .setPrivateKey(privateKey)
                            .setSubject("mailto:admin@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain());

                        String username = user.getUsername();
                        String token = tokens.get(username);

                        if (token == null)
                        {
                            token = TimeBasedOneTimePasswordUtil.generateBase32Secret();
                            tokens.put(token, username);
                        }

                        Subscription subscription = new Gson().fromJson(user.getProperties().get(key), Subscription.class);
                        Stanza stanza = new Stanza(msgtype == Message.Type.chat ? "chat" : "groupchat", jid.asBareJID().toString(), body, nickname, token);
                        Notification notification = new Notification(subscription, (new Gson().toJson(stanza)).toString());
                        HttpResponse response = pushService.send(notification);
                        int statusCode = response.getStatusLine().getStatusCode();

                        Log.debug( "For user '{}', Web push notification response '{}'", user.toString(), response.getStatusLine().getStatusCode() );
                    }
                }
            }
        } catch (Exception e) {
            Log.warn( "An exception occurred while trying send a web push for user '{}'.", new Object[] { user, e } );
        }
    }
}
