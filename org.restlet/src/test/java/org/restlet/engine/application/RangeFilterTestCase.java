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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

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

    @Test
    void getRangeService_returnsApplicationRangeService() {
        assertEquals(application.getRangeService(), filter.getRangeService());
    }
}
