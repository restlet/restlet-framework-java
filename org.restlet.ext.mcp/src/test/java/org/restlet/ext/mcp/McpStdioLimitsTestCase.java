/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

/**
 * The limits of the STDIO connector: the adapter's, or else the connector's parameters, and what a
 * request they refuse gets, the session going on.
 */
class McpStdioLimitsTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();

    private final StdioServerHelper prototype = new StdioServerHelper(null);
    private final List<AutoCloseable> closing = new ArrayList<>();
    private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
    private OutputStream toServer;

    @AfterEach
    void stop() throws Exception {
        try {
            for (AutoCloseable closeable : closing.reversed()) {
                closeable.close();
            }
        } finally {
            Engine.getInstance().getRegisteredServers().remove(prototype);
        }
    }

    /** The adapter's size limit applies both ways: above the default, a large call goes through. */
    @Test
    void theAdaptersSizeLimitApplies() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.setMaxMessageBytes(4 * 1024 * 1024);
        start(adapter, new Context());

        String text = "x".repeat(McpProtocol.DEFAULT_MAX_MESSAGE_BYTES + 1024);
        send(call(1, text));
        Map<String, Object> reply = read();
        assertEquals(1, ((Number) reply.get("id")).intValue(), reply.keySet().toString());
        assertEquals("echo:" + text, firstText(reply));
    }

    /**
     * A message over the limit is answered with -32600, under its id when its first bytes give it,
     * and the session goes on, in both eras.
     */
    @Test
    void aMessageOverTheLimitIsAnswered() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.setMaxMessageBytes(4096);
        start(adapter, new Context());

        send(call(7, "x".repeat(8192)));
        assertInvalid(read(), 7);

        // Its id after the oversized params: not read, answered with a null id.
        Map<String, Object> late = new LinkedHashMap<>();
        late.put("jsonrpc", "2.0");
        late.put("method", "tools/call");
        late.put(
                "params",
                Map.of(
                        "name",
                        "echo",
                        "arguments",
                        Map.of("text", "y".repeat(80_000)),
                        "_meta",
                        meta()));
        late.put("id", 8);
        send(late);
        assertInvalid(read(), null);

        // A legacy message, with a string id.
        sendRaw(
                "{\"jsonrpc\":\"2.0\",\"id\":\"big\",\"method\":\"tools/list\",\"params\":{\"pad\":\""
                        + "z".repeat(8192)
                        + "\"}}");
        assertInvalid(read(), "big");

        send(call(9, "small"));
        Map<String, Object> reply = read();
        assertEquals(9, ((Number) reply.get("id")).intValue());
        assertEquals("echo:small", firstText(reply));
    }

    /** Serving another Restlet, the parameters give the limits. */
    @Test
    void theParametersGiveTheLimitsOfAnotherRestlet() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        Context context = new Context();
        context.getParameters().add("maxMessageBytes", "2048");
        context.getParameters().add("requestTimeout", "500");
        start(new Blocking(release), context);

        // Too slow: answered within the timeout, and the session goes on.
        sendRaw(legacy(1, "slow"));
        Map<String, Object> slow = read();
        assertEquals(1, ((Number) slow.get("id")).intValue(), slow.toString());
        assertEquals(-32603, code(slow));
        release.countDown();

        sendRaw(legacy(2, "fast"));
        Map<String, Object> fast = read();
        assertEquals(2, ((Number) fast.get("id")).intValue(), fast.toString());
        assertTrue(fast.containsKey("result"), fast.toString());

        sendRaw(legacy(3, "p".repeat(4096)));
        assertInvalid(read(), 3);
    }

    /** Over the call limit, a request is answered at once and the session goes on. */
    @Test
    void aRequestOverTheCallLimitIsAnswered() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        Context context = new Context();
        context.getParameters().add("maxConcurrentCalls", "1");
        start(new Blocking(release), context);

        sendRaw(legacy(1, "slow"));
        sendRaw(legacy(2, "fast"));
        Map<String, Object> refused = read();
        assertEquals(2, ((Number) refused.get("id")).intValue(), refused.toString());
        assertEquals(-32603, code(refused));

        release.countDown();
        Map<String, Object> first = read();
        assertEquals(1, ((Number) first.get("id")).intValue(), first.toString());
        assertTrue(first.containsKey("result"), first.toString());
    }

    /** The limits are set in one place: an adapter's can't be given as parameters too. */
    @Test
    void anAdaptersLimitsAreNotParameters() throws Exception {
        Context context = new Context();
        context.getParameters().add("requestTimeout", "1000");
        Exception refused = assertThrows(Exception.class, () -> start(adapter(), context));
        assertTrue(
                String.valueOf(rootCause(refused).getMessage()).contains("requestTimeout"),
                String.valueOf(rootCause(refused)));
    }

    /** A parameter must be a positive number. */
    @Test
    void parametersMustBeValid() throws Exception {
        for (String value : List.of("0", "-1", "abc", "2147483647")) {
            Context context = new Context();
            context.getParameters()
                    .add(
                            value.equals("2147483647") ? "maxMessageBytes" : "maxConcurrentCalls",
                            value);
            Exception refused =
                    assertThrows(
                            Exception.class,
                            () -> start(new Blocking(new CountDownLatch(0)), context));
            assertTrue(
                    rootCause(refused) instanceof IllegalArgumentException,
                    value + ": " + rootCause(refused));
        }
    }

    private McpServerAdapter adapter() throws Exception {
        McpServerAdapter adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        closing.add(adapter::stop);
        return adapter;
    }

    /** Starts a STDIO server on a restlet, an adapter being started first. */
    private void start(Restlet next, Context context) throws Exception {
        if (next instanceof McpServerAdapter adapter) {
            adapter.start();
        }
        Pipe requests = Pipe.open();
        Pipe responses = Pipe.open();
        org.restlet.util.StreamTransport stream =
                new org.restlet.util.StreamTransport(
                        Channels.newInputStream(requests.source()),
                        Channels.newOutputStream(responses.sink()),
                        () -> {
                            try {
                                requests.source().close();
                                responses.sink().close();
                            } catch (IOException e) {
                                throw new java.io.UncheckedIOException(e);
                            }
                        });
        toServer = Channels.newOutputStream(requests.sink());
        BufferedReader fromServer =
                new BufferedReader(
                        new InputStreamReader(
                                Channels.newInputStream(responses.source()),
                                StandardCharsets.UTF_8));
        Thread.ofVirtual()
                .start(
                        () -> {
                            try {
                                for (String line = fromServer.readLine();
                                        line != null;
                                        line = fromServer.readLine()) {
                                    lines.add(line);
                                }
                            } catch (IOException _) {
                                // The server output ended.
                            }
                        });
        if (!Engine.getInstance().getRegisteredServers().contains(prototype)) {
            Engine.getInstance().getRegisteredServers().add(prototype);
        }
        context.getAttributes().put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, stream);
        Server server =
                new Server(
                        context,
                        List.of(Protocol.STDIO),
                        null,
                        -1,
                        next,
                        StdioServerHelper.class.getCanonicalName());
        closing.add(server::stop);
        server.start();
    }

    /** Answers legacy requests, those named "slow" once released. */
    private static final class Blocking extends Restlet {
        private final CountDownLatch release;

        private Blocking(CountDownLatch release) {
            this.release = release;
        }

        @Override
        public void handle(Request request, Response response) {
            try {
                Map<String, Object> message =
                        MAPPER.readValue(
                                request.getEntity().getText(),
                                new TypeRef<Map<String, Object>>() {});
                if (message.get("params") instanceof Map<?, ?> params
                        && "slow".equals(params.get("name"))) {
                    release.await(30, TimeUnit.SECONDS);
                }
                Map<String, Object> reply = new LinkedHashMap<>();
                reply.put("jsonrpc", "2.0");
                reply.put("id", message.get("id"));
                reply.put("result", Map.of());
                response.setEntity(
                        new StringRepresentation(
                                MAPPER.writeValueAsString(reply), MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String legacy(int id, String name) {
        return "{\"jsonrpc\":\"2.0\",\"id\":"
                + id
                + ",\"method\":\"tools/call\",\"params\":{\"name\":\""
                + name
                + "\"}}";
    }

    private static Map<String, Object> call(int id, String text) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("jsonrpc", "2.0");
        message.put("id", id);
        message.put("method", "tools/call");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "echo");
        params.put("arguments", Map.of("text", text));
        params.put("_meta", meta());
        message.put("params", params);
        return message;
    }

    private static Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, McpProtocol.MODERN_VERSION);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
        return meta;
    }

    private void send(Map<String, Object> message) throws IOException {
        sendRaw(MAPPER.writeValueAsString(message));
    }

    private void sendRaw(String line) throws IOException {
        toServer.write((line + "\n").getBytes(StandardCharsets.UTF_8));
        toServer.flush();
    }

    private Map<String, Object> read() throws Exception {
        String line = lines.poll(20, TimeUnit.SECONDS);
        assertNotNull(line, "no answer");
        return MAPPER.readValue(line, new TypeRef<Map<String, Object>>() {});
    }

    private static void assertInvalid(Map<String, Object> reply, Object id) {
        assertEquals(-32600, code(reply), reply.toString());
        if (id == null) {
            assertNull(reply.get("id"), reply.toString());
        } else if (id instanceof Number number) {
            assertEquals(number.intValue(), ((Number) reply.get("id")).intValue());
        } else {
            assertEquals(id, reply.get("id"));
        }
    }

    private static int code(Map<String, Object> reply) {
        assertTrue(reply.get("error") instanceof Map<?, ?>, reply.toString());
        return ((Number) ((Map<?, ?>) reply.get("error")).get("code")).intValue();
    }

    private static String firstText(Map<String, Object> reply) {
        Map<?, ?> result = (Map<?, ?>) reply.get("result");
        assertNotNull(result, String.valueOf(reply.get("error")));
        return (String) ((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text");
    }

    private static Throwable rootCause(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
