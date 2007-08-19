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
                this.availableSize = this.availableSize - result;
            }
        }

        return result;
    }

}
