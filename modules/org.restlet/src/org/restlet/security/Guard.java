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

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Filter guarding the access to an attached Restlet. More concretely, it guards
 * from unauthenticated and unauthorized requests, providing facilities to check
 * credentials such as passwords. It is also a relatively generic class which
 * can work with several authentication schemes such as HTTP Basic and HTTP
 * Digest.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://www.restlet.org/documentation/1.2/tutorial#part09">Tutorial:
 *      Guarding access to sensitive resources</a>
 * @author Jerome Louvel
 */
public class Guard extends Filter {

    /** The authenticator. */
    private Authenticator authenticator;

    /** The authorizer. */
    private Authorizer authorizer;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param authenticator
     *            The authenticator.
     * @param authorizer
     *            The authorizer.
     */
    public Guard(Context context, Authenticator authenticator,
            Authorizer authorizer) {
        super(context);
        setAuthenticator(authenticator);
        setAuthorizer(authorizer);
    }

    /**
     * Authentication and Authorization Enforcement Point. If an authenticator
     * is set, then it asks it to handle the request. If an authorizer is set,
     * then it asks it to handle the request. If after those two optional steps,
     * the response is still not in error, then it asks the Restlet attached to
     * this filter to handle the call.<br>
     * <br>
     * Note that by default, if no authenticator and no authorizer is set, and
     * the response is not in error, then the request goes directly to the
     * attached Restlet.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (response.getStatus().isSuccess() && (getAuthenticator() != null)) {
            // Authentication phase
            getAuthenticator().handle(request, response);
        }

        if (response.getStatus().isSuccess() && (getAuthorizer() != null)) {
            // Authorization phase
            getAuthorizer().handle(request, response);
        }

        return response.getStatus().isError() ? STOP : CONTINUE;
    }

    /**
     * Returns the authenticator Restlet.
     * 
     * @return The authenticator Restlet.
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Returns the authorizer Restlet.
     * 
     * @return The authorizer Restlet.
     */
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * Sets the authenticator Restlet.
     * 
     * @param authenticator
     *            The authenticator Restlet.
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;

        Filter next = authenticator;
        while (next != null) {
            if (next.getNext() == null) {
                next.setNext(new Restlet() {
                    // Empty Restlet to prevent changes on the response status
                });
                next = null;
            } else if (next.getNext() instanceof Filter) {
                next = (Filter) next.getNext();
            } else {
                next = null;
            }
        }
    }

    /**
     * Sets the authorizer Restlet.
     * 
     * @param authorizer
     *            The authorizer Restlet.
     */
    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;

        Filter next = authorizer;
        while (next != null) {
            if (next.getNext() == null) {
                next.setNext(new Restlet() {
                    // Empty Restlet to prevent changes on the response status
                });
                next = null;
            } else if (next.getNext() instanceof Filter) {
                next = (Filter) next.getNext();
            } else {
                next = null;
            }
        }
    }

}
