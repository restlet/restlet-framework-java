/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.Client;

/**
 * A named module calls a restJson1 service through the transport, set explicitly or discovered.
 * RPCv2 CBOR is left out: in smithy-java 1.8.0, its client modules split a package
 * (smithy-java#1437) and only work on the class path.
 */
class SmithyModuleTestCase {
    /**
     * Jars left off the module path: the RPCv2 CBOR client modules, and the code generator of the
     * tests, used at build time only, whose modules also split packages.
     */
    private static final List<String> EXCLUDED =
            List.of(
                    "client-rpcv2",
                    "codegen-",
                    "commonmark-",
                    "jsoup-",
                    "smithy-build-",
                    "smithy-cli-",
                    "smithy-codegen-core-",
                    "smithy-syntax-");

    private static boolean excluded(String jar) {
        return EXCLUDED.stream().anyMatch(jar::startsWith);
    }

    @Test
    void namedConsumerUsesTheTransport(@TempDir Path temp) throws Exception {
        String classPath =
                System.getProperty(
                        "surefire.test.class.path", System.getProperty("java.class.path"));
        var paths =
                new ArrayList<>(
                        Arrays.stream(classPath.split(File.pathSeparator))
                                .map(Path::of)
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".jar"))
                                .filter(path -> !excluded(path.getFileName().toString()))
                                .toList());
        paths.add(
                Path.of(Client.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        paths.add(
                Path.of(
                        SmithyClientAdapter.class
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .toURI()));
        String modulePath =
                paths.stream()
                        .distinct()
                        .map(Path::toString)
                        .collect(Collectors.joining(File.pathSeparator));
        Path descriptor = temp.resolve("module-info.java");
        Path example = temp.resolve("Main.java");
        Files.writeString(
                descriptor,
                """
                module r5.consumer {
                    requires org.restlet.ext.smithy;
                    requires software.amazon.smithy.java.aws.client.restjson;
                    requires software.amazon.smithy.java.core;
                    requires software.amazon.smithy.java.dynamicclient;
                    requires software.amazon.smithy.model;
                }
                """);
        Files.writeString(
                example,
                """
                package example;
                import java.util.Map;
                import org.restlet.Client;
                import org.restlet.Context;
                import org.restlet.Request;
                import org.restlet.Response;
                import org.restlet.Restlet;
                import org.restlet.Server;
                import org.restlet.data.MediaType;
                import org.restlet.data.Protocol;
                import org.restlet.ext.smithy.SmithyClientAdapter;
                import org.restlet.representation.StringRepresentation;
                import software.amazon.smithy.java.aws.client.restjson.RestJsonClientProtocol;
                import software.amazon.smithy.java.client.core.ClientTransportFactory;
                import software.amazon.smithy.java.core.serde.document.Document;
                import software.amazon.smithy.java.dynamicclient.DynamicClient;
                import software.amazon.smithy.model.Model;
                import software.amazon.smithy.model.shapes.ShapeId;
                public final class Main {
                    public static void main(String[] args) throws Exception {
                        ClassLoader loader = Main.class.getClassLoader();
                        var first = ClientTransportFactory.load(loader).getFirst().name();
                        if (!"restlet".equals(first)) {
                            throw new AssertionError("Discovered first: " + first);
                        }
                        Model model = Model.assembler(loader).discoverModels(loader)
                            .addUnparsedModel("consumer.smithy", String.join("\\n",
                                "$version: \\"2\\"",
                                "namespace consumer",
                                "use aws.protocols#restJson1",
                                "@restJson1",
                                "service Echo { version: \\"1\\", operations: [Say] }",
                                "@http(method: \\"POST\\", uri: \\"/say\\")",
                                "operation Say {",
                                "    input := { message: String }",
                                "    output := { message: String }",
                                "}"))
                            .assemble().unwrap();
                        var server = new Server(Protocol.HTTP, 0, new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity(new StringRepresentation(
                                    request.getEntityAsText(), MediaType.APPLICATION_JSON));
                            }
                        });
                        server.start();
                        var context = new Context();
                        context.getParameters().set("followRedirects", "false");
                        var client = new Client(context, Protocol.HTTP);
                        client.start();
                        var service = ShapeId.from("consumer#Echo");
                        try {
                            for (boolean discovered : new boolean[] {false, true}) {
                                var builder = DynamicClient.builder().model(model)
                                    .serviceId(service)
                                    .protocol(new RestJsonClientProtocol(service))
                                    .endpoint("http://localhost:" + server.getActualPort());
                                if (!discovered) {
                                    builder.transport(new SmithyClientAdapter(client));
                                }
                                try (var echo = builder.build()) {
                                    var output = echo.call("Say",
                                        Document.of(Map.of("message", Document.of("packaged"))));
                                    if (!"packaged".equals(output.getMember("message").asString())) {
                                        throw new AssertionError("Round trip failed: " + output);
                                    }
                                }
                            }
                            if (!client.isStarted()) {
                                throw new AssertionError("Borrowed client was stopped");
                            }
                        } finally {
                            try { client.stop(); }
                            finally { server.stop(); }
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
                                "r5.consumer/example.Main")
                        .redirectErrorStream(true)
                        .redirectOutput(output.toFile())
                        .start();
        try {
            assertTrue(child.waitFor(60, TimeUnit.SECONDS), "Named consumer did not terminate");
            assertEquals(0, child.exitValue(), Files.readString(output));
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                assertTrue(child.waitFor(10, TimeUnit.SECONDS));
            }
        }
    }
}
