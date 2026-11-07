/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
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
import io.modelcontextprotocol.server.McpAsyncListFilter;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.restlet.ext.mcp.McpModernResource;
import org.restlet.ext.mcp.McpModernResourceTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The features of the MCP {@code 2026-07-28} server, built from the adapter's registrations. The
 * {@code 2025-11-25} specifications are bridged: their definitions and results are converted
 * between the two SDK schemas through JSON, and their handlers receive the same transport context
 * as on the stateless server. Modern registrations are served as registered. List filters apply to
 * every listed tool. A name, URI or URI template may only be registered once across both kinds.
 */
public final class McpModernFeatures {

    private McpModernFeatures() {}

    /**
     * The registrations of an adapter.
     *
     * @param tools The {@code 2025-11-25} tools.
     * @param resources The {@code 2025-11-25} resources.
     * @param resourceTemplates The {@code 2025-11-25} resource templates.
     * @param prompts The {@code 2025-11-25} prompts.
     * @param toolFilters The tool list filters.
     * @param modernTools The {@code 2026-07-28} tools.
     * @param modernResources The {@code 2026-07-28} resources.
     * @param modernResourceTemplates The {@code 2026-07-28} resource templates.
     * @param modernPrompts The {@code 2026-07-28} prompts.
     */
    public record Registrations(
            List<McpStatelessServerFeatures.AsyncToolSpecification> tools,
            List<McpStatelessServerFeatures.AsyncResourceSpecification> resources,
            List<McpStatelessServerFeatures.AsyncResourceTemplateSpecification> resourceTemplates,
            List<McpStatelessServerFeatures.AsyncPromptSpecification> prompts,
            List<McpAsyncListFilter<io.modelcontextprotocol.spec.McpSchema.Tool>> toolFilters,
            List<McpAsyncTool> modernTools,
            List<McpModernResource> modernResources,
            List<McpModernResourceTemplate> modernResourceTemplates,
            List<McpAsyncPrompt> modernPrompts) {}

    /**
     * Builds the features for registrations, one per kind present.
     *
     * @param mapper The JSON mapper converting between the two schemas.
     * @param registrations The adapter's registrations.
     * @return The features.
     * @throws IllegalArgumentException If a name, URI or URI template is registered twice.
     */
    public static List<McpFeature> of(McpJsonMapper mapper, Registrations registrations) {
        List<McpFeature> features = new ArrayList<>();
        if (!registrations.tools().isEmpty() || !registrations.modernTools().isEmpty()) {
            features.add(ToolsFeature.ofAsync(new Tools(mapper, registrations)));
        }
        if (!registrations.resources().isEmpty()
                || !registrations.resourceTemplates().isEmpty()
                || !registrations.modernResources().isEmpty()
                || !registrations.modernResourceTemplates().isEmpty()) {
            features.add(ResourcesFeature.ofAsync(new Resources(mapper, registrations)));
        }
        if (!registrations.prompts().isEmpty() || !registrations.modernPrompts().isEmpty()) {
            features.add(PromptsFeature.ofAsync(new Prompts(mapper, registrations)));
        }
        return features;
    }

    /** The tools, bridged and modern, indexed by name. */
    private static final class Tools implements McpAsyncToolRepository {
        private final McpJsonMapper mapper;
        private final Map<String, McpAsyncTool> byName = new LinkedHashMap<>();
        private final McpAsyncListFilter<io.modelcontextprotocol.spec.McpSchema.Tool> filter;

        private Tools(McpJsonMapper mapper, Registrations registrations) {
            this.mapper = mapper;
            this.filter = McpAsyncListFilter.and(List.copyOf(registrations.toolFilters()));
            for (McpStatelessServerFeatures.AsyncToolSpecification spec : registrations.tools()) {
                add(bridge(spec));
            }
            for (McpAsyncTool tool : registrations.modernTools()) {
                add(tool);
            }
        }

        private void add(McpAsyncTool tool) {
            if (byName.putIfAbsent(tool.definition().name(), tool) != null) {
                throw new IllegalArgumentException(
                        "Duplicate MCP tool name: " + tool.definition().name());
            }
        }

        private McpAsyncTool bridge(McpStatelessServerFeatures.AsyncToolSpecification spec) {
            return McpAsyncTool.of(
                    mapper.convertValue(spec.tool(), McpSchema.Tool.class),
                    (ctx, request) ->
                            spec.callHandler()
                                    .apply(
                                            ctx.transportContext(),
                                            mapper.convertValue(
                                                    request,
                                                    io.modelcontextprotocol.spec.McpSchema
                                                            .CallToolRequest.class))
                                    .map(
                                            result ->
                                                    McpAsyncResponse
                                                            .<McpSchema.CallToolOutcome>result(
                                                                    mapper.convertValue(
                                                                            result,
                                                                            McpSchema.CallToolResult
                                                                                    .class))));
        }

        @Override
        public Mono<ToolsPage> list(McpRequestContext ctx, String cursor) {
            return Flux.fromIterable(byName.values())
                    .map(McpAsyncTool::definition)
                    .filterWhen(
                            tool ->
                                    filter.isVisible(
                                            ctx.transportContext(),
                                            mapper.convertValue(
                                                    tool,
                                                    io.modelcontextprotocol.spec.McpSchema.Tool
                                                            .class)))
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
     * The resources, bridged and modern: exact URIs first, then templates, in registration order.
     * Subscriptions are supported: the adapter publishes resource updates through its change feed.
     */
    private static final class Resources implements McpAsyncResourceRepository {
        private final McpJsonMapper mapper;
        private final List<McpSchema.Resource> listed = new ArrayList<>();
        private final Map<String, McpAsyncResource> byUri = new LinkedHashMap<>();
        private final List<Template> templates = new ArrayList<>();

        private Resources(McpJsonMapper mapper, Registrations registrations) {
            this.mapper = mapper;
            for (McpStatelessServerFeatures.AsyncResourceSpecification spec :
                    registrations.resources()) {
                add(
                        mapper.convertValue(spec.resource(), McpSchema.Resource.class),
                        read(spec.readHandler()));
            }
            for (McpModernResource resource : registrations.modernResources()) {
                add(resource.definition(), resource.resource());
            }
            for (McpStatelessServerFeatures.AsyncResourceTemplateSpecification spec :
                    registrations.resourceTemplates()) {
                add(
                        mapper.convertValue(
                                spec.resourceTemplate(), McpSchema.ResourceTemplate.class),
                        read(spec.readHandler()));
            }
            for (McpModernResourceTemplate template : registrations.modernResourceTemplates()) {
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

        private McpAsyncResource read(
                BiFunction<
                                McpTransportContext,
                                io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest,
                                Mono<io.modelcontextprotocol.spec.McpSchema.ReadResourceResult>>
                        handler) {
            return McpAsyncResource.of(
                    (ctx, request) ->
                            handler.apply(
                                            ctx.transportContext(),
                                            mapper.convertValue(
                                                    request,
                                                    io.modelcontextprotocol.spec.McpSchema
                                                            .ReadResourceRequest.class))
                                    .map(
                                            result ->
                                                    McpAsyncResponse
                                                            .<McpSchema.ReadResourceOutcome>result(
                                                                    mapper.convertValue(
                                                                            result,
                                                                            McpSchema
                                                                                    .ReadResourceResult
                                                                                    .class))));
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

    /** The prompts, bridged and modern, indexed by name. */
    private static final class Prompts implements McpAsyncPromptRepository {
        private final Map<String, McpAsyncPrompt> byName = new LinkedHashMap<>();

        private Prompts(McpJsonMapper mapper, Registrations registrations) {
            for (McpStatelessServerFeatures.AsyncPromptSpecification spec :
                    registrations.prompts()) {
                add(bridge(mapper, spec));
            }
            for (McpAsyncPrompt prompt : registrations.modernPrompts()) {
                add(prompt);
            }
        }

        private static McpAsyncPrompt bridge(
                McpJsonMapper mapper, McpStatelessServerFeatures.AsyncPromptSpecification spec) {
            return McpAsyncPrompt.of(
                    mapper.convertValue(spec.prompt(), McpSchema.Prompt.class),
                    (ctx, request) ->
                            spec.promptHandler()
                                    .apply(
                                            ctx.transportContext(),
                                            mapper.convertValue(
                                                    request,
                                                    io.modelcontextprotocol.spec.McpSchema
                                                            .GetPromptRequest.class))
                                    .map(
                                            result ->
                                                    McpAsyncResponse
                                                            .<McpSchema.GetPromptOutcome>result(
                                                                    mapper.convertValue(
                                                                            result,
                                                                            McpSchema
                                                                                    .GetPromptResult
                                                                                    .class))));
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
