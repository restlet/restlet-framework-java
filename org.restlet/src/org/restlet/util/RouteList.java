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

package org.restlet.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Route;

/**
 * Modifiable list of routes with some helper methods. Note that this class
 * implements the {@link List} interface using the Route class as the generic
 * type. This allows you to use an instance of this class as any other
 * {@link List}, in particular all the helper methods in {@link Collections}.<br>
 * <br>
 * Note that structural changes to this list are thread-safe, using an
 * underlying {@link CopyOnWriteArrayList}.
 * 
 * @author Jerome Louvel
 * @see java.util.Collections
 * @see java.util.List
 */
public final class RouteList extends WrapperList<Route> {
    /** The index of the last route used in the round robin mode. */
    private volatile int lastIndex;

    /**
     * Constructor.
     */
    public RouteList() {
        super(new CopyOnWriteArrayList<Route>());
        this.lastIndex = -1;
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *            The delegate list.
     */
    public RouteList(List<Route> delegate) {
        super(new CopyOnWriteArrayList<Route>(delegate));
        this.lastIndex = -1;
    }

    /**
     * Returns the best route match for a given call.
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @param requiredScore
     *            The minimum score required to have a match.
     * @return The best route match or null.
     */
    public Route getBest(Request request, Response response, float requiredScore) {
        Route result = null;
        float bestScore = 0F;
        float score;

        for (Route current : this) {
            score = current.score(request, response);

            if ((score > bestScore) && (score >= requiredScore)) {
                bestScore = score;
                result = current;
            }
        }

        return result;
    }

    /**
     * Returns the first route match for a given call.
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @param requiredScore
     *            The minimum score required to have a match.
     * @return The first route match or null.
     */
    public Route getFirst(Request request, Response response,
            float requiredScore) {
        for (Route current : this) {
            if (current.score(request, response) >= requiredScore) {
                return current;
            }
        }

        // No match found
        return null;
    }

    /**
     * Returns the last route match for a given call.
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @param requiredScore
     *            The minimum score required to have a match.
     * @return The last route match or null.
     */
    public synchronized Route getLast(Request request, Response response,
            float requiredScore) {
        for (int j = size() - 1; (j >= 0); j--) {
            final Route route = get(j);
            if (route.score(request, response) >= requiredScore) {
                return route;
            }
        }

        // No match found
        return null;
    }

    /**
     * Returns a next route match in a round robin mode for a given call.
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @param requiredScore
     *            The minimum score required to have a match.
     * @return A next route or null.
     */
    public synchronized Route getNext(Request request, Response response,
            float requiredScore) {
        if (!isEmpty()) {
            for (final int initialIndex = this.lastIndex++; initialIndex != this.lastIndex; this.lastIndex++) {
                if (this.lastIndex >= size()) {
                    this.lastIndex = 0;
                }

                final Route route = get(this.lastIndex);
                if (route.score(request, response) >= requiredScore) {
                    return route;
                }
            }
        }

        // No match found
        return null;
    }

    /**
     * Returns a random route match for a given call. Note that the current
     * implementation doesn't uniformly return routes unless they all score
     * above the required score.
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @param requiredScore
     *            The minimum score required to have a match.
     * @return A random route or null.
     */
    public synchronized Route getRandom(Request request, Response response,
            float requiredScore) {
        int length = size();

        if (length > 0) {
            int j = new Random().nextInt(length);
            Route route = get(j);

            if (route.score(request, response) >= requiredScore) {
                return route;
            }

            boolean loopedAround = false;

            do {
                if ((j == length) && (loopedAround == false)) {
                    j = 0;
                    loopedAround = true;
                }

                route = get(j++);

                if (route.score(request, response) >= requiredScore) {
                    return route;
                }
            } while ((j < length) || !loopedAround);
        }

        // No match found
        return null;
    }

    /**
     * Removes all routes routing to a given target.
     * 
     * @param target
     *            The target Restlet to detach.
     */
    public synchronized void removeAll(Restlet target) {
        for (int i = size() - 1; i >= 0; i--) {
            if (get(i).getNext() == target) {
                remove(i);
            }
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     * 
     * @param fromIndex
     *            The start position.
     * @param toIndex
     *            The end position (exclusive).
     * @return The sub-list.
     */
    @Override
    public RouteList subList(int fromIndex, int toIndex) {
        return new RouteList(getDelegate().subList(fromIndex, toIndex));
    }
}
