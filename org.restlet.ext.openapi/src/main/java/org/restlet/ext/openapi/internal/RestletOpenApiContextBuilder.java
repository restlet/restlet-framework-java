/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.internal;

import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.OpenApiContextLocator;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import java.util.List;
import org.restlet.engine.util.StringUtils;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

public class RestletOpenApiContextBuilder
        extends GenericOpenApiContextBuilder<RestletOpenApiContextBuilder> {
    private Router router;

    private List<ChallengeAuthenticator> authenticators = List.of();

    public RestletOpenApiContextBuilder router(Router router) {
        this.router = router;
        return this;
    }

    public RestletOpenApiContextBuilder authenticators(
            List<ChallengeAuthenticator> authenticators) {
        this.authenticators = authenticators;
        return this;
    }

    @Override
    public OpenApiContext buildContext(boolean init) throws OpenApiConfigurationException {
        if (StringUtils.isNullOrEmpty(ctxId)) {
            ctxId = OpenApiContext.OPENAPI_CONTEXT_ID_DEFAULT;
        }

        OpenApiContext ctx = OpenApiContextLocator.getInstance().getOpenApiContext(ctxId);

        if (ctx == null) {
            OpenApiContext rootCtx =
                    OpenApiContextLocator.getInstance()
                            .getOpenApiContext(OpenApiContext.OPENAPI_CONTEXT_ID_DEFAULT);

            ctx =
                    new RestletOpenApiContext(router, authenticators)
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
