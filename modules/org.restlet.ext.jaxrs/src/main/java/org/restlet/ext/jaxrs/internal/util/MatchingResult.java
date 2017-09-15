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

import java.util.Map;

import javax.ws.rs.Path;

/**
 * Wraps a result of a matching of a concrete path against a path pattern.
 * 
 * @author Stephan Koops
 */
public class MatchingResult {

    private final RemainingPath finalCapturingGroup;

    private final String matched;

    private final Map<String, String> variables;

    /**
     * Creates a new MatchingResult
     * 
     * @param matched
     *            The matched uri part
     * @param variables
     * @param finalCapturingGroup
     */
    public MatchingResult(String matched, Map<String, String> variables,
            String finalCapturingGroup) {
        this.matched = matched;
        this.variables = variables;
        this.finalCapturingGroup = new RemainingPath(finalCapturingGroup);
    }

    /**
     * Returns the final capturing group. Starts ever with a slash.
     * 
     * @return Returns the final capturing group.
     */
    public RemainingPath getFinalCapturingGroup() {
        return this.finalCapturingGroup;
    }

    /**
     * Returns the matched uri path.
     * 
     * @return Returns the matched uri path.
     */
    public String getMatched() {
        return this.matched;
    }

    /**
     * Returns the variables found in the given &#64;{@link Path}
     * 
     * @return Returns the variables found in the given &#64;{@link Path}
     */
    public Map<String, String> getVariables() {
        return this.variables;
    }
}
