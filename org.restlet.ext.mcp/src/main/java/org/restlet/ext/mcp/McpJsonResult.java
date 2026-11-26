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
 * for any result. Experimental.
 */
public final class McpJsonResult implements McpSchema.Result {
    private final Map<String, Object> fields;

    private McpJsonResult(Map<String, Object> fields) {
        this.fields = fields;
    }

    /**
     * Creates a {@code complete} result.
     *
     * @param fields The fields, without {@code resultType}; copied.
     * @return The result.
     */
    public static McpJsonResult complete(Map<String, ?> fields) {
        return of(McpSchema.ResultType.COMPLETE, fields);
    }

    /**
     * Creates a result of a given type, such as {@code task} for the Tasks extension.
     *
     * @param resultType The result type.
     * @param fields The fields, without {@code resultType}; copied.
     * @return The result.
     */
    public static McpJsonResult of(String resultType, Map<String, ?> fields) {
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(fields, "fields");
        if (resultType.isBlank()) {
            throw new IllegalArgumentException("resultType must not be blank");
        }
        if (fields.containsKey("resultType")) {
            throw new IllegalArgumentException("resultType is given apart from the fields");
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        copy.put("resultType", resultType);
        copy.putAll(fields);
        return new McpJsonResult(Collections.unmodifiableMap(copy));
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
        return other instanceof McpJsonResult result && fields.equals(result.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @Override
    public String toString() {
        return "McpJsonResult" + fields;
    }
}
