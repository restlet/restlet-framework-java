/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.engine;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.restlet.data.Disposition;
import org.restlet.engine.http.header.DispositionReader;
import org.restlet.engine.http.header.DispositionUtils;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the HTTP calls.
 * 
 * @author Kevin Conaway
 */
public class HttpCallTestCase extends RestletTestCase {

    public void testFormatContentDisposition() {
        Disposition disposition = new Disposition();
        assertNull(DispositionUtils.format(disposition));

        disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        assertEquals("attachment", DispositionUtils.format(disposition));
        disposition.setFilename("");
        assertEquals("attachment; filename=", DispositionUtils
                .format(disposition));
        disposition.setFilename("test.txt");
        assertEquals("attachment; filename=test.txt", DispositionUtils
                .format(disposition));
        disposition.setFilename("file with space.txt");
        assertEquals("attachment; filename=\"file with space.txt\"",
                DispositionUtils.format(disposition));

        disposition.setType(Disposition.TYPE_INLINE);
        assertEquals("inline; filename=\"file with space.txt\"",
                DispositionUtils.format(disposition));

        disposition.getParameters().clear();
        Calendar c = new GregorianCalendar(Locale.ENGLISH);
        c.set(Calendar.YEAR, 2009);
        c.set(Calendar.MONTH, 10);
        c.set(Calendar.DAY_OF_MONTH, 11);
        c.set(Calendar.HOUR, 10);
        c.set(Calendar.MINUTE, 11);
        c.set(Calendar.SECOND, 12);
        c.set(Calendar.MILLISECOND, 13);
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        disposition.setCreationDate(c.getTime());
        assertEquals("inline; creation-date=\"Wed, 11 Nov 09 22:11:12 GMT\"",
                DispositionUtils.format(disposition));

    }

    public void testParseContentDisposition() throws IOException {
        Disposition disposition = new DispositionReader(
                "attachment; fileName=\"file.txt\"").readDisposition();
        assertEquals("file.txt", disposition.getParameters().getFirstValue(
                "fileName"));

        disposition = new DispositionReader("attachment; fileName=file.txt")
                .readDisposition();
        assertEquals("file.txt", disposition.getParameters().getFirstValue(
                "fileName"));

        disposition = new DispositionReader(
                "attachment; filename=\"file with space.txt\"")
                .readDisposition();
        assertEquals("file with space.txt", disposition.getParameters()
                .getFirstValue("filename"));

        disposition = new DispositionReader("attachment; filename=\"\"")
                .readDisposition();
        assertEquals("", disposition.getParameters().getFirstValue("filename"));

        disposition = new DispositionReader("attachment; filename=")
                .readDisposition();
        assertEquals("", disposition.getParameters().getFirstValue("filename"));

        disposition = new DispositionReader("attachment; filenam")
                .readDisposition();
        assertNull(disposition.getParameters().getFirstValue("filename"));

        disposition = new DispositionReader(
                "attachment; modification-date=\"Wed, 11 Nov 09 22:11:12 GMT\"")
                .readDisposition();
        String str = disposition.getParameters().getFirstValue(
                "modification-date");
        assertEquals("Wed, 11 Nov 09 22:11:12 GMT", str);

    }
}
