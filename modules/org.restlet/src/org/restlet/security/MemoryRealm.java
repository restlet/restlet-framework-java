/**
 * Copyright 2005-2011 Noelios Technologies.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.ClientInfo;
import org.restlet.engine.security.RoleMapping;

/**
 * Security realm based on a memory model. The model is composed of root groups,
 * users and mapping to associated roles.
 * 
 * @author Jerome Louvel
 */
public class MemoryRealm extends Realm {

    /**
     * Enroler based on the default security model.
     */
    private class DefaultEnroler implements Enroler {

        public void enrole(ClientInfo clientInfo) {
            User user = findUser(clientInfo.getUser().getIdentifier());

            if (user != null) {
                // Find all the inherited groups of this user
                Set<Group> userGroups = findGroups(user);

                // Add roles specific to this user
                Set<Role> userRoles = findRoles(user);

                for (Role role : userRoles) {
                    clientInfo.getRoles().add(role);
                }

                // Add roles common to group members
                Set<Role> groupRoles = findRoles(userGroups);

                for (Role role : groupRoles) {
                    clientInfo.getRoles().add(role);
                }
            }
        }
    }

    /**
     * Verifier based on the default security model. It looks up users in the
     * mapped organizations.
     */
    private class DefaultVerifier extends LocalVerifier {

        @Override
        public char[] getLocalSecret(String identifier) {
            char[] result = null;
            User user = findUser(identifier);

            if (user != null) {
                result = user.getSecret();
            }

            return result;
        }
    }

    /** The modifiable list of role mappings. */
    private final List<RoleMapping> roleMappings;

    /** The modifiable list of root groups. */
    private final List<Group> rootGroups;

    /** The modifiable list of users. */
    private final List<User> users;

    /**
     * Constructor.
     */
    public MemoryRealm() {
        setVerifier(new DefaultVerifier());
        setEnroler(new DefaultEnroler());
        this.rootGroups = new CopyOnWriteArrayList<Group>();
        this.roleMappings = new CopyOnWriteArrayList<RoleMapping>();
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
                boolean inherit = !inheritOnly
                        || currentGroup.isInheritingRoles();
                Group group;

                for (int i = stack.size() - 2; inherit && (i >= 0); i--) {
                    group = stack.get(i);
                    userGroups.add(group);
                    inherit = !inheritOnly || group.isInheritingRoles();
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
     * Finds the roles mapped to given user group.
     * 
     * @param userGroup
     *            The user group.
     * @return The roles found.
     */
    public Set<Role> findRoles(Group userGroup) {
        Set<Role> result = new HashSet<Role>();

        Object source;
        for (RoleMapping mapping : getRoleMappings()) {
            source = mapping.getSource();

            if ((userGroup != null) && userGroup.equals(source)) {
                result.add(mapping.getTarget());
            }
        }

        return result;
    }

    /**
     * Finds the roles mapped to given user groups.
     * 
     * @param userGroups
     *            The user groups.
     * @return The roles found.
     */
    public Set<Role> findRoles(Set<Group> userGroups) {
        Set<Role> result = new HashSet<Role>();

        Object source;
        for (RoleMapping mapping : getRoleMappings()) {
            source = mapping.getSource();

            if ((userGroups != null) && userGroups.contains(source)) {
                result.add(mapping.getTarget());
            }
        }

        return result;
    }

    /**
     * Finds the roles mapped to a given user.
     * 
     * @param user
     *            The user.
     * @return The roles found.
     */
    public Set<Role> findRoles(User user) {
        Set<Role> result = new HashSet<Role>();

        Object source;
        for (RoleMapping mapping : getRoleMappings()) {
            source = mapping.getSource();

            if ((user != null) && user.equals(source)) {
                result.add(mapping.getTarget());
            }
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
     * Returns the modifiable list of role mappings.
     * 
     * @return The modifiable list of role mappings.
     */
    private List<RoleMapping> getRoleMappings() {
        return roleMappings;
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
     * Maps a group defined in a component to a role defined in the application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    public void map(Group group, Role role) {
        getRoleMappings().add(new RoleMapping(group, role));
    }

    /**
     * Maps a user defined in a component to a role defined in the application.
     * 
     * @param user
     *            The source user.
     * @param role
     *            The target role.
     */
    public void map(User user, Role role) {
        getRoleMappings().add(new RoleMapping(user, role));
    }

    /**
     * Sets the modifiable list of root groups. This method clears the current
     * list and adds all entries in the parameter list.
     * 
     * @param rootGroups
     *            A list of root groups.
     */
    public void setRootGroups(List<Group> rootGroups) {
        synchronized (getRootGroups()) {
            if (rootGroups != getRootGroups()) {
                getRootGroups().clear();

                if (rootGroups != null) {
                    getRootGroups().addAll(rootGroups);
                }
            }
        }
    }

    /**
     * Sets the modifiable list of users. This method clears the current list
     * and adds all entries in the parameter list.
     * 
     * @param users
     *            A list of users.
     */
    public void setUsers(List<User> users) {
        synchronized (getUsers()) {
            if (users != getUsers()) {
                getUsers().clear();

                if (users != null) {
                    getUsers().addAll(users);
                }
            }
        }
    }

    /**
     * Unmaps a group defined in a component from a role defined in the
     * application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    public void unmap(Group group, Role role) {
        unmap((Object) group, role);
    }

    /**
     * Unmaps an element (user, group or organization) defined in a component
     * from a role defined in the application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    private void unmap(Object source, Role role) {
        RoleMapping mapping;
        for (int i = getRoleMappings().size(); i >= 0; i--) {
            mapping = getRoleMappings().get(i);

            if (mapping.getSource().equals(source)
                    && mapping.getTarget().equals(role)) {
                getRoleMappings().remove(i);
            }
        }
    }

    /**
     * Unmaps a user defined in a component from a role defined in the
     * application.
     * 
     * @param user
     *            The source user.
     * @param role
     *            The target role.
     */
    public void unmap(User user, Role role) {
        unmap((Object) user, role);
    }

}
