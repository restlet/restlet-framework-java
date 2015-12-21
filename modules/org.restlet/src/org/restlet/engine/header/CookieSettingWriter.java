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

package org.restlet.engine.header;

import java.util.Date;
import java.util.List;

import org.restlet.data.CookieSetting;
import org.restlet.engine.util.DateUtils;

/**
 * Cookie setting header writer.
 * 
 * @author Jerome Louvel
 */
public class CookieSettingWriter extends HeaderWriter<CookieSetting> {

    /**
     * Writes a cookie setting.
     * 
     * @param cookieSetting
     *            The cookie setting to format.
     * @return The formatted cookie setting.
     */
    public static String write(CookieSetting cookieSetting) {
        return new CookieSettingWriter().append(cookieSetting).toString();
    }

    /**
     * Writes a list of cookie settings.
     * 
     * @param cookieSettings
     *            The cookie settings to write.
     * @return The formatted cookie setting.
     */
    public static String write(List<CookieSetting> cookieSettings) {
        return new CookieSettingWriter().append(cookieSettings).toString();
    }

    @Override
    public CookieSettingWriter append(CookieSetting cookieSetting)
            throws IllegalArgumentException {
        String name = cookieSetting.getName();
        String value = cookieSetting.getValue();
        int version = cookieSetting.getVersion();

        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                    "Can't write cookie. Invalid name detected");
        }

        append(name).append('=');

        // Append the value
        if ((value != null) && (value.length() > 0)) {
            appendValue(value, version);
        }

        // Append the version
        if (version > 0) {
            append("; Version=");
            appendValue(Integer.toString(version), version);
        }

        // Append the path
        String path = cookieSetting.getPath();

        if ((path != null) && (path.length() > 0)) {
            append("; Path=");

            if (version == 0) {
                append(path);
            } else {
                appendQuotedString(path);
            }
        }

        // Append the expiration date
        int maxAge = cookieSetting.getMaxAge();

        if (maxAge >= 0) {
            if (version == 0) {
                long currentTime = System.currentTimeMillis();
                long maxTime = (maxAge * 1000L);
                long expiresTime = currentTime + maxTime;
                Date expires = new Date(expiresTime);

                append("; Expires=");
                appendValue(
                        DateUtils.format(expires,
                                DateUtils.FORMAT_RFC_1123.get(0)), version);
            } else {
                append("; Max-Age=");
                appendValue(Integer.toString(cookieSetting.getMaxAge()),
                        version);
            }
        } else if ((maxAge == -1) && (version > 0)) {
            // Discard the cookie at the end of the user's session (RFC
            // 2965)
            append("; Discard");
        } else {
            // NetScape cookies automatically expire at the end of the
            // user's session
        }

        // Append the domain
        String domain = cookieSetting.getDomain();

        if ((domain != null) && (domain.length() > 0)) {
            append("; Domain=");
            appendValue(domain.toLowerCase(), version);
        }

        // Append the secure flag
        if (cookieSetting.isSecure()) {
            append("; Secure");
        }

        // Append the secure flag
        if (cookieSetting.isAccessRestricted()) {
            append("; HttpOnly");
        }

        // Append the comment
        if (version > 0) {
            String comment = cookieSetting.getComment();

            if ((comment != null) && (comment.length() > 0)) {
                append("; Comment=");
                appendValue(comment, version);
            }
        }

        return this;
    }

    /**
     * Appends a source string as an HTTP comment.
     * 
     * @param value
     *            The source string to format.
     * @param version
     *            The cookie version.
     * @return This writer.
     */
    public CookieSettingWriter appendValue(String value, int version) {
        if (version == 0) {
            append(value.toString());
        } else {
            appendQuotedString(value);
        }

        return this;
    }

}
