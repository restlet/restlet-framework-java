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

package com.noelios.restlet.util;

import java.io.IOException;
import java.util.Iterator;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.util.Series;

/**
 * Preference header reader. Works for character sets, encodings, languages or
 * media types.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class PreferenceReader<T extends Metadata> extends HeaderReader {
    public static final int TYPE_CHARACTER_SET = 1;

    public static final int TYPE_ENCODING = 2;

    public static final int TYPE_LANGUAGE = 3;

    public static final int TYPE_MEDIA_TYPE = 4;

    /** The type of metadata read. */
    private int type;

    /**
     * Constructor.
     * 
     * @param type
     *            The type of metadata read.
     * @param header
     *            The header to read.
     */
    public PreferenceReader(int type, String header) {
        super(header);
        this.type = type;
    }

    /**
     * Read the next preference.
     * 
     * @return The next preference.
     */
    public Preference<T> readPreference() throws IOException {
        Preference<T> result = null;

        boolean readingMetadata = true;
        boolean readingParamName = false;
        boolean readingParamValue = false;

        StringBuilder metadataBuffer = new StringBuilder();
        StringBuilder paramNameBuffer = null;
        StringBuilder paramValueBuffer = null;

        Series<Parameter> parameters = null;

        String nextValue = readValue();
        int nextIndex = 0;

        if (nextValue != null) {
            int nextChar = nextValue.charAt(nextIndex++);

            while (result == null) {
                if (readingMetadata) {
                    if (nextChar == -1) {
                        if (metadataBuffer.length() > 0) {
                            // End of metadata section
                            // No parameters detected
                            result = createPreference(metadataBuffer, null);
                            paramNameBuffer = new StringBuilder();
                        } else {
                            // Ignore empty metadata name
                        }
                    } else if (nextChar == ';') {
                        if (metadataBuffer.length() > 0) {
                            // End of metadata section
                            // Parameters detected
                            readingMetadata = false;
                            readingParamName = true;
                            paramNameBuffer = new StringBuilder();
                            parameters = new Form();
                        } else {
                            throw new IOException(
                                    "Empty metadata name detected.");
                        }
                    } else if (HeaderUtils.isSpace(nextChar)) {
                        // Ignore spaces
                    } else if (HeaderUtils.isText(nextChar)) {
                        metadataBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "Control characters are not allowed within a metadata name.");
                    }
                } else if (readingParamName) {
                    if (nextChar == '=') {
                        if (paramNameBuffer.length() > 0) {
                            // End of parameter name section
                            readingParamName = false;
                            readingParamValue = true;
                            paramValueBuffer = new StringBuilder();
                        } else {
                            throw new IOException(
                                    "Empty parameter name detected.");
                        }
                    } else if (nextChar == -1) {
                        if (paramNameBuffer.length() > 0) {
                            // End of parameters section
                            parameters.add(HeaderUtils.createParameter(
                                    paramNameBuffer, null));
                            result = createPreference(metadataBuffer,
                                    parameters);
                        } else {
                            throw new IOException(
                                    "Empty parameter name detected.");
                        }
                    } else if (nextChar == ';') {
                        // End of parameter
                        parameters.add(HeaderUtils.createParameter(
                                paramNameBuffer, null));
                        paramNameBuffer = new StringBuilder();
                        readingParamName = true;
                        readingParamValue = false;
                    } else if (HeaderUtils.isSpace(nextChar)
                            && (paramNameBuffer.length() == 0)) {
                        // Ignore white spaces
                    } else if (HeaderUtils.isTokenChar(nextChar)) {
                        paramNameBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "Separator and control characters are not allowed within a token.");
                    }
                } else if (readingParamValue) {
                    if (nextChar == -1) {
                        if (paramValueBuffer.length() > 0) {
                            // End of parameters section
                            parameters.add(HeaderUtils.createParameter(
                                    paramNameBuffer, paramValueBuffer));
                            result = createPreference(metadataBuffer,
                                    parameters);
                        } else {
                            throw new IOException(
                                    "Empty parameter value detected");
                        }
                    } else if (nextChar == ';') {
                        // End of parameter
                        parameters.add(HeaderUtils.createParameter(
                                paramNameBuffer, paramValueBuffer));
                        paramNameBuffer = new StringBuilder();
                        readingParamName = true;
                        readingParamValue = false;
                    } else if ((nextChar == '"')
                            && (paramValueBuffer.length() == 0)) {
                        // Parse the quoted string
                        boolean done = false;
                        boolean quotedPair = false;

                        while ((!done) && (nextChar != -1)) {
                            nextChar = (nextIndex < nextValue.length()) ? nextValue
                                    .charAt(nextIndex++) : -1;

                            if (quotedPair) {
                                // End of quoted pair (escape sequence)
                                if (HeaderUtils.isText(nextChar)) {
                                    paramValueBuffer.append((char) nextChar);
                                    quotedPair = false;
                                } else {
                                    throw new IOException(
                                            "Invalid character detected in quoted string. Please check your value");
                                }
                            } else if (HeaderUtils.isDoubleQuote(nextChar)) {
                                // End of quoted string
                                done = true;
                            } else if (nextChar == '\\') {
                                // Begin of quoted pair (escape sequence)
                                quotedPair = true;
                            } else if (HeaderUtils.isText(nextChar)) {
                                paramValueBuffer.append((char) nextChar);
                            } else {
                                throw new IOException(
                                        "Invalid character detected in quoted string. Please check your value");
                            }
                        }
                    } else if (HeaderUtils.isTokenChar(nextChar)) {
                        paramValueBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "Separator and control characters are not allowed within a token");
                    }
                }

                nextChar = (nextIndex < nextValue.length()) ? nextValue
                        .charAt(nextIndex++) : -1;
            }
        }

        return result;
    }

    /**
     * Extract the media parameters. Only leaveas the quality parameter if
     * found. Modifies the parameters list.
     * 
     * @param parameters
     *            All the preference parameters.
     * @return The media parameters.
     */
    protected Series<Parameter> extractMediaParams(Series<Parameter> parameters) {
        Series<Parameter> result = null;
        boolean qualityFound = false;
        Parameter param = null;

        if (parameters != null) {
            result = new Form();

            for (Iterator iter = parameters.iterator(); !qualityFound
                    && iter.hasNext();) {
                param = (Parameter) iter.next();

                if (param.getName().equals("q")) {
                    qualityFound = true;
                } else {
                    iter.remove();
                    result.add(param);
                }
            }
        }

        return result;
    }

    /**
     * Extract the quality value. If the value is not found, 1 is returned.
     * 
     * @param parameters
     *            The preference parameters.
     * @return The quality value.
     */
    protected float extractQuality(Series<Parameter> parameters) {
        float result = 1F;
        boolean found = false;

        if (parameters != null) {
            Parameter param = null;
            for (Iterator iter = parameters.iterator(); !found
                    && iter.hasNext();) {
                param = (Parameter) iter.next();
                if (param.getName().equals("q")) {
                    result = PreferenceUtils.parseQuality(param.getValue());
                    found = true;

                    // Remove the quality parameter as we will directly store it
                    // in the Preference object
                    iter.remove();
                }
            }
        }

        return result;
    }

    /**
     * Creates a new preference.
     * 
     * @param metadata
     *            The metadata name.
     * @param parameters
     *            The parameters list.
     * @return The new preference.
     */
    @SuppressWarnings("unchecked")
    protected Preference<T> createPreference(CharSequence metadata,
            Series<Parameter> parameters) {
        Preference<T> result;

        if (parameters == null) {
            result = new Preference<T>();

            switch (type) {
            case TYPE_CHARACTER_SET:
                result.setMetadata((T) new CharacterSet(metadata.toString()));
                break;

            case TYPE_ENCODING:
                result.setMetadata((T) new Encoding(metadata.toString()));
                break;

            case TYPE_LANGUAGE:
                result.setMetadata((T) new Language(metadata.toString()));
                break;

            case TYPE_MEDIA_TYPE:
                result.setMetadata((T) new MediaType(metadata.toString()));
                break;
            }
        } else {
            Series<Parameter> mediaParams = extractMediaParams(parameters);
            float quality = extractQuality(parameters);
            result = new Preference<T>(null, quality, parameters);

            switch (type) {
            case TYPE_CHARACTER_SET:
                result.setMetadata((T) new CharacterSet(metadata.toString()));
                break;

            case TYPE_ENCODING:
                result.setMetadata((T) new Encoding(metadata.toString()));
                break;

            case TYPE_LANGUAGE:
                result.setMetadata((T) new Language(metadata.toString()));
                break;

            case TYPE_MEDIA_TYPE:
                result.setMetadata((T) new MediaType(metadata.toString(),
                        mediaParams));
                break;
            }
        }

        return result;
    }

}
