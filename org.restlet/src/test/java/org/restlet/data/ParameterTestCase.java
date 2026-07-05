/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.util.Series;

/** Test {@link org.restlet.data.Parameter}. */
class ParameterTestCase {

    @Test
    void defaultConstructor_hasNullNameAndValue() {
        Parameter parameter = new Parameter();
        assertNull(parameter.getName());
        assertNull(parameter.getValue());
    }

    @Test
    void constructor_setsNameAndValue() {
        Parameter parameter = new Parameter("name", "value");
        assertEquals("name", parameter.getName());
        assertEquals("value", parameter.getValue());
    }

    @Test
    void create_withNonNullValue_createsParameter() throws Exception {
        Parameter parameter = Parameter.create("name", "value");
        assertEquals("name", parameter.getName());
        assertEquals("value", parameter.getValue());
    }

    @Test
    void create_withNullValue_createsParameterWithNullValue() throws Exception {
        Parameter parameter = Parameter.create("name", null);
        assertEquals("name", parameter.getName());
        assertNull(parameter.getValue());
    }

    @Test
    void settersUpdateState() {
        Parameter parameter = new Parameter();
        parameter.setName("name");
        parameter.setValue("value");
        assertEquals("name", parameter.getName());
        assertEquals("value", parameter.getValue());
    }

    @Test
    void compareTo_ordersByName() {
        Parameter a = new Parameter("a", "1");
        Parameter b = new Parameter("b", "2");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(new Parameter("a", "other")));
    }

    @Test
    void equals_sameNameAndValue_returnsTrue() {
        Parameter p1 = new Parameter("name", "value");
        Parameter p2 = new Parameter("name", "value");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        Parameter parameter = new Parameter("name", "value");
        assertEquals(parameter, parameter);
    }

    @Test
    void equals_differentType_returnsFalse() {
        Parameter parameter = new Parameter("name", "value");
        assertNotEquals(parameter, "name=value");
    }

    @Test
    void equals_differentValue_returnsFalse() {
        Parameter p1 = new Parameter("name", "value1");
        Parameter p2 = new Parameter("name", "value2");
        assertNotEquals(p1, p2);
    }

    @Test
    void toString_containsNameAndValue() {
        Parameter parameter = new Parameter("name", "value");
        assertEquals("[name=value]", parameter.toString());
    }

    @Test
    void encode_appendsNameAndEncodedValue() throws Exception {
        Parameter parameter = new Parameter("name", "a value");
        String encoded = parameter.encode(CharacterSet.UTF_8);
        assertEquals("name=a%20value", encoded);
    }

    @Test
    void encode_withNullValue_omitsEqualsSign() throws Exception {
        Parameter parameter = new Parameter("name", null);
        String encoded = parameter.encode(CharacterSet.UTF_8);
        assertEquals("name", encoded);
    }

    @Test
    void createSeries_returnsSeriesContainingThisParameter() {
        Parameter parameter = new Parameter("name", "value");
        Series<Parameter> series = parameter.createSeries();
        assertEquals(1, series.size());
        assertSame(parameter, series.get(0));
    }
}
