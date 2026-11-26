/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * A subscription field an application honours on {@code subscriptions/listen} streams, for {@link
 * McpServerAdapter#getSubscriptions()}, beyond the tools, prompts and resources changes the adapter
 * honours itself: the field its {@link McpNotification}s are sent on. When a client asks for the
 * field, the honour function decides the value the stream is acknowledged with, or refuses the
 * field. Experimental.
 *
 * @param field The subscription field, such as {@code io.modelcontextprotocol/tasks}.
 * @param honour Decides the honoured value.
 */
public record McpSubscription(String field, Honour honour) {

    /** Decides whether, and with which value, a stream's request for the field is honoured. */
    @FunctionalInterface
    public interface Honour {
        /**
         * Honours a request for the field.
         *
         * @param context The listening request's context, with its principal.
         * @param requested The value the client asked for, not null.
         * @return The honoured value, or null to refuse the field.
         */
        Object honour(McpRequestContext context, Object requested);
    }

    /**
     * Creates a subscription.
     *
     * @param field The subscription field.
     * @param honour Decides the honoured value.
     */
    public McpSubscription {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(honour, "honour");
        if (field.isBlank()) {
            throw new IllegalArgumentException("An MCP subscription field must not be blank");
        }
    }

    /**
     * A field honoured as the client asked for it, for notifications every stream may receive.
     *
     * @param field The subscription field.
     * @return The subscription.
     */
    public static McpSubscription of(String field) {
        return new McpSubscription(field, (context, requested) -> requested);
    }

    /**
     * A field honoured with the scope of the listening principal, for notifications only that
     * principal's streams may receive: publish them {@link McpNotification#scopedTo scoped to}
     * {@link #principalScope(String)} of their principal. The scope is a digest, so the
     * acknowledgment doesn't carry the principal in clear.
     *
     * @param field The subscription field.
     * @return The subscription.
     */
    public static McpSubscription scopedToPrincipal(String field) {
        return new McpSubscription(
                field, (context, requested) -> principalScope(context.principal().orElse(null)));
    }

    /**
     * The scope of a principal's streams, see {@link #scopedToPrincipal(String)}.
     *
     * @param principal The principal, or null for unauthenticated streams.
     * @return The scope: a SHA-256 digest, in hex.
     */
    public static String principalScope(String principal) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(
                                    (principal == null ? "\0" : "p:" + principal)
                                            .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
