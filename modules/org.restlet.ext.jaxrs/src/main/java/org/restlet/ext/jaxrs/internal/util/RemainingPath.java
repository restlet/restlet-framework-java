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

package org.restlet.ext.jaxrs.internal.util;

import org.restlet.engine.util.SystemUtils;

/**
 * This class contains the remaining path for a request. It does not contain any
 * matrix parameterand has no '/' t the start.
 * 
 * @author Stephan Koops
 */
public class RemainingPath implements Comparable<RemainingPath> {

    /**
     * removes the matrix parameters from the given uri path.<br>
     * Also adds a '/' at the end, if there is no slash at the end.
     * 
     * @param remainingPart
     * @return the given uri path without the matrix parameters.
     */
    private static String removeMatrixParams(String remainingPart) {
        final StringBuilder stb;
        if (remainingPart.startsWith("/"))
            stb = new StringBuilder(remainingPart.substring(1));
        else
            stb = new StringBuilder(remainingPart);
        int mpEndPos = Integer.MAX_VALUE;
        for (int i = stb.length() - 1; i >= 0; i--) {
            final char character = stb.charAt(i);
            if (character == '?') {
                stb.delete(i, Integer.MAX_VALUE);
            } else if (character == ';') {
                stb.delete(i, mpEndPos);
                mpEndPos = i;
            } else if (character == '/') {
                mpEndPos = i;
            }
        }
        if (stb.length() == 0 || stb.charAt(stb.length() - 1) != '/')
            stb.append('/');
        return stb.toString();
    }

    /** contains the given remaining path without matrix parameters. */
    private final String remainingPart;

    /**
     * Creates a new RemianingPath wrapper.
     * 
     * @param remainingPart
     */
    public RemainingPath(String remainingPart) {
        this.remainingPart = removeMatrixParams(remainingPart);
    }

    public int compareTo(RemainingPath other) {
        return this.remainingPart.compareTo(other.remainingPart);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RemainingPath)) {
            return false;
        }
        return this.remainingPart.equals(other.toString());
    }

    /**
     * Returns the path without matrix and query parameters.
     * 
     * @return the path without matrix and query parameters.
     */
    public String getWithoutParams() {
        return this.remainingPart;
    }

    @Override
    public int hashCode() {
        return SystemUtils.hashCode(this.remainingPart);
    }

    /**
     * Checks, if the remaining path is empty or slash.
     * 
     * @return true, of this remaining path is empty or '/', otherwise false.
     */
    public boolean isEmptyOrSlash() {
        return Util.isEmptyOrSlash(this.remainingPart);
    }

    @Override
    public String toString() {
        return this.remainingPart;
    }
}
