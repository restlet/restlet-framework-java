/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit tests for the {@link ClientResource} class.
 *
 * @author Jerome Louvel
 */
class ClientResourceTestCase {

    @BeforeEach
    void setUpEach() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().registerDefaultConverters();
    }

    @AfterEach
    void tearDownEach() {
        Engine.clearThreadLocalVariables();
    }

    @Test
    void constructorWithUri_setsReferenceAndDefaultGetMethod() {
        ClientResource cr = new ClientResource("http://localhost/test");

        assertEquals(new Reference("http://localhost/test"), cr.getReference());
        assertEquals(Method.GET, cr.getMethod());
    }

    @Test
    void constructorWithMethodAndUri_setsMethod() {
        ClientResource cr = new ClientResource(Method.POST, "http://localhost/test");

        assertEquals(Method.POST, cr.getMethod());
    }

    @Test
    void defaultSettings_matchDocumentedDefaults() {
        ClientResource cr = new ClientResource("http://localhost/test");

        assertEquals(10, cr.getMaxRedirects());
        assertTrue(cr.isFollowingRedirects());
        assertTrue(cr.isRetryOnError());
        assertEquals(2000L, cr.getRetryDelay());
        assertEquals(2, cr.getRetryAttempts());
        assertFalse(cr.isRequestEntityBuffering());
        assertFalse(cr.isResponseEntityBuffering());
    }

    @Test
    void copyConstructor_copiesConfiguration() {
        ClientResource original = new ClientResource("http://localhost/test");
        original.setMaxRedirects(5);
        original.setRetryAttempts(1);

        ClientResource copy = new ClientResource(original);

        assertEquals(5, copy.getMaxRedirects());
        assertEquals(1, copy.getRetryAttempts());
    }

    @Test
    void setNextThenGetNext_roundTrips() {
        ClientResource cr = new ClientResource("http://localhost/test");
        Restlet next =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setStatus(Status.SUCCESS_OK);
                    }
                };

        cr.setNext(next);

        assertEquals(next, cr.getNext());
        assertTrue(cr.hasNext());
    }

    @Test
    void get_returnsEntityFromNextRestlet() {
        ClientResource cr = new ClientResource("http://localhost/test");
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setStatus(Status.SUCCESS_OK);
                        response.setEntity(new StringRepresentation("hello"));
                    }
                });

        Representation result = cr.get();

        assertEquals("hello", getText(result));
    }

    @Test
    void put_sendsPutMethodToNextRestlet() {
        ClientResource cr = new ClientResource("http://localhost/test");
        final AtomicInteger methodSeen = new AtomicInteger();
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        if (Method.PUT.equals(request.getMethod())) {
                            methodSeen.incrementAndGet();
                        }
                        response.setStatus(Status.SUCCESS_OK);
                        response.setEntity(new StringRepresentation("put-ack"));
                    }
                });

        Representation result = cr.put(new StringRepresentation("payload"));

        assertEquals(1, methodSeen.get());
        assertEquals("put-ack", getText(result));
    }

    @Test
    void post_sendsPostMethodToNextRestlet() {
        ClientResource cr = new ClientResource("http://localhost/test");
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        assertEquals(Method.POST, request.getMethod());
                        response.setStatus(Status.SUCCESS_OK);
                        response.setEntity(new StringRepresentation("post-ack"));
                    }
                });

        Representation result = cr.post(new StringRepresentation("payload"));

        assertEquals("post-ack", getText(result));
    }

    @Test
    void delete_sendsDeleteMethodToNextRestlet() {
        ClientResource cr = new ClientResource("http://localhost/test");
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        assertEquals(Method.DELETE, request.getMethod());
                        response.setStatus(Status.SUCCESS_OK);
                    }
                });

        cr.delete();

        assertEquals(Status.SUCCESS_OK, cr.getStatus());
    }

    @Test
    void handle_withErrorStatus_throwsResourceException() {
        ClientResource cr = new ClientResource("http://localhost/test");
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    }
                });

        ResourceException exception = assertThrows(ResourceException.class, cr::get);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, exception.getStatus());
    }

    @Test
    void handle_retriesRecoverableErrorsUntilSuccess() {
        ClientResource cr = new ClientResource("http://localhost/test");
        cr.setRetryDelay(0);
        final AtomicInteger callCount = new AtomicInteger();
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        if (callCount.incrementAndGet() <= 2) {
                            response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                        } else {
                            response.setStatus(Status.SUCCESS_OK);
                            response.setEntity(new StringRepresentation("retried-result"));
                        }
                    }
                });

        Representation result = cr.get();

        assertEquals(3, callCount.get());
        assertEquals("retried-result", getText(result));
    }

    @Test
    void handle_followsSeeOtherRedirectForSafeMethod() {
        ClientResource cr = new ClientResource("http://localhost/test");
        final AtomicInteger callCount = new AtomicInteger();
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        if (callCount.incrementAndGet() == 1) {
                            response.setStatus(Status.REDIRECTION_SEE_OTHER);
                            response.setLocationRef(new Reference("http://localhost/other"));
                        } else {
                            response.setStatus(Status.SUCCESS_OK);
                            response.setEntity(new StringRepresentation("redirected-result"));
                        }
                    }
                });

        Representation result = cr.get();

        assertEquals(2, callCount.get());
        assertEquals("redirected-result", getText(result));
    }

    @Test
    void getChild_withRelativeReference_returnsChildResource() {
        ClientResource cr = new ClientResource("http://localhost/parent/");

        ClientResource child = cr.getChild("child");

        assertEquals(new Reference("http://localhost/parent/child"), child.getReference());
    }

    @Test
    void getChild_withAbsoluteReference_throwsResourceException() {
        ClientResource cr = new ClientResource("http://localhost/parent/");

        ResourceException exception =
                assertThrows(
                        ResourceException.class,
                        () -> cr.getChild(new Reference("http://otherhost/child")));

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getParent_withHierarchicalReference_returnsParentResource() {
        ClientResource cr = new ClientResource("http://localhost/parent/child");

        ClientResource parent = cr.getParent();

        assertEquals(new Reference("http://localhost/parent/"), parent.getReference());
    }

    @Test
    void getParent_withNonHierarchicalReference_throwsResourceException() {
        ClientResource cr = new ClientResource("mailto:someone@example.com");

        ResourceException exception = assertThrows(ResourceException.class, cr::getParent);

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, exception.getStatus());
    }

    @Test
    void wrap_proxiesAnnotatedInterfaceThroughFinder() {
        boolean previous =
                org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED;
        org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = true;
        try {
            ClientResource cr = new ClientResource("http://localhost/test");
            Finder finder = new Finder(new Context());
            finder.setTargetClass(MyServerResource01.class);
            cr.setNext(finder);

            MyResource01 proxy = cr.wrap(MyResource01.class);

            assertNotNull(proxy.represent());
            assertEquals("myName", proxy.represent().getName());
        } finally {
            org.restlet.representation.ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED =
                    previous;
        }
    }

    @Test
    void setEntityBuffering_setsBothRequestAndResponseBuffering() {
        ClientResource cr = new ClientResource("http://localhost/test");

        cr.setEntityBuffering(true);

        assertTrue(cr.isRequestEntityBuffering());
        assertTrue(cr.isResponseEntityBuffering());
    }

    private static ClientResource echoResource() {
        ClientResource cr = new ClientResource("http://localhost/test");
        cr.setNext(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setStatus(Status.SUCCESS_OK);
                        response.setEntity(
                                new StringRepresentation("echo-" + request.getMethod().getName()));
                    }
                });
        return cr;
    }

    @Test
    void get_withResultClass_convertsEntity() {
        ClientResource cr = echoResource();
        String result = cr.get(String.class);
        assertEquals("echo-GET", result);
    }

    @Test
    void get_withMediaType_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.get(org.restlet.data.MediaType.TEXT_PLAIN);
        assertEquals("echo-GET", getText(result));
    }

    @Test
    void head_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.head();
        assertEquals("echo-HEAD", getText(result));
        assertNotNull(result);
    }

    @Test
    void head_withMediaType_setsAcceptedMediaType() {
        ClientResource cr = echoResource();
        Representation result = cr.head(org.restlet.data.MediaType.TEXT_PLAIN);
        assertEquals("echo-HEAD", getText(result));
    }

    @Test
    void options_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.options();
        assertEquals("echo-OPTIONS", getText(result));
    }

    @Test
    void options_withResultClass_convertsEntity() {
        ClientResource cr = echoResource();
        String result = cr.options(String.class);
        assertEquals("echo-OPTIONS", result);
    }

    @Test
    void options_withMediaType_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.options(org.restlet.data.MediaType.TEXT_PLAIN);
        assertEquals("echo-OPTIONS", getText(result));
    }

    @Test
    void patch_withObjectEntity_sendsPatchMethod() {
        ClientResource cr = echoResource();
        Representation result = cr.patch("payload");
        assertEquals("echo-PATCH", getText(result));
    }

    @Test
    void patch_withObjectEntityAndResultClass_convertsEntity() {
        ClientResource cr = echoResource();
        String result = cr.patch("payload", String.class);
        assertEquals("echo-PATCH", result);
    }

    @Test
    void patch_withObjectEntityAndMediaType_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.patch("payload", org.restlet.data.MediaType.TEXT_PLAIN);
        assertEquals("echo-PATCH", getText(result));
    }

    @Test
    void patch_withRepresentationEntity_sendsPatchMethod() {
        ClientResource cr = echoResource();
        Representation result = cr.patch(new StringRepresentation("payload"));
        assertEquals("echo-PATCH", getText(result));
    }

    @Test
    void post_withObjectEntityAndResultClass_convertsEntity() {
        ClientResource cr = echoResource();
        String result = cr.post("payload", String.class);
        assertEquals("echo-POST", result);
    }

    @Test
    void post_withObjectEntityAndMediaType_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.post("payload", org.restlet.data.MediaType.TEXT_PLAIN);
        assertEquals("echo-POST", getText(result));
    }

    @Test
    void post_withObjectEntity_sendsPostMethod() {
        ClientResource cr = echoResource();
        Representation result = cr.post("payload");
        assertEquals("echo-POST", getText(result));
    }

    @Test
    void put_withObjectEntityAndResultClass_convertsEntity() {
        ClientResource cr = echoResource();
        String result = cr.put("payload", String.class);
        assertEquals("echo-PUT", result);
    }

    @Test
    void put_withObjectEntityAndMediaType_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.put("payload", org.restlet.data.MediaType.TEXT_PLAIN);
        assertEquals("echo-PUT", getText(result));
    }

    @Test
    void put_withObjectEntity_sendsPutMethod() {
        ClientResource cr = echoResource();
        Representation result = cr.put("payload");
        assertEquals("echo-PUT", getText(result));
    }

    @Test
    void delete_withResultClass_convertsEntity() {
        ClientResource cr = echoResource();
        String result = cr.delete(String.class);
        assertEquals("echo-DELETE", result);
    }

    @Test
    void delete_withMediaType_returnsRepresentation() {
        ClientResource cr = echoResource();
        Representation result = cr.delete(org.restlet.data.MediaType.TEXT_PLAIN);
        assertEquals("echo-DELETE", getText(result));
    }

    private static String getText(Representation representation) {
        try {
            return representation == null ? null : representation.getText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
