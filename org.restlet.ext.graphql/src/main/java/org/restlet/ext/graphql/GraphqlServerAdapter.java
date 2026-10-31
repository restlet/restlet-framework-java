/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.ext.graphql.internal.GraphqlMediaTypes;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;

/**
 * Serves a native {@link GraphQL} instance over HTTP, following the GraphQL over HTTP draft.
 *
 * <p>Attach it to a router next to other resources:
 *
 * <pre>
 * GraphqlServerAdapter graphql = new GraphqlServerAdapter(getContext());
 * graphql.setGraphQL(GraphQL.newGraphQL(schema).build());
 * router.attach("/graphql", graphql);
 * </pre>
 *
 * <p>Operations run on the thread of the call, a virtual thread by default. Asynchronous data
 * fetchers are joined on that thread. The configuration can't change once the adapter is started,
 * which happens at the latest when it handles its first request.
 */
public class GraphqlServerAdapter extends Restlet {

    private volatile GraphQL graphQL;

    /** Constructor. */
    public GraphqlServerAdapter() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param context The context.
     */
    public GraphqlServerAdapter(Context context) {
        super(context);
    }

    /**
     * Returns the GraphQL instance that executes operations.
     *
     * @return The GraphQL instance.
     */
    public GraphQL getGraphQL() {
        return graphQL;
    }

    /**
     * Sets the GraphQL instance that executes operations.
     *
     * @param graphQL The GraphQL instance.
     */
    public void setGraphQL(GraphQL graphQL) {
        checkNotStarted();
        this.graphQL = graphQL;
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            if (graphQL == null) {
                throw new IllegalStateException("No GraphQL instance set");
            }
            super.start();
        }
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        if (!isStarted()) {
            return;
        }

        if (!Method.POST.equals(request.getMethod())) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.POST);
            return;
        }

        MediaType responseType =
                GraphqlMediaTypes.negotiate(request.getClientInfo().getAcceptedMediaTypes());
        if (responseType == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
            return;
        }

        Representation entity = request.getEntity();
        if (entity == null || !entity.isAvailable()) {
            requestError(response, responseType, "The request has no body");
            return;
        }

        Object body;
        try (InputStream in = entity.getStream()) {
            body = GraphqlJson.read(in, Long.MAX_VALUE);
        } catch (GraphqlJson.InvalidJsonException e) {
            requestError(response, responseType, "Invalid JSON body: " + e.getMessage());
            return;
        } catch (IOException e) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            return;
        }

        Map<String, Object> params = GraphqlJson.asObject(body);
        if (params == null || !(params.get("query") instanceof String query)) {
            requestError(response, responseType, "The request must have a string 'query'");
            return;
        }

        ExecutionInput.Builder input = ExecutionInput.newExecutionInput().query(query);
        if (params.get("operationName") instanceof String operationName) {
            input.operationName(operationName);
        }
        Map<String, Object> variables = GraphqlJson.asObject(params.get("variables"));
        if (variables != null) {
            input.variables(variables);
        }
        Map<String, Object> extensions = GraphqlJson.asObject(params.get("extensions"));
        if (extensions != null) {
            input.extensions(extensions);
        }

        ExecutionResult result = graphQL.execute(input.build());
        write(response, responseType, result);
    }

    private static void requestError(Response response, MediaType responseType, String message) {
        GraphQLError error = GraphqlErrorBuilder.newError().message("%s", message).build();
        write(
                response,
                responseType,
                ExecutionResult.newExecutionResult().errors(List.of(error)).build());
        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    private static void write(Response response, MediaType responseType, ExecutionResult result) {
        response.setEntity(
                new ByteArrayRepresentation(
                        GraphqlJson.write(result.toSpecification()), responseType));
        response.setStatus(Status.SUCCESS_OK);
    }

    private void checkNotStarted() {
        if (isStarted()) {
            throw new IllegalStateException("The configuration can't change once started");
        }
    }
}
