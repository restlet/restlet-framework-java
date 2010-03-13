/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.http.header;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.data.Cookie;

/**
 * Cookie manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class CookieWriter {

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
    private static Appendable appendValue(CharSequence value, int version,
            Appendable destination) throws IOException {
        if (version == 0) {
            destination.append(value.toString());
        } else {
            HeaderUtils.appendQuotedString(value, destination);
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
    public static String write(Cookie cookie) throws IllegalArgumentException {
        final StringBuilder sb = new StringBuilder();
        try {
            append(cookie, sb);
        } catch (IOException e) {
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
    public static void append(Cookie cookie, Appendable destination)
            throws IllegalArgumentException, IOException {
        final String name = cookie.getName();
        final String value = cookie.getValue();
        final int version = cookie.getVersion();

        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                    "Can't write cookie. Invalid name detected");
        }

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
                HeaderUtils.appendQuotedString(path, destination);
            }
            // Append the domain
            final String domain = cookie.getDomain();
            if ((domain != null) && (domain.length() > 0)) {
                destination.append("; $Domain=");
                HeaderUtils.appendQuotedString(domain, destination);
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
    public static String write(List<Cookie> cookies)
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
                append(cookie, sb);
            } catch (IOException e) {
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

    /**
     * Parses the given String to a Cookie
     * 
     * @param cookie
     * @return the Cookie parsed from the String
     * @throws IllegalArgumentException
     *             Thrown if the String can not be parsed as Cookie.
     */
    public static Cookie write(String cookie) throws IllegalArgumentException {
        CookieReader cr = new CookieReader(cookie);

        try {
            return cr.readValue();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the cookie", e);
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private CookieWriter() {
    }

}
