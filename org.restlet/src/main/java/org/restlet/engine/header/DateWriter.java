/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.util.Date;
import org.restlet.engine.util.DateUtils;

/**
 * Date header writer.
 *
 * @author Jerome Louvel
 */
public class DateWriter {

    /**
     * Writes a date header.
     *
     * @param date The date to write.
     * @return The formatted date.
     */
    public static String write(Date date) {
        return write(date, false);
    }

    /**
     * Writes a date header.
     *
     * @param date The date to write.
     * @param cookie Indicates if the date should be in the cookie format.
     * @return The formatted date.
     */
    public static String write(Date date, boolean cookie) {
        if (cookie) {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1036.getFirst());
        }

        return DateUtils.format(date);
    }
}
