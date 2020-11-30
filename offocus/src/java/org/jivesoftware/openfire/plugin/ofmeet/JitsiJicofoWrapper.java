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

import org.jivesoftware.openfire.component.ExternalComponentManager;
import org.jivesoftware.openfire.component.ExternalComponentConfiguration;
import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mxro.process.*;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;

import java.io.*;
import org.jitsi.util.OSUtils;
import java.util.Properties;

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

        final String jicofoSubdomain = "focus";
        final ConnectionType connectionType = ConnectionType.COMPONENT;
        final ConnectionManagerImpl manager = (ConnectionManagerImpl) XMPPServer.getInstance().getConnectionManager();
        final ConnectionConfiguration plaintextConfiguration  = manager.getListener( connectionType, false ).generateConnectionConfiguration();

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
        final String MAIN_MUC = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + XMPPServer.getInstance().getServerInfo().getXMPPDomain());

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
        props.setProperty("org.jitsi.jicofo.BRIDGE_MUC", "ofmeet@" + MAIN_MUC);
        props.setProperty( "org.jitsi.jicofo.ALWAYS_TRUST_MODE_ENABLED", "true" );
        props.setProperty( "org.jitsi.jicofo.PING_INTERVAL", "-1" );
        props.setProperty( "org.jitsi.jicofo.SERVICE_REDISCOVERY_INTERVAL", "-1" );
        props.store(new FileOutputStream(props_file), "Jitsi Colibri Focus");

        final String javaHome = System.getProperty("java.home");
        String defaultOptions = "-Xmx3072m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp";
        String javaExec = javaHome + File.separator + "bin" + File.separator + "java";

        if (OSUtils.IS_WINDOWS)
        {
            javaExec = javaExec + ".exe";
            defaultOptions = "";
        }

        final String customOptions = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.focus.jvm.customOptions", defaultOptions);
        final String cmdLine = javaExec + " " + customOptions + " -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=" + jicofoHomePath + " -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=config -Djava.util.logging.config.file=./logging.properties -Djdk.tls.ephemeralDHKeySize=2048 -cp " + jicofoHomePath + "/jicofo-1.1-SNAPSHOT.jar" + File.pathSeparator + jicofoHomePath + "/jicofo-1.1-SNAPSHOT-jar-with-dependencies.jar org.jitsi.jicofo.Main" + parameters;
        jicofoThread = Spawn.startProcess(cmdLine, new File(jicofoHomePath), this);

        Log.info( "Successfully initialized Jitsi Focus Component (jicofo).\n"  + cmdLine);
    }

    public synchronized void destroy() throws Exception
    {
        Log.debug( "Destroying Jitsi Focus process..." );

        if (jicofoThread != null) jicofoThread.destory();

        Log.debug( "Destroyed Jitsi Focus process..." );
    }

    public void onOutputLine(final String line)
    {
        Log.info("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.info("onProcessQuit " + code);
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.info(line);
    }

    public void onError(final Throwable t)
    {
        Log.error("Thread error", t);
    }
}
