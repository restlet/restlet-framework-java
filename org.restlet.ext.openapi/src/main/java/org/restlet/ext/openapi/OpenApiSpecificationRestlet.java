package org.restlet.ext.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.routing.Router;

import java.util.List;

class OpenApiSpecificationRestlet extends Restlet {
    private static final VariantInfo VARIANT_JSON = new VariantInfo(
        MediaType.APPLICATION_JSON
    );

    private static final VariantInfo VARIANT_APPLICATION_YAML = new VariantInfo(
        MediaType.APPLICATION_YAML
    );

    private final Router router;

    OpenApiSpecificationRestlet(Router router) {
        super(router.getContext());
        this.router = router;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            response.setEntity(getOpenApiDefinitionAsRepresentation(request));
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }
    }

    private Representation getOpenApiDefinitionAsRepresentation(Request request) {
        List<Variant> allowedVariants = List.of(VARIANT_APPLICATION_YAML, VARIANT_JSON);

        Variant preferredVariant = getApplication()
            .getConnegService()
            .getPreferredVariant(
                allowedVariants,
                request,
                getApplication().getMetadataService()
            );

        OpenAPIConfiguration oasConfig = new SwaggerConfiguration()
            .prettyPrint(true);

        try {
            var context = new RestletOpenApiContextBuilder()
                .router(router)
                .openApiConfiguration(oasConfig)
                .buildContext(true);

            var openApiRead = context.read();

            if (VARIANT_JSON.isCompatible(preferredVariant)) {
                var openApiAsJson = context.getOutputJsonMapper()
                    .writer(new DefaultPrettyPrinter())
                    .writeValueAsString(openApiRead);

                return new StringRepresentation(openApiAsJson, MediaType.APPLICATION_JSON);
            } else {
                var openApiAsYaml = context.getOutputYamlMapper()
                    .writer(new DefaultPrettyPrinter())
                    .writeValueAsString(openApiRead);

                return new StringRepresentation(openApiAsYaml, MediaType.APPLICATION_YAML);
            }
        } catch (OpenApiConfigurationException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    void attach(Router router, String path) {
        router.attach(path, this);
    }
}
