/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.util.List;
import java.util.Map;
import org.restlet.data.Cookie;

/**
 * Cookie header writer.
 *
 * @author Jerome Louvel
 */
public class CookieWriter extends HeaderWriter<Cookie> {

    /**
     * Gets the cookies whose name is a key in the given map. If a matching cookie is found, its
     * value is put on the map.
     *
     * @param source The source list of cookies.
     * @param destination The cookies map controlling the reading.
     */
    public static void getCookies(List<Cookie> source, Map<String, Cookie> destination) {
        Cookie cookie;

        for (final Cookie value : source) {
            cookie = value;

            if (destination.containsKey(cookie.getName())) {
                destination.put(cookie.getName(), cookie);
            }
        }
    }

    /**
     * Writes a cookie.
     *
     * @param cookie The cookie to format.
     * @return The formatted cookie.
     * @throws IllegalArgumentException If the Cookie contains illegal values.
     */
    public static String write(Cookie cookie) throws IllegalArgumentException {
        return new CookieWriter().append(cookie).toString();
    }

    /**
     * Writes a cookie.
     *
     * @param cookies The cookies to format.
     * @return The formatted cookie.
     */
    public static String write(List<Cookie> cookies) {
        return new CookieWriter().append(cookies).toString();
    }

    @Override
    public CookieWriter append(Cookie cookie) throws IllegalArgumentException {
        String name = cookie.getName();
        String value = cookie.getValue();
        int version = cookie.getVersion();

        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Can't write cookie. Invalid name detected");
        }

        appendValue(name, 0).append('=');

        // Append the value
        if ((value != null) && (!value.isEmpty())) {
            appendValue(value, version);
        }

        if (version > 0) {
            // Append the path
            String path = cookie.getPath();

            if ((path != null) && (!path.isEmpty())) {
                append("; $Path=");
                appendQuotedString(path);
            }

            // Append the domain
            String domain = cookie.getDomain();

            if ((domain != null) && (!domain.isEmpty())) {
                append("; $Domain=");
                appendQuotedString(domain);
            }
        }

        return this;
    }

    /**
     * Appends a list of cookies as an HTTP header.
     *
     * @param cookies The list of cookies to format.
     * @return This writer.
     */
    public CookieWriter append(List<Cookie> cookies) {
        if ((cookies != null) && !cookies.isEmpty()) {
            Cookie cookie;

            for (int i = 0; i < cookies.size(); i++) {
                cookie = cookies.get(i);

                if (i == 0) {
                    if (cookie.getVersion() > 0) {
                        append("$Version=\"").append(cookie.getVersion()).append("\"; ");
                    }
                } else {
                    append("; ");
                }

                append(cookie);
            }
        }

        return this;
    }

    /**
     * Appends a source string as an HTTP comment.
     *
     * @param value The source string to format.
     * @param version The cookie version.
     * @return This writer.
     */
    public CookieWriter appendValue(String value, int version) {
        if (version == 0) {
            append(value);
        } else {
            appendQuotedString(value);
        }

        return this;
    }
}
