/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi;

import java.util.ArrayList;
import java.util.List;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

public class OpenApiApplication extends Application {
    /**
     * Default path for the OpenAPI specification. Can be overridden by overriding the {@link
     * #getOpenApiSpecificationPath()} method.
     */
    static final String OPENAPI_SPECIFICATION_DEFAULT_PATH = "/openapi";

    /** Indicates if this application has already been documented or not. */
    private volatile boolean documented;

    @Override
    public Restlet getInboundRoot() {
        Restlet inboundRoot = super.getInboundRoot();

        if (!documented) {
            synchronized (this) {
                if (!documented) {
                    List<ChallengeAuthenticator> authenticators = new ArrayList<>();
                    Router rootRouter = getNextRouter(inboundRoot, authenticators);

                    if (!documented && rootRouter != null) {
                        attachOpenApiSpecificationRestlet(rootRouter, authenticators);
                        documented = true;
                    }
                }
            }
        }

        return inboundRoot;
    }

    /** Path where the OpenAPI specification will be available. By default, it is "/openapi". */
    protected String getOpenApiSpecificationPath() {
        return OPENAPI_SPECIFICATION_DEFAULT_PATH;
    }

    /**
     * Returns the next router available, collecting any {@link ChallengeAuthenticator} found while
     * unwrapping the filter chain along the way.
     *
     * @param current The current Restlet to inspect.
     * @param authenticators The list of authenticators found so far, to be completed.
     * @return The first router available.
     */
    private static Router getNextRouter(
            Restlet current, List<ChallengeAuthenticator> authenticators) {
        Router result = null;

        if (current instanceof Router router) {
            result = router;
        } else if (current instanceof Filter filter) {
            if (filter instanceof ChallengeAuthenticator challengeAuthenticator) {
                authenticators.add(challengeAuthenticator);
            }
            result = getNextRouter(filter.getNext(), authenticators);
        }

        return result;
    }

    private void attachOpenApiSpecificationRestlet(
            Router router, List<ChallengeAuthenticator> authenticators) {
        getOpenApiSpecificationRestlet(router, authenticators)
                .attach(router, getOpenApiSpecificationPath());
        documented = true;
    }

    /**
     * The dedicated {@link Restlet} able to generate the Swagger specification formats.
     *
     * @return The {@link Restlet} able to generate the Swagger specification formats.
     */
    OpenApiSpecificationRestlet getOpenApiSpecificationRestlet(
            Router router, List<ChallengeAuthenticator> authenticators) {
        return new OpenApiSpecificationRestlet(router, authenticators);
    }
}
