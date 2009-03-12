/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.gwt.engine.http;

import java.util.Iterator;

import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.Encoding;
import org.restlet.gwt.data.Form;
import org.restlet.gwt.data.Language;
import org.restlet.gwt.data.MediaType;
import org.restlet.gwt.data.Metadata;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Preference;
import org.restlet.gwt.util.Series;

/**
 * Preference header reader. Works for character sets, encodings, languages or
 * media types.
 * 
 * @author Jerome Louvel
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

            switch (this.type) {
            case TYPE_CHARACTER_SET:
                result.setMetadata((T) CharacterSet
                        .valueOf(metadata.toString()));
                break;

            case TYPE_ENCODING:
                result.setMetadata((T) Encoding.valueOf(metadata.toString()));
                break;

            case TYPE_LANGUAGE:
                result.setMetadata((T) Language.valueOf(metadata.toString()));
                break;

            case TYPE_MEDIA_TYPE:
                result.setMetadata((T) MediaType.valueOf(metadata.toString()));
                break;
            }
        } else {
            final Series<Parameter> mediaParams = extractMediaParams(parameters);
            final float quality = extractQuality(parameters);
            result = new Preference<T>(null, quality, parameters);

            switch (this.type) {
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

    /**
     * Extract the media parameters. Only leave as the quality parameter if
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

            for (final Iterator<Parameter> iter = parameters.iterator(); !qualityFound
                    && iter.hasNext();) {
                param = iter.next();

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
            for (final Iterator<Parameter> iter = parameters.iterator(); !found
                    && iter.hasNext();) {
                param = iter.next();
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
     * Read the next preference.
     * 
     * @return The next preference.
     */
    public Preference<T> readPreference() throws Exception {
        Preference<T> result = null;

        boolean readingMetadata = true;
        boolean readingParamName = false;
        boolean readingParamValue = false;

        final StringBuilder metadataBuffer = new StringBuilder();
        StringBuilder paramNameBuffer = null;
        StringBuilder paramValueBuffer = null;

        Series<Parameter> parameters = null;

        final String nextValue = readValue();
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
                            throw new Exception("Empty metadata name detected.");
                        }
                    } else if (HttpUtils.isSpace(nextChar)) {
                        // Ignore spaces
                    } else if (HttpUtils.isText(nextChar)) {
                        metadataBuffer.append((char) nextChar);
                    } else {
                        throw new Exception(
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
                            throw new Exception(
                                    "Empty parameter name detected.");
                        }
                    } else if (nextChar == -1) {
                        if (paramNameBuffer.length() > 0) {
                            // End of parameters section
                            parameters.add(HttpUtils.createParameter(
                                    paramNameBuffer, null));
                            result = createPreference(metadataBuffer,
                                    parameters);
                        } else {
                            throw new Exception(
                                    "Empty parameter name detected.");
                        }
                    } else if (nextChar == ';') {
                        // End of parameter
                        parameters.add(HttpUtils.createParameter(
                                paramNameBuffer, null));
                        paramNameBuffer = new StringBuilder();
                        readingParamName = true;
                        readingParamValue = false;
                    } else if (HttpUtils.isSpace(nextChar)
                            && (paramNameBuffer.length() == 0)) {
                        // Ignore white spaces
                    } else if (HttpUtils.isTokenChar(nextChar)) {
                        paramNameBuffer.append((char) nextChar);
                    } else {
                        throw new Exception(
                                "Separator and control characters are not allowed within a token.");
                    }
                } else if (readingParamValue) {
                    if (nextChar == -1) {
                        if (paramValueBuffer.length() > 0) {
                            // End of parameters section
                            parameters.add(HttpUtils.createParameter(
                                    paramNameBuffer, paramValueBuffer));
                            result = createPreference(metadataBuffer,
                                    parameters);
                        } else {
                            throw new Exception(
                                    "Empty parameter value detected");
                        }
                    } else if (nextChar == ';') {
                        // End of parameter
                        parameters.add(HttpUtils.createParameter(
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
                                    .charAt(nextIndex++)
                                    : -1;

                            if (quotedPair) {
                                // End of quoted pair (escape sequence)
                                if (HttpUtils.isText(nextChar)) {
                                    paramValueBuffer.append((char) nextChar);
                                    quotedPair = false;
                                } else {
                                    throw new Exception(
                                            "Invalid character detected in quoted string. Please check your value");
                                }
                            } else if (HttpUtils.isDoubleQuote(nextChar)) {
                                // End of quoted string
                                done = true;
                            } else if (nextChar == '\\') {
                                // Begin of quoted pair (escape sequence)
                                quotedPair = true;
                            } else if (HttpUtils.isText(nextChar)) {
                                paramValueBuffer.append((char) nextChar);
                            } else {
                                throw new Exception(
                                        "Invalid character detected in quoted string. Please check your value");
                            }
                        }
                    } else if (HttpUtils.isTokenChar(nextChar)) {
                        paramValueBuffer.append((char) nextChar);
                    } else {
                        throw new Exception(
                                "Separator and control characters are not allowed within a token");
                    }
                }

                nextChar = (nextIndex < nextValue.length()) ? nextValue
                        .charAt(nextIndex++) : -1;
            }
        }

        return result;
    }

}
