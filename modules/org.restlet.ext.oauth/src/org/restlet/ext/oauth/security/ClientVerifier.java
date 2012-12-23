/*
 * Copyright 2012 Shotaro Uchida <fantom@xmaker.mx>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.restlet.ext.oauth.security;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.ext.oauth.Client;
import org.restlet.ext.oauth.ClientStore;
import org.restlet.ext.oauth.ClientStoreFactory;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 * Verifier for OAuth 2.0 Token Endpoints.
 * Verify incoming requests with client credentials. Typically,
 * use with ChallengeAuthenticator.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class ClientVerifier implements Verifier {
    
    private ClientStore<?> clients = ClientStoreFactory.getInstance();
    private boolean acceptBodyMethod = false;

    public int verify(Request request, Response response) {
        final String clientId;
        final char[] clientSecret;
        ChallengeResponse cr = request.getChallengeResponse();
        if (cr == null) {
            if (!isAcceptBodyMethod()) {
                return RESULT_MISSING;
            }
            // Alternative method...
            Form params = new Form(request.getEntity());
            clientId = params.getFirstValue(OAuthServerResource.CLIENT_ID);
            if (clientId == null || clientId.isEmpty()) {
                return RESULT_MISSING;
            }
            String s = params.getFirstValue(OAuthServerResource.CLIENT_SECRET);
            if (s == null || s.isEmpty()) {
                clientSecret = new char[0];
            } else {
                clientSecret = s.toCharArray();
            }
            // Restore the body
            request.setEntity(params.getWebRepresentation());
        } else {
            if (!cr.getScheme().equals(ChallengeScheme.HTTP_BASIC)) {
                // XXX: May be unsupported
                return RESULT_UNSUPPORTED;
            }
            clientId = cr.getIdentifier();
            clientSecret = cr.getSecret();
        }
        
        int result = verify(clientId, clientSecret);
        if (result == RESULT_VALID) {
            request.getClientInfo().setUser(new User(clientId));
        } else {
            response.setEntity(
                    OAuthServerResource.responseErrorRepresentation(
                        new OAuthException(OAuthError.invalid_client,
                        "Invalid client", null)));
        }
        return result;
    }
    
    private int verify(String clientId, char[] clientSecret) {
        Client client = clients.findById(clientId);
        if (client == null) {
            return RESULT_UNKNOWN;
        }
        // TODO: client secret MUST be char[]
        char[] s = client.getClientSecret().toCharArray();
        if (!SecretVerifier.compare(s, clientSecret)) {
            return RESULT_INVALID;
        }
        return RESULT_VALID;
    }

    /**
     * @return the acceptBodyMethod
     */
    public boolean isAcceptBodyMethod() {
        return acceptBodyMethod;
    }

    /**
     * @param acceptBodyMethod the acceptBodyMethod to set
     */
    public void setAcceptBodyMethod(boolean acceptBodyMethod) {
        this.acceptBodyMethod = acceptBodyMethod;
    }
}
