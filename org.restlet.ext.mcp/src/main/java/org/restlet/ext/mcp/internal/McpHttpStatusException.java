/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import java.io.IOException;

/**
 * An MCP HTTP exchange answered without a JSON-RPC response to its request, with the HTTP status
 * code it got. A dual-era client tells a legacy server by a {@code 4xx} of this kind.
 */
public final class McpHttpStatusException extends IOException {
    private static final long serialVersionUID = 1L;

    private final int status;

    /**
     * @param message The message.
     * @param status The HTTP status code.
     * @param cause The cause, or null.
     */
    public McpHttpStatusException(String message, int status, Throwable cause) {
        super(message, cause);
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

    /**
     * Whether the status is a client error, {@code 4xx}.
     *
     * @return True if it is.
     */
    public boolean isClientError() {
        return status >= 400 && status < 500;
    }
}
