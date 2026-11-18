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
import reactor.core.publisher.Mono;

/**
 * Decides which tools a request lists, on the SDK's {@code 2026-07-28} types. Every filter of an
 * {@link McpServerAdapter} applies, to the lists of both eras.
 */
@FunctionalInterface
public interface McpToolFilter {

    /**
     * Whether a tool is listed for a request.
     *
     * @param context The request context.
     * @param tool The tool's definition.
     * @return True to list it.
     */
    Mono<Boolean> isVisible(McpRequestContext context, McpSchema.Tool tool);
}
