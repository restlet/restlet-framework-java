/*
 * Copyright 2005-2008 Noelios Technologies.
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
     * Maximum length available from the index.
     */
    public final static long LENGTH_MAX = -1;

    /**
     * Index from which to start the range. If the index is superior or equal to
     * zero, the index will define the start of the range. If its value is
     * {@value #INDEX_LAST} (-1), then it defines the end of the range. The
     * default value is {@link #INDEX_FIRST} (0), starting at the first byte.
     */
    private volatile long index;

    /**
     * Length of the range in number of bytes. If the length is the maximum
     * available from the index, then use the {@value #LENGTH_MAX} constant.
     */
    private volatile long length;

    /**
     * Default constructor defining a range starting on the first byte and with
     * a maximum length, i.e. covering the whole entity.
     */
    public Range() {
        this(INDEX_FIRST, LENGTH_MAX);
    }

    /**
     * Constructor defining a range starting on the first byte and with the
     * given length.
     * 
     * @param length
     *            Length of the range in number of bytes.
     */
    public Range(long length) {
        this(INDEX_FIRST, length);
    }

    /**
     * Constructor.
     * 
     * @param index
     *            Index from which to start the range
     * @param length
     *            Length of the range in number of bytes.
     */
    public Range(long index, long length) {
        this.index = index;
        this.length = length;
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
     * Returns the length of the range in number of bytes. If the length is the
     * maximum available from the index, then use the {@value #LENGTH_MAX}
     * constant.
     * 
     * @return The length of the range in number of bytes.
     */
    public long getLength() {
        return length;
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
     * Sets the length of the range in number of bytes. If the length is the
     * maximum available from the index, then use the {@value #LENGTH_MAX}
     * constant.
     * 
     * @param length
     *            The length of the range in number of bytes.
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Indicates if the
     * 
     * @param index
     *            The index to test.
     * 
     * @return
     */
    public boolean isIncluded(long index, long totalLength) {

        
        return false;
    }
}
