/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.util.Series;

/**
 * Reads the parts of a {@code multipart/*} stream (RFC 2046) as they arrive, for example a {@link
 * MultipartStreamRepresentation} received by a client. Each part is returned once its closing
 * delimiter is read. The preamble, the epilogue and transport padding are ignored.
 *
 * <p>Part sizes are bounded: a part whose headers and content exceed the limit fails the reader
 * with an {@link IOException}, never truncated.
 */
public final class MultipartStreamReader
        implements Iterator<MultiPartRepresentation.Part>, AutoCloseable {

    private static final int MAX_HEADER_LINE = 8192;

    private final InputStream input;

    private final byte[] delimiter;

    private final int[] failure;

    private final long maxPartSize;

    private boolean started;

    private boolean finished;

    private MultiPartRepresentation.Part next;

    /**
     * Creates a reader for a multipart entity, using the boundary of its media type.
     *
     * @param entity The multipart entity.
     * @param maxPartSize The maximum size of a part, headers included, in bytes.
     * @throws IOException If the entity has no boundary or can't be read.
     */
    public MultipartStreamReader(Representation entity, long maxPartSize) throws IOException {
        // Validate before acquiring the stream, which would otherwise be left open.
        String boundary = boundary(Objects.requireNonNull(entity, "entity").getMediaType());
        checkMaxPartSize(maxPartSize);
        this(entity.getStream(), boundary, maxPartSize);
    }

    /**
     * Creates a reader.
     *
     * @param inputStream The multipart stream.
     * @param boundary The boundary.
     * @param maxPartSize The maximum size of a part, headers included, in bytes.
     */
    public MultipartStreamReader(InputStream inputStream, String boundary, long maxPartSize) {
        Objects.requireNonNull(inputStream, "inputStream");
        if (boundary == null || boundary.isEmpty()) {
            throw new IllegalArgumentException("A boundary is required");
        }
        checkMaxPartSize(maxPartSize);
        this.input = new BufferedInputStream(inputStream);
        this.delimiter = ("\r\n--" + boundary).getBytes(StandardCharsets.US_ASCII);
        this.failure = failureTable(delimiter);
        this.maxPartSize = maxPartSize;
    }

    private static void checkMaxPartSize(long maxPartSize) {
        if (maxPartSize <= 0) {
            throw new IllegalArgumentException("The maximum part size must be positive");
        }
    }

    private static String boundary(MediaType mediaType) throws IOException {
        // Parameter names are case-insensitive.
        String boundary =
                mediaType == null
                        ? null
                        : mediaType.getParameters().getFirstValue("boundary", true);
        if (boundary == null || boundary.isEmpty()) {
            throw new IOException("The multipart media type has no boundary: " + mediaType);
        }
        return boundary;
    }

    /** The Knuth-Morris-Pratt failure table of the delimiter. */
    private static int[] failureTable(byte[] pattern) {
        int[] table = new int[pattern.length];
        int k = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (k > 0 && pattern[i] != pattern[k]) {
                k = table[k - 1];
            }
            if (pattern[i] == pattern[k]) {
                k++;
            }
            table[i] = k;
        }
        return table;
    }

    /**
     * Reads the next part, blocking as needed.
     *
     * @return The next part, or null after the close delimiter.
     * @throws IOException If reading fails, the stream ends before the close delimiter, or a part
     *     exceeds the maximum size.
     */
    public MultiPartRepresentation.Part readPart() throws IOException {
        if (next != null) {
            MultiPartRepresentation.Part result = next;
            next = null;
            return result;
        }
        if (finished) {
            return null;
        }
        if (!started) {
            started = true;
            // The first delimiter may start the stream, without the leading line break.
            if (!skipToDelimiter(true)) {
                throw new IOException("No multipart delimiter found");
            }
        }

        // After a delimiter: "--" closes the stream, otherwise padding then a line break.
        int first = input.read();
        int second = input.read();
        if (first == '-' && second == '-') {
            finished = true;
            return null;
        }
        int c = second;
        if (first != '\r' || second != '\n') {
            c = first;
            while (c == ' ' || c == '\t') {
                c = second;
                second = input.read();
            }
            if (c != '\r' || second != '\n') {
                throw new IOException("Invalid multipart delimiter line");
            }
        }

        long[] size = {0};
        Series<Header> headers = readHeaders(size);
        byte[] content = readContent(size[0]);
        return new MultiPartRepresentation.Part(null, null, headers, content(headers, content));
    }

    private Series<Header> readHeaders(long[] size) throws IOException {
        Series<Header> headers = new Series<>(Header.class);
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        while (true) {
            int c = input.read();
            if (c == -1) {
                throw new IOException("The multipart stream ended in part headers");
            }
            if (++size[0] > maxPartSize || line.size() > MAX_HEADER_LINE) {
                throw new IOException("A multipart part exceeds " + maxPartSize + " bytes");
            }
            if (c != '\n') {
                line.write(c);
                continue;
            }
            String text = line.toString(StandardCharsets.UTF_8);
            line.reset();
            if (text.endsWith("\r")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.isEmpty()) {
                return headers;
            }
            int colon = text.indexOf(':');
            if (colon <= 0) {
                throw new IOException("Invalid multipart part header: " + text);
            }
            headers.add(
                    new Header(text.substring(0, colon).trim(), text.substring(colon + 1).trim()));
        }
    }

    /** Reads the content up to the next delimiter, which is consumed. */
    private byte[] readContent(long headerSize) throws IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        int matched = 0;
        long size = headerSize;
        while (matched < delimiter.length) {
            int c = input.read();
            if (c == -1) {
                throw new IOException("The multipart stream ended in a part");
            }
            while (matched > 0 && c != (delimiter[matched] & 0xFF)) {
                // Bytes that looked like a delimiter are content after all.
                int fallback = failure[matched - 1];
                content.write(delimiter, 0, matched - fallback);
                matched = fallback;
            }
            if (c == (delimiter[matched] & 0xFF)) {
                matched++;
            } else {
                content.write(c);
            }
            // Subtract the delimiter allowance rather than add it, which could overflow the limit.
            if (++size - delimiter.length > maxPartSize) {
                throw new IOException("A multipart part exceeds " + maxPartSize + " bytes");
            }
        }
        if (content.size() + headerSize > maxPartSize) {
            throw new IOException("A multipart part exceeds " + maxPartSize + " bytes");
        }
        return content.toByteArray();
    }

    /**
     * Skips the preamble up to the first delimiter.
     *
     * @param optionalLineBreak True if the delimiter may lack its leading line break.
     * @return False if the stream ended first.
     */
    private boolean skipToDelimiter(boolean optionalLineBreak) throws IOException {
        int matched = optionalLineBreak ? 2 : 0;
        long skipped = 0;
        while (matched < delimiter.length) {
            int c = input.read();
            if (c == -1) {
                return false;
            }
            while (matched > 0 && c != (delimiter[matched] & 0xFF)) {
                matched = failure[matched - 1];
            }
            if (c == (delimiter[matched] & 0xFF)) {
                matched++;
            }
            // The bytes currently matching the delimiter aren't part of the preamble.
            if (++skipped - matched > maxPartSize) {
                throw new IOException("The multipart preamble exceeds " + maxPartSize + " bytes");
            }
        }
        return true;
    }

    private static Representation content(Series<Header> headers, byte[] content) {
        String contentType = headers.getFirstValue("Content-Type", true);
        MediaType mediaType = contentType == null ? null : MediaType.valueOf(contentType);
        return new ByteArrayRepresentation(content, mediaType);
    }

    @Override
    public boolean hasNext() {
        if (next == null && !finished) {
            try {
                next = readPart();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return next != null;
    }

    @Override
    public MultiPartRepresentation.Part next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        MultiPartRepresentation.Part result = next;
        next = null;
        return result;
    }

    /**
     * Closes the underlying stream.
     *
     * @throws IOException If closing fails.
     */
    @Override
    public void close() throws IOException {
        finished = true;
        input.close();
    }
}
