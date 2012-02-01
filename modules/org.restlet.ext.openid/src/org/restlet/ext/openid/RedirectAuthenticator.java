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

package org.restlet.ext.openid;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Status;
import org.restlet.security.Authenticator;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 * An authenticator that redirects the authentication to some external resource.
 * After successful authentication, it will do a redirect to the original
 * request resourceRef. The RedirectAuthenticator keeps track of state using a
 * session cookie which is not automatically cleaned.
 * 
 * The typical use case for this {@link Authenticator} is to do remote
 * authentication using OpenID.
 * 
 * The RedirectAuthenticator has the following logic based on {@link Verifier}
 * returns:
 * <ol>
 * <li>If the verifier returns {@link Verifier#RESULT_VALID} it will clean up
 * any unneeded cookies and do a
 * {@link Response#redirectPermanent(org.restlet.data.Reference)} to the
 * original resource
 * <li>If the result is {@link Verifier#RESULT_INVALID} or
 * {@link Verifier#RESULT_UNKNOWN} it will clean up all cookies and call forbid
 * (default behavior to set {@link Status#CLIENT_ERROR_FORBIDDEN} if no
 * errorResource has been set)
 * <li>If the result is any other it will clean up the identifierCookie.
 * </ol>
 * 
 * <pre>
 * 
 * </pre>
 * 
 * @author Martin Svensson
 */
public class RedirectAuthenticator extends Authenticator {

    /** The default name of the cookie that contains the identifier. */
    public final static String DEFAULT_IDENTIFIER_COOKIE = "session_id";

    /**
     * The default name of the cookie that contains the original request's
     * reference.
     */
    public final static String DEFAULT_ORIGINAL_REF_COOKIE = "original_ref";

    public final static String ORIGINAL_REF_ATTRIBUTE = "origRef";

    public static void clearIdentifierCookie(String cookieId, Request req,
            Response res) {
        Cookie cookie = req.getCookies().getFirst(cookieId);
        CookieSetting identifierCookie = res.getCookieSettings().getFirst(
                cookieId);
        if (identifierCookie == null && cookie != null) {
            identifierCookie = new CookieSetting(cookieId, null);
            res.getCookieSettings().add(identifierCookie);
        }
        if (identifierCookie != null)
            identifierCookie.setMaxAge(0);
    }

    public static void clearIdentiiferCookie(Request req, Response res) {
        clearIdentifierCookie(DEFAULT_IDENTIFIER_COOKIE, req, res);
    }

    // private final String errorResource;
    /**
     * The restlet in charge of handling authentication or authorization
     * failure.
     */
    private Restlet forbiddenResource;

    /** The current name of the cookie that contains the identifier. */
    private final String identifierCookie;

    /**
     * The current name of the cookie that contains the original request's
     * reference.
     */
    private final String origRefCookie;

    /** The verifier of the credentials. */
    private final Verifier verifier;

    /**
     * Initialize a RedirectAuthenticator with a Verifier.
     * 
     * @param context
     *            - Context
     * @param verifier
     *            - A Verifier that sets user identifier upon completion
     */
    public RedirectAuthenticator(Context context, Verifier verifier,
            Restlet forbiddenResource) {
        super(context);
        this.forbiddenResource = forbiddenResource;
        this.verifier = verifier;
        this.origRefCookie = DEFAULT_ORIGINAL_REF_COOKIE;
        this.identifierCookie = DEFAULT_IDENTIFIER_COOKIE;
    }

    /**
     * Initializes a RedirectAuthenticator with a Verifier.
     * 
     * @param context
     *            The context.
     * @param verifier
     *            The verifier that sets user identifier upon completion.
     * @param identifierCookie
     *            The name of the cookie that contains the identifier.
     * @param origRefCookie
     *            The name of the cookie that contains the original request's
     *            reference.
     * @param forbiddenResource
     *            The Restlet that will handle the call in case of
     *            authentication or authorization failure.
     */
    public RedirectAuthenticator(Context context, Verifier verifier,
            String identifierCookie, String origRefCookie,
            Restlet forbiddenResource) {
        super(context);
        this.forbiddenResource = forbiddenResource;
        this.verifier = verifier;
        this.identifierCookie = identifierCookie != null ? identifierCookie
                : DEFAULT_IDENTIFIER_COOKIE;
        this.origRefCookie = origRefCookie != null ? origRefCookie
                : DEFAULT_ORIGINAL_REF_COOKIE;
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        User u = request.getClientInfo().getUser();
        String identifier = request.getCookies()
                .getFirstValue(identifierCookie);
        String origRef;

        if (identifier != null) {
            u = new User(identifier);
            request.getClientInfo().setUser(u);
            handleUser(u, true);
            return true;
        }

        if (request.getCookies().getFirstValue(origRefCookie) == null) {
            origRef = request.getResourceRef().toString();
            response.getCookieSettings().add(origRefCookie,
                    request.getResourceRef().toString());
        } else {
            origRef = request.getCookies().getFirstValue(origRefCookie);
        }

        int verified = verifier.verify(request, response);
        getLogger().fine("VERIFIED: " + verified);

        if (verified == Verifier.RESULT_VALID) {
            response.getCookieSettings().removeAll(identifierCookie);
            response.getCookieSettings().add(identifierCookie,
                    request.getClientInfo().getUser().getIdentifier());
            handleUser(request.getClientInfo().getUser(), false);
            // String origRef =
            // request.getCookies().getFirstValue(origRefCookie);
            request.getCookies().removeAll(origRefCookie);
            response.getCookieSettings().removeAll(origRefCookie);

            if (origRef != null) {
                response.redirectPermanent(origRef);

            }

            return true;
        }

        response.getCookieSettings().removeAll(identifierCookie);

        if (verified == Verifier.RESULT_UNKNOWN
                || verified == Verifier.RESULT_INVALID) {

            origRef = response.getCookieSettings().getFirstValue(origRefCookie);

            if (origRef == null)
                origRef = request.getCookies().getFirstValue(origRefCookie);

            // request.getCookies().removeAll(origRefCookie);
            // response.getCookieSettings().removeAll(origRefCookie);
            forbid(origRef, request, response);
        }

        return false;
    }

    /**
     * Rejects the call due to a failed authentication or authorization. This
     * can be overridden to change the default behavior, for example to display
     * an error page. By default, calls errorResource.handle (if provided)
     * otherwise it will set the response status to ClIENT_ERROR_FORBIDDEN
     * 
     * @param origRef
     *            The original ref stored by the RedirectAuthenticator
     * @param request
     *            The rejected request.
     * @param response
     *            The reject response.
     */
    public void forbid(String origRef, Request request, Response response) {
        if (forbiddenResource == null)
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        else {
            getLogger().fine("sending to error resource");
            forbiddenResource.handle(request, response);
        }

    }

    /**
     * Handles the retrieved user from the verifier. The only thing that will be
     * stored is the user identifier (in a cookie). Should be overridden as it
     * does nothing by default.
     * 
     * @param user
     *            The user.
     */
    protected void handleUser(User user, boolean cached) {
        getLogger().info(
                "Handle User: " + user.getIdentifier() + " " + user.getEmail());
        ;
    }

    @Override
    protected int unauthenticated(Request request, Response response) {
        int ret = super.unauthenticated(request, response);
        return ret;
    }

    @Override
    protected int authenticated(Request request, Response response) {
        int ret = super.authenticated(request, response);
        if( response != null && response.getStatus().isRedirection() )
            return STOP;
        return ret;
    }

}
