/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Filter guarding the access to an attached Restlet.
 * 
 * @see <a href="http://www.restlet.org/tutorial#part09">Tutorial: Guarding
 *      access to sensitive resources</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Guard extends Filter {
    /** Map of secrets (login/password combinations). */
    private final Map<String, char[]> secrets;

    /** The authentication scheme. */
    private ChallengeScheme scheme;

    /** The authentication realm. */
    private String realm;

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param scheme
     *                The authentication scheme to use.
     * @param realm
     *                The authentication realm.
     */
    public Guard(Context context, ChallengeScheme scheme, String realm) {
        super(context);
        this.secrets = new ConcurrentHashMap<String, char[]>();

        if ((scheme == null)) {
            throw new IllegalArgumentException(
                    "Please specify an authentication scheme. Use the 'None' challenge if no authentication is required.");
        } else {
            this.scheme = scheme;
            this.realm = realm;
        }
    }

    /**
     * Accepts the call. By default, it is invoked it the request is
     * authenticated and authorized, and asks the attached Restlet to handle the
     * call.
     * 
     * @param request
     *                The request to accept.
     * @param response
     *                The response to accept.
     */
    public void accept(Request request, Response response) {
        // Invoke the attached Restlet
        super.doHandle(request, response);
    }

    /**
     * Indicates if the call is properly authenticated. By default, this
     * delegates credential checking to checkSecret().
     * 
     * @param request
     *                The request to authenticate.
     * @return -1 if the given credentials were invalid, 0 if no credentials
     *         were found and 1 otherwise.
     * @see #checkSecret(String, char[])
     */
    public int authenticate(Request request) {
        int result = 0;

        if (this.scheme != null) {
            // An authentication scheme has been defined,
            // the request must be authenticated
            ChallengeResponse cr = request.getChallengeResponse();

            if (cr != null) {
                if (this.scheme.equals(cr.getScheme())) {
                    // The challenge schemes are compatible
                    String identifier = request.getChallengeResponse()
                            .getIdentifier();
                    char[] secret = request.getChallengeResponse().getSecret();

                    // Check the credentials
                    if ((identifier != null) && (secret != null)) {
                        result = checkSecret(identifier, secret) ? 1 : -1;
                    }
                } else {
                    // The challenge schemes are incompatible, we need to
                    // challenge the client
                }
            } else {
                // No challenge response found, we need to challenge the client
            }
        }

        return result;
    }

    /**
     * Indicates if the secret is valid for the given identifier. By default,
     * this returns true given the correct login/password couple as verified via
     * the findSecret() method.
     * 
     * @param identifier
     *                the identifier
     * @param secret
     *                the identifier's secret
     * @return true if the secret is valid for the given identifier
     */
    protected boolean checkSecret(String identifier, char[] secret) {
        boolean result = false;
        char[] secret2 = findSecret(identifier);
        if (secret == null || secret2 == null) {
            // check if both are null
            result = (secret == secret2);
        } else {
            if (secret.length == secret2.length) {
                boolean equals = true;
                for (int i = 0; i < secret.length && equals; i++) {
                    equals = (secret[i] == secret2[i]);
                }
                result = equals;
            }
        }

        return result;
    }

    /**
     * Indicates if the request is authorized to pass through the Guard. This
     * method is only called if the call was sucessfully authenticated. It
     * always returns true by default. If specific checks are required, they
     * could be added by overriding this method.
     * 
     * @param request
     *                The request to authorize.
     * @return True if the request is authorized.
     */
    public boolean authorize(Request request) {
        return true;
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *                The response to update.
     */
    public void challenge(Response response) {
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        response.setChallengeRequest(new ChallengeRequest(this.scheme,
                this.realm));
    }

    /**
     * Handles the call by distributing it to the next Restlet.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    public void doHandle(Request request, Response response) {
        switch (authenticate(request)) {
        case 1:
            // Valid credentials provided
            if (authorize(request)) {
                accept(request, response);
            } else {
                forbid(response);
            }
            break;
        case 0:
            // No credentials provided
            challenge(response);
            break;
        case -1:
            // Wrong credentials provided
            forbid(response);
            break;
        }
    }

    /**
     * Finds the secret associated to a given identifier. By default it looks up
     * into the secrets map, but this behavior can be overriden.
     * 
     * @param identifier
     *                The identifier to lookup.
     * @return The secret associated to the identifier or null.
     */
    protected char[] findSecret(String identifier) {
        return getSecrets().get(identifier);
    }

    /**
     * Rejects the call due to a failed authentication or authorization. This
     * can be overriden to change the defaut behavior, for example to display an
     * error page. By default, if authentication is required, the challenge
     * method is invoked, otherwise the call status is set to
     * CLIENT_ERROR_FORBIDDEN.
     * 
     * @param response
     *                The reject response.
     */
    public void forbid(Response response) {
        response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    /**
     * Returns the map of identifiers and secrets.
     * 
     * @return The map of identifiers and secrets.
     */
    public Map<String, char[]> getSecrets() {
        return this.secrets;
    }

}
