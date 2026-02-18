package org.restlet.ext.openapi;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

public class RestletOpenApiApplication extends Application {
    /**
     * Default path for the OpenAPI specification. Can be overridden by overriding the
     * {@link #getOpenapiSpecificationPath()} method.
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
    protected String getOpenapiSpecificationPath() {
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
        if (current instanceof Router) {
            result = (Router) current;
        } else if (current instanceof Filter) {
            result = getNextRouter(((Filter) current).getNext());
        }

        return result;
    }

    private void attachOpenApiSpecificationRestlet(Router router) {
        getOpenApiSpecificationRestlet(router).attach(router, getOpenapiSpecificationPath());
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
