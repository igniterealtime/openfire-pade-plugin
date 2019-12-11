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
<%@ page import="org.jitsi.impl.neomedia.transform.srtp.SRTPCryptoContext" %>
<%@ page import="org.jivesoftware.openfire.XMPPServer" %>
<%@ page import="org.jivesoftware.openfire.plugin.ofmeet.OfMeetPlugin" %>
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
    boolean update = request.getParameter( "update" ) != null;

    final Cookie csrfCookie = CookieUtils.getCookie( request, "csrf" );
    final String csrfParam = ParamUtils.getParameter( request, "csrf" );

    // Get handle on the plugin
    final OfMeetPlugin container = (OfMeetPlugin) XMPPServer.getInstance().getPluginManager().getPlugin( "ofmeet" );

    final Map<String, String> errors = new HashMap<>();

    if ( update )
    {
        if ( csrfCookie == null || csrfParam == null || !csrfCookie.getValue().equals( csrfParam ) )
        {
            errors.put( "csrf", "CSRF Failure!" );
        }

        final boolean securityenabled = ParamUtils.getBooleanParameter( request, "securityenabled" );
        final boolean disableRtx = !ParamUtils.getBooleanParameter( request, "enableRtx" );
        final String authusername = request.getParameter( "authusername" );
        final String sippassword = request.getParameter( "sippassword" );
        final String server = request.getParameter( "server" );
        final String outboundproxy = request.getParameter( "outboundproxy" );
        final String iceServers = request.getParameter( "iceservers" );

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

        final boolean useIPv6 = ParamUtils.getBooleanParameter( request, "useipv6" );
        final boolean useNicks = ParamUtils.getBooleanParameter( request, "usenicks" );
        final String videoConstraintsIdealAspectRatio = request.getParameter( "videoConstraintsIdealAspectRatio" );
        if(!videoConstraintsIdealAspectRatio.matches("[0-9: .,/]+"))
        {
            errors.put( "videoConstraintsIdealAspectRatio", "Cannot parse value as aspect ratio value." );
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

        final String clientusername = request.getParameter( "clientusername" );
        final String clientpassword = request.getParameter( "clientpassword" );
        final String enableSip = request.getParameter( "enableSip" );
        final boolean allowdirectsip = ParamUtils.getBooleanParameter( request, "allowdirectsip" );

        final String focuspassword = request.getParameter( "focuspassword" );
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

        if ( errors.isEmpty() )
        {
            JiveGlobals.setProperty( "ofmeet.security.enabled", Boolean.toString( securityenabled ) );
            JiveGlobals.setProperty( "voicebridge.default.proxy.sipauthuser", authusername );
            JiveGlobals.setProperty( "voicebridge.default.proxy.sippassword", sippassword );
            JiveGlobals.setProperty( "voicebridge.default.proxy.sipserver", server );
            JiveGlobals.setProperty( "voicebridge.default.proxy.outboundproxy", outboundproxy );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.iceservers", iceServers );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.useipv6", Boolean.toString( useIPv6 ) );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.usenicks", Boolean.toString( useNicks ) );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.username", clientusername );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.password", clientpassword );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.enabled", enableSip );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.allow.direct.sip", Boolean.toString( allowdirectsip ) );
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.sip.hq.voice", hqVoice );

            ofmeetConfig.setDisableRtx( disableRtx );
            ofmeetConfig.setStartAudioOnly( startaudioonly );
            ofmeetConfig.setStartAudioMuted( startaudiomuted == null || startaudiomuted.isEmpty() ? null : Integer.parseInt( startaudiomuted ));
            ofmeetConfig.setStartVideoMuted( startvideomuted == null || startvideomuted.isEmpty() ? null : Integer.parseInt( startvideomuted ));
            ofmeetConfig.setVideoConstraintsIdealAspectRatio( videoConstraintsIdealAspectRatio );
            ofmeetConfig.setVideoConstraintsMinHeight( Integer.parseInt( videoConstraintsMinHeight ) );
            ofmeetConfig.setVideoConstraintsIdealHeight( Integer.parseInt( videoConstraintsIdealHeight ) );
            ofmeetConfig.setVideoConstraintsMaxHeight( Integer.parseInt( videoConstraintsMaxHeight ) );
            ofmeetConfig.setChannelLastN( channelLastN );
            ofmeetConfig.setAdaptiveLastN( adaptivelastn );
            ofmeetConfig.setSimulcast( simulcast );
            ofmeetConfig.setAdaptiveSimulcast( adaptivesimulcast );
            ofmeetConfig.setFocusPassword( focuspassword );

            container.populateJitsiSystemPropertiesWithJivePropertyValues();

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

    <fmt:message key="config.page.configuration.ofmeet.title" var="boxtitleofmeet"/>
    <admin:contentBox title="${boxtitleofmeet}">
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
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
                <td colspan="2" align="left" width="200"><fmt:message key="config.page.configuration.ofmeet.iceservers"/>:</td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="text" size="100" maxlength="256" name="iceservers"
                           value="${admin:getProperty("org.jitsi.videobridge.ofmeet.iceservers", "")}"
                           placeholder="{ 'iceServers': [{ 'url': 'stun:stun.l.google.com:19302' }] }">
                </td>
            </tr>
        </table>
    </admin:contentBox>

    <fmt:message key="config.page.configuration.media.title" var="boxtitlemedia"/>
    <admin:contentBox title="${boxtitlemedia}">
        <p>
            <fmt:message key="config.page.configuration.ofmeet.constraints.description"/>
        </p>
        <table>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="videoConstraintsIdealAspectRatio"><fmt:message key="config.page.configuration.ofmeet.constraints.video.aspectratio.ideal"/></label>
                </td>
                <td>
                    <input type="text" name="videoConstraintsIdealAspectRatio" id="videoConstraintsIdealAspectRatio" value="${ofmeetConfig.videoConstraintsIdealAspectRatio}">
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="videoConstraintsMinHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.min"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsMinHeight" id="videoConstraintsMinHeight" value="${ofmeetConfig.videoConstraintsMinHeight}">
                    <label for="videoConstraintsMinHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.unit"/></label>
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="videoConstraintsIdealHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.ideal"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsIdealHeight" id="videoConstraintsIdealHeight" value="${ofmeetConfig.videoConstraintsIdealHeight}">
                    <label for="videoConstraintsIdealHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.unit"/></label>
                </td>
            </tr>
            <tr>
                <td nowrap>
                    <label class="jive-label" for="videoConstraintsMaxHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.max"/></label>
                </td>
                <td>
                    <input type="number" min="0" name="videoConstraintsMaxHeight" id="videoConstraintsMaxHeight" value="${ofmeetConfig.videoConstraintsMaxHeight}">
                    <label for="videoConstraintsMaxHeight"><fmt:message key="config.page.configuration.ofmeet.constraints.video.height.unit"/></label>
                </td>
            </tr>
        </table>

        <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.audioonly.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="startaudioonly" ${ofmeetConfig.startAudioOnly ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.ofmeet.audioonly"/>
                </td>
            </tr>
        </table>

        <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.startaudiomuted.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="400"><fmt:message key="config.page.configuration.ofmeet.startaudiomuted"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="startaudiomuted" value="${ofmeetConfig.startAudioMuted}"></td>
            </tr>
        </table>

        <p style="margin-top: 2em"><fmt:message key="config.page.configuration.ofmeet.startvideomuted.description"/></p>
        <table cellpadding="3" cellspacing="0" border="0" width="100%">
            <tr>
                <td align="left" width="400"><fmt:message key="config.page.configuration.ofmeet.startvideomuted"/>:</td>
                <td><input type="text" size="10" maxlength="5" name="startvideomuted" value="${ofmeetConfig.startVideoMuted}"></td>
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
                <td nowrap colspan="2">
                    <input type="checkbox" name="securityenabled" ${admin:getBooleanProperty( "ofmeet.security.enabled", true) ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.security.enabled_description" />
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
            <tr>
                <td nowrap colspan="2">
                    <input type="checkbox" name="adaptivesimulcast" ${ofmeetConfig.adaptiveSimulcast ? "checked" : ""}>
                    <fmt:message key="config.page.configuration.adaptivesimulcast" />
                </td>
            </tr>
        </table>
    </admin:contentBox>

    <input type="hidden" name="csrf" value="${csrf}">

    <input type="submit" name="update" value="<fmt:message key="global.save_settings" />">
</form>
</body>
</html>
