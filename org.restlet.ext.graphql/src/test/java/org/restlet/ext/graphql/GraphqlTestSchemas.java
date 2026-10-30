/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.dataloader.DataLoader;
import org.restlet.Request;
import org.restlet.data.ClientInfo;

/** Schemas shared by the tests. */
final class GraphqlTestSchemas {

    static final String SDL =
            """
            type Query {
              hello(name: String): String
              virtualThread: Boolean
              asyncHello: String
              failing: String
              whoami: String
              userAgent: String
              loaded(key: String!): String
              echo(input: EchoInput!): String
              header(name: String!): String
            }
            input EchoInput {
              text: String!
              tags: [String!]
            }
            type Mutation {
              increment(by: Int!): Int
            }
            type Subscription {
              ticks(count: Int!): Int
              idle: Int
            }
            """;

    private GraphqlTestSchemas() {}

    static GraphQLSchema schema(AtomicInteger counter) {
        DataFetcher<Object> hello = env -> "Hello " + env.getArgumentOrDefault("name", "world");
        DataFetcher<Object> failing =
                env -> {
                    throw new IllegalStateException("secret detail");
                };
        DataFetcher<Object> whoami =
                env -> {
                    ClientInfo clientInfo = env.getGraphQlContext().get(RestletGraphql.CLIENT_INFO);
                    return clientInfo.getUser() == null
                            ? "anonymous"
                            : clientInfo.getUser().getIdentifier();
                };
        DataFetcher<Object> userAgent =
                env -> {
                    Request request = env.getGraphQlContext().get(RestletGraphql.REQUEST);
                    return request.getClientInfo().getAgent();
                };
        DataFetcher<Object> loaded =
                env -> {
                    DataLoader<String, String> loader = env.getDataLoader("upper");
                    return loader.load(env.getArgument("key"));
                };
        DataFetcher<Object> echo =
                env -> {
                    Map<String, Object> input = env.getArgument("input");
                    List<?> tags = (List<?>) input.getOrDefault("tags", List.of());
                    return input.get("text") + "|" + tags.size();
                };
        DataFetcher<Object> header =
                env -> {
                    Request request = env.getGraphQlContext().get(RestletGraphql.REQUEST);
                    return request.getHeaders().getFirstValue(env.getArgument("name"), true);
                };
        DataFetcher<Object> ticks = env -> new TickPublisher(env.<Integer>getArgument("count"));

        RuntimeWiring wiring =
                RuntimeWiring.newRuntimeWiring()
                        .type(
                                "Query",
                                type ->
                                        type.dataFetcher("hello", hello)
                                                .dataFetcher(
                                                        "virtualThread",
                                                        env -> Thread.currentThread().isVirtual())
                                                .dataFetcher(
                                                        "asyncHello",
                                                        env ->
                                                                CompletableFuture.supplyAsync(
                                                                        () -> "async"))
                                                .dataFetcher("failing", failing)
                                                .dataFetcher("whoami", whoami)
                                                .dataFetcher("userAgent", userAgent)
                                                .dataFetcher("loaded", loaded)
                                                .dataFetcher("echo", echo)
                                                .dataFetcher("header", header))
                        .type(
                                "Mutation",
                                type ->
                                        type.dataFetcher(
                                                "increment",
                                                env -> counter.addAndGet(env.getArgument("by"))))
                        .type(
                                "Subscription",
                                type ->
                                        type.dataFetcher("ticks", ticks)
                                                .dataFetcher(
                                                        "idle", env -> new TickPublisher(0, true)))
                        .build();
        return new SchemaGenerator().makeExecutableSchema(new SchemaParser().parse(SDL), wiring);
    }

    static GraphQL graphQL(AtomicInteger counter) {
        return GraphQL.newGraphQL(schema(counter)).build();
    }
}
