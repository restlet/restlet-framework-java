/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class GrpcModuleTestCase {
    @Test
    void nativeChannelResolvesWithoutSdkNetworkTransportOrInternalExports(@TempDir Path temp)
            throws Exception {
        String classPath =
                System.getProperty(
                        "surefire.test.class.path", System.getProperty("java.class.path"));
        var paths =
                new ArrayList<>(
                        Arrays.stream(classPath.split(File.pathSeparator))
                                .map(Path::of)
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".jar"))
                                .filter(
                                        path ->
                                                !path.getFileName()
                                                        .toString()
                                                        .startsWith("grpc-netty"))
                                .toList());
        paths.add(
                Path.of(
                        System.getProperty(
                                "restlet.consumer.core",
                                Path.of(
                                                Client.class
                                                        .getProtectionDomain()
                                                        .getCodeSource()
                                                        .getLocation()
                                                        .toURI())
                                        .toString())));
        paths.add(
                Path.of(
                        System.getProperty(
                                "restlet.consumer.grpc",
                                Path.of(
                                                GrpcClientAdapter.class
                                                        .getProtectionDomain()
                                                        .getCodeSource()
                                                        .getLocation()
                                                        .toURI())
                                        .toString())));
        String modulePath =
                paths.stream()
                        .distinct()
                        .map(path -> path.toString())
                        .collect(Collectors.joining(File.pathSeparator));
        Path descriptor = temp.resolve("module-info.java");
        Path example = temp.resolve("Main.java");
        Files.writeString(descriptor, "module r3.consumer { requires org.restlet.ext.grpc; }");
        Files.writeString(
                example,
                """
                package example;
                import org.restlet.Client;
                import org.restlet.Context;
                import org.restlet.data.Protocol;
                import org.restlet.ext.grpc.GrpcClientAdapter;
                public final class Main {
                    public static void main(String[] args) throws Exception {
                        try {
                            Class.forName("io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder");
                            throw new AssertionError("SDK network transport is present");
                        } catch (ClassNotFoundException expected) { }
                        var context = new Context();
                        context.getParameters().set("httpClientTransportMode", "HTTP2");
                        context.getParameters().set("followRedirects", "false");
                        var client = new Client(context, Protocol.HTTP);
                        client.start();
                        try (var adapter = new GrpcClientAdapter()) {
                            adapter.setClient(client);
                            adapter.setPlaintext(true);
                            adapter.setTarget("dns:///localhost:12345");
                            adapter.start();
                            if (!adapter.getChannel().authority().equals("localhost:12345")) {
                                throw new AssertionError("Native authority was lost");
                            }
                        } finally { client.stop(); }
                    }
                }
                """);
        Path classes = temp.resolve("classes");
        var diagnostics = new java.io.ByteArrayOutputStream();
        var diagnosticStream = new java.io.PrintStream(diagnostics, true);
        assertEquals(
                0,
                java.util.spi.ToolProvider.findFirst("javac")
                        .orElseThrow()
                        .run(
                                diagnosticStream,
                                diagnosticStream,
                                "--module-path",
                                modulePath,
                                "-d",
                                classes.toString(),
                                descriptor.toString(),
                                example.toString()),
                diagnostics.toString());
        Path output = temp.resolve("consumer.log");
        Process child =
                new ProcessBuilder(
                                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                                "--module-path",
                                modulePath + File.pathSeparator + classes,
                                "--module",
                                "r3.consumer/example.Main")
                        .redirectErrorStream(true)
                        .redirectOutput(output.toFile())
                        .start();
        try {
            assertTrue(child.waitFor(30, TimeUnit.SECONDS), "Named consumer did not terminate");
            assertEquals(0, child.exitValue(), Files.readString(output));
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                assertTrue(child.waitFor(10, TimeUnit.SECONDS));
            }
        }
    }
}
