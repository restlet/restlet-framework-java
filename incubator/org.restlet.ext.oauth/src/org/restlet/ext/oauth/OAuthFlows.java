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

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.ext.oauth.internal.OAuthUtils;
import org.restlet.ext.openid.OpenIdFormFrowarder;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Class that lets clients retrieve tokens using different OAuth2 flows.
 * Currently this class enables use of the NONE (Autonomous) flow, the PASSWORD
 * flow and UserAgent flow. It also supports a client to refresh a token.
 * 
 * The class defines one function doFlow that wraps the above specified ways of
 * retrieving a token from an authorization server.
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
public class OAuthFlows {

    public enum Flow {
        NONE, PASSWORD, USERAGENT, REFRESH
    };

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
     * @param flow
     *            the flow to execute
     * @return OAuthUser containing a token that can be used for access
     */
    public static OAuthUser doFlow(OAuthParameters params, String callbackUri,
            String state, String username, String password,
            String refreshToken, Flow flow) {
        return doFlow(params, callbackUri, state, username, password,
                refreshToken, null, flow);
    }

    /**
     * Executes a specific OAuth Flow (including token refresh). Based on the
     * chosen flow some of the parameters need to be set (see description). Upon
     * successful authorization returns a new OAuthUser containing an access
     * token that can be used for getting protected resources.
     * 
     * @param params
     *            parameters specifying OAuth end point. Used for all flows
     * @param callbackUri
     *            callbackUri used for the userAgent flow
     * @param state
     *            state that should be returned by the Authorization server.
     *            Used in UserAgent flow
     * @param username
     *            used in the password flow
     * @param password
     *            used in the password flow
     * @param refreshToken
     *            the token to refresh, used in the refresh flow
     * @param flow
     *            the flow to execute
     * @param client
     *            provided client
     * @return OAuthUser containing a token that can be used for access
     */
    public static OAuthUser doFlow(OAuthParameters params, String callbackUri,
            String state, String username, String password,
            String refreshToken, org.restlet.Client c, Flow flow) {
        if (flow == Flow.PASSWORD) {
            return passwordFlow(params, username, password, c);
        } else if (flow == Flow.NONE) {
            return noneFlow(params, c);
        } else if (flow == Flow.USERAGENT) {
            return userAgent(params, callbackUri, state, c);
        } else if (flow == Flow.REFRESH) {
            return refreshToken(params, refreshToken, c);
        }
        return null;
    }

    private static OAuthUser refreshToken(OAuthParameters params,
            String refreshToken, org.restlet.Client c) {

        OAuthUser result = null;

        ClientResource tokenResource = new CookieCopyClientResource(
                params.getBaseRef() + params.getAccessTokenPath());
        tokenResource.setNext(c);
        Form form = new Form();
        form.add(OAuthServerResource.GRANT_TYPE,
                OAuthServerResource.GrantType.refresh_token.name());
        form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
        form.add(OAuthServerResource.CLIENT_SECRET, params.getClientSecret());
        form.add(OAuthServerResource.REFRESH_TOKEN, refreshToken);

        Context.getCurrentLogger().info(
                "Sending refresh form : " + form.getQueryString());

        Representation body = tokenResource.post(form.getWebRepresentation());

        if (tokenResource.getStatus().isSuccess()) {
            result = OAuthUtils.handleSuccessResponse(body);
        }

        body.release();
        tokenResource.release();

        return result;
    }

    public static OAuthUser userAgent(OAuthParameters params,
            String callbackUri, String state, org.restlet.Client c) {
        OAuthUser result = null;

        Form form = new Form();
        form.add(OAuthServerResource.RESPONSE_TYPE,
                OAuthServerResource.ResponseType.token.name());
        form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
        form.add(OAuthServerResource.REDIR_URI, callbackUri);

        if (params.getRoles() != null && params.getRoles().size() > 0) {
            form.add(OAuthServerResource.SCOPE, OAuthUtils.rolesToScope(params.getRoles()));
        }

        if (state != null && state.length() > 0) {
            form.add(OAuthServerResource.STATE, state);
        }

        String q = form.getQueryString();
        Reference redirRef = new Reference(params.getBaseRef(),
                params.getAuthorizePath(), q, null);
        ClientResource authResource = new CookieCopyClientResource(
                redirRef.toUri());
        authResource.setNext(c);
        authResource.setFollowingRedirects(false); // token is in a 3xx
        Representation r = authResource.get();

        int maxRedirCnt = 10; // Stop the maddness if out of hand...
        int cnt = 0;

        while (authResource.getStatus().isRedirection()) {
            String fragment = authResource.getLocationRef().getFragment();
            if (fragment != null && fragment.length() > 0) {
                Form f = new Form(fragment);

                String accessToken = f
                        .getFirstValue(OAuthServerResource.ACCESS_TOKEN);

                String refreshToken = f
                        .getFirstValue(OAuthServerResource.REFRESH_TOKEN);

                long expiresIn = 0;
                String exp = f.getFirstValue(OAuthServerResource.EXPIRES_IN);

                if (exp != null && exp.length() > 0) {
                    expiresIn = Long.parseLong(exp);
                }

                if (accessToken != null && accessToken.length() > 0) {
                    Context.getCurrentLogger().info(
                            "Successful UserAgent flow : AccessToken = "
                                    + accessToken + " RefreshToken = "
                                    + refreshToken + " ExpiresIn = "
                                    + expiresIn);
                    result = new OAuthUser(null, accessToken, refreshToken,
                            expiresIn);
                    result.setState(f.getFirstValue(OAuthServerResource.STATE));
                    break;
                } else {
                    // String error =
                    // f.getFirstValue(OAuthResource.ACCESS_TOKEN);
                    // TODO throw exception....
                }
            }

            if (++cnt >= maxRedirCnt)
                break;

            Context.getCurrentLogger().info(
                    "Redir to = " + authResource.getLocationRef());
            authResource.setReference(authResource.getLocationRef());

            // FOR TESTING!!!!
            if (cnt == 1) {

                for (CookieSetting cs : authResource.getCookieSettings()) {

                    authResource.getCookies().add(cs.getName(), cs.getValue());
                }
            }

            r = authResource.get();
            // Check if it is a OpenID form forward

            try {
                r = OpenIdFormFrowarder.handleFormRedirect(r, authResource);
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Failed in OpenID FW", e);
            }
        }

        r.release();
        authResource.release();

        return result;
    }

    private static OAuthUser noneFlow(OAuthParameters params,
            org.restlet.Client c) {
        OAuthUser result = null;

        Form form = new Form();
        form.add(OAuthServerResource.GRANT_TYPE,
                OAuthServerResource.GrantType.none.name());
        form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
        form.add(OAuthServerResource.CLIENT_SECRET, params.getClientSecret());

        if (params.getRoles() != null && params.getRoles().size() > 0) {
            form.add(OAuthServerResource.SCOPE, OAuthUtils.rolesToScope(params.getRoles()));
        }

        ClientResource tokenResource = new CookieCopyClientResource(
                params.getBaseRef() + params.getAccessTokenPath());
        tokenResource.setNext(c);
        Context.getCurrentLogger().info(
                "Sending NoneFlow form : " + form.getQueryString());

        Representation body = tokenResource.post(form.getWebRepresentation());

        if (tokenResource.getStatus().isSuccess()) {
            result = OAuthUtils.handleSuccessResponse(body);
        }

        body.release();
        tokenResource.release();

        return result;
    }

    private static OAuthUser passwordFlow(OAuthParameters params,
            String username, String password, org.restlet.Client c) {
        OAuthUser result = null;
        Form form = new Form();
        form.add(OAuthServerResource.GRANT_TYPE,
                OAuthServerResource.GrantType.password.name());
        form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
        form.add(OAuthServerResource.CLIENT_SECRET, params.getClientSecret());
        form.add(OAuthServerResource.USERNAME, username);
        form.add(OAuthServerResource.PASSWORD, password);

        ClientResource tokenResource = new CookieCopyClientResource(
                params.getBaseRef() + params.getAccessTokenPath());
        tokenResource.setNext(c);

        Context.getCurrentLogger().info(
                "Sending PasswordFlow form : " + form.getQueryString());
        Representation body = null;

        try {
            body = tokenResource.post(form.getWebRepresentation());

            if (tokenResource.getStatus().isSuccess()) {
                result = OAuthUtils.handleSuccessResponse(body);
            }
        } finally {
            if (body != null)
                body.release();
            tokenResource.release();
        }

        return result;
    }

}
