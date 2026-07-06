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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.StatusService;

class StatusFilterTestCase {

    private Response newResponse() {
        Request request = new Request(Method.GET, "http://localhost/test");
        return new Response(request);
    }

    @Test
    void constructor_withOverwritingFlag_setsFlagAndNullStatusService() {
        StatusFilter filter = new StatusFilter(new Context(), true);
        assertTrue(filter.isOverwriting());
        assertNull(filter.getStatusService());
    }

    @Test
    void constructor_withStatusService_copiesOverwritingFlag() {
        StatusService service = new StatusService();
        service.setOverwriting(true);
        StatusFilter filter = new StatusFilter(new Context(), service);
        assertTrue(filter.isOverwriting());
        assertEquals(service, filter.getStatusService());
    }

    @Test
    void setOverwriting_updatesFlag() {
        StatusFilter filter = new StatusFilter(new Context(), false);
        filter.setOverwriting(true);
        assertTrue(filter.isOverwriting());
    }

    @Test
    void setStatusService_updatesService() {
        StatusFilter filter = new StatusFilter(new Context(), false);
        StatusService service = new StatusService();
        filter.setStatusService(service);
        assertEquals(service, filter.getStatusService());
    }

    @Test
    void afterHandle_nullStatus_defaultsToSuccessOk() {
        StatusFilter filter = new StatusFilter(new Context(), new StatusService());
        Response response = newResponse();
        response.setStatus(null);

        filter.afterHandle(response.getRequest(), response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    void afterHandle_errorStatusWithoutEntity_setsRepresentationFromStatusService() {
        StatusFilter filter = new StatusFilter(new Context(), new StatusService());
        Response response = newResponse();
        response.setStatus(Status.SERVER_ERROR_INTERNAL);

        filter.afterHandle(response.getRequest(), response);

        assertNotNull(response.getEntity());
    }

    @Test
    void afterHandle_errorStatusWithEntityNotOverwriting_leavesEntityUnchanged() {
        StatusFilter filter = new StatusFilter(new Context(), false);
        filter.setStatusService(new StatusService());
        Response response = newResponse();
        response.setStatus(Status.SERVER_ERROR_INTERNAL);
        response.setEntity(new StringRepresentation("original"));

        filter.afterHandle(response.getRequest(), response);

        assertEquals("original", getText(response));
    }

    @Test
    void afterHandle_errorStatusWithEntityOverwriting_replacesEntity() {
        StatusFilter filter = new StatusFilter(new Context(), true);
        filter.setStatusService(new StatusService());
        Response response = newResponse();
        response.setStatus(Status.SERVER_ERROR_INTERNAL);
        response.setEntity(new StringRepresentation("original"));

        filter.afterHandle(response.getRequest(), response);

        assertNotEquals("original", getText(response));
    }

    @Test
    void afterHandle_successStatus_leavesEntityUnchanged() {
        StatusFilter filter = new StatusFilter(new Context(), new StatusService());
        Response response = newResponse();
        response.setStatus(Status.SUCCESS_OK);
        response.setEntity(new StringRepresentation("original"));

        filter.afterHandle(response.getRequest(), response);

        assertEquals("original", getText(response));
    }

    private static String getText(Response response) {
        try {
            return response.getEntity() == null ? null : response.getEntity().getText();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
