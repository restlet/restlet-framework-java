/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.service;

import java.io.IOException;
import java.util.HashMap;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.StatusService;
import org.restlet.test.RestletTestCase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Unit tests for the status service.
 * 
 * @author Jerome Louvel
 */
public class StatusServiceTestCase extends RestletTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Application application = new Application();
        Application.setCurrent(application);
    }

    protected void setUpEngine() {
        Engine.clearThreadLocalVariables();

        // Restore a clean engine
        org.restlet.engine.Engine.register(false);

        // Prefer the internal connectors
        Engine.getInstance().getRegisteredConverters()
                .add(0, new JacksonConverter());
    }

    public void testAnnotation() {
        StatusService ss = new StatusService();
        Status status = ss.toStatus(new Status400Exception("test message", 50),
                null, null);
        assertEquals(400, status.getCode());
    }

    public void testStatusSerialization() throws IOException {
        StatusService ss = new StatusService();

        Status status = new Status(400, new Status400Exception("test message",
                50));

        Request request = new Request();
        Representation representation = ss.toRepresentation(status, request,
                new Response(request));

        // verify
        Status expectedStatus = Status.CLIENT_ERROR_BAD_REQUEST;
        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());

        @SuppressWarnings("unchecked")
        HashMap<String, Object> map = (HashMap<String, Object>) new JacksonRepresentation<>(
                representation, HashMap.class).getObject();
        assertEquals(expectedStatus.getCode(), map.get("code"));
        assertEquals(expectedStatus.getDescription(), map.get("description"));
        assertEquals(expectedStatus.getReasonPhrase(), map.get("reasonPhrase"));
        assertEquals(expectedStatus.getUri(), map.get("uri"));
    }

    public void testSerializedException() throws IOException {
        Throwable exception = new Status401SerializableException(
                "test message", 50);
        Status status = new Status(400, exception);

        StatusService ss = new StatusService();

        Request request = new Request();
        Representation representation = ss.toRepresentation(status, request,
                new Response(request));

        // verify

        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());
        Status401SerializableException e = new JacksonRepresentation<>(
                representation, Status401SerializableException.class)
                .getObject();
        assertEquals(exception.getStackTrace().length, e.getStackTrace().length);
        assertEquals(50, e.getValue());
        // TODO cf issue #993
        // assertEquals("test message", e.getMessage());
        // assertEquals("test message", e.getLocalizedMessage());
        assertEquals(0, ((Throwable[]) e.getSuppressed()).length);
    }

    public void testSerializedExceptionWithCause() throws IOException {

        Throwable rootCause = new IOException(
                "File '/toto.txt' is not readable");
        Throwable exception = new Status401SerializableException(
                "test message", 50, rootCause);
        Status status = new Status(400, exception);

        StatusService ss = new StatusService();

        Request request = new Request();
        Representation representation = ss.toRepresentation(status, request,
                new Response(request));

        // verify
        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());
        Status401SerializableException e = new JacksonRepresentation<>(
                representation, Status401SerializableException.class)
                .getObject();
        assertEquals(exception.getStackTrace().length, e.getStackTrace().length);
        assertEquals(50, e.getValue());
        // TODO cf issue #993
        // assertEquals("test message", e.getMessage());
        // assertEquals("test message", e.getLocalizedMessage());
        assertEquals(0, ((Throwable[]) e.getSuppressed()).length);
        assertNotNull(e.getCause());
    }

    public void testSerializedBusinessException() throws IOException {
        Throwable exception = new Status402SerializableBusinessException(
                "test message", 50);
        Status status = new Status(400, exception);

        StatusService ss = new StatusService();

        Request request = new Request();
        Representation representation = ss.toRepresentation(status, request,
                new Response(request));

        // verify
        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());
        Status402SerializableBusinessException e = new JacksonRepresentation<>(
                representation, Status402SerializableBusinessException.class)
                .getObject();
        assertEquals(50, e.getValue());
    }

    public void testSerializedBusinessExceptionWithCause() throws IOException {

        Throwable rootCause = new IOException(
                "File '/toto.txt' is not readable");
        Throwable exception = new Status402SerializableBusinessException(
                "test message", 50, rootCause);
        Status status = new Status(400, exception);

        StatusService ss = new StatusService();

        Request request = new Request();
        Representation representation = ss.toRepresentation(status, request,
                new Response(request));

        // verify
        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());
        Status402SerializableBusinessException e = new JacksonRepresentation<>(
                representation, Status402SerializableBusinessException.class)
                .getObject();
        assertEquals(50, e.getValue());
    }

    @org.restlet.resource.Status(value = 400, serialize = false)
    private static class Status400Exception extends Throwable {

        private static final long serialVersionUID = 1L;

        private int value;

        public Status400Exception(String message, int value) {
            super(message);
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @org.restlet.resource.Status(value = 401, serialize = true)
    private static class Status401SerializableException extends Throwable {

        private static final long serialVersionUID = 1L;

        private int value;

        public Status401SerializableException() {

        }

        public Status401SerializableException(String message, int value) {
            super(message);
            this.value = value;
        }

        public Status401SerializableException(String message, int value,
                Throwable cause) {
            super(message, cause);
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @JsonIgnoreProperties({ "cause", "localizedMessage", "message",
            "stackTrace", "suppressed" })
    private static class StatusBusinessException extends Throwable {
        private StatusBusinessException(String message) {
            super(message);
        }

        private StatusBusinessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @org.restlet.resource.Status(value = 402)
    private static class Status402SerializableBusinessException extends
            StatusBusinessException {

        private static final long serialVersionUID = 1L;

        private int value;

        public Status402SerializableBusinessException() {
            super(null);
        }
        
        public Status402SerializableBusinessException(String message, int value) {
            super(message);
            this.value = value;
        }

        public Status402SerializableBusinessException(String message,
                int value, Throwable cause) {
            super(message, cause);
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
