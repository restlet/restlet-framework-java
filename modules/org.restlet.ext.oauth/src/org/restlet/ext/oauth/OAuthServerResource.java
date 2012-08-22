/**
 * Copyright 2005-2012 Restlet S.A.S.
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

import freemarker.template.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONException;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.TokenGenerator;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Base class for common resources used by the OAuth server side.
 *
 * <b>Originally written by Kristoffer Gronowski, Heavily modified for update to draft30.</b>
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public abstract class OAuthServerResource extends ServerResource {

    /*
     * OAuth 2.0 draft30 parameters.
     */
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String SCOPE = "scope";
    public static final String STATE = "state";
    public static final String REDIR_URI = "redirect_uri";
    public static final String ERROR = "error";
    public static final String ERROR_DESC = "error_description";
    public static final String ERROR_URI = "error_uri";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CODE = "code";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES_IN = "expires_in";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String REFRESH_TOKEN = "refresh_token";
    /*
     * Token Types
     */
    public static final String TOKEN_TYPE_BEARER = "Bearer";
    public static final String TOKEN_TYPE_MAC = "mac";
    /*
     * Other params (old)
     */
    public static final String AUTONOMOUS_USER = "__autonomous";
    public static final String ClientCookieID = "_cid";
    public static final String TOKEN_SERVER_MAX_TIME_SEC = "_token_server_max_time_sec";
    public static final String TOKEN_SERVER_TIME_SEC = "_token_server_time_sec";
    
    protected volatile ClientStore<?> clients;
    protected volatile TokenGenerator generator;
    protected volatile long tokenMaxTimeSec = Token.UNLIMITED;
    protected volatile long tokenTimeSec = Token.UNLIMITED;

    /**
     * Default constructor.
     */
    public OAuthServerResource() {
        super();
    }

    /**
     * Completes the given {@link StringBuilder} with the authentication attributes.
     *
     * @param location The {@link StringBuilder} to complete.
     */
    private void appendState(StringBuilder location) {
        String sessionId = (String) getRequest().getAttributes().get(ClientCookieID);
        if (sessionId == null) {
            sessionId = getCookies().getFirstValue(ClientCookieID);
        }
        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        AuthSession session = (AuthSession) attribs.get(sessionId);
        String state = session.getState();
        if (state != null && state.length() > 0) {
            location.append("&state=");
            location.append(state);
        }
        session.reset();
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        Context ctx = getContext();
        ConcurrentMap<String, Object> attribs = ctx.getAttributes();
        clients = ClientStoreFactory.getInstance();

        // NOT NEEDED I THINK:
        /*
         * clients = (ClientStore<?>) attribs.get(ClientStore.class
         * .getCanonicalName());
         */
        getLogger().fine("Found client store = " + clients);

        generator = clients.getTokenGenerator();
        getLogger().fine("Found token generator = " + generator);

        if (attribs.containsKey(TOKEN_SERVER_TIME_SEC)) {
            tokenTimeSec = (Long) attribs.get(TOKEN_SERVER_TIME_SEC);
        }

        if (attribs.containsKey(TOKEN_SERVER_MAX_TIME_SEC)) {
            tokenMaxTimeSec = (Long) attribs.get(TOKEN_SERVER_MAX_TIME_SEC);
        }
        generator.setMaxTokenTime(tokenMaxTimeSec);
    }

    /**
     * Returns the agent token for the given user, client and redirection URI.
     *
     * @param userId The identifier of the user.
     * @param client The oAuth client.
     * @param redirURL The redirection URI.
     * @return The agent token for the given user, client and redirection URI.
     */
    protected String generateAgentToken(String userId, Client client,
            String redirURL) {
        AuthenticatedUser user = null;
        if (client.containsUser(userId)) {
            user = client.findUser(userId);
        } else {
            user = client.createUser(userId);
        }

        // TODO generate token and keep for a while.
        Token token = generator.generateToken(user, tokenTimeSec);
        StringBuilder location = new StringBuilder(redirURL);
        location.append("#access_token=").append(token.getToken());

        // TODO add expires
        appendState(location);

        // Sets the no-store Cache-Control header
        addCacheDirective(getResponse(), CacheDirective.noStore());
        // TODO: Set Pragma: no-cache

        getLogger().fine("Redirecting to -> " + location.toString());
        // TODO add state to request string
        return location.toString();
    }

    /**
     * Returns the code for the given user, client and redirection URI.
     *
     * @param userId The identifier of the user.
     * @param client The oAuth client.
     * @param redirURL The redirection URI.
     * @return The code for the given user, client and redirection URI.
     */
    protected String generateCode(String userId, Client client, String redirURL) {
        AuthenticatedUser user = null;
        if (client.containsUser(userId)) {
            user = client.findUser(userId);
        } else {
            user = client.createUser(userId);
        }

        // TODO generate code and keep for a while.
        String code = generator.generateCode(user);
        StringBuilder location = new StringBuilder(redirURL);
        String c = (location.indexOf("?") == -1) ? "?code=" : "&code=";
        location.append(c).append(code);
        appendState(location);

        // Sets the no-store Cache-Control header
        addCacheDirective(getResponse(), CacheDirective.noStore());
        // TODO: Set Pragma: no-cache

        getLogger().fine("Redirecting to -> " + location.toString());
        // TODO add state to request string
        return location.toString();
    }

    /**
     * Returns the value of the first parameter found with the given name.
     *
     * @param parameter The parameter name.
     * @param defaultValue The default value to return if no matching parameter found or if the
     * parameter has a null value.
     * @return The value of the first parameter found with the given name or the default value.
     */
    protected String getParameter(String parameter, String defaultValue) {
        String val = (String) this.getContext().getAttributes().get(parameter);
        return val != null ? val : defaultValue;
    }
    
    /**
     * Get request parameter "client_id".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected Client getClient(Form params) throws OAuthException {
        // check clientId:
        String clientId = params.getFirstValue(CLIENT_ID);
        if (clientId == null || clientId.isEmpty()) {
            getLogger().warning("Could not find client ID");
            throw new OAuthException(OAuthError.invalid_request, "No client_id parameter found.", null);
        }
        Client client = clients.findById(clientId);
        getLogger().fine("Client = " + client);
        if (client == null) {
            getLogger().warning("Need to register the client : " + clientId);
            throw new OAuthException(OAuthError.invalid_request, "Need to register the client : " + clientId, null);
        }
        
        return client;
    }
    
    /**
     * Get request parameter "scope".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected String[] getScope(Form params) throws OAuthException {
        String scope = params.getFirstValue(SCOPE);
        if (scope == null || scope.isEmpty()) {
            /*
             * If the client omits the scope parameter when requesting
             * authorization, the authorization server MUST either process the
             * request using a pre-defined default value, or fail the request
             * indicating an invalid scope...
             * (draft-ietf-oauth-v2-30 3.3.)
             */
            Object defaultScope = getParameter("defaultScope", null);
            if (defaultScope == null || defaultScope.toString().isEmpty()) {
                throw new OAuthException(OAuthError.invalid_scope, "Scope has not provided.", null);
            }
            scope = defaultScope.toString();
        }
        return Scopes.parseScope(scope);
    }
    
    protected String getState(Form params) {
        return params.getFirstValue(STATE);
    }

    protected AuthSession getAuthSession() {
        // Get some basic information
        String sessionId = getCookies().getFirstValue(ClientCookieID);
        getLogger().fine("sessionId = " + sessionId);

        AuthSession session = (sessionId == null) ? null
                : (AuthSession) getContext().getAttributes().get(sessionId);
        return session;
    }

    /**
     *
     * Helper method to format error responses according to OAuth2 spec. (Redirect)
     *
     * @param redirectUri redirection URI to send error
     * @param ex Any OAuthException with error
     * @param state state parameter as presented in the initial auth request
     */
    protected void sendError(String redirectUri, OAuthException ex, String state) {
        Reference cb = new Reference(redirectUri);
        cb.addQueryParameter(ERROR, ex.getError().name());
        if (state != null && state.length() > 0) {
            cb.addQueryParameter(STATE, state);
        }
        String description = ex.getErrorDescription();
        if (description != null && description.length() > 0) {
            cb.addQueryParameter(ERROR_DESC, description);
        }
        String errorUri = ex.getErrorURI();
        if (errorUri != null && errorUri.length() > 0) {
            cb.addQueryParameter(ERROR_URI, errorUri);
        }
        redirectTemporary(cb.toString());
    }

    /**
     *
     * Helper method to format error responses according to OAuth2 spec. (Non Redirect)
     *
     * @param errPage errorPage template name
     * @param ex Any OAuthException with error
     */
    protected Representation getErrorPage(String errPage, OAuthException ex) {
        Configuration config = new Configuration();
        config.setTemplateLoader(new ContextTemplateLoader(getContext(), "clap:///"));
        getLogger().fine("loading: " + errPage);
        TemplateRepresentation response = new TemplateRepresentation(errPage, config, MediaType.TEXT_HTML);

        // Build the model
        HashMap<String, Object> data = new HashMap<String, Object>();

        data.put(ERROR, ex.getError().name());
        data.put(ERROR_DESC, ex.getErrorDescription());
        data.put(ERROR_URI, ex.getErrorURI());
        //data.put(STATE, state);

        response.setDataModel(data);

        return response;
    }
    
    /**
     * Returns the representation of the given error.
     * 
     * @param error
     *            The OAuth error.
     * @param description
     *            The error description.
     * @param errorUri
     *            the error URI.
     * @return The representation of the given error.
     */
    public static Representation getErrorJsonDocument(OAuthException ex) {
        try {
            return new JsonRepresentation(ex.createErrorDocument());
        } catch (JSONException e) {
            StringRepresentation r = new StringRepresentation(
                    "{\"error\":\"server_error\",\"error_description:\":\""
                    + e.getLocalizedMessage() + "\"}");
            r.setMediaType(MediaType.APPLICATION_JSON);
            return r;
        }
    }
    
    public static void addCacheDirective(Response response, CacheDirective cacheDirective) {
        List<CacheDirective> cacheDirectives = response.getCacheDirectives();
        if (cacheDirectives == null) {
            cacheDirectives = new ArrayList<CacheDirective>();
            response.setCacheDirectives(cacheDirectives);
        }
        cacheDirectives.add(cacheDirective);
    }
}
