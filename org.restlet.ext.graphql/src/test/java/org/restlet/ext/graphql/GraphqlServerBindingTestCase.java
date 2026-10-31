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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.parser.ParserOptions;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.ext.graphql.internal.GraphqlMediaTypes;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

/** The GraphQL over HTTP binding of {@link GraphqlServerAdapter}. */
class GraphqlServerBindingTestCase {

    private static final MediaType GRAPHQL = GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON;

    private Component component;

    private Client client;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);

        GraphqlServerAdapter post = adapter();
        component.getDefaultHost().attach("/graphql", post);

        GraphqlServerAdapter get = adapter();
        get.setGetEnabled(true);
        get.setMaxRequestBytes(64);
        get.setParserOptions(ParserOptions.newParserOptions().maxTokens(20).build());
        get.setExecutionInputCustomizer(
                (request, input) -> {
                    BatchLoader<String, String> upper =
                            keys ->
                                    CompletableFuture.completedFuture(
                                            keys.stream().map(String::toUpperCase).toList());
                    DataLoaderRegistry registry = new DataLoaderRegistry();
                    registry.register("upper", DataLoaderFactory.newDataLoader(upper));
                    input.dataLoaderRegistry(registry);
                    // Can't replace the request set by the adapter.
                    input.graphQLContext(Map.<Object, Object>of(RestletGraphql.REQUEST, "spoofed"));
                });
        component.getDefaultHost().attach("/limited", get);

        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        new Context(), false, ChallengeScheme.HTTP_BASIC, "test");
        authenticator.setVerifier(verifier);
        authenticator.setNext(adapter());
        component.getDefaultHost().attach("/secured", authenticator);

        component.start();
        base = "http://localhost:" + component.getServers().get(0).getActualPort();
        client = new Client(Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.stop();
        component.stop();
    }

    private static GraphqlServerAdapter adapter() {
        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        adapter.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        return adapter;
    }

    private Response post(String path, String body, MediaType accept) {
        Request request = new Request(Method.POST, base + path);
        request.setEntity(new StringRepresentation(body, MediaType.APPLICATION_JSON));
        if (accept != null) {
            request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(accept));
        }
        return client.handle(request);
    }

    private Response get(String path, Map<String, String> params) {
        Reference reference = new Reference(base + path);
        params.forEach(reference::addQueryParameter);
        Request request = new Request(Method.GET, reference);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(GRAPHQL));
        return client.handle(request);
    }

    private static Map<String, Object> json(Response response) throws Exception {
        return GraphqlJson.asObject(GraphqlJson.parse(response.getEntity().getText()));
    }

    @Test
    void getIsDisabledByDefault() {
        Response response = get("/graphql", Map.of("query", "{ hello }"));
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response.getStatus());
        assertTrue(response.getAllowedMethods().contains(Method.POST));
    }

    @Test
    void getExecutesQueriesOnly() throws Exception {
        Response query =
                get(
                        "/limited",
                        Map.of(
                                "query",
                                "query Q($n: String) { hello(name: $n) }",
                                "operationName",
                                "Q",
                                "variables",
                                "{\"n\":\"GET\"}"));
        assertEquals(Status.SUCCESS_OK, query.getStatus());
        assertEquals(GRAPHQL, query.getEntity().getMediaType());
        assertEquals(Map.of("hello", "Hello GET"), json(query).get("data"));

        Response mutation = get("/limited", Map.of("query", "mutation { increment(by: 1) }"));
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, mutation.getStatus());
        assertTrue(mutation.getAllowedMethods().contains(Method.POST));

        Response badVariables = get("/limited", Map.of("query", "{ hello }", "variables", "{bad"));
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, badVariables.getStatus());
    }

    @Test
    void postRequiresJson() {
        Request request = new Request(Method.POST, base + "/graphql");
        request.setEntity(new StringRepresentation("{ hello }", MediaType.TEXT_PLAIN));
        assertEquals(
                Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, client.handle(request).getStatus());
    }

    @Test
    void malformedParametersAreRequestErrors() {
        for (String body :
                new String[] {
                    "[{\"query\": \"{ hello }\"}]",
                    "\"query\"",
                    "{\"query\": \"{ hello }\", \"operationName\": 1}",
                    "{\"query\": \"{ hello }\", \"variables\": []}",
                    "{\"query\": \"{ hello }\", \"extensions\": \"x\"}"
                }) {
            assertEquals(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    post("/graphql", body, null).getStatus(),
                    body);
        }
    }

    @Test
    void documentErrorsDependOnTheResponseMediaType() throws Exception {
        for (String body :
                new String[] {
                    "{\"query\": \"{ hello \"}",
                    "{\"query\": \"{ unknownField }\"}",
                    "{\"query\": \"query A { hello } query B { hello }\"}",
                    "{\"query\": \"query A { hello }\", \"operationName\": \"B\"}",
                    "{\"query\": \"fragment F on Query { hello }\"}"
                }) {
            Response modern = post("/graphql", body, GRAPHQL);
            assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, modern.getStatus(), body);
            assertEquals(GRAPHQL, modern.getEntity().getMediaType());
            Map<String, Object> result = json(modern);
            assertTrue(result.containsKey("errors"), body);
            assertTrue(!result.containsKey("data"), body);

            Response legacy = post("/graphql", body, MediaType.APPLICATION_JSON);
            assertEquals(Status.SUCCESS_OK, legacy.getStatus(), body);
            assertTrue(json(legacy).containsKey("errors"), body);
        }
    }

    @Test
    void fieldErrorsAreOkOnceExecutionStarted() throws Exception {
        Response response = post("/graphql", "{\"query\": \"{ hello failing }\"}", GRAPHQL);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Map<String, Object> result = json(response);
        assertEquals("Hello world", GraphqlJson.asObject(result.get("data")).get("hello"));
        assertEquals(1, ((List<?>) result.get("errors")).size());
    }

    @Test
    void subscriptionsAreRejectedWithoutExecution() throws Exception {
        int created = TickPublisher.CREATED.get();
        Response response =
                post("/graphql", "{\"query\": \"subscription { ticks(count: 1) }\"}", GRAPHQL);
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
        assertEquals(created, TickPublisher.CREATED.get());
    }

    @Test
    void requestSizeIsLimitedBeforeParsing() throws Exception {
        String large =
                "{\"query\": \"{ hello }\", \"extensions\": {\"pad\": \"" + "x".repeat(64) + "\"}}";
        Response declared = post("/limited", large, null);
        assertEquals(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE, declared.getStatus());

        Request request = new Request(Method.POST, base + "/limited");
        Representation chunked =
                new InputRepresentation(
                        new ByteArrayInputStream(large.getBytes(StandardCharsets.UTF_8)),
                        MediaType.APPLICATION_JSON);
        request.setEntity(chunked);
        Response streamed = client.handle(request);
        // The server answers before reading the whole body and closes the connection: the client
        // gets the 413, or fails while still writing.
        assertTrue(
                Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE.equals(streamed.getStatus())
                        || streamed.getStatus().isConnectorError(),
                streamed.getStatus()::toString);
    }

    @Test
    void parserLimitsApplyPerAdapter() throws Exception {
        String query = "{" + " hello".repeat(30) + " }";
        Response limited = get("/limited", Map.of("query", query));
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, limited.getStatus());
        assertTrue(json(limited).get("errors").toString().contains("InvalidSyntax"));

        Response unlimited = post("/graphql", "{\"query\": \"" + query + "\"}", GRAPHQL);
        assertEquals(Status.SUCCESS_OK, unlimited.getStatus());
        assertEquals(
                ParserOptions.MAX_QUERY_TOKENS,
                ParserOptions.getDefaultOperationParserOptions().getMaxTokens(),
                "The process-wide defaults must not change");
    }

    @Test
    void requestContextReachesDataFetchers() throws Exception {
        Request request = new Request(Method.POST, base + "/limited");
        request.setEntity(
                new StringRepresentation(
                        "{\"query\": \"{ userAgent loaded(key: \\\"k\\\") }\"}",
                        MediaType.APPLICATION_JSON));
        request.getClientInfo().setAgent("graphql-test");
        Map<String, Object> data = GraphqlJson.asObject(json(client.handle(request)).get("data"));
        assertEquals("graphql-test", data.get("userAgent"));
        assertEquals("K", data.get("loaded"));
    }

    @Test
    void authenticatedUserReachesDataFetchers() throws Exception {
        String body = "{\"query\": \"{ whoami }\"}";
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, post("/secured", body, null).getStatus());

        Request request = new Request(Method.POST, base + "/secured");
        request.setEntity(new StringRepresentation(body, MediaType.APPLICATION_JSON));
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "alice", "secret"));
        assertEquals(Map.of("whoami", "alice"), json(client.handle(request)).get("data"));
        assertEquals(Map.of("whoami", "anonymous"), json(post("/graphql", body, null)).get("data"));
    }

    @Test
    void configurationIsFrozenOnceStarted() throws Exception {
        GraphqlServerAdapter adapter = adapter();
        assertThrows(IllegalArgumentException.class, () -> adapter.setMaxRequestBytes(0));
        assertNull(adapter.getParserOptions());
        adapter.start();
        assertEquals(
                ParserOptions.getDefaultOperationParserOptions().getMaxTokens(),
                adapter.getParserOptions().getMaxTokens());
        assertEquals(GraphqlServerAdapter.DEFAULT_MAX_REQUEST_BYTES, adapter.getMaxRequestBytes());
        assertThrows(IllegalStateException.class, () -> adapter.setGetEnabled(true));
        assertThrows(IllegalStateException.class, () -> adapter.setMaxRequestBytes(1));
        assertThrows(IllegalStateException.class, () -> adapter.setParserOptions(null));
        assertThrows(IllegalStateException.class, () -> adapter.setExecutionInputCustomizer(null));
        assertThrows(IllegalStateException.class, () -> adapter.setGraphQL(null));
        assertNull(adapter.getExecutionInputCustomizer());
        adapter.stop();
    }
}
