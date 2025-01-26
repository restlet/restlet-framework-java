/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.io;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.restlet.client.engine.util.emul.UnsupportedEncodingException;

import org.restlet.client.data.CharacterSet;
import org.restlet.client.data.Range;
import org.restlet.client.representation.Representation;

/**
 * IO manipulation utilities.
 *
 * @author Thierry Boileau
 */
public class IoUtils {

    /**
     * Returns the size effectively available. This returns the same value as {@link Representation#getSize()} if no
     * range is defined, otherwise it
     * returns the size of the range using {@link Range#getSize()}.
     *
     * @param representation
     *            The representation to evaluate.
     * @return The available size.
     */
    public static long getAvailableSize(Representation representation) {
        return representation.getSize();
    }

    /**
     * Returns a reader from an input stream and a character set.
     *
     * @param stream
     *            The input stream.
     * @param characterSet
     *            The character set. May be null.
     * @return The equivalent reader.
     * @throws UnsupportedEncodingException
     *             if a character set is given, but not supported
     */
    public static StringReader getReader(InputStream stream, CharacterSet characterSet)
            throws UnsupportedEncodingException {
        return new StringReader(toString(stream, characterSet));
    }


    /**
     * Converts an input stream to a string.<br>
     * As this method uses the InputStreamReader class, the default character
     * set is used for decoding the input stream.
     *
     * @see IoUtils#toString(InputStream, Charset)
     * @param inputStream
     *            The input stream.
     * @return The converted string.
     */
    public static String toString(InputStream inputStream) {
        return toString(inputStream, Charset.defaultCharset());
    }

    /**
     * Converts an input stream to a string using the specified character set
     * for decoding the input stream. Once read, the input stream is closed.
     *
     * @param inputStream
     *            The input stream.
     * @param characterSet
     *            The character set
     * @return The converted string.
     */
    public static String toString(InputStream inputStream,
                                  CharacterSet characterSet) {
        final Charset charset = characterSet == null
                ? Charset.defaultCharset()
                : Charset.forName(characterSet.getName());

        return toString(inputStream, charset);
    }

    /**
     * Converts an input stream to a string using the specified character set
     * for decoding the input stream. Once read, the input stream is closed.
     *
     * @param inputStream
     *            The input stream.
     * @param charset
     *            The character set
     * @return The converted string.
     */
    public static String toString(InputStream inputStream, final Charset charset) {
        String result = null;

        if (inputStream != null) {
            if (inputStream instanceof StringInputStream) {
                return ((StringInputStream) inputStream).getText();
            } else {
                try {
                    final Charset internalCharset = charset == null
                            ? Charset.defaultCharset()
                            : charset;

                    StringBuilder sb = new StringBuilder();
                    byte[] buffer = new byte[8196];
                    int charsRead = inputStream.read(buffer);

                    while (charsRead != -1) {
                        sb.append(new String(buffer, 0, charsRead, internalCharset));
                        charsRead = inputStream.read(buffer);
                    }

                    inputStream.close();
                    result = sb.toString();
                } catch (Exception e) {
                    // Returns a null string
                }
            }
        }

        return result;
    }


        /**
         * Converts a reader to a string.
         *
         * @param reader
         *            The characters' reader.
         * @return The converted string.
         */
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[8192];
                int charsRead = reader.read(buffer);

                while (charsRead != -1) {
                    sb.append(buffer, 0, charsRead);
                    charsRead = reader.read(buffer);
                }

                reader.close();
                result = sb.toString();
            } catch (Exception e) {
                // Returns a null string
            }
        }

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private IoUtils() {
    }
}
