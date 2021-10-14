package org.jivesoftware.openfire.plugin.rest.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * The Class MUCRoomUtils.
 */
public class MUCRoomUtils {

    /**
     * Instantiates a new MUC room utils.
     */
    private MUCRoomUtils() {
        throw new AssertionError();
    }

    /**
     * Convert jids to string list.
     * In case the jid is not bare (=it is a group jid) exclude it
     *
     * @param jids
     *            the jids
     * @return the array list< string>
     */
    public static List<String> convertJIDsToStringList(Collection<JID> jids) {
        List<String> result = new ArrayList<String>();

        for (JID jid : jids) {
            if (jid.getResource() == null) result.add(jid.toBareJID());
        }
        return result;
    }

    /**
     * Convert groups to string list
     * @param groups
     * 			the groups
     * @return the array list of the group names
     */
    public static List<String> convertGroupsToStringList(Collection<Group> groups) {
        List<String> result = new ArrayList<String>();
        for (Group group : groups) {
            result.add(group.getName());
        }
        return result;
    }

    /**
     * Convert strings to jids.
     *
     * @param jids
     *            the jids
     * @return the list<jid>
     */
    public static List<JID> convertStringsToJIDs(List<String> jids) {
        List<JID> result = new ArrayList<JID>();

        for (String jidString : jids) {
            result.add(new JID(jidString));
        }
        return result;
    }

    /**
     * Convert MUCRole.role instances to string list.
     *
     * @param roles
     *            the roles
     * @return the array list<string>
     */
    public static List<String> convertRolesToStringList(Collection<MUCRole.Role> roles) {
        return roles.stream()
            .map(MUCRole.Role::toString)
            .collect(Collectors.toList());
    }

    /**
     * Convert string instances to a MUCRole.role list.
     *
     * @param roles
     *            the roles
     * @return the array list<MUCRole.role>
     */
    public static List<MUCRole.Role> convertStringsToRoles(Collection<String> roles) {
        return roles.stream()
            .map(MUCRole.Role::valueOf)
            .collect(Collectors.toList());
    }

    /**
     * Wrapper around MUCRoom::send
     *
     * Attempts to call the legacy implementation of MUCRoom::send, if an Openfire version < 4.6 is used.
     */
    public static void send(MUCRoom room, Packet packet, MUCRole role)
        throws InvocationTargetException, IllegalAccessException {
        Method legacySend = legacySendMethod();

        if (legacySend != null) {
            legacySend.invoke(room, packet); // Openfire < 4.6
        } else {
            room.send(packet, role); // Openfire >= 4.6
        }
    }

    /**
     * Attempts to find the legacy MUCRoom::send method signature
     * depending on the used Openfire version via reflection.
     *
     * Openfire versions < 4.6 offer the MUCRoom::send function with one parameters,
     * while later versions expect two.
     *
     * @return the legacy MUCRoom::send method, if present, else null
     */
    private static Method legacySendMethod() {
        try {
            return MUCRoom.class.getMethod("send", Packet.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
