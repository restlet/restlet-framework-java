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
    public InputStreamReader(InputStream stream) {
        this(stream.getText());
    }
    
    /**
     * Constructor.
     * 
     * @param stream
     *            The source text to read.
     * @param  charsetName
     *         The name of a supported charset.
     */
    public InputStreamReader(InputStream stream, String charsetName) {
        this(stream.getText());
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
    public int read() throws IOException  {
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

}
