/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MapResolver}. */
class MapResolverTestCase {

    @Test
    void resolve_existingKey_returnsValue() {
        MapResolver resolver = new MapResolver(Map.of("key", "value"));
        assertEquals("value", resolver.resolve("key"));
    }

    @Test
    void resolve_missingKey_returnsNull() {
        MapResolver resolver = new MapResolver(Map.of("key", "value"));
        assertNull(resolver.resolve("missing"));
    }
}
