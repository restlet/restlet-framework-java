/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket.internal;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.ext.websocket.WebSocketMessage;
import org.restlet.ext.websocket.WebSocketSession;

/**
 * WebSocket session over a Netty channel whose pipeline decodes WebSocket frames and aggregates
 * fragmented messages. Received messages are queued for a blocking consumer; the channel is read on
 * demand, so a slow consumer slows down the peer. Pings are answered with pongs, and an idle
 * connection is pinged once before being closed.
 *
 * <p>This class is internal to the extension.
 */
final class NettyWebSocketSession implements WebSocketSession {

    /** Marks the end of the inbound messages. */
    private static final Object END = new Object();

    /** Number of queued messages above which the channel is no longer read. */
    private static final int MAX_QUEUED_MESSAGES = 16;

    /** Maximum time to wait for the peer to complete the close handshake. */
    private static final long CLOSE_TIMEOUT_MS = 5000;

    private final Channel channel;

    private volatile String subprotocol;

    private final int maxMessageSize;

    private final long writeTimeoutMs;

    private final Logger logger;

    private final LinkedBlockingQueue<Object> inbound = new LinkedBlockingQueue<>();

    private final AtomicBoolean readPaused = new AtomicBoolean();

    private final AtomicBoolean closeSent = new AtomicBoolean();

    private final ReentrantLock sendLock = new ReentrantLock();

    private final ReentrantLock writabilityLock = new ReentrantLock();

    private final Condition writable = writabilityLock.newCondition();

    private volatile boolean closeReceived;

    private volatile int closeCode = -1;

    private volatile String closeReason;

    private volatile boolean pingOutstanding;

    /**
     * Constructor.
     *
     * @param channel The channel carrying the session.
     * @param subprotocol The negotiated subprotocol, or null.
     * @param maxMessageSize The maximum size in bytes of a received message.
     * @param writeTimeoutMs The maximum time to wait for the channel to become writable, 0 for no
     *     limit.
     * @param logger The logger.
     */
    NettyWebSocketSession(
            Channel channel,
            String subprotocol,
            int maxMessageSize,
            long writeTimeoutMs,
            Logger logger) {
        this.channel = channel;
        setSubprotocol(subprotocol);
        this.maxMessageSize = maxMessageSize;
        this.writeTimeoutMs = writeTimeoutMs;
        this.logger = logger;
    }

    /**
     * Creates the inbound handler feeding this session, to add at the end of the pipeline.
     *
     * @return The handler.
     */
    ChannelHandler newHandler() {
        return new Handler();
    }

    /**
     * Sets the negotiated subprotocol, known once the client handshake completes.
     *
     * @param subprotocol The subprotocol, or null.
     */
    void setSubprotocol(String subprotocol) {
        this.subprotocol = (subprotocol == null || subprotocol.isEmpty()) ? null : subprotocol;
    }

    /** Starts reading messages on demand. */
    void start() {
        channel.config().setAutoRead(false);
        channel.read();
    }

    @Override
    public String getSubprotocol() {
        return subprotocol;
    }

    @Override
    public WebSocketMessage receive() throws IOException {
        Object next;
        try {
            next = inbound.take();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while receiving a WebSocket message");
        }
        if (next == END) {
            inbound.offer(END);
            return null;
        }
        if (inbound.size() < MAX_QUEUED_MESSAGES && readPaused.compareAndSet(true, false)) {
            channel.read();
        }
        return (WebSocketMessage) next;
    }

    @Override
    public void sendText(String text) throws IOException {
        send(new TextWebSocketFrame(text));
    }

    @Override
    public void sendBinary(byte[] data) throws IOException {
        send(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(data.clone())));
    }

    private void send(WebSocketFrame frame) throws IOException {
        sendLock.lock();
        try {
            if (!isOpen()) {
                throw new IOException("The WebSocket session is closed");
            }
            awaitWritable();
            ChannelFuture future = channel.writeAndFlush(frame);
            frame = null;
            if (future.isDone() && !future.isSuccess()) {
                throw new IOException("Unable to send the WebSocket message", future.cause());
            }
        } finally {
            sendLock.unlock();
            if (frame != null) {
                frame.release();
            }
        }
    }

    /** Blocks until the channel is writable, so that a slow peer slows down the sender. */
    private void awaitWritable() throws IOException {
        if (channel.isWritable()) {
            return;
        }
        channel.flush();
        long remaining =
                (writeTimeoutMs > 0)
                        ? TimeUnit.MILLISECONDS.toNanos(writeTimeoutMs)
                        : Long.MAX_VALUE;
        writabilityLock.lock();
        try {
            while (!channel.isWritable()) {
                if (!channel.isActive()) {
                    throw new IOException("Connection closed by peer");
                }
                if (remaining <= 0) {
                    throw new InterruptedIOException("WebSocket write timed out");
                }
                // Wake up periodically in case a signal raced with the check above.
                long slice = Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(100));
                long left = writable.awaitNanos(slice);
                if (writeTimeoutMs > 0) {
                    remaining -= (slice - Math.max(0, left));
                }
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while sending a WebSocket message");
        } finally {
            writabilityLock.unlock();
        }
    }

    private void signalWritability() {
        writabilityLock.lock();
        try {
            writable.signalAll();
        } finally {
            writabilityLock.unlock();
        }
    }

    @Override
    public void close(int code, String reason) throws IOException {
        if (closeSent.compareAndSet(false, true) && channel.isActive()) {
            channel.writeAndFlush(new CloseWebSocketFrame(code, reason));
        }
        try {
            if (!channel.closeFuture().await(CLOSE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                channel.close();
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            channel.close();
            throw new InterruptedIOException("Interrupted while closing the WebSocket session");
        }
    }

    @Override
    public boolean isOpen() {
        return channel.isActive() && !closeSent.get() && !closeReceived;
    }

    @Override
    public int getCloseCode() {
        return closeCode;
    }

    @Override
    public String getCloseReason() {
        return closeReason;
    }

    private void enqueue(WebSocketMessage message) {
        inbound.offer(message);
    }

    private void onClose(ChannelHandlerContext ctx, int status, String reason) {
        closeCode = (status < 0) ? NO_STATUS : status;
        closeReason = (reason == null || reason.isEmpty()) ? null : reason;
        closeReceived = true;
        if (closeSent.compareAndSet(false, true)) {
            int echo =
                    WebSocketCloseStatus.isValidStatusCode(closeCode) ? closeCode : NORMAL_CLOSURE;
            ctx.writeAndFlush(new CloseWebSocketFrame(echo, null))
                    .addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.close();
        }
        inbound.offer(END);
    }

    private void failSession(ChannelHandlerContext ctx, int code) {
        if (closeSent.compareAndSet(false, true) && ctx.channel().isActive()) {
            ctx.writeAndFlush(new CloseWebSocketFrame(code, null))
                    .addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.close();
        }
    }

    /** Converts frames into queued messages and handles the control frames. */
    private final class Handler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                pingOutstanding = false;
                if (msg instanceof WebSocketFrame frame
                        && !(msg instanceof CloseWebSocketFrame)
                        && frame.content().readableBytes() > maxMessageSize) {
                    failSession(ctx, MESSAGE_TOO_BIG);
                } else if (msg instanceof TextWebSocketFrame text) {
                    enqueue(WebSocketMessage.text(text.text()));
                } else if (msg instanceof BinaryWebSocketFrame binary) {
                    enqueue(WebSocketMessage.binary(ByteBufUtil.getBytes(binary.content())));
                } else if (msg instanceof PingWebSocketFrame ping) {
                    ctx.writeAndFlush(new PongWebSocketFrame(ping.content().retain()));
                } else if (msg instanceof CloseWebSocketFrame close) {
                    onClose(ctx, close.statusCode(), close.reasonText());
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            if (!closeReceived && ctx.channel().isActive()) {
                if (inbound.size() < MAX_QUEUED_MESSAGES) {
                    ctx.read();
                } else {
                    readPaused.set(true);
                    // The consumer may have drained the queue before the flag was set.
                    if (inbound.size() < MAX_QUEUED_MESSAGES
                            && readPaused.compareAndSet(true, false)) {
                        ctx.read();
                    }
                }
            }
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            if (ctx.channel().isWritable()) {
                signalWritability();
            }
            super.channelWritabilityChanged(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            signalWritability();
            if (!closeReceived) {
                closeCode = ABNORMAL_CLOSURE;
                closeReceived = true;
            }
            inbound.offer(END);
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                if (pingOutstanding) {
                    ctx.close();
                } else {
                    pingOutstanding = true;
                    ctx.writeAndFlush(new PingWebSocketFrame());
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            int code = INTERNAL_ERROR;
            if (cause instanceof CorruptedWebSocketFrameException corrupted) {
                code = corrupted.closeStatus().code();
            } else if (cause instanceof TooLongFrameException) {
                code = MESSAGE_TOO_BIG;
            } else {
                logger.log(Level.FINE, "WebSocket session failure", cause);
            }
            failSession(ctx, code);
        }
    }
}
