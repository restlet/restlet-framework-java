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
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public McpRequestContext(Request request) {
        this.request = request;
    }

    public Optional<Request> transportRequest() {
        return Optional.ofNullable(request);
    }

    public String protocolVersion() {
        return McpProtocol.VERSION;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }
}
