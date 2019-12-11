<%--
  - $Revision$
  - $Date$
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
<%@ page import="org.jivesoftware.openfire.plugin.ofmeet.*" %>
<%@ page import="org.jitsi.videobridge.openfire.*" %>
<%@ page import="org.jivesoftware.openfire.*" %>
<%@ page import="org.jivesoftware.util.*" %>
<%@ page import="org.jivesoftware.openfire.plugin.spark.Bookmark" %>
<%@ page import="org.jivesoftware.openfire.plugin.spark.BookmarkManager" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%

    String roomName = ParamUtils.getParameter(request,"name");
    String roomId = ParamUtils.getParameter(request,"room");    
    String bookmarkId = ParamUtils.getParameter(request,"id"); 
    String updatedEvents = ParamUtils.getParameter(request,"calendarevents");
    String quartz = ParamUtils.getParameter(request,"quartz");      

    String users = "[";
    String groups = "[";
    
    Bookmark bookmark = null;
    
    try {
    
        if (bookmarkId != null)
        {
            bookmark = BookmarkManager.getBookmark(Long.parseLong(bookmarkId)); 

            if (updatedEvents != null && !"".equals(updatedEvents))
            {
                bookmark.setProperty("calendar", updatedEvents);
                MeetingPlanner.processMeetingPlanner();
            }

            if (quartz != null)
            {
                String old = bookmark.getProperty("quartz"); 

                if (old == null || (old != null && old.equals(quartz) == false))
                {
                    String resp = MeetingPlanner.processMeetingCron(bookmark, quartz, old);     
                }   
            }

            int i = 0;
            int count = bookmark.getUsers().size();

            int j = 0;
            int count2 = bookmark.getGroups().size();    


            for (String user : bookmark.getUsers())
            {   
                users = users + "'" + user + "'";
                i++;

                if (i < count) users = users + ",";
            }

            for (String group : bookmark.getGroups())
            {   
                groups = groups + "'" + group + "'";
                j++;

                if (j < count2) groups = groups + ",";
            }
        }
    
        users = users + "]";    
        groups = groups + "]";  
        
    } catch (Exception e) {}
     
%>
<html>
  <head>
    <title><fmt:message key="config.page.calendar.title" /></title>
    <meta name="pageID" content="ofmeet-planner"/>    
    <link rel="stylesheet" href="vendor/font-awesome.min.css">
    <link rel="stylesheet" href="vendor/jquery-ui.custom.min.css">
    <link rel="stylesheet" href="vendor/fullcalendar.css"/>
    <link rel="stylesheet" href="stylesheets/workshop_manager.css">
    <link rel="stylesheet" href="vendor/foundation.css">
    <link rel="stylesheet" href="stylesheets/jquery-cron.css">    

    <script src="vendor/modernizr.js"></script>
    <script src="vendor/moment.min.js"></script>
    <script src="vendor/jquery-2.1.0.js"></script>
    <script src="vendor/jquery-ui.custom.min.js"></script>
    <script src="vendor/foundation.min.js"></script>
    <script src="vendor/fullcalendar.js"></script>
    <script src="vendor/jquery.ui.touch-punch.js"></script>
    <script src="vendor/fastclick.js"></script>
    <script src="vendor/jquery-cron.js"></script>    
    <script src="javascripts/workshop_manager.js"></script>
    <script src="javascripts/main.js"></script>
    <script>
        var roomName = "<%= roomName %>";
        var roomId = "<%= roomId %>";    
        var users = <%= users %>;       
        var groups = <%= groups %>; 
<%
        String events = "[]";
        String trigger = "";    

        if (bookmark != null)
        {
            events = bookmark.getProperty("calendar");    
            trigger = bookmark.getProperty("quartz");           

            if (events == null) events = "[]";
            if (trigger == null) trigger = ""; 
        }
%>
        $(document).ready(function() 
        {   
            var today = new Date();
            var cronFormat = "0 "+today.getMinutes()+" "+today.getHours()+" "+today.getDate()+" "+(today.getMonth()+1)+" ? *";
        
            $('#cron_container').cron({
              initial: cronFormat,
              useGentleSelect:false
            });
        });        

    var DATA = {events: <%= events %>};
    </script>
  </head>
  <body>
   <form action='ofmeet-calendar.jsp' id='calendarform' name='calendarform' method='post'>  
   <h1><%= roomName %></h1>
   <p>
    <fmt:message key="ofmeet.calendar.description" />
   </p> 
   <hr />
   <table width="100%"><tr>
    <td width="85%">
        <div id="container" class="row">
          <div id="workshop_manager" class="small-12 columns"></div>
        </div>
    </td>
    <td>
        <input type='hidden' id='calendarevents'    name='calendarevents'>
        <input type='hidden' id='name'          name='name'     value='<%= roomName %>'>    
        <input type='hidden' id='room'          name='room'     value='<%= roomId %>'>   
        <input type='hidden' id='id'            name='id'   value='<%= bookmarkId %>'> 
        <div>
         <span id='cron_container' ></span>
         <input type="button" value="Generate" onclick="$('#quartz').val($('#cron_container').cron('value'));" />         
        </div>
    </td>
   </tr></table> 
   <div>
        Quartz Cron Trigger&nbsp;<a target="_blank" href="http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html">&nbsp;?&nbsp;</a>
        <input type="text" id="quartz" name="quartz" placeholder="0 15 10 * * ? 2017 (Fire at 10:15am every day during the year 2017)" value='<%= trigger %>' />
        <input type="button" value="Accept" onclick="document.calendarform.submit()" />    
        <input type="button" value="Cancel" onclick="$('#quartz').val('delete');document.calendarform.submit()" />            
   </div>   
  </form>
 </body>
</html>
