/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.engine.application.RangeFilter;
import org.restlet.routing.Filter;

/** Unit tests for {@link RangeService}. */
class RangeServiceTestCase {

    @Test
    void defaultConstructor_isEnabled() {
        RangeService service = new RangeService();
        assertTrue(service.isEnabled());
    }

    @Test
    void constructorWithFlag_setsEnabled() {
        RangeService service = new RangeService(false);
        assertFalse(service.isEnabled());
    }

    @Test
    void createInboundFilter_returnsRangeFilter() {
        RangeService service = new RangeService();

        Filter filter = service.createInboundFilter(new Context());

        assertNotNull(filter);
        assertInstanceOf(RangeFilter.class, filter);
    }
}
