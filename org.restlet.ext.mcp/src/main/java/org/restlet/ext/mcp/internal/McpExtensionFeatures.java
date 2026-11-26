/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.McpFeature;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.restlet.ext.mcp.McpMethod;
import reactor.core.publisher.Mono;

/**
 * The SDK features of an adapter's extension methods and advertised extensions. Internal to the MCP
 * extension.
 */
public final class McpExtensionFeatures {

    /** The methods MCP itself defines, which an extension may not register. */
    private static final Set<String> RESERVED =
            Set.of(
                    McpSchema.METHOD_SERVER_DISCOVER,
                    McpSchema.METHOD_TOOLS_LIST,
                    McpSchema.METHOD_TOOLS_CALL,
                    McpSchema.METHOD_RESOURCES_LIST,
                    McpSchema.METHOD_RESOURCES_READ,
                    McpSchema.METHOD_RESOURCES_TEMPLATES_LIST,
                    McpSchema.METHOD_PROMPTS_LIST,
                    McpSchema.METHOD_PROMPTS_GET,
                    McpSchema.METHOD_COMPLETION_COMPLETE,
                    McpSchema.METHOD_SUBSCRIPTIONS_LISTEN,
                    "initialize",
                    "ping",
                    "logging/setLevel");

    private McpExtensionFeatures() {}

    /**
     * Builds the features of extension methods and advertisements.
     *
     * @param methods The extension methods.
     * @param extensions The advertised extensions, by id.
     * @return The features.
     * @throws IllegalArgumentException If a method is registered twice or is one MCP defines, or an
     *     extension id isn't a prefixed {@code _meta} key.
     */
    public static List<McpFeature> of(
            List<McpMethod> methods, Map<String, Map<String, Object>> extensions) {
        List<McpFeature> features = new ArrayList<>();
        Set<String> names = new java.util.HashSet<>();
        for (McpMethod method : methods) {
            if (RESERVED.contains(method.name()) || method.name().startsWith("notifications/")) {
                throw new IllegalArgumentException(
                        "MCP defines the method " + method.name() + "; an extension can't");
            }
            if (!names.add(method.name())) {
                throw new IllegalArgumentException("Duplicate MCP method: " + method.name());
            }
            features.add(new MethodFeature(method));
        }
        if (!extensions.isEmpty()) {
            Map<String, Map<String, Object>> copy = new LinkedHashMap<>();
            extensions.forEach(
                    (id, settings) ->
                            copy.put(id, settings == null ? Map.of() : Map.copyOf(settings)));
            // Fails here, at start, rather than when the SDK builds discover.
            McpSchema.ServerCapabilities.Builder probe = McpSchema.ServerCapabilities.builder();
            copy.forEach(probe::extension);
            features.add(new ExtensionsFeature(copy));
        }
        return features;
    }

    /**
     * The params field each extension method's {@code Mcp-Name} header mirrors.
     *
     * @param methods The extension methods.
     * @return The params field by method, for those taking the header.
     */
    public static Map<String, String> nameParams(List<McpMethod> methods) {
        Map<String, String> params = new LinkedHashMap<>();
        for (McpMethod method : methods) {
            if (method.nameParam() != null) {
                params.put(method.name(), method.nameParam());
            }
        }
        return Map.copyOf(params);
    }

    /**
     * The SDK transport context key of the value an extension method's name param has in a request.
     * The sealed {@code requestState} is bound to it: the SDK binds the primitive name of the
     * methods MCP defines only.
     */
    public static final String BOUND_NAME_KEY = "org.restlet.ext.mcp.boundName";

    /**
     * The SDK transport context entries binding a request's sealed state to the value of its
     * method's name param, if it is an extension method that declares one.
     *
     * @param request The request.
     * @param nameParams The params field each extension method's {@code Mcp-Name} mirrors.
     * @return The entries, empty when nothing is bound.
     */
    public static Map<String, Object> boundName(
            io.modelcontextprotocol.modern.JsonRpc.JSONRPCRequest request,
            Map<String, String> nameParams) {
        String param = nameParams.get(request.method());
        if (param != null
                && request.params() instanceof Map<?, ?> params
                && params.get(param) instanceof String value) {
            return Map.of(BOUND_NAME_KEY, value);
        }
        return Map.of();
    }

    /**
     * The identity a sealed state is bound to: the principal, and the name param value when one is
     * bound. A state sealed for one value then fails to open for another. Without a bound value,
     * the principal alone, as before.
     *
     * @param principal The principal, or null.
     * @param boundName The bound value, or null.
     * @return The identity, or null.
     */
    public static String bind(String principal, Object boundName) {
        if (!(boundName instanceof String name)) {
            return principal;
        }
        // Length-prefixed and led by a NUL, so no principal alone or other pair collides with it.
        return "\0" + name.length() + ":" + name + (principal == null ? "" : principal);
    }

    /** One extension method. */
    private record MethodFeature(McpMethod method) implements McpFeature {
        @Override
        public Set<String> methods() {
            return Set.of(method.name());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Mono<? extends McpAsyncResponse<? extends McpSchema.Result>> handle(
                McpRequestContext ctx, Object params) {
            return method.handler()
                    .handle(
                            ctx,
                            params instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of());
        }

        @Override
        public Set<String> inputRequiredMethods() {
            return method.inputRequired() ? Set.of(method.name()) : Set.of();
        }
    }

    /** Advertises extensions in {@code server/discover}, serving no method. */
    private record ExtensionsFeature(Map<String, Map<String, Object>> extensions)
            implements McpFeature {
        @Override
        public Set<String> methods() {
            return Set.of();
        }

        @Override
        public Mono<? extends McpAsyncResponse<? extends McpSchema.Result>> handle(
                McpRequestContext ctx, Object params) {
            return Mono.error(new IllegalStateException("Serves no method"));
        }

        @Override
        public void capabilities(McpSchema.ServerCapabilities.Builder builder) {
            extensions.forEach(builder::extension);
        }
    }
}
