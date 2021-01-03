<%@ page import="java.util.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.jivesoftware.openfire.*" %>
<%@ page import="org.jivesoftware.util.*" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    Logger Log = LoggerFactory.getLogger( "converse-settings.jsp" );
    boolean update = request.getParameter("update") != null;
    String errorMessage = null;

    if (update)
    {    
        List<String> properties = JiveGlobals.getPropertyNames();

        for (String propertyName : properties) 
        {
            if (propertyName.indexOf("pade.branding.") == 0) 
            {   
                String jsonString = JiveGlobals.getProperty(propertyName);   
                JSONObject json = new JSONObject(jsonString);
                
                String name = propertyName.substring(14);                
                String newValue = request.getParameter( "value_" + name );
                String newDisable = request.getParameter( "disable_" + name );                                
                
                Log.debug("update name=" + name + ", value= " + newValue + ", disable= " + newDisable);              

                if (newValue != null)
                {
                    if ("false".equals(newValue)) 
                    {
                        json.put("value", false);                     
                    }
                    else
                    
                    if ("true".equals(newValue)) 
                    {
                        json.put("value", true);                     
                    }
                    else {
                        json.put("value", newValue);
                    }
                }

                json.put("disable", true);
                
                if (newDisable != null)
                {
                    json.put("disable", !"on".equals(newDisable));
                }

                JiveGlobals.setProperty(propertyName, json.toString());                                        
             }
        }                                        
    }

    String service_url = "https://" + XMPPServer.getInstance().getServerInfo().getHostname() + ":" + JiveGlobals.getProperty("httpbind.port.secure", "7443") + "/pade";    

%>
<html>
<head>
   <title><fmt:message key="config.converse.settings.branding" /></title>
   <meta name="pageID" content="converse-settings"/>
</head>
<body>
<% if (errorMessage != null) { %>
<div class="error">
    <%= errorMessage%>
</div>
<br/>
<% } %>

<p>
    <fmt:message key="config.converse.settings.description" />
    <br/>&nbsp;<br/>
</p>
<p>
    <fmt:message key="config.converse.connectivity.description" />&nbsp;<a target="_blank" href="<%= service_url %>"><%= service_url %></a>
    <br/>&nbsp;<br/>
</p> 
<div class="jive-table">
<form action="converse-settings.jsp" method="post">
    <p>
        <table class="jive-table" cellpadding="0" cellspacing="0" border="0" width="100%">
            <thead> 
            <tr>
                <th><fmt:message key="config.converse.settings.name"/></th>
                <th><fmt:message key="config.converse.settings.value"/></th>                
            </tr>
            </thead>
            <tbody>  
<%
            List<String> properties = JiveGlobals.getPropertyNames();
            Collections.sort(properties);
            Set<String> unique = new LinkedHashSet<>(properties);

            for (String propertyName : unique) 
            {
                if (propertyName.indexOf("pade.branding.") == 0) 
                {   
                    String name = propertyName.substring(14);
                    String jsonString = JiveGlobals.getProperty(propertyName);   
                    JSONObject json = new JSONObject(jsonString);
                    
                    String value = "";
                    if (json.has("value")) value = json.get("value").toString();
                    Boolean disable = json.getBoolean("disable");
                    
                    String valueLabel = "value_" + name;
                    String disableLabel = "disable_" + name;
%>
                    <tr>
                        <td>
                            <input type="checkbox" id="<%= disableLabel %>" name="<%= disableLabel %>" <%= (disable ? "" : "checked") %> >
                            <%= name %>
                        </td>                                           
                        <td>
                            <input type="text" size="50" maxlength="100" id="<%= valueLabel %>" name="<%= valueLabel %>" value="<%= value %>">
                        </td>
                    </tr>             
<%              }
            }
%>                       
            </tbody>
        </table>
    </p>
   <p>
        <table class="jive-table" cellpadding="0" cellspacing="0" border="0" width="100%">
            <thead> 
            <tr>
                <th colspan="2"><fmt:message key="config.page.configuration.save.title"/></th>
            </tr>
            </thead>
            <tbody>         
            <tr>
                <th colspan="2"><input type="submit" name="update" value="<fmt:message key="config.page.configuration.submit" />">&nbsp;&nbsp;<fmt:message key="config.page.configuration.restart.warning"/></th>
            </tr>       
            </tbody>            
        </table> 
    </p>
</form>
</div>
</body>
</html>
