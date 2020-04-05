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

package org.igniterealtime.openfire.plugin.ofmeet.config;

import org.jivesoftware.util.JiveGlobals;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A utility class to store various configuration items for OFMeet. The purpose of this class is to centralize all
 * interaction with Jive properties (and the definition of their default values)
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class OFMeetConfig
{
    public static final String OFMEET_WEBAPP_CONTEXTPATH_PROPERTYNAME = "ofmeet.webapp.contextpath";

    // No static methods! Static methods are not accessible when using this class as a bean in the Admin Console JSP pages.
    public void setWebappContextPath( String contextPath )
    {
        JiveGlobals.setProperty( OFMEET_WEBAPP_CONTEXTPATH_PROPERTYNAME, contextPath );
    }

    public String getWebappContextPath()
    {
        String value = JiveGlobals.getProperty( OFMEET_WEBAPP_CONTEXTPATH_PROPERTYNAME, "/ofmeet" ).trim();

        // Ensure that the value starts with a slash, but does not end with one (unless the root context is used).
        if ( !value.startsWith( "/" ) )
        {
            value = "/" + value;
        }

        while ( value.endsWith( "/" ) && value.length() > 1 )
        {
            value = value.substring( 0, value.length() -1 );
        }

        return value;
    }

    public void resetWebappContextPath()
    {
        JiveGlobals.deleteProperty( OFMEET_WEBAPP_CONTEXTPATH_PROPERTYNAME );
    }

    public void setChannelLastN( int lastN )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.channel.lastn", Integer.toString( lastN ) );
    }

    public int getChannelLastN()
    {
        return JiveGlobals.getIntProperty("org.jitsi.videobridge.ofmeet.channel.lastn", -1 );
    }

    public void resetChannelLastN()
    {
        JiveGlobals.deleteProperty("org.jitsi.videobridge.ofmeet.channel.lastn" );
    }

    public void setAdaptiveLastN( boolean adaptiveChannelLastN )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.adaptive.lastn", Boolean.toString( adaptiveChannelLastN ) );
    }

    public boolean getAdaptiveLastN()
    {
        return JiveGlobals.getBooleanProperty("org.jitsi.videobridge.ofmeet.adaptive.lastn", false );
    }

    public void resetAdaptiveLastN()
    {
        JiveGlobals.deleteProperty("org.jitsi.videobridge.ofmeet.adaptive.lastn" );
    }

    public void setSimulcast( boolean simulcast )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.simulcast", Boolean.toString( simulcast ) );
    }

    public boolean getSimulcast()
    {
        return JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.simulcast", true );
    }

    public void resetSimulcast()
    {
        JiveGlobals.deleteXMLProperty( "org.jitsi.videobridge.ofmeet.simulcast" );
    }

    public void setDisableRtx( boolean disableRtx )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.disableRtx", Boolean.toString( disableRtx ) );
    }

    public boolean getDisableRtx()
    {
        return JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.disableRtx", false );
    }

    public void resetDisableRtx()
    {
        JiveGlobals.deleteXMLProperty( "org.jitsi.videobridge.ofmeet.disableRtx" );
    }

    public void setAdaptiveSimulcast( boolean simulcast )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.adaptive.simulcast", Boolean.toString( simulcast ) );
    }

    public boolean getAdaptiveSimulcast()
    {
        return JiveGlobals.getBooleanProperty("org.jitsi.videobridge.ofmeet.adaptive.simulcast", false );
    }

    public void resetAdaptiveSimulcast()
    {
        JiveGlobals.deleteXMLProperty("org.jitsi.videobridge.ofmeet.adaptive.simulcast" );
    }

    public void setWatermarkLogoUrl( URL url )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.watermark.logo", url == null ? null : url.toExternalForm() );
    }

    public URL getWatermarkLogoUrl()
    {
        final String value = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.watermark.logo" );
        if ( value == null || value.isEmpty() )
        {
            return null;
        }

        try
        {
            return new URL( value );
        }
        catch ( MalformedURLException e )
        {
            return null;
        }
    }

    public void resetWatermarkLogoUrl( URL url )
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.watermark.logo" );
    }

    public void setBrandWatermarkLogoUrl( URL url )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.brand.watermark.logo", url == null ? null : url.toExternalForm() );
    }

    public URL getBrandWatermarkLogoUrl()
    {
        final String value = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.brand.watermark.logo" );
        if ( value == null || value.isEmpty() )
        {
            return null;
        }

        try
        {
            return new URL( value );
        }
        catch ( MalformedURLException e )
        {
            return null;
        }
    }

    public void resetBrandWatermarkLogoUrl( URL url )
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.brand.watermark.logo" );
    }

    public void setButtonsImplemented( List<String> buttons )
    {
        JiveGlobals.setProperty( "ofmeet.buttons.implemented", buttons );
    }

    public List<String> getButtonsImplemented() // BAO
    {
        // These should match the implementations that are provided in the Toolbox.web.js file in jitsi-meet.
        return JiveGlobals.getListProperty( "ofmeet.buttons.implemented", Arrays.asList( "microphone", "camera", "closedcaptions", "desktop", "fullscreen", "fodeviceselection", "hangup", "profile", "info", "chat", "recording", "livestreaming", "etherpad", "sharedvideo", "settings", "raisehand", "videoquality", "filmstrip", "invite", "feedback", "stats", "shortcuts", "tileview", "videobackgroundblur", "download", "help", "mute-everyone" ) );
    }

    public void resetButtonsImplemented()
    {
        JiveGlobals.deleteProperty( "ofmeet.buttons.implemented" );
    }

    public void setButtonsEnabled( List<String> buttons )
    {
        JiveGlobals.setProperty( "ofmeet.buttons.enabled", buttons );
    }

    public List<String> getButtonsEnabled() // BAO
    {
        return JiveGlobals.getListProperty( "ofmeet.buttons.enabled", Arrays.asList( "microphone", "camera", "closedcaptions", "desktop", "fullscreen", "fodeviceselection", "hangup", "profile", "info", "chat", "recording", "livestreaming", "etherpad", "sharedvideo", "settings", "raisehand", "videoquality", "filmstrip", "invite", "feedback", "stats", "shortcuts", "tileview", "videobackgroundblur", "download", "help", "mute-everyone" ) );
    }

    public void resetButtonsEnabled()
    {
        JiveGlobals.deleteProperty( "ofmeet.buttons.enabled" );
    }

    public void setPublicNATAddress( InetAddress address )
    {
        JiveGlobals.setProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_PUBLIC_ADDRESS", address == null ? null : address.getHostAddress() );
    }

    public InetAddress getPublicNATAddress()
    {
        final String address = JiveGlobals.getProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_PUBLIC_ADDRESS" );
        if ( address == null || address.isEmpty() )
        {
            return null;
        }

        try
        {
            return InetAddress.getByName( address );
        }
        catch ( UnknownHostException e )
        {
            return null;
        }
    }

    public void resetPublicNATAddress()
    {
        JiveGlobals.deleteProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_PUBLIC_ADDRESS" );
    }

    public void setLocalNATAddress( InetAddress address )
    {
        JiveGlobals.setProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_PRIVATE_ADDRESS", address == null ? null : address.getHostAddress() );
    }

    public InetAddress getLocalNATAddress()
    {
        final String address = JiveGlobals.getProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_PRIVATE_ADDRESS" );
        if ( address == null || address.isEmpty() )
        {
            return null;
        }

        try
        {
            return InetAddress.getByName( address );
        }
        catch ( UnknownHostException e )
        {
            return null;
        }
    }

    public void resetLocalNATAddress()
    {
        JiveGlobals.deleteProperty( "org.ice4j.ice.harvest.NAT_HARVESTER_PRIVATE_ADDRESS" );
    }

    public void setStunMappingHarversterAddresses( List<String> addresses )
    {
        if (addresses == null || addresses.isEmpty() )
        {
            JiveGlobals.deleteProperty( "org.ice4j.ice.harvest.STUN_MAPPING_HARVESTER_ADDRESSES" );
        }
        else
        {
            JiveGlobals.setProperty( "org.ice4j.ice.harvest.STUN_MAPPING_HARVESTER_ADDRESSES", addresses );
        }
    }

    public List<String> getStunMappingHarversterAddresses()
    {
        return JiveGlobals.getListProperty( "org.ice4j.ice.harvest.STUN_MAPPING_HARVESTER_ADDRESSES", Collections.<String>emptyList() );
    }

    public void resetStunMappingHarversterAddresses()
    {
        JiveGlobals.deleteProperty( "org.ice4j.ice.harvest.STUN_MAPPING_HARVESTER_ADDRESSES" );
    }

    public void setResolution( int resolution )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.resolution", Integer.toString( resolution ) );
    }

    // Resolution is used in older clients (chrome < 61) while 'constraints' are used in newer clients.
    public int getResolution()
    {
        return JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.resolution", 720 );
    }

    public void resetResolution()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.resolution" );
    }

    public void setVideoConstraintsIdealAspectRatio( String ratio )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.constraints.video.aspectratio.ideal", ratio );
    }

    // Resolution is used in older clients (chrome < 61) while 'constraints' are used in newer clients.
    public String getVideoConstraintsIdealAspectRatio()
    {
        return JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.constraints.video.aspectratio.ideal", "16 / 9" );
    }

    public void resetVideoConstraintsIdealAspectRatio()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.constraints.video.aspectratio.ideal" );
    }

    public void setVideoConstraintsIdealHeight( int idealHeight )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.ideal", Integer.toString( idealHeight ) );
    }

    // Resolution is used in older clients (chrome < 61) while 'constraints' are used in newer clients.
    public int getVideoConstraintsIdealHeight()
    {
        return JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.ideal", getResolution() );
    }

    public void resetVideoConstraintsIdealHeight()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.ideal" );
    }

    public void setVideoConstraintsMaxHeight( int maxHeight )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.max", Integer.toString( maxHeight ) );
    }

    // Resolution is used in older clients (chrome < 61) while 'constraints' are used in newer clients.
    public int getVideoConstraintsMaxHeight()
    {
        final int value = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.max", getVideoConstraintsIdealHeight() * 3 );
        return Math.max( value, getVideoConstraintsIdealHeight() ); // don't have a 'max' that is lower than 'ideal'.
    }

    public void resetVideoConstraintsMaxHeight( int maxHeight )
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.max" );
    }

    public void setVideoConstraintsMinHeight( int minHeight )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.min", Integer.toString( minHeight ) );
    }

    // Resolution is used in older clients (chrome < 61) while 'constraints' are used in newer clients.
    public int getVideoConstraintsMinHeight()
    {
        final int value = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.min", getVideoConstraintsIdealHeight() / 3 );
        return Math.min( value, getVideoConstraintsIdealHeight() ); // don't have a 'min' that is lower than 'ideal'.
    }

    public void resetVideoConstraintsMinHeight()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.constraints.video.height.min" );
    }

    public void setLipSync( boolean lipSync )
    {
        JiveGlobals.setProperty( "ofmeet.lipSync.enabled", Boolean.toString( lipSync ) );
    }

    public boolean getLipSync()
    {
        return JiveGlobals.getBooleanProperty( "ofmeet.lipSync.enabled", false ); // defaults to false, known to cause client reconnects in Chrome 58.
    }

    public void resetLipSync()
    {
        JiveGlobals.deleteProperty( "ofmeet.lipSync.enabled" );
    }

    public void setFocusPassword( String password )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.focus.user.password", password );
    }

    public String getFocusPassword()
    {
        return JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.focus.user.password" );
    }

    public void resetFocusPassword()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.focus.user.password" );
    }

    public void setFilmstripOnly( boolean filmstripOnly )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.filmstriponly", Boolean.toString( filmstripOnly ) );
    }

    public boolean getFilmstripOnly()
    {
        return JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.filmstriponly", false );
    }

    public void resetFilmstripOnly()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.filmstriponly" );
    }

    public void setFilmstripMaxHeight( int filmstripMaxHeight )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.film.strip.max.height", Integer.toString( filmstripMaxHeight ) );
    }

    public int getFilmstripMaxHeight()
    {
        return JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.film.strip.max.height", 120 );
    }

    public void resetFilmstripMaxHeight()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.film.strip.max.height" );
    }

    public void setVerticalFilmstrip( boolean verticalFilmstrip )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.vertical.filmstrip", Boolean.toString( verticalFilmstrip ) );
    }

    public boolean getVerticalFilmstrip()
    {
        return JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.vertical.filmstrip", true );
    }

    public void resetVerticalFilmstrip()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.vertical.filmstrip" );
    }

    public void setStartAudioOnly( boolean startAudioOnly )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.startaudioonly", Boolean.toString( startAudioOnly ) );
    }

    public boolean getStartAudioOnly()
    {
        return JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.startaudioonly", false );
    }

    public void resetStartAudioOnly()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.startaudioonly" );
    }

    public void setStartAudioMuted( Integer startAudioMuted )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.startaudiomuted", Integer.toString( startAudioMuted == null ? Integer.MAX_VALUE : startAudioMuted ) );
    }

    public Integer getStartAudioMuted()
    {
        // In storage, 'no limit' is represented by Integer.MAX_VALUE. 'Default' is represented by a null value.
        final int startAudioMuted = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.startaudiomuted", 9 );
        return startAudioMuted == Integer.MAX_VALUE ? null : startAudioMuted;
    }

    public void resetStartAudioMuted()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.startaudiomuted" );
    }

    public void setStartVideoMuted( Integer startVideoMuted )
    {
        JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.startvideomuted", Integer.toString( startVideoMuted == null ? Integer.MAX_VALUE : startVideoMuted ) );
    }

    public Integer getStartVideoMuted()
    {
        // In storage, 'no limit' is represented by Integer.MAX_VALUE. 'Default' is represented by a null value.
        final int startVideoMuted = JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.startvideomuted", 9 );
        return startVideoMuted == Integer.MAX_VALUE ? null : startVideoMuted;
    }

    public void resetStartVideoMuted()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.startvideomuted" );
    }

    public void setInviteOptions( List<String> options )
    {
        if (options == null || options.isEmpty() )
        {
            JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.inviteOptions" );
        }
        else
        {
            JiveGlobals.setProperty( "org.jitsi.videobridge.ofmeet.inviteOptions", options);
        }
    }

    public List<String> getInviteOptions()
    {
        return JiveGlobals.getListProperty( "org.jitsi.videobridge.ofmeet.inviteOptions", Arrays.asList( "invite"  ) ); // "invite", "dialout", "addtocall"
    }

    public void resetInviteOptions()
    {
        JiveGlobals.deleteProperty( "org.jitsi.videobridge.ofmeet.inviteOptions" );
    }

    public void setP2pEnabled( boolean p2pEnabled )
    {
        JiveGlobals.setProperty( "ofmeet.p2p.enabled", Boolean.toString( p2pEnabled ) );
    }

    public boolean getP2pEnabled()
    {
        return JiveGlobals.getBooleanProperty( "ofmeet.p2p.enabled", true );
    }

    public void resetP2pEnabled()
    {
        JiveGlobals.deleteProperty( "ofmeet.p2p.enabled" );
    }

    public void setP2pPreferH264( boolean p2pPreferH264 )
    {
        JiveGlobals.setProperty( "ofmeet.p2p.preferH264", Boolean.toString( p2pPreferH264 ) );
    }

    public boolean getP2pPreferH264()
    {
        return JiveGlobals.getBooleanProperty( "ofmeet.p2p.preferH264", true );
    }

    public void resetP2pPreferH264()
    {
        JiveGlobals.deleteProperty( "ofmeet.p2p.preferH264" );
    }

    public void setP2pDisableH264( boolean p2pDisableH264 )
    {
        JiveGlobals.setProperty( "ofmeet.p2p.disableH264", Boolean.toString( p2pDisableH264 ) );
    }

    public boolean getP2pDisableH264()
    {
        return JiveGlobals.getBooleanProperty( "ofmeet.p2p.disableH264", true );
    }

    public void resetP2pDisableH264()
    {
        JiveGlobals.deleteProperty( "ofmeet.p2p.disableH264" );
    }

    public void setP2pUseStunTurn( boolean p2pUseStunTurn )
    {
        JiveGlobals.setProperty( "ofmeet.p2p.useStunTurn", Boolean.toString( p2pUseStunTurn ) );
    }

    public boolean getP2pUseStunTurn()
    {
        return JiveGlobals.getBooleanProperty( "ofmeet.p2p.useStunTurn", false );
    }

    public void resetP2pUseStunTurn()
    {
        JiveGlobals.deleteProperty( "ofmeet.p2p.useStunTurn" );
    }

    public void setWebrtcIceUdpDisable( boolean webrtcIceUdpDisable )
    {
        JiveGlobals.setProperty( "ofmeet.webrtcIceUdpDisable", Boolean.toString( webrtcIceUdpDisable ) );
    }

    public boolean getWebrtcIceUdpDisable()
    {
        return JiveGlobals.getBooleanProperty("ofmeet.webrtcIceUdpDisable", false );
    }

    public void resetWebrtcIceUdpDisable()
    {
        JiveGlobals.deleteProperty( "ofmeet.webrtcIceUdpDisable" );
    }

    public void setWebrtcIceTcpDisable( boolean webrtcIceTcpDisable )
    {
        JiveGlobals.setProperty( "ofmeet.webrtcIceTcpDisable", Boolean.toString( webrtcIceTcpDisable ) );
    }

    public boolean getWebrtcIceTcpDisable()
    {
        return JiveGlobals.getBooleanProperty("ofmeet.webrtcIceTcpDisable", false );
    }

    public void resetWebrtcIceTcpDisable()
    {
        JiveGlobals.deleteProperty( "ofmeet.webrtcIceTcpDisable" );
    }

    public final StringProperty jigasiSipUserId = new StringProperty( "ofmeet.jigasi.sip.user-id", null );
    public StringProperty getJigasiSipUserId() { return jigasiSipUserId; }

    public final StringProperty jigasiSipPassword = new StringProperty( "ofmeet.jigasi.sip.password", "" );
    public StringProperty getJigasiSipPassword() { return jigasiSipPassword; }

    public final StringProperty jigasiSipServerAddress = new StringProperty( "ofmeet.jigasi.sip.server-address", null );
    public StringProperty getJigasiSipServerAddress() { return jigasiSipServerAddress; }

    public final StringProperty jigasiSipDomainBase = new StringProperty( "ofmeet.jigasi.sip.domain-base", null );
    public StringProperty getJigasiSipDomainBase() { return jigasiSipDomainBase; }

    public final StringProperty jigasiXmppUserId = new StringProperty( "ofmeet.jigasi.xmpp.user-id", "jigasi" );
    public StringProperty getJigasiXmppUserId() { return jigasiXmppUserId; }

    public final StringProperty jigasiXmppPassword = new StringProperty( "ofmeet.jigasi.xmpp.password", null );
    public StringProperty getJigasiXmppPassword() { return jigasiXmppPassword; }
}
