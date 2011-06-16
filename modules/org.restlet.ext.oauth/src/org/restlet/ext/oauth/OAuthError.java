/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.oauth;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;

/**
 * Utility class for formating OAuth errors
 * 
 * @author Kristoffer Gronowski
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-3.2">Authorizatoin
 *      Error Responses</a>
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.3">Access
 *      Error Responses</a>
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-5.2.1">WWW-Authenticate
 *      Error Responses</a>
 */
public enum OAuthError {

    ACCESS_DENIED, // 3.2.1 & 4.3.1 & 5.2.1
    EXPIRED_TOKEN, // 3.2.1 & 4.3.1
    INSUFFICIENT_SCOPE, // 3.2.1 & 4.3.1
    INVALID_CLIENT, // 3.2.1
    INVALID_GRANT, // 3.2.1
    INVALID_REQUEST, // 3.2.1
    INVALID_SCOPE, // 3.2.1 & 4.3.1

    INVALID_TOKEN, // 4.3.1
    REDIRECT_URI_MISMATCH, // 4.3.1

    UNAUTHORIZED_CLIENT, // 5.2.1
    UNSUPPORTED_GRANT_TYPE, // 5.2.1
    UNSUPPORTED_RESPONSE_TYPE;
    // 5.2.1

    /**
     * Used for formatting error according to chapter 4.3.1
     * 
     * 
     * @param error
     * @param description
     * @param errorUri
     * @return
     * @throws JSONException
     */
    static JSONObject getErrorMessage(OAuthError error, String description,
            String errorUri) throws JSONException {
        JSONObject response = new JSONObject();
        response.put("error", error.name());
        if (description != null && description.length() > 0) {
            response.put("error_description", description);
        }
        if (errorUri != null && errorUri.length() > 0) {
            response.put("error_uri", errorUri);
        }

        return response;
    }

    /**
     * Used for formatting error according to chapter 3.2.1
     * 
     * 
     * @param error
     * @param description
     * @param errorUri
     * @param state
     * @return
     */
    static Form getErrorMessage(OAuthError error, String description,
            String errorUri, String state) {
        Form response = new Form();
        response.add("error", error.name());
        if (description != null && description.length() > 0) {
            response.add("error_description", description);
        }
        if (errorUri != null && errorUri.length() > 0) {
            response.add("error_uri", errorUri);
        }
        // TODO could automatically check for state....
        if (state != null && state.length() > 0) {
            response.add("state", state);
        }
        return response;
    }

    /**
     * Used for formatting error according to chapter 5.2.1
     * 
     * 
     * @param realm
     * @param error
     * @param description
     * @param errorUri
     * @param scopes
     * @return
     */
    static ChallengeRequest getErrorMessage(String realm, OAuthError error,
            String description, String errorUri, String[] scopes) {
        ChallengeRequest challenge = new ChallengeRequest(
                ChallengeScheme.HTTP_OAUTH, realm);

        Form response = new Form();
        response.add("error", error.name());
        if (description != null && description.length() > 0) {
            response.add("error_description", description);
        }
        if (errorUri != null && errorUri.length() > 0) {
            response.add("error_uri", errorUri);
        }
        // TODO could automatically check for state....
        if (scopes != null && scopes.length > 0) {
            StringBuilder scope = new StringBuilder(scopes[0]);
            for (int i = 1; i < scopes.length; i++) {
                scope.append(' ').append(scopes[i]);
            }
            response.add("scope", scope.toString());
        }

        challenge.setParameters(response);

        return challenge;
    }
}
