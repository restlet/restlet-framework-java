package org.restlet.test.ext.json;

import junit.framework.Assert;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.json.JsonpRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Writer;

import static org.restlet.data.Status.SUCCESS_OK;

public class JsonpRepresentationTestCase extends RestletTestCase {

    public static final String CALLBACK          = "callback";
    public static final String JSON_SAMPLE       = "{'attribute': value}";
    public static final String JSONP_STATUS_BODY = "({status:,body:});";

    public void testGetSizeJson() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(CALLBACK, SUCCESS_OK, new JsonRepresentation(JSON_SAMPLE));

        long actual = jsonpRepresentation.getSize();

        long expected = JSON_SAMPLE.length() + Integer.toString(SUCCESS_OK.getCode()).length() + CALLBACK.length() + JSONP_STATUS_BODY.length();

        Assert.assertEquals(expected, actual);
    }

    public void testGetSize_with_text_is_UNKNOWN_SIZE() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(CALLBACK, SUCCESS_OK, new StringRepresentation(JSON_SAMPLE, MediaType.TEXT_HTML));

        long actual = jsonpRepresentation.getSize();

        long expected = Representation.UNKNOWN_SIZE;

        Assert.assertEquals(expected, actual);
    }

    public void testWrite() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(CALLBACK, SUCCESS_OK, new JsonRepresentation(JSON_SAMPLE));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonpRepresentation.write(out);

        String expected = "callback({status:200,body:{'attribute': value}});";

        Assert.assertEquals(expected, out.toString());
    }

    // with a text representation, apostrophe are escaped and text is embeded between 2 apostrophe
    public void testWrite_with_text_then_apostrophe_are_escaped() throws Exception {
        JsonpRepresentation jsonpRepresentation = new JsonpRepresentation(CALLBACK, SUCCESS_OK, new StringRepresentation("whatever'with'apostrophe'", MediaType.TEXT_HTML));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonpRepresentation.write(out);

        String expected = "callback({status:200,body:'whatever\\\'with\\\'apostrophe\\\''});";

        Assert.assertEquals(expected, out.toString());
    }

}
