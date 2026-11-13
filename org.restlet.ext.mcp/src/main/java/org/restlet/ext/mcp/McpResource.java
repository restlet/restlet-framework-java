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
 * A resource of an {@link McpServerAdapter}, for {@link McpServerAdapter#getResources()}: its
 * definition, listed, and its read handler, which may answer {@code input_required}. Legacy clients
 * read it too, without input. Experimental: the SDK's modern API is a preview.
 *
 * @param definition The resource, with its URI.
 * @param resource The read handler.
 */
public record McpResource(McpSchema.Resource definition, McpAsyncResource resource) {

    /**
     * Creates a resource.
     *
     * @param definition The resource, with its URI.
     * @param resource The read handler.
     */
    public McpResource {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(resource, "resource");
    }
}
