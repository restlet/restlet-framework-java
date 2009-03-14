/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.http;

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Content type header reader.
 * 
 * @author Jerome Louvel
 */
public class ContentTypeReader extends HeaderReader {

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public ContentTypeReader(String header) {
        super(header);
    }

    /**
     * Creates a content type.
     * 
     * @param mediaType
     *            The media type name.
     * @param parameters
     *            The parameters parsed.
     * @return The content type.
     */
    private ContentType createContentType(StringBuilder mediaType,
            Series<Parameter> parameters) {
        // Attempt to extract the character set
        CharacterSet characterSet = null;

        if (parameters != null) {
            final String charSet = parameters.getFirstValue("charset");
            if (charSet != null) {
                parameters.removeAll("charset");
                characterSet = new CharacterSet(charSet);
            }

            return new ContentType(new MediaType(mediaType.toString(),
                    parameters), characterSet);
        } else {
            return new ContentType(new MediaType(mediaType.toString()), null);
        }
    }

    /**
     * Read the content type.
     * 
     * @return The next preference.
     */
    public ContentType readContentType() throws IOException {
        ContentType result = null;

        boolean readingMediaType = true;
        boolean readingParamName = false;
        boolean readingParamValue = false;

        final StringBuilder mediaTypeBuffer = new StringBuilder();
        StringBuilder paramNameBuffer = null;
        StringBuilder paramValueBuffer = null;

        Series<Parameter> parameters = null;

        final String nextValue = readValue();
        int nextIndex = 0;

        if (nextValue != null) {
            int nextChar = nextValue.charAt(nextIndex++);

            while (result == null) {
                if (readingMediaType) {
                    if (nextChar == -1) {
                        if (mediaTypeBuffer.length() > 0) {
                            // End of metadata section
                            // No parameters detected
                            result = createContentType(mediaTypeBuffer, null);
                            paramNameBuffer = new StringBuilder();
                        } else {
                            // Ignore empty metadata name
                        }
                    } else if (nextChar == ';') {
                        if (mediaTypeBuffer.length() > 0) {
                            // End of mediaType section
                            // Parameters detected
                            readingMediaType = false;
                            readingParamName = true;
                            paramNameBuffer = new StringBuilder();
                            parameters = new Form();
                        } else {
                            throw new IOException(
                                    "Empty mediaType name detected.");
                        }
                    } else if (HttpUtils.isSpace(nextChar)) {
                        // Ignore spaces
                    } else if (HttpUtils.isText(nextChar)) {
                        mediaTypeBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "Control characters are not allowed within a mediaType name.");
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
                            parameters.add(HttpUtils.createParameter(
                                    paramNameBuffer, null));
                            result = createContentType(mediaTypeBuffer,
                                    parameters);
                        } else {
                            throw new IOException(
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
                        throw new IOException(
                                "Separator and control characters are not allowed within a token.");
                    }
                } else if (readingParamValue) {
                    if (nextChar == -1) {
                        if (paramValueBuffer.length() > 0) {
                            // End of parameters section
                            parameters.add(HttpUtils.createParameter(
                                    paramNameBuffer, paramValueBuffer));
                            result = createContentType(mediaTypeBuffer,
                                    parameters);
                        } else {
                            throw new IOException(
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
                                    throw new IOException(
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
                                throw new IOException(
                                        "Invalid character detected in quoted string. Please check your value");
                            }
                        }
                    } else if (HttpUtils.isTokenChar(nextChar)) {
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

}
