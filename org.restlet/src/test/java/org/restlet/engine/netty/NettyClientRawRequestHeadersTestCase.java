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
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

/** Tests the raw request headers that the Netty client writes as-is. */
class NettyClientRawRequestHeadersTestCase {
    private final AtomicReference<com.sun.net.httpserver.Headers> received =
            new AtomicReference<>();
    private HttpServer peer;
    private Client client;

    @BeforeEach
    void start() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        peer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        peer.createContext(
                "/",
                exchange -> {
                    exchange.getRequestBody().readAllBytes();
                    received.set(exchange.getRequestHeaders());
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                });
        peer.start();
        client = new Client(new Context(), Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void stop() throws Exception {
        client.stop();
        peer.stop(0);
    }

    @Test
    void rawHeadersReplaceTheRestletOnesExceptFramingHeaders() {
        Request request =
                new Request(Method.POST, "http://localhost:" + peer.getAddress().getPort() + "/");
        request.setEntity("{}", MediaType.APPLICATION_JSON);
        request.getClientInfo().setAgent("Restlet-Agent");
        var raw = new Series<Header>(Header.class);
        raw.add("If-Match", "\"v1\"");
        raw.add("Accept", "application/json;q=0.9");
        raw.add("Accept", "*/*;q=0.1");
        raw.add("User-Agent", "sdk/1.0");
        raw.add("Content-Type", "application/x-amz-json-1.0");
        raw.add("Content-Length", "999");
        raw.add("Host", "elsewhere.example");
        request.getAttributes().put(HeaderConstants.ATTRIBUTE_RAW_REQUEST_HEADERS, raw);

        Response response = client.handle(request);

        assertEquals(204, response.getStatus().getCode());
        var headers = received.get();
        assertEquals(List.of("\"v1\""), headers.get("If-Match"));
        assertEquals(List.of("application/json;q=0.9", "*/*;q=0.1"), headers.get("Accept"));
        assertEquals(List.of("sdk/1.0"), headers.get("User-Agent"));
        assertEquals(List.of("application/x-amz-json-1.0"), headers.get("Content-Type"));
        assertEquals(List.of("2"), headers.get("Content-Length"));
        assertEquals(List.of("localhost:" + peer.getAddress().getPort()), headers.get("Host"));
    }

    /** A raw User-Agent also replaces the one set by the client's userAgentField parameter. */
    @Test
    void rawUserAgentReplacesTheConfiguredOne() throws Exception {
        Context context = new Context();
        context.getParameters().set("userAgentField", "configured/1.0");
        Client configured = new Client(context, Protocol.HTTP);
        configured.start();
        try {
            String uri = "http://localhost:" + peer.getAddress().getPort() + "/";
            configured.handle(new Request(Method.GET, uri));
            assertEquals(List.of("configured/1.0"), received.get().get("User-Agent"));
            Request request = new Request(Method.GET, uri);
            var raw = new Series<Header>(Header.class);
            raw.add("User-Agent", "sdk/1.0");
            request.getAttributes().put(HeaderConstants.ATTRIBUTE_RAW_REQUEST_HEADERS, raw);
            configured.handle(request);
            assertEquals(List.of("sdk/1.0"), received.get().get("User-Agent"));
        } finally {
            configured.stop();
        }
    }
}
