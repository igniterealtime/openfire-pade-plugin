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
package org.jivesoftware.openfire.plugin.ofmeet;

import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jivesoftware.util.JiveGlobals;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * A servlet that generates a snippet of javascript (json) that is the 'interfaceConfig' variable, as used by the Jitsi
 * Meet webapplication.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class InterfaceConfigServlet extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger( InterfaceConfigServlet.class );

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            Log.trace( "[{}] interface_config requested.", request.getRemoteAddr() );

            writeHeader( response );

            final OFMeetConfig ofMeetConfig = new OFMeetConfig();
            final JSONObject config = new JSONObject();

            config.put( "TOOLBAR_BUTTONS",                       new JSONArray( ofMeetConfig.getButtonsEnabled() ) );
            config.put( "INITIAL_TOOLBAR_TIMEOUT",               JiveGlobals.getIntProperty(     "org.jitsi.videobridge.ofmeet.initial.toolbar.timeout",       20000               ) );
            config.put( "TOOLBAR_TIMEOUT",                       JiveGlobals.getIntProperty(     "org.jitsi.videobridge.ofmeet.toolbar.timeout",               4000                ) );

            config.put( "SETTINGS_SECTIONS",                     new JSONArray( JiveGlobals.getListProperty( "org.jitsi.videobridge.ofmeet.settings.sections",    Arrays.asList( "language", "devices", "moderator", "profile", "calendar") ) ) );

            config.put( "DEFAULT_LOGO_URL",                      JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.watermark.logo",                  ""                 ) );
            config.put( "DEFAULT_WELCOME_PAGE_LOGO_URL",         JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.watermark.logo",                  ""                 ) );

            config.put( "CANVAS_EXTRA",                          JiveGlobals.getIntProperty(     "org.jitsi.videobridge.ofmeet.canvas.extra",                  104                 ) );
            config.put( "CANVAS_RADIUS",                         JiveGlobals.getIntProperty(     "org.jitsi.videobridge.ofmeet.canvas.radius",                 0                   ) );
            config.put( "SHADOW_COLOR",                          JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.shadow.color",                  "#ffffff"           ) );
            config.put( "DEFAULT_BACKGROUND",                    JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.default.background",            "#474747"           ) );
            config.put( "DEFAULT_REMOTE_DISPLAY_NAME",           JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.default.remote.displayname",    "Change Me"         ) );
            config.put( "DEFAULT_DOMINANT_SPEAKER_DISPLAY_NAME", JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.default.speaker.displayname",   "Speaker"           ) );
            config.put( "DEFAULT_LOCAL_DISPLAY_NAME",            JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.default.local.displayname",     "Me"                ) );
            config.put( "SHOW_JITSI_WATERMARK",                  JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.show.watermark",                false               ) );
            config.put( "JITSI_WATERMARK_LINK",                  JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.watermark.link",                ""                  ) );
            config.put( "SHOW_BRAND_WATERMARK",                  JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.brand.show.watermark",          false               ) );
            config.put( "BRAND_WATERMARK_LINK",                  JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.brand.watermark.link",          ""                  ) );
            config.put( "RECENT_LIST_ENABLED",                   JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.recent.list.enabled",           true                ) );
            config.put( "SHOW_POWERED_BY",                       JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.show.poweredby",                false               ) );
            config.put( "SHOW_PROMOTIONAL_CLOSE_PAGE",           JiveGlobals.getBooleanProperty( "ofmeet.feedback.enabled",                                    true               ) );
            config.put( "GENERATE_ROOMNAMES_ON_WELCOME_PAGE",    JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.random.roomnames",              true                ) );
            config.put( "DISPLAY_WELCOME_PAGE_CONTENT",          JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.welcomepage.content",           true                ) );
            config.put( "DISPLAY_WELCOME_PAGE_TOOLBAR_ADDITIONAL_CONTENT",JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.welcomepage.toolbarcontent",          false) );
            config.put( "APP_NAME",                              JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.application.name",              "Pade Meetings"     ) );
            config.put( "LANG_DETECTION",                        JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.languagedetection",      false               ) );
            config.put( "INVITATION_POWERED_BY",                 JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.invitation.poweredby",          true                ) );
            config.put( "VIDEO_LAYOUT_FIT",                      JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.video.layout.fit",              "both"              ) );
            config.put( "TILE_VIEW_MAX_COLUMNS",                 JiveGlobals.getIntProperty(     "org.jitsi.videobridge.ofmeet.tileview.columns.max",          5                   ) );
            config.put( "SHOW_CONTACTLIST_AVATARS",              JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.show.contactlist.avatars",      false               ) );
            config.put( "RANDOM_AVATAR_URL_PREFIX",              JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.random.avatar.url.prefix",      ""                  ) );
            config.put( "RANDOM_AVATAR_URL_SUFFIX",              JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.random.avatar.url.suffix",      ""                  ) );
            config.put( "ENABLE_FEEDBACK_ANIMATION",             JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.feedback_animation",     false               ) );
            config.put( "DISABLE_FOCUS_INDICATOR",               JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.disable.focus.indicator",       false               ) );
            config.put( "DISABLE_VIDEO_BACKGROUND",              JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.disable.video_background",      false               ) );
            config.put( "ACTIVE_SPEAKER_AVATAR_SIZE",            JiveGlobals.getIntProperty(     "org.jitsi.videobridge.ofmeet.active.speaker.avatarsize",     100                 ) );

            config.put( "OFMEET_ENABLE_MOUSE_SHARING",           JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.mouse.sharing",          true                ) );
            config.put( "OFMEET_RECORD_CONFERENCE",              JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.conference.recording",          true                ) );
            config.put( "OFMEET_TAG_CONFERENCE",                 JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.conference.tags",               true                ) );
            config.put( "OFMEET_ENABLE_BREAKOUT",                JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.breakout",               false               ) );
            config.put( "OFMEET_ENABLE_CRYPTPAD",                JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.cryptpad",               true                ) );
            config.put( "OFMEET_ENABLE_WHITEBOARD",              JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.whiteboard",             true                ) );
            config.put( "OFMEET_ENABLE_CONFETTI",                JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.confetti",               true                ) );
            config.put( "OFMEET_CACHE_PASSWORD",                 JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.cache.password",                true                ) );
            config.put( "OFMEET_ALLOW_UPLOADS",                  JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.allow.uploads",                 true                ) );
            config.put( "OFMEET_SHOW_CAPTIONS",                  JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.show.captions",                 false               ) );
            config.put( "OFMEET_ENABLE_TRANSCRIPTION",           JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.enable.transcription",          false               ) );
            config.put( "OFMEET_CONTACTS_MGR",                   JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.contacts.manager",              false               ) );
            config.put( "OFMEET_WELCOME_PAGE_TITLE",             JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.welcomepage.title",             "Pade Meetings"     ) );
            config.put( "OFMEET_WELCOME_PAGE_DESCRIPTION",       JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.welcomepage.description",       ""                  ) );
            config.put( "OFMEET_WELCOME_PAGE_CONTENT",           JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.welcome.content",               ""                  ) );
            config.put( "OFMEET_WELCOME_PAGE_TOOLBARCONTENT",    JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.welcome.toolbarcontent",        ""                  ) );
            config.put( "OFMEET_CRYPTPAD_URL",                   JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.cryptpad.url", "https://cryptpad.fr"                ) );
            config.put( "OFMEET_WHITEBOARD_URL",                 JiveGlobals.getProperty(        "org.jitsi.videobridge.ofmeet.whiteboard.url", "https://wbo.ophir.dev/boards/"    ) );

            config.put( "RECENT_LIST_ENABLED",                   JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.welcomepage.recentlist",        true                ) );
            config.put( "IN_PROGRESS_LIST_ENABLED",              JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist",    false               ) );
            config.put( "IN_PROGRESS_LIST_INTERVAL",             JiveGlobals.getIntProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist.interval", 10                ) );

            config.put( "filmStripOnly",                         ofMeetConfig.getFilmstripOnly()      );
            config.put( "VERTICAL_FILMSTRIP",                    ofMeetConfig.getVerticalFilmstrip()  );
            config.put( "FILM_STRIP_MAX_HEIGHT",                 ofMeetConfig.getFilmstripMaxHeight() );
            config.put( "INVITE_OPTIONS",                        new JSONArray( ofMeetConfig.getInviteOptions() ) );
            config.put( "ENFORCE_NOTIFICATION_AUTO_DISMISS_TIMEOUT", "15000" );


            // Jitsi-meet appears to have replaced LOCAL_THUMBNAIL_RATIO_WIDTH and LOCAL_THUMBNAIL_RATIO_HEIGHT with a combined value in LOCAL_THUMBNAIL_RATIO.
            final int localThumbnailRatioWidth  = JiveGlobals.getIntProperty("org.jitsi.videobridge.ofmeet.local.thumbnail.ratio.width",16 );
            final int localThumbnailRatioHeight = JiveGlobals.getIntProperty("org.jitsi.videobridge.ofmeet.local.thumbnail.ratio.height",9 );
            config.put( "LOCAL_THUMBNAIL_RATIO", (double) localThumbnailRatioWidth / localThumbnailRatioHeight);

            // Same for REMOTE_THUMBNAIL_RATIO, but for some reason, the resulting value is a single digit.
            final int remoteThumbnailRatioWidth  = JiveGlobals.getIntProperty("org.jitsi.videobridge.ofmeet.remote.thumbnail.ratio.width",1 );
            final int remoteThumbnailRatioHeight = JiveGlobals.getIntProperty("org.jitsi.videobridge.ofmeet.remote.thumbnail.ratio.height",1 );
            config.put( "REMOTE_THUMBNAIL_RATIO", (double) remoteThumbnailRatioWidth / remoteThumbnailRatioHeight );

            // Add response headers that instruct not to cache this data.
            response.setHeader( "Expires",       "Sat, 6 May 1995 12:00:00 GMT" );
            response.setHeader( "Cache-Control", "no-store, no-cache, must-revalidate" );
            response.addHeader( "Cache-Control", "post-check=0, pre-check=0" );
            response.setHeader( "Pragma",        "no-cache" );
            response.setHeader( "Content-Type",  "application/javascript" );
            response.setHeader( "Connection",    "close" );

            // Write out the JSON object.
            response.getOutputStream().println( "var interfaceConfig = " + config.toString( 2 ) + ";" );
        }
        catch ( Exception e )
        {
            Log.error( "[{}] Failed to generate interfaceconfig!", request.getRemoteAddr(), e );
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
}
