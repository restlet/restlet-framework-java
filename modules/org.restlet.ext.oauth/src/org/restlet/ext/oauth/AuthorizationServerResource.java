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

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;

import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.routing.Redirector;
import org.restlet.security.Role;

/**
 * Restlet implementation class AuthorizationService. Used for initiating an
 * OAuth 2.0 authorization request.
 * 
 * This Resource is controlled by to Context Attribute Parameters<br/>
 * OAuthServerResource.LOGIN_PARAM specifies the location of a Login resource.
 * 
 * Implements OAuth 2.0 draft 10
 * 
 * The following example shows how to set up a simple Authorization Service.
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
 * @author Martin Svensson
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-3">OAuth
 *      2 draft 10</a>
 */
public class AuthorizationServerResource extends OAuthServerResource {

    /**
     * Checks that all incoming requests have a type parameter. Requires
     * response_type, client_id and redirect_uri parameters. For the code flow
     * client_secret is also mandatory.
     */
    @Get("html")
    @Post("html")
    public Representation represent() {

        // Get some basic information
        Form params = getQuery();
        String sessionId = getCookies().getFirstValue(ClientCookieID);
        getLogger().fine("sessionId = " + sessionId);

        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        AuthSession session = (sessionId == null) ? null
                : (AuthSession) attribs.get(sessionId);

        // check owner:
        String scopeOwner = null;
        if (getRequest().getClientInfo().getUser() != null)
            scopeOwner = getRequest().getClientInfo().getUser().getIdentifier();
        if (scopeOwner == null && session != null)
            scopeOwner = session.getScopeOwner();
        getLogger().fine("OWNER - " + scopeOwner);
        if (scopeOwner == null) {
            sendError(sessionId, OAuthError.invalid_request,
                    params.getFirstValue(STATE), "No Scope Owner", null);
            return getResponseEntity();
        }

        // check clientId:
        String clientId = params.getFirstValue(CLIENT_ID);
        if (clientId == null || clientId.length() < 1) {
            sendError(sessionId, OAuthError.invalid_request,
                    params.getFirstValue(STATE),
                    "No client_id parameter found.", null);
            getLogger().warning("Could not find client ID");
            return getResponseEntity();
        }
        Client client = clients.findById(clientId);
        getLogger().fine("Client = " + client);
        if (client == null) {
            // client = clients.createClient(clientId, redirUri);
            sendError(sessionId, OAuthError.invalid_client,
                    params.getFirstValue(STATE),
                    "Need to register the client : " + clientId, null);
            getLogger().warning("Need to register the client : " + clientId);
            return getResponseEntity();
        }
        getLogger().fine("CLIENT ID - " + clientId);

        // check redir:
        String redirUri = params.getFirstValue(REDIR_URI);
        if (redirUri == null || redirUri.length() == 0) {
            sendError(sessionId, OAuthError.invalid_request,
                    params.getFirstValue(STATE),
                    "No redirect_uri parameter found.", null);
            getLogger().warning("No mandatory redirect URI provided");
            return getResponseEntity();
        }
        if (!redirUri.startsWith(client.getRedirectUri())) {
            sendError(sessionId, OAuthError.redirect_uri_mismatch,
                    params.getFirstValue(STATE),
                    "Callback URI does not match.", null);
            getLogger().warning("Callback URI does not match.");
            return getResponseEntity();
        }
        getLogger().fine("CLIENT ID - " + clientId);

        // check response type:
        String typeString = params.getFirstValue(RESPONSE_TYPE);
        ResponseType type = null;

        try {
            type = Enum.valueOf(ResponseType.class, typeString);
            getLogger().fine("Found flow - " + type);
            if (!Method.GET.equals(getMethod()))
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        } catch (IllegalArgumentException iae) {
            sendError(sessionId, OAuthError.unsupported_response_type,
                    params.getFirstValue(STATE), "Unsupported flow", null);
            getLogger().log(Level.WARNING, "Error in execution.", iae);
        } catch (NullPointerException npe) {
            sendError(sessionId, OAuthError.invalid_request,
                    params.getFirstValue(STATE),
                    "No response_type parameter found.", null);
        }

        getLogger().fine("RESPONSE TYPE - " + type);

        // setup session if needed:
        if (session != null)
            getLogger().fine("client = " + session.getClient());
        else { // cleanup old cookie...and setup session
            getCookieSettings().removeAll(ClientCookieID);
        }
        if (session == null) {
            getLogger().fine("Setting ClientCookieID");
            session = new AuthSession(getContext().getAttributes(),
                    new ScheduledThreadPoolExecutor(5));
            CookieSetting cs = new CookieSetting(ClientCookieID,
                    session.getId());
            // TODO create a secure mode setting, update all cookies
            // cs.setAccessRestricted(true);
            // cs.setSecure(true);
            getCookieSettings().add(cs);
            getLogger().fine("Setting cookie - " + session.getId());
        }
        setupSession(session, client, type, redirUri, params);
        session.setScopeOwner(scopeOwner);

        return doPostAuthenticate(session, client);
    }

    /**
     * Sets up a session.
     * 
     * @param in
     *            The OAuth session.
     * @param client
     *            The OAuth client.
     * @param flow
     *            The glow.
     * @param redirUri
     *            The redirection URI.
     * @param params
     *            The authentication parameters.
     */
    protected void setupSession(AuthSession in, Client client,
            ResponseType flow, String redirUri, Form params) {
        getLogger().fine("Base ref = " + getReference().getParentRef());
        getLogger().fine("OAuth2 session = " + in);

        AuthSession session = in;

        if (session == null) {
            session = new AuthSession(getContext().getAttributes(),
                    new ScheduledThreadPoolExecutor(5));
            CookieSetting cs = new CookieSetting(ClientCookieID,
                    session.getId());
            // TODO create a secure mode setting, update all cookies
            // cs.setAccessRestricted(true);
            // cs.setSecure(true);
            getCookieSettings().add(cs);
            getLogger().fine(
                    "Setting cookie in SetupSession - " + session.getId());
        }

        session.setClient(client);
        session.setAuthFlow(flow);

        if (!redirUri.equals(client.getRedirectUri())) {
            session.setDynamicCallbackURI(redirUri);
            getLogger().fine("OAuth2 set dynamic callback = " + redirUri);
        }

        // Save away the state
        String state = getCookies().getFirstValue(STATE);
        if (state != null && state.length() > 0)
            session.setState(state);

        // Get scope and scope owner
        String[] scopes = parseScope(params.getFirstValue(SCOPE));
        session.setRequestedScope(scopes);
    }

    /**
     * Handle the authentication request.
     * 
     * @param session
     *            The OAuth session.
     * @param client
     *            The OAuth client.
     * @return The result as a {@link Representation}.
     */
    protected Representation doPostAuthenticate(AuthSession session,
            Client client) {

        Reference ref = new Reference("riap://application"
                + HttpOAuthHelper.getAuthPage(getContext()));
        getLogger().fine("Name = " + getApplication().getInboundRoot());
        ref.addQueryParameter("client", client.getClientId());
        // Requested
        String[] scopes = session.getRequestedScope();

        if (scopes != null && scopes.length > 0) {
            for (String s : scopes)
                ref.addQueryParameter("scope", s);
        }

        // Granted
        AuthenticatedUser user = client.findUser(session.getScopeOwner());

        if (user != null) { // null before first code generated
            // scopes = OAuthUtils.roluser.getGrantedScopes();
            List<Role> roles = user.getGrantedRoles();
            if (roles != null && roles.size() > 0) {
                for (Role r : roles)
                    ref.addQueryParameter("grantedScope", Scopes.toScope(r));
            }
        }

        getLogger().fine("Redir = " + ref);
        Redirector dispatcher = new Redirector(getContext(), ref.toString(),
                Redirector.MODE_SERVER_OUTBOUND);
        // getRequest().setCookies(getResponse().getCookieSettings().get
        // getRequest().setCookies(cookies)
        getRequest().getAttributes().put(ClientCookieID, session.getId());
        dispatcher.handle(getRequest(), getResponse());
        return getResponseEntity();

    }

    /**
     * 
     * Helper method to format error responses according to OAuth2 spec.
     * 
     * @param sessionId
     *            local server session object
     * @param error
     *            code, one of the valid from spec
     * @param state
     *            state parameter as presented in the initial auth request
     */
    public void sendError(String sessionId, OAuthError error, String state) {
        sendError(sessionId, error, state, null, null);
    }

    /**
     * 
     * Helper method to format error responses according to OAuth2 spec.
     * 
     * @param sessionId
     *            local server session object
     * @param error
     *            code, one of the valid from spec
     * @param state
     *            state parameter as presented in the initial auth request
     * @param description
     *            any text describing the error
     * @param errorUri
     *            uri to a page with more description about the error
     */
    public void sendError(String sessionId, OAuthError error, String state,
            String description, String errorUri) {
        Form params = getQuery();
        String redirUri = params.getFirstValue(REDIR_URI);
        if (redirUri == null || redirUri.length() == 0) {
            // create a fake uri...
            redirUri = "https://127.0.0.1/cb";
        }
        Reference cb = new Reference(redirUri);
        cb.addQueryParameter("error", error.name());
        if (state != null && state.length() > 0) {
            cb.addQueryParameter("state", state);
        }
        if (description != null && description.length() > 0) {
            cb.addQueryParameter("error_description", description);
        }
        if (errorUri != null && errorUri.length() > 0) {
            cb.addQueryParameter("error_uri", errorUri);
        }
        redirectTemporary(cb.toString());

        // cleanup cookie..
        if (sessionId != null && sessionId.length() > 0) {
            ConcurrentMap<String, Object> attribs = getContext()
                    .getAttributes();
            attribs.remove(sessionId);
        }
    }
}
