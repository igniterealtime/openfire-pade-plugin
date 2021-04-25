<%--
  ~ Copyright (C) 2018 Ignite Realtime Foundation. All rights reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ page import="org.jivesoftware.openfire.XMPPServer" %>
<%@ page import="org.jivesoftware.openfire.plugin.ofmeet.OfMeetPlugin" %>
<%@ page import="org.jivesoftware.util.CookieUtils" %>
<%@ page import="org.jivesoftware.util.ParamUtils" %>
<%@ page import="org.jivesoftware.util.StringUtils" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jivesoftware.openfire.user.UserManager" %>
<%@ page import="org.jivesoftware.util.JiveGlobals" %>
<%@ page import="org.jivesoftware.openfire.auth.AuthFactory" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="admin" prefix="admin" %>
<jsp:useBean id="ofmeetConfig" class="org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig"/>
<%
    boolean update = request.getParameter("update") != null;

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

        final String jigasiSipServerAddress = request.getParameter( "jigasiSipServerAddress" );
        final String jigasiSipTransport = request.getParameter( "jigasiSipTransport" );
        final String jigasiSipPassword = request.getParameter( "jigasiSipPassword" );
        final String jigasiProxyServer = request.getParameter( "jigasiProxyServer" );     
        final String jigasiProxyPort = request.getParameter( "jigasiProxyPort" );          
        final String jigasiSipUserId = request.getParameter( "jigasiSipUserId" );
        final String jigasiXmppPassword = request.getParameter( "jigasiXmppPassword" );
        final String jigasiXmppUserId = request.getParameter( "jigasiXmppUserId" );      
        final String jigasiXmppRoomName = request.getParameter( "jigasiXmppRoomName" ); 
		final String jigasiSipHeaderRoomName = request.getParameter( "jigasiSipHeaderRoomName" ); 
        final String jigasiFreeSwitchHost = request.getParameter( "jigasiFreeSwitchHost" );          
        final String jigasiFreeSwitchPassword = request.getParameter( "jigasiFreeSwitchPassword" );    
        
        final boolean jigasiSipEnabled = ParamUtils.getBooleanParameter( request, "jigasiSipEnabled" );         
        final boolean jigasiFreeSwitchEnabled = ParamUtils.getBooleanParameter( request, "jigasiFreeSwitchEnabled" );
		final boolean audiobridgeEnabled = ParamUtils.getBooleanParameter( request, "audiobridgeEnabled" );
		final boolean audiobridgeLogging = ParamUtils.getBooleanParameter( request, "audiobridgeLogging" );
		final boolean audiobridgeRegisterAll = ParamUtils.getBooleanParameter( request, "audiobridgeRegisterAll" );		
		
        if (audiobridgeEnabled && jigasiFreeSwitchEnabled)
		{
			errors.put( "Audiobridge and FreeSWITCH", "Cannot use both at the same time" );		
		}

        final String jvmJigasi = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.jigasi.jvm.customOptions" );
		
        if (jigasiSipEnabled && jvmJigasi != null && jvmJigasi.isEmpty())
		{
			JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.jigasi.jvm.customOptions", "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp" );
		}
		

        if ( errors.isEmpty() )
        {
            ofmeetConfig.jigasiSipServerAddress.set( jigasiSipServerAddress );
            ofmeetConfig.jigasiSipTransport.set( jigasiSipTransport );
            ofmeetConfig.jigasiProxyServer.set( jigasiProxyServer );            
            ofmeetConfig.jigasiProxyPort.set( jigasiProxyPort );              
            ofmeetConfig.jigasiSipPassword.set( jigasiSipPassword );
            ofmeetConfig.jigasiSipUserId.set( jigasiSipUserId );
            ofmeetConfig.jigasiXmppPassword.set( jigasiXmppPassword );
            ofmeetConfig.jigasiXmppRoomName.set( jigasiXmppRoomName );
            ofmeetConfig.jigasiSipHeaderRoomName.set( jigasiSipHeaderRoomName );			
            ofmeetConfig.jigasiXmppUserId.set( jigasiXmppUserId );  
            ofmeetConfig.jigasiFreeSwitchPassword.set( jigasiFreeSwitchPassword );
            ofmeetConfig.jigasiFreeSwitchHost.set( jigasiFreeSwitchHost );
            ofmeetConfig.jigasiSipEnabled.set( Boolean.toString(jigasiSipEnabled) );            
            ofmeetConfig.jigasiFreeSwitchEnabled.set( Boolean.toString(jigasiFreeSwitchEnabled) );            
            ofmeetConfig.audiobridgeEnabled.set( Boolean.toString(audiobridgeEnabled) ); 
			ofmeetConfig.audiobridgeLogging.set( Boolean.toString(audiobridgeLogging) ); 
			ofmeetConfig.audiobridgeRegisterAll.set( Boolean.toString(audiobridgeRegisterAll) ); 
			
            response.sendRedirect( "ofmeet-sipsettings.jsp?settingsSaved=true" );
            return;
        }
    }

    // Check if the XMPP account can be used to log in.
    boolean xmppAccountVerified = true;
    try
    {
        AuthFactory.getAuthProvider().authenticate( ofmeetConfig.getJigasiXmppUserId().get(), ofmeetConfig.getJigasiXmppPassword().get() );
    }
    catch ( Exception e )
    {
        xmppAccountVerified = false;
    }

    final String csrf = StringUtils.randomString( 15 );
    CookieUtils.setCookie( request, response, "csrf", csrf, -1 );

    pageContext.setAttribute( "csrf", csrf );
    pageContext.setAttribute( "errors", errors );
    pageContext.setAttribute( "userProviderReadOnly", UserManager.getUserProvider().isReadOnly() );
    pageContext.setAttribute( "allowAnonymousClientAuth", JiveGlobals.getBooleanProperty( "xmpp.auth.anonymous" ) );
    pageContext.setAttribute( "xmppAccountVerified", xmppAccountVerified );
%>
<html>
<head>
    <title><fmt:message key="sipsettings.title" /></title>
    <meta name="pageID" content="ofmeet-sipsettings"/>
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

<p><fmt:message key="sipsettings.introduction" /></p>

<form action="ofmeet-sipsettings.jsp" method="post">

	<!--
    <fmt:message key="audiobridge.title" var="boxtitleAudiobridge"/>
    <admin:contentBox title="${boxtitleAudiobridge}">
        <p>
            <fmt:message key="audiobridge.description"/>
        </p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" id="audiobridgeEnabled" name="audiobridgeEnabled" ${ofmeetConfig.getAudiobridgeEnabled() ? "checked" : ""}>
                    <fmt:message key="audiobridge.enabled" />
                </td>				
            </tr> 			
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" id="audiobridgeLogging" name="audiobridgeLogging" ${ofmeetConfig.getAudiobridgeLogging() ? "checked" : ""}>
                    <fmt:message key="audiobridge.logging.enabled" />
                </td>				
            </tr>  		
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" id="audiobridgeRegisterAll" name="audiobridgeRegisterAll" ${ofmeetConfig.getAudiobridgeRegisterAll() ? "checked" : ""}>
                    <fmt:message key="audiobridge.allusers.register" />
                </td>				
            </tr>  		
        </table>
    </admin:contentBox>  
	-->
    
    <fmt:message key="sipsettings.freeswitch.title" var="boxtitleFreeswitch"/>
    <admin:contentBox title="${boxtitleFreeswitch}">
        <p>
            <fmt:message key="sipsettings.freeswitch.description"/>
        </p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" id="jigasiFreeSwitchEnabled" name="jigasiFreeSwitchEnabled" ${ofmeetConfig.getJigasiFreeSwitchEnabled() ? "checked" : ""}>
                    <fmt:message key="sipsettings.freeswitch.enabled" />
                </td>
            </tr>         
            <tr>
                <td width="200"><label for="jigasiFreeSwitchHost"><fmt:message key="sipsettings.freeswitch.hostname"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiFreeSwitchHost" id="jigasiFreeSwitchHost" value="${ofmeetConfig.jigasiFreeSwitchHost.get() == null ? '' : ofmeetConfig.jigasiFreeSwitchHost.get()}"></td>
            </tr>
            <tr>
                <td width="200"><label for="jigasiFreeSwitchPassword"><fmt:message key="sipsettings.freeswitch.password"/>:</label></td>
                <td><input type="password" size="60" maxlength="100" name="jigasiFreeSwitchPassword" id="jigasiFreeSwitchPassword" value="${ofmeetConfig.jigasiFreeSwitchPassword.get() == null ? 'ClueCon' : ofmeetConfig.jigasiFreeSwitchPassword.get()}"></td>
            </tr>            
        </table>
    </admin:contentBox>  
    <fmt:message key="sipsettings.sip.account.title" var="boxtitleAccount"/>
    <admin:contentBox title="${boxtitleAccount}">
        <p>
            <fmt:message key="sipsettings.sip.account.description"/>
        </p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" id="jigasiSipEnabled" name="jigasiSipEnabled" ${ofmeetConfig.getJigasiSipEnabled() ? "checked" : ""}>
                    <fmt:message key="sipsettings.sip.enabled" />
                </td>
            </tr>        
            <tr>
                <td width="200"><label for="jigasiSipUserId"><fmt:message key="sipsettings.sip.account.user-id"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiSipUserId" id="jigasiSipUserId" value="${ofmeetConfig.jigasiSipUserId.get() == null ? '' : ofmeetConfig.jigasiSipUserId.get()}"></td>
            </tr>
            <tr>
                <td width="200"><label for="jigasiSipPassword"><fmt:message key="sipsettings.sip.account.password"/>:</label></td>
                <td><input type="password" size="60" maxlength="100" name="jigasiSipPassword" id="jigasiSipPassword" value="${ofmeetConfig.jigasiSipPassword.get() == null ? '' : ofmeetConfig.jigasiSipPassword.get()}"></td>
            </tr>
            <tr>
                <td width="200"><label for="jigasiSipServerAddress"><fmt:message key="sipsettings.sip.account.server-address"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiSipServerAddress" id="jigasiSipServerAddress" value="${ofmeetConfig.jigasiSipServerAddress.get() == null ? '' : ofmeetConfig.jigasiSipServerAddress.get()}"></td>
            </tr>
            <tr>
                <td width="200"><label for="jigasiSipTransport"><fmt:message key="sipsettings.sip.account.sip.transport"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiSipTransport" id="jigasiSipTransport" value="${ofmeetConfig.jigasiSipTransport.get() == null ? '' : ofmeetConfig.jigasiSipTransport.get()}"></td>
            </tr>
            <tr>
                <td width="200"><label for="jigasiProxyServer"><fmt:message key="sipsettings.sip.account.proxy.server"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiProxyServer" id="jigasiProxyServer" value="${ofmeetConfig.jigasiProxyServer.get() == null ? '' : ofmeetConfig.jigasiProxyServer.get()}"></td>
            </tr>  
            <tr>
                <td width="200"><label for="jigasiProxyPort"><fmt:message key="sipsettings.sip.account.proxy.port"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiProxyPort" id="jigasiProxyPort" value="${ofmeetConfig.jigasiProxyPort.get() == null ? '' : ofmeetConfig.jigasiProxyPort.get()}"></td>
            </tr> 
            <tr>
                <td width="200"><label for="jigasiSipHeaderRoomName"><fmt:message key="sipsettings.sip.header.room.name"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiSipHeaderRoomName" id="jigasiSipHeaderRoomName" value="${ofmeetConfig.jigasiSipHeaderRoomName.get() == null ? 'Jitsi-Conference-Room' : ofmeetConfig.jigasiSipHeaderRoomName.get()}"></td>
            </tr> 			
        </table>
    </admin:contentBox>

    <fmt:message key="sipsettings.xmpp.account.title" var="boxtitleAccount"/>
    <admin:contentBox title="${boxtitleAccount}">
        <p>
            <fmt:message key="sipsettings.xmpp.account.description"/>
        </p>
        <c:if test="${userProviderReadOnly}">
            <p><em><fmt:message key="sipsettings.xmpp.account.readonly"/></em></p>
        </c:if>

        <c:choose>
            <c:when test="${allowAnonymousClientAuth}">
                <p><em><fmt:message key="sipsettings.xmpp.account.anonymous"/></em></p>
            </c:when>
            <c:otherwise>
                <p><fmt:message key="sipsettings.xmpp.account.no-anonymous"/></p>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${not empty ofmeetConfig.jigasiXmppUserId.get() and xmppAccountVerified }">
                <admin:infoBox type="success"><fmt:message key="sipsettings.xmpp.account.verified" /></admin:infoBox>
            </c:when>
            <c:otherwise>
                <admin:infoBox type="warning"><fmt:message key="sipsettings.xmpp.account.unverified" /></admin:infoBox>
            </c:otherwise>
        </c:choose>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td width="200"><label for="jigasiXmppUserId"><fmt:message key="sipsettings.xmpp.account.user-id"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiXmppUserId" id="jigasiXmppUserId" value="${ofmeetConfig.jigasiXmppUserId.get() == null ? '' : ofmeetConfig.jigasiXmppUserId.get()}"></td>
            </tr>
            <tr>
                <td width="200"><label for="jigasiXmppPassword"><fmt:message key="sipsettings.xmpp.account.password"/>:</label></td>
                <td><input type="password" size="60" maxlength="100" name="jigasiXmppPassword" id="jigasiXmppPassword" value="${ofmeetConfig.jigasiXmppPassword.get() == null ? '' : ofmeetConfig.jigasiXmppPassword.get()}"></td>
            </tr>
            <tr>
                <td width="200"><label for="jigasiXmppRoomName"><fmt:message key="sipsettings.xmpp.room.name"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiXmppRoomName" id="jigasiXmppRoomName" value="${ofmeetConfig.jigasiXmppRoomName.get() == null ? 'siptest' : ofmeetConfig.jigasiXmppRoomName.get()}"></td>
            </tr>   			
        </table>
    </admin:contentBox>  

    <input type="hidden" name="csrf" value="${csrf}">

    <input type="submit" name="update" value="<fmt:message key="global.save_settings" />">

</form>
</body>
</html>
