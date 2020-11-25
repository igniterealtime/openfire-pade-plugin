<%@ page import="org.jivesoftware.util.*,
                 org.jivesoftware.openfire.XMPPServer,
                 org.jivesoftware.openfire.muc.*,
                 org.jivesoftware.util.JiveGlobals,                
                 java.io.*,
                 java.util.*,                 
                 net.sf.json.*,
                 java.net.URLEncoder"                 
    errorPage="error.jsp"
%>
<%@ page import="org.xmpp.packet.JID" %>
<%@ page import="org.jivesoftware.openfire.plugin.ofmeet.OfMeetPlugin" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager"  />
<% webManager.init(request, response, session, application, out ); %>

<%         
        List<MUCRoom> rooms = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRooms();        
        int confCount = rooms.size();        
%>
<html>
    <head>
        <title><fmt:message key="config.page.summary.title"/></title>
        <meta name="pageID" content="ofmeet-summary"/>
    </head>
    <body>

<% 
    final OfMeetPlugin container = (OfMeetPlugin) XMPPServer.getInstance().getPluginManager().getPlugin( "ofmeet" );
    
    JSONObject summary = new JSONObject("{\"current_timestamp\":\"\", \"total_conference_seconds\":0, \"total_participants\":0, \"total_failed_conferences\":0, \"total_conferences_created\":0, \"total_conferences_completed\":0, \"conferences\":0, \"participants\":0, \"largest_conference\":0, \"p2p_conferences\":0}");        

    String json = container.getConferenceStats();
    
    if (json != null)
    {
        JSONObject jsonObj = new JSONObject(json);
        if (jsonObj != null && jsonObj.has("current_timestamp")) summary = jsonObj;
    }

    String current_timestamp = summary.getString("current_timestamp");
    int total_conference_seconds = summary.getInt("total_conference_seconds");      
    int total_participants = summary.getInt("total_participants");
    int total_failed_conferences = summary.getInt("total_failed_conferences");          
    int total_conferences_created = summary.getInt("total_conferences_created"); 
    int total_conferences_completed = summary.getInt("total_conferences_completed");     
    int conferences = summary.getInt("conferences");      
    int participants = summary.getInt("participants");
    int largest_conference = summary.getInt("largest_conference");          
    int p2p_conferences = summary.getInt("p2p_conferences");     

%>
    <div class="jive-table">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <thead>
        <tr>
            <th nowrap><fmt:message key="ofmeet.summary.current_timestamp" /></th>   
            <th nowrap><fmt:message key="ofmeet.summary.total_conference_seconds" /></th>
            <th nowrap><fmt:message key="ofmeet.summary.total_participants" /></th>
            <th nowrap><fmt:message key="ofmeet.summary.total_failed_conferences" /></th>           
            <th nowrap><fmt:message key="ofmeet.summary.total_conferences_created" /></th>    
            <th nowrap><fmt:message key="ofmeet.summary.total_conferences_completed" /></th>   
            <th nowrap><fmt:message key="ofmeet.summary.conferences" /></th>
            <th nowrap><fmt:message key="ofmeet.summary.participants" /></th>
            <th nowrap><fmt:message key="ofmeet.summary.largest_conference" /></th>           
            <th nowrap><fmt:message key="ofmeet.summary.p2p_conferences" /></th>              
        </tr>
    </thead>
    <tbody>
        <tr>
            <td nowrap><%= current_timestamp %></th>   
            <td nowrap><%= total_conference_seconds %></th>
            <td nowrap><%= total_participants %></th>
            <td nowrap><%= total_failed_conferences %></th>           
            <td nowrap><%= total_conferences_created %></th>     
            <td nowrap><%= total_conferences_completed %></th>   
            <td nowrap><%= conferences %></th>
            <td nowrap><%= participants %></th>
            <td nowrap><%= largest_conference %></th>           
            <td nowrap><%= p2p_conferences %></th>              
        </tr>
    </tbody>
    </table>
    </div>
    <br/>
    
    <p>&nbsp;</p>

    <div class="jive-table">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <thead>
        <tr>
            <th>&nbsp;</th>
            <th nowrap><fmt:message key="ofmeet.summary.conference" /></th>
            <th nowrap><fmt:message key="ofmeet.summary.participants" /></th>                    
            <th nowrap><fmt:message key="ofmeet.summary.focus" /></th>               
        </tr>
    </thead>
    <tbody>  
<% 
    if (confCount == 0) {
%>
    <tr>
        <td align="center" colspan="10">
            <fmt:message key="ofmeet.summary.no.conferences" />
        </td>
    </tr>

<%
    }
    int i = 0;
    
    for (MUCRoom chatRoom : rooms)     
    {    
        String occupants = "";
        String focus = null;

        for ( final MUCRole occupant : chatRoom.getOccupants() ) {
            JID jid = occupant.getUserAddress();
            String nick = jid.getNode();
            
            if (!nick.startsWith("focus"))
            {
                occupants = occupants + "<a href=\"/session-details.jsp?jid=" + jid + "\">" + nick + "</a>&nbsp;";
            }
            
            if (jid.getNode().equals("focus"))
            {
                focus = jid.getResource();
            }
        }  
        
        if (!chatRoom.getJID().getNode().equals("ofmeet") && focus != null)
        {
            i++;            
%>
            <tr class="jive-<%= (((i%2)==0) ? "even" : "odd") %>">
                <td width="1%">
                    <%= i %>
                </td>
                <td width="10%" valign="middle">
                <%= "<a href=\"/muc-room-occupants.jsp?roomJID=" + chatRoom.getJID().toBareJID() + "\">" + chatRoom.getName() + "</a>" %>
                </td>
                <td width="10%">
                <%= occupants %>
                </td>   
                <td width="10%">
                <%= focus %>
                </td>                 
            </tr>
<%
        }
    }
%>
</tbody>
</table>
</div>
</body>
</html>