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

package org.restlet.ext.oauth.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.provider.OAuthServerResource;
import org.restlet.ext.oauth.provider.OAuthUser;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.security.User;
import org.restlet.util.Series;

/**
 * Helper class for creating client OAuth code
 * 
 * @author Kristoffer Gronowski
 */
public class OAuthUtils {

    /**
     * Fetch a new accessToken based on a refreshToken received in the initial
     * OAuth request.
     * 
     * @param params
     *            OAuth parameters
     * @param refreshToken
     *            as received in the initial accessToken response
     * @return OAuthUser object containing accessToken, refreshToken and
     *         expiration time.
     * @see OAuthParameters
     */

    public static OAuthUser refreshToken(OAuthParameters params,
            String refreshToken) {
        OAuthUser result = null;
        ClientResource tokenResource = new ClientResource(params.baseRef
                + params.accessTokenPath);

        Form form = new Form();
        form.add(OAuthServerResource.GRANT_TYPE,
                OAuthServerResource.GrantType.refresh_token.name());
        form.add(OAuthServerResource.CLIENT_ID, params.clientId);
        form.add(OAuthServerResource.CLIENT_SECRET, params.clientSecret);
        form.add(OAuthServerResource.REFRESH_TOKEN, refreshToken);
        // form.add(OAuthResource.REDIR_URI,
        // request.getResourceRef().getBaseRef().toUri().toString());

        Context.getCurrentLogger().info(
                "Sending refresh form : " + form.getQueryString());

        Representation body = tokenResource.post(form.getWebRepresentation());

        if (tokenResource.getResponse().getStatus().isSuccess()) {
            result = handleSuccessResponse(body);
        }

        body.release();
        tokenResource.release();

        return result;
    }

    /**
     * Fetch a new accessToken using the userAgent flow This flow is specified
     * as response_type = token
     * 
     * Most useful for JavaScript applications, but could also be used by a REST
     * endpoint client.
     * 
     * @param params
     *            OAuth parameters
     * @param callbackUri
     *            selfURI previously allocated in the authServer
     * @param state
     *            a state string that the authorization server should return
     * @return OAuthUser object containing accessToken, refreshToken and
     *         expiration time.
     * @see <a
     *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-1.4.2">User
     *      Agent Flow</a>
     */

    // TODO add error Exception....
    public static OAuthUser userAgent(OAuthParameters params,
            String callbackUri, String state) {

        OAuthUser result = null;

        Form form = new Form();
        form.add(OAuthServerResource.RESPONSE_TYPE,
                OAuthServerResource.ResponseType.token.name());
        form.add(OAuthServerResource.CLIENT_ID, params.clientId);
        form.add(OAuthServerResource.REDIR_URI, callbackUri);
        if (params.scope != null && params.scope.length() > 0) {
            form.add(OAuthServerResource.SCOPE, params.scope);
        }
        if (state != null && state.length() > 0) {
            form.add(OAuthServerResource.STATE, state);
        }

        String q = form.getQueryString();
        Reference redirRef = new Reference(params.baseRef,
                params.authorizePath, q, null);
        ClientResource authResource = new ClientResource(redirRef.toUri());
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

            Context.getCurrentLogger()
                    .info("Redir to = "
                            + authResource.getResponse().getLocationRef());
            authResource.setReference(authResource.getResponse()
                    .getLocationRef());
            authResource.get();
        }

        r.release();
        authResource.release();

        return result;
    }

    /**
     * Fetch a set of cookies based using the userAgent flow This flow is
     * specified as response_type = token
     * 
     * <strong>Experimental due to that FB has not specified any of
     * this!</strong>
     * 
     * @param params
     *            OAuth parameters [clientId, clientSecret, auth endpoint uri,
     *            scope etc]
     * @param callbackUri
     *            selfURI previously allocated in the authServer
     * @return Series<CookieSetting> object containing access cookies.
     * @see <a
     *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-1.4.2">User
     *      Agent Flow</a>
     */

    public static Series<CookieSetting> fbUserAgent(OAuthParameters params,
            String callbackUri, String fbUser, String fbPass) {

        Series<CookieSetting> result = null;

        Form form = new Form();
        form.add(OAuthServerResource.RESPONSE_TYPE,
                OAuthServerResource.ResponseType.code_and_token.name());
        form.add(OAuthServerResource.CLIENT_ID, params.clientId);
        form.add(OAuthServerResource.REDIR_URI, callbackUri);
        if (params.scope != null && params.scope.length() > 0) {
            form.add(OAuthServerResource.SCOPE, params.scope);
        }
        form.add("email", fbUser);
        form.add("pass", fbPass);

        String q = form.getQueryString();
        Reference redirRef = new Reference(params.baseRef,
                params.authorizePath, q, null);
        ClientResource authResource = new ClientResource(redirRef.toUri());
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
                    break;
                }
            }

            if (++cnt >= maxRedirCnt)
                break;

            Context.getCurrentLogger()
                    .info("Redir to = "
                            + authResource.getResponse().getLocationRef());
            authResource.setReference(authResource.getResponse()
                    .getLocationRef());
            authResource.get();
        }

        if (authResource.getStatus().isSuccess()) {
            result = authResource.getCookieSettings();
        }

        r.release();
        authResource.release();

        return result;
    }

    /**
     * Fetch a new accessToken based using the autonomous none flow
     * 
     * @param params
     *            OAuth parameters [clientId, clientSecret, callback, scope etc]
     * @return OAuthUser object containg accessToken, refreshToken and
     *         expiration time. * @see <a
     *         href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-1.4.4"
     *         >Autonomous Flow</a>
     */

    public static OAuthUser noneFlow(OAuthParameters params) {
        OAuthUser result = null;

        Form form = new Form();
        form.add(OAuthServerResource.GRANT_TYPE,
                OAuthServerResource.GrantType.none.name());
        form.add(OAuthServerResource.CLIENT_ID, params.clientId);
        form.add(OAuthServerResource.CLIENT_SECRET, params.clientSecret);
        if (params.scope != null && params.scope.length() > 0) {
            form.add(OAuthServerResource.SCOPE, params.scope);
        }

        ClientResource tokenResource = new ClientResource(params.baseRef
                + params.accessTokenPath);

        Context.getCurrentLogger().info(
                "Sending NoneFlow form : " + form.getQueryString());

        Representation body = tokenResource.post(form.getWebRepresentation());

        if (tokenResource.getResponse().getStatus().isSuccess()) {
            result = handleSuccessResponse(body);
        }

        body.release();
        tokenResource.release();

        return result;
    }

    /**
     * Convert successful JSON token body responses to OAuthUser.
     * 
     * @param body
     *            Representation containing a successful JSON body element.
     * @return OAuthUser object containing accessToken, refreshToken and
     *         expiration time.
     */

    public static OAuthUser handleSuccessResponse(Representation body) {
        Logger log = Context.getCurrentLogger();
        try {
            // Debug test for tracing back error
            String text = body.getText();
            log.info("Debug JSON body = " + text);
            StringRepresentation sr = new StringRepresentation(text);

            JsonRepresentation returned = new JsonRepresentation(sr);
            JSONObject answer = returned.getJsonObject();

            log.info("Got answer on JSON = " + answer.toString());

            String accessToken = null;
            if (answer.has(OAuthServerResource.ACCESS_TOKEN)) {
                accessToken = answer
                        .getString(OAuthServerResource.ACCESS_TOKEN);
                log.info("AccessToken = " + accessToken);
            }

            String refreshToken = null;
            if (answer.has(OAuthServerResource.REFRESH_TOKEN)) {
                refreshToken = answer
                        .getString(OAuthServerResource.REFRESH_TOKEN);
                log.info("RefreshToken = " + refreshToken);
            }

            long expiresIn = 0;
            if (answer.has(OAuthServerResource.EXPIRES_IN)) {
                expiresIn = answer.getLong(OAuthServerResource.EXPIRES_IN);
                log.info("ExpiresIn = " + expiresIn);
            }

            // Store away the user
            return new OAuthUser(null, accessToken, refreshToken, expiresIn);

        } catch (JSONException e) {
            log.log(Level.WARNING, "Error parsing JSON", e);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error creating representation JSON", e);
        }
        return null;
    }

    /**
     * Retrieve the access token from the user if and only if the user is of
     * type OAuthUser
     * 
     * @param user
     * @return access token
     * @see org.restlet.ext.oauth.provider.OAuthUser
     */
    public static String getToken(User user) {
        String token = null;
        if (user != null) {
            if (user instanceof OAuthUser) {
                OAuthUser ou = (OAuthUser) user;
                token = ou.getAccessToken();
            } else { // Token is stored in secret field
                token = new String(user.getSecret());
            }
        }
        return token;
    }

}
