/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.io;

import static org.restlet.client.data.Range.isBytesRange;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.client.Context;
import org.restlet.client.data.CharacterSet;
import org.restlet.client.data.Range;
import org.restlet.client.engine.Edition;
import org.restlet.client.engine.Engine;
import org.restlet.client.representation.Representation;

/**
 * IO manipulation utilities.
 * 
 * @author Thierry Boileau
 */
public class IoUtils {

    /**
     * The size to use when instantiating buffered items such as instances of
     * the {@link BufferedReader} class. It looks for the System property
     * "org.restlet.client.engine.io.bufferSize" and if not defined, uses the "8192"
     * default value.
     */
    public static final int BUFFER_SIZE = getProperty(
            "org.restlet.client.engine.io.bufferSize", 8192);


    /**
     * The number of milliseconds after which IO operation will time out. It
     * looks for the System property "org.restlet.client.engine.io.timeoutMs" and if
     * not defined, uses the "60000" default value.
     */
    public final static int TIMEOUT_MS = getProperty(
            "org.restlet.client.engine.io.timeoutMs", 60000);









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




    private static int getProperty(String name, int defaultValue) {
        int result = defaultValue;


        return result;
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
    public static Reader getReader(InputStream stream, CharacterSet characterSet)
            throws UnsupportedEncodingException {
        if (characterSet != null) {
            return new InputStreamReader(stream, characterSet.getName());
        }

        return new InputStreamReader(stream);
    }
















    /**
     * Converts an input stream to a string.<br>
     * As this method uses the InputstreamReader class, the default character
     * set is used for decoding the input stream.
     * 
     * @see InputStreamReader
     * @see IoUtils#toString(InputStream, CharacterSet)
     * @param inputStream
     *            The input stream.
     * @return The converted string.
     */
    public static String toString(InputStream inputStream) {
        return IoUtils.toString(inputStream, null);
    }

    /**
     * Converts an input stream to a string using the specified character set
     * for decoding the input stream. Once read, the input stream is closed.
     * 
     * @see InputStreamReader
     * @param inputStream
     *            The input stream.
     * @param characterSet
     *            The character set
     * @return The converted string.
     */
    public static String toString(InputStream inputStream,
            CharacterSet characterSet) {
        String result = null;

        if (inputStream != null) {
             if (inputStream instanceof StringInputStream) {
             return ((StringInputStream) inputStream).getText();
             } else {
             try {
             if (characterSet != null) {
             result = toString(new InputStreamReader(inputStream,
             characterSet.getName()));
             } else {
             result = toString(new InputStreamReader(inputStream));
             }
             } catch (Exception e) {
             // Returns an empty string
             }
             }
        }

        return result;
    }

    /**
     * Converts a reader to a string.
     * 
     * @see InputStreamReader
     * 
     * @param reader
     *            The characters reader.
     * @return The converted string.
     */
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader
                        : new BufferedReader(reader, BUFFER_SIZE);
                char[] buffer = new char[2048];
                int charsRead = br.read(buffer);

                while (charsRead != -1) {
                    sb.append(buffer, 0, charsRead);
                    charsRead = br.read(buffer);
                }

                br.close();
                result = sb.toString();
            } catch (Exception e) {
                // Returns an empty string
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
