/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

/**
 * The explicitly supported, sessionless MCP profile. No Ikanos compatibility profile is implied.
 */
public final class McpProtocol {
    public static final String VERSION = "2025-11-25";
    public static final String VERSION_HEADER = "MCP-Protocol-Version";
    public static final String CONTEXT_KEY = "org.restlet.ext.mcp.requestContext";
    public static final String STREAM_TRANSPORT_ATTRIBUTE = "org.restlet.ext.mcp.streamTransport";
    public static final int DEFAULT_MAX_MESSAGE_BYTES = 1024 * 1024;
    public static final int DEFAULT_MAX_CONCURRENT_CALLS = 128;

    private McpProtocol() {}
}
