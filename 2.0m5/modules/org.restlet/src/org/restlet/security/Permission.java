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

import java.security.BasicPermission;

/**
 * Generic Restlet permission that gives fine grained rights to its owner,
 * typically a role granted to the authenticated user.
 * 
 * @author Jerome Louvel
 */
public class Permission extends BasicPermission {

    private static final long serialVersionUID = 1L;

    /** The list of authorized actions. */
    private volatile String actions;

    /**
     * Constructor.
     * 
     * @param name
     *            The {@link Authorizer} identifier.
     * @param actions
     *            The list of authorized actions.
     */
    public Permission(String name, String actions) {
        super(name);
        this.actions = actions;
    }

    /**
     * Returns the canonical string representation of the actions. The actions
     * are separated by a comma and sorted alphabetically.
     * 
     * @return The list of authorized uniform methods.
     */
    @Override
    public String getActions() {
        return this.actions;
    }

}
