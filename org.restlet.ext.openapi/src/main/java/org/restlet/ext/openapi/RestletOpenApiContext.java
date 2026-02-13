package org.restlet.ext.openapi;

import io.swagger.v3.oas.integration.GenericOpenApiContext;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import org.apache.commons.lang3.StringUtils;
import org.restlet.routing.Router;

class RestletOpenApiContext extends GenericOpenApiContext<RestletOpenApiContext> implements OpenApiContext {
    private final Router router;

    RestletOpenApiContext(Router router) {
        this.router = router;
    }

    @Override
    protected OpenApiReader buildReader(OpenAPIConfiguration openApiConfiguration) throws Exception {
        OpenApiReader reader;

        if (StringUtils.isNotBlank(openApiConfiguration.getReaderClass())) {
            Class<?> cls = getClass().getClassLoader().loadClass(openApiConfiguration.getReaderClass());
            reader = (OpenApiReader) cls.getDeclaredConstructor().newInstance();
        } else {
            reader = new RestletOpenApiReader();
        }

        if (reader instanceof RestletOpenApiReader restletOpenApiReader) {
            restletOpenApiReader.setRouter(router);
        }

        reader.setConfiguration(openApiConfiguration);
        return reader;
    }
}
