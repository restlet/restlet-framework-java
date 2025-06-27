/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.CookieSetting;
import org.restlet.engine.util.DateUtils;

import java.util.Date;
import java.util.List;

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
		int version = cookieSetting.getVersion();

		if ((name == null) || (name.isEmpty())) {
			throw new IllegalArgumentException("Can't write cookie. Invalid name detected");
		}

		append(name).append('=');

		// Append the value
		if ((value != null) && (!value.isEmpty())) {
			appendValue(value, version);
		}

		// Append the version
		if (version > 0) {
			append("; Version=");
			appendValue(Integer.toString(version), version);
		}

		// Append the path
		String path = cookieSetting.getPath();

		if ((path != null) && (!path.isEmpty())) {
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
				appendValue(DateUtils.format(expires, DateUtils.FORMAT_RFC_1123.get(0)), version);
			} else {
				append("; Max-Age=");
				appendValue(Integer.toString(cookieSetting.getMaxAge()), version);
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

		if ((domain != null) && (!domain.isEmpty())) {
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

			if ((comment != null) && (!comment.isEmpty())) {
				append("; Comment=");
				appendValue(comment, version);
			}
		}

		return this;
	}

	/**
	 * Appends a source string as an HTTP comment.
	 * 
	 * @param value   The source string to format.
	 * @param version The cookie version.
	 * @return This writer.
	 */
	public CookieSettingWriter appendValue(String value, int version) {
		if (version == 0) {
			append(value);
		} else {
			appendQuotedString(value);
		}

		return this;
	}

}
