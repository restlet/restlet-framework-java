/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.security;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.CryptoUtils;

/**
 * Intercepts /login and /logout requests by default.
 * 
 * On login attempts to login as any other Guard subclass. It expects by default
 * to find a "login" and "password" post parameters.
 * 
 * On logout attempts to discard the cookie.
 * 
 * A time stamp value is embedded in the cookie so that the timeout is also
 * checked server-side.
 * 
 * Also embedded in the cookie the user's principal.
 * 
 * The cookie value is not encrypted by default but it has to be encrypted.
 * Default encryption uses AES with a default cipher secret.
 * 
 * ServerSecret must be a Base64 encrypted string.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class HttpCookieAuthenticator extends ChallengeAuthenticator {

    public static final String DEFAULT_COOKIE_NAME = "Authorization";

    public static final String DEFAULT_LOGIN_PATH = "/login";

    public static final String DEFAULT_LOGOUT_PATH = "/logout";

    public static final String DEFAULT_LOGIN_INPUT_NAME = "login";

    public static final String DEFAULT_PASSWORD_INPUT_NAME = "password";

    public static final String DEFAULT_CRYPT_ALGORITHM = "AES";

    public static final String DEFAULT_AES_SERVER_KEY = "HtWEm80kADxMjLcZ6IbcLQ==";

    protected volatile String cookieName = DEFAULT_COOKIE_NAME;

    protected volatile String loginPath = DEFAULT_LOGIN_PATH;

    protected volatile String logoutPath = DEFAULT_LOGOUT_PATH;

    protected volatile boolean handleLogin = true;

    protected volatile boolean handleLogout = true;

    protected volatile String loginInputName = DEFAULT_LOGIN_INPUT_NAME;

    protected volatile String passwordInputName = DEFAULT_PASSWORD_INPUT_NAME;

    protected volatile String cryptAlgorithm = DEFAULT_CRYPT_ALGORITHM;

    private volatile String serverCipher = DEFAULT_AES_SERVER_KEY;

    // Timeout in minutes
    protected volatile int timeout = -1;

    public HttpCookieAuthenticator(Context context, String realm)
            throws IllegalArgumentException {
        super(context, ChallengeScheme.HTTP_COOKIE, realm);
        setServerCipher(DEFAULT_AES_SERVER_KEY);
    }

    protected void removeCookie(Response resp) {
        CookieSetting authCookie = getOrInitAuthCookieSetting(resp);
        authCookie.setMaxAge(0);
    }

    protected CookieSetting getOrInitAuthCookieSetting(Response response) {
        CookieSetting authCookie = response.getCookieSettings().getFirst(
                cookieName);
        Request request = response.getRequest();
        if (authCookie == null) {
            authCookie = new CookieSetting(cookieName, null);
            authCookie.setAccessRestricted(true);
            // authCookie.setVersion(1); // Problems with Safari !?
            if (request.getRootRef() != null) {
                String p = request.getRootRef().getPath();
                authCookie.setPath(p == null ? "/" : p);
            } else {
                // authCookie.setPath("/");
            }
            response.getCookieSettings().add(authCookie);
        }
        return authCookie;
    }

    /**
     * Handle login and logout. Restore from cookie.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        String rp = request.getResourceRef().getRemainingPart();
        if (handleLogin && loginPath.equals(rp)
                && Method.POST.equals(request.getMethod())) {
            Form f = request.getEntityAsForm();
            Parameter login = f.getFirst(loginInputName);
            Parameter password = f.getFirst(passwordInputName);
            ChallengeResponse cr = new ChallengeResponse(getScheme(),
                    login != null ? login.getValue() : null,
                    password != null ? password.getValue() : null);
            request.setChallengeResponse(cr);
        } else if (handleLogout && logoutPath.equals(rp)) {
            if (request.getChallengeResponse() != null
                    && getScheme().equals(
                            request.getChallengeResponse().getScheme())) {
                request.setChallengeResponse(null);
            }
            removeCookie(response);
        } else {
            // Restore credentials from cookie
            Cookie cookieVal = request.getCookies().getFirst(cookieName);
            if (cookieVal != null) {
                ChallengeResponse cr = new ChallengeResponse(getScheme(),
                        cookieVal.getValue());
                request.setChallengeResponse(cr);
            }
        }
        return super.beforeHandle(request, response);
    }

    /**
     * Sets the cookie
     */
    @Override
    protected void afterHandle(Request request, Response response) {
        super.afterHandle(request, response);
        ChallengeResponse cr = request.getChallengeResponse();
        // Write the cookie if authenticated
        // On logout ChallengeResponse has been set to null
        if (cr != null && getScheme().equals(cr.getScheme())
                && request.getClientInfo().isAuthenticated()) {
            CookieSetting authCookie = getOrInitAuthCookieSetting(response);
            authCookie.setValue(formatCredentials(cr));
            authCookie.setMaxAge(60 * getTimeout());
        }
    }

    @Override
    public void challenge(Response response, boolean stale) {
        if (stale) {
            removeCookie(response);
        }
        response.setStatus(Status.REDIRECTION_SEE_OTHER);
        response.setLocationRef(new Reference(response.getRequest()
                .getResourceRef().getPath()));
    }

    public String formatCredentials(ChallengeResponse challenge) {
        try {
            String cookieValue = challenge.getIdentifier() + '/'
                    + System.currentTimeMillis();
            byte[] credentials;
            if (getServerCipher() == null) {
                credentials = cookieValue.getBytes();
            } else {
                credentials = CryptoUtils.encrypt(getCryptAlgorithm(),
                        getServerCipher(), cookieValue);
            }
            return Base64.encode(credentials, false);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Could not format credentials", e);
            return null;
        }
    }

    @Override
    public void setVerifier(Verifier verifier) {
        super.setVerifier(new CookieVerifier(verifier));
    }

    public Verifier getWrappedVerifier() {
        return ((CookieVerifier) getVerifier()).wrappedVerifier;
    }

    class CookieVerifier extends Verifier {

        final Verifier wrappedVerifier;

        public CookieVerifier(Verifier wrappedVerifier) {
            this.wrappedVerifier = wrappedVerifier;
        }

        @Override
        public int verify(Request request, Response response) {
            ChallengeResponse cr = request.getChallengeResponse();
            try {
                if (cr != null && cr.getCredentials() != null) {
                    final byte[] credentialsEncoded = Base64.decode(cr
                            .getCredentials());
                    if (credentialsEncoded == null) {
                        getLogger().warning(
                                "Cannot decode credentials : "
                                        + cr.getCredentials());
                    }

                    String decoded;
                    if (getServerCipher() == null) {
                        decoded = new String(credentialsEncoded);
                    } else {
                        decoded = CryptoUtils.decrypt(getCryptAlgorithm(),
                                getServerCipher(), credentialsEncoded);
                    }
                    // Process the decoded cookie
                    int si = decoded.lastIndexOf('/');
                    // TimeElapsed in minutes
                    long timeElapsed = (System.currentTimeMillis() - Long
                            .parseLong(decoded.substring(si + 1))) / 60000;
                    int timeout = getTimeout();
                    if (timeout == -1 || timeElapsed < timeout) {
                        cr.setIdentifier(decoded.substring(0, si));
                        return RESULT_VALID;
                    } else {
                        return RESULT_STALE;
                    }
                } else {
                    return wrappedVerifier.verify(request, response);
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Unable to decode credentials",
                        e);
            }
            return RESULT_INVALID;
        }

    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public String getLogoutPath() {
        return logoutPath;
    }

    public void setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
    }

    public boolean isHandleLogin() {
        return handleLogin;
    }

    public void setHandleLogin(boolean handleLogin) {
        this.handleLogin = handleLogin;
    }

    public boolean isHandleLogout() {
        return handleLogout;
    }

    public void setHandleLogout(boolean handleLogout) {
        this.handleLogout = handleLogout;
    }

    public String getLoginInputName() {
        return loginInputName;
    }

    public void setLoginInputName(String loginInputName) {
        this.loginInputName = loginInputName;
    }

    public String getPasswordInputName() {
        return passwordInputName;
    }

    public void setPasswordInputName(String passwordInputName) {
        this.passwordInputName = passwordInputName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getCryptAlgorithm() {
        return cryptAlgorithm;
    }

    public void setCryptAlgorithm(String cryptAlgorithm) {
        this.cryptAlgorithm = cryptAlgorithm;
    }

    public String getServerCipher() {
        return serverCipher;
    }

    public void setServerCipher(String serverCipher) {
        this.serverCipher = serverCipher;
    }
}
