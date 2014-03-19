/**
 * Copyright 2005-2014 Restlet S.A.S.
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tooling library for the Swagger extension.
 * 
 * @author Grzegorz Godlewski
 */
public class SwaggerUtils {

    /**
     * Removes doubled "/" sequences from a given String.
     * 
     * @param path
     *            The path to clean.
     * @return A String cleaned from doubled "/" sequences.
     */
    public static String cleanSlashes(String path) {
        path = path.replaceAll("/+", "/");
        return path;
    }

    /**
     * 
     * @param path
     * @return
     */
    public static String leaveOnlyPathParamNames(String path) {
        path = path.replaceAll(":[^}]*\\}", "}");

        return path;
    }

    /**
     * Splits a coma separated list of strings, into an array. If the list is
     * null or empty, it returns an empty array.
     * 
     * @param csvString
     *            The coma separated list of String.
     * @return The array of String.
     */
    public static List<String> toObjectList(String csvString) {
        if (csvString == null || csvString.isEmpty())
            return new ArrayList<String>();
        return Arrays.asList(csvString.split(","));
    }

    /**
     * Private constructor for tooling class.
     */
    private SwaggerUtils() {
    }

}
