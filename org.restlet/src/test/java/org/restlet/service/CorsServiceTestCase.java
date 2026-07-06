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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.engine.application.CorsFilter;
import org.restlet.routing.Filter;

/** Unit tests for {@link CorsService}. */
class CorsServiceTestCase {

    @Test
    void defaultConstructor_isEnabled() {
        CorsService service = new CorsService();
        assertTrue(service.isEnabled());
        assertEquals(Set.of("*"), service.getAllowedOrigins());
        assertTrue(service.isAllowingAllRequestedHeaders());
        assertFalse(service.isAllowedCredentials());
        assertFalse(service.isSkippingResourceForCorsOptions());
        assertEquals(-1, service.getMaxAge());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        CorsService service = new CorsService(false);
        assertFalse(service.isEnabled());

        service.setAllowedCredentials(true);
        assertTrue(service.isAllowedCredentials());

        service.setAllowedHeaders(Set.of("X-Custom"));
        assertEquals(Set.of("X-Custom"), service.getAllowedHeaders());

        service.setAllowedOrigins(Set.of("http://example.com"));
        assertEquals(Set.of("http://example.com"), service.getAllowedOrigins());

        service.setAllowingAllRequestedHeaders(false);
        assertFalse(service.isAllowingAllRequestedHeaders());

        service.setDefaultAllowedMethods(Set.of(Method.GET));
        assertEquals(Set.of(Method.GET), service.getDefaultAllowedMethods());

        service.setExposedHeaders(Set.of("X-Exposed"));
        assertEquals(Set.of("X-Exposed"), service.getExposedHeaders());

        service.setMaxAge(3600);
        assertEquals(3600, service.getMaxAge());

        service.setSkippingResourceForCorsOptions(true);
        assertTrue(service.isSkippingResourceForCorsOptions());
    }

    @Test
    void createInboundFilter_returnsConfiguredCorsFilter() {
        CorsService service = new CorsService();
        service.setAllowedCredentials(true);
        service.setAllowedOrigins(Set.of("http://example.com"));
        service.setAllowingAllRequestedHeaders(false);
        service.setAllowedHeaders(Set.of("X-Custom"));
        service.setExposedHeaders(Set.of("X-Exposed"));
        service.setSkippingResourceForCorsOptions(true);
        service.setDefaultAllowedMethods(Set.of(Method.GET));
        service.setMaxAge(60);

        Filter filter = service.createInboundFilter(new Context());

        assertInstanceOf(CorsFilter.class, filter);
    }
}
