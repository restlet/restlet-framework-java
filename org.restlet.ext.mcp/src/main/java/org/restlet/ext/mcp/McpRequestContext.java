/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.restlet.Request;
import org.restlet.Response;

/** MCP-only transport metadata, also available in native SDK callbacks under CONTEXT_KEY. */
public final class McpRequestContext {
    private final Request request;
    private final Response response;
    private final String protocolVersion;
    private final org.restlet.Application application = org.restlet.Application.getCurrent();
    private final org.restlet.Context context = org.restlet.Context.getCurrent();
    private final Integer virtualHost = org.restlet.routing.VirtualHost.getCurrent();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Creates the context of a {@link McpProtocol#MODERN_VERSION} request, the revision {@link
     * McpServerAdapter#dispatch} serves. The legacy edge passes its revision to the other
     * constructor.
     *
     * @param request The transport request, or null.
     */
    public McpRequestContext(Request request) {
        this(request, McpProtocol.MODERN_VERSION);
    }

    /**
     * Creates the context of a request using a given MCP revision.
     *
     * @param request The transport request, or null.
     * @param protocolVersion The MCP revision of the request, such as {@link
     *     McpProtocol#MODERN_VERSION}.
     */
    public McpRequestContext(Request request, String protocolVersion) {
        this(request, null, protocolVersion);
    }

    /**
     * Creates the context of a request using a given MCP revision, with its transport response.
     *
     * @param request The transport request, or null.
     * @param response The transport response, or null.
     * @param protocolVersion The MCP revision of the request, such as {@link
     *     McpProtocol#MODERN_VERSION}.
     */
    public McpRequestContext(Request request, Response response, String protocolVersion) {
        this.request = request;
        this.response = response;
        this.protocolVersion = java.util.Objects.requireNonNull(protocolVersion, "protocolVersion");
    }

    /**
     * Calls code with Restlet's thread-local variables as they were where this context was created,
     * the connector's call: the current response, context, application and virtual host, restoring
     * the caller's afterwards. Blocking handlers run this way.
     *
     * @param call The code.
     * @param <T> Its result.
     * @return Its result.
     * @throws Exception If the code fails.
     */
    public <T> T withRestletLocals(java.util.concurrent.Callable<T> call) throws Exception {
        Response previousResponse = Response.getCurrent();
        org.restlet.Context previousContext = org.restlet.Context.getCurrent();
        Integer previousHost = org.restlet.routing.VirtualHost.getCurrent();
        org.restlet.Application previousApplication = org.restlet.Application.getCurrent();
        Response.setCurrent(response);
        org.restlet.Context.setCurrent(context);
        org.restlet.routing.VirtualHost.setCurrent(virtualHost);
        org.restlet.Application.setCurrent(application);
        try {
            return call.call();
        } finally {
            Response.setCurrent(previousResponse);
            org.restlet.Context.setCurrent(previousContext);
            org.restlet.routing.VirtualHost.setCurrent(previousHost);
            org.restlet.Application.setCurrent(previousApplication);
        }
    }

    public Optional<Request> transportRequest() {
        return Optional.ofNullable(request);
    }

    /**
     * The transport response, Restlet's current response while a blocking handler runs.
     *
     * @return The response, or empty without transport, as for {@link McpServerAdapter#dispatch}.
     */
    public Optional<Response> transportResponse() {
        return Optional.ofNullable(response);
    }

    public String protocolVersion() {
        return protocolVersion;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    /**
     * The authenticated user of the request: the identifier of its {@code ClientInfo} user, else
     * the name of its first principal. It binds the sealed {@code requestState} of {@code
     * input_required} results, and owns the tasks a {@code tools/call} creates.
     *
     * @return The principal, or empty for an unauthenticated request or one without transport.
     */
    public Optional<String> principal() {
        return transportRequest()
                .map(Request::getClientInfo)
                .map(
                        info ->
                                info.getUser() != null
                                        ? info.getUser().getIdentifier()
                                        : info.getPrincipals().stream()
                                                .map(java.security.Principal::getName)
                                                .findFirst()
                                                .orElse(null));
    }
}
