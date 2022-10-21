/*
 * Copyright (c) 2017 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.ofmeet;

import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mxro.process.*;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jitsi.util.OSUtils;
import java.util.Properties;
import org.jitsi.videobridge.openfire.PluginImpl;

/**
 * A wrapper object for the Jitsi Gateway to SIP (jigasi) component.
 *
 */
public class JitsiJigasiWrapper implements ProcessListener
{
    private static final Logger Log = LoggerFactory.getLogger( JitsiJigasiWrapper.class );
    private XProcess jigasiThread = null;

    public synchronized void initialize( File pluginDirectory) throws Exception
    {
        Log.info( "Initializing Jitsi Sip Gateway Component (jigasi)...");
        System.setProperty("ofmeet.jigasi.started", "false");

        final OFMeetConfig config = new OFMeetConfig();
        final String IPADDR = JiveGlobals.getProperty( PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME, getIpAddress() );
        final String DOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        final String MAIN_MUC = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + DOMAIN);
        final String PUBLIC_PORT = JiveGlobals.getProperty( "httpbind.port.secure", "7443");
        final String PLAIN_PORT = JiveGlobals.getProperty( "httpbind.port.plain", "7070");		
		final String HOSTNAME = XMPPServer.getInstance().getServerInfo().getHostname();

        final String jigasiHomePath = pluginDirectory.getPath() + File.separator + "classes" + File.separator + "jigasi";
        final File props_file = new File(jigasiHomePath + File.separator + "sip-communicator.properties");
        Properties props = new Properties();

        props.load(new FileInputStream(props_file));

        props.setProperty("org.jitsi.jigasi.DEFAULT_JVB_ROOM_NAME", JiveGlobals.getProperty("ofmeet.jigasi.xmpp.room-name", "siptest") + "@" + MAIN_MUC);
        props.setProperty("org.jitsi.jigasi.MUC_SERVICE_ADDRESS", MAIN_MUC);
        props.setProperty("org.jitsi.jigasi.BREWERY_ENABLED", "true");

        props.setProperty("org.jitsi.jigasi.xmpp.acc.ANONYMOUS_AUTH", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.IS_SERVER_OVERRIDDEN", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.SERVER_ADDRESS", IPADDR);
        props.setProperty("org.jitsi.jigasi.xmpp.acc.VIDEO_CALLING_DISABLED", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.JINGLE_NODES_ENABLED", "false");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.AUTO_DISCOVER_STUN", "false");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.IM_DISABLED", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.SERVER_STORED_INFO_DISABLED", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.IS_FILE_TRANSFER_DISABLED", "true");

        props.setProperty("net.java.sip.communicator.service.gui.ALWAYS_TRUST_MODE_ENABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.SingleCallInProgressPolicy.enabled", "false");
        props.setProperty("net.java.sip.communicator.impl.neomedia.codec.audio.opus.encoder.COMPLEXITY", "10");
        props.setProperty("org.jitsi.impl.neomedia.transform.csrc.CsrcTransformEngine.DISCARD_CONTRIBUTING_SOURCES", "true");
        props.setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_ENABLED", "false");

        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647", "acc1403273890647");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.JITSI_MEET_ROOM_HEADER_NAME", config.jigasiSipHeaderRoomName.get());				
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ACCOUNT_UID", "SIP:" + config.jigasiSipUserId.get());
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PASSWORD", Base64.getEncoder().encodeToString( config.jigasiSipPassword.get().getBytes() ) );
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROTOCOL_NAME", "SIP");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.SERVER_ADDRESS", config.jigasiSipServerAddress.get() );
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.USER_ID", config.jigasiSipUserId.get() );
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PREFERRED_TRANSPORT", config.jigasiSipTransport.get());
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROTOCOL_NAME", "SIP");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROXY_AUTO_CONFIG", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROXY_ADDRESS", config.jigasiProxyServer.get());
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROXY_PORT", config.jigasiProxyPort.get());
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.KEEP_ALIVE_INTERVAL", "25");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.KEEP_ALIVE_METHOD", "OPTIONS");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.VOICEMAIL_ENABLED", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.AMR-WB/16000", "750");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.G722/8000", "700");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.GSM/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.H263-1998/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.H264/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.PCMA/8000", "600");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.PCMU/8000", "650");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.SILK/12000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.SILK/16000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.SILK/24000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.SILK/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.VP8/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.iLBC/8000", "10");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.opus/48000", "1000");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.red/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.speex/16000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.speex/32000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.speex/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.telephone-event/8000", "1");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.Encodings.ulpfec/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.OVERRIDE_ENCODINGS", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.DEFAULT_ENCRYPTION", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.SAVP_OPTION", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.DEFAULT_ENCRYPTION", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.DEFAULT_SIPZRTP_ATTRIBUTE", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ENCRYPTION_PROTOCOL.ZRTP", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ENCRYPTION_PROTOCOL.SDES", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ENCRYPTION_PROTOCOL.DTLS-SRTP", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ENCRYPTION_PROTOCOL_STATUS.ZRTP", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ENCRYPTION_PROTOCOL_STATUS.SDES", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ENCRYPTION_PROTOCOL_STATUS.DTLS-SRTP", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647.DOMAIN_BASE", DOMAIN);

        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1", "acc-xmpp-1");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.ACCOUNT_UID", "Jabber:" + config.jigasiXmppUserId.get() + "@" + DOMAIN);
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.USER_ID", config.jigasiXmppUserId.get() + "@" + DOMAIN);
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IS_SERVER_OVERRIDDEN", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.SERVER_ADDRESS", IPADDR);
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.SERVER_PORT", "5222");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.ALLOW_NON_SECURE", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.PASSWORD", Base64.getEncoder().encodeToString( config.jigasiXmppPassword.get().getBytes() ));
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.AUTO_GENERATE_RESOURCE", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.RESOURCE_PRIORITY", "30");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.KEEP_ALIVE_METHOD", "XEP-0199");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.KEEP_ALIVE_INTERVAL", "30");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.CALLING_DISABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.JINGLE_NODES_ENABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IS_CARBON_DISABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.DEFAULT_ENCRYPTION", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IS_USE_ICE", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IS_ACCOUNT_DISABLED", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IS_PREFERRED_PROTOCOL", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.AUTO_DISCOVER_JINGLE_NODES", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.PROTOCOL", "Jabber");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IS_USE_UPNP", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IM_DISABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.VIDEO_CALLING_DISABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.AUTO_DISCOVER_STUN", "false");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.SERVER_STORED_INFO_DISABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.IS_FILE_TRANSFER_DISABLED", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.USE_DEFAULT_STUN_SERVER", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.ENCRYPTION_PROTOCOL.DTLS-SRTP", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.ENCRYPTION_PROTOCOL_STATUS.DTLS-SRTP", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.OVERRIDE_ENCODINGS", "true");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.G722/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.GSM/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.H263-1998/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.H264/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.PCMA/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.PCMU/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.SILK/12000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.SILK/16000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.SILK/24000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.SILK/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.VP8/90000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.iLBC/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.opus/48000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.speex/16000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.speex/32000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.Encodings.speex/8000", "0");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.BREWERY", "ofgasi@" + MAIN_MUC);
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.BOSH_URL_PATTERN", "http://" + HOSTNAME + ":" + PLAIN_PORT + "/http-bind?room={roomName}");
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.DOMAIN_BASE", DOMAIN);

        Log.debug("sip-communicator.properties");

        for (Object key: props.keySet()) {
            Log.debug(key + ": " + props.getProperty(key.toString()));
        }

        props.store(new FileOutputStream(props_file), "Jitsi Sip Gateway");

        final String javaHome = System.getProperty("java.home");
        String defaultOptions = "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp";
        String javaExec = javaHome + File.separator + "bin" + File.separator + "java";

        if (OSUtils.IS_WINDOWS)
        {
            javaExec = javaExec + ".exe";
            defaultOptions = "";
        }
        final String classPath = "jigasi.jar" + File.pathSeparator + "lib/*";
        final String customOptions = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.jigasi.jvm.customOptions", defaultOptions);
        final String cmdLine = javaExec + " " + customOptions + " -Dlog4j.configurationFile=log4j2.xml -Djava.util.logging.config.file=./logging.properties -Djdk.tls.ephemeralDHKeySize=2048 -cp " + classPath + " org.jitsi.jigasi.Main";
        jigasiThread = Spawn.startProcess(cmdLine, new File(jigasiHomePath), this);

        Log.info( "Successfully initialized Jitsi Sip Gateway Component (jigasi).\n"  + cmdLine);
    }

    public synchronized void destroy() throws Exception
    {
        Log.debug( "Destroying Jitsi Sip Gateway process..." );

        if (jigasiThread != null) jigasiThread.destory();

        Log.debug( "Destroyed Jitsi Sip Gateway process..." );
    }

    public void onOutputLine(final String line)
    {
        Log.debug("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.debug("onProcessQuit " + code);
        System.setProperty("ofmeet.jigasi.started", "false");
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.debug(line);
        if (line.contains("newState=RegistrationState=Registered")) System.setProperty("ofmeet.jigasi.started", "true");
    }

    public void onError(final Throwable t)
    {
        Log.error("Thread error", t);
    }

    public String getIpAddress()
    {
        String ourHostname = XMPPServer.getInstance().getServerInfo().getHostname();
        String ourIpAddress = "127.0.0.1";

        try {
            ourIpAddress = InetAddress.getByName(ourHostname).getHostAddress();
        } catch (Exception e) {

        }

        return ourIpAddress;
    }
}
