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

package org.restlet.security;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

/**
 * Authorizer based on authorized methods. Note that this authorizer makes the
 * difference between authenticated and anonymous users.
 * 
 * @author Jerome Louvel
 */
public class MethodAuthorizer extends Authorizer {

    /** The modifiable list of methods authorized for anonymous users. */
    private List<Method> anonymousMethods;

    /** The modifiable list of methods authorized for authenticated users. */
    private List<Method> authenticatedMethods;

    /**
     * Default constructor.
     */
    public MethodAuthorizer() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier unique within an application.
     */
    public MethodAuthorizer(String identifier) {
        super(identifier);

        this.anonymousMethods = new CopyOnWriteArrayList<Method>();
        this.authenticatedMethods = new CopyOnWriteArrayList<Method>();
    }

    /**
     * Authorizes the request only if its method is one of the authorized
     * methods.
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

        if (request.getClientInfo().isAuthenticated()) {
            // Verify if the request method is one of the forbidden methods
            for (Method authenticatedMethod : getAuthenticatedMethods()) {
                authorized = authorized
                        || request.getMethod().equals(authenticatedMethod);
            }
        } else {
            // Verify if the request method is one of the authorized methods
            for (Method authorizedMethod : getAnonymousMethods()) {
                authorized = authorized
                        || request.getMethod().equals(authorizedMethod);
            }
        }

        return authorized;
    }

    /**
     * Returns the modifiable list of methods authorized for anonymous users.
     * 
     * @return The modifiable list of methods authorized for anonymous users.
     */
    public List<Method> getAnonymousMethods() {
        return anonymousMethods;
    }

    /**
     * Returns the modifiable list of methods authorized for authenticated
     * users.
     * 
     * @return The modifiable list of methods authorized for authenticated
     *         users.
     */
    public List<Method> getAuthenticatedMethods() {
        return authenticatedMethods;
    }

    /**
     * Sets the modifiable list of methods authorized for anonymous users. This
     * method clears the current list and adds all entries in the parameter
     * list.
     * 
     * @param anonymousMethods
     *            A list of methods authorized for anonymous users.
     */
    public void setAnonymousMethods(List<Method> anonymousMethods) {
        synchronized (getAnonymousMethods()) {
            if (anonymousMethods != getAnonymousMethods()) {
                getAnonymousMethods().clear();

                if (anonymousMethods != null) {
                    getAnonymousMethods().addAll(anonymousMethods);
                }
            }
        }
    }

    /**
     * Sets the modifiable list of methods authorized for authenticated users.
     * This method clears the current list and adds all entries in the parameter
     * list.
     * 
     * @param authenticatedMethods
     *            A list of methods authorized for authenticated users.
     */
    public void setAuthenticatedMethods(List<Method> authenticatedMethods) {
        synchronized (getAuthenticatedMethods()) {
            if (authenticatedMethods != getAuthenticatedMethods()) {
                getAuthenticatedMethods().clear();

                if (authenticatedMethods != null) {
                    getAuthenticatedMethods().addAll(authenticatedMethods);
                }
            }
        }
    }

}
