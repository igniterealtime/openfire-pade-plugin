package org.ifsoft.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;

import net.lingala.zip4j.core.ZipFile;

import org.jivesoftware.util.*;
import org.jivesoftware.openfire.*;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.plugin.rest.*;

import org.jivesoftware.openfire.plugin.spark.Bookmark;
import org.jivesoftware.openfire.plugin.spark.Bookmarks;
import org.jivesoftware.openfire.plugin.spark.BookmarkManager;

public class Servlet extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger( Servlet.class );

    @Override
    protected void doPut( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        String authorization = request.getHeader("authorization");

        if (authorization == null)
        {
            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            return;
        }

        String[] usernameAndPassword = BasicAuth.decode(authorization);

        if (usernameAndPassword == null || usernameAndPassword.length != 2) {
            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            return;
        }

        try {
            AuthFactory.authenticate(usernameAndPassword[0], usernameAndPassword[1]);
        } catch (Exception e) {
            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
            return;
        }

        String name = request.getParameter("name");
        String username = request.getParameter("username");

        Log.debug( "Processing PUT request... ({} submitting to {})", request.getRemoteAddr(), request.getRequestURI() );
        response.setHeader( "Cache-Control", "max-age=31536000" );

        if (name.endsWith(".webm"))
        {
            String dir = JiveGlobals.getHomeDirectory() + File.separator + "resources" + File.separator + "spank" + File.separator + "ofmeet-cdn" + File.separator + "recordings";
            Path path = Paths.get( dir, name);

            try {
                writeFile(path, request);

            } catch (Exception e) {
               Log.error("upload webm servlet", e);
               response.setStatus( HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else

        if (name.endsWith(".zip") || name.endsWith(".h5p"))
        {
            Path path = Paths.get( "/tmp", "upload." + System.currentTimeMillis() + "." + name);

            try {
                writeFile(path, request);

                String source = path.toString();
                String folder = name.substring(0, name.lastIndexOf("."));
                String destination = JiveGlobals.getHomeDirectory() + File.separator + "resources" + File.separator + "spank" + File.separator + username + File.separator + folder;

                Log.debug( "Extracting application..." + source + " " + destination);

                ZipFile zipFile = new ZipFile(path.toFile());
                zipFile.extractAll(destination);

                Files.deleteIfExists(path);

                String bookmarkValue = JiveGlobals.getProperty("ofmeet.root.url.secure", "https://" + XMPPServer.getInstance().getServerInfo().getHostname() + ":" + JiveGlobals.getProperty("httpbind.port.secure", "7443")) + "/" + username + "/" + folder;

                if (name.endsWith(".h5p"))
                {
                    bookmarkValue = JiveGlobals.getProperty("ofmeet.root.url.secure", "https://" + XMPPServer.getInstance().getServerInfo().getHostname() + ":" + JiveGlobals.getProperty("httpbind.port.secure", "7443")) + "/apps/h5p/?path=" + username + "/" + folder;
                }

                long id = -1;

                for (Bookmark bookmark : BookmarkManager.getBookmarks())
                {
                    if (bookmark.getValue().equals(bookmarkValue)) id = bookmark.getBookmarkID();
                }

                if (id == -1)
                {
                    new Bookmark(Bookmark.Type.url, folder, bookmarkValue, new ArrayList<String>(Arrays.asList(new String[] {username})), null);
                }

            } catch (Exception e) {
               Log.error("upload application zip servlet", e);
               response.setStatus( HttpServletResponse.SC_BAD_REQUEST);
            }

        } else {
            Log.warn("Chat API upload. " + name + " is not a valid file from " + username);
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST);
        }

        response.setHeader( "Location", request.getRequestURL().toString() );
        response.setStatus( HttpServletResponse.SC_CREATED );
    }

    private void writeFile(Path path, HttpServletRequest request) throws Exception
    {
        final OutputStream outStream = new BufferedOutputStream(Files.newOutputStream(path, java.nio.file.StandardOpenOption.CREATE ));
        final InputStream in = request.getInputStream();

        final byte[] buffer = new byte[ 1024 * 4 ];
        int bytesRead;

        while ( ( bytesRead = in.read( buffer ) ) != -1 )
        {
            outStream.write( buffer, 0, bytesRead );
        }

        outStream.close();
    }
}