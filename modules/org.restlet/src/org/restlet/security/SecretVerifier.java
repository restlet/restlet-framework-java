/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

import javax.security.auth.Subject;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Verifier of identifier/secret couples.
 * 
 * @author Jerome Louvel
 */
public abstract class SecretVerifier extends Verifier {

    /**
     * When the verification succeeds, we need to update the {@link Subject}
     * associated to the request. By default, it adds a {@link UserPrincipal}.
     * 
     * @param identifier
     *            The user identifier.
     * @return The user principal created.
     */
    protected UserPrincipal createUserPrincipal(String identifier) {
        return new UserPrincipal(identifier);
    }

    /**
     * Verifies that the proposed secret is correct for the specified request.
     * By default, it compares the inputSecret of the request's authentication
     * response with the one obtain by the {@link ChallengeResponse#getSecret()}
     * method and adds a new {@link RolePrincipal} instance to the subject if
     * successful.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to handle.
     * @return True if the proposed secret was correct and the subject updated.
     */
    @Override
    public int verify(Request request, Response response) {
        int result = RESULT_VALID;

        if (request.getChallengeResponse() == null) {
            result = RESULT_MISSING;
        } else {
            String identifier = request.getChallengeResponse().getIdentifier();
            char[] inputSecret = request.getChallengeResponse().getSecret();

            if (verify(identifier, inputSecret)) {
                // Add a principal for this identifier
                request.getClientInfo().getSubject().getPrincipals().add(
                        createUserPrincipal(identifier));
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
     * @param inputSecret
     * @return true if the identifier/secret couple is valid.
     */
    public abstract boolean verify(String identifier, char[] inputSecret);

}
