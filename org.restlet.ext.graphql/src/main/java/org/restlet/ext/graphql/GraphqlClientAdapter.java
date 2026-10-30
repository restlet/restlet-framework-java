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
import graphql.GraphQLError;
import graphql.ParseAndValidate;
import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import graphql.parser.ParserEnvironment;
import graphql.parser.ParserOptions;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
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
 * graphql.setSchema(schema); // optional: validate before sending
 * graphql.start();
 * ExecutionResult result = graphql.execute(document, "GetIssue", variables);
 * </pre>
 *
 * <p>Operations are sent with {@code POST}, accepting {@code application/graphql-response+json} and
 * {@code application/json}. Variables are serialized as JSON values, never templated as text.
 *
 * <p>Errors reported by the server, including request errors sent with a {@code 4xx} status and a
 * GraphQL body, are returned in the {@link ExecutionResult}: without data when execution didn't
 * start. Failures to obtain a GraphQL response throw a {@link GraphqlTransportException}:
 * connection failures and timeouts, responses larger than {@link #getMaxResponseBytes()}, and
 * responses that are not GraphQL, such as an HTML error page or an empty {@code 401}.
 *
 * <p>The adapter never starts or stops the borrowed client, and never fetches a schema. Its
 * configuration can't change once started.
 */
public class GraphqlClientAdapter {

    /** The default maximum size of a response body: 10 MiB. */
    public static final long DEFAULT_MAX_RESPONSE_BYTES = 10L << 20;

    private static final int EXCERPT_BYTES = 1024;

    private static final MediaType INCREMENTAL =
            new MediaType(MediaType.MULTIPART_MIXED, "deferSpec", "20220824");

    private enum State {
        NEW,
        STARTED,
        STOPPED
    }

    private final Context context;

    private volatile Client client;

    private volatile String endpoint;

    private volatile GraphQLSchema schema;

    private volatile ParserOptions parserOptions;

    private volatile long maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;

    private volatile Consumer<Request> requestCustomizer;

    private volatile State state = State.NEW;

    private volatile boolean incrementalDeliveryEnabled;

    private final Set<GraphqlSsePublisher.EventStreamSubscription> subscriptions =
            ConcurrentHashMap.newKeySet();

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
     * Sets the borrowed client. The adapter never starts or stops it. Its authentication, proxy and
     * timeout settings apply to every operation.
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
     * Returns the schema used to validate documents before sending them.
     *
     * @return The schema, or null.
     */
    public GraphQLSchema getSchema() {
        return schema;
    }

    /**
     * Sets a schema used to validate documents before sending them. Invalid documents are returned
     * as an {@link ExecutionResult} with errors, without a network call. The schema can be built
     * without data fetchers, for example from SDL with {@code UnExecutableSchemaGenerator}. The
     * adapter never fetches a schema by itself.
     *
     * @param schema The schema, or null to send documents unchecked.
     */
    public void setSchema(GraphQLSchema schema) {
        checkNew();
        this.schema = schema;
    }

    /**
     * Returns the parser options used to validate documents locally.
     *
     * @return The parser options, or null for graphql-java's default operation parser options.
     */
    public ParserOptions getParserOptions() {
        return parserOptions;
    }

    /**
     * Sets the parser options used to validate documents locally, when a schema is set.
     *
     * @param parserOptions The parser options, or null for graphql-java's default operation parser
     *     options, read when the adapter starts.
     */
    public void setParserOptions(ParserOptions parserOptions) {
        checkNew();
        this.parserOptions = parserOptions;
    }

    /**
     * Returns the maximum size of a response body.
     *
     * @return The maximum size of a response body, in bytes.
     */
    public long getMaxResponseBytes() {
        return maxResponseBytes;
    }

    /**
     * Sets the maximum size of a response body. Larger responses throw a {@link
     * GraphqlTransportException}; they are never truncated. Defaults to {@link
     * #DEFAULT_MAX_RESPONSE_BYTES}.
     *
     * @param maxResponseBytes The maximum size of a response body, in bytes.
     */
    public void setMaxResponseBytes(long maxResponseBytes) {
        checkNew();
        if (maxResponseBytes <= 0) {
            throw new IllegalArgumentException("The maximum response size must be positive");
        }
        this.maxResponseBytes = maxResponseBytes;
    }

    /**
     * Returns the request customizer.
     *
     * @return The request customizer, or null.
     */
    public Consumer<Request> getRequestCustomizer() {
        return requestCustomizer;
    }

    /**
     * Sets a customizer called on each HTTP request before it is sent, for example to add an API
     * key header or a {@code ChallengeResponse}.
     *
     * @param requestCustomizer The request customizer, or null.
     */
    public void setRequestCustomizer(Consumer<Request> requestCustomizer) {
        checkNew();
        this.requestCustomizer = requestCustomizer;
    }

    /**
     * Indicates if incremental delivery is accepted.
     *
     * @return True if incremental delivery is accepted.
     */
    public boolean isIncrementalDeliveryEnabled() {
        return incrementalDeliveryEnabled;
    }

    /**
     * Indicates if incremental delivery ({@code @defer}) is accepted. Experimental and off by
     * default: the GraphQL incremental delivery RFC is not finalized.
     *
     * <p>When enabled, {@code execute} accepts {@code multipart/mixed} responses in graphql-java's
     * format ({@code deferSpec=20220824}). Such a response is returned as an {@link
     * graphql.incremental.IncrementalExecutionResult} with the initial result. When it has more
     * results, its item publisher reads them from the response, which stays open until the
     * publisher completes, fails or is cancelled: subscribe to it, or cancel its subscription. Each
     * part is bounded by {@link #getMaxResponseBytes()}.
     *
     * @param incrementalDeliveryEnabled True to accept incremental delivery.
     */
    public void setIncrementalDeliveryEnabled(boolean incrementalDeliveryEnabled) {
        checkNew();
        this.incrementalDeliveryEnabled = incrementalDeliveryEnabled;
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
        if (parserOptions == null) {
            parserOptions = ParserOptions.getDefaultOperationParserOptions();
        }
        state = State.STARTED;
    }

    /**
     * Stops the adapter. Open subscriptions are cancelled, and their subscribers receive a {@link
     * GraphqlTransportException}. The borrowed client is left running.
     */
    public synchronized void stop() {
        state = State.STOPPED;
        for (GraphqlSsePublisher.EventStreamSubscription subscription :
                List.copyOf(subscriptions)) {
            subscription.stop();
        }
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
        Objects.requireNonNull(document, "document");
        checkStarted();
        ExecutionResult invalid = validate(document);
        return invalid != null
                ? invalid
                : send(AstPrinter.printAst(document), operationName, variables);
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
        checkStarted();
        if (schema != null) {
            ExecutionResult invalid;
            try {
                invalid = validate(parse(document));
            } catch (InvalidSyntaxException e) {
                invalid = failure(List.of(e.toInvalidSyntaxError()));
            }
            if (invalid != null) {
                return invalid;
            }
        }
        return send(document, operationName, variables);
    }

    /**
     * Subscribes to a parsed operation, usually a subscription, with {@code graphql-sse} in
     * distinct connections mode.
     *
     * @param document The document, printed before sending.
     * @param operationName The name of the operation to execute, or null.
     * @param variables The variables, or null.
     * @return A cold publisher: each subscriber sends its own request on its first demand.
     * @see #subscribe(String, String, Map)
     */
    public Publisher<ExecutionResult> subscribe(
            Document document, String operationName, Map<String, Object> variables) {
        Objects.requireNonNull(document, "document");
        checkStarted();
        ExecutionResult invalid = validate(document);
        return invalid != null
                ? single(invalid)
                : new GraphqlSsePublisher(
                        this, AstPrinter.printAst(document), operationName, variables);
    }

    /**
     * Subscribes to an operation, usually a subscription, with {@code graphql-sse} in distinct
     * connections mode. The request is sent with {@code POST}, accepting {@code text/event-stream},
     * when the first result is requested; each {@code next} event becomes a result, honoring
     * demand, and the {@code complete} event completes the publisher.
     *
     * <p>Request errors are published as a single result with errors. Transport failures, events
     * larger than {@link #getMaxResponseBytes()} and streams that end without a {@code complete}
     * event are signalled with a {@link GraphqlTransportException}. Cancelling the subscription
     * aborts the request.
     *
     * @param document The document.
     * @param operationName The name of the operation to execute, or null.
     * @param variables The variables, or null.
     * @return A cold publisher: each subscriber sends its own request on its first demand.
     */
    public Publisher<ExecutionResult> subscribe(
            String document, String operationName, Map<String, Object> variables) {
        Objects.requireNonNull(document, "document");
        checkStarted();
        if (schema != null) {
            ExecutionResult invalid;
            try {
                invalid = validate(parse(document));
            } catch (InvalidSyntaxException e) {
                invalid = failure(List.of(e.toInvalidSyntaxError()));
            }
            if (invalid != null) {
                return single(invalid);
            }
        }
        return new GraphqlSsePublisher(this, document, operationName, variables);
    }

    /** A publisher of one result, for documents that fail local validation. */
    private static Publisher<ExecutionResult> single(ExecutionResult result) {
        return subscriber ->
                subscriber.onSubscribe(
                        new Subscription() {
                            private boolean done;

                            @Override
                            public void request(long n) {
                                synchronized (this) {
                                    if (done) {
                                        return;
                                    }
                                    done = true;
                                }
                                if (n <= 0) {
                                    subscriber.onError(
                                            new IllegalArgumentException(
                                                    "The requested number of results must be"
                                                            + " positive"));
                                } else {
                                    subscriber.onNext(result);
                                    subscriber.onComplete();
                                }
                            }

                            @Override
                            public synchronized void cancel() {
                                done = true;
                            }
                        });
    }

    /** Registers an open subscription. Returns false if the adapter is not started. */
    synchronized boolean register(GraphqlSsePublisher.EventStreamSubscription subscription) {
        if (state != State.STARTED) {
            return false;
        }
        subscriptions.add(subscription);
        return true;
    }

    void unregister(GraphqlSsePublisher.EventStreamSubscription subscription) {
        subscriptions.remove(subscription);
    }

    /**
     * Fills the HTTP request of an operation sent for an event stream.
     *
     * @param request The new request to fill.
     * @param document The document.
     * @param operationName The name of the operation to execute, or null.
     * @param variables The variables, or null.
     * @return The request, customized.
     */
    Request createEventStreamRequest(
            Request request, String document, String operationName, Map<String, Object> variables) {
        createRequest(request, document, operationName, variables);
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_EVENT_STREAM, 1F));
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.APPLICATION_JSON, 0.5F));
        return request;
    }

    private Document parse(String document) {
        return Parser.parse(
                ParserEnvironment.newParserEnvironment()
                        .document(document)
                        .parserOptions(parserOptions)
                        .build());
    }

    /** Returns a result with the validation errors, or null if the document is valid. */
    private ExecutionResult validate(Document document) {
        GraphQLSchema validationSchema = schema;
        if (validationSchema == null) {
            return null;
        }
        List<ValidationError> errors = ParseAndValidate.validate(validationSchema, document);
        return errors.isEmpty() ? null : failure(new ArrayList<GraphQLError>(errors));
    }

    private static ExecutionResult failure(List<GraphQLError> errors) {
        return ExecutionResult.newExecutionResult().errors(errors).build();
    }

    /**
     * Creates the HTTP request of an operation.
     *
     * @param document The document.
     * @param operationName The name of the operation to execute, or null.
     * @param variables The variables, or null.
     * @return The request, customized.
     */
    Request createRequest(String document, String operationName, Map<String, Object> variables) {
        return createRequest(new Request(), document, operationName, variables);
    }

    private Request createRequest(
            Request request, String document, String operationName, Map<String, Object> variables) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", document);
        if (operationName != null) {
            body.put("operationName", operationName);
        }
        if (variables != null && !variables.isEmpty()) {
            body.put("variables", variables);
        }

        request.setMethod(Method.POST);
        request.setResourceRef(endpoint);
        request.setEntity(
                new StringRepresentation(
                        GraphqlJson.writeString(body),
                        MediaType.APPLICATION_JSON,
                        null,
                        CharacterSet.UTF_8));
        Consumer<Request> customizer = requestCustomizer;
        if (customizer != null) {
            customizer.accept(request);
        }
        return request;
    }

    private ExecutionResult send(
            String document, String operationName, Map<String, Object> variables)
            throws GraphqlTransportException {
        Request request = createRequest(document, operationName, variables);
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON, 1F));
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.APPLICATION_JSON, 0.9F));
        if (incrementalDeliveryEnabled) {
            request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(INCREMENTAL, 1F));
        }

        Response response = client.handle(request);
        Representation multipart = response.getEntity();
        if (incrementalDeliveryEnabled
                && response.getStatus().isSuccess()
                && multipart != null
                && multipart.getMediaType() != null
                && MediaType.MULTIPART_MIXED.equals(multipart.getMediaType(), true)) {
            // Releases the response itself, once the last result is read.
            return GraphqlIncrementalPublisher.read(response, maxResponseBytes);
        }
        try {
            return toResult(response);
        } finally {
            Representation entity = response.getEntity();
            if (entity != null) {
                entity.release();
            }
        }
    }

    /**
     * Converts a response with a single GraphQL result.
     *
     * @param response The response.
     * @return The result.
     * @throws GraphqlTransportException If the response is not a GraphQL result.
     */
    ExecutionResult toResult(Response response) throws GraphqlTransportException {
        Status status = response.getStatus();
        if (status.isConnectorError()) {
            throw new GraphqlTransportException(
                    "No response from the GraphQL endpoint: " + status,
                    null,
                    null,
                    status.getThrowable());
        }

        Representation entity = response.getEntity();
        long limit = maxResponseBytes;
        if (entity != null && entity.getSize() > limit) {
            throw tooLarge(status, limit, null);
        }
        byte[] bytes;
        try (InputStream in = stream(entity)) {
            bytes = GraphqlJson.readBytes(in, limit);
        } catch (GraphqlJson.TooLargeException e) {
            throw tooLarge(status, limit, e);
        } catch (IOException e) {
            throw new GraphqlTransportException(
                    "Unable to read the GraphQL response", status, null, e);
        }

        if (isJson(entity) && (status.isSuccess() || status.isClientError())) {
            Map<String, Object> result = null;
            try {
                result = GraphqlJson.asObject(GraphqlJson.parse(bytes));
            } catch (GraphqlJson.InvalidJsonException _) {
                // Reported below as a non-GraphQL response
            }
            if (result != null && (result.containsKey("data") || result.containsKey("errors"))) {
                try {
                    ExecutionResult executionResult = ExecutionResult.fromSpecification(result);
                    if (result.containsKey("data") && result.get("data") == null) {
                        // fromSpecification() drops an explicit null data entry
                        executionResult = executionResult.transform(builder -> builder.data(null));
                    }
                    return executionResult;
                } catch (RuntimeException e) {
                    throw new GraphqlTransportException(
                            "Not a GraphQL response: " + status, status, excerpt(bytes), e);
                }
            }
        }

        throw new GraphqlTransportException(
                "Not a GraphQL response: " + status, status, excerpt(bytes), null);
    }

    private static InputStream stream(Representation entity) throws IOException {
        InputStream in = entity == null ? null : entity.getStream();
        return in == null ? InputStream.nullInputStream() : in;
    }

    private static boolean isJson(Representation entity) {
        MediaType type = entity == null ? null : entity.getMediaType();
        return type != null
                && (type.equals(GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON, true)
                        || type.equals(MediaType.APPLICATION_JSON, true));
    }

    private static GraphqlTransportException tooLarge(Status status, long limit, Throwable cause) {
        return new GraphqlTransportException(
                "The GraphQL response exceeds " + limit + " bytes", status, null, cause);
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

    private void checkStarted() {
        if (state != State.STARTED) {
            throw new IllegalStateException("The adapter is not started");
        }
    }
}
