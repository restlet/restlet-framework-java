/**
 * Copyright 2005-2026 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open source license available at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.openapi;

import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.OpenApiContextLocator;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import org.apache.commons.lang3.StringUtils;
import org.restlet.routing.Router;

public class RestletOpenApiContextBuilder extends GenericOpenApiContextBuilder<RestletOpenApiContextBuilder> {
    private Router router;

    public RestletOpenApiContextBuilder router(Router router) {
        this.router = router;
        return this;
    }

    @Override
    public OpenApiContext buildContext(boolean init) throws OpenApiConfigurationException {
        if (StringUtils.isBlank(ctxId)) {
            ctxId = OpenApiContext.OPENAPI_CONTEXT_ID_DEFAULT;
        }

        OpenApiContext ctx = OpenApiContextLocator.getInstance().getOpenApiContext(ctxId);

        if (ctx == null) {
            OpenApiContext rootCtx = OpenApiContextLocator.getInstance()
                .getOpenApiContext(OpenApiContext.OPENAPI_CONTEXT_ID_DEFAULT);

            ctx = new RestletOpenApiContext(router)
                .id(ctxId)
                .openApiConfiguration(openApiConfiguration)
                .parent(rootCtx);

            if (init) {
                ctx.init();
            }
        }

        return ctx;
    }
}
