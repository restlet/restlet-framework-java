/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream based on a source stream that must only be partially read.
 */
public class InputEntityStream extends InputStream {

    /** The source stream. */
    private InputStream source;

    /** The total size that should be read from the source stream. */
    private long availableSize;

    /**
     * Constructor.
     * 
     * @param source
     *                The source stream.
     * @param size
     *                The total size that should be read from the source stream.
     */
    public InputEntityStream(InputStream source, long size) {
        this.source = source;
        this.availableSize = size;
    }

    /**
     * Reads a byte from the underlying stream.
     * 
     * @return The byte read, or -1 if the end of the stream has been reached.
     */
    public int read() throws IOException {
        int result = -1;

        if (this.availableSize > 0) {
            result = this.source.read();

            if (result > 0) {
                this.availableSize = this.availableSize - 1;
            }
        }

        return result;
    }

}
