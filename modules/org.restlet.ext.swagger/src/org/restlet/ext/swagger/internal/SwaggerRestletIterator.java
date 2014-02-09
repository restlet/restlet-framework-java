/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger.internal;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.util.RouteList;

/**
 * Iterator over a collection of {@link Restlet} instances seen as a hierarchy,
 * based on the hierarchy of URIs.
 * 
 * @author Grzegorz Godlewski
 */
public class SwaggerRestletIterator implements Iterator<Restlet> {
    /** The path associated to the current iterated Restlet. */
    private String currentPath = "/";

    /** The internal map of Restlet instances. */
    private Map<Restlet, String> toCrawl = new LinkedHashMap<Restlet, String>();

    /**
     * Constructor.
     * 
     * @param restlet
     *            The root restlet.
     */
    public SwaggerRestletIterator(Restlet restlet) {
        toCrawl.put(restlet, "/");
    }

    /**
     * Returns the sub-tree of Restlet instances discovered from the given
     * Restlet. Filters are transparently expanded, as they have no meaning in
     * terms of hierarchy of URIs.
     * 
     * @param restlet
     *            the Restlet instance to discover.
     * @param currentPath
     *            Its associated path.
     * @return
     */
    private Map<Restlet, String> expand(Restlet restlet, String currentPath) {
        Map<Restlet, String> retVal = new LinkedHashMap<Restlet, String>();

        if (restlet instanceof Filter) {
            Filter filter = (Filter) restlet;
            retVal.put(filter.getNext(), currentPath);
        } else if (restlet instanceof Router) {
            Router router = (Router) restlet;
            RouteList routeList = router.getRoutes();
            for (Route route : routeList) {
                if (route instanceof TemplateRoute) {
                    TemplateRoute templateRoute = (TemplateRoute) route;
                    String templatePattern = templateRoute.getTemplate()
                            .getPattern();

                    String path = SwaggerUtils.cleanSlashes(currentPath
                            + templatePattern + "/");
                    retVal.put(templateRoute.getNext(), path);
                }
            }
        }

        return retVal;
    }

    /**
     * Return the path associated to the current Restlet.
     * 
     * @return The path associated to the current Restlet.
     */
    public String getCurrentPath() {
        return currentPath;
    }

    @Override
    public boolean hasNext() {
        return !toCrawl.isEmpty();
    }

    @Override
    public Restlet next() {
        if (!hasNext()) {
            return null;
        }

        Restlet currentRestlet = toCrawl.keySet().iterator().next();
        currentPath = toCrawl.remove(currentRestlet);
        toCrawl.putAll(expand(currentRestlet, currentPath));

        return currentRestlet;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
