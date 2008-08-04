/*
 * Copyright 2005-2008 Noelios Technologies.
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

import java.util.Map;

import javax.ws.rs.Path;

/**
 * Wraps a result of a matching of a concrete path against a path pattern.
 * 
 * @author Stephan Koops
 */
public class MatchingResult {

    private final String matched;

    private final Map<String, String> variables;

    private final RemainingPath finalCapturingGroup;

    /**
     * Creates a new MatchingResult
     * 
     * @param matched
     *                The matched uri part
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