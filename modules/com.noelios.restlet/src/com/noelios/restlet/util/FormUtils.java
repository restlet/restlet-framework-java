/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.util;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.resource.Representation;

/**
 * Representation of a Web form containing submitted parameters.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
     * @param logger
     *            The logger.
     * @param post
     *            The web form representation.
     * @param name
     *            The parameter name to match.
     * @return The parameter.
     * @throws IOException
     */
    public static Parameter getFirstParameter(Logger logger,
            Representation post, String name) throws IOException {
        if (!post.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        } else {
            return new FormReader(logger, post).readFirstParameter(name);
        }

    }

    /**
     * Reads the first parameter with the given name.
     * 
     * @param logger
     *            The logger.
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
    public static Parameter getFirstParameter(Logger logger, String query,
            String name, CharacterSet characterSet, char separator)
            throws IOException {
        return new FormReader(logger, query, characterSet, separator)
                .readFirstParameter(name);
    }

    /**
     * Reads the parameters with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param logger
     *            The logger.
     * @param form
     *            The web form representation.
     * @param name
     *            The parameter name to match.
     * @return The parameter value or list of values.
     * @throws IOException
     *             If the parameters could not be read.
     */
    public static Object getParameter(Logger logger, Representation form,
            String name) throws IOException {
        if (!form.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        } else {
            return new FormReader(logger, form).readParameter(name);
        }
    }

    /**
     * Reads the parameters with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param logger
     *            The logger.
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
    public static Object getParameter(Logger logger, String query, String name,
            CharacterSet characterSet, char separator) throws IOException {
        return new FormReader(logger, query, characterSet, separator)
                .readParameter(name);
    }

    /**
     * Reads the parameters whose name is a key in the given map.<br>
     * If a matching parameter is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param logger
     *            The logger.
     * @param post
     *            The web form representation.
     * @param parameters
     *            The parameters map controlling the reading.
     * @throws IOException
     *             If the parameters could not be read.
     */
    public static void getParameters(Logger logger, Representation post,
            Map<String, Object> parameters) throws IOException {
        if (!post.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        } else {
            new FormReader(logger, post).readParameters(parameters);
        }
    }

    /**
     * Reads the parameters whose name is a key in the given map.<br>
     * If a matching parameter is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param logger
     *            The logger.
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
    public static void getParameters(Logger logger, String parametersString,
            Map<String, Object> parameters, CharacterSet characterSet,
            char separator) throws IOException {
        new FormReader(logger, parametersString, characterSet, separator)
                .readParameters(parameters);
    }

    /**
     * Parses a post into a given form.
     * 
     * @param logger
     *            The logger.
     * @param form
     *            The target form.
     * @param post
     *            The posted form.
     */
    public static void parse(Logger logger, Form form, Representation post) {
        if (post.isAvailable()) {
            FormReader fr = null;
            try {
                fr = new FormReader(logger, post);
            } catch (final IOException ioe) {
                if (logger != null) {
                    logger.log(Level.WARNING,
                            "Unable to create a form reader. Parsing aborted.",
                            ioe);
                }
            }

            if (fr != null) {
                fr.addParameters(form);
            }
        } else {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        }
    }

    /**
     * Parses a parameters string into a given form.
     * 
     * @param logger
     *            The logger.
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
    public static void parse(Logger logger, Form form, String parametersString,
            CharacterSet characterSet, boolean decode, char separator) {
        FormReader fr = null;

        if (decode) {
            fr = new FormReader(logger, parametersString, characterSet,
                    separator);
        } else {
            fr = new FormReader(logger, parametersString, separator);
        }

        fr.addParameters(form);
    }
}
