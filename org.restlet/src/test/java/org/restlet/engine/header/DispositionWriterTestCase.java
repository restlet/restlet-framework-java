/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.junit.jupiter.api.Test;
import org.restlet.data.Disposition;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the HTTP calls.
 * 
 * @author Kevin Conaway
 */
public class DispositionWriterTestCase {

    @Test
    public void testFormatContentDisposition() {
        Disposition disposition = new Disposition();
        assertEquals("", DispositionWriter.write(disposition));

        disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        assertEquals("attachment", DispositionWriter.write(disposition));
        disposition.setFilename("");
        assertEquals("attachment; filename=",
                DispositionWriter.write(disposition));
        disposition.setFilename("test.txt");
        assertEquals("attachment; filename=test.txt",
                DispositionWriter.write(disposition));
        disposition.setFilename("file with space.txt");
        assertEquals("attachment; filename=\"file with space.txt\"",
                DispositionWriter.write(disposition));

        disposition.setType(Disposition.TYPE_INLINE);
        assertEquals("inline; filename=\"file with space.txt\"",
                DispositionWriter.write(disposition));

        disposition.getParameters().clear();
        Calendar c = new GregorianCalendar(Locale.ENGLISH);
        c.set(Calendar.YEAR, 2009);
        c.set(Calendar.MONTH, 10);
        c.set(Calendar.DAY_OF_MONTH, 11);
        c.set(Calendar.AM_PM, Calendar.AM);
        c.set(Calendar.HOUR, 10);
        c.set(Calendar.MINUTE, 11);
        c.set(Calendar.SECOND, 12);
        c.set(Calendar.MILLISECOND, 13);
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        disposition.setCreationDate(c.getTime());
        assertEquals("inline; creation-date=\"Wed, 11 Nov 09 10:11:12 GMT\"",
                DispositionWriter.write(disposition));
    }

}
