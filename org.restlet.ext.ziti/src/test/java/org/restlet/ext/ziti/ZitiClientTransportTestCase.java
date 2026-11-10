/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.ziti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openziti.ZitiContext;
import org.openziti.edge.model.ServiceDetail;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.netty.NettyClientTransport;
import org.restlet.representation.ByteArrayRepresentation;

/**
 * Runs the Ziti socket channel under the Restlet client connector. Only the overlay network is
 * replaced: the fake {@link ZitiContext} intercepts {@code crm.internal}, and its {@link
 * ZitiContext#open()} returns an asynchronous socket that connects to a local Restlet server
 * instead of dialing a Ziti service.
 */
class ZitiClientTransportTestCase {

    private static final String SERVICE_HOST = "crm.internal";

    private static final int SERVICE_PORT = 8443;

    /** The local port the fake overlay connects the service to. */
    private int targetPort;

    /** The number of channels opened through the fake overlay. */
    private final AtomicInteger opened = new AtomicInteger();

    /** Whether the fake context has loaded its services when the transport is created. */
    private volatile boolean activeAtStart = true;

    /** The number of service lookups made on the fake context. */
    private final AtomicInteger lookups = new AtomicInteger();

    /** The status listener registered on the fake context. */
    private volatile Consumer<ZitiContext.Status> statusListener;

    /** The service event listener registered on the fake context. */
    private volatile Consumer<ZitiContext.ServiceEvent> serviceListener;

    private Server server;

    private Client client;

    @AfterEach
    void stop() throws Exception {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    /**
     * A Ziti context intercepting crm.internal:8443 and dialing the local server. Like the SDK's,
     * its {@code getService(InetSocketAddress)} waits for a service event: here it fails instead.
     */
    @SuppressWarnings("unchecked")
    private ZitiContext fakeZiti() {
        return (ZitiContext)
                Proxy.newProxyInstance(
                        ZitiContext.class.getClassLoader(),
                        new Class<?>[] {ZitiContext.class},
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "getService" -> {
                                        if (args.length == 1) {
                                            throw new AssertionError(
                                                    "Waits for a service event, forever if none");
                                        }
                                        lookups.incrementAndGet();
                                        yield (args[0] instanceof InetSocketAddress a
                                                        && SERVICE_HOST.equals(a.getHostString())
                                                        && a.getPort() == SERVICE_PORT)
                                                ? new ServiceDetail().name("crm")
                                                : null;
                                    }
                                    case "onStatus" -> {
                                        statusListener = (Consumer<ZitiContext.Status>) args[0];
                                        if (activeAtStart) {
                                            statusListener.accept(
                                                    ZitiContext.Status.Active.INSTANCE);
                                        }
                                        yield new CompletableFuture<>();
                                    }
                                    case "onServiceEvent" -> {
                                        serviceListener =
                                                (Consumer<ZitiContext.ServiceEvent>) args[0];
                                        yield new CompletableFuture<>();
                                    }
                                    case "open" -> {
                                        opened.incrementAndGet();
                                        yield new OverlaySocket(
                                                AsynchronousSocketChannel.open(),
                                                new InetSocketAddress("127.0.0.1", targetPort));
                                    }
                                    case "name", "toString" -> "fake-ziti";
                                    case "hashCode" -> System.identityHashCode(proxy);
                                    case "equals" -> proxy == args[0];
                                    default ->
                                            throw new UnsupportedOperationException(
                                                    method.getName());
                                });
    }

    /** Echoes the path and the number of bytes received, then the received bytes if asked. */
    private static Restlet echo() {
        return new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                byte[] body = new byte[0];
                try {
                    if (request.isEntityAvailable()) {
                        try (InputStream in = request.getEntity().getStream()) {
                            body = in.readAllBytes();
                        }
                    }
                } catch (IOException e) {
                    response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                    return;
                }
                if (request.getResourceRef().getPath().endsWith("/echo")) {
                    response.setEntity(
                            new ByteArrayRepresentation(body, MediaType.APPLICATION_OCTET_STREAM));
                } else {
                    response.setEntity(
                            request.getResourceRef().getPath() + " " + body.length,
                            MediaType.TEXT_PLAIN);
                }
            }
        };
    }

    private void startHttp() throws Exception {
        server = new Server(new Context(), Protocol.HTTP, 0, echo());
        server.start();
        targetPort = server.getActualPort();
        client = new Client(new Context(), Protocol.HTTP);
        ZitiClientTransport.install(client, fakeZiti());
        client.start();
    }

    @Test
    void routesAnInterceptedOriginOverZiti() throws Exception {
        startHttp();

        Response response = client.handle(new Request(Method.GET, "http://crm.internal:8443/hi"));

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("/hi 0", response.getEntity().getText());
        assertEquals(1, opened.get(), "The connection should be dialed through Ziti");
    }

    @Test
    void reusesTheZitiConnection() throws Exception {
        startHttp();

        for (int i = 0; i < 3; i++) {
            Response response =
                    client.handle(new Request(Method.GET, "http://crm.internal:8443/hi/" + i));
            assertEquals("/hi/" + i + " 0", response.getEntity().getText());
        }

        assertEquals(1, opened.get(), "Keep-alive should reuse the Ziti connection");
    }

    @Test
    void streamsLargeBodiesBothWays() throws Exception {
        startHttp();
        byte[] body = new byte[2 * 1024 * 1024];
        for (int i = 0; i < body.length; i++) {
            body[i] = (byte) (i * 31);
        }

        Response response =
                client.handle(
                        new Request(
                                Method.POST,
                                "http://crm.internal:8443/echo",
                                new ByteArrayRepresentation(
                                        body, MediaType.APPLICATION_OCTET_STREAM)));

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        byte[] received;
        try (InputStream in = response.getEntity().getStream()) {
            received = in.readAllBytes();
        }
        assertTrue(Arrays.equals(body, received), "The 2 MiB body should come back intact");
    }

    @Test
    void connectsToOtherOriginsOverTcp() throws Exception {
        startHttp();

        Response response =
                client.handle(
                        new Request(Method.GET, "http://localhost:" + targetPort + "/direct"));

        assertEquals("/direct 0", response.getEntity().getText());
        assertEquals(0, opened.get(), "An origin Ziti doesn't intercept should use TCP");
    }

    @Test
    void routesOnlyInterceptedOrigins() {
        ZitiClientTransport transport = new ZitiClientTransport(fakeZiti());

        assertNotNull(transport.route(SERVICE_HOST, SERVICE_PORT));
        // Without waiting for a service event, which may never come.
        assertNull(transport.route(SERVICE_HOST, 80));
        assertNull(transport.route("example.org", SERVICE_PORT));
    }

    @Test
    void cachesTheLookupsUntilAServiceEvent() {
        ZitiClientTransport transport = new ZitiClientTransport(fakeZiti());

        transport.route("example.org", 443);
        transport.route("example.org", 443);
        assertEquals(1, lookups.get(), "The lookup is cached");

        serviceListener.accept(
                new ZitiContext.ServiceEvent(
                        new ServiceDetail().name("crm"), ZitiContext.ServiceUpdate.Available));
        transport.route("example.org", 443);
        assertEquals(2, lookups.get(), "A service event clears the cache");
    }

    @Test
    void waitsForTheServicesUpToTheTimeout() {
        activeAtStart = false;
        ZitiClientTransport transport = new ZitiClientTransport(fakeZiti(), Duration.ofMillis(200));

        long start = System.nanoTime();
        assertNull(transport.route(SERVICE_HOST, SERVICE_PORT), "Not loaded: over TCP");
        assertTrue(System.nanoTime() - start >= Duration.ofMillis(150).toNanos());
        assertEquals(0, lookups.get());

        statusListener.accept(ZitiContext.Status.Active.INSTANCE);
        assertNotNull(transport.route(SERVICE_HOST, SERVICE_PORT), "Loaded: over Ziti");
    }

    @Test
    void installsOnAClientWithoutContext() {
        client = new Client(Protocol.HTTP);

        ZitiClientTransport transport = ZitiClientTransport.install(client, fakeZiti());

        assertSame(
                transport, client.getContext().getAttributes().get(NettyClientTransport.ATTRIBUTE));
    }

    /** TLS runs end to end over the Ziti channel, with the host name of the service verified. */
    @Test
    void runsTlsOverZiti(@TempDir Path dir) throws Exception {
        Path keyStore = dir.resolve("crm.p12");
        Process keytool =
                new ProcessBuilder(
                                Path.of(System.getProperty("java.home"), "bin", "keytool")
                                        .toString(),
                                "-genkeypair",
                                "-alias",
                                "crm",
                                "-keyalg",
                                "EC",
                                "-groupname",
                                "secp256r1",
                                "-dname",
                                "CN=" + SERVICE_HOST,
                                "-ext",
                                "SAN=dns:" + SERVICE_HOST,
                                "-validity",
                                "2",
                                "-storetype",
                                "PKCS12",
                                "-keystore",
                                keyStore.toString(),
                                "-storepass",
                                "changeit",
                                "-keypass",
                                "changeit")
                        .redirectErrorStream(true)
                        .start();
        keytool.getInputStream().readAllBytes();
        assertTrue(keytool.waitFor(60, TimeUnit.SECONDS) && keytool.exitValue() == 0);

        Context serverContext = new Context();
        serverContext.getParameters().add("keyStorePath", keyStore.toString());
        serverContext.getParameters().add("keyStorePassword", "changeit");
        serverContext.getParameters().add("keyPassword", "changeit");
        serverContext.getParameters().add("keyStoreType", "PKCS12");
        server = new Server(serverContext, Protocol.HTTPS, 0, echo());
        server.start();
        targetPort = server.getActualPort();

        Context clientContext = new Context();
        clientContext.getParameters().add("trustStorePath", keyStore.toString());
        clientContext.getParameters().add("trustStorePassword", "changeit");
        clientContext.getParameters().add("trustStoreType", "PKCS12");
        client = new Client(clientContext, Protocol.HTTPS);
        ZitiClientTransport.install(client, fakeZiti());
        client.start();

        Response response =
                client.handle(new Request(Method.GET, "https://crm.internal:8443/secure"));

        assertEquals(Status.SUCCESS_OK, response.getStatus(), String.valueOf(response.getStatus()));
        assertEquals("/secure 0", response.getEntity().getText());
        assertEquals(1, opened.get());
    }

    /**
     * An asynchronous socket standing for a Ziti connection: it connects to the local server
     * whatever unresolved service address it is given.
     */
    private static final class OverlaySocket extends AsynchronousSocketChannel {

        private final AsynchronousSocketChannel socket;

        private final InetSocketAddress target;

        OverlaySocket(AsynchronousSocketChannel socket, InetSocketAddress target) {
            super(socket.provider());
            this.socket = socket;
            this.target = target;
        }

        @Override
        public AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
            socket.bind(local);
            return this;
        }

        @Override
        public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value)
                throws IOException {
            socket.setOption(name, value);
            return this;
        }

        @Override
        public <T> T getOption(SocketOption<T> name) throws IOException {
            return socket.getOption(name);
        }

        @Override
        public Set<SocketOption<?>> supportedOptions() {
            return socket.supportedOptions();
        }

        @Override
        public AsynchronousSocketChannel shutdownInput() throws IOException {
            socket.shutdownInput();
            return this;
        }

        @Override
        public AsynchronousSocketChannel shutdownOutput() throws IOException {
            socket.shutdownOutput();
            return this;
        }

        @Override
        public SocketAddress getRemoteAddress() throws IOException {
            return socket.getRemoteAddress();
        }

        @Override
        public <A> void connect(
                SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
            socket.connect(target, attachment, handler);
        }

        @Override
        public Future<Void> connect(SocketAddress remote) {
            return socket.connect(target);
        }

        @Override
        public <A> void read(
                ByteBuffer dst,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            socket.read(dst, timeout, unit, attachment, handler);
        }

        @Override
        public Future<Integer> read(ByteBuffer dst) {
            return socket.read(dst);
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
            socket.read(dsts, offset, length, timeout, unit, attachment, handler);
        }

        @Override
        public <A> void write(
                ByteBuffer src,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            socket.write(src, timeout, unit, attachment, handler);
        }

        @Override
        public Future<Integer> write(ByteBuffer src) {
            return socket.write(src);
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
            socket.write(srcs, offset, length, timeout, unit, attachment, handler);
        }

        @Override
        public SocketAddress getLocalAddress() throws IOException {
            return socket.getLocalAddress();
        }

        @Override
        public boolean isOpen() {
            return socket.isOpen();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
