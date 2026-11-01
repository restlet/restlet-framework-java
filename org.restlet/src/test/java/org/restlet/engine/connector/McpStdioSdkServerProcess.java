/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import reactor.core.publisher.Mono;

public final class McpStdioSdkServerProcess {

    private McpStdioSdkServerProcess() {}

    public static void main(String[] args) throws InterruptedException {
        StdioServerTransportProvider transport =
                new StdioServerTransportProvider(McpJsonDefaults.getMapper());
        McpSchema.Tool tool =
                new McpSchema.Tool(
                        "echo",
                        null,
                        "Returns the supplied text",
                        Map.of(
                                "type",
                                "object",
                                "properties",
                                Map.of("text", Map.of("type", "string")),
                                "required",
                                List.of("text")),
                        null,
                        null,
                        null,
                        null);
        var server =
                McpServer.async(transport)
                        .serverInfo("restlet-r0-stdio-spike", "1")
                        .toolCall(
                                tool,
                                (context, call) ->
                                        Mono.just(
                                                McpSchema.CallToolResult.builder()
                                                        .addTextContent(
                                                                "echo:"
                                                                        + call.arguments()
                                                                                .get("text"))
                                                        .build()))
                        .build();
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> server.closeGracefully().block(),
                                "mcp-stdio-r0-server-close"));
        new CountDownLatch(1).await();
    }
}
