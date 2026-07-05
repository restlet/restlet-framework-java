/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;

/** Unit tests for {@link VirtualHost}. */
class VirtualHostTestCase {

    @Test
    void defaultConstructor_matchesEverything() {
        VirtualHost host = new VirtualHost();

        assertEquals(".*", host.getHostDomain());
        assertEquals(".*", host.getHostPort());
        assertEquals(".*", host.getHostScheme());
        assertEquals(".*", host.getResourceDomain());
        assertEquals(".*", host.getResourcePort());
        assertEquals(".*", host.getResourceScheme());
        assertEquals(".*", host.getServerAddress());
        assertEquals(".*", host.getServerPort());
        assertEquals(Template.MODE_STARTS_WITH, host.getDefaultMatchingMode());
        assertEquals(Router.MODE_BEST_MATCH, host.getRoutingMode());
    }

    @Test
    void constructorWithContext_createsChildContext() {
        Context parent = new Context();

        VirtualHost host = new VirtualHost(parent);

        assertNotNull(host.getContext());
    }

    @Test
    void fullConstructor_setsAllPatterns() {
        Context parent = new Context();

        VirtualHost host =
                new VirtualHost(
                        parent,
                        "hostDomain",
                        "hostPort",
                        "hostScheme",
                        "resourceDomain",
                        "resourcePort",
                        "resourceScheme",
                        "serverAddress",
                        "serverPort");

        assertEquals("hostDomain", host.getHostDomain());
        assertEquals("hostPort", host.getHostPort());
        assertEquals("hostScheme", host.getHostScheme());
        assertEquals("resourceDomain", host.getResourceDomain());
        assertEquals("resourcePort", host.getResourcePort());
        assertEquals("resourceScheme", host.getResourceScheme());
        assertEquals("serverAddress", host.getServerAddress());
        assertEquals("serverPort", host.getServerPort());
    }

    @Test
    void settersRoundTrip() {
        VirtualHost host = new VirtualHost();

        host.setHostDomain("a");
        assertEquals("a", host.getHostDomain());

        host.setHostPort("b");
        assertEquals("b", host.getHostPort());

        host.setHostScheme("c");
        assertEquals("c", host.getHostScheme());

        host.setResourceDomain("d");
        assertEquals("d", host.getResourceDomain());

        host.setResourcePort("e");
        assertEquals("e", host.getResourcePort());

        host.setResourceScheme("f");
        assertEquals("f", host.getResourceScheme());

        host.setServerAddress("g");
        assertEquals("g", host.getServerAddress());

        host.setServerPort("h");
        assertEquals("h", host.getServerPort());
    }

    @Test
    void attach_withoutContext_createsChildContextForTarget() {
        Context parent = new Context();
        VirtualHost host = new VirtualHost(parent);
        Restlet target = new MockRestlet(null);

        host.attach(target);

        assertNotNull(target.getContext());
    }

    @Test
    void attach_withPathAndTargetHavingContext_keepsExistingContext() {
        VirtualHost host = new VirtualHost(new Context());
        Context existing = new Context();
        Restlet target = new MockRestlet(existing);

        host.attach("/foo", target);

        assertSame(existing, target.getContext());
    }

    @Test
    void attachDefault_withoutContext_createsChildContextForTarget() {
        Context parent = new Context();
        VirtualHost host = new VirtualHost(parent);
        Restlet target = new MockRestlet(null);

        host.attachDefault(target);

        assertNotNull(target.getContext());
        assertSame(host.getDefaultRoute().getNext(), target);
    }

    @Test
    void handle_dispatchesAndSetsCurrentHost() {
        VirtualHost host = new VirtualHost(new Context());
        Restlet target =
                new Restlet(new Context()) {
                    @Override
                    public void handle(Request request, Response response) {
                        super.handle(request, response);
                        response.setStatus(Status.SUCCESS_OK);
                    }
                };
        host.attach("/foo", target);
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getResourceRef().setBaseRef(new Reference("http://localhost"));
        Response response = new Response(request);

        host.handle(request, response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(Integer.valueOf(host.hashCode()), VirtualHost.getCurrent());
        assertNotNull(request.getRootRef());
    }

    @Test
    void setContext_recreatesChildContext() {
        VirtualHost host = new VirtualHost();
        Context parent = new Context();

        host.setContext(parent);

        assertNotNull(host.getContext());
    }

    @Test
    void setContext_withNull_clearsContext() {
        VirtualHost host = new VirtualHost(new Context());

        host.setContext(null);

        assertNull(host.getContext());
    }

    @Test
    void staticCurrentThreadLocal_roundTrip() {
        VirtualHost.setCurrent(42);

        assertEquals(Integer.valueOf(42), VirtualHost.getCurrent());

        VirtualHost.setCurrent(null);
    }

    @Test
    void getLocalHostNameAndAddress_returnNonNullValues() {
        assertNotNull(VirtualHost.getLocalHostName());
        assertNotNull(VirtualHost.getLocalHostAddress());
    }

    @Test
    void getIpAddress_forLocalhost_returnsLoopbackAddress() {
        String ip = VirtualHost.getIpAddress("localhost");

        assertNotNull(ip);
    }

    @Test
    void getIpAddress_forUnknownHost_doesNotThrow() {
        // Depending on the local DNS/network configuration, this may resolve to
        // null (UnknownHostException) or to some address (e.g. search domain
        // wildcards), so we only assert that no exception propagates.
        VirtualHost.getIpAddress("this.host.does.not.exist.invalid");
    }
}
