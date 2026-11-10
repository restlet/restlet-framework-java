/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.ziti;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openziti.ZitiAddress;
import org.openziti.ZitiContext;
import org.openziti.edge.model.ServiceDetail;

/**
 * Checks that {@link ZitiSocketChannel} gives a Ziti connection the semantics of Netty's own socket
 * channels, over a scripted SDK socket.
 */
class ZitiSocketChannelTestCase {

    private static final InetSocketAddress SERVICE =
            InetSocketAddress.createUnresolved("crm.internal", 443);

    private EventLoopGroup group;

    private ScriptedSocket socket;

    private ZitiSocketChannel channel;

    /** What the pipeline received. */
    private final Recorder recorder = new Recorder();

    @BeforeEach
    void start() throws Exception {
        group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        socket = new ScriptedSocket();
        channel = new ZitiSocketChannel(fakeZiti(socket));
        channel.config().setAutoRead(false);
        channel.pipeline().addLast(recorder);
        group.register(channel).sync();
    }

    @AfterEach
    void stop() throws Exception {
        channel.close().sync();
        group.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
    }

    /** A Ziti context intercepting crm.internal:443 with the "crm" service. */
    private static ZitiContext fakeZiti(AsynchronousSocketChannel socket) {
        return (ZitiContext)
                Proxy.newProxyInstance(
                        ZitiContext.class.getClassLoader(),
                        new Class<?>[] {ZitiContext.class},
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "getService" ->
                                            SERVICE.equals(args[0])
                                                    ? new ServiceDetail().name("crm")
                                                    : null;
                                    case "open" -> socket;
                                    case "toString" -> "fake-ziti";
                                    case "hashCode" -> System.identityHashCode(proxy);
                                    case "equals" -> proxy == args[0];
                                    default ->
                                            throw new UnsupportedOperationException(
                                                    method.getName());
                                });
    }

    /** Runs a task on the event loop and waits for it. */
    private void onEventLoop(Runnable task) throws Exception {
        channel.eventLoop().submit(task).get(5, TimeUnit.SECONDS);
    }

    private void connect() throws Exception {
        channel.connect(SERVICE).sync();
    }

    @Test
    void dialsTheServiceInterceptingTheOrigin() throws Exception {
        connect();

        assertTrue(channel.isActive());
        ZitiAddress.Dial dialed = assertInstanceOf(ZitiAddress.Dial.class, socket.dialed);
        assertEquals("crm", dialed.getService());
        assertTrue(recorder.active.get());
    }

    @Test
    void dialsAZitiAddressAsIs() throws Exception {
        ZitiAddress.Dial address = new ZitiAddress.Dial("billing", null, null, null);

        channel.connect(address).sync();

        assertSame(address, socket.dialed);
    }

    @Test
    void failsToConnectToAnOriginWithoutService() {
        ChannelFuture future =
                channel.connect(InetSocketAddress.createUnresolved("other.internal", 443))
                        .awaitUninterruptibly();

        assertInstanceOf(UnresolvedAddressException.class, future.cause());
        assertEquals(0, socket.connects.get());
    }

    @Test
    void reportsAConnectFailure() {
        socket.connectFailure = new IOException("no terminators");

        ChannelFuture future = channel.connect(SERVICE).awaitUninterruptibly();

        ConnectException failure = assertInstanceOf(ConnectException.class, future.cause());
        assertSame(socket.connectFailure, failure.getCause());
        assertFalse(channel.isActive());
    }

    @Test
    void timesOutAConnect() {
        socket.connectHangs = true;
        channel.config().setOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100);

        ChannelFuture future = channel.connect(SERVICE).awaitUninterruptibly();

        assertInstanceOf(ConnectTimeoutException.class, future.cause());
        assertTrue(channel.closeFuture().awaitUninterruptibly(5, TimeUnit.SECONDS));
    }

    @Test
    void refusesToBind() {
        ChannelFuture future = channel.bind(new InetSocketAddress(0)).awaitUninterruptibly();

        assertInstanceOf(UnsupportedOperationException.class, future.cause());
    }

    @Test
    void readsOneAtATime() throws Exception {
        connect();

        onEventLoop(
                () -> {
                    channel.read();
                    channel.read();
                    channel.read();
                });

        assertEquals(1, socket.reads.get(), "A read requested while one is pending is ignored");

        // Completed by an SDK thread: the bytes are fired on the event loop.
        socket.completePendingRead("hello");
        recorder.received.get(5, TimeUnit.SECONDS);

        assertEquals("hello", recorder.text());
        assertTrue(recorder.allOnEventLoop.get());

        onEventLoop(channel::read);
        assertEquals(2, socket.reads.get(), "A read is accepted once the pending one completed");
    }

    @Test
    void neverFiresAReadNestedInTheReadRequest() throws Exception {
        connect();
        // The SDK completes the read inside read(), the data being buffered already.
        socket.bufferedReads.add("buffered");
        AtomicBoolean inRead = new AtomicBoolean();
        recorder.onRead = bytes -> recorder.nested.compareAndSet(false, inRead.get());

        onEventLoop(
                () -> {
                    inRead.set(true);
                    channel.read();
                    inRead.set(false);
                });
        recorder.received.get(5, TimeUnit.SECONDS);

        assertEquals("buffered", recorder.text());
        assertFalse(recorder.nested.get(), "The read was fired nested in Channel.read()");
    }

    @Test
    void readsOnItsOwnWithAutoRead() throws Exception {
        connect();
        socket.bufferedReads.add("first");
        socket.bufferedReads.add("second");

        channel.config().setAutoRead(true);
        recorder.awaitText("firstsecond");

        assertEquals(3, socket.reads.get(), "A read is started after each one");
    }

    @Test
    void closesAtTheEndOfTheStream() throws Exception {
        connect();
        socket.bufferedReads.add(-1);

        onEventLoop(channel::read);

        assertTrue(channel.closeFuture().await(5, TimeUnit.SECONDS));
        assertFalse(recorder.active.get());
    }

    @Test
    void reportsAReadFailure() throws Exception {
        connect();
        IOException failure = new IOException("connection reset");
        socket.bufferedReads.add(failure);

        onEventLoop(channel::read);

        assertTrue(channel.closeFuture().await(5, TimeUnit.SECONDS));
        assertSame(failure, recorder.failures.get(0));
    }

    @Test
    void writesInOrderWithoutBlocking() throws Exception {
        connect();
        // The SDK accepts 3 bytes per write, completing the write inside the call.
        socket.maxWrite = 3;

        channel.write(Unpooled.copiedBuffer("hello ", StandardCharsets.UTF_8));
        channel.write(Unpooled.EMPTY_BUFFER);
        ChannelFuture last =
                channel.writeAndFlush(Unpooled.copiedBuffer("over ziti", StandardCharsets.UTF_8));

        assertTrue(last.await(5, TimeUnit.SECONDS));
        assertTrue(last.isSuccess());
        assertArrayEquals(
                "hello over ziti".getBytes(StandardCharsets.UTF_8), socket.written.toByteArray());
        assertEquals(1, socket.maxWritesInFlight.get(), "One write is in flight at a time");
    }

    @Test
    void reportsAWriteFailure() throws Exception {
        connect();
        socket.writeFailure = new IOException("broken pipe");

        ChannelFuture future =
                channel.writeAndFlush(Unpooled.copiedBuffer("lost", StandardCharsets.UTF_8))
                        .awaitUninterruptibly();

        assertFalse(future.isSuccess());
        assertTrue(channel.closeFuture().await(5, TimeUnit.SECONDS));
        assertSame(socket.writeFailure, recorder.failures.get(0));
    }

    @Test
    void rejectsMessagesOtherThanBuffers() throws Exception {
        connect();

        ChannelFuture future = channel.writeAndFlush("text").awaitUninterruptibly();

        assertInstanceOf(UnsupportedOperationException.class, future.cause());
    }

    /** Records what reaches the end of the pipeline. */
    private final class Recorder extends ChannelInboundHandlerAdapter {

        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        final CompletableFuture<Void> received = new CompletableFuture<>();

        final AtomicBoolean active = new AtomicBoolean();

        final AtomicBoolean allOnEventLoop = new AtomicBoolean(true);

        final AtomicBoolean nested = new AtomicBoolean();

        final CopyOnWriteArrayList<Throwable> failures = new CopyOnWriteArrayList<>();

        volatile Consumer<byte[]> onRead = b -> {};

        synchronized String text() {
            return bytes.toString(StandardCharsets.UTF_8);
        }

        void awaitText(String expected) throws InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!text().equals(expected) && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
            assertEquals(expected, text());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            active.set(true);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            active.set(false);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            allOnEventLoop.compareAndSet(true, ctx.channel().eventLoop().inEventLoop());
            ByteBuf buffer = (ByteBuf) msg;
            byte[] read = new byte[buffer.readableBytes()];
            buffer.readBytes(read);
            buffer.release();
            synchronized (this) {
                bytes.writeBytes(read);
            }
            onRead.accept(read);
            received.complete(null);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            failures.add(cause);
        }
    }

    /**
     * A socket of the SDK, scripted by the test. A read completes inside the call with the next
     * buffered result, or stays pending until {@link #completePendingRead(String)}. A write
     * completes inside the call.
     */
    private static final class ScriptedSocket extends AsynchronousSocketChannel {

        volatile boolean open = true;

        volatile SocketAddress dialed;

        final AtomicInteger connects = new AtomicInteger();

        volatile IOException connectFailure;

        volatile boolean connectHangs;

        /** Read results: a String read, an Integer -1 for the end of stream, or a Throwable. */
        final Queue<Object> bufferedReads = new ArrayDeque<>();

        final AtomicInteger reads = new AtomicInteger();

        private ByteBuffer pendingTarget;

        private CompletionHandler<Integer, Object> pendingHandler;

        final ByteArrayOutputStream written = new ByteArrayOutputStream();

        volatile int maxWrite = Integer.MAX_VALUE;

        volatile IOException writeFailure;

        private final AtomicInteger writesInFlight = new AtomicInteger();

        final AtomicInteger maxWritesInFlight = new AtomicInteger();

        ScriptedSocket() {
            super(AsynchronousChannelProvider.provider());
        }

        @Override
        public <A> void connect(
                SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
            connects.incrementAndGet();
            dialed = remote;
            if (connectHangs) {
                return;
            }
            if (connectFailure != null) {
                handler.failed(connectFailure, attachment);
            } else {
                handler.completed(null, attachment);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <A> void read(
                ByteBuffer dst,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            reads.incrementAndGet();
            Object result;
            synchronized (this) {
                result = bufferedReads.poll();
                if (result == null) {
                    pendingTarget = dst;
                    pendingHandler = (CompletionHandler<Integer, Object>) handler;
                    return;
                }
            }
            complete(dst, (CompletionHandler<Integer, Object>) handler, result);
        }

        void completePendingRead(String text) {
            ByteBuffer target;
            CompletionHandler<Integer, Object> handler;
            synchronized (this) {
                target = pendingTarget;
                handler = pendingHandler;
                pendingHandler = null;
            }
            complete(target, handler, text);
        }

        private static void complete(
                ByteBuffer dst, CompletionHandler<Integer, Object> handler, Object result) {
            if (result instanceof Throwable failure) {
                handler.failed(failure, null);
            } else if (result instanceof Integer end) {
                handler.completed(end, null);
            } else {
                byte[] bytes = ((String) result).getBytes(StandardCharsets.UTF_8);
                dst.put(bytes);
                handler.completed(bytes.length, null);
            }
        }

        @Override
        public <A> void write(
                ByteBuffer src,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            int inFlight = writesInFlight.incrementAndGet();
            maxWritesInFlight.accumulateAndGet(inFlight, Math::max);
            if (writeFailure != null) {
                writesInFlight.decrementAndGet();
                handler.failed(writeFailure, attachment);
                return;
            }
            byte[] bytes = new byte[Math.min(maxWrite, src.remaining())];
            src.get(bytes);
            synchronized (written) {
                written.writeBytes(bytes);
            }
            writesInFlight.decrementAndGet();
            handler.completed(bytes.length, attachment);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return dialed;
        }

        @Override
        public SocketAddress getLocalAddress() throws IOException {
            throw new IOException("No local address");
        }

        @Override
        public AsynchronousSocketChannel bind(SocketAddress local) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) {
            return this;
        }

        @Override
        public <T> T getOption(SocketOption<T> name) {
            return null;
        }

        @Override
        public Set<SocketOption<?>> supportedOptions() {
            return Set.of();
        }

        @Override
        public AsynchronousSocketChannel shutdownInput() {
            return this;
        }

        @Override
        public AsynchronousSocketChannel shutdownOutput() {
            return this;
        }

        @Override
        public Future<Void> connect(SocketAddress remote) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<Integer> read(ByteBuffer dst) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <A> void read(
                ByteBuffer[] dsts,
                int offset,
                int length,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Long, ? super A> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<Integer> write(ByteBuffer src) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <A> void write(
                ByteBuffer[] srcs,
                int offset,
                int length,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Long, ? super A> handler) {
            throw new UnsupportedOperationException();
        }
    }
}
