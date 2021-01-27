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
        final String IPADDR = getIpAddress();
        final String DOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        final String MAIN_MUC = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + DOMAIN);

        final String jigasiHomePath = pluginDirectory.getPath() + File.separator + "classes" + File.separator + "jigasi";
        final File props_file = new File(jigasiHomePath + File.separator + "sip-communicator.properties");
        Properties props = new Properties();

        props.load(new FileInputStream(props_file));

        props.setProperty("org.jitsi.jigasi.DEFAULT_JVB_ROOM_NAME", JiveGlobals.getProperty("ofmeet.jigasi.xmpp.room-name", "siptest") + "@" + MAIN_MUC);
        props.setProperty("org.jitsi.jigasi.MUC_SERVICE_ADDRESS", MAIN_MUC);
        props.setProperty("org.jitsi.jigasi.ALLOWED_JID", "ofgasi@" + MAIN_MUC);
        props.setProperty("org.jitsi.jigasi.BREWERY_ENABLED", "true");

        props.setProperty("org.jitsi.jigasi.xmpp.acc.IS_SERVER_OVERRIDDEN", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.SERVER_ADDRESS", IPADDR);
        props.setProperty("org.jitsi.jigasi.xmpp.acc.VIDEO_CALLING_DISABLED", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.JINGLE_NODES_ENABLED", "false");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.AUTO_DISCOVER_STUN", "false");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.IM_DISABLED", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.SERVER_STORED_INFO_DISABLED", "true");
        props.setProperty("org.jitsi.jigasi.xmpp.acc.IS_FILE_TRANSFER_DISABLED", "true");

        props.setProperty("net.java.sip.communicator.impl.protocol.SingleCallInProgressPolicy.enabled", "false");
        props.setProperty("net.java.sip.communicator.impl.neomedia.codec.audio.opus.encoder.COMPLEXITY", "10");
        props.setProperty("org.jitsi.impl.neomedia.transform.csrc.CsrcTransformEngine.DISCARD_CONTRIBUTING_SOURCES", "true");
        props.setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_ENABLED", "false");

        props.setProperty("net.java.sip.communicator.impl.protocol.sip.acc1403273890647", "acc1403273890647");
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
        props.setProperty("net.java.sip.communicator.impl.protocol.jabber.acc-xmpp-1.BOSH_URL_PATTERN", "https://{host}{subdomain}/http-bind?room={roomName}");
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
        final String classPath = "jigasi.jar" + File.pathSeparator + "lib/agafua-syslog-0.4.jar" + File.pathSeparator + "lib/animal-sniffer-annotations-1.17.jar" + File.pathSeparator + "lib/annotations-15.0.jar" + File.pathSeparator + "lib/annotations-4.1.1.4.jar" + File.pathSeparator + "lib/aopalliance-repackaged-2.5.0.jar" + File.pathSeparator + "lib/api-common-1.8.1.jar" + File.pathSeparator + "lib/auto-value-annotations-1.6.6.jar" + File.pathSeparator + "lib/bcpkix-jdk15on-1.54.jar" + File.pathSeparator + "lib/bcprov-jdk15on-1.54.jar" + File.pathSeparator + "lib/callstats-java-sdk-5.2.0.jar" + File.pathSeparator + "lib/cglib-nodep-2.2.jar" + File.pathSeparator + "lib/checker-qual-2.5.2.jar" + File.pathSeparator + "lib/commons-codec-1.9.jar" + File.pathSeparator + "lib/commons-collections4-4.2.jar" + File.pathSeparator + "lib/commons-compress-1.20.jar" + File.pathSeparator + "lib/commons-lang3-3.9.jar" + File.pathSeparator + "lib/commons-logging-1.2.jar" + File.pathSeparator + "lib/concurrentlinkedhashmap-lru-1.0_jdk5.jar" + File.pathSeparator + "lib/conscrypt-openjdk-uber-2.2.1.jar" + File.pathSeparator + "lib/core-2.0.1.jar" + File.pathSeparator + "lib/dbus-java-2.7.jar" + File.pathSeparator + "lib/debug-1.1.1.jar" + File.pathSeparator + "lib/dnsjava-2.1.7.jar" + File.pathSeparator + "lib/dnssecjava-1.1.jar" + File.pathSeparator + "lib/dom4j-1.6.1.jar" + File.pathSeparator + "lib/error_prone_annotations-2.2.0.jar" + File.pathSeparator + "lib/failureaccess-1.0.jar" + File.pathSeparator + "lib/fmj-1.0-20190327.151046-25.jar" + File.pathSeparator + "lib/gax-1.50.1.jar" + File.pathSeparator + "lib/gax-grpc-1.50.1.jar" + File.pathSeparator + "lib/gax-httpjson-0.66.1.jar" + File.pathSeparator + "lib/google-api-client-1.30.4.jar" + File.pathSeparator + "lib/google-api-services-translate-v2-rev20170525-1.30.1.jar" + File.pathSeparator + "lib/google-auth-library-credentials-0.18.0.jar" + File.pathSeparator + "lib/google-auth-library-oauth2-http-0.18.0.jar" + File.pathSeparator + "lib/google-cloud-core-1.91.3.jar" + File.pathSeparator + "lib/google-cloud-core-http-1.91.3.jar" + File.pathSeparator + "lib/google-cloud-speech-1.22.1.jar" + File.pathSeparator + "lib/google-cloud-translate-1.94.1.jar" + File.pathSeparator + "lib/google-http-client-1.32.1.jar" + File.pathSeparator + "lib/google-http-client-appengine-1.32.1.jar" + File.pathSeparator + "lib/google-http-client-jackson2-1.32.1.jar" + File.pathSeparator + "lib/google-oauth-client-1.30.3.jar" + File.pathSeparator + "lib/grpc-alts-1.25.0.jar" + File.pathSeparator + "lib/grpc-api-1.25.0.jar" + File.pathSeparator + "lib/grpc-auth-1.25.0.jar" + File.pathSeparator + "lib/grpc-context-1.25.0.jar" + File.pathSeparator + "lib/grpc-core-1.25.0.jar" + File.pathSeparator + "lib/grpc-grpclb-1.25.0.jar" + File.pathSeparator + "lib/grpc-netty-shaded-1.25.0.jar" + File.pathSeparator + "lib/grpc-protobuf-1.25.0.jar" + File.pathSeparator + "lib/grpc-protobuf-lite-1.25.0.jar" + File.pathSeparator + "lib/grpc-stub-1.25.0.jar" + File.pathSeparator + "lib/gson-2.3.1.jar" + File.pathSeparator + "lib/guava-27.0-jre.jar" + File.pathSeparator + "lib/hamcrest-core-1.3.jar" + File.pathSeparator + "lib/hexdump-0.2.1.jar" + File.pathSeparator + "lib/hk2-api-2.5.0.jar" + File.pathSeparator + "lib/hk2-locator-2.5.0.jar" + File.pathSeparator + "lib/hk2-utils-2.5.0.jar" + File.pathSeparator + "lib/httpclient-4.4.jar" + File.pathSeparator + "lib/httpcore-4.4.jar" + File.pathSeparator + "lib/httpmime-4.4.jar" + File.pathSeparator + "lib/ice4j-2.0.0-20190607.184546-36.jar" + File.pathSeparator + "lib/j2objc-annotations-1.1.jar" + File.pathSeparator + "lib/jackson-annotations-2.9.9.jar" + File.pathSeparator + "lib/jackson-core-2.10.0.jar" + File.pathSeparator + "lib/jackson-databind-2.9.9.jar" + File.pathSeparator + "lib/jackson-module-jaxb-annotations-2.9.9.jar" + File.pathSeparator + "lib/jain-sip-ri-ossonly-1.2.98c7f8c-jitsi-oss1.jar" + File.pathSeparator + "lib/jakarta.annotation-api-1.3.4.jar" + File.pathSeparator + "lib/jakarta.inject-2.5.0.jar" + File.pathSeparator + "lib/jakarta.ws.rs-api-2.1.5.jar" + File.pathSeparator + "lib/java-dogstatsd-client-2.5.jar" + File.pathSeparator + "lib/java-sdp-nist-bridge-1.2.jar" + File.pathSeparator + "lib/javassist-3.22.0-CR2.jar" + File.pathSeparator + "lib/javax.annotation-api-1.3.2.jar" + File.pathSeparator + "lib/javax.servlet-api-3.1.0.jar" + File.pathSeparator + "lib/jbosh-0.9.2.jar" + File.pathSeparator + "lib/jcip-annotations-1.0.jar" + File.pathSeparator + "lib/jcl-core-2.8.jar" + File.pathSeparator + "lib/jersey-client-2.29.jar" + File.pathSeparator + "lib/jersey-common-2.29.jar" + File.pathSeparator + "lib/jersey-container-jetty-http-2.29.jar" + File.pathSeparator + "lib/jersey-container-servlet-2.29.jar" + File.pathSeparator + "lib/jersey-container-servlet-core-2.29.jar" + File.pathSeparator + "lib/jersey-entity-filtering-2.29.jar" + File.pathSeparator + "lib/jersey-hk2-2.29.jar" + File.pathSeparator + "lib/jersey-media-jaxb-2.29.jar" + File.pathSeparator + "lib/jersey-media-json-jackson-2.29.jar" + File.pathSeparator + "lib/jersey-server-2.29.jar" + File.pathSeparator + "lib/jetty-client-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-continuation-9.4.12.v20180830.jar" + File.pathSeparator + "lib/jetty-http-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-io-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-proxy-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-security-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-server-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-servlet-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-util-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jetty-webapp-7.0.1.v20091125.jar" + File.pathSeparator + "lib/jetty-xml-9.4.15.v20190215.jar" + File.pathSeparator + "lib/jicoco-1.1-22-gbec9167.jar" + File.pathSeparator + "lib/jitsi-android-osgi-1.0-20190327.160432-3.jar" + File.pathSeparator + "lib/jitsi-argdelegation-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-certificate-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-configuration-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-contactlist-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-credentialsstorage-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-desktoputil-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-dns-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-dnsservice-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-fileaccess-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-globaldisplaydetails-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-hid-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-lgpl-dependencies-1.1-20190327.160813-5.jar" + File.pathSeparator + "lib/jitsi-muc-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-neomedia-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-netaddr-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-notification-service-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-packetlogging-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-protocol-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-protocol-jabber-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-protocol-media-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-protocol-sip-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-reconnect-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-resourcemanager-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-srtp-1.0-24-g6823dfa.jar" + File.pathSeparator + "lib/jitsi-stats-1.0-5-g2a92c0e.jar" + File.pathSeparator + "lib/jitsi-sysactivity-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-systray-service-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-ui-service-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-util-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-utils-1.0-39-gd481c98.jar" + File.pathSeparator + "lib/jitsi-version-2.13.1387ed6a7.jar" + File.pathSeparator + "lib/jitsi-webrtcvadwrapper-1.0-20200414.144800-2.jar" + File.pathSeparator + "lib/jitsi-xmpp-extensions-1.0-17-gcf66bcd.jar" + File.pathSeparator + "lib/jna-4.1.0.jar" + File.pathSeparator + "lib/jnsapi-0.0.3-jitsi-smack4.2-3.jar" + File.pathSeparator + "lib/jose4j-0.5.1.jar" + File.pathSeparator + "lib/json-20180130.jar" + File.pathSeparator + "lib/json-simple-1.1.1.jar" + File.pathSeparator + "lib/jsr305-3.0.2.jar" + File.pathSeparator + "lib/junit-4.12.jar" + File.pathSeparator + "lib/jxmpp-core-0.6.2.jar" + File.pathSeparator + "lib/jxmpp-jid-0.6.2.jar" + File.pathSeparator + "lib/jxmpp-util-cache-0.6.2.jar" + File.pathSeparator + "lib/laf-widget-4.0.jar" + File.pathSeparator + "lib/libidn-1.15.jar" + File.pathSeparator + "lib/libjitsi-1.0-45-g738e2573.jar" + File.pathSeparator + "lib/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar" + File.pathSeparator + "lib/log4j-api-2.3.jar" + File.pathSeparator + "lib/log4j-core-2.3.jar" + File.pathSeparator + "lib/object-cloner-0.1.jar" + File.pathSeparator + "lib/objenesis-2.1.jar" + File.pathSeparator + "lib/okhttp-3.9.1.jar" + File.pathSeparator + "lib/okio-1.13.0.jar" + File.pathSeparator + "lib/opencensus-api-0.24.0.jar" + File.pathSeparator + "lib/opencensus-contrib-grpc-metrics-0.21.0.jar" + File.pathSeparator + "lib/opencensus-contrib-http-util-0.24.0.jar" + File.pathSeparator + "lib/orange-extensions-1.3.0.jar" + File.pathSeparator + "lib/org.apache.felix.framework-4.4.0.jar" + File.pathSeparator + "lib/org.apache.felix.main-4.4.0.jar" + File.pathSeparator + "lib/org.osgi.core-4.3.1.jar" + File.pathSeparator + "lib/osgi-resource-locator-1.0.3.jar" + File.pathSeparator + "lib/perfmark-api-0.19.0.jar" + File.pathSeparator + "lib/proto-google-cloud-speech-v1-1.22.1.jar" + File.pathSeparator + "lib/proto-google-cloud-speech-v1p1beta1-0.75.1.jar" + File.pathSeparator + "lib/proto-google-cloud-translate-v3-1.0.1.jar" + File.pathSeparator + "lib/proto-google-cloud-translate-v3beta1-0.77.1.jar" + File.pathSeparator + "lib/proto-google-common-protos-1.17.0.jar" + File.pathSeparator + "lib/proto-google-iam-v1-0.13.0.jar" + File.pathSeparator + "lib/protobuf-java-3.10.0.jar" + File.pathSeparator + "lib/protobuf-java-util-3.10.0.jar" + File.pathSeparator + "lib/reflections-0.9.11.jar" + File.pathSeparator + "lib/sdes4j-1.1.5.jar" + File.pathSeparator + "lib/sdp-api-1.0.jar" + File.pathSeparator + "lib/sentry-1.7.30.jar" + File.pathSeparator + "lib/sip-api-1.2-1.2.jar" + File.pathSeparator + "lib/slf4j-api-1.7.30.jar" + File.pathSeparator + "lib/slf4j-jdk14-1.7.30.jar" + File.pathSeparator + "lib/slf4j-simple-1.6.1.jar" + File.pathSeparator + "lib/smack-bosh-4.2.4-4fa73bd.jar" + File.pathSeparator + "lib/smack-core-4.2.4-4fa73bd.jar" + File.pathSeparator + "lib/smack-debug-4.2.4-47d17fc.jar" + File.pathSeparator + "lib/smack-experimental-4.2.4-47d17fc.jar" + File.pathSeparator + "lib/smack-extensions-4.2.4-4fa73bd.jar" + File.pathSeparator + "lib/smack-im-4.2.4-4fa73bd.jar" + File.pathSeparator + "lib/smack-java7-4.2.4-47d17fc.jar" + File.pathSeparator + "lib/smack-legacy-4.2.4-47d17fc.jar" + File.pathSeparator + "lib/smack-resolver-javax-4.2.4-47d17fc.jar" + File.pathSeparator + "lib/smack-sasl-javax-4.2.4-47d17fc.jar" + File.pathSeparator + "lib/smack-tcp-4.2.4-4fa73bd.jar" + File.pathSeparator + "lib/spotbugs-annotations-4.0.0-RC1.jar" + File.pathSeparator + "lib/threetenbp-1.4.0.jar" + File.pathSeparator + "lib/tinder-1.3.0.jar" + File.pathSeparator + "lib/unix-0.5.1.jar" + File.pathSeparator + "lib/validation-api-2.0.1.Final.jar" + File.pathSeparator + "lib/vorbis-java-core-0.8.jar" + File.pathSeparator + "lib/websocket-api-9.4.15.v20190215.jar" + File.pathSeparator + "lib/websocket-client-9.4.15.v20190215.jar" + File.pathSeparator + "lib/websocket-common-9.4.15.v20190215.jar" + File.pathSeparator + "lib/weupnp-0.1.4.jar" + File.pathSeparator + "lib/xml-apis-1.0.b2.jar" + File.pathSeparator + "lib/xmlpull-1.1.3.4a.jar" + File.pathSeparator + "lib/xpp3-1.1.4c.jar" + File.pathSeparator + "lib/zrtp4j-light-4.1.1.jar";
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
        Log.info("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.info("onProcessQuit " + code);
        System.setProperty("ofmeet.jigasi.started", "false");
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.info(line);
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
