/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.common.McpTransportContext;
import java.util.Optional;
import org.restlet.ext.mcp.internal.McpBindings;

/**
 * What the sealed {@code requestState} of an {@code input_required} result is bound to, for a
 * {@link io.modelcontextprotocol.modern.server.RequestStateCodec} given to {@link
 * McpServerAdapter#setRequestStateCodec}: the authenticated user, the value of an extension
 * method's {@code Mcp-Name} param, and a digest of the request's arguments. A state sealed for one
 * of them must not open for another. The method and primitive name come from the SDK's request
 * context. Experimental.
 */
public final class McpRequestBinding {

    private McpRequestBinding() {}

    /**
     * The identity a request's sealed state is bound to: its user, the value of its extension
     * method's name param, and its arguments digest, in one string where no two different triples
     * collide.
     *
     * @param context The SDK's request context.
     * @return The identity.
     */
    public static String identity(io.modelcontextprotocol.modern.server.McpRequestContext context) {
        return identity(context.transportContext());
    }

    /**
     * The identity a request's sealed state is bound to, from its SDK transport context.
     *
     * @param transport The SDK transport context.
     * @return The identity.
     */
    public static String identity(McpTransportContext transport) {
        StringBuilder identity = new StringBuilder();
        for (Object part :
                new Object[] {
                    user(transport).orElse(null),
                    transport.get(McpBindings.BOUND_NAME_KEY),
                    transport.get(McpBindings.ARGUMENTS_DIGEST_KEY)
                }) {
            // Each part length-prefixed, an absent one as "-": no two triples give one string.
            identity.append(part instanceof String value ? value.length() + ":" + value : "-")
                    .append(';');
        }
        return identity.toString();
    }

    /**
     * The authenticated user of a request.
     *
     * @param transport The SDK transport context.
     * @return The user, or empty.
     */
    public static Optional<String> user(McpTransportContext transport) {
        return transport.get(McpProtocol.CONTEXT_KEY) instanceof McpRequestContext context
                ? context.principal()
                : Optional.empty();
    }

    /**
     * The digest of a request's arguments: SHA-256, in hex, of its params without {@code _meta},
     * {@code requestState} and {@code inputResponses}, written with sorted keys. A retry with the
     * same arguments has the same digest.
     *
     * @param transport The SDK transport context.
     * @return The digest, or empty when the request carried none.
     */
    public static Optional<String> argumentsDigest(McpTransportContext transport) {
        return transport.get(McpBindings.ARGUMENTS_DIGEST_KEY) instanceof String digest
                ? Optional.of(digest)
                : Optional.empty();
    }
}
