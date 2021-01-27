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

            String service = "conference"; //mainMuc.split(".")[0];
            List<MUCRoom> rooms = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(service).getChatRooms();

            String url = (String) request.getHeader("referer");
            if (url == null) url = request.getRequestURL().toString();
            Log.debug("ofmeet base url: {}", url);
            URL requestUrl = new URL(url);

            final JSONArray meetings = new JSONArray();

            final String[] excludeKeywords = JiveGlobals.getProperty( "org.jitsi.videobridge.ofmeet.welcomepage.inprogresslist.exclude", "").split(":");

            for (MUCRoom chatRoom : rooms)
            {
                final JSONObject meeting = new JSONObject();

                final JSONArray members = new JSONArray();
                String focus = null;
                String roomName = chatRoom.getJID().getNode();
                String roomDecodedName = URLDecoder.decode(roomName, "UTF-8");
                boolean bExclusion = false;

                if ( roomName.equals("ofmeet") || roomName.equals("ofgasi") || !chatRoom.isPublicRoom() )
                {
                    continue;
                }

                for ( String keyword : excludeKeywords )
                {
                    if ( !keyword.isEmpty() && roomDecodedName.toLowerCase().contains(keyword.toLowerCase()) )
                    {
                        bExclusion = true;
                        break;
                    }
                }

                if ( bExclusion )
                {
                    continue;
                }

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
                        final JSONObject member = new JSONObject();

                        member.put("id", occupant.getUserAddress().toBareJID());
                        members.put(member);
                    }
                }

                if (focus == null)
                {
                    continue;
                }

                meeting.put( "name", roomDecodedName);
                meeting.put( "url", new URL(requestUrl, "./" + roomName).toString());
                meeting.put( "date", chatRoom.getCreationDate().getTime());
                meeting.put( "members", members);
                meeting.put( "password", StringUtils.isEmpty(chatRoom.getPassword())? "false" : "true" );

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
