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
import java.util.*;
import java.util.concurrent.*;
import java.text.*;

import net.sf.json.*;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;


public class Password extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger(Password.class);
    public static final ConcurrentHashMap<String, String> passwords = new ConcurrentHashMap<>();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String sessionName = request.getRemoteUser();
            String windowsName = request.getUserPrincipal().getName();  // Single Sign-On

            String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
            String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

            Log.debug("Password Servlet: " + sessionName + " " + windowsName);

            if (windowsName != null && sessionName != null && sessionName.equals(windowsName))
            {
                int pos = windowsName.indexOf("\\");

                if (pos > -1)
                {
                    String userName = windowsName.substring(pos + 1).toLowerCase();
                    String domainName = windowsName.substring(0, pos);

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
        }
        catch(Exception e5) {
            Log.error("Password Servlet Error", e5);
        }

        writeHeader(response);
        response.getOutputStream().println("error:error");
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

        response.setHeader("Access-Control-Allow-Origin", boshManager.getCORSAllowOrigin());
        response.setHeader("Access-Control-Allow-Headers", HttpBindManager.HTTP_BIND_CORS_ALLOW_HEADERS_DEFAULT + ", Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", HttpBindManager.HTTP_BIND_CORS_ALLOW_METHODS_DEFAULT);
    }
}
