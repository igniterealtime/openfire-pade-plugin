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
import java.security.cert.X509Certificate;
import java.security.Principal;

import net.sf.json.*;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;


public class SmartIdCardCert extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger(SmartIdCardCert.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
            String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

            X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

            if (certs != null && certs.length > 1)
            {
                X509Certificate principalCert = certs[0];
                Principal principal = principalCert.getSubjectDN();

                if (principal != null)
                {
                    String subjectDN = principal.getName();
                    String userName = null;
                    String firstName = null;
                    String lastName = null;

                    String ou = null;
                    String o = null;
                    String c = null;

                    Log.debug("SmartIdCardCert servlet: Found subject DN " + subjectDN);

                    String[] attrs = subjectDN.split(",");

                    for (int i=0; i<attrs.length; i++)
                    {
                        String[] attr = attrs[i].split("=");
                        if (attr.length < 2) continue;

                        String name = attr[0].trim();
                        String value = attr[1].trim();

                        if ("OU".equals(name))   ou = value;
                        if ("O".equals(name))    o = value;
                        if ("C".equals(name))    c = value;

                        if ("SERIALNUMBER".equals(name)) userName = value;
                        if ("GIVENNAME".equals(name))    firstName = value;
                        if ("SURNAME".equals(name))      lastName = value;
                    }

                    if ("EE".equals(c) && "ESTEID (DIGI-ID E-RESIDENT)".equals(o) && "authentication".equals(ou))
                    {
                        String fullName = firstName + " " + lastName;
                        String email = userName + "@" + domain;

                        JSONObject json = new JSONObject();
                        json.put("firstname", firstName);
                        json.put("lastname", lastName);
                        json.put("email", email);
                        json.put("idcode", userName);

                        String password = TimeBasedOneTimePasswordUtil.generateBase32Secret();
                        Password.passwords.put(userName, password);

                        json.put("password", password);

                        UserManager userManager = XMPPServer.getInstance().getUserManager();
                        User user = null;

                        try {
                            user = userManager.getUser(userName);
                            user.setPassword(password);
                            Log.debug( "SmartIdCardCert servlet: Found user " + userName + " " + fullName);
                        }
                        catch (UserNotFoundException e) {

                            try {
                                Log.debug("SmartIdCardCert servlet: Creating user " + userName + " " + fullName + " " + email);
                                user = userManager.createUser(userName, password, fullName, email);

                                Group group = null;
                                String groupName = JiveGlobals.getProperty("ofchat.smartid.groupname", "smartid");
                                String groupTitle = JiveGlobals.getProperty("ofchat.smartid.groupname", "Smart ID");

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
                            response.getOutputStream().println(json.toString());
                            return;
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            Log.error("SmartIdCardCert Servlet Error", e);
        }

        writeHeader(response);
        response.getOutputStream().println("{\"status\":\"ERROR\"}");
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
