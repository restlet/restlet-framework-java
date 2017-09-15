/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.json;

import static org.restlet.data.Status.SUCCESS_OK;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.json.JsonpRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the {@link JsonpRepresentation} class.
 * 
 * @author Cyril Lakech
 */
public class JsonpRepresentationTestCase extends RestletTestCase {

    public static final String CALLBACK = "callback";

    public static final String JSON_SAMPLE = "{\"attribute\": value}";

    public static final String JSONP_STATUS_BODY = "({\"status\":,\"body\":});";

    public void testGetSizeJson() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new JsonRepresentation(JSON_SAMPLE));

        long actual = jsonpRepresentation.getSize();

        long expected = JSON_SAMPLE.length()
                + Integer.toString(SUCCESS_OK.getCode()).length()
                + CALLBACK.length() + JSONP_STATUS_BODY.length();

        Assert.assertEquals(expected, actual);
    }

    public void testGetSize_with_text_is_UNKNOWN_SIZE() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new StringRepresentation(JSON_SAMPLE,
                        MediaType.TEXT_HTML));

        long actual = jsonpRepresentation.getSize();

        long expected = Representation.UNKNOWN_SIZE;

        Assert.assertEquals(expected, actual);
    }

    public void testWrite() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new JsonRepresentation(JSON_SAMPLE));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonpRepresentation.write(out);

        String expected = "callback({\"status\":200,\"body\":{\"attribute\": value}});";

        Assert.assertEquals(expected, out.toString());
    }

    // with a text representation, apostrophe are escaped and text is embedded
    // between 2 apostrophe
    public void testWrite_with_text_then_apostrophe_are_escaped()
            throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(
                CALLBACK, SUCCESS_OK, new StringRepresentation(
                        "whatever\"with\"apostrophe", MediaType.TEXT_HTML));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonpRepresentation.write(out);

        String expected = "callback({\"status\":200,\"body\":\"whatever\\\"with\\\"apostrophe\"});";

        Assert.assertEquals(expected, out.toString());
    }

}
