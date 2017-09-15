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

package org.restlet.engine.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Date manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public final class DateUtils {

    /**
     * Obsoleted HTTP date format (ANSI C asctime() format). Pattern:
     * "EEE MMM dd HH:mm:ss yyyy".
     */
    public static final List<String> FORMAT_ASC_TIME = unmodifiableList("EEE MMM dd HH:mm:ss yyyy");

    /**
     * Obsoleted HTTP date format (RFC 1036). Pattern:
     * "EEEE, dd-MMM-yy HH:mm:ss zzz".
     */
    public static final List<String> FORMAT_RFC_1036 = unmodifiableList("EEEE, dd-MMM-yy HH:mm:ss zzz");

    /**
     * Preferred HTTP date format (RFC 1123). Pattern:
     * "EEE, dd MMM yyyy HH:mm:ss zzz".
     */
    public static final List<String> FORMAT_RFC_1123 = unmodifiableList("EEE, dd MMM yyyy HH:mm:ss zzz");

    /** W3C date format (RFC 3339). Pattern: "yyyy-MM-dd'T'HH:mm:ssz". */
    public static final List<String> FORMAT_RFC_3339 = unmodifiableList("yyyy-MM-dd'T'HH:mm:ssz");

    /** AWS date format (ISO 8601). Pattern: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'". */
    public static final List<String> FORMAT_ISO_8601 = unmodifiableList("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Common date format (RFC 822). Patterns: "EEE, dd MMM yy HH:mm:ss z" or
     * "EEE, dd MMM yy HH:mm z", "dd MMM yy HH:mm:ss z" or "dd MMM yy HH:mm z".
     */
    public static final List<String> FORMAT_RFC_822 = unmodifiableList(
            "EEE, dd MMM yy HH:mm:ss z", "EEE, dd MMM yy HH:mm z",
            "dd MMM yy HH:mm:ss z", "dd MMM yy HH:mm z");

    // [ifndef gwt] member
    /** Remember the often used GMT time zone. */
    private static final java.util.TimeZone TIMEZONE_GMT = java.util.TimeZone.getTimeZone("GMT");

    // [ifdef gwt] member uncomment
    // private static final com.google.gwt.i18n.client.TimeZone TIMEZONE_GMT =
    // com.google.gwt.i18n.client.TimeZone.createTimeZone(0);
    /**
     * Compares two date with a precision of one second.
     * 
     * @param baseDate
     *            The base date
     * @param afterDate
     *            The date supposed to be after.
     * @return True if the afterDate is indeed after the baseDate.
     */
    public static boolean after(final Date baseDate, final Date afterDate) {
        if ((baseDate == null) || (afterDate == null)) {
            throw new IllegalArgumentException(
                    "Can't compare the dates, at least one of them is null");
        }

        long baseTime = baseDate.getTime() / 1000;
        long afterTime = afterDate.getTime() / 1000;
        return baseTime < afterTime;
    }

    /**
     * Compares two date with a precision of one second.
     * 
     * @param baseDate
     *            The base date
     * @param beforeDate
     *            The date supposed to be before.
     * @return True if the beforeDate is indeed before the baseDate.
     */
    public static boolean before(final Date baseDate, final Date beforeDate) {
        if ((baseDate == null) || (beforeDate == null)) {
            throw new IllegalArgumentException(
                    "Can't compare the dates, at least one of them is null");
        }

        long baseTime = baseDate.getTime() / 1000;
        long beforeTime = beforeDate.getTime() / 1000;
        return beforeTime < baseTime;
    }

    /**
     * Compares two date with a precision of one second.
     * 
     * @param baseDate
     *            The base date
     * @param otherDate
     *            The other date supposed to be equals.
     * @return True if both dates are equals.
     */
    public static boolean equals(final Date baseDate, final Date otherDate) {
        if ((baseDate == null) || (otherDate == null)) {
            throw new IllegalArgumentException(
                    "Can't compare the dates, at least one of them is null");
        }

        long baseTime = baseDate.getTime() / 1000;
        long otherTime = otherDate.getTime() / 1000;
        return otherTime == baseTime;
    }

    /**
     * Formats a Date in the default HTTP format (RFC 1123).
     * 
     * @param date
     *            The date to format.
     * @return The formatted date.
     */
    public static String format(final Date date) {
        return format(date, DateUtils.FORMAT_RFC_1123.get(0));
    }

    /**
     * Formats a Date according to the first format in the array.
     * 
     * @param date
     *            The date to format.
     * @param formats
     *            The array of date formats to use.
     * @return The formatted date.
     */
    public static String format(final Date date, final List<String> formats) {
        return format(date, formats != null ? formats.get(0) : null);
    }

    /**
     * Formats a Date according to the given format.
     * 
     * @param date
     *            The date to format.
     * @param format
     *            The date format to use.
     * @return The formatted date.
     */
    public static String format(final Date date, final String format) {
        if (date == null) {
            throw new IllegalArgumentException("Date is null");
        }

        // [ifndef gwt]
        java.text.DateFormat formatter = null;

        if (FORMAT_RFC_3339.get(0).equals(format)) {
            formatter = new InternetDateFormat(TIMEZONE_GMT);
        } else {
            formatter = new java.text.SimpleDateFormat(format,
                    java.util.Locale.US);
            formatter.setTimeZone(TIMEZONE_GMT);
        }

        return formatter.format(date);
        // [enddef]
        // [ifdef gwt]
        /*
         * GWT difference: DateTimeFormat parser is not passed a Locale in the
         * same way as SimpleDateFormat. It derives locale information from the
         * GWT application's locale.
         * 
         * Default timezone is GMT unless specified via a GMT:hhmm, GMT:+hhmm,
         * or GMT:-hhmm string.
         */
        // [enddef]
        // [ifdef gwt] uncomment
        // final com.google.gwt.i18n.client.DateTimeFormat formatter =
        // com.google.gwt.i18n.client.DateTimeFormat.getFormat(format);
        // return formatter.format(date, TIMEZONE_GMT);
        // [enddef]
    }

    /**
     * Parses a formatted date into a Date object using the default HTTP format
     * (RFC 1123).
     * 
     * @param date
     *            The date to parse.
     * @return The parsed date.
     */
    public static Date parse(String date) {
        return parse(date, FORMAT_RFC_1123);
    }

    /**
     * Parses a formatted date into a Date object.
     * 
     * @param date
     *            The date to parse.
     * @param formats
     *            The date formats to use sorted by completeness.
     * @return The parsed date.
     */
    public static Date parse(String date, List<String> formats) {
        if (date == null) {
            throw new IllegalArgumentException("Date is null");
        }

        Date result = null;

        String format = null;
        int formatsSize = formats.size();

        for (int i = 0; (result == null) && (i < formatsSize); i++) {
            format = formats.get(i);
            // [ifndef gwt]
            java.text.DateFormat parser = null;

            if (FORMAT_RFC_3339.get(0).equals(format)) {
                parser = new InternetDateFormat(TIMEZONE_GMT);
            } else {
                parser = new java.text.SimpleDateFormat(format, java.util.Locale.US);
                parser.setTimeZone(TIMEZONE_GMT);
            }
            // [enddef]
            // [ifdef gwt]
            /*
             * GWT difference: DateTimeFormat parser is not passed a Locale in
             * the same way as SimpleDateFormat. It derives locale information
             * from the GWT application's locale.
             * 
             * Default timezone is GMT unless specified via a GMT:hhmm,
             * GMT:+hhmm, or GMT:-hhmm string.
             */
            // [enddef]
            // [ifdef gwt] uncomment
            // final com.google.gwt.i18n.client.DateTimeFormat parser =
            // com.google.gwt.i18n.client.DateTimeFormat.getFormat(format);
            // [enddef]
            try {
                result = parser.parse(date);
            } catch (Exception e) {
                // Ignores error as the next format may work better
            }
        }

        return result;
    }

    /**
     * Returns an immutable version of a given date.
     * 
     * @param date
     *            The modifiable date.
     * @return An immutable version of a given date.
     */
    public static Date unmodifiable(Date date) {
        return (date == null) ? null : new ImmutableDate(date);
    }

    /**
     * Helper method to help initialize this class by providing unmodifiable
     * lists based on arrays.
     * 
     * @param <T>
     *            Any valid java object
     * @param array
     *            to be converted into an unmodifiable list
     * @return unmodifiable list based on the provided array
     */
    @SafeVarargs
    private static <T> List<T> unmodifiableList(final T... array) {
        return Collections.unmodifiableList(Arrays.asList(array));
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private DateUtils() {

    }

}
