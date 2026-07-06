/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.log.LogFilter;
import org.restlet.routing.Filter;
import org.restlet.routing.Template;

/** Unit tests for {@link LogService}. */
class LogServiceTestCase {

    @Test
    void defaultConstructor_isEnabledWithNullDefaults() {
        LogService service = new LogService();

        assertTrue(service.isEnabled());
        assertNull(service.getLoggableTemplate());
        assertNull(service.getLoggerName());
        assertNull(service.getResponseLogFormat());
        assertNull(service.getLogPropertiesRef());
        assertFalse(service.isIdentityCheck());
    }

    @Test
    void constructorWithFlag_setsEnabled() {
        LogService service = new LogService(false);
        assertFalse(service.isEnabled());
    }

    @Test
    void createInboundFilter_returnsLogFilter() {
        LogService service = new LogService();

        Filter filter = service.createInboundFilter(new Context());

        assertNotNull(filter);
        assertInstanceOf(LogFilter.class, filter);
    }

    @Test
    void isLoggable_withoutTemplate_returnsTrue() {
        LogService service = new LogService();
        Request request = new Request(Method.GET, "http://localhost/foo");

        assertTrue(service.isLoggable(request));
    }

    @Test
    void isLoggable_withMatchingTemplate_returnsTrue() {
        LogService service = new LogService();
        service.setLoggableTemplate("http://localhost/foo");
        Request request = new Request(Method.GET, "http://localhost/foo");

        assertTrue(service.isLoggable(request));
    }

    @Test
    void isLoggable_withNonMatchingTemplate_returnsFalse() {
        LogService service = new LogService();
        service.setLoggableTemplate("/bar");
        Request request = new Request(Method.GET, "http://localhost/foo");

        assertFalse(service.isLoggable(request));
    }

    @Test
    void setLoggableTemplate_withNullString_clearsTemplate() {
        LogService service = new LogService();
        service.setLoggableTemplate("/foo");

        service.setLoggableTemplate((String) null);

        assertNull(service.getLoggableTemplate());
    }

    @Test
    void setLoggableTemplate_withTemplateInstance_roundTrips() {
        LogService service = new LogService();
        Template template = new Template("/foo");

        service.setLoggableTemplate(template);

        assertEquals(template, service.getLoggableTemplate());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        LogService service = new LogService();

        service.setIdentityCheck(true);
        assertTrue(service.isIdentityCheck());

        service.setLoggerName("my.logger");
        assertEquals("my.logger", service.getLoggerName());

        service.setLogPropertiesRef("file:///tmp/log.properties");
        assertEquals("file:///tmp/log.properties", service.getLogPropertiesRef().toString());

        service.setResponseLogFormat("{m}");
        assertEquals("{m}", service.getResponseLogFormat());
    }

    @Test
    void getResponseLogMessage_withoutFormat_usesDefaultFormat() {
        LogService service = new LogService();
        Request request = new Request(Method.GET, "http://localhost/foo?bar=baz");
        Response response = new Response(request);
        response.setStatus(Status.SUCCESS_OK);

        String message = service.getResponseLogMessage(response, 42);

        assertNotNull(message);
        assertTrue(message.contains("GET"));
        assertTrue(message.contains("200"));
        assertTrue(message.contains("42"));
    }

    @Test
    void getResponseLogMessage_afterStartWithCustomFormat_usesTemplate() throws Exception {
        LogService service = new LogService();
        service.setResponseLogFormat("{m}");
        service.start();

        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);
        response.setStatus(Status.SUCCESS_OK);

        String message = service.getResponseLogMessage(response, 10);

        assertEquals("GET", message);
    }

    @Test
    void start_withoutLogPropertiesRef_startsNormally() throws Exception {
        LogService service = new LogService();

        service.start();

        assertTrue(service.isStarted());
    }
}
