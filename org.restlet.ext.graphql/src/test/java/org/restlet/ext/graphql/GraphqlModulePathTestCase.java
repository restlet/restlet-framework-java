/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Compiles a named consumer module against the extension and runs a GraphQL round trip in a module
 * layer. graphql-java, java-dataloader and reactive-streams have no module descriptor: this checks
 * that their automatic module names resolve.
 */
class GraphqlModulePathTestCase {

    private static final String MODULE_INFO =
            """
            module consumer {
                requires org.restlet.ext.graphql;
                exports consumer;
            }
            """;

    private static final String MAIN =
            """
            package consumer;

            import graphql.ExecutionResult;
            import graphql.GraphQL;
            import graphql.schema.idl.RuntimeWiring;
            import graphql.schema.idl.SchemaGenerator;
            import graphql.schema.idl.SchemaParser;
            import java.util.Map;
            import java.util.function.Supplier;
            import org.restlet.Client;
            import org.restlet.Server;
            import org.restlet.data.Protocol;
            import org.restlet.ext.graphql.GraphqlClientAdapter;
            import org.restlet.ext.graphql.GraphqlServerAdapter;

            public class Main implements Supplier<String> {
                @Override
                public String get() {
                    try {
                        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                                .type("Query", t -> t.dataFetcher("hello", e -> "Hello "
                                        + e.getArgument("name")))
                                .build();
                        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
                        adapter.setGraphQL(GraphQL.newGraphQL(new SchemaGenerator()
                                .makeExecutableSchema(new SchemaParser()
                                        .parse("type Query { hello(name: String): String }"),
                                        wiring)).build());
                        Server server = new Server(Protocol.HTTP, 0, adapter);
                        server.start();
                        Client client = new Client(Protocol.HTTP);
                        client.start();
                        try {
                            GraphqlClientAdapter graphql = new GraphqlClientAdapter();
                            graphql.setClient(client);
                            graphql.setEndpoint("http://localhost:" + server.getActualPort());
                            graphql.start();
                            ExecutionResult result = graphql.execute(
                                    "query($n: String) { hello(name: $n) }", null,
                                    Map.of("n", "module"));
                            return result.getData().toString();
                        } finally {
                            client.stop();
                            server.stop();
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            """;

    @Test
    void namedConsumerResolvesAndRuns(@TempDir Path dir) throws Exception {
        List<Path> modulePath = modulePath();

        Path src = dir.resolve("src");
        Files.createDirectories(src.resolve("consumer"));
        Files.writeString(src.resolve("module-info.java"), MODULE_INFO);
        Files.writeString(src.resolve("consumer/Main.java"), MAIN);
        Path out = dir.resolve("out");

        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        int status =
                ToolProvider.getSystemJavaCompiler()
                        .run(
                                null,
                                diagnostics,
                                diagnostics,
                                "--module-path",
                                modulePath.stream()
                                        .map(Path::toString)
                                        .collect(Collectors.joining(File.pathSeparator)),
                                "-d",
                                out.toString(),
                                src.resolve("module-info.java").toString(),
                                src.resolve("consumer/Main.java").toString());
        assertEquals(0, status, () -> diagnostics.toString(StandardCharsets.UTF_8));

        List<Path> layerPath = new ArrayList<>(modulePath);
        layerPath.addFirst(out);
        Configuration configuration =
                ModuleLayer.boot()
                        .configuration()
                        .resolve(
                                ModuleFinder.of(layerPath.toArray(Path[]::new)),
                                ModuleFinder.of(),
                                Set.of("consumer"));
        assertNotNull(configuration.findModule("com.graphqljava").orElse(null));
        assertNotNull(configuration.findModule("org.reactivestreams").orElse(null));
        assertTrue(configuration.findModule("org.restlet.ext.websocket").isEmpty());

        ModuleLayer layer =
                ModuleLayer.boot()
                        .defineModulesWithOneLoader(
                                configuration, ClassLoader.getPlatformClassLoader());
        Class<?> main = layer.findLoader("consumer").loadClass("consumer.Main");
        @SuppressWarnings("unchecked")
        Supplier<String> consumer = (Supplier<String>) main.getConstructor().newInstance();
        assertEquals("{hello=Hello module}", consumer.get());
    }

    /** The modules of the test class path, one entry per module name, test tooling excluded. */
    private static List<Path> modulePath() {
        Map<String, Path> modules = new LinkedHashMap<>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Path path = Path.of(entry);
            if (entry.isEmpty()
                    || !Files.exists(path)
                    || entry.contains("test-classes")
                    || entry.contains("junit")
                    || entry.contains("opentest4j")
                    || entry.contains("apiguardian")
                    // The WebSocket extension is optional: the consumer must run without it.
                    || entry.contains("ext.websocket")) {
                continue;
            }
            try {
                for (ModuleReference module : ModuleFinder.of(path).findAll()) {
                    modules.putIfAbsent(module.descriptor().name(), path);
                }
            } catch (FindException | IllegalArgumentException _) {
                // Not usable as a module, for example a directory without a descriptor
            }
        }
        return List.copyOf(modules.values());
    }
}
