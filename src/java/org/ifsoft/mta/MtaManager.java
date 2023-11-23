package org.ifsoft.mta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.cert.X509Certificate;
import java.security.Principal;


import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jivesoftware.openfire.Connection;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.keystore.CertificateStore;
import org.jivesoftware.openfire.keystore.IdentityStore;
import org.jivesoftware.openfire.spi.ConnectionConfiguration;
import org.jivesoftware.openfire.spi.ConnectionManagerImpl;
import org.jivesoftware.openfire.spi.ConnectionType;
import org.jivesoftware.openfire.spi.EncryptionArtifactFactory;
import org.jivesoftware.openfire.websocket.OpenfireWebSocketServlet;
import org.jivesoftware.util.CertificateEventListener;
import org.jivesoftware.util.CertificateManager;
import org.jivesoftware.util.JiveConstants;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.jivesoftware.util.TaskEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class MtaManager {
    private static final Logger Log = LoggerFactory.getLogger(MtaManager.class);
    private static MtaManager instance = new MtaManager();
    private Server mtaServer;
    private final HandlerList handlerList = new HandlerList();

    private static String keyStoreLocation;
    private static String keypass;
    private static String c2sTrustStoreLocation;
    private static String c2sTrustpass;

    static {
        keyStoreLocation = JiveGlobals.getProperty("xmpp.socket.ssl.keystore", "resources" + File.separator + "security" + File.separator + "keystore");
        keyStoreLocation = JiveGlobals.getHomeDirectory() + File.separator + keyStoreLocation;

        keypass = JiveGlobals.getProperty("xmpp.socket.ssl.keypass", "changeit");
        keypass = keypass.trim();

        c2sTrustStoreLocation = JiveGlobals.getProperty("xmpp.socket.ssl.client.truststore", "resources" + File.separator + "security" + File.separator + "client.truststore");
        c2sTrustStoreLocation = JiveGlobals.getHomeDirectory() + File.separator + c2sTrustStoreLocation;

        c2sTrustpass = JiveGlobals.getProperty("xmpp.socket.ssl.client.trustpass", "changeit");
        c2sTrustpass = c2sTrustpass.trim();

    }

    public static MtaManager getInstance() {
        return instance;
    }

    private MtaManager() {
        final Handler staticContentHandler = createStaticContentHandler();

        if ( staticContentHandler != null )
        {
            this.handlerList.addHandler( staticContentHandler );
        }
    }

    public void start() {

        // this is the number of threads allocated to each connector/port
        final int processingThreads = JiveGlobals.getIntProperty("mta.client.processing.threads", 200);

        final QueuedThreadPool tp = new QueuedThreadPool(processingThreads);
        tp.setName("Mutual-TLS-Authentication");

        mtaServer = new Server(tp);

        final Connector httpConnector = createConnector( mtaServer );
        final Connector httpsConnector = createSSLConnector( mtaServer);

        if (httpConnector == null && httpsConnector == null) {
            mtaServer = null;
            return;
        }
        if (httpConnector != null) {
            mtaServer.addConnector(httpConnector);
        }
        if (httpsConnector != null) {
            mtaServer.addConnector(httpsConnector);
        }

        mtaServer.setHandler( handlerList );

        try {
            mtaServer.start();
            handlerList.start();
            Log.info("MTA service started");
        }
        catch (Exception e) {
            Log.error("Error starting MTA service", e);
        }
    }

    public void stop() {

        if (mtaServer != null) {
            try {
                handlerList.stop();
                mtaServer.stop();
                Log.info("MTA service stopped");
            }
            catch (Exception e) {
                Log.error("Error stopping MTA service", e);
            }
            mtaServer = null;
        }
    }

    private Connector createConnector( final Server mtaServer ) {
        final int port = getHttpBindUnsecurePort();
        if (port > 0) {
            HttpConfiguration httpConfig = new HttpConfiguration();
            ServerConnector connector = new ServerConnector(mtaServer, new HttpConnectionFactory(httpConfig));

            // Listen on a specific network interface if it has been set.
            connector.setHost(getBindInterface());
            connector.setPort(port);
            return connector;
        }
        else
        {
            return null;
        }
    }

    private Connector createSSLConnector( final Server mtaServer ) {
        final int securePort = getHttpBindSecurePort();
        try {

            final HttpConfiguration httpsConfig = new HttpConfiguration();
            httpsConfig.setSecureScheme("https");
            httpsConfig.setSecurePort(securePort);
            httpsConfig.setOutputBufferSize(32768);
            httpsConfig.setRequestHeaderSize(8192);
            httpsConfig.setResponseHeaderSize(8192);
            httpsConfig.setSendServerVersion(true);
            httpsConfig.setSendDateHeader(false);
            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.addExcludeProtocols("SSLv3");
            sslContextFactory.setEndpointIdentificationAlgorithm(null);
            sslContextFactory.setTrustStorePath(c2sTrustStoreLocation);
            sslContextFactory.setTrustStorePassword(c2sTrustpass);
            sslContextFactory.setTrustStoreType(JiveGlobals.getProperty("xmpp.socket.ssl.storeType", "jks"));
            sslContextFactory.setKeyStorePath(keyStoreLocation);
            sslContextFactory.setKeyStorePassword(keypass);
            sslContextFactory.setKeyStoreType(JiveGlobals.getProperty("xmpp.socket.ssl.storeType", "jks"));
            sslContextFactory.setNeedClientAuth(true);
            sslContextFactory.setWantClientAuth(true);

            final ServerConnector sslConnector = new ServerConnector(mtaServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpsConfig));
            sslConnector.setHost(getBindInterface());
            sslConnector.setPort(securePort);
            return sslConnector;
        }
        catch (Exception e) {
            Log.error("Error creating SSL connector for Http bind", e);
        }

        return null;
    }

    private String getBindInterface() {
        String interfaceName = JiveGlobals.getXMLProperty("network.interface");
        String bindInterface = null;
        if (interfaceName != null) {
            if (interfaceName.trim().length() > 0) {
                bindInterface = interfaceName;
            }
        }
        return bindInterface;
    }

    /**
     * Creates a Jetty context handler that can be used to expose static files.
     *
     * Note that an invocation of this method will not register the handler (and thus make the related functionality
     * available to the end user). Instead, the created handler is returned by this method, and will need to be
     * registered with the embedded Jetty webserver by the caller.
     *
     * @return A Jetty context handler, or null when the static content could not be accessed.
     */
    protected Handler createStaticContentHandler()
    {
        final File mtaDirectory = new File( JiveGlobals.getHomeDirectory() + File.separator + "resources" + File.separator + "mta" );

        if ( mtaDirectory.exists() )
        {
            if ( mtaDirectory.canRead() )
            {
                final WebAppContext context = new WebAppContext( null, mtaDirectory.getPath(), "/" );
                final List<ContainerInitializer> initializers = new ArrayList<>();
                initializers.add(new ContainerInitializer(new JettyJasperInitializer(), null));
                context.setAttribute("org.eclipse.jetty.containerInitializers", initializers);
                context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
                context.setWelcomeFiles( new String[] { "index.html" } );

                return context;
            }
            else
            {
                Log.warn( "Openfire cannot read the directory: " + mtaDirectory );
            }
        }
        return null;
    }

    /**
     * Returns the MTAing port which does not use SSL.
     *
     * @return the MTAing port which does not use SSL.
     */
    public int getHttpBindUnsecurePort() {
        return 6060;
    }

    /**
     * Returns the MTAing port which uses SSL.
     *
     * @return the MTAing port which uses SSL.
     */
    public int getHttpBindSecurePort() {
        return 6443;
    }
}
