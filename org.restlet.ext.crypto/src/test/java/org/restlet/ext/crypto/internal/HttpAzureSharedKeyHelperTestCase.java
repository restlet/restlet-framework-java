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

/** Unit tests for {@link HttpAzureSharedKeyHelper}. */
class HttpAzureSharedKeyHelperTestCase {

    @Test
    void constructor_registersAzureSharedKeyScheme() {
        HttpAzureSharedKeyHelper helper = new HttpAzureSharedKeyHelper();
        assertEquals(ChallengeScheme.HTTP_AZURE_SHAREDKEY, helper.getChallengeScheme());
    }

    @Test
    void formatResponse_withoutOptionalHeaders_addsDateHeaderAndFormatsCredential() {
        HttpAzureSharedKeyHelper helper = new HttpAzureSharedKeyHelper();
        Request request =
                new Request(Method.GET, "http://myaccount.blob.core.windows.net/container");
        ChallengeResponse challenge =
                new ChallengeResponse(
                        ChallengeScheme.HTTP_AZURE_SHAREDKEY,
                        "myaccount",
                        "c2VjcmV0".toCharArray());
        Series<Header> httpHeaders = new Series<>(Header.class);

        ChallengeWriter cw = new ChallengeWriter();
        helper.formatResponse(cw, challenge, request, httpHeaders);

        String result = cw.toString();
        assertTrue(result.startsWith("myaccount:"));
        assertNotNull(httpHeaders.getFirstValue(HeaderConstants.HEADER_DATE, true));
    }

    @Test
    void formatResponse_withHeadersAndCompQuery_isDeterministic() {
        HttpAzureSharedKeyHelper helper = new HttpAzureSharedKeyHelper();
        Request request =
                new Request(
                        Method.PUT,
                        "http://myaccount.blob.core.windows.net/container/blob?comp=metadata");
        ChallengeResponse challenge =
                new ChallengeResponse(
                        ChallengeScheme.HTTP_AZURE_SHAREDKEY,
                        "myaccount",
                        "c2VjcmV0".toCharArray());

        Series<Header> httpHeaders1 = new Series<>(Header.class);
        httpHeaders1.add(HeaderConstants.HEADER_CONTENT_TYPE, "application/octet-stream");
        httpHeaders1.add(HeaderConstants.HEADER_CONTENT_MD5, "abc123");
        httpHeaders1.add(HeaderConstants.HEADER_DATE, "Wed, 21 Oct 2015 07:28:00 GMT");
        httpHeaders1.add("x-ms-version", "2020-02-10");
        httpHeaders1.add("x-ms-blob-type", "BlockBlob");

        Series<Header> httpHeaders2 = new Series<>(Header.class);
        httpHeaders2.add(HeaderConstants.HEADER_CONTENT_TYPE, "application/octet-stream");
        httpHeaders2.add(HeaderConstants.HEADER_CONTENT_MD5, "abc123");
        httpHeaders2.add(HeaderConstants.HEADER_DATE, "Wed, 21 Oct 2015 07:28:00 GMT");
        httpHeaders2.add("x-ms-version", "2020-02-10");
        httpHeaders2.add("x-ms-blob-type", "BlockBlob");

        ChallengeWriter cw1 = new ChallengeWriter();
        helper.formatResponse(cw1, challenge, request, httpHeaders1);

        ChallengeWriter cw2 = new ChallengeWriter();
        helper.formatResponse(cw2, challenge, request, httpHeaders2);

        assertEquals(cw1.toString(), cw2.toString());
    }
}
