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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;

/**
 * Verifier of identifier/secret couples. By default, it extracts the identifier
 * and the secret from the {@link ChallengeResponse}.
 * 
 * @author Jerome Louvel
 */
public abstract class SecretVerifier extends Verifier {
    /**
     * Compares that two secrets are equal.
     * 
     * @param secret1
     *            The input secret.
     * @param secret2
     *            The output secret.
     * @return True if both are equal.
     */
    public static boolean compare(char[] secret1, char[] secret2) {
        boolean result = false;

        if ((secret1 == null) || (secret2 == null)) {
            // Check if both are null
            result = (secret1 == secret2);
        } else {
            // None is null
            if (secret1.length == secret2.length) {
                boolean equals = true;

                for (int i = 0; (i < secret1.length) && equals; i++) {
                    equals = (secret1[i] == secret2[i]);
                }

                result = equals;
            }
        }

        return result;
    }

    /**
     * Returns the user identifier.
     * 
     * @param request
     *            The request to inspect.
     * @param response
     *            The response to inspect.
     * @return
     */
    protected String getIdentifier(Request request, Response response) {
        return request.getChallengeResponse().getIdentifier();
    }

    /**
     * Returns the user secret.
     * 
     * @param request
     *            The request to inspect.
     * @param response
     *            The response to inspect.
     * @return
     */
    protected char[] getSecret(Request request, Response response) {
        return request.getChallengeResponse().getSecret();
    }

    /**
     * Verifies that the proposed secret is correct for the specified request.
     * By default, it compares the inputSecret of the request's authentication
     * response with the one obtain by the {@link ChallengeResponse#getSecret()}
     * method and adds a new {@link RolePrincipal} instance to the subject if
     * successful.
     * 
     * @param request
     *            The request to inspect.
     * @param response
     *            The response to inspect.
     * @return True if the proposed secret was correct and the subject updated.
     */
    @Override
    public int verify(Request request, Response response) {
        int result = RESULT_VALID;

        if (request.getChallengeResponse() == null) {
            result = RESULT_MISSING;
        } else {
            String identifier = getIdentifier(request, response);
            char[] inputSecret = getSecret(request, response);

            if (verify(identifier, inputSecret)) {
                request.getClientInfo().setUser(new User(identifier));
            } else {
                result = RESULT_INVALID;
            }
        }

        return result;
    }

    /**
     * Verifies that the identifier/secret couple is valid.
     * 
     * @param identifier
     *            The user identifier to match.
     * @param inputSecret
     *            The input secret to verify.
     * @return true if the identifier/secret couple is valid.
     */
    public abstract boolean verify(String identifier, char[] inputSecret);

}
