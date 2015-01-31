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

package org.restlet.test.ext.html;

import java.io.IOException;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the {@link FormDataSet} class in multipart mode.
 * 
 * @author Jerome Louvel
 */
public class MultiPartFormTestCase extends RestletTestCase {

    public void testWrite() throws IOException {

        // considered as a simple field entry
        Representation textFile = new EmptyRepresentation();
        textFile.setMediaType(MediaType.TEXT_PLAIN);

        // considered as a file
        Representation textFile2 = new StringRepresentation("test",
                MediaType.TEXT_PLAIN);
        textFile2.setDisposition(new Disposition());
        textFile2.getDisposition().setFilename("test.txt");

        // considered as a file
        Representation file = new EmptyRepresentation();
        file.setMediaType(MediaType.APPLICATION_OCTET_STREAM);

        String boundary = "-----------------------------1294919323195";
        String boundaryBis = "--" + boundary;
        String expected;

        FormDataSet form = new FormDataSet(boundary);
        form.getEntries().add(new FormData("number", "5555555555"));
        form.getEntries().add(new FormData("clip", "rickroll"));
        form.getEntries().add(new FormData("upload_file", file));
        form.getEntries().add(new FormData("upload_textfile", textFile));
        form.getEntries().add(new FormData("upload_textfile2", textFile2));
        form.getEntries().add(new FormData("tos", "agree"));

        expected = boundaryBis
                + "\r\n"
                + "Content-Disposition: form-data; name=\"number\"\r\n"
                + "\r\n"
                + "5555555555\r\n"
                + boundaryBis
                + "\r\n"
                + "Content-Disposition: form-data; name=\"clip\"\r\n"
                + "\r\n"
                + "rickroll\r\n"
                + boundaryBis
                + "\r\n"
                + "Content-Disposition: form-data; name=\"upload_file\"; filename=\"\"\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "\r\n"
                + "\r\n"
                + boundaryBis
                + "\r\n"
                + "Content-Disposition: form-data; name=\"upload_textfile\"\r\n"
                + "\r\n"
                + "\r\n"
                + boundaryBis
                + "\r\n"
                + "Content-Disposition: form-data; name=\"upload_textfile2\"; filename=\"test.txt\"\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n" + "\r\n"
                + "test" + "\r\n" + boundaryBis + "\r\n"
                + "Content-Disposition: form-data; name=\"tos\"\r\n" + "\r\n"
                + "agree\r\n" + boundaryBis + "--\r\n";
        assertEquals("Value 1", expected, form.getText());
    }
}
