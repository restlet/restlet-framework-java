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

package org.restlet.engine.header;

import java.util.List;

import org.restlet.data.Range;
import org.restlet.representation.Representation;

/**
 * Range header writer.
 * 
 * @author Jerome Louvel
 */
public class RangeWriter extends HeaderWriter<Range> {

    /**
     * Formats {@code ranges} as a Range header value
     * 
     * @param ranges
     *            List of ranges to format
     * @return {@code ranges} formatted or null if the list is null or empty.
     */
    public static String write(List<Range> ranges) {
        return new RangeWriter().append(ranges).toString();
    }

    /**
     * Formats {@code range} as a Content-Range header value.
     * 
     * @param range
     *            Range to format
     * @param size
     *            Total size of the entity
     * @return {@code range} formatted
     */
    public static String write(Range range, long size) {
        StringBuilder b = new StringBuilder(range.getUnitName() + " ");

        if (range.getIndex() >= Range.INDEX_FIRST) {
            b.append(range.getIndex());
            b.append("-");
            if (range.getSize() != Range.SIZE_MAX) {
                b.append(range.getIndex() + range.getSize() - 1);
            } else {
                if (size != Representation.UNKNOWN_SIZE) {
                    b.append(size - 1);
                } else {
                    throw new IllegalArgumentException(
                            "The entity has an unknown size, can't determine the last byte position.");
                }
            }
        } else if (range.getIndex() == Range.INDEX_LAST) {
            if (range.getSize() != Range.SIZE_MAX) {
                if (size != Representation.UNKNOWN_SIZE) {
                    if (range.getSize() <= size) {
                        b.append(size - range.getSize());
                        b.append("-");
                        b.append(size - 1);
                    } else {
                        throw new IllegalArgumentException(
                                "The size of the range ("
                                        + range.getSize()
                                        + ") is higher than the size of the entity ("
                                        + size + ").");
                    }
                } else {
                    throw new IllegalArgumentException(
                            "The entity has an unknown size, can't determine the last byte position.");
                }
            } else {
                // This is not a valid range.
                throw new IllegalArgumentException(
                        "The range provides no index and no size, it is invalid.");
            }
        }

        if (size != Representation.UNKNOWN_SIZE) {
            b.append("/").append(size);
        } else {
            b.append("/*");
        }

        return b.toString();
    }

    /**
     * Formats {@code ranges} as a Range header value
     * 
     * @param ranges
     *            List of ranges to format
     * @return This writer.
     */
    public RangeWriter append(List<Range> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return this;
        }

        append(ranges.get(0).getUnitName() + "=");

        for (int i = 0; i < ranges.size(); i++) {
            if (i > 0) {
                append(", ");
            }

            append(ranges.get(i));
        }

        return this;
    }

    @Override
    public HeaderWriter<Range> append(Range range) {
        if (range.getIndex() >= Range.INDEX_FIRST) {
            append(range.getIndex());
            append("-");

            if (range.getSize() != Range.SIZE_MAX) {
                append(range.getIndex() + range.getSize() - 1);
            }
        } else if (range.getIndex() == Range.INDEX_LAST) {
            append("-");

            if (range.getSize() != Range.SIZE_MAX) {
                append(range.getSize());
            }
        }

        return this;
    }

}
