/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import graphql.ExecutionResult;
import graphql.language.AstPrinter;
import graphql.language.Document;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.ext.graphql.internal.GraphqlMediaTypes;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Sends GraphQL operations over HTTP through a borrowed Restlet {@link Client}.
 *
 * <pre>
 * GraphqlClientAdapter graphql = new GraphqlClientAdapter(getContext());
 * graphql.setClient(restletClient);
 * graphql.setEndpoint("https://example.com/graphql");
 * graphql.start();
 * ExecutionResult result = graphql.execute(document, "GetIssue", variables);
 * </pre>
 *
 * <p>The adapter never starts or stops the borrowed client. Its configuration can't change once
 * started. Errors reported by the server are returned in the {@link ExecutionResult}; failures to
 * obtain a GraphQL response throw a {@link GraphqlTransportException}.
 */
public class GraphqlClientAdapter {

    private static final int EXCERPT_BYTES = 1024;

    private static final long MAX_RESPONSE_BYTES = 10L << 20;

    private enum State {
        NEW,
        STARTED,
        STOPPED
    }

    private final Context context;

    private volatile Client client;

    private volatile String endpoint;

    private volatile State state = State.NEW;

    /** Constructor. */
    public GraphqlClientAdapter() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param context The context, or null.
     */
    public GraphqlClientAdapter(Context context) {
        this.context = context;
    }

    /**
     * Returns the context.
     *
     * @return The context, or null.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the borrowed client.
     *
     * @return The borrowed client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Sets the borrowed client. The adapter never starts or stops it.
     *
     * @param client The borrowed client.
     */
    public void setClient(Client client) {
        checkNew();
        this.client = client;
    }

    /**
     * Returns the URI of the GraphQL endpoint.
     *
     * @return The URI of the GraphQL endpoint.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the URI of the GraphQL endpoint.
     *
     * @param endpoint The URI of the GraphQL endpoint.
     */
    public void setEndpoint(String endpoint) {
        checkNew();
        this.endpoint = endpoint;
    }

    /**
     * Validates and freezes the configuration.
     *
     * @throws IllegalStateException If the client or the endpoint is missing, or if the adapter was
     *     already started.
     */
    public synchronized void start() {
        checkNew();
        if (client == null) {
            throw new IllegalStateException("No client set");
        }
        if (endpoint == null) {
            throw new IllegalStateException("No endpoint set");
        }
        state = State.STARTED;
    }

    /** Stops the adapter. The borrowed client is left running. */
    public synchronized void stop() {
        state = State.STOPPED;
    }

    /**
     * Indicates if the adapter is started.
     *
     * @return True if the adapter is started.
     */
    public boolean isStarted() {
        return state == State.STARTED;
    }

    /**
     * Executes a parsed operation.
     *
     * @param document The document, printed before sending.
     * @param operationName The name of the operation to execute, or null.
     * @param variables The variables, or null.
     * @return The result.
     * @throws GraphqlTransportException If no GraphQL response could be obtained.
     */
    public ExecutionResult execute(
            Document document, String operationName, Map<String, Object> variables)
            throws GraphqlTransportException {
        return execute(AstPrinter.printAst(document), operationName, variables);
    }

    /**
     * Executes an operation.
     *
     * @param document The document.
     * @param operationName The name of the operation to execute, or null.
     * @param variables The variables, or null.
     * @return The result.
     * @throws GraphqlTransportException If no GraphQL response could be obtained.
     */
    public ExecutionResult execute(
            String document, String operationName, Map<String, Object> variables)
            throws GraphqlTransportException {
        Objects.requireNonNull(document, "document");
        if (state != State.STARTED) {
            throw new IllegalStateException("The adapter is not started");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", document);
        if (operationName != null) {
            body.put("operationName", operationName);
        }
        if (variables != null && !variables.isEmpty()) {
            body.put("variables", variables);
        }

        Request request = new Request(Method.POST, endpoint);
        request.setEntity(
                new StringRepresentation(
                        GraphqlJson.writeString(body),
                        MediaType.APPLICATION_JSON,
                        null,
                        CharacterSet.UTF_8));
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON, 1F));
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.APPLICATION_JSON, 0.9F));

        Response response = client.handle(request);
        try {
            return toResult(response);
        } finally {
            Representation entity = response.getEntity();
            if (entity != null) {
                entity.release();
            }
        }
    }

    private static ExecutionResult toResult(Response response) throws GraphqlTransportException {
        Status status = response.getStatus();
        if (status.isConnectorError()) {
            throw new GraphqlTransportException(
                    "No response from the GraphQL endpoint: " + status,
                    null,
                    null,
                    status.getThrowable());
        }

        Representation entity = response.getEntity();
        byte[] bytes;
        try (InputStream in = entity == null ? InputStream.nullInputStream() : entity.getStream()) {
            bytes = GraphqlJson.readBytes(in, MAX_RESPONSE_BYTES);
        } catch (GraphqlJson.TooLargeException e) {
            throw new GraphqlTransportException(
                    "The GraphQL response exceeds " + MAX_RESPONSE_BYTES + " bytes",
                    status,
                    null,
                    e);
        } catch (IOException e) {
            throw new GraphqlTransportException(
                    "Unable to read the GraphQL response", status, null, e);
        }

        boolean json =
                entity != null
                        && entity.getMediaType() != null
                        && (entity.getMediaType()
                                        .equals(GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON, true)
                                || entity.getMediaType().equals(MediaType.APPLICATION_JSON, true));
        if (json && (status.isSuccess() || status.isClientError())) {
            Map<String, Object> result = null;
            try {
                result = GraphqlJson.asObject(GraphqlJson.parse(bytes));
            } catch (GraphqlJson.InvalidJsonException _) {
                // Reported below as a non-GraphQL response
            }
            if (result != null && (result.containsKey("data") || result.containsKey("errors"))) {
                return ExecutionResult.fromSpecification(result);
            }
        }

        throw new GraphqlTransportException(
                "Not a GraphQL response: " + status, status, excerpt(bytes), null);
    }

    private static String excerpt(byte[] bytes) {
        if (bytes.length == 0) {
            return null;
        }
        return new String(bytes, 0, Math.min(bytes.length, EXCERPT_BYTES), StandardCharsets.UTF_8);
    }

    private void checkNew() {
        if (state != State.NEW) {
            throw new IllegalStateException("The configuration can't change once started");
        }
    }
}
