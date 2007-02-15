/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Modifiable list of routes with some helper methods. Note that this class
 * implements the java.util.List interface using the Route class as the generic
 * type. This allows you to use an instance of this class as any other
 * java.util.List, in particular all the helper methods in
 * java.util.Collections.<br/> <br/> Note that structural changes to this list
 * are synchronized.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @see java.util.Collections
 * @see java.util.List
 */
public final class RouteList extends WrapperList<Route> {
    /** The index of the last route used in the round robin mode. */
    private int lastIndex;

    /**
     * Constructor.
     */
    public RouteList() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity
     *            The initial list capacity.
     */
    public RouteList(int initialCapacity) {
        this(new ArrayList<Route>(initialCapacity));
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *            The delegate list.
     */
    public RouteList(List<Route> delegate) {
        super(delegate);
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
    public synchronized Route getBest(Request request, Response response,
            float requiredScore) {
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
    public synchronized Route getFirst(Request request, Response response,
            float requiredScore) {
        for (Route current : this) {
            if (current.score(request, response) >= requiredScore)
                return current;
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
        for (int j = (size() - 1); (j >= 0); j--) {
            if (get(j).score(request, response) >= requiredScore)
                return get(j);
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
        for (int initialIndex = lastIndex++; initialIndex != lastIndex; lastIndex++) {
            if (lastIndex == size()) {
                lastIndex = 0;
            }

            if (get(lastIndex).score(request, response) >= requiredScore)
                return get(lastIndex);
        }

        // No match found
        return null;
    }

    /**
     * Returns a random route match for a given call.
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
        int j = new Random().nextInt(size());
        if (get(j).score(request, response) >= requiredScore)
            return get(j);

        for (int initialIndex = j++; initialIndex != j; j++) {
            if (j == size()) {
                j = 0;
            }

            if (get(j).score(request, response) >= requiredScore)
                return get(j);
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
    public void removeAll(Restlet target) {
        for (int i = size() - 1; i >= 0; i--) {
            if (get(i).getNext() == target)
                remove(i);
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
    public synchronized RouteList subList(int fromIndex, int toIndex) {
        return new RouteList(getDelegate().subList(fromIndex, toIndex));
    }
}
