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
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.McpSubscription;
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

    /** The subscription fields MCP itself defines, which an application may not honour. */
    private static final Set<String> CORE_FIELDS =
            Set.of(
                    McpSchema.SubscriptionFilter.TOOLS_LIST_CHANGED,
                    McpSchema.SubscriptionFilter.PROMPTS_LIST_CHANGED,
                    McpSchema.SubscriptionFilter.RESOURCES_LIST_CHANGED,
                    McpSchema.SubscriptionFilter.RESOURCE_SUBSCRIPTIONS);

    /**
     * The feature honouring the application's subscription fields on {@code subscriptions/listen},
     * or none.
     *
     * @param subscriptions The application's subscriptions.
     * @return The feature, or empty.
     * @throws IllegalArgumentException If a field is honoured twice or is one MCP defines.
     */
    public static List<McpFeature> subscriptions(List<McpSubscription> subscriptions) {
        if (subscriptions.isEmpty()) {
            return List.of();
        }
        Map<String, McpSubscription> byField = new LinkedHashMap<>();
        for (McpSubscription subscription : subscriptions) {
            if (CORE_FIELDS.contains(subscription.field())) {
                throw new IllegalArgumentException(
                        "MCP defines the subscription field " + subscription.field());
            }
            if (byField.putIfAbsent(subscription.field(), subscription) != null) {
                throw new IllegalArgumentException(
                        "Duplicate MCP subscription field: " + subscription.field());
            }
        }
        return List.of(new SubscriptionsFeature(Map.copyOf(byField)));
    }

    /** Honours the application's subscription fields, serving no method. */
    private record SubscriptionsFeature(Map<String, McpSubscription> byField)
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
        public McpSchema.SubscriptionFilter honouredSubscriptions(
                McpRequestContext ctx, McpSchema.SubscriptionFilter requested) {
            org.restlet.ext.mcp.McpRequestContext context =
                    ctx.transportContext().get(McpProtocol.CONTEXT_KEY)
                                    instanceof org.restlet.ext.mcp.McpRequestContext restlet
                            ? restlet
                            : new org.restlet.ext.mcp.McpRequestContext(null);
            Map<String, Object> honoured = new LinkedHashMap<>();
            byField.forEach(
                    (field, subscription) -> {
                        Object asked = requested.get(field);
                        if (asked != null && !Boolean.FALSE.equals(asked)) {
                            Object value = subscription.honour().honour(context, asked);
                            if (value != null) {
                                honoured.put(field, value);
                            }
                        }
                    });
            return McpSchema.SubscriptionFilter.of(honoured);
        }
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
