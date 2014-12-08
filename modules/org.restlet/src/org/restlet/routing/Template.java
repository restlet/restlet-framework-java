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

package org.restlet.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.util.Resolver;

/**
 * String template with a pluggable model. Supports both formatting and parsing.
 * The template variables can be inserted using the "{name}" syntax and
 * described using the modifiable map of variable descriptors. When no
 * descriptor is found for a given variable, the template logic uses its default
 * variable property initialized using the default {@link Variable} constructor.<br>
 * <br>
 * Note that the variable descriptors can be changed before the first parsing or
 * matching call. After that point, changes won't be taken into account.<br>
 * <br>
 * Format and parsing methods are specially available to deal with requests and
 * response. See {@link #format(Request, Response)} and
 * {@link #parse(String, Request)}.
 * 
 * @see Resolver
 * @see <a href="http://code.google.com/p/uri-templates/">URI Template
 *      specification</a>
 * @author Jerome Louvel
 */
public class Template {

    /** Mode where all characters must match the template and size be identical. */
    public static final int MODE_EQUALS = 2;

    /** Mode where characters at the beginning must match the template. */
    public static final int MODE_STARTS_WITH = 1;

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
    private static void appendClass(StringBuilder pattern, String content,
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
    private static void appendGroup(StringBuilder pattern, String content,
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
     * Returns the Regex pattern string corresponding to a variable.
     * 
     * @param variable
     *            The variable.
     * @return The Regex pattern string corresponding to a variable.
     */
    private static String getVariableRegex(Variable variable) {
        String result = null;

        if (variable.isFixed()) {
            result = "(" + Pattern.quote(variable.getDefaultValue()) + ")";
        } else {
            // Expressions to create character classes
            final String ALL = ".";
            final String ALPHA = "a-zA-Z";
            final String DIGIT = "\\d";
            final String ALPHA_DIGIT = ALPHA + DIGIT;
            final String HEXA = DIGIT + "ABCDEFabcdef";
            final String URI_UNRESERVED = ALPHA_DIGIT + "\\-\\.\\_\\~";
            final String URI_GEN_DELIMS = "\\:\\/\\?\\#\\[\\]\\@";
            final String URI_SUB_DELIMS = "\\!\\$\\&\\'\\(\\)\\*\\+\\,\\;\\=";
            final String URI_RESERVED = URI_GEN_DELIMS + URI_SUB_DELIMS;
            final String WORD = "\\w";

            // Basic rules expressed by the HTTP rfc.
            final String CRLF = "\\r\\n";
            final String CTL = "\\p{Cntrl}";
            final String LWS = CRLF + "\\ \\t";
            final String SEPARATOR = "\\(\\)\\<\\>\\@\\,\\;\\:\\[\\]\"\\/\\\\?\\=\\{\\}\\ \\t";
            final String TOKEN = "[^" + SEPARATOR + "]";
            final String COMMENT = "[^" + CTL + "]" + "[^\\(\\)]" + LWS;
            final String COMMENT_ATTRIBUTE = "[^\\;\\(\\)]";

            // Expressions to create non-capturing groups
            final String PCT_ENCODED = "\\%[" + HEXA + "][" + HEXA + "]";
            // final String PCHAR = "[" + URI_UNRESERVED + "]|(?:" + PCT_ENCODED
            // + ")|[" + URI_SUB_DELIMS + "]|\\:|\\@";
            final String PCHAR = "[" + URI_UNRESERVED + URI_SUB_DELIMS
                    + "\\:\\@]|(?:" + PCT_ENCODED + ")";
            final String QUERY = PCHAR + "|\\/|\\?";
            final String FRAGMENT = QUERY;
            final String URI_PATH = PCHAR + "|\\/";
            final String URI_ALL = "[" + URI_RESERVED + URI_UNRESERVED
                    + "]|(?:" + PCT_ENCODED + ")";

            // Special case of query parameter characters
            final String QUERY_PARAM_DELIMS = "\\!\\$\\'\\(\\)\\*\\+\\,\\;";
            final String QUERY_PARAM_CHAR = "[" + URI_UNRESERVED
                    + QUERY_PARAM_DELIMS + "\\:\\@]|(?:" + PCT_ENCODED + ")";
            final String QUERY_PARAM = QUERY_PARAM_CHAR + "|\\/|\\?";

            final StringBuilder coreRegex = new StringBuilder();

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
                appendGroup(coreRegex, URI_ALL, variable.isRequired());
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
            case Variable.TYPE_URI_PATH:
                appendGroup(coreRegex, URI_PATH, variable.isRequired());
                break;
            case Variable.TYPE_URI_QUERY:
                appendGroup(coreRegex, QUERY, variable.isRequired());
                break;
            case Variable.TYPE_URI_QUERY_PARAM:
                appendGroup(coreRegex, QUERY_PARAM, variable.isRequired());
                break;
            case Variable.TYPE_URI_SEGMENT:
                appendGroup(coreRegex, PCHAR, variable.isRequired());
                break;
            case Variable.TYPE_TOKEN:
                appendClass(coreRegex, TOKEN, variable.isRequired());
                break;
            case Variable.TYPE_COMMENT:
                appendClass(coreRegex, COMMENT, variable.isRequired());
                break;
            case Variable.TYPE_COMMENT_ATTRIBUTE:
                appendClass(coreRegex, COMMENT_ATTRIBUTE, variable.isRequired());
                break;
            }

            result = coreRegex.toString();
        }

        return result;
    }

    /** The default variable to use when no matching variable descriptor exists. */
    private volatile Variable defaultVariable;

    /** True if the variables must be encoded when formatting the template. */
    private volatile boolean encodingVariables;

    /** The logger to use. */
    private volatile Logger logger;

    /** The matching mode to use when parsing a formatted reference. */
    private volatile int matchingMode;

    /** The pattern to use for formatting or parsing. */
    private volatile String pattern;

    /** The internal Regex pattern. */
    private volatile Pattern regexPattern;

    /** The sequence of Regex variable names as found in the pattern string. */
    private volatile List<String> regexVariables;

    /** The map of variables associated to the route's template. */
    private final Map<String, Variable> variables;

    /**
     * Default constructor. Each variable matches any sequence of characters by
     * default. When parsing, the template will attempt to match the whole
     * template. When formatting, the variable are replaced by an empty string
     * if they don't exist in the model.
     * 
     * @param pattern
     *            The pattern to use for formatting or parsing.
     */
    public Template(String pattern) {
        this(pattern, MODE_EQUALS, Variable.TYPE_ALL, "", true, false);
    }

    /**
     * Constructor.
     * 
     * @param pattern
     *            The pattern to use for formatting or parsing.
     * @param matchingMode
     *            The matching mode to use when parsing a formatted reference.
     */
    public Template(String pattern, int matchingMode) {
        this(pattern, matchingMode, Variable.TYPE_ALL, "", true, false);
    }

    /**
     * Constructor.
     * 
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
    public Template(String pattern, int matchingMode, int defaultType,
            String defaultDefaultValue, boolean defaultRequired,
            boolean defaultFixed) {
        this(pattern, matchingMode, defaultType, defaultDefaultValue,
                defaultRequired, defaultFixed, false);
    }

    /**
     * Constructor.
     * 
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
     * @param encodingVariables
     *            True if the variables must be encoded when formatting the
     *            template.
     */
    public Template(String pattern, int matchingMode, int defaultType,
            String defaultDefaultValue, boolean defaultRequired,
            boolean defaultFixed, boolean encodingVariables) {
        this.logger = (logger == null) ? Context.getCurrentLogger() : logger;
        this.pattern = pattern;
        this.defaultVariable = new Variable(defaultType, defaultDefaultValue,
                defaultRequired, defaultFixed);
        this.matchingMode = matchingMode;
        this.variables = new ConcurrentHashMap<String, Variable>();
        this.regexPattern = null;
        this.encodingVariables = encodingVariables;
    }

    /**
     * Creates a formatted string based on the given map of values.
     * 
     * @param values
     *            The values to use when formatting.
     * @return The formatted string.
     * @see Resolver#createResolver(Map)
     */
    public String format(Map<String, ?> values) {
        return format(Resolver.createResolver(values));
    }

    /**
     * Creates a formatted string based on the given request and response.
     * 
     * @param request
     *            The request to use as a model.
     * @param response
     *            The response to use as a model.
     * @return The formatted string.
     * @see Resolver#createResolver(Request, Response)
     */
    public String format(Request request, Response response) {
        return format(Resolver.createResolver(request, response));
    }

    /**
     * Creates a formatted string based on the given variable resolver.
     * 
     * @param resolver
     *            The variable resolver to use.
     * @return The formatted string.
     */
    public String format(Resolver<?> resolver) {
        final StringBuilder result = new StringBuilder();
        StringBuilder varBuffer = null;
        char next;
        boolean inVariable = false;
        final int patternLength = getPattern().length();
        for (int i = 0; i < patternLength; i++) {
            next = getPattern().charAt(i);

            if (inVariable) {
                if (Reference.isUnreserved(next)) {
                    // Append to the variable name
                    varBuffer.append(next);
                } else if (next == '}') {
                    // End of variable detected
                    if (varBuffer.length() == 0) {
                        getLogger().warning(
                                "Empty pattern variables are not allowed : "
                                        + this.regexPattern);
                    } else {
                        final String varName = varBuffer.toString();
                        Object varValue = resolver.resolve(varName);

                        Variable var = getVariables().get(varName);

                        // Use the default values instead
                        if (varValue == null) {
                            if (var == null) {
                                var = getDefaultVariable();
                            }

                            if (var != null) {
                                varValue = var.getDefaultValue();
                            }
                        }

                        String varValueString = (varValue == null) ? null
                                : varValue.toString();

                        if (this.encodingVariables) {
                            // In case the values must be encoded.
                            if (var != null) {
                                result.append(var.encode(varValueString));
                            } else {
                                result.append(Reference.encode(varValueString));
                            }
                        } else {
                            if ((var != null) && var.isEncodingOnFormat()) {
                                result.append(Reference.encode(varValueString));
                            } else {
                                result.append(varValueString);
                            }
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
     * @return The Regex pattern.
     */
    private Pattern getRegexPattern() {
        if (this.regexPattern == null) {
            synchronized (this) {
                if (this.regexPattern == null) {
                    getRegexVariables().clear();
                    final StringBuilder patternBuffer = new StringBuilder();
                    StringBuilder varBuffer = null;
                    char next;
                    boolean inVariable = false;
                    for (int i = 0; i < getPattern().length(); i++) {
                        next = getPattern().charAt(i);

                        if (inVariable) {
                            if (Reference.isUnreserved(next)) {
                                // Append to the variable name
                                varBuffer.append(next);
                            } else if (next == '}') {
                                // End of variable detected
                                if (varBuffer.length() == 0) {
                                    getLogger().warning(
                                            "Empty pattern variables are not allowed : "
                                                    + this.regexPattern);
                                } else {
                                    final String varName = varBuffer.toString();
                                    final int varIndex = getRegexVariables()
                                            .indexOf(varName);

                                    if (varIndex != -1) {
                                        // The variable is used several times in
                                        // the pattern, ensure that this
                                        // constraint is enforced when parsing.
                                        patternBuffer.append("\\"
                                                + (varIndex + 1));
                                    } else {
                                        // New variable detected. Insert a
                                        // capturing group.
                                        getRegexVariables().add(varName);
                                        Variable var = getVariables().get(
                                                varName);
                                        if (var == null) {
                                            var = getDefaultVariable();
                                        }
                                        patternBuffer
                                                .append(getVariableRegex(var));
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
                                patternBuffer.append(quote(next));
                            }
                        }
                    }

                    this.regexPattern = Pattern.compile(patternBuffer
                            .toString());
                }
            }
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
        // Lazy initialization with double-check.
        List<String> rv = this.regexVariables;
        if (rv == null) {
            synchronized (this) {
                rv = this.regexVariables;
                if (rv == null) {
                    this.regexVariables = rv = new CopyOnWriteArrayList<String>();
                }
            }
        }
        return rv;
    }

    /**
     * Returns the list of variable names in the template.
     * 
     * @return The list of variable names.
     */
    public List<String> getVariableNames() {
        final List<String> result = new ArrayList<String>();
        StringBuilder varBuffer = null;
        char next;
        boolean inVariable = false;
        final String pattern = getPattern();

        for (int i = 0; i < pattern.length(); i++) {
            next = pattern.charAt(i);

            if (inVariable) {
                if (Reference.isUnreserved(next)) {
                    // Append to the variable name
                    varBuffer.append(next);
                } else if (next == '}') {
                    // End of variable detected
                    if (varBuffer.length() == 0) {
                        getLogger().warning(
                                "Empty pattern variables are not allowed : "
                                        + this.pattern);
                    } else {
                        result.add(varBuffer.toString());

                        // Reset the variable name buffer
                        varBuffer = new StringBuilder();
                    }

                    inVariable = false;
                } else {
                    getLogger().warning(
                            "An invalid character was detected inside a pattern variable : "
                                    + this.pattern);
                }
            } else {
                if (next == '{') {
                    inVariable = true;
                    varBuffer = new StringBuilder();
                } else if (next == '}') {
                    getLogger().warning(
                            "An invalid character was detected inside a pattern variable : "
                                    + this.pattern);
                }
            }
        }

        return result;
    }

    /**
     * Returns the modifiable map of variable descriptors. Creates a new
     * instance if no one has been set. Note that those variables are only
     * descriptors that can influence the way parsing and formatting is done,
     * they don't contain the actual value parsed.
     * 
     * @return The modifiable map of variables.
     */
    public synchronized Map<String, Variable> getVariables() {
        return this.variables;
    }

    /**
     * Indicates if the variables must be encoded when formatting the template.
     * 
     * @return True if the variables must be encoded when formatting the
     *         template, false otherwise.
     */
    public boolean isEncodingVariables() {
        return this.encodingVariables;
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

        try {
            if (formattedString != null) {
                final Matcher matcher = getRegexPattern().matcher(
                        formattedString);

                if ((getMatchingMode() == MODE_EQUALS) && matcher.matches()) {
                    result = matcher.end();
                } else if ((getMatchingMode() == MODE_STARTS_WITH)
                        && matcher.lookingAt()) {
                    result = matcher.end();
                }
            }
        } catch (StackOverflowError soe) {
            getLogger().warning(
                    "StackOverflowError exception encountered while matching this string : "
                            + formattedString);
        }

        return result;
    }

    /**
     * Attempts to parse a formatted reference. If the parsing succeeds, the
     * given request's attributes are updated.<br>
     * Note that the values parsed are directly extracted from the formatted
     * reference and are therefore not percent-decoded.
     * 
     * @see Reference#decode(String)
     * 
     * @param formattedString
     *            The string to parse.
     * @param variables
     *            The map of variables to update.
     * @return The number of matched characters or -1 if no character matched.
     */
    public int parse(String formattedString, Map<String, Object> variables) {
        return parse(formattedString, variables, true);
    }

    /**
     * Attempts to parse a formatted reference. If the parsing succeeds, the
     * given request's attributes are updated.<br>
     * Note that the values parsed are directly extracted from the formatted
     * reference and are therefore not percent-decoded.
     * 
     * @see Reference#decode(String)
     * 
     * @param formattedString
     *            The string to parse.
     * @param variables
     *            The map of variables to update.
     * @param loggable
     *            True if the parsing should be logged.
     * @return The number of matched characters or -1 if no character matched.
     */
    public int parse(String formattedString, Map<String, Object> variables,
            boolean loggable) {
        int result = -1;

        if (formattedString != null) {
            try {
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
                        Variable var = getVariables().get(attributeName);

                        if ((var != null) && var.isDecodingOnParse()) {
                            attributeValue = Reference.decode(attributeValue);
                        }

                        if (loggable) {
                            getLogger().fine(
                                    "Template variable \"" + attributeName
                                            + "\" matched with value \""
                                            + attributeValue + "\"");
                        }

                        variables.put(attributeName, attributeValue);
                    }
                }
            } catch (StackOverflowError soe) {
                getLogger().warning(
                        "StackOverflowError exception encountered while matching this string : "
                                + formattedString);
            }
        }

        return result;
    }

    /**
     * Attempts to parse a formatted reference. If the parsing succeeds, the
     * given request's attributes are updated.<br>
     * Note that the values parsed are directly extracted from the formatted
     * reference and are therefore not percent-decoded.
     * 
     * @see Reference#decode(String)
     * 
     * @param formattedString
     *            The string to parse.
     * @param request
     *            The request to update.
     * @return The number of matched characters or -1 if no character matched.
     */
    public int parse(String formattedString, Request request) {
        return parse(formattedString, request.getAttributes(),
                request.isLoggable());
    }

    /**
     * Quotes special characters that could be taken for special Regex
     * characters.
     * 
     * @param character
     *            The character to quote if necessary.
     * @return The quoted character.
     */
    private String quote(char character) {
        switch (character) {
        case '[':
            return "\\[";
        case ']':
            return "\\]";
        case '.':
            return "\\.";
        case '\\':
            return "\\\\";
        case '$':
            return "\\$";
        case '^':
            return "\\^";
        case '?':
            return "\\?";
        case '*':
            return "\\*";
        case '|':
            return "\\|";
        case '(':
            return "\\(";
        case ')':
            return "\\)";
        case ':':
            return "\\:";
        case '-':
            return "\\-";
        case '!':
            return "\\!";
        case '<':
            return "\\<";
        case '>':
            return "\\>";
        default:
            return Character.toString(character);
        }
    }

    /**
     * Sets the variable to use, if no variable is given.
     * 
     * @param defaultVariable
     */
    public void setDefaultVariable(Variable defaultVariable) {
        this.defaultVariable = defaultVariable;
    }

    /**
     * Indicates if the variables must be encoded when formatting the template.
     * 
     * @param encodingVariables
     *            True if the variables must be encoded when formatting the
     *            template.
     */
    public void setEncodingVariables(boolean encodingVariables) {
        this.encodingVariables = encodingVariables;
    }

    /**
     * Sets the logger to use.
     * 
     * @param logger
     *            The logger to use.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets the matching mode to use when parsing a formatted reference.
     * 
     * @param matchingMode
     *            The matching mode to use when parsing a formatted reference.
     */
    public void setMatchingMode(int matchingMode) {
        this.matchingMode = matchingMode;
    }

    /**
     * Sets the pattern to use for formatting or parsing.
     * 
     * @param pattern
     *            The pattern to use for formatting or parsing.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
        this.regexPattern = null;
    }

    /**
     * Sets the modifiable map of variables.
     * 
     * @param variables
     *            The modifiable map of variables.
     */
    public void setVariables(Map<String, Variable> variables) {
        synchronized (this.variables) {
            if (variables != this.variables) {
                this.variables.clear();

                if (variables != null) {
                    this.variables.putAll(variables);
                }
            }
        }
    }

}
