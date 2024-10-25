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

package com.google.gwt.emul.java.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Emulation of the {@link java.io.InputStreamReader} class for the GWT edition.
 * 
 * @author Jerome Louvel
 */
public class InputStreamReader extends Reader {

    /** The next position to read. */
    private int position;

    /** The text to read. */
    private final String text;

    /**
     * Constructor.
     * 
     * @param stream
     *            The source text to read.
     */
    public InputStreamReader(java.io.InputStream stream) {
        this(stream, Charset.defaultCharset());
    }

    /**
     * Constructor.
     * 
     * @param stream
     *            The source text to read.
     * @param charsetName
     *            The name of a supported charset.
     */
    public InputStreamReader(InputStream stream, String charsetName) {
        this(stream, Charset.forName(charsetName));
    }

    /**
     * Constructor.
     *
     * @param stream
     *            The source text to read.
     * @param charset
     *            The charset.
     */
    public InputStreamReader(InputStream stream, Charset charset) {
        this(toString(stream, charset));
    }

    /**
     * Constructor.
     * 
     * @param text
     *            The source text to read.
     */
    public InputStreamReader(String text) {
        this.text = text;
        this.position = 0;
    }

    @Override
    public void close() throws IOException {

    }

    /**
     * Reads the next character in the source text.
     * 
     * @return The next character or -1 if end of text is reached.
     */
    @Override
    public int read() throws IOException {
        return (this.position == this.text.length()) ? -1 : this.text
                .charAt(this.position++);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (position >= text.length())
            return -1;
        int n = Math.min(text.length() - position, len);
        text.getChars(position, position + n, cbuf, off);
        position += n;
        return n;
    }

    /**
     * Converts a reader to a string.
     *
     * @see java.io.InputStreamReader
     *
     * @param inputStream
     *            The inputStream to read.
     * @param charset
     *            The charset.
     * @return The converted string.
     */
    private static String toString(final InputStream inputStream, final Charset charset) {
        String result = null;

        if (inputStream != null) {
            try {
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[2048];
                int charsRead = inputStream.read(buffer);

                while (charsRead != -1) {
                    sb.append(new String(buffer, 0, charsRead, charset));
                    charsRead = inputStream.read(buffer);
                }

                inputStream.close();
                result = sb.toString();
            } catch (Exception e) {
                // Returns a null string
            }
        }

        return result;
    }

}
