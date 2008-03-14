/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.ext.jaxrs.internal.util;

/**
 * This class contains the remaining path for a request.
 * 
 * @author Stephan Koops
 */
public class RemainingPath implements Comparable<RemainingPath> {

    /**
     * removes the matrix parameters from the given uri path
     * 
     * @param remainingPart
     * @return the given uri path without the matrix parameters.
     */
    public static String removeMatrixParams(String remainingPart) {
        StringBuilder stb = new StringBuilder(remainingPart);
        int mpEndPos = Integer.MAX_VALUE;
        for (int i = stb.length() - 1; i >= 0; i--) {
            char character = stb.charAt(i);
            if (character == '?') {
                stb.delete(i, Integer.MAX_VALUE);
            } else if (character == ';') {
                stb.delete(i, mpEndPos);
                mpEndPos = i;
            } else if (character == '/') {
                mpEndPos = i;
            }
        }
        return stb.toString();
    }

    /**
     * Creates a new RemianingPath wrapper.
     * 
     * @param remainingPart
     */
    public RemainingPath(String remainingPart) {
        this.remainingPart = removeMatrixParams(remainingPart);
    }

    /** contains the given remaining path without matrix parameters. */
    private String remainingPart;

    @Override
    public String toString() {
        return remainingPart;
    }

    /**
     * Checks, if the remaining path is empty or slash.
     * 
     * @return
     */
    public boolean isEmptyOrSlash() {
        return Util.isEmptyOrSlash(remainingPart);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof RemainingPath))
            return false;
        return this.remainingPart.equals(other.toString());
    }

    @Override
    public int hashCode() {
        return this.remainingPart.hashCode();
    }

    public int compareTo(RemainingPath other) {
        return this.remainingPart.compareTo(other.remainingPart);
    }

    /**
     * Returns the oath without matrix and query parameters.
     * 
     * @return
     */
    public String getWithoutParams() {
        return this.remainingPart;
    }
}