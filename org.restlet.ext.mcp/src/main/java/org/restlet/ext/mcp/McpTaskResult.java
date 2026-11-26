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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.modelcontextprotocol.modern.McpSchema;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * What a {@code tools/call} handler returns when it runs the call as a task of the MCP Tasks
 * extension instead of answering it: {@code resultType: "task"} and the task's state. The adapter
 * only sends it to a client that declared the extension, and only when it has a {@link
 * McpTaskStore} to answer {@code tasks/*}: check {@link #isDeclared} first. Experimental.
 *
 * @param task The state of the task, not ended.
 */
// Written through fields() only: the task component and the interface accessors would repeat it.
@JsonIgnoreProperties({"task", "resultType", "meta"})
public record McpTaskResult(McpTask task) implements McpSchema.CallToolOutcome {

    /** The {@code resultType} of a task. */
    public static final String RESULT_TYPE = "task";

    /**
     * Creates the result of a call run as a task.
     *
     * @param task The state of the task, not ended.
     */
    public McpTaskResult {
        Objects.requireNonNull(task, "task");
        if (task.status().isTerminal()) {
            throw new IllegalArgumentException("A tools/call task result is a task not ended");
        }
    }

    /**
     * Whether the client of a request declared the Tasks extension, so that its {@code tools/call}
     * may be answered with a task.
     *
     * @param context The SDK's request context.
     * @return True when the request declares {@link McpTask#EXTENSION_ID}.
     */
    public static boolean isDeclared(
            io.modelcontextprotocol.modern.server.McpRequestContext context) {
        McpSchema.ClientCapabilities capabilities = context.clientCapabilities();
        return capabilities != null
                && capabilities.extensions() != null
                && capabilities.extensions().containsKey(McpTask.EXTENSION_ID);
    }

    @Override
    public String resultType() {
        return RESULT_TYPE;
    }

    @Override
    public Map<String, Object> meta() {
        return null;
    }

    /**
     * The fields written for this result: {@code resultType} and the task's fields.
     *
     * @return The fields.
     */
    @JsonAnyGetter
    public Map<String, Object> fields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("resultType", RESULT_TYPE);
        fields.putAll(task.toFields());
        return Collections.unmodifiableMap(fields);
    }
}
