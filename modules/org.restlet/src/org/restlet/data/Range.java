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

package org.restlet.data;

/**
 * Describes a range of bytes.
 * 
 * @author Jerome Louvel
 */
public class Range {

    /**
     * Index for the first byte of an entity.
     */
    public final static long INDEX_FIRST = 0;

    /**
     * Index for the last byte of an entity.
     */
    public final static long INDEX_LAST = -1;

    /**
     * Maximum size available from the index.
     */
    public final static long SIZE_MAX = -1;

    /**
     * Index from which to start the range. If the index is superior or equal to
     * zero, the index will define the start of the range. If its value is
     * {@value #INDEX_LAST} (-1), then it defines the end of the range. The
     * default value is {@link #INDEX_FIRST} (0), starting at the first byte.
     */
    private volatile long index;

    /**
     * Size of the range in number of bytes. If the size is the maximum
     * available from the index, then use the {@value #SIZE_MAX} constant.
     */
    private volatile long size;

    /**
     * Default constructor defining a range starting on the first byte and with
     * a maximum size, i.e. covering the whole entity.
     */
    public Range() {
        this(INDEX_FIRST, SIZE_MAX);
    }

    /**
     * Constructor defining a range starting on the first byte and with the
     * given size.
     * 
     * @param size
     *            Size of the range in number of bytes.
     */
    public Range(long size) {
        this(INDEX_FIRST, size);
    }

    /**
     * Constructor.
     * 
     * @param index
     *            Index from which to start the range
     * @param size
     *            Size of the range in number of bytes.
     */
    public Range(long index, long size) {
        this.index = index;
        this.size = size;
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Range)
                && ((Range) object).getIndex() == getIndex()
                && ((Range) object).getSize() == getSize();
    }

    /**
     * Returns the index from which to start the range. If the index is superior
     * or equal to zero, the index will define the start of the range. If its
     * value is {@value #INDEX_LAST} (-1), then it defines the end of the range.
     * The default value is {@link #INDEX_FIRST} (0), starting at the first
     * byte.
     * 
     * @return The index from which to start the range.
     */
    public long getIndex() {
        return index;
    }

    /**
     * Returns the size of the range in number of bytes. If the size is the
     * maximum available from the index, then use the {@value #SIZE_MAX}
     * constant.
     * 
     * @return The size of the range in number of bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * Indicates if the given index is included in the range.
     * 
     * @param position
     *            The position to test.
     * @param totalSize
     * 
     * @return True if the given index is included in the range, false
     *         otherwise.
     */
    public boolean isIncluded(long position, long totalSize) {
        boolean result = false;

        if (getIndex() == INDEX_LAST) {
            // The range starts from the end
            result = (0 <= position) && (position < totalSize);

            if (result) {
                result = position >= (totalSize - getSize());
            }
        } else {
            // The range starts from the beginning
            result = position >= getIndex();

            if (result && (getSize() != SIZE_MAX)) {
                result = position < getIndex() + getSize();
            }
        }

        return result;
    }

    /**
     * Sets the index from which to start the range. If the index is superior or
     * equal to zero, the index will define the start of the range. If its value
     * is {@value #INDEX_LAST} (-1), then it defines the end of the range. The
     * default value is {@link #INDEX_FIRST} (0), starting at the first byte
     * 
     * @param index
     *            The index from which to start the range.
     */
    public void setIndex(long index) {
        this.index = index;
    }

    /**
     * Sets the size of the range in number of bytes. If the size is the maximum
     * available from the index, then use the {@value #SIZE_MAX} constant.
     * 
     * @param size
     *            The size of the range in number of bytes.
     */
    public void setSize(long size) {
        this.size = size;
    }
}
