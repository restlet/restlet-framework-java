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

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.authentication.ChallengeAuthenticatorHelper;

/**
 * Authenticator based on a challenge scheme such as HTTP Basic.
 * 
 * @author Jerome Louvel
 */
public class ChallengeAuthenticator extends Authenticator {

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations).
     */
    private volatile boolean rechallengeEnabled;

    private final ChallengeScheme scheme;

    /** The secrets verifier (login/password combinations). */
    private Verifier verifier;

    /**
     * Constructor.
     * 
     * @param challengeScheme
     */
    public ChallengeAuthenticator(Context context, int mode,
            ChallengeScheme challengeScheme) {
        super(context, mode);
        this.scheme = challengeScheme;
        this.verifier = context.getVerifier();
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        int result = Guard.AUTHENTICATION_MISSING;

        if (getScheme() != null) {
            // An authentication scheme has been defined,
            // the request must be authenticated
            final ChallengeResponse cr = request.getChallengeResponse();

            if (cr != null) {
                if (getScheme().equals(cr.getScheme())) {
                    final ChallengeAuthenticatorHelper helper = Engine
                            .getInstance().findHelper(cr.getScheme(), false,
                                    true);

                    if (helper != null) {
                        result = Guard.AUTHENTICATION_MISSING;

                        // The challenge schemes are compatible
                        final String identifier = cr.getIdentifier();
                        final char[] secret = cr.getSecret();

                        // Check the credentials
                        if ((identifier != null) && (secret != null)) {
                            // result = getVerifier().verify(request, response)
                            // ? Guard.AUTHENTICATION_VALID
                            // : Guard.AUTHENTICATION_INVALID;
                        }
                    } else {
                        throw new IllegalArgumentException("Challenge scheme "
                                + getScheme()
                                + " not supported by the Restlet engine.");
                    }
                } else {
                    // The challenge schemes are incompatible, we need to
                    // challenge the client
                }
            } else {
                // No challenge response found, we need to challenge the client
            }
        }

        if (request.getChallengeResponse() != null) {
            // Update the challenge response accordingly
            request.getChallengeResponse().setAuthenticated(
                    result == Guard.AUTHENTICATION_VALID);
        }

        return true; // result;
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *            The response to update.
     * @param stale
     *            Indicates if the new challenge is due to a stale response.
     */
    public void challenge(Response response, boolean stale) {
    }

    /**
     * Returns the authentication challenge scheme.
     * 
     * @return The authentication challenge scheme.
     */
    public ChallengeScheme getScheme() {
        return scheme;
    }

    /**
     * Returns the secrets verifier.
     * 
     * @return The secrets verifier.
     */
    public Verifier getVerifier() {
        return verifier;
    }

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations). If set to
     * false, upon reception of invalid credentials, the Guard will forbid the
     * access ({@link Status#CLIENT_ERROR_FORBIDDEN}).
     * 
     * @return True if invalid credentials result in a new challenge.
     */
    public boolean isRechallengeEnabled() {
        return this.rechallengeEnabled;
    }

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received.
     * 
     * @param rechallengeEnabled
     *            True if invalid credentials result in a new challenge.
     * @see #isRechallengeEnabled()
     */
    public void setRechallengeEnabled(boolean rechallengeEnabled) {
        this.rechallengeEnabled = rechallengeEnabled;
    }

    /**
     * Sets the secrets verifier.
     * 
     * @param verifier
     *            The secrets verifier.
     */
    public void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }

}
