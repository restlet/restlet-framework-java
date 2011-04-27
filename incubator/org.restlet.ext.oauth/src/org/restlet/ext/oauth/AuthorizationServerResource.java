/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.ext.oauth;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;

import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.routing.Redirector;

/**
 * Restlet implementation class AuthorizationService Used for initiating an
 * OAuth 2.0 authorization request.
 * 
 * Implements OAuth 2.0 draft 10
 * 
 * @author Kristoffer Gronowski
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-3">OAuth
 *      2 draft 10</a>
 */
public class AuthorizationServerResource extends OAuthServerResource {

    public static final String ID = "id";

    public static final String OPENID = "openid";

    /**
     * Checks that all incoming requests have a type parameter Requires
     * response_type, client_id and redirect_uri parameters. For the code flow
     * client_secret is also mandatory.
     */
    @Get("html")
    @Post("html")
    public Representation represent() {

        Form params = getQuery();
        String sessionId = getCookies().getFirstValue(ClientCookieID);
        // //Special case when we just created the session cookie
        // if( sessionId == null )
        // sessionId =
        // getResponse().getCookieSettings().getFirstValue(ClientCookieID);
        log.info("sessionId = " + sessionId);

        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        AuthSession session = (sessionId == null) ? null
                : (AuthSession) attribs.get(sessionId);

        String id = (String) getContext().getAttributes().get(ID);
        log.info("id = " + id);
        log.info("session = " + session);
        if (session != null)
            log.info("client = " + session.getClient());
        else { // cleanup old cookie
            getResponse().getCookieSettings().removeAll(ClientCookieID);
        }

        if (id != null && session != null && session.getClient() != null) {

            log.info("After Authentication - cleanup");
            params.removeFirst(OPENID);
            Client client = (Client) session.getClient();
            log.info("Found client = " + client);
            session.setScopeOwner(id);
            return doPostAuthenticate(session, client);
        }

        String typeString = params.getFirstValue(RESPONSE_TYPE);
        log.info("In service type = " + typeString);

        try {
            ResponseType type = Enum.valueOf(ResponseType.class, typeString);
            log.info("Found flow - " + type);
            if (Method.GET.equals(getMethod())) {
                doGet(type, session);
            } else
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        } catch (IllegalArgumentException iae) {
            sendError(sessionId, OAuthError.UNSUPPORTED_RESPONSE_TYPE,
                    params.getFirstValue(STATE), "Unsupported flow", null);
            log.log(Level.WARNING, "Error in execution.", iae);
        } catch (NullPointerException npe) {
            sendError(sessionId, OAuthError.INVALID_REQUEST,
                    params.getFirstValue(STATE),
                    "No response_type parameter found.", null);
        }

        return getResponse().getEntity();
    }

    protected void doGet(ResponseType flow, AuthSession session) {
        log.info("doGet()");
        // Form requestHeaders = (Form)
        // getRequest().getAttributes().get("org.restlet.http.headers");
        Form params = getQuery();
        String clientId = params.getFirstValue(CLIENT_ID);
        String sessionId = null;
        if (session != null)
            sessionId = session.getId();

        // user_agent, web_server
        if (clientId != null && clientId.length() > 0) {
            try {
                doServerFlow(flow, clientId, session);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error in execution.", e);
                setStatus(Status.SERVER_ERROR_INTERNAL);
                log.log(Level.WARNING, "", e);
            }
        } else { // No client ID
            sendError(sessionId, OAuthError.INVALID_REQUEST,
                    params.getFirstValue(STATE),
                    "No client_id parameter found.", null);
            log.info("Could not find client ID");
        }
    }

    private void doServerFlow(ResponseType flow, String clientId,
            AuthSession session) {
        Form params = getQuery();
        String redirUri = params.getFirstValue(REDIR_URI);

        String sessionId = null;
        if (session != null)
            sessionId = session.getId();

        if (redirUri == null || redirUri.length() == 0) {
            sendError(sessionId, OAuthError.INVALID_REQUEST,
                    params.getFirstValue(STATE),
                    "No redirect_uri parameter found.", null);
            log.info("No mandatory redirect URI provided");
            return;
        }

        // Check that clientID and redirURI match
        Client client = clients.findById(clientId);
        log.info("Client = " + client);

        if (client == null) {
            // client = clients.createClient(clientId, redirUri);
            sendError(sessionId, OAuthError.INVALID_CLIENT,
                    params.getFirstValue(STATE),
                    "Need to register the client : " + clientId, null);
            log.info("Need to register the client : " + clientId);
            return;
        }

        log.info("Compare client redir:provided redir = "
                + client.getRedirectUri() + ":" + redirUri);

        if (!redirUri.startsWith(client.getRedirectUri())) {
            sendError(sessionId, OAuthError.REDIRECT_URI_MISMATCH,
                    params.getFirstValue(STATE),
                    "Callback URI does not match.", null);
            log.info("Callback URI does not match.");
            return;
        }

        // Set the real redir URI since it might be longer then the entered one

        // Cookie or OpenID
        if (session != null && session.getScopeOwner() != null) {

            if (flow.equals(ResponseType.token)
                    || flow.equals(ResponseType.code)) {

                String[] requestedScopes = parseScope(params
                        .getFirstValue(SCOPE));
                for (String scope : requestedScopes) {
                    log.info("Requested scopes = " + scope);
                }

                session.setClient(client);
                session.setAuthFlow(flow);
                session.setRequestedScope(requestedScopes);

                // Dynamic URL- we know that the base is same
                // Might be used to allow more fine grained path or query params
                if (!redirUri.equals(client.getRedirectUri())) {
                    session.setDynamicCallbackURI(redirUri);
                    getLogger().info(
                            "OAuth2 set dynamic callback = " + redirUri);
                }

                // Save away the state
                String state = getCookies().getFirstValue(STATE);
                if (state != null && state.length() > 0)
                    session.setState(state);

                // Use this code if granted scopes should be remembered or no
                // scope

                // String location = generateAgentToken(userId, client);
                // redirectTemporary(location.toString());
                // We know the user but asking for scope specific
                getResponse().setEntity(doPostAuthenticate(session, client));
                return;
            }
        } else { // need to login
            log.info("Base ref = " + getReference().getParentRef());
            Reference ref = new Reference("riap://application/login");
            log.info("OAuth2 session = " + session);
            if (session == null) {
                session = new AuthSession(getContext().getAttributes(),
                        new ScheduledThreadPoolExecutor(5));
                CookieSetting cs = new CookieSetting(ClientCookieID,
                        session.getId());
                // TODO create a secure mode setting, update all cookies
                // cs.setAccessRestricted(true);
                // cs.setSecure(true);
                getResponse().getCookieSettings().add(cs);
                log.info("Setting cookie - " + session.getId());

            }
            session.setClient(client);
            session.setAuthFlow(flow);

            if (!redirUri.equals(client.getRedirectUri())) {
                session.setDynamicCallbackURI(redirUri);
                getLogger().info("OAuth2 set dynamic callback = " + redirUri);
            }

            // Save away the state
            String state = getCookies().getFirstValue(STATE);
            if (state != null && state.length() > 0)
                session.setState(state);

            // Get scope and scope owner
            String[] scopes = parseScope(params.getFirstValue(SCOPE));
            session.setRequestedScope(scopes);

            // String owner = params.getFirstValue(OWNER);
            // if( owner != null && owner.length() > 0) {
            // session.setScopeOwner(owner);
            // }

            log.info("Setting callback url after Authentication = "
                    + getReference().getBaseRef());
            Reference cb = new Reference(getReference().getBaseRef());
            cb.addQueryParameter("internal", null); // set the internal flag
                                                    // optimization
            // Append the callback to the request URL to be picked up by the
            // CallbackFilter
            ref.addQueryParameter("callback", cb.toString(true, false));
            getResponse().setCacheDirectives(noStore);

            Redirector dispatcher = new Redirector(getContext(),
                    ref.toString(), Redirector.MODE_SERVER_OUTBOUND);
            dispatcher.handle(getRequest(), getResponse());
        }
    }

    protected Representation doPostAuthenticate(AuthSession session,
            Client client) {
        // Reference ref = new
        // Reference(getReference().getParentRef(),"auth_page");
        Reference ref = new Reference("riap://application/auth_page");
        log.info("Name = " + getApplication().getInboundRoot());
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
            scopes = user.getGrantedScopes();
            if (scopes != null && scopes.length > 0) {
                for (String s : scopes)
                    ref.addQueryParameter("grantedScope", s);
            }
        }
        log.info("Redir = " + ref);
        Redirector dispatcher = new Redirector(getContext(), ref.toString(),
                Redirector.MODE_SERVER_OUTBOUND);
        dispatcher.handle(getRequest(), getResponse());

        return getResponse().getEntity();

    }

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
