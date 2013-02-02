/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */
package org.restlet.ext.oauth;

import org.restlet.Context;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
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
    
    private Context context;
    private boolean acceptBodyMethod = false;
    
    public ClientVerifier(Context context) {
        this.context = context;
    }

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
        ClientManager clients = (ClientManager) context.getAttributes().get(ClientManager.class.getName());
        Client client = clients.findById(clientId);
        if (client == null) {
            return RESULT_UNKNOWN;
        }
        char[] s = client.getClientSecret();
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
