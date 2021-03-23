<%@ page import="com.j256.twofactorauth.TimeBasedOneTimePasswordUtil, org.jivesoftware.openfire.plugin.rest.BasicAuth, org.ifsoft.sso.Password" %>
<%
    String secret = TimeBasedOneTimePasswordUtil.generateBase32Secret();
    String auth = request.getHeader("authorization");
    String token = auth.substring(6);
    String[] usernameAndPassword = BasicAuth.decode(auth);   

    if (usernameAndPassword != null && usernameAndPassword.length == 2) 
    {    
        Password.passwords.put(usernameAndPassword[0], secret);
%>
        {"token": "<%= token %>", "secret": "<%= secret %>", "username": "<%= usernameAndPassword[0] %>", "password": "<%= usernameAndPassword[1] %>"}
<%
    }
%>
