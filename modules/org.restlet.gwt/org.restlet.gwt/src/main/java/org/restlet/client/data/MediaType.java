/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.client.Context;
import org.restlet.client.engine.header.HeaderWriter;
import org.restlet.client.engine.util.StringUtils;
import org.restlet.client.engine.util.SystemUtils;
import org.restlet.client.util.Series;

/**
 * Metadata used to specify the format of representations. The
 * {@link #getName()} method returns a full String representation of the media
 * type including the parameters.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 * @author Jerome Louvel
 */
public final class MediaType extends Metadata {

    /**
     * Illegal ASCII characters as defined in RFC 1521.<br>
     * Keep the underscore for the ordering
     * 
     * @see http://www.ietf.org/rfc/rfc1521.txt
     */
    private static final String _TSPECIALS = "()<>@,;:/[]?=\\\"";

    /**
     * The known media types registered with {@link #register(String, String)},
     * retrievable using {@link #valueOf(String)}.<br>
     * Keep the underscore for the ordering.
     */
    private static volatile Map<String, MediaType> _types = null;

    public static final MediaType ALL = register("*/*", "All media");

    public static final MediaType APPLICATION_ALL = register("application/*",
            "All application documents");

    public static final MediaType APPLICATION_ALL_JSON = register(
            "application/*+json", "All application/*+json documents");

    public static final MediaType APPLICATION_ALL_XML = register(
            "application/*+xml", "All application/*+xml documents");

    public static final MediaType APPLICATION_ATOM = register(
            "application/atom+xml", "Atom document");














    public static final MediaType APPLICATION_JAVA_OBJECT_GWT = register(
            "application/x-java-serialized-object+gwt",
            "Java serialized object (using GWT-RPC encoder)");


    public static final MediaType APPLICATION_JAVASCRIPT = register(
            "application/x-javascript", "Javascript document");


    public static final MediaType APPLICATION_JSON = register(
            "application/json", "JavaScript Object Notation document");



    public static final MediaType APPLICATION_JSON_SMILE = register(
            "application/x-json-smile",
            "JavaScript Object Notation smile document");






    public static final MediaType APPLICATION_MSML = register(
            "application/msml+xml", "Media Server Markup Language");


















































    public static final MediaType APPLICATION_SDP = register("application/sdp",
            "Session Description Protocol");















    public static final MediaType APPLICATION_WWW_FORM = register(
            "application/x-www-form-urlencoded", "Web form (URL encoded)");

    public static final MediaType APPLICATION_XHTML = register(
            "application/xhtml+xml", "XHTML document");

    public static final MediaType APPLICATION_XMI = register(
            "application/xmi+xml", "XMI document");

    public static final MediaType APPLICATION_XML = register("application/xml",
            "XML document");


























    public static final MediaType TEXT_ALL = register("text/*", "All texts");


    public static final MediaType TEXT_CSS = register("text/css",
            "CSS stylesheet");



    public static final MediaType TEXT_HTML = register("text/html",
            "HTML document");


    public static final MediaType TEXT_JAVASCRIPT = register("text/javascript",
            "Javascript document");

    public static final MediaType TEXT_PLAIN = register("text/plain",
            "Plain text");





    public static final MediaType TEXT_URI_LIST = register("text/uri-list",
            "List of URIs");


    public static final MediaType TEXT_XML = register("text/xml", "XML text");








    /**
     * Returns the first of the most specific media type of the given array of
     * {@link MediaType}s.
     * <p>
     * Examples:
     * <ul>
     * <li>"text/plain" is more specific than "text/*" or "image/*"</li>
     * <li>"text/html" is same specific as "application/pdf" or "image/jpg"</li>
     * <li>"text/*" is same specific than "application/*" or "image/*"</li>
     * <li>"*<!---->/*" is the most unspecific MediaType</li>
     * </ul>
     * 
     * @param mediaTypes
     *            An array of media types.
     * @return The most concrete MediaType.
     * @throws IllegalArgumentException
     *             If the array is null or empty.
     */
    public static MediaType getMostSpecific(MediaType... mediaTypes)
            throws IllegalArgumentException {
        if ((mediaTypes == null) || (mediaTypes.length == 0)) {
            throw new IllegalArgumentException(
                    "You must give at least one MediaType");
        }

        if (mediaTypes.length == 1) {
            return mediaTypes[0];
        }

        MediaType mostSpecific = mediaTypes[0];

        for (int i = 1; i < mediaTypes.length; i++) {
            MediaType mediaType = mediaTypes[i];

            if (mediaType != null) {
                if (mediaType.getMainType().equals("*")) {
                    continue;
                }

                if (mostSpecific.getMainType().equals("*")) {
                    mostSpecific = mediaType;
                    continue;
                }

                if (mostSpecific.getSubType().contains("*")) {
                    mostSpecific = mediaType;
                    continue;
                }
            }
        }

        return mostSpecific;
    }

    /**
     * Returns the known media types map.
     * 
     * @return the known media types map.
     */
    private static Map<String, MediaType> getTypes() {
        if (_types == null) {
            _types = new HashMap<String, MediaType>();
        }
        return _types;
    }

    /**
     * Normalizes the specified token.
     * 
     * @param token
     *            Token to normalize.
     * @return The normalized token.
     * @throws IllegalArgumentException
     *             if <code>token</code> is not legal.
     */
    private static String normalizeToken(String token) {
        int length;
        char c;

        // Makes sure we're not dealing with a "*" token.
        token = token.trim();
        if ("".equals(token) || "*".equals(token))
            return "*";

        // Makes sure the token is RFC compliant.
        length = token.length();
        for (int i = 0; i < length; i++) {
            c = token.charAt(i);
            if (c <= 32 || c >= 127 || _TSPECIALS.indexOf(c) != -1)
                throw new IllegalArgumentException("Illegal token: " + token);
        }

        return token;
    }

    /**
     * Normalizes the specified media type.
     * 
     * @param name
     *            The name of the type to normalize.
     * @param parameters
     *            The parameters of the type to normalize.
     * @return The normalized type.
     */
    private static String normalizeType(String name,
            Series<Parameter> parameters) {
        int slashIndex;
        int colonIndex;
        String mainType;
        String subType;
        StringBuilder params = null;

        // Ignore null names (backward compatibility).
        if (name == null)
            return null;

        // Check presence of parameters
        if ((colonIndex = name.indexOf(';')) != -1) {
            params = new StringBuilder(name.substring(colonIndex));
            name = name.substring(0, colonIndex);
        }

        // No main / sub separator, assumes name/*.
        if ((slashIndex = name.indexOf('/')) == -1) {
            mainType = normalizeToken(name);
            subType = "*";
        } else {
            // Normalizes the main and sub types.
            mainType = normalizeToken(name.substring(0, slashIndex));
            subType = normalizeToken(name.substring(slashIndex + 1));
        }

        // Merge parameters taken from the name and the method argument.
        if (parameters != null && !parameters.isEmpty()) {
            try {
                if (params == null) {
                    params = new StringBuilder();
                }
                HeaderWriter<Parameter> hw = new HeaderWriter<Parameter>() {
                    @Override
                    public HeaderWriter<Parameter> append(Parameter value) {
                        return appendExtension(value);
                    }
                };
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter p = parameters.get(i);
                    hw.appendParameterSeparator();
                    hw.appendSpace();
                    hw.append(p);
                }
                params.append(hw.toString());
                hw.close();
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.INFO,
                        "Unable to parse the media type parameter", e);
            }
        }

        return (params == null) ? mainType + '/' + subType : mainType + '/'
                + subType + params.toString();
    }

    /**
     * Register a media type as a known type that can later be retrieved using
     * {@link #valueOf(String)}. If the type already exists, the existing type
     * is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
     * @return The registered media type
     */
    public static synchronized MediaType register(String name,
            String description) {

        if (!getTypes().containsKey(name)) {
            final MediaType type = new MediaType(name, description);
            getTypes().put(name, type);
        }

        return getTypes().get(name);
    }

    /**
     * Returns the media type associated to a name. If an existing constant
     * exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated media type.
     */
    public static MediaType valueOf(String name) {
        MediaType result = null;

        if (!StringUtils.isNullOrEmpty(name)) {
            result = getTypes().get(name);
            if (result == null) {
                result = new MediaType(name);
            }
        }

        return result;
    }

    /** The list of parameters. */
    private volatile Series<Parameter> parameters;

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     */
    public MediaType(String name) {
        this(name, null, "Media type or range of media types");
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param parameters
     *            The list of parameters.
     */
    public MediaType(String name, Series<Parameter> parameters) {
        this(name, parameters, "Media type or range of media types");
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param parameters
     *            The list of parameters.
     * @param description
     *            The description.
     */
    @SuppressWarnings("unchecked")
    public MediaType(String name, Series<Parameter> parameters,
            String description) {
        super(normalizeType(name, parameters), description);

        if (parameters != null) {
             this.parameters =
             org.restlet.client.engine.util.ParameterSeries.unmodifiableSeries(parameters);
        }
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
     */
    public MediaType(String name, String description) {
        this(name, null, description);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return equals(obj, false);
    }

    /**
     * Test the equality of two media types, with the possibility to ignore the
     * parameters.
     * 
     * @param obj
     *            The object to compare to.
     * @param ignoreParameters
     *            Indicates if parameters should be ignored during comparison.
     * @return True if both media types are equal.
     */
    public boolean equals(Object obj, boolean ignoreParameters) {
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // if obj isn't a mediatype or is null don't evaluate further
            if (obj instanceof MediaType) {
                final MediaType that = (MediaType) obj;
                if (getMainType().equals(that.getMainType())
                        && getSubType().equals(that.getSubType())) {
                    result = ignoreParameters
                            || getParameters().equals(that.getParameters());
                }
            }
        }

        return result;
    }

    /**
     * Returns the main type.
     * 
     * @return The main type.
     */
    public String getMainType() {
        String result = null;

        if (getName() != null) {
            int index = getName().indexOf('/');

            // Some clients appear to use name types without subtypes
            if (index == -1) {
                index = getName().indexOf(';');
            }

            if (index == -1) {
                result = getName();
            } else {
                result = getName().substring(0, index);
            }
        }

        return result;
    }

    /**
     * Returns the unmodifiable list of parameters corresponding to subtype
     * modifiers. Creates a new instance if no one has been set.
     * 
     * @return The list of parameters.
     */
    @SuppressWarnings("unchecked")
    public Series<Parameter> getParameters() {
        // Lazy initialization with double-check.
        Series<Parameter> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null) {
                    Series<Parameter> params = null;

                    if (getName() != null) {
                        int index = getName().indexOf(';');

                        if (index != -1) {
                            params = new Form(getName().substring(index + 1)
                                    .trim(), ';');
                        }
                    }

                    if (params == null) {
                         params = new
                         org.restlet.client.engine.util.ParameterSeries();
                    }

                     this.parameters = p =
                     org.restlet.client.engine.util.ParameterSeries.unmodifiableSeries(params);
                }
            }
        }
        return p;
    }

    /**
     * {@inheritDoc}<br>
     * In case the media type has parameters, this method returns the
     * concatenation of the main type and the subtype. If the subtype is not
     * equal to "*", it returns the concatenation of the main type and "*".
     * Otherwise, it returns either the {@link #ALL} media type if it is already
     * the {@link #ALL} media type, or null.
     */
    @Override
    public MediaType getParent() {
        MediaType result = null;

        if (getParameters().size() > 0) {
            result = MediaType.valueOf(getMainType() + "/" + getSubType());
        } else {
            if (getSubType().equals("*")) {
                result = equals(ALL) ? null : ALL;
            } else {
                result = MediaType.valueOf(getMainType() + "/*");
            }
        }

        return result;
    }

    /**
     * Returns the sub-type.
     * 
     * @return The sub-type.
     */
    public String getSubType() {
        String result = null;

        if (getName() != null) {
            final int slash = getName().indexOf('/');

            if (slash == -1) {
                // No subtype found, assume that all subtypes are accepted
                result = "*";
            } else {
                final int separator = getName().indexOf(';');
                if (separator == -1) {
                    result = getName().substring(slash + 1);
                } else {
                    result = getName().substring(slash + 1, separator);
                }
            }
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(super.hashCode(), getParameters());
    }

    /**
     * Indicates if a given media type is included in the current one @see
     * {@link #includes(Metadata, boolean)}. It ignores the parameters.
     * 
     * @param included
     *            The media type to test for inclusion.
     * @return True if the given media type is included in the current one.
     * @see #isCompatible(Metadata)
     */
    @Override
    public boolean includes(Metadata included) {
        return includes(included, true);
    }

    /**
     * Indicates if a given media type is included in the current one @see
     * {@link #includes(Metadata, boolean)}. The test is true if both types are
     * equal or if the given media type is within the range of the current one.
     * For example, ALL includes all media types. Parameters are ignored for
     * this comparison. A null media type is considered as included into the
     * current one. It ignores the parameters.
     * <p>
     * Examples:
     * <ul>
     * <li>TEXT_ALL.includes(TEXT_PLAIN) -> true</li>
     * <li>TEXT_PLAIN.includes(TEXT_ALL) -> false</li>
     * </ul>
     * 
     * @param included
     *            The media type to test for inclusion.
     * @return True if the given media type is included in the current one.
     * @see #isCompatible(Metadata)
     */
    public boolean includes(Metadata included, boolean ignoreParameters) {
        boolean result = equals(ALL) || equals(included);

        if (!result && (included instanceof MediaType)) {
            MediaType includedMediaType = (MediaType) included;

            if (getMainType().equals(includedMediaType.getMainType())) {
                // Both media types are different
                if (getSubType().equals(includedMediaType.getSubType())) {
                    if (ignoreParameters) {
                        result = true;
                    } else {
                        // Check parameters:
                        // Media type A includes media type B if for each param
                        // name/value pair in A, B contains the same name/value.
                        result = true;
                        for (int i = 0; result && i < getParameters().size(); i++) {
                            Parameter param = getParameters().get(i);
                            Parameter includedParam = includedMediaType
                                    .getParameters().getFirst(param.getName());

                            // If there was no param with the same name, or the
                            // param with the same name had a different value,
                            // then no match.
                            result = (includedParam != null && param.getValue()
                                    .equals(includedParam.getValue()));
                        }
                    }
                } else if (getSubType().equals("*")) {
                    result = true;
                } else if (getSubType().startsWith("*+")
                        && includedMediaType.getSubType().endsWith(
                                getSubType().substring(2))) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Checks if the current media type is concrete. A media type is concrete if
     * neither the main type nor the sub-type are equal to "*".
     * 
     * @return True if this media type is concrete.
     */
    public boolean isConcrete() {
        return !getName().contains("*");
    }

}
