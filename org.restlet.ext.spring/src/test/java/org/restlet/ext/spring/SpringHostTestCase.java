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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.spring.resources.UserResource;

/** Unit tests for {@link SpringHost}. */
class SpringHostTestCase {

    private static class TestRestlet extends Restlet {}

    private SpringHost host;

    @BeforeEach
    void setUpEach() {
        this.host = new SpringHost(new Context());
    }

    @AfterEach
    void tearDownEach() {
        this.host = null;
    }

    @Test
    void testConstructorWithComponent() {
        Component component = new Component();
        SpringHost componentHost = new SpringHost(component);
        assertNotNull(componentHost.getContext(), "Host context should come from the component");
    }

    @Test
    void testSetAttachmentSetsContextOnContextlessRestlet() {
        TestRestlet target = new TestRestlet();
        this.host.setAttachment("/target", target);

        assertNotNull(target.getContext(), "Target should have received a context");
        assertEquals(1, this.host.getRoutes().size(), "One route should have been attached");
    }

    @Test
    void testSetAttachmentWithClass() {
        this.host.setAttachment("/user", UserResource.class);
        assertEquals(1, this.host.getRoutes().size(), "One route should have been attached");
    }

    @Test
    void testSetAttachmentsAttachesAllRoutes() {
        Map<String, Object> routes = new HashMap<>();
        routes.put("/one", new TestRestlet());
        routes.put("/two", new TestRestlet());
        this.host.setAttachments(routes);
        assertEquals(2, this.host.getRoutes().size(), "Both routes should have been attached");
    }

    @Test
    void testSetDefaultAttachment() {
        TestRestlet target = new TestRestlet();
        this.host.setDefaultAttachment(target);
        assertEquals(1, this.host.getRoutes().size(), "Default route should have been attached");
        assertEquals(
                target,
                this.host.getRoutes().getFirst().getNext(),
                "Default route should point to target");
    }

    @Test
    void testSetAttachmentInstanceMethodSingleRoute() {
        this.host.setAttachments(Collections.singletonMap("/one", new TestRestlet()));
        assertEquals(1, this.host.getRoutes().size(), "One route should have been attached");
    }
}
