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
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.ServerSentEvent;

/**
 * A streaming {@code text/event-stream} representation. The writer runs on the connector's call
 * thread; each event is flushed before {@link EventSink#send(ServerSentEvent)} returns.
 */
public final class EventStreamRepresentation extends OutputRepresentation {

    private final EventWriter writer;

    private final Duration heartbeatInterval;

    /**
     * Creates an event stream.
     *
     * @param writer Writes events to the provided sink until the stream is complete.
     */
    public EventStreamRepresentation(EventWriter writer) {
        this(writer, null);
    }

    /**
     * Creates an event stream with optional automatic comment heartbeats.
     *
     * @param writer Writes events to the provided sink until the stream is complete.
     * @param heartbeatInterval The heartbeat interval, or null to disable automatic heartbeats.
     */
    public EventStreamRepresentation(EventWriter writer, Duration heartbeatInterval) {
        Objects.requireNonNull(writer, "writer");
        if (heartbeatInterval != null
                && (heartbeatInterval.isZero() || heartbeatInterval.isNegative())) {
            throw new IllegalArgumentException("The heartbeat interval must be positive");
        }
        super(MediaType.TEXT_EVENT_STREAM);
        this.writer = writer;
        this.heartbeatInterval = heartbeatInterval;
        setCharacterSet(CharacterSet.UTF_8);
        setTransient(true);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try (EventSink sink = new EventSink(outputStream)) {
            if (heartbeatInterval == null) {
                writer.write(sink);
                return;
            }

            AtomicBoolean stopped = new AtomicBoolean();
            AtomicReference<IOException> heartbeatFailure = new AtomicReference<>();
            Thread writerThread = Thread.currentThread();
            Thread heartbeatThread =
                    Thread.ofVirtual()
                            .name("restlet-sse-heartbeat")
                            .start(
                                    () ->
                                            writeHeartbeats(
                                                    sink, stopped, heartbeatFailure, writerThread));
            try {
                writer.write(sink);
            } catch (IOException | RuntimeException e) {
                stopHeartbeats(stopped, heartbeatThread, e);
                throw e;
            } finally {
                // Ensures the heartbeat thread stops even when the writer fails with an Error.
                stopped.set(true);
                heartbeatThread.interrupt();
            }
            stopHeartbeats(stopped, heartbeatThread, null);
            IOException failure = heartbeatFailure.get();
            if (failure != null) {
                throw failure;
            }
        }
    }

    private static void stopHeartbeats(
            AtomicBoolean stopped, Thread heartbeatThread, Exception writerFailure)
            throws IOException {
        stopped.set(true);
        heartbeatThread.interrupt();
        try {
            heartbeatThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            IOException interrupted =
                    new IOException("Interrupted while stopping SSE heartbeats", e);
            if (writerFailure == null) {
                throw interrupted;
            }
            writerFailure.addSuppressed(interrupted);
        }
    }

    private void writeHeartbeats(
            EventSink sink,
            AtomicBoolean stopped,
            AtomicReference<IOException> failure,
            Thread writerThread) {
        while (!stopped.get()) {
            try {
                Thread.sleep(heartbeatInterval);
            } catch (InterruptedException _) {
                if (!stopped.get()) {
                    Thread.currentThread().interrupt();
                }
                return;
            }
            if (stopped.get()) {
                return;
            }
            try {
                sink.send(ServerSentEvent.builder().comment("").build());
            } catch (IOException e) {
                failure.set(e);
                writerThread.interrupt();
                return;
            }
        }
    }

    /** Writes events for the lifetime of the response. */
    @FunctionalInterface
    public interface EventWriter {

        /**
         * Writes events until the stream is complete.
         *
         * @param sink The event sink.
         * @throws IOException If writing an event fails.
         */
        void write(EventSink sink) throws IOException;
    }

    /** A blocking sink for events in an {@link EventStreamRepresentation}. */
    public static final class EventSink implements AutoCloseable {

        private final OutputStream outputStream;

        private boolean closed;

        private EventSink(OutputStream outputStream) {
            this.outputStream = Objects.requireNonNull(outputStream, "outputStream");
        }

        /**
         * Encodes and flushes one event.
         *
         * @param event The event to send.
         * @throws IOException If the response stream has closed or writing fails.
         */
        public synchronized void send(ServerSentEvent event) throws IOException {
            Objects.requireNonNull(event, "event");
            if (closed) {
                throw new IOException("Event sink is closed");
            }

            writeField("id", event.getId());
            writeField("event", event.getEvent());
            writeMultilineField("data", event.getData());
            if (event.getRetry() != null) {
                writeField("retry", Long.toString(event.getRetry()));
            }
            writeMultilineComment(event.getComment());
            write("\n");
            outputStream.flush();
        }

        /**
         * Closes this sink without closing the connector-owned response stream.
         *
         * @throws IOException If flushing fails.
         */
        @Override
        public synchronized void close() throws IOException {
            if (!closed) {
                closed = true;
                outputStream.flush();
            }
        }

        private void writeField(String name, String value) throws IOException {
            if (value != null) {
                write(name + ":" + (value.isEmpty() ? "" : " " + value) + "\n");
            }
        }

        private void writeMultilineField(String name, String value) throws IOException {
            if (value != null) {
                for (String line : value.split("\\r\\n|\\r|\\n", -1)) {
                    write(name + ":" + (line.isEmpty() ? "" : " " + line) + "\n");
                }
            }
        }

        private void writeMultilineComment(String value) throws IOException {
            if (value != null) {
                for (String line : value.split("\\r\\n|\\r|\\n", -1)) {
                    write(":" + (line.isEmpty() ? "" : " " + line) + "\n");
                }
            }
        }

        private void write(String value) throws IOException {
            outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
