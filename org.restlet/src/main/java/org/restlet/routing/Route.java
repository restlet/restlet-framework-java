/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.routing;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

/**
 * Filter scoring the affinity of calls with the attached Restlet. The score is
 * used by an associated Router in order to determine the most appropriate
 * Restlet for a given call.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see org.restlet.routing.Template
 * @author Jerome Louvel
 */
public abstract class Route extends Filter {

    /** The parent router. */
    private volatile Router router;

    /**
     * Constructor behaving as a simple extractor filter.
     * 
     * @param next
     *            The next Restlet.
     */
    public Route(Restlet next) {
        this(null, next);
    }

    /**
     * Constructor.
     * 
     * @param router
     *            The parent router.
     * @param next
     *            The next Restlet.
     */
    public Route(Router router, Restlet next) {
        super((router != null) ? router.getContext() : (next != null) ? next
                .getContext() : null, next);
        this.router = router;
    }

    /**
     * Returns the parent router.
     * 
     * @return The parent router.
     */
    public Router getRouter() {
        return this.router;
    }

    /**
     * Returns the score for a given call (between 0 and 1.0).
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @return The score for a given call (between 0 and 1.0).
     */
    public abstract float score(Request request, Response response);

    /**
     * Sets the parent router.
     * 
     * @param router
     *            The parent router.
     */
    public void setRouter(Router router) {
        this.router = router;
    }

}
