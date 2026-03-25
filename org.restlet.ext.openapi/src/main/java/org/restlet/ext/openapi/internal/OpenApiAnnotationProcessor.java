/*
 *  Copyright 2005-2026 Qlik
 *
 *  The contents of this file is subject to the terms of the Apache 2.0 open
 *  source license available at http://www.opensource.org/licenses/apache-2.0
 *
 *  Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.openapi.internal;

import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Optional;

public class OpenApiAnnotationProcessor {

    public static void documentOpenApiDefinition(
        OpenAPI openAPIDefinition,
        OpenAPIDefinition openAPIDefinitionAnnotation
    ) {
        if (openAPIDefinitionAnnotation.info() != null) {
            AnnotationsUtils.getInfo(openAPIDefinitionAnnotation.info())
                .ifPresent(openAPIDefinition::setInfo);
        }
    }

    public static void documentOperation(
        Operation operation,
        io.swagger.v3.oas.annotations.Operation operationAnnotation
    ) {
        if (operationAnnotation.summary() != null && !operationAnnotation.summary().isEmpty()) {
            operation.setSummary(operationAnnotation.summary());
        }

        if (operationAnnotation.description() != null && !operationAnnotation.description().isEmpty()) {
            operation.setDescription(operationAnnotation.description());
        }

        if (operationAnnotation.parameters() != null) {
            for (io.swagger.v3.oas.annotations.Parameter parameterAnnotation : operationAnnotation.parameters()) {
                resolveParameterFromAnnotation(parameterAnnotation)
                    .ifPresent(operation::addParametersItem);
            }
        }

        if (operationAnnotation.responses() != null) {
            for (ApiResponse apiResponseAnnotation : operationAnnotation.responses()) {
                documentOperationResponse(operation, apiResponseAnnotation);
            }
        }
    }

    public static void documentOperationResponse(
        Operation operation,
        ApiResponse apiResponseAnnotation
    ) {
        var defaultDescription = apiResponseAnnotation.responseCode() + " response";

        io.swagger.v3.oas.models.responses.ApiResponse apiResponse =
            new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(apiResponseAnnotation.description() == null
                    ? defaultDescription
                    : apiResponseAnnotation.description().isEmpty()
                        ? defaultDescription
                        : apiResponseAnnotation.description()
                );

        if (!isContentEmpty(apiResponseAnnotation)) {
            AnnotationsUtils.getContent(
                apiResponseAnnotation.content(),
                null,
                null,
                null,
                null,
                null
            ).ifPresent(apiResponse::content);
        }

        AnnotationsUtils.getHeaders(apiResponseAnnotation.headers(), null)
            .ifPresent(apiResponse::headers);

        Operations.addApiResponse(operation, apiResponseAnnotation.responseCode(), apiResponse);
    }

    private static Optional<Parameter> resolveParameterFromAnnotation(io.swagger.v3.oas.annotations.Parameter parameterAnnotation) {
        return switch (parameterAnnotation.in()) {
            case DEFAULT, PATH -> Optional.empty();
            case COOKIE, HEADER, QUERY -> {
                Parameter parameter = new Parameter()
                    .name(parameterAnnotation.name())
                    .description(parameterAnnotation.description() == null
                        ? null
                        : parameterAnnotation.description().isEmpty() ? null : parameterAnnotation.description()
                    )
                    .in(parameterAnnotation.in().name().toLowerCase())
                    .required(parameterAnnotation.required());

                AnnotationsUtils.getSchemaFromAnnotation(parameterAnnotation.schema(), null)
                    .ifPresent(parameter::schema);

                yield Optional.of(parameter);
            }
        };
    }

    private static boolean isContentEmpty(ApiResponse apiResponseAnnotation) {
        if (apiResponseAnnotation.content() == null || apiResponseAnnotation.content().length == 0) {
            return true;
        }

        Content content = apiResponseAnnotation.content()[0];

        return content.mediaType().isEmpty() &&
            content.schema().implementation() == Void.class &&
            content.schema().ref().isEmpty();
    }
}
