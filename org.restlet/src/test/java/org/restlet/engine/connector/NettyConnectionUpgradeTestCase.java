/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.netty.NettyConnectionUpgrade;
import org.restlet.engine.netty.NettyUpgradeContext;

/** Tests the connection upgrade hook of the Netty server connector. */
class NettyConnectionUpgradeTestCase {

    private Server server;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
        Engine.register();
    }

    private int start(NettyConnectionUpgrade upgrade, String protocol) throws Exception {
        Context context = new Context();
        if (protocol != null) {
            context.getParameters().add("http.transport.protocol", protocol);
        }
        server =
                new Server(
                        context,
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.getAttributes()
                                        .put(NettyConnectionUpgrade.ATTRIBUTE, upgrade);
                                response.setStatus(Status.INFO_SWITCHING_PROTOCOL);
                            }
                        });
        server.start();
        return server.getActualPort();
    }

    private static String exchange(int port) throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            OutputStream out = socket.getOutputStream();
            out.write(
                    ("GET /up HTTP/1.1\r\nHost: localhost\r\nConnection: Upgrade\r\n"
                                    + "Upgrade: test\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream(), StandardCharsets.US_ASCII));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line).append('\n');
            }
            return result.toString();
        }
    }

    @Test
    void handsTheConnectionOverToTheUpgrade() throws Exception {
        AtomicReference<NettyUpgradeContext> seen = new AtomicReference<>();
        int port =
                start(
                        context -> {
                            seen.set(context);
                            FullHttpResponse response =
                                    new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.SWITCHING_PROTOCOLS,
                                            Unpooled.EMPTY_BUFFER);
                            response.headers().set("Upgrade", "test");
                            context.getChannel()
                                    .writeAndFlush(response)
                                    .addListener(ChannelFutureListener.CLOSE);
                        },
                        null);

        String reply = exchange(port);

        assertTrue(reply.startsWith("HTTP/1.1 101 Switching Protocols"), reply);
        NettyUpgradeContext context = seen.get();
        assertEquals("/up", context.getRequest().uri());
        assertFalse(context.isConfidential());
        assertEquals(30000, context.getIdleTimeout());
        assertTrue(context.getParameters().isEmpty());
        context.getAttributes().put("seen", true);
        assertTrue(context.getLogger() != null);
    }

    @Test
    void failingUpgradeClosesTheConnection() throws Exception {
        int port =
                start(
                        context -> {
                            throw new IllegalStateException("refused");
                        },
                        null);

        assertEquals("", exchange(port));
    }
}
