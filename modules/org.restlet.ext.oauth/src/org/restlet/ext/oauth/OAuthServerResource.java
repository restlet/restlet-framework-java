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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONException;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.ClientManager;
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
public abstract class OAuthServerResource extends ServerResource implements
        OAuthResourceDefs {

    public static final String PARAMETER_DEFAULT_SCOPE = "defaultScope";

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
        Context ctx = getContext();
        ConcurrentMap<String, Object> attribs = ctx.getAttributes();
        clients = (ClientManager) attribs.get(ClientManager.class.getName());
        tokens = (TokenManager) attribs.get(TokenManager.class.getName());

        getLogger().fine("Found client store = " + clients);
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
            throw new OAuthException(OAuthError.invalid_request,
                    "No client_id parameter found.", null);
        }
        Client client = clients.findById(clientId);
        getLogger().fine("Client = " + client);
        if (client == null) {
            getLogger().warning("Need to register the client : " + clientId);
            throw new OAuthException(OAuthError.invalid_request,
                    "Need to register the client : " + clientId, null);
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
             * indicating an invalid scope... (draft-ietf-oauth-v2-30 3.3.)
             */
            Object defaultScope = getContext().getAttributes().get(
                    PARAMETER_DEFAULT_SCOPE);
            if (defaultScope == null || defaultScope.toString().isEmpty()) {
                throw new OAuthException(OAuthError.invalid_scope,
                        "Scope has not provided.", null);
            }
            scope = defaultScope.toString();
        }
        return Scopes.parseScope(scope);
    }

    /**
     * Get request parameter "state".
     * 
     * @param params
     * @return
     * @throws OAuthException
     */
    protected String getState(Form params) {
        return params.getFirstValue(STATE);
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
                    "{\"error\":\"server_error\",\"error_description:\":\""
                            + e.getLocalizedMessage() + "\"}");
            r.setMediaType(MediaType.APPLICATION_JSON);
            return r;
        }
    }

    public static void addCacheDirective(Response response,
            CacheDirective cacheDirective) {
        List<CacheDirective> cacheDirectives = response.getCacheDirectives();
        if (cacheDirectives == null) {
            cacheDirectives = new ArrayList<CacheDirective>();
            response.setCacheDirectives(cacheDirectives);
        }
        cacheDirectives.add(cacheDirective);
    }
}
