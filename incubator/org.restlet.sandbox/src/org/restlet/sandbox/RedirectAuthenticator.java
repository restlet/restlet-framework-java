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

package org.restlet.sandbox;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;

/**
 * @author msvens
 * 
 */
public class RedirectAuthenticator extends CookieAuthenticator {

    /**
     * The default name of the cookie that contains the original request's
     * reference.
     */
    public final static String DEFAULT_ORIGINAL_REF_COOKIE = "original_ref";

    /**
     * The current name of the cookie that contains the original request's
     * reference.
     */
    private final String origRefCookie;

    public RedirectAuthenticator(Context context, boolean optional,
            ChallengeScheme challengeScheme, String realm) {
        super(context, optional, challengeScheme, realm);
        this.origRefCookie = DEFAULT_ORIGINAL_REF_COOKIE;
    }

    @Override
    protected int authenticated(Request request, Response response) {
        int ret = super.authenticated(request, response);
        Cookie redir = this.getOriginalRefCookie(request);
        String ref = redir != null ? redir.getValue() : null;
        clearCookie(origRefCookie, request, response);
        if (ref != null && response != null)
            response.redirectPermanent(ref);
        if (response != null && response.getStatus().isRedirection())
            return STOP;
        return ret;
    }

    public static void clearCookie(String cookieId, Request req, Response res) {
        Cookie cookie = req.getCookies().getFirst(cookieId);
        CookieSetting cookieSetting = res.getCookieSettings()
                .getFirst(cookieId);
        if (cookieSetting == null && cookie != null) {
            cookieSetting = new CookieSetting(cookieId, null);
            res.getCookieSettings().add(cookieSetting);
        }
        if (cookieSetting != null)
            cookieSetting.setMaxAge(0);
    }

    public Cookie getOriginalRefCookie(Request request) {
        return request.getCookies().getFirst(origRefCookie);
    }

    @Override
    public boolean isLoggingOut(Request request, Response response) {
        return false;
    }

    @Override
    public void login(Request request, Response response) {
        String origRef = request.getResourceRef().toString();
        response.getCookieSettings().add(origRefCookie, origRef);
    }

    public boolean isLoggingIn(Request request, Response response) {
        // Restore credentials from the cookie
        Cookie credentialsCookie = request.getCookies().getFirst(
                getCookieName());
        if (credentialsCookie != null || getOriginalRefCookie(request) != null) {
            return false;
        }
        return true;
    }

}
