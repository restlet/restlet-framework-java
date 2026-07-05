/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

class ResolverTestCase {

    private static class UppercaseResolver extends Resolver<String> {
        @Override
        public String resolve(String name) {
            return name.toUpperCase();
        }
    }

    @Test
    void resolve_customImplementation_returnsExpectedValue() {
        Resolver<String> resolver = new UppercaseResolver();
        assertEquals("HELLO", resolver.resolve("hello"));
    }

    @Test
    void createResolver_fromMap_resolvesEntries() {
        Resolver<?> resolver = Resolver.createResolver(Map.of("key", "value"));
        assertEquals("value", resolver.resolve("key"));
    }

    @Test
    void createResolver_fromRequestAndResponse_isNotNull() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        Resolver<?> resolver = Resolver.createResolver(request, response);
        assertNotNull(resolver);
        assertEquals("GET", resolver.resolve("m"));
    }
}
