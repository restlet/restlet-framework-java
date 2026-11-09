/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.restlet.data.Range;
import org.restlet.representation.Representation;

/**
 * Range header reader.
 *
 * @author Jerome Louvel
 */
public class RangeReader {

    private static final String BYTES_RANGE_PREFIX = "bytes ";

    /** The range unit and equal sign starting the value of a Range header. */
    private static final String BYTES_RANGES_PREFIX = "bytes=";

    /** A range-spec, with the optional whitespace allowed around the elements of a list. */
    private static final Pattern RANGE_SPEC =
            Pattern.compile("[ \\t]*(?:(\\d+)-(\\d*)|-(\\d+))[ \\t]*");

    /**
     * Parse the Content-Range header value and update the given representation.
     *
     * @param value Content-range header.
     * @param representation Representation to update.
     */
    public static void update(String value, Representation representation) {
        if (value != null && value.startsWith(BYTES_RANGE_PREFIX)) {
            value = value.substring(BYTES_RANGE_PREFIX.length());

            int index = value.indexOf('-');
            int index1 = value.indexOf('/');

            if (index != -1) {
                long startIndex =
                        (index == 0) ? Range.INDEX_LAST : Long.parseLong(value.substring(0, index));
                long endIndex = Long.parseLong(value.substring(index + 1, index1));

                representation.setRange(new Range(startIndex, endIndex - startIndex + 1));
            }

            String strLength = value.substring(index1 + 1);
            if (!("*".equals(strLength))) {
                representation.setSize(Long.parseLong(strLength));
            }
        }
    }

    /**
     * Parse the Range header and returns the list of corresponding Range objects.<br>
     * <br>
     * As allowed by RFC 9110 section 14.2, a header that cannot be served as a range request is
     * ignored, which returns an empty list and never throws: a range unit other than "bytes"
     * (matched ignoring case), a syntax error, a number that does not fit in a {@code long}, or an
     * invalid range-spec (last position lower than the first one, RFC 9110 section 14.1.1). One
     * invalid range-spec invalidates the whole header. Empty list elements are skipped.<br>
     * <br>
     * A valid range-spec that cannot be satisfied (for example "bytes=-0", or a first position past
     * the end of the entity) is returned, as only the entity tells if it is satisfiable.
     *
     * @param rangeHeader The Range header value.
     * @return The list of corresponding Range objects, empty if the header must be ignored.
     */
    public static List<Range> read(String rangeHeader) {
        List<Range> result = new ArrayList<>();

        if (rangeHeader != null
                && rangeHeader.regionMatches(
                        true, 0, BYTES_RANGES_PREFIX, 0, BYTES_RANGES_PREFIX.length())) {
            for (String element :
                    rangeHeader.substring(BYTES_RANGES_PREFIX.length()).split(",", -1)) {
                if (isOptionalWhitespace(element)) {
                    // Empty list element, to be ignored (RFC 9110 section 5.6.1.2)
                    continue;
                }

                Range range = readRangeSpec(element);

                if (range == null) {
                    result.clear();
                    return result;
                }

                result.add(range);
            }
        }

        return result;
    }

    /**
     * Parse a range-spec: "first-last", "first-" or "-suffix-length" (RFC 9110 section 14.1.1).
     *
     * @param rangeSpec The range-spec, possibly surrounded by optional whitespace.
     * @return The corresponding range, or null if the range-spec is invalid.
     */
    private static Range readRangeSpec(String rangeSpec) {
        Matcher matcher = RANGE_SPEC.matcher(rangeSpec);

        if (!matcher.matches()) {
            return null;
        }

        try {
            if (matcher.group(3) != null) {
                return new Range(Range.INDEX_LAST, Long.parseLong(matcher.group(3)));
            }

            long first = Long.parseLong(matcher.group(1));

            if (matcher.group(2).isEmpty()) {
                return new Range(first, Range.SIZE_MAX);
            }

            long last = Long.parseLong(matcher.group(2));

            if (last < first) {
                return null;
            }

            try {
                return new Range(first, Math.addExact(last - first, 1));
            } catch (ArithmeticException e) {
                // Only an entity of more than Long.MAX_VALUE bytes could tell the difference with
                // an open-ended range.
                return new Range(first, Range.SIZE_MAX);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isOptionalWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c != ' ' && c != '\t') {
                return false;
            }
        }

        return true;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class i.e., it isn't
     * instantiable and extensible.
     */
    private RangeReader() {}
}
