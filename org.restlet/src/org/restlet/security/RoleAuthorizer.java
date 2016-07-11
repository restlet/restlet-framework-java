/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.security;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Request;
import org.restlet.Response;

/**
 * Authorizer based on authorized and forbidden roles. Note that if no role is
 * added to the "authorizedRoles" list, then only the "forbiddenRoles" list is
 * considered.
 * 
 * @author Jerome Louvel
 */
public class RoleAuthorizer extends Authorizer {

    /** The modifiable list of authorized roles. */
    private List<Role> authorizedRoles;

    /** The modifiable list of forbidden roles. */
    private List<Role> forbiddenRoles;

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
                        || request.getClientInfo().getRoles()
                                .contains(authorizedRole);
            }
        }

        // Verify if the subject is in one of the forbidden roles
        for (Role forbiddenRole : getForbiddenRoles()) {
            forbidden = forbidden
                    || request.getClientInfo().getRoles()
                            .contains(forbiddenRole);
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

    /**
     * Sets the modifiable list of authorized roles. This method clears the
     * current list and adds all entries in the parameter list.
     * 
     * @param authorizedRoles
     *            A list of authorized roles.
     */
    public void setAuthorizedRoles(List<Role> authorizedRoles) {
        synchronized (getAuthorizedRoles()) {
            if (authorizedRoles != getAuthorizedRoles()) {
                getAuthorizedRoles().clear();

                if (authorizedRoles != null) {
                    getAuthorizedRoles().addAll(authorizedRoles);
                }
            }
        }
    }

    /**
     * Sets the modifiable list of forbidden roles. This method clears the
     * current list and adds all entries in the parameter list.
     * 
     * @param forbiddenRoles
     *            A list of forbidden roles.
     */
    public void setForbiddenRoles(List<Role> forbiddenRoles) {
        synchronized (getForbiddenRoles()) {
            if (forbiddenRoles != getForbiddenRoles()) {
                getForbiddenRoles().clear();

                if (forbiddenRoles != null) {
                    getForbiddenRoles().addAll(forbiddenRoles);
                }
            }
        }
    }

}
