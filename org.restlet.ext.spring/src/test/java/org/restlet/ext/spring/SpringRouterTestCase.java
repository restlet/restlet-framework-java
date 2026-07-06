/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.spring.resources.UserResource;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;

/** Unit tests for {@link SpringRouter}. */
class SpringRouterTestCase {

    private static class TestRestlet extends Restlet {}

    private SpringRouter router;

    @BeforeEach
    void setUpEach() {
        this.router = new SpringRouter();
    }

    @AfterEach
    void tearDownEach() {
        this.router = null;
    }

    @Test
    void testConstructorWithContext() {
        SpringRouter contextRouter = new SpringRouter(new Context());
        assertNotNull(contextRouter.getContext(), "Router context should not be null");
    }

    @Test
    void testConstructorWithNoArguments() {
        assertEquals(0, this.router.getRoutes().size(), "New router should have no routes");
    }

    @Test
    void testConstructorWithParentRestlet() {
        Router parent = new Router(new Context());
        SpringRouter childRouter = new SpringRouter(parent);
        assertNotNull(
                childRouter.getContext(), "Router context should come from the parent Restlet");
    }

    @Test
    void testSetAttachmentIgnoresUnknownObjectType() {
        SpringRouter.setAttachment(this.router, "/unknown", 42);
        assertEquals(0, this.router.getRoutes().size(), "No route should have been attached");
    }

    @Test
    void testSetAttachmentIgnoresUnresolvableClassName() {
        SpringRouter.setAttachment(this.router, "/missing", "com.example.DoesNotExist");
        assertEquals(0, this.router.getRoutes().size(), "No route should have been attached");
    }

    @Test
    void testSetAttachmentIgnoresStringForNonResourceClass() {
        SpringRouter.setAttachment(this.router, "/notaresource", "java.lang.String");
        assertEquals(0, this.router.getRoutes().size(), "No route should have been attached");
    }

    @Test
    void testSetAttachmentWithClass() {
        SpringRouter.setAttachment(this.router, "/user", UserResource.class);
        assertEquals(1, this.router.getRoutes().size(), "One route should have been attached");
    }

    @Test
    void testSetAttachmentWithClassName() {
        SpringRouter.setAttachment(
                this.router, "/user", "org.restlet.ext.spring.resources.UserResource");
        assertEquals(1, this.router.getRoutes().size(), "One route should have been attached");
    }

    @Test
    void testSetAttachmentWithRestlet() {
        TestRestlet target = new TestRestlet();
        SpringRouter.setAttachment(this.router, "/target", target);

        Route route = this.router.getRoutes().getFirst();
        assertInstanceOf(TemplateRoute.class, route, "Attached route should be a TemplateRoute");
        assertEquals(target, route.getNext(), "Attached route should point to the given Restlet");
    }

    @Test
    void testSetAttachmentsAttachesAllRoutes() {
        Map<String, Object> routes = new HashMap<>();
        routes.put("/one", new TestRestlet());
        routes.put("/two", new TestRestlet());
        SpringRouter.setAttachments(this.router, routes);
        assertEquals(2, this.router.getRoutes().size(), "Both routes should have been attached");
    }

    @Test
    void testSetAttachmentsInstanceMethod() {
        this.router.setAttachments(Collections.singletonMap("/one", new TestRestlet()));
        assertEquals(1, this.router.getRoutes().size(), "One route should have been attached");
    }

    @Test
    void testSetDefaultAttachment() {
        TestRestlet target = new TestRestlet();
        this.router.setDefaultAttachment(target);
        assertEquals(1, this.router.getRoutes().size(), "Default route should have been attached");
        assertEquals(
                target,
                this.router.getRoutes().getFirst().getNext(),
                "Default route should point to target");
    }
}
