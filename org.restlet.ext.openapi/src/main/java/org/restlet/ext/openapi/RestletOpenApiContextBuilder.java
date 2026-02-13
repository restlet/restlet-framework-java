package org.restlet.ext.openapi;

import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.OpenApiContextLocator;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import org.apache.commons.lang3.StringUtils;
import org.restlet.routing.Router;

class RestletOpenApiContextBuilder extends GenericOpenApiContextBuilder<RestletOpenApiContextBuilder> {
    private Router router;

    RestletOpenApiContextBuilder router(Router router) {
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
