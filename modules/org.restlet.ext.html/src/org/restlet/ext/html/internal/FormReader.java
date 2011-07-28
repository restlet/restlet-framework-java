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

    /** Indicates if the fields should be decoded. */
    private volatile boolean decoding;

    /** The separator character used between fields. */
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
     * Adds the fields into a given series.
     * 
     * @param fields
     *            The target fields series.
     */
    public void addFields(Series<FormData> fields) {
        boolean readNext = true;
        FormData field = null;

        if (this.stream != null) {
            // Let's read all form fields
            try {
                while (readNext) {
                    field = readNextField();

                    if (field != null) {
                        // Add parsed field to the form
                        fields.add(field);
                    } else {
                        // Last field parsed
                        readNext = false;
                    }
                }
            } catch (IOException ioe) {
                Context.getCurrentLogger()
                        .log(Level.WARNING,
                                "Unable to parse a form field. Skipping the remaining fields.",
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
     * Reads all the fields.
     * 
     * @return The form read.
     * @throws IOException
     *             If the fields could not be read.
     */
    public Series<FormData> read() throws IOException {
        Series<FormData> result = new Series<FormData>(FormData.class);
        FormData field = readNextField();

        while (field != null) {
            result.add(field);
            field = readNextField();
        }

        this.stream.close();
        return result;
    }

    /**
     * Reads the fields with the given name. If multiple values are found, a
     * list is returned created.
     * 
     * @param name
     *            The field name to match.
     * @return The field value or list of values.
     * @throws IOException
     *             If the field could not be read.
     */
    @SuppressWarnings("unchecked")
    public Object readField(String name) throws IOException {
        FormData field = readNextField();
        Object result = null;

        while (field != null) {
            if (field.getName().equals(name)) {
                if (result != null) {
                    List<Object> values = null;

                    if (result instanceof List) {
                        // Multiple values already found for this field
                        values = (List<Object>) result;
                    } else {
                        // Second value found for this field
                        // Create a list of values
                        values = new ArrayList<Object>();
                        values.add(result);
                        result = values;
                    }

                    if (field.getValue() == null) {
                        values.add(Series.EMPTY_VALUE);
                    } else {
                        values.add(field.getValue());
                    }
                } else {
                    if (field.getValue() == null) {
                        result = Series.EMPTY_VALUE;
                    } else {
                        result = field.getValue();
                    }
                }
            }

            field = readNextField();
        }

        this.stream.close();
        return result;
    }

    /**
     * Reads the fields whose name is a key in the given map. If a matching
     * field is found, its value is put in the map. If multiple values are
     * found, a list is created and set in the map.
     * 
     * @param fields
     *            The fields map controlling the reading.
     * @throws IOException
     *             If the fields could not be read.
     */
    @SuppressWarnings("unchecked")
    public void readFields(Map<String, Object> fields) throws IOException {
        FormData field = readNextField();
        Object currentValue = null;

        while (field != null) {
            if (fields.containsKey(field.getName())) {
                currentValue = fields.get(field.getName());

                if (currentValue != null) {
                    List<Object> values = null;

                    if (currentValue instanceof List) {
                        // Multiple values already found for this field
                        values = (List<Object>) currentValue;
                    } else {
                        // Second value found for this field
                        // Create a list of values
                        values = new ArrayList<Object>();
                        values.add(currentValue);
                        fields.put(field.getName(), values);
                    }

                    if (field.getValue() == null) {
                        values.add(Series.EMPTY_VALUE);
                    } else {
                        values.add(field.getValue());
                    }
                } else {
                    if (field.getValue() == null) {
                        fields.put(field.getName(), Series.EMPTY_VALUE);
                    } else {
                        fields.put(field.getName(), field.getValue());
                    }
                }
            }

            field = readNextField();
        }

        this.stream.close();
    }

    /**
     * Reads the first field with the given name.
     * 
     * @param name
     *            The field name to match.
     * @return The field value.
     * @throws IOException
     */
    public FormData readFirstField(String name) throws IOException {
        FormData field = readNextField();
        FormData result = null;

        while ((field != null) && (result == null)) {
            if (field.getName().equals(name)) {
                result = field;
            }

            field = readNextField();
        }

        this.stream.close();
        return result;
    }

    /**
     * Reads the next field available or null.
     * 
     * @return The next field available or null.
     * @throws IOException
     *             If the next field could not be read.
     */
    public FormData readNextField() throws IOException {
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
                                    "Empty field name detected. Please check your form data");
                        }
                    } else if ((nextChar == this.separator) || (nextChar == -1)) {
                        if (nameBuffer.length() > 0) {
                            result = FormUtils.create(nameBuffer, null,
                                    this.decoding, this.characterSet);
                        } else if (nextChar == -1) {
                            // Do nothing return null preference
                        } else {
                            Context.getCurrentLogger()
                                    .fine("Empty field name detected. Please check your form data");
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
