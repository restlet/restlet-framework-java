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
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Authenticator based on a challenge scheme such as HTTP Basic.
 * 
 * @author Jerome Louvel
 */
public class ChallengeAuthenticator extends Authenticator {

    /** The authentication realm. */
    private volatile String realm;

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations).
     */
    private volatile boolean rechallenge;

    /** The expected challenge scheme. */
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
     * Authenticates the call, relying on the verifier to check the credentials
     * provided (in general an identifier + secret couple).<br>
     * <br>
     * If the credentials are valid, the next Restlet attached is invoked.<br>
     * <br>
     * If the credentials are missing, then
     * {@link #challenge(Response, boolean)} is invoked.<br>
     * <br>
     * If the credentials are invalid and if the "rechallenge" property is true
     * then {@link #challenge(Response, boolean)} is invoked. Otherwise,
     * {@link #forbid(Response)} is invoked.<br>
     * <br>
     * If the credentials are stale, then {@link #challenge(Response, boolean)}
     * is invoked with the "stale" parameter to true.<br>
     * <br>
     * At the end of the process, the
     * {@link ChallengeResponse#setAuthenticated(boolean)} method is invoked.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     */
    @Override
    protected boolean authenticate(Request request, Response response) {
        boolean result = false;
        final boolean loggable = getLogger().isLoggable(Level.FINE);
        ChallengeResponse challengeResponse = request.getChallengeResponse();

        if (getVerifier() != null) {
            switch (getVerifier().verify(request, response)) {
            case Verifier.RESULT_VALID:
                // Valid credentials provided
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
            case Verifier.RESULT_MISSING:
                // No credentials provided
                if (loggable) {
                    getLogger().fine(
                            "Authentication failed. No credentials provided.");
                }

                challenge(response, false);
                break;
            case Verifier.RESULT_INVALID:
                // Invalid credentials provided
                if (loggable) {
                    getLogger()
                            .fine(
                                    "Authentication failed. Invalid credentials provided.");
                }

                if (isRechallenge()) {
                    challenge(response, false);
                } else {
                    forbid(response);
                }
                break;
            case Verifier.RESULT_STALE:
                if (loggable) {
                    getLogger()
                            .fine(
                                    "Authentication failed. Stale credentials provided.");
                }

                challenge(response, true);
                break;
            }
        } else {
            getLogger().warning("Authentication failed. No verifier provided.");
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
    public boolean isRechallenge() {
        return this.rechallenge;
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
     * @see #isRechallenge()
     */
    public void setRechallenge(boolean rechallengeEnabled) {
        this.rechallenge = rechallengeEnabled;
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
