/*
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

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.util.JiveGlobals;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.net.URL;
import java.net.URLDecoder;

/**
 * A servlet that generates a snippet of json that is the 'conferences' variable, as used by the Jitsi
 * Meet webapplication.
 *
 * @author Cool0707, cool0707@gmail.com
 */
public class InProgressListServlet extends HttpServlet
{
    /**
     *
     */
    private static final long serialVersionUID = -9012313048172452140L;
    private static final Logger Log = LoggerFactory.getLogger( InProgressListServlet.class );

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            Log.trace( "[{}] conferences requested.", request.getRemoteAddr() );

            response.setCharacterEncoding( "UTF-8" );
            response.setContentType( "UTF-8" );

            final String url = (String) request.getHeader("referer");
            // if (url == null) url = request.getRequestURL().toString();
            if ((url == null) || (url.trim().isEmpty()))
            {
              response.sendError(HttpServletResponse.SC_NOT_FOUND);
              return;
            }
            Log.debug("ofmeet base url: {}", url);

            final String service = "conference"; //mainMuc.split(".")[0];
            final List<MUCRoom> chatRooms = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(service).getActiveChatRooms();
            final JSONArray meetings = new JSONArray();
            final String[] excludedRoomNames = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist.exclude", "").split(":");
            final String[] excludedNickNames = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist.excludeNicks", "").split(":");
            final Boolean isSizeInfoEnabled = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist.enableSizeInfo", false);
            final Boolean isParticipantsInfoEnabled = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist.enableParticipantsInfo", false);
            final Boolean isProtectionInfoEnabled = JiveGlobals.getBooleanProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist.enableProtectionInfo", false);
            final URL requestUrl = new URL(url);
            final Date Now = new Date();
            for (MUCRoom chatRoom : chatRooms)
            {
                final String roomName = chatRoom.getJID().getNode();
                final String roomDecodedName = URLDecoder.decode(roomName, "UTF-8");

                if ( roomName.equals("ofmeet") || roomName.equals("ofgasi") || !chatRoom.isPublicRoom() )
                {
                    continue;
                }

                boolean foundExclusion = false;
                for ( String excludedRoomName : excludedRoomNames )
                {
                    if ( !excludedRoomName.isEmpty() && roomDecodedName.toLowerCase().contains(excludedRoomName.toLowerCase()) )
                    {
                        foundExclusion = true;
                        break;
                    }
                }
                if ( foundExclusion )
                {
                    continue;
                }


                final JSONArray members = new JSONArray();
                List<String> nicks = new ArrayList<String>();
                String focus = null;
                for ( final MUCRole occupant : chatRoom.getOccupants() )
                {
                    JID jid = occupant.getUserAddress();
                    String nick = jid.getNode();

                    if (nick.equals("focus"))
                    {
                        focus = jid.getResource();
                    }
                    else
                    {
                        foundExclusion = false;
                        for ( String excludedNickName : excludedNickNames )
                        {
                            if ( !nick.isEmpty() && nick.equals(excludedNickName) )
                            {
                                foundExclusion = true;
                                break;
                            }
                        }
                        if ( foundExclusion )
                        {
                            continue;
                        }

                        final String bareJID = occupant.getUserAddress().toBareJID();
                        final JSONObject member = new JSONObject();
                        member.put("id", bareJID);
                        members.put(member);
                        nicks.add(nick);
                    }
                }
                if (focus == null)
                {
                    continue;
                }


                final JSONObject meeting = new JSONObject();
                final int size = members.length();
                final long duration = Now.getTime() - chatRoom.getCreationDate().getTime();
                final boolean hasPassword = ! StringUtils.isEmpty(chatRoom.getPassword());
                final String title = roomDecodedName
                                   + ( isProtectionInfoEnabled && hasPassword ? " \uD83D\uDD12" :  "")
                                   + ( isSizeInfoEnabled && size > 0 ? " (" + Integer.toString(size) + ")" : "")
                                   + ( isParticipantsInfoEnabled ? " " + nicks.toString() : "");

                meeting.put( "room", roomDecodedName);
                meeting.put( "url", new URL(requestUrl, "./" + roomName).toString());
                meeting.put( "date", chatRoom.getCreationDate().getTime());
                meeting.put( "duration", (duration/60000)*60000); // round down to minutes
                meeting.put( "name", title); // TODO: might be refactored to title in app.bundle.min.js

                if ( isSizeInfoEnabled ) meeting.put( "size", size);
                if ( isProtectionInfoEnabled) meeting.put( "password", Boolean.toString(hasPassword));
                if ( isParticipantsInfoEnabled) meeting.put( "members", members);

                meetings.put(meeting);
            }

            // Add response headers that instruct not to cache this data.
            response.setHeader( "Expires",       "Sat, 6 May 1995 12:00:00 GMT" );
            response.setHeader( "Cache-Control", "no-store, no-cache, must-revalidate" );
            response.addHeader( "Cache-Control", "post-check=0, pre-check=0" );
            response.setHeader( "Pragma",        "no-cache" );
            response.setHeader( "Content-Type",  "application/json" );
            response.setHeader( "Connection",    "close" );

            // Write out the JSON object.
            response.getOutputStream().println(meetings.toString( 2 ));
        }
        catch ( Exception e )
        {
            Log.error( "[{}] Failed to generate meeting list!", request.getRemoteAddr(), e );
        }
    }
}
