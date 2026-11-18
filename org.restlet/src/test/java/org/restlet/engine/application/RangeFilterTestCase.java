/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

class RangeFilterTestCase {

    private Application application;

    private RangeFilter filter;

    @BeforeEach
    void setUpEach() {
        application = new Application(new Context());
        Application.setCurrent(application);
        filter = new RangeFilter(application.getContext());
    }

    @AfterEach
    void tearDownEach() {
        Application.setCurrent(null);
    }

    private Response successResponse(Method method, Representation entity) {
        Request request = new Request(method, "http://localhost/test");
        Response response = new Response(request);
        response.setStatus(Status.SUCCESS_OK);
        response.setEntity(entity);
        return response;
    }

    @Test
    void afterHandle_rangeServiceDisabled_marksServerInfoNotAcceptingRanges() {
        application.getRangeService().setEnabled(false);
        Response response = successResponse(Method.GET, new StringRepresentation("body"));

        filter.afterHandle(response.getRequest(), response);

        assertFalse(response.getServerInfo().isAcceptingRanges());
    }

    @Test
    void afterHandle_rangeServiceEnabled_marksServerInfoAcceptingRanges() {
        application.getRangeService().setEnabled(true);
        Response response = successResponse(Method.GET, new StringRepresentation("body"));

        filter.afterHandle(response.getRequest(), response);

        assertTrue(response.getServerInfo().isAcceptingRanges());
    }

    @Test
    void afterHandle_unsafeMethod_leavesEntityUnchanged() {
        Response response = successResponse(Method.POST, new StringRepresentation("body"));
        Representation original = response.getEntity();

        filter.afterHandle(response.getRequest(), response);

        assertEquals(original, response.getEntity());
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    void afterHandle_nonSuccessStatus_leavesStatusUnchanged() {
        Response response = successResponse(Method.GET, new StringRepresentation("body"));
        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    @Test
    void afterHandle_multipleRequestedRanges_setsNotImplemented() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(0, 2));
        response.getRequest().getRanges().add(new Range(3, 2));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SERVER_ERROR_NOT_IMPLEMENTED, response.getStatus());
    }

    @Test
    void afterHandle_noRequestedRange_leavesResponseUnchanged() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    void afterHandle_singleMatchingRange_wrapsEntityAsPartialContent() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        Range requested = new Range(0, 5);
        response.getRequest().getRanges().add(requested);

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
    }

    private static String contentRangeOf(Representation entity) {
        Series<Header> headers = new Series<>(Header.class);
        HeaderUtils.addEntityHeaders(entity, headers);
        return headers.getFirstValue("Content-Range", true);
    }

    @Test
    void afterHandle_suffixRange_writesContentRangeFromTheEnd() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(Range.INDEX_LAST, 4));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 6-9/10", contentRangeOf(response.getEntity()));
    }

    @Test
    void afterHandle_lastPositionPastTheEnd_isClampedToTheEntity() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(5, 96));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 5-9/10", contentRangeOf(response.getEntity()));
    }

    @Test
    void afterHandle_rangeEndingPastTheEntityWithSmallSize_isClampedToTheEntity() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        // "bytes=8-12": the size (5) is lower than the entity length, but the end is past it
        response.getRequest().getRanges().add(new Range(8, 5));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 8-9/10", contentRangeOf(response.getEntity()));
    }

    @Test
    void afterHandle_servedRangeIsClamped_requestedRangeIsLeftUnchanged() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        Range requested = new Range(Range.INDEX_LAST, 500);
        response.getRequest().getRanges().add(requested);

        filter.afterHandle(response.getRequest(), response);

        assertEquals(new Range(Range.INDEX_LAST, 500), requested);
    }

    /** RFC 9110 section 14.1.2: a suffix longer than the representation selects all of it. */
    @Test
    void afterHandle_suffixLongerThanEntity_servesTheWholeEntity() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(Range.INDEX_LAST, 500));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 0-9/10", contentRangeOf(response.getEntity()));
    }

    private void assertNotSatisfiable(Response response, long completeLength) {
        assertEquals(Status.CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatus());
        assertNull(response.getEntity());
        assertEquals(
                completeLength,
                response.getAttributes().get(HeaderConstants.ATTRIBUTE_RANGE_COMPLETE_LENGTH));
    }

    @Test
    void afterHandle_firstPositionAtTheEnd_isNotSatisfiable() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(10, Range.SIZE_MAX));

        filter.afterHandle(response.getRequest(), response);

        assertNotSatisfiable(response, 10);
    }

    @Test
    void afterHandle_firstPositionPastTheEnd_isNotSatisfiable() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(50, 5));

        filter.afterHandle(response.getRequest(), response);

        assertNotSatisfiable(response, 10);
    }

    @Test
    void afterHandle_firstPositionOnTheLastByte_isSatisfiable() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(9, Range.SIZE_MAX));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 9-9/10", contentRangeOf(response.getEntity()));
    }

    @Test
    void afterHandle_zeroLengthSuffix_isNotSatisfiable() {
        Response response = successResponse(Method.GET, new StringRepresentation("0123456789"));
        response.getRequest().getRanges().add(new Range(Range.INDEX_LAST, 0));

        filter.afterHandle(response.getRequest(), response);

        assertNotSatisfiable(response, 10);
    }

    /**
     * An empty entity is not "available", so the filter leaves it alone: the Range header is
     * ignored, which RFC 9110 section 14.2 allows ("A server MAY ignore the Range header field").
     */
    @Test
    void afterHandle_emptyEntity_ignoresTheRange() {
        Response response = successResponse(Method.GET, new StringRepresentation(""));
        response.getRequest().getRanges().add(new Range(0, 5));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    void afterHandle_unsatisfiableRangeWithFailingIfRange_servesTheWholeEntity() {
        StringRepresentation entity = new StringRepresentation("0123456789");
        entity.setTag(new Tag("current"));
        Response response = successResponse(Method.GET, entity);
        response.getRequest().getRanges().add(new Range(50, 5));
        response.getRequest().getConditions().setRangeTag(new Tag("stale"));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    void afterHandle_unsatisfiableRangeOnEntityOfUnknownSize_isNotRejected() {
        Representation entity =
                new OutputRepresentation(null) {
                    @Override
                    public void write(java.io.OutputStream outputStream)
                            throws java.io.IOException {
                        outputStream.write("0123456789".getBytes());
                    }
                };
        Response response = successResponse(Method.GET, entity);
        response.getRequest().getRanges().add(new Range(50, 5));

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_PARTIAL_CONTENT, response.getStatus());
    }

    @Test
    void getRangeService_returnsApplicationRangeService() {
        assertEquals(application.getRangeService(), filter.getRangeService());
    }
}
