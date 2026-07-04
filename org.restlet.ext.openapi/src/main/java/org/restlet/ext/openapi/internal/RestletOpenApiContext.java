/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.internal;

import io.swagger.v3.oas.integration.GenericOpenApiContext;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import org.restlet.engine.util.StringUtils;
import org.restlet.routing.Router;

public class RestletOpenApiContext extends GenericOpenApiContext<RestletOpenApiContext> {
    private final Router router;

    public RestletOpenApiContext(Router router) {
        this.router = router;
    }

    @Override
    protected OpenApiReader buildReader(OpenAPIConfiguration openApiConfiguration)
            throws Exception {
        final OpenApiReader reader;

        if (StringUtils.isNullOrEmpty(openApiConfiguration.getReaderClass())) {
            reader = new RestletOpenApiReader();
        } else {
            Class<?> cls =
                    getClass().getClassLoader().loadClass(openApiConfiguration.getReaderClass());
            reader = (OpenApiReader) cls.getDeclaredConstructor().newInstance();
        }

        if (reader instanceof RestletOpenApiReader restletOpenApiReader) {
            restletOpenApiReader.setRouter(router);
        }

        reader.setConfiguration(openApiConfiguration);
        return reader;
    }
}
