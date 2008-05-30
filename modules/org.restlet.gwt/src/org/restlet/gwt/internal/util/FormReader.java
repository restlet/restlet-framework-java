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

package org.restlet.gwt.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.Form;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.util.Series;

/**
 * Form reader.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class FormReader {
    /** The encoding to use, decoding is enabled, see {@link #decode}. */
    private volatile CharacterSet characterSet;

    /** Indicates if the parameters should be decoded. */
    private volatile boolean decode;

    /** The form stream. */
    private volatile String text;

    /** The separator character used between parameters. */
    private volatile char separator;

    /**
     * Constructor.<br>
     * In case the representation does not define a character set, the UTF-8
     * character set is used.
     * 
     * @param representation
     *                The web form content.
     * @throws IOException
     *                 if the stream of the representation could not be opened.
     */
    public FormReader(Representation representation) throws Exception {
        this.decode = true;
        this.text = representation.getText();
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
     * @param parametersString
     *                The parameters string.
     */
    public FormReader(String parametersString, char separator) {
        this.decode = false;
        this.text = parametersString;
        this.characterSet = null;
        this.separator = separator;
    }

    /**
     * Constructor.
     * 
     * @param parametersString
     *                The parameters string.
     * @param characterSet
     *                The supported character encoding. Set to null to leave the
     *                data encoded.
     */
    public FormReader(String parametersString, CharacterSet characterSet,
            char separator) {
        this.decode = true;
        this.text = parametersString;
        this.characterSet = characterSet;
        this.separator = separator;
    }

    /**
     * Adds the parameters into a given form.
     * 
     * @param form
     *                The target form.
     */
    public void addParameters(Form form) {
        boolean readNext = true;
        Parameter param = null;

        // Let's read all form parameters
        try {
            while (readNext) {
                param = readNextParameter();

                if (param != null) {
                    // Add parsed parameter to the form
                    form.add(param);
                } else {
                    // Last parameter parsed
                    readNext = false;
                }
            }
        } catch (Exception ioe) {
            System.err
                    .println("Unable to parse a form parameter. Skipping the remaining parameters.");
        }
    }

    /**
     * Reads all the parameters.
     * 
     * @return The form read.
     * @throws IOException
     *                 If the parameters could not be read.
     */
    public Form read() throws Exception {
        Form result = new Form();
        Parameter param = readNextParameter();

        while (param != null) {
            result.add(param);
            param = readNextParameter();
        }

        return result;
    }

    /**
     * Reads the first parameter with the given name.
     * 
     * @param name
     *                The parameter name to match.
     * @return The parameter value.
     * @throws IOException
     */
    public Parameter readFirstParameter(String name) throws Exception {
        Parameter param = readNextParameter();
        Parameter result = null;

        while ((param != null) && (result == null)) {
            if (param.getName().equals(name)) {
                result = param;
            }

            param = readNextParameter();
        }

        return result;
    }

    /**
     * Reads the next parameter available or null.
     * 
     * @return The next parameter available or null.
     * @throws IOException
     *                 If the next parameter could not be read.
     */
    public Parameter readNextParameter() throws Exception {
        Parameter result = null;
        boolean readingName = true;
        boolean readingValue = false;
        StringBuilder nameBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();

        CharacterReader cr = new CharacterReader(this.text);
        int nextChar = 0;
        while ((result == null) && (nextChar != -1)) {
            nextChar = cr.read();

            if (readingName) {
                if (nextChar == '=') {
                    if (nameBuffer.length() > 0) {
                        readingName = false;
                        readingValue = true;
                    } else {
                        throw new Exception(
                                "Empty parameter name detected. Please check your form data");
                    }
                } else if ((nextChar == this.separator) || (nextChar == -1)) {
                    if (nameBuffer.length() > 0) {
                        result = FormUtils.create(nameBuffer, null,
                                this.decode, characterSet);
                    } else if (nextChar == -1) {
                        // Do nothing return null preference
                    } else {
                        throw new Exception(
                                "Empty parameter name detected. Please check your form data");
                    }
                } else {
                    nameBuffer.append((char) nextChar);
                }
            } else if (readingValue) {
                if ((nextChar == this.separator) || (nextChar == -1)) {
                    if (valueBuffer.length() > 0) {
                        result = FormUtils.create(nameBuffer, valueBuffer,
                                this.decode, characterSet);
                    } else {
                        result = FormUtils.create(nameBuffer, null,
                                this.decode, characterSet);
                    }
                } else {
                    valueBuffer.append((char) nextChar);
                }
            }
        }

        return result;
    }

    /**
     * Reads the parameters with the given name. If multiple values are found, a
     * list is returned created.
     * 
     * @param name
     *                The parameter name to match.
     * @return The parameter value or list of values.
     * @throws IOException
     *                 If the parameters could not be read.
     */
    @SuppressWarnings("unchecked")
    public Object readParameter(String name) throws Exception {
        Parameter param = readNextParameter();
        Object result = null;

        while (param != null) {
            if (param.getName().equals(name)) {
                if (result != null) {
                    List<Object> values = null;

                    if (result instanceof List) {
                        // Multiple values already found for this parameter
                        values = (List) result;
                    } else {
                        // Second value found for this parameter
                        // Create a list of values
                        values = new ArrayList<Object>();
                        values.add(result);
                        result = values;
                    }

                    if (param.getValue() == null) {
                        values.add(Series.EMPTY_VALUE);
                    } else {
                        values.add(param.getValue());
                    }
                } else {
                    if (param.getValue() == null) {
                        result = Series.EMPTY_VALUE;
                    } else {
                        result = param.getValue();
                    }
                }
            }

            param = readNextParameter();
        }

        return result;
    }

    /**
     * Reads the parameters whose name is a key in the given map. If a matching
     * parameter is found, its value is put in the map. If multiple values are
     * found, a list is created and set in the map.
     * 
     * @param parameters
     *                The parameters map controlling the reading.
     * @throws IOException
     *                 If the parameters could not be read.
     */
    @SuppressWarnings("unchecked")
    public void readParameters(Map<String, Object> parameters) throws Exception {
        Parameter param = readNextParameter();
        Object currentValue = null;

        while (param != null) {
            if (parameters.containsKey(param.getName())) {
                currentValue = parameters.get(param.getName());

                if (currentValue != null) {
                    List<Object> values = null;

                    if (currentValue instanceof List) {
                        // Multiple values already found for this parameter
                        values = (List) currentValue;
                    } else {
                        // Second value found for this parameter
                        // Create a list of values
                        values = new ArrayList<Object>();
                        values.add(currentValue);
                        parameters.put(param.getName(), values);
                    }

                    if (param.getValue() == null) {
                        values.add(Series.EMPTY_VALUE);
                    } else {
                        values.add(param.getValue());
                    }
                } else {
                    if (param.getValue() == null) {
                        parameters.put(param.getName(), Series.EMPTY_VALUE);
                    } else {
                        parameters.put(param.getName(), param.getValue());
                    }
                }
            }

            param = readNextParameter();
        }
    }
}
