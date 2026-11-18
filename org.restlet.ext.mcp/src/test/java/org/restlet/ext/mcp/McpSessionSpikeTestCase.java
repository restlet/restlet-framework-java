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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.representation.EventStreamRepresentation;
import reactor.core.publisher.Mono;

/**
 * R6b gate 0: the MCP SDK's sessionful server SPI ({@link McpStreamableServerTransportProvider})
 * hosted on a Restlet {@link Restlet}, with core SSE ({@link EventStreamRepresentation}) for both
 * response streams and listening streams, against the SDK's own {@link
 * HttpClientStreamableHttpTransport}. Test-only: no production API is implied.
 */
class McpSessionSpikeTestCase {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();

    private Server server;
    private McpAsyncServer sdkServer;
    private McpAsyncClient sdkClient;
    private McpLegacySessionHost host;
    private String target;

    @AfterEach
    void cleanup() throws Exception {
        // Each resource is released even when an earlier shutdown fails or times out.
        try {
            if (sdkClient != null) {
                sdkClient.closeGracefully().onErrorComplete().block(TIMEOUT);
            }
        } finally {
            try {
                if (sdkServer != null) {
                    sdkServer.closeGracefully().onErrorComplete().block(TIMEOUT);
                }
            } finally {
                if (server != null) {
                    server.stop();
                }
            }
        }
    }

    /** A tool's progress notification streams on the request's own SSE reply, before its result. */
    @Test
    void aToolReplyStreamsItsProgressBeforeItsResult() throws Exception {
        start();
        var progress = new CopyOnWriteArrayList<String>();
        connect(
                spec ->
                        spec.progressConsumer(
                                p -> Mono.fromRunnable(() -> progress.add(p.message()))));
        assertEquals(1, host.sessions.size());

        McpSchema.CallToolResult result =
                sdkClient
                        .callTool(
                                McpSchema.CallToolRequest.builder("progress")
                                        .arguments(Map.of())
                                        .progressToken("p1")
                                        .build())
                        .block(TIMEOUT);

        assertEquals("done", text(result));
        await(() -> progress.contains("half"));
        assertEquals(List.of("notification", "response"), host.replies.get("progress"));
    }

    /** The client's listening stream carries a notification the server sends to every session. */
    @Test
    void theListeningStreamCarriesServerNotifications() throws Exception {
        start();
        var changed = new CountDownLatch(1);
        connect(spec -> spec.toolsChangeConsumer(tools -> Mono.fromRunnable(changed::countDown)));
        await(() -> host.listening.get() == 1);

        sdkServer.notifyToolsListChanged().block(TIMEOUT);

        assertTrue(changed.await(TIMEOUT.toSeconds(), TimeUnit.SECONDS));
    }

    /** A server request sent on a reply stream reaches the client, whose answer is posted back. */
    @Test
    void aServerRequestReachesTheClientAndItsAnswerComesBack() throws Exception {
        start();
        connect(
                spec ->
                        spec.capabilities(
                                        McpSchema.ClientCapabilities.builder().roots(true).build())
                                .roots(new McpSchema.Root("file:///spike", "spike")));

        McpSchema.CallToolResult result =
                sdkClient
                        .callTool(
                                McpSchema.CallToolRequest.builder("roots")
                                        .arguments(Map.of())
                                        .build())
                        .block(TIMEOUT);

        assertEquals("file:///spike", text(result));
        assertEquals(List.of("request", "response"), host.replies.get("roots"));
    }

    /** Closing the client deletes its session, which ends the listening stream. */
    @Test
    void deletingTheSessionEndsItAndItsStreams() throws Exception {
        start();
        connect(spec -> spec);
        await(() -> host.listening.get() == 1);
        String session = host.sessions.keySet().iterator().next();

        sdkClient.closeGracefully().block(TIMEOUT);
        sdkClient = null;

        await(() -> host.sessions.isEmpty() && host.listening.get() == 0);
        try (HttpClient http = HttpClient.newHttpClient()) {
            HttpResponse<String> response =
                    http.send(
                            HttpRequest.newBuilder(URI.create(target))
                                    .timeout(TIMEOUT)
                                    .header("Content-Type", "application/json")
                                    .header("Accept", "application/json, text/event-stream")
                                    .header("Mcp-Session-Id", session)
                                    .POST(
                                            HttpRequest.BodyPublishers.ofString(
                                                    "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}"))
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());
            assertEquals(404, response.statusCode());
        }
    }

    /** A listening stream whose client goes away is released, and the session stays usable. */
    @Test
    void aDisconnectedListeningStreamIsReleased() throws Exception {
        start();
        connect(spec -> spec);
        await(() -> host.listening.get() == 1);
        String session = host.sessions.keySet().iterator().next();
        int port = server.getActualPort();

        // A second listening stream replaces the client's, then its peer goes away.
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout((int) TIMEOUT.toMillis());
            OutputStream out = socket.getOutputStream();
            out.write(
                    ("GET /mcp HTTP/1.1\r\nHost: localhost:"
                                    + port
                                    + "\r\nAccept: text/event-stream\r\nMcp-Session-Id: "
                                    + session
                                    + "\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            var reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream(), StandardCharsets.US_ASCII));
            assertEquals("HTTP/1.1 200 OK", reader.readLine());
            await(() -> host.opened.get() == 2);
        }

        await(() -> host.listening.get() == 0);
        McpSchema.ListToolsResult tools = sdkClient.listTools().block(TIMEOUT);
        assertEquals(2, tools.tools().size());
    }

    private interface Customizer {
        McpClient.AsyncSpec apply(McpClient.AsyncSpec spec);
    }

    private void start() throws Exception {
        host = new McpLegacySessionHost(Duration.ofMillis(200), TIMEOUT);
        McpSchema.Tool progressTool =
                McpSchema.Tool.builder("progress", Map.of("type", "object")).build();
        McpSchema.Tool rootsTool =
                McpSchema.Tool.builder("roots", Map.of("type", "object")).build();
        sdkServer =
                McpServer.async(host)
                        .serverInfo("restlet-session-spike", "1")
                        .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                        .requestTimeout(TIMEOUT)
                        .tools(
                                McpServerFeatures.AsyncToolSpecification.builder()
                                        .tool(progressTool)
                                        .callHandler(
                                                (exchange, request) ->
                                                        exchange.progressNotification(
                                                                        new McpSchema
                                                                                .ProgressNotification(
                                                                                request
                                                                                        .progressToken(),
                                                                                1.0,
                                                                                2.0,
                                                                                "half",
                                                                                null))
                                                                .then(Mono.just(result("done"))))
                                        .build(),
                                McpServerFeatures.AsyncToolSpecification.builder()
                                        .tool(rootsTool)
                                        .callHandler(
                                                (exchange, request) ->
                                                        exchange.listRoots()
                                                                .map(
                                                                        roots ->
                                                                                result(
                                                                                        roots.roots()
                                                                                                .get(
                                                                                                        0)
                                                                                                .uri())))
                                        .build())
                        .build();
        server = new Server(new Context(), Protocol.HTTP, 0, host);
        server.start();
        target = "http://localhost:" + server.getActualPort() + "/mcp";
    }

    private void connect(Customizer customizer) {
        var transport =
                HttpClientStreamableHttpTransport.builder(
                                "http://localhost:" + server.getActualPort())
                        .endpoint("/mcp")
                        .build();
        sdkClient =
                customizer
                        .apply(
                                McpClient.async(transport)
                                        .requestTimeout(TIMEOUT)
                                        .initializationTimeout(TIMEOUT))
                        .build();
        sdkClient.initialize().block(TIMEOUT);
    }

    private static McpSchema.CallToolResult result(String text) {
        return McpSchema.CallToolResult.builder().addTextContent(text).build();
    }

    private static String text(McpSchema.CallToolResult result) {
        return ((McpSchema.TextContent) result.content().get(0)).text();
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long end = System.nanoTime() + TIMEOUT.toNanos();
        var tick = new CountDownLatch(1);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < end, "condition not met in time");
            tick.await(20, TimeUnit.MILLISECONDS);
        }
    }
}
