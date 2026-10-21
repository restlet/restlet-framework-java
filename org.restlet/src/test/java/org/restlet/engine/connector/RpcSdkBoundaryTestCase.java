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
import org.restlet.Client;

class RpcSdkBoundaryTestCase {

    @Test
    void resolvesPinnedSdkModulesWithoutOfficialNetworkTransports() throws Exception {
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
