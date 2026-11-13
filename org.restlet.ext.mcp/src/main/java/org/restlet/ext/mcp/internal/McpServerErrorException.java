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
import io.modelcontextprotocol.modern.McpException;

/**
 * A JSON-RPC error answering its request with an HTTP server error, {@code 5xx}. It keeps the error
 * for the caller, and the status for a dual-era client, which never takes a server error for a
 * legacy server.
 */
public final class McpServerErrorException extends McpException {
    private static final long serialVersionUID = 1L;

    private final int status;

    /**
     * @param error The JSON-RPC error.
     * @param status The HTTP status code.
     */
    public McpServerErrorException(JsonRpc.JSONRPCResponse.JSONRPCError error, int status) {
        super(error);
        this.status = status;
    }

    /**
     * The HTTP status code.
     *
     * @return The code.
     */
    public int getStatus() {
        return status;
    }
}
