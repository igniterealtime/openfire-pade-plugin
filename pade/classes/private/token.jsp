<%@ page import="org.jivesoftware.openfire.plugin.rest.BasicAuth" %>
<%
    String auth = request.getHeader("authorization");
    String token = auth.substring(6);
    String[] usernameAndPassword = BasicAuth.decode(token);   

    if (usernameAndPassword != null && usernameAndPassword.length == 2) 
    {    
%>
        {"token": "<%= token %>", "username": "<%= usernameAndPassword[0] %>", "password": "<%= usernameAndPassword[1] %>"}
<%
    }
%>
