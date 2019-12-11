package org.jitsi.videobridge.openfire;

import org.ice4j.StackProperties;
import org.ice4j.ice.harvest.MappingCandidateHarvesters;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.DefaultStreamConnector;
import org.jitsi.videobridge.IceUdpTransportManager;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Exposes various bits of Jitsi configuration.
 *
 * Various Jitsi Components use different mechanisms to get their configuration. Notably, LibJitsi uses a configuration
 * service implementation, that's typically used in a OSGi environment, while ice4j uses system properties.
 *
 * This class wraps each of these mechanisms.
 *
 * This implementation preserves the configured values when initially used. This allows the implementation to detect
 * configuration changes, that are not yet applied.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class RuntimeConfiguration
{
    private static final Logger Log = LoggerFactory.getLogger( RuntimeConfiguration.class );

    /**
     * The default UDP port value used when multiplexing multiple media streams.
     */
    public static final int SINGLE_PORT_DEFAULT_VALUE = 10000; // should be equal to org.jitsi.videobridge.IceUdpTransportManager.SINGLE_PORT_DEFAULT_VALUE

    /**
     * The minimum port number default value.
     */
    public static final int MIN_PORT_DEFAULT_VALUE = 10001;

    /**
     * The maximum port number default value.
     */
    public static final int MAX_PORT_DEFAULT_VALUE = 20000;

    /**
     * The default setting for _disabling_ the TCP connectivity.
     */
    public static final boolean DISABLE_TCP_HARVESTER_DEFAULT_VALUE = false; // should be equal to the default behavior as implemented in org.jitsi.videobridge.IceUdpTransportManager

    /**
     * The default value for SslTcp wrapping.
     */
    public static final boolean SSLTCP_TCP_HARVESTER_DEFAULT_VALUE = true; // should be equal to org.jitsi.videobridge.IceUdpTransportManager.TCP_HARVESTER_SSLTCP_DEFAULT

    /**
     * Changes to the allowed network interfaces require a restart of the plugin to take effect.
     * The value that is currently in use is equal to the value that was configured when this plugin got initialized,
     * which is what is stored in this field.
     */
    private static final String ALLOWED_INTERFACES_AT_STARTUP = RuntimeConfiguration.getAllowedInterfaces();

    /**
     * Changes to the blocked network interfaces require a restart of the plugin to take effect.
     * The value that is currently in use is equal to the value that was configured when this plugin got initialized,
     * which is what is stored in this field.
     */
    private static final String BLOCKED_INTERFACES_AT_STARTUP = RuntimeConfiguration.getBlockedInterfaces();

    /**
     * Changes to the allowed network addresses require a restart of the plugin to take effect.
     * The value that is currently in use is equal to the value that was configured when this plugin got initialized,
     * which is what is stored in this field.
     */
    private static final String ALLOWED_ADDRESSES_AT_STARTUP = RuntimeConfiguration.getAllowedAddresses();

    /**
     * Changes to the blocked network addresses require a restart of the plugin to take effect.
     * The value that is currently in use is equal to the value that was configured when this plugin got initialized,
     * which is what is stored in this field.
     */
    private static final String BLOCKED_ADDRESSES_AT_STARTUP = RuntimeConfiguration.getBlockedAddresses();

    /**
     * Changes to availability of single-port multiplexing of UDP data require a restart of the plugin to take effect.
     * The value that is currently in use is equal to the value that was configured when this plugin got initialized,
     * which is what is stored in this field.
     */
    private static final boolean WAS_SINGLE_PORT_ENABLED_AT_STARTUP = RuntimeConfiguration.isSinglePortEnabled();

    /**
     * Changes to port number configuration require a restart of the plugin to take effect.
     * The single port number value that is currently in use is equal to the port number
     * that was configured when this plugin got initialized, which is what is stored in this field.
     */
    private static final int SINGLE_PORT_AT_STARTUP = RuntimeConfiguration.getSinglePort();

    /**
     * Changes to availability of dynamic port usage of UDP data require a restart of the plugin to take effect.
     * The value that is currently in use is equal to the value that was configured when this plugin got initialized,
     * which is what is stored in this field.
     */
    private static final boolean WAS_MINMAX_PORT_ENABLED_AT_STARTUP = RuntimeConfiguration.isMinMaxPortEnabled();

    /**
     * Changes to port number configuration require a restart of the plugin to take effect.
     * The minimum port number value that is currently in use is equal to the port number
     * that was configured when this plugin got initialized, which is what is stored in this field.
     */
    private static final int MIN_PORT_AT_STARTUP = RuntimeConfiguration.getMinPort();

    /**
     * Changes to port number configuration require a restart of the plugin to take effect.
     * The maximum port number value that is currently in use is equal to the port number
     * that was configured when this plugin got initialized, which is what is stored in this field.
     */
    private static final int MAX_PORT_AT_STARTUP = RuntimeConfiguration.getMaxPort();

    /**
     * Changes to TCP harvester availability require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field.
     */
    private static final boolean WAS_TCP_PORT_ENABLED_AT_STARTUP = RuntimeConfiguration.isTcpEnabled();

    /**
     * Changes to TCP harvester availability require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field. Note: can be null.
     */
    private static final Integer TCP_PORT_AT_STARTUP = RuntimeConfiguration.getTcpPort();

    /**
     * Changes to TCP harvester availability require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field. Note: can be null.
     */
    private static final Integer TCP_MAPPED_PORT_AT_STARTUP = RuntimeConfiguration.getTcpMappedPort();

    /**
     * Changes to TCP harvester availability require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field.
     */
    private static final boolean SSLTCP_ENABLED_AT_STARTUP = RuntimeConfiguration.isSslTcpEnabled();

    /**
     * Changes to AWS harvester availability require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field.
     */
    private static final boolean WAS_AWS_MAPPING_HARVESTER_ENABLED_AT_STARTUP = RuntimeConfiguration.isAWSMappingHarvesterEnabled();

    /**
     * Changes to AWS harvester forced usage require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field.
     */
    private static final boolean WAS_AWS_MAPPING_HARVESTER_FORCED_AT_STARTUP = RuntimeConfiguration.isAWSMappingHarvesterForced();

    /**
     * Changes to collection of STUN servers used for network address mapping require a restart of the plugin to take effect.
     * The list currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field.
     */
    private static final List<InetSocketAddress> STUN_MAPPING_HARVESTER_ADDRESSES_AT_STARTUP = RuntimeConfiguration.getSTUNMappingHarvesterAddresses();

    /**
     * Changes to manually provided local network address value require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field. Note: can be null.
     */
    private static final String MANUAL_MAPPED_LOCAL_ADDRESS_AT_STARTUP = RuntimeConfiguration.getManualMappedLocalAddress();

    /**
     * Changes to manually provided public network address value require a restart of the plugin to take effect.
     * The value currently used is equal to the value that was configured when this
     * plugin got initialized, which is what is stored in this field. Note: can be null.
     */
    private static final String MANUAL_MAPPED_PUBLIC_ADDRESS_AT_STARTUP = RuntimeConfiguration.getManualMappedPublicAddress();

    /**
     * Returns the (;-separated) string of interfaces that are allowed to be used, or null if all of them are allowed.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A string of interface names, possibly null.
     */
    public static String getAllowedInterfaces()
    {
        return StackProperties.getString( StackProperties.ALLOWED_INTERFACES );
    }

    /**
     * Returns the (;-separated) string of interfaces that are blocked from usage.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A string of interface names, possibly null.
     */
    public static String getBlockedInterfaces()
    {
        return StackProperties.getString( StackProperties.BLOCKED_INTERFACES );
    }

    /**
     * Returns the (;-separated) string of addresses that are allowed to be used, or null if all of them are allowed.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A string of interface names, possibly null.
     */
    public static String getAllowedAddresses()
    {
        return StackProperties.getString( StackProperties.ALLOWED_ADDRESSES );
    }

    /**
     * Returns the (;-separated) string of addresses that are blocked from usage.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A string of interface names, possibly null.
     */
    public static String getBlockedAddresses()
    {
        return StackProperties.getString( StackProperties.BLOCKED_ADDRESSES );
    }

    /**
     * Verifies if Jitsi Videobridge allows to multiplex media data over a single UDP port.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A boolean value that indicates if the videobridge is configured to allow single-port UDP multiplexing..
     */
    public static boolean isSinglePortEnabled()
    {
        return getSinglePort() != -1;
    }

    /**
     * Returns the UDP port number that is used for multiplexing multiple media streams.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return a UDP port number value.
     */
    public static int getSinglePort()
    {
        return LibJitsi.getConfigurationService().getInt( IceUdpTransportManager.SINGLE_PORT_HARVESTER_PORT, SINGLE_PORT_DEFAULT_VALUE );
    }

    /**
     * Verifies if Jitsi Videobridge allows to use dynamically allocated ports ofr media streaming.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A boolean value that indicates if the videobridge is configured to allow dynamically allocated ports.
     */
    public static boolean isMinMaxPortEnabled()
    {
        return StackProperties.getBoolean( StackProperties.USE_DYNAMIC_HOST_HARVESTER,true );
    }

    /**
     * When multiplexing of media streams is not possible, the videobridge will automatically fallback to using
     * dynamically allocated UDP ports in a specific range. This method returns the upper-bound of that range.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A UDP port number value.
     */
    public static int getMaxPort()
    {
        return LibJitsi.getConfigurationService().getInt( DefaultStreamConnector.MAX_PORT_NUMBER_PROPERTY_NAME, MAX_PORT_DEFAULT_VALUE );
    }

    /**
     * When multiplexing of media streams is not possible, the videobridge will automatically fallback to using
     * dynamically allocated UDP ports in a specific range. This method returns the lower-bound of that range.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A UDP port number value.
     */
    public static int getMinPort()
    {
        return LibJitsi.getConfigurationService().getInt( DefaultStreamConnector.MIN_PORT_NUMBER_PROPERTY_NAME, MIN_PORT_DEFAULT_VALUE );
    }

    /**
     * Jitsi Videobridge can accept and route RTP traffic over TCP. If enabled, TCP addresses will automatically be
     * returned as ICE candidates via COLIBRI. Typically, the point of using TCP instead of UDP is to simulate HTTP
     * traffic in a number of environments where it is the only allowed form of communication.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return A boolean value that indicates if the videobridge is configured to allow RTP traffic over TCP.
     */
    public static boolean isTcpEnabled()
    {
        // Jitsi uses a 'disable' option here. We should negate their setting.
        return !LibJitsi.getConfigurationService().getBoolean( IceUdpTransportManager.DISABLE_TCP_HARVESTER, DISABLE_TCP_HARVESTER_DEFAULT_VALUE );
    }

    /**
     * Returns the TCP port number that is used for multiplexing multiple media streams over TCP, or null if the default
     * is to be used.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return a TCP port number value, possibly null.
     */
    public static Integer getTcpPort()
    {
        final int value = LibJitsi.getConfigurationService().getInt( IceUdpTransportManager.TCP_HARVESTER_PORT, -1 );

        if ( value == -1 )
        {
            return null;
        }

        return value;
    }

    /**
     * Returns the TCP port number mapping that is used for multiplexing multiple media streams over TCP, or null if the
     * default is to be used.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return a TCP port number value, possibly null.
     */
    public static Integer getTcpMappedPort()
    {
        final int value = LibJitsi.getConfigurationService().getInt( IceUdpTransportManager.TCP_HARVESTER_MAPPED_PORT, -1 );

        if ( value == -1 )
        {
            return null;
        }

        return value;
    }

    /**
     * Indicates if the media stream that is received over TCP is expected to be wrapped in (pseudo) TLS.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return true if pseudo TCP wrapping is enabled, otherwise false.
     */
    public static boolean isSslTcpEnabled()
    {
        return LibJitsi.getConfigurationService().getBoolean( IceUdpTransportManager.TCP_HARVESTER_SSLTCP, SSLTCP_TCP_HARVESTER_DEFAULT_VALUE );
    }

    /**
     * Indicates if the automatic network address mapping for AWS is enabled.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return true if the automatic network address mapping for AWS is enabled, otherwise false.
     */
    public static boolean isAWSMappingHarvesterEnabled()
    {
        final boolean disabled = StackProperties.getBoolean( MappingCandidateHarvesters.DISABLE_AWS_HARVESTER_PNAME, false );
        return !disabled;
    }

    /**
     * Indicates if the automatic network address mapping for AWS is forced (used even when the bridge does not detect
     * it's running on AWS).
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return true if the automatic network address mapping for AWS is forced, otherwise false.
     */
    public static boolean isAWSMappingHarvesterForced()
    {
        if ( isAWSMappingHarvesterEnabled() )
        {
            return StackProperties.getBoolean( MappingCandidateHarvesters.FORCE_AWS_HARVESTER_PNAME, false );
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns a list of STUN server addresses, used to perform network address mapping.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return a list of address/port pairs, possibly empty, but never null.
     */
    public static List<InetSocketAddress> getSTUNMappingHarvesterAddresses()
    {
        final List<InetSocketAddress> results = new ArrayList<>();
        final String[] addresses = StackProperties.getStringArray( MappingCandidateHarvesters.STUN_MAPPING_HARVESTER_ADDRESSES_PNAME, "," );
        if( addresses == null )
        {
            return results;
        }

        for ( String address : addresses )
        {
            if ( address != null )
            {
                try
                {
                    final String[] parts = address.split( ":" );
                    final InetSocketAddress result = new InetSocketAddress( parts[ 0 ], Integer.parseInt( parts[ 1 ] ) );
                    results.add( result );
                }
                catch ( Throwable t )
                {
                    Log.warn( "Unable to parse STUN Mappping Harvester address '{}'. This value will be ignored.", address );
                }
            }
        }
        return results;
    }

    /**
     * Returns a manually provided network address that represents the 'local' (or 'private') address. Used for network
     * address mapping.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return a network address value, possibly null.
     */
    public static String getManualMappedLocalAddress()
    {
        return StackProperties.getString( MappingCandidateHarvesters.NAT_HARVESTER_LOCAL_ADDRESS_PNAME );
    }

    /**
     * Returns a manually provided network address that represents the 'public' address. Used for network
     * address mapping.
     *
     * Note that this method returns the configured value, which might differ from the configuration that is
     * in effect (as configuration changes require a restart to be taken into effect).
     *
     * @return a network address value, possibly null.
     */
    public static String getManualMappedPublicAddress()
    {
        return StackProperties.getString( MappingCandidateHarvesters.NAT_HARVESTER_PUBLIC_ADDRESS_PNAME );
    }

    /**
     * Checks if the plugin requires a restart to apply pending configuration changes.
     *
     * @return true if a restart is needed to apply pending changes, otherwise false.
     */
    public static boolean restartNeeded()
    {
        return
           ( ALLOWED_INTERFACES_AT_STARTUP == null && RuntimeConfiguration.getAllowedInterfaces() != null ) || ( ALLOWED_INTERFACES_AT_STARTUP != null && !ALLOWED_INTERFACES_AT_STARTUP.equals( RuntimeConfiguration.getAllowedInterfaces() ) )
        || ( BLOCKED_INTERFACES_AT_STARTUP == null && RuntimeConfiguration.getBlockedInterfaces() != null ) || ( BLOCKED_INTERFACES_AT_STARTUP != null && !BLOCKED_INTERFACES_AT_STARTUP.equals( RuntimeConfiguration.getBlockedInterfaces() ) )
        || ( ALLOWED_ADDRESSES_AT_STARTUP == null && RuntimeConfiguration.getAllowedAddresses() != null ) || ( ALLOWED_ADDRESSES_AT_STARTUP != null && !ALLOWED_ADDRESSES_AT_STARTUP.equals( RuntimeConfiguration.getAllowedAddresses() ) )
        || ( BLOCKED_ADDRESSES_AT_STARTUP == null && RuntimeConfiguration.getBlockedAddresses() != null ) || ( BLOCKED_ADDRESSES_AT_STARTUP != null && !BLOCKED_ADDRESSES_AT_STARTUP.equals( RuntimeConfiguration.getBlockedAddresses() ) )
        || WAS_SINGLE_PORT_ENABLED_AT_STARTUP != RuntimeConfiguration.isSinglePortEnabled()
        || SINGLE_PORT_AT_STARTUP != RuntimeConfiguration.getSinglePort()
        || WAS_MINMAX_PORT_ENABLED_AT_STARTUP != RuntimeConfiguration.isMinMaxPortEnabled()
        || MAX_PORT_AT_STARTUP != RuntimeConfiguration.getMaxPort()
        || MIN_PORT_AT_STARTUP != RuntimeConfiguration.getMinPort()
        || WAS_TCP_PORT_ENABLED_AT_STARTUP != RuntimeConfiguration.isTcpEnabled()
        || ( TCP_PORT_AT_STARTUP == null && RuntimeConfiguration.getTcpPort() != null) || ( TCP_PORT_AT_STARTUP != null && !TCP_PORT_AT_STARTUP.equals( RuntimeConfiguration.getTcpPort() ) )
        || ( TCP_MAPPED_PORT_AT_STARTUP == null && RuntimeConfiguration.getTcpMappedPort() != null) || ( TCP_MAPPED_PORT_AT_STARTUP != null && !TCP_MAPPED_PORT_AT_STARTUP.equals( RuntimeConfiguration.getTcpMappedPort() ) )
        || SSLTCP_ENABLED_AT_STARTUP != RuntimeConfiguration.isSslTcpEnabled()
        || WAS_AWS_MAPPING_HARVESTER_ENABLED_AT_STARTUP != RuntimeConfiguration.isAWSMappingHarvesterEnabled()
        || WAS_AWS_MAPPING_HARVESTER_FORCED_AT_STARTUP != RuntimeConfiguration.isAWSMappingHarvesterForced()
        || !STUN_MAPPING_HARVESTER_ADDRESSES_AT_STARTUP.equals( RuntimeConfiguration.getSTUNMappingHarvesterAddresses() )
        || (MANUAL_MAPPED_LOCAL_ADDRESS_AT_STARTUP == null && RuntimeConfiguration.getManualMappedLocalAddress() != null) || (MANUAL_MAPPED_LOCAL_ADDRESS_AT_STARTUP != null && !MANUAL_MAPPED_LOCAL_ADDRESS_AT_STARTUP.equals( RuntimeConfiguration.getManualMappedLocalAddress() ) )
        || (MANUAL_MAPPED_PUBLIC_ADDRESS_AT_STARTUP == null && RuntimeConfiguration.getManualMappedPublicAddress() != null) || (MANUAL_MAPPED_PUBLIC_ADDRESS_AT_STARTUP != null && !MANUAL_MAPPED_PUBLIC_ADDRESS_AT_STARTUP.equals( RuntimeConfiguration.getManualMappedPublicAddress() ) );
    }
}
