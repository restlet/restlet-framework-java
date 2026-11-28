/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema;
import java.util.List;
import org.restlet.ext.mcp.McpProtocol;

/**
 * The protocol versions an adapter serves through {@code handle}, over HTTP or stdio, as every
 * {@code UnsupportedProtocolVersionError} (-32022) it answers over HTTP lists them, whichever check
 * failed. Through {@code dispatch}, only the modern version is served, as the SDK's answer says.
 */
public final class McpVersions {

    /** The versions served: the modern one through the SDK, the legacy one at the edge. */
    public static final List<String> SERVED =
            List.of(McpProtocol.MODERN_VERSION, McpProtocol.VERSION);

    /** The longest {@code requested} version echoed back, so that the answer stays small. */
    static final int MAX_REQUESTED_LENGTH = 64;

    private McpVersions() {}

    /**
     * The requested version as an answer echoes it, cut to {@link #MAX_REQUESTED_LENGTH}
     * characters.
     *
     * @param version The version requested.
     * @return The version echoed, or null.
     */
    public static String requested(String version) {
        return version == null || version.length() <= MAX_REQUESTED_LENGTH
                ? version
                : version.substring(0, MAX_REQUESTED_LENGTH);
    }

    /** The message of every -32022 the endpoint answers; the versions are in its data. */
    public static final String MESSAGE = "Unsupported protocol version";

    /**
     * A response of the SDK's modern server, answered over HTTP, with the versions of the whole
     * endpoint: the SDK knows only the modern one, so its -32022 answers would leave out the legacy
     * edge's. Its message becomes {@link #MESSAGE}: for a headered {@code initialize}, the SDK's
     * names the versions and echoes the requested one in full.
     *
     * @param response The SDK's response.
     * @return The response, with {@link #SERVED} when it is an unsupported version error.
     */
    public static JsonRpc.JSONRPCResponse served(JsonRpc.JSONRPCResponse response) {
        JsonRpc.JSONRPCResponse.JSONRPCError error = response == null ? null : response.error();
        if (error == null
                || error.code() == null
                || error.code() != McpProtocol.UNSUPPORTED_PROTOCOL_VERSION
                || !(error.data() instanceof McpSchema.UnsupportedProtocolVersionData data)) {
            return response;
        }
        return JsonRpc.JSONRPCResponse.error(
                response.id(),
                new JsonRpc.JSONRPCResponse.JSONRPCError(
                        error.code(),
                        MESSAGE,
                        new McpSchema.UnsupportedProtocolVersionData(
                                SERVED, requested(data.requested()))));
    }
}
