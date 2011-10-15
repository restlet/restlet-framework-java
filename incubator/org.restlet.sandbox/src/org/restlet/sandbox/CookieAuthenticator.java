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

import java.security.GeneralSecurityException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.engine.util.Base64;
import org.restlet.ext.crypto.internal.CryptoUtils;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

/**
 * @author msvens
 *
 */
public abstract class CookieAuthenticator extends ChallengeAuthenticator {
    
    /** The name of the cookie that stores log info. */
    private volatile String cookieName;
    
    /** The name of the algorithm used to encrypt the log info cookie value. */
    private volatile String encryptAlgorithm;

    /**
     * The secret key for the algorithm used to encrypt the log info cookie
     * value.
     */
    private volatile byte[] encryptSecretKey;

    public CookieAuthenticator(Context context, boolean optional,
            ChallengeScheme challengeScheme, String realm, Verifier verifier) {
        super(context, optional, challengeScheme, realm, verifier);
        this.cookieName = "Credentials";
        this.encryptAlgorithm = "AES";
    }

    public CookieAuthenticator(Context context, boolean optional,
            ChallengeScheme challengeScheme, String realm) {
        super(context, optional, challengeScheme, realm);
    }

    public CookieAuthenticator(Context context,
            ChallengeScheme challengeScheme, String realm) {
        super(context, challengeScheme, realm);
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
     * Returns the cookie name to use for the authentication credentials. By
     * default, it is is "Credentials".
     * 
     * @return The cookie name to use for the authentication credentials.
     */
    public String getCookieName() {
        return cookieName;
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
    
    public boolean isLoggingIn(Request request, Response response){
     // Restore credentials from the cookie
        Cookie credentialsCookie = request.getCookies().getFirst(
                getCookieName());
        if (credentialsCookie != null) {
            return false;
        }
        return true;
    }
    
    public abstract boolean isLoggingOut(Request request, Response response);
    
    public abstract void login(Request request, Response response);
    
    
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
        //attemptRedirect(request, response);
        return STOP;
    }
    
    
    /**
     * Formats the raws credentials to store in the cookie.
     * 
     * @param challenge
     *            The challenge response to format.
     * @return The raw credentials.
     * @throws GeneralSecurityException
     */
    protected String formatCredentials(ChallengeResponse challenge)
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
     * Decodes the credentials stored in a cookie into a proper
     * {@link ChallengeResponse} object.
     * 
     * @param cookieValue
     *            The credentials to decode from cookie value.
     * @return The credentials as a proper challenge response.
     */
    protected ChallengeResponse parseCredentials(String cookieValue) {
        // 1) Decode Base64 string
        byte[] encrypted = Base64.decode(cookieValue);

        if (encrypted == null) {
            getLogger().warning(
                    "Cannot decode cookie credentials : " + cookieValue);
        }

        // 2) Decrypt the credentials
        try {
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
    
    protected Cookie getCredentialsCookie(Request request){
        return request.getCookies().getFirst(getCookieName());
    }

}
