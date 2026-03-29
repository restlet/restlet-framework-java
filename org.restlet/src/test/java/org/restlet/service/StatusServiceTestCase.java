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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Serial;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.application.StatusInfo;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;

/**
 * Unit tests for the status service.
 *
 * @author Jerome Louvel
 */
@SuppressWarnings("unchecked")
class StatusServiceTestCase {

    StatusService statusService = new StatusService();

    @BeforeEach
    void setUp() {
        // Restore a clean engine
        Engine.clearThreadLocalVariables();
        Engine.register(true);
        ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = true;
        Application.setCurrent(new Application());
    }

    @AfterEach
    void cleanUp() {
        ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = false;
    }

    @Test
    void shouldConvertToStatus() {
        AnnotatedNotSerializableException statusException =
                new AnnotatedNotSerializableException("test message", 50);

        Status status = statusService.toStatus(statusException, null, null);

        assertEquals(400, status.getCode());
        assertEquals(statusException, status.getThrowable());
    }

    @Test
    void exceptionShouldNotBeSerialized() throws IOException {
        AnnotatedNotSerializableException statusException =
                new AnnotatedNotSerializableException("test message", 50);
        Status status = new Status(400, statusException);

        Representation representation = statusServiceToRepresentation(status);

        // verify
        assertEquals(MediaType.APPLICATION_JAVA_OBJECT, representation.getMediaType());

        Status expectedStatus = Status.CLIENT_ERROR_BAD_REQUEST;

        StatusInfo statusInfo = (StatusInfo) ((ObjectRepresentation<?>) representation).getObject();
        assertEquals(expectedStatus.getCode(), statusInfo.getCode());
        assertEquals(expectedStatus.getDescription(), statusInfo.getDescription());
        assertEquals(expectedStatus.getReasonPhrase(), statusInfo.getReasonPhrase());
        assertEquals(expectedStatus.getUri(), statusInfo.getUri());
    }

    @Test
    void shouldSerializeAnnotatedException() throws IOException {
        Status status =
                new Status(400, AnnotatedSerializableException.withoutCause("test message", 50));

        Representation representation = statusServiceToRepresentation(status);

        assertEquals(MediaType.APPLICATION_JAVA_OBJECT, representation.getMediaType());
        AnnotatedSerializableException throwable =
                ((ObjectRepresentation<AnnotatedSerializableException>) representation).getObject();
        assertEquals(50, throwable.value);
        assertEquals("test message", throwable.getMessage());
        assertNull(throwable.getCause());
    }

    @Test
    void shouldSerializeAnnotatedExceptionWithCause() throws IOException {
        Status status =
                new Status(400, AnnotatedSerializableException.withCause("test message", 50));

        Representation representation = statusServiceToRepresentation(status);

        assertEquals(MediaType.APPLICATION_JAVA_OBJECT, representation.getMediaType());
        AnnotatedSerializableException throwable =
                ((ObjectRepresentation<AnnotatedSerializableException>) representation).getObject();
        assertEquals(50, throwable.value);
        assertEquals("test message", throwable.getMessage());
        assertEquals(0, throwable.getStackTrace().length);
        assertNotNull(throwable.getCause());
    }

    @Test
    void shouldSerializeAnnotatedExceptionWithStackTrace() throws IOException {
        Status status =
                new Status(400, AnnotatedSerializableException.withCause("test message", 50));

        Request request = new Request();
        Response response = new Response(request);

        Application application = new Application();
        application.setDebugging(true);
        Application.setCurrent(application);

        Representation representation = statusService.toRepresentation(status, request, response);

        assertEquals(MediaType.APPLICATION_JAVA_OBJECT, representation.getMediaType());
        AnnotatedSerializableException throwable =
                ((ObjectRepresentation<AnnotatedSerializableException>) representation).getObject();
        assertEquals(50, throwable.value);
        assertEquals("test message", throwable.getMessage());
        assertEquals(1, throwable.getStackTrace().length);
        assertNotNull(throwable.getCause());
    }

    private Representation statusServiceToRepresentation(Status status) {
        Request request = new Request();
        Response response = new Response(request);
        return statusService.toRepresentation(status, request, response);
    }

    @org.restlet.resource.Status(value = 400, serialize = false)
    private static class AnnotatedNotSerializableException extends Throwable {

        @Serial private static final long serialVersionUID = 1L;

        private final int value;

        public AnnotatedNotSerializableException(String message, int value) {
            super(message);
            this.value = value;
        }

        @SuppressWarnings("unused")
        public int getValue() {
            return value;
        }
    }

    @org.restlet.resource.Status(value = 401)
    private static class AnnotatedSerializableException extends Throwable {

        @Serial private static final long serialVersionUID = 1L;

        private int value;

        public static AnnotatedSerializableException withoutCause(
                final String message, final int value) {
            StackTraceElement[] stackTrace = new StackTraceElement[1];
            stackTrace[0] = new StackTraceElement("DeclaringClass", "MethodName", "FileName", 1);

            AnnotatedSerializableException annotatedSerializableException =
                    new AnnotatedSerializableException(message, value);
            annotatedSerializableException.setStackTrace(stackTrace);
            return annotatedSerializableException;
        }

        public static AnnotatedSerializableException withCause(
                final String message, final int value) {
            StackTraceElement[] stackTrace = new StackTraceElement[1];
            stackTrace[0] = new StackTraceElement("DeclaringClass", "MethodName", "FileName", 1);

            Throwable rootCause = new IOException("File '/toto.txt' is not readable");
            rootCause.setStackTrace(stackTrace);

            AnnotatedSerializableException annotatedSerializableException =
                    new AnnotatedSerializableException(message, value, rootCause);
            annotatedSerializableException.setStackTrace(stackTrace);
            return annotatedSerializableException;
        }

        @SuppressWarnings("unused")
        public AnnotatedSerializableException() {}

        public AnnotatedSerializableException(String message, int value) {
            super(message);
            this.value = value;
        }

        public AnnotatedSerializableException(String message, int value, Throwable cause) {
            super(message, cause);
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
