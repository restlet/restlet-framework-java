/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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
     * Indicates if the roles of the parent group should be inherited. Those
     * roles indirectly cover the granted or denied permissions.
     */
    private volatile boolean inheritRoles;

    /** The modifiable list of child groups. */
    private final List<Group> memberGroups;

    /** The modifiable list of members user references. */
    private final List<User> memberUsers;

    /** The display name. */
    private volatile String name;

    /**
     * Default constructor. Note that roles are inherited by default.
     */
    public Group() {
        this(null, null);
    }

    /**
     * Constructor. Note that roles are inherited by default.
     * 
     * @param name
     *            The display name.
     * @param description
     *            The description.
     */
    public Group(String name, String description) {
        this(name, description, true);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The display name.
     * @param description
     *            The description.
     * @param inheritRoles
     *            Indicates if the roles of the parent group should be
     *            inherited.
     */
    public Group(String name, String description, boolean inheritRoles) {
        this.name = name;
        this.description = description;
        this.inheritRoles = inheritRoles;
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

    /**
     * Indicates if the roles of the parent group should be inherited. Those
     * roles indirectly cover the granted or denied permissions.
     * 
     * @return True if the roles of the parent group should be inherited.
     */
    public boolean isInheritRoles() {
        return inheritRoles;
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

    /**
     * Indicates if the roles of the parent group should be inherited. Those
     * roles indirectly cover the granted or denied permissions.
     * 
     * @param inheritPermissions
     *            True if the roles of the parent group should be inherited.
     */
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

    @Override
    public String toString() {
        return getName();
    }

}
