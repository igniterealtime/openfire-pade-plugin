package org.ifsoft.websockets;

import org.jivesoftware.util.JiveGlobals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.*;
import java.util.*;
import java.text.*;
import java.net.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import org.jivesoftware.util.ParamUtils;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.security.SecurityAuditManager;
import de.mxro.process.*;
import org.jitsi.util.OSUtils;
import net.sf.json.JSONObject;
import org.jivesoftware.openfire.plugin.ofmeet.OfMeetPlugin;
import org.ifsoft.oju.openfire.MUCRoomProperties;


@WebSocket public class LiveStreamSocket implements ProcessListener
{
    private static Logger Log = LoggerFactory.getLogger( "LiveStreamSocket" );
    private Session wsSession;
	private JSONObject metadata = null;
	private String streamKey = null;	
    private XProcess ffmpegThread = null;	
	private final SecurityAuditManager securityAuditManager = SecurityAuditManager.getInstance();

	public LiveStreamSocket(String streamKey)
	{
		this.streamKey = streamKey;		
	}
		
    public boolean isOpen() {
        return wsSession.isOpen();
    }

    @OnWebSocketConnect public void onConnect(Session wsSession)
    {
        this.wsSession = wsSession;
        Log.debug("onConnect");			
    }

    @OnWebSocketClose public void onClose(int statusCode, String reason)
    {
        try {
			if (ffmpegThread != null) ffmpegThread.destory();	
			if (metadata != null) MUCRoomProperties.remove("conference", metadata.getString("room"), "ofmeet.livestream.metadata");			

        } catch ( Exception e ) {
            Log.error( "An error occurred while attempting to remove the socket", e );
        }

        Log.debug(" : onClose : " + statusCode + " " + reason);
    }

    @OnWebSocketError public void onError(Throwable error)
    {
        Log.error("LiveStreamSocket onError", error);
    }

    @OnWebSocketMessage public void onTextMethod(String data)
    {
		Log.debug("onTextMethod \n" + data);
				
        final String liveStreamUrl = JiveGlobals.getProperty( "ofmeet.live.stream.url", "rtmp://a.rtmp.youtube.com/live2");		
		final String path = OfMeetPlugin.webRoot + File.separator +  "ffmpeg";		

        try {
			metadata = new JSONObject(data);
			
			String ffmpegName = null;
			if (OSUtils.IS_LINUX64) 	ffmpegName = "ffmpeg";
			if (OSUtils.IS_WINDOWS64) 	ffmpegName = "ffmpeg.exe";									
					
			final String ffmpeg = path + File.separator + ffmpegName;	
			final String url = liveStreamUrl + "/" + metadata.getString("key");
			final String cmdLine = ffmpeg + " -i - -vcodec copy -f flv " + url;
			ffmpegThread = Spawn.startProcess(cmdLine, new File(path), this);
            
			securityAuditManager.logEvent("pade", "meeting - " + metadata.getString("room") + " live stream by " + metadata.getString("user"), url);
			MUCRoomProperties.put("conference", metadata.getString("room"), "ofmeet.livestream.metadata", metadata.toString());
		
			Log.info( "ffmpeg staring with "  + cmdLine);	

        } catch ( Exception e ) {
            Log.error( "An error occurred while starting ffmpeg", e );
			disconnect();
        }		
    }

    @OnWebSocketMessage public void onBinaryMethod(byte data[], int offset, int length)
    {
		ffmpegThread.sendByte(data, offset, length);
    }

    public void disconnect()
    {
        Log.debug("disconnect : LiveStreamSocket disconnect");

        try {
            if (wsSession != null && wsSession.isOpen())
            {
                wsSession.close();
            }
        } catch ( Exception e ) {

            try {
                wsSession.disconnect();
            } catch ( Exception e1 ) {

            }
        }
    }
	
	
    public void onOutputLine(final String line)
    {
        Log.debug("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.debug("onProcessQuit " + code);
        System.setProperty("ofmeet.ffmpeg.started", "false");
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.debug(line);
    }	
}