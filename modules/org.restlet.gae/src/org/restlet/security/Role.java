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
 * Application specific role.
 * 
 * @author Jerome Louvel
 */
public class Role {

    /**
     * Unmodifiable role that covers all existing roles. Its name is "*" by
     * convention.
     */
    public static final Role ALL = new Role("*",
            "Role that covers all existing roles.") {
        @Override
        public void setDescription(String description) {
            throw new IllegalStateException("Unmodifiable role");
        }

        @Override
        public void setName(String name) {
            throw new IllegalStateException("Unmodifiable role");
        }

    };

    /** The modifiable list of child roles. */
    private List<Role> childRoles;

    /** The modifiable list of denied permissions. */
    private List<Permission> deniedPermissions;

    /** The description. */
    private volatile String description;

    /** The modifiable list of granted permissions. */
    private List<Permission> grantedPermissions;

    /** Indicates if the permissions are inherited from the parent role. */
    private boolean inheritPermissions;

    /** The name. */
    private volatile String name;

    /**
     * Default constructor.
     */
    public Role() {
        this(null, null);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
     */
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
        this.childRoles = new CopyOnWriteArrayList<Role>();
    }

    /**
     * Returns the modifiable list of child roles.
     * 
     * @return The modifiable list of child roles.
     */
    public List<Role> getChildRoles() {
        return childRoles;
    }

    /**
     * Returns the modifiable list of denied permissions.
     * 
     * @return The modifiable list of denied permissions.
     */
    public List<Permission> getDeniedPermissions() {
        return deniedPermissions;
    }

    /**
     * Returns the description.
     * 
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the modifiable list of granted permissions.
     * 
     * @return The modifiable list of granted permissions.
     */
    public List<Permission> getGrantedPermissions() {
        return grantedPermissions;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates if the permissions are inherited from the parent role.
     * 
     * @return True if the permissions are inherited
     */
    public boolean isInheritPermissions() {
        return inheritPermissions;
    }

    /**
     * Sets the list of child roles.
     * 
     * @param children
     *            The list of child roles.
     */
    public void setChildRoles(List<Role> children) {
        this.childRoles.clear();

        if (children != null) {
            this.childRoles.addAll(children);
        }
    }

    /**
     * Sets the modifiable list of denied permissions.
     * 
     * @param deniedPermissions
     *            The modifiable list of denied permissions.
     */
    public void setDeniedPermissions(List<Permission> deniedPermissions) {
        this.deniedPermissions = deniedPermissions;
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
     * Sets the modifiable list of granted permissions.
     * 
     * @param grantedPermissions
     *            The modifiable list of granted permissions.
     */
    public void setGrantedPermissions(List<Permission> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }

    /**
     * Indicates if the permissions are inherited from the parent role.
     * 
     * @param inheritPermissions
     *            True if the permissions are inherited.
     */
    public void setInheritPermissions(boolean inheritPermissions) {
        this.inheritPermissions = inheritPermissions;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
