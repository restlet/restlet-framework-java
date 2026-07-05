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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit tests for the {@link Finder} class.
 *
 * @author Jerome Louvel
 */
class FinderTestCase {

    /** Simple server resource used as a target for the finder. */
    public static class SampleServerResource extends ServerResource {

        @Override
        protected Representation get() {
            return new StringRepresentation("sample-content");
        }
    }

    @BeforeEach
    void setUpEach() {
        Engine.clearThreadLocalVariables();
    }

    @AfterEach
    void tearDownEach() {
        Engine.clearThreadLocalVariables();
    }

    @Test
    void defaultConstructor_hasNoTargetClass() {
        Finder finder = new Finder();

        assertNull(finder.getTargetClass());
    }

    @Test
    void constructorWithTargetClass_setsTargetClass() {
        Finder finder = new Finder(new Context(), SampleServerResource.class);

        assertEquals(SampleServerResource.class, finder.getTargetClass());
    }

    @Test
    void setTargetClassThenGetTargetClass_roundTrips() {
        Finder finder = new Finder();

        finder.setTargetClass(SampleServerResource.class);

        assertEquals(SampleServerResource.class, finder.getTargetClass());
    }

    @Test
    void create_withoutTargetClass_returnsNull() {
        Finder finder = new Finder();
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        assertNull(finder.create(request, response));
    }

    @Test
    void create_withTargetClass_instantiatesDefaultConstructor() {
        Finder finder = new Finder(null, SampleServerResource.class);
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        ServerResource result = finder.create(request, response);

        assertNotNull(result);
        assertTrue(result instanceof SampleServerResource);
    }

    @Test
    void find_delegatesToCreate() {
        Finder finder = new Finder(null, SampleServerResource.class);
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        assertNotNull(finder.find(request, response));
    }

    @Test
    void handle_withoutTargetClass_setsNotFoundStatus() {
        Finder finder = new Finder();
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        finder.handle(request, response);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    @Test
    void handle_withTargetClass_dispatchesToServerResource() {
        Finder finder = new Finder(new Context(), SampleServerResource.class);
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        finder.handle(request, response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void toString_withoutTargetClass_indicatesNoTarget() {
        Finder finder = new Finder();

        assertEquals("Finder with no target class", finder.toString());
    }

    @Test
    void toString_withTargetClass_includesSimpleName() {
        Finder finder = new Finder(null, SampleServerResource.class);

        assertTrue(finder.toString().contains("SampleServerResource"));
    }

    @Test
    void createFinder_withoutFinderClass_createsPlainFinder() {
        Finder finder = Finder.createFinder(SampleServerResource.class, null, new Context(), null);

        assertNotNull(finder);
        assertEquals(Finder.class, finder.getClass());
        assertEquals(SampleServerResource.class, finder.getTargetClass());
    }

    /** Custom finder subclass exposing the expected (Context, Class) constructor. */
    public static class CustomFinder extends Finder {
        public CustomFinder(Context context, Class<? extends ServerResource> targetClass) {
            super(context, targetClass);
        }
    }

    @Test
    void createFinder_withFinderClass_instantiatesCustomFinder() {
        Finder finder =
                Finder.createFinder(
                        SampleServerResource.class, CustomFinder.class, new Context(), null);

        assertNotNull(finder);
        assertTrue(finder instanceof CustomFinder);
        assertEquals(SampleServerResource.class, finder.getTargetClass());
    }
}
