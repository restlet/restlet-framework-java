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

package com.noelios.restlet.application;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.restlet.data.Range;

/**
 * Filters an input stream to expose only a given range.
 * 
 * @author Jerome Louvel
 */
public class RangeInputStream extends FilterInputStream {

    /** The current position. */
    private long position;

    /** The range to satisfy. */
    private Range range;

    /** The total size of the source stream. */
    private long totalSize;

    /**
     * Constructs a stream exposing only a range of a given source stream.
     * 
     * @param in
     *            The source input stream.
     * @param totalSize
     *            The total size of the source stream.
     * @param range
     *            The range to satisfy.
     */
    protected RangeInputStream(InputStream in, long totalSize, Range range) {
        super(in);
        this.range = range;
        this.position = 0;
        this.totalSize = totalSize;
    }

    @Override
    public int available() throws IOException {
        // Might need a smarter logic to restrict available bytes to the
        // range
        return super.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        if (range.getIndex() == Range.INDEX_LAST) {
            super.mark(readlimit + (int) (totalSize - range.getSize()));
        } else {
            super.mark(readlimit + (int) range.getIndex());
        }
    }

    @Override
    public int read() throws IOException {
        int result = super.read();

        while ((result != -1) && !this.range.isIncluded(position++, totalSize)) {
            result = super.read();
        }

        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long startIndex = (range.getIndex() != Range.INDEX_LAST) ? range
                .getIndex() : totalSize - range.getSize();
        long skipped = skip(startIndex - position);

        // skip to the index of the range.
        while ((skipped >= 0) && !(position >= startIndex)
                && !this.range.isIncluded(position += skipped, totalSize)) {
            skipped = skip(startIndex - position);
        }

        // read the number of bytes required, otherwise returns -1
        // TODO refactoring
        if (range.getSize() != Range.SIZE_MAX) {
            long finalIndex = startIndex + range.getSize();
            if (position >= finalIndex) {
                return -1;
            } else {
                int n = super
                        .read(
                                b,
                                off,
                                ((position + len) > finalIndex) ? (int) (finalIndex - position)
                                        : len);
                if (n > 0) {
                    position += n;
                }

                return n;
            }
        } else {
            return super.read(b, off, len);
        }

    }
}
