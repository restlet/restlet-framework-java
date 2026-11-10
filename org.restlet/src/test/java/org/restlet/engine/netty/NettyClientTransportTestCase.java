/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.resolver.AbstractAddressResolver;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;

/** Tests the routes of a {@link NettyClientTransport} set on a client. */
class NettyClientTransportTestCase {

    /** The origin routed by the transport: not in the DNS. */
    private static final String ROUTED_HOST = "overlay.invalid";

    private HttpServer peer;

    private Client client;

    /** The origins the transport was asked for, as host:port. */
    private final List<String> asked = new CopyOnWriteArrayList<>();

    /** The addresses the route's resolver resolved. */
    private final List<String> resolved = new CopyOnWriteArrayList<>();

    @BeforeEach
    void start() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        peer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        peer.createContext(
                "/",
                exchange -> {
                    byte[] body =
                            exchange.getRequestHeaders()
                                    .getFirst("Host")
                                    .getBytes(StandardCharsets.US_ASCII);
                    exchange.sendResponseHeaders(200, body.length);
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.write(body);
                    }
                });
        peer.start();
    }

    @AfterEach
    void stop() throws Exception {
        if (client != null) {
            client.stop();
        }
        peer.stop(0);
        Engine.clearThreadLocalVariables();
    }

    /** Routes ROUTED_HOST over TCP to the peer, with a resolver that never asks the DNS. */
    private NettyClientTransport transport() {
        int peerPort = peer.getAddress().getPort();
        AddressResolverGroup<InetSocketAddress> resolver =
                new AddressResolverGroup<>() {
                    @Override
                    protected AddressResolver<InetSocketAddress> newResolver(
                            EventExecutor executor) {
                        return new AbstractAddressResolver<>(executor) {
                            @Override
                            protected boolean doIsResolved(InetSocketAddress address) {
                                return !address.isUnresolved();
                            }

                            @Override
                            protected void doResolve(
                                    InetSocketAddress address, Promise<InetSocketAddress> promise) {
                                resolved.add(address.getHostString() + ":" + address.getPort());
                                promise.setSuccess(new InetSocketAddress("127.0.0.1", peerPort));
                            }

                            @Override
                            protected void doResolveAll(
                                    InetSocketAddress address,
                                    Promise<List<InetSocketAddress>> promise) {
                                promise.setSuccess(
                                        List.of(new InetSocketAddress("127.0.0.1", peerPort)));
                            }
                        };
                    }
                };
        NettyClientTransport.Route route =
                new NettyClientTransport.Route(NioSocketChannel::new, resolver);
        return (host, port) -> {
            asked.add(host + ":" + port);
            return ROUTED_HOST.equals(host) ? route : null;
        };
    }

    private void startClient(Context context) throws Exception {
        context.getAttributes().put(NettyClientTransport.ATTRIBUTE, transport());
        client = new Client(context, Protocol.HTTP);
        client.start();
    }

    @Test
    void connectsToARoutedOriginThroughTheRoute() throws Exception {
        startClient(new Context());

        Response response =
                client.handle(new Request(Method.GET, "http://" + ROUTED_HOST + ":8080/a"));

        assertEquals(Status.SUCCESS_OK, response.getStatus(), String.valueOf(response.getStatus()));
        assertEquals(ROUTED_HOST + ":8080", response.getEntity().getText());
        assertEquals(List.of(ROUTED_HOST + ":8080"), asked);
        assertEquals(List.of(ROUTED_HOST + ":8080"), resolved);
    }

    @Test
    void reusesTheConnectionOfARoutedOrigin() throws Exception {
        startClient(new Context());

        for (int i = 0; i < 3; i++) {
            Response response =
                    client.handle(new Request(Method.GET, "http://" + ROUTED_HOST + "/b" + i));
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            response.getEntity().exhaust();
        }

        assertEquals(1, asked.size(), "The route is asked once per connection");
    }

    @Test
    void doesNotUseTheProxyForARoutedOrigin() throws Exception {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        Context context = new Context();
        context.getParameters().add("proxyHost", "127.0.0.1");
        context.getParameters().add("proxyPort", Integer.toString(closedPort));
        startClient(context);

        Response response = client.handle(new Request(Method.GET, "http://" + ROUTED_HOST + "/c"));

        assertEquals(Status.SUCCESS_OK, response.getStatus(), String.valueOf(response.getStatus()));
        assertEquals(ROUTED_HOST, response.getEntity().getText());
    }

    @Test
    void connectsToOtherOriginsOverTcp() throws Exception {
        startClient(new Context());
        String origin = "127.0.0.1:" + peer.getAddress().getPort();

        Response response = client.handle(new Request(Method.GET, "http://" + origin + "/d"));

        assertEquals(Status.SUCCESS_OK, response.getStatus(), String.valueOf(response.getStatus()));
        assertEquals(origin, response.getEntity().getText());
        assertEquals(List.of(origin), asked);
        assertEquals(List.of(), resolved);
    }
}
