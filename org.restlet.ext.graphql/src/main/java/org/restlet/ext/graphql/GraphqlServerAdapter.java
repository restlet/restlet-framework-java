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
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import graphql.parser.ParserEnvironment;
import graphql.parser.ParserOptions;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Dimension;
import org.restlet.data.Form;
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
 * <p>Binding rules:
 *
 * <ul>
 *   <li>{@code POST} with an {@code application/json} body accepts all operations; other request
 *       media types get {@code 415}. A JSON array (a batch) is rejected, never executed.
 *   <li>{@code GET} accepts queries only, and only when {@link #setGetEnabled(boolean)} is set.
 *       Other operations over {@code GET} get {@code 405} with {@code Allow: POST}.
 *   <li>{@code application/graphql-response+json} is returned when the client names it, otherwise
 *       {@code application/json}; {@code 406} if neither is acceptable.
 *   <li>Malformed requests get {@code 400}. Documents that fail to parse or validate, or that don't
 *       select an operation, get {@code 400} with {@code application/graphql-response+json} and
 *       {@code 200} with {@code application/json}. Once execution starts, the status is {@code
 *       200}, even with field errors.
 * </ul>
 *
 * <p>Operations run on the thread of the call, a virtual thread by default. Asynchronous data
 * fetchers are joined on that thread. The {@code graphql.GraphQLContext} of each operation holds
 * the Restlet request under {@link RestletGraphql#REQUEST} and its client info under {@link
 * RestletGraphql#CLIENT_INFO}. Policies such as query depth and complexity limits, introspection
 * and the rendering of data fetcher exceptions are configured on the {@code GraphQL} instance with
 * graphql-java's own instrumentation and handlers.
 *
 * <p>The configuration can't change once the adapter is started, which happens at the latest when
 * it handles its first request.
 */
public class GraphqlServerAdapter extends Restlet {

    /** The default maximum size of a request body: 1 MiB. */
    public static final long DEFAULT_MAX_REQUEST_BYTES = 1L << 20;

    private volatile GraphQL graphQL;

    private volatile long maxRequestBytes = DEFAULT_MAX_REQUEST_BYTES;

    private volatile boolean getEnabled;

    private volatile ParserOptions parserOptions;

    private volatile BiConsumer<Request, ExecutionInput.Builder> executionInputCustomizer;

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

    /**
     * Returns the maximum size of a request body, checked before parsing.
     *
     * @return The maximum size of a request body, in bytes.
     */
    public long getMaxRequestBytes() {
        return maxRequestBytes;
    }

    /**
     * Sets the maximum size of a request body, checked before parsing. Larger requests get {@code
     * 413}. Defaults to {@link #DEFAULT_MAX_REQUEST_BYTES}.
     *
     * @param maxRequestBytes The maximum size of a request body, in bytes.
     */
    public void setMaxRequestBytes(long maxRequestBytes) {
        checkNotStarted();
        if (maxRequestBytes <= 0) {
            throw new IllegalArgumentException("The maximum request size must be positive");
        }
        this.maxRequestBytes = maxRequestBytes;
    }

    /**
     * Indicates if queries are accepted over {@code GET}.
     *
     * @return True if queries are accepted over {@code GET}.
     */
    public boolean isGetEnabled() {
        return getEnabled;
    }

    /**
     * Indicates if queries are accepted over {@code GET}. Off by default: {@code GET} requests can
     * be sent cross-origin without a preflight.
     *
     * @param getEnabled True if queries are accepted over {@code GET}.
     */
    public void setGetEnabled(boolean getEnabled) {
        checkNotStarted();
        this.getEnabled = getEnabled;
    }

    /**
     * Returns the parser options applied to each operation.
     *
     * @return The parser options, or null for graphql-java's default operation parser options.
     */
    public ParserOptions getParserOptions() {
        return parserOptions;
    }

    /**
     * Sets the parser options applied to each operation, such as the maximum number of tokens,
     * characters and rule depth. They are passed through the {@code GraphQLContext} of each
     * operation, never through graphql-java's process-wide defaults.
     *
     * @param parserOptions The parser options, or null for graphql-java's default operation parser
     *     options, read when the adapter starts.
     */
    public void setParserOptions(ParserOptions parserOptions) {
        checkNotStarted();
        this.parserOptions = parserOptions;
    }

    /**
     * Returns the execution input customizer.
     *
     * @return The execution input customizer, or null.
     */
    public BiConsumer<Request, ExecutionInput.Builder> getExecutionInputCustomizer() {
        return executionInputCustomizer;
    }

    /**
     * Sets a customizer called for each operation before execution, for example to set a {@code
     * DataLoaderRegistry}, a locale or extra context entries.
     *
     * @param executionInputCustomizer The execution input customizer, or null.
     */
    public void setExecutionInputCustomizer(
            BiConsumer<Request, ExecutionInput.Builder> executionInputCustomizer) {
        checkNotStarted();
        this.executionInputCustomizer = executionInputCustomizer;
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            if (graphQL == null) {
                throw new IllegalStateException("No GraphQL instance set");
            }
            if (parserOptions == null) {
                parserOptions = ParserOptions.getDefaultOperationParserOptions();
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

        Method method = request.getMethod();
        boolean get = Method.GET.equals(method);
        if (!Method.POST.equals(method) && !(get && getEnabled)) {
            methodNotAllowed(response, getEnabled);
            return;
        }

        // The response media type is negotiated, which caches must know of.
        response.getDimensions().add(Dimension.MEDIA_TYPE);
        MediaType responseType =
                GraphqlMediaTypes.negotiate(request.getClientInfo().getAcceptedMediaTypes());
        if (responseType == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
            return;
        }

        Map<String, Object> params;
        try {
            params = get ? decodeQuery(request) : decodeBody(request, response, responseType);
        } catch (RequestException e) {
            requestError(response, responseType, e.getMessage());
            return;
        }
        if (params == null) {
            return;
        }

        ExecutionInput.Builder input;
        try {
            String query = string(params, "query", true);
            String operationName = string(params, "operationName", false);
            input = ExecutionInput.newExecutionInput().query(query).operationName(operationName);
            Map<String, Object> variables = object(params, "variables");
            if (variables != null) {
                input.variables(variables);
            }
            Map<String, Object> extensions = object(params, "extensions");
            if (extensions != null) {
                input.extensions(extensions);
            }
        } catch (RequestException e) {
            requestError(response, responseType, e.getMessage());
            return;
        }

        // The operation is selected once customized, as the customizer can change it.
        ExecutionInput executionInput = customize(request, input).build();
        OperationDefinition.Operation operation;
        try {
            operation =
                    selectOperation(executionInput.getQuery(), executionInput.getOperationName());
        } catch (InvalidSyntaxException e) {
            write(response, responseType, failure(e.toInvalidSyntaxError()));
            return;
        } catch (RequestException e) {
            write(response, responseType, failure(error(e.getMessage())));
            return;
        }

        if (get && operation != OperationDefinition.Operation.QUERY) {
            methodNotAllowed(response, false);
            return;
        }
        if (operation == OperationDefinition.Operation.SUBSCRIPTION) {
            write(
                    response,
                    responseType,
                    failure(error("Subscriptions are not supported by this endpoint")));
            return;
        }

        write(response, responseType, graphQL.execute(executionInput));
    }

    private ExecutionInput.Builder customize(Request request, ExecutionInput.Builder input) {
        BiConsumer<Request, ExecutionInput.Builder> customizer = executionInputCustomizer;
        if (customizer != null) {
            customizer.accept(request, input);
        }
        // graphql-java reads per-execution parser options from the context under their class.
        return input.graphQLContext(
                builder ->
                        builder.put(ParserOptions.class, parserOptions)
                                .put(RestletGraphql.REQUEST, request)
                                .put(RestletGraphql.CLIENT_INFO, request.getClientInfo()));
    }

    /**
     * Parses the document with the configured limits and returns the type of the operation to
     * execute.
     */
    private OperationDefinition.Operation selectOperation(String query, String operationName)
            throws RequestException {
        Document document =
                Parser.parse(
                        ParserEnvironment.newParserEnvironment()
                                .document(query)
                                .parserOptions(parserOptions)
                                .build());
        List<OperationDefinition> operations =
                document.getDefinitionsOfType(OperationDefinition.class);
        // An empty operation name, such as an empty GET parameter, is omitted.
        if (operationName == null || operationName.isEmpty()) {
            if (operations.size() != 1) {
                throw new RequestException(
                        operations.isEmpty()
                                ? "The document has no operation"
                                : "An operationName is required when the document has several"
                                        + " operations");
            }
            return operations.getFirst().getOperation();
        }
        for (OperationDefinition operation : operations) {
            if (operationName.equals(operation.getName())) {
                return operation.getOperation();
            }
        }
        throw new RequestException("Unknown operation named '" + operationName + "'");
    }

    private static Map<String, Object> decodeQuery(Request request) throws RequestException {
        Form form = request.getResourceRef().getQueryAsForm();
        Map<String, Object> params = new HashMap<>();
        params.put("query", form.getFirstValue("query"));
        params.put("operationName", form.getFirstValue("operationName"));
        params.put("variables", parseParameter(form, "variables"));
        params.put("extensions", parseParameter(form, "extensions"));
        return params;
    }

    private static Object parseParameter(Form form, String name) throws RequestException {
        String value = form.getFirstValue(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return GraphqlJson.parse(value);
        } catch (GraphqlJson.InvalidJsonException e) {
            throw new RequestException("Invalid JSON in '" + name + "': " + e.getMessage());
        }
    }

    private Map<String, Object> decodeBody(
            Request request, Response response, MediaType responseType) throws RequestException {
        Representation entity = request.getEntity();
        if (entity == null || !entity.isAvailable()) {
            throw new RequestException("The request has no body");
        }
        if (entity.getMediaType() == null
                || !MediaType.APPLICATION_JSON.equals(entity.getMediaType(), true)) {
            response.setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            return null;
        }
        if (entity.getSize() > maxRequestBytes) {
            tooLarge(response, responseType);
            return null;
        }

        Object body;
        try (InputStream in = entity.getStream()) {
            body = GraphqlJson.read(in, maxRequestBytes);
        } catch (GraphqlJson.TooLargeException e) {
            tooLarge(response, responseType);
            return null;
        } catch (GraphqlJson.InvalidJsonException e) {
            throw new RequestException("Invalid JSON body: " + e.getMessage());
        } catch (IOException e) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            return null;
        }

        if (body instanceof List<?>) {
            throw new RequestException("Batched requests are not supported");
        }
        Map<String, Object> params = GraphqlJson.asObject(body);
        if (params == null) {
            throw new RequestException("The request body must be a JSON object");
        }
        return params;
    }

    private static String string(Map<String, Object> params, String name, boolean required)
            throws RequestException {
        Object value = params.get(name);
        if (value instanceof String string) {
            return string;
        }
        if (value == null && !required) {
            return null;
        }
        throw new RequestException(
                "The request must have a string '" + name + "'" + (required ? "" : " or none"));
    }

    private static Map<String, Object> object(Map<String, Object> params, String name)
            throws RequestException {
        Object value = params.get(name);
        Map<String, Object> object = GraphqlJson.asObject(value);
        if (value != null && object == null) {
            throw new RequestException("'" + name + "' must be a JSON object");
        }
        return object;
    }

    private static GraphQLError error(String message) {
        return GraphqlErrorBuilder.newError().message("%s", message).build();
    }

    private static ExecutionResult failure(GraphQLError error) {
        return ExecutionResult.newExecutionResult().errors(List.of(error)).build();
    }

    private static void methodNotAllowed(Response response, boolean getAllowed) {
        response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        if (getAllowed) {
            response.getAllowedMethods().add(Method.GET);
        }
        response.getAllowedMethods().add(Method.POST);
    }

    private static void tooLarge(Response response, MediaType responseType) {
        writeEntity(response, responseType, failure(error("The request body is too large")));
        response.setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
    }

    private static void requestError(Response response, MediaType responseType, String message) {
        writeEntity(response, responseType, failure(error(message)));
        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    /**
     * Writes a result. A result without data means that execution didn't start: a request error,
     * reported with {@code 400} in {@code application/graphql-response+json} only.
     */
    private static void write(Response response, MediaType responseType, ExecutionResult result) {
        writeEntity(response, responseType, result);
        boolean requestError =
                !result.isDataPresent()
                        && GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON.equals(responseType);
        response.setStatus(requestError ? Status.CLIENT_ERROR_BAD_REQUEST : Status.SUCCESS_OK);
    }

    private static void writeEntity(
            Response response, MediaType responseType, ExecutionResult result) {
        response.setEntity(
                new ByteArrayRepresentation(
                        GraphqlJson.write(result.toSpecification()), responseType));
    }

    private void checkNotStarted() {
        if (isStarted()) {
            throw new IllegalStateException("The configuration can't change once started");
        }
    }

    /** A malformed request. */
    private static final class RequestException extends Exception {

        private static final long serialVersionUID = 1L;

        RequestException(String message) {
            super(message, null, false, false);
        }
    }
}
