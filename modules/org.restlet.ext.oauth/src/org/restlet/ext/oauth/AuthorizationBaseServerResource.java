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

import freemarker.template.Configuration;
import java.util.HashMap;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.representation.Representation;

/**
 * Base Restlet resource class for Authorization service resource. Handle errors
 * according to OAuth2.0 specification, and manage AuthSession. Authorization
 * Endndpoint, Authorization pages, and Login pages should extends this class.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class AuthorizationBaseServerResource extends OAuthServerResource {

    @Override
    protected void doCatch(Throwable t) {
        final OAuthException oex = OAuthException.toOAuthException(t);
        AuthSession session = getAuthSession();
        if (session == null) {
            Representation resp = getErrorPage(
                    HttpOAuthHelper.getErrorPageTemplate(getContext()), oex);
            getResponse().setEntity(resp);
        } else {
            /*
             * If the resource owner denies the access request or if the request
             * fails for reasons other than a missing or invalid redirection
             * URI, the authorization server informs the client by adding the
             * following parameters to the query component of the redirection
             * URI using the "application/x-www-form-urlencoded" format, per
             * Appendix B: (4.2.2.1. Error Response)
             */
            String redirectURI = session.getDynamicCallbackURI();
            if (redirectURI == null) {
                redirectURI = session.getClient().getRedirectUri();
            }
            // Use fragment component for Implicit Grant (4.2.2.1. Error
            // Response)
            boolean fragment = session.getAuthFlow().equals(ResponseType.token);
            sendError(redirectURI, oex, session.getState(), fragment);
        }
    }

    /**
     * Get current authentication session.
     * 
     * @return Current AuthSession
     */
    protected AuthSession getAuthSession() {
        // Get some basic information
        String sessionId = (String) getRequest().getAttributes().get(
                ClientCookieID);
        
        if (sessionId == null)
            sessionId = getCookies().getFirstValue(ClientCookieID);
        
        getLogger().fine("sessionId = " + sessionId);

        AuthSession session = (sessionId == null) ? null
                : (AuthSession) getContext().getAttributes().get(sessionId);
        return session;
    }

    /**
     * 
     * Helper method to format error responses according to OAuth2 spec.
     * (Redirect)
     * 
     * @param redirectURI
     *            redirection URI to send error
     * @param ex
     *            Any OAuthException with error
     * @param state
     *            state parameter as presented in the initial auth request
     * @param fragment
     *            true if use URL Fragment.
     */
    protected void sendError(String redirectURI, OAuthException ex,
            String state, boolean fragment) {
        Reference cb = new Reference(redirectURI);
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
        if (fragment) {
            cb.setFragment(cb.getQuery());
            cb.setQuery("");
        }
        redirectTemporary(cb);
    }

    /**
     * 
     * Helper method to format error responses according to OAuth2 spec. (Non
     * Redirect)
     * 
     * @param errPage
     *            errorPage template name
     * @param ex
     *            Any OAuthException with error
     */
    protected Representation getErrorPage(String errPage, OAuthException ex) {
        Configuration config = new Configuration();
        config.setTemplateLoader(new ContextTemplateLoader(getContext(),
                "clap:///"));
        getLogger().fine("loading: " + errPage);
        TemplateRepresentation response = new TemplateRepresentation(errPage,
                config, MediaType.TEXT_HTML);

        // Build the model
        HashMap<String, Object> data = new HashMap<String, Object>();

        data.put(ERROR, ex.getError().name());
        data.put(ERROR_DESC, ex.getErrorDescription());
        data.put(ERROR_URI, ex.getErrorURI());
        // data.put(STATE, state);

        response.setDataModel(data);

        return response;
    }
}
