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
import java.util.Date;

import org.restlet.data.CookieSetting;
import org.restlet.engine.util.DateUtils;

/**
 * Cookie manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class CookieSettingWriter {

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
        } catch (IOException e) {
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
            Appendable destination) throws IOException,
            IllegalArgumentException {
        final String name = cookieSetting.getName();
        final String value = cookieSetting.getValue();
        final int version = cookieSetting.getVersion();

        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                    "Can't write cookie. Invalid name detected");
        }

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
                HeaderUtils.appendQuotedString(path, destination);
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
                appendValue(DateUtils.format(expires, DateUtils.FORMAT_RFC_1036
                        .get(0)), version, destination);
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

    /**
     * Parses the given String to a CookieSetting
     * 
     * @param cookieSetting
     * @return the CookieSetting parsed from the String
     * @throws IllegalArgumentException
     *             Thrown if the String can not be parsed as CookieSetting.
     */
    public static CookieSetting parseSetting(String cookieSetting)
            throws IllegalArgumentException {
        CookieSettingReader cr = new CookieSettingReader(cookieSetting);

        try {
            return cr.readValue();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read the cookie setting", e);
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private CookieSettingWriter() {
    }

}
