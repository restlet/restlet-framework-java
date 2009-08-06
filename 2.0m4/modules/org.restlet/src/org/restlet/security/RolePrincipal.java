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

import java.io.Serializable;

/**
 * Principal corresponding to an application role.
 * 
 * @author Jerome Louvel
 */
public class RolePrincipal implements java.security.Principal, Serializable {

    private static final long serialVersionUID = 1L;

    /** The underlying role. */
    private final Role role;

    /**
     * Constructor.
     * 
     * @param role
     *            The underlying role.
     */
    public RolePrincipal(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object another) {
        if (another == this) {
            return true;
        }
        if (!(another instanceof RolePrincipal)) {
            return false;
        }
        final RolePrincipal otherPrinc = (RolePrincipal) another;
        return getRole().equals(otherPrinc.getRole());
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return getRole().getName();
    }

    /**
     * Returns the underlying role.
     * 
     * @return The underlying role.
     */
    private Role getRole() {
        return role;
    }

    /**
     * Indicates if the given role matches the underlying role of this
     * principal.
     * 
     * @param role
     *            The given role to test.
     * @return True if the given role matches the underlying role of this
     *         principal.
     */
    public boolean matches(Role role) {
        return getRole().equals(role);
    }

    @Override
    public String toString() {
        return "Role principal: " + getName();
    }

}
