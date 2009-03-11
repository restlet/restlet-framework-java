/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Organization that contains users and groups.
 * 
 * @author Jerome Louvel
 */
public class Organization {

    /** The description. */
    private volatile String description;

    /** The domain name (ex: "noelios.com"). */
    private volatile String domainName;

    /** The display name. */
    private volatile String name;

    /** The modifiable list of root groups. */
    private final List<Group> rootGroups;

    /** The modifiable list of users. */
    private final List<User> users;

    /**
     * Constructor.
     */
    public Organization() {
        this(null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The display name.
     * @param description
     *            The description.
     * @param domainName
     *            The domain name (ex: "noelios.com").
     */
    public Organization(String name, String description, String domainName) {
        this.name = name;
        this.description = description;
        this.domainName = domainName;
        this.rootGroups = new CopyOnWriteArrayList<Group>();
        this.users = new CopyOnWriteArrayList<User>();
    }

    /**
     * Recursively adds groups where a given user is a member.
     * 
     * @param user
     *            The member user.
     * @param userGroups
     *            The set of user groups to update.
     * @param currentGroup
     *            The current group to inspect.
     * @param stack
     *            The stack of ancestor groups.
     * @param inheritOnly
     *            Indicates if only the ancestors groups that have their
     *            "inheritRoles" property enabled should be added.
     */
    private void addGroups(User user, Set<Group> userGroups,
            Group currentGroup, List<Group> stack, boolean inheritOnly) {
        if ((currentGroup != null) && !stack.contains(currentGroup)) {
            stack.add(currentGroup);

            if (currentGroup.getMemberUsers().contains(user)) {
                userGroups.add(currentGroup);

                // Add the ancestor groups as well
                boolean inherit = !inheritOnly || currentGroup.isInheritRoles();
                Group group;

                for (int i = stack.size() - 2; inherit && (i >= 0); i--) {
                    group = stack.get(i);
                    userGroups.add(group);
                    inherit = !inheritOnly || group.isInheritRoles();
                }
            }

            for (Group group : currentGroup.getMemberGroups()) {
                addGroups(user, userGroups, group, stack, inheritOnly);
            }
        }
    }

    /**
     * Finds the set of groups where a given user is a member. Note that
     * inheritable ancestors groups are also returned.
     * 
     * @param user
     *            The member user.
     * @return The set of groups.
     */
    public Set<Group> findGroups(User user) {
        return findGroups(user, true);
    }

    /**
     * Finds the set of groups where a given user is a member.
     * 
     * @param user
     *            The member user.
     * @param inheritOnly
     *            Indicates if only the ancestors groups that have their
     *            "inheritRoles" property enabled should be added.
     * @return The set of groups.
     */
    public Set<Group> findGroups(User user, boolean inheritOnly) {
        Set<Group> result = new HashSet<Group>();
        List<Group> stack;

        // Recursively find user groups
        for (Group group : getRootGroups()) {
            stack = new ArrayList<Group>();
            addGroups(user, result, group, stack, inheritOnly);
        }

        return result;
    }

    /**
     * Finds a user in the organization based on its identifier.
     * 
     * @param userIdentifier
     *            The identifier to match.
     * @return The matched user or null.
     */
    public User findUser(String userIdentifier) {
        User result = null;
        User user;

        for (int i = 0; (result == null) && (i < getUsers().size()); i++) {
            user = getUsers().get(i);

            if (user.getIdentifier().equals(userIdentifier)) {
                result = user;
            }
        }

        return result;
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
     * Returns the domain name (ex: "noelios.com").
     * 
     * @return The domain name.
     */
    public String getDomainName() {
        return domainName;
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
     * Returns the modifiable list of root groups.
     * 
     * @return The modifiable list of root groups.
     */
    public List<Group> getRootGroups() {
        return rootGroups;
    }

    /**
     * Returns the modifiable list of users.
     * 
     * @return The modifiable list of users.
     */
    public List<User> getUsers() {
        return users;
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
     * Sets the domain name (ex: "noelios.com").
     * 
     * @param domainName
     *            The domain name.
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
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

    /**
     * Sets the list of root groups.
     * 
     * @param rootGroups
     *            The list of root groups.
     */
    public synchronized void setRootGroups(List<Group> rootGroups) {
        this.rootGroups.clear();

        if (rootGroups != null) {
            this.rootGroups.addAll(rootGroups);
        }
    }

    /**
     * Sets the list of users.
     * 
     * @param users
     *            The list of users.
     */
    public synchronized void setUsers(List<User> users) {
        this.users.clear();

        if (users != null) {
            this.users.addAll(users);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}
