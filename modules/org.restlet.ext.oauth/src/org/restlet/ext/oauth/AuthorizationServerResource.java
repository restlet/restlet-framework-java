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
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.routing.Redirector;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 * Restlet implementation class AuthorizationService. Used for initiating an
 * OAuth 2.0 authorization request.
 * 
 * This Resource is controlled by to Context Attribute Parameters
 * 
 * Implements OAuth 2.0 draft 30
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
 * <b>Originally written by Martin Svensson, Heavily modified for update to draft30.</b>
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * 
 * @see <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-30#section-3.1">OAuth 2 draft 30</a>
 */
public class AuthorizationServerResource extends OAuthServerResource {

    @Override
    protected void doCatch(Throwable t) {
        final OAuthException oex;
        if (t instanceof OAuthException) {
            oex = (OAuthException) t;
        } else if (t.getCause() instanceof OAuthException) {
            oex = (OAuthException) t.getCause();
        } else {
            oex = new OAuthException(OAuthError.server_error, t.getMessage(), null);
        }
        AuthSession session = getAuthSession();
        if (session == null) {
            Representation resp = getErrorPage(
                    HttpOAuthHelper.getErrorPageTemplate(getContext()),
                    oex);
            getResponse().setEntity(resp);
        } else {
            /* 
             * If the resource owner denies the access request or if the request
             * fails for reasons other than a missing or invalid redirection URI,
             * the authorization server informs the client by adding the following
             * parameters to the query component of the redirection URI using the
             * "application/x-www-form-urlencoded" format, per Appendix B:
             * (draft-ietf-oauth-v2-30 4.1.2.1.)
             */
            String redirectURI = session.getDynamicCallbackURI();
            if (redirectURI == null) {
                redirectURI = session.getClient().getRedirectUri();
            }
            sendError(redirectURI, oex, session.getState());
        }
    }
    
    /**
     * Checks that all incoming requests have a type parameter. Requires
     * response_type, client_id and redirect_uri parameters. For the code flow
     * client_secret is also mandatory.
     */
    @Get("html")
    @Post("html")
    public Representation represent() throws OAuthException {
        final Form params = getQuery();
        final Client client;
        final String redirectURI;
        try {
            client = getClient(params);
            redirectURI = getRedirectURI(params, client);
        } catch (OAuthException ex) {
           /* 
            * MUST NOT automatically redirect the user-agent to the invalid redirection URI.
            * (see 3.1.2.4. Invalid Endpoint)
            */
            return getErrorPage(
                    HttpOAuthHelper.getErrorPageTemplate(getContext()),
                    ex);
        } catch (Exception ex) {
            // All other exception should be caught as server_error.
            OAuthException oex = new OAuthException(OAuthError.server_error, ex.getMessage(), null);
            return getErrorPage(
                    HttpOAuthHelper.getErrorPageTemplate(getContext()), 
                    oex);
        }
        
        final AuthSession session;
        try {
            ResponseType responseType = getResponseType(params);
            // TODO: check if unauthorized_client
            session = getAuthSession(client, responseType, redirectURI);
            String[] scope = getScope(params);
            session.setRequestedScope(scope);
        } catch (OAuthException ex) {
            ungetAuthSession();
            throw ex;
        }
        
        if (session.getScopeOwner() == null) {
            // Redirect to login page.
            Reference ref = new Reference("." + HttpOAuthHelper.getLoginPage(getContext()));
            ref.addQueryParameter("continue", getRequest().getOriginalRef().toString(true, false));
            redirectTemporary(ref.toString());
            return new EmptyRepresentation();
        }

        return doPostAuthorization(session);
    }
    
    /**
     * Handle the authorization request.
     * 
     * @param session The OAuth session.
     * 
     * @return The result as a {@link Representation}.
     */
    protected Representation doPostAuthorization(AuthSession session) {
        Reference ref = new Reference("riap://application"
                + HttpOAuthHelper.getAuthPage(getContext()));
        getLogger().fine("Name = " + getApplication().getInboundRoot());
        ref.addQueryParameter("client", session.getClient().getClientId());
        // Requested
        String[] scopes = session.getRequestedScope();

        if (scopes != null && scopes.length > 0) {
            for (String s : scopes)
                ref.addQueryParameter("scope", s);
        }

        // Granted
        AuthenticatedUser user = session.getClient().findUser(session.getScopeOwner());

        if (user != null) { // null before first code generated
            // scopes = OAuthUtils.roluser.getGrantedScopes();
            List<Role> roles = user.getGrantedRoles();
            if (roles != null && roles.size() > 0) {
                for (Role r : roles) {
                    ref.addQueryParameter("grantedScope", Scopes.toScope(r));
                }
            }
        }

        // Redirect to AuthPage.
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
     * Get request parameter "response_type".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected ResponseType getResponseType(Form params) throws OAuthException {
        // check response type:
        String typeString = params.getFirstValue(RESPONSE_TYPE);

        try {
            ResponseType type = Enum.valueOf(ResponseType.class, typeString);
            getLogger().fine("Found flow - " + type);
            return type;
        } catch (IllegalArgumentException iae) {
            throw new OAuthException(OAuthError.unsupported_response_type, "Unsupported flow", null);
        } catch (NullPointerException npe) {
            throw new OAuthException(OAuthError.invalid_request, "No response_type parameter found.", null);
        }
    }
    
    /**
     * Get request parameter "redirect_uri".
     * 
     * @param params
     * @param client
     * @return
     * @throws OAuthException 
     */
    protected String getRedirectURI(Form params, Client client) throws OAuthException {
        // check redir:
        String redirUri = params.getFirstValue(REDIR_URI);
        if (redirUri == null || redirUri.isEmpty()) {
            // As described in draft30 4.1.1., redirect_uri is OPTIONAL.
            // If the optional parameter redirect_uri is not provided,
            // we use the one provided during client registration.
            redirUri = client.getRedirectUri();
            if (redirUri == null) {
                /* if no redirection URI has been registered,
                 * the client MUST include a redirection URI with the
                 * authorization request using the "redirect_uri" request parameter.
                 * (See 3.1.2.3. Dynamic Configuration)
                 */
                throw new OAuthException(OAuthError.invalid_request,
                        "Client MUST include a redirection URI.", null);
            }
        } else if (!redirUri.startsWith(client.getRedirectUri())) {
            // The provided uri is no based on the uri with the client registration.
            throw new OAuthException(OAuthError.invalid_request, "Callback URI does not match.", null);
        }
        return redirUri;
    }
    
    private AuthSession getAuthSession(Client client, ResponseType responseType, String redirectURI) {
        AuthSession session = getAuthSession();

        session = setupSession(session, client, responseType, redirectURI);
        
        User scopeOwner = getRequest().getClientInfo().getUser();
        if (scopeOwner != null) {
            // If user information is present, use as scope owner.
            session.setScopeOwner(scopeOwner.getIdentifier());
        }
        
        return session;
    }
    
    private void ungetAuthSession() {
        String sessionId = getCookies().getFirstValue(ClientCookieID);
        // cleanup cookie..
        if (sessionId != null && sessionId.length() > 0) {
            ConcurrentMap<String, Object> attribs = getContext()
                    .getAttributes();
            attribs.remove(sessionId);
        }
    }

    /**
     * Sets up a session.
     *
     * @param in  The OAuth session.
     * @param client The OAuth client.
     * @param flow The glow.
     * @param redirUri The redirection URI.
     * @param params The authentication parameters.
     */
    protected AuthSession setupSession(AuthSession in, Client client, ResponseType flow, String redirUri) {
        getLogger().fine("Base ref = " + getReference().getParentRef());
        getLogger().fine("OAuth2 session = " + in);

        AuthSession session = in;

        if (session == null) {
            session = new AuthSession(getContext().getAttributes(),
                    new ScheduledThreadPoolExecutor(5));
            CookieSetting cs = new CookieSetting(ClientCookieID, session.getId());
            // TODO create a secure mode setting, update all cookies
            // cs.setAccessRestricted(true);
            // cs.setSecure(true);
            getCookieSettings().add(cs);
            getLogger().fine("Setting cookie in SetupSession - " + session.getId());
        }

        session.setClient(client);
        session.setAuthFlow(flow);

        if (!redirUri.equals(client.getRedirectUri())) {
            session.setDynamicCallbackURI(redirUri);
            getLogger().fine("OAuth2 set dynamic callback = " + redirUri);
        }

        // Save away the state
        String state = getCookies().getFirstValue(STATE);
        if (state != null && !state.isEmpty()) {
            session.setState(state);
        }
        
        return session;
    }
}
