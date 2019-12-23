/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package com.google.gwt.emul.java.io;

import java.io.IOException;

/**
 * Emulation of the {@link java.io.InputStream} class for the GWT edition.
 * 
 * @author Jerome Louvel
 */
public class InputStream {

    /** The next position to read. */
    private int position;

    /** The text to stream. */
    protected String text;

    public InputStream() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param text
     */
    public InputStream(String text) {
        super();
        this.position = 0;
        this.text = text;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public int available() throws IOException {
        if (text != null) {
            return text.length();
        }

        return 0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void close() throws IOException {

    }

    /**
     * Reads the next character in the source text.
     * 
     * @return The next character or -1 if end of text is reached.
     * @throws IOException
     */
    public int read() throws IOException {
        return (this.position == this.text.length()) ? -1 : this.text
                .charAt(this.position++);
    }

    /**
     * 
     * @param cbuf
     * @return
     * @throws IOException
     */
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * 
     * @param cbuf
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (position >= text.length())
            return -1;
        int n = Math.min(text.length() - position, len);
        text.getChars(position, position + n, cbuf, off);
        position += n;
        return n;
    }
}
