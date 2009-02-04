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

import java.io.Serializable;

import org.restlet.Context;

/**
 * Principal corresponding to an authenticated user.
 * 
 * @author Jerome Louvel
 */
public class UserPrincipal implements java.security.Principal, Serializable {

    private static final long serialVersionUID = 1L;

    /** The underlying user. */
    private final User user;

    /**
     * Constructor.
     * 
     * @param user
     *            The underlying user.
     */
    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object another) {
        if (another == this) {
            return true;
        }
        if (!(another instanceof UserPrincipal)) {
            return false;
        }
        final UserPrincipal otherPrinc = (UserPrincipal) another;
        return getUser().equals(otherPrinc.getUser());
    }

    /**
     * Returns the user identifier.
     * 
     * @return The user identifier.
     */
    public String getName() {
        return getUser().getIdentifier();
    }

    /**
     * Returns the underlying user.
     * 
     * @return The underlying user.
     */
    private User getUser() {
        return user;
    }

    /**
     * Indicates if the user represented by this principal has been granted a
     * specific role in the given context. The context contains a mapping
     * between user and groups defined in a component, and roles defined in an
     * application.
     * 
     * @param context
     *            The mapping context.
     * @param role
     *            The role.
     * @return True if the user has been granted the specific role.
     */
    public boolean isInRole(Context context, Role role) {
        return context.isUserInRole(getUser(), role);
    }

    @Override
    public String toString() {
        return "User principal: " + getName();
    }

}
