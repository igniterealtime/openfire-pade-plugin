<%--
  -	$Revision$
  -	$Date$
  -
  - Copyright (C) 2004-2008 Jive Software. All rights reserved.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
--%>

<%@ page import="org.jivesoftware.util.*,
                 com.sun.voip.server.*,
                 com.sun.voip.*,
                 java.util.*,
                 java.net.URLEncoder"                 
    errorPage="error.jsp"
%>
<%@ page import="org.xmpp.packet.JID" %>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager"  />
<% webManager.init(request, response, session, application, out ); %>

<% 
	ArrayList conferenceList = ConferenceManager.getConferenceList();
	int confCount = conferenceList.size();
%>
<html>
    <head>
        <title><fmt:message key="audiobridge.title"/></title>
        <meta name="pageID" content="audiobridge-summary"/>
    </head>
    <body>

<p>
<fmt:message key="audiobridge.conference.summary" />
</p>

<%  if (request.getParameter("deletesuccess") != null) { %>

    <div class="jive-success">
    <table cellpadding="0" cellspacing="0" border="0">
    <tbody>
        <tr><td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0" alt=""></td>
        <td class="jive-icon-label">
        <fmt:message key="audiobridge.conference.deleted" />
        </td></tr>
    </tbody>
    </table>
    </div><br>

<%  } %>

<p>
<fmt:message key="audiobridge.summary.conferences" />: <%= confCount %>,
</p>

<div class="jive-table">
<table cellpadding="0" cellspacing="0" border="0" width="100%">
<thead>
    <tr>
        <th>&nbsp;</th>
        <th nowrap><fmt:message key="audiobridge.summary.conference.id" /></th>
        <th nowrap><fmt:message key="audiobridge.summary.participants" /></th>           
        <th nowrap><fmt:message key="audiobridge.summary.mediainfo" /></th>
        <th nowrap><fmt:message key="audiobridge.summary.is.permanent" /></th>
        <th nowrap><fmt:message key="audiobridge.summary.recording" /></th>    
        <th nowrap><fmt:message key="audiobridge.conference.delete" /></th>            
    </tr>
</thead>
<tbody>

<% 
    if (confCount == 0) {
%>
    <tr>
        <td align="center" colspan="10">
            <fmt:message key="audiobridge.summary.no.conferences" />
        </td>
    </tr>

<%
    }
    
    for (int i = 0; i < conferenceList.size(); i++) 
    {
	ConferenceManager conferenceManager = (ConferenceManager) conferenceList.get(i);

	String id = conferenceManager.getId();
	String displayName = conferenceManager.getDisplayName();
%>
    <tr class="jive-<%= (((i%2)==0) ? "even" : "odd") %>">
        <td width="1%">
            <%= i + 1 %>
        </td>
        <td width="20%" valign="middle">
		<%= id %>&nbsp;<%= displayName == null ? "&nbsp;" : displayName %>
        </td>
        <td width="5%" align="center">
 		<%= conferenceManager.getMemberList().size() %>
        </td>          
        <td width="10%" align="center">
                <%= conferenceManager.getMediaInfo().toString() %>           
        </td>
        <td width="5%" align="center">	
       
            <% if (conferenceManager.isPermanent()) { %>
                <img src="images/success-16x16.gif" width="16" height="16" border="0" alt="">
            <% }
               else { %>
                &nbsp;
            <% } %>	            
        </td>
        <td width="38%" align="left">        
            <% 
		String recordingFile = conferenceManager.getWGManager().getRecordingFile();  
		if (recordingFile == null)  recordingFile = "&nbsp;";		
            %>
	    
	    <%= recordingFile %>              
        </td>        
        <td width="1%" align="center" style="border-right:1px #ccc solid;">
            <a href="audiobridge-delete.jsp?confid=<%= URLEncoder.encode(id, "UTF-8") %>" title="<fmt:message key="audiobridge.conference.delete" />"><img src="images/delete-16x16.gif" width="16" height="16" border="0" alt=""></a>
        </td>
    </tr>

<%
    }
%>
</tbody>
</table>
</div>
</body>
</html>
