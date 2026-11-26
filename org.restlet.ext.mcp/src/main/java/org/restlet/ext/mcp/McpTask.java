/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The state of a task of the MCP Tasks extension ({@code io.modelcontextprotocol/tasks}): what
 * {@code tools/call} answers when it runs a task, and what {@code tasks/get} answers about it.
 * Experimental.
 *
 * @param taskId The task identifier.
 * @param status The status.
 * @param createdAt When the task was created.
 * @param lastUpdatedAt When the task last changed.
 * @param ttlMs How long the task is kept after its creation, in milliseconds, or null when the
 *     server doesn't say.
 * @param pollIntervalMs How often a client should poll {@code tasks/get}, in milliseconds, or null.
 * @param inputRequests The questions of an {@link Status#INPUT_REQUIRED} task, keyed as {@code
 *     tasks/update} answers them, else null.
 * @param result The {@code tools/call} result of a {@link Status#COMPLETED} task, else null.
 * @param error The JSON-RPC error of a {@link Status#FAILED} task, with its {@code code} and {@code
 *     message}, else null.
 */
public record McpTask(
        String taskId,
        Status status,
        Instant createdAt,
        Instant lastUpdatedAt,
        Long ttlMs,
        Long pollIntervalMs,
        Map<String, Object> inputRequests,
        Map<String, Object> result,
        Map<String, Object> error) {

    /** The extension identifier, advertised and declared under {@code extensions}. */
    public static final String EXTENSION_ID = "io.modelcontextprotocol/tasks";

    /** The status of a task. */
    public enum Status {
        /** Running. */
        WORKING("working"),
        /** Waiting for the answers to its {@code inputRequests}. */
        INPUT_REQUIRED("input_required"),
        /** Ended with a result. */
        COMPLETED("completed"),
        /** Ended with an error. */
        FAILED("failed"),
        /** Ended by a cancellation. */
        CANCELLED("cancelled");

        private final String wireValue;

        Status(String wireValue) {
            this.wireValue = wireValue;
        }

        /**
         * The value written on the wire.
         *
         * @return The value, such as {@code input_required}.
         */
        public String wireValue() {
            return wireValue;
        }

        /**
         * Whether the task has ended.
         *
         * @return True when completed, failed or cancelled.
         */
        public boolean isTerminal() {
            return this == COMPLETED || this == FAILED || this == CANCELLED;
        }

        /**
         * The status written as {@code value}.
         *
         * @param value The wire value.
         * @return The status.
         * @throws IllegalArgumentException If no status is written so.
         */
        public static Status of(String value) {
            for (Status status : values()) {
                if (status.wireValue.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown MCP task status: " + value);
        }
    }

    /**
     * Creates a task state, checking that only the status it belongs to carries input requests, a
     * result or an error.
     */
    public McpTask {
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(lastUpdatedAt, "lastUpdatedAt");
        if (taskId.isBlank()) {
            throw new IllegalArgumentException("An MCP task id must not be blank");
        }
        if (ttlMs != null && ttlMs < 0 || pollIntervalMs != null && pollIntervalMs < 0) {
            throw new IllegalArgumentException("ttlMs and pollIntervalMs must not be negative");
        }
        if ((inputRequests != null) != (status == Status.INPUT_REQUIRED)) {
            throw new IllegalArgumentException("Only an input_required task has input requests");
        }
        if ((result != null) != (status == Status.COMPLETED)) {
            throw new IllegalArgumentException("Only a completed task has a result");
        }
        if ((error != null) != (status == Status.FAILED)) {
            throw new IllegalArgumentException("Only a failed task has an error");
        }
        inputRequests = copy(inputRequests);
        result = copy(result);
        error = copy(error);
    }

    /**
     * Creates the state of a task that just started.
     *
     * @param taskId The task identifier.
     * @param createdAt When the task was created.
     * @param ttlMs How long the task is kept, or null.
     * @param pollIntervalMs How often to poll, or null.
     * @return The state, {@link Status#WORKING}.
     */
    public static McpTask working(
            String taskId, Instant createdAt, Long ttlMs, Long pollIntervalMs) {
        return new McpTask(
                taskId,
                Status.WORKING,
                createdAt,
                createdAt,
                ttlMs,
                pollIntervalMs,
                null,
                null,
                null);
    }

    /**
     * The fields written for this state, in the order the extension shows them. {@code ttlMs} is
     * written even when null, as the server keeping the task without a limit.
     *
     * @return The fields.
     */
    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("taskId", taskId);
        fields.put("status", status.wireValue());
        fields.put("createdAt", createdAt.toString());
        fields.put("lastUpdatedAt", lastUpdatedAt.toString());
        fields.put("ttlMs", ttlMs);
        if (pollIntervalMs != null) {
            fields.put("pollIntervalMs", pollIntervalMs);
        }
        if (inputRequests != null) {
            fields.put("inputRequests", inputRequests);
        }
        if (result != null) {
            fields.put("result", result);
        }
        if (error != null) {
            fields.put("error", error);
        }
        return fields;
    }

    /**
     * Reads a state from the fields of a {@code tools/call} or {@code tasks/get} result.
     *
     * @param fields The result fields.
     * @return The state.
     * @throws IllegalArgumentException If a field is missing or malformed.
     */
    @SuppressWarnings("unchecked")
    public static McpTask fromFields(Map<String, ?> fields) {
        Objects.requireNonNull(fields, "fields");
        try {
            return new McpTask(
                    (String) fields.get("taskId"),
                    Status.of((String) fields.get("status")),
                    Instant.parse((String) fields.get("createdAt")),
                    Instant.parse((String) fields.get("lastUpdatedAt")),
                    number(fields.get("ttlMs")),
                    number(fields.get("pollIntervalMs")),
                    (Map<String, Object>) fields.get("inputRequests"),
                    (Map<String, Object>) fields.get("result"),
                    (Map<String, Object>) fields.get("error"));
        } catch (ClassCastException | NullPointerException | DateTimeParseException e) {
            throw new IllegalArgumentException("Malformed MCP task: " + fields, e);
        }
    }

    private static Long number(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static Map<String, Object> copy(Map<String, Object> map) {
        return map == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }
}
