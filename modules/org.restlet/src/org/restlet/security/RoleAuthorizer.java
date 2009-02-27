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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Authorizer based on authorized and forbidden roles. Note that if no role is
 * added to the "authorizedRoles" list, then only the "forbiddenRoles" list is
 * considered.
 * 
 * @author Jerome Louvel
 */
public class RoleAuthorizer extends Authorizer {

    /** The modifiable list of authorized roles. */
    private final List<Role> authorizedRoles;

    /** The modifiable list of forbidden roles. */
    private final List<Role> forbiddenRoles;

    /**
     * Default constructor.
     */
    public RoleAuthorizer() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier unique within an application.
     */
    public RoleAuthorizer(String identifier) {
        super(identifier);

        this.authorizedRoles = new CopyOnWriteArrayList<Role>();
        this.forbiddenRoles = new CopyOnWriteArrayList<Role>();
    }

    /**
     * Authorizes the request only if its subject is in one of the authorized
     * roles and in none of the forbidden ones.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return True if the authorization succeeded.
     */
    @Override
    public boolean authorize(Request request, Response response) {
        boolean authorized = false;
        boolean forbidden = false;

        // Verify if the subject is in one of the authorized roles
        if (getAuthorizedRoles().isEmpty()) {
            authorized = true;
        } else {
            for (Role authorizedRole : getAuthorizedRoles()) {
                authorized = authorized
                        || request.getClientInfo().isInRole(authorizedRole);
            }
        }

        // Verify if the subject is in one of the forbidden roles
        for (Role forbiddenRole : getForbiddenRoles()) {
            forbidden = forbidden
                    || request.getClientInfo().isInRole(forbiddenRole);
        }

        return authorized && !forbidden;
    }

    /**
     * Returns the modifiable list of authorized roles.
     * 
     * @return The modifiable list of authorized roles.
     */
    public List<Role> getAuthorizedRoles() {
        return authorizedRoles;
    }

    /**
     * Returns the modifiable list of forbidden roles.
     * 
     * @return The modifiable list of forbidden roles.
     */
    public List<Role> getForbiddenRoles() {
        return forbiddenRoles;
    }

}
