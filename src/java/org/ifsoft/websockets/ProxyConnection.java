package org.ifsoft.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.time.Duration;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.auth.callback.*;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.openfire.XMPPServer;

import net.sf.json.*;
import org.dom4j.Element;
import org.xmpp.packet.*;


public class ProxyConnection
{
    private static Logger Log = LoggerFactory.getLogger( "ProxyConnection" );
    private ProxyWebSocket socket;
    private boolean connected = false;

    private WebSocketClient wsClient = null;
	private HttpClient httpClient = null;
    private ProxySocket proxySocket = null;
    private final boolean isSecure;

    public ProxyConnection(URI uri, List<String> subprotocol, int connectTimeout)
    {
        Log.debug("ProxyConnection " + uri + " " + subprotocol);

        if("wss".equals(uri.getScheme()))
        {
            Log.debug("ProxyConnection - SSL");
            getSSLContext();
            isSecure = true;
        }
        else isSecure = false;

        httpClient = new HttpClient();
        final QueuedThreadPool queuedThreadPool = QueuedThreadPoolProvider.getQueuedThreadPool("ProxyConnection-HttpClient");
        httpClient.setExecutor(queuedThreadPool);
        httpClient.setConnectTimeout(connectTimeout);

        try
        {
			httpClient.start();
			wsClient = new WebSocketClient(httpClient);
			wsClient.setIdleTimeout(Duration.ofMinutes(5));			
            wsClient.start();
			
            final ClientUpgradeRequest request = new ClientUpgradeRequest(uri);
            if (subprotocol != null) request.setSubProtocols(subprotocol);
			
			proxySocket = new ProxySocket(this);			
            wsClient.connect(proxySocket, uri, request);

            Log.debug("Connecting to : " + uri);
            connected = true;
        }
        catch (Exception e)
        {
            Log.error("ProxyConnection " + uri, e);
            connected = false;
        }
    }

    public void setSocket( ProxyWebSocket socket ) {
        this.socket = socket;
    }

    public void deliver(String text)
    {
        Log.debug("ProxyConnection - deliver \n" + text);

        if (proxySocket != null)
        {
            proxySocket.deliver(text);
        }
    }

    public void stop()
    {
        Log.debug("ProxyConnection - stop");

        try
        {
            if (wsClient != null) wsClient.stop();
            if (httpClient != null) httpClient.stop();			
        }
        catch (Exception e)
        {
            Log.error("ProxyConnection - stop", e);
        }
    }

    public void disconnect()
    {
        Log.debug("ProxyConnection - disconnect");
        if (proxySocket != null) proxySocket.disconnect();
        if (wsClient != null) stop();
    }

    public void onClose(int code, String reason)
    {
        Log.debug("ProxyConnection - onClose " + reason + " " + code);
        connected = false;

        if (this.socket != null) this.socket.disconnect();
    }

    public void onMessage(String text) {
        Log.debug("ProxyConnection - onMessage \n" + text);

        try {
            this.socket.deliver(text);
        }

        catch (Exception e) {
            Log.error("deliverRawText error", e);
        }
    }

    public boolean isSecure() {
        return isSecure;
    }

    public boolean isConnected() {
        return connected;
    }

    private SSLContext getSSLContext()
    {
        SSLContext sc = null;

        try {
            Log.debug("ProxyConnection SSL truster");

            TrustManager[] trustAllCerts = new TrustManager[]
            {
               new X509TrustManager() {
                  public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                  }

                  public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                  public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

               }
            };

            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier()
            {
                public boolean verify(String hostname, SSLSession session) {
                  return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        } catch (Exception e)   {
            Log.error("getSSLContext SSL truster", e);
        }

        return sc;
    }

    @WebSocket(maxTextMessageSize = 64 * 1024) public class ProxySocket
    {
        private Session session;
        private ProxyConnection proxyConnection;
        private String lastMessage = null;
        private String ipaddr = null;

        public ProxySocket(ProxyConnection proxyConnection)
        {
            this.proxyConnection = proxyConnection;
        }

        @OnWebSocketError public void onError(Throwable t)
        {
            Log.error("Error: "  + t.getMessage(), t);
        }

        @OnWebSocketClose public void onClose(int statusCode, String reason)
        {
            Log.debug("ProxySocket onClose " + statusCode + " " + reason);
            this.session = null;
            if (proxyConnection != null) proxyConnection.onClose(statusCode, reason);
        }

        @OnWebSocketConnect public void onConnect(Session session)
        {
            Log.debug("ProxySocket onConnect: " + session);
            this.session = session;

            if (lastMessage != null) deliver(lastMessage);
        }

        @OnWebSocketMessage public void onMessage(String msg)
        {
            Log.debug("ProxySocket onMessage text \n" + msg);
            if (proxyConnection != null) proxyConnection.onMessage(msg);
        }
		
		@OnWebSocketMessage public void onMessage(byte[] data, int offset, int length)  
		{
            Log.debug("ProxySocket onMessage binary \n" + data);
		}		

        public void deliver(String text)
        {
            if (session != null)
            {
                try {
                    Log.debug("ProxySocket deliver: \n" + text);
                    session.getRemote().sendString(text);
                    lastMessage = null;
                } catch (Exception e) {
                    Log.error("ProxySocket deliver", e);
                }
            } else lastMessage = text;
        }

        public void disconnect()
        {
            if (session != null) session.close(StatusCode.NORMAL,"I'm done");
        }

    }
}
