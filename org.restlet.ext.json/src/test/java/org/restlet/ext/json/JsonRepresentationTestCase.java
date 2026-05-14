/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link JsonRepresentation}. */
class JsonRepresentationTestCase {

    @Test
    void write_fromJsonObject_producesCorrectJson() throws Exception {
        JSONObject obj = new JSONObject("{\"key\":\"value\"}");
        String result = new JsonRepresentation(obj).getText();
        assertEquals("value", new JSONObject(result).getString("key"));
    }

    @Test
    void write_fromJsonArray_producesCorrectJson() throws Exception {
        JSONArray arr = new JSONArray("[1,2,3]");
        String result = new JsonRepresentation(arr).getText();
        assertEquals(3, new JSONArray(result).length());
    }

    @Test
    void write_fromJsonStringer_producesCorrectJson() throws Exception {
        JSONStringer stringer = new JSONStringer();
        stringer.object().key("key").value("value").endObject();
        String result = new JsonRepresentation(stringer).getText();
        assertEquals("{\"key\":\"value\"}", result);
    }

    @Test
    void write_fromString_producesOriginalText() throws Exception {
        String json = "{\"key\":\"value\"}";
        assertEquals(json, new JsonRepresentation(json).getText());
    }

    @Test
    void write_fromRepresentation_producesWrappedText() throws Exception {
        String json = "{\"key\":\"value\"}";
        assertEquals(json, new JsonRepresentation(new StringRepresentation(json)).getText());
    }

    @Test
    void write_fromMap_containsExpectedKey() throws Exception {
        String result = new JsonRepresentation(Map.of("key", "value")).getText();
        assertEquals("value", new JSONObject(result).getString("key"));
    }

    @Test
    void getJsonObject_fromJsonObjectValue_returnsValue() {
        JSONObject obj = new JSONObject("{\"key\":\"value\"}");
        assertEquals("value", new JsonRepresentation(obj).getJsonObject().getString("key"));
    }

    @Test
    void getJsonArray_fromJsonArrayValue_returnsValue() {
        JSONArray arr = new JSONArray("[1,2,3]");
        assertEquals(3, new JsonRepresentation(arr).getJsonArray().length());
    }

    @Test
    void getJsonObject_fromString_parsesCorrectly() {
        JSONObject result = new JsonRepresentation("{\"key\":\"value\"}").getJsonObject();
        assertEquals("value", result.getString("key"));
    }

    @Test
    void getJsonArray_fromString_parsesCorrectly() {
        JSONArray result = new JsonRepresentation("[1,2,3]").getJsonArray();
        assertEquals(3, result.length());
    }

    @Test
    void getJsonTokener_fromString_returnsUsableTokener() {
        JSONTokener tokener = new JsonRepresentation("{\"key\":\"value\"}").getJsonTokener();
        assertNotNull(tokener);
        assertEquals("value", new JSONObject(tokener).getString("key"));
    }

    @Test
    void indenting_defaultIsFalse() {
        assertFalse(new JsonRepresentation(new JSONObject()).isIndenting());
    }

    @Test
    void indentingSize_defaultIsThree() {
        assertEquals(3, new JsonRepresentation(new JSONObject()).getIndentingSize());
    }

    @Test
    void write_withIndentingOnJsonObject_producesFormattedOutput() throws Exception {
        // json.org adds a space after ":" when indenting — output differs from compact form
        String compact = new JsonRepresentation(new JSONObject("{\"key\":\"value\"}")).getText();
        JsonRepresentation jr = new JsonRepresentation(new JSONObject("{\"key\":\"value\"}"));
        jr.setIndenting(true);
        assertNotEquals(compact, jr.getText());
    }

    @Test
    void write_withIndentingOnJsonArray_producesFormattedOutput() throws Exception {
        JsonRepresentation jr = new JsonRepresentation(new JSONArray("[1,2,3]"));
        jr.setIndenting(true);
        assertTrue(jr.getText().contains("\n"));
    }

    @Test
    void setIndentingSize_changesIndentationWidth() throws Exception {
        // Arrays produce multi-line indented output; use one to verify the indent width
        JsonRepresentation jr = new JsonRepresentation(new JSONArray("[1,2,3]"));
        jr.setIndenting(true);
        jr.setIndentingSize(2);
        assertEquals(2, jr.getIndentingSize());
        String result = jr.getText();
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("  ")); // 2-space indent present
        assertFalse(result.contains("   ")); // 3-space indent absent
    }

    @Test
    void getSize_fromString_delegatesToWrappedRepresentation() {
        String json = "{\"key\":\"value\"}";
        assertEquals(json.length(), new JsonRepresentation(json).getSize());
    }

    @Test
    void getSize_fromJsonValue_returnsUnknownSize() {
        assertEquals(
                Representation.UNKNOWN_SIZE, new JsonRepresentation(new JSONObject()).getSize());
    }
}
