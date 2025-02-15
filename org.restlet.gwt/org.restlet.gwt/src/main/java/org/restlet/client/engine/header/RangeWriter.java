/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.header;

import java.util.List;

import org.restlet.client.data.Range;
import org.restlet.client.representation.Representation;

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

        append(ranges.get(0).getUnitName());
        append("=");

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
