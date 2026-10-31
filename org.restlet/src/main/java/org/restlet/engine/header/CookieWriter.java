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

        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Can't write cookie. Invalid name detected");
        }

        append(name).append('=');

        if ((value != null) && (!value.isEmpty())) {
            append(value);
        }

        return this;
    }

    /**
     * Appends a list of cookies as an HTTP header, separated by semicolons (RFC 6265 section 5.4).
     *
     * @param cookies The list of cookies to format.
     * @return This writer.
     */
    public CookieWriter append(List<Cookie> cookies) {
        if ((cookies != null) && !cookies.isEmpty()) {
            for (int i = 0; i < cookies.size(); i++) {
                if (i > 0) {
                    append("; ");
                }

                append(cookies.get(i));
            }
        }

        return this;
    }
}
