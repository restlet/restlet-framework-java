/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.util.StreamTransport;

/**
 * The MCP framing of one stdio stream, one JSON-RPC message per line, shared by the clients that
 * use it in turn: a {@code 2026-07-28} client probing the server, then the legacy client if the
 * probe fails. It owns the stream's only reader, a virtual thread, and serializes the writes.
 *
 * <p>Frames go to the attached sink, one at a time and in order, on the reader thread. Frames read
 * while no sink is attached are queued, within a bound, and delivered to the next one. The ids of
 * the requests a sink wrote and left unanswered when it detached are remembered, within a bound, so
 * that their late responses are dropped rather than reaching the next sink, which never sent them.
 * The end of the stream, or a read failure, is told to the attached sink, or else to the next one.
 */
public final class McpStdioChannel {

    /** Receives the frames of a channel. */
    public interface Sink {

        /**
         * Receives one frame, without its newline, on the reader thread.
         *
         * @param frame The frame.
         */
        void receive(byte[] frame);

        /**
         * Tells that no more frames will come.
         *
         * @param failure An {@link EOFException} for the end of the stream, or the read failure.
         */
        void end(IOException failure);
    }

    /** The most frames queued while no sink is attached. */
    static final int MAX_QUEUED = 64;

    /** The most request ids remembered, unanswered or abandoned. */
    static final int MAX_IDS = 1024;

    private static final Logger LOGGER = Logger.getLogger(McpStdioChannel.class.getName());

    private static final TypeRef<Map<String, Object>> MAP = new TypeRef<>() {};

    private final StreamTransport stream;
    private final McpJsonMapper mapper = McpJsonDefaults.getMapper();
    private final ReentrantLock delivery = new ReentrantLock();
    private final Object writing = new Object();
    private final ExecutorService writer =
            Executors.newSingleThreadExecutor(
                    Thread.ofVirtual().name("restlet-mcp-stdio-writer").factory());
    private final ArrayDeque<byte[]> queued = new ArrayDeque<>();
    private final SequencedSet<Object> pending = new LinkedHashSet<>();
    private final SequencedSet<Object> abandoned = new LinkedHashSet<>();
    private int maxMessageBytes;
    private Sink sink;
    private IOException ended;
    private Thread reader;
    private volatile boolean closed;

    /**
     * @param stream The unstarted stream, which {@link #close(Duration)} stops.
     */
    public McpStdioChannel(StreamTransport stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
    }

    /**
     * Starts the stream and its reader, once, with a message limit, which later calls must repeat.
     *
     * @param limit The largest message, in bytes.
     * @throws IOException If the stream fails to start.
     * @throws IllegalStateException If closed, or already opened with another limit.
     */
    public synchronized void open(int limit) throws IOException {
        if (closed) {
            throw new IllegalStateException("MCP STDIO channel is closed");
        }
        if (reader != null) {
            if (limit != maxMessageBytes) {
                throw new IllegalStateException("MCP STDIO limits are fixed once connected");
            }
            return;
        }
        if (limit <= 0 || limit == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Positive finite MCP byte limit required");
        }
        stream.setMaxOperationBytes(limit + 1);
        stream.start();
        maxMessageBytes = limit;
        reader = Thread.ofVirtual().name("restlet-mcp-stdio-reader").start(() -> read(limit));
    }

    /**
     * The message limit, once opened.
     *
     * @return The largest message, in bytes, or zero before {@link #open(int)}.
     */
    public synchronized int maxMessageBytes() {
        return maxMessageBytes;
    }

    /**
     * Attaches the sink of the frames, then delivers it those queued, or the end of the stream.
     *
     * @param next The sink.
     * @throws IllegalStateException If another sink is attached.
     */
    public void attach(Sink next) {
        Objects.requireNonNull(next, "sink");
        delivery.lock();
        try {
            List<byte[]> backlog;
            IOException end;
            synchronized (this) {
                if (sink != null) {
                    throw new IllegalStateException("MCP STDIO channel is already in use");
                }
                sink = next;
                backlog = new ArrayList<>(queued);
                queued.clear();
                end = ended;
            }
            for (byte[] frame : backlog) {
                next.receive(frame);
            }
            if (end != null) {
                next.end(end);
            }
        } finally {
            delivery.unlock();
        }
    }

    /**
     * Detaches a sink, if attached: the responses to its unanswered requests will be dropped.
     *
     * @param current The sink.
     */
    public void detach(Sink current) {
        synchronized (writing) {
            synchronized (this) {
                if (sink != current) {
                    return;
                }
                sink = null;
                for (Object id : pending) {
                    remember(abandoned, id);
                }
                pending.clear();
            }
        }
    }

    /**
     * Writes a frame for the attached sink, remembering the id of a request until it is answered.
     *
     * @param owner The sink writing.
     * @param bytes The message, without newline.
     * @throws IOException If the sink is not attached, the message too large, or the write fails.
     */
    public void write(Sink owner, byte[] bytes) throws IOException {
        synchronized (writing) {
            Object id = null;
            synchronized (this) {
                if (closed || sink != owner) {
                    throw new IOException("MCP STDIO channel is not attached");
                }
                if (ended != null) {
                    throw new IOException("MCP STDIO stream ended", ended);
                }
                Map<String, Object> message = parse(bytes);
                if (message != null && message.get("method") instanceof String method) {
                    if (message.get("id") != null) {
                        id = McpMessages.idKey(message.get("id"));
                        abandoned.remove(id); // Its id reused: the next response is for it.
                        remember(pending, id);
                    } else if ("notifications/cancelled".equals(method)
                            && message.get("params") instanceof Map<?, ?> params
                            && params.get("requestId") != null) {
                        // Its sink no longer awaits a response, which may never come.
                        Object cancelled = McpMessages.idKey(params.get("requestId"));
                        if (pending.remove(cancelled)) {
                            remember(abandoned, cancelled);
                        }
                    }
                }
            }
            try {
                writeFrame(bytes);
            } catch (IOException | RuntimeException e) {
                if (id != null) {
                    synchronized (this) {
                        pending.remove(id);
                    }
                }
                throw e;
            }
        }
    }

    /**
     * Writes a frame on the writer thread, waiting for it even if interrupted: the calling thread,
     * such as a scheduler's that an SDK transport disposes of, may be interrupted, which would
     * close an interruptible stream, such as a pipe, for the clients to come.
     */
    private void writeFrame(byte[] bytes) throws IOException {
        Future<?> written;
        try {
            written =
                    writer.submit(
                            () -> {
                                McpMessages.writeFrame(stream, bytes);
                                return null;
                            });
        } catch (RejectedExecutionException e) {
            throw new IOException("MCP STDIO channel is closed", e);
        }
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    written.get();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof IOException io) {
                        throw io;
                    }
                    if (e.getCause() instanceof RuntimeException runtime) {
                        throw runtime;
                    }
                    throw new IOException(e.getCause());
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Adds to a bounded set of ids, forgetting the oldest beyond {@link #MAX_IDS}. */
    private static void remember(SequencedSet<Object> ids, Object id) {
        ids.add(id);
        if (ids.size() > MAX_IDS) {
            ids.removeFirst();
        }
    }

    private Map<String, Object> parse(byte[] bytes) {
        try {
            return mapper.readValue(McpMessages.utf8(bytes), MAP);
        } catch (IOException | RuntimeException e) {
            return null; // Not a JSON object: the sink tells what it makes of it.
        }
    }

    private void read(int limit) {
        IOException end;
        try {
            byte[] frame;
            while ((frame = McpMessages.readFrame(stream, limit)) != null) {
                dispatch(frame);
            }
            end = new EOFException("MCP subprocess or pipe ended");
        } catch (IOException e) {
            end = e;
        } catch (RuntimeException e) {
            end = new IOException(e);
        }
        if (closed) {
            return;
        }
        delivery.lock();
        try {
            Sink current;
            synchronized (this) {
                ended = end;
                current = sink;
            }
            if (current != null) {
                current.end(end);
            }
        } finally {
            delivery.unlock();
        }
    }

    /** Delivers a frame to the attached sink, queues it, or drops an abandoned response. */
    private void dispatch(byte[] frame) {
        delivery.lock();
        try {
            Sink current;
            synchronized (this) {
                if (!pending.isEmpty() || !abandoned.isEmpty()) {
                    Map<String, Object> message = parse(frame);
                    if (message != null
                            && !message.containsKey("method")
                            && message.get("id") != null) {
                        Object id = McpMessages.idKey(message.get("id"));
                        if (!pending.remove(id) && abandoned.remove(id)) {
                            LOGGER.log(
                                    Level.FINE,
                                    "Dropped the late MCP STDIO response to abandoned request {0}",
                                    id);
                            return;
                        }
                    }
                }
                current = sink;
                if (current == null) {
                    if (queued.size() == MAX_QUEUED) {
                        LOGGER.warning("Dropped an MCP STDIO message no client was ready for");
                        queued.removeFirst();
                    }
                    queued.add(frame);
                    return;
                }
            }
            current.receive(frame);
        } finally {
            delivery.unlock();
        }
    }

    /**
     * Stops the stream, and waits for the reader to end.
     *
     * @param timeout The time to wait for the reader.
     * @throws IOException If the stream fails to stop, or the reader to end in time.
     */
    public void close(Duration timeout) throws IOException {
        Thread current;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            current = reader;
            sink = null;
            queued.clear();
            pending.clear();
            abandoned.clear();
        }
        writer.shutdown();
        IOException error = null;
        try {
            stream.stop();
        } catch (IOException e) {
            error = e;
        }
        try {
            if (current != null && !current.join(timeout)) {
                IOException join = new IOException("MCP STDIO reader shutdown timed out");
                if (error == null) {
                    error = join;
                } else {
                    error.addSuppressed(join);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            IOException interrupted = new InterruptedIOException("Interrupted");
            if (error == null) {
                error = interrupted;
            } else {
                error.addSuppressed(interrupted);
            }
        }
        if (error != null) {
            throw error;
        }
    }
}
