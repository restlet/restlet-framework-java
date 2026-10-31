/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import org.restlet.Response;
import org.restlet.engine.util.SystemUtils;

/**
 * Cookie setting provided by a server. This allows a server side application to add, modify, or
 * remove a cookie on the client.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the "Set-Cookie" header (RFC 6265).
 *
 * @see Response#getCookieSettings()
 * @author Jerome Louvel
 */
public final class CookieSetting extends Cookie {

    private static final String DESCRIPTION = "Cookie setting";

    /**
     * Indicates whether to restrict cookie access to untrusted parties. This toggles the HttpOnly
     * cookie attribute.
     */
    private volatile boolean accessRestricted;

    /** The maximum age in seconds. Use 0 to discard an existing cookie. */
    private volatile int maxAge;

    /** Indicates if the cookie should only be transmitted by secure means. */
    private volatile boolean secure;

    /** The SameSite attribute, or null if not set. */
    private volatile SameSite sameSite;

    /** Default constructor. */
    public CookieSetting() {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param name The cookie's name.
     * @param value The cookie's value.
     * @param path The cookie's path.
     * @param domain The cookie's domain name.
     */
    public CookieSetting(String name, String value, String path, String domain) {
        this(name, value, path, domain, -1, false, false);
    }

    /**
     * Constructor.
     *
     * @param name The cookie's name.
     * @param value The cookie's value.
     * @param path The cookie's path.
     * @param domain The cookie's domain name.
     * @param maxAge Sets the maximum age in seconds.<br>
     *     Use 0 to immediately discard an existing cookie.<br>
     *     Use -1 to discard the cookie at the end of the session (default).
     * @param secure Indicates if the cookie should only be transmitted by secure means.
     */
    public CookieSetting(
            String name, String value, String path, String domain, int maxAge, boolean secure) {
        this(name, value, path, domain, maxAge, secure, false);
    }

    /**
     * Constructor.
     *
     * @param name The cookie's name.
     * @param value The cookie's value.
     * @param path The cookie's path.
     * @param domain The cookie's domain name.
     * @param maxAge Sets the maximum age in seconds.<br>
     *     Use 0 to immediately discard an existing cookie.<br>
     *     Use -1 to discard the cookie at the end of the session (default).
     * @param secure Indicates if the cookie should only be transmitted by secure means.
     * @param accessRestricted Indicates whether to restrict cookie access to untrusted parties.
     *     This toggles the HttpOnly cookie attribute.
     */
    public CookieSetting(
            String name,
            String value,
            String path,
            String domain,
            int maxAge,
            boolean secure,
            boolean accessRestricted) {
        super(name, value, path, domain);
        this.maxAge = maxAge;
        this.secure = secure;
        this.accessRestricted = accessRestricted;
    }

    /**
     * Preferred constructor.
     *
     * @param name The cookie's name.
     * @param value The cookie's value.
     */
    public CookieSetting(String name, String value) {
        this(name, value, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CookieSetting that)) {
            return false;
        }

        return super.equals(obj)
                && this.maxAge == that.maxAge
                && this.secure == that.secure
                && accessRestricted == that.accessRestricted
                && this.sameSite == that.sameSite;
    }

    /**
     * Returns the description of this REST element.
     *
     * @return The description of this REST element.
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Returns the maximum age in seconds. Use 0 to immediately discard an existing cookie. Use -1
     * to discard the cookie at the end of the session (default).
     *
     * @return The maximum age in seconds.
     */
    public int getMaxAge() {
        return this.maxAge;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(
                super.hashCode(), getMaxAge(), isSecure(), isAccessRestricted(), getSameSite());
    }

    /**
     * Indicates if cookie access is restricted for untrusted parties. This toggles the HttpOnly
     * cookie attribute.
     *
     * @return accessRestricted True if cookie access should be restricted
     */
    public boolean isAccessRestricted() {
        return this.accessRestricted;
    }

    /**
     * Returns the SameSite attribute (RFC 6265bis).
     *
     * @return The SameSite attribute, or null if not set.
     */
    public SameSite getSameSite() {
        return this.sameSite;
    }

    /**
     * Indicates if the cookie should only be transmitted by secure means.
     *
     * @return True if the cookie should only be transmitted by secure means.
     */
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * Indicates whether to restrict cookie access to untrusted parties. This toggles the HttpOnly
     * cookie attribute.
     *
     * @param accessRestricted True if cookie access should be restricted
     */
    public void setAccessRestricted(boolean accessRestricted) {
        this.accessRestricted = accessRestricted;
    }

    /**
     * Sets the SameSite attribute (RFC 6265bis). Note that browsers require the Secure attribute
     * when SameSite is {@link SameSite#NONE}.
     *
     * @param sameSite The SameSite attribute, or null to not send it.
     */
    public void setSameSite(SameSite sameSite) {
        this.sameSite = sameSite;
    }

    /**
     * Sets the maximum age in seconds. Use 0 to immediately discard an existing cookie. Use -1 to
     * discard the cookie at the end of the session (default).
     *
     * @param maxAge The maximum age in seconds.
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Indicates if the cookie should only be transmitted by secure means.
     *
     * @param secure True if the cookie should only be transmitted by secure means.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public String toString() {
        return "CookieSetting [accessRestricted="
                + accessRestricted
                + ", maxAge="
                + maxAge
                + ", secure="
                + secure
                + ", sameSite="
                + sameSite
                + ", domain="
                + getDomain()
                + ", name="
                + getName()
                + ", path="
                + getPath()
                + ", value="
                + getValue()
                + "]";
    }
}
