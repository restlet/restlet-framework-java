/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.WeakHashMap;

/**
 * Date manipulation utilities.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @author Piyush Purang (ppurang@gmail.com)
 */
public final class DateUtils {
    /** Preferred HTTP date format (RFC 1123). */
    public static final List<String> FORMAT_RFC_1123 = unmodifiableList("EEE, dd MMM yyyy HH:mm:ss zzz");

    /** Obsoleted HTTP date format (RFC 1036). */
    public static final List<String> FORMAT_RFC_1036 = unmodifiableList("EEEE, dd-MMM-yy HH:mm:ss zzz");

    /** Obsoleted HTTP date format (ANSI C asctime() format). */
    public static final List<String> FORMAT_ASC_TIME = unmodifiableList("EEE MMM dd HH:mm:ss yyyy");

    /** W3C date format (RFC 3339). */
    public static final List<String> FORMAT_RFC_3339 = unmodifiableList(
            "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mmz", "yyyy-MM-dd",
            "yyyy-MM", "yyyy");

    /** Common date format (RFC 822). */
    public static final List<String> FORMAT_RFC_822 = unmodifiableList(
            "EEE, dd MMM yy HH:mm:ss z", "EEE, dd MMM yy HH:mm z",
            "dd MMM yy HH:mm:ss z", "dd MMM yy HH:mm z");

    /** Remember the often used GMT time zone. */
    private static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");

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
        } else {
            long baseTime = baseDate.getTime() / 1000;
            long afterTime = afterDate.getTime() / 1000;
            return baseTime < afterTime;
        }
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
        } else {
            long baseTime = baseDate.getTime() / 1000;
            long beforeTime = beforeDate.getTime() / 1000;
            return beforeTime < baseTime;
        }
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
        } else {
            long baseTime = baseDate.getTime() / 1000;
            long otherTime = otherDate.getTime() / 1000;
            return otherTime == baseTime;
        }
    }

    /**
     * Formats a Date according to the first format in the array.
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
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
            formatter.setTimeZone(TIMEZONE_GMT);
            return formatter.format(date);
        }
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
    public static Date parse(final String date, final List<String> formats) {
        Date result = null;

        if (date == null) {
            throw new IllegalArgumentException("Date is null");
        } else {
            String format = null;
            final int formatsSize = formats.size();
            for (int i = 0; (result == null) && (i < formatsSize); i++) {
                format = formats.get(i);
                SimpleDateFormat parser = new SimpleDateFormat(format,
                        Locale.US);
                parser.setTimeZone(TIMEZONE_GMT);

                try {
                    result = parser.parse(date);
                } catch (ParseException e) {
                    // Ignores error as the next format may work better
                }
            }
        }

        return result;
    }

    /**
     * Helper method to help initialize this class by providing unmodifiable
     * lists based on arrays.
     * 
     * @param <T>
     *            Any valid java object
     * @param array
     *            to be convereted into an unmodifiable list
     * @return unmodifiable list based on the provided array
     */
    private static <T> List<T> unmodifiableList(final T... array) {
        return Collections.unmodifiableList(Arrays.asList(array));
    }

    /**
     * Returns an immutable version of a given date.
     * 
     * @param date
     *            The modifiable date.
     * @return An immutable version of a given date.
     */
    public static Date unmodifiable(Date date) {
        return (date == null) ? null : ImmutableDate.valueOf(date);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instatiable and extensible.
     */
    private DateUtils() {

    }

    /**
     * Class acting as an immutable date class based on the
     * {@link java.util.Date} class.
     * 
     * Throws {@link UnsupportedOperationException} when muttable methopds are
     * invoked.
     * 
     * @author Piyush Purang (ppurang@gmail.com)
     * @see java.util.Date
     * @see <a
     *      href="http://discuss.fogcreek.com/joelonsoftware3/default.asp?cmd=show&ixPost=73959&ixReplies=24">Immutable
     *      Date</a>
     */
    private static final class ImmutableDate extends Date {
        // TODO Are we serializable?
        private static final long serialVersionUID = -5946186780670229206L;

        private static final transient WeakHashMap<Date, ImmutableDate> CACHE = new WeakHashMap<Date, ImmutableDate>();

        /**
         * Returns an ImmutableDate object wrapping the given date.
         * 
         * @param date
         *            object to be made immutable
         * @return an immutable date object
         */
        public static ImmutableDate valueOf(Date date) {
            if (!CACHE.containsKey(date)) {
                CACHE.put(date, new ImmutableDate(date));
            }
            return CACHE.get(date);
        }

        /** Delegate being wrapped */
        private final Date delegate;

        /**
         * Private constructor. A factory method is provided.
         * 
         * @param date
         *            date to be made immutable
         */
        private ImmutableDate(Date date) {
            this.delegate = (Date) date.clone();
        }

        /** {@inheritDoc} */
        @Override
        public boolean after(Date when) {
            return delegate.after(when);
        }

        /** {@inheritDoc} */
        @Override
        public boolean before(Date when) {
            return delegate.before(when);
        }

        /** {@inheritDoc} */
        @Override
        public Object clone() {
            throw new UnsupportedOperationException(
                    "ImmutableDate is immutable");
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(Date anotherDate) {
            return delegate.compareTo(anotherDate);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        /** {@inheritDoc} */
        @Override
        public long getTime() {
            return delegate.getTime();
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return delegate.toString();
        }
    }

}
