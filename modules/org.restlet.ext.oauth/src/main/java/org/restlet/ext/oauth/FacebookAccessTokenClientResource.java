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

package org.restlet.ext.oauth;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;

/**
 * Client resource used to acquire an Facebook OAuth token.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class FacebookAccessTokenClientResource extends AccessTokenClientResource {

    private static class FacebookTokenResponse implements Token {

        public static FacebookTokenResponse parseResponse(Form result) {
            FacebookTokenResponse token = new FacebookTokenResponse();
            token.accessToken = result.getFirstValue(ACCESS_TOKEN);
            token.expirePeriod = Integer.parseInt(result
                    .getFirstValue("expires"));
            return token;
        }

        private String accessToken;

        private Integer expirePeriod;

        public String getAccessToken() {
            return accessToken;
        }

        public int getExpirePeriod() {
            return expirePeriod;
        }

        public String getRefreshToken() {
            return null;
        }

        public String[] getScope() {
            return null;
        }

        public String getTokenType() {
            return TOKEN_TYPE_BEARER;
        }

    }

    /**
     * Constructor.
     * 
     * @param tokenUri
     *            The URI of the target authorization resource.
     */
    public FacebookAccessTokenClientResource(Reference tokenUri) {
        super(tokenUri);
    }

    @Override
    public Token requestToken(OAuthParameters parameters) throws OAuthException {
        // Graph API MUST use body method.
        setupBodyClientCredentials(parameters);

        Representation input = parameters.toRepresentation();

        // Unlike RFC6749, Facebook token parameters are included in www-form.
        Form result = new Form(post(input));

        if (result.getFirstValue(ERROR) != null) {
            throw OAuthException.toOAuthException(result);
        }

        return FacebookTokenResponse.parseResponse(result);
    }
}
