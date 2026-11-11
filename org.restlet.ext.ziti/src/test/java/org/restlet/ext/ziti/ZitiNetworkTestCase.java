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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.openziti.Ziti;
import org.openziti.ZitiAddress;
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
import org.restlet.representation.ByteArrayRepresentation;

/**
 * Calls a Restlet server over a real OpenZiti network with {@link ZitiClientTransport}.
 *
 * <p>Runs only when given two enrolled identities: {@code -Dziti.client.identity=client.json},
 * which may dial the {@code crm} service, intercepted as {@code crm.internal:8443}, and {@code
 * -Dziti.server.identity=server.json}, which may bind it. The server identity binds the service
 * with the SDK's server API and pipes each Ziti connection to a local Restlet server, so a request
 * goes: Restlet client, Ziti socket channel, edge router, Ziti server socket, Restlet server.
 */
@EnabledIfSystemProperty(named = "ziti.client.identity", matches = ".+")
class ZitiNetworkTestCase {

    private static final String SERVICE = "crm";

    private static final String HOST = "crm.internal";

    private static ZitiContext clientZiti;

    private static ZitiContext serverZiti;

    private static AsynchronousServerSocketChannel binding;

    private static ExecutorService bridge;

    /** The local port the bridge pipes the Ziti connections to. */
    private static volatile int targetPort;

    private static Server server;

    @BeforeAll
    static void start() throws Exception {
        clientZiti =
                Ziti.newContext(new File(System.getProperty("ziti.client.identity")), new char[0]);
        serverZiti =
                Ziti.newContext(new File(System.getProperty("ziti.server.identity")), new char[0]);

        // A context learns its services asynchronously after authenticating: binding before the
        // server identity has loaded the service is refused as "no permission to bind".
        awaitUntil(
                () -> loaded(() -> serverZiti.getService(SERVICE, 0L)) != null,
                "The server never saw the crm service");

        // Host the service: accept the Ziti connections and pipe them to the local server.
        binding = serverZiti.openServer();
        binding.bind(new ZitiAddress.Bind(SERVICE, null, false), 64);
        bridge = Executors.newVirtualThreadPerTaskExecutor();
        bridge.submit(ZitiNetworkTestCase::acceptLoop);

        // The client learns its services asynchronously after authenticating, and the bind
        // registers a terminator asynchronously: dialing before gives "no terminators".
        awaitReachable(InetSocketAddress.createUnresolved(HOST, 8443));
    }

    /**
     * Waits until the client sees a service intercepting an address, with a terminator.
     *
     * @param address The intercepted address.
     */
    private static void awaitReachable(InetSocketAddress address) throws Exception {
        awaitUntil(
                () -> {
                    ServiceDetail service = loaded(() -> clientZiti.getService(address, 0L));
                    return service != null && !clientZiti.getServiceTerminators(service).isEmpty();
                },
                "The service of " + address + " is not reachable");
    }

    /**
     * Looks a service up among the loaded ones, without waiting: the overloads without timeout wait
     * for a service event, forever if the identity has none.
     *
     * @param lookup A lookup with a zero timeout.
     * @return The service, or null if not loaded.
     */
    private static ServiceDetail loaded(ServiceLookup lookup) {
        try {
            return lookup.get();
        } catch (TimeoutException _) {
            return null;
        }
    }

    /** A service lookup declaring the TimeoutException that the SDK throws undeclared. */
    @FunctionalInterface
    private interface ServiceLookup {
        ServiceDetail get() throws TimeoutException;
    }

    /**
     * Waits up to a minute for a condition on the network, checked every 250 ms.
     *
     * @param condition The condition.
     * @param message The failure message.
     */
    private static void awaitUntil(BooleanSupplier condition, String message) throws Exception {
        CompletableFuture<Void> met = new CompletableFuture<>();
        ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
        try {
            poller.scheduleWithFixedDelay(
                    () -> {
                        try {
                            if (condition.getAsBoolean()) {
                                met.complete(null);
                            }
                        } catch (RuntimeException e) {
                            met.completeExceptionally(e);
                        }
                    },
                    0,
                    250,
                    TimeUnit.MILLISECONDS);
            met.get(60, TimeUnit.SECONDS);
        } catch (TimeoutException _) {
            fail(message);
        } finally {
            met.cancel(false);
            // Interrupts a check still running, rather than waiting for it.
            poller.shutdownNow();
        }
    }

    @AfterAll
    static void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
        if (binding != null) {
            binding.close();
        }
        if (bridge != null) {
            bridge.shutdownNow();
        }
        if (clientZiti != null) {
            clientZiti.destroy();
        }
        if (serverZiti != null) {
            serverZiti.destroy();
        }
    }

    private static Void acceptLoop() throws Exception {
        while (binding.isOpen()) {
            AsynchronousSocketChannel ziti = binding.accept().get();
            bridge.submit(() -> pipe(ziti));
        }
        return null;
    }

    /** Pipes one Ziti connection to the local server, both ways. */
    private static Void pipe(AsynchronousSocketChannel ziti) throws Exception {
        try (ziti;
                AsynchronousSocketChannel local = AsynchronousSocketChannel.open()) {
            local.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), targetPort))
                    .get();
            Future<?> up = bridge.submit(copy(ziti, local));
            copy(local, ziti).call();
            up.get();
        }
        return null;
    }

    private static Callable<Void> copy(
            AsynchronousSocketChannel from, AsynchronousSocketChannel to) {
        return () -> {
            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
            try {
                while (from.read(buffer).get() >= 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        to.write(buffer).get();
                    }
                    buffer.clear();
                }
                to.shutdownOutput();
            } catch (Exception _) {
                // The other side closed: the pipe ends.
            }
            return null;
        };
    }

    /** Echoes the path and the size of the body, or the body itself on /echo. */
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

    private static void startServer(Protocol protocol, Context context) throws Exception {
        if (server != null) {
            server.stop();
        }
        server = new Server(context, protocol, "127.0.0.1", 0, echo());
        server.start();
        targetPort = server.getActualPort();
    }

    private static Client zitiClient(Protocol protocol, Context context) throws Exception {
        Client client = new Client(context, protocol);
        ZitiClientTransport.install(client, clientZiti);
        client.start();
        return client;
    }

    @Test
    void theServiceHostIsNotInTheDns() {
        assertThrows(UnknownHostException.class, () -> InetAddress.getByName(HOST));
    }

    @Test
    void callsOverZitiWithKeepAliveAndLargeBodies() throws Exception {
        startServer(Protocol.HTTP, new Context());
        Client client = zitiClient(Protocol.HTTP, new Context());
        try {
            for (int i = 0; i < 3; i++) {
                Response response =
                        client.handle(new Request(Method.GET, "http://crm.internal:8443/hi/" + i));
                assertEquals(Status.SUCCESS_OK, response.getStatus(), "call " + i);
                assertEquals("/hi/" + i + " 0", response.getEntity().getText());
            }

            byte[] body = new byte[4 * 1024 * 1024];
            for (int i = 0; i < body.length; i++) {
                body[i] = (byte) (i * 31 + 7);
            }
            Response response =
                    client.handle(
                            new Request(
                                    Method.POST,
                                    "http://crm.internal:8443/echo",
                                    new ByteArrayRepresentation(
                                            body, MediaType.APPLICATION_OCTET_STREAM)));
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            try (InputStream in = response.getEntity().getStream()) {
                assertArrayEquals(body, in.readAllBytes(), "4 MiB should come back intact");
            }
        } finally {
            client.stop();
        }
    }

    @Test
    void callsConcurrentlyOverZiti() throws Exception {
        startServer(Protocol.HTTP, new Context());
        Client client = zitiClient(Protocol.HTTP, new Context());
        try (ExecutorService callers = Executors.newFixedThreadPool(8)) {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                String path = "/concurrent/" + i;
                results.add(
                        callers.submit(
                                () -> {
                                    Response response =
                                            client.handle(
                                                    new Request(
                                                            Method.GET,
                                                            "http://crm.internal:8443" + path));
                                    if (response.getEntity() == null) {
                                        Throwable cause = response.getStatus().getThrowable();
                                        return response.getStatus()
                                                + (cause == null ? "" : " caused by " + cause);
                                    }
                                    return response.getEntity().getText();
                                }));
            }
            for (int i = 0; i < results.size(); i++) {
                assertEquals("/concurrent/" + i + " 0", results.get(i).get(60, TimeUnit.SECONDS));
            }
        } finally {
            client.stop();
        }
    }

    /**
     * The same concurrent calls to the {@code crm2} service, intercepted as {@code
     * crm2.internal:8443} and hosted by the edge router itself (a {@code host.v1} config forwarding
     * to {@code 127.0.0.1:<ziti.router.port>}), so that no Java SDK hosts the service: only the
     * client side of the connections is the Restlet transport.
     */
    @Test
    @EnabledIfSystemProperty(named = "ziti.router.port", matches = "\\d+")
    void callsConcurrentlyARouterHostedService() throws Exception {
        InetSocketAddress service = InetSocketAddress.createUnresolved("crm2.internal", 8443);
        awaitReachable(service);

        Server routerTarget =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        "127.0.0.1",
                        Integer.parseInt(System.getProperty("ziti.router.port")),
                        echo());
        routerTarget.start();
        Client client = zitiClient(Protocol.HTTP, new Context());
        try (ExecutorService callers = Executors.newFixedThreadPool(8)) {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                String path = "/router/" + i;
                results.add(
                        callers.submit(
                                () -> {
                                    Response response =
                                            client.handle(
                                                    new Request(
                                                            Method.GET,
                                                            "http://crm2.internal:8443" + path));
                                    if (response.getEntity() == null) {
                                        Throwable cause = response.getStatus().getThrowable();
                                        return response.getStatus()
                                                + (cause == null ? "" : " caused by " + cause);
                                    }
                                    return response.getEntity().getText();
                                }));
            }
            for (int i = 0; i < results.size(); i++) {
                assertEquals("/router/" + i + " 0", results.get(i).get(60, TimeUnit.SECONDS));
            }
        } finally {
            client.stop();
            routerTarget.stop();
        }
    }

    /**
     * Control experiment: the same 32 concurrent calls to the router-hosted {@code crm2} service
     * with the plain SDK ({@link ZitiContext#connect(String, int)} sockets and raw HTTP), without
     * Netty nor Restlet on the client side.
     */
    @Test
    @EnabledIfSystemProperty(named = "ziti.router.port", matches = "\\d+")
    void callsConcurrentlyWithThePlainSdk() throws Exception {
        Server routerTarget =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        "127.0.0.1",
                        Integer.parseInt(System.getProperty("ziti.router.port")),
                        echo());
        routerTarget.start();
        try (ExecutorService callers = Executors.newFixedThreadPool(8)) {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                String path = "/raw/" + i;
                results.add(
                        callers.submit(
                                () -> {
                                    try (java.net.Socket socket =
                                            clientZiti.connect("crm2.internal", 8443)) {
                                        socket.setSoTimeout(30_000);
                                        socket.getOutputStream()
                                                .write(
                                                        ("GET "
                                                                        + path
                                                                        + " HTTP/1.1\r\n"
                                                                        + "Host: crm2.internal\r\n"
                                                                        + "Connection: close\r\n\r\n")
                                                                .getBytes(
                                                                        java.nio.charset
                                                                                .StandardCharsets
                                                                                .US_ASCII));
                                        socket.getOutputStream().flush();
                                        return new String(
                                                socket.getInputStream().readAllBytes(),
                                                java.nio.charset.StandardCharsets.US_ASCII);
                                    } catch (Exception e) {
                                        return "failed: " + e;
                                    }
                                }));
            }
            int failed = 0;
            StringBuilder failures = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                String response = results.get(i).get(90, TimeUnit.SECONDS);
                if (!response.endsWith("/raw/" + i + " 0")) {
                    failed++;
                    failures.append("\n  ")
                            .append(i)
                            .append(": ")
                            .append(
                                    response.length() > 160
                                            ? response.substring(0, 160)
                                            : response);
                }
            }
            assertEquals(0, failed, "Plain SDK calls failed:" + failures);
        } finally {
            routerTarget.stop();
        }
    }

    /** TLS runs end to end over Ziti, the server certificate checked against crm.internal. */
    @Test
    void callsHttpsOverZiti(@TempDir Path dir) throws Exception {
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
                                "CN=" + HOST,
                                "-ext",
                                "SAN=dns:" + HOST,
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
        startServer(Protocol.HTTPS, serverContext);

        Context clientContext = new Context();
        clientContext.getParameters().add("trustStorePath", keyStore.toString());
        clientContext.getParameters().add("trustStorePassword", "changeit");
        clientContext.getParameters().add("trustStoreType", "PKCS12");
        Client client = zitiClient(Protocol.HTTPS, clientContext);
        try {
            Response response =
                    client.handle(new Request(Method.GET, "https://crm.internal:8443/secure"));
            assertEquals(
                    Status.SUCCESS_OK, response.getStatus(), String.valueOf(response.getStatus()));
            assertNotNull(response.getEntity());
            assertEquals("/secure 0", response.getEntity().getText());
        } finally {
            client.stop();
        }
    }
}
