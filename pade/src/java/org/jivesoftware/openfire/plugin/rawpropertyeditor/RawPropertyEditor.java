package org.jivesoftware.openfire.plugin.rawpropertyeditor;

import java.io.File;
import java.util.Map;
import org.jivesoftware.util.Log;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

public class RawPropertyEditor {

    private static final Logger Log = LoggerFactory.getLogger(RawPropertyEditor.class);
    public static RawPropertyEditor self = new RawPropertyEditor();


    public static RawPropertyEditor getInstance() {
        return self;
    }

    public User getAndCheckUser(String username) {
        JID targetJID = XMPPServer.getInstance().createJID(username, null);
        try {
            return XMPPServer.getInstance().getUserManager().getUser(targetJID.getNode());
        } catch (UserNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Group getAndCheckGroup(String groupname) {
        JID targetJID = XMPPServer.getInstance().createJID(groupname, null, true);

        try {
            return GroupManager.getInstance().getGroup(targetJID.getNode());
        } catch (GroupNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void addProperties(String username, String propname, String propvalue) {

        try {
            User user = getAndCheckUser(username);
            user.getProperties().put(propname, propvalue);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addGroupProperties(String groupname, String propname, String propvalue) {

        try {
            Group group = getAndCheckGroup(groupname);
            group.getProperties().put(propname, propvalue);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteGroupProperties(String groupname, String propname) {
        try {
            Group group = getAndCheckGroup(groupname);
            group.getProperties().remove(propname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteProperties(String username, String propname) {
        try {
            User user = getAndCheckUser(username);
            user.getProperties().remove(propname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * public List<UserProperty> getUserProperties(UserEntity user) { return
     * user.getProperties();
     *
     * }
     */
    public Map<String, String> getUserProperties(String username) {
        User user = getAndCheckUser(username);
        return user.getProperties();
    }

    public Map<String, String> getGroupProperties(String groupname) {
        Group group = getAndCheckGroup(groupname);
        return group.getProperties();
    }

    public String getName() {
        return "rawpropertyeditor";
    }

    public String getDescription() {

        return "rawpropertyeditor Plugin";

    }

}