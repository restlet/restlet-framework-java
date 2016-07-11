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
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;

/**
 * Authenticator based on a challenge scheme. This is typically used to support
 * the HTTP BASIC and DIGEST challenge schemes.
 * 
 * @see ChallengeScheme
 * @see ChallengeRequest
 * @see ChallengeResponse
 * @see <a href="http://wiki.restlet.org/docs_2.2/112-restlet.html">User Guide -
 *      Authentication</a>
 * @author Jerome Louvel
 */
public class ChallengeAuthenticator extends Authenticator {

    /** The authentication realm. */
    private volatile String realm;

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations).
     */
    private volatile boolean rechallenging;

    /** The expected challenge scheme. */
    private final ChallengeScheme scheme;

    /** The credentials verifier. */
    private volatile Verifier verifier;

    /**
     * Constructor using the context's default verifier.
     * 
     * @param context
     *            The context.
     * @param optional
     *            Indicates if the authentication success is optional.
     * @param challengeScheme
     *            The authentication scheme to use.
     * @param realm
     *            The authentication realm.
     * 
     * @see #ChallengeAuthenticator(Context, boolean, ChallengeScheme, String,
     *      Verifier)
     */
    public ChallengeAuthenticator(Context context, boolean optional,
            ChallengeScheme challengeScheme, String realm) {
        this(context, optional, challengeScheme, realm,
                (context != null) ? context.getDefaultVerifier() : null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param optional
     *            Indicates if the authentication success is optional.
     * @param challengeScheme
     *            The authentication scheme to use.
     * @param realm
     *            The authentication realm.
     * @param verifier
     *            The credentials verifier.
     */
    public ChallengeAuthenticator(Context context, boolean optional,
            ChallengeScheme challengeScheme, String realm, Verifier verifier) {
        super(context, optional);
        this.realm = realm;
        this.rechallenging = true;
        this.scheme = challengeScheme;
        this.verifier = verifier;
    }

    /**
     * Constructor setting the optional property to false.
     * 
     * @param context
     *            The context.
     * @param challengeScheme
     *            The authentication scheme to use.
     * @param realm
     *            The authentication realm.
     * @see #ChallengeAuthenticator(Context, boolean, ChallengeScheme, String,
     *      Verifier)
     */
    public ChallengeAuthenticator(Context context,
            ChallengeScheme challengeScheme, String realm) {
        this(context, false, challengeScheme, realm);
    }

    /**
     * Authenticates the call, relying on the verifier to check the credentials
     * provided (in general an identifier + secret couple). If the credentials
     * are valid, the next Restlet attached is invoked.<br>
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
     * {@link ClientInfo#setAuthenticated(boolean)} method is invoked.
     */
    @Override
    protected boolean authenticate(Request request, Response response) {
        boolean result = false;
        boolean loggable = request.isLoggable()
                && getLogger().isLoggable(Level.FINE);

        if (getVerifier() != null) {
            switch (getVerifier().verify(request, response)) {
            case Verifier.RESULT_VALID:
                // Valid credentials provided
                result = true;

                if (loggable) {
                    ChallengeResponse challengeResponse = request
                            .getChallengeResponse();

                    if (challengeResponse != null) {
                        getLogger().fine(
                                "Authentication succeeded. Valid credentials provided for identifier: "
                                        + request.getChallengeResponse()
                                                .getIdentifier() + ".");
                    } else {
                        getLogger()
                                .fine("Authentication succeeded. Valid credentials provided.");
                    }
                }
                break;
            case Verifier.RESULT_MISSING:
                // No credentials provided
                if (loggable) {
                    getLogger().fine(
                            "Authentication failed. No credentials provided.");
                }

                if (!isOptional()) {
                    challenge(response, false);
                }
                break;
            case Verifier.RESULT_INVALID:
                // Invalid credentials provided
                if (loggable) {
                    getLogger()
                            .fine("Authentication failed. Invalid credentials provided.");
                }

                if (!isOptional()) {
                    if (isRechallenging()) {
                        challenge(response, false);
                    } else {
                        forbid(response);
                    }
                }
                break;
            case Verifier.RESULT_STALE:
                if (loggable) {
                    getLogger()
                            .fine("Authentication failed. Stale credentials provided.");
                }

                if (!isOptional()) {
                    challenge(response, true);
                }
                break;
            case Verifier.RESULT_UNKNOWN:
                if (loggable) {
                    getLogger().fine(
                            "Authentication failed. Identifier is unknown.");
                }

                if (!isOptional()) {
                    if (isRechallenging()) {
                        challenge(response, false);
                    } else {
                        forbid(response);
                    }
                }
                break;
            }
        } else {
            getLogger().warning("Authentication failed. No verifier provided.");
            response.setStatus(Status.SERVER_ERROR_INTERNAL,
                    "Authentication failed. No verifier provided.");
        }

        return result;
    }

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to {@link Status#CLIENT_ERROR_UNAUTHORIZED}.
     * 
     * @param response
     *            The response to update.
     * @param stale
     *            Indicates if the new challenge is due to a stale response.
     */
    public void challenge(Response response, boolean stale) {
        boolean loggable = response.getRequest().isLoggable()
                && getLogger().isLoggable(Level.FINE);

        if (loggable) {
            getLogger().log(Level.FINE,
                    "An authentication challenge was requested.");
        }

        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        response.getChallengeRequests().add(createChallengeRequest(stale));
    }

    /**
     * Creates a new challenge request.
     * 
     * @param stale
     *            Indicates if the new challenge is due to a stale response.
     * @return A new challenge request.
     */
    protected ChallengeRequest createChallengeRequest(boolean stale) {
        return new ChallengeRequest(getScheme(), getRealm());
    }

    /**
     * Rejects the call due to a failed authentication or authorization. This
     * can be overridden to change the default behavior, for example to display
     * an error page. By default, if authentication is required, the challenge
     * method is invoked, otherwise the call status is set to
     * CLIENT_ERROR_FORBIDDEN.
     * 
     * @param response
     *            The reject response.
     */
    public void forbid(Response response) {
        boolean loggable = response.getRequest().isLoggable()
                && getLogger().isLoggable(Level.FINE);

        if (loggable) {
            getLogger().log(
                    Level.FINE,
                    "Authentication or authorization failed for this URI: "
                            + response.getRequest().getResourceRef());
        }

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
     * Returns the credentials verifier.
     * 
     * @return The credentials verifier.
     */
    public Verifier getVerifier() {
        return verifier;
    }

    /**
     * Indicates if a new challenge should be sent when invalid credentials are
     * received (true by default to conform to HTTP recommendations). If set to
     * false, upon reception of invalid credentials, the method
     * {@link #forbid(Response)} will be called.
     * 
     * @return True if invalid credentials result in a new challenge.
     */
    public boolean isRechallenging() {
        return this.rechallenging;
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
     * @param rechallenging
     *            True if invalid credentials result in a new challenge.
     * @see #isRechallenging()
     */
    public void setRechallenging(boolean rechallenging) {
        this.rechallenging = rechallenging;
    }

    /**
     * Sets the credentials verifier.
     * 
     * @param verifier
     *            The credentials verifier.
     */
    public void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }

}
