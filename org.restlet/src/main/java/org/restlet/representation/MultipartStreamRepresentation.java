/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * A streaming {@code multipart/*} representation (RFC 2046) whose parts are produced over time,
 * such as {@code multipart/mixed} responses of incremental protocols. The writer runs on the
 * connector's call thread; each part is flushed, followed by the next delimiter, before {@link
 * PartSink#send(Series, byte[])} returns, so that readers can process it at once. The close
 * delimiter is written when the writer returns.
 *
 * @see MultipartStreamReader
 */
public final class MultipartStreamRepresentation extends OutputRepresentation {

    private final PartWriter writer;

    private final String boundary;

    /**
     * Creates a {@code multipart/mixed} stream with a generated boundary.
     *
     * @param writer Writes parts to the provided sink until the stream is complete.
     */
    public MultipartStreamRepresentation(PartWriter writer) {
        this(MediaType.MULTIPART_MIXED, null, writer);
    }

    /**
     * Creates a multipart stream.
     *
     * @param mediaType The multipart media type, for example {@link MediaType#MULTIPART_MIXED}. Its
     *     parameters are kept, and its boundary parameter replaced.
     * @param boundary The boundary, or null to generate one.
     * @param writer Writes parts to the provided sink until the stream is complete.
     */
    public MultipartStreamRepresentation(MediaType mediaType, String boundary, PartWriter writer) {
        Objects.requireNonNull(mediaType, "mediaType");
        Objects.requireNonNull(writer, "writer");
        if (!MediaType.MULTIPART_ALL.includes(mediaType, true)) {
            throw new IllegalArgumentException("Not a multipart media type: " + mediaType);
        }
        String actualBoundary =
                boundary == null ? MultiPartRepresentation.generateBoundary() : boundary;
        if (actualBoundary.isEmpty()
                || actualBoundary.length() > 70
                || actualBoundary.chars().anyMatch(c -> c < 0x20 || c > 0x7E)) {
            throw new IllegalArgumentException("Invalid boundary: " + actualBoundary);
        }
        super(withBoundary(mediaType, actualBoundary));
        this.writer = writer;
        this.boundary = actualBoundary;
        setTransient(true);
    }

    /** Sets the boundary parameter, keeping the other parameters of the media type. */
    private static MediaType withBoundary(MediaType mediaType, String boundary) {
        Series<Parameter> parameters = new Series<>(Parameter.class);
        for (Parameter parameter : mediaType.getParameters()) {
            if (!"boundary".equalsIgnoreCase(parameter.getName())) {
                parameters.add(parameter);
            }
        }
        parameters.add(new Parameter("boundary", boundary));
        // The name of the original type may carry its parameters, including the old boundary.
        return new MediaType(
                mediaType.getMainType() + "/" + mediaType.getSubType(),
                parameters,
                mediaType.getDescription());
    }

    /**
     * Returns the boundary.
     *
     * @return The boundary.
     */
    public String getBoundary() {
        return boundary;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try (PartSink sink = new PartSink(outputStream, boundary)) {
            writer.write(sink);
        }
    }

    /** Writes parts for the lifetime of the response. */
    @FunctionalInterface
    public interface PartWriter {

        /**
         * Writes parts until the stream is complete.
         *
         * @param sink The part sink.
         * @throws IOException If writing a part fails.
         */
        void write(PartSink sink) throws IOException;
    }

    /** A blocking sink for the parts of a {@link MultipartStreamRepresentation}. */
    public static final class PartSink implements AutoCloseable {

        private final OutputStream outputStream;

        private final byte[] delimiter;

        private boolean started;

        private boolean closed;

        private PartSink(OutputStream outputStream, String boundary) {
            this.outputStream = Objects.requireNonNull(outputStream, "outputStream");
            this.delimiter = ("\r\n--" + boundary).getBytes(StandardCharsets.US_ASCII);
        }

        /** Indicates whether the content starts with the delimiter without its line break. */
        private boolean startsWithDashBoundary(byte[] content) {
            int length = delimiter.length - 2;
            return content.length >= length
                    && Arrays.equals(content, 0, length, delimiter, 2, delimiter.length);
        }

        /**
         * Writes and flushes one part, followed by the next delimiter.
         *
         * @param headers The part headers, such as "Content-Type", or null.
         * @param content The part content, which must not contain the delimiter.
         * @throws IOException If the response stream has closed or writing fails.
         * @throws IllegalArgumentException If a header is invalid or the content contains the
         *     delimiter, which would let it forge parts.
         */
        public synchronized void send(Series<Header> headers, byte[] content) throws IOException {
            Objects.requireNonNull(content, "content");
            if (closed) {
                throw new IOException("Part sink is closed");
            }
            // The line break ending the headers precedes the content, so content starting with
            // the boundary would also form a delimiter.
            if (contains(content, delimiter) || startsWithDashBoundary(content)) {
                throw new IllegalArgumentException("The part content contains the delimiter");
            }
            StringBuilder head = new StringBuilder();
            if (headers != null) {
                for (Header header : headers) {
                    String name = header.getName();
                    String value = header.getValue();
                    if (name.isEmpty()
                            || name.indexOf(':') >= 0
                            || containsLineBreak(name)
                            || containsLineBreak(value)) {
                        throw new IllegalArgumentException("Invalid part header: " + name);
                    }
                    head.append(name).append(": ").append(value).append("\r\n");
                }
            }
            head.append("\r\n");
            if (!started) {
                outputStream.write(delimiter);
                started = true;
            }
            outputStream.write('\r');
            outputStream.write('\n');
            outputStream.write(head.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.write(content);
            outputStream.write(delimiter);
            outputStream.flush();
        }

        /**
         * Writes the close delimiter, without closing the connector-owned response stream.
         *
         * @throws IOException If writing fails.
         */
        @Override
        public synchronized void close() throws IOException {
            if (!closed) {
                closed = true;
                if (!started) {
                    outputStream.write(delimiter);
                }
                outputStream.write("--\r\n".getBytes(StandardCharsets.US_ASCII));
                outputStream.flush();
            }
        }

        /** Searches a pattern with the Knuth-Morris-Pratt algorithm. */
        private static boolean contains(byte[] content, byte[] pattern) {
            int[] failure = new int[pattern.length];
            for (int i = 1, k = 0; i < pattern.length; i++) {
                while (k > 0 && pattern[i] != pattern[k]) {
                    k = failure[k - 1];
                }
                if (pattern[i] == pattern[k]) {
                    k++;
                }
                failure[i] = k;
            }
            for (int i = 0, matched = 0; i < content.length; i++) {
                while (matched > 0 && content[i] != pattern[matched]) {
                    matched = failure[matched - 1];
                }
                if (content[i] == pattern[matched] && ++matched == pattern.length) {
                    return true;
                }
            }
            return false;
        }

        private static boolean containsLineBreak(String value) {
            return value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0;
        }
    }
}
