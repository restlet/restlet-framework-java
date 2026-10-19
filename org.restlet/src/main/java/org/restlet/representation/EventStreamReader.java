/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.restlet.data.ServerSentEvent;

/**
 * A blocking reader of Server-Sent Events. When created with an {@link EventStreamSource}, it
 * reconnects after EOF, honoring the server's {@code retry:} field and passing the latest event ID
 * to the source for use in a {@code Last-Event-ID} request header.
 */
public final class EventStreamReader implements Iterator<ServerSentEvent>, AutoCloseable {

    private static final long DEFAULT_RETRY_MS = 3000;

    private final EventStreamSource source;

    private final Object reconnectLock = new Object();

    private BufferedReader reader;

    private ServerSentEvent next;

    private volatile String lastEventId = "";

    private volatile long retryDelayMs = DEFAULT_RETRY_MS;

    private boolean beginning = true;

    private boolean currentEof;

    private boolean sourceStopped;

    private volatile boolean closed;

    /**
     * Creates a one-shot reader.
     *
     * @param inputStream The UTF-8 encoded event stream.
     */
    public EventStreamReader(InputStream inputStream) {
        this(
                new InputStreamReader(
                        Objects.requireNonNull(inputStream, "inputStream"), StandardCharsets.UTF_8),
                null);
    }

    /**
     * Creates a one-shot reader.
     *
     * @param reader The event stream reader.
     */
    public EventStreamReader(Reader reader) {
        this(reader, null);
    }

    /**
     * Creates a reconnecting reader. The source is called initially and after each EOF. It receives
     * the most recently parsed event ID; return null to stop reconnecting.
     *
     * @param source Opens a stream, setting {@code Last-Event-ID} from the supplied value when it
     *     is non-empty.
     * @throws IOException If the initial connection fails.
     */
    public EventStreamReader(EventStreamSource source) throws IOException {
        this.source = Objects.requireNonNull(source, "source");
        InputStream inputStream = source.open("");
        if (inputStream == null) {
            reader = new BufferedReader(new java.io.StringReader(""));
            sourceStopped = true;
        } else {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
    }

    private EventStreamReader(Reader reader, EventStreamSource source) {
        this.reader = new BufferedReader(Objects.requireNonNull(reader, "reader"));
        this.source = source;
    }

    /** Returns the last event ID seen, or the empty string before any ID was read. */
    public String getLastEventId() {
        return lastEventId;
    }

    /** Returns the most recently received reconnection delay in milliseconds. */
    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    /**
     * Reads the next event, blocking as needed.
     *
     * @return The next event, or null at EOF for a one-shot stream or when a source stops.
     * @throws IOException If reading or reconnecting fails.
     */
    public ServerSentEvent readEvent() throws IOException {
        if (closed || sourceStopped) {
            return null;
        }
        while (!closed) {
            ServerSentEvent event = readCurrentEvent();
            if (event != null) {
                return event;
            }
            if (!currentEof || source == null || sourceStopped || !reconnect()) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if (next == null && !closed) {
            try {
                next = readEvent();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return next != null;
    }

    @Override
    public ServerSentEvent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        ServerSentEvent result = next;
        next = null;
        return result;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        synchronized (reconnectLock) {
            reconnectLock.notifyAll();
        }
        reader.close();
    }

    private ServerSentEvent readCurrentEvent() throws IOException {
        PendingEvent pending = new PendingEvent();
        ServerSentEvent result = null;
        while (result == null && !closed) {
            String line = reader.readLine();
            if (line == null) {
                currentEof = true;
                return pending.build();
            }
            result = processLine(stripBom(line), pending);
        }
        return result;
    }

    private String stripBom(String line) {
        if (beginning) {
            beginning = false;
            if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                return line.substring(1);
            }
        }
        return line;
    }

    private ServerSentEvent processLine(String line, PendingEvent pending) {
        if (line.isEmpty()) {
            ServerSentEvent event = pending.build();
            if (event == null) {
                pending.resetFields();
            }
            return event;
        }
        if (line.charAt(0) == ':') {
            pending.appendComment(stripLeadingSpace(line.substring(1)));
            return null;
        }

        int separator = line.indexOf(':');
        String field = separator < 0 ? line : line.substring(0, separator);
        String value = separator < 0 ? "" : stripLeadingSpace(line.substring(separator + 1));
        switch (field) {
            case "data" -> pending.appendData(value);
            case "event" -> pending.event = value;
            case "id" -> applyId(value, pending);
            case "retry" -> applyRetry(value, pending);
            default -> {
                // Unknown fields are ignored by the SSE protocol.
            }
        }
        return null;
    }

    private void applyId(String value, PendingEvent pending) {
        if (value.indexOf('\0') < 0) {
            pending.id = value;
            lastEventId = value;
        }
    }

    private void applyRetry(String value, PendingEvent pending) {
        if (!isDigits(value)) {
            return;
        }
        try {
            long retry = Long.parseLong(value);
            pending.retry = retry;
            retryDelayMs = retry;
        } catch (NumberFormatException _) {
            // An out-of-range retry value is ignored by the SSE parsing rules.
        }
    }

    private static String stripLeadingSpace(String value) {
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    private boolean reconnect() throws IOException {
        if (!awaitReconnectDelay()) {
            return false;
        }
        reader.close();
        InputStream inputStream;
        try {
            inputStream = source.open(lastEventId);
        } catch (IOException e) {
            sourceStopped = true;
            throw e;
        }
        if (inputStream == null) {
            sourceStopped = true;
            return false;
        }
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        beginning = true;
        currentEof = false;
        return true;
    }

    private boolean awaitReconnectDelay() throws IOException {
        synchronized (reconnectLock) {
            long deadline = System.nanoTime() + retryDelayMs * 1_000_000L;
            long remainingMs = retryDelayMs;
            while (!closed && remainingMs > 0) {
                try {
                    reconnectLock.wait(remainingMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(
                            "Interrupted while waiting to reconnect to the event stream", e);
                }
                remainingMs = (deadline - System.nanoTime()) / 1_000_000L;
            }
            return !closed;
        }
    }

    private static boolean isDigits(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) < '0' || value.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    /** Accumulates the fields of the event being parsed. */
    private static final class PendingEvent {

        private final StringBuilder data = new StringBuilder();

        private final StringBuilder comment = new StringBuilder();

        private String id;

        private String event;

        private Long retry;

        private boolean hasData;

        private boolean hasComment;

        private void appendData(String value) {
            if (hasData) {
                data.append('\n');
            }
            data.append(value);
            hasData = true;
        }

        private void appendComment(String value) {
            if (hasComment) {
                comment.append('\n');
            }
            comment.append(value);
            hasComment = true;
        }

        private void resetFields() {
            id = null;
            event = null;
            retry = null;
        }

        private ServerSentEvent build() {
            if (!hasData && !hasComment) {
                return null;
            }
            ServerSentEvent.Builder builder = ServerSentEvent.builder();
            if (id != null) {
                builder.id(id);
            }
            if (event != null) {
                builder.event(event);
            }
            if (retry != null) {
                builder.retry(retry);
            }
            if (hasData) {
                builder.data(data.toString());
            }
            if (hasComment) {
                builder.comment(comment.toString());
            }
            return builder.build();
        }
    }

    /** Opens a new stream using the latest event ID. */
    @FunctionalInterface
    public interface EventStreamSource {

        /**
         * Opens an event stream.
         *
         * @param lastEventId The last parsed event ID, or an empty string before any ID was read.
         * @return The next UTF-8 stream, or null to stop reconnecting.
         * @throws IOException If opening the stream fails.
         */
        InputStream open(String lastEventId) throws IOException;
    }
}
