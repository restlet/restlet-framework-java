/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.internal;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.representation.Variant;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.service.MetadataService;

public class RestletOpenApiReader implements OpenApiReader {

    private final MetadataService metadataService = new MetadataService();

    private Router router;

    private List<ChallengeAuthenticator> authenticators = List.of();

    private OpenAPIConfiguration config;

    private final Paths paths = new Paths();

    private final OpenAPI openApi = new OpenAPI();

    private final Components components = new Components();

    public void setRouter(Router router) {
        this.router = router;
    }

    public void setAuthenticators(List<ChallengeAuthenticator> authenticators) {
        this.authenticators = authenticators == null ? List.of() : authenticators;
    }

    @Override
    public void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        this.config = openApiConfiguration;
    }

    @Override
    public OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        if (config == null) {
            throw new IllegalStateException(
                    "configuration must be set before processing OpenAPI definition");
        }

        if (router == null) {
            throw new IllegalStateException(
                    "router must be set before processing OpenAPI definition");
        }

        return processRouter(router);
    }

    private OpenAPI processRouter(Router router) {
        OpenAPIDefinition openAPIDefinitionAnnotation =
                ReflectionUtils.getAnnotation(
                        router.getApplication().getClass(), OpenAPIDefinition.class);

        if (openAPIDefinitionAnnotation != null) {
            OpenApiAnnotationProcessor.documentOpenApiDefinition(
                    openApi, openAPIDefinitionAnnotation);
        }

        completeOpenApiInfo(router);

        processRoutes(router, "", authenticators);

        openApi.setComponents(components);
        openApi.setOpenapi("3.1.0");
        openApi.setSpecVersion(SpecVersion.V31);

        return openApi;
    }

    private void processRoutes(
            Router router, String pathPrefix, List<ChallengeAuthenticator> activeAuthenticators) {
        List<Route> allRoutes = new ArrayList<>(router.getRoutes());
        if (router.getDefaultRoute() != null) {
            allRoutes.add(router.getDefaultRoute());
        }

        for (Route route : allRoutes) {
            processRoute(route, pathPrefix, activeAuthenticators);
        }
    }

    private void processRoute(
            Route route, String pathPrefix, List<ChallengeAuthenticator> activeAuthenticators) {
        if (route instanceof TemplateRoute templateRoute) {
            String path = pathPrefix + templateRoute.getTemplate().getPattern();

            List<ChallengeAuthenticator> branchAuthenticators =
                    new ArrayList<>(activeAuthenticators);
            Restlet next = unwrapAuthenticators(route.getNext(), branchAuthenticators);

            if (next instanceof Finder finder) {
                ServerResource serverResource = finder.find(null, null);

                if (serverResource != null) {
                    List<String> pathVariableNames = templateRoute.getTemplate().getVariableNames();

                    processServerResource(
                            serverResource, path, pathVariableNames, branchAuthenticators);
                }
            } else if (next instanceof Router childRouter) {
                processRoutes(childRouter, path, branchAuthenticators);
            }
        } else {
            Context.getCurrentLogger().info("Route type ignored: " + route.getClass());
        }
    }

    /**
     * Unwraps {@link ChallengeAuthenticator}s found on the way to the route's actual target (a
     * {@link Finder} or a child {@link Router}), collecting them along the way so that the
     * operations they guard can be documented accordingly.
     *
     * @param current The current Restlet to inspect.
     * @param collected The list of authenticators found so far, to be completed.
     * @return The first non-authenticator Restlet found.
     */
    private Restlet unwrapAuthenticators(Restlet current, List<ChallengeAuthenticator> collected) {
        if (current instanceof ChallengeAuthenticator challengeAuthenticator) {
            collected.add(challengeAuthenticator);
            return unwrapAuthenticators(challengeAuthenticator.getNext(), collected);
        }

        return current;
    }

    private void processServerResource(
            ServerResource serverResource,
            String operationPath,
            List<String> pathVariableNames,
            List<ChallengeAuthenticator> activeAuthenticators) {
        List<AnnotationInfo> annotations =
                serverResource.isAnnotated()
                        ? AnnotationUtils.getInstance().getAnnotations(serverResource.getClass())
                        : null;

        if (annotations == null) {
            return;
        }

        for (AnnotationInfo annotationInfo : annotations) {
            if (annotationInfo instanceof MethodAnnotationInfo methodAnnotationInfo) {

                Operation operation = buildOperationFromRestletMethod(methodAnnotationInfo);

                completePathParameters(operation, pathVariableNames);
                completeOperation(serverResource, operation, methodAnnotationInfo);
                applySecurity(operation, activeAuthenticators);

                PathItem pathItem =
                        Optional.ofNullable(openApi.getPaths())
                                .map(openApiPaths -> openApiPaths.get(operationPath))
                                .orElseGet(PathItem::new);

                PathItems.setOperation(
                        pathItem, methodAnnotationInfo.getRestletMethod(), operation);

                paths.addPathItem(operationPath, pathItem);
                if (openApi.getPaths() != null) {
                    this.paths.putAll(openApi.getPaths());
                }

                openApi.setPaths(this.paths);
            }
        }
    }

    /**
     * Documents every {@link ChallengeAuthenticator} guarding this operation: registers a {@link
     * SecurityScheme} for each of them, and requires the mandatory ones (those for which {@link
     * ChallengeAuthenticator#isOptional()} is {@code false}) on the operation.
     */
    private void applySecurity(
            Operation operation, List<ChallengeAuthenticator> activeAuthenticators) {
        if (activeAuthenticators.isEmpty()) {
            return;
        }

        SecurityRequirement mandatoryRequirement = new SecurityRequirement();

        for (ChallengeAuthenticator authenticator : activeAuthenticators) {
            ChallengeScheme scheme = authenticator.getScheme();

            components.addSecuritySchemes(scheme.getName(), toSecurityScheme(scheme));

            if (!authenticator.isOptional()) {
                mandatoryRequirement.addList(scheme.getName());
            }
        }

        if (!mandatoryRequirement.isEmpty()) {
            operation.setSecurity(List.of(mandatoryRequirement));
        }
    }

    private SecurityScheme toSecurityScheme(ChallengeScheme scheme) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(scheme.getTechnicalName().toLowerCase());
    }

    private void completeOpenApiInfo(Router router) {
        var applicationClassName = router.getApplication().getClass().getSimpleName();

        var applicationName =
                applicationClassName.endsWith("Application")
                        ? applicationClassName.substring(
                                0, applicationClassName.length() - "Application".length())
                        : applicationClassName;

        var defaultTitle = applicationName + " REST API";

        if (openApi.getInfo() == null) {
            openApi.setInfo(new Info().title(defaultTitle).version("1.0.0"));
        } else if (openApi.getInfo().getTitle() == null) {
            openApi.getInfo().setTitle(defaultTitle);
        } else if (openApi.getInfo().getVersion() == null) {
            openApi.getInfo().setVersion("1.0.0");
        }
    }

    private void completePathParameters(Operation operation, List<String> pathVariableNames) {
        if (pathVariableNames != null) {
            for (String pathVariableName : pathVariableNames) {
                operation.addParametersItem(
                        new io.swagger.v3.oas.models.parameters.Parameter()
                                .name(pathVariableName)
                                .in("path")
                                .required(true)
                                .schema(new Schema<>().type("string")));
            }
        }
    }

    private Operation buildOperationFromRestletMethod(
            final MethodAnnotationInfo methodAnnotationInfo) {
        Operation operation = new Operation();
        operation.setOperationId(methodAnnotationInfo.getJavaMethod().getName());
        return operation;
    }

    private void completeOperation(
            final ServerResource serverResource,
            final Operation operation,
            MethodAnnotationInfo methodAnnotationInfo) {
        var methodOperationAnnotation =
                ReflectionUtils.getAnnotation(
                        methodAnnotationInfo.getJavaMethod(),
                        io.swagger.v3.oas.annotations.Operation.class);

        if (methodOperationAnnotation != null) {
            OpenApiAnnotationProcessor.documentOperation(operation, methodOperationAnnotation);
        }

        completeOperationInput(serverResource, operation, methodAnnotationInfo);
        completeOperationSuccessfulOutput(serverResource, operation, methodAnnotationInfo);
    }

    private void completeOperationInput(
            ServerResource serverResource,
            Operation operation,
            MethodAnnotationInfo methodAnnotationInfo) {
        Type[] parameterTypes = methodAnnotationInfo.getJavaMethod().getGenericParameterTypes();
        if (parameterTypes.length == 0) {
            return;
        }

        Type firstParameterType = parameterTypes[0];

        List<Variant> requestVariants =
                methodAnnotationInfo.getRequestVariants(
                        metadataService, serverResource.getConverterService());

        if (requestVariants == null || requestVariants.isEmpty()) {
            return;
        }

        Variant firstVariant = requestVariants.getFirst();

        processTypeToContent(firstParameterType, List.of(firstVariant))
                .ifPresent(
                        content ->
                                operation.requestBody(
                                        new io.swagger.v3.oas.models.parameters.RequestBody()
                                                .content(content)));
    }

    private void completeOperationSuccessfulOutput(
            ServerResource serverResource,
            Operation operation,
            MethodAnnotationInfo methodAnnotationInfo) {
        List<Variant> responseVariants =
                methodAnnotationInfo.getResponseVariants(
                        metadataService, serverResource.getConverterService());

        Type javaMethodReturnType = methodAnnotationInfo.getJavaMethod().getGenericReturnType();

        if (responseVariants == null || responseVariants.isEmpty()) {
            var hasResponsesDefined =
                    operation.getResponses() != null && !operation.getResponses().isEmpty();

            if (!hasResponsesDefined) {
                Operations.addApiResponse(
                        operation, "200", new ApiResponse().description("Success"));
            }
        } else {
            processMethodReturnType(operation, javaMethodReturnType, responseVariants);
        }
    }

    private void processMethodReturnType(
            Operation operation, Type returnType, List<Variant> responseVariants) {
        Variant firstVariant = responseVariants.getFirst();

        processTypeToContent(returnType, List.of(firstVariant))
                .ifPresent(
                        content ->
                                Operations.addApiResponse(
                                        operation,
                                        "200",
                                        new ApiResponse().content(content).description("Success")));
    }

    private Optional<Content> processTypeToContent(Type type, List<Variant> variants) {
        ResolvedSchema resolvedSchema =
                ModelConverters.getInstance(config.toConfiguration())
                        .resolveAsResolvedSchema(
                                new AnnotatedType(type).resolveAsRef(true).components(components));

        if (resolvedSchema.schema == null) {
            return Optional.empty();
        }

        Content content = new Content();
        MediaType mediaType = new MediaType().schema(resolvedSchema.schema);

        for (Variant variant : variants) {
            if (variant.getMediaType() == null) {
                Context.getCurrentLogger().warning(() -> "Variant has no media type: " + variant);
                continue;
            }

            content.addMediaType(variant.getMediaType().toString(), mediaType);
        }

        @SuppressWarnings("rawtypes") // Imposed by the ModelConverters API
        Map<String, Schema> schemaMap = resolvedSchema.referencedSchemas;
        if (schemaMap != null) {
            schemaMap.forEach(components::addSchemas);
        }

        return Optional.of(content);
    }
}
