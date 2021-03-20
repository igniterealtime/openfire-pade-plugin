package org.jivesoftware.openfire.plugin.rest.entity;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class UserEntities.
 */
@XmlRootElement(name = "users")
public class UserEntities {

    /** The users. */
    Collection<UserEntity> users;

    /**
     * Instantiates a new user entities.
     */
    public UserEntities() {

    }

    /**
     * Instantiates a new user entities.
     *
     * @param users
     *            the users
     */
    public UserEntities(Collection<UserEntity> users) {
        this.users = users;
    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    @XmlElement(name = "user")
    public Collection<UserEntity> getUsers() {
        return users;
    }

    /**
     * Sets the users.
     *
     * @param users
     *            the new users
     */
    public void setUsers(Collection<UserEntity> users) {
        this.users = users;
    }

}
