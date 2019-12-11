/**
 * Copyright (C) 1999-2008 Jive Software. All rights reserved.
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

package org.jivesoftware.openfire.plugin.spark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response;

import org.dom4j.Element;
import org.xmpp.packet.*;

import org.jivesoftware.openfire.*;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.util.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.group.*;
import org.jivesoftware.openfire.user.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;

/**
 * Manages global bookmarks. Bookmarks are defined by
 * <a href="http://www.jabber.org/jeps/jep-0048.html">JEP-0048</a>. Users can define and
 * manage their own bookmarks. Global bookmarks add to a user's own bookmarks and are
 * defined by system administrators to apply to all users, groups, or sets of users.
 *
 * @see Bookmark
 * @author Derek DeMoro
 */
public class BookmarkManager {

    private static final Logger Log = LoggerFactory.getLogger(BookmarkManager.class);

    private static final String DELETE_BOOKMARK = "DELETE FROM ofBookmark where bookmarkID=?";
    private static final String SELECT_BOOKMARKS = "SELECT bookmarkID from ofBookmark";

    private static final String DOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
    private static final MessageRouter MESSAGE_ROUTER = XMPPServer.getInstance().getMessageRouter();
    private static final UserManager USER_MANAGER = XMPPServer.getInstance().getUserManager().getInstance();
    private static final PresenceManager PRESENCE_MANAGER = XMPPServer.getInstance().getPresenceManager();

    /**
     * Returns the specified bookmark.
     *
     * @param bookmarkID the ID of the bookmark.
     * @return the bookmark.
     * @throws NotFoundException if the bookmark could not be found or loaded.
     */
    public static Bookmark getBookmark(long bookmarkID) throws NotFoundException {
        // TODO add caching
        return new Bookmark(bookmarkID);
    }

    /**
     * Returns the specified bookmark.
     *
     * @param bookmarkValue the value of the bookmark.
     * @return the bookmark.
     * @throws NotFoundException if the bookmark could not be found or loaded.
     */
    public static Bookmark getBookmark(String bookmarkValue)
    {
        Cache<String, Bookmark> bookmarkCache = CacheFactory.createLocalCache("Bookmarks");
        Bookmark bookmark = bookmarkCache.get(bookmarkValue);

        if (bookmark != null) {
            Log.debug("getBookmark: using cache "  + bookmarkValue);
            return bookmark;
        }

        try {
            bookmark = new Bookmark(bookmarkValue);
            bookmarkCache.put(bookmarkValue, bookmark);

            Log.debug("getBookmark: adding to cache "  + bookmarkValue);

        } catch (Exception e) {
            // ignore as bookmark will be null
        }
        return bookmark;
    }

    /**
     * Returns true if bookmark is valid for user with JID.
     *
     * @param username.
     * @param bookmark.
     * @return true or false.
     */

    public static boolean isBookmarkForUser(String username, Bookmark bookmark)
    {
        if (username == null || username.equals("null") || username.equals("")) return false;

        if (bookmark.isGlobalBookmark())            return true;
        if (bookmark.getUsers().contains(username)) return true;

        Collection<String> groups = bookmark.getGroups();

        if (groups != null && !groups.isEmpty())
        {
            GroupManager groupManager = GroupManager.getInstance();

            for (String groupName : groups) {
                try {
                    Group group = groupManager.getGroup(groupName);

                    if (group.isUser(username)) {
                        return true;
                    }
                } catch (GroupNotFoundException e) { }
            }
        }
        return false;
    }

    /**
     * Broadcasts groupchat notification message to all users of a bookmark.
     *
     * @param bookmark from which a user list will be compiled from.
     * @param nickname of user who posts groupchat message.
     * @param body text of groupchat message.
     */
    public static void broadcastMessage(String roomJID, String userJID, String nickname, String body, Bookmark bookmark)
    {
        Log.debug("broadcastMessage " + roomJID + " " + userJID + " " + nickname + "\n" + body);

        Message message = new Message();
        message.setFrom(roomJID);

        Element notification = message.addChildElement("notification", "http://igniterealtime.org/ofchat/notification");
        notification.setText(body);
        notification.addAttribute("jid", userJID);
        notification.addAttribute("nickname", nickname);

        if (bookmark.isGlobalBookmark())
        {
           SessionManager.getInstance().broadcast(message);
           return;
        }

        for (String username : bookmark.getUsers())
        {
            Message newMessage = message.createCopy();
            JID jid = new JID(username + "@" + DOMAIN);
            newMessage.setTo(jid);
            routeMessage(jid, newMessage);
        }

        GroupManager groupManager = GroupManager.getInstance();

        for (String groupName : bookmark.getGroups())
        {
            try {
                Group group = groupManager.getGroup(groupName);

                for (JID memberJID : group.getMembers())
                {
                    Message newMessage = message.createCopy();
                    newMessage.setTo(memberJID);
                    routeMessage(memberJID, newMessage);
                }

                for (JID memberJID : group.getAdmins())
                {
                    Message newMessage = message.createCopy();
                    newMessage.setTo(memberJID);
                    routeMessage(memberJID, newMessage);
                }

            } catch (GroupNotFoundException e) { }
        }
    }

    private static void routeMessage(JID jid, Message message)
    {
        try {
            User toUser = USER_MANAGER.getUser(jid.getNode());
            boolean available = PRESENCE_MANAGER.isAvailable(toUser);

            Log.debug("routeMessage " + available + " " + toUser.getName() + "\n" + message.toString());

            if (available) MESSAGE_ROUTER.route(message);

        } catch (Exception e) {
            Log.error("routeMessage", e);
        }
    }

    /**
     * Returns all bookmarks.
     *
     * @return the collection of bookmarks.
     */
    public static Collection<Bookmark> getBookmarks() throws ServiceException {
        // TODO: add caching.
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_BOOKMARKS);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long bookmarkID = rs.getLong(1);
                try {
                    Bookmark bookmark = new Bookmark(bookmarkID);
                    bookmarks.add(bookmark);
                }
                catch (NotFoundException nfe) {
                    Log.error(nfe.getMessage(), nfe);
                }
            }
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
            throw new ServiceException("SQL Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return bookmarks;
    }

    /**
     * Deletes a bookmark with the specified bookmark ID.
     *
     * @param bookmarkID the ID of the bookmark to remove from the database.
     */
    public static void deleteBookmark(long bookmarkID)  throws ServiceException {
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_BOOKMARK);
            pstmt.setLong(1, bookmarkID);
            pstmt.execute();
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
            throw new ServiceException("SQL Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
}
