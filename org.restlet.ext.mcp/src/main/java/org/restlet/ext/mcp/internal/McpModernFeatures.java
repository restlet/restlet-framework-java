/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The features of the MCP {@code 2026-07-28} server, built from the adapter's registrations. The
 * {@code 2025-11-25} specifications are bridged: their definitions and results are converted
 * between the two SDK schemas through JSON, and their handlers receive the same transport context
 * as on the stateless server. Modern tools are served as registered. List filters apply to every
 * listed tool.
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
     */
    public record Registrations(
            List<McpStatelessServerFeatures.AsyncToolSpecification> tools,
            List<McpStatelessServerFeatures.AsyncResourceSpecification> resources,
            List<McpStatelessServerFeatures.AsyncResourceTemplateSpecification> resourceTemplates,
            List<McpStatelessServerFeatures.AsyncPromptSpecification> prompts,
            List<McpAsyncListFilter<io.modelcontextprotocol.spec.McpSchema.Tool>> toolFilters,
            List<McpAsyncTool> modernTools) {}

    /**
     * Builds the features for registrations, one per kind present.
     *
     * @param mapper The JSON mapper converting between the two schemas.
     * @param registrations The adapter's registrations.
     * @return The features.
     * @throws IllegalArgumentException If two tools share a name.
     */
    public static List<McpFeature> of(McpJsonMapper mapper, Registrations registrations) {
        List<McpFeature> features = new ArrayList<>();
        if (!registrations.tools().isEmpty() || !registrations.modernTools().isEmpty()) {
            features.add(ToolsFeature.ofAsync(new Tools(mapper, registrations)));
        }
        if (!registrations.resources().isEmpty() || !registrations.resourceTemplates().isEmpty()) {
            features.add(ResourcesFeature.ofAsync(new Resources(mapper, registrations)));
        }
        if (!registrations.prompts().isEmpty()) {
            features.add(PromptsFeature.ofAsync(new Prompts(mapper, registrations.prompts())));
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

    /** The bridged resources: exact URIs first, then templates, in registration order. */
    private static final class Resources implements McpAsyncResourceRepository {
        private final McpJsonMapper mapper;
        private final List<McpStatelessServerFeatures.AsyncResourceSpecification> exact;
        private final List<McpStatelessServerFeatures.AsyncResourceTemplateSpecification> templates;

        private Resources(McpJsonMapper mapper, Registrations registrations) {
            this.mapper = mapper;
            this.exact = List.copyOf(registrations.resources());
            this.templates = List.copyOf(registrations.resourceTemplates());
        }

        @Override
        public Mono<ResourcesPage> list(McpRequestContext ctx, String cursor) {
            return Mono.fromSupplier(
                    () ->
                            ResourcesPage.of(
                                    exact.stream()
                                            .map(
                                                    spec ->
                                                            mapper.convertValue(
                                                                    spec.resource(),
                                                                    McpSchema.Resource.class))
                                            .toList()));
        }

        @Override
        public Mono<ResourceTemplatesPage> listTemplates(McpRequestContext ctx, String cursor) {
            return Mono.fromSupplier(
                    () ->
                            ResourceTemplatesPage.of(
                                    templates.stream()
                                            .map(
                                                    spec ->
                                                            mapper.convertValue(
                                                                    spec.resourceTemplate(),
                                                                    McpSchema.ResourceTemplate
                                                                            .class))
                                            .toList()));
        }

        @Override
        public Mono<McpAsyncResource> find(McpRequestContext ctx, String uri) {
            for (McpStatelessServerFeatures.AsyncResourceSpecification spec : exact) {
                if (spec.resource().uri().equals(uri)) {
                    return Mono.just(read(spec.readHandler()));
                }
            }
            for (McpStatelessServerFeatures.AsyncResourceTemplateSpecification spec : templates) {
                if (new DefaultMcpUriTemplateManager(spec.resourceTemplate().uriTemplate())
                        .matches(uri)) {
                    return Mono.just(read(spec.readHandler()));
                }
            }
            return Mono.empty();
        }

        private McpAsyncResource read(
                java.util.function.BiFunction<
                                io.modelcontextprotocol.common.McpTransportContext,
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
    }

    /** The bridged prompts, indexed by name. */
    private static final class Prompts implements McpAsyncPromptRepository {
        private final McpJsonMapper mapper;
        private final Map<String, McpStatelessServerFeatures.AsyncPromptSpecification> byName =
                new LinkedHashMap<>();

        private Prompts(
                McpJsonMapper mapper,
                List<McpStatelessServerFeatures.AsyncPromptSpecification> prompts) {
            this.mapper = mapper;
            for (McpStatelessServerFeatures.AsyncPromptSpecification spec : prompts) {
                byName.put(spec.prompt().name(), spec);
            }
        }

        @Override
        public Mono<PromptsPage> list(McpRequestContext ctx, String cursor) {
            return Mono.fromSupplier(
                    () ->
                            PromptsPage.of(
                                    byName.values().stream()
                                            .map(
                                                    spec ->
                                                            mapper.convertValue(
                                                                    spec.prompt(),
                                                                    McpSchema.Prompt.class))
                                            .toList()));
        }

        @Override
        public Mono<McpAsyncPrompt> find(McpRequestContext ctx, String name) {
            McpStatelessServerFeatures.AsyncPromptSpecification spec = byName.get(name);
            if (spec == null) {
                return Mono.empty();
            }
            return Mono.just(
                    McpAsyncPrompt.of(
                            mapper.convertValue(spec.prompt(), McpSchema.Prompt.class),
                            (ctx2, request) ->
                                    spec.promptHandler()
                                            .apply(
                                                    ctx2.transportContext(),
                                                    mapper.convertValue(
                                                            request,
                                                            io.modelcontextprotocol.spec.McpSchema
                                                                    .GetPromptRequest.class))
                                            .map(
                                                    result ->
                                                            McpAsyncResponse
                                                                    .<McpSchema.GetPromptOutcome>
                                                                            result(
                                                                                    mapper
                                                                                            .convertValue(
                                                                                                    result,
                                                                                                    McpSchema
                                                                                                            .GetPromptResult
                                                                                                            .class)))));
        }
    }
}
