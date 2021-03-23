package org.jivesoftware.openfire.plugin.rest.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class WorkgroupEntity.
 */
@XmlRootElement(name = "group")
@XmlType(propOrder = { "name", "description", "members" })
public class WorkgroupEntity {

    /** The name. */
    private String name;

    /** The description. */
    private String description;

    /** The members. */
    private String members;

    /**
     * Instantiates a new group entity.
     */
    public WorkgroupEntity() {
    }

    /**
     * Instantiates a new group entity.
     *
     * @param name
     *            the name
     * @param description
     *            the description
     */
    public WorkgroupEntity(String name, String description, String members) {
        this.name = name;
        this.description = description;
        this.members = members;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    @XmlElement
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the members.
     *
     * @return the members
     */
    @XmlElement
    public String getMembers() {
        return members;
    }

    /**
     * Sets the members.
     *
     * @param members the new members
     */
    public void setMembers(String members) {
        this.members = members;
    }

}
