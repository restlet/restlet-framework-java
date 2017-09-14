/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.xdb.internal;

import java.io.IOException;
import java.io.InputStream;

// [excludes gwt]
/**
 * Input stream based on a source stream that must only be partially read.
 * 
 * @author Jerome Louvel
 * @deprecated Not actively developed anymore.
 */
@Deprecated
public class SizedInputStream extends InputEntityStream {

    /** The total size that should be read from the source stream. */
    private volatile long availableSize;

    /** The total size when the {@link #mark(int)} method was called. */
    private volatile long markedAvailableSize;

    /**
     * Constructor.
     * 
     * @param notifiable
     *            The notifiable connection.
     * @param inboundStream
     *            The inbound stream.
     * @param size
     *            The total size that should be read from the source stream.
     */
    public SizedInputStream(Notifiable notifiable, InputStream inboundStream,
            long size) {
        super(notifiable, inboundStream);
        this.availableSize = size;
        this.markedAvailableSize = -1;
    }

    @Override
    public int available() throws IOException {
        return Math.min((int) this.availableSize, getInboundStream()
                .available());
    }

    @Override
    public void close() throws IOException {
        // Don't close it directly
    }

    @Override
    public synchronized void mark(int readlimit) {
        if (markSupported()) {
            this.markedAvailableSize = availableSize;
        }

        getInboundStream().mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return getInboundStream().markSupported();
    }

    /**
     * Reads a byte from the underlying stream.
     * 
     * @return The byte read, or -1 if the end of the stream has been reached.
     */
    @Override
    public int read() throws IOException {
        int result = -1;

        if (this.availableSize > 0) {
            result = getInboundStream().read();

            if (result != -1) {
                this.availableSize--;
            } else {
                onEndReached();
            }
        }

        // The stream has been fully read.
        if (this.availableSize <= 0) {
            onEndReached();
        }

        return result;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int result = -1;

        if (this.availableSize > 0) {
            result = getInboundStream().read(b, off,
                    Math.min(len, (int) this.availableSize));

            if (result > 0) {
                this.availableSize -= result;
            } else if (result == -1) {
                onEndReached();
            }
        }

        if (this.availableSize <= 0) {
            onEndReached();
        }

        return result;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (markSupported()) {
            if (this.markedAvailableSize != -1) {
                this.availableSize = markedAvailableSize;
                this.markedAvailableSize = -1;
            }
        }

        getInboundStream().reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return getInboundStream().skip(n);
    }

}
