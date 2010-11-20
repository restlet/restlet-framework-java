package org.restlet.example.book.restlet.ch08.sec2;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

public class CookieAuthenticator extends ChallengeAuthenticator {

    private CookieSetting cookieSetting;

    public CookieAuthenticator(Context context, boolean optional, String realm) {
        super(context, optional, ChallengeScheme.HTTP_COOKIE, realm);
    }

    public CookieAuthenticator(Context context, boolean optional, String realm,
            Verifier verifier) {
        super(context, optional, ChallengeScheme.HTTP_COOKIE, realm, verifier);
    }

    public CookieAuthenticator(Context context, String realm) {
        super(context, ChallengeScheme.HTTP_COOKIE, realm);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        super.afterHandle(request, response);
        Cookie cookie = request.getCookies().getFirst("Credentials");

        if (request.getClientInfo().isAuthenticated() && (cookie == null)) {
            String identifier = request.getChallengeResponse().getIdentifier();
            String secret = new String(request.getChallengeResponse()
                    .getSecret());
            CookieSetting cookieSetting = new CookieSetting("Credentials",
                    identifier + "=" + secret);
            cookieSetting.setAccessRestricted(true);
            cookieSetting.setPath("/");
            cookieSetting.setComment("Unsecured cookie based authentication");
            cookieSetting.setMaxAge(30);
            response.getCookieSettings().add(cookieSetting);
        }
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        Cookie cookie = request.getCookies().getFirst("Credentials");

        if (cookie != null) {
            // Extract the challenge response from the cookie
            String[] credentials = cookie.getValue().split("=");

            if (credentials.length == 2) {
                String identifier = credentials[0];
                String secret = credentials[1];
                request.setChallengeResponse(new ChallengeResponse(
                        ChallengeScheme.HTTP_COOKIE, identifier, secret));
            }
        } else if (Method.POST.equals(request.getMethod())
                && request.getResourceRef().getQueryAsForm().getFirst("login") != null) {
            // Intercepting a login form
            Form credentials = new Form(request.getEntity());
            String identifier = credentials.getFirstValue("identifier");
            String secret = credentials.getFirstValue("secret");
            request.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_COOKIE, identifier, secret));

            // Continue call processing to return the target representation if
            // authentication is successful or a new login page
            request.setMethod(Method.GET);
        }

        return super.beforeHandle(request, response);
    }

    /**
     * Returns the modifiable {@link CookieSetting} prototype.
     * 
     * @return The modifiable {@link CookieSetting} prototype.
     */
    public CookieSetting getCookieSetting() {
        return cookieSetting;
    }

    /**
     * Sets the modifiable {@link CookieSetting} prototype.
     * 
     * @param cookieSetting
     *            The modifiable {@link CookieSetting} prototype.
     */
    public void setCookieSetting(CookieSetting cookieSetting) {
        this.cookieSetting = cookieSetting;
    }

}
