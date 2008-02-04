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

package org.restlet.ext.jaxrs.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.restlet.ext.jaxrs.util.Template;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.util.Variable;

/**
 * Immutable
 * 
 * @author Stephan Koops
 * 
 */
public class PathRegExp {

    private static final String VARNAME_FUER_REST = "restlet.jaxrs.rest";

    // TODO javax.annotation.Resource as specified in JSR 250 beachten !!!

    private Template template;

    private String pathPattern;

    private boolean isEmptyOrSlash;

    /** Contains the number of literal chars in this Regular Expression */
    private Integer noLitChars;

    /**
     * 
     * @param pathPattern
     * @param limitedToOneSegment
     *                Controls whether a trailing template variable is limited
     *                to a single path segment (<code>true</code>) or not (<code>false</code>).
     *                E.g. <code>@Path("widgets/{id}")</code> would match widgets/foo but not
     *                       widgets/foo/bar whereas
     *                       <code>@Path(value="widgets/{id}", limit=false)</code> would match both.
     * @see Path#limited()
     */
    public PathRegExp(String pathPattern, boolean limitedToOneSegment) {
        this.pathPattern = pathPattern;
        this.isEmptyOrSlash = Util.isEmptyOrSlash(pathPattern);
        StringBuilder patternStb = new StringBuilder(pathPattern);
        if (!pathPattern.endsWith("/"))
            patternStb.append('/');
        patternStb.append('{');
        patternStb.append(VARNAME_FUER_REST);
        patternStb.append('}');
        this.template = new Template(patternStb.toString(),
                org.restlet.util.Template.MODE_EQUALS);
        if (limitedToOneSegment)
            this.template.getDefaultVariable().setType(
                    Variable.TYPE_URI_SEGMENT);

        Variable restVar = template.getVariables().get(VARNAME_FUER_REST);
        if (restVar == null) {
            restVar = new Variable(Variable.TYPE_ALL);
            template.getVariables().put(VARNAME_FUER_REST, restVar);
        }
        restVar.setRequired(false);
    }

    /**
     * @param givenPath
     * @return Returns an MatchingResult, if the givenPath matches to this
     *         template, or null, if not.
     */
    @SuppressWarnings("unchecked")
    public MatchingResult match(String givenPath) {
        Map<String, String> variables = new HashMap<String, String>();
        boolean pathSuppl = !givenPath.endsWith("/");
        if (pathSuppl)
            givenPath += '/';
        boolean matches = template.parse(givenPath, (Map) variables) >= 0;
        if (!matches)
            return null;
        String finalMatchingGroup = variables.remove(VARNAME_FUER_REST);
        if (finalMatchingGroup.length() > 0) {
            if (pathSuppl && finalMatchingGroup.endsWith("/"))
                finalMatchingGroup = finalMatchingGroup.substring(0,
                        finalMatchingGroup.length() - 1);
            if (!finalMatchingGroup.startsWith("/"))
                finalMatchingGroup = "/" + finalMatchingGroup;
        }
        String finalCapturingGroup = variables.get(Util.getLastElement(template
                .getVariableNames()));
        // TODO JSR311: finalCapturingGroup habe ich noch nicht richtig
        // verstanden.
        if (finalCapturingGroup == null)
            finalCapturingGroup = ""; // TODO ob das stimmt, weiss ich nicht
        finalCapturingGroup = finalMatchingGroup;
        return new MatchingResult(variables, finalMatchingGroup,
                finalCapturingGroup, variables.size());
    }

    /**
     * Checks if the URI template is empty or only a slash.
     * 
     * @return
     */
    public boolean isEmptyOrSlash() {
        return isEmptyOrSlash;
    }

    /**
     * See JSR-311-Spec, Section 2.5, Algorithmus, Teil 3a, Punkt 1.
     * 
     * @param remainingPath
     * @return
     */
    public boolean matchesWithEmpty(String remainingPath) {
        MatchingResult matchingResult = this.match(remainingPath);
        if (matchingResult == null)
            return false;
        return Util.isEmptyOrSlash(matchingResult.getFinalCapturingGroup());
    }

    /**
     * @return Returns the path pattern.
     */
    public String getPathPattern() {
        return pathPattern;
    }

    @Override
    public String toString() {
        return this.pathPattern;
    }

    /**
     * See Footnode to JSR-311-Spec, Section 2.5, Algorithm, Part 1e
     * 
     * @return Returns the number of literal chars in the path patern
     */
    public int getNumberOfLiteralChars() {
        if (noLitChars == null) {
            noLitChars = getWithEmptyVars().length();
            // TODO Jerome: a corresponding Formatter is better, because the Map
            // does not keep all Map constraints and it is a little bit faster
            // and less code.
        }
        return noLitChars;
    }

    /**
     * @return
     */
    private String getWithEmptyVars() {
        return this.template.format(EmptyStringVariableResolver);
    }

    /**
     * @return Returns the number of capturing groups.
     */
    public int getNumberOfCapturingGroups() {
        return this.template.getVariableNames().size();
    }

    @Override
    public boolean equals(Object anotherObject) {
        // TODO talk with Jerome about Template.equals(Object)
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof PathRegExp))
            return false;
        PathRegExp otherRegExp = (PathRegExp) anotherObject;
        return this.getWithEmptyVars().equals(otherRegExp.getWithEmptyVars());
    }

    @Override
    public int hashCode() {
        return this.template.hashCode();
    }

    private static EverNullVariableResolver EmptyStringVariableResolver = new EverNullVariableResolver();

    /**
     * VariableResolver that returns "" vor every variable name
     * 
     * @author Stephan Koops
     */
    private static class EverNullVariableResolver implements
            Template.VariableResolver {
        public String resolve(String variableName) {
            return "";
        }
    }
}