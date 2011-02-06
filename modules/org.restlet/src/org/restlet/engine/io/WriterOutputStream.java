/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

// [excludes gwt]
/**
 * Outputstream wrapping a character writer.
 * 
 * @author Kevin Conaway
 */
public class WriterOutputStream extends OutputStream {
    /** The wrapped writer. */
    private final Writer writer;

    /**
     * Constructor.
     * 
     * @param writer
     *            The wrapped writer.
     */
    public WriterOutputStream(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.writer.close();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.writer.flush();
    }

    @Override
    public void write(int b) throws IOException {
        this.writer.write(b);
    }
}