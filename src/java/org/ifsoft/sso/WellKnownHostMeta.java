package org.ifsoft.sso;

import org.jivesoftware.util.*;
import org.jivesoftware.openfire.*;
import org.jivesoftware.openfire.group.*;
import org.jivesoftware.openfire.user.*;
import org.jivesoftware.openfire.http.HttpBindManager;

import org.slf4j.*;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;


public class WellKnownHostMeta extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger(WellKnownHostMeta.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            writeHeader(response);
            final String server = XMPPServer.getInstance().getServerInfo().getHostname() + ":" + JiveGlobals.getProperty("pade.publicport.secure", JiveGlobals.getProperty("httpbind.port.secure", "7443"));
            response.getOutputStream().println("<XRD xmlns=\"http://docs.oasis-open.org/ns/xri/xrd-1.0\">");
            response.getOutputStream().println("  <Link rel=\"urn:xmpp:alt-connections:xbosh\" href=\"https://" + server + "/http-bind/\"/>");
            response.getOutputStream().println("  <Link rel=\"urn:xmpp:alt-connections:websocket\" href=\"wss://" + server + "/ws/\"/>");
            response.getOutputStream().println("</XRD>");
        }
        catch(Exception e5) {
            Log.error("WellKnownHostMeta Servlet Error", e5);
        }
    }

    private void writeHeader(HttpServletResponse response)
    {
        response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Type", "application/javascript");
        response.setHeader("Connection", "close");

		HttpBindManager boshManager = HttpBindManager.getInstance();
		response.setHeader("Access-Control-Allow-Methods", String.join(",", HttpBindManager.HTTP_BIND_CORS_ALLOW_METHODS.getValue()));
		response.setHeader("Access-Control-Allow-Headers", String.join(",", HttpBindManager.HTTP_BIND_CORS_ALLOW_HEADERS.getValue() + ", Authorization"));
		response.setHeader("Access-Control-Max-Age", String.valueOf(HttpBindManager.HTTP_BIND_CORS_MAX_AGE.getValue().toSeconds()));
		response.setHeader("Access-Control-Allow-Origin", String.valueOf(HttpBindManager.HTTP_BIND_ALLOWED_ORIGINS.getDefaultValue()));
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
