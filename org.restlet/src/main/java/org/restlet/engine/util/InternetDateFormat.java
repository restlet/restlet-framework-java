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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles Internet date/time strings in accordance with RFC 3339. It
 * provides static methods to convert from various Java constructs (long, Date,
 * and Calendar) to RFC 3339 format strings and to parse these strings back into
 * the same Java constructs.
 * <p>
 * In addition to the static utility methods, this class also wraps a Calendar
 * object allowing this class to be used as a value object in place of a Java
 * construct.
 * <p>
 * Strings are parsed in accordance with the RFC 3339 format:
 * 
 * <pre>
 * YYYY-MM-DD(T|t|\s)hh:mm:ss[.ddd][tzd]
 * </pre>
 * 
 * The <code>tzd</code> represents the time zone designator and is either an
 * upper or lower case 'Z' indicating UTC or a signed <code>hh:mm</code> offset.
 * 
 * @author Frank Hellwig (frank@hellwig.org)
 */
public class InternetDateFormat extends DateFormat {

    private static volatile DecimalFormat df2 = new DecimalFormat("00");

    private static volatile DecimalFormat df4 = new DecimalFormat("0000");

    /** The Regex pattern to match. */
    private static volatile Pattern pattern;

    private static final long serialVersionUID = 1L;

    /**
     * A time zone with zero offset and no DST.
     */
    public static final TimeZone UTC = new SimpleTimeZone(0, "Z");

    static {
        String reDate = "(\\d{4})-(\\d{2})-(\\d{2})";
        String reTime = "(\\d{2}):(\\d{2}):(\\d{2})(\\.\\d+)?";
        String reZone = "(?:([zZ])|(?:(\\+|\\-)(\\d{2}):(\\d{2})))";
        String re = reDate + "[tT\\s]" + reTime + reZone;
        pattern = Pattern.compile(re);
    }

    /**
     * Returns the current date and time as an RFC 3339 date/time string using
     * the UTC (Z) time zone.
     * 
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public static String now() {
        return now(UTC);
    }

    /**
     * Returns the current date and time as an RFC 3339 date/time string using
     * the specified time zone.
     * 
     * @param zone
     *            the time zone to use
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public static String now(TimeZone zone) {
        return toString(System.currentTimeMillis(), zone);
    }

    /**
     * Our private parse utility that parses the string, clears the calendar,
     * and then sets the fields.
     * 
     * @param s
     *            the string to parse
     * @param cal
     *            the calendar object to populate
     * @throws IllegalArgumentException
     *             if the string is not a valid RFC 3339 date/time string
     */
    private static void parse(String s, Calendar cal) {
        Matcher m = pattern.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid date/time: " + s);
        }
        cal.clear();
        cal.set(Calendar.YEAR, Integer.parseInt(m.group(1)));
        cal.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(3)));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(4)));
        cal.set(Calendar.MINUTE, Integer.parseInt(m.group(5)));
        cal.set(Calendar.SECOND, Integer.parseInt(m.group(6)));
        if (m.group(7) != null) {
            float fraction = Float.parseFloat(m.group(7));
            cal.set(Calendar.MILLISECOND, (int) (fraction * 1000F));
        }
        if (m.group(8) != null) {
            cal.setTimeZone(new SimpleTimeZone(0, "Z"));
        } else {
            int sign = m.group(9).equals("-") ? -1 : 1;
            int tzhour;
            tzhour = Integer.parseInt(m.group(10));
            int tzminute = Integer.parseInt(m.group(11));
            int offset = sign * ((tzhour * 60) + tzminute);
            String id = Integer.toString(offset);
            cal.setTimeZone(new SimpleTimeZone(offset * 60000, id));
        }
    }

    /**
     * Parses an RFC 3339 date/time string to a Calendar object.
     * 
     * @param s
     *            the string to parse
     * @return the Calendar object
     * @throws IllegalArgumentException
     *             if the string is not a valid RFC 3339 date/time string
     */
    public static Calendar parseCalendar(String s) {
        Calendar cal = new GregorianCalendar();
        parse(s, cal);
        return cal;
    }

    /**
     * Parses an RFC 3339 date/time string to a Date object.
     * 
     * @param s
     *            the string to parse
     * @return the Date object
     * @throws IllegalArgumentException
     *             if the string is not a valid RFC 3339 date/time string
     */
    public static Date parseDate(String s) {
        Calendar cal = new GregorianCalendar();
        parse(s, cal);
        return cal.getTime();
    }

    /**
     * Parses an RFC 3339 date/time string to a millisecond time value.
     * 
     * @param s
     *            the string to parse
     * @return the millisecond time value
     * @throws IllegalArgumentException
     *             if the string is not a valid RFC 3339 date/time string
     */
    public static long parseTime(String s) {
        Calendar cal = new GregorianCalendar();
        parse(s, cal);
        return cal.getTimeInMillis();
    }

    /**
     * Converts the specified Calendar object to an RFC 3339 date/time string.
     * Unlike the toString methods for Date and long, no additional variant of
     * this method taking a time zone is provided since the time zone is built
     * into the Calendar object.
     * 
     * @param cal
     *            the Calendar object
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public static String toString(Calendar cal) {
        StringBuilder buf = new StringBuilder();
        buf.append(df4.format(cal.get(Calendar.YEAR)));
        buf.append("-");
        buf.append(df2.format(cal.get(Calendar.MONTH) + 1l));
        buf.append("-");
        buf.append(df2.format(cal.get(Calendar.DAY_OF_MONTH)));
        buf.append("T");
        buf.append(df2.format(cal.get(Calendar.HOUR_OF_DAY)));
        buf.append(":");
        buf.append(df2.format(cal.get(Calendar.MINUTE)));
        buf.append(":");
        buf.append(df2.format(cal.get(Calendar.SECOND)));

        int ms = cal.get(Calendar.MILLISECOND);
        if (ms != 0) {
            buf.append(".").append((int) (ms / 10F));
        }

        int tzminute = (cal.get(Calendar.ZONE_OFFSET) + cal
                .get(Calendar.DST_OFFSET)) / 60000;
        if (tzminute == 0) {
            buf.append("Z");
        } else {
            if (tzminute < 0) {
                tzminute = -tzminute;
                buf.append("-");
            } else {
                buf.append("+");
            }
            int tzhour = tzminute / 60;
            tzminute -= tzhour * 60;
            buf.append(df2.format(tzhour));
            buf.append(":");
            buf.append(df2.format(tzminute));
        }
        return buf.toString();
    }

    /**
     * Converts the specified Date object to an RFC 3339 date/time string using
     * the UTC (Z) time zone.
     * 
     * @param date
     *            the Date object
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public static String toString(Date date) {
        return toString(date, UTC);
    }

    /**
     * Converts the specified Date object to an RFC 3339 date/time string using
     * the specified time zone.
     * 
     * @param date
     *            the Date object
     * @param zone
     *            the time zone to use
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public static String toString(Date date, TimeZone zone) {
        InternetDateFormat dt = new InternetDateFormat(date, zone);
        return dt.toString();
    }

    /**
     * Converts the specified millisecond time value to an RFC 3339 date/time
     * string using the UTC (Z) time zone.
     * 
     * @param time
     *            the millisecond time value
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public static String toString(long time) {
        return toString(time, UTC);
    }

    /**
     * Converts the specified millisecond time value to an RFC 3339 date/time
     * string using the specified time zone.
     * 
     * @param time
     *            the millisecond time value
     * @param zone
     *            the time zone to use
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public static String toString(long time, TimeZone zone) {
        InternetDateFormat dt = new InternetDateFormat(time, zone);
        return dt.toString();
    }

    /**
     * Creates a new InternetDateFormat object from the specified Date object
     * using the UTC (Z) time zone.
     * 
     * @param date
     *            the Date object
     * @return the InternetDateFormat object
     */
    public static InternetDateFormat valueOf(Date date) {
        return new InternetDateFormat(date);
    }

    /**
     * Creates a new InternetDateFormat object from the specified Date object
     * using the specified time zone.
     * 
     * @param date
     *            the Date object
     * @param zone
     *            the time zone to use
     * @return the InternetDateFormat object
     */
    public static InternetDateFormat valueOf(Date date, TimeZone zone) {
        return new InternetDateFormat(date, zone);
    }

    /**
     * Creates a new InternetDateFormat object from the specified millisecond
     * time value using the UTC (Z) time zone.
     * 
     * @param time
     *            the millisecond time value
     * @return the InternetDateFormat object
     */
    public static InternetDateFormat valueOf(long time) {
        return new InternetDateFormat(time);
    }

    /**
     * Creates a new InternetDateFormat object from the specified millisecond
     * time value using the specified time zone.
     * 
     * @param time
     *            the millisecond time value
     * @param zone
     *            the time zone to use
     * @return the InternetDateFormat object
     */
    public static InternetDateFormat valueOf(long time, TimeZone zone) {
        return new InternetDateFormat(time, zone);
    }

    /**
     * Creates a new InternetDateFormat object by parsing an RFC 3339 date/time
     * string.
     * 
     * @param s
     *            the string to parse
     * @return the InternetDateFormat object
     * @throws IllegalArgumentException
     *             if the string is not a valid RFC 3339 date/time string
     */
    public static InternetDateFormat valueOf(String s) {
        return new InternetDateFormat(s);
    }

    /**
     * The Calendar object that allows this class to act as a value holder.
     */
    private Calendar cal;

    /**
     * Creates a new InternetDateFormat object set to the current time using the
     * UTC (Z) time zone.
     */
    public InternetDateFormat() {
        this(UTC);
    }

    /**
     * Creates a new InternetDateFormat object initialized from a Calendar
     * object. The specified calendar object is cloned thereby isolating this
     * InternetDateFormat object from any changes made to the specified calendar
     * object after calling this constructor.
     * 
     * @param cal
     *            the Calendar object
     */
    public InternetDateFormat(Calendar cal) {
        this.cal = (Calendar) cal.clone();
    }

    /**
     * Creates a new InternetDateFormat object initialized from a Date object
     * using the UTC (Z) time zone.
     * 
     * @param date
     *            the Date object
     */
    public InternetDateFormat(Date date) {
        this(date, UTC);
    }

    /**
     * Creates a new InternetDateFormat object initialized from a Date object
     * using the specified time zone.
     * 
     * @param date
     *            the Date object
     * @param zone
     *            the time zone to use
     */
    public InternetDateFormat(Date date, TimeZone zone) {
        cal = new GregorianCalendar(zone);
        cal.setTime(date);
    }

    /**
     * Creates a new InternetDateFormat object initialized from a millisecond
     * time value using the UTC (Z) time zone.
     * 
     * @param time
     *            the millisecond time value
     */
    public InternetDateFormat(long time) {
        this(time, UTC);
    }

    /**
     * Creates a new InternetDateFormat object initialized from a millisecond
     * time value using the specified time zone.
     * 
     * @param time
     *            the millisecond time value
     * @param zone
     *            the time zone to use
     */
    public InternetDateFormat(long time, TimeZone zone) {
        cal = new GregorianCalendar(zone);
        cal.setTimeInMillis(time);
    }

    /**
     * Creates a new InternetDateFormat object by parsing an RFC 3339 date/time
     * string.
     * 
     * @param s
     *            the string to parse
     * @throws IllegalArgumentException
     *             if the string is not a valid RFC 3339 date/time string
     */
    public InternetDateFormat(String s) {
        cal = parseCalendar(s);
    }

    /**
     * Creates a new InternetDateFormat object set to the current time using the
     * specified time zone.
     * 
     * @param zone
     *            the time zone to use
     */
    public InternetDateFormat(TimeZone zone) {
        cal = new GregorianCalendar(zone);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo,
            FieldPosition fieldPosition) {
        return toAppendTo.append(valueOf(date));
    }

    /**
     * Gets the Calendar object wrapped by this InternetDateFormat object.
     * 
     * @return the cloned Calendar object
     */
    public Calendar getCalendar() {
        return (Calendar) cal.clone();
    }

    /**
     * Gets the value of this InternetDateFormat object as a Date object.
     * 
     * @return the Date object
     */
    public Date getDate() {
        return cal.getTime();
    }

    /**
     * Gets the value of this InternetDateFormat object as millisecond time
     * value.
     * 
     * @return the millisecond time value
     */
    public long getTime() {
        return cal.getTimeInMillis();
    }

    @Override
    public Date parse(String source) throws ParseException {
        return parse(source, (ParsePosition) null);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        return parseDate(source);
    }

    /**
     * Converts this InternetDateFormat object to an RFC 3339 date/time string.
     * 
     * @return an RFC 3339 date/time string (does not include milliseconds)
     */
    public String toString() {
        return toString(cal);
    }
}
