/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import java.util.Objects;

/**
 * A resource served to MCP {@link McpProtocol#MODERN_VERSION} clients only, for {@link
 * McpServerAdapter#getModernResources()}: its definition, listed, and its read handler, which may
 * answer {@code input_required}. Experimental: the SDK's modern API is a preview.
 *
 * @param definition The resource, with its URI.
 * @param resource The read handler.
 */
public record McpModernResource(McpSchema.Resource definition, McpAsyncResource resource) {

    /**
     * Creates a modern resource.
     *
     * @param definition The resource, with its URI.
     * @param resource The read handler.
     */
    public McpModernResource {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(resource, "resource");
    }
}
