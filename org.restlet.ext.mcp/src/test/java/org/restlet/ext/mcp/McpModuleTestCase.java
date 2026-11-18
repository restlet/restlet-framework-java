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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.Client;

class McpModuleTestCase {
    @Test
    void namedConsumerUsesNativeSdkAndMapperWithoutInternalExports(@TempDir Path temporary)
            throws Exception {
        String classPath =
                System.getProperty(
                        "surefire.test.class.path", System.getProperty("java.class.path"));
        var dependencies =
                Arrays.stream(classPath.split(File.pathSeparator))
                        .map(Path::of)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".jar"))
                        .toList();
        assertFalse(
                dependencies.stream()
                        .anyMatch(p -> p.getFileName().toString().contains("mcp-transport")));
        var paths = new ArrayList<>(dependencies);
        paths.add(
                Path.of(Client.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        paths.add(
                Path.of(
                        McpServerAdapter.class
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .toURI()));
        String modulePath =
                paths.stream()
                        .distinct()
                        .map(path -> path.toString())
                        .collect(Collectors.joining(File.pathSeparator));
        Path descriptor = temporary.resolve("module-info.java");
        Path example = temporary.resolve("Main.java");
        Files.writeString(descriptor, "module r2.consumer { requires org.restlet.ext.mcp; }");
        Files.writeString(
                example,
                """
                package example;
                import io.modelcontextprotocol.modern.JsonRpc;
                import java.util.Map;
                import org.restlet.ext.mcp.McpRequestContext;
                import org.restlet.ext.mcp.McpServerAdapter;
                public final class Main {
                    public static void main(String[] args) throws Exception {
                        var server = new McpServerAdapter();
                        server.getInterceptors().add((message, context, chain) ->
                                chain.proceed(message, context));
                        server.start();
                        try {
                            var meta = Map.of("io.modelcontextprotocol/protocolVersion", "2026-07-28",
                                    "io.modelcontextprotocol/clientCapabilities", Map.of());
                            var result = server.dispatch(new JsonRpc.JSONRPCRequest(
                                    "server/discover", 1, Map.of("_meta", meta)),
                                    new McpRequestContext(null)).orElseThrow();
                            if (result.error() != null) { throw new AssertionError(result.error()); }
                        } finally { server.stop(); }
                    }
                }
                """);
        Path classes = temporary.resolve("classes");
        var diagnostics = new java.io.ByteArrayOutputStream();
        var diagnosticsStream = new java.io.PrintStream(diagnostics, true);
        assertEquals(
                0,
                java.util.spi.ToolProvider.findFirst("javac")
                        .orElseThrow()
                        .run(
                                diagnosticsStream,
                                diagnosticsStream,
                                "--module-path",
                                modulePath,
                                "-d",
                                classes.toString(),
                                descriptor.toString(),
                                example.toString()),
                diagnostics.toString());
        Path output = temporary.resolve("consumer.log");
        Process child =
                new ProcessBuilder(
                                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                                "--module-path",
                                modulePath + File.pathSeparator + classes,
                                "--module",
                                "r2.consumer/example.Main")
                        .redirectErrorStream(true)
                        .redirectOutput(output.toFile())
                        .start();
        try {
            assertTrue(child.waitFor(15, TimeUnit.SECONDS), "Named MCP consumer did not terminate");
            assertEquals(0, child.exitValue(), Files.readString(output));
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
            }
        }
    }
}
