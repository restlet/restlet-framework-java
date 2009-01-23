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

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.authentication.ChallengeAuthenticatorHelper;

/**
 * Authenticator based on a challenge scheme like HTTP Basic or HTTP Digest.
 * 
 * @author Jerome Louvel
 */
public class ChallengeAuthenticator implements Authenticator {

    private final ChallengeAuthenticatorHelper helper;

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
    public ChallengeAuthenticator(ChallengeScheme challengeScheme) {
        this.scheme = challengeScheme;
        this.verifier = null;

        if (this.scheme != null) {
            this.helper = Engine.getInstance().findHelper(challengeScheme,
                    false, true);
        } else {
            this.helper = null;
        }
    }

    /**
     * 
     */
    public int authenticate(Request request, Response response) {
        return getHelper().authenticate(request.getChallengeResponse(),
                request, null);
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
     * Returns the private helper.
     * 
     * @return The private helper.
     */
    private ChallengeAuthenticatorHelper getHelper() {
        return helper;
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
