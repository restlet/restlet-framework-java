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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import graphql.ErrorType;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;

/** {@link GraphqlClientAdapter} against the extension's server and a third-party server. */
class GraphqlClientTestCase {

    private static final GraphQLSchema REMOTE_SCHEMA =
            UnExecutableSchemaGenerator.makeUnExecutableSchema(
                    new SchemaParser().parse(GraphqlTestSchemas.SDL));

    private Component component;

    private HttpServer thirdParty;

    private Client client;

    private String base;

    private String thirdPartyBase;

    private final AtomicInteger thirdPartyCalls = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);
        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        adapter.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        component.getDefaultHost().attach("/graphql", adapter);
        component.start();
        base = "http://localhost:" + component.getServers().get(0).getActualPort();

        // An independent HTTP stack serving graphql-java the legacy way: application/json and
        // 200 for every well-formed request, 422 with a GraphQL body for unknown paths.
        GraphQL graphQL = GraphqlTestSchemas.graphQL(new AtomicInteger());
        thirdParty = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        thirdParty.createContext("/", exchange -> thirdParty(exchange, graphQL));
        thirdParty.start();
        thirdPartyBase = "http://localhost:" + thirdParty.getAddress().getPort();

        client = new Client(Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.stop();
        component.stop();
        thirdParty.stop(0);
    }

    private void thirdParty(HttpExchange exchange, GraphQL graphQL) throws IOException {
        thirdPartyCalls.incrementAndGet();
        Map<String, Object> params =
                GraphqlJson.asObject(GraphqlJson.read(exchange.getRequestBody(), 1 << 20));
        ExecutionInput.Builder input =
                ExecutionInput.newExecutionInput().query((String) params.get("query"));
        Map<String, Object> variables = GraphqlJson.asObject(params.get("variables"));
        if (variables != null) {
            input.variables(variables);
        }
        int status = exchange.getRequestURI().getPath().equals("/graphql") ? 200 : 422;
        byte[] body = GraphqlJson.write(graphQL.execute(input).toSpecification());
        exchange.getResponseHeaders()
                .add(
                        "Content-Type",
                        status == 200
                                ? "application/json; charset=utf-8"
                                : "application/graphql-response+json");
        exchange.sendResponseHeaders(status, 0); // chunked
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private GraphqlClientAdapter adapter(String endpoint) {
        GraphqlClientAdapter adapter = new GraphqlClientAdapter();
        adapter.setClient(client);
        adapter.setEndpoint(endpoint);
        return adapter;
    }

    @Test
    void variablesAreJsonValuesNotTemplates() throws Exception {
        String text = "{{name}} \"quoted\" {key=value} \\ é\n";
        for (String endpoint : List.of(base + "/graphql", thirdPartyBase + "/graphql")) {
            GraphqlClientAdapter graphql = adapter(endpoint);
            graphql.start();
            ExecutionResult result =
                    graphql.execute(
                            "query($in: EchoInput!) { echo(input: $in) }",
                            null,
                            Map.of("in", Map.of("text", text, "tags", List.of("a", "b"))));
            assertTrue(result.getErrors().isEmpty(), result.getErrors()::toString);
            assertEquals(Map.of("echo", text + "|2"), result.getData(), endpoint);
        }
    }

    @Test
    void legacyServerErrorsAreResults() throws Exception {
        GraphqlClientAdapter graphql = adapter(thirdPartyBase + "/graphql");
        graphql.start();
        ExecutionResult invalid = graphql.execute("{ unknownField }", null, null);
        assertFalse(invalid.isDataPresent());
        assertEquals(1, invalid.getErrors().size());

        GraphqlClientAdapter unprocessable = adapter(thirdPartyBase + "/other");
        unprocessable.start();
        ExecutionResult result = unprocessable.execute("{ hello }", null, null);
        assertEquals(Map.of("hello", "Hello world"), result.getData());
    }

    @Test
    void schemaValidatesLocallyWithoutNetworkCall() throws Exception {
        GraphqlClientAdapter graphql = adapter(thirdPartyBase + "/graphql");
        graphql.setSchema(REMOTE_SCHEMA);
        graphql.start();
        assertSame(REMOTE_SCHEMA, graphql.getSchema());

        ExecutionResult unknown = graphql.execute("{ unknownField }", null, null);
        assertFalse(unknown.isDataPresent());
        assertEquals(ErrorType.ValidationError, unknown.getErrors().get(0).getErrorType());

        ExecutionResult syntax = graphql.execute("{ hello ", null, null);
        assertEquals(ErrorType.InvalidSyntax, syntax.getErrors().get(0).getErrorType());

        ExecutionResult parsed = graphql.execute(Parser.parse("{ unknownField }"), null, Map.of());
        assertEquals(ErrorType.ValidationError, parsed.getErrors().get(0).getErrorType());
        assertEquals(0, thirdPartyCalls.get());

        ExecutionResult valid = graphql.execute(Parser.parse("{ hello }"), null, null);
        assertEquals(Map.of("hello", "Hello world"), valid.getData());
        assertEquals(1, thirdPartyCalls.get());
    }

    @Test
    void largeResponsesFailInsteadOfBeingTruncated() {
        GraphqlClientAdapter graphql = adapter(base + "/graphql");
        graphql.setMaxResponseBytes(64);
        graphql.start();
        GraphqlTransportException e =
                assertThrows(
                        GraphqlTransportException.class,
                        () ->
                                graphql.execute(
                                        "query($n: String) { hello(name: $n) }",
                                        null,
                                        Map.of("n", "x".repeat(100))));
        assertEquals(Status.SUCCESS_OK, e.getStatus());
        assertNull(e.getBodyExcerpt());

        GraphqlClientAdapter chunked = adapter(thirdPartyBase + "/graphql");
        chunked.setMaxResponseBytes(64);
        chunked.start();
        assertThrows(
                GraphqlTransportException.class,
                () ->
                        chunked.execute(
                                "query($n: String) { hello(name: $n) }",
                                null,
                                Map.of("n", "x".repeat(100))));
    }

    @Test
    void requestCustomizerAddsHeaders() throws Exception {
        GraphqlClientAdapter graphql = adapter(base + "/graphql");
        graphql.setRequestCustomizer(request -> request.getHeaders().add("X-Api-Key", "k123"));
        graphql.start();
        ExecutionResult result = graphql.execute("{ header(name: \"X-Api-Key\") }", null, null);
        assertEquals(Map.of("header", "k123"), result.getData());
    }

    @Test
    void configurationIsFrozenOnceStarted() {
        GraphqlClientAdapter graphql = adapter(base + "/graphql");
        assertThrows(IllegalArgumentException.class, () -> graphql.setMaxResponseBytes(0));
        assertNull(graphql.getParserOptions());
        graphql.start();
        assertEquals(
                GraphqlClientAdapter.DEFAULT_MAX_RESPONSE_BYTES, graphql.getMaxResponseBytes());
        assertTrue(graphql.getParserOptions() != null);
        assertNull(graphql.getRequestCustomizer());
        assertNull(graphql.getContext());
        assertThrows(IllegalStateException.class, () -> graphql.setSchema(null));
        assertThrows(IllegalStateException.class, () -> graphql.setParserOptions(null));
        assertThrows(IllegalStateException.class, () -> graphql.setMaxResponseBytes(1));
        assertThrows(IllegalStateException.class, () -> graphql.setRequestCustomizer(null));
        assertThrows(IllegalStateException.class, graphql::start);
    }
}
