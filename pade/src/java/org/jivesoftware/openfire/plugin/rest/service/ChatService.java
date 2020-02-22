package org.jivesoftware.openfire.plugin.rest.service;

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.*;

import javax.servlet.http.HttpServletRequest;
import javax.annotation.PostConstruct;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;

import java.security.Principal;

import javax.xml.bind.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.xc.*;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.util.*;

import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;

import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;

import org.jivesoftware.openfire.plugin.rest.*;
import org.jivesoftware.openfire.plugin.rest.BasicAuth;
import org.jivesoftware.openfire.plugin.rest.entity.RosterEntities;
import org.jivesoftware.openfire.plugin.rest.entity.RosterItemEntity;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCChannelType;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntity;
import org.jivesoftware.openfire.plugin.rest.entity.OccupantEntities;
import org.jivesoftware.openfire.plugin.rest.entity.ParticipantEntities;
import org.jivesoftware.openfire.plugin.rest.entity.WorkgroupEntities;
import org.jivesoftware.openfire.plugin.rest.entity.AssistQueues;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomMessageEntities;

import org.jivesoftware.openfire.user.*;
import org.jivesoftware.openfire.SharedGroupException;
import org.jivesoftware.openfire.user.UserNotFoundException;

import org.jivesoftware.openfire.auth.*;
import org.jivesoftware.openfire.sip.sipaccount.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmpp.packet.*;
import org.jivesoftware.openfire.archive.*;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import de.mxro.process.*;

import org.ifsoft.sso.Password;
import org.jitsi.util.OSUtils;
import org.jivesoftware.openfire.archive.ConversationPDFServlet;

@Path("restapi/v1/chat")
public class ChatService {

    private static final Logger Log = LoggerFactory.getLogger(ChatService.class);
    private XMPPServer server;
    private UserServiceController userService;

    @Context
    private HttpServletRequest httpRequest;

    @PostConstruct
    public void init()
    {
        server = XMPPServer.getInstance();
        userService = UserServiceController.getInstance();
    }

    //-------------------------------------------------------
    //
    //  certificate/enroll
    //
    //-------------------------------------------------------

    @GET
    @Path("/certificate")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCertificate() throws ServiceException
    {
        String c2sTrustStoreLocation = JiveGlobals.getHomeDirectory() + File.separator + "resources" + File.separator + "security" + File.separator;
        String base64String = httpRequest.getHeader("authorization");
        String certsLocation = JiveGlobals.getHomeDirectory() + File.separator + "certificates";
        String openSslPath = JiveGlobals.getProperty("ofchat.openssl.path", null);

        Log.debug("getCertificate " + base64String);

        if (base64String != null)
        {
            String[] usernameAndPassword = BasicAuth.decode(base64String);

            if (usernameAndPassword != null && usernameAndPassword.length == 2)
            {
                try {
                    // /usr/bin/openssl
                    String aliasHome = checkCertificatesFolder(usernameAndPassword[0]);
                    String pfxFile = aliasHome + File.separator + usernameAndPassword[0] + ".pfx";

                    if (!(new File(pfxFile)).exists())
                    {
                        if (openSslPath == null)
                        {
                            throw new ServiceException("Exception", "OpenSSL not configured", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                        }

                        String jid = usernameAndPassword[0] + "@" + server.getServerInfo().getXMPPDomain();

                        if (aliasHome != null)
                        {
                            String command1 = openSslPath + " req -new -newkey rsa:4096 -days 365 -nodes -x509 -keyout " + usernameAndPassword[0] + ".key -out " + usernameAndPassword[0] + ".crt -config " + usernameAndPassword[0] + ".cnf -extensions v3_extensions -subj \"/CN=" + jid + "\"";
                            String out1 = Spawn.runCommand(command1, new File(aliasHome));
                            Log.debug(command1 + "\n" + out1);

                            String command2 = openSslPath + " pkcs12 -export -inkey " + usernameAndPassword[0] + ".key -in " + usernameAndPassword[0] + ".crt -out " + usernameAndPassword[0] + ".pfx -passout pass:" + usernameAndPassword[1] + " -passin pass:" + usernameAndPassword[1];
                            String out2 = Spawn.runCommand(command2, new File(aliasHome));
                            Log.debug(command2 + "\n" + out2);
                        }
                    }

                    if (!(new File(pfxFile)).exists())
                    {
                        throw new ServiceException("Exception", "certificate creation failed", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                    }

                    return Response.ok(new FileInputStream(pfxFile)).type("application/x-pkcs12").build();

                } catch (Exception e) {
                    throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                }
            }
        }

        throw new ServiceException("Exception", "Access denied", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
    }

    @GET
    @Path("/enroll")
    public String getTotpQrCode() throws ServiceException
    {
        String base64String = httpRequest.getHeader("authorization");
        Log.debug("addTotp " + base64String);

        if (base64String != null)
        {
            String[] usernameAndPassword = BasicAuth.decode(base64String);

            if (usernameAndPassword != null && usernameAndPassword.length == 2)
            {
                try {
                    User user = userService.getUser(usernameAndPassword[0]);
                    String base32Secret = user.getProperties().get("ofchat.totp.secret");

                    if (base32Secret == null)
                    {
                        base32Secret = TimeBasedOneTimePasswordUtil.generateBase32Secret();
                        user.getProperties().put("ofchat.totp.secret", base32Secret);
                    }
                    return TimeBasedOneTimePasswordUtil.qrImageUrl(usernameAndPassword[0] + "@" + server.getServerInfo().getXMPPDomain(), base32Secret);

                } catch (Exception e) {
                    throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                }
            }
        }
        throw new ServiceException("Exception", "Access denied", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
    }


    @DELETE
    @Path("/enroll")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteTotpQr() throws ServiceException
    {
        String base64String = httpRequest.getHeader("authorization");
        Log.debug("deleteTotpQr " + base64String);

        if (base64String != null)
        {
            String[] usernameAndPassword = BasicAuth.decode(base64String);

            if (usernameAndPassword != null && usernameAndPassword.length == 2)
            {
                try {
                    User user = userService.getUser(usernameAndPassword[0]);
                    String base32Secret = user.getProperties().get("ofchat.totp.secret");

                    if (base32Secret != null)
                    {
                        user.getProperties().remove("ofchat.totp.secret");
                    }
                    return Response.status(Response.Status.OK).build();

                } catch (Exception e) {
                    throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                }
            }
        }

        throw new ServiceException("Exception", "Access denied", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
    }

    @GET
    @Path("/{username}/pdf")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getConversationsPdf(@PathParam("username") String username, @QueryParam("keywords") String keywords, @QueryParam("to") String to, @QueryParam("start") String start, @QueryParam("end") String end, @QueryParam("room") String room, @DefaultValue("conference") @QueryParam("service") String service, String password) throws ServiceException
    {
        StreamingOutput output = new StreamingOutput()
        {
            @Override public void write(OutputStream out)
            {
                ConversationPDFServlet.doSearch(out, username, keywords, to, start, end, room, service);
            }
        };
        return Response.ok(output).build();
    }

    @GET
    @Path("/{username}/messages")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Conversations getConversations(@PathParam("username") String username, @QueryParam("keywords") String keywords, @QueryParam("to") String to, @QueryParam("start") String start, @QueryParam("end") String end, @QueryParam("room") String room, @DefaultValue("conference") @QueryParam("service") String service, String password) throws ServiceException
    {
        Log.debug("getConversations " + username + " " + keywords + " " + " " + to  + " " + start + " " + end + " " + room + " " + service);

        try {
            // can be done out of band
            // authentication required. secret, admin user or authenicated user

            String token = httpRequest.getHeader("authorization");

            if (token != null)
            {
                String[] usernameAndPassword = BasicAuth.decode(token.substring(6));
                usernameAndPassword = BasicAuth.decode(token);

                if (usernameAndPassword == null || usernameAndPassword.length != 2 || !usernameAndPassword[0].equals(username))
                {
                    throw new ServiceException("Exception", "not authorized", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                }

            } else {
                authenticate( username, password );
            }

            ArchiveSearch search = new ArchiveSearch();
            JID participant1JID = makeJid(username);
            JID participant2JID = null;

            if (to != null) participant2JID = makeJid(to);

            if (participant2JID != null) {
                search.setParticipants(participant1JID, participant2JID);
            } else  {
                search.setParticipants(participant1JID);
            }

            if (start != null)
            {
                Date startDate = null;

                try {
                    if (start.contains("T"))
                    {
                        startDate = Date.from(Instant.parse(start));
                    }
                    else {
                        DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                        startDate = formatter.parse(start);
                    }
                    startDate = new Date(startDate.getTime() - JiveConstants.MINUTE * 5);
                    search.setDateRangeMin(startDate);
                }
                catch (Exception e) {
                    Log.error("ConversationPDFServlet", e);
                }
            }

            if (end != null)
            {
                Date endDate = null;

                try {
                    if (end.contains("T"))
                    {
                        endDate = Date.from(Instant.parse(end));
                    }
                    else {
                        DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                        endDate = formatter.parse(end);
                    }
                    endDate = new Date(endDate.getTime() + JiveConstants.DAY - 1);
                    search.setDateRangeMax(endDate);
                }
                catch (Exception e) {
                    Log.error("ConversationPDFServlet", e);
                }
            }

            if (keywords != null) search.setQueryString(keywords);

            if (service == null) service = "conference";

            if (room != null)
            {
                search.setRoom(new JID(room + "@" + service + "." + server.getServerInfo().getXMPPDomain()));
            }

            search.setSortOrder(ArchiveSearch.SortOrder.ascending);

            Collection<Conversation> conversations = new ArchiveSearcher().search(search);
            Collection<Conversation> list = new ArrayList<Conversation>();

            for (Conversation conversation : conversations)
            {
                list.add(conversation);
            }

            return new Conversations(list);

        } catch (Exception e) {
            Log.error("getConversations", e);
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
    }
    //-------------------------------------------------------
    //
    //  Search for users. CRUD user profile (properties)
    //
    //-------------------------------------------------------

    @GET
    @Path("/users")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserEntities getUser(@QueryParam("search") String search) throws ServiceException
    {
        return userService.getUsersBySearch(search);
    }

    //-------------------------------------------------------
    //
    //  get, join, leave, post message chat rooms
    //
    //-------------------------------------------------------

    @GET
    @Path("/rooms")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomEntities getMUCRooms(@DefaultValue("conference") @QueryParam("servicename") String serviceName, @DefaultValue(MUCChannelType.PUBLIC) @QueryParam("type") String channelType, @QueryParam("search") String roomSearch, @DefaultValue("false") @QueryParam("expandGroups") Boolean expand)
    {
        return MUCRoomController.getInstance().getChatRooms(serviceName, channelType, roomSearch, expand);
    }

    @GET
    @Path("/rooms/{roomName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomEntity getMUCRoomJSON2(@PathParam("roomName") String roomName, @DefaultValue("conference") @QueryParam("servicename") String serviceName, @DefaultValue("false") @QueryParam("expandGroups") Boolean expand) throws ServiceException
    {
        return MUCRoomController.getInstance().getChatRoom(roomName, serviceName, expand);
    }

    @GET
    @Path("/rooms/{roomName}/participants")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ParticipantEntities getMUCRoomParticipants(@PathParam("roomName") String roomName, @DefaultValue("conference") @QueryParam("servicename") String serviceName)
    {
        return MUCRoomController.getInstance().getRoomParticipants(roomName, serviceName);
    }

    @GET
    @Path("/rooms/{roomName}/occupants")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OccupantEntities getMUCRoomOccupants(@PathParam("roomName") String roomName, @DefaultValue("conference") @QueryParam("servicename") String serviceName)
    {
        return MUCRoomController.getInstance().getRoomOccupants(roomName, serviceName);
    }

    @GET
    @Path("/rooms/{roomName}/chathistory")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomMessageEntities getMUCRoomHistory(@PathParam("roomName") String roomName, @DefaultValue("conference") @QueryParam("servicename") String serviceName) throws ServiceException
    {
        return MUCRoomController.getInstance().getRoomHistory(roomName, serviceName);
    }

    //-------------------------------------------------------
    //
    //  Utitlities
    //
    //-------------------------------------------------------

    private JID makeJid(String participant1)
    {
        JID participant1JID = null;

        try {
            int position = participant1.lastIndexOf("@");

            if (position > -1) {
                String node = participant1.substring(0, position);
                participant1JID = new JID(JID.escapeNode(node) + participant1.substring(position));
            } else {
                participant1JID = new JID(JID.escapeNode(participant1), server.getServerInfo().getXMPPDomain(), null);
            }
        } catch (Exception e) {
            Log.error("makeJid", e);
        }
        return participant1JID;
    }

    private Object jsonToObject(String json, Class objectClass)
    {
        Object object = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            mapper.setAnnotationIntrospector(introspector);
            object = mapper.readValue(json, objectClass);

        } catch (Exception e) {
            Log.error("jsonToObject", e);
        }

        Log.debug("jsonToObject\n" + json + "\nObject= " + object);
        return object;
    }

    private String objectToJson(Object object)
    {
        String json = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            mapper.setAnnotationIntrospector(introspector);
            json = mapper.writeValueAsString(object);

        } catch (Exception e) {
            Log.error("objectToJson", e);
        }

        Log.debug("objectToJson\n" + json + "\nObject= " + object);
        return json;
    }

    private String checkCertificatesFolder(String alias)
    {
        String jid = alias + "@" + server.getServerInfo().getXMPPDomain();
        String certificatesHome = JiveGlobals.getHomeDirectory() + File.separator + "certificates";
        String aliasHome = certificatesHome + File.separator + alias;

        try
        {
            File certificatesFolderPath = new File(certificatesHome);
            if (!certificatesFolderPath.exists()) certificatesFolderPath.mkdirs();

            File aliasFolderPath = new File(aliasHome);
            if (!aliasFolderPath.exists()) aliasFolderPath.mkdirs();

            List<String> lines = Arrays.asList("[req]", "x509_extensions = v3_extensions", "req_extensions = v3_extensions", "distinguished_name = distinguished_name", "[v3_extensions]", "extendedKeyUsage = clientAuth", "keyUsage = digitalSignature,keyEncipherment", "basicConstraints = CA:FALSE", "subjectAltName = @subject_alternative_name", "[subject_alternative_name]", "otherName.0 = 1.3.6.1.5.5.7.8.5;UTF8:" + jid, "[distinguished_name]", "commonName = " + jid);
            java.nio.file.Path file = java.nio.file.Paths.get(aliasHome + File.separator + alias + ".cnf");


            Files.write(file, lines, Charset.forName("UTF-8"));

            return aliasHome;
        }
        catch (Exception e)
        {
            Log.error("checkCertificatesFolder", e);
        }
        return null;
    }

    private void authenticate(String username, String password) throws Exception
    {
        Log.debug("authenticate " + username + " " + Password.passwords.get(username));

        if (Password.passwords.containsKey(username))     // SSO
        {
            String passkey = Password.passwords.get(username).trim();

            if (password.trim().equals(passkey) == false)
            {
                throw new ServiceException("Exception", "not authorized", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }
            return;
        }
        else AuthFactory.authenticate( username, password );
    }
}
