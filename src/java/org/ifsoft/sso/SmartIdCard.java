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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;

import net.sf.json.*;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;


public class SmartIdCard extends HttpServlet
{
    private static final Logger Log = LoggerFactory.getLogger(SmartIdCard.class);

    private final String clientId = "s5D6gnTwOqmFISb7KY5maMe2XgEcKNOa";
    private final String clientSecret = "YUj4ZYuOCTmXPqDYc52maYsSNMjCG5RG";
    private final String callbackUri = "https://igniterealtime.github.io/Pade/index.html";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
            String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
            String code = request.getParameter("code");

            Map<String,String> params = new HashMap<String,String>();
            params.put("code", code);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("redirect_uri", callbackUri);
            params.put("grant_type", "authorization_code");

            String body = post("https://id.smartid.ee/oauth/access_token", params);
            JSONObject json = new JSONObject(body);

            Log.debug("SmartIdCard access_token: \n" + json);

            if (json != null && json.has("access_token"))
            {
                json = new JSONObject(get("https://id.smartid.ee/api/v2/user_data?access_token=" + json.getString("access_token")));

                Log.debug("SmartIdCard data: \n" + json);

                if (json != null && json.has("status") && "OK".equals(json.getString("status")) && json.has("idcode"))
                {
                    String userName = json.getString("idcode");
                    String fullName = null;
                    String email = null;

                    if (json.has("firstname") && json.has("lastname"))
                    {
                        fullName = json.getString("firstname") + " " + json.getString("lastname");
                    }

                    if (json.has("email"))
                    {
                        email = json.getString("email");
                    }

                    String password = Password.passwords.get(userName);

                    if (password == null)
                    {
                        password = TimeBasedOneTimePasswordUtil.generateBase32Secret();
                        Password.passwords.put(userName, password);
                    }

                    json.put("password", password);

                    UserManager userManager = XMPPServer.getInstance().getUserManager();
                    User user = null;

                    try {
                        user = userManager.getUser(userName);
                        user.setPassword(password);
                        Log.debug( "SmartIdCard servlet: Found user " + userName + " " + fullName);
                    }
                    catch (UserNotFoundException e) {

                        try {
                            Log.debug("SmartIdCard servlet: Creating user " + userName + " " + fullName);
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
        catch(Exception e5) {
            Log.error("SmartIdCard Servlet Error", e5);
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

    public String get(String url) throws ClientProtocolException, IOException
    {
        return execute(new HttpGet(url));
    }

    public String post(String url, Map<String,String> formParameters) throws ClientProtocolException, IOException
    {
        HttpPost request = new HttpPost(url);

        List <NameValuePair> nvps = new ArrayList <NameValuePair>();

        for (String key : formParameters.keySet()) {
            nvps.add(new BasicNameValuePair(key, formParameters.get(key)));
        }

        request.setEntity(new UrlEncodedFormEntity(nvps));

        return execute(request);
    }

    private String execute(HttpRequestBase request) throws ClientProtocolException, IOException
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
        }

        return body;
    }
}
