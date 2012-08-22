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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import freemarker.template.Configuration;
import org.restlet.data.CacheDirective;

/**
 * Helper class to the AuhorizationResource Handles Authorization requests. By
 * default it will accept all scopes requested.
 * 
 * To intercept and allow a user to control authorization you should set
 * the OAuthHelper.setAuthPageTemplate parameter. It should contain a static HTML page 
 * or a FreeMarker page that will be loaded with the CLAP protocol straight from root.
 * 
 * Example. Add an AuthPageResource to your inbound root.
 * <pre>
 * {
 *      &#064;code
 *      public Restlet createInboundRoot(){
 *              ...
 *              root.attach(OAuthHelper.getAuthPage(getContext()), AuthPageServerResource.class);
 *              //Set Template for AuthPage:
 *              OAuthHelper.setAuthPageTemplate(&quot;authorize.html&quot;, getContext());
 *              //Dont ask for approval if previously approved
 *              OAuthHelper.setAuthSkipApproved(true, getContext());
 *              ...
 *      }
 *      
 * }
 * </pre>
 * 
 * The FreeMarker data model looks like the following
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
 * Below is an example of a simple FreeMarker page for authorization
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
 * FreeMarker page that will be loaded with the CLAP protocol straight from
 * root.
 * 
 * @author Kristoffer Gronowski
 */

public class AuthPageServerResource extends OAuthServerResource {

    private static final String ACTION_ACCEPT = "Accept";
    private static final String ACTION_REJECT = "Reject";
    
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
    public Representation showPage() throws OAuthException {
        String action = getQuery().getFirstValue("action");
        // Came back after user interacted with the page
        if (action != null) {
            String[] scopes = getQuery().getValuesArray("scope");
            handleAction(action, scopes);
            return new EmptyRepresentation();
        }

        // Check if an auth page is set in the Context
        String authPage = HttpOAuthHelper.getAuthPageTemplate(getContext());
        getLogger().fine("this is auth page: " + authPage);
        if (authPage != null && authPage.length() > 0) {
            getLogger().fine("loading authPage: " + authPage);
            // Check if we should skip the page if already approved scopes
            boolean sameScope = HttpOAuthHelper.getAuthSkipApproved(getContext());
            if (sameScope) {
                String[] scopesArray = getQuery().getValuesArray("scope");

                List<String> scopes = Arrays.asList(scopesArray);
                List<String> previousScopes = Arrays.asList(getQuery()
                        .getValuesArray("grantedScope"));

                if (previousScopes.containsAll(scopes)) {
                    // we already have approved the current scopes being
                    // requested...
                    getLogger().fine(
                            "All scopes already approved. - skip auth page.");
                    handleAction("Accept", scopesArray);
                    return new EmptyRepresentation(); // Will redirect
                }
            }

            addCacheDirective(getResponse(), CacheDirective.noCache());
            return getPage(authPage);
        }
        getLogger().fine("accepting scopes since no authPage: " + authPage);
        // No page automatically accept all the scopes requested
        handleAction(ACTION_ACCEPT, getQuery().getValuesArray("scope"));
        getLogger().fine("action handled");
        return new EmptyRepresentation(); // Will redirect
    }

    /**
     * 
     * Helper method to handle a FORM response. Returns with setting a 307 with
     * the location header. Token if the token flow was requested or code is
     * included.
     * 
     * @param action
     *            as interacted by the user.
     * @param scopes
     *            the scopes that was approved.
     */
    protected void handleAction(String action, String[] scopes) throws OAuthException {
        // TODO: SessionId should maybe be removed
        AuthSession session = getAuthSession();

        if (action.equals(ACTION_REJECT)) {
            getLogger().fine("Rejected.");
            throw new OAuthException(OAuthError.access_denied, "Rejected.", null);
        }
        getLogger().fine("Accepting scopes - in handleAction");
        Client client = session.getClient();
        String id = session.getScopeOwner();

        String redirUrl = session.getDynamicCallbackURI();
        getLogger().fine("OAuth2 get dynamic callback = " + redirUrl);
        if (redirUrl == null || redirUrl.isEmpty()) {
            redirUrl = client.getRedirectUri();
        }

        String location = null;
        ResponseType flow = session.getAuthFlow();

        if (flow.equals(ResponseType.token)) {
            location = generateAgentToken(id, client, redirUrl);
        } else if (flow.equals(ResponseType.code)) {
            location = generateCode(id, client, redirUrl);
        }

        // Following scopes were approved
        AuthenticatedUser user = client.findUser(id);
        if (user == null) {
            // XXX: We 'know' user has created before.
            throw new OAuthException(OAuthError.server_error, "authenticated_user not found", null);
        }

        // clear scopes.... if user wants to downgrade
        user.revokeRoles();

        // TODO compare scopes and add an error if some were not approved.
        // Scope parameter should be appended only if different.

        for (String s : scopes) {
            getLogger().fine("Adding scope = " + s + " to user = " + id);
            user.addRole(Scopes.toRole(s), "");
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
        getLogger().fine("loading: " + authPage);
        TemplateRepresentation result = new TemplateRepresentation(authPage,
                config, MediaType.TEXT_HTML);

        // Build the model
        HashMap<String, Object> data = new HashMap<String, Object>();

        data.put("target", getRootRef() + HttpOAuthHelper.getAuthPage(getContext()));

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
}
