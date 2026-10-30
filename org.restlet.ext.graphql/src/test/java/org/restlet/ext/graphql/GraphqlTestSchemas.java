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
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/** Schemas shared by the tests. */
final class GraphqlTestSchemas {

    static final String SDL =
            """
            type Query {
              hello(name: String): String
              virtualThread: Boolean
              asyncHello: String
              failing: String
            }
            type Mutation {
              increment(by: Int!): Int
            }
            """;

    private GraphqlTestSchemas() {}

    static GraphQLSchema schema(AtomicInteger counter) {
        RuntimeWiring wiring =
                RuntimeWiring.newRuntimeWiring()
                        .type(
                                "Query",
                                type ->
                                        type.dataFetcher(
                                                        "hello",
                                                        env ->
                                                                "Hello "
                                                                        + env.getArgumentOrDefault(
                                                                                "name", "world"))
                                                .dataFetcher(
                                                        "virtualThread",
                                                        env -> Thread.currentThread().isVirtual())
                                                .dataFetcher(
                                                        "asyncHello",
                                                        env ->
                                                                CompletableFuture.supplyAsync(
                                                                        () -> "async"))
                                                .dataFetcher(
                                                        "failing",
                                                        env -> {
                                                            throw new IllegalStateException(
                                                                    "secret detail");
                                                        }))
                        .type(
                                "Mutation",
                                type ->
                                        type.dataFetcher(
                                                "increment",
                                                env -> counter.addAndGet(env.getArgument("by"))))
                        .build();
        return new SchemaGenerator().makeExecutableSchema(new SchemaParser().parse(SDL), wiring);
    }

    static GraphQL graphQL(AtomicInteger counter) {
        return GraphQL.newGraphQL(schema(counter)).build();
    }
}
