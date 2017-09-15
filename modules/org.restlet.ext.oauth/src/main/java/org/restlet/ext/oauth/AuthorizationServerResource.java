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
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.RedirectionURI;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.ServerToken;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.routing.Redirector;
import org.restlet.security.User;

/**
 * Restlet implementation class AuthorizationService. Used for initiating an OAuth 2.0 authorization request.
 * 
 * This Resource is controlled by to Context Attribute Parameters
 * 
 * Implements OAuth 2.0 (RFC6749)
 * 
 * The following example shows how to set up a simple Authorization Service.
 * 
 * <pre>
 * {
 *      &#064;code
 *      public Restlet createInboundRoot(){
 *      ...
 *      ChallengeAuthenticator au = new ChallengeAuthenticator(getContext(),
 *              ChallengeScheme.HTTP_BASIC, &quot;OAuth Test Server&quot;);
 *      au.setVerifier(new MyVerifier());
 *      au.setNext(AuthorizationServerResource.class);
 *      root.attach(&quot;/authorize&quot;, au);
 *      ...
 * }
 * </pre>
 * 
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @author Martin Svensson
 * 
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.1">OAuth 2.0</a>
 */
public class AuthorizationServerResource extends AuthorizationBaseServerResource {

    /**
     * The authorization server MUST support the use of the HTTP "GET" method [RFC2616] for the authorization endpoint
     * and MAY support the use of the "POST" method as well. (3.1. Authorization Endpoint)
     */
    public static final String PARAMETER_SUPPORT_POST = "supportPost";

    /**
     * Handles the authorization request.
     * 
     * @param session
     *            The OAuth session.
     * @param client
     *            The OAuth client.
     * @return The result as a {@link Representation}.
     */
    protected Representation doPostAuthorization(AuthSession session, Client client) {
        Reference ref = new Reference("riap://application" + HttpOAuthHelper.getAuthPage(getContext()));
        getLogger().fine("Name = " + getApplication().getInboundRoot());
        ref.addQueryParameter("client", session.getClientId());

        // Requested scope should not be null.
        String[] scopes = session.getRequestedScope();
        for (String s : scopes) {
            ref.addQueryParameter("scope", s);
        }

        // XXX
        ServerToken token = (ServerToken) tokens.findToken(client, session.getScopeOwner());
        if (token != null && !token.isExpired()) {
            for (String s : token.getScope()) {
                ref.addQueryParameter("grantedScope", s);
            }
        }

        // Redirect to AuthPage.
        getLogger().fine("Redir = " + ref);
        Redirector dispatcher = new Redirector(getContext(), ref.toString(), Redirector.MODE_SERVER_OUTBOUND);
        // XXX: Remove? getRequest().getAttributes().put(ClientCookieID, session.getId());
        dispatcher.handle(getRequest(), getResponse());

        return getResponseEntity();
    }

    /**
     * Get request parameter "redirect_uri". (See 3.1.2.3. Dynamic Configuration)
     * 
     * @param parameters
     * @param client
     *            The OAuth client.
     * @return
     * @throws OAuthException
     */
    protected RedirectionURI getRedirectionURI(Form parameters, Client client) throws OAuthException {
        String redirectUri = parameters.getFirstValue(REDIR_URI);
        String[] redirectUris = client.getRedirectURIs();

        /*
         * If multiple redirection URIs have been registered, if only part of
         * the redirection URI has been registered, or if no redirection URI has
         * been registered, the client MUST include a redirection URI with the
         * authorization request using the "redirect_uri" request parameter.
         * (See 3.1.2.3. Dynamic Configuration)
         */
        if (StringUtils.isNullOrEmpty(redirectUri)) {
            if (redirectUris == null || redirectUris.length != 1) {
                throw new OAuthException(OAuthError.invalid_request, "Client MUST include a redirection URI.", null);
            }
            // If the optional parameter redirect_uri is not provided, we use the one provided during client
            // registration.
            return new RedirectionURI(redirectUris[0]);
        }

        /*
         * When a redirection URI is included in an authorization request, the
         * authorization server MUST compare and match the value received
         * against at least one of the registered redirection URIs (or URI
         * components) as defined in [RFC3986] Section 6, if any redirection
         * URIs were registered. (See 3.1.2.3. Dynamic Configuration)
         */
        for (String uri : redirectUris) {
            if (redirectUri.startsWith(uri)) {
                return new RedirectionURI(redirectUri, true);
            }
        }

        // The provided uri is no based on the uri with the client registration.
        throw new OAuthException(OAuthError.invalid_request, "Callback URI does not match.", null);
    }

    /**
     * Returns the value of the request parameter "response_type".
     * 
     * @param parameters
     * @return
     * @throws OAuthException
     */
    protected ResponseType[] getResponseType(Form parameters) throws OAuthException {
        String responseType = parameters.getFirstValue(RESPONSE_TYPE);
        if (StringUtils.isNullOrEmpty(responseType)) {
            throw new OAuthException(OAuthError.invalid_request, "No \"response_type\" parameter found.", null);
        }

        // Extension response types MAY contain a space-delimited (%x20) list of values (3.1.1. Response Type)
        String[] typesString = Scopes.parseScope(responseType); // The same format as scope.
        ResponseType[] types = new ResponseType[typesString.length];

        for (int i = 0; i < typesString.length; i++) {
            try {
                ResponseType type = Enum.valueOf(ResponseType.class, typesString[i]);
                getLogger().fine("Found flow - " + type);
                types[i] = type;
            } catch (IllegalArgumentException iae) {
                throw new OAuthException(OAuthError.unsupported_response_type, "Unsupported flow", null);
            }
        }

        return types;
    }

    @Get("html")
    public Representation requestAuthorization() throws OAuthException {
        return requestAuthorization(getQuery());
    }

    /**
     * Checks that all incoming requests have a type parameter. Requires
     * response_type, client_id and redirect_uri parameters. For the code flow
     * client_secret is also mandatory.
     */
    public Representation requestAuthorization(Form params) throws OAuthException {
        AuthSession session = getAuthSession();
        if (session != null) {
            return doPostAuthorization(session, clients.findById(session.getClientId()));
        }

        final Client client;
        final RedirectionURI redirectURI;
        try {
            client = getClient(params);
            redirectURI = getRedirectionURI(params, client);
        } catch (OAuthException ex) {
            // MUST NOT automatically redirect the user-agent to the invalid redirection URI. (see 3.1.2.4. Invalid Endpoint)
            return getErrorPage(HttpOAuthHelper.getErrorPageTemplate(getContext()), ex);
        } catch (Exception ex) {
            // All other exception should be caught as server_error.
            OAuthException oex = new OAuthException(OAuthError.server_error, ex.getMessage(), null);
            return getErrorPage(HttpOAuthHelper.getErrorPageTemplate(getContext()), oex);
        }

        // Start Session
        session = setupAuthSession(redirectURI);

        // Setup session attributes
        try {
            ResponseType[] responseTypes = getResponseType(params);
            if (responseTypes.length != 1) {
                throw new OAuthException(OAuthError.unsupported_response_type, "Extension response types are not supported.", null);
            }
            if (!client.isResponseTypeAllowed(responseTypes[0])) {
                throw new OAuthException(OAuthError.unauthorized_client, "Unauthorized response type.", null);
            }
            session.setAuthFlow(responseTypes[0]);
            session.setClientId(client.getClientId());
            String[] scope = getScope(params);
            session.setRequestedScope(scope);
            String state = getState(params);
            if (state != null && !state.isEmpty()) {
                session.setState(state);
            }
        } catch (OAuthException ex) {
            ungetAuthSession();
            throw ex;
        }

        User scopeOwner = getRequest().getClientInfo().getUser();
        if (scopeOwner != null) {
            // If user information is present, use as scope owner.
            session.setScopeOwner(scopeOwner.getIdentifier());
        }

        if (session.getScopeOwner() == null) {
            // Redirect to login page.
            Reference ref = new Reference("." + HttpOAuthHelper.getLoginPage(getContext()));
            ref.addQueryParameter("continue", getRequest().getOriginalRef().toString(true, false)); // XXX: Don't need full query.
            redirectTemporary(ref.toString());
            return new EmptyRepresentation();
        }

        return doPostAuthorization(session, client);
    }

    @Post("html")
    public Representation requestAuthorization(Representation input) throws OAuthException {
        Object supportPost = getContext().getAttributes().get(PARAMETER_SUPPORT_POST);
        if (!Boolean.parseBoolean(supportPost.toString())) {
            throw new OAuthException(
                    OAuthError.invalid_request,
                    "Authorization endpoint does NOT support the use of the POST method.",
                    null);
        }

        return requestAuthorization(new Form(input));
    }
}
