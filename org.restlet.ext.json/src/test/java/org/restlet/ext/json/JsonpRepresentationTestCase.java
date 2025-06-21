/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.json;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.restlet.data.Status.SUCCESS_OK;

/**
 * Test case for the {@link JsonpRepresentation} class.
 * 
 * @author Cyril Lakech
 */
public class JsonpRepresentationTestCase {

    public static final String CALLBACK = "callback";

    public static final String JSON_SAMPLE = "{\"attribute\": value}";

    public static final String JSONP_STATUS_BODY = "({\"status\":,\"body\":});";

    @Test
    public void testGetSizeJson() {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new JsonRepresentation(JSON_SAMPLE));

        long actual = jsonpRepresentation.getSize();

        long expected = JSON_SAMPLE.length()
                + Integer.toString(SUCCESS_OK.getCode()).length()
                + CALLBACK.length() + JSONP_STATUS_BODY.length();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetSize_with_text_is_UNKNOWN_SIZE() {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new StringRepresentation(JSON_SAMPLE,
                        MediaType.TEXT_HTML));

        long actual = jsonpRepresentation.getSize();

        long expected = Representation.UNKNOWN_SIZE;

        assertEquals(expected, actual);
    }

    @Test
    public void testWrite() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new JsonRepresentation(JSON_SAMPLE));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonpRepresentation.write(out);

        String expected = "callback({\"status\":200,\"body\":{\"attribute\": value}});";

        assertEquals(expected, out.toString());
    }

    // with a text representation, apostrophes are escaped and the text is embedded
    // between 2 apostrophes
    @Test
    public void testWrite_with_text_then_apostrophe_are_escaped()
            throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new StringRepresentation(
                        "whatever\"with\"apostrophe", MediaType.TEXT_HTML));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonpRepresentation.write(out);

        String expected = "callback({\"status\":200,\"body\":\"whatever\\\"with\\\"apostrophe\"});";

        assertEquals(expected, out.toString());
    }

}
