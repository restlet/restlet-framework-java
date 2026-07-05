/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.spring;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/** Unit tests for {@link SpringFinder}. */
class SpringFinderTestCase {

    public static class SamplePrivateConstructorResource extends ServerResource {
        private SamplePrivateConstructorResource() {}
    }

    public static class SampleResource extends ServerResource {}

    @Test
    void testConstructorWithContext() {
        Context context = new Context();
        SpringFinder finder = new SpringFinder(context);
        assertNotNull(finder.getContext(), "Finder context should not be null");
    }

    @Test
    void testConstructorWithContextAndTargetClass() {
        Context context = new Context();
        SpringFinder finder = new SpringFinder(context, SampleResource.class);
        assertInstanceOf(
                SampleResource.class,
                finder.create(),
                "Finder should create instances of the target class");
    }

    @Test
    void testConstructorWithNoArguments() {
        SpringFinder finder = new SpringFinder();
        assertNull(finder.getTargetClass(), "Target class should be null by default");
    }

    @Test
    void testConstructorWithParentRestlet() {
        Context context = new Context();
        Restlet parent = new Router(context);
        SpringFinder finder = new SpringFinder(parent);
        assertNotNull(finder.getContext(), "Finder context should come from the parent Restlet");
    }

    @Test
    void testCreateDelegatesToNoArgCreate() {
        SpringFinder finder = new SpringFinder();
        finder.setTargetClass(SampleResource.class);

        Request request = new Request();
        Response response = new Response(request);

        ServerResource resource = finder.create(request, response);
        assertInstanceOf(
                SampleResource.class, resource, "Should create an instance of SampleResource");
    }

    @Test
    void testCreateLongFormDelegatesToNoArgCreate() {
        SpringFinder finder = new SpringFinder();
        finder.setTargetClass(SampleResource.class);

        Request request = new Request();
        Response response = new Response(request);

        ServerResource resource = finder.create(SampleResource.class, request, response);
        assertInstanceOf(
                SampleResource.class, resource, "Should create an instance of SampleResource");
    }

    @Test
    void testCreateReturnsInstanceOfTargetClass() {
        SpringFinder finder = new SpringFinder();
        finder.setTargetClass(SampleResource.class);

        ServerResource resource = finder.create();
        assertInstanceOf(
                SampleResource.class, resource, "Should create an instance of SampleResource");
    }

    @Test
    void testCreateReturnsNullWhenInstantiationFails() {
        SpringFinder finder = new SpringFinder();
        finder.setTargetClass(SamplePrivateConstructorResource.class);

        ServerResource resource = finder.create();
        assertNull(resource, "Should return null when the target class cannot be instantiated");
    }

    @Test
    void testCreateReturnsNullWhenNoTargetClassSet() {
        SpringFinder finder = new SpringFinder();
        assertNull(finder.create(), "Should return null when no target class is set");
    }
}
