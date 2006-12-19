/*
 * Copyright 2005-2006 Noelios Consulting.
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
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * String template with a model is based on a request. Supports both formatting
 * and parsing. The template variables can be inserted using the "{name}" syntax
 * and described using the modifiable map of variable descriptors. When no
 * descriptor is found for a given variable, the template logic uses its default
 * variable property initialized using the default {@link Variable} constructor.
 * 
 * <table>
 * <tr>
 * <th>Model property</th>
 * <th>Variable name</th>
 * <th>Content type</th>
 * </tr>
 * <tr>
 * <td>request.confidential</td>
 * <td>c</td>
 * <td>boolean (true|false)</td>
 * </tr>
 * <tr>
 * <td>request.clientInfo.address</td>
 * <td>cia</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.clientInfo.agent</td>
 * <td>cig</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.challengeResponse.identifier</td>
 * <td>cri</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.challengeResponse.scheme</td>
 * <td>crs</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.entity.characterSet</td>
 * <td>ecs</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>response.entity.characterSet</td>
 * <td>ECS</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.entity.encoding</td>
 * <td>ee</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>response.entity.encoding</td>
 * <td>EE</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.entity.expirationDate</td>
 * <td>eed</td>
 * <td>Date (HTTP format)</td>
 * </tr>
 * <tr>
 * <td>response.entity.expirationDate</td>
 * <td>EED</td>
 * <td>Date (HTTP format)</td>
 * </tr>
 * <tr>
 * <td>request.entity.language</td>
 * <td>el</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>response.entity.language</td>
 * <td>EL</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.entity.modificationDate</td>
 * <td>emd</td>
 * <td>Date (HTTP format)</td>
 * </tr>
 * <tr>
 * <td>response.entity.modificationDate</td>
 * <td>EMD</td>
 * <td>Date (HTTP format)</td>
 * </tr>
 * <tr>
 * <td>request.entity.mediaType</td>
 * <td>emt</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>response.entity.mediaType</td>
 * <td>EMT</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.entity.size</td>
 * <td>es</td>
 * <td>Integer</td>
 * </tr>
 * <tr>
 * <td>response.entity.size</td>
 * <td>ES</td>
 * <td>Integer</td>
 * </tr>
 * <tr>
 * <td>request.entity.tag</td>
 * <td>et</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>response.entity.tag</td>
 * <td>ET</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.referrerRef</td>
 * <td>f*</td>
 * <td>Reference (see table below variable name sub-parts)</td>
 * </tr>
 * <tr>
 * <td>request.hostRef</td>
 * <td>h*</td>
 * <td>Reference (see table below variable name sub-parts)</td>
 * </tr>
 * <tr>
 * <td>request.method</td>
 * <td>m</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.protocol</td>
 * <td>p</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>request.resourceRef</td>
 * <td>r*</td>
 * <td>Reference (see table below variable name sub-parts)</td>
 * </tr>
 * <tr>
 * <td>response.redirectRef</td>
 * <td>R*</td>
 * <td>Reference (see table below variable name sub-parts)</td>
 * </tr>
 * <tr>
 * <td>response.status</td>
 * <td>S</td>
 * <td>Integer</td>
 * </tr>
 * <tr>
 * <td>response.serverInfo.address</td>
 * <td>SIA</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>response.serverInfo.agent</td>
 * <td>SIG</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>response.serverInfo.port</td>
 * <td>SIP</td>
 * <td>Integer</td>
 * </tr>
 * </table> <br/>
 * 
 * Below is the list of name sub-parts, for Reference variables, that can
 * replace the asterix in the variable names above:<br/><br/>
 * 
 * <table>
 * <tr>
 * <th>Reference property</th>
 * <th>Sub-part name</th>
 * <th>Content type</th>
 * </tr>
 * <tr>
 * <td>authority</td>
 * <td>a</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>baseRef</td>
 * <td>b*</td>
 * <td>Reference</td>
 * </tr>
 * <tr>
 * <td>relativePart</td>
 * <td>e</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>fragment</td>
 * <td>f</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>hostIdentifier</td>
 * <td>h</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>identifier</td>
 * <td>i</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>path</td>
 * <td>p</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>query</td>
 * <td>q</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>remainingPart</td>
 * <td>r</td>
 * <td>String</td>
 * </tr>
 * </table>
 * 
 * @see <a href="http://bitworking.org/projects/URI-Templates/">URI Template
 *      specification</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Template {
    public static final int MODE_STARTS_WITH = 1;

    public static final int MODE_EQUALS = 2;

    /**
     * Indicates if the given character is alphabetical (a-z or A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is alphabetical (a-z or A-Z).
     */
    private static boolean isAlpha(int character) {
        return isUpperCase(character) || isLowerCase(character);
    }

    /**
     * Indicates if the given character is a digit (0-9).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a digit (0-9).
     */
    private static boolean isDigit(int character) {
        return (character >= '0') && (character <= '9');
    }

    /**
     * Indicates if the given character is lower case (a-z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is lower case (a-z).
     */
    private static boolean isLowerCase(int character) {
        return (character >= 'a') && (character <= 'z');
    }

    /**
     * Indicates if the given character is an unreserved URI character.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is an unreserved URI character.
     */
    private static boolean isUnreserved(int character) {
        return isAlpha(character) || isDigit(character) || (character == '-')
                || (character == '.') || (character == '_')
                || (character == '~');
    }

    /**
     * Indicates if the given character is upper case (A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is upper case (A-Z).
     */
    private static boolean isUpperCase(int character) {
        return (character >= 'A') && (character <= 'Z');
    }

    /** The pattern to use for formatting or parsing. */
    private String pattern;

    /** The default variable to use when no matching variable descriptor exists. */
    private Variable defaultVariable;

    /** The logger to use. */
    private Logger logger;

    /** The matching mode to use when parsing a formatted reference. */
    private int matchingMode;

    /** The map of variables associated to the route's template. */
    private Map<String, Variable> variables;

    /** The internal Regex pattern. */
    private Pattern regexPattern;

    /** The sequence of Regex variable names as found in the pattern string. */
    private List<String> regexVariables;

    /**
     * Default constructor. Each variable matches any sequence of characters by
     * default. When parsing, the template will attempt to match the whole
     * template. When formatting, the variable are replaced by an empty string
     * if they don't exist in the model.
     * 
     * @param logger
     *            The logger to use.
     * @param pattern
     *            The pattern to use for formatting or parsing.
     */
    public Template(Logger logger, String pattern) {
        this(logger, pattern, MODE_EQUALS, Variable.TYPE_ALL, "", true, false);
    }

    /**
     * Constructor.
     * 
     * @param logger
     *            The logger to use.
     * @param pattern
     *            The pattern to use for formatting or parsing.
     * @param matchingMode
     *            The matching mode to use when parsing a formatted reference.
     */
    public Template(Logger logger, String pattern, int matchingMode) {
        this(logger, pattern, matchingMode, Variable.TYPE_ALL, "", true, false);
    }

    /**
     * Constructor.
     * 
     * @param logger
     *            The logger to use.
     * @param pattern
     *            The pattern to use for formatting or parsing.
     * @param matchingMode
     *            The matching mode to use when parsing a formatted reference.
     * @param defaultType
     *            The default type of variables with no descriptor.
     * @param defaultDefaultValue
     *            The default value for null variables with no descriptor.
     * @param defaultRequired
     *            The default required flag for variables with no descriptor.
     * @param defaultFixed
     *            The default fixed value for variables with no descriptor.
     */
    public Template(Logger logger, String pattern, int matchingMode,
            int defaultType, String defaultDefaultValue,
            boolean defaultRequired, boolean defaultFixed) {
        this.logger = logger;
        this.pattern = pattern;
        this.defaultVariable = new Variable(defaultType, defaultDefaultValue,
                defaultRequired, defaultFixed);
        this.matchingMode = matchingMode;
        this.variables = new TreeMap<String, Variable>();
        this.regexPattern = null;
        this.regexVariables = null;
    }

    /**
     * Creates a formatted string based on the given request.
     * 
     * @param request
     *            The request to use as a model.
     * @param response
     *            The response to use as a model.
     * @return The formatted string.
     */
    public String format(Request request, Response response) {
        StringBuilder result = new StringBuilder();
        StringBuilder varBuffer = null;
        char next;
        boolean inVariable = false;
        for (int i = 0; i < getPattern().length(); i++) {
            next = getPattern().charAt(i);

            if (inVariable) {
                if (isUnreserved(next)) {
                    // Append to the variable name
                    varBuffer.append(next);
                } else if (next == '}') {
                    // End of variable detected
                    if (varBuffer.length() == 0) {
                        getLogger().warning(
                                "Empty pattern variables are not allowed : "
                                        + this.regexPattern);
                    } else {
                        String varName = varBuffer.toString();
                        result.append(getVariableContent(varName, request,
                                response));

                        // Reset the variable name buffer
                        varBuffer = new StringBuilder();
                    }
                    inVariable = false;
                } else {
                    getLogger().warning(
                            "An invalid character was detected inside a pattern variable : "
                                    + this.regexPattern);
                }
            } else {
                if (next == '{') {
                    inVariable = true;
                    varBuffer = new StringBuilder();
                } else if (next == '}') {
                    getLogger().warning(
                            "An invalid character was detected inside a pattern variable : "
                                    + this.regexPattern);
                } else {
                    result.append(next);
                }
            }
        }

        return result.toString();
    }

    /**
     * Returns the default variable.
     * 
     * @return The default variable.
     */
    public Variable getDefaultVariable() {
        return this.defaultVariable;
    }

    /**
     * Returns the logger to use.
     * 
     * @return The logger to use.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Returns the matching mode to use when parsing a formatted reference.
     * 
     * @return The matching mode to use when parsing a formatted reference.
     */
    public int getMatchingMode() {
        return this.matchingMode;
    }

    /**
     * Returns the pattern to use for formatting or parsing.
     * 
     * @return The pattern to use for formatting or parsing.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Compiles the URI pattern into a Regex pattern.
     * 
     * @param uriPattern
     *            The URI pattern.
     * @return The Regex pattern.
     */
    private Pattern getRegexPattern() {
        if (this.regexPattern == null) {
            StringBuilder patternBuffer = new StringBuilder();
            StringBuilder varBuffer = null;
            char next;
            boolean inVariable = false;
            for (int i = 0; i < getPattern().length(); i++) {
                next = getPattern().charAt(i);

                if (inVariable) {
                    if (isUnreserved(next)) {
                        // Append to the variable name
                        varBuffer.append(next);
                    } else if (next == '}') {
                        // End of variable detected
                        if (varBuffer.length() == 0) {
                            getLogger().warning(
                                    "Empty pattern variables are not allowed : "
                                            + this.regexPattern);
                        } else {
                            String varName = varBuffer.toString();
                            int varIndex = getRegexVariables().indexOf(varName);

                            if (varIndex != -1) {
                                // The variable is used several times in the
                                // pattern, ensure that this constraint is
                                // enforced when parsing.
                                patternBuffer.append("\\" + varIndex);
                            } else {
                                // New variable detected. Insert a capturing
                                // group.
                                getRegexVariables().add(varName);
                                Variable var = getVariables().get(varName);
                                if (var == null)
                                    var = getDefaultVariable();
                                patternBuffer.append(getVariableRegex(var));
                            }

                            // Reset the variable name buffer
                            varBuffer = new StringBuilder();
                        }
                        inVariable = false;

                    } else {
                        getLogger().warning(
                                "An invalid character was detected inside a pattern variable : "
                                        + this.regexPattern);
                    }
                } else {
                    if (next == '{') {
                        inVariable = true;
                        varBuffer = new StringBuilder();
                    } else if (next == '}') {
                        getLogger().warning(
                                "An invalid character was detected inside a pattern variable : "
                                        + this.regexPattern);
                    } else {
                        patternBuffer.append(next);
                    }
                }
            }

            this.regexPattern = Pattern.compile(patternBuffer.toString());
        }

        return this.regexPattern;
    }

    /**
     * Returns the sequence of Regex variable names as found in the pattern
     * string.
     * 
     * @return The sequence of Regex variable names as found in the pattern
     *         string.
     */
    private List<String> getRegexVariables() {
        if (this.regexVariables == null)
            this.regexVariables = new ArrayList<String>();
        return this.regexVariables;
    }

    /**
     * Returns the content corresponding to a variable.
     * 
     * @param variableName
     *            The variable name.
     * @param request
     *            The request to use as a model.
     * @param response
     *            The response to use as a model.
     * @return The content corresponding to a variable.
     */
    private String getVariableContent(String variableName, Request request,
            Response response) {
        String result = null;

        Variable var = getVariables().get(variableName);
        if (var == null)
            var = getDefaultVariable();

        // Check for a matching request attribute
        if (request != null) {
            Object variable = request.getAttributes().get(variableName);
            if (variable != null) {
                result = variable.toString();
            }
        }

        // Check for a matching response attribute
        if ((result == null) && (response != null)
                && response.getAttributes().containsKey(variableName)) {
            result = response.getAttributes().get(variableName).toString();
        }

        // Check for a matching request or response property
        if (result == null) {
            if (request != null) {
                if (variableName.equals("c")) {
                    result = Boolean.toString(request.isConfidential());
                } else if (variableName.equals("cia")) {
                    result = request.getClientInfo().getAddress();
                } else if (variableName.equals("cig")) {
                    result = request.getClientInfo().getAgent();
                } else if (variableName.equals("cri")) {
                    result = request.getChallengeResponse().getIdentifier();
                } else if (variableName.equals("crs")) {
                    if (request.getChallengeResponse().getScheme() != null) {
                        result = request.getChallengeResponse().getScheme()
                                .getTechnicalName();
                    }
                } else if (variableName.equals("ecs")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getCharacterSet() != null)) {
                        result = request.getEntity().getCharacterSet()
                                .getName();
                    }
                } else if (variableName.equals("ee")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getEncoding() != null)) {
                        result = request.getEntity().getEncoding().getName();
                    }
                } else if (variableName.equals("eed")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getExpirationDate() != null)) {
                        result = DateUtils.format(request.getEntity()
                                .getExpirationDate(), DateUtils.FORMAT_RFC_1123
                                .get(0));
                    }
                } else if (variableName.equals("el")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getLanguage() != null)) {
                        result = request.getEntity().getLanguage().getName();
                    }
                } else if (variableName.equals("emd")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getModificationDate() != null)) {
                        result = DateUtils.format(request.getEntity()
                                .getModificationDate(),
                                DateUtils.FORMAT_RFC_1123.get(0));
                    }
                } else if (variableName.equals("emt")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getMediaType() != null)) {
                        result = request.getEntity().getMediaType().getName();
                    }
                } else if (variableName.equals("es")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getSize() != -1)) {
                        result = Long.toString(request.getEntity().getSize());
                    }
                } else if (variableName.equals("et")) {
                    if ((request.getEntity() != null)
                            && (request.getEntity().getTag() != null)) {
                        result = request.getEntity().getTag().getName();
                    }
                } else if (variableName.startsWith("f")) {
                    result = getReferenceContent(variableName.substring(1),
                            request.getReferrerRef());
                } else if (variableName.startsWith("h")) {
                    result = getReferenceContent(variableName.substring(1),
                            request.getHostRef());
                } else if (variableName.equals("m")) {
                    if (request.getMethod() != null) {
                        result = request.getMethod().getName();
                    }
                } else if (variableName.equals("p")) {
                    if (request.getProtocol() != null) {
                        result = request.getProtocol().getName();
                    }
                } else if (variableName.startsWith("r")) {
                    result = getReferenceContent(variableName.substring(1),
                            request.getResourceRef());
                }
            }

            if ((result == null) && (response != null)) {
                if (variableName.equals("ECS")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getCharacterSet() != null)) {
                        result = response.getEntity().getCharacterSet()
                                .getName();
                    }
                } else if (variableName.equals("EE")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getEncoding() != null)) {
                        result = response.getEntity().getEncoding().getName();
                    }
                } else if (variableName.equals("EED")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getExpirationDate() != null)) {
                        result = DateUtils.format(response.getEntity()
                                .getExpirationDate(), DateUtils.FORMAT_RFC_1123
                                .get(0));
                    }
                } else if (variableName.equals("EL")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getLanguage() != null)) {
                        result = response.getEntity().getLanguage().getName();
                    }
                } else if (variableName.equals("EMD")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getModificationDate() != null)) {
                        result = DateUtils.format(response.getEntity()
                                .getModificationDate(),
                                DateUtils.FORMAT_RFC_1123.get(0));
                    }
                } else if (variableName.equals("EMT")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getMediaType() != null)) {
                        result = response.getEntity().getMediaType().getName();
                    }
                } else if (variableName.equals("ES")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getSize() != -1)) {
                        result = Long.toString(response.getEntity().getSize());
                    }
                } else if (variableName.equals("ET")) {
                    if ((response.getEntity() != null)
                            && (response.getEntity().getTag() != null)) {
                        result = response.getEntity().getTag().getName();
                    }
                } else if (variableName.startsWith("R")) {
                    result = getReferenceContent(variableName.substring(1),
                            response.getRedirectRef());
                } else if (variableName.equals("S")) {
                    if (response.getStatus() != null) {
                        result = Integer.toString(response.getStatus()
                                .getCode());
                    }
                } else if (variableName.equals("SIA")) {
                    result = response.getServerInfo().getAddress();
                } else if (variableName.equals("SIG")) {
                    result = response.getServerInfo().getAgent();
                } else if (variableName.equals("SIP")) {
                    if (response.getServerInfo().getPort() != null) {
                        result = response.getServerInfo().getPort().toString();
                    }
                }
            }
        }

        if (result == null) {
            // Use the default value instead
            result = var.getDefaultValue();
        }

        return result;
    }

    /**
     * Returns the content corresponding to a reference property.
     * 
     * @param partName
     *            The variable sub-part name.
     * @param reference
     *            The reference to use as a model.
     * @return The content corresponding to a reference property.
     */
    private String getReferenceContent(String partName, Reference reference) {
        String result = null;

        if (reference != null) {
            if (partName.equals("a")) {
                result = reference.getAuthority();
            } else if (partName.startsWith("b")) {
                result = getReferenceContent(partName.substring(1), reference
                        .getBaseRef());
            } else if (partName.equals("e")) {
                result = reference.getRelativePart();
            } else if (partName.equals("f")) {
                result = reference.getFragment();
            } else if (partName.equals("h")) {
                result = reference.getHostIdentifier();
            } else if (partName.equals("i")) {
                result = reference.getIdentifier();
            } else if (partName.equals("p")) {
                result = reference.getPath();
            } else if (partName.equals("q")) {
                result = reference.getQuery();
            } else if (partName.equals("r")) {
                result = reference.getRemainingPart();
            }
        }

        return result;
    }

    /**
     * Returns the Regex pattern string corresponding to a variable.
     * 
     * @param variable
     *            The variable.
     * @return The Regex pattern string corresponding to a variable.
     */
    private String getVariableRegex(Variable variable) {
        String result = null;

        if (variable.isFixed()) {
            result = Pattern.quote(variable.getDefaultValue());
        } else {
            // Expressions to create character classes
            final String ALL = ".";
            final String ALPHA = "a-zA-Z";
            final String DIGIT = "0-9";
            final String ALPHA_DIGIT = ALPHA + DIGIT;
            final String HEXA = DIGIT + "ABCDEF";
            final String URI_UNRESERVED = ALPHA_DIGIT + "\\-\\.\\_\\~";
            final String URI_GEN_DELIMS = "\\:\\/\\?\\#\\[\\]\\@";
            final String URI_SUB_DELIMS = "\\!\\$\\&\\'\\(\\)\\*\\+\\,\\;\\=";
            final String URI_RESERVED = URI_GEN_DELIMS + URI_SUB_DELIMS;
            final String URI_ALL = URI_RESERVED + URI_UNRESERVED;
            final String WORD = "\\w";

            // Expressions to create non-capturing groups
            final String PCT_ENCODED = "\\%[" + HEXA + "][" + HEXA + "]";
            final String PCHAR = "[" + URI_UNRESERVED + "]|(?:" + PCT_ENCODED
                    + ")|[" + URI_SUB_DELIMS + "]|\\:|\\@";
            final String QUERY = PCHAR + "|\\/|\\?";
            final String FRAGMENT = QUERY;

            StringBuilder coreRegex = new StringBuilder();

            switch (variable.getType()) {
            case Variable.TYPE_ALL:
                appendClass(coreRegex, ALL, variable.isRequired());
                break;
            case Variable.TYPE_ALPHA:
                appendClass(coreRegex, ALPHA, variable.isRequired());
                break;
            case Variable.TYPE_DIGIT:
                appendClass(coreRegex, DIGIT, variable.isRequired());
                break;
            case Variable.TYPE_ALPHA_DIGIT:
                appendClass(coreRegex, ALPHA_DIGIT, variable.isRequired());
                break;
            case Variable.TYPE_URI_ALL:
                appendClass(coreRegex, URI_ALL, variable.isRequired());
                break;
            case Variable.TYPE_URI_UNRESERVED:
                appendClass(coreRegex, URI_UNRESERVED, variable.isRequired());
                break;
            case Variable.TYPE_WORD:
                appendClass(coreRegex, WORD, variable.isRequired());
                break;

            case Variable.TYPE_URI_FRAGMENT:
                appendGroup(coreRegex, FRAGMENT, variable.isRequired());
                break;
            case Variable.TYPE_URI_QUERY:
                appendGroup(coreRegex, QUERY, variable.isRequired());
                break;
            case Variable.TYPE_URI_SEGMENT:
                appendGroup(coreRegex, PCHAR, variable.isRequired());
                break;
            }

            result = coreRegex.toString();
        }

        return result;
    }

    /**
     * Appends to a pattern a repeating group of a given content based on a
     * class of characters.
     * 
     * @param pattern
     *            The pattern to append to.
     * @param content
     *            The content of the group.
     * @param required
     *            Indicates if the group is required.
     */
    private void appendClass(StringBuilder pattern, String content,
            boolean required) {

        pattern.append("(");

        if (content.equals(".")) {
            // Special case for the TYPE_ALL variable type because the
            // dot looses its meaning inside a character class
            pattern.append(content);
        } else {
            pattern.append("[").append(content).append(']');

        }

        if (required) {
            pattern.append("+");
        } else {
            pattern.append("*");
        }

        pattern.append(")");
    }

    /**
     * Appends to a pattern a repeating group of a given content based on a
     * non-capturing group.
     * 
     * @param pattern
     *            The pattern to append to.
     * @param content
     *            The content of the group.
     * @param required
     *            Indicates if the group is required.
     */
    private void appendGroup(StringBuilder pattern, String content,
            boolean required) {
        pattern.append("((?:").append(content).append(')');

        if (required) {
            pattern.append("+");
        } else {
            pattern.append("*");
        }

        pattern.append(")");
    }

    /**
     * Returns the modifiable map of variables.
     * 
     * @return The modifiable map of variables.
     */
    public Map<String, Variable> getVariables() {
        return this.variables;
    }

    /**
     * Indicates if the current pattern matches the given formatted string.
     * 
     * @param formattedString
     *            The formatted string to match.
     * @return The number of matched characters or -1 if the match failed.
     */
    public int match(String formattedString) {
        int result = -1;
        Matcher matcher = getRegexPattern().matcher(formattedString);

        if ((getMatchingMode() == MODE_EQUALS) && matcher.matches()) {
            result = matcher.end();
        } else if ((getMatchingMode() == MODE_STARTS_WITH)
                && matcher.lookingAt()) {
            result = matcher.end();
        }

        return result;
    }

    /**
     * Attempts to parse a formatted reference. If the parsing suceeds, the
     * given request's attributes are updated.
     * 
     * @param formattedString
     *            The string to parse.
     * @param request
     *            The request to update.
     * @return The number of matched characters or -1 if no character matched.
     */
    public int parse(String formattedString, Request request) {
        int result = -1;
        Matcher matcher = getRegexPattern().matcher(formattedString);
        boolean matched = ((getMatchingMode() == MODE_EQUALS) && matcher
                .matches())
                || ((getMatchingMode() == MODE_STARTS_WITH) && matcher
                        .lookingAt());

        if (matched) {
            // Update the number of matched characters
            result = matcher.end();

            // Update the attributes with the variables value
            String attributeName = null;
            String attributeValue = null;
            for (int i = 0; i < getRegexVariables().size(); i++) {
                attributeName = getRegexVariables().get(i);
                attributeValue = matcher.group(i + 1);
                request.getAttributes().put(attributeName, attributeValue);
            }
        }

        return result;
    }

    /**
     * Sets the pattern to use for formatting or parsing.
     * 
     * @param pattern
     *            The pattern to use for formatting or parsing.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

}
