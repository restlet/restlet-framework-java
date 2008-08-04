/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.util.RouteList;

/**
 * Restlet routing calls to one of the attached routes. Each route can compute
 * an affinity score for each call depending on various criteria. The attach()
 * method allow the creation of routes based on URI patterns matching the
 * beginning of a the resource reference's remaining part.<br>
 * <br>
 * In addition, several routing modes are supported, implementing various
 * algorithms:
 * <ul>
 * <li>Best match (default)</li>
 * <li>First match</li>
 * <li>Last match</li>
 * <li>Random match</li>
 * <li>Round robin</li>
 * <li>Custom</li>
 * </ul>
 * <br>
 * Note that for routes using URI patterns will update the resource reference's
 * base reference during the routing if they are selected. It is also important
 * to know that the routing is very strict about path separators in your URI
 * patterns. Finally, you can modify the list of routes while handling incoming
 * calls as the delegation code is ensured to be thread-safe.
 * 
 * @see <a href="http://www.restlet.org/documentation/1.0/tutorial#part11">Tutorial: Routers and
 *      hierarchical URIs</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Router extends Restlet {
    /**
     * Each call will be routed to the route with the best score, if the
     * required score is reached.
     */
    public static final int BEST = 1;

    /**
     * Each call is routed to the first route if the required score is reached.
     * If the required score is not reached, then the route is skipped and the
     * next one is considered.
     */
    public static final int FIRST = 2;

    /**
     * Each call will be routed to the last route if the required score is
     * reached. If the required score is not reached, then the route is skipped
     * and the previous one is considered.
     */
    public static final int LAST = 3;

    /**
     * Each call is be routed to the next route target if the required score is
     * reached. The next route is relative to the previous call routed (round
     * robin mode). If the required score is not reached, then the route is
     * skipped and the next one is considered. If the last route is reached, the
     * first route will be considered.
     */
    public static final int NEXT = 4;

    /**
     * Each call will be randomly routed to one of the routes that reached the
     * required score. If the random route selected is not a match then the
     * immediate next route is evaluated until one matching route is found. If
     * we get back to the inital random route selected with no match, then we
     * return null.
     */
    public static final int RANDOM = 5;

    /**
     * Each call will be routed according to a custom mode.
     */
    public static final int CUSTOM = 6;

    /** Finder class to instantiate. */
    private Class<? extends Finder> finderClass;

    /** The modifiable list of routes. */
    private RouteList routes;

    /** The default route tested if no other one was available. */
    private Route defaultRoute;

    /** The routing mode. */
    private int routingMode;

    /** The minimum score required to have a match. */
    private float requiredScore;

    /**
     * The maximum number of attempts if no attachment could be matched on the
     * first attempt.
     */
    private int maxAttempts;

    /** The delay (in milliseconds) before a new attempt. */
    private long retryDelay;

    /**
     * Constructor. Note that usage of this constructor is not recommended as
     * the Router won't have a proper context set. In general you will prefer to
     * use the other constructor and pass it the parent application's context or
     * eventually the parent component's context if you don't use applications.
     */
    public Router() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public Router(Context context) {
        super(context);
        this.routes = null;
        this.defaultRoute = null;
        this.finderClass = Finder.class;
        this.routingMode = BEST;
        this.requiredScore = 0.5F;
        this.maxAttempts = 1;
        this.retryDelay = 500L;
    }

    /**
     * Attaches a target Restlet to this router with an empty URI pattern. A new
     * route will be added routing to the target when any call is received.
     * 
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    public Route attach(Restlet target) {
        return attach("", target);
    }

    /**
     * Attaches a target Resource class to this router based on a given URI
     * pattern. A new route will be added routing to the target when calls with
     * a URI matching the pattern will be received.
     * 
     * @param uriPattern
     *            The URI pattern that must match the relative part of the
     *            resource URI.
     * @param targetClass
     *            The target Resource class to attach.
     * @return The created route.
     */
    public Route attach(String uriPattern, Class<? extends Resource> targetClass) {
        return attach(uriPattern, createFinder(targetClass));
    }

    /**
     * Creates a new finder instance based on the "targetClass" property.
     * 
     * @param targetClass
     *            The target Resource class to attach.
     * @return The new finder instance.
     */
    private Finder createFinder(Class<? extends Resource> targetClass) {
        Finder result = null;

        if (getFinderClass() != null) {
            try {
                Constructor<? extends Finder> constructor = getFinderClass()
                        .getConstructor(Context.class, Class.class);

                if (constructor != null) {
                    result = constructor.newInstance(getContext(), targetClass);
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING,
                        "Exception while instantiating the finder.", e);
            }
        }

        return result;
    }

    /**
     * Attaches a target Restlet to this router based on a given URI pattern. A
     * new route will be added routing to the target when calls with a URI
     * matching the pattern will be received.
     * 
     * @param uriPattern
     *            The URI pattern that must match the relative part of the
     *            resource URI.
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    public Route attach(String uriPattern, Restlet target) {
        Route result = createRoute(uriPattern, target);
        getRoutes().add(result);
        return result;
    }

    /**
     * Attaches a Resource class to this router as the default target to invoke
     * when no route matches. It actually sets a default route that scores all
     * calls to 1.0.
     * 
     * @param defaultTargetClass
     *            The target Resource class to attach.
     * @return The created route.
     */
    public Route attachDefault(Class<? extends Resource> defaultTargetClass) {
        return attachDefault(createFinder(defaultTargetClass));
    }

    /**
     * Attaches a Restlet to this router as the default target to invoke when no
     * route matches. It actually sets a default route that scores all calls to
     * 1.0.
     * 
     * @param defaultTarget
     *            The Restlet to use as the default target.
     * @return The created route.
     */
    public Route attachDefault(Restlet defaultTarget) {
        Route result = createRoute("", defaultTarget);
        setDefaultRoute(result);
        return result;
    }

    /**
     * Creates a new route for the given URI pattern and target.
     * 
     * @param uriPattern
     *            The URI pattern that must match the relative part of the
     *            resource URI.
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    protected Route createRoute(String uriPattern, Restlet target) {
        return new Route(this, uriPattern, target);
    }

    /**
     * Detaches the target from this router. All routes routing to this target
     * Restlet are removed from the list of routes and the default route is set
     * to null.
     * 
     * @param target
     *            The target Restlet to detach.
     */
    public void detach(Restlet target) {
        getRoutes().removeAll(target);
        if ((getDefaultRoute() != null)
                && (getDefaultRoute().getNext() == target))
            setDefaultRoute(null);
    }

    /**
     * Returns the matched route according to a custom algorithm. To use in
     * combination of the RouterMode.CUSTOM enumeration. The default
     * implementation (to be overriden), returns null.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The matched route if available or null.
     */
    protected Route getCustom(Request request, Response response) {
        return null;
    }

    /**
     * Returns the default route to test if no other one was available after
     * retrying the maximum number of attemps.
     * 
     * @return The default route tested if no other one was available.
     */
    public Route getDefaultRoute() {
        return this.defaultRoute;
    }

    /**
     * Returns the maximum number of attempts if no attachment could be matched
     * on the first attempt. This is useful when the attachment scoring is
     * dynamic and therefore could change on a retry. The default value is set
     * to 1.
     * 
     * @return The maximum number of attempts if no attachment could be matched
     *         on the first attempt.
     */
    public int getMaxAttempts() {
        return this.maxAttempts;
    }

    /**
     * Returns the next Restlet if available.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The next Restlet if available or null.
     */
    public Restlet getNext(Request request, Response response) {
        Route result = null;

        for (int i = 0; (result == null) && (i < getMaxAttempts()); i++) {
            if (i > 0) {
                // Before attempting another time, let's
                // sleep during the "retryDelay" set.
                try {
                    Thread.sleep(getRetryDelay());
                } catch (InterruptedException e) {
                }
            }

            if (this.routes != null) {
                // Select the routing mode
                switch (getRoutingMode()) {
                case BEST:
                    result = getRoutes().getBest(request, response,
                            getRequiredScore());
                    break;

                case FIRST:
                    result = getRoutes().getFirst(request, response,
                            getRequiredScore());
                    break;

                case LAST:
                    result = getRoutes().getLast(request, response,
                            getRequiredScore());
                    break;

                case NEXT:
                    result = getRoutes().getNext(request, response,
                            getRequiredScore());
                    break;

                case RANDOM:
                    result = getRoutes().getRandom(request, response,
                            getRequiredScore());
                    break;

                case CUSTOM:
                    result = getCustom(request, response);
                    break;
                }
            }
        }

        if (result == null) {
            // If nothing matched in the routes list, check the default
            // route
            if ((getDefaultRoute() != null)
                    && (getDefaultRoute().score(request, response) >= getRequiredScore())) {
                result = getDefaultRoute();
            } else {
                // No route could be found
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }

        return result;
    }

    /**
     * Returns the minimum score required to have a match.
     * 
     * @return The minimum score required to have a match.
     */
    public float getRequiredScore() {
        return this.requiredScore;
    }

    /**
     * Returns the delay (in seconds) before a new attempt. The default value is
     * 500 ms.
     * 
     * @return The delay (in seconds) before a new attempt.
     */
    public long getRetryDelay() {
        return this.retryDelay;
    }

    /**
     * Returns the modifiable list of routes.
     * 
     * @return The modifiable list of routes.
     */
    public RouteList getRoutes() {
        if (this.routes == null)
            this.routes = new RouteList();
        return this.routes;
    }

    /**
     * Returns the routing mode.
     * 
     * @return The routing mode.
     */
    public int getRoutingMode() {
        return this.routingMode;
    }

    /**
     * Handles a call by invoking the next Restlet if it is available.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
	@Override
    public void handle(Request request, Response response) {
        init(request, response);

        Restlet next = getNext(request, response);
        if (next != null) {
            next.handle(request, response);
        } else {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    /**
     * Sets the default route tested if no other one was available.
     * 
     * @param defaultRoute
     *            The default route tested if no other one was available.
     */
    public void setDefaultRoute(Route defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    /**
     * Sets the maximum number of attempts if no attachment could be matched on
     * the first attempt. This is useful when the attachment scoring is dynamic
     * and therefore could change on a retry.
     * 
     * @param maxAttempts
     *            The maximum number of attempts.
     */
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /**
     * Sets the score required to have a match.
     * 
     * @param score
     *            The score required to have a match.
     */
    public void setRequiredScore(float score) {
        this.requiredScore = score;
    }

    /**
     * Sets the delay (in seconds) before a new attempt.
     * 
     * @param retryDelay
     *            The delay (in seconds) before a new attempt.
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Sets the routing mode.
     * 
     * @param routingMode
     *            The routing mode.
     */
    public void setRoutingMode(int routingMode) {
        this.routingMode = routingMode;
    }

    /**
     * Returns the finder class to instantiate.
     * 
     * @return the finder class to instantiate.
     */
    public Class<? extends Finder> getFinderClass() {
        return this.finderClass;
    }

    /**
     * Sets the finder class to instantiate.
     * 
     * @param finderClass
     *            The finder class to instantiate.
     */
    public void setFinderClass(Class<? extends Finder> finderClass) {
        this.finderClass = finderClass;
    }

}
