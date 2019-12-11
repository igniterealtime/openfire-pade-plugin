/*
 * Copyright @ 2015 Atlassian Pty Ltd
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
package org.jitsi.videobridge.openfire;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import org.ice4j.StackProperties;
import org.ice4j.ice.harvest.MappingCandidateHarvesters;
import org.jitsi.meet.OSGi;
import org.jitsi.meet.OSGiBundleConfig;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.*;
import org.jitsi.util.*;
import org.jitsi.videobridge.IceUdpTransportManager;
import org.jitsi.videobridge.TransportManager;
import org.jitsi.videobridge.xmpp.*;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.XMPPServerInfo;
import org.jivesoftware.openfire.container.*;
import org.jivesoftware.util.*;
import org.slf4j.*;
import org.slf4j.Logger;
import org.xmpp.component.*;

/**
 * Implements <tt>org.jivesoftware.openfire.container.Plugin</tt> to integrate
 * Jitsi Video Bridge into Openfire.
 *
 * @author Lyubomir Marinov
 * @author Damian Minkov
 */
public class PluginImpl
    implements Plugin,
               PropertyEventListener
{
    /**
     * The logger.
     */
    private static final Logger Log = LoggerFactory.getLogger(PluginImpl.class);

    /**
     * The name of the Openfire property that defines what interfaces are allowed to be used (or null, if all of them
     * are allowed).
     */
    public static final String INTERFACES_ALLOWED_PROPERTY_NAME = "org.jitsi.videobridge.media.INTERFACES_ALLOWED";

    /**
     * The name of the Openfire property that defines what interfaces are not allowed to be used.
     */
    public static final String INTERFACES_BLOCKED_PROPERTY_NAME = "org.jitsi.videobridge.media.INTERFACES_BLOCKED";

    /**
     * The name of the Openfire property that defines what IP addresses are allowed to be used (or null, if all of them
     * are allowed).
     */
    public static final String ADDRESSES_ALLOWED_PROPERTY_NAME = "org.jitsi.videobridge.media.ADDRESSES_ALLOWED";

    /**
     * The name of the Openfire property that defines what IP addresses are not allowed to be used.
     */
    public static final String ADDRESSES_BLOCKED_PROPERTY_NAME = "org.jitsi.videobridge.media.ADDRESSES_BLOCKED";

    /**
     * The name of the Openfire property that defines what the state of the AWS Mapping Harvester is (disabled, auto or
     * forced).
     */
    public static final String AWS_HARVESTER_CONFIG_PROPERTY_NAME = "org.jitsi.videobridge.media.AWS_HARVESTER";

    /**
     * The name of the Openfire property that contains the single port number for the STUN server that is used for
     * IP address mapping.
     */
    public static final String STUN_HARVESTER_ADDRESS_PROPERTY_NAME = "org.jitsi.videobridge.media.STUN_HARVESTER_ADDRESS";

    /**
     * The name of the Openfire property that contains the single port number for the STUN server that is used for
     * IP address mapping.
     */
    public static final String STUN_HARVESTER_PORT_PROPERTY_NAME = "org.jitsi.videobridge.media.STUN_HARVESTER_PORT";

    /**
     * The name of the Openfire property that contains the local address, used for IP address mapping in a NAT.
     */
    public static final String MANUAL_HARVESTER_LOCAL_PROPERTY_NAME = "org.ice4j.ice.harvest.NAT_HARVESTER_PRIVATE_ADDRESS";

    /**
     * The name of the Openfire property that contains the public address, used for IP address mapping in a NAT.
     */
    public static final String MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME = "org.ice4j.ice.harvest.NAT_HARVESTER_PUBLIC_ADDRESS";

    /**
     * The name of the Openfire property that contains the boolean value that indices if we'd
     * allow our RTP managers to use multiplexing of media streams.
     */
    public static final String SINGLE_PORT_ENABLED_PROPERTY_NAME = "org.jitsi.videobridge.media.SINGLE_PORT_HARVESTER_ENABLED";

    /**
     * The name of the Openfire property that contains the single UDP port number that we'd
     * like our RTP managers to bind upon when using multiplexing of media streams.
     */
    public static final String SINGLE_PORT_NUMBER_PROPERTY_NAME = "org.jitsi.videobridge.media.SINGLE_PORT_HARVESTER_PORT";

    /**
     * The name of the Openfire property that contains the boolean value that indices if we'd
     * allow our RTP managers to use dynamically allocated UDP ports.
     */
    public static final String MINMAX_PORT_ENABLED_PROPERTY_NAME = "org.jitsi.videobridge.media.USE_DYNAMIC_HOST_HARVESTER";

    /**
     * The name of the Openfire property that contains the maximum UDP port number that we'd
     * like our RTP managers to bind upon when dynamically allocating ports for media streams.
     */
    public static final String MAX_PORT_NUMBER_PROPERTY_NAME = "org.jitsi.videobridge.media.MAX_PORT_NUMBER";

    /**
     * The name of the Openfire property that contains the minimum UDP port number that we'd
     * like our RTP managers to bind upon when dynamically allocating ports for media streams.
     */
    public static final String MIN_PORT_NUMBER_PROPERTY_NAME = "org.jitsi.videobridge.media.MIN_PORT_NUMBER";

    /**
     * The name of the Openfire property that contains the TCP port number (if any).
     */
    public static final String TCP_PORT_PROPERTY_NAME = "org.jitsi.videobridge.media.TCP_HARVESTER_PORT";

    /**
     * The name of the Openfire property that contains the Mapped TCP port number (if any).
     */
    public static final String TCP_MAPPED_PORT_PROPERTY_NAME = "org.jitsi.videobridge.media.TCP_HARVESTER_MAPPED_PORT";

    /**
     * The name of the Openfire property that contains the boolean value to determine that TCP
     * connectivity is available.
     */
    public static final String TCP_ENABLED_PROPERTY_NAME = "org.jitsi.videobridge.TCP_HARVESTER_ENABLED";

    /**
     * The name of the Openfire property that controls the SSLTCP availability of the TCP harvester.
     */
    public static final String TCP_SSLTCP_ENABLED_PROPERTY_NAME = "org.jitsi.videobridge.TCP_HARVESTER_SSLTCP";

    /**
     * The <tt>ComponentManager</tt> to which the component of this
     * <tt>Plugin</tt> has been added.
     */
    private ComponentManager componentManager;

    /**
     * The <tt>Component</tt> that has been registered by this plugin. This
     * wraps the Videobridge service.
     */
    private ComponentImpl component;

    /**
     * The subdomain of the address of component with which it has been
     * added to {@link #componentManager}.
     */
    private String subdomain;

    /**
     * Destroys this <tt>Plugin</tt> i.e. releases the resources acquired by
     * this <tt>Plugin</tt> throughout its life up until now and prepares it for
     * garbage collection.
     *
     * @see Plugin#destroyPlugin()
     */
    public void destroyPlugin()
    {
        PropertyEventDispatcher.removeListener(this);

        if ((componentManager != null) && (subdomain != null))
        {
            try
            {
                componentManager.removeComponent(subdomain);
            }
            catch (ComponentException ce)
            {
                Log.warn( "An unexpected exception occurred while " +
                          "destroying the plugin.", ce );
            }
            componentManager = null;
            component = null;
            subdomain = null;
        }
    }

    /**
     * Initializes this <tt>Plugin</tt>.
     *
     * @param manager the <tt>PluginManager</tt> which loads and manages this
     * <tt>Plugin</tt>
     * @param pluginDirectory the directory into which this <tt>Plugin</tt> is
     * located
     * @see Plugin#initializePlugin(PluginManager, File)
     */
    public void initializePlugin(PluginManager manager, File pluginDirectory)
    {
        PropertyEventDispatcher.addListener(this);

        try
        {
            checkNatives();
        }
        catch ( Exception e )
        {
            Log.warn( "An unexpected error occurred while checking the " +
                "native libraries.", e );
        }

        ComponentManager componentManager
            = ComponentManagerFactory.getComponentManager();
        String subdomain = ComponentImpl.SUBDOMAIN;

        // The ComponentImpl implementation expects to be an External Component,
        // which in the case of an Openfire plugin is untrue. As a result, most
        // of its constructor arguments are unneeded when the instance is
        // deployed as an Openfire plugin. None of the values below are expected
        // to be used (but where possible, valid values are provided for good
        // measure).
        final XMPPServerInfo info = XMPPServer.getInstance().getServerInfo();
        final String hostname = info.getHostname();
        final int port = -1;
        final String domain = info.getXMPPDomain();
        final String secret = null;

        final List<String> allowedInterfaces = JiveGlobals.getListProperty( INTERFACES_ALLOWED_PROPERTY_NAME, null );
        if ( allowedInterfaces != null )
        {
            System.setProperty( StackProperties.ALLOWED_INTERFACES, String.join( ";", allowedInterfaces ) );
            System.clearProperty( StackProperties.BLOCKED_INTERFACES );
        }
        else
        {
            System.clearProperty( StackProperties.ALLOWED_INTERFACES );
            System.clearProperty( StackProperties.BLOCKED_INTERFACES );
        }

        final List<String> allowedAddresses = JiveGlobals.getListProperty( ADDRESSES_ALLOWED_PROPERTY_NAME, null );
        if ( allowedAddresses != null )
        {
            System.setProperty( StackProperties.ALLOWED_ADDRESSES, String.join( ";", allowedAddresses ) );
            System.clearProperty( StackProperties.BLOCKED_ADDRESSES );
        }
        else
        {
            System.clearProperty( StackProperties.ALLOWED_ADDRESSES );
            System.clearProperty( StackProperties.BLOCKED_ADDRESSES );
        }

        if ( JiveGlobals.getProperty( AWS_HARVESTER_CONFIG_PROPERTY_NAME ) != null )
        {
            switch ( JiveGlobals.getProperty( AWS_HARVESTER_CONFIG_PROPERTY_NAME ) ) {
                case "disabled":
                    System.setProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME, "true" );
                    System.clearProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME );
                    break;
                case "forced":
                    System.setProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME, "false" );
                    System.setProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME, "true" );
                    break;
                default:
                    System.clearProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME );
                    System.clearProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME );
                    break;
            }
        }
        else
        {
            System.clearProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME );
            System.clearProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME );
        }

        if ( JiveGlobals.getProperty( MANUAL_HARVESTER_LOCAL_PROPERTY_NAME ) != null ) {
            System.setProperty( MappingCandidateHarvesters.NAT_HARVESTER_LOCAL_ADDRESS_PNAME, JiveGlobals.getProperty( MANUAL_HARVESTER_LOCAL_PROPERTY_NAME ) );
        } else {
            System.clearProperty( MappingCandidateHarvesters.NAT_HARVESTER_LOCAL_ADDRESS_PNAME );
        }

        if ( JiveGlobals.getProperty( MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME ) != null ) {
            System.setProperty( MappingCandidateHarvesters.NAT_HARVESTER_PUBLIC_ADDRESS_PNAME, JiveGlobals.getProperty( MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME ) );
        } else {
            System.clearProperty( MappingCandidateHarvesters.NAT_HARVESTER_PUBLIC_ADDRESS_PNAME );
        }

        final String stunAddress = JiveGlobals.getProperty( STUN_HARVESTER_ADDRESS_PROPERTY_NAME );
        final String stunPort = JiveGlobals.getProperty( STUN_HARVESTER_PORT_PROPERTY_NAME );
        if ( stunAddress != null && !stunAddress.isEmpty() && stunPort != null && !stunPort.isEmpty() ) {
            System.setProperty( MappingCandidateHarvesters.STUN_MAPPING_HARVESTER_ADDRESSES_PNAME, stunAddress + ":" + stunPort );
        } else {
            System.clearProperty( MappingCandidateHarvesters.STUN_MAPPING_HARVESTER_ADDRESSES_PNAME );
        }

        System.setProperty( StackProperties.USE_DYNAMIC_HOST_HARVESTER, Boolean.toString( JiveGlobals.getBooleanProperty( MINMAX_PORT_ENABLED_PROPERTY_NAME, true ) ) );

        // The ComponentImpl implementation depends on OSGI-based loading of
        // Components, which is prepared for here. Note that a configuration
        // is used that is slightly different from the default configuration
        // for Jitsi Videobridge: the REST API is not loaded.
        final OSGiBundleConfig osgiBundles = new JvbOpenfireBundleConfig();
        OSGi.setBundleConfig(osgiBundles);

        // The class loader to be used here should be the Openfire PluginClassLoader
        // that loads the plugin (as that will have access to the relevant files).
        // The plugin class loader is used to initialize the plugin, so the class loader
        // that's doing this invocation can be used instead of explicitly looking up
        // the class loader. Using the implicit approach will allow from some flexiblity
        // (for instance, to use a different classloader than the plugin classloader)
        // which is utilized by at least one project that depends on this code (OFMeet).
        final ClassLoader classLoader;
        // classLoader = manager.getPluginClassloader(this)); // Explicitly look up the classloader
        classLoader = Thread.currentThread().getContextClassLoader(); // Use the loader of the current thread.

        OSGi.setClassLoader( classLoader );

        ComponentImpl component =
            new ComponentImpl( hostname, port, domain, subdomain, secret );

        try
        {
            componentManager.addComponent(subdomain, component);
            this.componentManager = componentManager;
            this.component = component;
            this.subdomain = subdomain;

            // Note that property setting uses an OSGi service that's only available after the component is started.
            //
            // TODO I suspect that there's a race condition here. When a client requests a socket before the changes
            //      below are applied, these new values might be ignored and an implementation default might be used
            //      instead.
            LibJitsi.getConfigurationService().setProperty(
                IceUdpTransportManager.SINGLE_PORT_HARVESTER_PORT,
                JiveGlobals.getBooleanProperty( SINGLE_PORT_ENABLED_PROPERTY_NAME, true )
                    ? JiveGlobals.getIntProperty( SINGLE_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.SINGLE_PORT_DEFAULT_VALUE)
                    : -1
            );

            LibJitsi.getConfigurationService().setProperty(
                DefaultStreamConnector.MAX_PORT_NUMBER_PROPERTY_NAME,
                JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
            );

            LibJitsi.getConfigurationService().setProperty(
                DefaultStreamConnector.MIN_PORT_NUMBER_PROPERTY_NAME,
                JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE)
            );

            // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
            TransportManager.portTracker.setRange(
                JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
            );

            if ( JiveGlobals.getProperty( TCP_ENABLED_PROPERTY_NAME ) != null ) {
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.DISABLE_TCP_HARVESTER,
                    !JiveGlobals.getBooleanProperty( TCP_ENABLED_PROPERTY_NAME )
                );
            } else {
                LibJitsi.getConfigurationService().removeProperty(
                    IceUdpTransportManager.DISABLE_TCP_HARVESTER
                );
            }

            if ( JiveGlobals.getProperty( TCP_PORT_PROPERTY_NAME ) != null) {
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.TCP_HARVESTER_PORT,
                    JiveGlobals.getProperty( TCP_PORT_PROPERTY_NAME )
                );
            } else {
                LibJitsi.getConfigurationService().removeProperty(
                    IceUdpTransportManager.TCP_HARVESTER_PORT
                );
            }

            if ( JiveGlobals.getProperty( TCP_MAPPED_PORT_PROPERTY_NAME ) != null) {
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.TCP_HARVESTER_MAPPED_PORT,
                    JiveGlobals.getProperty( TCP_MAPPED_PORT_PROPERTY_NAME )
                );
            } else {
                LibJitsi.getConfigurationService().removeProperty(
                    IceUdpTransportManager.TCP_HARVESTER_MAPPED_PORT
                );
            }

            if ( JiveGlobals.getProperty( TCP_SSLTCP_ENABLED_PROPERTY_NAME ) != null) {
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.TCP_HARVESTER_SSLTCP,
                    JiveGlobals.getProperty( TCP_SSLTCP_ENABLED_PROPERTY_NAME )
                );
            } else {
                LibJitsi.getConfigurationService().removeProperty(
                    IceUdpTransportManager.TCP_HARVESTER_SSLTCP
                );
            }
        }
        catch (ComponentException ce)
        {
            Log.error( "An exception occurred when loading the plugin: " +
                "the component could not be added.", ce );
            this.componentManager = null;
            this.component = null;
            this.subdomain = null;
        }
    }

    /**
     * Returns the <tt>Component</tt> that has been registered by this plugin.
     * This wraps the Videobridge service.
     *
     * When the plugin is not running, <tt>null</tt> will be returned.
     *
     * @return The Videobridge component, or <tt>null</tt> when not running.
     */
    public ComponentImpl getComponent()
    {
        return component;
    }

    /**
     * Checks whether we have folder with extracted natives, if missing
     * find the appropriate jar file and extract them. Normally this is
     * done once when plugin is installed or updated.
     * If folder with natives exist add it to the java.library.path so
     * libjitsi can use those native libs.
     */
    private void checkNatives() throws Exception
    {
        // Find the root path of the class that will be our plugin lib folder.
        String binaryPath =
            (new URL(ComponentImpl.class.getProtectionDomain()
                .getCodeSource().getLocation(), ".")).openConnection()
                .getPermission().getName();

        File pluginJarfile = new File(binaryPath);
        File nativeLibFolder =
            new File(pluginJarfile.getParentFile(), "native");

        if(!nativeLibFolder.exists())
        {
            // lets find the appropriate jar file to extract and
            // extract it
            String jarFileSuffix = null;
            if ( OSUtils.IS_LINUX32 )
            {
                jarFileSuffix = "-native-linux-32.jar";
            }
            else if ( OSUtils.IS_LINUX64 )
            {
                jarFileSuffix = "-native-linux-64.jar";
            }
            else if ( OSUtils.IS_WINDOWS32 )
            {
                jarFileSuffix = "-native-windows-32.jar";
            }
            else if ( OSUtils.IS_WINDOWS64 )
            {
                jarFileSuffix = "-native-windows-64.jar";
            }
            else if ( OSUtils.IS_MAC )
            {
                jarFileSuffix = "-native-macosx.jar";
            }

            if ( jarFileSuffix == null )
            {
                Log.warn( "Unable to determine what the native libraries are " +
                    "for this OS." );
            }
            else if ( nativeLibFolder.mkdirs() )
            {
                String nativeLibsJarPath = pluginJarfile.getCanonicalPath();
                nativeLibsJarPath = nativeLibsJarPath.replaceFirst( "\\.jar",
                    jarFileSuffix );
                Log.debug("Applicable native jar: '{}'", nativeLibsJarPath);
                JarFile jar = new JarFile( nativeLibsJarPath );
                Enumeration en = jar.entries();
                while ( en.hasMoreElements() )
                {
                    try
                    {
                        JarEntry jarEntry = (JarEntry) en.nextElement();
                        if ( jarEntry.isDirectory() || jarEntry.getName()
                                                            .contains( "/" ) )
                        {
                            // Skip everything that's not in the root of the
                            // jar-file.
                            continue;
                        }
                        final File extractedFile = new File( nativeLibFolder,
                                                        jarEntry.getName() );
                        Log.debug( "Copying file '{}' from native library " +
                            "into '{}'.", jarEntry, extractedFile );

                        try ( InputStream is = jar.getInputStream( jarEntry );
                              FileOutputStream fos = new FileOutputStream(
                                  extractedFile ) )
                        {
                            while ( is.available() > 0 )
                            {
                                fos.write( is.read() );
                            }
                        }
                    }
                    catch ( Throwable t )
                    {
                        Log.warn( "An unexpected error occurred while copying" +
                            " native libraries.", t );
                    }
                }
                Log.info( "Native lib folder created and natives extracted" );
            }
            else
            {
                Log.warn( "Unable to create native lib folder." );
            }
        }
        else
            Log.info("Native lib folder already exist.");

        String newLibPath =
            nativeLibFolder.getCanonicalPath() + File.pathSeparator +
                System.getProperty("java.library.path");

        System.setProperty("java.library.path", newLibPath);

        // this will reload the new setting
        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(System.class.getClassLoader(), null);
    }

    /**
     * A property was set. The parameter map <tt>params</tt> will contain the
     * the value of the property under the key <tt>value</tt>.
     *
     * @param property the name of the property.
     * @param params event parameters.
     */
    public void propertySet(String property, Map params)
    {
        switch ( property )
        {
            case INTERFACES_ALLOWED_PROPERTY_NAME:
                System.setProperty( StackProperties.ALLOWED_INTERFACES, String.join( ";", (List<String>) params.get("value") ) );
                break;

            case INTERFACES_BLOCKED_PROPERTY_NAME:
                System.setProperty( StackProperties.BLOCKED_INTERFACES, String.join( ";", (List<String>) params.get("value") ) );
                break;

            case ADDRESSES_ALLOWED_PROPERTY_NAME:
                System.setProperty( StackProperties.ALLOWED_ADDRESSES, String.join( ";", (List<String>) params.get("value") ) );
                break;

            case ADDRESSES_BLOCKED_PROPERTY_NAME:
                System.setProperty( StackProperties.BLOCKED_ADDRESSES, String.join( ";", (List<String>) params.get("value") ) );
                break;

            case AWS_HARVESTER_CONFIG_PROPERTY_NAME:
                switch ( (String) params.get( "value" ) ) {
                    case "disabled":
                        System.setProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME, "true" );
                        System.clearProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME );
                        break;
                    case "forced":
                        System.setProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME, "false" );
                        System.setProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME, "true" );
                        break;
                    default:
                        System.clearProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME );
                        System.clearProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME );
                        break;
                }
                break;

            case STUN_HARVESTER_ADDRESS_PROPERTY_NAME:
            case STUN_HARVESTER_PORT_PROPERTY_NAME: // intended fall-through;
                final String stunAddress = JiveGlobals.getProperty( STUN_HARVESTER_ADDRESS_PROPERTY_NAME );
                final String stunPort = JiveGlobals.getProperty( STUN_HARVESTER_PORT_PROPERTY_NAME );
                // Only set when both address and port are defined.
                if ( stunAddress != null && !stunAddress.isEmpty() && stunPort != null && !stunPort.isEmpty() )
                {
                    System.setProperty( MappingCandidateHarvesters.STUN_MAPPING_HARVESTER_ADDRESSES_PNAME, stunAddress + ":" + stunPort );
                }
                break;

            case MANUAL_HARVESTER_LOCAL_PROPERTY_NAME:
                System.setProperty( MappingCandidateHarvesters.NAT_HARVESTER_LOCAL_ADDRESS_PNAME, (String) params.get( "value" ) );
                break;

            case MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME:
                System.setProperty( MappingCandidateHarvesters.NAT_HARVESTER_PUBLIC_ADDRESS_PNAME, (String) params.get( "value" ) );
                break;

            case SINGLE_PORT_ENABLED_PROPERTY_NAME: // intended fall-through
            case SINGLE_PORT_NUMBER_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.SINGLE_PORT_HARVESTER_PORT,
                    JiveGlobals.getBooleanProperty( SINGLE_PORT_ENABLED_PROPERTY_NAME, true )
                        ? JiveGlobals.getIntProperty( SINGLE_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.SINGLE_PORT_DEFAULT_VALUE)
                        : -1
                );
                break;

            case MINMAX_PORT_ENABLED_PROPERTY_NAME:
                System.setProperty( StackProperties.USE_DYNAMIC_HOST_HARVESTER, Boolean.toString( JiveGlobals.getBooleanProperty( MINMAX_PORT_ENABLED_PROPERTY_NAME, true ) ) );
                break;

            case MAX_PORT_NUMBER_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    DefaultStreamConnector.MAX_PORT_NUMBER_PROPERTY_NAME,
                    (String) params.get( "value" )
                );

                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
                );
                break;

            case MIN_PORT_NUMBER_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    DefaultStreamConnector.MIN_PORT_NUMBER_PROPERTY_NAME,
                    (String) params.get( "value" )
                );

                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
                );
                break;

            case TCP_ENABLED_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.DISABLE_TCP_HARVESTER,
                    !Boolean.parseBoolean( (String) params.get( "value" ) )
                );
                break;

            case TCP_PORT_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.TCP_HARVESTER_PORT,
                    (String) params.get( "value" )
                );
                break;

            case TCP_MAPPED_PORT_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.TCP_HARVESTER_MAPPED_PORT,
                    (String) params.get( "value" )
                );
                break;

            case TCP_SSLTCP_ENABLED_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.TCP_HARVESTER_SSLTCP,
                    Boolean.parseBoolean( (String) params.get( "value" ) )
                );
                break;
        }
    }

    /**
     * A property was deleted.
     *
     * @param property the name of the property deleted.
     * @param params event parameters.
     */
    public void propertyDeleted(String property, Map params)
    {
        switch ( property )
        {
            case INTERFACES_ALLOWED_PROPERTY_NAME:
                System.clearProperty( StackProperties.ALLOWED_INTERFACES );
                break;

            case INTERFACES_BLOCKED_PROPERTY_NAME:
                System.clearProperty( StackProperties.BLOCKED_INTERFACES );
                break;

            case ADDRESSES_ALLOWED_PROPERTY_NAME:
                System.clearProperty( StackProperties.ALLOWED_ADDRESSES );
                break;

            case ADDRESSES_BLOCKED_PROPERTY_NAME:
                System.clearProperty( StackProperties.BLOCKED_ADDRESSES );
                break;

            case AWS_HARVESTER_CONFIG_PROPERTY_NAME:
                System.clearProperty( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME );
                System.clearProperty( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME );
                break;

            case STUN_HARVESTER_ADDRESS_PROPERTY_NAME:
            case STUN_HARVESTER_PORT_PROPERTY_NAME: // intended fall-through;
                final String stunAddress = JiveGlobals.getProperty( STUN_HARVESTER_ADDRESS_PROPERTY_NAME );
                final String stunPort = JiveGlobals.getProperty( STUN_HARVESTER_PORT_PROPERTY_NAME );
                // Only delete when both address and port are gone.
                if ( stunAddress == null && stunPort == null )
                {
                    System.clearProperty( MappingCandidateHarvesters.STUN_MAPPING_HARVESTER_ADDRESSES_PNAME );
                }
                break;

            case MANUAL_HARVESTER_LOCAL_PROPERTY_NAME:
                System.clearProperty( MappingCandidateHarvesters.NAT_HARVESTER_LOCAL_ADDRESS_PNAME );
                break;

            case MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME:
                System.clearProperty( MappingCandidateHarvesters.NAT_HARVESTER_PUBLIC_ADDRESS_PNAME );
                break;

            case SINGLE_PORT_ENABLED_PROPERTY_NAME: // intended fall-through
            case SINGLE_PORT_NUMBER_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    IceUdpTransportManager.SINGLE_PORT_HARVESTER_PORT,
                    JiveGlobals.getBooleanProperty( SINGLE_PORT_ENABLED_PROPERTY_NAME, true )
                        ? JiveGlobals.getIntProperty( SINGLE_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.SINGLE_PORT_DEFAULT_VALUE)
                        : -1
                );
                break;

            case MINMAX_PORT_ENABLED_PROPERTY_NAME:
                System.setProperty( StackProperties.USE_DYNAMIC_HOST_HARVESTER, Boolean.toString( JiveGlobals.getBooleanProperty( MINMAX_PORT_ENABLED_PROPERTY_NAME, true ) ) );
                break;

            case MAX_PORT_NUMBER_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    DefaultStreamConnector.MAX_PORT_NUMBER_PROPERTY_NAME,
                    String.valueOf( RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE ) // note that ĺibjitsi's default (6000) is different from JVB's default (20000). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                );

                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE) // note that ĺibjitsi's default (6000) is different from JVB's default (20000). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                );

                break;

            case MIN_PORT_NUMBER_PROPERTY_NAME:
                LibJitsi.getConfigurationService().setProperty(
                    DefaultStreamConnector.MIN_PORT_NUMBER_PROPERTY_NAME,
                    String.valueOf( RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE ) // note that ĺibjitsi's default (5000) is different from JVB's default (10001). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                );

                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE), // note that ĺibjitsi's default (5000) is different from JVB's default (10001). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
                );

                break;

            case TCP_ENABLED_PROPERTY_NAME:
                LibJitsi.getConfigurationService().removeProperty( IceUdpTransportManager.DISABLE_TCP_HARVESTER );
                break;

            case TCP_PORT_PROPERTY_NAME:
                LibJitsi.getConfigurationService().removeProperty( IceUdpTransportManager.TCP_HARVESTER_PORT );
                break;

            case TCP_MAPPED_PORT_PROPERTY_NAME:
                LibJitsi.getConfigurationService().removeProperty( IceUdpTransportManager.TCP_HARVESTER_MAPPED_PORT );
                break;

            case TCP_SSLTCP_ENABLED_PROPERTY_NAME:
                LibJitsi.getConfigurationService().removeProperty( IceUdpTransportManager.TCP_HARVESTER_SSLTCP );
                break;
        }
    }

    /**
     * An XML property was set. The parameter map <tt>params</tt> will contain
     * the value of the property under the key <tt>value</tt>.
     *
     * @param property the name of the property.
     * @param params event parameters.
     */
    public void xmlPropertySet(String property, Map params)
    {
        propertySet(property, params);
    }

    /**
     * An XML property was deleted.
     *
     * @param property the name of the property.
     * @param params event parameters.
     */
    public void xmlPropertyDeleted(String property, Map params)
    {
        propertyDeleted(property, params);
    }
}
