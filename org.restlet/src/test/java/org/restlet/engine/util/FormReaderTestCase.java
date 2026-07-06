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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

class FormReaderTestCase {

    @Nested
    class ReadParameter {
        /** Returns null when no parameter matches the requested name. */
        @Test
        void shouldReturnNullWhenNameNotFound() throws IOException {
            FormReader reader = new FormReader("foo=bar&baz=qux", '&');

            Object result = reader.readParameter("missing");

            assertNull(result);
        }

        /** Returns the value string when the name is found exactly once. */
        @Test
        void shouldReturnSingleValue() throws IOException {
            FormReader reader = new FormReader("color=red&size=large", '&');

            Object result = reader.readParameter("color");

            assertEquals("red", result);
        }

        /** Returns {@link Series#EMPTY_VALUE} when the parameter has no value (no '=' sign). */
        @Test
        void shouldReturnEmptyValueForParameterWithoutValue() throws IOException {
            FormReader reader = new FormReader("flag&other=val", '&');

            Object result = reader.readParameter("flag");

            assertSame(Series.EMPTY_VALUE, result);
        }

        /** Returns a List containing both values when the name appears twice. */
        @Test
        void shouldReturnListForDuplicateNames() throws IOException {
            FormReader reader = new FormReader("accept=text/html&accept=application/json", '&');

            Object result = reader.readParameter("accept");

            assertInstanceOf(List.class, result);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertEquals(2, list.size());
            assertEquals("text/html", list.getFirst());
            assertEquals("application/json", list.get(1));
        }

        /** A third occurrence is appended to the existing list (not nested). */
        @Test
        void shouldReturnListForTriplicateNames() throws IOException {
            FormReader reader = new FormReader("x=1&x=2&x=3", '&');

            Object result = reader.readParameter("x");

            assertInstanceOf(List.class, result);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertEquals(3, list.size());
            assertEquals("1", list.getFirst());
            assertEquals("2", list.get(1));
            assertEquals("3", list.get(2));
        }

        /** Null values in a multi-value entry are stored as {@link Series#EMPTY_VALUE}. */
        @Test
        void nullValuesInListStoredAsEmptyValue() throws IOException {
            FormReader reader = new FormReader("h&h", '&');

            Object result = reader.readParameter("h");

            assertInstanceOf(List.class, result);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            assertEquals(2, list.size());
            assertSame(Series.EMPTY_VALUE, list.getFirst());
            assertSame(Series.EMPTY_VALUE, list.get(1));
        }
    }

    @Nested
    class ReadParameters {
        /** A parameter whose name is not a key in the map is silently ignored. */
        @Test
        void shouldIgnoreUnknownName() throws IOException {
            FormReader reader = new FormReader("foo=bar&baz=qux", '&');
            Map<String, Object> params = map("other");

            reader.readParameters(params);

            assertNull(params.get("other"));
        }

        /** A matching parameter stores its value directly (not in a list). */
        @Test
        void shouldStoreSingleValue() throws IOException {
            FormReader reader = new FormReader("color=red&size=large", '&');
            Map<String, Object> params = map("color");

            reader.readParameters(params);

            assertEquals("red", params.get("color"));
        }

        /** A parameter with no value stores {@link Series#EMPTY_VALUE} as a sentinel. */
        @Test
        void shouldStoreEmptyValueWhenNullValue() throws IOException {
            FormReader reader = new FormReader("flag&other=val", '&');
            Map<String, Object> params = map("flag");

            reader.readParameters(params);

            assertSame(Series.EMPTY_VALUE, params.get("flag"));
        }

        /** Two parameters with the same name produce a List containing both values. */
        @Test
        void shouldProduceListWhenDuplicateNames() throws IOException {
            FormReader reader = new FormReader("accept=text/html&accept=application/json", '&');
            Map<String, Object> params = map("accept");

            reader.readParameters(params);

            assertInstanceOf(List.class, params.get("accept"));
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) params.get("accept");
            assertEquals(2, list.size());
            assertEquals("text/html", list.getFirst());
            assertEquals("application/json", list.get(1));
        }

        /** Three parameters with the same name all end up in a single growing List. */
        @Test
        void shouldStoreListOfThreeWhenTriplicateNames() throws IOException {
            FormReader reader = new FormReader("x=1&x=2&x=3", '&');
            Map<String, Object> params = map("x");

            reader.readParameters(params);

            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) params.get("x");
            assertEquals(3, list.size());
            assertEquals("1", list.getFirst());
            assertEquals("2", list.get(1));
            assertEquals("3", list.get(2));
        }

        /** Null values in a multi-value list entry are stored as {@link Series#EMPTY_VALUE}. */
        @Test
        void shouldStoreEmptyValuesWhenNullValuesInList() throws IOException {
            FormReader reader = new FormReader("h&h", '&');
            Map<String, Object> params = map("h");

            reader.readParameters(params);

            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) params.get("h");
            assertEquals(2, list.size());
            assertSame(Series.EMPTY_VALUE, list.getFirst());
            assertSame(Series.EMPTY_VALUE, list.get(1));
        }

        /** Only keys already present in the map are populated; unrelated parameters are skipped. */
        @Test
        void shouldPopulateOnlyMapKeys() throws IOException {
            FormReader reader = new FormReader("a=1&b=2&c=3", '&');
            Map<String, Object> params = map("a", "c");

            reader.readParameters(params);

            assertEquals("1", params.get("a"));
            assertEquals("3", params.get("c"));
            assertEquals(2, params.size(), "Key 'b' should not have been inserted");
        }
    }

    @Nested
    class Read {
        /** An empty input string produces an empty Form. */
        @Test
        void shouldReturnEmptyFormForEmptyString() throws IOException {
            FormReader reader = new FormReader("", '&');

            Form form = reader.read();

            assertTrue(form.isEmpty());
        }

        /** All parameters are parsed into the returned Form. */
        @Test
        void shouldReturnAllParameters() throws IOException {
            FormReader reader = new FormReader("color=red&size=large", '&');

            Form form = reader.read();

            assertEquals(2, form.size());
            assertEquals("red", form.getFirstValue("color"));
            assertEquals("large", form.getFirstValue("size"));
        }

        /** A parameter without a value is stored with a null value. */
        @Test
        void shouldHandleParameterWithNoValue() throws IOException {
            FormReader reader = new FormReader("flag&other=val", '&');

            Form form = reader.read();

            assertEquals(2, form.size());
            assertNull(form.getFirstValue("flag"));
            assertEquals("val", form.getFirstValue("other"));
        }

        /** Parameters with duplicate names are all present in the Form. */
        @Test
        void shouldRetainAllDuplicateNameParameters() throws IOException {
            FormReader reader = new FormReader("accept=text/html&accept=application/json", '&');

            Form form = reader.read();

            assertEquals(2, form.size());
            List<Parameter> values = form.subList("accept");
            assertEquals(2, values.size());
        }
    }

    @Nested
    class ReadFirstParameter {
        /** Returns null when the name is not present in the query string. */
        @Test
        void shouldReturnNullWhenNameNotFound() throws IOException {
            FormReader reader = new FormReader("foo=bar&baz=qux", '&');

            Parameter result = reader.readFirstParameter("missing");

            assertNull(result);
        }

        /** Returns the matching Parameter when the name appears exactly once. */
        @Test
        void shouldReturnParameterWhenNameFound() throws IOException {
            FormReader reader = new FormReader("color=red&size=large", '&');

            Parameter result = reader.readFirstParameter("size");

            assertEquals("size", result.getName());
            assertEquals("large", result.getValue());
        }

        /** Returns only the first occurrence when the name appears multiple times. */
        @Test
        void shouldReturnFirstOccurrenceWhenDuplicate() throws IOException {
            FormReader reader = new FormReader("accept=text/html&accept=application/json", '&');

            Parameter result = reader.readFirstParameter("accept");

            assertEquals("accept", result.getName());
            assertEquals("text/html", result.getValue());
        }

        /** Returns a Parameter with a null value when no '=' is present. */
        @Test
        void shouldReturnNullValueForParameterWithoutEquals() throws IOException {
            FormReader reader = new FormReader("flag&other=val", '&');

            Parameter result = reader.readFirstParameter("flag");

            assertEquals("flag", result.getName());
            assertNull(result.getValue());
        }
    }

    @Nested
    class ReadNextParameter {
        /** Returns null immediately for an empty string. */
        @Test
        void shouldReturnNullForEmptyString() throws IOException {
            FormReader reader = new FormReader("", '&');

            Parameter result = reader.readNextParameter();

            assertNull(result);
        }

        /** Parses name and value correctly for a single parameter. */
        @Test
        void shouldParseSingleNameValueParameter() throws IOException {
            FormReader reader = new FormReader("key=value", '&');

            Parameter result = reader.readNextParameter();

            assertEquals("key", result.getName());
            assertEquals("value", result.getValue());
        }

        /** A parameter with no '=' sign is parsed with a null value. */
        @Test
        void shouldParseParameterWithNoValue() throws IOException {
            FormReader reader = new FormReader("flag", '&');

            Parameter result = reader.readNextParameter();

            assertEquals("flag", result.getName());
            assertNull(result.getValue());
        }

        /** Successive calls return parameters one at a time, then null at the end. */
        @Test
        void shouldIterateParametersSequentially() throws IOException {
            FormReader reader = new FormReader("a=1&b=2", '&');

            Parameter first = reader.readNextParameter();
            Parameter second = reader.readNextParameter();
            Parameter third = reader.readNextParameter();

            assertEquals("a", first.getName());
            assertEquals("1", first.getValue());
            assertEquals("b", second.getName());
            assertEquals("2", second.getValue());
            assertNull(third);
        }
    }

    /** All parsed parameters are appended to the given series. */
    @Test
    void shouldAddAllParametersToSeries() {
        FormReader reader = new FormReader("color=red&size=large", '&');
        Form form = new Form();

        reader.addParameters(form);

        assertEquals(2, form.size());
        assertEquals("red", form.getFirstValue("color"));
        assertEquals("large", form.getFirstValue("size"));
    }

    /** A parameter without a value is appended with a null value. */
    @Test
    void shouldAddParameterWithNullValue() {
        FormReader reader = new FormReader("flag&other=val", '&');
        Form form = new Form();

        reader.addParameters(form);

        assertEquals(2, form.size());
        assertNull(form.getFirstValue("flag"));
        assertEquals("val", form.getFirstValue("other"));
    }

    /** Duplicate names are all appended as separate entries. */
    @Test
    void shouldAddAllDuplicateEntries() {
        FormReader reader = new FormReader("x=1&x=2&x=3", '&');
        Form form = new Form();

        reader.addParameters(form);

        assertEquals(3, form.size());
        List<Parameter> values = form.subList("x");
        assertEquals(3, values.size());
    }

    private static Map<String, Object> map(String... keys) {
        Map<String, Object> m = new HashMap<>();
        for (String key : keys) {
            m.put(key, null);
        }
        return m;
    }
}
