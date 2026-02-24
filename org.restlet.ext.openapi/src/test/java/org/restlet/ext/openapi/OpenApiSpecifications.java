/**
 * Copyright 2005-2026 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open source license available at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.openapi;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.List;

class OpenApiSpecifications {

    @SuppressWarnings("SameParameterValue")
    static String readFromClasspath(String resourcePath) throws Exception {
        try (var inputStream = OpenApiSpecifications.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found in classpath: " + resourcePath);
            }
            return new String(inputStream.readAllBytes());
        }
    }

    static ValidationResult validate(String yamlSpecification) {
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(yamlSpecification, null, null);

        if (result.getMessages().isEmpty()) {
            return new ValidationResult.Valid();
        } else {
            return new ValidationResult.Invalid(result.getMessages());
        }
    }

    sealed interface ValidationResult {
        record Valid() implements ValidationResult {
        }

        record Invalid(List<String> errors) implements ValidationResult {
        }
    }
}
