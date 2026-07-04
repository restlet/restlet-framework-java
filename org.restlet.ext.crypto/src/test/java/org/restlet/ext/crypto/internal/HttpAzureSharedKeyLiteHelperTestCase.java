/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

/** Unit tests for {@link HttpAzureSharedKeyLiteHelper}. */
class HttpAzureSharedKeyLiteHelperTestCase {

    @Test
    void constructor_registersAzureSharedKeyLiteScheme() {
        HttpAzureSharedKeyLiteHelper helper = new HttpAzureSharedKeyLiteHelper();
        assertEquals(ChallengeScheme.HTTP_AZURE_SHAREDKEY_LITE, helper.getChallengeScheme());
    }

    @Test
    void formatResponse_withoutDateHeader_addsFreshDateHeader() {
        HttpAzureSharedKeyLiteHelper helper = new HttpAzureSharedKeyLiteHelper();
        Request request =
                new Request(Method.GET, "http://myaccount.table.core.windows.net/mytable");
        ChallengeResponse challenge =
                new ChallengeResponse(
                        ChallengeScheme.HTTP_AZURE_SHAREDKEY_LITE,
                        "myaccount",
                        "c2VjcmV0".toCharArray());
        Series<Header> httpHeaders = new Series<>(Header.class);

        ChallengeWriter cw = new ChallengeWriter();
        helper.formatResponse(cw, challenge, request, httpHeaders);

        assertTrue(cw.toString().startsWith("myaccount:"));
        assertNotNull(httpHeaders.getFirstValue(HeaderConstants.HEADER_DATE, true));
    }

    @Test
    void formatResponse_withXmsDateHeader_usesItInsteadOfDateHeader() {
        HttpAzureSharedKeyLiteHelper helper = new HttpAzureSharedKeyLiteHelper();
        Request request =
                new Request(
                        Method.GET,
                        "http://myaccount.table.core.windows.net/mytable?comp=metadata");
        ChallengeResponse challenge =
                new ChallengeResponse(
                        ChallengeScheme.HTTP_AZURE_SHAREDKEY_LITE,
                        "myaccount",
                        "c2VjcmV0".toCharArray());
        Series<Header> httpHeaders = new Series<>(Header.class);
        httpHeaders.add("x-ms-date", "Wed, 21 Oct 2015 07:28:00 GMT");
        httpHeaders.add(HeaderConstants.HEADER_DATE, "should-be-ignored");

        ChallengeWriter cw = new ChallengeWriter();
        helper.formatResponse(cw, challenge, request, httpHeaders);

        assertTrue(cw.toString().startsWith("myaccount:"));
        assertEquals(
                "should-be-ignored", httpHeaders.getFirstValue(HeaderConstants.HEADER_DATE, true));
    }
}
