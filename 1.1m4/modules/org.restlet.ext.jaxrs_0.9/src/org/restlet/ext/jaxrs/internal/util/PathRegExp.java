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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.util.Resolver;
import org.restlet.util.Template;
import org.restlet.util.Variable;

/**
 * Wraps a regular expression of a &#64;{@link Path}. Instances are immutable.
 * 
 * @author Stephan Koops
 */
public class PathRegExp {

    private static Resolver<String> EmptyStringVariableResolver = new Resolver<String>() {
        @Override
        public String resolve(String variableName) {
            return "";
        }
    };

    /**
     * The PathRegExp with an empty path.
     */
    public static PathRegExp EMPTY = new PathRegExp("", true);

    private static final String VARNAME_FUER_REST = "org.restlet.jaxrs.rest";

    /**
     * Creates a {@link PathRegExp} for a root resource class.
     * 
     * @param rrc
     *                the JAX-RS root resource class
     * @return the PathRegExp from the given root resource class
     * @throws MissingAnnotationException
     *                 if the {@link Path} annotation is missing.
     * @throws IllegalPathOnClassException
     *                 if the {@link Path} annotation is not valid.
     * @throws IllegalArgumentException
     *                 if the rrc is null.
     * @see {@link #EMPTY}
     */
    public static PathRegExp createForClass(Class<?> rrc)
            throws MissingAnnotationException, IllegalPathOnClassException,
            IllegalArgumentException {
        try {
            return new PathRegExp(Util.getPathAnnotation(rrc));
        } catch (IllegalPathException e) {
            throw new IllegalPathOnClassException(e);
        }
    }

    /**
     * Creates a {@link PathRegExp} for a sub resource method or sub resource
     * locator. Returns {@link #EMPTY}, if the method is not annotated with
     * &#64;Path.
     * 
     * @param annotatedMethod
     * @return the {@link PathRegExp}. Never returns null.
     * @throws IllegalPathOnMethodException
     *                 tif the annotation on the method is invalid.
     * @throws IllegalArgumentException
     *                 if the method is null.
     */
    public static PathRegExp createForMethod(Method annotatedMethod)
            throws IllegalPathOnMethodException, IllegalArgumentException {
        Path pathAnnotation = Util.getPathAnnotationOrNull(annotatedMethod);
        if (pathAnnotation == null)
            return EMPTY;
        try {
            return new PathRegExp(pathAnnotation);
        } catch (IllegalPathException e) {
            throw new IllegalPathOnMethodException(e);
        }
    }

    /**
     * get the pattern of the path. Ensures also, that it starts with a "/"
     * 
     * @param path
     * @return
     * @throws IllegalPathException
     *                 if the found {@link Path} is invalid.
     * @throws IllegalArgumentException
     *                 if the path is null.
     */
    private static String getPathPattern(Path path)
            throws IllegalArgumentException, IllegalPathException {
        if (path == null)
            throw new IllegalArgumentException("The path must not be null");
        // LATER @Path("{p}/abc/{p}") is allowed and p may be != p
        String pathPattern = Util.getPathTemplate(path);
        if (pathPattern.startsWith("/"))
            return pathPattern;
        return "/" + pathPattern;
    }

    private boolean isEmptyOrSlash;

    /** Contains the number of literal chars in this Regular Expression */
    private Integer noLitChars;

    private String pathPattern;

    private Template template;

    private PathRegExp(Path path) throws IllegalArgumentException,
            IllegalPathException {
        this(getPathPattern(path), path.limited());
    }

    /**
     * Is intended for internal use and testing. Otherwise use the static
     * methods {@link #createForClass(Class)} or
     * {@link #createForMethod(Method)}, or the constant {@link #EMPTY}.
     * 
     * @param pathPattern
     * @param limitedToOneSegment
     * @deprecated public for testing only
     */
    @Deprecated
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
        Variable defaultVariable = this.template.getDefaultVariable();
        if (limitedToOneSegment)
            defaultVariable.setType(Variable.TYPE_URI_SEGMENT);
        else
            defaultVariable.setType(Variable.TYPE_URI_PATH);

        Variable restVar = template.getVariables().get(VARNAME_FUER_REST);
        if (restVar == null) {
            restVar = new Variable(Variable.TYPE_ALL);
            template.getVariables().put(VARNAME_FUER_REST, restVar);
        }
        restVar.setRequired(false);
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof PathRegExp))
            return false;
        PathRegExp otherRegExp = (PathRegExp) anotherObject;
        return this.getWithEmptyVars().equals(otherRegExp.getWithEmptyVars());
    }

    /**
     * @return Returns the number of capturing groups.
     */
    public int getNumberOfCapturingGroups() {
        return this.template.getVariableNames().size();
    }

    /**
     * See Footnode to JSR-311-Spec, Section 2.6, Algorithm, Part 1e
     * 
     * @return Returns the number of literal chars in the path patern
     */
    public int getNumberOfLiteralChars() {
        if (noLitChars == null) {
            noLitChars = getWithEmptyVars().length();
        }
        return noLitChars;
    }

    /**
     * @return Returns the path pattern.
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * @return
     */
    private String getWithEmptyVars() {
        return this.template.format(EmptyStringVariableResolver);
    }

    @Override
    public int hashCode() {
        return this.template.hashCode();
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
     * Checks if this regular expression matches the given remaining path.
     * 
     * @param remainingPath
     * @return Returns an MatchingResult, if the remainingPath matches to this
     *         template, or null, if not.
     */
    @SuppressWarnings("unchecked")
    public MatchingResult match(RemainingPath remainingPath) {
        String givenPath = remainingPath.getWithoutParams();
        Map<String, String> templateVars = new HashMap<String, String>();
        boolean pathSuppl = !givenPath.endsWith("/");
        if (pathSuppl)
            givenPath += '/';
        boolean matches = template.parse(givenPath, (Map) templateVars) >= 0;
        if (!matches)
            return null;
        String finalCapturingGroup = templateVars.remove(VARNAME_FUER_REST);
        if (finalCapturingGroup.length() > 0) {
            if (pathSuppl && finalCapturingGroup.endsWith("/"))
                finalCapturingGroup = finalCapturingGroup.substring(0,
                        finalCapturingGroup.length() - 1);
            if (!finalCapturingGroup.startsWith("/"))
                finalCapturingGroup = "/" + finalCapturingGroup;
        }
        String matched;
        int matchedChars = givenPath.length() - finalCapturingGroup.length();
        if (matchedChars > 0 && givenPath.charAt(matchedChars - 1) == '/')
            matchedChars--;
        matched = givenPath.substring(0, matchedChars); // ignore '/' at end
        return new MatchingResult(matched, templateVars, finalCapturingGroup,
                templateVars.size());
    }

    /**
     * See JSR-311-Spec, Section 2.6, Algorithm, part 3a, point 1.
     * 
     * @param remainingPath
     * @return
     */
    public boolean matchesWithEmpty(RemainingPath remainingPath) {
        MatchingResult matchingResult = this.match(remainingPath);
        if (matchingResult == null)
            return false;
        return matchingResult.getFinalCapturingGroup().isEmptyOrSlash();
    }

    @Override
    public String toString() {
        return this.pathPattern;
    }
}
