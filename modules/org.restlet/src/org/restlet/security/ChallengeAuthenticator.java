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

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.authentication.AuthenticationHelper;

/**
 * Authenticator based on a challenge scheme such as HTTP Basic.
 * 
 * @author Jerome Louvel
 */
public class ChallengeAuthenticator extends Authenticator {

    /** Indicates that an authentication response is considered invalid. */
    public static final int AUTHENTICATION_INVALID = -1;

    /** Indicates that an authentication response couldn't be found. */
    public static final int AUTHENTICATION_MISSING = 0;

    /** Indicates that an authentication response is stale. */
    public static final int AUTHENTICATION_STALE = 2;

    /** Indicates that an authentication response is valid. */
    public static final int AUTHENTICATION_VALID = 1;

    /** The authentication realm. */
    private volatile String realm;

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
     * @param context
     *            The context.
     * @param challengeScheme
     *            The authentication scheme to use.
     * @param realm
     *            The authentication realm.
     */
    public ChallengeAuthenticator(Context context,
            ChallengeScheme challengeScheme, String realm) {
        this(context, MODE_REQUIRED, challengeScheme, realm);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param mode
     *            The authentication mode. See MODE_* constants.
     * @param challengeScheme
     *            The authentication scheme to use.
     * @param realm
     *            The authentication realm.
     */
    public ChallengeAuthenticator(Context context, int mode,
            ChallengeScheme challengeScheme, String realm) {
        super(context, mode);
        this.realm = realm;
        this.scheme = challengeScheme;
        this.verifier = context.getVerifier();
    }

    /**
     * Indicates if the request is properly authenticated. By default, this
     * delegates credential checking to checkSecret().
     * 
     * @param request
     *            The request to authenticate.
     * @return -1 if the given credentials were invalid, 0 if no credentials
     *         were found and 1 otherwise.
     * @see Guard#checkSecret(Request, String, char[])
     */
    protected int authenticate(ChallengeResponse cr) {
        int result = Guard.AUTHENTICATION_MISSING;

        if (getScheme() != null) {
            // An authentication scheme has been defined,
            // the request must be authenticated
            if (cr != null) {
                if (getScheme().equals(cr.getScheme())) {
                    final AuthenticationHelper helper = Engine.getInstance()
                            .findHelper(cr.getScheme(), false, true);

                    if (helper != null) {
                        // result = helper.authenticate(cr, request, guard);
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

        if (cr != null) {
            // Update the challenge response accordingly
            cr.setAuthenticated(result == Guard.AUTHENTICATION_VALID);
        }

        return result;
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        boolean result = false;
        final boolean loggable = getLogger().isLoggable(Level.FINE);

        switch (authenticate(request.getChallengeResponse())) {
        case AUTHENTICATION_VALID:
            // Valid credentials provided
            ChallengeResponse challengeResponse = request
                    .getChallengeResponse();
            result = true;

            if (loggable) {
                if (challengeResponse != null) {
                    getLogger().fine(
                            "Authentication succeeded. Valid credentials provided for identifier: "
                                    + request.getChallengeResponse()
                                            .getIdentifier() + ".");
                } else {
                    getLogger()
                            .fine(
                                    "Authentication succeeded. Valid credentials provided.");
                }
            }
            break;
        case AUTHENTICATION_MISSING:
            // No credentials provided
            if (loggable) {
                getLogger().fine(
                        "Authentication failed. No credentials provided.");
            }

            challenge(response, false);
            break;
        case AUTHENTICATION_INVALID:
            // Invalid credentials provided
            if (loggable) {
                getLogger().fine(
                        "Authentication failed. Invalid credentials provided.");
            }

            if (isRechallengeEnabled()) {
                challenge(response, false);
            } else {
                forbid(response);
            }
            break;
        case AUTHENTICATION_STALE:
            if (loggable) {
                getLogger().fine(
                        "Authentication failed. Stale credentials provided.");
            }

            challenge(response, true);
            break;
        }

        return result;
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
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        response.setChallengeRequest(new ChallengeRequest(getScheme(),
                getRealm()));
    }

    /**
     * Rejects the call due to a failed authentication or authorization. This
     * can be overriden to change the defaut behavior, for example to display an
     * error page. By default, if authentication is required, the challenge
     * method is invoked, otherwise the call status is set to
     * CLIENT_ERROR_FORBIDDEN.
     * 
     * @param response
     *            The reject response.
     */
    public void forbid(Response response) {
        response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    /**
     * Returns the authentication realm.
     * 
     * @return The authentication realm.
     */
    public String getRealm() {
        return this.realm;
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
     * Sets the authentication realm.
     * 
     * @param realm
     *            The authentication realm.
     */
    public void setRealm(String realm) {
        this.realm = realm;
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
