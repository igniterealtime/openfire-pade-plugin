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
     * The name of the Openfire property that contains the JVB port used for plain text websockets
     */
    public static final String PLAIN_PORT_NUMBER_PROPERTY_NAME = "ofmeet.websockets.plainport";

    /**
     * The name of the Openfire property that contains the JVB port advertised as the public websockets
     */
    public static final String PUBLIC_PORT_NUMBER_PROPERTY_NAME = "ofmeet.websockets.publicport";

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
     * Destroys this <tt>Plugin</tt> i.e. releases the resources acquired by
     * this <tt>Plugin</tt> throughout its life up until now and prepares it for
     * garbage collection.
     *
     * @see Plugin#destroyPlugin()
     */
    public void destroyPlugin()
    {
        PropertyEventDispatcher.removeListener(this);
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
                    System.setProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER", "true" );
                    System.clearProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
                    break;
                case "forced":
                    System.setProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER", "false" );
                    System.setProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER", "true" );
                    break;
                default:
                    System.clearProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER" );
                    System.clearProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
                    break;
            }
        }
        else
        {
            System.clearProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER" );
            System.clearProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
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
            System.setProperty( "org.jitsi.videobridge.STUN_MAPPING_HARVESTER_ADDRESSES", stunAddress + ":" + stunPort );
        } else {
            System.clearProperty( "org.jitsi.videobridge.STUN_MAPPING_HARVESTER_ADDRESSES" );
        }

        System.setProperty( "org.jitsi.videobridge.media.USE_DYNAMIC_HOST_HARVESTER", String.valueOf(JiveGlobals.getBooleanProperty( MINMAX_PORT_ENABLED_PROPERTY_NAME, false) ) );

        try
        {
            System.setProperty(
                SINGLE_PORT_NUMBER_PROPERTY_NAME,
                JiveGlobals.getBooleanProperty( SINGLE_PORT_ENABLED_PROPERTY_NAME, true )
                    ? JiveGlobals.getProperty( SINGLE_PORT_NUMBER_PROPERTY_NAME, String.valueOf(RuntimeConfiguration.SINGLE_PORT_DEFAULT_VALUE))
                    : "-1"
            );

            System.setProperty(
                MAX_PORT_NUMBER_PROPERTY_NAME,
                JiveGlobals.getProperty(MAX_PORT_NUMBER_PROPERTY_NAME, String.valueOf(RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE))
            );

            System.setProperty(
                MIN_PORT_NUMBER_PROPERTY_NAME,
                JiveGlobals.getProperty(MIN_PORT_NUMBER_PROPERTY_NAME, String.valueOf(RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE))
            );
/*
            // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
            TransportManager.portTracker.setRange(
                JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
            );
*/
            if ( JiveGlobals.getProperty( TCP_ENABLED_PROPERTY_NAME ) != null ) {
                System.setProperty(TCP_ENABLED_PROPERTY_NAME,  String.valueOf(JiveGlobals.getBooleanProperty( TCP_ENABLED_PROPERTY_NAME) ));
            } else {
                System.clearProperty(TCP_ENABLED_PROPERTY_NAME);
            }

            if ( JiveGlobals.getProperty( TCP_PORT_PROPERTY_NAME ) != null) {
                System.setProperty(TCP_PORT_PROPERTY_NAME, JiveGlobals.getProperty( TCP_PORT_PROPERTY_NAME ));
            } else {
                System.clearProperty(TCP_PORT_PROPERTY_NAME);
            }

            if ( JiveGlobals.getProperty( TCP_MAPPED_PORT_PROPERTY_NAME ) != null) {
                System.setProperty(
                    TCP_MAPPED_PORT_PROPERTY_NAME,
                    JiveGlobals.getProperty( TCP_MAPPED_PORT_PROPERTY_NAME )
                );
            } else {
                System.clearProperty(
                    TCP_MAPPED_PORT_PROPERTY_NAME
                );
            }

            if ( JiveGlobals.getProperty( TCP_SSLTCP_ENABLED_PROPERTY_NAME ) != null) {
                System.setProperty(
                    TCP_SSLTCP_ENABLED_PROPERTY_NAME,
                    JiveGlobals.getProperty( TCP_SSLTCP_ENABLED_PROPERTY_NAME )
                );
            } else {
                System.clearProperty(
                    TCP_SSLTCP_ENABLED_PROPERTY_NAME
                );
            }
        }
        catch (Exception ce)
        {
            Log.error( "An exception occurred when loading the plugin", ce );
        }
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
                        System.setProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER", "true" );
                        System.clearProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
                        break;
                    case "forced":
                        System.setProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER", "false" );
                        System.setProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER", "true" );
                        break;
                    default:
                        System.clearProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER" );
                        System.clearProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
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
                    System.setProperty( "org.jitsi.videobridge.STUN_MAPPING_HARVESTER_ADDRESSES", stunAddress + ":" + stunPort );
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
                System.setProperty(
                    SINGLE_PORT_NUMBER_PROPERTY_NAME,
                    JiveGlobals.getBooleanProperty( SINGLE_PORT_ENABLED_PROPERTY_NAME, true )
                        ? JiveGlobals.getProperty( SINGLE_PORT_NUMBER_PROPERTY_NAME, String.valueOf(RuntimeConfiguration.SINGLE_PORT_DEFAULT_VALUE))
                        : "-1"
                );
                break;

            case MINMAX_PORT_ENABLED_PROPERTY_NAME:
                System.setProperty( "org.jitsi.videobridge.media.USE_DYNAMIC_HOST_HARVESTER", Boolean.toString( JiveGlobals.getBooleanProperty( MINMAX_PORT_ENABLED_PROPERTY_NAME, false ) ) );
                break;

            case MAX_PORT_NUMBER_PROPERTY_NAME:
                System.setProperty(
                    MAX_PORT_NUMBER_PROPERTY_NAME,
                    (String) params.get( "value" )
                );
/*
                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
                );
*/
                break;

            case MIN_PORT_NUMBER_PROPERTY_NAME:
                System.setProperty(
                    MIN_PORT_NUMBER_PROPERTY_NAME,
                    (String) params.get( "value" )
                );
/*
                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
                );
*/
                break;

            case TCP_ENABLED_PROPERTY_NAME:
                System.setProperty(
                    TCP_ENABLED_PROPERTY_NAME,
                    String.valueOf(!Boolean.parseBoolean( (String) params.get( "value" ) ))
                );
                break;

            case TCP_PORT_PROPERTY_NAME:
                System.setProperty(
                    TCP_PORT_PROPERTY_NAME,
                    (String) params.get( "value" )
                );
                break;

            case TCP_MAPPED_PORT_PROPERTY_NAME:
                System.setProperty(
                    TCP_MAPPED_PORT_PROPERTY_NAME,
                    (String) params.get( "value" )
                );
                break;

            case TCP_SSLTCP_ENABLED_PROPERTY_NAME:
                System.setProperty(
                    TCP_SSLTCP_ENABLED_PROPERTY_NAME,
                    (String) params.get( "value" )
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
                System.clearProperty( "org.ice4j.ice.harvest.DISABLE_AWS_HARVESTER" );
                System.clearProperty( "org.ice4j.ice.harvest.FORCE_AWS_HARVESTER" );
                break;

            case STUN_HARVESTER_ADDRESS_PROPERTY_NAME:
            case STUN_HARVESTER_PORT_PROPERTY_NAME: // intended fall-through;
                final String stunAddress = JiveGlobals.getProperty( STUN_HARVESTER_ADDRESS_PROPERTY_NAME );
                final String stunPort = JiveGlobals.getProperty( STUN_HARVESTER_PORT_PROPERTY_NAME );
                // Only delete when both address and port are gone.
                if ( stunAddress == null && stunPort == null )
                {
                    System.clearProperty( "org.jitsi.videobridge.STUN_MAPPING_HARVESTER_ADDRESSES" );
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
                System.setProperty(
                    SINGLE_PORT_NUMBER_PROPERTY_NAME,
                    JiveGlobals.getBooleanProperty( SINGLE_PORT_ENABLED_PROPERTY_NAME, true )
                        ? JiveGlobals.getProperty( SINGLE_PORT_NUMBER_PROPERTY_NAME, String.valueOf(RuntimeConfiguration.SINGLE_PORT_DEFAULT_VALUE))
                        : "-1"
                );
                break;

            case MINMAX_PORT_ENABLED_PROPERTY_NAME:
                System.setProperty( "org.jitsi.videobridge.media.USE_DYNAMIC_HOST_HARVESTER", Boolean.toString( JiveGlobals.getBooleanProperty( MINMAX_PORT_ENABLED_PROPERTY_NAME, false ) ) );
                break;

            case MAX_PORT_NUMBER_PROPERTY_NAME:
                System.setProperty(
                    MAX_PORT_NUMBER_PROPERTY_NAME,
                    String.valueOf( RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE ) // note that ĺibjitsi's default (6000) is different from JVB's default (20000). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                );

/*
                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE),
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE) // note that ĺibjitsi's default (6000) is different from JVB's default (20000). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                );
*/
                break;

            case MIN_PORT_NUMBER_PROPERTY_NAME:
                System.setProperty(
                    MIN_PORT_NUMBER_PROPERTY_NAME,
                    String.valueOf( RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE ) // note that ĺibjitsi's default (5000) is different from JVB's default (10001). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                );

/*
                // TODO: The port range is set both for DefaultStreamConnector, but also in the TransportManager. Figure out what's the difference.
                TransportManager.portTracker.setRange(
                    JiveGlobals.getIntProperty(MIN_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MIN_PORT_DEFAULT_VALUE), // note that ĺibjitsi's default (5000) is different from JVB's default (10001). We need to manually set a value (instead of removing the value) to prevent the wrong default to be used!
                    JiveGlobals.getIntProperty(MAX_PORT_NUMBER_PROPERTY_NAME, RuntimeConfiguration.MAX_PORT_DEFAULT_VALUE)
                );
*/
                break;

            case TCP_ENABLED_PROPERTY_NAME:
                System.clearProperty( TCP_ENABLED_PROPERTY_NAME);
                break;

            case TCP_PORT_PROPERTY_NAME:
                System.clearProperty( TCP_PORT_PROPERTY_NAME);
                break;

            case TCP_MAPPED_PORT_PROPERTY_NAME:
                System.clearProperty( TCP_MAPPED_PORT_PROPERTY_NAME);
                break;

            case TCP_SSLTCP_ENABLED_PROPERTY_NAME:
                System.clearProperty( TCP_SSLTCP_ENABLED_PROPERTY_NAME );
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
