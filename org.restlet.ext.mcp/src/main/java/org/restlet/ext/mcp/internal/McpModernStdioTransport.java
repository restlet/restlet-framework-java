/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.client.transport.McpClientTransport;
import io.modelcontextprotocol.modern.client.transport.McpTransportRequest;
import io.modelcontextprotocol.modern.client.transport.StdioMcpClientTransport;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Flux;

/**
 * Sends MCP {@code 2026-07-28} requests over a stdio channel, through the SDK's stdio transport,
 * which routes the responses, progress and subscription notifications to their requests, and sends
 * {@code notifications/cancelled} when one is cancelled. The SDK's transport reads and writes
 * streams that are views of the channel: its input gives the frames the channel delivers, each
 * followed by a newline, and ends when this transport closes or the channel ends; its output splits
 * what is written into frames at newlines. Closing this transport detaches it from the channel,
 * which stays open for another client, and drops the late responses to the requests left
 * unanswered. Calls in flight are limited, listen streams excepted.
 */
public final class McpModernStdioTransport implements McpClientTransport, AutoCloseable {

    /** The most frames received but not yet read by the SDK's transport. */
    private static final int MAX_BUFFERED = 64;

    private final McpStdioChannel channel;
    private final int maxConcurrentCalls;
    private final AtomicInteger calls = new AtomicInteger();
    private final Frames frames = new Frames();
    private final StdioMcpClientTransport sdk;

    /**
     * Attaches to a channel, opening it with the message limit if not yet open.
     *
     * @param channel The channel.
     * @param mapper The JSON mapper.
     * @param maxMessageBytes The size limit of each message, sent or received.
     * @param maxConcurrentCalls The number of calls in flight at once.
     * @param shutdownTimeout How long closing waits.
     * @throws IOException If the channel fails to open.
     */
    public McpModernStdioTransport(
            McpStdioChannel channel,
            McpJsonMapper mapper,
            int maxMessageBytes,
            int maxConcurrentCalls,
            Duration shutdownTimeout)
            throws IOException {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.maxConcurrentCalls = maxConcurrentCalls;
        channel.open(maxMessageBytes);
        channel.attach(frames);
        try {
            sdk =
                    StdioMcpClientTransport.builder(
                                    new FrameInput(), new FrameOutput(maxMessageBytes))
                            .jsonMapper(mapper)
                            .inputMaxSize(maxMessageBytes)
                            .shutdownTimeout(shutdownTimeout)
                            .start();
        } catch (RuntimeException e) {
            frames.close();
            throw e;
        }
    }

    @Override
    public Flux<JsonRpc.JSONRPCMessage> exchange(McpTransportRequest request) {
        if (McpSchema.METHOD_SUBSCRIPTIONS_LISTEN.equals(request.method())) {
            return sdk.exchange(request);
        }
        return Flux.defer(
                () -> {
                    if (calls.incrementAndGet() > maxConcurrentCalls) {
                        calls.decrementAndGet();
                        return Flux.error(
                                new IOException(
                                        "MCP STDIO call limit of "
                                                + maxConcurrentCalls
                                                + " reached"));
                    }
                    return sdk.exchange(request).doFinally(signal -> calls.decrementAndGet());
                });
    }

    /** Ends the requests in flight, and detaches from the channel. */
    @Override
    public void close() {
        try {
            sdk.close();
        } finally {
            frames.close();
        }
    }

    /** The frames the channel delivers, until closed or the channel ends. */
    private final class Frames implements McpStdioChannel.Sink {
        private final ArrayDeque<byte[]> queue = new ArrayDeque<>();
        private IOException failure;
        private boolean ended;
        private boolean closed;

        @Override
        public synchronized void receive(byte[] frame) {
            // Holds back the channel's reader while the SDK's transport catches up.
            while (!closed && queue.size() >= MAX_BUFFERED) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (!closed) {
                queue.add(frame);
                notifyAll();
            }
        }

        @Override
        public synchronized void end(IOException cause) {
            ended = true;
            if (!(cause instanceof EOFException)) {
                failure = cause;
            }
            notifyAll();
        }

        /** The next frame, null at the end. */
        synchronized byte[] take() throws IOException {
            while (queue.isEmpty() && !ended && !closed) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException("MCP STDIO read interrupted");
                }
            }
            if (closed) {
                return null;
            }
            byte[] frame = queue.poll();
            if (frame == null && failure != null) {
                throw new IOException("MCP STDIO read failed", failure);
            }
            notifyAll();
            return frame;
        }

        void close() {
            synchronized (this) {
                if (closed) {
                    return;
                }
                closed = true;
                queue.clear();
                notifyAll();
            }
            channel.detach(this);
        }
    }

    /** The input of the SDK's transport: each frame, then a newline. */
    private final class FrameInput extends InputStream {
        private byte[] current;
        private int position;
        private boolean done;

        @Override
        public int read() throws IOException {
            byte[] single = new byte[1];
            return read(single, 0, 1) == -1 ? -1 : single[0] & 0xff;
        }

        @Override
        public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, buffer.length);
            if (length == 0) {
                return 0;
            }
            if (done) {
                return -1;
            }
            if (current == null || position > current.length) {
                current = frames.take();
                position = 0;
                if (current == null) {
                    done = true;
                    return -1;
                }
            }
            if (position == current.length) {
                buffer[offset] = '\n';
                position++;
                return 1;
            }
            int count = Math.min(length, current.length - position);
            System.arraycopy(current, position, buffer, offset, count);
            position += count;
            return count;
        }

        @Override
        public void close() {
            frames.close();
        }
    }

    /** The output of the SDK's transport: a frame written to the channel at each newline. */
    private final class FrameOutput extends OutputStream {
        private final int limit;
        private final ByteArrayOutputStream pending;

        FrameOutput(int limit) {
            this.limit = limit;
            pending = new ByteArrayOutputStream(Math.min(limit, 8192));
        }

        @Override
        public void write(int value) throws IOException {
            write(new byte[] {(byte) value}, 0, 1);
        }

        @Override
        public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, bytes.length);
            int start = offset;
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                if (bytes[i] == '\n') {
                    append(bytes, start, i - start);
                    byte[] frame = pending.toByteArray();
                    pending.reset();
                    channel.write(frames, frame);
                    start = i + 1;
                }
            }
            append(bytes, start, end - start);
        }

        private void append(byte[] bytes, int offset, int length) throws IOException {
            if (pending.size() + length > limit) {
                pending.reset();
                throw new IOException("MCP encoded message exceeds " + limit + " bytes");
            }
            pending.write(bytes, offset, length);
        }

        @Override
        public void close() {
            frames.close();
        }
    }
}
