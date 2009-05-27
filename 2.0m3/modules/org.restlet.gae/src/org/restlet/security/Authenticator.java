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
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

/**
 * Filter authenticating the subject sending the request.
 * 
 * @author Jerome Louvel
 */
public abstract class Authenticator extends Filter {

    /**
     * Indicates if the authenticator is not required to succeed. In those
     * cases, the attached Restlet is invoked.
     */
    private volatile boolean optional;

    /**
     * Invoked upon successful authentication to update the subject with new
     * principals.
     */
    private volatile Enroler enroler;

    /**
     * Default constructor setting the mode to "required".
     */
    public Authenticator(Context context) {
        this(context, false);
    }

    /**
     * Constructor. Use the context's enroler by default.
     * 
     * @param optional
     *            The authentication mode.
     */
    public Authenticator(Context context, boolean optional) {
        this(context, optional, context.getEnroler());
    }

    /**
     * Constructor.
     * 
     * @param optional
     *            The authentication mode.
     * @param enroler
     *            The enroler to invoke upon successful authentication.
     */
    public Authenticator(Context context, boolean optional, Enroler enroler) {
        super(context);
        this.optional = optional;
        this.enroler = enroler;
    }

    /**
     * Attempts to authenticate the subject sending the request.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return True if the authentication succeeded.
     */
    protected abstract boolean authenticate(Request request, Response response);

    /**
     * Handles the authentication by first invoking the
     * {@link #authenticate(Request, Response)} method. Then, depending on the
     * result and the mode set, it either skips or invoke the (optionally)
     * attached Restlet.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected int beforeHandle(Request request, Response response) {
        int result = CONTINUE;
        boolean success = authenticate(request, response);

        if (success) {
            // Update the challenge response accordingly
            if (request.getChallengeResponse() != null) {
                request.getChallengeResponse().setAuthenticated(success);
            }

            // Update the client info accordingly
            if (request.getClientInfo() != null) {
                request.getClientInfo().setAuthenticated(success);
            }

            // Add the roles for the authenticated subject
            if (getEnroler() != null) {
                getEnroler().enrole(request.getClientInfo().getSubject());
            }
        } else if (!isOptional()) {
            // Update the challenge response accordingly
            if (request.getChallengeResponse() != null) {
                request.getChallengeResponse().setAuthenticated(success);
            }

            // Update the client info accordingly
            if (request.getClientInfo() != null) {
                request.getClientInfo().setAuthenticated(success);
            }

            // Stop the filtering chain
            result = STOP;
        }

        return result;
    }

    /**
     * Returns the enroler invoked upon successful authentication to update the
     * subject with new principals. Typically new {@link RolePrincipal} are
     * added based on the available {@link UserPrincipal} instances available.
     * 
     * @return The enroler invoked upon successful authentication
     */
    public Enroler getEnroler() {
        return enroler;
    }

    /**
     * Indicates if the authenticator is not required to succeed. In those
     * cases, the attached Restlet is invoked.
     * 
     * @return True if the authentication success is optional.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Sets the enroler invoked upon successful authentication.
     * 
     * @param enroler
     *            The enroler invoked upon successful authentication.
     */
    public void setEnroler(Enroler enroler) {
        this.enroler = enroler;
    }

    /**
     * Indicates if the authenticator is not required to succeed. In those
     * cases, the attached Restlet is invoked.
     * 
     * @param optional
     *            True if the authentication success is optional.
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

}
