/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.test;

import java.util.Date;

import junit.framework.TestCase;

import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.util.DateUtils;

import com.noelios.restlet.http.HeaderReader;
import com.noelios.restlet.http.PreferenceUtils;

/**
 * Unit tests for the header.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HeaderTestCase extends TestCase {
    public void testInvalidDate() {
        final String headerValue = "-1";
        final Date date = DateUtils.parse(headerValue,
                DateUtils.FORMAT_RFC_1123);
        assertNull(date);

        final Date unmodifiableDate = DateUtils.unmodifiable(date);
        assertNull(unmodifiableDate);
    }

    /**
     * Tests the parsing.
     */
    public void testParsing() {
        String header1 = "Accept-Encoding,User-Agent";
        String header2 = "Accept-Encoding , User-Agent";
        final String header3 = "Accept-Encoding,\r\tUser-Agent";
        final String header4 = "Accept-Encoding,\r User-Agent";
        final String header5 = "Accept-Encoding, \r \t User-Agent";
        String[] values = new String[] { "Accept-Encoding", "User-Agent" };
        testValues(header1, values);
        testValues(header2, values);
        testValues(header3, values);
        testValues(header4, values);
        testValues(header5, values);

        header1 = "Accept-Encoding, Accept-Language, Accept";
        header2 = "Accept-Encoding,Accept-Language,Accept";
        values = new String[] { "Accept-Encoding", "Accept-Language", "Accept" };
        testValues(header1, values);
        testValues(header2, values);

        // Test the parsing of a "Accept-encoding" header
        header1 = "gzip;q=1.0, identity;q=0.5 , *;q=0";
        ClientInfo clientInfo = new ClientInfo();
        PreferenceUtils.parseEncodings(header1, clientInfo);
        assertEquals(clientInfo.getAcceptedEncodings().get(0).getMetadata(),
                Encoding.GZIP);
        assertEquals(clientInfo.getAcceptedEncodings().get(0).getQuality(),
                1.0F);
        assertEquals(clientInfo.getAcceptedEncodings().get(1).getMetadata(),
                Encoding.IDENTITY);
        assertEquals(clientInfo.getAcceptedEncodings().get(1).getQuality(),
                0.5F);
        assertEquals(clientInfo.getAcceptedEncodings().get(2).getMetadata(),
                Encoding.ALL);
        assertEquals(clientInfo.getAcceptedEncodings().get(2).getQuality(), 0F);

        // Test the parsing of a "Accept" header
        header1 = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
        clientInfo = new ClientInfo();
        PreferenceUtils.parseMediaTypes(header1, clientInfo);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(0).getMetadata(),
                MediaType.TEXT_HTML);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(0).getQuality(),
                1.0F);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(1).getMetadata(),
                MediaType.IMAGE_GIF);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(1).getQuality(),
                1.0F);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(2).getMetadata(),
                MediaType.IMAGE_JPEG);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(2).getQuality(),
                1.0F);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(3).getMetadata(),
                new MediaType("*"));
        assertEquals(clientInfo.getAcceptedMediaTypes().get(3).getQuality(),
                0.2F);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(4).getMetadata(),
                MediaType.ALL);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(4).getQuality(),
                0.2F);

        // Test a more complex header
        header1 = "text/html, application/vnd.wap.xhtml+xml, "
                + "application/xhtml+xml; profile=\"http://www.wapforum.org/xhtml\", "
                + "image/gif, image/jpeg, image/pjpeg, audio/amr, */*";
        clientInfo = new ClientInfo();
        PreferenceUtils.parseMediaTypes(header1, clientInfo);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(0).getMetadata(),
                MediaType.TEXT_HTML);
        assertEquals(clientInfo.getAcceptedMediaTypes().get(0).getQuality(),
                1.0F);
    }

    /**
     * Test that the parsing of a header returns the given array of values.
     * 
     * @param header
     *            The header value to parse.
     * @param values
     *            The parsed values.
     */
    public void testValues(String header, String[] values) {
        final HeaderReader hr = new HeaderReader(header);
        String value = hr.readValue();
        int index = 0;
        while (value != null) {
            assertEquals(value, values[index]);
            index++;
            value = hr.readValue();
        }
    }

}
