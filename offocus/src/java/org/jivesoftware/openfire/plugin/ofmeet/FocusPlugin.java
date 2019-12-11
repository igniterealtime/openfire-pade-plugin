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
import org.jitsi.jicofo.FocusManager;
import org.jitsi.jicofo.auth.AuthenticationAuthority;
import org.jitsi.jicofo.reservation.ReservationSystem;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import java.io.File;

/**
 * An Openfire plugin that provides 'focus' functionality to Openfire.
 *
 * This plugin is largely based on Jitsi's Jicofo implementation, which is complemented by Openfire-specific provisioning.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public class FocusPlugin implements Plugin
{
    private static final Logger Log = LoggerFactory.getLogger( FocusPlugin.class );

    private final JitsiJicofoWrapper jitsiJicofoWrapper = new JitsiJicofoWrapper();

    private Thread initThread = null;

    @Override
    public void initializePlugin( PluginManager pluginManager, File file )
    {
        ensureFocusUser();

        // OFMeet must be fully loaded before focus starts to do service discovery.
        initThread = new Thread() {
            @Override
            public void run()
            {
                boolean running = true;
                while ( running )
                {
                    if ( pluginManager.getPlugin( "ofmeet" ) != null && isAcceptingClientConnections() )
                    {
                        try
                        {
                            Thread.sleep( 20000 );

                            Log.info( "starting jitsiJicofoWrapper");
                            jitsiJicofoWrapper.initialize();
                            return;
                        }
                        catch ( Exception e )
                        {
                            Log.error( "An exception occurred while initializing the Jitsi Jicofo wrapper.", e );
                        }
                    }

                    Log.info( "Waiting for ofmeet plugin to become available and the server to accept client connections..." );
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException e )
                    {
                        Log.debug( "Interrupted wait for ofmeet plugin to become available.", e );
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

    private void ensureFocusUser()
    {
        final OFMeetConfig config = new OFMeetConfig();

        // Ensure that the 'focus' user exists.
        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        if ( !userManager.isRegisteredUser( "focus" ) )
        {
            Log.info( "No pre-existing 'focus' user detected. Generating one." );

            String password = config.getFocusPassword();
            if ( password == null || password.isEmpty() )
            {
                password = StringUtils.randomString( 40 );
            }

            try
            {
                userManager.createUser(
                        "focus",
                        password,
                        "Focus User (generated)",
                        null
                );
                config.setFocusPassword( password );
            }
            catch ( Exception e )
            {
                Log.error( "Unable to provision a 'focus' user.", e );
            }
        }

        // Ensure that the 'focus' user can grant permissions in persistent MUCs by making it a sysadmin of the conference service(s).
        final JID focusUserJid = new JID( "focus@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() );
        for ( final MultiUserChatService mucService : XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatServices() )
        {
            if ( !mucService.isSysadmin( focusUserJid ) )
            {
                Log.info( "Adding 'focus' user as a sysadmin to the '{}' MUC service.", mucService.getServiceName() );
                mucService.addSysadmin( focusUserJid );
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
            jitsiJicofoWrapper.destroy();
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to destroy the Jitsi Jicofo wrapper.", ex );
        }
    }

    public ReservationSystem getReservationService()
    {
        return this.jitsiJicofoWrapper.getReservationService();
    }

    public FocusManager getFocusManager()
    {
        return this.jitsiJicofoWrapper.getFocusManager();
    }

    public AuthenticationAuthority getAuthenticationAuthority()
    {
        return this.jitsiJicofoWrapper.getAuthenticationAuthority();
    }
}
