/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs.internal.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;

/**
 * Wraps a regular expression of a &#64;{@link Path}. Instances are immutable.
 * The regular expression has no '/' t the start.
 * 
 * @author Stephan Koops
 */
public class PathRegExp {

    /**
     * Default regular expression to use, if no reg exp is given (matches every
     * character expect '/').
     * 
     * @see #defaultRegExp
     */
    private static final String DEFAULT_REG_EXP = "[^/]+?";

    /**
     * The PathRegExp with an empty path.
     */
    public static final PathRegExp EMPTY;

    private static final byte NAME_READ = 2;

    private static final byte NAME_READ_READY = 3;

    private static final byte NAME_READ_START = 1;

    private static final byte REGEXP_READ = 12;

    private static final byte REGEXP_READ_READY = 13;

    private static final byte REGEXP_READ_START = 11;

    static {
        try {
            EMPTY = new PathRegExp("", null);
        } catch (IllegalPathException e) {
            throw new RuntimeException("This could not occur", e);
        }
    }

    /**
     * Creates a {@link PathRegExp} for a root resource class.
     * 
     * @param rrc
     *            the JAX-RS root resource class
     * @return the PathRegExp from the given root resource class
     * @throws MissingAnnotationException
     *             if the {@link Path} annotation is missing.
     * @throws IllegalPathOnClassException
     *             if the {@link Path} annotation is not valid.
     * @throws IllegalArgumentException
     *             if the rrc is null.
     * @see {@link #EMPTY}
     */
    public static PathRegExp createForClass(Class<?> rrc)
            throws MissingAnnotationException, IllegalPathOnClassException,
            IllegalArgumentException {
        final Path path = Util.getPathAnnotation(rrc);
        try {
            return new PathRegExp(path.value(), path);
        } catch (IllegalPathException ipe) {
            throw new IllegalPathOnClassException(ipe);
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
     *             if the annotation on the method is invalid.
     * @throws IllegalArgumentException
     *             if the method is null.
     */
    public static PathRegExp createForMethod(Method annotatedMethod)
            throws IllegalPathOnMethodException, IllegalArgumentException {
        final Path pathAnno = Util.getPathAnnotationOrNull(annotatedMethod);
        if (pathAnno == null) {
            return EMPTY;
        }
        try {
            return new PathRegExp(pathAnno.value(), pathAnno);
        } catch (IllegalPathException ipe) {
            throw new IllegalPathOnMethodException(ipe);
        }
    }

    private final boolean emptyOrSlash;

    /** Contains the number of literal chars in this Regular Expression */
    private final Integer noLitChars;

    /**
     * Contains the number of capturing groups with regular expressions that are
     * not the default.
     * 
     * @see #DEFAULT_REG_EXP
     */
    private int noNonDefaultRegExp = 0;

    private final int noOfCapturingGroups;

    private final String pathTemplateDec;

    private final String pathTemplateEnc;

    private final Pattern pattern;

    private final List<String> varNames = new ArrayList<String>();

    /**
     * Is intended for internal use and testing. Otherwise use the static
     * methods {@link #createForClass(Class)} or
     * {@link #createForMethod(Method)}, or the constant {@link #EMPTY}.
     * 
     * @param pathPattern
     * @param pathForExcMess
     */
    private PathRegExp(String pathTemplate, Path pathForExcMess)
            throws IllegalPathException {

        // 1. URI encode the template, ignoring URI template variable specs.
        // 2. Escape any regular expression characters in the URI template,
        // again ignoring URI template variable specifications.
        // 3. Replace each URI template variable with a capturing group
        // containing the specified regular expression or "([^/]+?)" if no
        // regular expression is specified.

        // LATER regexp in @Path: @Path("{p}/abc/{p}") is allowed, p may be != p
        if (pathTemplate == null) {
            throw new IllegalArgumentException(
                    "The path template must not be null");
        }
        final int l = pathTemplate.length();
        final StringBuilder pathPattern = new StringBuilder();
        int forStart = 0;
        if (l > 0 && pathTemplate.charAt(0) == '/')
            forStart = 1;
        int noLitChars = 0;
        int numberOfCapturingGroups = 0;
        for (int i = forStart; i < l; i++) {
            final char c = pathTemplate.charAt(i);
            if (c == '{') {
                i = processTemplVarname(pathTemplate, i, pathPattern,
                        pathForExcMess);
                numberOfCapturingGroups++;
            } else if (c == '%') {
                try {
                    EncodeOrCheck.processPercent(i, true, pathTemplate,
                            pathPattern);
                } catch (IllegalArgumentException e) {
                    throw new IllegalPathException(pathForExcMess, e);
                }
            } else if (c == '}') {
                throw new IllegalPathException(pathForExcMess,
                        "'}' is only allowed as "
                                + "end of a variable name in \"" + pathTemplate
                                + "\"");
            } else if (c == ';') {
                throw new IllegalPathException(pathForExcMess,
                        "matrix parameters are not allowed in a @Path");
            } else if (!false && (c == '/')) {
                pathPattern.append(c);
                noLitChars++;
            } else {
                noLitChars += EncodeOrCheck.encode(c, pathPattern);
            }
        }
        this.noLitChars = noLitChars;
        this.noOfCapturingGroups = numberOfCapturingGroups;
        // 4. If the resulting string ends with "/" then remove the final char.
        // 5. Append "(/.*)?" to the result.
        if (pathPattern.length() > 0
                && pathPattern.charAt(pathPattern.length() - 1) != '/') {
            pathPattern.append('/');
        }
        pathPattern.append("(.*)");

        this.pattern = Pattern.compile(pathPattern.toString());
        this.emptyOrSlash = Util.isEmptyOrSlash(pathTemplate);
        if (l > 0) {
            if (pathTemplate.charAt(0) != '/') {
                pathTemplate = '/' + pathTemplate;
            }
            if (l > 1 && pathTemplate.endsWith("/")) {
                pathTemplate = pathTemplate.substring(0,
                        pathTemplate.length() - 2);
            }
        }
        this.pathTemplateEnc = pathTemplate; // LATER encode unencoded here
        this.pathTemplateDec = pathTemplate; // LATER decode here
    }

    /**
     * Compares this regular expression of a &#64;{@link Path} with the given
     * Object by comparing given patterns.
     */
    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (!(anotherObject instanceof PathRegExp)) {
            return false;
        }
        final PathRegExp otherRegExp = (PathRegExp) anotherObject;
        return this.pattern.pattern().equals(otherRegExp.pattern.pattern());
    }

    /**
     * @return the number of capturing groups with regular expressions that are
     *         not the default.
     */
    public int getNoNonDefCaprGroups() {
        return this.noNonDefaultRegExp;
    }

    /**
     * @return Returns the number of capturing groups.
     */
    public int getNoOfCapturingGroups() {
        return this.noOfCapturingGroups;
    }

    /**
     * See Footnode to JSR-311-Spec, Section 2.6, Algorithm, Part 1e
     * 
     * @return Returns the number of literal chars in the path patern
     */
    public int getNoOfLiteralChars() {
        return this.noLitChars;
    }

    /**
     * @return the decoded path template with a '/' at the beginning, and no one
     *         at the end.
     */
    public String getPathTemplateDec() {
        return this.pathTemplateDec;
    }

    /**
     * @return the encoded path template with a '/' at the beginning, and no one
     *         at the end.
     */
    public String getPathTemplateEnc() {
        return this.pathTemplateEnc;
    }

    @Override
    public int hashCode() {
        return this.pattern.pattern().hashCode();
    }

    /**
     * Checks if the URI template is empty or only a slash.
     * 
     * @return if this path regular expression is empty or "/"
     */
    public boolean isEmptyOrSlash() {
        return this.emptyOrSlash;
    }

    /**
     * Checks if this regular expression matches the given remaining path.
     * 
     * @param remainingPath
     * @return Returns an MatchingResult, if the remainingPath matches to this
     *         template, or null, if not.
     */
    public MatchingResult match(RemainingPath remainingPath) {
        String givenPath = remainingPath.getWithoutParams();
        Matcher matcher = pattern.matcher(givenPath);
        if (!matcher.matches()) {
            return null;
        }

        final Map<String, String> templateVars = new HashMap<String, String>();

        for (int i = 1; i < matcher.groupCount(); i++) {
            templateVars.put(this.varNames.get(i - 1), matcher.group(i));
        }

        String finalCapturingGroup = matcher.group(matcher.groupCount());
        // if (finalCapturingGroup.length() > 0) {
        // if (pathSuppl && finalCapturingGroup.endsWith("/")) {
        // finalCapturingGroup = finalCapturingGroup.substring(0,
        // finalCapturingGroup.length() - 1);
        // }
        // if (!finalCapturingGroup.startsWith("/")) {
        // finalCapturingGroup = "/" + finalCapturingGroup;
        // }
        // }

        int matchedChars = givenPath.length() - finalCapturingGroup.length();
        if ((matchedChars > 0) && (givenPath.charAt(matchedChars - 1) == '/')) {
            matchedChars--; // ignore '/' at end
        }
        final String matchedPart = givenPath.substring(0, matchedChars);
        return new MatchingResult(matchedPart, templateVars,
                finalCapturingGroup);
    }

    /**
     * Checks, if this regular expression matches the given path with no final
     * matching group.
     * 
     * @param remainingPath
     * @return true, if this regular expression matches exectly the given path,
     *         without a final capturing group.
     */
    public boolean matchesWithEmpty(RemainingPath remainingPath) {
        final MatchingResult matchingResult = match(remainingPath);
        if (matchingResult == null) {
            return false;
        }
        return matchingResult.getFinalCapturingGroup().isEmptyOrSlash();
    }

    /**
     * @param pathTemplate
     * @param braceIndex
     * @param pathPattern
     * @param pathForExcMess
     * @throws IllegalPathException
     */
    private int processTemplVarname(final String pathTemplate,
            final int braceIndex, final StringBuilder pathPattern,
            final Path pathForExcMess) throws IllegalPathException {
        pathPattern.append('(');
        final int l = pathTemplate.length();
        final StringBuilder varName = new StringBuilder();
        final StringBuilder regExp = new StringBuilder();
        int state = NAME_READ_START;
        for (int i = braceIndex + 1; i < l; i++) {
            final char c = pathTemplate.charAt(i);
            if (c == '{') {
                throw new IllegalPathException(pathForExcMess,
                        "A variable must not " + "contain an extra '{' in \""
                                + pathTemplate + "\"");
            } else if (c == ' ' || c == '\t') {
                if (state == NAME_READ)
                    state = NAME_READ_READY;
                else if (state == REGEXP_READ)
                    state = REGEXP_READ_READY;
                continue;
            } else if (c == ':') {
                if (state == NAME_READ_START) {
                    throw new IllegalPathException(pathForExcMess,
                            "The variable name at position must not be null at "
                                    + braceIndex + " of \"" + pathTemplate
                                    + "\"");
                }
                if (state == NAME_READ || state == NAME_READ_READY) {
                    state = REGEXP_READ_START;
                }
                continue;
            } else if (c == '}') {
                if (state == NAME_READ_START) {
                    throw new IllegalPathException(pathForExcMess,
                            "The template variable name '{}' is not allowed in "
                                    + "\"" + pathTemplate + "\"");
                } else if ((state == REGEXP_READ)
                        || (state == REGEXP_READ_READY)) {
                    pathPattern.append(regExp);
                    if (!regExp.equals(DEFAULT_REG_EXP)) {
                        this.noNonDefaultRegExp++;
                    }
                } else {
                    pathPattern.append(DEFAULT_REG_EXP);
                }
                pathPattern.append(')');
                this.varNames.add(varName.toString());
                return i;
            }

            if (state == NAME_READ_START) {
                state = NAME_READ;
                varName.append(c);
            } else if (state == NAME_READ) {
                varName.append(c);
            } else if (state == REGEXP_READ_START) {
                state = REGEXP_READ;
                regExp.append(c);
            } else if (state == REGEXP_READ) {
                regExp.append(c);
            } else {
                throw new IllegalPathException(pathForExcMess,
                        "Invalid character found at position " + i + " of \""
                                + pathTemplate + "\"");
            }
        }
        throw new IllegalPathException(pathForExcMess,
                "No '}' found after '{' " + "at position " + braceIndex
                        + " of \"" + pathTemplate + "\"");
    }

    @Override
    public String toString() {
        return this.pattern.pattern();
    }
}