/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import org.junit.jupiter.api.Test;

/**
 * Test case for the {@link FormDataSet} class in multipart mode.
 * 
 * @author Jerome Louvel
 */
public class MultiPartFormTestCase {

    /*
    TODO restore test about support of multi part representations.


    @Test
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
        assertEquals(expected, form.getText());
    }
     */

    /**
     * Tests the multipart content-type.
     */
    @Test
    public void testContentType() {
        /*
TODO restore test of Form class
        FormDataSet form = null;

        form = new FormDataSet();
        form.setMultipart(true);
        assertTrue(form.getMediaType().equals(MediaType.MULTIPART_FORM_DATA,
                true));

        form = new FormDataSet("test");
        assertTrue(form.isMultipart());
        assertTrue(form.getMediaType().equals(MediaType.MULTIPART_FORM_DATA,
                true));
        assertEquals(
                form.getMediaType().getParameters().getFirstValue("boundary"),
                "test");
        form = new FormDataSet();

        form.setMultipartBoundary("test2");
        assertTrue(form.isMultipart());
        assertTrue(form.getMediaType().equals(MediaType.MULTIPART_FORM_DATA,
                true));
        assertEquals(
                form.getMediaType().getParameters().getFirstValue("boundary"),
                "test2");

         */
    }
}
