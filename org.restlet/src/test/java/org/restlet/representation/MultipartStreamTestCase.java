/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

class MultipartStreamTestCase {

    private static Series<Header> headers(String... nameValues) {
        Series<Header> headers = new Series<>(Header.class);
        for (int i = 0; i < nameValues.length; i += 2) {
            headers.add(new Header(nameValues[i], nameValues[i + 1]));
        }
        return headers;
    }

    private static byte[] write(MultipartStreamRepresentation representation) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);
        return out.toByteArray();
    }

    private static MultipartStreamReader reader(String content, String boundary, long max) {
        return new MultipartStreamReader(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), boundary, max);
    }

    @Test
    void writesFlushedPartsFollowedByTheirDelimiter() throws Exception {
        MultipartStreamRepresentation representation =
                new MultipartStreamRepresentation(
                        MediaType.MULTIPART_MIXED,
                        "b",
                        sink -> {
                            sink.send(headers("Content-Type", "application/json"), "{}".getBytes());
                            sink.send(null, "two".getBytes());
                        });
        assertEquals("b", representation.getBoundary());
        assertEquals("b", MultiPartRepresentation.getBoundary(representation.getMediaType()));
        assertTrue(representation.isTransient());
        assertEquals(
                "\r\n--b\r\nContent-Type: application/json\r\n\r\n{}\r\n--b\r\n\r\ntwo\r\n--b--\r\n",
                new String(write(representation), StandardCharsets.UTF_8));

        assertEquals(
                "\r\n--b--\r\n",
                new String(
                        write(
                                new MultipartStreamRepresentation(
                                        MediaType.MULTIPART_MIXED, "b", sink -> {})),
                        StandardCharsets.UTF_8));
    }

    @Test
    void roundTripsBinaryContentResemblingTheDelimiter() throws Exception {
        byte[] tricky =
                "x\r\n--bo\r\n-\r\n--boundar\r\n--boundarX\r".getBytes(StandardCharsets.UTF_8);
        byte[] binary = new byte[256];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) i;
        }
        MultipartStreamRepresentation representation =
                new MultipartStreamRepresentation(
                        MediaType.MULTIPART_MIXED,
                        "boundary",
                        sink -> {
                            sink.send(headers("X-Index", "1"), tricky);
                            sink.send(headers("Content-Type", "application/octet-stream"), binary);
                            sink.send(null, new byte[0]);
                        });
        try (MultipartStreamReader reader =
                new MultipartStreamReader(
                        new ByteArrayInputStream(write(representation)), "boundary", 1024)) {
            MultiPartRepresentation.Part first = reader.next();
            assertEquals("1", first.getHeaders().getFirstValue("X-Index"));
            assertArrayEquals(tricky, first.getContent().getStream().readAllBytes());

            MultiPartRepresentation.Part second = reader.next();
            assertEquals(MediaType.APPLICATION_OCTET_STREAM, second.getContent().getMediaType());
            assertArrayEquals(binary, second.getContent().getStream().readAllBytes());

            assertEquals(0, reader.next().getContent().getStream().readAllBytes().length);
            assertFalse(reader.hasNext());
            assertNull(reader.readPart());
            assertThrows(NoSuchElementException.class, reader::next);
        }
    }

    @Test
    void ignoresPreambleEpilogueAndTransportPadding() throws Exception {
        String content =
                "preamble\r\n--b \t\r\nA: 1\r\n\r\nfirst\r\n--b\r\n\r\nsecond\r\n--b--\r\nepilogue";
        try (MultipartStreamReader reader = reader(content, "b", 100)) {
            MultiPartRepresentation.Part first = reader.readPart();
            assertEquals("1", first.getHeaders().getFirstValue("A"));
            assertEquals("first", first.getContentAsString(StandardCharsets.UTF_8));
            assertEquals("second", reader.readPart().getContentAsString(StandardCharsets.UTF_8));
            assertNull(reader.readPart());
        }
        try (MultipartStreamReader reader = reader("--b\r\n\r\nx\r\n--b--", "b", 100)) {
            assertEquals("x", reader.readPart().getContentAsString(StandardCharsets.UTF_8));
            assertNull(reader.readPart());
        }
    }

    @Test
    void rejectsMalformedAndOversizedStreams() {
        assertThrows(IOException.class, () -> reader("no delimiter", "b", 100).readPart());
        assertThrows(IOException.class, () -> reader("--bX\r\n", "b", 100).readPart());
        assertThrows(IOException.class, () -> reader("--b\r\nA: 1", "b", 100).readPart());
        assertThrows(IOException.class, () -> reader("--b\r\nbad\r\n\r\n", "b", 100).readPart());
        assertThrows(IOException.class, () -> reader("--b\r\n\r\ntruncated", "b", 100).readPart());
        assertThrows(
                IOException.class,
                () -> reader("--b\r\n\r\n" + "x".repeat(200) + "\r\n--b--", "b", 100).readPart());
        assertThrows(
                IOException.class,
                () -> reader("x".repeat(200) + "\r\n--b--", "b", 100).readPart());
        assertThrows(
                IOException.class,
                () ->
                        reader("--b\r\nA: " + "x".repeat(200) + "\r\n\r\n\r\n--b--", "b", 100)
                                .readPart());
        assertThrows(
                UncheckedIOException.class,
                () -> reader("--b\r\n\r\ntruncated", "b", 100).hasNext());
        assertThrows(
                IllegalArgumentException.class,
                () -> new MultipartStreamReader(new ByteArrayInputStream(new byte[0]), "", 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MultipartStreamReader(new ByteArrayInputStream(new byte[0]), "b", 0));
        assertThrows(
                IOException.class,
                () ->
                        new MultipartStreamReader(
                                new StringRepresentation("x", MediaType.MULTIPART_MIXED), 1));
    }

    @Test
    void writerFailuresLeaveTheStreamTruncated() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MultipartStreamRepresentation.PartSink[] sinks = {null};
        MultipartStreamRepresentation representation =
                new MultipartStreamRepresentation(
                        MediaType.MULTIPART_MIXED,
                        "b",
                        sink -> {
                            sinks[0] = sink;
                            sink.send(null, "one".getBytes());
                            throw new IOException("failed");
                        });
        IOException failure = assertThrows(IOException.class, () -> representation.write(out));
        assertEquals("failed", failure.getMessage());
        assertEquals("\r\n--b\r\n\r\none\r\n--b", out.toString(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> sinks[0].send(null, new byte[0]));
        assertThrows(
                UncheckedIOException.class,
                () ->
                        new MultipartStreamReader(
                                        new ByteArrayInputStream(out.toByteArray()), "b", 100)
                                .forEachRemaining(part -> {}));

        ByteArrayOutputStream runtime = new ByteArrayOutputStream();
        assertThrows(
                IllegalStateException.class,
                () ->
                        new MultipartStreamRepresentation(
                                        sink -> {
                                            throw new IllegalStateException();
                                        })
                                .write(runtime));
        assertFalse(runtime.toString(StandardCharsets.UTF_8).contains("--\r\n"));
    }

    @Test
    void validatesTheEntityBeforeAcquiringItsStream() {
        AtomicBoolean acquired = new AtomicBoolean();
        Function<MediaType, Representation> entity =
                mediaType ->
                        new StringRepresentation("", mediaType) {
                            @Override
                            public InputStream getStream() throws IOException {
                                acquired.set(true);
                                return super.getStream();
                            }
                        };
        assertThrows(
                IOException.class,
                () -> new MultipartStreamReader(entity.apply(MediaType.MULTIPART_MIXED), 1));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MultipartStreamReader(
                                entity.apply(
                                        new MediaType(MediaType.MULTIPART_MIXED, "boundary", "b")),
                                0));
        assertFalse(acquired.get());
    }

    @Test
    void acceptsCaseInsensitiveBoundaryAndLargeLimits() throws Exception {
        Representation entity =
                new StringRepresentation(
                        "--b\r\n\r\nx\r\n--b--",
                        new MediaType(MediaType.MULTIPART_MIXED, "Boundary", "b"));
        try (MultipartStreamReader reader = new MultipartStreamReader(entity, Long.MAX_VALUE)) {
            assertEquals("x", reader.readPart().getContentAsString(StandardCharsets.UTF_8));
            assertNull(reader.readPart());
        }

        // The opening delimiter is longer than the limit, but isn't part of the preamble.
        try (MultipartStreamReader reader =
                reader("--boundary\r\n\r\nx\r\n--boundary--", "boundary", 8)) {
            assertEquals("x", reader.readPart().getContentAsString(StandardCharsets.UTF_8));
            assertNull(reader.readPart());
        }
    }

    @Test
    void validatesTheRepresentation() throws Exception {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MultipartStreamRepresentation(MediaType.TEXT_PLAIN, null, sink -> {}));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MultipartStreamRepresentation(
                                MediaType.MULTIPART_MIXED, "a\r\nb", sink -> {}));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        write(
                                new MultipartStreamRepresentation(
                                        sink -> sink.send(headers("A", "1\r\n"), new byte[0]))));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        write(
                                new MultipartStreamRepresentation(
                                        MediaType.MULTIPART_MIXED,
                                        "-",
                                        sink -> sink.send(null, "a\r\n---b".getBytes()))));
        assertThrows(
                IOException.class,
                () ->
                        write(
                                new MultipartStreamRepresentation(
                                        sink -> {
                                            sink.close();
                                            sink.send(null, new byte[0]);
                                        })));
        Series<Parameter> original = new Series<>(Parameter.class);
        original.add(new Parameter("deferSpec", "20220824"));
        original.add(new Parameter("boundary", "old"));
        MultipartStreamRepresentation parameters =
                new MultipartStreamRepresentation(
                        new MediaType(MediaType.MULTIPART_MIXED, original), "-", sink -> {});
        assertEquals(
                "20220824", parameters.getMediaType().getParameters().getFirstValue("deferSpec"));
        assertEquals("-", MultiPartRepresentation.getBoundary(parameters.getMediaType()));
        assertEquals(2, parameters.getMediaType().getParameters().size());
        assertEquals(
                "multipart/mixed; deferSpec=20220824; boundary=-",
                parameters.getMediaType().getName());
        // Content starting with the boundary would forge a part after the header line break.
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        write(
                                new MultipartStreamRepresentation(
                                        MediaType.MULTIPART_MIXED,
                                        "b",
                                        sink ->
                                                sink.send(
                                                        null,
                                                        "--b\r\nA: 1\r\n\r\nforged".getBytes()))));
        assertEquals(
                "\r\n--bb\r\n\r\n--b\r\n--bb--\r\n",
                new String(
                        write(
                                new MultipartStreamRepresentation(
                                        MediaType.MULTIPART_MIXED,
                                        "bb",
                                        sink -> sink.send(null, "--b".getBytes()))),
                        StandardCharsets.UTF_8));
        MultipartStreamRepresentation generated = new MultipartStreamRepresentation(sink -> {});
        assertTrue(MediaType.MULTIPART_MIXED.equals(generated.getMediaType(), true));
        assertFalse(generated.getBoundary().isEmpty());
    }

    @Test
    void partsArriveBeforeTheStreamEnds() throws Exception {
        CountDownLatch firstSent = new CountDownLatch(1);
        CountDownLatch continueWriting = new CountDownLatch(1);
        Restlet restlet =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(
                                new MultipartStreamRepresentation(
                                        sink -> {
                                            sink.send(
                                                    headers("Content-Type", "text/plain"),
                                                    "first".getBytes());
                                            firstSent.countDown();
                                            try {
                                                if (!continueWriting.await(5, TimeUnit.SECONDS)) {
                                                    throw new IOException("Timed out");
                                                }
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                throw new IOException(e);
                                            }
                                            sink.send(null, "second".getBytes());
                                        }));
                    }
                };
        Server server = new Server(Protocol.HTTP, 0, restlet);
        server.start();
        Client client = new Client(Protocol.HTTP);
        client.start();
        try {
            Response response =
                    client.handle(
                            new Request(Method.GET, "http://localhost:" + server.getActualPort()));
            assertTrue(MediaType.MULTIPART_MIXED.equals(response.getEntity().getMediaType(), true));
            try (MultipartStreamReader reader =
                    new MultipartStreamReader(response.getEntity(), 1024)) {
                assertTrue(firstSent.await(5, TimeUnit.SECONDS));
                MultiPartRepresentation.Part first = reader.readPart();
                assertEquals("first", first.getContentAsString(StandardCharsets.UTF_8));
                assertTrue(MediaType.TEXT_PLAIN.equals(first.getContent().getMediaType(), true));
                continueWriting.countDown();
                assertEquals(
                        "second", reader.readPart().getContentAsString(StandardCharsets.UTF_8));
                assertNull(reader.readPart());
            }
        } finally {
            continueWriting.countDown();
            client.stop();
            server.stop();
        }
    }
}
