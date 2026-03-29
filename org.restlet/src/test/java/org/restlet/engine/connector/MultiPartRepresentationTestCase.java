/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPart.Part;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.MultiPartRepresentation;
import org.restlet.representation.StringRepresentation;

/**
 * Test case for the {@link MultiPartRepresentation} class in multipart mode.
 *
 * @author Jerome Louvel
 */
class MultiPartRepresentationTestCase {

    @Test
    void testWriteFromParts() throws IOException {
        Path textFilePath = Files.createTempFile("multiPart", "");
        Files.write(
                textFilePath, "this is the content of the file".getBytes(StandardCharsets.UTF_8));
        MultiPart.PathPart filePart =
                new MultiPart.PathPart("icon", "text.txt", HttpFields.EMPTY, textFilePath);

        MultiPart.ContentSourcePart contentSourcePart =
                new MultiPart.ContentSourcePart(
                        "field", null, HttpFields.EMPTY, new StringRequestContent("foo"));

        final String boundary = "-----------------------------1294919323195";

        MultiPartRepresentation rep = new MultiPartRepresentation(contentSourcePart, filePart);
        rep.setBoundary(boundary);

        final String expected =
                """
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

    /** Tests the multipart content-type. */
    @Test
    void testContentType() {
        MultiPart.ContentSourcePart contentSourcePart =
                new MultiPart.ContentSourcePart(
                        "field", null, HttpFields.EMPTY, new StringRequestContent("foo"));
        MultiPartRepresentation rep =
                new MultiPartRepresentation("myInitialBoundary", contentSourcePart);
        rep.setBoundary("myActualBoundary");
        assertEquals(
                "multipart/form-data; boundary=myActualBoundary", rep.getMediaType().toString());
    }

    @Test
    void testParseIntoParts() throws IOException {
        final String boundary = "-----------------------------1294919323195";
        final String multipartEntityContent =
                """
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

        StringRepresentation multipartEntity = new StringRepresentation(multipartEntityContent);
        multipartEntity.setMediaType(
                MediaType.valueOf("multipart/form-data; boundary=" + boundary));
        Path tempDir = Files.createTempDirectory("multipartRepresentationTestCase");
        MultiPartRepresentation rep = new MultiPartRepresentation(multipartEntity, tempDir);

        Part part1 = rep.getParts().getFirst();
        assertEquals("field", part1.getName());
        assertNull(part1.getFileName());
        assertEquals(3, part1.getLength());
        assertEquals("foo", part1.getContentAsString(null));

        Part part2 = rep.getParts().get(1);
        assertEquals("icon", part2.getName());
        assertEquals("text.txt", part2.getFileName());
        assertEquals(31, part2.getLength());
        assertEquals("this is the content of the file", part2.getContentAsString(null));
    }
}
