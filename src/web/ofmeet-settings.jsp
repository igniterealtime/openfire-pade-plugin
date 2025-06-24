<%--
  ~ Copyright (c) 2017 Ignite Realtime Foundation. All rights reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ page import="org.jivesoftware.openfire.XMPPServer" %>
<%@ page import="org.jivesoftware.openfire.cluster.ClusterManager" %>
<%@ page import="org.jivesoftware.openfire.plugin.ofmeet.OfMeetPlugin" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.jivesoftware.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="admin" prefix="admin" %>
<jsp:useBean id="random" class="java.util.Random"/>
<jsp:useBean id="ofmeetConfig" class="org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig"/>
<%
    Logger Log = LoggerFactory.getLogger( "ofmeet-settings.jsp" );
    boolean update = request.getParameter( "update" ) != null;

    final Cookie csrfCookie = CookieUtils.getCookie( request, "csrf" );
    final String csrfParam = ParamUtils.getParameter( request, "csrf" );

    // Get handle on the plugin
    final OfMeetPlugin container = OfMeetPlugin.self;

    final Map<String, String> errors = new HashMap<>();

    if ( update )
    {
        if ( csrfCookie == null || csrfParam == null || !csrfCookie.getValue().equals( csrfParam ) )
        {
            errors.put( "csrf", "CSRF Failure!" );
        }
        final boolean conferenceadmin = ParamUtils.getBooleanParameter( request, "conferenceadmin" );
        final boolean securityenabled = ParamUtils.getBooleanParameter( request, "securityenabled" );
        final boolean webauthn_enabled = ParamUtils.getBooleanParameter( request, "webauthn_enabled" );
        final boolean disableRtx = !ParamUtils.getBooleanParameter( request, "enableRtx" ); 		
        final String authusername = request.getParameter( "authusername" );
        final String sippassword = request.getParameter( "sippassword" );
        final String server = request.getParameter( "server" );
        final String outboundproxy = request.getParameter( "outboundproxy" );
        final boolean p2pEnabled = ParamUtils.getBooleanParameter( request, "p2pEnabled" );        
        final boolean p2pUseStunTurn = ParamUtils.getBooleanParameter( request, "p2pUseStunTurn" );   
        final boolean p2pDisableH264 = ParamUtils.getBooleanParameter( request, "p2pDisableH264" );   
        final boolean p2pPreferH264 = ParamUtils.getBooleanParameter( request, "p2pPreferH264" ); 

        final String liveStreamUrl = request.getParameter( "liveStreamUrl" );  
        final String liveStreamKey = request.getParameter( "liveStreamKey" );            
        final boolean liveStreamEnabled = ParamUtils.getBooleanParameter( request, "liveStreamEnabled" );       

        final boolean startaudioonly = ParamUtils.getBooleanParameter( request, "startaudioonly" );

        final String startaudiomuted = request.getParameter( "startaudiomuted" );
        if ( startaudiomuted != null && !startaudiomuted.isEmpty() )
        {
            try {
                Integer.parseInt( startaudiomuted );
            } catch (NumberFormatException ex) {
                errors.put( "startaudiomuted", "Cannot parse value as integer value." );
            }
        }

        final String startvideomuted = request.getParameter( "startvideomuted" );
        if ( startvideomuted != null && !startvideomuted.isEmpty() )
        {
            try {
                Integer.parseInt( startvideomuted );
            } catch (NumberFormatException ex) {
                errors.put( "startvideomuted", "Cannot parse value as integer value." );
            }
        }

        final boolean websockets = ParamUtils.getBooleanParameter( request, "websockets" );     
        final boolean useIPv6 = ParamUtils.getBooleanParameter( request, "useipv6" );
        final boolean useNicks = ParamUtils.getBooleanParameter( request, "usenicks" );

        final String maxFullResolutionParticipants = request.getParameter( "maxFullResolutionParticipants" );  
        final String desktopSharingFrameRateMin = request.getParameter( "desktopSharingFrameRateMin" ); 
        final String desktopSharingFrameRateMax = request.getParameter( "desktopSharingFrameRateMax" );         
        final String minHeightForQualityLow = request.getParameter( "minHeightForQualityLow" );  
        final String minHeightForQualityStd = request.getParameter( "minHeightForQualityStd" );         
        final String minHeightForQualityHigh = request.getParameter( "minHeightForQualityHigh" );           
        final boolean capScreenshareBitrate = ParamUtils.getBooleanParameter( request, "capScreenshareBitrate" );     
        final boolean enableLayerSuspension = ParamUtils.getBooleanParameter( request, "enableLayerSuspension" );        

        final String videoConstraintsIdealAspectRatio = request.getParameter( "videoConstraintsIdealAspectRatio" );
        if(!videoConstraintsIdealAspectRatio.matches("[0-9: .,/]+"))
        {
            errors.put( "videoConstraintsIdealAspectRatio", "Cannot parse value as aspect ratio value." );
        }
        final String resolution = request.getParameter( "resolution" );	
        try {
            Integer.parseInt( resolution );
        } catch (NumberFormatException ex ) {
            errors.put( "resolution", "Cannot parse value as integer value." );
        }		
        final String videoConstraintsMinHeight = request.getParameter( "videoConstraintsMinHeight" );
        try {
            Integer.parseInt( videoConstraintsMinHeight );
        } catch (NumberFormatException ex ) {
            errors.put( "videoConstraintsMinHeight", "Cannot parse value as integer value." );
        }
        final String videoConstraintsIdealHeight = request.getParameter( "videoConstraintsIdealHeight" );
        try {
            Integer.parseInt( videoConstraintsIdealHeight );
        } catch (NumberFormatException ex ) {
            errors.put( "videoConstraintsIdealHeight", "Cannot parse value as integer value." );
        }
        final String videoConstraintsMaxHeight = request.getParameter( "videoConstraintsMaxHeight" );
        try {
            Integer.parseInt( videoConstraintsMaxHeight );
        } catch (NumberFormatException ex ) {
            errors.put( "videoConstraintsMaxHeight", "Cannot parse value as integer value." );
        }
        final String videoConstraintsMinWidth = request.getParameter( "videoConstraintsMinWidth" );
        try {
            Integer.parseInt( videoConstraintsMinWidth );
        } catch (NumberFormatException ex ) {
            errors.put( "videoConstraintsMinWidth", "Cannot parse value as integer value." );
        }
        final String videoConstraintsIdealWidth = request.getParameter( "videoConstraintsIdealWidth" );
        try {
            Integer.parseInt( videoConstraintsIdealWidth );
        } catch (NumberFormatException ex ) {
            errors.put( "videoConstraintsIdealWidth", "Cannot parse value as integer value." );
        }
        final String videoConstraintsMaxWidth = request.getParameter( "videoConstraintsMaxWidth" );
        try {
            Integer.parseInt( videoConstraintsMaxWidth );
        } catch (NumberFormatException ex ) {
            errors.put( "videoConstraintsMaxWidth", "Cannot parse value as integer value." );
        }

        final String lowMaxBitratesVideo = request.getParameter( "lowMaxBitratesVideo" );
        try {
            Integer.parseInt( lowMaxBitratesVideo );
        } catch (NumberFormatException ex ) {
            errors.put( "lowMaxBitratesVideo", "Cannot parse value as integer value." );
        }    
        final String standardMaxBitratesVideo = request.getParameter( "standardMaxBitratesVideo" );
        try {
            Integer.parseInt( standardMaxBitratesVideo );
        } catch (NumberFormatException ex ) {
            errors.put( "standardMaxBitratesVideo", "Cannot parse value as integer value." );
        } 
        final String highMaxBitratesVideo = request.getParameter( "highMaxBitratesVideo" );
        try {
            Integer.parseInt( highMaxBitratesVideo );
        } catch (NumberFormatException ex ) {
            errors.put( "highMaxBitratesVideo", "Cannot parse value as integer value." );
        }         

        final String jvmJvb = request.getParameter( "jvmJvb" );
        final String jvmJicofo = request.getParameter( "jvmJicofo" );
        final String jvmJigasi = request.getParameter( "jvmJigasi" );

        final String displayNotice = request.getParameter( "displayNotice" );
        final String clientusername = request.getParameter( "clientusername" );
        final String clientpassword = request.getParameter( "clientpassword" );
        final String enableSip = request.getParameter( "enableSip" );
        final boolean allowdirectsip = ParamUtils.getBooleanParameter( request, "allowdirectsip" );

        final String focuspassword = request.getParameter( "focuspassword" );
        final String jvbname = request.getParameter( "jvbname" );
        final String jvbpassword = request.getParameter( "jvbpassword" );        
        final String hqVoice = request.getParameter( "hqVoice" );

        int channelLastN = -1;
        try {
            channelLastN = Integer.parseInt( request.getParameter( "channellastn" ) );
        } catch (NumberFormatException ex ) {
            errors.put( "channellastn", "Cannot parse value as integer value." );
        }

        final boolean adaptivelastn = ParamUtils.getBooleanParameter( request, "adaptivelastn" );
        final boolean simulcast = ParamUtils.getBooleanParameter( request, "simulcast" );
        final boolean adaptivesimulcast = ParamUtils.getBooleanParameter( request, "adaptivesimulcast" );
        final boolean enableStereo = ParamUtils.getBooleanParameter( request, "enableStereo" );
        final boolean lipSync = ParamUtils.getBooleanParameter( request, "lipSync" );
        
        if (jvmJvb.isEmpty()) errors.put( "jvmJvb", "Cannot be empty" );
        if (jvmJicofo.isEmpty()) errors.put( "jvmJicofo", "Cannot be empty" );
        final boolean jigasiSipEnabled = JiveGlobals.getBooleanProperty( "ofmeet.jigasi.enabled" );
        if (jigasiSipEnabled && jvmJigasi.isEmpty()) errors.put( "jvmJigasi", "Cannot be empty" );

        if ( errors.isEmpty() )
        {
            JiveGlobals.setProperty( "ofmeet.stereo.enabled", Boolean.toString(enableStereo) );
            JiveGlobals.setProperty( "ofmeet.winsso.enabled", Boolean.toString( securityenabled ) );
            JiveGlobals.setProperty( "ofmeet.webauthn.enabled", Boolean.toString( webauthn_enabled ) );         
            JiveGlobals.setProperty( "ofmeet.conference.admin", Boolean.toString( conferenceadmin ) );            
            JiveGlobals.setProperty( "voicebridge.default.proxy.sipauthuser", authusername );
            JiveGlobals.setProperty( "voicebridge.default.proxy.sippassword", sippassword );
            JiveGlobals.setProperty( "voicebridge.default.proxy.sipserver", server );
            JiveGlobals.setProperty( "voicebridge.default.proxy.outboundproxy", outboundproxy );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.websockets", Boolean.toString( websockets ) );                    
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.useipv6", Boolean.toString( useIPv6 ) );    
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.usenicks", Boolean.toString( useNicks ) );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.username", clientusername );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.password", clientpassword );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.enabled", enableSip );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.allow.direct.sip", Boolean.toString( allowdirectsip ) );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.hq.voice", hqVoice );
            
            JiveGlobals.setProperty( "org.jitsi.videobridge.low.max.bitrates.video", lowMaxBitratesVideo );            
            JiveGlobals.setProperty( "org.jitsi.videobridge.high.max.bitrates.video", highMaxBitratesVideo );
            JiveGlobals.setProperty( "org.jitsi.videobridge.standard.max.bitrates.video", standardMaxBitratesVideo );
            
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.display.notice", displayNotice );            
            
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.jvb.jvm.customOptions", jvmJvb );               
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.focus.jvm.customOptions", jvmJicofo ); 
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.jigasi.jvm.customOptions", jvmJigasi ); 
 			
            JiveGlobals.setProperty( "ofmeet.desktop.sharing.framerate.min", desktopSharingFrameRateMin);           
            JiveGlobals.setProperty( "ofmeet.desktop.sharing.framerate.max", desktopSharingFrameRateMax);
            JiveGlobals.setProperty( "ofmeet.max.full.resolution.participants", maxFullResolutionParticipants );            
            JiveGlobals.setProperty( "ofmeet.min.height.for.quality.level.low", minHeightForQualityLow );  
            JiveGlobals.setProperty( "ofmeet.min.height.for.quality.level.std", minHeightForQualityStd );  
            JiveGlobals.setProperty( "ofmeet.min.height.for.quality.level.high", minHeightForQualityHigh );    
            JiveGlobals.setProperty( "ofmeet.cap.screenshare.bitrate", Boolean.toString(capScreenshareBitrate) );             
            JiveGlobals.setProperty( "ofmeet.enable.layer.suspension", Boolean.toString(enableLayerSuspension) );             
            
            ofmeetConfig.setDisableRtx( disableRtx );
            ofmeetConfig.setStartAudioOnly( startaudioonly );
            ofmeetConfig.setStartAudioMuted( startaudiomuted == null || startaudiomuted.isEmpty() ? null : Integer.parseInt( startaudiomuted ));
            ofmeetConfig.setStartVideoMuted( startvideomuted == null || startvideomuted.isEmpty() ? null : Integer.parseInt( startvideomuted ));
            ofmeetConfig.setLipSync( lipSync );

            ofmeetConfig.setResolution( Integer.parseInt( resolution ) );
            ofmeetConfig.setVideoConstraintsIdealAspectRatio( videoConstraintsIdealAspectRatio );
            ofmeetConfig.setVideoConstraintsMinHeight( Integer.parseInt( videoConstraintsMinHeight ) );
            ofmeetConfig.setVideoConstraintsIdealHeight( Integer.parseInt( videoConstraintsIdealHeight ) );
            ofmeetConfig.setVideoConstraintsMaxHeight( Integer.parseInt( videoConstraintsMaxHeight ) );
            ofmeetConfig.setVideoConstraintsMinWidth( Integer.parseInt( videoConstraintsMinWidth ) );
            ofmeetConfig.setVideoConstraintsIdealWidth( Integer.parseInt( videoConstraintsIdealWidth ) );
            ofmeetConfig.setVideoConstraintsMaxWidth( Integer.parseInt( videoConstraintsMaxWidth ) );

            ofmeetConfig.setChannelLastN( channelLastN );
            ofmeetConfig.setAdaptiveLastN( adaptivelastn );
            ofmeetConfig.setSimulcast( simulcast );
            ofmeetConfig.setAdaptiveSimulcast( adaptivesimulcast );
            ofmeetConfig.setFocusPassword( focuspassword );
            ofmeetConfig.setJvbPassword( jvbpassword );
            ofmeetConfig.setJvbName( jvbname );            
            ofmeetConfig.setP2pEnabled( p2pEnabled );
            ofmeetConfig.setP2pPreferH264( p2pPreferH264 );
            ofmeetConfig.setP2pDisableH264( p2pDisableH264 );
            ofmeetConfig.setP2pUseStunTurn( p2pUseStunTurn );    

            ofmeetConfig.liveStreamUrl.set( liveStreamUrl );            
            ofmeetConfig.liveStreamKey.set( liveStreamKey );
            ofmeetConfig.liveStreamEnabled.set( Boolean.toString(liveStreamEnabled) );          

            container.restartNeeded = true;

            response.sendRedirect( "ofmeet-settings.jsp?settingsSaved=true" );
            return;
        }
    }

    final String csrf = StringUtils.randomString( 15 );
    CookieUtils.setCookie( request, response, "csrf", csrf, -1 );

    pageContext.setAttribute( "csrf", csrf );
    pageContext.setAttribute( "errors", errors );
    pageContext.setAttribute( "restartNeeded", container.restartNeeded );
    pageContext.setAttribute( "serverInfo", XMPPServer.getInstance().getServerInfo() );

    String enabledTitle = LocaleUtils.getLocalizedString("config.page.configuration.enabled", "pade");
    String disabledTitle = LocaleUtils.getLocalizedString("config.page.configuration.disabled", "pade");	
    String clusteringTitle = LocaleUtils.getLocalizedString("config.page.configuration.clustering.title", "pade");	
	clusteringTitle = clusteringTitle + " (" + (ClusterManager.isClusteringEnabled() ? enabledTitle : disabledTitle) + ")";

%>
<html>
<head>
    <title><fmt:message key="config.page.settings.title"/></title>
    <meta name="pageID" content="ofmeet-settings"/>
</head>
<body>

<c:choose>
    <c:when test="${not empty param.settingsSaved and empty errors}">
        <admin:infoBox type="success"><fmt:message key="config.page.configuration.save.success" /></admin:infoBox>
    </c:when>
    <c:otherwise>
        <c:forEach var="err" items="${errors}">
            <admin:infobox type="error">
                <c:choose>
                    <c:when test="${err.key eq 'csrf'}"><fmt:message key="global.csrf.failed"/></c:when>
                    <c:otherwise>
                        <c:if test="${not empty err.value}">
                            <c:out value="${err.value}"/>
                        </c:if>
                        (<c:out value="${err.key}"/>)
                    </c:otherwise>
                </c:choose>
            </admin:infobox>
        </c:forEach>
    </c:otherwise>
</c:choose>

<c:if test="${restartNeeded}">
    <admin:infoBox type="warning"><fmt:message key="config.page.configuration.restart.warning"/></admin:infoBox>
</c:if>

<p><fmt:message key="config.page.settings.introduction" /></p>

<form action="ofmeet-settings.jsp" method="post">   

    <fmt:message key="config.page.configuration.jvm.title" var="boxtitlejvm"/>
    <admin:contentBox title="${boxtitlejvm}">
        <table cellpadding="3" cellspacing="0" border="0" width="100%">   
            <tr>
                <td nowrap>
                    <label class="jive-label" for="jvmJvb"><fmt:message key="config.page.configuration.ofmeet.jvm.jvb"/></label>
                </td>
                <td>
                    <input type="text" size="80" maxlength="2048" name="jvmJvb" id="jvmJvb" value="${admin:getProperty( "org.jitsi.videobridge.ofmeet.jvb.jvm.customOptions", "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp")}" placeholder="-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp">
                </td>
            </tr>  
            <tr>
                <td nowrap>
                    <label class="jive-label" for="jvmJicofo"><fmt:message key="config.page.configuration.ofmeet.jvm.jicofo"/></label>
                </td>
                <td>
                    <input type="text" size="80" maxlength="2028" name="jvmJicofo" id="jvmJicofo" value="${admin:getProperty( "org.jitsi.videobridge.ofmeet.focus.jvm.customOptions", "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp")}" placeholder="-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp">
                </td>
            </tr>     
            <tr>
                <td nowrap>
                    <label class="jive-label" for="jvmJigasi"><fmt:message key="config.page.configuration.ofmeet.jvm.jigasi"/></label>
                </td>
                <td>
                    <input type="text" size="80" maxlength="2048" name="jvmJigasi" id="jvmJigasi" value="${admin:getProperty( "org.jitsi.videobridge.ofmeet.jigasi.jvm.customOptions", "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp")}" placeholder="-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp">
                </td>
            </tr>               
        </table>        
    </admin:contentBox> 

    <admin:contentBox title="<%= clusteringTitle%>">
        <p>
            <fmt:message key="config.page.configuration.clustering.description">
                <fmt:param value="https://github.com/igniterealtime/openfire-pade-plugin/wiki/Clustering-multiple-Jitsi-Videobridges-using-Hazelcast-plugin"/>
            </fmt:message>			
        </p><p>&nbsp;</p>	
        <table cellpadding="3" cellspacing="0" border="0" width="100%"> 
            <tr>
                <td nowrap>
                    <label class="jive-label" for="fqdn"><fmt:message key="config.page.configuration.fqdn"/></label>
                </td>
                <td>
                    <input disabled type="text" size="40" maxlength="1024" name="fqdn" id="fqdn" value="${JiveGlobals.getXMLProperty( "fqdn", "")}">
                </td>
            </tr> 		
            <tr>
                <td nowrap>
                    <label class="jive-label" for="network_interface"><fmt:message key="config.page.configuration.network.interface"/></label>
                </td>
                <td>
                    <input disabled type="text" size="40" maxlength="1024" name="network_interface" id="network_interface" value="${JiveGlobals.getXMLProperty( "network.interface", "")}">
                </td>
            </tr> 		
            <tr>
                <td nowrap>
                    <label class="jive-label" for="interfaces_allowed"><fmt:message key="config.page.configuration.ofmeet.interfaces_allowed"/></label>
                </td>
                <td>
                    <input disabled type="text" size="40" maxlength="1024" name="interfaces_allowed" id="interfaces_allowed" value="${JiveGlobals.getXMLProperty( "ofmeet.interfaces_allowed", "")}">
                </td>
            </tr>  
            <tr>
                <td nowrap>
                    <label class="jive-label" for="addresses_allowed"><fmt:message key="config.page.configuration.ofmeet.addresses_allowed"/></label>
                </td>
                <td>
                    <input disabled type="text" size="40" maxlength="1024" name="addresses_allowed" id="addresses_allowed" value="${JiveGlobals.getXMLProperty( "ofmeet.addresses_allowed", "")}">
                </td>
            </tr> 
            <tr>
                <td nowrap>
                    <label class="jive-label" for="local_address"><fmt:message key="config.page.configuration.ofmeet.local_address"/></label>
                </td>
                <td>
                    <input disabled type="text" size="40" maxlength="1024" name="local_address" id="local_address" value="${JiveGlobals.getXMLProperty( "ofmeet.local_address", "")}">
                </td>
            </tr> 
            <tr>
                <td nowrap>
                    <label class="jive-label" for="public_address"><fmt:message key="config.page.configuration.ofmeet.public_address"/></label>
                </td>
                <td>
                    <input disabled type="text" size="40" maxlength="1024" name="public_address" id="public_address" value="${JiveGlobals.getXMLProperty( "ofmeet.public_address", "")}">
                </td>
            </tr> 
            <tr>
                <td nowrap>
                    <label class="jive-label" for="octo_id"><fmt:message key="config.page.configuration.ofmeet.octo_id"/></label>
                </td>
                <td>
                    <input disabled type="text" size="40" maxlength="1024" name="octo_id" id="octo_id" value="${JiveGlobals.getXMLProperty( "ofmeet.octo_id", "")}">
                </td>
            </tr> 			
        </table>        
    </admin:contentBox> 

    <fmt:message key="config.page.configuration.ofmeet.title" var="boxtitleofmeet"/>    
    <admin:contentBox title="${boxtitleofmeet}">
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="websockets" ${admin:getBooleanProperty( "org.jitsi.videobridge.ofmeet.websockets", false) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.websockets.enabled" />
                </td>
            </tr>      			
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="enableRtx" ${ofmeetConfig.disableRtx ? "" : "checked"}>
                    <fmt:message key="config.page.configuration.ofmeet.disableRtx.enabled_desc" />
                </td>
            </tr>            
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="useipv6" ${admin:getBooleanProperty( "org.jitsi.videobridge.ofmeet.useipv6", false) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.useipv6.enabled_desc" />
                </td>
            </tr>
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="usenicks" ${admin:getBooleanProperty( "org.jitsi.videobridge.ofmeet.usenicks", false) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.usenicks.enabled_desc" />
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="displayNotice"><fmt:message key="config.page.configuration.ofmeet.display.notice"/></label>
                </td>
                <td>
                    <input type="text" size="70" maxlength="255" name="displayNotice" id="displayNotice" value="${admin:getProperty( "org.jitsi.videobridge.ofmeet.display.notice", "")}">
                </td>
            </tr>            
        </table>
    </admin:contentBox>   
    
    <fmt:message key="config.page.configuration.p2p.title" var="boxtitlep2p"/>
    <admin:contentBox title="${boxtitlep2p}">
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="p2pEnabled" ${!ofmeetConfig.p2pEnabled ? "" : "checked"}>
                    <fmt:message key="config.page.configuration.p2p.enabled" />
                </td>
            </tr>
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="p2pPreferH264" ${!ofmeetConfig.p2pPreferH264 ? "" : "checked"}>
                    <fmt:message key="config.page.configuration.p2p.preferh264" />
                </td>
            </tr>   
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="p2pDisableH264" ${!ofmeetConfig.p2pDisableH264 ? "" : "checked"}>
                    <fmt:message key="config.page.configuration.p2p.disableh264" />
                </td>
            </tr>              
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="p2pUseStunTurn" ${!ofmeetConfig.p2pUseStunTurn ? "" : "checked"}>
                    <fmt:message key="config.page.configuration.p2p.usestunturn" />
                </td>
            </tr>             
        </table>
    </admin:contentBox>    

    <fmt:message key="config.page.configuration.media.title" var="boxtitlemedia"/>
    <admin:contentBox title="${boxtitlemedia}">
        <p>
            <fmt:message key="config.page.configuration.ofmeet.constraints.description"/>
        </p><p>&nbsp;</p>
        <table>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="resolution"><fmt:message key="config.page.configuration.ofmeet.resolution"/></label>
                </td>
                <td>
                    <input type="text" name="resolution" id="resolution" value="${ofmeetConfig.resolution}">
                    <label for="resolution"><fmt:message key="config.page.configuration.ofmeet.resolution.unit"/></label>					
                </td>			
                <td nowrap style="padding-left: 10px;">
                    <label class="jive-label" for="videoConstraintsIdealAspectRatio"><fmt:message key="config.page.configuration.ofmeet.constraints.video.aspectratio.ideal"/></label>
                </td>
                <td>
                    <input type="text" name="videoConstraintsIdealAspectRatio" id="videoConstraintsIdealAspectRatio" value="${ofmeetConfig.videoConstraintsIdealAspectRatio}">
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="videoConstraintsMinWidth"><fmt:message key="config.page.configuration.ofmeet.constraints.video.width.min"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsMinWidth" id="videoConstraintsMinWidth" value="${ofmeetConfig.videoConstraintsMinWidth}">
                    <label for="videoConstraintsMinWidth"><fmt:message key="config.page.configuration.ofmeet.constraints.video.width.unit"/></label>
                </td>
                <td nowrap style="padding-left: 10px;">
                    <label class="jive-label" for="videoConstraintsMinHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.min"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsMinHeight" id="videoConstraintsMinHeight" value="${ofmeetConfig.videoConstraintsMinHeight}">
                    <label for="videoConstraintsMinHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.unit"/></label>
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="videoConstraintsIdealWidth"><fmt:message key="config.page.configuration.ofmeet.constraints.video.width.ideal"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsIdealWidth" id="videoConstraintsIdealWidth" value="${ofmeetConfig.videoConstraintsIdealWidth}">
                    <label for="videoConstraintsIdealWidth"><fmt:message key="config.page.configuration.ofmeet.constraints.video.width.unit"/></label>
                </td>
                <td nowrap style="padding-left: 10px;">
                    <label class="jive-label" for="videoConstraintsIdealHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.ideal"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsIdealHeight" id="videoConstraintsIdealHeight" value="${ofmeetConfig.videoConstraintsIdealHeight}">
                    <label for="videoConstraintsIdealHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.unit"/></label>
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="videoConstraintsMaxWidth"><fmt:message key="config.page.configuration.ofmeet.constraints.video.width.max"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsMaxWidth" id="videoConstraintsMaxWidth" value="${ofmeetConfig.videoConstraintsMaxWidth}">
                    <label for="videoConstraintsMaxWidth"><fmt:message key="config.page.configuration.ofmeet.constraints.video.width.unit"/></label>
                </td>
                <td nowrap style="padding-left: 10px;">
                    <label class="jive-label" for="videoConstraintsMaxHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.max"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsMaxHeight" id="videoConstraintsMaxHeight" value="${ofmeetConfig.videoConstraintsMaxHeight}">
                    <label for="videoConstraintsMaxHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.unit"/></label>
                </td>
            </tr>
        </table>
               
        <p style="margin-top: 2em">
            <fmt:message key="config.page.configuration.ofmeet.bitrates.description"/>
        </p>
        <table>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="lowMaxBitratesVideo"><fmt:message key="config.page.configuration.ofmeet.bitrates.low.max.bitrates.video"/></label>
                </td>
                <td>
                    <input type="text" name="lowMaxBitratesVideo" id="lowMaxBitratesVideo" value="${admin:getProperty( "org.jitsi.videobridge.low.max.bitrates.video", "100000")}">
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="highMaxBitratesVideo"><fmt:message key="config.page.configuration.ofmeet.bitrates.high.max.bitrates.video"/></label>
                </td>
                <td>
                    <input type="text" name="highMaxBitratesVideo" id="highMaxBitratesVideo" value="${admin:getProperty( "org.jitsi.videobridge.high.max.bitrates.video", "1200000")}">
                </td>
            </tr>       
            <tr>
                <td nowrap>
                    <label class="jive-label" for="standardMaxBitratesVideo"><fmt:message key="config.page.configuration.ofmeet.bitrates.standard.max.bitrates.video"/></label>
                </td>
                <td>
                    <input type="text" name="standardMaxBitratesVideo" id="standardMaxBitratesVideo" value="${admin:getProperty( "org.jitsi.videobridge.standard.max.bitrates.video", "300000")}">
                </td>
            </tr>                
        </table>        

        <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.audiooptions.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="startaudioonly" ${ofmeetConfig.startAudioOnly ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.startaudioonly"/>
                </td>
            </tr>
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="enableStereo" ${admin:getBooleanProperty( "ofmeet.stereo.enabled", false) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.stereo.enabled"/>
                </td>
            </tr>            
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="lipSync" ${ofmeetConfig.lipSync ? "checked" : ""}>
                    <fmt:message key="ofmeet.lipSync.enabled" />
                </td>
            </tr>
        </table>
        
       <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.min.height.for.quality.level"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.min.height.for.quality.low"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="minHeightForQualityLow" value="${admin:getProperty( "ofmeet.min.height.for.quality.level.low", "180")}"></td>
            </tr>
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.min.height.for.quality.std"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="minHeightForQualityStd" value="${admin:getProperty( "ofmeet.min.height.for.quality.level.std", "360")}"></td>
            </tr>
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.min.height.for.quality.high"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="minHeightForQualityHigh" value="${admin:getProperty( "ofmeet.min.height.for.quality.level.high", "720")}"></td>
            </tr>            
        </table>
        
       <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.max.full.resolution.participants.desc"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.max.full.resolution.participants"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="maxFullResolutionParticipants" value="${admin:getProperty( "ofmeet.max.full.resolution.participants", "-1")}"></td>
            </tr>           
        </table>        

        <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.startvideomuted.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.startvideomuted"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="startvideomuted" value="${ofmeetConfig.startVideoMuted}"></td>
            </tr>
        </table>        

        <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.startaudiomuted.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.startaudiomuted"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="startaudiomuted" value="${ofmeetConfig.startAudioMuted}"></td>
            </tr>
        </table>
        
        <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.desktop.sharing.framerate.desc"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.desktop.sharing.framerate.min"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="desktopSharingFrameRateMin" value="${admin:getProperty( "ofmeet.desktop.sharing.framerate.min", "5")}"></td>
            </tr>
            <tr>
                <td align="left" width="300"><fmt:message key="config.page.configuration.ofmeet.desktop.sharing.framerate.max"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="desktopSharingFrameRateMax" value="${admin:getProperty( "ofmeet.desktop.sharing.framerate.max", "25")}"></td>
            </tr>           
        </table>        
    </admin:contentBox>

    <fmt:message key="config.page.configuration.security.title" var="boxtitlesecurity"/>
    <admin:contentBox title="${boxtitlesecurity}">
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="200"><fmt:message key="config.page.configuration.focus.jid"/>:</td>
                <td><input type="text" size="20" maxlength="100" value="focus" readonly></td>
            </tr>
            <tr>
                <td align="left" width="200"><fmt:message key="config.page.configuration.focus.password"/>:</td>
                <td><input type="password" size="20" maxlength="100" name="focuspassword" value="${ofmeetConfig.focusPassword}"></td>
            </tr>
            <tr>
                <td align="left" width="200"><fmt:message key="config.page.configuration.jvb.name"/>:</td>
                <td><input type="text" size="20" maxlength="100" name="jvbname" value="${ofmeetConfig.jvbName}"></td>
            </tr>
            <tr>
                <td align="left" width="200"><fmt:message key="config.page.configuration.jvb.password"/>:</td>
                <td><input type="password" size="20" maxlength="100" name="jvbpassword" value="${ofmeetConfig.jvbPassword}"></td>
            </tr>            
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="securityenabled" ${admin:getBooleanProperty( "ofmeet.winsso.enabled", false) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.winsso.enabled_description" />
                </td>
            </tr>
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="webauthn_enabled" ${admin:getBooleanProperty( "ofmeet.webauthn.enabled", false) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.webauthn.enabled_description" />
                </td>
            </tr>           
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="conferenceadmin" ${admin:getBooleanProperty( "ofmeet.conference.admin", true) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.security.conference.admin" />
                </td>
            </tr>            
        </table>
    </admin:contentBox>

    <fmt:message key="config.page.configuration.lastn.title" var="boxtitlelastn"/>
    <admin:contentBox title="${boxtitlelastn}">
        <p><fmt:message key="config.page.configuration.lastn.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="200"><fmt:message key="config.page.configuration.channellastn"/>:</td>
                <td><input type="text" size="20" maxlength="20" name="channellastn" value="${ofmeetConfig.channelLastN}"></td>
            </tr>
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="adaptivelastn" ${ofmeetConfig.adaptiveLastN ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.adaptivelastn" />
                </td>
            </tr>         
        </table>
    </admin:contentBox>

    <fmt:message key="config.page.configuration.simulcast.title" var="boxtitlesimulcast"/>
    <admin:contentBox title="${boxtitlesimulcast}">
        <p><fmt:message key="config.page.configuration.simulcast.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="simulcast" ${ofmeetConfig.simulcast ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.simulcast" />
                </td>
            </tr>
            </tr>
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="adaptivesimulcast" ${ofmeetConfig.adaptiveSimulcast ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.adaptivesimulcast" />
                </td>
            </tr>
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="capScreenshareBitrate" ${admin:getBooleanProperty( "ofmeet.cap.screenshare.bitrate", true) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.cap.screenshare.bitrate" />
                </td>
            </tr>  
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="enableLayerSuspension" ${admin:getBooleanProperty( "ofmeet.enable.layer.suspension", true) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.enable.layer.suspension" />
                </td>
            </tr>                 
        </table>
    </admin:contentBox>
    
    <fmt:message key="config.page.configuration.livestream.title" var="boxtitlelivestream"/>
    <admin:contentBox title="${boxtitlelivestream}">
        <p><fmt:message key="config.page.configuration.livestream.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>            
                <td nowrap colspan="2">
                    <input type="checkbox" id="liveStreamEnabled" name="liveStreamEnabled" ${ofmeetConfig.getLiveStreamEnabled() ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.livestream.enabled" />
                </td>               
            </tr>
            <tr>
                <td width="200"><label for="liveStreamUrl"><fmt:message key="config.page.configuration.livestream.url"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="liveStreamUrl" id="liveStreamUrl" value="${ofmeetConfig.liveStreamUrl.get()}"></td>
            </tr>   
            <tr>
                <td width="200"><label for="liveStreamKey"><fmt:message key="config.page.configuration.livestream.key"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="liveStreamKey" id="liveStreamKey" value="${ofmeetConfig.liveStreamKey.get()}"></td>
            </tr>               
        </table>
    </admin:contentBox> 

    <input type="hidden" name="csrf" value="${csrf}">

    <input type="submit" name="update" value="<fmt:message key="global.save_settings" />">
</form>
</body>
</html>
