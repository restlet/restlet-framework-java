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
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * How long a {@code 2026-07-28} client may reuse a result, and whether a cache shared between users
 * may keep it: the {@code ttlMs} and {@code cacheScope} of the result. A {@link
 * McpSchema.CacheScope#PUBLIC public} result must be the same for every client; a {@link
 * McpSchema.CacheScope#PRIVATE private} one is only reused by the client that got it. Results reach
 * {@code 2025-11-25} clients without them. Experimental: the SDK's modern API is a preview.
 *
 * @param ttlMs How long the result may be reused, in milliseconds; 0 when it must not be.
 * @param scope Who may reuse it.
 * @see McpServerAdapter#setCacheHint(String, McpCacheHint)
 */
public record McpCacheHint(long ttlMs, McpSchema.CacheScope scope) {

    /** A result that must not be reused, which is what a result without a hint says. */
    public static final McpCacheHint NONE = new McpCacheHint(0, McpSchema.CacheScope.PRIVATE);

    /**
     * The methods whose results an adapter gives a hint: {@code server/discover} and the listings.
     */
    public static final Set<String> METHODS =
            Set.of(
                    "server/discover",
                    "tools/list",
                    "resources/list",
                    "resources/templates/list",
                    "prompts/list");

    /**
     * Creates a hint.
     *
     * @param ttlMs How long the result may be reused, in milliseconds; 0 when it must not be.
     * @param scope Who may reuse it.
     * @throws IllegalArgumentException If the time to live is negative.
     */
    public McpCacheHint {
        if (ttlMs < 0) {
            throw new IllegalArgumentException("ttlMs must not be negative");
        }
        Objects.requireNonNull(scope, "scope");
    }

    /**
     * A result every client may reuse, through a shared cache too.
     *
     * @param ttl How long, to the millisecond.
     * @return The hint.
     * @throws IllegalArgumentException If the time to live is negative.
     * @throws ArithmeticException If it doesn't fit in milliseconds.
     */
    public static McpCacheHint publicFor(Duration ttl) {
        return new McpCacheHint(ttl.toMillis(), McpSchema.CacheScope.PUBLIC);
    }

    /**
     * A result only the client that got it may reuse.
     *
     * @param ttl How long, to the millisecond.
     * @return The hint.
     * @throws IllegalArgumentException If the time to live is negative.
     * @throws ArithmeticException If it doesn't fit in milliseconds.
     */
    public static McpCacheHint privateFor(Duration ttl) {
        return new McpCacheHint(ttl.toMillis(), McpSchema.CacheScope.PRIVATE);
    }
}
