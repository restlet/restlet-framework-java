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

package org.restlet.engine.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Representation of a Web form containing submitted parameters.
 * 
 * @author Jerome Louvel
 */
public class FormUtils {

    /**
     * Creates a parameter.
     * 
     * @param name
     *            The parameter name buffer.
     * @param value
     *            The parameter value buffer (can be null).
     * @param decode
     *            If true, the name and values are decoded with the given
     *            {@link CharacterSet}, if false, than nothing is decoded.
     * @param characterSet
     *            The supported character encoding.
     * @return The created parameter.
     */
    public static Parameter create(CharSequence name, CharSequence value,
            boolean decode, CharacterSet characterSet) {
        Parameter result = null;

        if (name != null) {
            String nameStr;
            if (decode) {
                nameStr = Reference.decode(name.toString(), characterSet);
            } else {
                nameStr = name.toString();
            }
            if (value != null) {
                String valueStr;
                if (decode) {
                    valueStr = Reference.decode(value.toString(), characterSet);
                } else {
                    valueStr = value.toString();
                }
                result = new Parameter(nameStr, valueStr);
            } else {
                result = new Parameter(nameStr, null);
            }
        }
        return result;
    }

    /**
     * Reads the first parameter with the given name.
     * 
     * @param post
     *            The web form representation.
     * @param name
     *            The parameter name to match.
     * @return The parameter.
     * @throws IOException
     */
    public static Parameter getFirstParameter(Representation post, String name)
            throws IOException {
        if (!post.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        }

        return new FormReader(post).readFirstParameter(name);
    }

    /**
     * Reads the first parameter with the given name.
     * 
     * @param query
     *            The query string.
     * @param name
     *            The parameter name to match.
     * @param characterSet
     *            The supported character encoding.
     * @param separator
     *            The separator character to append between parameters.
     * @return The parameter.
     * @throws IOException
     */
    public static Parameter getFirstParameter(String query, String name,
            CharacterSet characterSet, char separator) throws IOException {
        return new FormReader(query, characterSet, separator)
                .readFirstParameter(name);
    }

    /**
     * Reads the parameters with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param form
     *            The web form representation.
     * @param name
     *            The parameter name to match.
     * @return The parameter value or list of values.
     * @throws IOException
     *             If the parameters could not be read.
     */
    public static Object getParameter(Representation form, String name)
            throws IOException {
        if (!form.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        }

        return new FormReader(form).readParameter(name);
    }

    /**
     * Reads the parameters with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param query
     *            The query string.
     * @param name
     *            The parameter name to match.
     * @param characterSet
     *            The supported character encoding.
     * @param separator
     *            The separator character to append between parameters.
     * @return The parameter value or list of values.
     * @throws IOException
     *             If the parameters could not be read.
     */
    public static Object getParameter(String query, String name,
            CharacterSet characterSet, char separator) throws IOException {
        return new FormReader(query, characterSet, separator)
                .readParameter(name);
    }

    /**
     * Reads the parameters whose name is a key in the given map.<br>
     * If a matching parameter is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param post
     *            The web form representation.
     * @param parameters
     *            The parameters map controlling the reading.
     * @throws IOException
     *             If the parameters could not be read.
     */
    public static void getParameters(Representation post,
            Map<String, Object> parameters) throws IOException {
        if (!post.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        }

        new FormReader(post).readParameters(parameters);
    }

    /**
     * Reads the parameters whose name is a key in the given map.<br>
     * If a matching parameter is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param parametersString
     *            The query string.
     * @param parameters
     *            The parameters map controlling the reading.
     * @param characterSet
     *            The supported character encoding.
     * @param separator
     *            The separator character to append between parameters.
     * @throws IOException
     *             If the parameters could not be read.
     */
    public static void getParameters(String parametersString,
            Map<String, Object> parameters, CharacterSet characterSet,
            char separator) throws IOException {
        new FormReader(parametersString, characterSet, separator)
                .readParameters(parameters);
    }

    /**
     * Indicates if the searched parameter is specified in the given media
     * range.
     * 
     * @param searchedParam
     *            The searched parameter.
     * @param mediaRange
     *            The media range to inspect.
     * @return True if the searched parameter is specified in the given media
     *         range.
     */
    public static boolean isParameterFound(Parameter searchedParam,
            MediaType mediaRange) {
        boolean result = false;

        for (final Iterator<Parameter> iter = mediaRange.getParameters()
                .iterator(); !result && iter.hasNext();) {
            result = searchedParam.equals(iter.next());
        }

        return result;
    }

    /**
     * Parses a post into a given form.
     * 
     * @param form
     *            The target form.
     * @param post
     *            The posted form.
     */
    public static void parse(Form form, Representation post) {
        if (post != null) {
            if (post.isAvailable()) {
                FormReader fr = null;
                try {
                    fr = new FormReader(post);
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(Level.WARNING,
                            "Unable to create a form reader. Parsing aborted.",
                            ioe);
                }

                if (fr != null) {
                    fr.addParameters(form);
                }
            } else {
                throw new IllegalStateException(
                        "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
            }
        }
    }

    /**
     * Parses a parameters string into a given form.
     * 
     * @param form
     *            The target form.
     * @param parametersString
     *            The parameters string.
     * @param characterSet
     *            The supported character encoding.
     * @param decode
     *            Indicates if the query parameters should be decoded using the
     *            given character set.
     * @param separator
     *            The separator character to append between parameters.
     */
    public static void parse(Form form, String parametersString,
            CharacterSet characterSet, boolean decode, char separator) {
        if ((parametersString != null) && !parametersString.equals("")) {
            FormReader fr = null;

            if (decode) {
                fr = new FormReader(parametersString, characterSet, separator);
            } else {
                fr = new FormReader(parametersString, separator);
            }

            fr.addParameters(form);
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private FormUtils() {
    }
}
