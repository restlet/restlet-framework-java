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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.data.Parameter;

class NamedValuesCollectorTestCase {

    @Test
    void constructorWithNames_prePopulatesKeysWithNullValues() {
        NamedValuesCollector collector = new NamedValuesCollector("a", "b");
        assertTrue(collector.getCollectedValues().containsKey("a"));
        assertNull(collector.getCollectedValues().get("a"));
    }

    @Test
    void constructorWithMap_usesProvidedMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        NamedValuesCollector collector = new NamedValuesCollector(map);
        assertEquals(map, collector.getCollectedValues());
    }

    @Test
    void collect_unknownName_isIgnored() {
        NamedValuesCollector collector = new NamedValuesCollector("a");
        collector.collect(new Parameter("unknown", "value"));
        assertFalse(collector.getCollectedValues().containsKey("unknown"));
    }

    @Test
    void collect_firstValueForKnownName_setsValue() {
        NamedValuesCollector collector = new NamedValuesCollector("a");
        collector.collect(new Parameter("a", "1"));
        assertEquals("1", collector.getCollectedValues().get("a"));
    }

    @Test
    void collect_nullValueForKnownName_usesEmptyValueMarker() {
        NamedValuesCollector collector = new NamedValuesCollector("a");
        collector.collect(new Parameter("a", null));
        assertEquals(Series.EMPTY_VALUE, collector.getCollectedValues().get("a"));
    }

    @Test
    void collect_secondValueForSameName_createsListOfValues() {
        NamedValuesCollector collector = new NamedValuesCollector("a");
        collector.collect(new Parameter("a", "1"));
        collector.collect(new Parameter("a", "2"));

        Object result = collector.getCollectedValues().get("a");
        assertInstanceOf(List.class, result);
        assertEquals(List.of("1", "2"), result);
    }

    @Test
    void collect_thirdValueForSameName_appendsToExistingList() {
        NamedValuesCollector collector = new NamedValuesCollector("a");
        collector.collect(new Parameter("a", "1"));
        collector.collect(new Parameter("a", "2"));
        collector.collect(new Parameter("a", "3"));

        assertEquals(List.of("1", "2", "3"), collector.getCollectedValues().get("a"));
    }
}
