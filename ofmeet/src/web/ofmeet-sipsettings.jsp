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
	final OfMeetPlugin container = (OfMeetPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("ofmeet");

	final Map<String, String> errors = new HashMap<>();

    if ( update )
	{
		if ( csrfCookie == null || csrfParam == null || !csrfCookie.getValue().equals( csrfParam ) )
		{
			errors.put( "csrf", "CSRF Failure!" );
		}

        final String jigasiSipServerAddress = request.getParameter( "jigasiSipServerAddress" );
        final String jigasiSipDomainBase = request.getParameter( "jigasiSipDomainBase" );
        final String jigasiSipPassword = request.getParameter( "jigasiSipPassword" );
        final String jigasiSipUserId = request.getParameter( "jigasiSipUserId" );
        final String jigasiXmppPassword = request.getParameter( "jigasiXmppPassword" );
        final String jigasiXmppUserId = request.getParameter( "jigasiXmppUserId" );

        if ( errors.isEmpty() )
		{
		    ofmeetConfig.jigasiSipServerAddress.set( jigasiSipServerAddress );
		    ofmeetConfig.jigasiSipDomainBase.set( jigasiSipDomainBase );
		    ofmeetConfig.jigasiSipPassword.set( jigasiSipPassword );
		    ofmeetConfig.jigasiSipUserId.set( jigasiSipUserId );
            ofmeetConfig.jigasiXmppPassword.set( jigasiXmppPassword );
            ofmeetConfig.jigasiXmppUserId.set( jigasiXmppUserId );

		    // Only reload everything if something changed.
		    if ( ofmeetConfig.jigasiSipServerAddress.wasChanged()
              || ofmeetConfig.jigasiSipDomainBase.wasChanged()
              || ofmeetConfig.jigasiSipPassword.wasChanged()
              || ofmeetConfig.jigasiSipUserId.wasChanged()
              || ofmeetConfig.jigasiXmppUserId.wasChanged()
              || ofmeetConfig.jigasiXmppPassword.wasChanged() )
            {
                container.populateJitsiSystemPropertiesWithJivePropertyValues();
            }
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

    <fmt:message key="sipsettings.sip.account.title" var="boxtitleAccount"/>
    <admin:contentBox title="${boxtitleAccount}">
        <p>
            <fmt:message key="sipsettings.sip.account.description"/>
        </p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
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
                <td width="200"><label for="jigasiSipDomainBase"><fmt:message key="sipsettings.sip.account.domain-base"/>:</label></td>
                <td><input type="text" size="60" maxlength="100" name="jigasiSipDomainBase" id="jigasiSipDomainBase" value="${ofmeetConfig.jigasiSipDomainBase.get() == null ? '' : ofmeetConfig.jigasiSipDomainBase.get()}"></td>
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
        </table>
    </admin:contentBox>

    <input type="hidden" name="csrf" value="${csrf}">

    <input type="submit" name="update" value="<fmt:message key="global.save_settings" />">

</form>
</body>
</html>
