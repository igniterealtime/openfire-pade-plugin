/*
 * Copyright (C) 2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.openfire.archive;

import java.util.*;
import java.text.*;
import java.io.*;
import java.time.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jivesoftware.openfire.plugin.rest.BasicAuth;

import org.xmpp.packet.*;
import org.jivesoftware.openfire.archive.*;

public class ConversationPDFServlet extends HttpServlet {

    private static final Logger Log = LoggerFactory.getLogger(ConversationPDFServlet.class);

    @Override
    public void init() throws ServletException {

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String keywords = request.getParameter("keywords");
        String to = request.getParameter("to");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String room = request.getParameter("room");
        String service = request.getParameter("service");

        ServletOutputStream out = response.getOutputStream();

        if (service == null) service = "conference";

        String auth = request.getHeader("authorization");
        String token = auth.substring(6);
        String[] usernameAndPassword = BasicAuth.decode(auth);

        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentType("application/pdf");
        //response.setContentLength(stream.size());

        doSearch(out, usernameAndPassword[0], keywords, to, start, end, room, service);
    }


    public static void doSearch(OutputStream out, String username, String keywords, String to, String start, String end, String room, String service)
    {
        if (service == null) service = "conference";

        ArchiveSearch search = new ArchiveSearch();
        JID participant1JID = makeJid(username);
        JID participant2JID = null;

        if (to != null) participant2JID = makeJid(to);

        if (participant2JID != null) {
            search.setParticipants(participant1JID, participant2JID);
        } else  {
            search.setParticipants(participant1JID);
        }

        if (start != null)
        {
            Date startDate = null;

            try {
                if (start.contains("T"))
                {
                    startDate = Date.from(Instant.parse(start));
                }
                else {
                    DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                    startDate = formatter.parse(start);
                }
                startDate = new Date(startDate.getTime() - JiveConstants.MINUTE * 5);
                search.setDateRangeMin(startDate);
            }
            catch (Exception e) {
                Log.error("ConversationPDFServlet", e);
            }
        }

        if (end != null)
        {
            Date endDate = null;

            try {
                if (end.contains("T"))
                {
                    endDate = Date.from(Instant.parse(end));
                }
                else {
                    DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                    endDate = formatter.parse(end);
                }
                endDate = new Date(endDate.getTime() + JiveConstants.DAY - 1);
                search.setDateRangeMax(endDate);
            }
            catch (Exception e) {
                Log.error("ConversationPDFServlet", e);
            }
        }

        if (keywords != null) search.setQueryString(keywords);
        if (service == null) service = "conference";

        if (room != null)
        {
            search.setRoom(new JID(room + "@" + service + "." + XMPPServer.getInstance().getServerInfo().getXMPPDomain()));
        }

        search.setSortOrder(ArchiveSearch.SortOrder.ascending);

        Log.debug("ConversationPDFServlet " + search.getParticipants() + " " + search.getQueryString());

        Collection<Conversation> conversations = new ArchiveSearcher().search(search);

        if (conversations.size() > 0)
        {
            try {
                ByteArrayOutputStream stream = new ConversationUtils().buildPDFContent(conversations, search);

                stream.writeTo(out);
                out.flush();
            }
            catch (Exception e) {
                Log.error("ConversationPDFServlet", e);
            }
        }
    }

    private static JID makeJid(String participant1)
    {
        JID participant1JID = null;

        try {
            int position = participant1.lastIndexOf("@");

            if (position > -1) {
                String node = participant1.substring(0, position);
                participant1JID = new JID(JID.escapeNode(node) + participant1.substring(position));
            } else {
                participant1JID = new JID(JID.escapeNode(participant1), XMPPServer.getInstance().getServerInfo().getXMPPDomain(), null);
            }
        } catch (Exception e) {
            Log.error("makeJid", e);
        }
        return participant1JID;
    }
}
