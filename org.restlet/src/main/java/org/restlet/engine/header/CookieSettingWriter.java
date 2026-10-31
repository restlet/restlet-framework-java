/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.util.Date;
import java.util.List;
import org.restlet.data.CookieSetting;
import org.restlet.data.SameSite;
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
     * @param cookieSetting The cookie setting to format.
     * @return The formatted cookie setting.
     */
    public static String write(CookieSetting cookieSetting) {
        return new CookieSettingWriter().append(cookieSetting).toString();
    }

    /**
     * Writes a list of cookie settings.
     *
     * @param cookieSettings The cookie settings to write.
     * @return The formatted cookie setting.
     */
    public static String write(List<CookieSetting> cookieSettings) {
        return new CookieSettingWriter().append(cookieSettings).toString();
    }

    @Override
    public CookieSettingWriter append(CookieSetting cookieSetting) throws IllegalArgumentException {
        String name = cookieSetting.getName();
        String value = cookieSetting.getValue();

        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Can't write cookie. Invalid name detected");
        }

        append(name).append('=');

        if ((value != null) && (!value.isEmpty())) {
            append(value);
        }

        // RFC 6265 section 5.2.3 and 5.2.4: attribute values are never quoted
        String path = cookieSetting.getPath();

        if ((path != null) && (!path.isEmpty())) {
            append("; Path=").append(path);
        }

        // Max-Age is preferred by recent user agents, Expires is still needed by old ones
        int maxAge = cookieSetting.getMaxAge();

        if (maxAge >= 0) {
            Date expires = new Date(System.currentTimeMillis() + (maxAge * 1000L));
            append("; Expires=")
                    .append(DateUtils.format(expires, DateUtils.FORMAT_RFC_1123.getFirst()));
            append("; Max-Age=").append(Integer.toString(maxAge));
        }

        String domain = cookieSetting.getDomain();

        if ((domain != null) && (!domain.isEmpty())) {
            append("; Domain=").append(domain.toLowerCase());
        }

        if (cookieSetting.isSecure()) {
            append("; Secure");
        }

        if (cookieSetting.isAccessRestricted()) {
            append("; HttpOnly");
        }

        SameSite sameSite = cookieSetting.getSameSite();

        if (sameSite != null) {
            append("; SameSite=").append(sameSite.getHeaderValue());
        }

        return this;
    }
}
