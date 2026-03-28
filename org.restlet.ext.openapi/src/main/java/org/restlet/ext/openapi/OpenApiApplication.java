/**
 * Copyright 2005-2026 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open source license available at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.openapi;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

public class OpenApiApplication extends Application {
    /**
     * Default path for the OpenAPI specification. Can be overridden by overriding the
     * {@link #getOpenApiSpecificationPath()} method.
     */
    static final String OPENAPI_SPECIFICATION_DEFAULT_PATH = "/openapi";

    /**
     * Indicates if this application has already been documented or not.
     */
    private volatile boolean documented;

    @Override
    public Restlet getInboundRoot() {
        Restlet inboundRoot = super.getInboundRoot();

        if (!documented) {
            synchronized (this) {
                if (!documented) {
                    Router rootRouter = getNextRouter(inboundRoot);

                    if (!documented && rootRouter != null) {
                        attachOpenApiSpecificationRestlet(rootRouter);
                        documented = true;
                    }

                }
            }
        }

        return inboundRoot;
    }

    /**
     * Path where the OpenAPI specification will be available. By default, it is "/openapi".
     */
    protected String getOpenApiSpecificationPath() {
        return OPENAPI_SPECIFICATION_DEFAULT_PATH;
    }

    /**
     * Returns the next router available.
     *
     * @param current The current Restlet to inspect.
     * @return The first router available.
     */
    private static Router getNextRouter(Restlet current) {
        Router result = null;

        if (current instanceof Router router) {
            result = router;
        } else if (current instanceof Filter filter) {
            result = getNextRouter(filter.getNext());
        }

        return result;
    }

    private void attachOpenApiSpecificationRestlet(Router router) {
        getOpenApiSpecificationRestlet(router).attach(router, getOpenApiSpecificationPath());
        documented = true;
    }

    /**
     * The dedicated {@link Restlet} able to generate the Swagger specification formats.
     *
     * @return The {@link Restlet} able to generate the Swagger specification formats.
     */
    OpenApiSpecificationRestlet getOpenApiSpecificationRestlet(
        Router router
    ) {
        return new OpenApiSpecificationRestlet(router);
    }
}
