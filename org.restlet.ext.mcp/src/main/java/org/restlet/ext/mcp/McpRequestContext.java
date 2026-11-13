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

/** MCP-only transport metadata, also available in native SDK callbacks under CONTEXT_KEY. */
public final class McpRequestContext {
    private final Request request;
    private final String protocolVersion;
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
        this.request = request;
        this.protocolVersion = java.util.Objects.requireNonNull(protocolVersion, "protocolVersion");
    }

    public Optional<Request> transportRequest() {
        return Optional.ofNullable(request);
    }

    public String protocolVersion() {
        return protocolVersion;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }
}
