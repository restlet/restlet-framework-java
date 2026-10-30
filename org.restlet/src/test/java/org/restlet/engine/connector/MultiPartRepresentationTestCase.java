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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.MultiPartRepresentation;
import org.restlet.representation.MultiPartRepresentation.Part;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

/**
 * Test case for the {@link MultiPartRepresentation} class in multipart mode.
 *
 * @author Jerome Louvel
 */
class MultiPartRepresentationTestCase {

    private static final String BOUNDARY = "-----------------------------1294919323195";

    private static final String ENTITY =
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
                    .replace("%s", BOUNDARY);

    @TempDir Path tempDir;

    @Test
    void testWriteFromParts() throws IOException {
        Path textFilePath = Files.createTempFile(tempDir, "multiPart", "");
        Files.writeString(textFilePath, "this is the content of the file");
        Part filePart =
                MultiPartRepresentation.createPart(
                        "icon",
                        "text.txt",
                        new FileRepresentation(textFilePath.toFile(), MediaType.TEXT_PLAIN));
        Part fieldPart = new Part("field", null, new StringRepresentation("foo"));

        MultiPartRepresentation rep = new MultiPartRepresentation(fieldPart, filePart);
        rep.setBoundary(BOUNDARY);

        assertEquals(ENTITY, rep.getText());
    }

    /** Tests the multipart content-type. */
    @Test
    void testContentType() {
        Part part = new Part("field", null, new StringRepresentation("foo"));
        MultiPartRepresentation rep = new MultiPartRepresentation("myInitialBoundary", part);
        rep.setBoundary("myActualBoundary");
        assertEquals(
                "multipart/form-data; boundary=myActualBoundary", rep.getMediaType().toString());
    }

    @Test
    void setBoundaryKeepsTheOtherParameters() {
        assertEquals(
                "multipart/mixed; deferSpec=20220824; boundary=b",
                MultiPartRepresentation.setBoundary(
                                MediaType.valueOf("multipart/mixed; deferSpec=20220824"), "b")
                        .toString());
        assertEquals(
                "multipart/mixed; deferSpec=20220824; boundary=b",
                MultiPartRepresentation.setBoundary(
                                MediaType.valueOf(
                                        "multipart/mixed; boundary=a; deferSpec=20220824"),
                                "b")
                        .toString());
        assertEquals(
                "multipart/related; type=\"a;boundary=x\"; boundary=b",
                MultiPartRepresentation.setBoundary(
                                MediaType.valueOf(
                                        "multipart/related; Boundary=a; type=\"a;boundary=x\""),
                                "b")
                        .toString());
        // As parsed from a Content-Type header
        Series<Parameter> parameters = new Series<>(Parameter.class);
        parameters.add("type", "application/xml");
        parameters.add("BOUNDARY", "a");
        assertEquals(
                "multipart/related; type=\"application/xml\"; boundary=b",
                MultiPartRepresentation.setBoundary(
                                new MediaType("multipart/related", parameters), "b")
                        .toString());
        assertNull(MultiPartRepresentation.setBoundary(null, "b"));

        MultiPartRepresentation rep =
                new MultiPartRepresentation(
                        MediaType.valueOf("multipart/mixed; deferSpec=20220824"),
                        "b",
                        List.of(new Part("a", null, new StringRepresentation("b"))));
        assertEquals(
                "multipart/mixed; deferSpec=20220824; boundary=b", rep.getMediaType().toString());
        rep.setBoundary("c");
        assertEquals(
                "multipart/mixed; deferSpec=20220824; boundary=c", rep.getMediaType().toString());
    }

    @Test
    void testRandomBoundary() {
        MultiPartRepresentation rep =
                new MultiPartRepresentation(new Part("a", null, new StringRepresentation("b")));
        assertEquals(rep.getBoundary(), MultiPartRepresentation.getBoundary(rep.getMediaType()));
    }

    @Test
    void testParseIntoParts() throws IOException {
        StringRepresentation multipartEntity = new StringRepresentation(ENTITY);
        multipartEntity.setMediaType(
                MediaType.valueOf("multipart/form-data; boundary=" + BOUNDARY));
        MultiPartRepresentation rep = new MultiPartRepresentation(multipartEntity, tempDir);

        assertEquals(2, rep.getParts().size());

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

    @Test
    void testRoundTripWithHeadersAndLargePart() throws IOException {
        String large = "x\r\n--almost-a-boundary\r".repeat(10_000);
        StringRepresentation typed = new StringRepresentation("{\"a\":1}");
        Part jsonPart =
                new Part(
                        "data",
                        null,
                        new org.restlet.util.Series<>(org.restlet.data.Header.class),
                        typed);
        jsonPart.getHeaders().add("Content-Type", "application/json");
        Part largePart = new Part("big", "big.txt", new StringRepresentation(large));

        MultiPartRepresentation generated =
                new MultiPartRepresentation(List.of(jsonPart, largePart));
        StringRepresentation entity = new StringRepresentation(generated.getText());
        entity.setMediaType(generated.getMediaType());

        MultiPartRepresentation parsed = new MultiPartRepresentation(entity, tempDir);
        assertEquals(2, parsed.getParts().size());

        Part p1 = parsed.getParts().getFirst();
        assertEquals("data", p1.getName());
        assertEquals("{\"a\":1}", p1.getContentAsString(null));
        assertEquals(MediaType.APPLICATION_JSON, p1.getContent().getMediaType());

        Part p2 = parsed.getParts().get(1);
        assertEquals("big.txt", p2.getFileName());
        assertEquals(large.length(), p2.getLength());
        assertInstanceOf(FileRepresentation.class, p2.getContent());
        assertEquals(large, p2.getContentAsString(null));
    }

    @Test
    void testParseTruncatedEntity() {
        String truncated = ENTITY.substring(0, ENTITY.indexOf("this is"));
        StringRepresentation entity = new StringRepresentation(truncated);
        entity.setMediaType(MediaType.valueOf("multipart/form-data; boundary=" + BOUNDARY));
        assertThrows(IOException.class, () -> new MultiPartRepresentation(entity));
    }

    @Test
    void testParseRequiresBoundary() {
        StringRepresentation entity = new StringRepresentation(ENTITY);
        entity.setMediaType(MediaType.MULTIPART_FORM_DATA);
        assertThrows(IllegalArgumentException.class, () -> new MultiPartRepresentation(entity));
    }
}
