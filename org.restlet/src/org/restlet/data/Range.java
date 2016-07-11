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

package org.restlet.data;

import org.restlet.engine.util.SystemUtils;

/**
 * Describes a range of bytes.
 * 
 * @author Jerome Louvel
 */
public class Range {

    /**
     * Index for the first byte (or range unit) of an entity.
     */
    public final static long INDEX_FIRST = 0;

    /**
     * Index for the last byte (or range unit) of an entity.
     */
    public final static long INDEX_LAST = -1;

    public final static String RANGE_BYTES_UNIT = "bytes";

    /**
     * Maximum size available from the index.
     */
    public final static long SIZE_MAX = -1;

    /**
     * Indicates if the unit of the given range is "bytes".
     * 
     * @param range
     *            The range.
     * @return true if the unit of the given range is "bytes".
     */
    public static boolean isBytesRange(Range range) {
        return RANGE_BYTES_UNIT.equals(range.getUnitName());
    }

    /**
     * Index from which to start the range. If the index is superior or equal to
     * zero, the index will define the start of the range. If its value is {@value #INDEX_LAST} (-1), then it defines
     * the end of the range. The default value is {@link #INDEX_FIRST} (0), starting at the first byte.
     */
    private volatile long index;

    /**
     * Total size of the instance in number of bytes (or range unit). In case of "bytes" range, this attribute is
     * ignored, as the instance size is taken from the entity.
     */
    private volatile long instanceSize;

    /**
     * Size of the range in number of bytes (or range unit). If the size is the maximum
     * available from the index, then use the {@value #SIZE_MAX} constant.
     */
    private volatile long size;

    /**
     * Specifies the unit of the range. The HTTP/1.1 protocol specifies only
     * 'bytes', but other ranges are allowed {@link http
     * ://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.12}
     */
    private volatile String unitName;

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
     * Constructor. Sets the name of the range unit as "bytes" by default.
     * 
     * @param index
     *            Index from which to start the range
     * @param size
     *            Size of the range in number of bytes.
     */
    public Range(long index, long size) {
        this.index = index;
        this.size = size;
        this.unitName = RANGE_BYTES_UNIT;
    }

    /**
     * Constructor. Sets the name of the range unit as "bytes" by default.
     * 
     * @param index
     *            Index from which to start the range
     * @param size
     *            Size of the range in number of bytes.
     * @param instanceSize
     *            Size of the instance in number of bytes.
     * @param unitName
     *            Unit of the range.
     */
    public Range(long index, long size, long instanceSize, String unitName) {
        this.index = index;
        this.instanceSize = instanceSize;
        this.size = size;
        this.unitName = unitName;
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
     * Returns the total size of the instance in number of bytes (or range unit). In case of "bytes" range, this
     * attribute is ignored, as the instance size is taken from the entity.
     * 
     * @return The total size of the instance.
     */
    public long getInstanceSize() {
        return instanceSize;
    }

    /**
     * Returns the size of the range in number of bytes. If the size is the
     * maximum available from the index, then use the {@value #SIZE_MAX} constant.
     * 
     * @return The size of the range in number of bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the name of the range unit.
     * 
     * @return The name of the range unit.
     */
    public String getUnitName() {
        return unitName;
    }
    
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(index, instanceSize, size, unitName);
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
     * Sets the total size of the instance in number of bytes (or range unit).
     * 
     * @param instanceSize
     *            The total size of the instance.
     */
    public void setInstanceSize(long instanceSize) {
        this.instanceSize = instanceSize;
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

    /**
     * Sets the name of the range unit.
     * 
     * @param unitName
     *            The name of the range unit.
     */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
