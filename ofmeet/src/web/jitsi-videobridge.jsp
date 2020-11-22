<%--
 _ Copyright @ 2015 Atlassian Pty Ltd
 _
 _ Licensed under the Apache License, Version 2.0 (the "License");
 _ you may not use this file except in compliance with the License.
 _ You may obtain a copy of the License at
 _
 _     http://www.apache.org/licenses/LICENSE-2.0
 _
 _ Unless required by applicable law or agreed to in writing, software
 _ distributed under the License is distributed on an "AS IS" BASIS,
 _ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 _ See the License for the specific language governing permissions and
 _ limitations under the License.
--%>
<%@ page import="org.jitsi.videobridge.openfire.*" %>
<%@ page import="org.jivesoftware.util.*" %>
<%@ page import="org.ice4j.ice.harvest.AwsCandidateHarvester" %>
<%@ page import="java.net.NetworkInterface" %>
<%@ page import="org.ice4j.ice.NetworkUtils" %>
<%@ page import="java.util.*" %>
<%@ page import="java.net.InetAddress" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%
    boolean update = request.getParameter("update") != null;
    boolean reset = request.getParameter( "reset" ) != null;

    Map<String, String> errors = new HashMap<>();
    boolean singlePortEnabled, minmaxPortEnabled, tcpEnabled, sslTcpEnabled;
    String stunPort, singlePort, minPort, maxPort, tcpPort, mappedTcpPort;

    if (reset)
    {
        JiveGlobals.deleteProperty( PluginImpl.ADDRESSES_BLOCKED_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.ADDRESSES_ALLOWED_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.AWS_HARVESTER_CONFIG_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.INTERFACES_ALLOWED_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.INTERFACES_BLOCKED_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.MAX_PORT_NUMBER_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.MIN_PORT_NUMBER_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.MINMAX_PORT_ENABLED_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.SINGLE_PORT_ENABLED_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.STUN_HARVESTER_ADDRESS_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.STUN_HARVESTER_PORT_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.TCP_ENABLED_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.TCP_MAPPED_PORT_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.TCP_PORT_PROPERTY_NAME );
        JiveGlobals.deleteProperty( PluginImpl.TCP_SSLTCP_ENABLED_PROPERTY_NAME );
    }
    if (update)
    {
        String allowAllInterfaces = request.getParameter( "allow-all-interfaces" );
        if ( allowAllInterfaces.equalsIgnoreCase( "true" ) )
        {
            JiveGlobals.deleteProperty( PluginImpl.INTERFACES_ALLOWED_PROPERTY_NAME );
            JiveGlobals.deleteProperty( PluginImpl.INTERFACES_BLOCKED_PROPERTY_NAME );
        }
        else
        {
            final List<String> allowed = new ArrayList<>();
            final Enumeration<String> parameterNames = request.getParameterNames();
            while ( parameterNames.hasMoreElements() ) {
                final String parameterName = parameterNames.nextElement();
                if ( parameterName.startsWith( "interface-allowed-" ) ) {
                    allowed.add( parameterName.substring( "interface-allowed-".length() ) );
                }
            }
            JiveGlobals.setProperty( PluginImpl.INTERFACES_ALLOWED_PROPERTY_NAME, allowed );
            JiveGlobals.deleteProperty( PluginImpl.INTERFACES_BLOCKED_PROPERTY_NAME );
        }

        String allowAllAddresses = request.getParameter( "allow-all-addresses" );
        if ( allowAllAddresses.equalsIgnoreCase( "true" ) )
        {
            JiveGlobals.deleteProperty( PluginImpl.ADDRESSES_ALLOWED_PROPERTY_NAME );
            JiveGlobals.deleteProperty( PluginImpl.ADDRESSES_BLOCKED_PROPERTY_NAME );
        }
        else
        {
            final List<String> allowed = new ArrayList<>();
            final Enumeration<String> parameterNames = request.getParameterNames();
            while ( parameterNames.hasMoreElements() ) {
                final String parameterName = parameterNames.nextElement();
                if ( parameterName.startsWith( "address-allowed-" ) ) {
                    allowed.add( parameterName.substring( "address-allowed-".length() ) );
                }
            }
            JiveGlobals.setProperty( PluginImpl.ADDRESSES_ALLOWED_PROPERTY_NAME, allowed );
            JiveGlobals.deleteProperty( PluginImpl.ADDRESSES_BLOCKED_PROPERTY_NAME );
        }

        String aws = request.getParameter( "aws" );
        if ( aws == null || aws.trim().isEmpty() ) {
            JiveGlobals.deleteProperty( PluginImpl.AWS_HARVESTER_CONFIG_PROPERTY_NAME );
        } else {
            JiveGlobals.setProperty( PluginImpl.AWS_HARVESTER_CONFIG_PROPERTY_NAME, aws.trim() );
        }

        String stunAddress = request.getParameter( "stunAddress" );
        if ( stunAddress == null || stunAddress.trim().isEmpty() ) {
            JiveGlobals.deleteProperty( PluginImpl.STUN_HARVESTER_ADDRESS_PROPERTY_NAME );
        } else {
            JiveGlobals.setProperty( PluginImpl.STUN_HARVESTER_ADDRESS_PROPERTY_NAME, stunAddress.trim() );
        }

        stunPort = request.getParameter( "stunPort" );
        if ( stunPort == null || stunPort.trim().isEmpty() ) {
            JiveGlobals.deleteProperty( PluginImpl.STUN_HARVESTER_PORT_PROPERTY_NAME );
        } else {
            try {
                int port = Integer.valueOf(stunPort.trim());
                if(port >= 1 && port <= 65535) {
                    JiveGlobals.setProperty( PluginImpl.STUN_HARVESTER_PORT_PROPERTY_NAME, stunPort.trim() );
                } else {
                    throw new NumberFormatException( "out of range port" );
                }
            } catch( Exception e ) {
                errors.put( "stunPort", "Invalid port value" );
            }
        }

        final String manualMappedLocalAddress = request.getParameter( "manualMappedLocalAddress" );
        if ( manualMappedLocalAddress == null || manualMappedLocalAddress.trim().isEmpty() ) {
            JiveGlobals.deleteProperty( PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME );
        } else {
            JiveGlobals.setProperty( PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME, manualMappedLocalAddress.trim() );
        }

        final String manualMappedPublicAddress = request.getParameter( "manualMappedPublicAddress" );
        if ( manualMappedPublicAddress == null || manualMappedPublicAddress.trim().isEmpty() ) {
            JiveGlobals.deleteProperty( PluginImpl.MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME );
        } else {
            JiveGlobals.setProperty( PluginImpl.MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME, manualMappedPublicAddress.trim() );
        }

        singlePortEnabled = Boolean.parseBoolean( request.getParameter( "singlePortEnabled" ) );
        JiveGlobals.setProperty( PluginImpl.SINGLE_PORT_ENABLED_PROPERTY_NAME, Boolean.toString( singlePortEnabled ) );

        singlePort = request.getParameter("singlePort");
        if (singlePort != null) {
            singlePort = singlePort.trim();
            try
            {
                int port = Integer.valueOf(singlePort);
                if(port >= 1 && port <= 65535) {
                    JiveGlobals.setProperty( PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME, singlePort );
                } else {
                    throw new NumberFormatException( "out of range port" );
                }
            } catch (Exception e) {
                errors.put( "singlePort", "Invalid port value" );
            }
        }

        minmaxPortEnabled = Boolean.parseBoolean( request.getParameter( "minmaxPortEnabled" ) );
        JiveGlobals.setProperty( PluginImpl.MINMAX_PORT_ENABLED_PROPERTY_NAME, Boolean.toString( minmaxPortEnabled ) );

        minPort = request.getParameter("minPort");
        if (minPort != null) {
            minPort = minPort.trim();
            try {
                int port = Integer.valueOf(minPort);
                if( port >= 1 && port <= 65535 ) {
                    JiveGlobals.setProperty( PluginImpl.MIN_PORT_NUMBER_PROPERTY_NAME, minPort );
                } else {
                    throw new NumberFormatException( "out of range port" );
                }
            } catch (Exception e) {
                errors.put( "minPort", "Invalid port value" );
            }
        }

        maxPort = request.getParameter("maxPort");
        if (maxPort != null) {
            maxPort = maxPort.trim();
            try {
                int port = Integer.valueOf( maxPort );
                if ( port >= 1 && port <= 65535 ) {
                    JiveGlobals.setProperty( PluginImpl.MAX_PORT_NUMBER_PROPERTY_NAME, maxPort );
                } else {
                    throw new NumberFormatException( "out of range port" );
                }
            } catch (Exception e) {
                errors.put( "maxPort", "Invalid port value" );
            }
        }

        tcpEnabled = Boolean.parseBoolean( request.getParameter( "tcpEnabled" ) );
        JiveGlobals.setProperty( PluginImpl.TCP_ENABLED_PROPERTY_NAME, Boolean.toString( tcpEnabled ));

        tcpPort = request.getParameter( "tcpPort" );
        if ( tcpPort == null || tcpPort.trim().isEmpty() ) {
            JiveGlobals.deleteProperty( PluginImpl.TCP_PORT_PROPERTY_NAME );
        } else {
            try {
                int port = Integer.valueOf(tcpPort.trim());
                if(port >= 1 && port <= 65535) {
                    JiveGlobals.setProperty( PluginImpl.TCP_PORT_PROPERTY_NAME, tcpPort.trim() );
                } else {
                    throw new NumberFormatException( "out of range port" );
                }
            }
            catch( Exception e )
            {
                errors.put( "tcpPort", "Invalid port value" );
            }
        }

        mappedTcpPort = request.getParameter( "mappedTcpPort" );
        if ( mappedTcpPort == null || mappedTcpPort.trim().isEmpty() ) {
            JiveGlobals.deleteProperty( PluginImpl.TCP_MAPPED_PORT_PROPERTY_NAME );
        } else {
            try {
                int port = Integer.valueOf(mappedTcpPort.trim());
                if(port >= 1 && port <= 65535) {
                    JiveGlobals.setProperty( PluginImpl.TCP_MAPPED_PORT_PROPERTY_NAME, mappedTcpPort.trim() );
                } else {
                    throw new NumberFormatException( "out of range port" );
                }
            }
            catch( Exception e )
            {
                errors.put( "mappedTcpPort", "Invalid port value" );
            }
        }
        sslTcpEnabled = Boolean.parseBoolean( request.getParameter( "sslTcpEnabled" ) );
        JiveGlobals.setProperty( PluginImpl.TCP_SSLTCP_ENABLED_PROPERTY_NAME, Boolean.toString( sslTcpEnabled ) );
    }
    else
    {
        stunPort = RuntimeConfiguration.getSTUNMappingHarvesterAddresses() == null || RuntimeConfiguration.getSTUNMappingHarvesterAddresses().isEmpty() ? null : Integer.toString( RuntimeConfiguration.getSTUNMappingHarvesterAddresses().get(0).getPort() );
        singlePortEnabled = RuntimeConfiguration.isSinglePortEnabled();
        singlePort = Integer.toString( RuntimeConfiguration.getSinglePort() );
        if ( singlePort.equals( "-1" ) )
        {
            // Jitsi stores the 'disabled' value in the port value. We're not. Correcting that here.
            singlePort = Integer.toString( JiveGlobals.getIntProperty( PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME, 10000 ) );
        }

        minmaxPortEnabled = RuntimeConfiguration.isMinMaxPortEnabled();
        minPort = Integer.toString( RuntimeConfiguration.getMinPort() );
        maxPort = Integer.toString( RuntimeConfiguration.getMaxPort() );
        tcpPort = RuntimeConfiguration.getTcpPort() == null ? null : RuntimeConfiguration.getTcpPort().toString();
        mappedTcpPort = RuntimeConfiguration.getTcpMappedPort() == null ? null : RuntimeConfiguration.getTcpMappedPort().toString();
        tcpEnabled = RuntimeConfiguration.isTcpEnabled();
        sslTcpEnabled = RuntimeConfiguration.isSslTcpEnabled();
    }

    final Collection<String> allowedInterfaces = JiveGlobals.getListProperty( PluginImpl.INTERFACES_ALLOWED_PROPERTY_NAME, null ); // null if all interfaces are allowed.
    final Collection<String> blockedInterfaces = JiveGlobals.getListProperty( PluginImpl.INTERFACES_BLOCKED_PROPERTY_NAME, null );

    final Collection<InetAddress> allowedAddresses;
    final List<String> addressesAllowed = JiveGlobals.getListProperty( PluginImpl.ADDRESSES_ALLOWED_PROPERTY_NAME, null );
    if ( addressesAllowed == null || addressesAllowed.isEmpty() )
    {
        allowedAddresses = null;
    }
    else
    {
        allowedAddresses = new ArrayList<>();
        for ( final String addressAllowed : addressesAllowed )
        {
            allowedAddresses.add( InetAddress.getByName( addressAllowed ) );
        }
    }

    final Collection<InetAddress> blockedAddresses;
    final List<String> addressesBlocked = JiveGlobals.getListProperty( PluginImpl.ADDRESSES_BLOCKED_PROPERTY_NAME, null );
    if ( addressesBlocked == null || addressesBlocked.isEmpty() )
    {
        blockedAddresses = null;
    }
    else
    {
        blockedAddresses = new ArrayList<>();
        for ( final String addressBlocked : addressesBlocked )
        {
            blockedAddresses.add( InetAddress.getByName( addressBlocked ) );
        }
    }

    boolean isAllowAllInterfaces = allowedInterfaces == null && blockedAddresses == null;
    boolean isAllowAllAddresses = allowedAddresses == null && blockedAddresses == null;

    boolean isAtLeastOneHarvesterEnabled = singlePortEnabled || minmaxPortEnabled || tcpEnabled;
%>
<html>
<head>
   <title><fmt:message key="config.page.title" /></title>

   <meta name="pageID" content="jitsi-videobridge-settings"/>

   <style>
      label.jive-label {
          vertical-align: unset;
      }

      input[type="radio"] {
          margin-top: -1px;
          vertical-align: middle;
      }
   </style>

</head>
<body>
<% if (!errors.isEmpty()) { %>
<div class="jive-error">
    <table cellpadding="0" cellspacing="0" border="0">
        <tbody>
        <tr>
            <td class="jive-icon"><img src="/images/error-16x16.gif" width="16" height="16" border="0" alt=""/></td>
            <td class="jive-icon-label">
                <fmt:message key="config.page.configuration.error.generic"/>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<br/>
<% } %>

<% if ( RuntimeConfiguration.restartNeeded() ) { %>
<div class="jive-warning">
    <table cellpadding="0" cellspacing="0" border="0">
        <tbody>
        <tr>
            <td class="jive-icon"><img src="/images/warning-16x16.gif" width="16" height="16" border="0" alt=""/></td>
            <td class="jive-icon-label">
                <fmt:message key="config.page.configuration.restart-needed"/>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<br/>
<% } %>

<% if ( !isAtLeastOneHarvesterEnabled ) { %>
<div class="jive-warning">
    <table cellpadding="0" cellspacing="0" border="0">
        <tbody>
        <tr>
            <td class="jive-icon"><img src="/images/warning-16x16.gif" width="16" height="16" border="0" alt=""/></td>
            <td class="jive-icon-label">
                <fmt:message key="config.page.configuration.no-harvesters-enabled"/>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<br/>
<% } %>

<p>
    <fmt:message key="config.page.description"/>
</p>
<form action="jitsi-videobridge.jsp" method="post">

    <div class="jive-contentBoxHeader">
        <fmt:message key="config.page.configuration.interfaces.title"/>
    </div>
    <div class="jive-contentBox">
        <p>
            <fmt:message key="config.page.configuration.interfaces.info"/>
        </p>
        <table>
            <tr>
                <td>
                    <input type="radio" name="allow-all-interfaces" id="allow-all-interfaces" value="true" <%= isAllowAllInterfaces ? "checked" : ""%>/>
                    <label for="allow-all-interfaces"><fmt:message key="config.page.configuration.interfaces.allow-all"/></label>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="radio" name="allow-all-interfaces" id="allow-specific-interfaces" value="false" <%= isAllowAllInterfaces ? "" : "checked"%>/>
                    <label for="allow-specific-interfaces"><fmt:message key="config.page.configuration.interfaces.allow-specific"/></label>
                </td>
            </tr>
            <tbody>
            <%
                for ( final NetworkInterface networkInterface : Collections.list( NetworkInterface.getNetworkInterfaces() ) )
                {
                    if (NetworkUtils.isInterfaceLoopback(networkInterface) ) {
                        continue;
                    }

                    // use getDisplayName() on Windows and getName() on Linux.
                    final String ifName = (System.getProperty("os.name") == null || System.getProperty("os.name").startsWith("Windows"))
                            ? networkInterface.getDisplayName()
                            : networkInterface.getName();

                    final byte[] hardwareAddressAsBytes = networkInterface.getHardwareAddress();
                    final String hardwareAddress;
                    if ( hardwareAddressAsBytes != null ) {
                        final StringBuilder sb = new StringBuilder( 18 );
                        for ( byte b : hardwareAddressAsBytes ) {
                            if ( sb.length() > 0 ) {
                                sb.append( ':' );
                            }
                            sb.append( String.format( "%02x", b ) );
                        }
                        hardwareAddress = sb.toString();
                    } else {
                        hardwareAddress = null;
                    }

                    final boolean isDown = !NetworkUtils.isInterfaceUp( networkInterface );
                    final boolean isAllowed = ( allowedInterfaces == null  || (allowedInterfaces != null && allowedInterfaces.contains( ifName ) ) )
                            && ( blockedInterfaces == null || blockedInterfaces != null && !blockedInterfaces.contains( ifName ) );
            %>
            <tr>
                <td style="padding-left: 2em;">
                    <input type="checkbox" name="interface-allowed-<%=ifName%>" id="interface-allowed-<%=ifName%>" <%= isAllowed ? "checked" : ""%>/>
                    <label for="interface-allowed-<%=ifName%>"><fmt:message key="config.page.configuration.interfaces.allow"/> <%= ifName %> <%= hardwareAddress != null ? "(" + hardwareAddress + ")" : "" %></label>
                    <% if ( isDown ) { %>
                    <span class="jive-info-text"><fmt:message key="config.page.configuration.interfaces.interface-down" /></span>
                    <% } %>
                </td>
            </tr>
            <%
                }
            %>

            </tbody>
        </table>
    </div>

    <div class="jive-contentBoxHeader">
        <fmt:message key="config.page.configuration.addresses.title"/>
    </div>
    <div class="jive-contentBox">
        <p>
            <fmt:message key="config.page.configuration.addresses.info"/>
        </p>
        <table>
            <tbody>
            <tr>
                <td>
                    <input type="radio" name="allow-all-addresses" id="allow-all-addresses" value="true" <%= isAllowAllAddresses ? "checked" : ""%>/>
                    <label for="allow-all-addresses"><fmt:message key="config.page.configuration.addresses.allow-all"/></label>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="radio" name="allow-all-addresses" id="allow-specific-addresses" value="false" <%= isAllowAllAddresses ? "" : "checked"%>/>
                    <label for="allow-specific-addresses"><fmt:message key="config.page.configuration.addresses.allow-specific"/></label>
                </td>
            </tr>
            <%
                for ( final NetworkInterface networkInterface : Collections.list( NetworkInterface.getNetworkInterfaces() ) )
                {
                    if (NetworkUtils.isInterfaceLoopback(networkInterface) || !NetworkUtils.isInterfaceUp(networkInterface) )
                    {
                        continue;
                    }

                    // use getDisplayName() on Windows and getName() on Linux.
                    final String ifName = (System.getProperty("os.name") == null || System.getProperty("os.name").startsWith("Windows"))
                            ? networkInterface.getDisplayName()
                            : networkInterface.getName();

                    final boolean isInterfaceAllowed = ( allowedInterfaces == null || (allowedInterfaces != null && allowedInterfaces.contains( ifName ) ) )
                            && ( blockedInterfaces == null || blockedInterfaces != null && !blockedInterfaces.contains( ifName ) );

                    final Enumeration<InetAddress> ifaceAddresses = networkInterface.getInetAddresses();
                    while ( ifaceAddresses.hasMoreElements() ) {
                        final InetAddress address = ifaceAddresses.nextElement();
                        String hostAddress = address.getHostAddress();
                        if ( hostAddress.contains( "%" ) ) {
                            hostAddress = hostAddress.substring( 0, hostAddress.indexOf( '%' ) );
                        }

                        final boolean isAllowed = ( allowedAddresses == null || allowedAddresses != null && allowedAddresses.contains( address ) )
                            && ( blockedAddresses == null || blockedAddresses != null && !blockedAddresses.contains( address ) );
            %>
            <tr>
                <td style="padding-left: 2em;">
                    <input type="checkbox" name="address-allowed-<%=hostAddress%>" id="address-allowed-<%=hostAddress%>" <%= isAllowed ? "checked" : ""%>/>
                    <label for="address-allowed-<%=hostAddress%>"><fmt:message key="config.page.configuration.addresses.allow"/> <%= hostAddress %> </label>
                    <% if ( !isInterfaceAllowed ) { %>
                    <span class="jive-info-text"><fmt:message key="config.page.configuration.addresses.interface-not-allowed"><fmt:param value="<%=ifName%>"/></fmt:message></span>
                    <% } %>
                </td>
            </tr>
            <%
                    } }
            %>

            </tbody>
        </table>
    </div>

    <div class="jive-contentBoxHeader">
        <fmt:message key="config.page.configuration.address-mapping.title"/>
    </div>
    <div class="jive-contentBox">
        <p>
            <fmt:message key="config.page.configuration.address-mapping.info"/>
        </p>
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tbody>
            <!-- AWS Mapping Harvester -->
            <tr>
                <td colspan="2"><fmt:message key="config.page.configuration.address-mapping.aws.info"/></td>
            </tr>
            <tr>
                <td nowrap style="padding-left: 2em;">
                    <input type="radio" name="aws" value="disabled" id="aws01" <%= (RuntimeConfiguration.isAWSMappingHarvesterEnabled() ? "" : "checked") %>>
                    <label class="jive-label" for="aws01"><fmt:message key="config.page.configuration.address-mapping.aws.disabled" /></label>
                </td>
                <td>
                    <label for="aws01"><fmt:message key="config.page.configuration.address-mapping.aws.disabled_info" /></label>
                </td>
            </tr>
            <tr>
                <td nowrap style="padding-left: 2em;">
                    <input type="radio" name="aws" value="auto" id="aws02" <%= (RuntimeConfiguration.isAWSMappingHarvesterEnabled() && !RuntimeConfiguration.isAWSMappingHarvesterForced() ? "checked" : "") %>>
                    <label class="jive-label"for="aws02"><fmt:message key="config.page.configuration.address-mapping.aws.auto" /></label>
                </td>
                <td>
                    <label for="aws02"><fmt:message key="config.page.configuration.address-mapping.aws.auto_info" />
                        <% if ( AwsCandidateHarvester.smellsLikeAnEC2() ) { %>
                        <fmt:message key="config.page.configuration.address-mapping.aws.detected" />
                        <% } else { %>
                        <fmt:message key="config.page.configuration.address-mapping.aws.undetected" />
                        <% } %>
                    </label>
                </td>
            </tr>
            <tr>
                <td nowrap style="padding-left: 2em;">
                    <input type="radio" name="aws" value="forced" id="aws03" <%= (RuntimeConfiguration.isAWSMappingHarvesterForced() ? "checked" : "") %>>
                    <label class="jive-label" for="aws03"><fmt:message key="config.page.configuration.address-mapping.aws.forced" /></label>
                </td>
                <td>
                    <label for="aws03"><fmt:message key="config.page.configuration.address-mapping.aws.forced_info" /></label>
                </td>
            </tr>

            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>

            <!-- STUN Mapping Harvester -->
            <tr>
                <td colspan="2"><fmt:message key="config.page.configuration.address-mapping.stun.info"/></td>
            </tr>
            <tr>
                <td width="1px" nowrap style="padding-left: 2em;"><label class="jive-label" for="stunAddress"><fmt:message key="config.page.configuration.address-mapping.stun.address"/>:</label></td>
                <td>
                    <input name="stunAddress" id="stunAddress" type="text" value="<%=RuntimeConfiguration.getSTUNMappingHarvesterAddresses().isEmpty() ? "" : RuntimeConfiguration.getSTUNMappingHarvesterAddresses().get( 0 ).getHostString() %>"/>
                </td>
            </tr>
            <tr>
                <td width="1px" nowrap style="padding-left: 2em;"><label class="jive-label" for="stunPort"><fmt:message key="config.page.configuration.address-mapping.stun.port"/>:</label></td>
                <td>
                    <input name="stunPort" id="stunPort" type="number" min="1" max="65535" value="<%=stunPort %>"/>
                    <%  if (errors.get("stunPort") != null) { %>
                        <span class="jive-error-text"><fmt:message key="config.page.configuration.error.valid_port" /></span>
                    <%  } %>
                </td>
            </tr>

            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>

            <!-- Manual Mapping Harvester -->
            <tr>
                <td colspan="2"><fmt:message key="config.page.configuration.address-mapping.manual.info"/></td>
            </tr>
            <tr>
                <td nowrap style="padding-left: 2em;"><label class="jive-label" for="manualMappedLocalAddress"><fmt:message key="config.page.configuration.address-mapping.manual.local"/>:</label></td>
                <td>
                    <input name="manualMappedLocalAddress" id="manualMappedLocalAddress" type="text" value="<%=RuntimeConfiguration.getManualMappedLocalAddress() != null ? RuntimeConfiguration.getManualMappedLocalAddress() : "" %>"/>
                </td>
            </tr>
            <tr>
                <td width="1px" nowrap style="padding-left: 2em;"><label class="jive-label" for="manualMappedPublicAddress"><fmt:message key="config.page.configuration.address-mapping.manual.public"/>:</label></td>
                <td>
                    <input name="manualMappedPublicAddress" id="manualMappedPublicAddress" type="text" value="<%=RuntimeConfiguration.getManualMappedPublicAddress() != null ? RuntimeConfiguration.getManualMappedPublicAddress() : "" %>"/>
                </td>
            </tr>

            </tbody>
        </table>
    </div>

    <div class="jive-contentBoxHeader">
        <fmt:message key="config.page.configuration.title"/>
    </div>
    <div class="jive-contentBox">
        <p>
            <fmt:message key="config.page.configuration.single.port.description"/>
        </p>
        <table cellpadding="0" cellspacing="0" border="0">
            <tbody>
            <tr>
                <td width="1%" nowrap>
                    <input type="radio" name="singlePortEnabled" value="false" id="singlePortDisabled" <%= (!singlePortEnabled ? "checked" : "") %>>
                    <label class="jive-label" for="singlePortDisabled"><fmt:message key="config.page.configuration.single.port.disabled" /></label>
                </td>
                <td width="99%" colspan="2">
                    <label for="singlePortDisabled"><fmt:message key="config.page.configuration.single.port.disabled_info" /></label>
                </td>
            </tr>
            <tr>
                <td width="1%" nowrap>
                    <input type="radio" name="singlePortEnabled" value="true" id="singlePortEnabled" <%= (singlePortEnabled ? "checked" : "") %>>
                    <label  class="jive-label" for="singlePortEnabled"><fmt:message key="config.page.configuration.single.port.enabled" /></label>
                </td>
                <td width="99%" colspan="2">
                    <label for="singlePortEnabled"><fmt:message key="config.page.configuration.single.port.enabled_info" /></label>
                </td>
            </tr>
            <tr>
                <td width="10%" style="padding-left: 3em; padding-top: 1em;" nowrap colspan="2">
                    <label for="singlePort"><fmt:message key="config.page.configuration.single.port"/>:</label>
                </td>
                <td>
                    <input name="singlePort" id="singlePort" type="number" min="1" max="65535" value="<%=singlePort%>"/> <fmt:message key="config.page.configuration.udp"/>
                    <%  if (errors.get("singlePort") != null) { %>
                    <span class="jive-error-text"><fmt:message key="config.page.configuration.error.valid_port" /></span>
                    <%  } %>
                </td>
            </tr>
        </table>

        <p style="padding-top: 2em;">
            <fmt:message key="config.page.configuration.minmax.port.description"/>
        </p>
        <table cellpadding="0" cellspacing="0" border="0">
            <tbody>
            <tr>
                <td width="1%" nowrap>
                    <input type="radio" name="minmaxPortEnabled" value="false" id="minmaxPortDisabled" <%= (!minmaxPortEnabled ? "checked" : "") %>>
                    <label class="jive-label" for="minmaxPortDisabled"><fmt:message key="config.page.configuration.minmax.port.disabled" /></label>
                </td>
                <td width="99%" colspan="2">
                    <label for="minmaxPortDisabled"><fmt:message key="config.page.configuration.minmax.port.disabled_info" /></label>
                </td>
            </tr>
            <tr>
                <td width="1%" nowrap>
                    <input type="radio" name="minmaxPortEnabled" value="true" id="minmaxPortEnabled" <%= (minmaxPortEnabled ? "checked" : "") %>>
                    <label class="jive-label" for="minmaxPortEnabled"><fmt:message key="config.page.configuration.minmax.port.enabled" /></label>
                </td>
                <td width="99%" colspan="2">
                    <label for="minmaxPortEnabled"><fmt:message key="config.page.configuration.minmax.port.enabled_info" /></label>
                </td>
            </tr>
            <tr>
                <td width="10%" style="padding-left: 3em; padding-top: 1em;" nowrap colspan="2">
                    <label for="minPort"><fmt:message key="config.page.configuration.min.port"/>:</label>
                </td>
                <td>
                    <input name="minPort" id="minPort" type="number" min="1" max="65535" value="<%=minPort%>"/> <fmt:message key="config.page.configuration.udp"/>
                    <%  if (errors.get("minPort") != null) { %>
                    <span class="jive-error-text"><fmt:message key="config.page.configuration.error.valid_port" /></span>
                    <%  } %>
                </td>
            </tr>
            <tr>
                <td width="10%" style="padding-left: 3em;" nowrap colspan="2">
                    <label for="maxPort"><fmt:message key="config.page.configuration.max.port"/>:</label>
                </td>
                <td>
                    <input name="maxPort" id="maxPort" type="number" min="1" max="65535" value="<%=maxPort%>"/> <fmt:message key="config.page.configuration.udp"/>
                    <%  if (errors.get("maxPort") != null) { %>
                    <span class="jive-error-text"><fmt:message key="config.page.configuration.error.valid_port" /></span>
                    <%  } %>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="jive-contentBoxHeader">
        <fmt:message key="config.page.configuration.tcp.title" />
    </div>
    <div class="jive-contentBox">
        <p>
            <fmt:message key="config.page.configuration.tcp.info"/>
        </p>
        <table cellpadding="3" cellspacing="0" border="0">
            <tbody>
            <tr>
                <td width="1%" nowrap>
                    <input type="radio" name="tcpEnabled" value="false" id="rb01" <%= (RuntimeConfiguration.isTcpEnabled() ? "" : "checked") %>>
                    <label class="jive-label" for="rb01"><fmt:message key="config.page.configuration.tcp.disabled" /></label>
                </td>
                <td width="99%">
                    <label for="rb01"><fmt:message key="config.page.configuration.tcp.disabled_info" /></label>
                </td>
            </tr>
            <tr>
                <td width="1%" nowrap>
                    <input type="radio" name="tcpEnabled" value="true" id="rb02" <%= (RuntimeConfiguration.isTcpEnabled()  ? "checked" : "") %>>
                    <label class="jive-label" for="rb02"><fmt:message key="config.page.configuration.tcp.enabled" /></label>
                </td>
                <td width="99%">
                    <label for="rb02"><fmt:message key="config.page.configuration.tcp.enabled_info" /></label>
                </td>
            </tr>
            <tr>
                <td colspan="2" width="100%" style="padding-left: 2em;">
                    <table cellpadding="3" cellspacing="0" border="0">
                        <tr>
                            <td colspan="2" style="padding-top: 1em;">
                                <fmt:message key="config.page.configuration.tcp.port_info"/>
                            </td>
                        </tr>
                        <tr>
                            <td width="1%" nowrap class="c1" style="padding-left: 2em;">
                                <label class="jive-label" for="tcpPort"><fmt:message key="config.page.configuration.tcp.port" />:</label>
                            </td>
                            <td width="99%">
                                <input type="number" min="1" max="65535" name="tcpPort" id="tcpPort" value="<%= ((tcpPort != null) ? tcpPort : "") %>"> <fmt:message key="config.page.configuration.tcp" />
                                <%  if (errors.get("tcpPort") != null) { %>
                                <span class="jive-error-text"><fmt:message key="config.page.configuration.error.valid_port" /></span>
                                <%  } %>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" style="padding-top: 1em;">
                                <fmt:message key="config.page.configuration.tcp.mapped.port_info"/>
                            </td>
                        </tr>
                        <tr>
                            <td width="1%" nowrap class="c1" style="padding-left: 2em;">
                                <label class="jive-label" for="mappedTcpPort"><fmt:message key="config.page.configuration.tcp.mapped.port" />:</label>
                            </td>
                            <td width="99%">
                                <input type="number" min="1" max="65535" name="mappedTcpPort" id="mappedTcpPort" value="<%= ((mappedTcpPort != null) ? mappedTcpPort : "") %>"> <fmt:message key="config.page.configuration.tcp" />
                                <%  if (errors.get("mappedTcpPort") != null) { %>
                                <span class="jive-error-text"><fmt:message key="config.page.configuration.error.valid_port" /></span>
                                <%  } %>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" style="padding-top: 1em;">
                                <fmt:message key="config.page.configuration.ssltcp_info"/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" style="padding-left: 2em;">
                                <table cellpadding="3" cellspacing="0" border="0">
                                    <tbody>
                                    <tr>
                                        <td width="1%" nowrap>
                                            <input type="radio" name="sslTcpEnabled" value="false" id="rb03" <%= (!sslTcpEnabled ? "checked" : "") %>>
                                            <label class="jive-label" for="rb03"><fmt:message key="config.page.configuration.ssltcp.disabled" /></label>
                                        </td>
                                        <td>
                                            <label for="rb03"><fmt:message key="config.page.configuration.ssltcp.disabled_info" /></label>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td width="1%" nowrap>
                                        <input type="radio" name="sslTcpEnabled" value="true" id="rb04" <%= (sslTcpEnabled ? "checked" : "") %>>
                                            <label class="jive-label" for="rb04"><fmt:message key="config.page.configuration.ssltcp.enabled" /></label>
                                        </td>
                                        <td>
                                            <label for="rb04"><fmt:message key="config.page.configuration.ssltcp.enabled_info" /></label>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>

    </div>

    <input type="submit" name="update" value="<fmt:message key="config.page.configuration.submit" />">
    <input type="submit" name="reset" value="<fmt:message key="config.page.configuration.reset" />">
</form>

</body>
</html>
