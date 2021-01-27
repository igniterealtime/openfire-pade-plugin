<?xml version="1.0" encoding="UTF-8"?>

<!--
  ~ Copyright (c) 2017 Ignite Realtime Foundation. All rights reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
  
<%@ page import="org.jivesoftware.util.*,
                 java.util.*,
                 org.freeswitch.esl.client.transport.message.EslMessage,
                 java.net.URLEncoder"                 
    errorPage="error.jsp"
%>
<%@ page import="org.xmpp.packet.JID" %>
<%@ page import="org.jivesoftware.openfire.plugin.ofmeet.OfMeetPlugin" %>
<%@ page import="org.jivesoftware.openfire.XMPPServer" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager"  />
<jsp:useBean id="ofmeetConfig" class="org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig"/>

<% webManager.init(request, response, session, application, out ); %>

<html>
    <head>
        <title>Overview</title>
        <meta name="pageID" content="freeswitch-overview"/>
    </head>
    <body>

<pre>
<%
    if (ofmeetConfig.getJigasiSipEnabled() && ofmeetConfig.getJigasiFreeSwitchEnabled()) 
    {
        OfMeetPlugin container = OfMeetPlugin.self;
    
        if (container != null)
        {  
            EslMessage resp = container.sendFWCommand("banner");

            if (resp != null)
            {
                List<String> overviewLines = resp.getBodyLines();

                for (String line : overviewLines) 
                {
                    %><div style="font-family: Courier New,Courier,Lucida Sans Typewriter,Lucida Typewriter,monospace!important;"><%= line %></div><%
                }
                
                resp = container.sendFWCommand("sofia status");
                overviewLines = resp.getBodyLines();

                for (String line : overviewLines) 
                {
                    %><div style="font-family: Courier New,Courier,Lucida Sans Typewriter,Lucida Typewriter,monospace!important;"><%= line %></div><%
                }
                
                resp = container.sendFWCommand("version");
                overviewLines = resp.getBodyLines();

                for (String line : overviewLines) 
                {
                    %><p style="font-family: Courier New,Courier,Lucida Sans Typewriter,Lucida Typewriter,monospace!important;"><%= line %></p><%
                }
                
                
            } else {

                %>Please wait.......<%
            }
        }
        
    } else {
        %>Disabled<%            
    }       
%>
</pre>
</body>
</html>
