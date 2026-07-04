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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link WrapperMap}. */
class WrapperMapTestCase {

    @Test
    void defaultConstructor_usesConcurrentHashMapDelegate() {
        WrapperMap<String, String> map = new WrapperMap<>();
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    void delegatesAllMapOperations() {
        Map<String, String> delegate = new HashMap<>();
        WrapperMap<String, String> map = new WrapperMap<>(delegate);

        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsValue("value"));
        assertEquals("value", map.get("key"));
        assertTrue(map.equals(delegate));
        assertEquals(delegate.hashCode(), map.hashCode());
        assertEquals(1, map.keySet().size());
        assertEquals(1, map.entrySet().size());
        assertEquals(1, map.values().size());

        map.putAll(Map.of("key2", "value2"));
        assertEquals(2, map.size());

        map.remove("key2");
        assertEquals(1, map.size());

        map.clear();
        assertTrue(map.isEmpty());
    }
}
