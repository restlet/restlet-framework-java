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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.util.Series;

/**
 * Input representation that can either parse or generate a multipart form data representation (RFC
 * 7578) depending on which constructor is invoked.
 *
 * @author Jerome Louvel
 */
public class MultiPartRepresentation extends InputRepresentation {

    /**
     * A part of a multipart form data representation: a name, an optional file name, optional
     * headers and a content.
     */
    public static final class Part {

        private final String name;

        private final String fileName;

        private final Series<Header> headers;

        private final Representation content;

        /**
         * Constructor.
         *
         * @param name The name of the part (form field name).
         * @param fileName The client's suggested file name for storing the part, or null.
         * @param content The part content.
         */
        public Part(String name, String fileName, Representation content) {
            this(name, fileName, null, content);
        }

        /**
         * Constructor.
         *
         * @param name The name of the part (form field name).
         * @param fileName The client's suggested file name for storing the part, or null.
         * @param headers Additional part headers, such as "Content-Type", or null. The
         *     "Content-Disposition" header is computed from the name and file name.
         * @param content The part content.
         */
        public Part(String name, String fileName, Series<Header> headers, Representation content) {
            this.name = name;
            this.fileName = fileName;
            this.headers = (headers == null) ? new Series<>(Header.class) : headers;
            this.content = content;
        }

        /**
         * Returns the name of the part (form field name).
         *
         * @return The name of the part.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the client's suggested file name for storing the part, or null.
         *
         * @return The file name or null.
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Returns the additional part headers, excluding "Content-Disposition".
         *
         * @return The additional part headers.
         */
        public Series<Header> getHeaders() {
            return headers;
        }

        /**
         * Returns the part content.
         *
         * @return The part content.
         */
        public Representation getContent() {
            return content;
        }

        /**
         * Returns the size in bytes of the content, or {@link Representation#UNKNOWN_SIZE}.
         *
         * @return The size in bytes of the content.
         */
        public long getLength() {
            return (content == null) ? 0 : content.getSize();
        }

        /**
         * Returns the content as a string.
         *
         * @param charset The character set, or null for UTF-8.
         * @return The content as a string.
         * @throws IOException If the content can't be read.
         */
        public String getContentAsString(Charset charset) throws IOException {
            if (content == null) {
                return null;
            }
            try (InputStream in = content.getStream()) {
                return new String(
                        in.readAllBytes(), (charset == null) ? StandardCharsets.UTF_8 : charset);
            }
        }
    }

    private static final String PARAMETER_BOUNDARY = "boundary";

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    /** Part content size above which parsed parts are stored in a file, when possible. */
    private static final int MAX_MEMORY_PART_SIZE = 64 * 1024;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final char[] BOUNDARY_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private static final byte[] CRLF = {'\r', '\n'};

    /**
     * Creates a {@link Part} object based on a {@link Representation} plus metadata.
     *
     * @param name The name of the part.
     * @param fileName The client's suggested file name for storing the part.
     * @param partContent The part content.
     * @return The {@link Part} object created.
     */
    public static Part createPart(String name, String fileName, Representation partContent) {
        return new Part(name, fileName, partContent);
    }

    /**
     * Generates a random boundary.
     *
     * @return A random boundary.
     */
    public static String generateBoundary() {
        StringBuilder sb = new StringBuilder("RestletBoundary");
        for (int i = 0; i < 24; i++) {
            sb.append(BOUNDARY_CHARS[RANDOM.nextInt(BOUNDARY_CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * Returns the value of the first media-type parameter with the "boundary" name.
     *
     * @param mediaType The media type that might contain a "boundary" parameter.
     * @return The value of the first media-type parameter with the "boundary" name.
     */
    public static String getBoundary(MediaType mediaType) {
        final String result;

        if (mediaType != null) {
            result = mediaType.getParameters().getFirstValue(PARAMETER_BOUNDARY);
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Sets a boundary to an existing media type. If the original media-type already has a
     * "boundary" parameter, it will be erased. *
     *
     * @param mediaType The media type to update.
     * @param boundary The boundary to add as a parameter.
     * @return The updated media type.
     */
    public static MediaType setBoundary(MediaType mediaType, String boundary) {
        MediaType result = null;

        if (mediaType != null) {
            if (mediaType.getParameters().getFirst(PARAMETER_BOUNDARY) != null) {
                result = new MediaType(mediaType.getParent(), PARAMETER_BOUNDARY, boundary);
            } else {
                result = new MediaType(mediaType, PARAMETER_BOUNDARY, boundary);
            }
        }

        return result;
    }

    /** The boundary used to separate each part for the parsed or generated form. */
    private volatile String boundary;

    /** The wrapped multipart form data either parsed or to be generated. */
    private volatile List<Part> parts;

    /**
     * Constructor that wraps multiple parts, set a random boundary, then GENERATES the content via
     * {@link #getStream()} as a {@link MediaType#MULTIPART_FORM_DATA}.
     *
     * @param parts The source parts to use when generating the representation.
     */
    public MultiPartRepresentation(List<Part> parts) {
        this(generateBoundary(), parts);
    }

    /**
     * Constructor that wraps multiple parts, set a media type with a boundary, then GENERATES the
     * content via {@link #getStream()} as a {@link MediaType#MULTIPART_FORM_DATA}.
     *
     * @param mediaType The media type to set.
     * @param boundary The boundary to add as a parameter.
     * @param parts The source parts to use when generating the representation.
     */
    public MultiPartRepresentation(MediaType mediaType, String boundary, List<Part> parts) {
        super(null, setBoundary(mediaType, boundary));
        this.boundary = boundary;
        this.parts = parts;
    }

    /**
     * Constructor that wraps multiple parts, set a random boundary, then GENERATES the content via
     * {@link #getStream()} as a {@link MediaType#MULTIPART_FORM_DATA}.
     *
     * @param parts The source parts to use when generating the representation.
     */
    public MultiPartRepresentation(Part... parts) {
        this(Arrays.asList(parts));
    }

    /**
     * Constructor that PARSES the content into {@link #getParts()}. Part contents are kept in
     * memory.
     *
     * @param multiPartEntity The multipart entity to parse which should have a media type based on
     *     {@link MediaType#MULTIPART_FORM_DATA}, with a "boundary" parameter.
     * @throws IOException If the entity can't be read or parsed.
     */
    public MultiPartRepresentation(Representation multiPartEntity) throws IOException {
        this(multiPartEntity, null);
    }

    /**
     * Constructor that PARSES the content into {@link #getParts()}.
     *
     * @param multiPartEntity The multipart entity to parse which should have a media type based on
     *     {@link MediaType#MULTIPART_FORM_DATA}, with a "boundary" parameter.
     * @param storageLocation The directory where large parsed parts are stored, or null to keep all
     *     part contents in memory.
     * @throws IOException If the entity can't be read or parsed.
     */
    public MultiPartRepresentation(Representation multiPartEntity, Path storageLocation)
            throws IOException {
        this(multiPartEntity.getMediaType(), multiPartEntity.getStream(), storageLocation);
    }

    /**
     * Constructor that PARSES the content into {@link #getParts()}.
     *
     * @param mediaType The media type that should be based on {@link
     *     MediaType#MULTIPART_FORM_DATA}, with a "boundary" parameter.
     * @param multiPartEntity The multipart entity to parse.
     * @param storageLocation The directory where large parsed parts are stored, or null to keep all
     *     part contents in memory.
     * @throws IOException If the entity can't be read or parsed.
     */
    public MultiPartRepresentation(
            MediaType mediaType, InputStream multiPartEntity, Path storageLocation)
            throws IOException {
        super(null, mediaType);

        if (!MediaType.MULTIPART_FORM_DATA.equals(getMediaType(), true)) {
            throw new IllegalArgumentException(
                    "The content type must be \"multipart/form-data\" with a \"boundary\" parameter");
        }

        this.boundary = getMediaType().getParameters().getFirstValue(PARAMETER_BOUNDARY);

        if (this.boundary == null) {
            throw new IllegalArgumentException(
                    "The content type must have a \"boundary\" parameter");
        }

        if (multiPartEntity == null) {
            throw new IllegalArgumentException("The multipart entity can't be null");
        }

        try (InputStream in = multiPartEntity) {
            this.parts = new Parser(in, this.boundary, storageLocation).parse();
        }
    }

    /**
     * Constructor that wraps multiple parts, set a boundary, then GENERATES the content via {@link
     * #getStream()} as a {@link MediaType#MULTIPART_FORM_DATA}.
     *
     * @param boundary The boundary to add as a parameter.
     * @param parts The source parts to use when generating the representation.
     */
    public MultiPartRepresentation(String boundary, List<Part> parts) {
        this(MediaType.MULTIPART_FORM_DATA, boundary, parts);
    }

    /**
     * Constructor that wraps multiple parts, set a boundary, then GENERATES the content via {@link
     * #getStream()} as a {@link MediaType#MULTIPART_FORM_DATA}.
     *
     * @param boundary The boundary to add as a parameter.
     * @param parts The source parts to use when generating the representation.
     */
    public MultiPartRepresentation(String boundary, Part... parts) {
        this(boundary, Arrays.asList(parts));
    }

    /**
     * Returns the boundary used to separate each part for the parsed or generated form.
     *
     * @return The boundary used to separate each part for the parsed or generated form.
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Returns the wrapped multipart form data either parsed or to be generated.
     *
     * @return The wrapped multipart form data either parsed or to be generated.
     */
    public List<Part> getParts() {
        return parts;
    }

    /**
     * Returns an input stream that generates the multipart form data serialization for the wrapped
     * {@link #getParts()} object. The "boundary" must be non-null when invoking this method. Part
     * contents are streamed, not buffered.
     *
     * @return An input stream that generates the multipart form data.
     */
    @Override
    public InputStream getStream() throws IOException {
        if (getBoundary() == null) {
            throw new IllegalArgumentException("The boundary can't be null");
        }

        final List<Part> source = (this.parts == null) ? List.of() : this.parts;
        final byte[] delimiter = ("--" + getBoundary()).getBytes(StandardCharsets.US_ASCII);

        List<InputStream> streams = new ArrayList<>();
        for (Part part : source) {
            ByteArrayOutputStream head = new ByteArrayOutputStream();
            head.write(delimiter);
            head.write(CRLF);
            writeHeaders(part, head);
            head.write(CRLF);
            streams.add(new ByteArrayInputStream(head.toByteArray()));
            streams.add(new LazyPartStream(part));
            streams.add(new ByteArrayInputStream(CRLF));
        }

        ByteArrayOutputStream tail = new ByteArrayOutputStream();
        tail.write(delimiter);
        tail.write('-');
        tail.write('-');
        tail.write(CRLF);
        streams.add(new ByteArrayInputStream(tail.toByteArray()));

        setStream(null);
        return new SequenceInputStream(Collections.enumeration(streams));
    }

    private static void writeHeaders(Part part, OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder(HEADER_CONTENT_DISPOSITION);
        sb.append(": form-data; name=\"").append(escapeQuoted(part.getName())).append('"');
        if (part.getFileName() != null) {
            sb.append("; filename=\"").append(escapeQuoted(part.getFileName())).append('"');
        }
        sb.append("\r\n");

        for (Header header : part.getHeaders()) {
            if (!HEADER_CONTENT_DISPOSITION.equalsIgnoreCase(header.getName())) {
                sb.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
            }
        }

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** Escapes a quoted parameter value as browsers do (HTML form submission algorithm). */
    private static String escapeQuoted(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "%22").replace("\r", "%0D").replace("\n", "%0A");
    }

    /**
     * Sets the boundary used to separate each part for the parsed or generated form. It will also
     * update the {@link MediaType}'s "boundary" attribute.
     *
     * @param boundary The boundary used to separate each part for the parsed or generated form.
     */
    public void setBoundary(String boundary) {
        this.boundary = boundary;

        if (getMediaType() == null) {
            setMediaType(
                    new MediaType(MediaType.MULTIPART_FORM_DATA, PARAMETER_BOUNDARY, boundary));
        } else {
            setMediaType(setBoundary(getMediaType(), boundary));
        }
    }

    /** Opens the part content stream on first read, so that parts are streamed one at a time. */
    private static final class LazyPartStream extends InputStream {

        private final Part part;

        private InputStream delegate;

        LazyPartStream(Part part) {
            this.part = part;
        }

        private InputStream delegate() throws IOException {
            if (delegate == null) {
                Representation content = part.getContent();
                InputStream in = (content == null) ? null : content.getStream();
                delegate = (in == null) ? InputStream.nullInputStream() : in;
            }
            return delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate().read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate().read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            if (delegate != null) {
                delegate.close();
            }
        }
    }

    /** Streaming multipart/form-data parser. */
    private static final class Parser {

        private final InputStream in;

        /** The delimiter preceded by CRLF. Its first byte, CR, never recurs in it. */
        private final byte[] delimiter;

        private final Path storageLocation;

        Parser(InputStream in, String boundary, Path storageLocation) {
            // A virtual CRLF lets the first delimiter be matched like the others.
            this.in =
                    new BufferedInputStream(
                            new SequenceInputStream(new ByteArrayInputStream(CRLF), in));
            this.delimiter = ("\r\n--" + boundary).getBytes(StandardCharsets.US_ASCII);
            this.storageLocation = storageLocation;
        }

        List<Part> parse() throws IOException {
            List<Part> result = new ArrayList<>();

            // Skip the preamble
            if (!copyUntilDelimiter(OutputStream.nullOutputStream())) {
                throw new IOException("No multipart delimiter found");
            }

            while (true) {
                int c1 = in.read();
                int c2 = in.read();
                if (c1 == '-' && c2 == '-') {
                    // Close delimiter, ignore the epilogue
                    return result;
                }
                skipLineRest(c1, c2);

                Series<Header> headers = readHeaders();
                String disposition = headers.getFirstValue(HEADER_CONTENT_DISPOSITION, true);
                if (disposition == null) {
                    throw new IOException("Missing Content-Disposition header in a part");
                }
                headers.removeAll(HEADER_CONTENT_DISPOSITION, true);
                String name = getParameter(disposition, "name");
                String fileName = getParameter(disposition, "filename");

                SpillingOutputStream content = new SpillingOutputStream(storageLocation);
                boolean found;
                try (content) {
                    found = copyUntilDelimiter(content);
                }
                if (!found) {
                    content.discard();
                    throw new EOFException("Unexpected end of the multipart entity");
                }

                MediaType mediaType = null;
                String contentType = headers.getFirstValue("Content-Type", true);
                if (contentType != null) {
                    mediaType = MediaType.valueOf(contentType);
                }
                result.add(new Part(name, fileName, headers, content.toRepresentation(mediaType)));
            }
        }

        /** Skips the transport padding after a delimiter, up to and including the CRLF. */
        private void skipLineRest(int c1, int c2) throws IOException {
            if (c1 == '\r' && c2 == '\n') {
                return;
            }
            if (c1 == '\n') {
                if (c2 != -1) {
                    throw new IOException("Malformed multipart delimiter line");
                }
                return;
            }
            int c = c2;
            while (c != '\n') {
                if (c == -1) {
                    throw new EOFException("Unexpected end of the multipart entity");
                }
                c = in.read();
            }
        }

        private Series<Header> readHeaders() throws IOException {
            Series<Header> headers = new Series<>(Header.class);
            String line;
            while (!(line = readLine()).isEmpty()) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    headers.add(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
                }
            }
            return headers;
        }

        private String readLine() throws IOException {
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            int c;
            while ((c = in.read()) != '\n') {
                if (c == -1) {
                    throw new EOFException("Unexpected end of the multipart headers");
                }
                if (line.size() > 16 * 1024) {
                    throw new IOException("Multipart header line too long");
                }
                line.write(c);
            }
            byte[] bytes = line.toByteArray();
            int length = bytes.length;
            if (length > 0 && bytes[length - 1] == '\r') {
                length--;
            }
            return new String(bytes, 0, length, StandardCharsets.UTF_8);
        }

        /**
         * Copies bytes until the CRLF-prefixed delimiter, which is consumed.
         *
         * @return True if the delimiter was found, false at end of stream.
         */
        private boolean copyUntilDelimiter(OutputStream out) throws IOException {
            int matched = 0;
            int c;
            while ((c = in.read()) != -1) {
                if (c == (delimiter[matched] & 0xFF)) {
                    matched++;
                    if (matched == delimiter.length) {
                        return true;
                    }
                } else {
                    if (matched > 0) {
                        out.write(delimiter, 0, matched);
                        matched = 0;
                    }
                    if (c == (delimiter[0] & 0xFF)) {
                        matched = 1;
                    } else {
                        out.write(c);
                    }
                }
            }
            return false;
        }

        /** Returns a Content-Disposition parameter value, unquoted. */
        private static String getParameter(String disposition, String parameter) {
            Iterator<String> tokens = splitParameters(disposition).iterator();
            if (tokens.hasNext()) {
                tokens.next(); // disposition type
            }
            while (tokens.hasNext()) {
                String token = tokens.next();
                int eq = token.indexOf('=');
                if (eq > 0
                        && token.substring(0, eq)
                                .trim()
                                .toLowerCase(Locale.ROOT)
                                .equals(parameter)) {
                    return unquote(token.substring(eq + 1).trim());
                }
            }
            return null;
        }

        /** Splits on semicolons that are outside quoted strings. */
        private static List<String> splitParameters(String value) {
            List<String> result = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean quoted = false;
            boolean escaped = false;
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (escaped) {
                    escaped = false;
                } else if (quoted && ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    quoted = !quoted;
                } else if (ch == ';' && !quoted) {
                    result.add(current.toString());
                    current.setLength(0);
                    continue;
                }
                current.append(ch);
            }
            result.add(current.toString());
            return result;
        }

        private static String unquote(String value) {
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < value.length() - 1; i++) {
                    char ch = value.charAt(i);
                    // Browsers don't escape backslashes in file names (Windows paths), so only
                    // quotes and backslashes are unescaped.
                    if (ch == '\\'
                            && i + 1 < value.length() - 1
                            && (value.charAt(i + 1) == '"' || value.charAt(i + 1) == '\\')) {
                        ch = value.charAt(++i);
                    }
                    sb.append(ch);
                }
                return sb.toString();
            }
            return value;
        }
    }

    /** Buffers in memory, then spills to a temporary file above a threshold. */
    private static final class SpillingOutputStream extends OutputStream {

        private final Path storageLocation;

        private ByteArrayOutputStream memory = new ByteArrayOutputStream();

        private Path file;

        private OutputStream fileOut;

        private long size;

        SpillingOutputStream(Path storageLocation) {
            this.storageLocation = storageLocation;
        }

        @Override
        public void write(int b) throws IOException {
            prepare(1);
            if (fileOut != null) {
                fileOut.write(b);
            } else {
                memory.write(b);
            }
            size++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            prepare(len);
            if (fileOut != null) {
                fileOut.write(b, off, len);
            } else {
                memory.write(b, off, len);
            }
            size += len;
        }

        private void prepare(int len) throws IOException {
            if (fileOut == null
                    && storageLocation != null
                    && memory.size() + (long) len > MAX_MEMORY_PART_SIZE) {
                file = Files.createTempFile(storageLocation, "multipart", ".part");
                fileOut = new java.io.BufferedOutputStream(Files.newOutputStream(file));
                memory.writeTo(fileOut);
                memory = null;
            }
        }

        @Override
        public void close() throws IOException {
            if (fileOut != null) {
                fileOut.close();
            }
        }

        void discard() throws IOException {
            if (file != null) {
                Files.deleteIfExists(file);
            }
        }

        Representation toRepresentation(MediaType mediaType) {
            if (file != null) {
                FileRepresentation result = new FileRepresentation(file.toFile(), mediaType);
                result.setSize(size);
                return result;
            }
            return new ByteArrayRepresentation(memory.toByteArray(), mediaType, size);
        }
    }
}
