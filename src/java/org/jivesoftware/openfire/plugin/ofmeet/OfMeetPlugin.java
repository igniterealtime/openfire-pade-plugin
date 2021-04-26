/*
 * $Revision $
 * $Date $
 *
 * Copyright (C) 2005-2010 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.ofmeet;

import org.dom4j.Element;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlets.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.websocket.servlet.*;
import org.eclipse.jetty.websocket.server.*;
import org.eclipse.jetty.websocket.server.pathmap.ServletPathSpec;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;

import org.ice4j.ice.harvest.MappingCandidateHarvesters;

import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.MUCEventListener;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MUCEventDispatcher;
import org.jivesoftware.openfire.cluster.ClusterEventListener;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.security.SecurityAuditManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.*;

import org.ifsoft.websockets.*;
import java.util.concurrent.*;

import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.freeswitch.esl.client.manager.*;
import org.freeswitch.esl.client.transport.message.EslMessage;
import org.freeswitch.esl.client.IEslEventListener;
import org.freeswitch.esl.client.transport.event.EslEvent;

import org.jboss.netty.channel.ExceptionEvent;
import org.json.JSONObject;
import org.jitsi.util.OSUtils;
import de.mxro.process.*;
import javax.xml.bind.DatatypeConverter;
import org.voicebridge.Application;
import com.sun.voip.server.*;
import com.sun.voip.*;

/**
 * Bundles various Jitsi components into one, standalone Openfire plugin.
 */
public class OfMeetPlugin implements Plugin, SessionEventListener, ClusterEventListener, PropertyEventListener, IEslEventListener, MUCEventListener, ProcessListener, CallEventListener 
{
    private static final Logger Log = LoggerFactory.getLogger(OfMeetPlugin.class);
    private static final ScheduledExecutorService connExec = Executors.newSingleThreadScheduledExecutor();
    public static OfMeetPlugin self;
    public static String webRoot;
    public boolean restartNeeded = false;

    private ManagerConnection managerConnection;
    private Client client;
    private ScheduledFuture<ConnectThread> connectTask;
    private volatile boolean subscribed = false;

    private PluginManager manager;
    public File pluginDirectory;

    private OfMeetIQHandler ofmeetIQHandler;
    private WebAppContext publicWebApp;
    private ServletContextHandler jvbWsContext = null;
    private ServletContextHandler streamWsContext = null;	
    private BookmarkInterceptor bookmarkInterceptor;
	private Application voiceBridge = null;	

    private final OFMeetConfig config;
    private final JitsiJvbWrapper jitsiJvbWrapper;
    private final JitsiJicofoWrapper jitsiJicofoWrapper;
    private final JitsiJigasiWrapper jitsiJigasiWrapper;
    private final MeetingPlanner meetingPlanner;
    private final LobbyMuc lobbyMuc;
    private final SecurityAuditManager securityAuditManager = SecurityAuditManager.getInstance();
    
	private ComponentManager componentManager;
    private FocusComponent focusComponent = null;	

    public OfMeetPlugin()
    {
        config = new OFMeetConfig();

        jitsiJigasiWrapper = new JitsiJigasiWrapper();
        jitsiJicofoWrapper = new JitsiJicofoWrapper();
        jitsiJvbWrapper = new JitsiJvbWrapper();

        meetingPlanner = new MeetingPlanner();
        lobbyMuc = new LobbyMuc();
    }

    public String getName()
    {
        return "ofmeet";
    }

    public String getConferenceStats()
    {
        return jitsiJvbWrapper.getConferenceStats();
    }

    public String getJvbDuration()
    {
        long current_time = System.currentTimeMillis();
        String duration = "";

        try {
            long start_timestamp = Long.parseLong(System.getProperty("ofmeet.jvb.started.timestamp", String.valueOf(System.currentTimeMillis())));
            duration = StringUtils.getFullElapsedTime(System.currentTimeMillis() - start_timestamp);
        } catch (Exception e) {}

        return duration;
    }

    public String getDescription()
    {
        return "OfMeet Plugin";
    }

    public void initializePlugin(final PluginManager manager, final File pluginDirectory)
    {
        self = this;
        webRoot = pluginDirectory.getPath() + "/classes";

        this.manager = manager;
        this.pluginDirectory = pluginDirectory;

        // Initialize all Jitsi software, which provided the video-conferencing functionality.
        try
        {
            jitsiJvbWrapper.initialize( manager, pluginDirectory );

            if (config.getJigasiSipEnabled() && config.getJigasiSipUserId() != null)
            {
                ensureJigasiUser();
                jitsiJigasiWrapper.initialize(pluginDirectory);
            }

            ensureFocusUser();
            jitsiJicofoWrapper.initialize(pluginDirectory);
            loadBranding();	

            if ( JiveGlobals.getBooleanProperty( "ofmeet.use.internal.focus.component", false ) )
            {
				focusComponent = new FocusComponent();
				componentManager = ComponentManagerFactory.getComponentManager();			
				componentManager.addComponent("focus", focusComponent);		
			}
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while attempting to initialize the Jitsi components.", ex );
        }

        // Initialize our own additional functinality providers.
        try
        {
            meetingPlanner.initialize();
            lobbyMuc.initialize();
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while attempting to initialize the Meeting Planner.", ex );
        }

        try
        {
            loadPublicWebApp();
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while attempting to load the public web application.", ex );
        }

        try
        {
            ClusterManager.addListener(this);
            checkDownloadFolder(pluginDirectory);

            Log.info("OfMeet Plugin - Initialize IQ handler ");

            ofmeetIQHandler = new OfMeetIQHandler();
            XMPPServer.getInstance().getIQRouter().addHandler(ofmeetIQHandler);

            if ( JiveGlobals.getBooleanProperty( "ofmeet.bookmarks.auto-enable", true ) )
            {
                bookmarkInterceptor = new BookmarkInterceptor( this );
                InterceptorManager.getInstance().addInterceptor( bookmarkInterceptor );
            }

            SessionEventDispatcher.addListener(this);
            PropertyEventDispatcher.addListener(this);
            MUCEventDispatcher.addListener(this);
        }
        catch (Exception e) {
            Log.error("Could NOT start open fire meetings", e);
        }

        String freeswitchServer = config.jigasiFreeSwitchHost.get();
        String freeswitchPassword = config.jigasiFreeSwitchPassword.get();;
        System.setProperty("ofmeet.freeswitch.started", "false");

        try
        {
            if (config.getJigasiFreeSwitchEnabled() && freeswitchServer != null)
            {
                managerConnection = new DefaultManagerConnection(freeswitchServer, freeswitchPassword);
                managerConnection.getESLClient();
                ConnectThread connector = new ConnectThread();
                connectTask = (ScheduledFuture<ConnectThread>) connExec.scheduleAtFixedRate(connector, 30,  5, TimeUnit.SECONDS);
            }
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while attempting to connect to FreeSWITCH", ex );
        }
					
		if (config.getLiveStreamEnabled())
		{
			setupFFMPEG(pluginDirectory);
		}

		if (config.getAudiobridgeEnabled())
		{
			voiceBridge = new Application();
			voiceBridge.appStart(pluginDirectory);		
		}
    }

    public void destroyPlugin()
    {
        try
        {
            SessionEventDispatcher.removeListener(this);
            PropertyEventDispatcher.removeListener( this );
            MUCEventDispatcher.removeListener(this);

            unloadPublicWebApp();
			
			if (focusComponent != null) componentManager.removeComponent("focus");			
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to unload the public web application of OFMeet.", ex );
        }

        try
        {
            meetingPlanner.destroy();
            lobbyMuc.destroy();
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to destroy the Meeting Planner", ex );
        }

        try
        {
            XMPPServer.getInstance().getIQRouter().removeHandler(ofmeetIQHandler);
            ofmeetIQHandler = null;
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to destroy the OFMeet IQ Handler.", ex );
        }

        try
        {
            jitsiJigasiWrapper.destroy();
            jitsiJvbWrapper.destroy();
            jitsiJicofoWrapper.destroy();
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to destroy the Jitsi Videobridge plugin wrapper.", ex );
        }

        ClusterManager.removeListener(this);

        if ( bookmarkInterceptor != null )
        {
            InterceptorManager.getInstance().removeInterceptor( bookmarkInterceptor );
            bookmarkInterceptor = null;
        }

        if (connectTask != null) connectTask.cancel(true);
        if (managerConnection != null) managerConnection.disconnect();	
		if (voiceBridge != null) voiceBridge.appStop();
    }

    protected void loadPublicWebApp() throws Exception
    {
        Log.info( "Initializing public web application for /colibri-ws web socket" );

        jvbWsContext = new ServletContextHandler(null, "/colibri-ws", ServletContextHandler.SESSIONS);

        try {
            WebSocketUpgradeFilter wsfilter = WebSocketUpgradeFilter.configureContext(jvbWsContext);
            wsfilter.getFactory().getPolicy().setIdleTimeout(60 * 60 * 1000);
            wsfilter.getFactory().getPolicy().setMaxTextMessageSize(64000000);
            wsfilter.addMapping(new ServletPathSpec("/*"), new JvbSocketCreator());					

        } catch (Exception e) {
            Log.error("loadPublicWebApp", e);
        }

        HttpBindManager.getInstance().addJettyHandler(jvbWsContext);	

        publicWebApp = new WebAppContext(null, pluginDirectory.getPath() + "/classes/jitsi-meet",  new OFMeetConfig().getWebappContextPath());
        publicWebApp.setClassLoader(this.getClass().getClassLoader());

        final List<ContainerInitializer> initializers4 = new ArrayList<>();
        initializers4.add(new ContainerInitializer(new JettyJasperInitializer(), null));
        publicWebApp.setAttribute("org.eclipse.jetty.containerInitializers", initializers4);
        publicWebApp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());

        HttpBindManager.getInstance().addJettyHandler( publicWebApp );

        Log.debug( "Initialized public web application", publicWebApp.toString() );
    }

	
    public static class JvbSocketCreator implements WebSocketCreator
    {
        @Override public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
        {
            String ipaddr = getIpAddress();
            String jvbPort = JiveGlobals.getProperty( "ofmeet.websockets.plainport", "8180");

            HttpServletRequest request = req.getHttpServletRequest();
            String path = request.getRequestURI();
            String query = request.getQueryString();
            List<String> protocols = new ArrayList<String>();

            for (String subprotocol : req.getSubProtocols())
            {
                Log.info("WSocketCreator found protocol " + subprotocol);
                resp.setAcceptedSubProtocol(subprotocol);
                protocols.add(subprotocol);
            }

            if (query != null) path += "?" + query;

            Log.info("JvbSocketCreator " + path + " " + query);

            String url = "ws://localhost:" + jvbPort + path;

            ProxyWebSocket socket = null;
            ProxyConnection proxyConnection = new ProxyConnection(URI.create(url), protocols, 10000);

            socket = new ProxyWebSocket();
            socket.setProxyConnection(proxyConnection);
            return socket;
        }
    }	


    public void unloadPublicWebApp() throws Exception
    {
        if ( publicWebApp != null )
        {
            try
            {
                HttpBindManager.getInstance().removeJettyHandler( publicWebApp );
                publicWebApp.destroy();

                if (jvbWsContext != null)
                {
                    HttpBindManager.getInstance().removeJettyHandler(jvbWsContext);
                    jvbWsContext.destroy();
                }
				
                if (streamWsContext != null)
                {
                    HttpBindManager.getInstance().removeJettyHandler(streamWsContext);
                    streamWsContext.destroy();
                }				
            }
            finally
            {
                publicWebApp = null;
            }
        }
    }

    public URL getWebappURL()
    {
        final String override = JiveGlobals.getProperty( "ofmeet.webapp.url.override" );
        if ( override != null && !override.trim().isEmpty() )
        {
            try
            {
                return new URL( override );
            }
            catch ( MalformedURLException e )
            {
                Log.warn( "An override for the webapp address is defined in 'ofmeet.webapp.url.override', but its value is not a valid URL.", e );
            }
        }
        try
        {
            final String protocol = "https"; // No point in providing the non-SSL protocol, as webRTC won't work there.
            final String host = XMPPServer.getInstance().getServerInfo().getHostname();
            final int port = HttpBindManager.getInstance().getHttpBindSecurePort();
            final String path;
            if ( publicWebApp != null )
            {
                path = publicWebApp.getContextPath();
            }
            else
            {
                path = new OFMeetConfig().getWebappContextPath();
            }

            return new URL( protocol, host, port, path );
        }
        catch ( MalformedURLException e )
        {
            Log.error( "Unable to compose the webapp URL", e );
            return null;
        }
    }

    private void checkDownloadFolder(File pluginDirectory)
    {
        String ofmeetHome = JiveGlobals.getHomeDirectory() + File.separator + "resources" + File.separator + "spank" + File.separator + "ofmeet-cdn";

        try
        {
            File ofmeetFolderPath = new File(ofmeetHome);

            if(!ofmeetFolderPath.exists())
            {
                ofmeetFolderPath.mkdirs();

            }

            List<String> lines = Arrays.asList("Move on, nothing here....");
            Path file = Paths.get(ofmeetHome + File.separator + "index.html");
            Files.write(file, lines, Charset.forName("UTF-8"));

            File downloadHome = new File(ofmeetHome + File.separator + "download");

            if(!downloadHome.exists())
            {
                downloadHome.mkdirs();

            }

            lines = Arrays.asList("Move on, nothing here....");
            file = Paths.get(downloadHome + File.separator + "index.html");
            Files.write(file, lines, Charset.forName("UTF-8"));
        }
        catch (Exception e)
        {
            Log.error("checkDownloadFolder", e);
        }
    }

    public static String getIpAddress()
    {
        String ourHostname = XMPPServer.getInstance().getServerInfo().getHostname();
        String ourIpAddress = "127.0.0.1";

        try {
            ourIpAddress = InetAddress.getByName(ourHostname).getHostAddress();
        } catch (Exception e) {

        }

        return ourIpAddress;
    }

    private void ensureJigasiUser()
    {
        // Ensure that the 'jigasi' user exists.
        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        if ( !userManager.isRegisteredUser( config.jigasiXmppUserId.get() ) )
        {
            Log.info( "No pre-existing 'jigasi' user detected. Generating one." );

            String password = config.jigasiXmppPassword.get();
            if ( password == null || password.isEmpty() )
            {
                password = StringUtils.randomString( 40 );
            }

            try
            {
                userManager.createUser(config.jigasiXmppUserId.get(), password, "Jigasi User (generated)", null);
                config.jigasiXmppUserId.set( password );
            }
            catch ( Exception e )
            {
                Log.error( "Unable to provision a 'jigasi' user.", e );
            }
        }
    }
    private void ensureFocusUser()
    {
        // Ensure that the 'focus' user exists.
        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        if ( !userManager.isRegisteredUser( "focus" ) )
        {
            Log.info( "No pre-existing 'focus' user detected. Generating one." );

            String password = config.getFocusPassword();
            if ( password == null || password.isEmpty() )
            {
                password = StringUtils.randomString( 40 );
            }

            try
            {
                userManager.createUser(
                        "focus",
                        password,
                        "Focus User (generated)",
                        null
                );
                config.setFocusPassword( password );
            }
            catch ( Exception e )
            {
                Log.error( "Unable to provision a 'focus' user.", e );
            }
        }

        if ( JiveGlobals.getBooleanProperty( "ofmeet.conference.admin", true ) )
        {
            // Ensure that the 'focus' user can grant permissions in persistent MUCs by making it a sysadmin of the conference service(s).
            final JID focusUserJid = new JID( "focus@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() );

            for ( final MultiUserChatService mucService : XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatServices() )
            {
                if ( !mucService.isSysadmin( focusUserJid ) )
                {
                    Log.info( "Adding 'focus' user as a sysadmin to the '{}' MUC service.", mucService.getServiceName() );
                    mucService.addSysadmin( focusUserJid );
                }
            }
        }
    }

    private void loadBranding()
    {
        JSONObject jsonObject;
        String jsonString = getStringFromFile(webRoot + "/docs/options/branding.js");

        if (jsonString.indexOf("var branding = ") == 0)
        {
			try
			{	
				// TODO this regex is not working with URLS			
				//jsonString = jsonString.replaceAll("/\\*[\\s\\S]*?\\*/|//.*", "");
				jsonObject = new JSONObject( jsonString.substring(15) );

				for (String propertyName : jsonObject.keySet())
				{
					String existingValue = JiveGlobals.getProperty("pade.branding." + propertyName, null);
					String json = jsonObject.getJSONObject(propertyName).toString();

					Log.debug("loadBranding - processing " + propertyName + ", existing=" + existingValue + ", branding=" + json);

					if (existingValue == null)  // add new settings, don't overwrite existing settings
					{
						JiveGlobals.setProperty("pade.branding." + propertyName, json);
					}
				}
			} catch (Exception e) {
				Log.error( "[{}] Unexpected branding.js!\n" + jsonString);				
			}
        }
        else {
            Log.error( "[{}] Unexpected branding.js!\n" + jsonString);
        }
    }

    private String getStringFromFile(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (Exception e)
        {
            Log.error("getStringFromFile failed", e);
        }

        return contentBuilder.toString();
    }

    //-------------------------------------------------------
    //
    //      ffmpeg live streaming
    //
    //-------------------------------------------------------
	
	private void setupFFMPEG(File pluginDirectory)
	{
		final String path = pluginDirectory.getAbsolutePath() + File.separator + "classes" + File.separator +  "ffmpeg";
        final File folder = new File(path);			
		
		if (OSUtils.IS_LINUX64 || OSUtils.IS_WINDOWS64)
		{
			if (!folder.exists())
			{						
				new Thread()
				{
					@Override public void run()
					{
						try
						{
							folder.mkdir();
							
							String jarFileSuffix = null;

							if (OSUtils.IS_LINUX64) 	jarFileSuffix = "ffmpeg-linux64.zip";
							if (OSUtils.IS_WINDOWS64) 	jarFileSuffix = "ffmpeg-win64.zip";						

							InputStream inputStream = new URL("https://github.com/deleolajide/binaries/releases/download/v0.0.1/" + jarFileSuffix).openStream();
							ZipInputStream zipIn = new ZipInputStream(inputStream);
							ZipEntry entry = zipIn.getNextEntry();

							while (entry != null)
							{
								try
								{
									String filePath = path + File.separator + entry.getName();

									if (!entry.isDirectory())
									{	
										File file = new File(filePath);
										
										new File(file.getParent()).mkdirs();
										extractFile(zipIn, filePath);
										
										file.setReadable(true, true);
										file.setWritable(true, true);
										file.setExecutable(true, true);		

										Log.info("ffmpeg writing file..." + filePath);

									}
									zipIn.closeEntry();
									entry = zipIn.getNextEntry();
								}
								catch(Exception e) {
									Log.error("Error", e);
								}
							}
							zipIn.close();

							Log.info("ffmpeg binaries extracted ok");
							startFFMPEG(path);						
							
						}						
						catch (Exception e)
						{
							Log.error(e.getMessage(), e);
						}						
						
					}

				}.start();
			}
			else {
				Log.warn("ffmpeg folder already exist.");
				startFFMPEG(path);
			}						
		}
		else {
			Log.error("O/S platform not supported.");
		}		
	}
	
	private void startFFMPEG(String path)
	{	
        streamWsContext = new ServletContextHandler(null, "/livestream-ws", ServletContextHandler.SESSIONS);		

        try {			
            WebSocketUpgradeFilter wsfilter2 = WebSocketUpgradeFilter.configureContext(streamWsContext);
            wsfilter2.getFactory().getPolicy().setIdleTimeout(60 * 60 * 1000);
            wsfilter2.getFactory().getPolicy().setMaxTextMessageSize(64000000);
            wsfilter2.getFactory().getPolicy().setMaxBinaryMessageSize(64000000);			
			
            wsfilter2.addMapping(new ServletPathSpec("/*"), new StreamSocketCreator());						

        } catch (Exception e) {
            Log.error("loadPublicWebApp", e);
        }			

        try {
			String ffmpegName = null;
			if (OSUtils.IS_LINUX64) 	ffmpegName = "ffmpeg";
			if (OSUtils.IS_WINDOWS64) 	ffmpegName = "ffmpeg.exe";									
					
			final String ffmpeg = path + File.separator + ffmpegName;	
			final String cmdLine = ffmpeg + " -h";
			Spawn.startProcess(cmdLine, new File(path), this);
			Log.info( "ffmpeg testing with "  + cmdLine);	

        } catch ( Exception e ) {
            Log.error( "An error occurred while testing ffmpeg", e );
        }		
		
        HttpBindManager.getInstance().addJettyHandler(streamWsContext);	
		Log.info( "ffmpeg websocket proxy ready");
	}
	
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException
    {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;

        while ((read = zipIn.read(bytesIn)) != -1)
        {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }	
	
    public static class StreamSocketCreator implements WebSocketCreator
    {
        @Override public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
        {
			String streamKey = null;
			
            for (String subprotocol : req.getSubProtocols())
            {
                Log.info("WSocketCreator found protocol " + subprotocol);
                resp.setAcceptedSubProtocol(subprotocol);
				streamKey = subprotocol;
            }

            return new LiveStreamSocket(streamKey);
        }
    }

    public void onOutputLine(final String line)
    {
        Log.info("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.info("onProcessQuit " + code);
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.info(line);
		
        if (line.contains("ffmpeg version")) 
		{
			System.setProperty("ofmeet.ffmpeg.installed", "true");
			Log.info( "ffmpeg rtmp streamer installed");			
		}
    }	

    public void onError(Throwable error)
    {
        Log.error("onError", error);
    }
    //-------------------------------------------------------
    //
    //      clustering
    //
    //-------------------------------------------------------

    @Override
    public void joinedCluster()
    {
        Log.info("OfMeet Plugin - joinedCluster");
        try
        {
            jitsiJvbWrapper.destroy();
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to destroy the Jitsi Plugin.", ex );
        }
    }

    @Override
    public void joinedCluster(byte[] arg0)
    {
    }

    @Override
    public void leftCluster()
    {
        Log.info("OfMeet Plugin - leftCluster");
        try
        {
            jitsiJvbWrapper.initialize( manager, pluginDirectory );
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to initialize the Jitsi Plugin.", ex );
        }
    }

    @Override
    public void leftCluster(byte[] arg0)
    {
    }

    @Override
    public void markedAsSeniorClusterMember()
    {
        Log.info("OfMeet Plugin - markedAsSeniorClusterMember");
        try
        {
            jitsiJvbWrapper.initialize( manager, pluginDirectory );
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred while trying to initialize the Jitsi Plugin.", ex );
        }
    }

    public void setRecording(String roomName, String path)
    {

    }

    //-------------------------------------------------------
    //
    //      MUC room events
    //
    //-------------------------------------------------------

    @Override
    public void roomCreated(JID roomJID)
    {

    }

    @Override
    public void roomDestroyed(JID roomJID)
    {

    }

    @Override
    public void occupantJoined(final JID roomJID, JID user, String nickname)
    {
        if (config.getJigasiSipEnabled() && config.getJigasiSipUserId() != null)		
        {
            Log.debug("occupantJoined " + roomJID + " " + nickname + " " + user);

            String roomName = roomJID.getNode();
            String userName = user.getNode();

            try {

                if ("focus".equals(userName) && nickname.startsWith("focus") && !"ofgasi".equals(roomName) && !"ofmeet".equals(roomName))
                {
                    String sipUserId = config.jigasiSipUserId.get().split("@")[0];
					
					if (client != null)	
					{						
						String command = "originate {sip_from_user=" + userName + ",origination_uuid=" + roomName + "}[sip_h_Jitsi-Conference-Room=" + roomName + "]user/" + config.jigasiSipUserId.get() + " &conference(" + roomName + ")";
						String error = sendAsyncFWCommand(command);
						
						if (error != null)
						{
							Log.info("focus joined room, started freeswitch conference " + command);
							System.setProperty("ofmeet.freeswitch." + roomName, "true");
						} else {
							Log.error("focus joined room, freeswitch originate failed - " + error);
						}						
					}

					if (config.getAudiobridgeEnabled())
					{	
						SipServer.setSendSipUriToProxy(true);
						CallParticipant cp = new CallParticipant();
						cp.setProtocol("SIP");
						cp.setConferenceHeaderName("Jitsi-Conference-Room");
						cp.setTransport(config.jigasiSipTransport.get());						
						cp.setCallId(roomName);
						cp.setDisplayName(sipUserId);						
						cp.setVoiceDetection(false);
						cp.setConferenceId(roomName);	
						cp.setPhoneNumber(sipUserId);
						cp.setSipProxy(config.jigasiProxyServer.get() + ":" + config.jigasiProxyPort.get());
						
						OutgoingCallHandler outgoingCallHandler = new OutgoingCallHandler(this, cp);
						outgoingCallHandler.start();

						Log.info("focus joined room, started audiobridge with " + cp);

					}				
                }
            } catch ( Exception e ) {
                Log.error( "An exception occurred while trying to start freeswitch conference " + roomName, e );
            }
        }
    }

    @Override
    public void occupantLeft(final JID roomJID, JID user)
    {
        Log.debug("occupantLeft " + roomJID + " " + user);

        String roomName = roomJID.getNode();
        String userName = user.getNode();

        try {

            if ("focus".equals(userName) && !"ofgasi".equals(roomName) && !"ofmeet".equals(roomName))
            {
                if (client != null)
                {
                    String error = sendAsyncFWCommand("uuid_kill " + roomName);

                    if (error != null)
                    {
                        Log.info("focus left room, stop freeswitch conference " + roomName);
                        System.setProperty("ofmeet.freeswitch." + roomName, "false");
                    } else {
                        Log.error("focus left room, freeswitch uuid_kill failed - " + error);
                    }
                }

				if (config.getAudiobridgeEnabled())
				{				
					CallHandler.hangup(roomName, "User requested call termination");	
					Log.info("focus left room, stopping audiobridge conference " + roomName);					
				}					

                String json = getConferenceStats();
                String comment = "";

                if (json != null)
                {
                    try {
                        net.sf.json.JSONObject summary = new net.sf.json.JSONObject(json);

                        int total_conference_seconds = summary.getInt("total_conference_seconds");
                        int total_participants = summary.getInt("total_participants");
                        int total_failed_conferences = summary.getInt("total_failed_conferences");
                        int total_conferences_created = summary.getInt("total_conferences_created");
                        int total_conferences_completed = summary.getInt("total_conferences_completed");
                        int conferences = summary.getInt("conferences");
                        int participants = summary.getInt("participants");
                        int largest_conference = summary.getInt("largest_conference");
                        int p2p_conferences = summary.getInt("p2p_conferences");

                        comment = "uptime: " + getJvbDuration() + ", used (secs): " + total_conference_seconds + ", people: " + total_participants + ", failed: " + total_failed_conferences + ", completed: " + total_conferences_completed + ", conferences: " + conferences + ",participants: " + participants + ", largest: " + largest_conference + ", p2p: " + p2p_conferences;

                    } catch (Exception e1) {
                        Log.error("error getting jvb colibri stats");
                    }
                    securityAuditManager.logEvent("pade", "meeting - " + roomName, comment);
                }
            } else if (!roomName.equals("ofmeet") && !roomName.equals("ofgasi")) {
                MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(roomJID)
                        .getChatRoom(roomJID.getNode());

                if (room.getOwners().stream().anyMatch(o -> o.getNode().equals("focus"))) {
                    if (room.getAffiliation(user) == MUCRole.Affiliation.owner) {
                        // Remove owner authority of the user
                        List<Presence> addNonePresence = room.isMembersOnly()
                                ? room.addMember(user, null, room.getRole())
                                : room.addNone(user, room.getRole());

                        // Send a presence to other room members
                        for (Presence p : addNonePresence) {
                            room.send(p, room.getRole());
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            Log.error( "An exception occurred while trying to stop freeswitch conference " + roomName, e );
        }
    }

    @Override
    public void nicknameChanged(JID roomJID, JID user, String oldNickname, String newNickname)
    {

    }

    @Override
    public void messageReceived(JID roomJID, JID user, String nickname, Message message)
    {

    }

    @Override
    public void roomSubjectChanged(JID roomJID, JID user, String newSubject)
    {

    }

    @Override
    public void privateMessageRecieved(JID a, JID b, Message message)
    {

    }

    //-------------------------------------------------------
    //
    //      session management
    //
    //-------------------------------------------------------

    public void anonymousSessionCreated(Session session)
    {
        Log.debug("OfMeet Plugin -  anonymousSessionCreated "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void anonymousSessionDestroyed(Session session)
    {
        Log.debug("OfMeet Plugin -  anonymousSessionDestroyed "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void resourceBound(Session session)
    {
        Log.debug("OfMeet Plugin -  resourceBound "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void sessionCreated(Session session)
    {
        Log.debug("OfMeet Plugin -  sessionCreated "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void sessionDestroyed(Session session)
    {
        Log.debug("OfMeet Plugin -  sessionDestroyed "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    //-------------------------------------------------------
    //
    //      property management
    //
    //-------------------------------------------------------

    @Override
    public void propertySet( String s, Map map )
    {
        switch (s)
        {
            case OFMeetConfig.OFMEET_WEBAPP_CONTEXTPATH_PROPERTYNAME:
                final String currentValue = this.publicWebApp.getContextPath();
                final String updatedValue = new OFMeetConfig().getWebappContextPath();
                if ( !currentValue.equals( updatedValue ) )
                {
                    Log.debug( "A configuration change requires the web application to be reloaded on a different context path. Old path: {}, new path: {}.", currentValue, updatedValue );
                    try
                    {
                        unloadPublicWebApp();
                        loadPublicWebApp();
                    }
                    catch ( Exception e )
                    {
                        Log.error( "An exception occurred while trying to re-load the web application on a different context path. Old path: {}, new path: {}.", currentValue, updatedValue, e );
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void propertyDeleted( String s, Map map )
    {
        propertySet( s, map );
    }

    @Override
    public void xmlPropertySet( String s, Map map )
    {

    }

    @Override
    public void xmlPropertyDeleted( String s, Map map )
    {

    }

    //-------------------------------------------------------
    //
    //  FreeSWITCH Events
    //
    //-------------------------------------------------------

    private class ConnectThread implements Runnable
    {
        public void run()
        {
            try {
                client = managerConnection.getESLClient();
                if (! client.canSend()) {
                    Log.info("Attempting to connect to FreeSWITCH ESL");
                    subscribed = false;
                    managerConnection.connect();
                } else {
                    if (!subscribed) {
                        Log.info("Subscribing for FreeSWITCH ESL events.");
                        client.cancelEventSubscriptions();
                        client.addEventListener(self);
                        client.setEventSubscriptions( "plain", "all" );
                        client.addEventFilter("Event-Name", "heartbeat");
                        client.addEventFilter("Event-Name", "custom");
                        client.addEventFilter("Event-Name", "channel_callstate");
                        client.addEventFilter("Event-Name", "presence_in");
                        client.addEventFilter("Event-Name", "background_job");
                        client.addEventFilter("Event-Name", "recv_info");
                        client.addEventFilter("Event-Name", "dtmf");
                        client.addEventFilter("Event-Name", "conference::maintenance");
                        client.addEventFilter("Event-Name", "sofia::register");
                        client.addEventFilter("Event-Name", "sofia::expire");
                        client.addEventFilter("Event-Name", "message");
                        client.addEventFilter("Event-Name", "dtmf");
                        subscribed = true;
                        System.setProperty("ofmeet.freeswitch.started", "true");
                    }
                }
            } catch (InboundConnectionFailure e) {
                Log.error("Failed to connect to FreeSWITCH ESL", e);
            }
        }
    }


    @Override public void eventReceived( EslEvent event )
    {
        String eventName = event.getEventName();
        Map<String, String> headers = event.getEventHeaders();
        String eventType = headers.get("Event-Subclass");

        Log.debug("eventReceived " + eventName + " " + eventType);
    }

    @Override public void conferenceEventJoin(String uniqueId, String confName, int confSize, EslEvent event)
    {
        Log.info("conferenceEventJoin " + confName + " " + confSize);
    }

    @Override public void conferenceEventLeave(String uniqueId, String confName, int confSize, EslEvent event)
    {
        Log.info("conferenceEventLeave " + confName + " " + confSize);
    }

    @Override public void conferenceEventMute(String uniqueId, String confName, int confSize, EslEvent event)
    {

    }

    @Override public void conferenceEventUnMute(String uniqueId, String confName, int confSize, EslEvent event)
    {

    }

    @Override public void conferenceEventAction(String uniqueId, String confName, int confSize, String action, EslEvent event)
    {

    }

    @Override public void conferenceEventTransfer(String uniqueId, String confName, int confSize, EslEvent event)
    {

    }

    @Override public void conferenceEventThreadRun(String uniqueId, String confName, int confSize, EslEvent event)
    {

    }

    @Override public void conferenceEventRecord(String uniqueId, String confName, int confSize, EslEvent event)
    {

    }

    @Override public void conferenceEventPlayFile(String uniqueId, String confName, int confSize, EslEvent event)
    {

    }

    @Override public void backgroundJobResultReceived( EslEvent event )
    {

    }

    @Override public void exceptionCaught(ExceptionEvent e)
    {
        Log.error("exceptionCaught", e);
    }

    public String getSessionVar(String uuid, String var)
    {
        String value = null;

        if (client.canSend())
        {
            EslMessage response = client.sendSyncApiCommand("uuid_getvar", uuid + " " + var);

            if (response != null)
            {
                value = response.getBodyLines().get(0);
            }
        }

        return value;
    }

    public String sendAsyncFWCommand(String command)
    {
        String response = null;

        if (client != null)
        {
            Log.debug("sendAsyncFWCommand " + command);
            response = client.sendAsyncApiCommand(command, "");
        }

        return response;
    }

    public EslMessage sendFWCommand(String command)
    {
        EslMessage response = null;

        if (client != null)
        {
            Log.debug("sendFWCommand " + command);
            response = client.sendSyncApiCommand(command, "");
        }

        return response;
    }
	
    public boolean routeIncomingSIP(CallParticipant cp)
    {
		return false;
	}

    public void callEventNotification(CallEvent callEvent)
    {
 		Log.info( "Audiobridge callEventNotification " + callEvent.toString());
    }	
}
