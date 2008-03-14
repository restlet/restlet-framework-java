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
     * Parses a query into a given form.
     * 
     * @param logger
     *                The logger.
     * @param form
     *                The target form.
     * @param query
     *                Query string.
     * @param characterSet
     *                The supported character encoding.
     * @param decode
     *                Indicates if the query parameters should be decoded using
     *                the given character set.
     */
    public static void parseQuery(Logger logger, Form form, String query,
            CharacterSet characterSet, boolean decode) {
        FormReader fr = null;

        if (decode) {
            fr = new FormReader(logger, query, characterSet);
        } else {
            fr = new FormReader(logger, query);
        }

        fr.addParameters(form);
    }

    /**
     * Parses a post into a given form.
     * 
     * @param logger
     *                The logger.
     * @param form
     *                The target form.
     * @param post
     *                The posted form.
     */
    public static void parsePost(Logger logger, Form form, Representation post) {
        if (post.isAvailable()) {
            FormReader fr = null;
            try {
                fr = new FormReader(logger, post);
            } catch (IOException ioe) {
                if (logger != null)
                    logger.log(Level.WARNING,
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

    /**
     * Reads the parameters whose name is a key in the given map.<br>
     * If a matching parameter is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param logger
     *                The logger.
     * @param query
     *                The query string.
     * @param parameters
     *                The parameters map controlling the reading.
     * @param characterSet
     *                The supported character encoding.
     * @throws IOException
     *                 If the parameters could not be read.
     */
    public static void getParameters(Logger logger, String query,
            Map<String, Object> parameters, CharacterSet characterSet)
            throws IOException {
        new FormReader(logger, query, characterSet).readParameters(parameters);
    }

    /**
     * Reads the parameters whose name is a key in the given map.<br>
     * If a matching parameter is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param logger
     *                The logger.
     * @param post
     *                The web form representation.
     * @param parameters
     *                The parameters map controlling the reading.
     * @throws IOException
     *                 If the parameters could not be read.
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
     * Reads the first parameter with the given name.
     * 
     * @param logger
     *                The logger.
     * @param query
     *                The query string.
     * @param name
     *                The parameter name to match.
     * @param characterSet
     *                The supported character encoding.
     * @return The parameter.
     * @throws IOException
     */
    public static Parameter getFirstParameter(Logger logger, String query,
            String name, CharacterSet characterSet) throws IOException {
        return new FormReader(logger, query, characterSet)
                .readFirstParameter(name);
    }

    /**
     * Reads the first parameter with the given name.
     * 
     * @param logger
     *                The logger.
     * @param post
     *                The web form representation.
     * @param name
     *                The parameter name to match.
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
     * Reads the parameters with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param logger
     *                The logger.
     * @param query
     *                The query string.
     * @param name
     *                The parameter name to match.
     * @param characterSet
     *                The supported character encoding.
     * @return The parameter value or list of values.
     * @throws IOException
     *                 If the parameters could not be read.
     */
    public static Object getParameter(Logger logger, String query, String name,
            CharacterSet characterSet) throws IOException {
        return new FormReader(logger, query, characterSet).readParameter(name);
    }

    /**
     * Reads the parameters with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param logger
     *                The logger.
     * @param form
     *                The web form representation.
     * @param name
     *                The parameter name to match.
     * @return The parameter value or list of values.
     * @throws IOException
     *                 If the parameters could not be read.
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
     * Creates a parameter.
     * 
     * @param name
     *                The parameter name buffer.
     * @param value
     *                The parameter value buffer (can be null).
     * @param decode
     *                If true, the name and values are decoded with the given
     *                {@link CharacterSet}, if false, than nothing is decoded.
     * @param characterSet
     *                The supported character encoding.
     * @return The created parameter.
     */
    public static Parameter create(CharSequence name, CharSequence value,
            boolean decode, CharacterSet characterSet) {
        Parameter result = null;

        if (name != null) {
            String nameStr;
            if (decode)
                nameStr = Reference.decode(name.toString(), characterSet);
            else
                nameStr = name.toString();
            if (value != null) {
                String valueStr;
                if (decode)
                    valueStr = Reference.decode(value.toString(), characterSet);
                else
                    valueStr = value.toString();
                result = new Parameter(nameStr, valueStr);
            } else {
                result = new Parameter(nameStr, null);
            }
        }
        return result;
    }
}
