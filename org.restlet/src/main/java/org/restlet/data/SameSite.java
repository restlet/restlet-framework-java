/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

/**
 * Value of the SameSite cookie attribute (RFC 6265bis), which controls whether a cookie is sent
 * with cross-site requests.
 *
 * @see CookieSetting#setSameSite(SameSite)
 */
public enum SameSite {

    /** The cookie is only sent with same-site requests. */
    STRICT("Strict"),

    /** The cookie is also sent when navigating to the site from another one (browser default). */
    LAX("Lax"),

    /** The cookie is sent with all requests. Requires the Secure attribute. */
    NONE("None");

    private final String headerValue;

    SameSite(String headerValue) {
        this.headerValue = headerValue;
    }

    /**
     * Returns the value as written in the Set-Cookie header.
     *
     * @return The header value.
     */
    public String getHeaderValue() {
        return headerValue;
    }

    /**
     * Parses a header value, ignoring case.
     *
     * @param value The header value.
     * @return The matching constant, or null if the value is not recognized.
     */
    public static SameSite parse(String value) {
        if (value != null) {
            for (SameSite sameSite : values()) {
                if (sameSite.headerValue.equalsIgnoreCase(value.trim())) {
                    return sameSite;
                }
            }
        }

        return null;
    }
}
