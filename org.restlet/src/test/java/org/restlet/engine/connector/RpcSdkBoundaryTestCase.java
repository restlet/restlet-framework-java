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

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.Client;

class RpcSdkBoundaryTestCase {

    @Test
    void resolvesPinnedSdkModulesWithoutOfficialNetworkTransports(@TempDir Path temporary)
            throws Exception {
        Map<String, Class<?>> modules =
                Map.of(
                        "io.grpc",
                        io.grpc.Status.class,
                        "io.grpc.internal",
                        io.grpc.internal.ManagedChannelImplBuilder.class,
                        "io.grpc.protobuf",
                        io.grpc.protobuf.ProtoUtils.class,
                        "io.grpc.stub",
                        io.grpc.stub.ClientCalls.class,
                        "io.modelcontextprotocol.sdk.mcp.core",
                        io.modelcontextprotocol.server.McpServer.class,
                        "io.modelcontextprotocol.sdk.mcp.json.jackson2",
                        io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapperSupplier.class);
        for (var entry : modules.entrySet()) {
            Path jar =
                    Path.of(
                            entry.getValue()
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI());
            var descriptor = ModuleFinder.of(jar).findAll().iterator().next().descriptor();
            assertEquals(entry.getKey(), descriptor.name());
            assertTrue(descriptor.isAutomatic(), entry.getKey());
        }

        String classPath =
                System.getProperty(
                        "surefire.test.class.path", System.getProperty("java.class.path"));
        List<Path> dependencies =
                Arrays.stream(classPath.split(File.pathSeparator))
                        .map(Path::of)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".jar"))
                        .filter(
                                path ->
                                        !path.getFileName()
                                                .toString()
                                                .startsWith("grpc-netty-shaded-"))
                        .toList();
        var paths = new java.util.ArrayList<>(dependencies);
        paths.add(
                Path.of(Client.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        Path descriptor = temporary.resolve("module-info.java");
        Path example = temporary.resolve("PublicTransportExample.java");
        Files.writeString(descriptor, "module r1.consumer { requires org.restlet; }");
        Files.writeString(
                example,
                """
                package example;
                import java.io.IOException;
                import java.io.InputStream;
                import java.io.OutputStream;
                import org.restlet.util.StreamTransport;
                public final class PublicTransportExample {
                    public static void exchange(InputStream input, OutputStream output, Runnable cancel)
                            throws IOException {
                        try (var transport = new StreamTransport(input, output, cancel)) {
                            transport.start();
                            transport.write(new byte[] {0, 1, 2});
                            transport.read(new byte[8], 0, 8);
                        }
                    }
                }
                """);
        var diagnostics = new java.io.ByteArrayOutputStream();
        String modulePath =
                paths.stream()
                        .map(Path::toString)
                        .collect(java.util.stream.Collectors.joining(File.pathSeparator));
        assertEquals(
                0,
                javax.tools.ToolProvider.getSystemJavaCompiler()
                        .run(
                                null,
                                diagnostics,
                                diagnostics,
                                "--module-path",
                                modulePath,
                                "-d",
                                temporary.resolve("classes").toString(),
                                descriptor.toString(),
                                example.toString()),
                diagnostics.toString());
        Path grpcExample = temporary.resolve("GrpcBoundaryExample.java");
        Files.writeString(
                grpcExample,
                """
                package example;
                import io.grpc.CallOptions;
                import io.grpc.ClientCall;
                import io.grpc.ManagedChannel;
                import io.grpc.MethodDescriptor;
                public final class GrpcBoundaryExample {
                    public static <I, O> ClientCall<I, O> call(ManagedChannel channel,
                            MethodDescriptor<I, O> method, CallOptions options) {
                        return channel.newCall(method, options);
                    }
                }
                """);
        Path mcpExample = temporary.resolve("McpBoundaryExample.java");
        Files.writeString(
                mcpExample,
                """
                package example;
                import java.io.IOException;
                import io.modelcontextprotocol.json.McpJsonMapper;
                import io.modelcontextprotocol.spec.McpSchema;
                public final class McpBoundaryExample {
                    public static McpSchema.JSONRPCMessage decode(McpJsonMapper mapper, String json)
                            throws IOException {
                        return McpSchema.deserializeJsonRpcMessage(mapper, json);
                    }
                }
                """);
        for (var entry :
                Map.of("io.grpc", grpcExample, "io.modelcontextprotocol.sdk.mcp.core", mcpExample)
                        .entrySet()) {
            Files.writeString(
                    descriptor, "module r1.consumer { requires " + entry.getKey() + "; }");
            diagnostics.reset();
            assertEquals(
                    0,
                    javax.tools.ToolProvider.getSystemJavaCompiler()
                            .run(
                                    null,
                                    diagnostics,
                                    diagnostics,
                                    "--module-path",
                                    modulePath,
                                    "-d",
                                    temporary.resolve(entry.getKey()).toString(),
                                    descriptor.toString(),
                                    entry.getValue().toString()),
                    diagnostics.toString());
        }
        ModuleFinder finder = ModuleFinder.of(paths.toArray(Path[]::new));
        var roots = new java.util.HashSet<>(modules.keySet());
        roots.add("org.restlet");
        Configuration configuration =
                ModuleLayer.boot().configuration().resolveAndBind(finder, ModuleFinder.of(), roots);
        ModuleLayer layer =
                ModuleLayer.boot()
                        .defineModulesWithOneLoader(
                                configuration, ClassLoader.getPlatformClassLoader());
        assertTrue(layer.findModule("org.restlet").isPresent());
        assertFalse(
                layer.findModule("org.restlet")
                        .orElseThrow()
                        .isExported("org.restlet.engine.stdio"));
        Class<?> transportApi =
                layer.findLoader("org.restlet").loadClass("org.restlet.util.StreamTransport");
        var bytes = new java.io.ByteArrayOutputStream();
        Object transport =
                transportApi
                        .getConstructor(
                                java.io.InputStream.class,
                                java.io.OutputStream.class,
                                Runnable.class)
                        .newInstance(
                                new java.io.ByteArrayInputStream(new byte[0]),
                                bytes,
                                (Runnable) () -> {});
        transportApi.getMethod("start").invoke(transport);
        transportApi.getMethod("write", byte[].class).invoke(transport, new byte[] {0, 1, 2});
        transportApi.getMethod("stop").invoke(transport);
        org.junit.jupiter.api.Assertions.assertArrayEquals(
                new byte[] {0, 1, 2}, bytes.toByteArray());
        assertTrue(
                layer.findModule("org.restlet").orElseThrow().getDescriptor().requires().stream()
                        .noneMatch(
                                requirement ->
                                        requirement.name().startsWith("io.grpc")
                                                || requirement
                                                        .name()
                                                        .startsWith("io.modelcontextprotocol")));
        assertFalse(layer.findModule("io.grpc.netty.shaded").isPresent());
        assertFalse(layer.findModule("io.grpc.netty").isPresent());
        assertFalse(layer.findModule("io.grpc.okhttp").isPresent());
        for (var entry : modules.entrySet()) {
            Class<?> loaded =
                    layer.findLoader(entry.getKey()).loadClass(entry.getValue().getName());
            assertEquals(entry.getKey(), loaded.getModule().getName());
            assertEquals(layer, loaded.getModule().getLayer());
            loaded.getDeclaredConstructors();
        }

        Class<?> supplier =
                layer.findLoader("io.modelcontextprotocol.sdk.mcp.core")
                        .loadClass("io.modelcontextprotocol.json.McpJsonMapperSupplier");
        var providers = ServiceLoader.load(layer, supplier).stream().toList();
        assertEquals(1, providers.size());
        assertEquals(
                "io.modelcontextprotocol.sdk.mcp.json.jackson2",
                providers.getFirst().get().getClass().getModule().getName());
        Class<?> channelProvider =
                layer.findLoader("io.grpc").loadClass("io.grpc.ManagedChannelProvider");
        assertEquals(0, ServiceLoader.load(layer, channelProvider).stream().count());
    }
}
