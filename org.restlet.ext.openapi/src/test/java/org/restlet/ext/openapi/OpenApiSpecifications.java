package org.restlet.ext.openapi;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.List;

class OpenApiSpecifications {

    static String readFromClasspath(String resourcePath) throws Exception {
        try (var inputStream = OpenApiSpecifications.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found in classpath: " + resourcePath);
            }
            return new String(inputStream.readAllBytes());
        }
    }

    static ValidationResult validate(String yamlSpecification) throws Exception {
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
