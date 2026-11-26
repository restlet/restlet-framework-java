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
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
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

    private static String contentRangeOfResponse(Response response) {
        Series<Header> headers = new Series<>(Header.class);
        HeaderUtils.addResponseHeaders(response, headers);
        return headers.getFirstValue("Content-Range", true);
    }

    @Test
    void addResponseHeaders_unsatisfiableRange_writesTheCompleteLengthInContentRange() {
        Response response = new Response(new Request());
        response.setStatus(Status.CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE);
        response.getAttributes().put(HeaderConstants.ATTRIBUTE_RANGE_COMPLETE_LENGTH, 1234L);

        assertEquals("bytes */1234", contentRangeOfResponse(response));
    }

    @Test
    void addResponseHeaders_unsatisfiableRangeWithoutLength_writesNoContentRange() {
        Response response = new Response(new Request());
        response.setStatus(Status.CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE);

        assertNull(contentRangeOfResponse(response));
    }

    @Test
    void addResponseHeaders_completeLengthOnAnotherStatus_isIgnored() {
        Response response = new Response(new Request());
        response.setStatus(Status.SUCCESS_OK);
        response.getAttributes().put(HeaderConstants.ATTRIBUTE_RANGE_COMPLETE_LENGTH, 1234L);

        assertNull(contentRangeOfResponse(response));
    }

    private static Series<Header> notModifiedHeaders(Representation entity) {
        Series<Header> headers = new Series<>(Header.class);
        HeaderUtils.addNotModifiedEntityHeaders(entity, headers);
        return headers;
    }

    @Test
    void addNotModifiedEntityHeaders_writesTagLocationAndExpires() {
        StringRepresentation entity = new StringRepresentation("x");
        entity.setTag(new Tag("v1", false));
        entity.setLocationRef("http://example.com/loc");
        entity.setExpirationDate(new Date(1_800_000_000_000L));

        Series<Header> headers = notModifiedHeaders(entity);

        assertEquals("\"v1\"", headers.getFirstValue(HeaderConstants.HEADER_ETAG));
        assertEquals(
                "http://example.com/loc",
                headers.getFirstValue(HeaderConstants.HEADER_CONTENT_LOCATION));
        assertEquals(
                "Fri, 15 Jan 2027 08:00:00 GMT",
                headers.getFirstValue(HeaderConstants.HEADER_EXPIRES));
    }

    @Test
    void addNotModifiedEntityHeaders_withoutExpirationDate_writesNoExpires() {
        StringRepresentation entity = new StringRepresentation("x");
        entity.setTag(new Tag("v1", false));

        assertNull(notModifiedHeaders(entity).getFirstValue(HeaderConstants.HEADER_EXPIRES));
    }

    @Test
    void addNotModifiedEntityHeaders_doesNotWriteOtherEntityHeaders() {
        StringRepresentation entity = new StringRepresentation("x");
        entity.setTag(new Tag("v1", false));
        entity.setExpirationDate(new Date(1_800_000_000_000L));
        entity.setModificationDate(new Date(1_700_000_000_000L));

        Series<Header> headers = notModifiedHeaders(entity);

        assertNull(headers.getFirstValue(HeaderConstants.HEADER_LAST_MODIFIED));
        assertNull(headers.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE));
    }

    @Test
    void addNotModifiedEntityHeaders_withoutEntity_writesNothing() {
        assertEquals(0, notModifiedHeaders(null).size());
    }
}
