/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.security;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Group part of an organization that contains member groups and member user
 * references.
 * 
 * @author Jerome Louvel
 */
public class Group {

    /** The description. */
    private volatile String description;

    /**
     * Indicates if the roles (ie. indirectly the granted or denied permissions)
     * of the parent group should be inherited.
     */
    private volatile boolean inheritRoles;

    /** The modifiable list of child groups. */
    private final List<Group> memberGroups;

    /** The modifiable list of members user references. */
    private final List<User> memberUsers;

    /** The display name. */
    private volatile String name;

    /**
     * Indicates if the permissions (granted or denied) of this group should be
     * transmitted to member groups.
     */
    private volatile boolean transmitPermissions;

    /**
     * Constructor.
     */
    public Group() {
        this.memberGroups = new CopyOnWriteArrayList<Group>();
        this.memberUsers = new CopyOnWriteArrayList<User>();
    }

    /**
     * Returns the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the modifiable list of member groups.
     * 
     * @return The modifiable list of member groups.
     */
    public List<Group> getMemberGroups() {
        return memberGroups;
    }

    public List<User> getMemberUsers() {
        return memberUsers;
    }

    /**
     * Returns the display name.
     * 
     * @return The display name.
     */
    public String getName() {
        return this.name;
    }

    public boolean isInheritRoles() {
        return inheritRoles;
    }

    public boolean isTransmitPermissions() {
        return transmitPermissions;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public void setInheritRoles(boolean inheritPermissions) {
        this.inheritRoles = inheritPermissions;
    }

    /**
     * Sets the list of member groups.
     * 
     * @param memberGroups
     *            The list of member groups.
     */
    public void setMemberGroups(List<Group> memberGroups) {
        this.memberGroups.clear();

        if (memberGroups != null) {
            this.memberGroups.addAll(memberGroups);
        }
    }

    /**
     * Sets the list of member user references.
     * 
     * @param memberUsers
     *            The list of member user references.
     */
    public void setMemberUsers(List<User> memberUsers) {
        this.memberUsers.clear();

        if (memberUsers != null) {
            this.memberUsers.addAll(memberUsers);
        }
    }

    /**
     * Sets the display name.
     * 
     * @param name
     *            The display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setTransmitPermissions(boolean transmitPermissions) {
        this.transmitPermissions = transmitPermissions;
    }

}
