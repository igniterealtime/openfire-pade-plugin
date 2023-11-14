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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import org.jitsi.util.OSUtils;

import net.sf.json.*;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;


public class Password extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger(Password.class);
    public static final ConcurrentHashMap<String, String> passwords = new ConcurrentHashMap<>();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String userName = null;

            if (OSUtils.IS_WINDOWS) {
                String sessionName = request.getRemoteUser();
                String windowsName = request.getUserPrincipal().getName();

                if (windowsName != null && sessionName != null && sessionName.equals(windowsName))
                {
                    int pos = windowsName.indexOf("\\");
                    if (pos > -1) userName = windowsName.substring(pos + 1).toLowerCase();
                }

            } else {
                userName = getNtlmUserName(request, response);
            }

            String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
            String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

            Log.debug("Password Servlet: " + userName);

            if (userName != null)
            {
                String password = passwords.get(userName);

                if (password == null)
                {
                    password = TimeBasedOneTimePasswordUtil.generateBase32Secret();
                    passwords.put(userName, password);
                }

                UserManager userManager = XMPPServer.getInstance().getUserManager();
                User user = null;

                try {
                    user = userManager.getUser(userName);
                    Log.debug( "Password servlet: Found user " + userName);
                }
                catch (UserNotFoundException e) {

                    try {
                        Log.debug("Password servlet: Creating user " + userName);
                        user = userManager.createUser(userName, password, null, null);

                        Group group = null;
                        String groupName = JiveGlobals.getProperty("ofchat.winsso.groupname", "winsso");
                        String groupTitle = JiveGlobals.getProperty("ofchat.winsso.groupname", "Windows SSO");

                        try {
                            group = GroupManager.getInstance().getGroup(groupName);

                        } catch (GroupNotFoundException e1) {
                            try {
                                group = GroupManager.getInstance().createGroup(groupName);
                                group.getProperties().put("sharedRoster.showInRoster", "onlyGroup");
                                group.getProperties().put("sharedRoster.displayName", groupTitle);
                                group.getProperties().put("sharedRoster.groupList", "");

                            } catch (Exception e4) {
                                // not possible to create group, just ignore
                            }
                        }

                        if (group != null) group.getMembers().add(XMPPServer.getInstance().createJID(userName, null));
                    }
                    catch (Exception e2) {
                        Log.error("Config servlet: Failed creating user " + userName, e2);
                    }
                }
                catch (Exception e3) {
                    Log.error("Config servlet: Failed finding user " + userName, e3);
                }

                if (user != null)
                {
                    writeHeader(response);
                    response.getOutputStream().println(userName + ":" + password);
                    return;
                }
            }
        }
        catch(Exception e5) {
            Log.error("Password Servlet Error", e5);
        }

        writeHeader(response);
        response.getOutputStream().println("error:error");
    }

    private String getNtlmUserName(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String auth = request.getHeader("Authorization");

        //no auth, request NTLM
        if (auth == null)
        {
            response.setStatus(response.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "NTLM");
            return null;
        }

        //check what client sent
        if (auth.startsWith("NTLM "))
        {
            byte[] msg = java.util.Base64.getDecoder().decode(auth.substring(5));

            if (msg[8] == 1) {
                byte z = 0;
                byte[] msg1 =
                    {(byte)'N', (byte)'T', (byte)'L', (byte)'M', (byte)'S',(byte)'S', (byte)'P',
                        z,(byte)2, z, z, z, z, z, z, z,
                        (byte)40, z, z, z, (byte)1, (byte)130, z, z,
                        z, (byte)2, (byte)2, (byte)2, z, z, z, z, //
                        z, z, z, z, z, z, z, z};
                // send ntlm type2 msg

                response.setStatus(response.SC_UNAUTHORIZED);
                String enc2 = new String(java.util.Base64.getEncoder().encode(msg1), "UTF-8");
                response.setHeader("WWW-Authenticate", "NTLM "+ enc2.trim());
                return null;
            }
            else if (msg[8] == 3) {
                int off = 30, length, offset;
                Boolean unicode = (msg[60] & 0x1) == 1;
                Charset encoding = unicode ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_8;

                length = msg[off+1]*256 + msg[off];
                offset = msg[off+3]*256 + msg[off+2];
                String domain = new String(msg, offset, length, encoding);
                Log.debug("NTLMAuth Domain Name {}", domain);

                length = msg[off+17]*256 + msg[off+16];
                offset = msg[off+19]*256 + msg[off+18];
                String wkstn = new String(msg, offset, length, encoding);
                Log.debug("NTLMAuth Computer Name {}", wkstn);

                length = msg[off+9]*256 + msg[off+8];
                offset = msg[off+11]*256 + msg[off+10];
                String username = new String(msg, offset, length, encoding);
                Log.debug("NTLMAuth User Name {}", username);
                return username;
            }
        }
        return null;
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
