/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.modelcontextprotocol.modern.McpSchema;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A result made of JSON fields, for an {@link McpMethod} whose result has no SDK type, such as the
 * {@code skills/list} result of the Skills extension. Its fields are written after {@code
 * resultType}, as they are and in their order; the adapter adds its {@code _meta} server info as
 * for any result. A {@code complete} result takes its cache hint, {@code ttlMs} and {@code
 * cacheScope}, only through {@link #withCacheHint(McpCacheHint)}, so that the answer to a retry
 * after {@code input_required} is never reused. Experimental.
 */
public sealed class McpJsonResult implements McpSchema.Result permits McpJsonResult.Cached {
    private final Map<String, Object> fields;

    private McpJsonResult(Map<String, Object> fields) {
        this.fields = fields;
    }

    /**
     * Creates a {@code complete} result.
     *
     * @param fields The fields, without {@code resultType}, {@code ttlMs} or {@code cacheScope};
     *     copied.
     * @return The result.
     * @throws IllegalArgumentException If the fields hold {@code resultType}, {@code ttlMs} or
     *     {@code cacheScope}.
     * @see #withCacheHint(McpCacheHint)
     */
    public static McpJsonResult complete(Map<String, ?> fields) {
        return of(McpSchema.ResultType.COMPLETE, fields);
    }

    /**
     * Creates a result of a given type, such as {@code task} for the Tasks extension. An {@code
     * input_required} result must be an {@link McpSchema.InputRequiredResult} instead, whose
     * questions are checked against the client's capabilities and whose state is sealed.
     *
     * @param resultType The result type, not {@code input_required}.
     * @param fields The fields, without {@code resultType}, nor {@code ttlMs} or {@code cacheScope}
     *     for a {@code complete} result; copied.
     * @return The result.
     * @throws IllegalArgumentException If the type is blank or {@code input_required}, or the
     *     fields hold {@code resultType}, or a {@code complete} result's cache hint.
     */
    public static McpJsonResult of(String resultType, Map<String, ?> fields) {
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(fields, "fields");
        if (resultType.isBlank()) {
            throw new IllegalArgumentException("resultType must not be blank");
        }
        if (McpSchema.ResultType.INPUT_REQUIRED.equals(resultType)) {
            // The SDK checks and seals only results implementing InputRequired.
            throw new IllegalArgumentException(
                    "An input_required result is an McpSchema.InputRequiredResult");
        }
        if (fields.containsKey("resultType")) {
            throw new IllegalArgumentException("resultType is given apart from the fields");
        }
        if (McpSchema.ResultType.COMPLETE.equals(resultType)
                && (fields.containsKey(TTL_MS) || fields.containsKey(CACHE_SCOPE))) {
            // Raw, the hint would survive a retry after input_required.
            throw new IllegalArgumentException(
                    "A complete result's ttlMs and cacheScope are given by withCacheHint");
        }
        return unchecked(resultType, fields);
    }

    /**
     * Creates a result whose fields aren't checked, for the task a {@code tasks/get} result
     * describes, whose {@code ttlMs} is how long the task is kept.
     *
     * @param resultType The result type.
     * @param fields The fields, without {@code resultType}; copied.
     * @return The result.
     */
    static McpJsonResult unchecked(String resultType, Map<String, ?> fields) {
        Map<String, Object> copy = new LinkedHashMap<>();
        copy.put("resultType", resultType);
        copy.putAll(fields);
        return new McpJsonResult(Collections.unmodifiableMap(copy));
    }

    /**
     * This {@code complete} result with a cache hint: its {@code ttlMs} and {@code cacheScope}
     * follow its fields. As for a hinted {@code resources/read} result, a client doesn't reuse the
     * answer to a retry after {@code input_required}: there the hint becomes {@link
     * McpCacheHint#NONE}.
     *
     * @param hint The hint.
     * @return The result with the hint.
     * @throws IllegalStateException If this result isn't {@code complete}, already has a hint, or
     *     is a task's {@code tasks/get} result.
     */
    public McpJsonResult withCacheHint(McpCacheHint hint) {
        Objects.requireNonNull(hint, "hint");
        if (!McpSchema.ResultType.COMPLETE.equals(resultType())) {
            // A task's ttlMs is how long it is kept, which a retry must not reset.
            throw new IllegalStateException("Only a complete result takes a cache hint");
        }
        if (fields.containsKey(TTL_MS) || fields.containsKey(CACHE_SCOPE)) {
            throw new IllegalStateException("This result already has ttlMs or cacheScope");
        }
        Map<String, Object> copy = new LinkedHashMap<>(fields);
        copy.put(TTL_MS, hint.ttlMs());
        copy.put(CACHE_SCOPE, hint.scope() == McpSchema.CacheScope.PUBLIC ? "public" : "private");
        return new Cached(Collections.unmodifiableMap(copy), hint);
    }

    /**
     * The fields written for this result, {@code resultType} included.
     *
     * @return The fields, unmodifiable.
     */
    @JsonAnyGetter
    public Map<String, Object> fields() {
        return fields;
    }

    @Override
    public String resultType() {
        return (String) fields.get("resultType");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> meta() {
        return fields.get("_meta") instanceof Map<?, ?> meta ? (Map<String, Object>) meta : null;
    }

    @Override
    public boolean equals(Object other) {
        // A hinted result and an unhinted one aren't equal: only the first is a CacheableResult.
        return other instanceof McpJsonResult result
                && other.getClass() == getClass()
                && fields.equals(result.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @Override
    public String toString() {
        return "McpJsonResult" + fields;
    }

    private static final String TTL_MS = "ttlMs";

    private static final String CACHE_SCOPE = "cacheScope";

    /**
     * A result with a cache hint. Being a {@link McpSchema.CacheableResult}, the SDK answers a
     * retry after {@code input_required} with it as not reusable.
     */
    private static final class Cached extends McpJsonResult implements McpSchema.CacheableResult {
        private final McpCacheHint hint;

        private Cached(Map<String, Object> fields, McpCacheHint hint) {
            super(fields);
            this.hint = hint;
        }

        @Override
        public Long ttlMs() {
            return hint.ttlMs();
        }

        @Override
        public McpSchema.CacheScope cacheScope() {
            return hint.scope();
        }

        @Override
        public McpJsonResult withCacheHint(McpCacheHint hint) {
            throw new IllegalStateException("This result already has a cache hint");
        }

        @Override
        public String toString() {
            // Told apart from an unhinted result with the same fields, as equals does.
            return "McpJsonResult(cacheable)" + fields();
        }
    }
}
