package org.ifsoft.meet;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.security.*;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.util.*;
import org.jivesoftware.openfire.group.*;
import org.jivesoftware.openfire.user.*;
import org.jivesoftware.openfire.session.*;
import org.jivesoftware.openfire.muc.*;

import org.xmpp.packet.*;
import org.jivesoftware.openfire.plugin.rawpropertyeditor.RawPropertyEditor;
import org.jivesoftware.openfire.plugin.rest.dao.PropertyDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.json.*;
import org.xmpp.packet.*;
import org.dom4j.Element;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import nl.martijndwars.webpush.*;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

/**
 * The Class MeetController.
 */
public class MeetController {

    private static final Logger Log = LoggerFactory.getLogger(MeetController.class);
    public static final MeetController INSTANCE = new MeetController();

    /**
     * Gets the instance.
     *
     * @return the instance
     */
    public static MeetController getInstance() {
        return INSTANCE;
    }

    //-------------------------------------------------------
    //
    //  Web Push
    //
    //-------------------------------------------------------

    /**
     * push a payload to all subscribed web push resources of a group
     *
     */
    public boolean groupWebPush(String groupName, String payload)
    {
        boolean ok = false;

        String publicKey = JiveGlobals.getProperty("vapid.public.key", null);
        String privateKey = JiveGlobals.getProperty("vapid.private.key", null);
        String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

        try {
            Group group = GroupManager.getInstance().getGroup(groupName);

            if (group != null && publicKey != null && privateKey != null)
            {
                PushService pushService = new PushService()
                    .setPublicKey(publicKey)
                    .setPrivateKey(privateKey)
                    .setSubject("mailto:admin@" + domain);

                Log.debug("groupWebPush keys \n"  + publicKey + "\n" + privateKey);

                for (String key : group.getProperties().keySet())
                {
                    if (key.startsWith("webpush.subscribe."))
                    {
                        try {
                            Subscription subscription = new Gson().fromJson(group.getProperties().get(key), Subscription.class);
                            Notification notification = new Notification(subscription, payload);
                            HttpResponse response = pushService.send(notification);
                            int statusCode = response.getStatusLine().getStatusCode();

                            ok =  ok && (200 == statusCode) || (201 == statusCode);

                            Log.debug("groupWebPush delivered "  + statusCode + "\n" + response);

                        } catch (Exception e) {
                            Log.error("groupWebPush failed "  + "\n" + payload, e);
                        }
                    }
                }

            }
        } catch (Exception e1) {
            Log.error("groupWebPush failed "  + "\n" + payload, e1);
        }

        return ok;
    }

    /**
     * push a payload to all subscribed web push resources of a user
     *
     */
    public boolean postWebPush(String username, String payload)
    {
        Log.debug("postWebPush "  + username + "\n" + payload);

        User user = RawPropertyEditor.getInstance().getAndCheckUser(username);
        if (user == null) return false;
        boolean ok = false;

        String publicKey = user.getProperties().get("vapid.public.key");
        String privateKey = user.getProperties().get("vapid.private.key");

        if (publicKey == null) publicKey = JiveGlobals.getProperty("vapid.public.key", null);
        if (privateKey == null) privateKey = JiveGlobals.getProperty("vapid.private.key", null);

        try {
            if (publicKey != null && privateKey != null)
            {
                PushService pushService = new PushService()
                    .setPublicKey(publicKey)
                    .setPrivateKey(privateKey)
                    .setSubject("mailto:admin@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain());

                Log.debug("postWebPush keys \n"  + publicKey + "\n" + privateKey);

                for (String key : user.getProperties().keySet())
                {
                    if (key.startsWith("webpush.subscribe."))
                    {
                        try {
                            Subscription subscription = new Gson().fromJson(user.getProperties().get(key), Subscription.class);
                            Notification notification = new Notification(subscription, payload);
                            HttpResponse response = pushService.send(notification);
                            int statusCode = response.getStatusLine().getStatusCode();

                            ok = ok && (200 == statusCode) || (201 == statusCode);

                            Log.debug("postWebPush delivered "  + statusCode + "\n" + response);


                        } catch (Exception e) {
                            Log.error("postWebPush failed "  + username + "\n" + payload, e);
                        }
                    }
                }

            }
        } catch (Exception e1) {
            Log.error("postWebPush failed "  + username + "\n" + payload, e1);
        }

        return ok;
    }

    /**
     * store web push subscription as a user property
     *
     */
    public boolean putWebPushSubscription(String username, String resource, String subscription)
    {
        Log.debug("putWebPushSubscription "  + username + " " + resource + "\n" + subscription);

        User user = RawPropertyEditor.getInstance().getAndCheckUser(username);
        if (user == null) return false;

        user.getProperties().put("webpush.subscribe." + resource, subscription);
        return true;
    }

    /**
     * generate a new public/private key pair for VAPID and store in system properties
     * and user properties
     */
    public String getWebPushPublicKey(String username)
    {
        Log.debug("getWebPushPublicKey " + username);

        String ofPublicKey = null;
        String ofPrivateKey = null;

        User user = RawPropertyEditor.getInstance().getAndCheckUser(username);
        if (user == null) return null;

        ofPublicKey = user.getProperties().get("vapid.public.key");
        ofPrivateKey = user.getProperties().get("vapid.private.key");

        if (ofPublicKey == null || ofPrivateKey == null)
        {
            try {
                KeyPair keyPair = generateKeyPair();

                byte[] publicKey = Utils.savePublicKey((ECPublicKey) keyPair.getPublic());
                byte[] privateKey = Utils.savePrivateKey((ECPrivateKey) keyPair.getPrivate());

                ofPublicKey = BaseEncoding.base64Url().encode(publicKey);
                ofPrivateKey = BaseEncoding.base64Url().encode(privateKey);

                user.getProperties().put("vapid.public.key", ofPublicKey);
                JiveGlobals.setProperty("vapid.public.key", ofPublicKey);

                user.getProperties().put("vapid.private.key", ofPrivateKey);
                JiveGlobals.setProperty("vapid.private.key", ofPrivateKey);

            } catch (Exception e) {
                Log.error("getWebPushPublicKey", e);
            }

        } else {
            user.getProperties().put("vapid.public.key", ofPublicKey);
            user.getProperties().put("vapid.private.key", ofPrivateKey);
        }

        return ofPublicKey;
    }

    /**
     * Generate an EC keypair on the prime256v1 curve.
     *
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    private KeyPair generateKeyPair() throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
        keyPairGenerator.initialize(parameterSpec);

        return keyPairGenerator.generateKeyPair();
    }

    //-------------------------------------------------------
    //
    //  Message
    //
    //-------------------------------------------------------

    public boolean postMessage(String username, String payload)
    {
        Log.debug("postMessage "  + username + "\n" + payload);

        User user = RawPropertyEditor.getInstance().getAndCheckUser(username);
        if (user == null) return false;
        boolean ok = false;

        try {
            JSONObject json = new JSONObject(payload);

            if ("chat".equals(json.getString("msgType"))) {
                postChatMessage(user, json);
            } else {
                postGroupChatMessage(user, json);
            }

        } catch (Exception e1) {
            Log.error("postMessage failed "  + username + "\n" + payload, e1);
        }

        return ok;
    }

    private void postChatMessage(User user, JSONObject json)
    {
        Log.debug("postChatMessage "  + user + "\n" + json);

        try {
            String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
            JID jid1 = new JID(user.getUsername() + "@" + domain);
            JID jid2 = new JID(json.getString("msgFrom"));

            Message message = new Message();
            message.setFrom(jid1);
            message.setTo(jid2);
            message.setType(Message.Type.chat);
            message.setBody(">" + json.getString("msgBody") + "\n\n" + json.getString("reply"));
            XMPPServer.getInstance().getRoutingTable().routePacket(jid2, message, true);

        } catch (Exception e) {
            Log.error("postChatMessage", e);
        }
    }

    private void postGroupChatMessage(User user, JSONObject json)
    {
        Log.debug("postGroupChatMessage "  + user + "\n" + json);

        try {
            String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
            JID jid1 = new JID(user.getUsername() + "@" + domain);
            JID jid2 = new JID(json.getString("msgFrom"));
            String muc = jid2.getDomain();
            muc = muc.substring(0, muc.indexOf("."));

            MultiUserChatService mucService = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(muc);
            MUCRoom room = mucService.getChatRoom(jid2.getNode());

            Message message = new Message();
            message.setFrom(jid2 + "/" + user.getName());
            //message.setTo(jid2);
            message.setType(Message.Type.groupchat);
            message.setBody("> " + json.getString("msgNick") + " : " + json.getString("msgBody") + "\n\n" + json.getString("reply"));
            room.send(message);

        } catch (Exception e) {
            Log.error("postGroupChatMessage", e);
        }
    }


    //-------------------------------------------------------
    //
    //  Jitsi Meet
    //
    //-------------------------------------------------------

    public boolean inviteToJvb(String username, String jid)
    {
        if (SessionManager.getInstance().getSessions(username).size() > 0)
        {
            String room = username + "-" + System.currentTimeMillis();
            String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
            String mucDomain = JiveGlobals.getProperty( "ofmeet.main.muc", "conference" + "." + domain);

            try {
                JID jid1 = new JID(username + "@" + domain);
                JID jid2 = new JID(jid);

                String confJid = room + "@" + mucDomain;

                Message message1 = new Message();
                message1.setFrom(jid1);
                message1.setTo(jid2);
                Element x1 = message1.addChildElement("x", "jabber:x:conference").addAttribute("jid", confJid);
                x1.addElement("invite").addAttribute("from", jid1.toString());
                XMPPServer.getInstance().getRoutingTable().routePacket(jid2, message1, true);

                Message message2 = new Message();
                message2.setFrom(jid2);
                message2.setTo(jid1);
                Element x2 = message2.addChildElement("x", "jabber:x:conference").addAttribute("jid", confJid).addAttribute("autoaccept", "true");
                x2.addElement("invite").addAttribute("from", jid2.toString());
                XMPPServer.getInstance().getRoutingTable().routePacket(jid1, message2, true);

                return true;

            } catch (Exception e) {
                Log.error("inviteToJvb", e);
            }
        }
        return false;
    }
}