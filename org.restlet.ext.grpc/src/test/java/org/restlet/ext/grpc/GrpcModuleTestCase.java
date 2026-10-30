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
    void nativeAdaptersResolveWithoutSdkNetworkTransportOrInternalExports(@TempDir Path temp)
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
                import org.restlet.ext.grpc.GrpcServerAdapter;
                import org.restlet.routing.Router;
                import io.grpc.CallOptions;
                import io.grpc.ClientCall;
                import io.grpc.Metadata;
                import io.grpc.MethodDescriptor;
                import io.grpc.ServerCall;
                import io.grpc.ServerServiceDefinition;
                import io.grpc.Status;
                public final class Main {
                    public static void main(String[] args) throws Exception {
                        try {
                            Class.forName("io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder");
                            throw new AssertionError("SDK network transport is present");
                        } catch (ClassNotFoundException expected) { }
                        var marshaller = new MethodDescriptor.Marshaller<String>() {
                            public java.io.InputStream stream(String value) {
                                return new java.io.ByteArrayInputStream(
                                    value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            }
                            public String parse(java.io.InputStream input) {
                                try {
                                    return new String(input.readAllBytes(),
                                        java.nio.charset.StandardCharsets.UTF_8);
                                } catch (java.io.IOException e) {
                                    throw new java.io.UncheckedIOException(e);
                                }
                            }
                        };
                        var method = MethodDescriptor.<String, String>newBuilder()
                            .setType(MethodDescriptor.MethodType.UNARY)
                            .setFullMethodName("consumer.Echo/Call")
                            .setRequestMarshaller(marshaller)
                            .setResponseMarshaller(marshaller).build();
                        var server = new GrpcServerAdapter();
                        server.getServices().add(ServerServiceDefinition.builder("consumer.Echo")
                            .addMethod(method, (call, headers) -> {
                                call.request(1);
                                return new ServerCall.Listener<String>() {
                                    private String message;
                                    public void onMessage(String value) { message = value; }
                                    public void onHalfClose() {
                                        call.sendHeaders(new Metadata());
                                        call.sendMessage(message);
                                        call.close(Status.OK, new Metadata());
                                    }
                                };
                            }).build());
                        server.start();
                        var router = new Router();
                        server.attachTo(router);
                        var serverContext = new Context();
                        serverContext.getParameters().set("http.transport.protocol", "HTTP2");
                        var http = new org.restlet.Server(serverContext, Protocol.HTTP, 0, router);
                        http.start();
                        var context = new Context();
                        context.getParameters().set("httpClientTransportMode", "HTTP2");
                        context.getParameters().set("followRedirects", "false");
                        var client = new Client(context, Protocol.HTTP);
                        client.start();
                        try (var adapter = new GrpcClientAdapter()) {
                            adapter.setClient(client);
                            adapter.setPlaintext(true);
                            String authority = "localhost:" + http.getActualPort();
                            adapter.setTarget("dns:///" + authority);
                            adapter.start();
                            if (!adapter.getChannel().authority().equals(authority)) {
                                throw new AssertionError("Native authority was lost");
                            }
                            var done = new java.util.concurrent.CountDownLatch(1);
                            var reply = new java.util.concurrent.atomic.AtomicReference<String>();
                            var result = new java.util.concurrent.atomic.AtomicReference<Status>();
                            var call = adapter.getChannel().newCall(method,
                                CallOptions.DEFAULT.withDeadlineAfter(10,
                                    java.util.concurrent.TimeUnit.SECONDS));
                            call.start(new ClientCall.Listener<String>() {
                                public void onMessage(String value) { reply.set(value); }
                                public void onClose(Status status, Metadata trailers) {
                                    result.set(status);
                                    done.countDown();
                                }
                            }, new Metadata());
                            call.request(1);
                            call.sendMessage("packaged echo");
                            call.halfClose();
                            if (!done.await(15, java.util.concurrent.TimeUnit.SECONDS)
                                    || result.get() == null || !result.get().isOk()
                                    || !"packaged echo".equals(reply.get())) {
                                throw new AssertionError("Native round trip failed: " + result.get());
                            }
                            server.stop();
                            if (!http.isStarted()) {
                                throw new AssertionError("Borrowed listener was stopped");
                            }
                        } finally {
                            try { server.stop(); }
                            finally {
                                try { client.stop(); }
                                finally { http.stop(); }
                            }
                        }
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
