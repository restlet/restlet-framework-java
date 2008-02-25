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

package org.restlet.ext.jaxrs.util;

import java.util.Map;

import javax.ws.rs.Path;


/**
 * Wraps a result of a matching of a concrete path against a path pattern.
 * @author Stephan Koops
 *
 */
public class MatchingResult {
    private Map<String, String> variables;

    private String finalMatchingGroup;

    private RemainingPath finalCapturingGroup;

    private int numberOfCapturingGroups;

    /**
     * 
     * @param variables
     * @param finalMatchingGroup
     * @param finalCapturingGroup
     * @param numberOfCapturingGroups
     */
    public MatchingResult(Map<String, String> variables,
            String finalMatchingGroup, String finalCapturingGroup,
            int numberOfCapturingGroups) {
        this.variables = variables;
        this.finalMatchingGroup = finalMatchingGroup;
        this.finalCapturingGroup = new RemainingPath(finalCapturingGroup);
        this.numberOfCapturingGroups = numberOfCapturingGroups;
    }

    /**
     * @return Returns the variables found in the given {@link Path}
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * @return returns the final matching group.
     */
    public String getFinalMatchingGroup() {
        return finalMatchingGroup;
    }

    /**
     * Starts ever with a slash
     * 
     * @return Returns the final capturing group.
     */
    public RemainingPath getFinalCapturingGroup() {
        return finalCapturingGroup;
    }

    /**
     * @return Returns the number of capturing groups.
     */
    public int getNumberOfCapturingGroups() {
        return numberOfCapturingGroups;
    }
}