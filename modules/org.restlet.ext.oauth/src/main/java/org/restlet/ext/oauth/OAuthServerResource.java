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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONException;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.TokenManager;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Base class for common resources used by the OAuth server side. Implements
 * OAuth 2.0 (RFC6749)
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @author Kristoffer Gronowski
 */
public abstract class OAuthServerResource extends ServerResource implements OAuthResourceDefs {

    public static final String PARAMETER_DEFAULT_SCOPE = "defaultScope";

    /**
     * Adds a cache directive to the response.
     * 
     * @param response
     *            The current response.
     * @param cacheDirective
     *            The cache directive to add.
     */
    public static void addCacheDirective(Response response, CacheDirective cacheDirective) {
        List<CacheDirective> cacheDirectives = response.getCacheDirectives();
        if (cacheDirectives == null) {
            cacheDirectives = new ArrayList<CacheDirective>();
            response.setCacheDirectives(cacheDirectives);
        }
        cacheDirectives.add(cacheDirective);
    }

    /**
     * Returns the representation of the given error. The format of the JSON
     * document is according to 5.2. Error Response.
     * 
     * @param ex
     *            Any OAuthException with error
     * @return The representation of the given error.
     */
    public static Representation responseErrorRepresentation(OAuthException ex) {
        try {
            return new JsonRepresentation(ex.createErrorDocument());
        } catch (JSONException e) {
            StringRepresentation r = new StringRepresentation(
                    "{\"error\":\"server_error\",\"error_description:\":\"" + e.getLocalizedMessage() + "\"}");
            r.setMediaType(MediaType.APPLICATION_JSON);
            return r;
        }
    }

    protected volatile ClientManager clients;

    protected volatile TokenManager tokens;

    /**
     * Default constructor.
     */
    public OAuthServerResource() {
        super();
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        Context context = getContext();
        ConcurrentMap<String, Object> attributes = context.getAttributes();
        clients = (ClientManager) attributes.get(ClientManager.class.getName());
        tokens = (TokenManager) attributes.get(TokenManager.class.getName());

        getLogger().fine("Found client store = " + clients);
    }

    /**
     * Get request parameter "client_id".
     * 
     * @param parameters
     * @return
     * @throws OAuthException
     */
    protected Client getClient(Form parameters) throws OAuthException {
        // check clientId:
        String clientId = parameters.getFirstValue(CLIENT_ID);
        if (StringUtils.isNullOrEmpty(clientId)) {
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
     * @param parameters
     * @return
     * @throws OAuthException
     */
    protected String[] getScope(Form parameters) throws OAuthException {
        String scope = parameters.getFirstValue(SCOPE);
        
        if (StringUtils.isNullOrEmpty(scope)) {
            // If the client omits the scope parameter when requesting authorization, the authorization server MUST
            // either process the request using a pre-defined default value, or fail the request indicating an invalid
            // scope... (draft-ietf-oauth-v2-30 3.3.)
            Object defaultScope = getContext().getAttributes().get(PARAMETER_DEFAULT_SCOPE);
            if (defaultScope == null || defaultScope.toString().isEmpty()) {
                throw new OAuthException(OAuthError.invalid_scope, "Scope has not been provided.", null);
            }
            scope = defaultScope.toString();
        }
        return Scopes.parseScope(scope);
    }

    /**
     * Get request parameter "state".
     * 
     * @param parameters
     * @return
     * @throws OAuthException
     */
    protected String getState(Form parameters) {
        return parameters.getFirstValue(STATE);
    }
}
