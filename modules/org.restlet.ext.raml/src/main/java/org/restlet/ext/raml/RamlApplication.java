/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.raml;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

/**
 * RAML enabled application. This subclass of {@link Application} can describe
 * itself in the format described by the <a
 * href="http://raml.org/spec.html">RAML specification project</a>.<br>
 * <br>
 * It requires you to set up a specific end point that serves the RAML
 * definition.<br>
 * <br>
 * By default, nothing is required. This application adds to its inbound root
 * the endpoint ("/raml"). You can override this behavior by using the
 * RamlApplication#attachRaml* methods<br>
 * 
 * By default, the description is generated by introspecting the application
 * itself. You can override this behavior by specifying your own implementation
 * of {@link RamlSpecificationRestlet}.
 * 
 * @author Cyprien Quilici
 * 
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
public class RamlApplication extends Application {

    /**
     * Returns the next router available.
     * 
     * @param current
     *            The current Restlet to inspect.
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

    /**
     * Indicates if the given {@link Restlet} provides a
     * {@link RamlSpecificationRestlet} able to generate RAML documentation.
     * 
     * @param current
     *            The current Restlet to inspect.
     * @return True if the given {@link Restlet} provides a
     *         {@link RamlSpecificationRestlet} able to generate RAML
     *         documentation.
     */
    private static boolean isDocumented(Restlet current) {
        boolean documented = false;

        Router router = null;
        if (current instanceof Router) {
            router = (Router) current;
            for (Route route : router.getRoutes()) {
                if (isDocumented(route.getNext())) {
                    documented = true;
                    break;
                }
            }
        } else if (current instanceof Filter) {
            documented = isDocumented(((Filter) current).getNext());
        } else if (current instanceof RamlSpecificationRestlet) {
            documented = true;
        }

        return documented;
    }

    /** Indicates if this application can document herself. */
    private boolean documented;

    /**
     * Defines the route on which the Raml definition will be provided.
     * 
     * @param router
     *            The router on which defining the new route.
     * @param ramlPath
     *            The path to which attach the Restlet that serves the RAML
     *            definition.
     * @param ramlRestlet
     *            The Restlet that serves the RAML definition.
     */
    public void attachRamlDocumentationRestlet(Router router, String ramlPath,
            Restlet ramlRestlet) {
        router.attach(ramlPath, ramlRestlet);
        documented = true;
    }

    /**
     * Defines the route on which the RAML definition will be provided (by
     * default, "/raml").
     * 
     * @param router
     *            The router on which defining the new route.
     */
    public void attachRamlSpecificationRestlet(Router router) {
        getRamlSpecificationRestlet(getContext()).attach(router);
        documented = true;
    }

    /**
     * Overrides the parent's implementation. It checks that the application has
     * been documented using a {@link RamlSpecificationRestlet}. By default, the
     * documentation is attached to the high level router, with the "/raml"
     * path.
     */
    @Override
    public Restlet getInboundRoot() {
        Restlet inboundRoot = super.getInboundRoot();
        if (!documented) {
            synchronized (this) {
                if (!documented) {
                    Router rootRouter = getNextRouter(inboundRoot);

                    // Check that the application has been documented.
                    documented = isDocumented(rootRouter);
                    if (!documented) {
                        attachRamlSpecificationRestlet(rootRouter);
                        documented = true;
                    }
                }
            }
        }
        return inboundRoot;
    }

    /**
     * The dedicated {@link Restlet} able to generate the RAML specification
     * format.
     * 
     * @return The {@link Restlet} able to generate the RAML specification
     *         format.
     */
    public RamlSpecificationRestlet getRamlSpecificationRestlet(Context context) {
        RamlSpecificationRestlet result = new RamlSpecificationRestlet(context);
        result.setApiInboundRoot(this);
        return result;
    }
}
