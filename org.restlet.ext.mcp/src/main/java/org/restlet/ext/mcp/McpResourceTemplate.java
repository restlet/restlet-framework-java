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
 * A resource template of an {@link McpServerAdapter}, for {@link
 * McpServerAdapter#getResourceTemplates()}: its definition, listed, and the read handler of the
 * URIs it matches. Experimental: the SDK's modern API is a preview.
 *
 * @param definition The template, with its URI template.
 * @param resource The read handler of the matching URIs.
 */
public record McpResourceTemplate(
        McpSchema.ResourceTemplate definition, McpAsyncResource resource) {

    /**
     * Creates a resource template.
     *
     * @param definition The template, with its URI template.
     * @param resource The read handler of the matching URIs.
     */
    public McpResourceTemplate {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(resource, "resource");
    }
}
