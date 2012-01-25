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

package org.restlet.example.ext.oauth.experimental;

import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Experimental facebook code
 * @author Martin Svensson
 *
 */
public class FB {

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
                "code_and_token");
        form.add(OAuthServerResource.CLIENT_ID, params.getClientId());
        form.add(OAuthServerResource.REDIR_URI, callbackUri);
        if (params.getRoles() != null && params.getRoles().size() > 0) {
            form.add(OAuthServerResource.SCOPE, Scopes.toScope(params.getRoles()));
        }
        form.add("email", fbUser);
        form.add("pass", fbPass);

        String q = form.getQueryString();
        Reference redirRef = new Reference(params.getBaseRef(),
                params.getAuthorizePath(), q, null);
        ClientResource authResource = new CookieCopyClientResource(
                redirRef.toUri());
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

            Context.getCurrentLogger().info(
                    "Redir to = " + authResource.getLocationRef());
            authResource.setReference(authResource.getLocationRef());
            authResource.get();
        }

        if (authResource.getStatus().isSuccess()) {
            result = authResource.getCookieSettings();
        }

        r.release();
        authResource.release();

        return result;
    }
}
