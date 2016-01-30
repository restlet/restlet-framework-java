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

import static org.restlet.ext.oauth.OAuthResourceDefs.ACCESS_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.ERROR;
import static org.restlet.ext.oauth.OAuthResourceDefs.ERROR_DESC;
import static org.restlet.ext.oauth.OAuthResourceDefs.SCOPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.TOKEN_TYPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.TOKEN_TYPE_BEARER;
import static org.restlet.ext.oauth.OAuthResourceDefs.USERNAME;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 * Verifier for OAuth 2.0 protected resources<br>
 * Typically use with ChallengeAuthenticator. "Bearer" and "MAC" challenge schemes are supported.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @see <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-bearer-22"> Bearer Token Usage</a>
 * @see <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-http-mac-01"> MAC Access Authentication</a>
 */
public class TokenVerifier implements Verifier {

    private static final Logger logger = Logger.getLogger(TokenVerifier.class.getName());

    private static JSONObject createBearerAuthRequest(String token) throws JSONException {
        JSONObject request = new JSONObject();
        request.put(TOKEN_TYPE, TOKEN_TYPE_BEARER);
        request.put(ACCESS_TOKEN, token);

        return request;
    }

    /** Indicates if the credentials are "Form-Encoded Body Parameter" (chap 2.2). */
    private boolean acceptingAccessTokenSentByBody = false;

    /** Indicates if the credentials are "URI Query Parameter" (chap 2.3). */
    private boolean acceptingAccessTokenSentByQuery = false;

    /** The authorization resource URI. */
    private Reference authReference;

    /**
     * Constructor.
     * 
     * @param authReference
     *            The authorization resource URI.
     */
    public TokenVerifier(Reference authReference) {
        this.authReference = authReference;
    }

    private String getAccessTokenFromBody(Request request) {
        // Try to find token in the request body
        if (!request.isEntityAvailable()
                || !MediaType.APPLICATION_WWW_FORM.equals(request.getEntity().getMediaType())) {
            return null;
        }

        Form form = new Form(request.getEntity());

        // As the entity can only be read once, we restore a new copy
        request.setEntity(form.getWebRepresentation());

        String token = form.getFirstValue(ACCESS_TOKEN);

        if (StringUtils.isNullOrEmpty(token)) {
            return null;
        }

        logger.fine("Found Bearer Token in Body");

        return token;
    }

    private String getAccessTokenFromQuery(Request request) {
        // Try to find token in URI query
        Form params = request.getOriginalRef().getQueryAsForm();
        String token = params.getFirstValue(ACCESS_TOKEN);

        if (!StringUtils.isNullOrEmpty(token)) {
            logger.fine("Found Bearer Token in URI query.");
            return token;
        }

        return null;
    }

    /**
     * Indicates if the credentials are "Form-Encoded Body Parameter".
     * 
     * @return True if the credentials are "Form-Encoded Body Parameter".
     * @Deprecated use {@link #isAcceptingAccessTokenSentByBody()} instead.
     */
    @Deprecated
    public boolean isAcceptBodyMethod() {
        return acceptingAccessTokenSentByBody;
    }

    /**
     * Indicates if the credentials are "Form-Encoded Body Parameter".
     * 
     * @return True if the credentials are "Form-Encoded Body Parameter".
     */
    public boolean isAcceptingAccessTokenSentByBody() {
        return acceptingAccessTokenSentByBody;
    }

    /**
     * Indicates if the credentials are "URI Query Parameter".
     * 
     * @return True if the credentials are "URI Query Parameter".
     */
    public boolean isAcceptingAccessTokenSentByQuery() {
        return acceptingAccessTokenSentByQuery;
    }

    /**
     * Indicates if the credentials are "URI Query Parameter".
     * 
     * @return True if the credentials are "URI Query Parameter".
     * @Deprecated use {@link #isAcceptingAccessTokenSentByQuery()} instead.
     */
    public boolean isAcceptQueryMethod() {
        return acceptingAccessTokenSentByQuery;
    }

    /**
     * Indicates if the credentials are "Form-Encoded Body Parameter".
     * 
     * @param acceptBodyMethod
     *            True if the credentials are "Form-Encoded Body Parameter".
     * @deprecated use {@link #setAcceptingAccessTokenSentByBody(boolean)} instead.
     */
    @Deprecated
    public void setAcceptBodyMethod(boolean acceptBodyMethod) {
        this.acceptingAccessTokenSentByBody = acceptBodyMethod;
    }

    /**
     * Indicates if the credentials are "Form-Encoded Body Parameter".
     * 
     * @param acceptBodyMethod
     *            True if the credentials are "Form-Encoded Body Parameter".
     */
    public void setAcceptingAccessTokenSentByBody(boolean acceptingAccessTokenSentByBody) {
        this.acceptingAccessTokenSentByBody = acceptingAccessTokenSentByBody;
    }

    /**
     * Indicates if the credentials are "URI Query Parameter".
     * 
     * @param acceptQueryMethod
     *            True if the credentials are "URI Query Parameter".
     */
    public void setAcceptingAccessTokenSentByQuery(boolean acceptingAccessTokenSentByQuery) {
        this.acceptingAccessTokenSentByQuery = acceptingAccessTokenSentByQuery;
    }

    /**
     * Indicates if the credentials are "URI Query Parameter".
     * 
     * @param acceptQueryMethod
     *            True if the credentials are "URI Query Parameter".
     * @deprecated use {@link #setAcceptingAccessTokenSentByQuery(boolean)} instead.
     */
    @Deprecated
    public void setAcceptQueryMethod(boolean acceptQueryMethod) {
        this.acceptingAccessTokenSentByQuery = acceptQueryMethod;
    }

    @Override
    public int verify(Request request, Response response) {
        final JSONObject authRequest;

        try {
            ChallengeResponse cr = request.getChallengeResponse();
            if (cr == null) {
                // Try Bearer alternative methods
                String bearer = null;
                if (acceptingAccessTokenSentByBody) {
                    bearer = getAccessTokenFromBody(request);
                }
                if (bearer == null && acceptingAccessTokenSentByQuery) {
                    bearer = getAccessTokenFromQuery(request);
                    if (bearer != null) {
                        response.getCacheDirectives().add(CacheDirective.privateInfo());
                    }
                }
                if (bearer == null) {
                    return RESULT_MISSING;
                }
                logger.config("Verify: Bearer (Alternative)");
                authRequest = createBearerAuthRequest(bearer);
            } else if (ChallengeScheme.HTTP_OAUTH_BEARER.equals(cr.getScheme())) {
                logger.config("Verify: " + cr.getScheme().getName());
                final String bearer = cr.getRawValue();
                if (StringUtils.isNullOrEmpty(bearer)) {
                    return RESULT_MISSING;
                }
                authRequest = createBearerAuthRequest(bearer);
            } else if (ChallengeScheme.HTTP_OAUTH_MAC.equals(cr.getScheme())) {
                logger.config("Verify: " + cr.getScheme().getName());
                // TODO Add support of HTTP_OAUTH_MAC challenge scheme
                return RESULT_UNSUPPORTED;
            } else {
                return RESULT_UNSUPPORTED;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Cannot compute the authorization request.", ex);
            return RESULT_INVALID;
        }

        ClientResource authResource = new ClientResource(authReference);

        JSONObject jsonResponse;

        try {
            logger.fine("Post auth request to auth resource...");
            Representation resp = authResource.post(new JsonRepresentation(authRequest));
            JsonRepresentation jsonRepresentation = new JsonRepresentation(resp);
            jsonResponse = jsonRepresentation.getJsonObject();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error while requesting the OAuth authorization resource.", ex);
            return RESULT_INVALID;
        }

        if (jsonResponse.has(ERROR)) {
            try {
                String error = jsonResponse.getString(ERROR);
                logger.warning(error);
                logger.warning(jsonResponse.getString(ERROR_DESC));
            } catch (JSONException ex) {
                logger.log(Level.SEVERE, "Error while parsing the OAuth authorization error response.", ex);
            }
            // TODO: Configure challenge request
            return RESULT_INVALID;
        }

        try {
            ClientInfo clientInfo = request.getClientInfo();
            clientInfo.setUser(new User(jsonResponse.getString(USERNAME)));
            clientInfo.setRoles(Scopes.toRoles(jsonResponse.getString(SCOPE)));
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, "Error while parsing the OAuth authorization success response.", ex);
            return RESULT_INVALID;
        }

        return RESULT_VALID;
    }
}
