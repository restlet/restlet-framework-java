/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt.internal.http;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.gwt.data.Cookie;
import org.restlet.gwt.data.CookieSetting;
import org.restlet.gwt.util.DateUtils;

/**
 * Cookie manipulation utilities.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class CookieUtils {
    /**
     * Appends a source string as an HTTP comment.
     * 
     * @param value
     *            The source string to format.
     * @param version
     *            The cookie version.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     */
    private static StringBuilder appendValue(CharSequence value, int version,
            StringBuilder destination) throws Exception {
        if (version == 0) {
            destination.append(value.toString());
        } else {
            HttpUtils.appendQuote(value, destination);
        }

        return destination;
    }

    /**
     * Formats a cookie.
     * 
     * @param cookie
     *            The cookie to format.
     * @return The formatted cookie.
     * @throws IllegalArgumentException
     *             If the Cookie contains illegal values.
     */
    public static String format(Cookie cookie) throws IllegalArgumentException {
        final StringBuilder sb = new StringBuilder();
        try {
            format(cookie, sb);
        } catch (final Exception e) {
            // IOExceptions are not possible on StringBuilders
        }
        return sb.toString();
    }

    /**
     * Formats a cookie setting.
     * 
     * @param cookie
     *            The cookie to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     * @throws IllegalArgumentException
     *             If the Cookie contains illegal values.
     */
    public static void format(Cookie cookie, StringBuilder destination)
            throws IllegalArgumentException, Exception {
        final String name = cookie.getName();
        final String value = cookie.getValue();
        final int version = cookie.getVersion();

        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                    "Can't write cookie. Invalid name detected");
        } else {
            appendValue(name, 0, destination).append('=');

            // Append the value
            if ((value != null) && (value.length() > 0)) {
                appendValue(value, version, destination);
            }
            if (version > 0) {
                // Append the path
                final String path = cookie.getPath();
                if ((path != null) && (path.length() > 0)) {
                    destination.append("; $Path=");
                    HttpUtils.appendQuote(path, destination);
                }
                // Append the domain
                final String domain = cookie.getDomain();
                if ((domain != null) && (domain.length() > 0)) {
                    destination.append("; $Domain=");
                    HttpUtils.appendQuote(domain, destination);
                }
            }
        }
    }

    /**
     * Formats a cookie setting.
     * 
     * @param cookieSetting
     *            The cookie setting to format.
     * @return The formatted cookie setting.
     * @throws IllegalArgumentException
     *             If the CookieSetting can not be formatted to a String
     */
    public static String format(CookieSetting cookieSetting)
            throws IllegalArgumentException {
        final StringBuilder sb = new StringBuilder();

        try {
            format(cookieSetting, sb);
        } catch (final Exception e) {
            // log error
        }

        return sb.toString();
    }

    /**
     * Formats a cookie setting.
     * 
     * @param cookieSetting
     *            The cookie setting to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     * @throws IllegalArgumentException
     *             If the CookieSetting can not be formatted to a String
     */
    public static void format(CookieSetting cookieSetting,
            StringBuilder destination) throws Exception,
            IllegalArgumentException {
        final String name = cookieSetting.getName();
        final String value = cookieSetting.getValue();
        final int version = cookieSetting.getVersion();

        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                    "Can't write cookie. Invalid name detected");
        } else {
            destination.append(name).append('=');

            // Append the value
            if ((value != null) && (value.length() > 0)) {
                appendValue(value, version, destination);
            }

            // Append the version
            if (version > 0) {
                destination.append("; Version=");
                appendValue(Integer.toString(version), version, destination);
            }

            // Append the path
            final String path = cookieSetting.getPath();
            if ((path != null) && (path.length() > 0)) {
                destination.append("; Path=");

                if (version == 0) {
                    destination.append(path);
                } else {
                    HttpUtils.appendQuote(path, destination);
                }
            }

            // Append the expiration date
            final int maxAge = cookieSetting.getMaxAge();
            if (maxAge >= 0) {
                if (version == 0) {
                    final long currentTime = System.currentTimeMillis();
                    final long maxTime = (maxAge * 1000L);
                    final long expiresTime = currentTime + maxTime;
                    final Date expires = new Date(expiresTime);
                    destination.append("; Expires=");
                    appendValue(DateUtils.format(expires,
                            DateUtils.FORMAT_RFC_1036.get(0)), version,
                            destination);
                } else {
                    destination.append("; Max-Age=");
                    appendValue(Integer.toString(cookieSetting.getMaxAge()),
                            version, destination);
                }
            } else if ((maxAge == -1) && (version > 0)) {
                // Discard the cookie at the end of the user's session (RFC
                // 2965)
                destination.append("; Discard");
            } else {
                // Netscape cookies automatically expire at the end of the
                // user's session
            }

            // Append the domain
            final String domain = cookieSetting.getDomain();
            if ((domain != null) && (domain.length() > 0)) {
                destination.append("; Domain=");
                appendValue(domain.toLowerCase(), version, destination);
            }

            // Append the secure flag
            if (cookieSetting.isSecure()) {
                destination.append("; Secure");
            }

            // Append the secure flag
            if (cookieSetting.isAccessRestricted()) {
                destination.append("; HttpOnly");
            }

            // Append the comment
            if (version > 0) {
                final String comment = cookieSetting.getComment();
                if ((comment != null) && (comment.length() > 0)) {
                    destination.append("; Comment=");
                    appendValue(comment, version, destination);
                }
            }
        }
    }

    /**
     * Formats a list of cookies as an HTTP header.
     * 
     * @param cookies
     *            The list of cookies to format.
     * @return The HTTP header.
     * @throws IllegalArgumentException
     *             If one of the Cookies contains illegal values
     */
    public static String format(List<Cookie> cookies)
            throws IllegalArgumentException {
        final StringBuilder sb = new StringBuilder();

        Cookie cookie;
        for (int i = 0; i < cookies.size(); i++) {
            cookie = cookies.get(i);

            if (i == 0) {
                if (cookie.getVersion() > 0) {
                    sb.append("$Version=\"").append(cookie.getVersion())
                            .append("\"; ");
                }
            } else {
                sb.append("; ");
            }

            try {
                format(cookie, sb);
            } catch (final Exception e) {
                // IOExceptiosn are not possible on StringBuilder
            }
        }

        return sb.toString();
    }

    /**
     * Gets the cookies whose name is a key in the given map. If a matching
     * cookie is found, its value is put in the map.
     * 
     * @param source
     *            The source list of cookies.
     * @param destination
     *            The cookies map controlling the reading.
     */
    public static void getCookies(List<Cookie> source,
            Map<String, Cookie> destination) {
        Cookie cookie;

        for (final Iterator<Cookie> iter = source.iterator(); iter.hasNext();) {
            cookie = iter.next();

            if (destination.containsKey(cookie.getName())) {
                destination.put(cookie.getName(), cookie);
            }
        }
    }
}
