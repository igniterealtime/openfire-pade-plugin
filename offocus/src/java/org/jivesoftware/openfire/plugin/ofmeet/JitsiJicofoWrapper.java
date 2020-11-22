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

        final ConnectionType connectionType = ConnectionType.COMPONENT;
        final ConnectionManagerImpl manager = (ConnectionManagerImpl) XMPPServer.getInstance().getConnectionManager();
        final ConnectionConfiguration plaintextConfiguration  = manager.getListener( connectionType, false ).generateConnectionConfiguration();

        final OFMeetConfig config = new OFMeetConfig();
        final String port = String.valueOf(plaintextConfiguration.getPort());
        final String jicofoSubdomain = "focus";
        final String MAIN_MUC = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + XMPPServer.getInstance().getServerInfo().getXMPPDomain());

        final String parameters =
            " --host=" + XMPPServer.getInstance().getServerInfo().getHostname() +
            " --port=" + port +
            " --domain=" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() +
            " --secret=" + config.getFocusPassword() +
            " --user_domain=" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() +
            " --user_name=" + jicofoSubdomain +
            " --user_password=" + config.getFocusPassword();

        String jicofoHomePath = pluginDirectory.getAbsolutePath() + File.separator + "classes" + File.separator + "jicofo";
        File props_file = new File(jicofoHomePath + File.separator + "sip-communicator.properties");
        Properties props = new Properties();

        props.load(new FileInputStream(props_file));
        props.setProperty("org.jitsi.jicofo.BRIDGE_MUC", "admin@" + MAIN_MUC);
        props.setProperty( "net.java.sip.communicator.service.gui.ALWAYS_TRUST_MODE_ENABLED", "true");;
        props.setProperty( "org.jitsi.jicofo.PING_INTERVAL", "-1" );
        props.setProperty( "org.jitsi.jicofo.SERVICE_REDISCOVERY_INTERVAL", "-1" );
        props.store(new FileOutputStream(props_file), "Properties");

        String jicofoExePath = jicofoHomePath + File.separator + "offocus";

        if(OSUtils.IS_LINUX64)
        {
            jicofoExePath = jicofoExePath + ".sh";
        }
        else if(OSUtils.IS_WINDOWS64)
        {
            jicofoExePath = jicofoExePath + ".bat";
        }

         jicofoThread = Spawn.startProcess(jicofoExePath + parameters, new File(jicofoHomePath), this);

        Log.info( "Successfully initialized Jitsi Focus Component (jicofo)."  + jicofoExePath + parameters);
    }

    public synchronized void destroy() throws Exception
    {
        Log.debug( "Destroying Jitsi Focus Component..." );

        if (jicofoThread != null) jicofoThread.destory();
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
