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

        form.write(System.out);
    }
}
