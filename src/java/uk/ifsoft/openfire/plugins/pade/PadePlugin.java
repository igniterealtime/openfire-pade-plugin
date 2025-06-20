package uk.ifsoft.openfire.plugins.pade;

import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.net.SASLAuthentication;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.plugin.rest.sasl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.util.security.*;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.*;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;

import org.jivesoftware.openfire.*;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.session.*;
import org.jivesoftware.openfire.group.*;
import org.jivesoftware.util.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.io.File;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.security.Security;

import org.xmpp.packet.*;
import org.dom4j.Element;
import org.igniterealtime.openfire.plugins.pushnotification.WebPushInterceptor;
import org.jivesoftware.openfire.plugin.ofmeet.OfMeetPlugin;

import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.ifsoft.webauthn.UserRegistrationStorage;
import org.ifsoft.sso.Password;

import java.util.concurrent.TimeUnit;

public class PadePlugin implements Plugin, MUCEventListener
{
    private static final Logger Log = LoggerFactory.getLogger( PadePlugin.class );
    public static PadePlugin self;
    public static String webRoot;
    private WebPushInterceptor interceptor;
    private OfMeetPlugin ofMeetPlugin;
	private RelyingParty relyingParty;
	private UserRegistrationStorage userRegistrationStorage;
    private String server;
	private HashMap<String, Object> requests = new HashMap<>();
    private MastodonIQHandler mastodonIQHandler;	
	private boolean isCSPEnabled = true;

    /**
     * Initializes the plugin.
     *
     * @param manager         the plugin manager.
     * @param pluginDirectory the directory where the plugin is located.
     */
    @Override
    public void initializePlugin( final PluginManager manager, final File pluginDirectory )
    {
        self = this;
        webRoot = pluginDirectory.getPath() + "/classes";
		server = XMPPServer.getInstance().getServerInfo().getHostname() + ":" + JiveGlobals.getProperty("httpbind.port.secure", "7443");		

        Log.info("start pade server " + server);

        interceptor = new WebPushInterceptor();
        InterceptorManager.getInstance().addInterceptor( interceptor );
        OfflineMessageStrategy.addListener( interceptor );
		
		isCSPEnabled = HttpBindManager.HTTP_BIND_CONTENT_SECURITY_POLICY_ENABLED.getValue();
		HttpBindManager.HTTP_BIND_CONTENT_SECURITY_POLICY_ENABLED.setValue( false );

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

        Log.info("Starting Openfire Meetings");
        ofMeetPlugin = new OfMeetPlugin();
        ofMeetPlugin.initializePlugin( manager, pluginDirectory );
		
		Log.info("Creating webauthn RelyingParty");
		String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
		Set<String> origins = new HashSet<>();		
		origins.add("https://" + server);
		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder().id(hostname).name("Pade").build();
		userRegistrationStorage = new UserRegistrationStorage();
		relyingParty = RelyingParty.builder().identity(rpIdentity).credentialRepository(userRegistrationStorage).origins(origins).build();	

        if (JiveGlobals.getBooleanProperty("pade.mastodon.enabled", true))  {
			mastodonIQHandler = new MastodonIQHandler();
			mastodonIQHandler.startHandler();		
			XMPPServer.getInstance().getIQRouter().addHandler(mastodonIQHandler);	
			XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeature("urn:xmpp:sfu:mastodon:0");				
		}	
    }
	
	public String startRegisterWebAuthn(String username, String name)
	{
        Log.debug("startRegisterWebAuthn " + username + " " + name);		
		SecureRandom random = new SecureRandom();
		byte[] userHandle = new byte[64];
		random.nextBytes(userHandle);

		PublicKeyCredentialCreationOptions request = relyingParty.startRegistration(StartRegistrationOptions.builder()
			.user(com.yubico.webauthn.data.UserIdentity.builder()
			.name(username).displayName(name).id(new ByteArray(userHandle)).build())
			.build());		

		return requestToJson((PublicKeyCredentialCreationOptions) request, username);		
	}
	
	public RegistrationResult finishRegisterWebAuthn(String responseJson, String username)
	{	
        Log.debug("finishRegisterWebAuthn " + username + "\n" + responseJson);	
		PublicKeyCredentialCreationOptions request = (PublicKeyCredentialCreationOptions) requests.get(username);
		RegistrationResult result = null;	
		
		try {
			PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc = PublicKeyCredential.parseRegistrationResponseJson(responseJson);			
			result = relyingParty.finishRegistration(FinishRegistrationOptions.builder().request(request).response(pkc).build());
			userRegistrationStorage.addCredential(username, request.getUser().getId().getBytes(), result.getKeyId().getId().getBytes(), result.getPublicKeyCose().getBytes());			
			
		} catch (Exception e) {
            Log.error( "finishRegisterWebAuthn exception occurred", e );			
		}

		return result;
	}	

	public String startAuthentication(String username)
	{
        Log.debug("startAuthentication " + username);				
		
		AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
			.username(Optional.of(username))
			.build());
		
		return requestToJson((AssertionRequest) request, username);		
	}
	
	public boolean finishAuthentication(String responseJson, String username)
	{	
        Log.debug("finishAuthentication " + username + "\n" + responseJson);		
		AssertionRequest request = (AssertionRequest) requests.get(username);
		boolean response = false;
		
		try {
			PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc = PublicKeyCredential.parseAssertionResponseJson(responseJson);
			AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder().request(request).response(pkc).build());						
			response = result.isSuccess();
			
		} catch (Exception e) {
           Log.error( "finishAuthentication exception occurred", e );	
		}
		return response;
	}	
	
	private String requestToJson(Object request, String username)
	{				
		ObjectMapper jsonMapper = new ObjectMapper()
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.setSerializationInclusion(Include.NON_ABSENT)
			.registerModule(new Jdk8Module());
		
		String json = "{}";
        try
        {
			json = jsonMapper.writeValueAsString(request);
        }
        catch ( Exception ex )
        {
            Log.error( "registerWebAuthn exception occurred", ex );
        }
		requests.put(username, request);
		return json;		
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
		
		HttpBindManager.HTTP_BIND_CONTENT_SECURITY_POLICY_ENABLED.setValue( isCSPEnabled );		

        if (ofMeetPlugin != null) ofMeetPlugin.destroyPlugin();

        try {
            SASLAuthentication.removeSupportedMechanism( OfChatSaslServer.MECHANISM_NAME );
            Security.removeProvider( OfChatSaslProvider.NAME );
        } catch (Exception e) {}

        if ( JiveGlobals.getBooleanProperty( "pade.mucevent.dispatcher.enabled", true))
        {
            MUCEventDispatcher.removeListener(this);
        }

        OfflineMessageStrategy.removeListener( interceptor );
        InterceptorManager.getInstance().removeInterceptor( interceptor );
		self = null;
		
		XMPPServer.getInstance().getIQRouter().removeHandler(mastodonIQHandler);
		mastodonIQHandler.stopHandler();
		mastodonIQHandler = null;			
    }

    public OfMeetPlugin getContainer()
    {
        return ofMeetPlugin;
    }

    private void checkRecordingsFolder()
    {
        Path resourcesHome = JiveGlobals.getHomePath().resolve("resources").resolve("spank");

        try
        {
            Path ofmeetHome = resourcesHome.resolve("ofmeet-cdn");

            if(!Files.exists(ofmeetHome))
            {
                Files.createDirectory(ofmeetHome);
                List<String> lines = List.of("Move on, nothing here....");
                Path file = ofmeetHome.resolve("index.html");
                Files.write(file, lines, StandardCharsets.UTF_8);
            }

            Path recordingsHome = ofmeetHome.resolve("recordings");

            if(!Files.exists(recordingsHome))
            {
                Files.createDirectory(recordingsHome);

                List<String> lines = List.of("Move on, nothing here....");
                Path file = Paths.get(recordingsHome + File.separator + "index.html");
                Files.write(file, lines, StandardCharsets.UTF_8);
            }

            // create .well-known/host-meta

            Path wellknownFolder = resourcesHome.resolve(".well-known");

            if(!Files.exists(wellknownFolder))
            {
                Files.createDirectory(wellknownFolder);
            }

            List<String> lines = Arrays.asList("<XRD xmlns=\"http://docs.oasis-open.org/ns/xri/xrd-1.0\">", "<Link rel=\"urn:xmpp:alt-connections:xbosh\" href=\"https://" + server + "/http-bind/\"/>", "<Link rel=\"urn:xmpp:alt-connections:websocket\" href=\"wss://" + server + "/ws/\"/>", "</XRD>");
            Path file = wellknownFolder.resolve("host-meta");
            Files.write(file, lines, StandardCharsets.UTF_8);

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

                        if (room != null)
                        {
                            for (JID jid : room.getOwners())
                            {
                                Log.debug("notifyRoomSubscribers owners " + jid + " " + roomJID);
                                notifyRoomSubscribers(jid, room, roomJID, message, nickname, userJid);
                            }

                            for (JID jid : room.getAdmins())
                            {
                                Log.debug("notifyRoomSubscribers admins " + jid + " " + roomJID);
                                notifyRoomSubscribers(jid, room, roomJID, message, nickname, userJid);
                            }

                            for (JID jid : room.getMembers())
                            {
                                Log.debug("notifyRoomSubscribers members " + jid + " " + roomJID);
                                notifyRoomSubscribers(jid, room, roomJID, message, nickname, userJid);
                            }

                            for (MUCOccupant role : room.getModerators())
                            {
                                Log.debug("notifyRoomSubscribers moderators " + role.getUserAddress() + " " + roomJID, message);
                            }

                            for (MUCOccupant role : room.getParticipants())
                            {
                                Log.debug("notifyRoomSubscribers participants " + role.getUserAddress() + " " + roomJID, message);
                            }
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

	public void occupantNickKicked(JID roomJID, String nickname)
	{
		
	}
	
    public void occupantLeft(JID roomJID, JID user, String nickname)
	{
		
	}
	

    public void roomClearChatHistory(long roomID, JID roomJID) {

    }

    public void roomCreated(long roomID, JID roomJID) {

    }
	
    public void roomDestroyed(long roomID, JID roomJID) {

    }	
	
    private void notifyRoomSubscribers(JID subscriberJID, MUCRoom room, JID roomJID, Message message, String nickname, String senderJid)
    {
        try {
            if (GroupJID.isGroup(subscriberJID)) {
                Group group = GroupManager.getInstance().getGroup(subscriberJID);

                for (JID groupMemberJID : group.getAll()) {
                    notifyRoomActivity(groupMemberJID, room, roomJID, message, nickname, senderJid);
                }
            } else {
                notifyRoomActivity(subscriberJID, room, roomJID, message, nickname, senderJid);
            }

        } catch (GroupNotFoundException gnfe) {
            Log.warn("Invalid group JID in the member list: " + subscriberJID);
        }
    }

    private void notifyRoomActivity(JID subscriberJID, MUCRoom room, JID roomJID, Message message, String nickname, String senderJid)
    {
        if (room.getAffiliation(subscriberJID) != Affiliation.none && !senderJid.equals(subscriberJID.toBareJID()))
        {
            Log.debug("notifyRoomActivity checking " + subscriberJID + " " + roomJID);
            boolean inRoom = false;

            try {
                for (MUCOccupant role : room.getOccupants())
                {
                    if (role.getUserAddress().asBareJID().toString().equals(subscriberJID.toString())) inRoom = true;
                }

            } catch (Exception e) {
                inRoom = false;
                Log.error("notifyRoomActivity error", e);
            }

            Log.debug("notifyRoomActivity confirmed " + subscriberJID + " " + roomJID + " " + inRoom);

            if (!inRoom)
            {
                if (XMPPServer.getInstance().getRoutingTable().getRoutes(subscriberJID, null).size() > 0)
                {
                    Log.debug("notifyRoomActivity notifying " + subscriberJID + " " + roomJID);
                    Message notification = new Message();
                    notification.setFrom(roomJID);
                    notification.setTo(subscriberJID);
                    Element rai = notification.addChildElement("rai", "urn:xmpp:rai:0");
                    rai.addElement("activity").setText(roomJID.toString());
                    XMPPServer.getInstance().getRoutingTable().routePacket(subscriberJID, notification);
                }
                else {
                    // user is offline, send web push notification if user mentioned
                    // <reference xmlns='urn:xmpp:reference:0' uri='xmpp:juliet@capulet.lit' begin='72' end='78' type='mention' />

                    Element referenceElement = message.getChildElement("reference", "urn:xmpp:reference:0");
                    boolean mentioned = message.getBody().indexOf(subscriberJID.getNode()) > -1;

                    if (referenceElement != null)
                    {
                        String uri = referenceElement.attribute("uri").getStringValue();

                        if (uri.startsWith("xmpp:") && uri.substring(5).equals(subscriberJID.toString()))
                        {
                            mentioned = true;
                        }
                    }

                    if (mentioned)
                    {
                        try
                        {
                            User user = XMPPServer.getInstance().getUserManager().getUser(subscriberJID.getNode());
                            interceptor.webPush(user, message.getBody(), roomJID, Message.Type.groupchat, nickname );
                            Log.debug( "notifyRoomActivity - notifying mention of " + user.getName());
                        }
                        catch ( UserNotFoundException e )
                        {
                            Log.debug( "notifyRoomActivity - Not a recognized user.", e );
                        }
                    }
                }
            }
        }
    }
}
