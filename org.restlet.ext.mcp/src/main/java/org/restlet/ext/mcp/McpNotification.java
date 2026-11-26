/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.modern.McpSchema.SubscriptionFilter;
import io.modelcontextprotocol.modern.server.feature.ServerChange;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A notification an application publishes to {@code subscriptions/listen} streams through {@link
 * McpServerAdapter#getChanges()}, such as the {@code notifications/tasks} of the Tasks extension. A
 * stream receives it when its client asked for the notification's subscription field and the
 * adapter honours that field, through an {@link McpSubscription}; a scoped notification only goes
 * to the streams the field was honoured with that scope for, such as the streams of the principal a
 * task belongs to.
 *
 * <p>A stream too slow for its notifications keeps only the latest one per coalescing key: a
 * notification carrying a full state, such as a task's, replaces the one of the same key still
 * pending. Experimental.
 *
 * @param method The JSON-RPC method of the notification.
 * @param params The params of the notification; the stream adds its subscription id to their {@code
 *     _meta}.
 * @param field The subscription field a stream must have asked for.
 * @param scope The value the field must have been honoured with, or null for any.
 * @param key The coalescing key, or null to never replace a pending notification.
 */
public record McpNotification(
        String method, Map<String, Object> params, String field, Object scope, Object key)
        implements ServerChange {

    /**
     * Creates a notification.
     *
     * @param method The JSON-RPC method of the notification.
     * @param params The params of the notification.
     * @param field The subscription field a stream must have asked for.
     * @param scope The value the field must have been honoured with, or null for any.
     * @param key The coalescing key, or null.
     */
    public McpNotification {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(field, "field");
        if (method.isBlank() || field.isBlank()) {
            throw new IllegalArgumentException("method and field must not be blank");
        }
        params = Collections.unmodifiableMap(new LinkedHashMap<>(params));
    }

    /**
     * Creates a notification for the streams that asked for a field, without scope or coalescing.
     *
     * @param method The JSON-RPC method of the notification.
     * @param params The params of the notification.
     * @param field The subscription field a stream must have asked for.
     * @return The notification.
     */
    public static McpNotification of(String method, Map<String, ?> params, String field) {
        return new McpNotification(method, new LinkedHashMap<>(params), field, null, null);
    }

    /**
     * Returns this notification for the streams its field was honoured with a scope for only, see
     * {@link McpSubscription#scopedToPrincipal(String)}.
     *
     * @param value The scope.
     * @return A copy of this notification.
     */
    public McpNotification scopedTo(Object value) {
        return new McpNotification(
                method, params, field, Objects.requireNonNull(value, "scope"), key);
    }

    /**
     * Returns this notification replacing a pending one of the same key on a slow stream.
     *
     * @param value The coalescing key, such as a task identifier.
     * @return A copy of this notification.
     */
    public McpNotification coalescedBy(Object value) {
        return new McpNotification(
                method, params, field, scope, Objects.requireNonNull(value, "key"));
    }

    @Override
    public boolean isHonouredBy(SubscriptionFilter honoured) {
        Object agreed = honoured.get(field);
        if (agreed == null || Boolean.FALSE.equals(agreed)) {
            return false;
        }
        return scope == null || scope.equals(agreed);
    }
}
