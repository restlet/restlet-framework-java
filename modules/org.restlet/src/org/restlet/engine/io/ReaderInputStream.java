/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.restlet.data.CharacterSet;

/**
 * Input stream based on a reader.
 * 
 * @author Jerome Louvel
 */
public class ReaderInputStream extends InputStream {
    /** The byte buffer. */
    private volatile byte[] buffer;

    /** The underlying character set. */
    private final CharacterSet characterSet;

    /** The reading index. */
    private volatile int index;

    /** The wrapped reader. */
    private final BufferedReader reader;

    /**
     * Constructor.
     * 
     * @param reader
     * @param characterSet
     */
    public ReaderInputStream(Reader reader, CharacterSet characterSet) {
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader
                : new BufferedReader(reader);
        this.buffer = null;
        this.index = -1;
        this.characterSet = characterSet;
    }

    @Override
    public int read() throws IOException {
        int result = -1;

        // If the buffer is empty, read a new line
        if (this.buffer == null) {
            refill();
        }

        if (this.buffer != null) {
            // Read the next byte and increment the index
            result = this.buffer[this.index++];

            // Check if the buffer has been fully read
            if (this.index == this.buffer.length) {
                this.buffer = null;
            }
        }

        return result;
    }

    /**
     * Refills the byte buffer.
     * 
     * @throws IOException
     */
    private void refill() throws IOException {
        final String line = this.reader.readLine();

        if (line != null) {
            this.buffer = line.getBytes(this.characterSet.getName());
            this.index = 0;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);
    }
}