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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.auth.Subject;

import org.restlet.Context;
import org.restlet.engine.security.RoleMapping;

/**
 * Security realm based on a memory model. The model is composed of
 * organizations.
 * 
 * @author Jerome Louvel
 */
public class MemoryRealm extends Realm {

    /**
     * Enroler based on the default security model.
     */
    private class DefaultEnroler extends Enroler {

        @Override
        public void enrole(Subject subject) {
            Set<UserPrincipal> userPrincipals = subject
                    .getPrincipals(UserPrincipal.class);

            for (UserPrincipal userPrincipal : userPrincipals) {
                Organization orga = findUserOrganization(userPrincipal
                        .getName());
                User user = findUser(orga, userPrincipal.getName());

                if (user != null) {
                    // Add a principal for the user roles
                    Set<Group> userGroups = orga.findGroups(user);
                    Set<Role> userRoles = findRoles(orga, userGroups, user);

                    for (Role role : userRoles) {
                        subject.getPrincipals().add(new RolePrincipal(role));
                    }
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
        protected char[] getSecret(String identifier) {
            char[] result = null;
            User user = findUser(identifier);

            if (user != null) {
                result = user.getSecret();
            }

            return result;
        }
    }

    /** The modifiable list of organizations. */
    private List<Organization> organizations;

    /** The modifiable list of role mappings. */
    private final List<RoleMapping> roleMappings;

    /**
     * Constructor.
     */
    public MemoryRealm() {
        setVerifier(new DefaultVerifier());
        setEnroler(new DefaultEnroler());
        this.organizations = new CopyOnWriteArrayList<Organization>();
        this.roleMappings = new CopyOnWriteArrayList<RoleMapping>();
    }

    /**
     * Finds an organization based on its domain name.
     * 
     * @param domainName
     *            The domain name to match.
     * @return The organization found.
     */
    public Organization findOrganization(String domainName) {
        for (Organization org : getOrganizations()) {
            if (org.getDomainName().equals(domainName)) {
                return org;
            }
        }

        return null;
    }

    /**
     * Finds the roles mapped to a specific user/groups/organization triple.
     * 
     * @param userOrganization
     *            The user organization.
     * @param userGroups
     *            The user groups.
     * @param user
     *            The user.
     * @return The roles found.
     */
    public Set<Role> findRoles(Organization userOrganization,
            Set<Group> userGroups, User user) {
        Set<Role> result = new HashSet<Role>();

        Object source;
        for (RoleMapping mapping : getRoleMappings()) {
            source = mapping.getSource();

            if (userOrganization.equals(source) || user.equals(source)
                    || userGroups.contains(source)) {
                result.add(mapping.getTarget());
            }
        }

        return result;
    }

    /**
     * Returns a user based on its (optionally qualified) identifier and the
     * mapped organization.
     * 
     * @param organization
     *            The user organization.
     * @param identifier
     *            The user identifier.
     * @return The matching user.
     */
    public User findUser(Organization organization, String identifier) {
        User result = null;

        // Parse qualified identifiers
        int at = identifier.indexOf('@');
        String userIdentifier = (at == -1) ? identifier : identifier.substring(
                0, at);

        // Lookup the user
        if (organization != null) {
            result = organization.findUser(userIdentifier);
        }

        return result;
    }

    /**
     * Returns a user based on its (optionally qualified) identifier and the
     * mapped organization.
     * 
     * @param identifier
     *            The user identifier.
     * @return The matching user.
     */
    public User findUser(String identifier) {
        return findUser(findUserOrganization(identifier), identifier);
    }

    /**
     * Returns a user based on its (optionally qualified) identifier and the
     * mapped organization.
     * 
     * @param identifier
     *            The user identifier.
     * @return The matching user.
     */
    public Organization findUserOrganization(String identifier) {
        Organization result = null;

        // Parse qualified identifiers
        int at = identifier.indexOf('@');
        String domainName = (at == -1) ? null : identifier.substring(at + 1);

        if (domainName == null) {
            if (getOrganizations().size() == 1) {
                result = getOrganizations().get(0);
            } else {
                Context
                        .getCurrentLogger()
                        .info(
                                "Unable to identify an unqualified user. Multiple organizations were bounded.");
            }
        } else {
            result = findOrganization(domainName);
        }

        return result;
    }

    /**
     * Returns the modifiable list of organizations.
     * 
     * @return The modifiable list of organizations.
     */
    public List<Organization> getOrganizations() {
        return organizations;
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
     * Maps an organization defined in a component to a role defined in the
     * application.
     * 
     * @param organization
     *            The source organization.
     * @param role
     *            The target role.
     */
    public void map(Organization organization, Role role) {
        getRoleMappings().add(new RoleMapping(organization, role));
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
     * Sets the list of organizations.
     * 
     * @param organizations
     *            The list of organizations.
     */
    public synchronized void setOrganizations(List<Organization> organizations) {
        this.organizations.clear();

        if (organizations != null) {
            this.organizations.addAll(organizations);
        }
    }

    /**
     * Sets the modifiable list of role mappings.
     * 
     * @param roleMappings
     *            The modifiable list of role mappings.
     */
    public void setRoleMappings(List<RoleMapping> roleMappings) {
        this.roleMappings.clear();

        if (roleMappings != null) {
            this.roleMappings.addAll(roleMappings);
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
     * Unmaps an organization defined in a component from a role defined in the
     * application.
     * 
     * @param organization
     *            The source organization.
     * @param role
     *            The target role.
     */
    public void unmap(Organization organization, Role role) {
        unmap((Object) organization, role);
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
