/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.restlet.data.Digest.ALGORITHM_MD5;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

class HeaderUtilsTestCase {

    @Test
    void whenHeaderRetryAfterIsDecimalThenParsingIsStillFine() {
        // Given a retry_after header that contains a decimal value
        Header header = new Header(HeaderConstants.HEADER_RETRY_AFTER, "2.1");

        // Given a response
        Response response = new Response(new Request());

        // When I copy the retry_after header to the response
        HeaderUtils.copyResponseTransportHeaders(
                new Series<>(Header.class, Collections.singletonList(header)), response);

        // Then the response contains a valid value for the retry_after header
        assertNotNull(response.getRetryAfter());
    }

    @Test
    void whenHeaderRetryAfterIsAlphabeticalThenParsingFailsSilently() {
        // Given a retry_after header that contains an alphabetical value
        Header header = new Header(HeaderConstants.HEADER_RETRY_AFTER, "2.1a");

        // Given a response
        Response response = new Response(new Request());

        // When I copy the retry_after header to the response
        HeaderUtils.copyResponseTransportHeaders(
                new Series<>(Header.class, Collections.singletonList(header)), response);

        // Then the response does not contain a retry_after header
        assertNull(response.getRetryAfter());
    }

    @Test
    void testExtracting() {
        ArrayList<Header> headers = new ArrayList<>();
        String md5hash = "aaaaaaaaaaaaaaaa";
        // encodes to "YWFhYWFhYWFhYWFhYWFhYQ==", the "==" at the end is padding
        String encodedWithPadding = Base64.getEncoder().encodeToString(md5hash.getBytes());
        String encodedNoPadding = encodedWithPadding.substring(0, 22);

        Header header = new Header(HeaderConstants.HEADER_CONTENT_MD5, encodedWithPadding);
        headers.add(header);

        // extract Content-MD5 header with padded Base64 encoding, make sure it
        // decodes to the original hash
        Representation rep = HeaderUtils.extractEntityHeaders(headers, null);
        assertEquals(ALGORITHM_MD5, rep.getDigest().getAlgorithm());
        assertEquals(md5hash, new String(rep.getDigest().getValue()));

        // extract the header with UNpadded encoding, make sure it also decodes to
        //  the original hash
        header.setValue(encodedNoPadding);
        rep = HeaderUtils.extractEntityHeaders(headers, null);
        assertEquals(ALGORITHM_MD5, rep.getDigest().getAlgorithm());
        assertEquals(md5hash, new String(rep.getDigest().getValue()));
    }
}
