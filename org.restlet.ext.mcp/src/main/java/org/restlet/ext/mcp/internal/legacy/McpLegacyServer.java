/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal.legacy;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.McpRequestContext;
import org.restlet.ext.mcp.internal.McpLegacyEdge;
import org.restlet.ext.mcp.internal.McpMessages;

/**
 * The server edge answering MCP {@code 2025-11-25} clients from the {@code 2026-07-28} core. Each
 * request is translated to a modern one, with the {@code _meta} the revision requires and no client
 * capabilities, and its final result translated back: streamed messages are dropped, and a result
 * that requires input is an error, since a legacy client can't provide it.
 *
 * <p>{@code initialize} is answered from {@code server/discover}, advertising only tools, resources
 * and prompts, with no session. {@code ping} gets an empty result and notifications are accepted
 * and dropped, without reaching the core. Every other method a 2026-07-28 server doesn't share with
 * legacy clients, such as {@code logging/setLevel} or {@code resources/subscribe}, is not found.
 */
public final class McpLegacyServer implements McpLegacyEdge.Server {
    private static final Logger LOGGER = Logger.getLogger(McpLegacyServer.class.getName());
    private static final TypeRef<Map<String, Object>> MAP = new TypeRef<>() {};

    /** The methods translated to the core, which both eras share. */
    private static final Set<String> SHARED =
            Set.of(
                    McpSchema.METHOD_TOOLS_LIST,
                    McpSchema.METHOD_TOOLS_CALL,
                    McpSchema.METHOD_RESOURCES_LIST,
                    McpSchema.METHOD_RESOURCES_TEMPLATES_LIST,
                    McpSchema.METHOD_RESOURCES_READ,
                    McpSchema.METHOD_PROMPT_LIST,
                    McpSchema.METHOD_PROMPT_GET);

    /** The capabilities a legacy client may see, all without change notifications. */
    private static final Map<String, Map<String, Object>> CAPABILITIES =
            Map.of(
                    "tools", Map.of("listChanged", false),
                    "resources", Map.of("subscribe", false, "listChanged", false),
                    "prompts", Map.of("listChanged", false));

    /** What a modern result carries that a legacy client doesn't know. */
    private static final Set<String> MODERN_FIELDS = Set.of("resultType", "ttlMs", "cacheScope");

    private final McpJsonMapper mapper;
    private final McpLegacyEdge.Core core;
    private final int maxMessageBytes;
    private final Duration timeout;

    /**
     * Creates the server edge of an adapter.
     *
     * @param mapper The JSON mapper.
     * @param core The modern core.
     * @param maxMessageBytes The largest request message.
     * @param timeout The request timeout.
     */
    public McpLegacyServer(
            McpJsonMapper mapper, McpLegacyEdge.Core core, int maxMessageBytes, Duration timeout) {
        this.mapper = mapper;
        this.core = core;
        this.maxMessageBytes = maxMessageBytes;
        this.timeout = timeout;
    }

    @Override
    public McpLegacyEdge.Reply exchange(
            Request request, String version, McpRequestContext context) {
        McpSchema.JSONRPCMessage message;
        try (var input = request.getEntity().getStream()) {
            byte[] bytes = McpMessages.read(input, maxMessageBytes);
            message = decode(McpMessages.utf8(bytes));
        } catch (McpMessages.MessageLimitException e) {
            LOGGER.log(Level.FINE, "Oversized MCP request", e);
            return new McpLegacyEdge.Reply(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE, null);
        } catch (McpMessages.InvalidEnvelopeException e) {
            LOGGER.log(Level.FINE, "Invalid MCP envelope", e);
            return badRequest(McpMessages.error(null, -32600, "Invalid MCP request envelope"));
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.FINE, "Invalid MCP request", e);
            return badRequest(McpMessages.error(null, -32700, "Invalid MCP JSON request"));
        }
        if (request.getEntity().getSize() == 0) {
            return badRequest(McpMessages.error(null, -32600, "Empty MCP request body"));
        }
        if (!(message instanceof McpSchema.JSONRPCRequest rpc)) {
            // Notifications, such as notifications/initialized or roots changes, have no use here.
            return new McpLegacyEdge.Reply(Status.SUCCESS_ACCEPTED, null);
        }
        boolean initialize = McpSchema.METHOD_INITIALIZE.equals(rpc.method());
        if (version == null && !initialize) {
            return badRequest(
                    McpMessages.error(rpc.id(), -32600, "MCP-Protocol-Version is required"));
        }
        if (initialize && !supported(rpc)) {
            return badRequest(McpMessages.error(rpc.id(), -32600, "Unsupported MCP version"));
        }
        Object reply;
        if (initialize) {
            reply = initialize(rpc, context);
        } else if (McpSchema.METHOD_PING.equals(rpc.method())) {
            reply =
                    new McpSchema.JSONRPCResponse(
                            McpSchema.JSONRPC_VERSION, rpc.id(), Map.of(), null);
        } else if (SHARED.contains(rpc.method())) {
            reply = forward(rpc, context);
        } else {
            reply = McpMessages.error(rpc.id(), -32601, "Method not found: " + rpc.method());
        }
        return new McpLegacyEdge.Reply(Status.SUCCESS_OK, reply);
    }

    private static McpLegacyEdge.Reply badRequest(Object body) {
        return new McpLegacyEdge.Reply(Status.CLIENT_ERROR_BAD_REQUEST, body);
    }

    /** Whether an {@code initialize} request offers the revision the edge speaks. */
    private static boolean supported(McpSchema.JSONRPCRequest rpc) {
        return rpc.params() instanceof Map<?, ?> params
                && McpProtocol.VERSION.equals(params.get("protocolVersion"));
    }

    /** Answers {@code initialize} from the core's {@code server/discover}. */
    private Object initialize(McpSchema.JSONRPCRequest rpc, McpRequestContext context) {
        JsonRpc.JSONRPCResponse discovered =
                call(
                        new JsonRpc.JSONRPCRequest(
                                io.modelcontextprotocol.modern.McpSchema.METHOD_SERVER_DISCOVER,
                                rpc.id(),
                                modern(Map.of())),
                        context);
        if (discovered.error() != null) {
            return legacy(rpc.id(), discovered);
        }
        Map<String, Object> result = mapper.convertValue(discovered.result(), MAP);
        Map<String, Object> capabilities = new LinkedHashMap<>();
        if (result.get("capabilities") instanceof Map<?, ?> advertised) {
            CAPABILITIES.forEach(
                    (name, value) -> {
                        if (advertised.containsKey(name)) {
                            capabilities.put(name, value);
                        }
                    });
        }
        Map<String, Object> initialized = new LinkedHashMap<>();
        initialized.put("protocolVersion", McpProtocol.VERSION);
        initialized.put("capabilities", capabilities);
        initialized.put(
                "serverInfo",
                result.get("_meta") instanceof Map<?, ?> meta
                        ? meta.get(io.modelcontextprotocol.modern.McpSchema.MetaKeys.SERVER_INFO)
                        : null);
        if (result.get("instructions") != null) {
            initialized.put("instructions", result.get("instructions"));
        }
        return new McpSchema.JSONRPCResponse(
                McpSchema.JSONRPC_VERSION, rpc.id(), initialized, null);
    }

    /** Forwards a shared method to the core, translating its final response back. */
    private Object forward(McpSchema.JSONRPCRequest rpc, McpRequestContext context) {
        Map<String, Object> params =
                rpc.params() == null ? Map.of() : mapper.convertValue(rpc.params(), MAP);
        JsonRpc.JSONRPCResponse response =
                call(new JsonRpc.JSONRPCRequest(rpc.method(), rpc.id(), modern(params)), context);
        if (response.error() != null) {
            return legacy(rpc.id(), response);
        }
        Map<String, Object> result =
                new LinkedHashMap<>(mapper.convertValue(response.result(), MAP));
        if (io.modelcontextprotocol.modern.McpSchema.ResultType.INPUT_REQUIRED.equals(
                result.get("resultType"))) {
            return McpMessages.error(
                    rpc.id(), -32603, "MCP request requires input a legacy client can't provide");
        }
        result.keySet().removeAll(MODERN_FIELDS);
        return new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, rpc.id(), result, null);
    }

    /** Runs a modern request through the core, within the request timeout. */
    private JsonRpc.JSONRPCResponse call(
            JsonRpc.JSONRPCRequest request, McpRequestContext context) {
        Optional<JsonRpc.JSONRPCResponse> response =
                core.call(request, context).blockOptional(timeout);
        // An interceptor answering nothing leaves the request unanswered: an internal error.
        return response.orElseGet(
                () ->
                        JsonRpc.JSONRPCResponse.error(
                                request.id(),
                                new JsonRpc.JSONRPCResponse.JSONRPCError(
                                        -32603, "MCP result suppressed")));
    }

    /** A modern error response as a legacy one. */
    private static Map<String, Object> legacy(Object id, JsonRpc.JSONRPCResponse response) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", response.error().code());
        error.put("message", response.error().message());
        if (response.error().data() != null) {
            error.put("data", response.error().data());
        }
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", id);
        envelope.put("error", error);
        return envelope;
    }

    /**
     * The params of a modern request: a legacy request's, with the {@code _meta} the revision
     * requires added to its own, such as a progress token.
     */
    private static Map<String, Object> modern(Map<String, Object> params) {
        Map<String, Object> modern = new LinkedHashMap<>(params);
        Map<String, Object> meta = new LinkedHashMap<>();
        if (params.get("_meta") instanceof Map<?, ?> given) {
            given.forEach((key, value) -> meta.put(String.valueOf(key), value));
        }
        meta.put(
                io.modelcontextprotocol.modern.McpSchema.MetaKeys.PROTOCOL_VERSION,
                McpProtocol.MODERN_VERSION);
        meta.put(io.modelcontextprotocol.modern.McpSchema.MetaKeys.CLIENT_CAPABILITIES, Map.of());
        modern.put("_meta", meta);
        return modern;
    }

    /** Decodes a legacy request or notification, checking its envelope first. */
    private McpSchema.JSONRPCMessage decode(String json) throws IOException {
        Object decoded = mapper.readValue(json, new TypeRef<Object>() {});
        if (!(decoded instanceof Map<?, ?> envelope)
                || !"2.0".equals(envelope.get("jsonrpc"))
                || !(envelope.get("method") instanceof String method)
                || method.isBlank()
                || envelope.containsKey("params")
                        && envelope.get("params") != null
                        && !(envelope.get("params") instanceof Map<?, ?>)
                || envelope.containsKey("id")
                        && !(envelope.get("id") instanceof String
                                || envelope.get("id") instanceof Number)) {
            throw new McpMessages.InvalidEnvelopeException(
                    "Invalid MCP request or notification envelope");
        }
        return McpSchema.deserializeJsonRpcMessage(mapper, json);
    }
}
