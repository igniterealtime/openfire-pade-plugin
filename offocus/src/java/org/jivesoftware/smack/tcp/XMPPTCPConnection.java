/**
 *
 * Copyright 2003-2007 Jive Software.
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
package org.jivesoftware.smack.tcp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.security.cert.Certificate;

import javax.net.ssl.*;
import javax.security.auth.callback.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.util.*;

import org.jxmpp.jid.*;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import org.jivesoftware.openfire.*;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.net.VirtualConnection;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.auth.AuthToken;
import org.jivesoftware.openfire.auth.AuthFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmpp.packet.JID;
import org.dom4j.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.EventSource;
import org.eclipse.jetty.servlets.EventSourceServlet;

import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;


/**
 * Creates a socket connection to an XMPP server. This is the default connection
 * to an XMPP server and is specified in the XMPP Core (RFC 6120).
 *
 * @see XMPPConnection
 * @author Matt Tucker
 */
public class XMPPTCPConnection extends AbstractXMPPConnection
{
    private static Logger Log = LoggerFactory.getLogger( "XMPPTCPConnection" );

    private boolean reconnect = false;
    private LocalClientSession session;
    private SmackConnection smackConnection;
    private final XMPPTCPConnectionConfiguration config;

    public static void setUseStreamManagementResumptionDefault(boolean flag)
    {

    }

    public static void setUseStreamManagementDefault(boolean flag)
    {

    }

    /**
     * Creates a new XMPP connection over TCP (optionally using proxies).
     * <p>
     * Note that XMPPTCPConnection constructors do not establish a connection to the server
     * and you must call {@link #connect()}.
     * </p>
     *
     * @param config the connection configuration.
     */
    public XMPPTCPConnection(XMPPTCPConnectionConfiguration config) {
        super(config);
        this.config = config;
    }

    /**
     * Creates a new XMPP connection over TCP.
     * <p>
     * Note that {@code jid} must be the bare JID, e.g. "user@example.org". More fine-grained control over the
     * connection settings is available using the {@link #XMPPTCPConnection(XMPPTCPConnectionConfiguration)}
     * constructor.
     * </p>
     *
     * @param jid the bare JID used by the client.
     * @param password the password or authentication token.
     * @throws XmppStringprepException
     */
    public XMPPTCPConnection(CharSequence jid, String password) throws XmppStringprepException {
        this(XmppStringUtils.parseLocalpart(jid.toString()), password, XmppStringUtils.parseDomain(jid.toString()));
    }

    /**
     * Creates a new XMPP connection over TCP.
     * <p>
     * This is the simplest constructor for connecting to an XMPP server. Alternatively,
     * you can get fine-grained control over connection settings using the
     * {@link #XMPPTCPConnection(XMPPTCPConnectionConfiguration)} constructor.
     * </p>
     * @param username
     * @param password
     * @param serviceName
     * @throws XmppStringprepException
     */
    public XMPPTCPConnection(CharSequence username, String password, String serviceName) throws XmppStringprepException {
        this(XMPPTCPConnectionConfiguration.builder().setUsernameAndPassword(username, password).setXmppDomain(
                                        JidCreate.domainBareFrom(serviceName)).build());
    }

    @Override
    protected void connectInternal() {
        Log.info("connectInternal " + config.getXMPPServiceDomain());

        connected = true;
        saslFeatureReceived.reportSuccess();
        tlsHandled.reportSuccess();

        streamId = "ofchat" + new Random(new Date().getTime()).nextInt();
        smackConnection = new SmackConnection(streamId, this);

        if (reconnect) {
            //notifyReconnection();
        }
    }

    @Override
    protected void shutdown() {
        Log.info("shutdown " + user);

        try {
            JID userJid = new JID(user.toString());

            session = (LocalClientSession) SessionManager.getInstance().getSession(userJid);

            if (session != null)
            {
                session.close();
                SessionManager.getInstance().removeSession(session);
            }

        } catch (Exception e) {
            Log.error("shutdown", e);
        }

        user = null;
        authenticated = false;
        reconnect = true;
    }

    @Override
    public boolean isSecureConnection() {
        return false;
    }

    @Override
    public boolean isUsingCompression() {
        return false;
    }

    @Override
    protected void loginInternal(String username, String password, Resourcepart resource) throws XMPPException
    {
        Log.info("loginInternal " + username + " " + password + " " + resource );

        try {
            AuthToken authToken = null;

            if (username == null || password == null || "".equals(username) || "".equals(password))
            {
                authToken = new AuthToken(resource.toString(), true);

            } else {
                username = username.toLowerCase().trim();
                user = getUserJid(username);
                JID userJid = XMPPServer.getInstance().createJID(username, resource.toString());

                session = (LocalClientSession) SessionManager.getInstance().getSession(userJid);

                if (session != null)
                {
                    session.close();
                    SessionManager.getInstance().removeSession(session);
                }


                try {
                    authToken = AuthFactory.authenticate( username, password );

                } catch ( UnauthorizedException e ) {
                    authToken = new AuthToken(resource.toString(), true);
                }
            }

            session = SessionManager.getInstance().createClientSession( smackConnection, (Locale) null );
            smackConnection.setRouter( new SessionPacketRouter( session ) );
            session.setAuthToken(authToken, resource.toString());
            authenticated = true;

            afterSuccessfulLogin(false);

        } catch (Exception e) {
            Log.error("loginInternal", e);
        }
    }

    private void sendPacket(TopLevelStreamElement stanza)
    {
        sendPacket(stanza.toXML().toString());
        firePacketSendingListeners((Stanza) stanza);
    }

    public void sendPacket(String data)
    {
        try {
            Log.debug("sendPacket " + data );
            smackConnection.getRouter().route(DocumentHelper.parseText(data).getRootElement());

        } catch ( Exception e ) {
            Log.error( "An error occurred while attempting to route the packet : \n" + data);
        }
    }

    @Override
    public void sendNonza(Nonza element) {
        TopLevelStreamElement stanza = (TopLevelStreamElement) element;
        sendPacket(stanza);
    }

    @Override
    protected void sendStanzaInternal(Stanza packet) {
        TopLevelStreamElement stanza = (TopLevelStreamElement) packet;
        sendPacket(stanza);
    }

    @Override
    public void processStanza(Stanza packet) {
        invokeStanzaCollectorsAndNotifyRecvListeners(packet);
    }

    public void enableStreamFeature(ExtensionElement streamFeature) {
        addStreamFeature(streamFeature);
    }


    // -------------------------------------------------------
    //
    // Common
    //
    // -------------------------------------------------------


    private EntityFullJid getUserJid(String username)
    {
        try {
            return JidCreate.entityFullFrom(username + "@" + config.getXMPPServiceDomain() + "/" + config.getResource());
        }
        catch (XmppStringprepException e) {
            throw new IllegalStateException(e);
        }
    }

    public void handleParser(String xml)
    {
        Stanza stanza = null;

        try {
            stanza = PacketParserUtils.parseStanza(xml);
        }
        catch (Exception e) {
            Log.error("handleParser", e);
        }

        if (stanza != null) {
            invokeStanzaCollectorsAndNotifyRecvListeners(stanza);
        }
    }

    // -------------------------------------------------------
    //
    // SmackConnection
    //
    // -------------------------------------------------------

    public class SmackConnection extends VirtualConnection
    {
        private SessionPacketRouter router;
        private String remoteAddr;
        private String hostName;
        private LocalClientSession session;
        private boolean isSecure = false;
        private XMPPTCPConnection connection;

        public SmackConnection(String hostName, XMPPTCPConnection connection)
        {
            this.remoteAddr = hostName;
            this.hostName = hostName;
            this.connection = connection;
        }

        public void setConnection(XMPPTCPConnection connection) {
            this.connection = connection;
        }

        public boolean isSecure() {
            return isSecure;
        }

        public void setSecure(boolean isSecure) {
            this.isSecure = isSecure;
        }

        public SessionPacketRouter getRouter()
        {
            return router;
        }

        public void setRouter(SessionPacketRouter router)
        {
            this.router = router;
        }

        public void closeVirtualConnection()
        {
            Log.info("SmackConnection - close ");

            if (this.connection!= null) this.connection.shutdown();
        }

        public byte[] getAddress() {
            return remoteAddr.getBytes();
        }

        public String getHostAddress() {
            return remoteAddr;
        }

        public String getHostName()  {
            return ( hostName != null ) ? hostName : "0.0.0.0";
        }

        public void systemShutdown() {

        }

        public void deliver(org.xmpp.packet.Packet packet) throws UnauthorizedException
        {
            deliverRawText(packet.toXML());
        }

        public void deliverRawText(String text)
        {
            int pos = text.indexOf("<message ");

            if (pos > -1)
            {
                text = text.substring(0, pos + 9) + "xmlns=\"jabber:client\"" + text.substring(pos + 8);
            }

            pos = text.indexOf("<presence ");

            if (pos > -1)
            {
                text = text.substring(0, pos + 10) + "xmlns=\"jabber:client\"" + text.substring(pos + 9);
            }

            Log.debug("SmackConnection - deliverRawText\n" + text);
            connection.handleParser(text);
        }

        @Override
        public org.jivesoftware.openfire.spi.ConnectionConfiguration getConfiguration()
        {
            // TODO Here we run into an issue with the ConnectionConfiguration introduced in Openfire 4:
            //      it is not extensible in the sense that unforeseen connection types can be added.
            //      For now, null is returned, as this object is likely to be unused (its lifecycle is
            //      not managed by a ConnectionListener instance).
            return null;
        }

        public Certificate[] getPeerCertificates() {
            return null;
        }

    }
}