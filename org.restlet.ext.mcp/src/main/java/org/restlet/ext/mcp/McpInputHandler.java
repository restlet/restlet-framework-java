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
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Answers the input an MCP {@link McpProtocol#MODERN_VERSION} server asks for, through an {@code
 * input_required} result, such as an elicitation: {@link McpModernClient} then retries the request
 * with the responses and the result's request state. Experimental: the SDK's modern API is a
 * preview.
 */
@FunctionalInterface
public interface McpInputHandler {

    /**
     * Answers the input requests of a result.
     *
     * @param required The result, with its input requests by key.
     * @return The responses, by the keys of the input requests, such as {@code {"action": "accept",
     *     "content": {...}}} for an elicitation; or an empty {@code Mono} to return the result to
     *     the caller instead of retrying.
     */
    Mono<Map<String, Object>> respond(McpSchema.InputRequiredResult required);
}
