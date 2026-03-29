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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.data.Form;
import org.restlet.data.Parameter;

class SeriesTestCase {

    /** A parameter whose name is not a key in the map is silently skipped. */
    @Test
    void copyTo_unknownNameIsIgnored() {
        Form form = form("foo", "bar");
        Map<String, Object> params = map("other");

        form.copyTo(params);

        assertNull(params.get("other"));
    }

    /** An empty Series leaves the map untouched. */
    @Test
    void copyTo_emptySeriesLeavesMapUnchanged() {
        Form form = new Form();
        Map<String, Object> params = map("key");

        form.copyTo(params);

        assertNull(params.get("key"));
    }

    /** A single parameter whose name matches a key stores its value directly (not in a list). */
    @Test
    void copyTo_singleValueStoredDirectly() {
        Form form = form("color", "red");
        Map<String, Object> params = map("color");

        form.copyTo(params);

        assertEquals("red", params.get("color"));
    }

    /** A parameter with a null value stores {@link Series#EMPTY_VALUE} as a sentinel. */
    @Test
    void copyTo_nullValueStoredAsEmptyValue() {
        Form form = new Form();
        form.add(new Parameter("flag", null));
        Map<String, Object> params = map("flag");

        form.copyTo(params);

        assertSame(Series.EMPTY_VALUE, params.get("flag"));
    }

    /** Two parameters with the same name produce a List containing both values. */
    @Test
    void copyTo_duplicateNamesProduceList() {
        Form form = form("accept", "text/html", "accept", "application/json");
        Map<String, Object> params = map("accept");

        form.copyTo(params);

        Object value = params.get("accept");
        assertInstanceOf(List.class, value, "Expected a List for duplicate entries");
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) value;
        assertEquals(2, list.size());
        assertEquals("text/html", list.get(0));
        assertEquals("application/json", list.get(1));
    }

    /** Three parameters with the same name all end up in a single growing List. */
    @Test
    void copyTo_triplicateNamesProduceListOfThree() {
        Form form = form("x", "1", "x", "2", "x", "3");
        Map<String, Object> params = map("x");

        form.copyTo(params);

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) params.get("x");
        assertEquals(3, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("3", list.get(2));
    }

    /** Null values in a multi-value entry are stored as {@link Series#EMPTY_VALUE}. */
    @Test
    void copyTo_nullValuesInListStoredAsEmptyValue() {
        Form form = new Form();
        form.add(new Parameter("h", null));
        form.add(new Parameter("h", null));
        Map<String, Object> params = map("h");

        form.copyTo(params);

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) params.get("h");
        assertEquals(2, list.size());
        assertSame(Series.EMPTY_VALUE, list.get(0));
        assertSame(Series.EMPTY_VALUE, list.get(1));
    }

    /** Mixed null and non-null values in a multi-value entry are handled correctly. */
    @Test
    void copyTo_mixedNullAndNonNullValuesInList() {
        Form form = new Form();
        form.add(new Parameter("h", "value1"));
        form.add(new Parameter("h", null));
        form.add(new Parameter("h", "value2"));
        Map<String, Object> params = map("h");

        form.copyTo(params);

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) params.get("h");
        assertEquals(3, list.size());
        assertEquals("value1", list.get(0));
        assertSame(Series.EMPTY_VALUE, list.get(1));
        assertEquals("value2", list.get(2));
    }

    /**
     * Only the keys initially present in the map receive values; unrelated entries are untouched.
     */
    @Test
    void copyTo_onlyMapKeysArePopulated() {
        Form form = form("a", "1", "b", "2", "c", "3");
        Map<String, Object> params = map("a", "c");

        form.copyTo(params);

        assertEquals("1", params.get("a"));
        assertEquals("3", params.get("c"));
        assertFalse(params.containsKey("b"), "Key 'b' should not have been inserted");
    }

    // -- helpers --

    private static Form form(String... pairs) {
        Form form = new Form();
        for (int i = 0; i < pairs.length; i += 2) {
            form.add(pairs[i], pairs[i + 1]);
        }
        return form;
    }

    private static Map<String, Object> map(String... keys) {
        Map<String, Object> m = new HashMap<>();
        for (String key : keys) {
            m.put(key, null);
        }
        return m;
    }
}
