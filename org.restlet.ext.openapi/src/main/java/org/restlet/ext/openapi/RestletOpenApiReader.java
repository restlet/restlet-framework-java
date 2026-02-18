package org.restlet.ext.openapi;

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
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.representation.Variant;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.service.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class RestletOpenApiReader implements OpenApiReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestletOpenApiReader.class);

    private final MetadataService metadataService = new MetadataService();

    private Router router;

    private OpenAPIConfiguration config;

    private Paths paths = new Paths();

    private OpenAPI openAPI = new OpenAPI();

    private Components components = new Components();

    public void setRouter(Router router) {
        this.router = router;
    }

    @Override
    public void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        this.config = openApiConfiguration;
    }

    @Override
    public OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        return processRouter(router);
    }

    private OpenAPI processRouter(Router router) {
        OpenAPIDefinition openAPIDefinitionAnnotation = ReflectionUtils.getAnnotation(
            router.getApplication().getClass(),
            OpenAPIDefinition.class
        );

        if (openAPIDefinitionAnnotation != null) {
            OpenApiAnnotationProcessor.documentOpenApiDefinition(openAPI, openAPIDefinitionAnnotation);
        }

        completeOpenApiInfo(router);

        List<Route> allRoutes = new ArrayList<>(router.getRoutes());
        if (router.getDefaultRoute() != null) {
            allRoutes.add(router.getDefaultRoute());
        }

        for (Route route : allRoutes) {
            processRoute(route);
        }

        openAPI.setComponents(components);
        openAPI.setOpenapi("3.1.0");
        openAPI.setSpecVersion(SpecVersion.V31);

        return openAPI;
    }

    private void processRoute(Route route) {
        if (route instanceof TemplateRoute templateRoute) {
            String path = templateRoute.getTemplate().getPattern();

            if (route.getNext() instanceof Finder finder) {
                ServerResource serverResource = finder.find(null, null);

                if (serverResource != null) {
                    List<String> pathVariableNames = templateRoute.getTemplate().getVariableNames();

                    processServerResource(serverResource, path, pathVariableNames);
                }
            }
        } else {
            LOGGER.info("Route type ignored: {}", route.getClass());
        }
    }

    private void processServerResource(
        ServerResource serverResource,
        String operationPath,
        List<String> pathVariableNames
    ) {
        List<AnnotationInfo> annotations = serverResource.isAnnotated()
            ? AnnotationUtils.getInstance().getAnnotations(serverResource.getClass())
            : null;

        if (annotations == null) {
            return;
        }

        for (AnnotationInfo annotationInfo : annotations) {
            if (annotationInfo instanceof MethodAnnotationInfo methodAnnotationInfo) {
                PathItem pathItem = Optional.ofNullable(openAPI.getPaths())
                    .map(paths -> paths.get(operationPath))
                    .orElseGet(PathItem::new);

                Operation operation = buildOperationFromRestletMethod(methodAnnotationInfo);

                completePathParameters(operation, pathVariableNames);
                completeOperation(serverResource, operation, methodAnnotationInfo);

                PathItems.setOperation(pathItem, methodAnnotationInfo.getRestletMethod(), operation);

                paths.addPathItem(operationPath, pathItem);
                if (openAPI.getPaths() != null) {
                    this.paths.putAll(openAPI.getPaths());
                }

                openAPI.setPaths(this.paths);
            }
        }
    }

    private void completeOpenApiInfo(Router router) {
        var applicationClassName = router.getApplication().getClass().getSimpleName();

        var applicationName = applicationClassName.endsWith("Application")
            ? applicationClassName.substring(0, applicationClassName.length() - "Application".length())
            : applicationClassName;

        var defaultTitle = applicationName + " REST API";

        if (openAPI.getInfo() == null) {
            openAPI.setInfo(new Info()
                .title(defaultTitle)
                .version("1.0.0")
            );
        } else if (openAPI.getInfo().getTitle() == null) {
            openAPI.getInfo().setTitle(defaultTitle);
        } else if (openAPI.getInfo().getVersion() == null) {
            openAPI.getInfo().setVersion("1.0.0");
        }
    }

    private void completePathParameters(
        Operation operation,
        List<String> pathVariableNames
    ) {
        if (pathVariableNames != null) {
            for (String pathVariableName : pathVariableNames) {
                operation.addParametersItem(
                    new io.swagger.v3.oas.models.parameters.Parameter()
                        .name(pathVariableName)
                        .in("path")
                        .required(true)
                );
            }
        }
    }

    private Operation buildOperationFromRestletMethod(
        final MethodAnnotationInfo methodAnnotationInfo
    ) {
        Operation operation = new Operation();
        operation.setOperationId(methodAnnotationInfo.getJavaMethod().getName());
        return operation;
    }

    private void completeOperation(
        final ServerResource serverResource,
        final Operation operation,
        MethodAnnotationInfo methodAnnotationInfo
    ) {
        try {
            var methodOperationAnnotation = ReflectionUtils.getAnnotation(
                methodAnnotationInfo.getJavaMethod(),
                io.swagger.v3.oas.annotations.Operation.class
            );

            if (methodOperationAnnotation != null) {
                OpenApiAnnotationProcessor.documentOperation(operation, methodOperationAnnotation);
            }

            completeOperationInput(serverResource, operation, methodAnnotationInfo);
            completeOperationSuccessfulOutput(serverResource, operation, methodAnnotationInfo);
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    private void completeOperationInput(
        ServerResource serverResource,
        Operation operation,
        MethodAnnotationInfo methodAnnotationInfo
    ) throws IOException {
        Type[] parameterTypes = methodAnnotationInfo.getJavaMethod().getGenericParameterTypes();
        if (parameterTypes.length == 0) {
            return;
        }

        Type firstParameterType = parameterTypes[0];

        List<Variant> requestVariants = methodAnnotationInfo.getRequestVariants(
            metadataService,
            serverResource.getConverterService()
        );

        if (requestVariants == null || requestVariants.isEmpty()) {
            return;
        }

        Variant firstVariant = requestVariants.getFirst();

        processTypeToContent(firstParameterType, List.of(firstVariant))
            .ifPresent(content -> {
                operation.requestBody(
                    new io.swagger.v3.oas.models.parameters.RequestBody()
                        .content(content)
                );
            });
    }

    private void completeOperationSuccessfulOutput(
        ServerResource serverResource,
        Operation operation,
        MethodAnnotationInfo methodAnnotationInfo
    ) throws IOException {
        List<Variant> responseVariants = methodAnnotationInfo.getResponseVariants(
            metadataService,
            serverResource.getConverterService()
        );

        Type javaMethodReturnType = methodAnnotationInfo.getJavaMethod().getGenericReturnType();

        boolean shouldIgnoreClass = methodAnnotationInfo.getJavaMethod().getReturnType() == Void.class
            || methodAnnotationInfo.getJavaMethod().getReturnType() == void.class;

        if (!shouldIgnoreClass) {
            processMethodReturnType(operation, javaMethodReturnType, responseVariants);
        }
    }

    private void processMethodReturnType(
        Operation operation,
        Type returnType,
        List<Variant> responseVariants
    ) {
        if (responseVariants == null || responseVariants.isEmpty()) {
            return;
        }

        Variant firstVariant = responseVariants.getFirst();

        processTypeToContent(returnType, List.of(firstVariant))
            .ifPresent(content -> Operations.addApiResponse(
                operation,
                "200",
                new ApiResponse()
                    .content(content)
                    .description("Success")
            ));
    }

    private Optional<Content> processTypeToContent(Type type, List<Variant> variants) {
        ResolvedSchema resolvedSchema = ModelConverters.getInstance(config.toConfiguration())
            .resolveAsResolvedSchema(
                new AnnotatedType(type)
                    .resolveAsRef(true)
                    .components(components)
            );

        if (resolvedSchema.schema == null) {
            return Optional.empty();
        }

        Content content = new Content();
        MediaType mediaType = new MediaType().schema(resolvedSchema.schema);

        for (Variant variant : variants) {
            if (variant.getMediaType() == null) {
                LOGGER.warn("Variant has no media type: {}", variant);
                continue;
            }

            content.addMediaType(variant.getMediaType().toString(), mediaType);
        }

        @SuppressWarnings("rawtypes") // Imposed by the ModelConverters API
        Map<String, Schema> schemaMap = resolvedSchema.referencedSchemas;
        if (schemaMap != null) {
            schemaMap.forEach((key, schema) -> components.addSchemas(key, schema));
        }

        return Optional.of(content);
    }
}
