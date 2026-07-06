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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

class FormUtilsTestCase {

    @Test
    void create_nullName_returnsNull() {
        assertNull(FormUtils.create(null, "value", false, CharacterSet.UTF_8));
    }

    @Test
    void create_notDecoded_usesRawValues() {
        Parameter param = FormUtils.create("a%20b", "c%20d", false, CharacterSet.UTF_8);
        assertEquals("a%20b", param.getName());
        assertEquals("c%20d", param.getValue());
    }

    @Test
    void create_decoded_decodesNameAndValue() {
        Parameter param = FormUtils.create("a%20b", "c%20d", true, CharacterSet.UTF_8);
        assertEquals("a b", param.getName());
        assertEquals("c d", param.getValue());
    }

    @Test
    void create_nullValue_producesNullValuedParameter() {
        Parameter param = FormUtils.create("name", null, false, CharacterSet.UTF_8);
        assertEquals("name", param.getName());
        assertNull(param.getValue());
    }

    @Test
    void getFirstParameter_fromQueryString_returnsMatch() throws IOException {
        Parameter param =
                FormUtils.getFirstParameter("a=1&b=2", "b", CharacterSet.UTF_8, '&', true);
        assertEquals("2", param.getValue());
    }

    @Test
    void getFirstParameter_fromRepresentation_returnsMatch() throws IOException {
        Representation post = new StringRepresentation("a=1&b=2", MediaType.APPLICATION_WWW_FORM);
        Parameter param = FormUtils.getFirstParameter(post, "a");
        assertEquals("1", param.getValue());
    }

    @Test
    void getParameter_singleMatch_returnsParameterValue() throws IOException {
        Object result = FormUtils.getParameter("a=1&b=2", "a", CharacterSet.UTF_8, '&', true);
        assertEquals("1", result);
    }

    @Test
    void getParameter_multipleMatches_returnsListOfValues() throws IOException {
        Object result = FormUtils.getParameter("a=1&a=2", "a", CharacterSet.UTF_8, '&', true);
        assertInstanceOf(List.class, result);
        assertEquals(2, ((java.util.List<?>) result).size());
    }

    @Test
    void getParameters_fromQueryString_populatesMap() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("a", null);
        params.put("b", null);
        FormUtils.getParameters("a=1&b=2", params, CharacterSet.UTF_8, '&', true);
        assertEquals("1", params.get("a"));
        assertEquals("2", params.get("b"));
    }

    @Test
    void getParameters_fromRepresentation_populatesMap() throws IOException {
        Representation post = new StringRepresentation("a=1", MediaType.APPLICATION_WWW_FORM);
        Map<String, Object> params = new HashMap<>();
        params.put("a", null);
        FormUtils.getParameters(post, params);
        assertEquals("1", params.get("a"));
    }

    @Test
    void isParameterFound_matchingParameter_returnsTrue() {
        Parameter searched = new Parameter("charset", "UTF-8");
        MediaType typeWithParam = new MediaType("application/json;charset=UTF-8");
        assertTrue(FormUtils.isParameterFound(searched, typeWithParam));
    }

    @Test
    void isParameterFound_nonMatchingParameter_returnsFalse() {
        Parameter searched = new Parameter("charset", "ISO-8859-1");
        MediaType typeWithParam = new MediaType("application/json;charset=UTF-8");
        assertFalse(FormUtils.isParameterFound(searched, typeWithParam));
    }

    @Test
    void parse_representationOverload_addsParametersToForm() {
        Form form = new Form();
        Representation post = new StringRepresentation("a=1", MediaType.APPLICATION_WWW_FORM);
        FormUtils.parse(form, post, true);
        assertEquals("1", form.getFirstValue("a"));
    }

    @Test
    void parse_nullRepresentation_leavesFormUnchanged() {
        Form form = new Form();
        FormUtils.parse(form, (Representation) null, true);
        assertTrue(form.isEmpty());
    }

    @Test
    void parse_stringOverload_addsParametersToForm() {
        Form form = new Form();
        FormUtils.parse(form, "a=1&b=2", CharacterSet.UTF_8, true, '&');
        assertEquals("1", form.getFirstValue("a"));
        assertEquals("2", form.getFirstValue("b"));
    }

    @Test
    void parse_emptyString_leavesFormUnchanged() {
        Form form = new Form();
        FormUtils.parse(form, "", CharacterSet.UTF_8, true, '&');
        assertTrue(form.isEmpty());
    }
}
