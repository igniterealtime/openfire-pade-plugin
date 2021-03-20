package org.ifsoft.meet;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class Friend.
 */
@XmlRootElement(name = "friend")
@XmlType(propOrder = { "jid", "nickname", "groups"})
public class Friend {

    /** The jid. */
    private String jid;

    /** The nickname. */
    private String nickname;

    /** The groups. */
    private String groups;

    /**
     * Instantiates a new roster item entity.
     */
    public Friend() {

    }

    public Friend(String jid, String nickname, String groups) {
        this.jid = jid;
        this.nickname = nickname;
        this.groups = groups;
    }

    /**
     * Gets the jid.
     *
     * @return the jid
     */
    @XmlElement
    public String getJid() {
        return jid;
    }

    /**
     * Sets the jid.
     *
     * @param jid
     *            the new jid
     */
    public void setJid(String jid) {
        this.jid = jid;
    }

    /**
     * Gets the nickname.
     *
     * @return the nickname
     */
    @XmlElement
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname.
     *
     * @param nickname
     *            the new nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    @XmlElement
    public String getGroups() {
        return groups;
    }

    /**
     * Sets the groups.
     *
     * @param groups
     *            the new groups
     */
    public void setGroups(String groups) {
        this.groups = groups;
    }
}
