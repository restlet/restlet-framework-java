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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restlet.Client;

/**
 * Consumers in child JVMs. A named module calls a restJson1 service through the transport, set
 * explicitly or discovered, and hosts a generated service with the SDK server on the class path. A
 * class-path consumer discovers both the server and the transport, over RPCv2 CBOR. In smithy-java
 * 1.8.0, the RPCv2 CBOR modules and the server modules split packages (smithy-java#1437), so they
 * stay off the module path.
 */
class SmithyModuleTestCase {
    /**
     * Jars left off the module path: the RPCv2 CBOR modules and the SDK server, and the code
     * generator of the tests, used at build time only, whose modules also split packages.
     */
    private static final List<String> EXCLUDED =
            List.of(
                    "aws-server-",
                    "client-rpcv2",
                    "server-",
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
        String modulePath = modulePath();
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
        compile(
                "--module-path",
                modulePath,
                "-d",
                classes.toString(),
                descriptor.toString(),
                example.toString());
        run(
                temp,
                "--module-path",
                modulePath + File.pathSeparator + classes,
                "--module",
                "r5.consumer/example.Main");
    }

    /**
     * A named module hosts a generated service with the adapter. The SDK server and the generated
     * code are on the class path, which the named modules read.
     */
    @Test
    void namedConsumerHostsAService(@TempDir Path temp) throws Exception {
        String classPath = join(serverClassPath());
        String modulePath = modulePath();
        Path descriptor = temp.resolve("module-info.java");
        Path example = temp.resolve("Main.java");
        Files.writeString(
                descriptor,
                """
                module r5.server.consumer {
                    requires org.restlet.ext.smithy;
                    requires software.amazon.smithy.java.aws.client.restjson;
                    requires software.amazon.smithy.java.core;
                }
                """);
        Files.writeString(
                example,
                """
                package example;
                import org.restlet.Client;
                import org.restlet.Context;
                import org.restlet.Server;
                import org.restlet.data.Protocol;
                import org.restlet.ext.smithy.SmithyClientAdapter;
                import org.restlet.ext.smithy.SmithyServerAdapter;
                import org.restlet.ext.smithy.generated.notes.client.NotesClient;
                import org.restlet.ext.smithy.generated.notes.model.GetNoteInput;
                import org.restlet.ext.smithy.generated.notes.model.GetNoteOutput;
                import org.restlet.ext.smithy.generated.notes.model.PutNoteOutput;
                import org.restlet.ext.smithy.generated.notes.model.RequiredUploadOutput;
                import org.restlet.ext.smithy.generated.notes.model.UploadOutput;
                import org.restlet.ext.smithy.generated.notes.model.WaitOutput;
                import org.restlet.ext.smithy.generated.notes.service.GetNoteOperation;
                import org.restlet.ext.smithy.generated.notes.service.Notes;
                import org.restlet.ext.smithy.generated.notes.service.PutNoteOperation;
                import org.restlet.ext.smithy.generated.notes.service.RequiredUploadOperation;
                import org.restlet.ext.smithy.generated.notes.service.UploadOperation;
                import org.restlet.ext.smithy.generated.notes.service.WaitOperation;
                import org.restlet.routing.Router;
                public final class Main {
                    public static void main(String[] args) throws Exception {
                        var adapter = new SmithyServerAdapter();
                        adapter.getServices().add(Notes.builder()
                            .addGetNoteOperation((GetNoteOperation) (input, context) ->
                                GetNoteOutput.builder().text("named " + input.getId()).build())
                            .addPutNoteOperation((PutNoteOperation) (input, context) ->
                                PutNoteOutput.builder().build())
                            .addRequiredUploadOperation((RequiredUploadOperation)
                                (input, context) -> RequiredUploadOutput.builder().build())
                            .addUploadOperation((UploadOperation) (input, context) ->
                                UploadOutput.builder().build())
                            .addWaitOperation((WaitOperation) (input, context) ->
                                WaitOutput.builder().build())
                            .build());
                        adapter.start();
                        var router = new Router();
                        adapter.attachTo(router);
                        var server = new Server(Protocol.HTTP, 0, router);
                        server.start();
                        var context = new Context();
                        context.getParameters().set("followRedirects", "false");
                        var client = new Client(context, Protocol.HTTP);
                        client.start();
                        var notes = NotesClient.builder()
                            .transport(new SmithyClientAdapter(client))
                            .endpoint("http://localhost:" + server.getActualPort())
                            .build();
                        try {
                            var text = notes.getNote(GetNoteInput.builder().id("n1").build())
                                .getText();
                            if (!"named n1".equals(text)) {
                                throw new AssertionError("Round trip failed: " + text);
                            }
                        } finally {
                            try { client.stop(); }
                            finally { try { server.stop(); } finally { adapter.stop(); } }
                        }
                    }
                }
                """);
        Path classes = temp.resolve("classes");
        compile(
                "--module-path",
                modulePath,
                "--class-path",
                classPath,
                "--add-reads",
                "org.restlet.ext.smithy=ALL-UNNAMED",
                "--add-reads",
                "r5.server.consumer=ALL-UNNAMED",
                "-d",
                classes.toString(),
                descriptor.toString(),
                example.toString());
        run(
                temp,
                "--module-path",
                modulePath + File.pathSeparator + classes,
                "--class-path",
                classPath,
                "--add-reads",
                "org.restlet.ext.smithy=ALL-UNNAMED",
                "--add-reads",
                "r5.server.consumer=ALL-UNNAMED",
                "--module",
                "r5.server.consumer/example.Main");
    }

    /**
     * On the class path, SDK servers and clients built without naming one discover the Restlet
     * server and transport, here over RPCv2 CBOR.
     */
    @Test
    void classPathConsumerDiscoversTheServer(@TempDir Path temp) throws Exception {
        String classPath = join(Stream.concat(testClassPath().stream(), Stream.of(testClasses())));
        Path example = temp.resolve("Main.java");
        Files.writeString(
                example,
                """
                package example;
                import java.net.ServerSocket;
                import java.net.URI;
                import org.restlet.ext.smithy.SmithyServerBuilder;
                import org.restlet.ext.smithy.generated.counter.client.CounterClient;
                import org.restlet.ext.smithy.generated.counter.model.AddInput;
                import org.restlet.ext.smithy.generated.counter.model.AddOutput;
                import org.restlet.ext.smithy.generated.counter.service.AddOperation;
                import org.restlet.ext.smithy.generated.counter.service.Counter;
                import software.amazon.smithy.java.client.core.ClientTransportFactory;
                import software.amazon.smithy.java.server.Server;
                public final class Main {
                    public static void main(String[] args) throws Exception {
                        var builder = Server.builder();
                        if (!(builder instanceof SmithyServerBuilder)) {
                            throw new AssertionError("Discovered " + builder);
                        }
                        if (!(Server.builder("restlet") instanceof SmithyServerBuilder)) {
                            throw new AssertionError("Not found by name");
                        }
                        var first = ClientTransportFactory.load(Main.class.getClassLoader())
                            .getFirst().name();
                        if (!"restlet".equals(first)) {
                            throw new AssertionError("Discovered first: " + first);
                        }
                        int port;
                        try (var socket = new ServerSocket(0)) {
                            port = socket.getLocalPort();
                        }
                        var server = builder
                            .endpoints(URI.create("http://localhost:" + port))
                            .addService(Counter.builder()
                                .addAddOperation((AddOperation) (input, context) ->
                                    AddOutput.builder().total(input.getValue() + 1).build())
                                .build())
                            .build();
                        server.start();
                        var client = CounterClient.builder()
                            .endpoint("http://localhost:" + port)
                            .build();
                        try {
                            long total = client.add(AddInput.builder().value(1L).label("cp")
                                .build()).getTotal();
                            if (total != 2L) {
                                throw new AssertionError("Round trip failed: " + total);
                            }
                        } finally {
                            // The generated interface isn't closeable: its client owns the
                            // discovered transport.
                            try { ((AutoCloseable) client).close(); }
                            finally { server.shutdown().get(); }
                        }
                    }
                }
                """);
        Path classes = temp.resolve("classes");
        compile("--class-path", classPath, "-d", classes.toString(), example.toString());
        run(temp, "--class-path", classPath + File.pathSeparator + classes, "example.Main");
    }

    /** The test class path's jars, with Restlet's and this module's classes. */
    private static List<Path> testClassPath() throws Exception {
        String classPath =
                System.getProperty(
                        "surefire.test.class.path", System.getProperty("java.class.path"));
        var paths =
                new ArrayList<>(
                        Arrays.stream(classPath.split(File.pathSeparator))
                                .map(Path::of)
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".jar"))
                                .toList());
        paths.add(classesOf(Client.class));
        paths.add(classesOf(SmithyClientAdapter.class));
        return paths.stream().distinct().toList();
    }

    /** The module path of the named consumers, without the jars that split packages. */
    private static String modulePath() throws Exception {
        return join(
                testClassPath().stream().filter(path -> !excluded(path.getFileName().toString())));
    }

    /** The SDK server jars, and the generated test code, beside a named consumer. */
    private static Stream<Path> serverClassPath() throws Exception {
        return Stream.concat(
                testClassPath().stream()
                        .filter(
                                path -> {
                                    String name = path.getFileName().toString();
                                    return name.startsWith("server-")
                                            || name.startsWith("aws-server-");
                                }),
                Stream.of(testClasses()));
    }

    private static Path testClasses() throws Exception {
        return classesOf(SmithyModuleTestCase.class);
    }

    private static Path classesOf(Class<?> type) throws Exception {
        return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private static String join(Stream<Path> paths) {
        return paths.distinct().map(Path::toString).collect(Collectors.joining(File.pathSeparator));
    }

    private static void compile(String... arguments) {
        var diagnostics = new ByteArrayOutputStream();
        var stream = new PrintStream(diagnostics, true);
        assertEquals(
                0,
                ToolProvider.findFirst("javac").orElseThrow().run(stream, stream, arguments),
                diagnostics.toString());
    }

    private static void run(Path temp, String... arguments) throws Exception {
        var command = new ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.addAll(List.of(arguments));
        Path output = temp.resolve("consumer.log");
        Process child =
                new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .redirectOutput(output.toFile())
                        .start();
        try {
            if (!child.waitFor(60, TimeUnit.SECONDS)) {
                child.descendants().forEach(ProcessHandle::destroy);
                fail("Consumer did not terminate: " + Files.readString(output));
            }
            assertEquals(0, child.exitValue(), Files.readString(output));
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                assertTrue(child.waitFor(10, TimeUnit.SECONDS));
            }
        }
    }
}
