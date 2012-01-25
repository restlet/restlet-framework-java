/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package com.google.gwt.emul.java.io;

import java.io.IOException;

/**
 * Emulation of the {@link java.io.BufferedReader} class for the GWT edition.
 * 
 * @author Jerome Louvel
 */
public class BufferedReader extends Reader {

    /** The next saved character. */
    private int savedNextChar;

    private Reader source;

    /**
     * Constructor.
     * 
     * @param source
     *            The source reader.
     */
    public BufferedReader(Reader source) {
        this.source = source;
        this.savedNextChar = -2;
    }

    /**
     * Constructor.
     * 
     * @param source
     *            The source reader.
     * @param size
     *            The size of the buffer.
     */
    public BufferedReader(Reader source, int size) {
        this.source = source;
        this.savedNextChar = -2;
    }

    /**
     * 
     */
    public void close() throws IOException {

    }

    /**
     * Returns the source reader.
     * 
     * @return The source reader.
     */
    private Reader getSource() {
        return source;
    }

    /**
     * Returns the next character, either the saved one or the next one from the
     * source reader.
     * 
     * @return The next character.
     * @throws IOException
     */
    public int read() throws IOException {
        int result = -1;

        if (this.savedNextChar != -2) {
            result = this.savedNextChar;
            this.savedNextChar = -2;
        } else {
            result = getSource().read();
        }

        return result;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return source.read(cbuf, off, len);
    }

    /**
     * Reads the next line of characters.
     * 
     * @return The next line.
     */
    public String readLine() throws IOException {
        StringBuilder sb = null;
        boolean eol = false;
        int nextChar = read();

        while (!eol && (nextChar != -1)) {
            if (nextChar == 10) {
                eol = true;
            } else if (nextChar == 13) {
                eol = true;

                // Check if there is a immediate LF following the CR
                nextChar = read();
                if (nextChar != 10) {
                    this.savedNextChar = nextChar;
                }
            }

            if (!eol) {
                if (sb == null) {
                    sb = new StringBuilder();
                }

                sb.append((char) nextChar);
                nextChar = read();
            }

        }

        return (sb == null) ? null : sb.toString();
    }
}
