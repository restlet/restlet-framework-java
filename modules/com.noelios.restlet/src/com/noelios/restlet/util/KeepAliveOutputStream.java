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

package com.noelios.restlet.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream decorator to trap close() calls so that the decorated stream
 * does not get closed.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 */
public class KeepAliveOutputStream extends OutputStream {

    /** The decorated source stream. */
    private final OutputStream source;

    /**
     * Constructor.
     * 
     * @param source
     *                The decorated source stream.
     */
    public KeepAliveOutputStream(OutputStream source) {
        this.source = source;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
        source.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        source.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        source.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        source.write(b);
    }
}
