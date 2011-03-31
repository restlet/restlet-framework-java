/**
 * Copyright 2005-2010 Noelios Technologies.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.oauth.OAuthError.ErrorCode;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import freemarker.template.Configuration;

/**
 * Helper class to the AuhorizationResource Handles Authorization requests. By
 * default it will accept all scopes requested.
 * 
 * To intercept and allow a user to control a Context parameter
 * 'oauth_auth_page' should be set in the attributes. It should contain a static
 * HTML page or a Fremarker page that will be loaded with the CLAP protocol
 * straight from root.
 * 
 * The freemarker data model looks like the following
 * 
 * <pre>
 * {
 *     &#064;code
 *     HashMap&lt;String, Object&gt; data = new HashMap&lt;String, Object&gt;();
 *     data.put(&quot;target&quot;, &quot;/oauth/auth_page&quot;);
 *     data.put(&quot;clientId&quot;, clientId);
 *     data.put(&quot;clientDescription&quot;, client.toString());
 *     data.put(&quot;clientCallback&quot;, client.getRedirectUri());
 *     data.put(&quot;clientName&quot;, client.getApplicationName());
 *     data.put(&quot;requestingScopes&quot;, scopes);
 *     data.put(&quot;grantedScopes&quot;, previousScopes);
 * }
 * </pre>
 * 
 * Below is an example of a simple freemarker page for authorization
 * 
 * <pre>
 * {@code
 * <html>
 * <head>
 * <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
 * <link rel="stylesheet" href="resources/style.css" type="text/css" media="screen"
 *   charset="utf-8">
 * <title>OAuth2 Authorization Server</title>
 * </head>
 * <body>
 *   <div id="container">
 *    <div id="header">
 *      <h2>OAuth authorization page</h2>
 *      <section id="intro">
 *         <h2>Application requesting scope</h2>
 *         <p>Client ClientId = ${clientId} CB = ${clientDescription} wants to get access to your information.</p>
 *       </section>
 *     </div>
 *     <aside>
 *     <form action="${target}" method="get">
 *    <h4>The following private info is requested</h4>
 * 
 *     <#list requestingScopes as r> <input type="checkbox" name="scope" value="${r}" checked />
 *       <b>${r}</b><br/>
 *     </#list> 
 *     <#if grantedScopes?has_content>
 *       <hr />
 *       <h4>Previously approved scopes</h4>
 *       <#list grantedScopes as g> <input type="checkbox" name="scope" value="${g}" checked />
 *         <b>${g}</b><br/>
 *       </#list>
 *     </#if>
 *     <br/>
 *     <input type="submit" name="action" value="Reject"/>
 *     <input type="submit" name="action" value="Accept" />
 *     </form>
 *     </aside>
 *     <footer>
 *       <p class="copyright">Copyright &copy; 2010 Ericsson Inc. All rights reserved.</p>
 *     </footer>
 *   </div>
 * </body>
 * </html>
 * }
 * </pre>
 * 
 * 
 * should be set in the attributes. It should contain a static HTML page or a
 * Fremarker page that will be loaded with the CLAP protocol straight from root.
 * 
 * @author Kristoffer Gronowski
 */

public class AuthPageServerResource extends OAuthServerResource {
    /**
     * Entry point to the AuthPageResource. The AuthorizationResource dispatches
     * the call to this method. Should also be invoked by an eventual HTML page
     * FORM. In the from HTTP GET should be used and a result parameter: action
     * = Accept results in approving requested scope while action = Reject
     * results in a rejection error back to the requestor.
     * 
     * @return HTML page with the graphical policy page
     */

    @Get("html")
    public Representation showPage() {
        String action = getQuery().getFirstValue("action");
        // Came back after user interacted with the page
        if (action != null) {
            String[] scopes = getQuery().getValuesArray("scope");
            handleAction(action, scopes);
            return new EmptyRepresentation();
        }

        // Check if an auth page is set in the Context
        String authPage = (String) getContext().getAttributes().get(
                "oauth_auth_page");
        if (authPage != null && authPage.length() > 0) {

            // Check if we should skip the page if already approved scopes
            String sameScope = (String) getContext().getAttributes().get(
                    "oauth_auth_skip_same_scope");
            if (sameScope != null && Boolean.parseBoolean(sameScope)) {
                String[] scopesArray = getQuery().getValuesArray("scope");

                List<String> scopes = Arrays.asList(scopesArray);
                List<String> previousScopes = Arrays.asList(getQuery()
                        .getValuesArray("grantedScope"));

                if (previousScopes.containsAll(scopes)) {
                    // we already have approved the current scopes being
                    // requested...
                    log.fine("All scopes already approved. - skip auth page.");
                    handleAction("Accept", scopesArray);
                    return new EmptyRepresentation(); // Will redirect
                }
            }

            getResponse().setCacheDirectives(noCache);
            return getPage(authPage);
        }

        // No page automatically accept all the scopes requested
        handleAction("Accept", getQuery().getValuesArray("scope"));
        return new EmptyRepresentation(); // Will redirect
    }

    /**
     * 
     * Helper method to handle a FORM response. Returns with setting a 307 with
     * the location header. Token if the token flow was requested or code is
     * included.
     * 
     * @param action
     *            as interacted by the user
     * @param scopes
     *            the scopes that was approved
     */

    protected void handleAction(String action, String[] scopes) {
        String sessionId = getCookies().getFirstValue(ClientCookieID);
        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        AuthSession session = (sessionId == null) ? null
                : (AuthSession) attribs.get(sessionId);

        if ("Reject".equals(action)) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            sendError(session, ErrorCode.access_denied, session.getState(),
                    "Rejected.", null);
            log.info("Rejected.");
            return;
        }
        Client client = session.getClient();
        String id = session.getScopeOwner();

        String redirUrl = session.getDynamicCallbackURI();
        getLogger().info("OAuth2 get dynamic callback = " + redirUrl);
        if (redirUrl == null || redirUrl.length() == 0)
            redirUrl = client.getRedirectUri();

        String location = null;
        ResponseType flow = session.getAuthFlow();
        if (flow.equals(ResponseType.token)) {
            location = generateAgentToken(id, client, redirUrl);
        } else if (flow.equals(ResponseType.code)) {
            location = generateCode(id, client, redirUrl);
        }

        // Following scopes were approved
        AuthenticatedUser user = client.findUser(session.getScopeOwner());
        if( user == null ) {
        	setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Can't find User id : "+session.getScopeOwner());
        }
        
        // clear scopes.... if user wants to downgrade
        user.revokeScopes();

        // TODO compare scopes and add an error if some were not approved.
        // Scope parameter should be appended only if different.

        for (String s : scopes) {
            getLogger().info("Adding scope = " + s + " to user = " + id);
            user.addScope(s, "");
        }

        String state = session.getState();
        if (state != null && state.length() > 0) {
            // Setting state information back.
            Reference stateful = new Reference(location);
            stateful.addQueryParameter(OAuthServerResource.STATE, state);
            location = stateful.toString();
        }
        // Reset the state
        session.setState(null);
        // Save the user if using DB
        user.persist();
        
        redirectTemporary(location);
    }

    /**
     * Helper method if a auth page was present in a context attribute.
     * 
     * The Freemarker Data model looks the following :
     * 
     * HashMap<String,Object> data = new HashMap<String,Object>();
     * data.put("target", "/oauth/auth_page"); data.put("clientId", clientId);
     * data.put("clientDescription", client.toString());
     * data.put("clientCallback", client.getRedirectUri());
     * data.put("clientName", client.getApplicationName());
     * data.put("requestingScopes", scopes); data.put("grantedScopes",
     * previousScopes);
     * 
     * @param authPage
     *            name of the page in class loader context
     * @return html page representation
     */

    protected Representation getPage(String authPage) {
        String clientId = getQuery().getFirstValue("client");
        Client client = clients.findById(clientId);
        String[] scopes = getQuery().getValuesArray("scope");
        String[] previousScopes = getQuery().getValuesArray("grantedScope");

        Configuration config = new Configuration();
        ContextTemplateLoader ctl = new ContextTemplateLoader(getContext(),
                "clap:///");
        config.setTemplateLoader(ctl);

        TemplateRepresentation result = new TemplateRepresentation(authPage,
                config, MediaType.TEXT_HTML);

        // Build the model
        HashMap<String, Object> data = new HashMap<String, Object>();
        
        data.put("target", getRequest().getRootRef()+"/auth_page");
        // TODO check with Restlet lead
        data.put("clientId", clientId);
        data.put("clientDescription", client.toString());
        data.put("clientCallback", client.getRedirectUri());
        data.put("clientName", client.getApplicationName());
        // scopes
        data.put("requestingScopes", scopes);
        data.put("grantedScopes", previousScopes);

        result.setDataModel(data);
        return result;
    }

    /**
     * 
     * Helper method to format error responses according to OAuth2 spec.
     * 
     * @param session
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

    protected void sendError(AuthSession session, OAuthError.ErrorCode error,
            String state, String description, String errorUri) {
        String redirUri = session.getDynamicCallbackURI();

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
        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        attribs.remove(session.getId());

    }

}
