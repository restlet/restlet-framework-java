/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.crypto;

import java.security.GeneralSecurityException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.util.Base64;
import org.restlet.ext.crypto.internal.CryptoUtils;
import org.restlet.security.ChallengeAuthenticator;

/**
 * Challenge authenticator based on browser cookies. This is useful when the web
 * application requires a finer grained control on the login and logout process
 * and can't rely solely on standard schemes such as
 * {@link ChallengeScheme#HTTP_BASIC}.<br>
 * <br>
 * Login can be automatically handled by intercepting HTTP POST calls to the
 * {@link #getLoginPath()} URI. The request entity should contain an HTML form
 * with two fields, the first one named {@link #getIdentifierFormName()} and the
 * second one named {@link #getSecretFormName()}.<br>
 * <br>
 * Logout can be automatically handled as well by intercepting HTTP GET or POST
 * calls to the {@link #getLogoutPath()} URI.<br>
 * <br>
 * After login or logout, the user's browser can be redirected to the URI
 * provided in a query parameter named by {@link #getRedirectQueryName()}.<br>
 * <br>
 * When the credentials are missing or stale, the
 * {@link #challenge(Response, boolean)} method is invoked by the parent class,
 * and its default behavior is to redirect the user's browser to the
 * {@link #getLoginFormPath()} URI, adding the URI of the target resource as a
 * query parameter of name {@link #getRedirectQueryName()}.<br>
 * <br>
 * Note that credentials, both identifier and secret, are stored in a cookie in
 * an encrypted manner. The default encryption algorithm is AES but can be
 * changed with {@link #setEncryptAlgorithm(String)}. It is also strongly
 * recommended to
 * 
 * @author Remi Dewitte
 * @author Jerome Louvel
 */
public class CookieAuthenticator extends ChallengeAuthenticator {

    /** The name of the cookie that stores log info. */
    private volatile String cookieName;

    /** The name of the algorithm used to encrypt the log info cookie value. */
    private volatile String encryptAlgorithm;

    /**
     * The secret key for the algorithm used to encrypt the log info cookie
     * value.
     */
    private volatile byte[] encryptSecretKey;

    /** The name of the HTML login form field containing the identifier. */
    private volatile String identifierFormName;

    /** Indicates if the login requests should be intercepted. */
    private volatile boolean interceptingLogin;

    /** Indicates if the logout requests should be intercepted. */
    private volatile boolean interceptingLogout;

    /** The URI path of the HTML login form to use to challenge the user. */
    private volatile String loginFormPath;

    /** The login URI path to intercept. */
    private volatile String loginPath;

    /** The logout URI path to intercept. */
    private volatile String logoutPath;

    /** The maximum age of the log info cookie. */
    private volatile int maxCookieAge;

    /**
     * The name of the query parameter containing the URI to redirect the
     * browser to after login or logout.
     */
    private volatile String redirectQueryName;

    /** The name of the HTML login form field containing the secret. */
    private volatile String secretFormName;

    /**
     * Constructor. Use the {@link ChallengeScheme#HTTP_COOKIE} pseudo-scheme.
     * 
     * @param context
     *            The parent context.
     * @param optional
     *            Indicates if this authenticator is optional so alternative
     *            authenticators down the chain can be attempted.
     * @param realm
     *            The name of the security realm.
     * @param encryptSecretKey
     *            The secret key used to encrypt the cookie value.
     */
    public CookieAuthenticator(Context context, boolean optional, String realm,
            byte[] encryptSecretKey) {
        super(context, optional, ChallengeScheme.HTTP_COOKIE, realm);
        this.cookieName = "Credentials";
        this.interceptingLogin = true;
        this.interceptingLogout = true;
        this.identifierFormName = "login";
        this.loginPath = "/login";
        this.logoutPath = "/logout";
        this.secretFormName = "password";
        this.encryptAlgorithm = "AES";
        this.encryptSecretKey = encryptSecretKey;
        this.maxCookieAge = -1;
        this.redirectQueryName = "targetUri";
    }

    /**
     * Constructor for mandatory cookie authenticators.
     * 
     * @param context
     *            The parent context.
     * @param realm
     *            The name of the security realm.
     * @param encryptSecretKey
     *            The secret key used to encrypt the cookie value.
     */
    public CookieAuthenticator(Context context, String realm,
            byte[] encryptSecretKey) {
        this(context, false, realm, encryptSecretKey);
    }

    /**
     * Attempts to redirect the user's browser to the URI provided in a query
     * parameter named by {@link #getRedirectQueryName()}.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    protected void attemptRedirect(Request request, Response response) {
        String targetUri = request.getResourceRef().getQueryAsForm()
                .getFirstValue(getRedirectQueryName());

        if (targetUri != null) {
            response.redirectSeeOther(Reference.decode(targetUri));
        }
    }

    /**
     * Restores credentials from the cookie named {@link #getCookieName()} if
     * available. The usual processing is the followed.
     */
    @Override
    protected boolean authenticate(Request request, Response response) {
        // Restore credentials from the cookie
        Cookie credentialsCookie = request.getCookies().getFirst(
                getCookieName());

        if (credentialsCookie != null) {
            request.setChallengeResponse(parseCredentials(credentialsCookie
                    .getValue()));
        }

        return super.authenticate(request, response);
    }

    /**
     * Sets or updates the credentials cookie.
     */
    @Override
    protected int authenticated(Request request, Response response) {
        try {
            CookieSetting credentialsCookie = getCredentialsCookie(request,
                    response);
            credentialsCookie.setValue(formatCredentials(request
                    .getChallengeResponse()));
            credentialsCookie.setMaxAge(getMaxCookieAge());
        } catch (GeneralSecurityException e) {
            getLogger().log(Level.SEVERE,
                    "Could not format credentials cookie", e);
        }

        return super.authenticated(request, response);
    }

    /**
     * Optionally handles the login and logout actions by intercepting the HTTP
     * calls to the {@link #getLoginPath()} and {@link #getLogoutPath()} URIs.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (isLoggingIn(request, response)) {
            login(request, response);
        } else if (isLoggingOut(request, response)) {
            return logout(request, response);
        }

        return super.beforeHandle(request, response);
    }

    /**
     * This method should be overridden to return a login form representation.<br>
     * By default, it redirects the user's browser to the
     * {@link #getLoginFormPath()} URI, adding the URI of the target resource as
     * a query parameter of name {@link #getRedirectQueryName()}.<br>
     * In case the getLoginFormPath() is not set, it calls the parent's method.
     */
    @Override
    public void challenge(Response response, boolean stale) {
        if (getLoginFormPath() == null) {
            super.challenge(response, stale);
        } else {
            Reference ref = response.getRequest().getResourceRef();
            String redirectQueryName = getRedirectQueryName();
            String redirectQueryValue = ref.getQueryAsForm().getFirstValue(
                    redirectQueryName, "");

            if ("".equals(redirectQueryValue)) {
                redirectQueryValue = new Reference(getLoginFormPath())
                        .addQueryParameter(redirectQueryName, ref.toString())
                        .toString();
            }

            response.redirectSeeOther(redirectQueryValue);
        }
    }

    /**
     * Formats the raws credentials to store in the cookie.
     * 
     * @param challenge
     *            The challenge response to format.
     * @return The raw credentials.
     * @throws GeneralSecurityException
     */
    public String formatCredentials(ChallengeResponse challenge)
            throws GeneralSecurityException {
        // Data buffer
        StringBuffer sb = new StringBuffer();

        // Indexes buffer
        StringBuffer isb = new StringBuffer();
        String timeIssued = Long.toString(System.currentTimeMillis());
        int i = timeIssued.length();
        sb.append(timeIssued);

        isb.append(i);

        String identifier = challenge.getIdentifier();
        sb.append('/');
        sb.append(identifier);

        i += identifier.length() + 1;
        isb.append(',').append(i);

        sb.append('/');
        sb.append(challenge.getSecret());

        // Store indexes at the end of the string
        sb.append('/');
        sb.append(isb);

        return Base64.encode(CryptoUtils.encrypt(getEncryptAlgorithm(),
                getEncryptSecretKey(), sb.toString()), false);
    }

    /**
     * Returns the cookie name to use for the authentication credentials. By
     * default, it is is "Credentials".
     * 
     * @return The cookie name to use for the authentication credentials.
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * Returns the credentials cookie setting. It first try to find an existing
     * cookie. If necessary, it creates a new one.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The credentials cookie setting.
     */
    protected CookieSetting getCredentialsCookie(Request request,
            Response response) {
        CookieSetting credentialsCookie = response.getCookieSettings()
                .getFirst(getCookieName());

        if (credentialsCookie == null) {
            credentialsCookie = new CookieSetting(getCookieName(), null);
            credentialsCookie.setAccessRestricted(true);
            // authCookie.setVersion(1);

            if (request.getRootRef() != null) {
                String p = request.getRootRef().getPath();
                credentialsCookie.setPath(p == null ? "/" : p);
            } else {
                // authCookie.setPath("/");
            }

            response.getCookieSettings().add(credentialsCookie);
        }

        return credentialsCookie;
    }

    /**
     * Returns the name of the algorithm used to encrypt the log info cookie
     * value. By default, it returns "AES".
     * 
     * @return The name of the algorithm used to encrypt the log info cookie
     *         value.
     */
    public String getEncryptAlgorithm() {
        return encryptAlgorithm;
    }

    /**
     * Returns the secret key for the algorithm used to encrypt the log info
     * cookie value.
     * 
     * @return The secret key for the algorithm used to encrypt the log info
     *         cookie value.
     */
    public byte[] getEncryptSecretKey() {
        return encryptSecretKey;
    }

    /**
     * Returns the name of the HTML login form field containing the identifier.
     * Returns "login" by default.
     * 
     * @return The name of the HTML login form field containing the identifier.
     */
    public String getIdentifierFormName() {
        return identifierFormName;
    }

    /**
     * Returns the URI path of the HTML login form to use to challenge the user.
     * 
     * @return The URI path of the HTML login form to use to challenge the user.
     */
    public String getLoginFormPath() {
        return loginFormPath;
    }

    /**
     * Returns the login URI path to intercept.
     * 
     * @return The login URI path to intercept.
     */
    public String getLoginPath() {
        return loginPath;
    }

    /**
     * Returns the logout URI path to intercept.
     * 
     * @return The logout URI path to intercept.
     */
    public String getLogoutPath() {
        return logoutPath;
    }

    /**
     * Returns the maximum age of the log info cookie. By default, it uses -1 to
     * make the cookie only last until the end of the current browser session.
     * 
     * @return The maximum age of the log info cookie.
     * @see CookieSetting#getMaxAge()
     */
    public int getMaxCookieAge() {
        return maxCookieAge;
    }

    /**
     * Returns the name of the query parameter containing the URI to redirect
     * the browser to after login or logout. By default, it uses "targetUri".
     * 
     * @return The name of the query parameter containing the URI to redirect
     *         the browser to after login or logout.
     */
    public String getRedirectQueryName() {
        return redirectQueryName;
    }

    /**
     * Returns the name of the HTML login form field containing the secret.
     * Returns "password" by default.
     * 
     * @return The name of the HTML login form field containing the secret.
     */
    public String getSecretFormName() {
        return secretFormName;
    }

    /**
     * Indicates if the login requests should be intercepted.
     * 
     * @return True if the login requests should be intercepted.
     */
    public boolean isInterceptingLogin() {
        return interceptingLogin;
    }

    /**
     * Indicates if the logout requests should be intercepted.
     * 
     * @return True if the logout requests should be intercepted.
     */
    public boolean isInterceptingLogout() {
        return interceptingLogout;
    }

    /**
     * Indicates if the request is an attempt to log in and should be
     * intercepted.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return True if the request is an attempt to log in and should be
     *         intercepted.
     */
    protected boolean isLoggingIn(Request request, Response response) {
        return isInterceptingLogin()
                && getLoginPath()
                        .equals(request.getResourceRef().getRemainingPart(
                                false, false))
                && Method.POST.equals(request.getMethod());
    }

    /**
     * Indicates if the request is an attempt to log out and should be
     * intercepted.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return True if the request is an attempt to log out and should be
     *         intercepted.
     */
    protected boolean isLoggingOut(Request request, Response response) {
        return isInterceptingLogout()
                && getLogoutPath()
                        .equals(request.getResourceRef().getRemainingPart(
                                false, false))
                && (Method.GET.equals(request.getMethod()) || Method.POST
                        .equals(request.getMethod()));
    }

    /**
     * Processes the login request.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    protected void login(Request request, Response response) {
        // Login detected
        Form form = new Form(request.getEntity());
        Parameter identifier = form.getFirst(getIdentifierFormName());
        Parameter secret = form.getFirst(getSecretFormName());

        // Set credentials
        ChallengeResponse cr = new ChallengeResponse(getScheme(),
                identifier != null ? identifier.getValue() : null,
                secret != null ? secret.getValue() : null);
        request.setChallengeResponse(cr);

        // Attempt to redirect
        attemptRedirect(request, response);
    }

    /**
     * Processes the logout request.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    protected int logout(Request request, Response response) {
        // Clears the credentials
        request.setChallengeResponse(null);
        CookieSetting credentialsCookie = getCredentialsCookie(request,
                response);
        credentialsCookie.setMaxAge(0);

        // Attempt to redirect
        attemptRedirect(request, response);

        return STOP;
    }

    /**
     * Decodes the credentials stored in a cookie into a proper
     * {@link ChallengeResponse} object.
     * 
     * @param cookieValue
     *            The credentials to decode from cookie value.
     * @return The credentials as a proper challenge response.
     */
    protected ChallengeResponse parseCredentials(String cookieValue) {
        try {
            // 1) Decode Base64 string
            byte[] encrypted = Base64.decode(cookieValue);
            
            if (encrypted == null) {
                getLogger().warning(
                        "Cannot decode cookie credentials : " + cookieValue);
            }
            
            // 2) Decrypt the credentials
            String decrypted = CryptoUtils.decrypt(getEncryptAlgorithm(),
                    getEncryptSecretKey(), encrypted);

            // 3) Parse the decrypted cookie value
            int lastSlash = decrypted.lastIndexOf('/');
            String[] indexes = decrypted.substring(lastSlash + 1).split(",");
            int identifierIndex = Integer.parseInt(indexes[0]);
            int secretIndex = Integer.parseInt(indexes[1]);

            // 4) Create the challenge response
            ChallengeResponse cr = new ChallengeResponse(getScheme());
            cr.setRawValue(cookieValue);
            cr.setTimeIssued(Long.parseLong(decrypted.substring(0,
                    identifierIndex)));
            cr.setIdentifier(decrypted.substring(identifierIndex + 1,
                    secretIndex));
            cr.setSecret(decrypted.substring(secretIndex + 1, lastSlash));
            return cr;
        } catch (Exception e) {
            getLogger().log(Level.INFO, "Unable to decrypt cookie credentials",
                    e);
            return null;
        }
    }

    /**
     * Sets the cookie name to use for the authentication credentials.
     * 
     * @param cookieName
     *            The cookie name to use for the authentication credentials.
     */
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * Sets the name of the algorithm used to encrypt the log info cookie value.
     * 
     * @param secretAlgorithm
     *            The name of the algorithm used to encrypt the log info cookie
     *            value.
     */
    public void setEncryptAlgorithm(String secretAlgorithm) {
        this.encryptAlgorithm = secretAlgorithm;
    }

    /**
     * Sets the secret key for the algorithm used to encrypt the log info cookie
     * value.
     * 
     * @param secretKey
     *            The secret key for the algorithm used to encrypt the log info
     *            cookie value.
     */
    public void setEncryptSecretKey(byte[] secretKey) {
        this.encryptSecretKey = secretKey;
    }

    /**
     * Sets the name of the HTML login form field containing the identifier.
     * 
     * @param loginInputName
     *            The name of the HTML login form field containing the
     *            identifier.
     */
    public void setIdentifierFormName(String loginInputName) {
        this.identifierFormName = loginInputName;
    }

    /**
     * Indicates if the login requests should be intercepted.
     * 
     * @param intercepting
     *            True if the login requests should be intercepted.
     */
    public void setInterceptingLogin(boolean intercepting) {
        this.interceptingLogin = intercepting;
    }

    /**
     * Indicates if the logout requests should be intercepted.
     * 
     * @param intercepting
     *            True if the logout requests should be intercepted.
     */
    public void setInterceptingLogout(boolean intercepting) {
        this.interceptingLogout = intercepting;
    }

    /**
     * Sets the URI path of the HTML login form to use to challenge the user.
     * 
     * @param loginFormPath
     *            The URI path of the HTML login form to use to challenge the
     *            user.
     */
    public void setLoginFormPath(String loginFormPath) {
        this.loginFormPath = loginFormPath;
    }

    /**
     * Sets the login URI path to intercept.
     * 
     * @param loginPath
     *            The login URI path to intercept.
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    /**
     * Sets the logout URI path to intercept.
     * 
     * @param logoutPath
     *            The logout URI path to intercept.
     */
    public void setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
    }

    /**
     * Sets the maximum age of the log info cookie.
     * 
     * @param timeout
     *            The maximum age of the log info cookie.
     * @see CookieSetting#setMaxAge(int)
     */
    public void setMaxCookieAge(int timeout) {
        this.maxCookieAge = timeout;
    }

    /**
     * Sets the name of the query parameter containing the URI to redirect the
     * browser to after login or logout.
     * 
     * @param redirectQueryName
     *            The name of the query parameter containing the URI to redirect
     *            the browser to after login or logout.
     */
    public void setRedirectQueryName(String redirectQueryName) {
        this.redirectQueryName = redirectQueryName;
    }

    /**
     * Sets the name of the HTML login form field containing the secret.
     * 
     * @param passwordInputName
     *            The name of the HTML login form field containing the secret.
     */
    public void setSecretFormName(String passwordInputName) {
        this.secretFormName = passwordInputName;
    }

}
