/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.routing;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
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
 * <li>Best match</li>
 * <li>First match (default)</li>
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
 * calls as the delegation code is ensured to be thread-safe.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet/376-restlet.html">User
 *      Guide - Routers and hierarchical URIs</a>
 * @author Jerome Louvel
 */
public class Router extends Restlet {

    /**
     * Each call will be routed to the route with the best score, if the
     * required score is reached.
     * 
     * @deprecated Use {@link #MODE_BEST_MATCH} instead.
     */
    @Deprecated
    public static final int BEST = 1;

    /**
     * Each call will be routed according to a custom mode.
     * 
     * @deprecated Use {@link #MODE_CUSTOM} instead.
     */
    @Deprecated
    public static final int CUSTOM = 6;

    /**
     * Each call is routed to the first route if the required score is reached.
     * If the required score is not reached, then the route is skipped and the
     * next one is considered.
     * 
     * @deprecated Use {@link #MODE_FIRST_MATCH} instead.
     */
    @Deprecated
    public static final int FIRST = 2;

    /**
     * Each call will be routed to the last route if the required score is
     * reached. If the required score is not reached, then the route is skipped
     * and the previous one is considered.
     * 
     * @deprecated Use {@link #MODE_LAST_MATCH} instead.
     */
    @Deprecated
    public static final int LAST = 3;

    /**
     * Each call will be routed to the route with the best score, if the
     * required score is reached.
     */
    public static final int MODE_BEST_MATCH = 1;

    /**
     * Each call will be routed according to a custom mode.
     */
    public static final int MODE_CUSTOM = 6;

    /**
     * Each call is routed to the first route if the required score is reached.
     * If the required score is not reached, then the route is skipped and the
     * next one is considered.
     */
    public static final int MODE_FIRST_MATCH = 2;

    /**
     * Each call will be routed to the last route if the required score is
     * reached. If the required score is not reached, then the route is skipped
     * and the previous one is considered.
     */
    public static final int MODE_LAST_MATCH = 3;

    /**
     * Each call is be routed to the next route target if the required score is
     * reached. The next route is relative to the previous call routed (round
     * robin mode). If the required score is not reached, then the route is
     * skipped and the next one is considered. If the last route is reached, the
     * first route will be considered.
     */
    public static final int MODE_NEXT_MATCH = 4;

    /**
     * Each call will be randomly routed to one of the routes that reached the
     * required score. If the random route selected is not a match then the
     * immediate next route is evaluated until one matching route is found. If
     * we get back to the initial random route selected with no match, then we
     * return null.
     */
    public static final int MODE_RANDOM_MATCH = 5;

    /**
     * Each call is be routed to the next route target if the required score is
     * reached. The next route is relative to the previous call routed (round
     * robin mode). If the required score is not reached, then the route is
     * skipped and the next one is considered. If the last route is reached, the
     * first route will be considered.
     * 
     * @deprecated Use {@link #MODE_NEXT_MATCH} instead.
     */
    @Deprecated
    public static final int NEXT = 4;

    /**
     * Each call will be randomly routed to one of the routes that reached the
     * required score. If the random route selected is not a match then the
     * immediate next route is evaluated until one matching route is found. If
     * we get back to the initial random route selected with no match, then we
     * return null.
     * 
     * @deprecated Use {@link #MODE_RANDOM_MATCH} instead.
     */
    @Deprecated
    public static final int RANDOM = 5;

    /** The default matching mode to use when selecting routes based on URIs. */
    private volatile int defaultMatchingMode;

    /**
     * The default setting for whether the routing should be done on URIs with
     * or without taking into account query string.
     */
    private volatile boolean defaultMatchingQuery;

    /** The default route tested if no other one was available. */
    @SuppressWarnings("deprecation")
    private volatile Route defaultRoute;

    /** Finder class to instantiate. */
    private volatile Class<? extends Finder> finderClass;

    /**
     * The maximum number of attempts if no attachment could be matched on the
     * first attempt.
     */
    private volatile int maxAttempts;

    /** The minimum score required to have a match. */
    private volatile float requiredScore;

    /** The delay (in milliseconds) before a new attempt. */
    private volatile long retryDelay;

    /** The modifiable list of routes. */
    private volatile RouteList routes;

    /** The routing mode. */
    private volatile int routingMode;

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
        this.routes = new RouteList();
        this.defaultMatchingMode = Template.MODE_EQUALS;
        this.defaultMatchingQuery = false;
        this.defaultRoute = null;
        this.finderClass = Finder.class;
        this.routingMode = MODE_FIRST_MATCH;
        this.requiredScore = 0.5F;
        this.maxAttempts = 1;
        this.retryDelay = 500L;
    }

    /**
     * Attaches a target Restlet to this router with an empty URI pattern. A new
     * route using the matching mode returned by
     * {@link #getMatchingMode(Restlet)} will be added routing to the target
     * when any call is received.
     * 
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    public Route attach(Restlet target) {
        return attach(target, getMatchingMode(target));
    }

    /**
     * Attaches a target Restlet to this router with an empty URI pattern. A new
     * route will be added routing to the target when any call is received.
     * 
     * @param target
     *            The target Restlet to attach.
     * @param matchingMode
     *            The matching mode.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    public Route attach(Restlet target, int matchingMode) {
        return attach("", target, matchingMode);
    }

    /**
     * Attaches a target Resource class to this router based on a given URI
     * pattern. A new route using the matching mode returned by
     * {@link #getMatchingMode(Restlet)} will be added routing to the target
     * when calls with a URI matching the pattern will be received.
     * 
     * @param pathTemplate
     *            The URI path template that must match the relative part of the
     *            resource URI.
     * @param targetClass
     *            The target Resource class to attach.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    public Route attach(String pathTemplate, Class<?> targetClass) {
        return attach(pathTemplate, createFinder(targetClass));
    }

    /**
     * Attaches a target Resource class to this router based on a given URI
     * pattern. A new route will be added routing to the target when calls with
     * a URI matching the pattern will be received.
     * 
     * @param pathTemplate
     *            The URI path template that must match the relative part of the
     *            resource URI.
     * @param targetClass
     *            The target Resource class to attach.
     * @param matchingMode
     *            The matching mode.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    public Route attach(String pathTemplate, Class<?> targetClass,
            int matchingMode) {
        return attach(pathTemplate, createFinder(targetClass), matchingMode);
    }

    /**
     * Attaches a target Restlet to this router based on a given URI pattern. A
     * new route using the matching mode returned by
     * {@link #getMatchingMode(Restlet)} will be added routing to the target
     * when calls with a URI matching the pattern will be received.
     * 
     * @param pathTemplate
     *            The URI path template that must match the relative part of the
     *            resource URI.
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    public Route attach(String pathTemplate, Restlet target) {
        return attach(pathTemplate, target, getMatchingMode(target));
    }

    /**
     * Attaches a target Restlet to this router based on a given URI pattern. A
     * new route will be added routing to the target when calls with a URI
     * matching the pattern will be received.
     * 
     * @param pathTemplate
     *            The URI path template that must match the relative part of the
     *            resource URI.
     * @param target
     *            The target Restlet to attach.
     * @param matchingMode
     *            The matching mode.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    public Route attach(String pathTemplate, Restlet target, int matchingMode) {
        final Route result = createRoute(pathTemplate, target, matchingMode);
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
    @SuppressWarnings("deprecation")
    public Route attachDefault(Class<?> defaultTargetClass) {
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
    @SuppressWarnings("deprecation")
    public Route attachDefault(Restlet defaultTarget) {
        Route result = createRoute("", defaultTarget);
        result.setMatchingMode(Template.MODE_STARTS_WITH);
        setDefaultRoute(result);
        return result;
    }

    /**
     * Creates a new finder instance based on the "targetClass" property.
     * 
     * @param targetClass
     *            The target Resource class to attach.
     * @return The new finder instance.
     */
    public Finder createFinder(Class<?> targetClass) {
        return Finder.createFinder(targetClass, getFinderClass(), getContext(),
                getLogger());
    }

    /**
     * Creates a new route for the given URI pattern and target. The route will
     * match the URI query string depending on the result of
     * {@link #getDefaultMatchingQuery()} and the matching mode will be given by
     * {@link #getMatchingMode(Restlet)}.
     * 
     * @param uriPattern
     *            The URI pattern that must match the relative part of the
     *            resource URI.
     * @param target
     *            The target Restlet to attach.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    protected Route createRoute(String uriPattern, Restlet target) {
        return createRoute(uriPattern, target, getMatchingMode(target));
    }

    /**
     * Creates a new route for the given URI pattern, target and matching mode.
     * The route will match the URI query string depending on the result of
     * {@link #getDefaultMatchingQuery()}.
     * 
     * @param uriPattern
     *            The URI pattern that must match the relative part of the
     *            resource URI.
     * @param target
     *            The target Restlet to attach.
     * @param matchingMode
     *            The matching mode.
     * @return The created route.
     */
    @SuppressWarnings("deprecation")
    protected Route createRoute(String uriPattern, Restlet target,
            int matchingMode) {
        Route result = new Route(this, uriPattern, target);
        result.getTemplate().setMatchingMode(matchingMode);
        result.setMatchingQuery(getDefaultMatchingQuery());
        return result;
    }

    /**
     * Detaches the target from this router. All routes routing to this target
     * Restlet are removed from the list of routes and the default route is set
     * to null.
     * 
     * @param targetClass
     *            The target class to detach.
     */
    public void detach(Class<?> targetClass) {
        for (int i = getRoutes().size() - 1; i >= 0; i--) {
            Restlet target = getRoutes().get(i).getNext();
            if (target != null
                    && Finder.class.isAssignableFrom(target.getClass())) {
                Finder finder = (Finder) target;
                if (finder.getTargetClass().equals(targetClass)) {
                    getRoutes().remove(i);
                }
            }
        }

        if (getDefaultRoute() != null) {
            Restlet target = getDefaultRoute().getNext();
            if (target != null
                    && Finder.class.isAssignableFrom(target.getClass())) {
                Finder finder = (Finder) target;
                if (finder.getTargetClass().equals(targetClass)) {
                    setDefaultRoute(null);
                }
            }
        }
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
                && (getDefaultRoute().getNext() == target)) {
            setDefaultRoute(null);
        }
    }

    /**
     * Effectively handles the call using the selected next {@link Restlet},
     * typically the selected {@link Route}. By default, it just invokes the
     * next Restlet.
     * 
     * @param next
     *            The next Restlet to invoke.
     * @param request
     *            The request.
     * @param response
     *            The response.
     */
    protected void doHandle(Restlet next, Request request, Response response) {
        next.handle(request, response);
    }

    /**
     * Returns the matched route according to a custom algorithm. To use in
     * combination of the {@link #MODE_CUSTOM} option. The default
     * implementation (to be overridden), returns null.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The matched route if available or null.
     */
    @SuppressWarnings("deprecation")
    protected Route getCustom(Request request, Response response) {
        return null;
    }

    /**
     * Returns the default matching mode to use when selecting routes based on
     * URIs. By default it returns {@link Template#MODE_EQUALS}.
     * 
     * @return The default matching mode.
     */
    public int getDefaultMatchingMode() {
        return this.defaultMatchingMode;
    }

    /**
     * Returns the default setting for whether the routing should be done on
     * URIs with or without taking into account query string. By default, it
     * returns false.
     * 
     * @return the default setting for whether the routing should be done on
     *         URIs with or without taking into account query string.
     */
    public boolean getDefaultMatchingQuery() {
        return getDefaultMatchQuery();
    }

    /**
     * Returns the default setting for whether the routing should be done on
     * URIs with or without taking into account query string. By default, it
     * returns false.
     * 
     * @return the default setting for whether the routing should be done on
     *         URIs with or without taking into account query string.
     * @deprecated Use {@link #getDefaultMatchingQuery()} instead.
     */
    @Deprecated
    public boolean getDefaultMatchQuery() {
        return this.defaultMatchingQuery;
    }

    /**
     * Returns the default route to test if no other one was available after
     * retrying the maximum number of attempts.
     * 
     * @return The default route tested if no other one was available.
     */
    @SuppressWarnings("deprecation")
    public Route getDefaultRoute() {
        return this.defaultRoute;
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
     * Returns the matching mode for the target Restlet. By default it returns
     * {@link #getDefaultMatchingMode()}. If the target is an instance of
     * {@link Directory} or {@link Router} then the mode returned is
     * {@link Template#MODE_STARTS_WITH} to allow further routing by those
     * objects. If the target is an instance of {@link Filter}, then it returns
     * the matching mode for the {@link Filter#getNext()} Restlet recursively.
     * 
     * @param target
     *            The target Restlet.
     * @return The preferred matching mode.
     */
    protected int getMatchingMode(Restlet target) {
        int result = getDefaultMatchingMode();

        if ((target instanceof Directory) || (target instanceof Router)) {
            result = Template.MODE_STARTS_WITH;
        } else if (target instanceof Filter) {
            result = getMatchingMode(((Filter) target).getNext());
        }

        return result;
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
    @SuppressWarnings("deprecation")
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
                case MODE_BEST_MATCH:
                    result = getRoutes().getBest(request, response,
                            getRequiredScore());
                    break;

                case MODE_FIRST_MATCH:
                    result = getRoutes().getFirst(request, response,
                            getRequiredScore());
                    break;

                case MODE_LAST_MATCH:
                    result = getRoutes().getLast(request, response,
                            getRequiredScore());
                    break;

                case MODE_NEXT_MATCH:
                    result = getRoutes().getNext(request, response,
                            getRequiredScore());
                    break;

                case MODE_RANDOM_MATCH:
                    result = getRoutes().getRandom(request, response,
                            getRequiredScore());
                    break;

                case MODE_CUSTOM:
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

        logRoute(result);
        return result;
    }

    /**
     * Returns the minimum score required to have a match. By default, it
     * returns {@code 0.5}.
     * 
     * @return The minimum score required to have a match.
     */
    public float getRequiredScore() {
        return this.requiredScore;
    }

    /**
     * Returns the delay in milliseconds before a new attempt is made. The
     * default value is {@code 500}.
     * 
     * @return The delay in milliseconds before a new attempt is made.
     */
    public long getRetryDelay() {
        return this.retryDelay;
    }

    /**
     * Returns the modifiable list of routes. Creates a new instance if no one
     * has been set.
     * 
     * @return The modifiable list of routes.
     */
    public RouteList getRoutes() {
        return this.routes;
    }

    /**
     * Returns the routing mode. By default, it returns the
     * {@link #MODE_FIRST_MATCH} mode.
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
        super.handle(request, response);
        Restlet next = getNext(request, response);

        if (next != null) {
            doHandle(next, request, response);
        } else {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    /**
     * Logs the route selected.
     * 
     * @param route
     *            The route selected.
     */
    @SuppressWarnings("deprecation")
    protected void logRoute(Route route) {
        if (getLogger().isLoggable(Level.FINE)) {
            if (getDefaultRoute() == route) {
                getLogger().fine("The default route was selected.");
            } else {
                getLogger().fine("This route was selected: " + route);
            }
        }
    }

    /**
     * Sets the default matching mode to use when selecting routes based on
     * URIs. By default it is set to {@link Template#MODE_EQUALS}.
     * 
     * @param defaultMatchingMode
     *            The default matching mode.
     */
    public void setDefaultMatchingMode(int defaultMatchingMode) {
        this.defaultMatchingMode = defaultMatchingMode;
    }

    /**
     * Sets the default setting for whether the routing should be done on URIs
     * with or without taking into account query string. By default, it is set
     * to false.
     * 
     * @param defaultMatchingQuery
     *            The default setting for whether the routing should be done on
     *            URIs with or without taking into account query string.
     * 
     */
    public void setDefaultMatchingQuery(boolean defaultMatchingQuery) {
        setDefaultMatchQuery(defaultMatchingQuery);
    }

    /**
     * Sets the default setting for whether the routing should be done on URIs
     * with or without taking into account query string. By default, it is set
     * to false.
     * 
     * @param defaultMatchingQuery
     *            The default setting for whether the routing should be done on
     *            URIs with or without taking into account query string.
     * @deprecated Use {@link #setDefaultMatchingQuery(boolean)} instead.
     */
    @Deprecated
    public void setDefaultMatchQuery(boolean defaultMatchingQuery) {
        this.defaultMatchingQuery = defaultMatchingQuery;
    }

    /**
     * Sets the default route tested if no other one was available.
     * 
     * @param defaultRoute
     *            The default route tested if no other one was available.
     */
    @SuppressWarnings("deprecation")
    public void setDefaultRoute(Route defaultRoute) {
        this.defaultRoute = defaultRoute;
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
     * Sets the score required to have a match. By default, it is set to
     * {@code 0.5}.
     * 
     * @param score
     *            The score required to have a match.
     */
    public void setRequiredScore(float score) {
        this.requiredScore = score;
    }

    /**
     * Sets the delay in milliseconds before a new attempt is made. By default,
     * it is set to {@code 500}.
     * 
     * @param retryDelay
     *            The delay in milliseconds before a new attempt is made.
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Sets the modifiable list of routes.
     * 
     * @param routes
     *            The modifiable list of routes.
     */
    public void setRoutes(RouteList routes) {
        this.routes = routes;
    }

    /**
     * Sets the routing mode. By default, it is set to the
     * {@link #MODE_FIRST_MATCH} mode.
     * 
     * @param routingMode
     *            The routing mode.
     */
    public void setRoutingMode(int routingMode) {
        this.routingMode = routingMode;
    }

    /**
     * Starts the filter and the attached routes.
     */
    @SuppressWarnings("deprecation")
    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            super.start();

            for (Route route : getRoutes()) {
                route.start();
            }

            if (getDefaultRoute() != null) {
                getDefaultRoute().start();
            }
        }
    }

    /**
     * Stops the filter and the attached routes.
     */
    @SuppressWarnings("deprecation")
    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            if (getDefaultRoute() != null) {
                getDefaultRoute().stop();
            }

            for (Route route : getRoutes()) {
                route.stop();
            }

            super.stop();
        }
    }

}
