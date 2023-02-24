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

/*
 * Jitsi Videobridge, OpenSource video conferencing.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jivesoftware.openfire.plugin.ofmeet;

import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A servlet that generates a snippet of javascript (json) that is the 'config' variable, as used by the Jitsi
 * Meet webapplication.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class ConfigServlet extends HttpServlet
{
    /*
        As compared to version 0.3 of OFMeet, various bits are missing:
        - user avatars cannot be set in the config any longer - likely, it needs to be retrieved by the webapp though XMPP
        - bookmarks/autojoin should now also be retrieved by the webapp, through XMPP.
        - authentication should no longer occur at a servlet base, as the webapp now performs XMPP-based auth. We want to prevent duplicate logins.
        - SIP functionality was removed (but should likely be restored).
        - usenodejs config property was removed (does not appear to do anything any longer?)
     */
    private static final Logger Log = LoggerFactory.getLogger( ConfigServlet.class );
    public static String globalConferenceId = null;

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            Log.trace( "[{}] config requested.", request.getRemoteAddr() );

            final OFMeetConfig ofMeetConfig = new OFMeetConfig();

            final String xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
            final String mucDomain = JiveGlobals.getProperty( "ofmeet.main.muc", "conference" + "." + xmppDomain);

            final JSONArray conferences = new JSONArray();

            writeHeader( response );

            ServletOutputStream out = response.getOutputStream();

            String recordingKey = null;

            int minHDHeight = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.min.hdheight", 540 );
            int desktopSharingFrameRateMin = JiveGlobals.getIntProperty( "ofmeet.desktop.sharing.framerate.min", 5);            
            int desktopSharingFrameRateMax = JiveGlobals.getIntProperty( "ofmeet.desktop.sharing.framerate.max", 25);                   
            String defaultLanguage = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.default.language", null );
            boolean useNicks = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.usenicks", false );
            boolean websockets = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.websockets", true );
            boolean useIPv6 = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.useipv6", false );
            boolean useStunTurn = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.use.stunturn", false );
            boolean recordVideo = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.media.record", false );
            String defaultSipNumber = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.default.sip.number", "" );
            boolean useRtcpMux = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.use.rtcp.mux", true );
            boolean useBundle = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.use.bundle", true );
            boolean enableWelcomePage = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.welcomePage", true );
            boolean enableRtpStats = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.rtp.stats", true );
            String desktopSharingChromeExtensionId = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.chrome.extension.id", null );
            String desktopSharingFirefoxExtensionId = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.firefox.extension.id", null );
            boolean desktopSharingChromeEnabled = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.desktop.sharing.chrome.enabled", true );
            boolean desktopSharingFirefoxEnabled = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.desktop.sharing.firefox.enabled", true );
            String desktopSharingChromeSources = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.desktop.sharing.sources", "[\"screen\", \"window\", \"tab\"]" );
            String desktopSharingChromeMinExtVersion = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.min.chrome.ext.ver", null );
            String desktopSharingFirefoxMaxVersionExtRequired = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.desktop.sharing.firefox.max.ver.ext.required" );
            String desktopSharingFirefoxExtensionURL = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.desktop.sharing.firefox.ext.url" );
            int startBitrate = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.start.bitrate", 800 );
            boolean logStats = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.stats.logging", false );
            String iceServers = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.iceservers", "" );
            String xirsysUrl = JiveGlobals.getProperty( "ofmeet.xirsys.url", null );
            String etherpadBase = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.etherpad.url", null );
            boolean enableEtherpad = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.etherpad", true );
            boolean startEtherpad = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.start.etherpad", false );
            boolean ofmeetWinSSOEnabled = JiveGlobals.getBooleanProperty( "ofmeet.winsso.enabled", false );
            boolean ofmeetWebAuthnEnabled = JiveGlobals.getBooleanProperty( "ofmeet.webauthn.enabled", false );
            boolean enablePreJoinPage = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.prejoin.page", false );
            boolean enableStereo = JiveGlobals.getBooleanProperty( "ofmeet.stereo.enabled", false );
            boolean enableAudioLevels = JiveGlobals.getBooleanProperty( "ofmeet.audioLevels.enabled", false );
            boolean enableFeedback = JiveGlobals.getBooleanProperty( "ofmeet.feedback.enabled", true );
            
            int video_width_ideal =  JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.constraints.video.width.ideal", ofMeetConfig.getVideoConstraintsIdealHeight() * 16/9);
            int video_width_max = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.constraints.video.width.max", ofMeetConfig.getVideoConstraintsMaxHeight() * 16/9);
            int video_width_min = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.constraints.video.width.min", ofMeetConfig.getVideoConstraintsMinHeight() * 16/9);

            int lowMaxBitratesVideo = JiveGlobals.getIntProperty( "org.jitsi.videobridge.low.max.bitrates.video", 100000 );
            int standardMaxBitratesVideo = JiveGlobals.getIntProperty( "org.jitsi.videobridge.standard.max.bitrates.video", 300000 );
            int highMaxBitratesVideo = JiveGlobals.getIntProperty( "org.jitsi.videobridge.high.max.bitrates.video", 1200000 );

            boolean capScreenshareBitrate = JiveGlobals.getBooleanProperty( "ofmeet.cap.screenshare.bitrate", true);
            boolean enableLayerSuspension = JiveGlobals.getBooleanProperty( "ofmeet.enable.layer.suspension", true);
            String minHeightForQualityLvlLow = JiveGlobals.getProperty( "ofmeet.min.height.for.quality.level.low", "180" );
            String minHeightForQualityLvlStd = JiveGlobals.getProperty( "ofmeet.min.height.for.quality.level.std", "360" );
            String minHeightForQualityLvlHigh = JiveGlobals.getProperty( "ofmeet.min.height.for.quality.level.high", "720" );

            String displayNotice = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.display.notice", "");
            String ofmeetStreamKey = JiveGlobals.getProperty( "ofmeet.live.stream.key", "");
            String ofmeetStreamPort = JiveGlobals.getProperty( "ofmeet.live.stream.port", "8080");          
            boolean ofmeetLiveStream = JiveGlobals.getBooleanProperty( "ofmeet.live.stream.enabled", false);  
			boolean useNewBandwidthAllocationStrategy = JiveGlobals.getBooleanProperty( "ofmeet.use.new.bandwidth.allocation.strategy", true); 
            
            boolean wsBridgeChannel = JiveGlobals.getBooleanProperty( "ofmeet.bridge.ws.channel", org.jitsi.util.OSUtils.IS_WINDOWS);

            if ( xirsysUrl != null )
            {
                Log.info( "OFMeetConfig. found xirsys Url " + xirsysUrl );

                String xirsysJson = getHTML( xirsysUrl );
                Log.info( "OFMeetConfig. got xirsys json " + xirsysJson );

                JSONObject jsonObject = new JSONObject( xirsysJson );
                iceServers = jsonObject.getString( "d" );

                Log.info( "OFMeetConfig. got xirsys iceSevers " + iceServers );
            }

            final JSONObject config = new JSONObject();

            final Map<String, String> hosts = new HashMap<>();
            hosts.put( "domain", xmppDomain );
            hosts.put( "muc", mucDomain );			
            config.put( "hosts", hosts );

            final Map<String, Object> p2p = new HashMap<>();
            p2p.put( "enabled", ofMeetConfig.getP2pEnabled() );
			p2p.put( "enableUnifiedOnChrome", true);
            p2p.put( "preferredCodec", "VP9" );
            p2p.put( "preferH264", ofMeetConfig.getP2pPreferH264() );			
            p2p.put( "disableH264", ofMeetConfig.getP2pDisableH264() );
            p2p.put( "useStunTurn", ofMeetConfig.getP2pUseStunTurn() );
            config.put( "enableP2P", true );
            config.put( "p2p", p2p );			
            // TODO
            //if ( ofMeetConfig.getP2pStunServers() != null && !ofMeetConfig.getP2pStunServers().isEmpty() )
            //p2p.put( "stunServers", ofMeetConfig.getP2pStunServers() );

            if ( iceServers != null && !iceServers.trim().isEmpty() )
            {
                config.put( "iceServers", iceServers.trim() );
            }
            //config.put( "enforcedBridge", "jitsi-videobridge." + xmppDomain );
            config.put( "useStunTurn", useStunTurn );
			config.put( "useTurnUdp", false);
			
            if ( defaultLanguage != null && !defaultLanguage.trim().isEmpty() )
            {
                config.put( "defaultLanguage", defaultLanguage.trim() );
            }
            config.put( "prejoinPageEnabled", enablePreJoinPage );
            config.put( "useIPv6", useIPv6 );
            config.put( "useNicks", useNicks );
            config.put( "useRtcpMux", useRtcpMux );
            //config.put( "useBundle", useBundle );
            config.put( "enableWelcomePage", enableWelcomePage );
            config.put( "isBrand", false );			
            config.put( "enableClosePage", enableFeedback );
            config.put( "enableRtpStats", enableRtpStats );
            config.put( "enableLipSync", ofMeetConfig.getLipSync() );

            //config.put( "enableRemb", true );
            //config.put( "enableTcc", true );

            if ( recordingKey == null || recordingKey.isEmpty() )
            {
                config.put( "enableRecording", recordVideo );
            }
            else
            {
                config.put( "recordingKey", recordingKey );
            }
            config.put( "clientNode", "http://igniterealtime.org/ofmeet/jitsi-meet/" );
            config.put( "focusUserJid", XMPPServer.getInstance().createJID( "focus", null ).toBareJID() );
            config.put( "defaultSipNumber", defaultSipNumber );

            // Id of desktop streamer Chrome extension
            config.put( "desktopSharingChromeExtId", desktopSharingChromeExtensionId );

            // Whether desktop sharing should be disabled on Chrome.
            config.put( "desktopSharingChromeDisabled", !desktopSharingChromeEnabled );

            // The media sources to use when using screen sharing with the Chrome
            // extension.
            config.put( "desktopSharingChromeSources", new JSONArray( desktopSharingChromeSources ) );

            // Required version of Chrome extension
            config.put( "desktopSharingChromeMinExtVersion", desktopSharingChromeMinExtVersion );

            // Whether desktop sharing should be disabled on Firefox.
            config.put( "desktopSharingFirefoxExtId", desktopSharingFirefoxExtensionId );

            // Whether desktop sharing should be disabled on Firefox.
            config.put( "desktopSharingFirefoxDisabled", !desktopSharingFirefoxEnabled );

            config.put( "desktopSharingFirefoxMaxVersionExtRequired", desktopSharingFirefoxMaxVersionExtRequired );
            config.put( "desktopSharingFirefoxExtensionURL", desktopSharingFirefoxExtensionURL );

            config.put( "minHDHeight", minHDHeight );
            config.put( "hiddenDomain", "recorder." + xmppDomain );
            config.put( "startBitrate", startBitrate );
			
            config.put( "useNewBandwidthAllocationStrategy", useNewBandwidthAllocationStrategy );			

            final JSONObject videoQuality = new JSONObject();
            videoQuality.put( "preferredCodec", "VP9" );	
			
            final JSONObject maxBitratesVideo = new JSONObject();
            final JSONObject vp9 = new JSONObject();			
            vp9.put( "low", lowMaxBitratesVideo );
            vp9.put( "standard", standardMaxBitratesVideo );
            vp9.put( "high", highMaxBitratesVideo );
            maxBitratesVideo.put( "VP9", vp9 );	
			
            videoQuality.put( "maxBitratesVideo", maxBitratesVideo );

            final JSONObject minHeightForQualityLvl = new JSONObject();
            minHeightForQualityLvl.put( minHeightForQualityLvlLow, "low" );
            minHeightForQualityLvl.put( minHeightForQualityLvlStd, "standard" );
            minHeightForQualityLvl.put( minHeightForQualityLvlHigh, "high" );
            videoQuality.put( "minHeightForQualityLvl", minHeightForQualityLvl );
            config.put( "videoQuality", videoQuality );

            config.put( "recordingType", "colibri" );
            config.put( "disableAudioLevels", ! enableAudioLevels );
            config.put( "stereo", false );
            config.put( "requireDisplayName", true );
            config.put( "startAudioOnly", ofMeetConfig.getStartAudioOnly() );

            if ( ofMeetConfig.getStartAudioMuted() != null )
            {
                config.put( "startAudioMuted", ofMeetConfig.getStartAudioMuted() );
            }
            if ( ofMeetConfig.getStartVideoMuted() != null )
            {
                config.put( "startVideoMuted", ofMeetConfig.getStartVideoMuted() );
            }
            
            final JSONObject desktopSharingFrameRate = new JSONObject();
            desktopSharingFrameRate.put( "min", desktopSharingFrameRateMin );
            desktopSharingFrameRate.put( "max", desktopSharingFrameRateMax );           
            config.put( "desktopSharingFrameRate", desktopSharingFrameRate );
            

            // 'resolution' is used in some cases (chrome <61), newer versions use 'constraints'.
            config.put( "resolution", ofMeetConfig.getResolution() );
            final JSONObject constraints = new JSONObject();
            final JSONObject videoConstraints = new JSONObject();
            videoConstraints.put( "aspectRatio", (JSONString) ofMeetConfig::getVideoConstraintsIdealAspectRatio ); // This cast causes the JSON-quoting of strings to be skipped (the ratio here is a simple function, not text)
            final Map<String, Object> height = new HashMap<>();
            height.put( "ideal", ofMeetConfig.getVideoConstraintsIdealHeight() );
            height.put( "max", ofMeetConfig.getVideoConstraintsMaxHeight() );
            height.put( "min", ofMeetConfig.getVideoConstraintsMinHeight() );
            videoConstraints.put( "height", height );
            final Map<String, Object> width = new HashMap<>();
            width.put( "ideal", video_width_ideal );
            width.put( "max", video_width_max );
            width.put( "min", video_width_min );

            videoConstraints.put( "width", width );
            constraints.put( "video", videoConstraints );
            config.put( "constraints", constraints );
            config.put( "enableLayerSuspension", enableLayerSuspension );
			config.put( "enableUnifiedOnChrome", true);
			config.put( "enableForcedReload", true);
			config.put( "enableUserRolesBasedOnToken", false);

            final JSONObject deploymentInfo = new JSONObject();
            deploymentInfo.put( "region", "region" + JiveGlobals.getXMLProperty("ofmeet.octo_id", "1"));	
			deploymentInfo.put( "userRegion", "region" + JiveGlobals.getXMLProperty("ofmeet.octo_id", "1"));	
            config.put( "deploymentInfo", deploymentInfo );
			
            final JSONObject testing = new JSONObject();
            final JSONObject octo = new JSONObject();
            octo.put( "probability", ClusterManager.isClusteringEnabled() ? 1 : 0 );
            testing.put( "octo", octo);
            testing.put( "capScreenshareBitrate", capScreenshareBitrate ? 1 : 0 );
            config.put( "testing", testing );

            config.put( "maxFullResolutionParticipants", -1);
            config.put( "useRoomAsSharedDocumentName", false );
            config.put( "logStats", logStats );
            config.put( "ofmeetWinSSOEnabled", ofmeetWinSSOEnabled );
            config.put( "ofmeetWebAuthnEnabled", ofmeetWebAuthnEnabled );
            config.put( "ofmeetStreamKey", ofmeetStreamKey );
            config.put( "ofmeetLiveStream", ofmeetLiveStream );
            config.put( "ofmeetStreamPort", ofmeetStreamPort );

            config.put( "conferences", conferences );

            if ( globalConferenceId != null && !globalConferenceId.isEmpty() )
            {
                config.put( "globalConferenceId", globalConferenceId );
            }
            config.put( "disableRtx", ofMeetConfig.getDisableRtx() );
            config.put( "bosh", new URI( request.getScheme(), null, request.getServerName(), request.getServerPort(), "/http-bind/", null, null) );
            if (websockets)
            {
                config.put( "websocket", new URI( "https".equals(request.getScheme()) ? "wss" : "ws", null, request.getServerName(), request.getServerPort(), "/ws/", null, null) );
                config.put( "websocketKeepAliveUrl", new URI( request.getScheme(), null, request.getServerName(), request.getServerPort(), "/pade/keepalive/index.html", null, null) );
				
            }
            //config.put( "openBridgeChannel", wsBridgeChannel ? "websocket" : "datachannel" );
            config.put( "channelLastN", ofMeetConfig.getChannelLastN() );
            config.put( "adaptiveLastN", ofMeetConfig.getAdaptiveLastN() );
            config.put( "disableSimulcast", !ofMeetConfig.getSimulcast() );

            config.put( "webrtcIceUdpDisable", ofMeetConfig.getWebrtcIceUdpDisable() );
            config.put( "webrtcIceTcpDisable", ofMeetConfig.getWebrtcIceTcpDisable() );

            // TODO: find out if both of the settings below are in use (seems silly).
            config.put( "adaptiveSimulcast", ofMeetConfig.getAdaptiveSimulcast() );
            config.put( "disableAdaptiveSimulcast", !ofMeetConfig.getAdaptiveSimulcast() );

            if (enableStereo)
            {
                config.put( "disableAP", true );
                config.put( "disableAEC", true );
                config.put( "disableNS", true );
                config.put( "disableAGC", true );
                config.put( "disableHPF", true );
                config.put( "enableLipSync", false );
                config.put( "stereo", true );
                config.put( "opusMaxAverageBitrate", 510000 );
            }

            config.put( "enableNoisyMicDetection", true );
            config.put( "enableNoAudioDetection", true );

            config.put( "noticeMessage", displayNotice);

            if ( enableEtherpad && etherpadBase != null && !etherpadBase.trim().isEmpty() )
            {
                config.put( "etherpad_base", etherpadBase.trim() );
                config.put( "openSharedDocumentOnJoin", startEtherpad );
            }

            out.println( "var config = " + config.toString( 2 ) + ";" );
        }
        catch ( Exception e )
        {
            Log.error( "OFMeetConfig doGet Error", e );
        }
    }

    private void writeHeader( HttpServletResponse response )
    {
        try
        {
            response.setHeader( "Expires", "Sat, 6 May 1995 12:00:00 GMT" );
            response.setHeader( "Cache-Control", "no-store, no-cache, must-revalidate" );
            response.addHeader( "Cache-Control", "post-check=0, pre-check=0" );
            response.setHeader( "Pragma", "no-cache" );
            response.setHeader( "Content-Type", "application/javascript" );
            response.setHeader( "Connection", "close" );
            response.setCharacterEncoding( "UTF-8" );
        }
        catch ( Exception e )
        {
            Log.error( "OFMeetConfig writeHeader Error", e );
        }
    }

    private String getHTML( String urlToRead )
    {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        StringBuilder result = new StringBuilder();

        try
        {
            url = new URL( urlToRead );
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod( "GET" );
            rd = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            while ( ( line = rd.readLine() ) != null )
            {
                result.append( line );
            }
            rd.close();
        }
        catch ( Exception e )
        {
            Log.error( "getHTML", e );
        }

        return result.toString();
    }

    /**
     * Generates an URL on which client / BOSH connections are expected.
     *
     * This method will verify if the websocket plugin is available. If it is, the websocket endpoint is returned. When
     * websocket is not available, the http-bind endpoint is returned.
     *
     * Since Openfire version 4.2.0 (OF-1339) the websocket functionality that was previously provided by a plugin, was
     * merged with the core Openfire code. After version 4.2.1, there is no need to check for the presence of the
     * plugin.
     *
     * The request that is made to this servlet is used to determine if the client prefers secure/encrypted connections
     * (https, wss) over plain ones (http, ws), and to determine what the server address and port is.
     *
     * @param request the request to this servlet.
     * @return An URI (never null).
     * @throws URISyntaxException When an URI could not be constructed.
     */
    public static URI getMostPreferredConnectionURL( HttpServletRequest request ) throws URISyntaxException
    {
        Log.debug( "[{}] Generating BOSH URL based on {}", request.getRemoteAddr(), request.getRequestURL() );
        final String preferredMechanism = JiveGlobals.getProperty( "ofmeet.connection.mechanism.preferred" );
        final boolean webSocketInCore = !new Version(4, 2, 0, null, -1 ).isNewerThan( XMPPServer.getInstance().getServerInfo().getVersion() );
        if ( !"http-bind".equalsIgnoreCase( preferredMechanism ) && (webSocketInCore || XMPPServer.getInstance().getPluginManager().getPlugin( "websocket" ) != null ) )
        {
            Log.debug( "[{}] Websocket functionality is available. Returning a websocket address.", request.getRemoteAddr() );
            final String websocketScheme;
            if ( request.getScheme().endsWith( "s" ) )
            {
                websocketScheme = "wss";
            }
            else
            {
                websocketScheme = "ws";
            }

            return new URI( websocketScheme, null, request.getServerName(), request.getServerPort(), "/ws/", null, null);
        }
        else
        {
            Log.debug( "[{}] No Websocket functionality available. Returning an HTTP-BIND address.", request.getRemoteAddr() );
            return new URI( request.getScheme(), null, request.getServerName(), request.getServerPort(), "/http-bind/", null, null);
        }
    }
}
