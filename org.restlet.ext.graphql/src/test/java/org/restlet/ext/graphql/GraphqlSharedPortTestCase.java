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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionResult;
import graphql.parser.Parser;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/** GraphQL next to a REST resource on one HTTP/2 server connector. */
class GraphqlSharedPortTestCase {

    private final AtomicInteger counter = new AtomicInteger();

    private Component component;

    private Server server;

    private Client client;

    private GraphqlClientAdapter graphql;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        server = component.getServers().add(Protocol.HTTP, 0);
        server.getContext().getParameters().add("http.transport.protocol", "HTTP2");
        Router router = component.getDefaultHost();
        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        adapter.setGraphQL(GraphqlTestSchemas.graphQL(counter));
        router.attach("/graphql", adapter);
        router.attach(
                "/hello",
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity("REST", MediaType.TEXT_PLAIN);
                    }
                });

        component.start();
        base = "http://localhost:" + server.getActualPort();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        graphql = new GraphqlClientAdapter();
        graphql.setClient(client);
        graphql.setEndpoint(base + "/graphql");
        graphql.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        graphql.stop();
        client.stop();
        component.stop();
    }

    @Test
    void queryAndMutationShareThePortWithRest() throws Exception {
        Response rest = client.handle(new Request(Method.GET, base + "/hello"));
        assertEquals("2.0", rest.getProtocol().getVersion());
        assertEquals("REST", rest.getEntity().getText());

        ExecutionResult query =
                graphql.execute("query Q($n: String) { hello(name: $n) }", "Q", Map.of("n", "G0"));
        assertTrue(query.getErrors().isEmpty(), query.getErrors()::toString);
        assertEquals(Map.of("hello", "Hello G0"), query.getData());

        ExecutionResult mutation =
                graphql.execute(Parser.parse("mutation { increment(by: 2) }"), null, Map.of());
        assertEquals(Map.of("increment", 2), mutation.getData());
        assertEquals(2, counter.get());
    }

    @Test
    void dataFetchersRunOnTheCallVirtualThread() throws Exception {
        ExecutionResult result = graphql.execute("{ virtualThread asyncHello }", null, null);
        assertEquals(Map.of("virtualThread", true, "asyncHello", "async"), result.getData());
    }

    @Test
    void fieldErrorsKeepPartialDataAndNativeErrors() throws Exception {
        ExecutionResult result = graphql.execute("{ hello failing }", null, null);
        Map<String, Object> data = result.getData();
        assertEquals("Hello world", data.get("hello"));
        assertNull(data.get("failing"));
        assertEquals(1, result.getErrors().size());
        // graphql-java's default exception handler includes the exception message: hiding it is
        // the application's choice, with its own DataFetcherExceptionHandler.
        assertEquals(
                "Exception while fetching data (/failing) : secret detail",
                result.getErrors().get(0).getMessage());
        assertEquals(List.of("failing"), result.getErrors().get(0).getPath());
    }

    @Test
    void transportFailureThrows() throws Exception {
        server.stop();
        GraphqlTransportException e =
                assertThrows(
                        GraphqlTransportException.class,
                        () -> graphql.execute("{ hello }", null, null));
        assertNull(e.getStatus());
    }

    @Test
    void lifecycleIsEnforced() {
        GraphqlClientAdapter other = new GraphqlClientAdapter(new Context());
        assertThrows(IllegalStateException.class, other::start);
        other.setClient(client);
        assertThrows(IllegalStateException.class, other::start);
        assertThrows(IllegalStateException.class, () -> other.execute("{ hello }", null, null));
        other.setEndpoint(base + "/graphql");
        other.start();
        assertThrows(IllegalStateException.class, () -> other.setEndpoint("x"));
        assertThrows(IllegalStateException.class, () -> other.setClient(null));
        other.stop();
        assertFalse(other.isStarted());
        assertThrows(IllegalStateException.class, () -> other.execute("{ hello }", null, null));
        assertTrue(client.isStarted(), "The borrowed client must survive stop()");
    }
}
