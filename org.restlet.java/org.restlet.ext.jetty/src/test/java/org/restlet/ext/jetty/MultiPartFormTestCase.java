/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MultiPart;
import org.junit.jupiter.api.Test;

/**
 * Test case for the {@link MultiPartFormDataRepresentation} class in multipart
 * mode.
 * 
 * @author Jerome Louvel
 */
public class MultiPartFormTestCase {

    @Test
    public void testWriteFromParts() throws IOException {
        Path textFilePath = Files.createTempFile("multiPart", "");
        Files.write(textFilePath, "this is the content of the file"
                .getBytes(StandardCharsets.UTF_8));
        MultiPart.PathPart filePart = new MultiPart.PathPart("icon", "text.txt",
                HttpFields.EMPTY, textFilePath);

        MultiPart.ContentSourcePart contentSourcePart = new MultiPart.ContentSourcePart(
                "field", null, HttpFields.EMPTY,
                new StringRequestContent("foo"));

        final String boundary = "-----------------------------1294919323195";

        MultiPartFormDataRepresentation rep = new MultiPartFormDataRepresentation(
                contentSourcePart, filePart);
        rep.setBoundary(boundary);

        final String expected = """
                --%s\r
                Content-Disposition: form-data; name="field"\r
                \r
                foo\r
                --%s\r
                Content-Disposition: form-data; name="icon"; filename="text.txt"\r
                \r
                this is the content of the file\r
                --%s--\r
                """
                .replace("%s", boundary);
        assertEquals(expected, rep.getText());
    }

    /**
     * Tests the multipart content-type.
     */
    @Test
    public void testContentType() {
        MultiPart.ContentSourcePart contentSourcePart = new MultiPart.ContentSourcePart(
                "field", null, HttpFields.EMPTY,
                new StringRequestContent("foo"));
        MultiPartFormDataRepresentation rep = new MultiPartFormDataRepresentation(
                "myInitialBoundary", contentSourcePart);
        rep.setBoundary("myActualBoundary");
        assertEquals("multipart/form-data; boundary=myActualBoundary",
                rep.getMediaType().toString());
    }
}
