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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.ext.html.FormData;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Form reader.
 * 
 * @author Jerome Louvel
 */
public class FormReader {

    /** The encoding to use, decoding is enabled, see {@link #decoding}. */
    private volatile CharacterSet characterSet;

    /** Indicates if the entries should be decoded. */
    private volatile boolean decoding;

    /** The separator character used between entries. */
    private volatile char separator;

    /** The form stream. */
    private volatile InputStream stream;

    /**
     * Constructor.<br>
     * In case the representation does not define a character set, the UTF-8
     * character set is used.
     * 
     * @param representation
     *            The web form content.
     * @throws IOException
     *             if the stream of the representation could not be opened.
     */
    public FormReader(Representation representation) throws IOException {
        this.decoding = true;
        this.stream = representation.getStream();
        this.separator = '&';

        if (representation.getCharacterSet() != null) {
            this.characterSet = representation.getCharacterSet();
        } else {
            this.characterSet = CharacterSet.UTF_8;
        }
    }

    /**
     * Constructor. Will leave the parsed data encoded.
     * 
     * @param queryString
     *            The query string.
     */
    public FormReader(String queryString, char separator) {
        this.decoding = false;
        // [ifndef gwt] line
        this.stream = new ByteArrayInputStream(queryString.getBytes());
        // [ifdef gwt] line uncomment
        // this.stream = new
        // org.restlet.engine.io.StringInputStream(queryString);
        this.characterSet = null;
        this.separator = separator;
    }

    /**
     * Constructor.
     * 
     * @param queryString
     *            The query string.
     * @param characterSet
     *            The supported character encoding. Set to null to leave the
     *            data encoded.
     */
    public FormReader(String queryString, CharacterSet characterSet,
            char separator) {
        this.decoding = true;
        // [ifndef gwt] line
        this.stream = new ByteArrayInputStream(queryString.getBytes());
        // [ifdef gwt] line uncomment
        // this.stream = new
        // org.restlet.engine.io.StringInputStream(queryString);
        this.characterSet = characterSet;
        this.separator = separator;
    }

    /**
     * Adds the entries into a given series.
     * 
     * @param entries
     *            The target series of entries.
     */
    public void addEntries(Series<FormData> entries) {
        boolean readNext = true;
        FormData entry = null;

        if (this.stream != null) {
            // Let's read all form data entries
            try {
                while (readNext) {
                    entry = readNextEntry();

                    if (entry != null) {
                        // Add parsed entry to the form
                        entries.add(entry);
                    } else {
                        // Last entry parsed
                        readNext = false;
                    }
                }
            } catch (IOException ioe) {
                Context.getCurrentLogger()
                        .log(Level.WARNING,
                                "Unable to parse a form entry. Skipping the remaining entries.",
                                ioe);
            }

            try {
                this.stream.close();
            } catch (IOException ioe) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to close the form input stream", ioe);
            }
        }
    }

    /**
     * Reads all the entries.
     * 
     * @return The form read.
     * @throws IOException
     *             If the entries could not be read.
     */
    public Series<FormData> read() throws IOException {
        Series<FormData> result = new Series<FormData>(FormData.class);
        FormData entry = readNextEntry();

        while (entry != null) {
            result.add(entry);
            entry = readNextEntry();
        }

        this.stream.close();
        return result;
    }

    /**
     * Reads the entries whose name is a key in the given map. If a matching
     * entry is found, its value is put in the map. If multiple values are
     * found, a list is created and set in the map.
     * 
     * @param entries
     *            The entries map controlling the reading.
     * @throws IOException
     *             If the entries could not be read.
     */
    @SuppressWarnings("unchecked")
    public void readEntries(Map<String, Object> entries) throws IOException {
        FormData entry = readNextEntry();
        Object currentValue = null;

        while (entry != null) {
            if (entries.containsKey(entry.getName())) {
                currentValue = entries.get(entry.getName());

                if (currentValue != null) {
                    List<Object> values = null;

                    if (currentValue instanceof List) {
                        // Multiple values already found for this entry
                        values = (List<Object>) currentValue;
                    } else {
                        // Second value found for this entry
                        // Create a list of values
                        values = new ArrayList<Object>();
                        values.add(currentValue);
                        entries.put(entry.getName(), values);
                    }

                    if (entry.getValue() == null) {
                        values.add(Series.EMPTY_VALUE);
                    } else {
                        values.add(entry.getValue());
                    }
                } else {
                    if (entry.getValue() == null) {
                        entries.put(entry.getName(), Series.EMPTY_VALUE);
                    } else {
                        entries.put(entry.getName(), entry.getValue());
                    }
                }
            }

            entry = readNextEntry();
        }

        this.stream.close();
    }

    /**
     * Reads the entries with the given name. If multiple values are found, a
     * list is returned created.
     * 
     * @param name
     *            The entry name to match.
     * @return The entry value or list of values.
     * @throws IOException
     *             If the entry could not be read.
     */
    @SuppressWarnings("unchecked")
    public Object readEntry(String name) throws IOException {
        FormData entry = readNextEntry();
        Object result = null;

        while (entry != null) {
            if (entry.getName().equals(name)) {
                if (result != null) {
                    List<Object> values = null;

                    if (result instanceof List) {
                        // Multiple values already found for this entry
                        values = (List<Object>) result;
                    } else {
                        // Second value found for this entry
                        // Create a list of values
                        values = new ArrayList<Object>();
                        values.add(result);
                        result = values;
                    }

                    if (entry.getValue() == null) {
                        values.add(Series.EMPTY_VALUE);
                    } else {
                        values.add(entry.getValue());
                    }
                } else {
                    if (entry.getValue() == null) {
                        result = Series.EMPTY_VALUE;
                    } else {
                        result = entry.getValue();
                    }
                }
            }

            entry = readNextEntry();
        }

        this.stream.close();
        return result;
    }

    /**
     * Reads the first entry with the given name.
     * 
     * @param name
     *            The entry name to match.
     * @return The entry value.
     * @throws IOException
     */
    public FormData readFirstEntry(String name) throws IOException {
        FormData entry = readNextEntry();
        FormData result = null;

        while ((entry != null) && (result == null)) {
            if (entry.getName().equals(name)) {
                result = entry;
            }

            entry = readNextEntry();
        }

        this.stream.close();
        return result;
    }

    /**
     * Reads the next entry available or null.
     * 
     * @return The next entry available or null.
     * @throws IOException
     *             If the next entry could not be read.
     */
    public FormData readNextEntry() throws IOException {
        FormData result = null;

        try {
            boolean readingName = true;
            boolean readingValue = false;
            final StringBuilder nameBuffer = new StringBuilder();
            final StringBuilder valueBuffer = new StringBuilder();

            int nextChar = 0;
            while ((result == null) && (nextChar != -1)) {
                nextChar = this.stream.read();

                if (readingName) {
                    if (nextChar == '=') {
                        if (nameBuffer.length() > 0) {
                            readingName = false;
                            readingValue = true;
                        } else {
                            throw new IOException(
                                    "Empty entry name detected. Please check your form data");
                        }
                    } else if ((nextChar == this.separator) || (nextChar == -1)) {
                        if (nameBuffer.length() > 0) {
                            result = FormUtils.create(nameBuffer, null,
                                    this.decoding, this.characterSet);
                        } else if (nextChar == -1) {
                            // Do nothing return null preference
                        } else {
                            Context.getCurrentLogger()
                                    .fine("Empty entry name detected. Please check your form data");
                        }
                    } else {
                        nameBuffer.append((char) nextChar);
                    }
                } else if (readingValue) {
                    if ((nextChar == this.separator) || (nextChar == -1)) {
                        if (valueBuffer.length() > 0) {
                            result = FormUtils.create(nameBuffer, valueBuffer,
                                    this.decoding, this.characterSet);
                        } else {
                            result = FormUtils.create(nameBuffer, null,
                                    this.decoding, this.characterSet);
                        }
                    } else {
                        valueBuffer.append((char) nextChar);
                    }
                }
            }
        } catch (UnsupportedEncodingException uee) {
            throw new IOException(
                    "Unsupported encoding. Please contact the administrator");
        }

        return result;
    }
}
