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

/**
 * Common class for all Restlet principals. Can be specialized to represent
 * users, roles, groups, etc.
 * 
 * @author Jerome Louvel
 */
public class Principal implements java.security.Principal, Serializable {

    private static final long serialVersionUID = 1L;

    /** The name. */
    private final String name;

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     */
    public Principal(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object another) {
        if (another == this) {
            return true;
        }
        if (!(another instanceof Principal)) {
            return false;
        }
        final Principal otherPrinc = (Principal) another;
        return getName().equals(otherPrinc.getName());
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Restlet Principal: " + getName();
    }

}
