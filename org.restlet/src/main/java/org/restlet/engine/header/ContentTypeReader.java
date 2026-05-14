/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.io.IOException;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Content type header reader.
 *
 * @author Jerome Louvel
 */
public class ContentTypeReader extends HeaderReader<ContentType> {

    /**
     * Constructor.
     *
     * @param header The header to read.
     */
    public ContentTypeReader(String header) {
        super(header);
    }

    /**
     * Creates a content type.
     *
     * @param mediaType The media type name.
     * @param parameters The parameters parsed.
     * @return The content type.
     */
    private ContentType createContentType(StringBuilder mediaType, Series<Parameter> parameters) {
        // Attempt to extract the character set
        CharacterSet characterSet = null;

        if (parameters != null) {
            String charSet = parameters.getFirstValue("charset");

            if (charSet != null) {
                parameters.removeAll("charset");
                characterSet = new CharacterSet(charSet);
            }

            return new ContentType(new MediaType(mediaType.toString(), parameters), characterSet);
        }

        return new ContentType(new MediaType(mediaType.toString()), null);
    }

    @Override
    public ContentType readValue() throws IOException {
        ContentType result = null;

        boolean readingMediaType = true;
        boolean readingParamName = false;
        boolean readingParamValue = false;

        StringBuilder mediaTypeBuffer = new StringBuilder();
        StringBuilder paramNameBuffer = null;
        StringBuilder paramValueBuffer = null;

        Series<Parameter> parameters = null;
        String nextValue = readRawValue();
        int nextIndex = 0;

        if (nextValue != null) {
            int nextChar = nextValue.charAt(nextIndex++);

            while (result == null) {
                if (readingMediaType) {
                    if (nextChar == -1) {
                        if (mediaTypeBuffer.isEmpty()) {
                            // Ignore empty metadata name
                        } else {
                            // End of metadata section
                            // No parameters detected
                            result = createContentType(mediaTypeBuffer, null);
                            paramNameBuffer = new StringBuilder();
                        }
                    } else if (nextChar == ';') {
                        if (mediaTypeBuffer.isEmpty()) {
                            throw new IOException("Empty mediaType name detected.");
                        } else {
                            // End of mediaType section
                            // Parameters detected
                            readingMediaType = false;
                            readingParamName = true;
                            paramNameBuffer = new StringBuilder();
                            parameters = new Series<>(Parameter.class);
                        }
                    } else if (HeaderUtils.isSpace(nextChar)) {
                        // Ignore spaces
                    } else if (HeaderUtils.isText(nextChar)) {
                        mediaTypeBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "The "
                                        + (char) nextChar
                                        + " character isn't allowed in a media type name.");
                    }
                } else if (readingParamName) {
                    if (nextChar == '=') {
                        if (paramNameBuffer.isEmpty()) {
                            throw new IOException("Empty parameter name detected.");
                        } else {
                            // End of a parameter name section
                            readingParamName = false;
                            readingParamValue = true;
                            paramValueBuffer = new StringBuilder();
                        }
                    } else if (nextChar == -1) {
                        if (paramNameBuffer.isEmpty()) {
                            result = createContentType(mediaTypeBuffer, parameters);
                        } else {
                            // End of the parameters section
                            parameters.add(Parameter.create(paramNameBuffer, null));
                            result = createContentType(mediaTypeBuffer, parameters);
                        }
                    } else if (nextChar == ';') {
                        // End of parameter
                        parameters.add(Parameter.create(paramNameBuffer, null));
                        paramNameBuffer = new StringBuilder();
                        readingParamName = true;
                        readingParamValue = false;
                    } else if (HeaderUtils.isSpace(nextChar) && paramNameBuffer.isEmpty()) {
                        // Ignore white spaces
                    } else if (HeaderUtils.isTokenChar(nextChar)) {
                        paramNameBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "The \""
                                        + (char) nextChar
                                        + "\" character isn't allowed in a media type parameter name.");
                    }
                } else if (readingParamValue) {
                    if (nextChar == -1) {
                        if (paramValueBuffer.isEmpty()) {
                            throw new IOException("Empty parameter value detected");
                        } else {
                            // End of the parameters section
                            parameters.add(Parameter.create(paramNameBuffer, paramValueBuffer));
                            result = createContentType(mediaTypeBuffer, parameters);
                        }
                    } else if (nextChar == ';') {
                        // End of parameter
                        parameters.add(Parameter.create(paramNameBuffer, paramValueBuffer));
                        paramNameBuffer = new StringBuilder();
                        readingParamName = true;
                        readingParamValue = false;
                    } else if ((nextChar == '"') && paramValueBuffer.isEmpty()) {
                        // Parse the quoted string
                        boolean done = false;
                        boolean quotedPair = false;

                        while ((!done) && (nextChar != -1)) {
                            nextChar =
                                    (nextIndex < nextValue.length())
                                            ? nextValue.charAt(nextIndex++)
                                            : -1;

                            if (quotedPair) {
                                // End of a quoted pair (escape sequence)
                                if (HeaderUtils.isText(nextChar)) {
                                    paramValueBuffer.append((char) nextChar);
                                    quotedPair = false;
                                } else {
                                    throw new IOException(
                                            "Invalid character \""
                                                    + (char) nextChar
                                                    + "\" detected in quoted string. Please check your value");
                                }
                            } else if (HeaderUtils.isDoubleQuote(nextChar)) {
                                // End of quoted string
                                done = true;
                            } else if (nextChar == '\\') {
                                // Begin of a quoted pair (escape sequence)
                                quotedPair = true;
                            } else if (HeaderUtils.isText(nextChar)) {
                                paramValueBuffer.append((char) nextChar);
                            } else {
                                throw new IOException(
                                        "Invalid character \""
                                                + (char) nextChar
                                                + "\" detected in quoted string. Please check your value");
                            }
                        }
                    } else if (HeaderUtils.isTokenChar(nextChar)) {
                        paramValueBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "The \""
                                        + (char) nextChar
                                        + "\" character isn't allowed in a media type parameter value.");
                    }
                }

                nextChar = (nextIndex < nextValue.length()) ? nextValue.charAt(nextIndex++) : -1;
            }
        }

        return result;
    }
}
