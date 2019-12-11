/*
 * Copyright (C) 2018 Ignite Realtime Foundation. All rights reserved.
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

package org.jivesoftware.openfire.plugin.ofgasi;

import net.java.sip.communicator.impl.configuration.ConfigurationActivator;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jitsi.jigasi.osgi.JigasiBundleConfig;
import org.jitsi.jigasi.xmpp.CallControlComponent;
import org.jitsi.meet.OSGi;
import org.jitsi.meet.OSGiBundleConfig;
import org.jitsi.service.neomedia.DefaultStreamConnector;
import org.jitsi.util.OSUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.XMPPServerInfo;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserProvider;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A wrapper object for the Jitsi Gateway to SIP (jigasi) component.
 *
 * This wrapper can be used to instantiate/initialize and tearing down an instance of the wrapped component. An instance
 * of this class is re-usable.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JigasiWrapper
{
    private static final Logger Log = LoggerFactory.getLogger( JigasiWrapper.class );

    /**
     * The name of the command-line argument which specifies the XMPP domain
     * to use.
     */
    private static final String DOMAIN_ARG_NAME = "--domain";

    /**
     * The name of the command-line argument which specifies the IP address or
     * the name of the XMPP host to connect to.
     */
    private static final String HOST_ARG_NAME = "--host";

    /**
     * The default value of the {@link #HOST_ARG_NAME} command-line argument if
     * it is not explicitly provided.
     */
    private static final String HOST_ARG_VALUE = "localhost";

    /**
     * The name of the command-line argument which specifies the value of the
     * <tt>System</tt> property
     * {@link DefaultStreamConnector#MAX_PORT_NUMBER_PROPERTY_NAME}.
     */
    private static final String MAX_PORT_ARG_NAME = "--max-port";

    /**
     * The default value of the {@link #MAX_PORT_ARG_NAME} command-line argument
     * if it is not explicitly provided.
     */
    private static final int MAX_PORT_ARG_VALUE = 20000;

    /**
     * The name of the command-line argument which specifies the value of the
     * <tt>System</tt> property
     * {@link DefaultStreamConnector#MIN_PORT_NUMBER_PROPERTY_NAME}.
     */
    private static final String MIN_PORT_ARG_NAME = "--min-port";

    /**
     * The default value of the {@link #MIN_PORT_ARG_NAME} command-line argument
     * if
     * it is not explicitly provided.
     */
    private static final int MIN_PORT_ARG_VALUE = 10000;

    /**
     * The name of the command-line argument which specifies the port of the
     * XMPP host to connect on.
     */
    private static final String PORT_ARG_NAME = "--port";

    /**
     * The default value of the {@link #PORT_ARG_NAME} command-line argument if
     * it is not explicitly provided.
     */
    private static final int PORT_ARG_VALUE = 5347;

    /**
     * The name of the command-line argument which specifies the secret key for
     * the sub-domain of the Jabber component implemented by this application
     * with which it is to authenticate to the XMPP server to connect to.
     */
    private static final String SECRET_ARG_NAME = "--secret";

    /**
     * The name of the command-line argument which specifies sub-domain name for
     * the jigasi component.
     */
    private static final String SUBDOMAIN_ARG_NAME = "--subdomain";

    /**
     * The name of the command-line argument which specifies that connecting the
     * component is disabled.
     */
    private static final String DISABLE_COMPONENT_ARG_NAME = "--nocomponent";

    /**
     * The name of the command-line argument which specifies log folder to use.
     */
    private static final String LOGDIR_ARG_NAME = "--logdir";

    /**
     * The name of the property that stores the home dir for application log
     * files (not history).
     */
    public static final String PNAME_SC_LOG_DIR_LOCATION =
        "net.java.sip.communicator.SC_LOG_DIR_LOCATION";

    /**
     * The name of the property that stores the home dir for cache data, such
     * as avatars and spelling dictionaries.
     */
    public static final String PNAME_SC_CACHE_DIR_LOCATION =
        "net.java.sip.communicator.SC_CACHE_DIR_LOCATION";

    /**
     * The name of the command-line argument which specifies config folder to use.
     */
    private static final String CONFIG_DIR_ARG_NAME = "--configdir";

    /**
     * The name of the command-line argument which specifies config folder to use.
     */
    private static final String CONFIG_DIR_NAME_ARG_NAME = "--configdirname";


    private ComponentManager componentManager;
    private CallControlComponent component;
    private File pluginDirectory;

    private final OFMeetConfig config = new OFMeetConfig();

    public void initialize( final PluginManager manager, final File pluginDirectory )
    {
        this.pluginDirectory = pluginDirectory;

        try
        {
            checkNatives();
        }
        catch ( Exception e )
        {
            Log.warn( "An unexpected error occurred while checking the native libraries.", e );
        }

        // The CallControlComponent implementation depends on OSGI-based loading of
        // Components, which is prepared for here.
        if ( OSGi.class.getClassLoader() != Thread.currentThread().getContextClassLoader() )
        {
            // the OSGi class should not be present in Openfire itself, or in the parent plugin of these modules. The OSGi implementation does not allow for more than one bundle to be configured/started, which leads to undesired re-used of
            // configuration of one bundle while starting another bundle.
            Log.warn( "The OSGi class is loaded by a class loader different from the one that's loading this module. This suggests that residual configuration is in the OSGi class instance, which is likely to prevent Jigasi from functioning correctly." );
        }

        reloadConfiguration();
    }

    /**
     * Checks whether we have folder with extracted natives, if missing
     * find the appropriate archive and extract them. Normally this is
     * done once when plugin is installed or updated.
     * If folder with natives exist add it to the java.library.path so
     * libjitsi can use those native libs.
     */
    private void checkNatives() throws Exception
    {
        // Find the root path of the class that will be our plugin lib folder.
        String binaryPath = (new URL( CallControlComponent.class.getProtectionDomain().getCodeSource().getLocation(), ".")).openConnection().getPermission().getName();

        File jigasiJarFile = new File(binaryPath);
        File nativeLibFolder = new File(jigasiJarFile.getParentFile(), "native");

        if(!nativeLibFolder.exists() || nativeLibFolder.listFiles().length == 0 )
        {
            // Lets find the appropriate jar file to extract and extract it.
            String archiveFileSuffix = null;
            if ( OSUtils.IS_LINUX32 )
            {
                archiveFileSuffix = "-native-linux-32.jar";
            }
            else if ( OSUtils.IS_LINUX64 )
            {
                archiveFileSuffix = "-native-linux-64.jar";
            }
            else if ( OSUtils.IS_WINDOWS64 )
            {
                archiveFileSuffix = "jitsi-videobridge-1.1-20180621.193237-72-native-windows-64.jar";
            }
            else if ( OSUtils.IS_MAC )
            {
                archiveFileSuffix = "-native-macosx.jar";
            }
            if ( archiveFileSuffix == null )
            {
                Log.warn( "Unable to determine what the native libraries are for this OS." );
            }
            else if ( nativeLibFolder.exists() || nativeLibFolder.mkdirs() )
            {
                // The name of the native library is the same as the name of the jigasi jar, but has a different ending.
                String nativeLibsJarPath = jigasiJarFile.getCanonicalPath();
                nativeLibsJarPath = nativeLibsJarPath.replaceFirst( "jigasi-1.1-20180806.165930-11.jar", archiveFileSuffix );

                Log.debug("Applicable archive with native libraries: '{}'", nativeLibsJarPath);
                JarFile archive = new JarFile( nativeLibsJarPath );

                Enumeration en = archive.entries();

                while ( en.hasMoreElements() )
                {
                    try
                    {
                        JarEntry archiveEntry = (JarEntry) en.nextElement();
                        Log.debug( "Iterating over: {}", archiveEntry.getName() );
                        if ( archiveEntry.isDirectory() || archiveEntry.getName().contains( "/" ) )
                        {
                            // Skip everything that's not in the root directory of the archive.
                            continue;
                        }
                        final File extractedFile = new File( nativeLibFolder, archiveEntry.getName() );
                        Log.debug( "Copying file '{}' from native library into '{}'.", archiveEntry, extractedFile );

                        try ( InputStream is = archive.getInputStream( archiveEntry );
                              FileOutputStream fos = new FileOutputStream( extractedFile ) )
                        {
                            while ( is.available() > 0 )
                            {
                                fos.write( is.read() );
                            }
                        }
                    }
                    catch ( Throwable t )
                    {
                        Log.warn( "An unexpected error occurred while copying native libraries.", t );
                    }
                }

                // When running on Linux, jitsi-sysactivity needs another native library.
                if ( OSUtils.IS_LINUX ) {
                    final Path start = jigasiJarFile.getParentFile().toPath();
                    final int maxDepth = 1;
                    Files.find( start, maxDepth, ( path, basicFileAttributes ) -> path.getFileName().toString().startsWith( "libunix" ) && path.getFileName().toString().endsWith( ".so" ) )
                        .forEach(path -> {
                            final Path target = path.getParent().resolve( "native" ).resolve( "libunix-java.so" );
                            Log.debug( "Create a symbolic link target '{}' for native file '{}'", target, path );
                            try
                            {
                                Files.createSymbolicLink( target, path );
                            }
                            catch ( IOException e )
                            {
                                Log.debug( "Unable to create a symbolic link target '{}' for native file '{}'. Will attempt to copy instead.", target, path );
                                try
                                {
                                    Files.copy( target, path );
                                }
                                catch ( IOException e1 )
                                {
                                    Log.warn( "Unable to move native file '{}' into folder containing natives.", path, e1 );
                                }
                            }
                        } );
                }
                Log.info( "Native lib folder created and natives extracted" );
            }
            else
            {
                Log.warn( "Unable to create native lib folder." );
            }
        }
        else
        {
            Log.info( "Native lib folder already exist." );
        }

        if ( nativeLibFolder.exists() )
        {
            String newLibPath = nativeLibFolder.getCanonicalPath() + File.pathSeparator + System.getProperty( "java.library.path" );
            System.setProperty( "java.library.path", newLibPath );

            // this will reload the new setting
            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( System.class.getClassLoader(), null );
        }
    }

    public void reloadConfiguration()
    {
        ensureJigasiUser();

        int maxPort = MAX_PORT_ARG_VALUE;

        int minPort = MIN_PORT_ARG_VALUE;

        //int port = PORT_ARG_VALUE;

        // Jingle Raw UDP transport
        System.setProperty(
            DefaultStreamConnector.MAX_PORT_NUMBER_PROPERTY_NAME,
            String.valueOf(maxPort));

        // Jingle ICE-UDP transport
        System.setProperty(
            OperationSetBasicTelephony.MAX_MEDIA_PORT_NUMBER_PROPERTY_NAME,
            String.valueOf(maxPort));

        // Jingle Raw UDP transport
        System.setProperty(
            DefaultStreamConnector.MIN_PORT_NUMBER_PROPERTY_NAME,
            String.valueOf(minPort));

        // Jingle ICE-UDP transport
        System.setProperty(
            OperationSetBasicTelephony.MIN_MEDIA_PORT_NUMBER_PROPERTY_NAME,
            String.valueOf(minPort));

//        // FIXME: properties used for debug purposes
//        // jigasi-home will be create in current directory (from where the
//        // process is launched). It must contain sip-communicator.properties
//        // with one XMPP and one SIP account configured.
//        String configDir = System.getProperty("user.dir");
//
//        System.setProperty(
//            ConfigurationService.PNAME_SC_HOME_DIR_LOCATION, configDir);
//
//        String configDirName = "jigasi-home";
//
//        System.setProperty(
//            ConfigurationService.PNAME_SC_HOME_DIR_NAME,
//            configDirName);

//        String logdir = cmdLine.getOptionValue(LOGDIR_ARG_NAME);
//        if (!StringUtils.isNullOrEmpty( logdir))
//        {
//            System.setProperty(PNAME_SC_LOG_DIR_LOCATION, logdir);
//            // set it same as cache dir so if something is written lets write it
//            // there, currently only empty avatarcache folders, if something
//            // is really needed to cache we can chanege it to /var/lib/jigasi
//            // or something similar
//            System.setProperty(PNAME_SC_CACHE_DIR_LOCATION, logdir);
//        }

        // make sure we use the properties files for configuration
        System.setProperty( ConfigurationActivator.PNAME_USE_PROPFILE_CONFIG, "true" );

        // The CallControlComponent implementation expects to be an External Component,
        // which in the case of an Openfire plugin is untrue. As a result, most
        // of its constructor arguments are unneeded when the instance is
        // deployed as an Openfire plugin. None of the values below are expected
        // to be used (but where possible, valid values are provided for good
        // measure).

        final String subdomain = "callcontrol"; // TODO make configurable
        final XMPPServerInfo info = XMPPServer.getInstance().getServerInfo();
        final String hostname = info.getHostname();
        final int port = -1;
        final String domain = info.getXMPPDomain();
        final String secret = null;

        // Unload any previous instances
        if (componentManager != null && component != null)
        {
            try
            {
                componentManager.removeComponent(component.getSubdomain());
            }
            catch (ComponentException ce)
            {
                Log.warn( "An unexpected exception occurred while reloading the wrapper.", ce );
            }
            componentManager = null;
            component = null;
        }

        // Restart if possible
        if (canBeUsed())
        {
            // Set the SIP account details to be used. Ideally, you'd want to set this through
            // JigasiBundleActivator.getConfigurationService(). That can only be done after the OSGi context
            // is started. By then, it's to late (as the OSGi context needs to be started with these settings).
            // As a work-around, the property file is modified here directly).

            System.setProperty( "net.java.sip.communicator.SC_HOME_DIR_LOCATION",  pluginDirectory.getAbsolutePath() );
            System.setProperty( "net.java.sip.communicator.SC_HOME_DIR_NAME",      "classes" );

            final File sipCommunicatorPropertyFile = new File(pluginDirectory.getAbsolutePath() + "/classes/sip-communicator.properties");
            final Properties sipCommunicatorProps = new Properties();
            try ( final FileReader reader = new FileReader( sipCommunicatorPropertyFile ) )
            {
                sipCommunicatorProps.load( reader );

                // SIP account (that will invite users, or receive inbound calls and merge them into the Meet)
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PASSWORD", Base64.getEncoder().encodeToString( config.jigasiSipPassword.get().getBytes() ) );
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.SERVER_ADDRESS", config.jigasiSipServerAddress.get() );
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.USER_ID", config.jigasiSipUserId.get() );
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ACCOUNT_UID","SIP:" + config.jigasiSipUserId.get());
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.DOMAIN_BASE", config.jigasiSipDomainBase.get() );

                sipCommunicatorProps.setProperty( "net.java.sip.communicator.CUSTOM_XMPP_DOMAIN", domain);
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.SIP_PREFERRED_CLEAR_PORT", "5060");
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.SIP_PREFERRED_SECURE_PORT", "5061");

                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PREFERRED_TRANSPORT", "TCP");
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROXY_ADDRESS", config.jigasiSipServerAddress.get() );
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROXY_PORT", "5060" );
                sipCommunicatorProps.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PROXY_AUTO_CONFIG", "false" );

                // XMPP account (which will join the MUC on behalf of the user joining via SIP.
                boolean xmppAccountVerified = true;
                try
                {
                    AuthFactory.getAuthProvider().authenticate( config.jigasiXmppUserId.get(), config.jigasiXmppPassword.get() );
                }
                catch ( Exception e )
                {
                    xmppAccountVerified = false;
                }

                if ( xmppAccountVerified || !JiveGlobals.getBooleanProperty( "xmpp.auth.anonymous" ) )
                {
                    // Use the XMPP account if it has been verified to work, or when anonymous access has been disabled (better to show that error than silently ignore things).
                    sipCommunicatorProps.setProperty( "org.jitsi.jigasi.xmpp.acc.USER_ID", XMPPServer.getInstance().createJID( config.jigasiXmppUserId.get(), null ).toBareJID() );
                    sipCommunicatorProps.setProperty( "org.jitsi.jigasi.xmpp.acc.PASS", config.jigasiXmppPassword.get() );
                    sipCommunicatorProps.setProperty( "org.jitsi.jigasi.xmpp.acc.ANONYMOUS_AUTH", "false" );
                    sipCommunicatorProps.setProperty( "org.jitsi.jigasi.DEFAULT_JVB_ROOM_NAME", "lobby@conference." + domain);
                }
                else
                {
                    sipCommunicatorProps.setProperty( "org.jitsi.jigasi.xmpp.acc.ANONYMOUS_AUTH", "true" );
                }

                //sipCommunicatorProps.setProperty( "org.jitsi.jigasi.xmpp.acc.DOMAIN_BASE", domain );
            }
            catch ( Exception e )
            {
                Log.warn( "Unable to read properties / add account info!", e );
            }
            try ( final FileWriter writer = new FileWriter( sipCommunicatorPropertyFile ) )
            {
                sipCommunicatorProps.store( writer, null );
            }
            catch ( Exception e )
            {
                Log.warn( "Unable to store properties / add account info!", e );
            }

            // Note: we CANNOT load the bundle without an account! A NullPointerException will be thrown by:
            // et.java.sip.communicator.service.protocol.ProtocolProviderFactory.createAccount(ProtocolProviderFactory.java:1109)
            //if ( OSGi.getBundleConfig() == null )
            //{
            //}

            try
            {
                Log.info("Starting callcontrol component");

                final OSGiBundleConfig osgiBundles = new JigasiBundleConfig();
                OSGi.setBundleConfig( osgiBundles );
                OSGi.setClassLoader( XMPPServer.getInstance().getPluginManager().getPluginClassloader( XMPPServer.getInstance().getPluginManager().getPlugin( "ofgasi" ) ) );

                componentManager = ComponentManagerFactory.getComponentManager();
                component = new CallControlComponent( hostname, port, domain, subdomain, secret );
                componentManager.addComponent( subdomain, component );

                SmackConfiguration.DEBUG = false; // work-around needed until https://github.com/jitsi/jitsi/pull/515 is in the dependency that we use.

//                ConfigurationService configurationService = JigasiBundleActivator.getConfigurationService();
//
//                // This can only be reconfigured after the OSGi context has been started.
//                //configurationService.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.ACCOUNT_UID","SIP:john.doe@example.org" );
//                configurationService.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.PASSWORD", Base64.getEncoder().encodeToString( config.jigasiSipPassword.get().getBytes() ) );
//                configurationService.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.SERVER_ADDRESS", config.jigasiSipServerAddress.get() );
//                configurationService.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.USER_ID", config.jigasiSipUserId.get() );
//                configurationService.setProperty( "net.java.sip.communicator.impl.protocol.sip.acc1403273890647.DOMAIN_BASE", config.jigasiSipDomainBase.get() );

                Log.info("Started callcontrol component");
            }
            catch ( ComponentException ce )
            {
                Log.error( "An exception occurred when loading the wrapper: the component could not be added.", ce );
                this.componentManager = null;
                this.component = null;
            }
        }
    }

    public void destroy()
    {
        if (componentManager != null && component != null)
        {
            try
            {
                componentManager.removeComponent(component.getSubdomain());
            }
            catch (ComponentException ce)
            {
                Log.warn( "An unexpected exception occurred while destroying the wrapper.", ce );
            }
            componentManager = null;
            component = null;
        }
        pluginDirectory = null;
    }

    public boolean canBeUsed()
    {
        final boolean hasUserID = config.jigasiSipUserId.get() != null && !config.jigasiSipUserId.get().isEmpty();
        final boolean hasPassword = config.jigasiSipPassword.get() != null && !config.jigasiSipPassword.get().isEmpty();
        final boolean hasServerAddress = config.jigasiSipServerAddress.get() != null && !config.jigasiSipServerAddress.get().isEmpty();
        final boolean canBeUsed = hasUserID && hasPassword && hasServerAddress;
        if ( canBeUsed ) {
            Log.trace( "Configured with user ID, password and server address." );
        } else {
            Log.info( "Configured with user ID: {}, configured with password: {}, configured with server address: {}. All are required for the module to be used.", new Object[] { hasUserID, hasPassword, hasServerAddress } );
        }
        return canBeUsed;
    }

    public Map<String, String> getServlets()
    {
        return null;
    }

    /**
     * Attemt to create an XMPP user that will represent the SIP contact that is pulled into a Meet.
     */
    private static void ensureJigasiUser()
    {
        final OFMeetConfig config = new OFMeetConfig();

        final String userId = config.getJigasiXmppUserId().get();

        // Ensure that the user exists.
        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        if ( !userManager.isRegisteredUser( userId ) )
        {
            Log.info( "No pre-existing jigasi user '{}' detected. Generating one.", userId );

            if ( UserManager.getUserProvider().isReadOnly() ) {
                Log.info( "The user provider on this system is read only. Cannot create a Jigasi user account." );
                return;
            }

            String password = config.getJigasiXmppPassword().get();
            if ( password == null || password.isEmpty() )
            {
                password = StringUtils.randomString( 40 );
            }

            try
            {
                userManager.createUser(
                    userId,
                    password,
                    "Jigasi User (generated)",
                    null
                );
                config.getJigasiXmppPassword().set( password );
            }
            catch ( Exception e )
            {
                Log.error( "Unable to provision a jigasi user.", e );
            }
        }
    }
}
