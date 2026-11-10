/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.ziti;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.EventLoop;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.openziti.ZitiAddress;
import org.openziti.ZitiContext;
import org.openziti.edge.model.ServiceDetail;

/**
 * A Netty channel over a connection of the OpenZiti SDK ({@link ZitiContext#open()}), with the
 * semantics of Netty's own socket channels.
 *
 * <ul>
 *   <li>One read at a time: {@link #doBeginRead()} is a no-op while a read is pending, so the
 *       connector can call {@code Channel.read()} at will, as Restlet's does with {@code AUTO_READ}
 *       off.
 *   <li>Writes don't block the event loop: one write is in flight at a time, and the next is
 *       started when it completes.
 *   <li>Every event is fired on the channel's event loop, never nested in the handler that asked
 *       for the read: the SDK completes a read or a write on its own threads, or inside the call
 *       when data is already buffered, so each completion is run as a task of the event loop.
 * </ul>
 *
 * <p>It replaces {@code org.openziti.netty.ZitiChannel} of ziti-netty 0.35.0, which fires its read
 * events from the thread completing the read: the SDK releases its pending read before calling the
 * completion handler, so a read requested during the delivery starts at once, and completes
 * concurrently on another thread or nested in the call, delivering bytes out of order. Its {@code
 * config()} also returns a new configuration on each call, so channel options such as {@code
 * AUTO_READ} are lost, and it blocks the event loop on every write.
 *
 * <p>The channel connects to an unresolved {@link InetSocketAddress} such as {@code
 * crm.internal:443}: it dials the Ziti service that intercepts it, so the host name is never
 * resolved by the system DNS. A {@link ZitiAddress} is dialed as is.
 */
final class ZitiSocketChannel extends AbstractChannel {

    private static final ChannelMetadata METADATA = new ChannelMetadata(false);

    /** The size of the buffer of a read, the payload size of a Ziti message. */
    private static final int READ_BUFFER_SIZE = 16 * 1024;

    /** The Ziti context dialing the services. */
    private final ZitiContext context;

    /** The socket of the Ziti connection. */
    private final AsynchronousSocketChannel socket;

    private final ChannelConfig config;

    /** Whether the connection is established. */
    private volatile boolean active;

    /** Whether a read is pending on the socket. Only used on the event loop. */
    private boolean readPending;

    /** Whether a write is in flight on the socket. Only used on the event loop. */
    private boolean writeInFlight;

    /**
     * Constructor.
     *
     * @param context The Ziti context dialing the services.
     */
    ZitiSocketChannel(ZitiContext context) {
        super(null);
        this.context = context;
        this.socket = context.open();
        this.config = new DefaultChannelConfig(this);
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    public boolean isOpen() {
        return socket.isOpen();
    }

    @Override
    public boolean isActive() {
        return active && socket.isOpen();
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return true;
    }

    @Override
    protected SocketAddress localAddress0() {
        try {
            return socket.getLocalAddress();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected SocketAddress remoteAddress0() {
        try {
            return socket.getRemoteAddress();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException("A Ziti connection can't be bound");
    }

    @Override
    protected void doDisconnect() throws IOException {
        doClose();
    }

    @Override
    protected void doClose() throws IOException {
        active = false;
        socket.close();
    }

    @Override
    protected Object filterOutboundMessage(Object msg) {
        if (msg instanceof ByteBuf) {
            return msg;
        }
        throw new UnsupportedOperationException(
                "Unsupported message type: " + msg.getClass().getName());
    }

    /** Runs a completion of the SDK on the event loop; returns false if the loop refused it. */
    private boolean onEventLoop(Runnable task) {
        try {
            eventLoop().execute(task);
            return true;
        } catch (RejectedExecutionException e) {
            // The event loop is shutting down, with the channel closed.
            return false;
        }
    }

    @Override
    protected void doBeginRead() {
        if (readPending || !isActive()) {
            return;
        }

        readPending = true;
        ByteBuf buffer = config.getAllocator().heapBuffer(READ_BUFFER_SIZE);
        ByteBuffer target = buffer.nioBuffer(buffer.writerIndex(), buffer.writableBytes());

        try {
            socket.read(target, null, new ReadHandler(buffer));
        } catch (RuntimeException e) {
            readPending = false;
            buffer.release();
            throw e;
        }
    }

    /** Fires the bytes of a completed read, on the event loop. */
    private void readCompleted(ByteBuf buffer, int read) {
        readPending = false;

        if (read < 0) {
            buffer.release();
            pipeline().fireChannelReadComplete();
            unsafe().close(voidPromise());
            return;
        }

        buffer.writerIndex(buffer.writerIndex() + read);
        pipeline().fireChannelRead(buffer);
        pipeline().fireChannelReadComplete();

        if (config.isAutoRead()) {
            read();
        }
    }

    /** Reports a failed read, on the event loop. */
    private void readFailed(ByteBuf buffer, Throwable cause) {
        readPending = false;
        buffer.release();

        if (isOpen()) {
            pipeline().fireExceptionCaught(cause);
            unsafe().close(voidPromise());
        }
    }

    /** Completes a read on the event loop. */
    private final class ReadHandler implements CompletionHandler<Integer, Void> {

        private final ByteBuf buffer;

        ReadHandler(ByteBuf buffer) {
            this.buffer = buffer;
        }

        @Override
        public void completed(Integer read, Void attachment) {
            if (!onEventLoop(() -> readCompleted(buffer, read))) {
                buffer.release();
            }
        }

        @Override
        public void failed(Throwable cause, Void attachment) {
            if (!onEventLoop(() -> readFailed(buffer, cause))) {
                buffer.release();
            }
        }
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) {
        if (writeInFlight) {
            // Resumed by the completion of the write in flight.
            return;
        }

        Object msg;
        while ((msg = in.current()) != null) {
            ByteBuf buffer = (ByteBuf) msg;

            if (!buffer.isReadable()) {
                in.remove();
                continue;
            }

            // Retained until the write completes: closing the channel meanwhile releases the
            // pending messages while the SDK may still read this one.
            writeInFlight = true;
            buffer.retain();
            try {
                socket.write(buffer.nioBuffer(), null, new WriteHandler(buffer));
            } catch (RuntimeException e) {
                writeInFlight = false;
                buffer.release();
                throw e;
            }
            return;
        }
    }

    /** Accounts for a completed write and starts the next one, on the event loop. */
    private void writeCompleted(int written) {
        writeInFlight = false;
        ChannelOutboundBuffer in = unsafe().outboundBuffer();

        if (in != null) {
            // Moves the reader index of the buffer written and releases it once fully written.
            in.removeBytes(written);
            unsafe().flush();
        }
    }

    /** Fails the pending writes and closes the channel, on the event loop. */
    private void writeFailed(Throwable cause) {
        writeInFlight = false;

        if (isOpen()) {
            pipeline().fireExceptionCaught(cause);
        }

        unsafe().close(voidPromise());
    }

    /** Completes a write on the event loop. */
    private final class WriteHandler implements CompletionHandler<Integer, Void> {

        /** The buffer written, retained for the write. */
        private final ByteBuf buffer;

        WriteHandler(ByteBuf buffer) {
            this.buffer = buffer;
        }

        @Override
        public void completed(Integer written, Void attachment) {
            buffer.release();
            onEventLoop(() -> writeCompleted(written));
        }

        @Override
        public void failed(Throwable cause, Void attachment) {
            buffer.release();
            onEventLoop(() -> writeFailed(cause));
        }
    }

    /**
     * Returns the Ziti address to dial for a remote address.
     *
     * @param remote An unresolved {@code host:port} or a Ziti address.
     * @return The Ziti address.
     */
    private SocketAddress dialAddress(SocketAddress remote) {
        if (remote instanceof ZitiAddress) {
            return remote;
        }

        if (remote instanceof InetSocketAddress address) {
            ServiceDetail service = context.getService(address);

            if (service != null) {
                return new ZitiAddress.Dial(service.getName(), null, null, null);
            }

            throw new UnresolvedAddressException();
        }

        throw new UnsupportedOperationException(
                "Unsupported address type: " + remote.getClass().getName());
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new ZitiUnsafe();
    }

    /** Connects with the SDK's asynchronous connect, completed on the event loop. */
    private final class ZitiUnsafe extends AbstractUnsafe {

        @Override
        public void connect(
                SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            SocketAddress dial;
            try {
                dial = dialAddress(remoteAddress);
            } catch (RuntimeException e) {
                promise.tryFailure(e);
                closeIfClosed();
                return;
            }

            int timeoutMillis = config.getConnectTimeoutMillis();
            ScheduledFuture<?> timeout =
                    (timeoutMillis > 0)
                            ? eventLoop()
                                    .schedule(
                                            () -> {
                                                if (promise.tryFailure(
                                                        new ConnectTimeoutException(
                                                                "Connection timed out after "
                                                                        + timeoutMillis
                                                                        + " ms: "
                                                                        + remoteAddress))) {
                                                    close(voidPromise());
                                                }
                                            },
                                            timeoutMillis,
                                            TimeUnit.MILLISECONDS)
                            : null;

            try {
                socket.connect(
                        dial,
                        null,
                        new CompletionHandler<Void, Void>() {
                            @Override
                            public void completed(Void result, Void attachment) {
                                onEventLoop(() -> connected(promise, timeout));
                            }

                            @Override
                            public void failed(Throwable cause, Void attachment) {
                                onEventLoop(
                                        () ->
                                                connectFailed(
                                                        promise, timeout, remoteAddress, cause));
                            }
                        });
            } catch (RuntimeException e) {
                connectFailed(promise, timeout, remoteAddress, e);
            }
        }

        private void connected(ChannelPromise promise, ScheduledFuture<?> timeout) {
            if (timeout != null) {
                timeout.cancel(false);
            }

            active = true;

            if (promise.trySuccess()) {
                pipeline().fireChannelActive();
            } else {
                // Timed out or cancelled meanwhile.
                close(voidPromise());
            }
        }

        private void connectFailed(
                ChannelPromise promise,
                ScheduledFuture<?> timeout,
                SocketAddress remoteAddress,
                Throwable cause) {
            if (timeout != null) {
                timeout.cancel(false);
            }

            Throwable failure = cause;
            if (cause instanceof IOException && !(cause instanceof ClosedChannelException)) {
                ConnectException exception =
                        new ConnectException(cause.getMessage() + ": " + remoteAddress);
                exception.initCause(cause);
                failure = exception;
            }

            promise.tryFailure(failure);
            closeIfClosed();
        }
    }
}
