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

package org.jivesoftware.openfire.plugin.ofgasi;

import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jivesoftware.openfire.ConnectionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.spi.ConnectionListener;
import org.jivesoftware.openfire.spi.ConnectionManagerImpl;
import org.jivesoftware.openfire.spi.ConnectionType;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import java.io.File;

/**
 * An Openfire plugin that provides a SIP gateway functionality to Openfire Meetings.
 *
 * This plugin is largely based on Jitsi's Jigasi implementation, which is complemented by Openfire-specific provisioning.
 *
 */
public class OfGasiPlugin implements Plugin
{
    private static final Logger Log = LoggerFactory.getLogger( OfGasiPlugin.class );

    private final JitsiJigasiWrapper jitsiJigasiWrapper = new JitsiJigasiWrapper();

    private Thread initThread = null;

    @Override
    public void initializePlugin( PluginManager pluginManager, File file )
    {
        ensureJigasiUser();

        // openfire must be ready before jigasi starts to do service discovery.

        initThread = new Thread() {
            @Override
            public void run()
            {
                boolean running = true;
                while ( running )
                {
                    if ( isAcceptingClientConnections() )
                    {
                        try
                        {
                            Thread.sleep( 20000 );

                            Log.info( "starting jitsiJigasiWrapper");
                            jitsiJigasiWrapper.initialize(file);
                            return;
                        }
                        catch ( Exception e )
                        {
                            Log.error( "An exception occurred while initializing the Jitsi Jigasi wrapper.", e );
                        }
                    }

                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException e )
                    {
                        Log.debug( "Interrupted wait for openfire to become available.", e );
                        running = false;
                    }
                }
            }
        };
        initThread.start();
    }

    /**
     * Checks if the server is accepting client connections on the default c2s port.
     *
     * @return true if the server is accepting connections, otherwise false.
     */
    private static boolean isAcceptingClientConnections()
    {
        final ConnectionManager cm = XMPPServer.getInstance().getConnectionManager();
        if ( cm != null )
        {
            final ConnectionManagerImpl cmi = (( ConnectionManagerImpl) cm );
            final ConnectionListener cl = cmi.getListener( ConnectionType.SOCKET_C2S, false );
            return cl != null && cl.getSocketAcceptor() != null;
        }
        return false;
    }

    private void ensureJigasiUser()
    {
        final OFMeetConfig config = new OFMeetConfig();

        // Ensure that the 'jigasi' user exists.
        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        if ( !userManager.isRegisteredUser( config.jigasiXmppUserId.get() ) )
        {
            Log.info( "No pre-existing 'jigasi' user detected. Generating one." );

            String password = config.jigasiXmppPassword.get();
            if ( password == null || password.isEmpty() )
            {
                password = StringUtils.randomString( 40 );
            }

            try
            {
                userManager.createUser(config.jigasiXmppUserId.get(), password, "Jigasi User (generated)", null);
                config.jigasiXmppUserId.set( password );
            }
            catch ( Exception e )
            {
                Log.error( "Unable to provision a 'jigasi' user.", e );
            }
        }
    }

    @Override
    public void destroyPlugin()
    {
        if ( initThread != null && initThread.isAlive() )
        {
            initThread.interrupt();
            initThread = null;
        }

        try
        {
            jitsiJigasiWrapper.destroy();
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to destroy the Jitsi Jigasi wrapper.", ex );
        }
    }
}
