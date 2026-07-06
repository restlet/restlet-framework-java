/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

class ReferenceUtilsTestCase {

    @Test
    void update_withoutChallengeResponse_returnsAbsoluteReferenceUnchanged() {
        Request request = new Request(Method.GET, "https://example.com/path");
        Reference result = ReferenceUtils.update(request.getResourceRef(), request);
        assertEquals("https://example.com/path", result.toString());
    }

    @Test
    void format_notProxied_returnsPathAndQuery() {
        Request request = new Request(Method.GET, "https://example.com/path?a=b");
        String result = ReferenceUtils.format(request.getResourceRef(), false, request);
        assertEquals("/path?a=b", result);
    }

    @Test
    void format_notProxiedWithoutQuery_returnsPathOnly() {
        Request request = new Request(Method.GET, "https://example.com/path");
        String result = ReferenceUtils.format(request.getResourceRef(), false, request);
        assertEquals("/path", result);
    }

    @Test
    void format_notProxiedWithEmptyPath_returnsRootSlash() {
        Request request = new Request(Method.GET, "https://example.com");
        String result = ReferenceUtils.format(request.getResourceRef(), false, request);
        assertEquals("/", result);
    }

    @Test
    void format_proxied_returnsFullIdentifier() {
        Request request = new Request(Method.GET, "https://example.com/path?a=b");
        String result = ReferenceUtils.format(request.getResourceRef(), true, request);
        assertEquals("https://example.com/path?a=b", result);
    }

    @Test
    void getOriginalRef_withNullHeaders_returnsTargetRef() {
        Reference resourceRef = new Reference("https://example.com/path");
        Reference result = ReferenceUtils.getOriginalRef(resourceRef, null);
        assertEquals("https://example.com/path", result.toString());
    }

    @Test
    void getOriginalRef_withForwardedHeaders_updatesPortAndScheme() {
        Reference resourceRef = new Reference("https://example.com/path");
        Series<Header> headers = new Series<>(Header.class);
        headers.add(HeaderConstants.HEADER_X_FORWARDED_PORT, "8443");
        headers.add(HeaderConstants.HEADER_X_FORWARDED_PROTO, "https");

        Reference result = ReferenceUtils.getOriginalRef(resourceRef, headers);

        assertEquals(8443, result.getHostPort());
        assertEquals("https", result.getScheme());
    }

    @Test
    void getOriginalRef_withoutForwardedHeaders_leavesReferenceUnchanged() {
        Reference resourceRef = new Reference("https://example.com/path");
        Series<Header> headers = new Series<>(Header.class);

        Reference result = ReferenceUtils.getOriginalRef(resourceRef, headers);

        assertEquals("https", result.getScheme());
    }
}
