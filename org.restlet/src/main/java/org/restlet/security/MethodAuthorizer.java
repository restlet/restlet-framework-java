/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

/**
 * Authorizer based on authorized methods. Note that this authorizer makes the difference between
 * authenticated and anonymous users.
 *
 * @author Jerome Louvel
 */
public class MethodAuthorizer extends Authorizer {

    /** The modifiable list of methods authorized for anonymous users. */
    private List<Method> anonymousMethods;

    /** The modifiable list of methods authorized for authenticated users. */
    private List<Method> authenticatedMethods;

    /** Default constructor. */
    public MethodAuthorizer() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param identifier The identifier unique within an application.
     */
    public MethodAuthorizer(String identifier) {
        super(identifier);

        this.anonymousMethods = new CopyOnWriteArrayList<>();
        this.authenticatedMethods = new CopyOnWriteArrayList<>();
    }

    /**
     * Authorizes the request only if its method is one of the authorized methods.
     *
     * @param request The request sent.
     * @param response The response to modify.
     * @return True if the authorization succeeded.
     */
    @Override
    public boolean authorize(Request request, Response response) {
        boolean authorized = false;

        if (request.getClientInfo().isAuthenticated()) {
            // Verify if the request method is one of the forbidden methods
            for (Method authenticatedMethod : getAuthenticatedMethods()) {
                authorized = authorized || request.getMethod().equals(authenticatedMethod);
            }
        } else {
            // Verify if the request method is one of the authorized methods
            for (Method authorizedMethod : getAnonymousMethods()) {
                authorized = authorized || request.getMethod().equals(authorizedMethod);
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
     * Returns the modifiable list of methods authorized for authenticated users.
     *
     * @return The modifiable list of methods authorized for authenticated users.
     */
    public List<Method> getAuthenticatedMethods() {
        return authenticatedMethods;
    }

    /**
     * Sets the modifiable list of methods authorized for anonymous users. This method clears the
     * current list and adds all entries in the parameter list.
     *
     * @param anonymousMethods A list of methods authorized for anonymous users.
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
     * Sets the modifiable list of methods authorized for authenticated users. This method clears
     * the current list and adds all entries in the parameter list.
     *
     * @param authenticatedMethods A list of methods authorized for authenticated users.
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
