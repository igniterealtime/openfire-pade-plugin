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


@WebSocket public class ProxyWebSocket
{
    private static Logger Log = LoggerFactory.getLogger( "ProxyWebSocket" );
    private Session wsSession;
    private ProxyConnection proxyConnection;

    public void setProxyConnection(ProxyConnection proxyConnection) {
        this.proxyConnection = proxyConnection;
        proxyConnection.setSocket(this);
        Log.debug("setProxyConnection");
    }

    public boolean isOpen() {
        return wsSession.isOpen();
    }

    @OnWebSocketConnect public void onConnect(Session wsSession)
    {
        this.wsSession = wsSession;
        //proxyConnection.setSecure(wsSession.isSecure());
        Log.debug("onConnect");
    }

    @OnWebSocketClose public void onClose(int statusCode, String reason)
    {
        try {
            proxyConnection.disconnect();

        } catch ( Exception e ) {
            Log.error( "An error occurred while attempting to remove the socket", e );
        }

        Log.debug(" : onClose : " + statusCode + " " + reason);
    }

    @OnWebSocketError public void onError(Throwable error)
    {
        Log.error("ProxyWebSocket onError", error);
    }

    @OnWebSocketMessage public void onTextMethod(String data)
    {
        if ( !"".equals( data.trim()))
        {
            try {
                Log.debug(" : onMessage : Received : \n" + data );
                proxyConnection.deliver(data);

            } catch ( Exception e ) {
                Log.error( "An error occurred while attempting to route the packet : ", e );
            }
        }
    }

    @OnWebSocketMessage public void onBinaryMethod(byte data[], int offset, int length)
    {
     // simple BINARY message received
    }

    public void deliver(String message)
    {
        if (wsSession != null && wsSession.isOpen() && !"".equals( message.trim() ) )
        {
            try {
                Log.debug(" : Delivered : \n" + message );
                wsSession.getRemote().sendString(message);
            } catch (Exception e) {
                Log.error("ProxyWebSocket deliver " + e);
                Log.warn("Could not deliver : \n" + message );
            }
        }
    }

    public void disconnect()
    {
        Log.debug("disconnect : ProxyWebSocket disconnect");

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
}