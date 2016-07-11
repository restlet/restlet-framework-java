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

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/**
 * Filter authenticating the client sending the inbound request. Its main role
 * is to inspect various credentials provided by the client and to add related
 * application roles to the request's {@link ClientInfo} property.
 * 
 * @author Jerome Louvel
 */
public abstract class Authenticator extends Filter {

    /**
     * Invoked upon successful authentication to update the subject with new
     * principals.
     */
    private volatile Enroler enroler;

    /**
     * Indicates if the authenticator should attempt to authenticate an already
     * authenticated client. By default, it is set to true.
     */
    private volatile boolean multiAuthenticating;

    /**
     * Indicates if the authenticator is not required to succeed. In those
     * cases, the attached Restlet is invoked. Note that authentication will be
     * attempted independently of this property unless the client is already
     * authenticated and the {@link #isMultiAuthenticating()} prevents multiple
     * authentications.
     */
    private volatile boolean optional;

    /**
     * Constructor setting the mode to "required".
     * 
     * @param context
     *            The context.
     * @see #Authenticator(Context, boolean)
     */
    public Authenticator(Context context) {
        this(context, false);
    }

    /**
     * Constructor using the context's default enroler.
     * 
     * @param context
     *            The context.
     * @param optional
     *            Indicates if the authenticator is not required to succeed.
     * @see #Authenticator(Context, boolean, Enroler)
     */
    public Authenticator(Context context, boolean optional) {
        this(context, optional, (context != null) ? context.getDefaultEnroler()
                : null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param multiAuthenticating
     *            Indicates if the authenticator should attempt to authenticate
     *            an already authenticated client.
     * @param optional
     *            Indicates if the authenticator is not required to succeed.
     * @param enroler
     *            The enroler to invoke upon successful authentication.
     */
    public Authenticator(Context context, boolean multiAuthenticating,
            boolean optional, Enroler enroler) {
        super(context);
        this.multiAuthenticating = multiAuthenticating;
        this.optional = optional;
        this.enroler = enroler;
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param optional
     *            Indicates if the authenticator is not required to succeed.
     * @param enroler
     *            The enroler to invoke upon successful authentication.
     */
    public Authenticator(Context context, boolean optional, Enroler enroler) {
        this(context, true, optional, enroler);
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
     * Invoked upon successful authentication. By default, it updates the
     * request's clientInfo and challengeResponse "authenticated" properties,
     * clears the existing challenge requests on the response, calls the enroler
     * and finally returns {@link Filter#CONTINUE}.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return The filter continuation code.
     */
    protected int authenticated(Request request, Response response) {
        boolean loggable = request.isLoggable()
                && getLogger().isLoggable(Level.FINE);

        if (loggable && request.getChallengeResponse() != null) {
            getLogger().log(
                    Level.FINE,
                    "The authentication succeeded for the identifer \""
                            + request.getChallengeResponse().getIdentifier()
                            + "\" using the "
                            + request.getChallengeResponse().getScheme()
                            + " scheme.");
        }

        // Update the client info accordingly
        if (request.getClientInfo() != null) {
            request.getClientInfo().setAuthenticated(true);
        }

        // Clear previous challenge requests
        response.getChallengeRequests().clear();

        // Add the roles for the authenticated subject
        if (getEnroler() != null) {
            getEnroler().enrole(request.getClientInfo());
        }

        return CONTINUE;
    }

    /**
     * Handles the authentication by first invoking the
     * {@link #authenticate(Request, Response)} method, only if
     * {@link #isMultiAuthenticating()} returns true and if
     * {@link ClientInfo#isAuthenticated()} returns false. If the method is
     * invoked and returns true, the {@link #authenticated(Request, Response)}
     * is called. Otherwise, if {@link #isOptional()} returns true it continues
     * to the next Restlet or if it returns false it calls the
     * {@link #unauthenticated(Request, Response)} method.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (isMultiAuthenticating()
                || !request.getClientInfo().isAuthenticated()) {
            if (authenticate(request, response)) {
                return authenticated(request, response);
            } else if (isOptional()) {
                response.setStatus(Status.SUCCESS_OK);
                return CONTINUE;
            } else {
                return unauthenticated(request, response);
            }
        } else {
            return CONTINUE;
        }
    }

    /**
     * Returns the enroler invoked upon successful authentication to update the
     * subject with new principals. Typically new {@link Role} are added based
     * on the available {@link User} instances available.
     * 
     * @return The enroler invoked upon successful authentication
     */
    public Enroler getEnroler() {
        return enroler;
    }

    /**
     * Indicates if the authenticator should attempt to authenticate an already
     * authenticated client. The client is considered authenticated if
     * {@link ClientInfo#isAuthenticated()} returns true. By default, it is set
     * to true.
     * 
     * @return True if the authenticator should attempt to authenticate an
     *         already authenticated client.
     */
    public boolean isMultiAuthenticating() {
        return multiAuthenticating;
    }

    /**
     * Indicates if the authenticator is not required to succeed. In those
     * cases, the attached Restlet is invoked. Note that authentication will be
     * attempted independently of this property unless the client is already
     * authenticated and the {@link #isMultiAuthenticating()} prevents multiple
     * authentications.
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
     * Indicates if the authenticator should attempt to authenticate an already
     * authenticated client. The client is considered authenticated if
     * {@link ClientInfo#isAuthenticated()} returns true. By default, it is set
     * to true.
     * 
     * @param multiAuthenticating
     *            True if the authenticator should attempt to authenticate an
     *            already authenticated client.
     */
    public void setMultiAuthenticating(boolean multiAuthenticating) {
        this.multiAuthenticating = multiAuthenticating;
    }

    /**
     * Indicates if the authenticator is not required to succeed. In those
     * cases, the attached Restlet is invoked. Note that authentication will be
     * attempted independently of this property unless the client is already
     * authenticated and the {@link #isMultiAuthenticating()} prevents multiple
     * authentications.
     * 
     * @param optional
     *            True if the authentication success is optional.
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * Invoked upon failed authentication. By default, it updates the request's
     * clientInfo and challengeResponse "authenticated" properties, and returns
     * {@link Filter#STOP}.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return The filter continuation code.
     */
    protected int unauthenticated(Request request, Response response) {
        boolean loggable = request.isLoggable()
                && getLogger().isLoggable(Level.FINE);

        if (request.getChallengeResponse() != null && loggable) {
            getLogger().log(
                    Level.FINE,
                    "The authentication failed for the identifer \""
                            + request.getChallengeResponse().getIdentifier()
                            + "\" using the "
                            + request.getChallengeResponse().getScheme()
                            + " scheme.");
        }

        // Update the client info accordingly
        if (request.getClientInfo() != null) {
            request.getClientInfo().setAuthenticated(false);
        }

        // Stop the filtering chain
        return STOP;
    }

}
