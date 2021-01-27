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
import org.jivesoftware.util.JiveGlobals;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
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
 * A servlet that generates the branding json that customises pade chat client
 *
 */
public class BrandingServlet extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger( BrandingServlet.class );

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            Log.debug( "[{}] branding json requested.", request.getRemoteAddr() );

            response.setCharacterEncoding( "UTF-8" );
            response.setContentType( "UTF-8" );

            List<String> properties = JiveGlobals.getPropertyNames();
            JSONObject jsonObject = new JSONObject();

            for (String propertyName : properties)
            {
                if (propertyName.indexOf("pade.branding.") == 0) {
                    String propertyValue = JiveGlobals.getProperty(propertyName);
                    Log.debug("BrandingServlet - Found " + propertyName + " " + propertyValue);

                    jsonObject.put(propertyName.substring(14), new JSONObject(propertyValue));
                }
            }

            // Add response headers that instruct not to cache this data.
            response.setHeader( "Expires",       "Sat, 6 May 1995 12:00:00 GMT" );
            response.setHeader( "Cache-Control", "no-store, no-cache, must-revalidate" );
            response.addHeader( "Cache-Control", "post-check=0, pre-check=0" );
            response.setHeader( "Pragma",        "no-cache" );
            response.setHeader( "Content-Type", "application/javascript" );
            response.setHeader( "Connection",    "close" );

            // Write out the JSON object.
            response.getOutputStream().println("var branding = " + jsonObject.toString( 2 ) + ";" );

        }
        catch ( Exception e )
        {
            Log.error( "[{}] Failed to generate branding.js!", request.getRemoteAddr(), e );
        }
    }
}
