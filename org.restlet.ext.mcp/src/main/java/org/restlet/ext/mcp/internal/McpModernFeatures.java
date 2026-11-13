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
import io.modelcontextprotocol.modern.server.McpFeature;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPromptRepository;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResourceRepository;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import io.modelcontextprotocol.modern.server.feature.McpAsyncToolRepository;
import io.modelcontextprotocol.modern.server.feature.PromptsFeature;
import io.modelcontextprotocol.modern.server.feature.PromptsPage;
import io.modelcontextprotocol.modern.server.feature.ResourceTemplatesPage;
import io.modelcontextprotocol.modern.server.feature.ResourcesFeature;
import io.modelcontextprotocol.modern.server.feature.ResourcesPage;
import io.modelcontextprotocol.modern.server.feature.ToolsFeature;
import io.modelcontextprotocol.modern.server.feature.ToolsPage;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.McpResource;
import org.restlet.ext.mcp.McpResourceTemplate;
import org.restlet.ext.mcp.McpToolFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The features of the MCP server, built from the adapter's registrations, on the SDK's modern
 * types. Tool filters apply to every listed tool, with the adapter's request context. A name, URI
 * or URI template may only be registered once.
 */
public final class McpModernFeatures {

    private McpModernFeatures() {}

    /**
     * The registrations of an adapter.
     *
     * @param tools The tools.
     * @param resources The resources.
     * @param resourceTemplates The resource templates.
     * @param prompts The prompts.
     * @param toolFilters The tool list filters.
     */
    public record Registrations(
            List<McpAsyncTool> tools,
            List<McpResource> resources,
            List<McpResourceTemplate> resourceTemplates,
            List<McpAsyncPrompt> prompts,
            List<McpToolFilter> toolFilters) {}

    /**
     * Builds the features for registrations, one per kind present.
     *
     * @param registrations The adapter's registrations.
     * @return The features.
     * @throws IllegalArgumentException If a name, URI or URI template is registered twice.
     */
    public static List<McpFeature> of(Registrations registrations) {
        List<McpFeature> features = new ArrayList<>();
        if (!registrations.tools().isEmpty()) {
            features.add(ToolsFeature.ofAsync(new Tools(registrations)));
        }
        if (!registrations.resources().isEmpty() || !registrations.resourceTemplates().isEmpty()) {
            features.add(ResourcesFeature.ofAsync(new Resources(registrations)));
        }
        if (!registrations.prompts().isEmpty()) {
            features.add(PromptsFeature.ofAsync(new Prompts(registrations)));
        }
        return features;
    }

    /** The adapter's context of a request, or null when the transport gave none. */
    private static org.restlet.ext.mcp.McpRequestContext context(McpRequestContext ctx) {
        return ctx.transportContext().get(McpProtocol.CONTEXT_KEY)
                        instanceof org.restlet.ext.mcp.McpRequestContext context
                ? context
                : null;
    }

    /** The tools, indexed by name. */
    private static final class Tools implements McpAsyncToolRepository {
        private final Map<String, McpAsyncTool> byName = new LinkedHashMap<>();
        private final List<McpToolFilter> filters;

        private Tools(Registrations registrations) {
            this.filters = List.copyOf(registrations.toolFilters());
            for (McpAsyncTool tool : registrations.tools()) {
                if (byName.putIfAbsent(tool.definition().name(), tool) != null) {
                    throw new IllegalArgumentException(
                            "Duplicate MCP tool name: " + tool.definition().name());
                }
            }
        }

        @Override
        public Mono<ToolsPage> list(McpRequestContext ctx, String cursor) {
            org.restlet.ext.mcp.McpRequestContext context = context(ctx);
            return Flux.fromIterable(byName.values())
                    .map(McpAsyncTool::definition)
                    .filterWhen(
                            tool ->
                                    Flux.fromIterable(filters)
                                            .concatMap(
                                                    filter ->
                                                            filter.isVisible(context, tool)
                                                                    .defaultIfEmpty(Boolean.FALSE))
                                            .all(Boolean::booleanValue))
                    .collectList()
                    .map(ToolsPage::of);
        }

        @Override
        public Mono<McpAsyncTool> find(McpRequestContext ctx, String name) {
            return Mono.justOrEmpty(byName.get(name));
        }
    }

    /** A URI template with its matcher and read handler. */
    private record Template(
            McpSchema.ResourceTemplate definition,
            DefaultMcpUriTemplateManager matcher,
            McpAsyncResource resource) {}

    /**
     * The resources: exact URIs first, then templates, in registration order. Subscriptions are
     * supported: the adapter publishes resource updates through its change feed.
     */
    private static final class Resources implements McpAsyncResourceRepository {
        private final List<McpSchema.Resource> listed = new ArrayList<>();
        private final Map<String, McpAsyncResource> byUri = new LinkedHashMap<>();
        private final List<Template> templates = new ArrayList<>();

        private Resources(Registrations registrations) {
            for (McpResource resource : registrations.resources()) {
                add(resource.definition(), resource.resource());
            }
            for (McpResourceTemplate template : registrations.resourceTemplates()) {
                add(template.definition(), template.resource());
            }
        }

        private void add(McpSchema.Resource definition, McpAsyncResource resource) {
            if (byUri.putIfAbsent(definition.uri(), resource) != null) {
                throw new IllegalArgumentException(
                        "Duplicate MCP resource URI: " + definition.uri());
            }
            listed.add(definition);
        }

        private void add(McpSchema.ResourceTemplate definition, McpAsyncResource resource) {
            for (Template template : templates) {
                if (template.definition().uriTemplate().equals(definition.uriTemplate())) {
                    throw new IllegalArgumentException(
                            "Duplicate MCP resource template: " + definition.uriTemplate());
                }
            }
            templates.add(
                    new Template(
                            definition,
                            new DefaultMcpUriTemplateManager(definition.uriTemplate()),
                            resource));
        }

        @Override
        public Mono<ResourcesPage> list(McpRequestContext ctx, String cursor) {
            return Mono.just(ResourcesPage.of(List.copyOf(listed)));
        }

        @Override
        public Mono<ResourceTemplatesPage> listTemplates(McpRequestContext ctx, String cursor) {
            return Mono.just(
                    ResourceTemplatesPage.of(
                            templates.stream().map(Template::definition).toList()));
        }

        @Override
        public Mono<McpAsyncResource> find(McpRequestContext ctx, String uri) {
            McpAsyncResource exact = byUri.get(uri);
            if (exact != null) {
                return Mono.just(exact);
            }
            for (Template template : templates) {
                if (template.matcher().matches(uri)) {
                    return Mono.just(template.resource());
                }
            }
            return Mono.empty();
        }

        @Override
        public boolean supportsSubscribe() {
            return true;
        }
    }

    /** The prompts, indexed by name. */
    private static final class Prompts implements McpAsyncPromptRepository {
        private final Map<String, McpAsyncPrompt> byName = new LinkedHashMap<>();

        private Prompts(Registrations registrations) {
            for (McpAsyncPrompt prompt : registrations.prompts()) {
                add(prompt);
            }
        }

        private void add(McpAsyncPrompt prompt) {
            if (byName.putIfAbsent(prompt.definition().name(), prompt) != null) {
                throw new IllegalArgumentException(
                        "Duplicate MCP prompt name: " + prompt.definition().name());
            }
        }

        @Override
        public Mono<PromptsPage> list(McpRequestContext ctx, String cursor) {
            return Mono.just(
                    PromptsPage.of(
                            byName.values().stream().map(McpAsyncPrompt::definition).toList()));
        }

        @Override
        public Mono<McpAsyncPrompt> find(McpRequestContext ctx, String name) {
            return Mono.justOrEmpty(byName.get(name));
        }
    }
}
