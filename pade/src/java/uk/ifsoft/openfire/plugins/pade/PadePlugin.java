package uk.ifsoft.openfire.plugins.pade;

import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.net.SASLAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.webapp.WebAppContext;

import org.eclipse.jetty.util.security.*;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.*;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;

import org.jivesoftware.openfire.*;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.session.*;
import org.jivesoftware.openfire.group.*;
import org.jivesoftware.openfire.plugin.rest.sasl.*;
import org.jivesoftware.openfire.plugin.rest.service.JerseyWrapper;
import org.jivesoftware.openfire.plugin.rest.OpenfireLoginService;
import org.jivesoftware.util.*;

import java.io.File;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.security.Security;
import javax.servlet.DispatcherType;

import org.jitsi.util.OSUtils;
import waffle.servlet.NegotiateSecurityFilter;
import waffle.servlet.WaffleInfoServlet;
import org.xmpp.packet.*;
import org.dom4j.Element;


public class PadePlugin implements Plugin, MUCEventListener
{
    private static final Logger Log = LoggerFactory.getLogger( PadePlugin.class );

    private ServletContextHandler contextRest;
    private WebAppContext contextPublic;
    private WebAppContext contextPrivate;
    private WebAppContext contextWinSSO;

    /**
     * Initializes the plugin.
     *
     * @param manager         the plugin manager.
     * @param pluginDirectory the directory where the plugin is located.
     */
    @Override
    public void initializePlugin( final PluginManager manager, final File pluginDirectory )
    {
        Log.info("start pade server");

        contextRest = new ServletContextHandler(null, "/rest", ServletContextHandler.SESSIONS);
        contextRest.setClassLoader(this.getClass().getClassLoader());
        contextRest.addServlet(new ServletHolder(new JerseyWrapper()), "/api/*");
        HttpBindManager.getInstance().addJettyHandler(contextRest);

        contextPrivate = new WebAppContext(null, pluginDirectory.getPath() + "/classes/private", "/dashboard");
        contextPrivate.setClassLoader(this.getClass().getClassLoader());
        contextPrivate.getMimeTypes().addMimeMapping("wasm", "application/wasm");
        SecurityHandler securityHandler = basicAuth("ofmeet");
        contextPrivate.setSecurityHandler(securityHandler);
        final List<ContainerInitializer> initializersDashboard = new ArrayList<>();
        initializersDashboard.add(new ContainerInitializer(new JettyJasperInitializer(), null));
        contextPrivate.setAttribute("org.eclipse.jetty.containerInitializers", initializersDashboard);
        contextPrivate.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        contextPrivate.setWelcomeFiles(new String[]{"index.jsp"});
        HttpBindManager.getInstance().addJettyHandler(contextPrivate);

        contextPublic = new WebAppContext(null, pluginDirectory.getPath() + "/classes/public", "/pade");
        contextPublic.setClassLoader(this.getClass().getClassLoader());
        contextPublic.getMimeTypes().addMimeMapping("wasm", "application/wasm");
        final List<ContainerInitializer> initializersCRM = new ArrayList<>();
        initializersCRM.add(new ContainerInitializer(new JettyJasperInitializer(), null));
        contextPublic.setAttribute("org.eclipse.jetty.containerInitializers", initializersCRM);
        contextPublic.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        contextPublic.setWelcomeFiles(new String[]{"index.html"});
        HttpBindManager.getInstance().addJettyHandler(contextPublic);

        if (OSUtils.IS_WINDOWS)
        {
            contextWinSSO = new WebAppContext(null, pluginDirectory.getPath() + "/classes/win-sso", "/sso");
            contextWinSSO.setClassLoader(this.getClass().getClassLoader());

            final List<ContainerInitializer> initializers7 = new ArrayList<>();
            initializers7.add(new ContainerInitializer(new JettyJasperInitializer(), null));
            contextWinSSO.setAttribute("org.eclipse.jetty.containerInitializers", initializers7);
            contextWinSSO.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
            contextWinSSO.setWelcomeFiles(new String[]{"index.jsp"});

            NegotiateSecurityFilter securityFilter = new NegotiateSecurityFilter();
            FilterHolder filterHolder = new FilterHolder();
            filterHolder.setFilter(securityFilter);
            EnumSet<DispatcherType> enums = EnumSet.of(DispatcherType.REQUEST);
            enums.add(DispatcherType.REQUEST);

            contextWinSSO.addFilter(filterHolder, "/*", enums);
            contextWinSSO.addServlet(new ServletHolder(new WaffleInfoServlet()), "/waffle");
            contextWinSSO.addServlet(new ServletHolder(new org.ifsoft.sso.Password()), "/password");
            HttpBindManager.getInstance().addJettyHandler(contextWinSSO);
        }

        try
        {
            Security.addProvider( new OfChatSaslProvider() );
            SASLAuthentication.addSupportedMechanism( OfChatSaslServer.MECHANISM_NAME );
        }
        catch ( Exception ex )
        {
            Log.error( "An exception occurred", ex );
        }

        Log.info("Create recordings folder");
        checkRecordingsFolder();

        if ( JiveGlobals.getBooleanProperty( "pade.mucevent.dispatcher.enabled", true))
        {
            MUCEventDispatcher.addListener(this);
        }
    }

    /**
     * Destroys the plugin.<p>
     * <p>
     * Implementations of this method must release all resources held
     * by the plugin such as file handles, database or network connections,
     * and references to core Openfire classes. In other words, a
     * garbage collection executed after this method is called must be able
     * to clean up all plugin classes.
     */
    @Override
    public void destroyPlugin()
    {
        Log.info("stop pade server");

        try {
            SASLAuthentication.removeSupportedMechanism( OfChatSaslServer.MECHANISM_NAME );
            Security.removeProvider( OfChatSaslProvider.NAME );
        } catch (Exception e) {}

        HttpBindManager.getInstance().removeJettyHandler(contextRest);
        HttpBindManager.getInstance().removeJettyHandler(contextPublic);
        HttpBindManager.getInstance().removeJettyHandler(contextPrivate);

        if (contextWinSSO != null) HttpBindManager.getInstance().removeJettyHandler(contextWinSSO);

        if ( JiveGlobals.getBooleanProperty( "pade.mucevent.dispatcher.enabled", true))
        {
            MUCEventDispatcher.removeListener(this);
        }
    }

    private static final SecurityHandler basicAuth(String realm) {

        OpenfireLoginService l = new OpenfireLoginService(realm);
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"ofmeet", "webapp-owner", "webapp-contributor", "warfile-admin"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName(realm);
        csh.addConstraintMapping(cm);
        csh.setLoginService(l);

        return csh;
    }

    private void checkRecordingsFolder()
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

            File recordingsHome = new File(ofmeetHome + File.separator + "recordings");

            if(!recordingsHome.exists())
            {
                recordingsHome.mkdirs();

            }

            lines = Arrays.asList("Move on, nothing here....");
            file = Paths.get(recordingsHome + File.separator + "index.html");
            Files.write(file, lines, Charset.forName("UTF-8"));
        }
        catch (Exception e)
        {
            Log.error("checkDownloadFolder", e);
        }
    }

    // -------------------------------------------------------
    //
    //  MUCEventListener
    //
    // -------------------------------------------------------

    public void roomCreated(JID roomJID)
    {

    }

    public void roomDestroyed(JID roomJID)
    {

    }

    public void occupantJoined(JID roomJID, JID user, String nickname)
    {

    }

    public void occupantLeft(JID roomJID, JID user)
    {

    }

    public void nicknameChanged(JID roomJID, JID user, String oldNickname, String newNickname)
    {

    }

    public void messageReceived(JID roomJID, JID user, String nickname, Message message)
    {
        if (JiveGlobals.getBooleanProperty("pade.room.activity.indicator", true))
        {
            final String body = message.getBody();
            final String roomJid = roomJID.toString();
            final String userJid = user.toBareJID();

            if (body != null)
            {
                Log.debug("MUC messageReceived " + roomJID + " " + user + " " + nickname + "\n" + message.getBody());

                try {
                    for ( MultiUserChatService mucService : XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatServices() )
                    {
                        MUCRoom room = mucService.getChatRoom(roomJID.getNode());

                        for (JID jid : room.getOwners())
                        {
                            notifyRoomSubscribers(jid, room, roomJID);
                        }

                        for (JID jid : room.getAdmins())
                        {
                            notifyRoomSubscribers(jid, room, roomJID);
                        }

                        for (JID jid : room.getMembers())
                        {
                            notifyRoomSubscribers(jid, room, roomJID);
                        }

                    }
                } catch (Exception e) {
                    Log.error("messageReceived", e);
                }
            }
        }
    }

    public void roomSubjectChanged(JID roomJID, JID user, String newSubject)
    {

    }

    public void privateMessageRecieved(JID a, JID b, Message message)
    {

    }

    private void notifyRoomSubscribers(JID subscriberJID, MUCRoom room, JID roomJID)
    {
        Log.debug("notifyRoomSubscribers " + subscriberJID + " " + roomJID);

        try {
            if (GroupJID.isGroup(subscriberJID)) {
                Group group = GroupManager.getInstance().getGroup(subscriberJID);

                for (JID groupMemberJID : group.getAll()) {
                    notifyRoomActivity(groupMemberJID, room, roomJID);
                }
            } else {
                notifyRoomActivity(subscriberJID, room, roomJID);
            }

        } catch (GroupNotFoundException gnfe) {
            Log.warn("Invalid group JID in the member list: " + subscriberJID);
        }
    }

    private void notifyRoomActivity(JID subscriberJID, MUCRoom room, JID roomJID)
    {
        if (room.getAffiliation(subscriberJID) != MUCRole.Affiliation.none)
        {
            Log.debug("notifyRoomActivity checking " + subscriberJID + " " + roomJID);
            boolean inRoom = true;

            try {
                List<MUCRole> roles = room.getOccupantsByBareJID(subscriberJID);

                if (roles.size() > 1 && roles.get(0).getPresence().isAvailable() == false)
                {
                    inRoom = false;
                }

            } catch (Exception e) {
                inRoom = false;
            }

            Log.debug("notifyRoomActivity confirmed " + subscriberJID + " " + roomJID + " " + inRoom);

            if (!inRoom)
            {
                Message message = new Message();
                message.setFrom(roomJID);
                message.setTo(subscriberJID);
                Element rai = message.addChildElement("rai", "xmpp:prosody.im/protocol/rai");
                rai.addElement("activity").setText(roomJID.toString());
                XMPPServer.getInstance().getRoutingTable().routePacket(subscriberJID, message, true);
            }
        }
    }
}
