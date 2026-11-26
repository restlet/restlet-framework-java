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
import io.modelcontextprotocol.modern.server.feature.McpAsyncCompletionHandler;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import java.util.Objects;

/**
 * A resource template of an {@link McpServerAdapter}, for {@link
 * McpServerAdapter#getResourceTemplates()}: its definition, listed, and the read handler of the
 * URIs it matches, and optionally the handler completing its variables for {@code
 * completion/complete}. Experimental: the SDK's modern API is a preview.
 *
 * @param definition The template, with its URI template.
 * @param resource The read handler of the matching URIs.
 * @param completionHandler The completion handler of its variables, or null.
 */
public record McpResourceTemplate(
        McpSchema.ResourceTemplate definition,
        McpAsyncResource resource,
        McpAsyncCompletionHandler completionHandler) {

    /**
     * Creates a resource template.
     *
     * @param definition The template, with its URI template.
     * @param resource The read handler of the matching URIs.
     * @param completionHandler The completion handler of its variables, or null.
     */
    public McpResourceTemplate {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(resource, "resource");
    }

    /**
     * Creates a resource template without completions.
     *
     * @param definition The template, with its URI template.
     * @param resource The read handler of the matching URIs.
     */
    public McpResourceTemplate(McpSchema.ResourceTemplate definition, McpAsyncResource resource) {
        this(definition, resource, null);
    }

    /**
     * Returns this template completing its variables with a handler.
     *
     * @param value The completion handler.
     * @return A copy of this template.
     */
    public McpResourceTemplate withCompletionHandler(McpAsyncCompletionHandler value) {
        return new McpResourceTemplate(
                definition, resource, Objects.requireNonNull(value, "completionHandler"));
    }
}
