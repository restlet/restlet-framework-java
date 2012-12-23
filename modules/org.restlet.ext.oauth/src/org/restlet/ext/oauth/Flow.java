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

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Enum that lets clients retrieve tokens using different OAuth2 flows.
 * Currently this class enables use of the NONE (Autonomous) flow and the
 * PASSWORD flow. It also supports a client to refresh a token.
 * 
 * The class defines one function doFlow that wraps the above specified ways of
 * retrieving a token from an authorization server.
 * 
 * Example:
 * 
 * <pre>
 * {
 *     &#064;code
 *     OAuthParameters params = new OAuthParameters(&quot;clientId&quot;, &quot;clientSecret&quot;);
 *     Flow f = Flow.NONE;
 *     User u = f.execute(params, null, null, null, null, null);
 * 
 *     f = Flow.PASSWORD;
 *     u = f.execute(params, null, null, &quot;username&quot;, &quot;password&quot;, null);
 * 
 * }
 * </pre>
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-1.4.2">User
 *      Agent Flow</a>
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-1.4.4"
 *      >Autonomous Flow</a>
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-1.4.3"
 *      >Password Flow (Native Application)</a>
 * 
 * @author Martin Svensson
 * @author Kristoffer Gronowski
 */
public enum Flow {
    PASSWORD, REFRESH;

    /**
     * Checks the credentials and returns a completed OAuth user.
     * 
     * @param params
     *            The authentication parameters.
     * @param username
     *            The username to check.
     * @param password
     *            The password to check.
     * @param c
     *            The client connector.
     * @return A completed OAuth user.
     */
    // TODO password should be passed as a char[]
    private static OAuthUser passwordFlow(OAuthParameters params,
            String username, String password, org.restlet.Client c) {
        OAuthUser result = null;
        Form form = new Form();
        form.add(OAuthServerResource.GRANT_TYPE,
                GrantType.password.name());
        form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
        form.add(OAuthServerResource.CLIENT_SECRET, params.getClientSecret());
        form.add(OAuthServerResource.USERNAME, username);
        form.add(OAuthServerResource.PASSWORD, password);

        ClientResource tokenResource = new CookieCopyClientResource(
                params.getBaseRef() + params.getAccessTokenPath());
        tokenResource.setNext(c);

        Context.getCurrentLogger().fine(
                "Sending PasswordFlow form : " + form.getQueryString());
        Representation body = null;

        try {
            body = tokenResource.post(form.getWebRepresentation());

            if (tokenResource.getStatus().isSuccess()) {
                result = OAuthUser.createJson(body);
            }
        } finally {
            if (body != null)
                body.release();
            tokenResource.release();
        }

        return result;
    }

    /**
     * Checks the credentials, refresh the current OAuth token and returns a
     * completed OAuth user.
     * 
     * @param params
     *            The authentication parameters.
     * @param refreshToken
     *            The token to refresh.
     * @param c
     *            The client connector.
     * @return A completed OAuth user.
     */
    private static OAuthUser refreshToken(OAuthParameters params,
            String refreshToken, org.restlet.Client c) {

        OAuthUser result = null;

        ClientResource tokenResource = new CookieCopyClientResource(
                params.getBaseRef() + params.getAccessTokenPath());
        tokenResource.setNext(c);
        Form form = new Form();
        form.add(OAuthServerResource.GRANT_TYPE,
                GrantType.refresh_token.name());
        form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
        form.add(OAuthServerResource.CLIENT_SECRET, params.getClientSecret());
        form.add(OAuthServerResource.REFRESH_TOKEN, refreshToken);

        Context.getCurrentLogger().fine(
                "Sending refresh form : " + form.getQueryString());

        Representation body = tokenResource.post(form.getWebRepresentation());

        if (tokenResource.getStatus().isSuccess()) {
            result = OAuthUser.createJson(body);
        }

        body.release();
        tokenResource.release();

        return result;
    }

    /**
     * Executes a specific OAuth Flow (including token refresh). Based on the
     * chosen flow some of the parameters need to be set (see description). Upon
     * successful authorization returns a new OAuthUser containing an access
     * token that can be used for getting protected resources.
     * 
     * @param params
     *            parameters specifying (clientId, clientSecret, scope etc).
     *            Used for all flows
     * @param callbackUri
     *            callbackUri used for the userAgent flow. The server
     * @param state
     *            used in the userAgent flow
     * @param username
     *            used in the password flow
     * @param password
     *            used in the password flow
     * @param refreshToken
     *            the token to refresh, used in the refresh flow
     * @return OAuthUser containing a token that can be used for access
     */
    public OAuthUser execute(OAuthParameters params, String callbackUri,
            String state, String username, String password, String refreshToken) {

        return execute(params, callbackUri, state, username, password,
                refreshToken, null);
    }

    /**
     * Executes a specific OAuth Flow (including token refresh). Based on the
     * chosen flow some of the parameters need to be set (see description). Upon
     * successful authorization returns a new OAuthUser containing an access
     * token that can be used for getting protected resources.
     * 
     * @param params
     *            parameters specifying OAuth end point. Used for all flows.
     * @param callbackUri
     *            callbackUri used for the userAgent flow.
     * @param state
     *            state that should be returned by the Authorization server.
     *            Used in UserAgent flow.
     * @param username
     *            used in the password flow.
     * @param password
     *            used in the password flow.
     * @param refreshToken
     *            the token to refresh, used in the refresh flow
     * @param client
     *            provided client
     * @return OAuthUser containing a token that can be used for access.
     */
    public OAuthUser execute(OAuthParameters params, String callbackUri,
            String state, String username, String password,
            String refreshToken, org.restlet.Client client) {
        if (this == Flow.PASSWORD) {
            return passwordFlow(params, username, password, client);
        } else if (this == Flow.REFRESH) {
            return refreshToken(params, refreshToken, client);
        }
        return null;
    }

}
