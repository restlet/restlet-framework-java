package com.noelios.restlet.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream decorator to trap {@code close()} calls so that the underlying
 * stream is not closed.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 * 
 */
public class KeepAliveInputStream extends FilterInputStream {

    /**
     * Constructor.
     * 
     * @param source
     *                The source input stream.
     */
    public KeepAliveInputStream(InputStream source) {
        super(source);
    }

    @Override
    public void close() throws IOException {
    }
}
