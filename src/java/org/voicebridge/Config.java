package org.voicebridge;

import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.net.InetAddress;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.util.*;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;

import org.xmpp.packet.*;
import org.ifsoft.oju.openfire.MUCRoomProperties;


public class Config implements MUCEventListener {

    private static final Logger Log = LoggerFactory.getLogger(Config.class);
    private static Cache muc_properties = CacheFactory.createLocalCache("MUC Room Properties");
	
	private MultiUserChatManager mucManager;
	private HashMap<String, Conference> conferences;
	private HashMap<String, Conference> confExtensions;

	private ArrayList<ProxyCredentials> registrations;
	private ArrayList<String> registrars;
	private HashMap<String, ProxyCredentials> sipExtensions;

	private static Config singletonConfig;
	public static boolean sipPlugin = false;
	private String hostname = XMPPServer.getInstance().getServerInfo().getHostname();

	private String minPort = JiveGlobals.getProperty("org.jitsi.videobridge.media.MIN_PORT_NUMBER", "26001");
	private String maxPort = JiveGlobals.getProperty("org.jitsi.videobridge.media.MAX_PORT_NUMBER", "27000");	
	private String privateHost = JiveGlobals.getProperty("org.ice4j.ice.harvest.NAT_HARVESTER_PRIVATE_ADDRESS", hostname);
	private String publicHost = JiveGlobals.getProperty("org.ice4j.ice.harvest.NAT_HARVESTER_PUBLIC_ADDRESS", hostname);
	private String defaultProtocol = JiveGlobals.getProperty("ofmeet.jigasi.sip.transport", "tcp").toLowerCase();
	private String defaultSIPPort = JiveGlobals.getProperty("ofmeet.jigasi.proxy.port", "5067");

    private boolean prefixPhoneNumber = true;
    private String internationalPrefix = "00";  // for international calls
    private String longDistancePrefix = "0";  	// for long Distancee
    private String outsideLinePrefix = "9";  	// for outside line
    private int internalExtenLength = 5;


	private Config() {

	}

	public void initialise()
	{
		conferences 	= new HashMap<String, Conference>();
		confExtensions 	= new HashMap<String, Conference>();
		sipExtensions 	= new HashMap<String, ProxyCredentials>();

		registrations = new ArrayList<ProxyCredentials>();
    	registrars = new ArrayList<String>();

    	MUCEventDispatcher.addListener(this);

		try {
			Log.info(String.format("VoiceBridge read site configuration"));

			mucManager 	= XMPPServer.getInstance().getMultiUserChatManager();

			if (mucManager.getMultiUserChatService("conference") != null)
			{
				List<MUCRoom> rooms = mucManager.getMultiUserChatService("conference").getChatRooms();

				for (MUCRoom room : rooms)
				{
					if (!"ofmeet".equals(room.getJID().getNode()) && !"ofgasi".equals(room.getJID().getNode()))
					{
						createConference(room);
					}
				}
			}

			String sipUserid = JiveGlobals.getProperty("ofmeet.jigasi.sip.user-id", "jigasi@" + getPrivateHost()).split("@")[0];	
			String username = JiveGlobals.getProperty("ofmeet.jigasi.xmpp.user-id", "jigasi");							

			if (JiveGlobals.getBooleanProperty("ofmeet.audiobridge.register.all", false))
			{
				processRegistrations();

			} else {
				processDefaultRegistration(username);
			}

			Log.info(String.format("Audiobridge is available"));

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	public void terminate()
	{
		MUCEventDispatcher.removeListener(this);
	}


    // ------------------------------------------------------------------------
    //
    // Registrations management
    //
    // ------------------------------------------------------------------------


	private void processDefaultRegistration(String username)
	{
		Log.info("Audiobridge process SIP registration " + username);	
		
		String sql = "SELECT username, sipusername, sipauthuser, sipdisplayname, sippassword, sipserver, enabled, status, stunserver, stunport, usestun, voicemail, outboundproxy, promptCredentials FROM ofSipUser WHERE USERNAME = '" + username + "'";
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DbConnectionManager.getConnection();
			pstmt = DbConnectionManager.createScrollablePreparedStatement(con, sql);
			ResultSet rs = pstmt.executeQuery();
			DbConnectionManager.scrollResultSet(rs, 0);

			if (rs.next())
			{
				sipPlugin = true;				
				ProxyCredentials credentials = read(rs);
				credentials.setName(username);

				try {
					InetAddress inetAddress = InetAddress.getByName(credentials.getHost());
					registrars.add(credentials.getHost());
					registrations.add(credentials);

					Log.info(String.format("VoiceBridge adding SIP registration: %s with user %s host %s", credentials.getXmppUserName(), credentials.getUserName(), credentials.getHost()));

				} catch (Exception e) {
					Log.info(String.format("processDefaultRegistration Bad Address  %s ", credentials.getHost()));
				}
			}
			rs.close();
			pstmt.close();
			con.close();

		} catch (SQLException e) {
			Log.info("processDefaultRegistration " + e);
		}
	}

	private void processRegistrations()
	{
		Log.info("Audiobridge process all SIP registrations");		
		
		String sql = "SELECT username, sipusername, sipauthuser, sipdisplayname, sippassword, sipserver, enabled, status, stunserver, stunport, usestun, voicemail, outboundproxy, promptCredentials FROM ofSipUser ORDER BY USERNAME";
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DbConnectionManager.getConnection();
			pstmt = DbConnectionManager.createScrollablePreparedStatement(con, sql);
			ResultSet rs = pstmt.executeQuery();
			DbConnectionManager.scrollResultSet(rs, 0);

			while (rs.next())
			{
				sipPlugin = true;				
				ProxyCredentials credentials = read(rs);

				try {
					InetAddress inetAddress = InetAddress.getByName(credentials.getHost());
					registrars.add(credentials.getHost());
					registrations.add(credentials);

					Log.info(String.format("Audiobridge adding SIP registration: %s with user %s host %s", credentials.getXmppUserName(), credentials.getUserName(), credentials.getHost()));

				} catch (Exception e) {
					Log.info(String.format("processRegistrations Bad Address  %s ", credentials.getHost()));
				}
			}
			rs.close();
			pstmt.close();
			con.close();


		} catch (SQLException e) {
			Log.info("processRegistrations " + e);
		}
	}

	private ProxyCredentials read(ResultSet rs)
	{
		ProxyCredentials sipAccount = new ProxyCredentials();

		try {
			String username = rs.getString("username");
			String sipusername = rs.getString("sipusername");
			String authusername = rs.getString("sipauthuser");
			String displayname = rs.getString("sipdisplayname");
			String password = rs.getString("sippassword");
			String server = rs.getString("sipserver");
			String stunServer = rs.getString("stunserver");
			String stunPort = rs.getString("stunport");
            String voicemail = rs.getString("voicemail");
            String outboundproxy = rs.getString("outboundproxy");

            sipAccount.setName(username);
			sipAccount.setXmppUserName(username);
			sipAccount.setUserName(sipusername);
			sipAccount.setAuthUserName(authusername);
			sipAccount.setUserDisplay(displayname);
			sipAccount.setPassword(password.toCharArray());
			sipAccount.setHost(server);
            sipAccount.setProxy(outboundproxy);
            sipAccount.setRealm(server);

            sipExtensions.put(username, sipAccount);

        } catch (SQLException e) {
			Log.info("ProxyCredentials " + e);
		}

		return sipAccount;
	}

	public static void updateStatus(String username, String status) throws SQLException {

		if (sipPlugin)
		{
			String sql = "UPDATE ofSipUser SET status = ?, enabled = ? WHERE username = ?";

			Connection con = null;
			PreparedStatement psmt = null;

			try {

				con = DbConnectionManager.getConnection();
				psmt = con.prepareStatement(sql);

				psmt.setString(1, status);
				psmt.setInt(2, 1);
				psmt.setString(3, username);

				psmt.executeUpdate();


			} catch (SQLException e) {
				Log.info("updateStatus " + e);

			} finally {
				DbConnectionManager.closeConnection(psmt, con);
			}
		}
	}

    public static void createCallRecord(String username, String addressFrom, String addressTo, long datetime, int duration, String calltype)  {

		if (sipPlugin)
		{
			Log.info("createCallRecord " + username + " " + addressFrom + " " + addressTo + " " + datetime);

			String sql = "INSERT INTO ofSipPhoneLog (username, addressFrom, addressTo, datetime, duration, calltype) values  (?, ?, ?, ?, ?, ?)";

			Connection con = null;
			PreparedStatement psmt = null;
			ResultSet rs = null;

			try {
				con = DbConnectionManager.getConnection();
				psmt = con.prepareStatement(sql);
				psmt.setString(1, username);
				psmt.setString(2, addressFrom);
				psmt.setString(3, addressTo);
				psmt.setLong(4, datetime);
				psmt.setInt(5, duration);
				psmt.setString(6, calltype);

				psmt.executeUpdate();

			} catch (SQLException e) {
				Log.error(e.getMessage(), e);
			} finally {
				DbConnectionManager.closeConnection(rs, psmt, con);
			}
		}
    }

	public static void updateCallRecord(long datetime, int duration) {

		if (sipPlugin)
		{
			Log.info("updateCallRecord " + datetime + " " + duration);

			String sql = "UPDATE ofSipPhoneLog SET duration = ? WHERE datetime = ?";

			Connection con = null;
			PreparedStatement psmt = null;

			try {

				con = DbConnectionManager.getConnection();
				psmt = con.prepareStatement(sql);

				psmt.setInt(1, duration);
				psmt.setLong(2, datetime);
				psmt.executeUpdate();


			} catch (SQLException e) {
				Log.error(e.getMessage(), e);

			} finally {
				DbConnectionManager.closeConnection(psmt, con);
			}
		}
	}

    public ProxyCredentials getProxyCredentialsByUser(String username)
    {
		ProxyCredentials sip = null;

		if (sipExtensions.containsKey(username))
		{
			sip = sipExtensions.get(username);
		}

		return sip;
    }


    // ------------------------------------------------------------------------
    //
    // Conference management
    //
    // ------------------------------------------------------------------------


	private void createConference(MUCRoom room)
	{
		Conference conference = new Conference();
		conference.id = room.getJID().getNode();		
		
		Map<String, String> props = (Map<String, String>) muc_properties.get(room.getJID().toString());
		if (props == null) props = new MUCRoomProperties(room.getID());
		
		if (props != null)
		{
			conference.pin = props.get("PIN");	
			conference.exten = props.get("EXTEN");	
		}

		if (conference.exten != null && conference.exten.length() > 0)
		{
			confExtensions.put(conference.exten, conference);
		}

		conferences.put(conference.id, conference);

		Log.info(String.format("Audiobridge add conference: %s with pin %s extension %s", conference.id, conference.pin, conference.exten));
	}

	private void destroyConference(MUCRoom room)
	{
		if (conferences.containsKey(room.getName()))
		{
			Conference conference1 = conferences.remove(room.getName());
			conference1 = null;

			Conference conference2 = confExtensions.remove(room.getName());
			conference2 = null;

			Log.info(String.format("Audiobridge destroy conference: %s", room.getName()));
		}
	}

	public static Config getInstance() {

		if (singletonConfig == null) {

			singletonConfig = new Config();
		}

		return singletonConfig;
	}

	public boolean isValidConference(String id)
	{
		return conferences.containsKey(id);
	}

	public boolean isValidConferenceExten(String id)
	{
		return confExtensions.containsKey(id);
	}

	public boolean isValidConferencePin(String id, String pin)
	{
		boolean valid = false;

		if (conferences.containsKey(id))
		{
			Conference conf = conferences.get(id);
			valid = conf.pin == null || pin.equals(conf.pin);
		}

		return valid;
	}

    public Conference getConferenceByPhone(String phoneNo)
    {
		Log.debug("getConferenceByPhone" + phoneNo + " " + conferences.keySet());
		
		Conference conf = null;

		if (conferences.containsKey(phoneNo))
		{
			conf = conferences.get(phoneNo);

		} else if (confExtensions.containsKey(phoneNo))	{

			conf = confExtensions.get(phoneNo);
		}

		return conf;
    }

    public String getMeetingCode(String phoneNo)
    {
		String id = null;

		if (conferences.containsKey(phoneNo))
		{
			Conference conf = conferences.get(phoneNo);
			id = conf.id;

		} else if (confExtensions.containsKey(phoneNo))	{

			Conference conf = confExtensions.get(phoneNo);
			id = conf.id;
		}

		return id;
    }

    public String getPassCode(String meetingId, String phoneNo)
    {
		String pin = null;

		if (confExtensions.containsKey(phoneNo))
		{
			Conference conf = confExtensions.get(phoneNo);
			pin = conf.pin;

		} else if (conferences.containsKey(meetingId)) {

			Conference conf = conferences.get(meetingId);
			pin = conf.pin;
		}

		return pin;
    }
	
	public boolean isLoggingEnabled()
	{
		return JiveGlobals.getBooleanProperty("ofmeet.audiobridge.logging.enabled", false);
	}

	public String getMinPort()
	{
		return minPort;
	}
	
	public String getMaxPort()
	{
		return maxPort;
	}
	
	public String getPrivateHost()
	{
        try {
            return InetAddress.getByName(privateHost).getHostAddress();
        } catch (Exception e) {
			return privateHost;
        }
	}

	public String getPublicHost()
	{
        try {
            return InetAddress.getByName(publicHost).getHostAddress();
        } catch (Exception e) {
			return publicHost;
        }		
	}

    public void setInternalExtenLength(int internalExtenLength)
    {
		this.internalExtenLength = internalExtenLength;
    }

    public int getInternalExtenLength()
    {
		return internalExtenLength;
    }

    public void setOutsideLinePrefix(String outsideLinePrefix)
    {
		this.outsideLinePrefix = outsideLinePrefix;
    }

    public String getOutsideLinePrefix()
    {
		return outsideLinePrefix;
    }

    public void setLongDistancePrefix(String longDistancePrefix)
    {
		this.longDistancePrefix = longDistancePrefix;
    }

    public String getLongDistancePrefix()
    {
		return longDistancePrefix;
    }


    public void setInternationalPrefix(String internationalPrefix)
    {
		this.internationalPrefix = internationalPrefix;
    }

    public String getInternationalPrefix()
    {
		return internationalPrefix;
    }


    public void setPrefixPhoneNumber(boolean prefixPhoneNumber)
    {
		this.prefixPhoneNumber = prefixPhoneNumber;
    }

    public boolean prefixPhoneNumber()
    {
		return prefixPhoneNumber;
    }

	public String getDefaultProtocol()
	{
		return defaultProtocol;
	}
	public String getDefaultSIPPort()
	{
		return defaultSIPPort;
	}

	public ArrayList<String> getRegistrars()
	{
		return registrars;
	}


	public ArrayList<ProxyCredentials> getRegistrations()
	{
		return registrations;
	}

    public String formatPhoneNumber(String phoneNumber, String location)
    {
		if (phoneNumber == null) {
			return null;
		}
		/*
		 * It's a softphone number.  Leave it as is.
		 */

		if (phoneNumber.indexOf("sip:") == 0)
		{
			/*
			 * There is a problem where Meeting Central gives
			 * us a phone number with only "sip:" which isn't valid.
			 * Check for that here.
			 * XXX
			 */
			if (phoneNumber.length() < 5) {
			return null;
			}

			return phoneNumber;
		}

		if (phoneNumber.indexOf("@") >= 0)
		{
			return "sip:" + phoneNumber;
		}

		/*
		 * If number starts with "Id-" it's a callId.  Leave it as is.
		 */

		if (phoneNumber.indexOf("Id-") == 0)
		{
			return phoneNumber;
		}

        /*
         * Get rid of white space in the phone number
         */

        phoneNumber = phoneNumber.replaceAll("\\s", "");

        /*
         * Get rid of "-" in the phone number
         */

        phoneNumber = phoneNumber.replaceAll("-", "");

		/*
		 * For Jon Kaplan who likes to use "." as a phone number separator!
		 */

		phoneNumber = phoneNumber.replaceAll("\\.", "");

		if (phoneNumber.length() == 0)
		{
			return null;
		}

		if (prefixPhoneNumber == false) {
			return phoneNumber;
		}

		/*
		 * Replace leading "+" (from namefinder) with appropriate numbers.
		 * +1 is a US number and becomes outsideLinePrefix.
		 * +<anything else> is considered to be an international number and
		 * becomes internationalPrefix.
		 */

		if (phoneNumber.charAt(0) == '+')
		{
			if (phoneNumber.charAt(1) == '1')
			{
				phoneNumber = outsideLinePrefix + phoneNumber.substring(1);

			} else {
				phoneNumber = outsideLinePrefix + internationalPrefix + phoneNumber.substring(1);
			}

		} else if (phoneNumber.charAt(0) == 'x' || phoneNumber.charAt(0) == 'X') {

			phoneNumber = phoneNumber.substring(1);
		}

		if (phoneNumber.length() == internalExtenLength)
		{
			/*
			 * This is an internal extension.  Determine if it needs
			 * a prefix of "70".
			 */
	   		//phoneNumber = PhoneNumberPrefix.getPrefix(location) + phoneNumber;

        } else if (phoneNumber.length() > 7) {
            /*
             * It's an outside number
             *
             * XXX No idea what lengths of 8 and 9 would be for...
             */
            if (phoneNumber.length() == 10)
            {
                /*
                 * It's US or Canada, number needs 91
                 */
                phoneNumber = outsideLinePrefix + longDistancePrefix + phoneNumber;

            } else if (phoneNumber.length() >= 11) {
                /*
                 * If it starts with 9 or 1, it's US or Canada.
                 * Otherwise, it's international.
                 */
                if (phoneNumber.length() == 11 && longDistancePrefix.length() > 0 && phoneNumber.charAt(0) == longDistancePrefix.charAt(0))
                {
                    phoneNumber = outsideLinePrefix + phoneNumber;

                } else if (phoneNumber.length() == 11 && outsideLinePrefix.length() > 0 && phoneNumber.charAt(0) == outsideLinePrefix.charAt(0)) {

                    phoneNumber = outsideLinePrefix + longDistancePrefix + phoneNumber.substring(1);

                } else if (phoneNumber.length() == 12 && phoneNumber.substring(0,2).equals(outsideLinePrefix + longDistancePrefix)) {

                    // nothing to do

                } else {
                    /*
                     * It's international, number needs outsideLinePrefix plus internationalPrefix
                     */
                    if (phoneNumber.substring(0,3).equals(internationalPrefix))
                    {
                        /*
                         * international prefix is already there, just prepend
			 			 * outsideLinePrefix
                         */
                         phoneNumber = outsideLinePrefix + phoneNumber;

                    } else if (!phoneNumber.substring(0,4).equals(outsideLinePrefix + internationalPrefix)) {

                        phoneNumber = outsideLinePrefix + internationalPrefix + phoneNumber;
                    }
                }
            }
        }

        return phoneNumber;
    }

    public void roomCreated(JID roomJID)
    {
		MUCRoom mucRoom = mucManager.getMultiUserChatService(roomJID).getChatRoom(roomJID.getNode());

        if (mucRoom != null)
        {
			createConference(mucRoom);
		}
    }

    public void roomDestroyed(JID roomJID)
    {
		MUCRoom mucRoom = mucManager.getMultiUserChatService(roomJID).getChatRoom(roomJID.getNode());

        if (mucRoom != null)
        {
			destroyConference(mucRoom);
		}
    }

    public void occupantJoined(JID roomJID, JID user, String nickname)
    {

    }

    public void nicknameChanged(JID roomJID, JID user, String oldNickname, String newNickname)
    {

    }

    public void messageReceived(JID roomJID, JID user, String nickname, Message message)
    {

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
	
	
	public class Conference
	{
		public String pin = null;
		public String id = null;
		public String exten = null;

	}
}