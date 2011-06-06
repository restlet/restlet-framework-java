package org.restlet.ext.html.internal;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.ext.html.MultiPartData;
import org.restlet.ext.html.MultiPartForm;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.test.RestletTestCase;

public class MultiPartFormTestCase extends RestletTestCase {

    public void testWrite() throws IOException {

        Representation file = new EmptyRepresentation();
        file.setMediaType(MediaType.APPLICATION_OCTET_STREAM);

        MultiPartForm form = new MultiPartForm(
                "-----------------------------1294919323195");
        form.getData().add(new MultiPartData("number", "5555555555"));
        form.getData().add(new MultiPartData("clip", "rickroll"));
        form.getData().add(new MultiPartData("upload_file", file));
        form.getData().add(new MultiPartData("tos", "agree"));

        String expected = "-----------------------------1294919323195\r\n"
                + "Content-Disposition: form-data; name=\"number\"\r\n"
                + "\r\n"
                + "5555555555\r\n"
                + "-----------------------------1294919323195\r\n"
                + "Content-Disposition: form-data; name=\"clip\"\r\n"
                + "\r\n"
                + "rickroll\r\n"
                + "-----------------------------1294919323195\r\n"
                + "Content-Disposition: form-data; name=\"upload_file\"; filename=\"\"\r\n"
                + "Content-Type: application/octet-stream\r\n" + "\r\n" + "\r\n"
                + "-----------------------------1294919323195\r\n"
                + "Content-Disposition: form-data; name=\"tos\"\r\n" + "\r\n"
                + "agree\r\n" + "-----------------------------1294919323195--\r\n";

        assertEquals("Value", expected, form.getText());
    }
}
