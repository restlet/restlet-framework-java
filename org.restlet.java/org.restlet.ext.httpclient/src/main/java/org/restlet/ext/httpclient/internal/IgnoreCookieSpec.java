/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.httpclient.internal;

import java.util.Collections;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.CookieSpecBase;

/**
 * Cookie specifications that ignore all cookies.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in next minor release in favor or Jetty extension.
 */
@Deprecated
public class IgnoreCookieSpec extends CookieSpecBase {

    /**
     * Returns an empty list.
     * 
     * @return An empty list.
     */
    public List<Header> formatCookies(List<Cookie> cookies) {
        return Collections.emptyList();
    }

    /**
     * Returns '0' as version.
     * 
     * @return '0' as version.
     */
    public int getVersion() {
        return 0;
    }

    /**
     * Returns a null version header.
     * 
     * @return A null version header.
     */
    public Header getVersionHeader() {
        return null;
    }

    /**
     * Returns an empty list.
     * 
     * @return An empty list.
     */
    public List<Cookie> parse(Header header, CookieOrigin origin)
            throws MalformedCookieException {
        return Collections.emptyList();
    }

}
