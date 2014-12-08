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

package org.restlet.ext.html.internal;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.html.FormData;
import org.restlet.representation.Representation;
import org.restlet.util.NamedValue;
import org.restlet.util.Series;

/**
 * Representation of a Web form containing submitted entries.
 * 
 * @author Jerome Louvel
 */
public class FormUtils {

    /**
     * Creates a form data.
     * 
     * @param name
     *            The name buffer.
     * @param value
     *            The value buffer (can be null).
     * @param decode
     *            If true, the name and values are decoded with the given
     *            {@link CharacterSet}, if false, than nothing is decoded.
     * @param characterSet
     *            The supported character encoding.
     * @return The created form data.
     */
    public static FormData create(CharSequence name, CharSequence value,
            boolean decode, CharacterSet characterSet) {
        FormData result = null;

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
                result = new FormData(nameStr, valueStr);
            } else {
                result = new FormData(nameStr, (String) null);
            }
        }
        return result;
    }

    /**
     * Reads the entries whose name is a key in the given map.<br>
     * If a matching entry is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param post
     *            The web form representation.
     * @param entries
     *            The entries map controlling the reading.
     * @throws IOException
     *             If the entries could not be read.
     */
    public static void getEntries(Representation post,
            Map<String, Object> entries) throws IOException {
        if (!post.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        }

        new FormReader(post).readEntries(entries);
    }

    /**
     * Reads the entries whose name is a key in the given map.<br>
     * If a matching entry is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param queryString
     *            The query string.
     * @param entries
     *            The entries map controlling the reading.
     * @param characterSet
     *            The supported character encoding.
     * @param separator
     *            The separator character to append between entries.
     * @throws IOException
     *             If the entries could not be read.
     */
    public static void getEntries(String queryString,
            Map<String, Object> entries, CharacterSet characterSet,
            char separator) throws IOException {
        new FormReader(queryString, characterSet, separator)
                .readEntries(entries);
    }

    /**
     * Reads the entries with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param form
     *            The web form representation.
     * @param name
     *            The name to match.
     * @return The form data or list of values.
     * @throws IOException
     *             If the entries could not be read.
     */
    public static Object getEntry(Representation form, String name)
            throws IOException {
        if (!form.isAvailable()) {
            throw new IllegalStateException(
                    "The HTML form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        }

        return new FormReader(form).readEntry(name);
    }

    /**
     * Reads the entries with the given name.<br>
     * If multiple values are found, a list is returned created.
     * 
     * @param query
     *            The query string.
     * @param name
     *            The entry name to match.
     * @param characterSet
     *            The supported character encoding.
     * @param separator
     *            The separator character to append between entries.
     * @return The entry value or list of values.
     * @throws IOException
     *             If the entries could not be read.
     */
    public static Object getEntry(String query, String name,
            CharacterSet characterSet, char separator) throws IOException {
        return new FormReader(query, characterSet, separator).readEntry(name);
    }

    /**
     * Reads the first entry with the given name.
     * 
     * @param post
     *            The web form representation.
     * @param name
     *            The name to match.
     * @return The form data entry.
     * @throws IOException
     */
    public static FormData getFirstEntry(Representation post, String name)
            throws IOException {
        if (!post.isAvailable()) {
            throw new IllegalStateException(
                    "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
        }

        return new FormReader(post).readFirstEntry(name);
    }

    /**
     * Reads the first entry with the given name.
     * 
     * @param query
     *            The query string.
     * @param name
     *            The name to match.
     * @param characterSet
     *            The supported character encoding.
     * @param separator
     *            The separator character to append between entries.
     * @return The form data entry.
     * @throws IOException
     */
    public static FormData getFirstEntry(String query, String name,
            CharacterSet characterSet, char separator) throws IOException {
        return new FormReader(query, characterSet, separator)
                .readFirstEntry(name);
    }

    /**
     * Indicates if the searched entry is specified in the given media range.
     * 
     * @param searchedEntry
     *            The searched entry.
     * @param mediaRange
     *            The media range to inspect.
     * @return True if the searched entry is specified in the given media range.
     */
    public static boolean isEntryFound(FormData searchedEntry,
            MediaType mediaRange) {
        boolean result = false;

        for (Iterator<? extends NamedValue<String>> iter = mediaRange
                .getParameters().iterator(); !result && iter.hasNext();) {
            result = searchedEntry.equals(iter.next());
        }

        return result;
    }

    /**
     * Parses a post into a given entries series.
     * 
     * @param entries
     *            The target entries series.
     * @param post
     *            The posted form.
     */
    public static void parse(Series<FormData> entries, Representation post) {
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
                    fr.addEntries(entries);
                }
            } else {
                throw new IllegalStateException(
                        "The Web form cannot be parsed as no fresh content is available. If this entity has been already read once, caching of the entity is required");
            }
        }
    }

    /**
     * Parses a entries string into a given form.
     * 
     * @param entriesSeries
     *            The target entries series.
     * @param queryString
     *            The query string.
     * @param characterSet
     *            The supported character encoding.
     * @param decode
     *            Indicates if the query string should be decoded using the
     *            given character set.
     * @param separator
     *            The separator character to append between entries.
     */
    public static void parse(Series<FormData> entriesSeries,
            String queryString, CharacterSet characterSet, boolean decode,
            char separator) {
        if ((queryString != null) && !queryString.equals("")) {
            FormReader fr = null;

            if (decode) {
                fr = new FormReader(queryString, characterSet, separator);
            } else {
                fr = new FormReader(queryString, separator);
            }

            fr.addEntries(entriesSeries);
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private FormUtils() {
    }
}
