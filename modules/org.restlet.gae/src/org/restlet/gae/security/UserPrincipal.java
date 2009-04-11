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

package org.restlet.gae.security;

import java.io.Serializable;

/**
 * Principal corresponding to an authenticated user.
 * 
 * @author Jerome Louvel
 */
public class UserPrincipal implements java.security.Principal, Serializable {

    private static final long serialVersionUID = 1L;

    /** The underlying user identifier. */
    private final String identifier;

    /**
     * Constructor.
     * 
     * @param identifier
     *            The underlying user identifier as the principal name.
     */
    public UserPrincipal(String identifier) {
        this.identifier = identifier;
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
        return getName().equals(otherPrinc.getName());
    }

    /**
     * Returns the user identifier as the principal name.
     * 
     * @return The user identifier.
     */
    public String getName() {
        return this.identifier;
    }

    @Override
    public String toString() {
        return "User principal: " + getName();
    }
}
