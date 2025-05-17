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

import org.jivesoftware.openfire.Connection;
import org.jivesoftware.openfire.component.ExternalComponentManager;
import org.jivesoftware.openfire.component.ExternalComponentConfiguration;
import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mxro.process.*;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.util.*;
import org.jitsi.util.OSUtils;
import java.util.Properties;
import org.xmpp.packet.*;

/**
 * A wrapper object for the Jitsi Component Focus (jicofo) component.
 *
 */
public class JitsiJicofoWrapper implements ProcessListener
{
    private static final Logger Log = LoggerFactory.getLogger( JitsiJicofoWrapper.class );
    private XProcess jicofoThread = null;

    public synchronized void initialize( File pluginDirectory) throws Exception
    {
        Log.info( "Initializing Jitsi Focus Component (jicofo)...");
        System.setProperty("ofmeet.jicofo.started", "false");

        final String jicofoSubdomain = "focus";
        final String jicofoPort = JiveGlobals.getProperty( "ofmeet.jicofo.rest.port", "8888");			
        final ConnectionManagerImpl manager = (ConnectionManagerImpl) XMPPServer.getInstance().getConnectionManager();
        final ConnectionConfiguration plaintextConfiguration  = manager.getListener( ConnectionType.COMPONENT, false ).generateConnectionConfiguration();
        final ConnectionConfiguration clientConfiguration = manager.getListener( ConnectionType.SOCKET_C2S, false ).generateConnectionConfiguration();	

        final String MAIN_MUC = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + XMPPServer.getInstance().getServerInfo().getXMPPDomain());
		final MultiUserChatService mucService = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference");
	    final String roomName = "ofmeet";		

        String defaultSecret = ExternalComponentManager.getDefaultSecret();

        if ( defaultSecret == null || defaultSecret.trim().isEmpty() )
        {
            defaultSecret = StringUtils.randomString(40);
            ExternalComponentManager.setDefaultSecret( defaultSecret );
        }
        else {
            final ExternalComponentConfiguration configuration = new ExternalComponentConfiguration( jicofoSubdomain, false, ExternalComponentConfiguration.Permission.allowed, defaultSecret );

            try
            {
                ExternalComponentManager.allowAccess( configuration );
                Log.info( "allowed external component access", "configuration = " + configuration );
            }
            catch ( Exception e )
            {
                Log.error("not allowed external component access", e);
            }
        }

        final OFMeetConfig config = new OFMeetConfig();
        final String port = String.valueOf(plaintextConfiguration.getPort());

        final String parameters =
            " --host=" + XMPPServer.getInstance().getServerInfo().getHostname() +
            " --port=" + port +
            " --domain=" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() +
            " --secret=" + defaultSecret +
            " --user_domain=" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() +
            " --user_name=" + jicofoSubdomain +
            " --user_password=" + config.getFocusPassword();

        final String jicofoHomePath = pluginDirectory.getPath() + File.separator + "classes" + File.separator + "jicofo";
        final File props_file = new File(jicofoHomePath + File.separator + "config" + File.separator + "sip-communicator.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(props_file));
		
		if (config.getJigasiSipEnabled() && config.getJigasiSipUserId() != null)
		{
			props.setProperty("org.jitsi.jicofo.jigasi.BREWERY", "ofgasi@" + MAIN_MUC);
		}				

        props.setProperty( "org.jitsi.jicofo.BRIDGE_MUC", roomName + "@" + MAIN_MUC);
        props.setProperty( "org.jitsi.jicofo.ALWAYS_TRUST_MODE_ENABLED", "true" );
		props.setProperty( "org.jitsi.jicofo.FOCUS_USER_DOMAIN", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
        props.setProperty( "org.jitsi.jicofo.PING_INTERVAL", "-1" );
        props.setProperty( "org.jitsi.jicofo.SERVICE_REDISCOVERY_INTERVAL", "60000" );
        props.setProperty( "org.jitsi.jicofo.DISABLE_AUTO_OWNER", Boolean.toString( !JiveGlobals.getBooleanProperty( "ofmeet.conference.auto-moderator", true ) ) );

        props.setProperty( "org.jitsi.jicofo.ENABLE_H264", Boolean.toString( !JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.vp9", false ) && !JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.av1", true ) ) );
        props.setProperty( "org.jitsi.jicofo.ENABLE_VP8", Boolean.toString( !JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.vp9", false ) && !JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.av1", true ) ) );
        props.setProperty( "org.jitsi.jicofo.ENABLE_VP9", Boolean.toString( JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.vp9", false ) && !JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.av1", true ) ) );
        props.setProperty( "org.jitsi.jicofo.ENABLE_AV1", Boolean.toString( JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.av1", true ) && !JiveGlobals.getBooleanProperty( "ofmeet.jicofo.force.vp9", false ) ) );
		
        Log.debug("sip-communicator.properties");

        for (Object key: props.keySet()) {
            Log.debug(key + ": " + props.getProperty(key.toString()));
        }

        props.store(new FileOutputStream(props_file), "Jitsi Colibri Focus");
		

        List<String> lines = Arrays.asList(
            "jicofo {",
            "    sctp {",
            "        # Whether SCTP data channels are enabled",
            "        enabled = " + (JiveGlobals.getBooleanProperty( "ofmeet.bridge.ws.channel", OSUtils.IS_WINDOWS) ? "false" : "true"),
            "    }",
            "    octo  {",
            "        enabled = " + (ClusterManager.isClusteringEnabled() ? "true" : "false"),
            "        id = " + (ClusterManager.isClusteringEnabled() ? JiveGlobals.getXMLProperty("ofmeet.octo_id", "1") : "1"),					
            "    }",
            "    bridge  {",
            "        selection-strategy = RegionBasedBridgeSelectionStrategy ",
            "        brewery-jid: \"JvbBrewery@internal.auth." + XMPPServer.getInstance().getServerInfo().getXMPPDomain() + "\"",
            "    }",
            "    rest   {",
            "        port = " + jicofoPort,
            "    }",			
            "    xmpp {",
            "      client {",
			"		 enabled = true",	
			"		 username = " + jicofoSubdomain,				
			"		 password = \"" + config.getFocusPassword() + "\"",				
			"		 port = 5222",
			"		 hostname = " +  XMPPServer.getInstance().getServerInfo().getHostname(),
			"		 domain = " +  XMPPServer.getInstance().getServerInfo().getXMPPDomain(),			
			"		 xmpp-domain = " +  XMPPServer.getInstance().getServerInfo().getXMPPDomain(),				
			"		 conference-muc-jid = " + MAIN_MUC,			
			"		 disable-certificate-verification = true",
            "        client-proxy = focus." + XMPPServer.getInstance().getServerInfo().getXMPPDomain(),
            "        use-tls = " + (clientConfiguration.getTlsPolicy() == Connection.TLSPolicy.required ? "true" : "false"),
            "       }",				
            "    }",			
            "}"
        );

        Path configFile = Paths.get(jicofoHomePath + File.separator + "application.conf");
        try
        {
            Files.write(configFile, lines, Charset.forName("UTF-8"));
        } catch (Exception e) {
            Log.error("createConfigFile error", e);
        }

        final String javaHome = System.getProperty("java.home");
        String defaultOptions = "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp";
        String javaExec = javaHome + File.separator + "bin" + File.separator + "java";

        if (OSUtils.IS_WINDOWS)
        {
            javaExec = javaExec + ".exe";
            defaultOptions = "";
        }

        String customOptions = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.focus.jvm.customOptions", defaultOptions);
        if(!customOptions.isEmpty() && !customOptions.endsWith(" ")) customOptions += " ";
        final String cmdLine = javaExec + " " + customOptions + "-Dconfig.file=" + configFile + " -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=" + jicofoHomePath + " -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=config -Djava.util.logging.config.file=./logging.properties -Djdk.tls.ephemeralDHKeySize=2048 -cp ./jicofo-1.1-SNAPSHOT.jar" + File.pathSeparator + "./jicofo-1.1-SNAPSHOT-jar-with-dependencies.jar org.jitsi.jicofo.Main" + parameters;
        jicofoThread = Spawn.startProcess(cmdLine, new File(jicofoHomePath), this);

		// create ofmeet muc room if it does not exist. needed for race condition when jicofo is a tls connection
		
		if (mucService.getChatRoom(roomName) == null)
		{
			try {
				MUCRoom mucRoom = mucService.getChatRoom(roomName, new JID("admin@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain()));
				mucRoom.setPersistent(false);
				mucRoom.setPublicRoom(false);
				mucRoom.unlock(mucRoom.getRole());
			} catch (Exception e) {
				Log.error("Cannot create MUC room", e);
			}	
		}			
		
        Log.info( "Successfully initialized Jitsi Focus Component (jicofo).\n"  + cmdLine);
        Log.debug( "Jicofo config.\n" + String.join("\n", lines));
    }

    public synchronized void destroy() throws Exception
    {
        Log.debug( "Destroying Jitsi Focus process..." );

        if (jicofoThread != null) jicofoThread.destory();

        Log.debug( "Destroyed Jitsi Focus process..." );
    }

    public void onOutputLine(final String line)
    {
        Log.debug("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.debug("onProcessQuit " + code);
        System.setProperty("ofmeet.jicofo.started", "false");
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.debug(line);
        if (line.contains("Added new videobridge:")) System.setProperty("ofmeet.jicofo.started", "true");
    }

    public void onError(final Throwable t)
    {
        Log.error("Thread error", t);
    }
}
