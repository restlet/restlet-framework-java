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

package org.restlet.ext.gae;

import org.restlet.data.ClientInfo;
import org.restlet.security.Enroler;
import org.restlet.security.Role;

import com.google.appengine.api.users.UserServiceFactory;

/**
 * Enroler that adds a Restlet Role object to the client info if the GAE API
 * reports that the user is an administrator.
 * 
 * @author Matt Kennedy
 */
public class GaeEnroler implements Enroler {

    /** The Administrator role. */
    private Role adminRole;

    /**
     * Default constructor. It defines an administrator role, which name is
     * "admin".
     */
    public GaeEnroler() {
        this("admin", "Administrator of the current application.");
    }

    /**
     * Constructor.
     * 
     * @param adminRole
     *            The administrator role.
     */
    public GaeEnroler(Role adminRole) {
        setAdminRole(adminRole);
    }

    /**
     * Constructor.
     * 
     * @param adminRoleName
     *            The name of the administrator role.
     */
    public GaeEnroler(String adminRoleName) {
        this(adminRoleName, "Administrator of the current application.");
    }

    /**
     * Constructor.
     * 
     * @param adminRoleName
     *            The name of the administrator role.
     * @param adminRoleDescription
     *            The description of the administrator role.
     */
    public GaeEnroler(String adminRoleName, String adminRoleDescription) {
        this(new Role(adminRoleName, adminRoleDescription));
    }

    /**
     * Adds admin role object if user is an administrator according to Google
     * App Engine UserService.
     * 
     * @see org.restlet.security.Enroler#enrole(org.restlet.data.ClientInfo)
     */
    public void enrole(ClientInfo info) {
        if (UserServiceFactory.getUserService().isUserAdmin()
                && getAdminRole() != null) {
            info.getRoles().add(getAdminRole());
        }
    }

    /**
     * Returns the administrator role.
     * 
     * @return The administrator role.
     */
    public Role getAdminRole() {
        return adminRole;
    }

    /**
     * Sets the administrator role.
     * 
     * @param adminRole
     *            The administrator role.
     */
    public void setAdminRole(Role adminRole) {
        this.adminRole = adminRole;
    }

}
