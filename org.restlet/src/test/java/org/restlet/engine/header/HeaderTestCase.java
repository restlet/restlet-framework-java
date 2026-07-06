/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.engine.util.DateUtils;

/**
 * Unit tests for the header.
 *
 * @author Jerome Louvel
 */
class HeaderTestCase {
    /** Test the {@link HeaderReader#addValues(java.util.Collection)} method. */
    @Test
    void testAddValues() {
        List<Encoding> list = new ArrayList<>();
        new EncodingReader("gzip,deflate").addValues(list);
        assertEquals(2, list.size());
        assertEquals(Encoding.GZIP, list.getFirst());
        assertEquals(Encoding.DEFLATE, list.get(1));

        list = new ArrayList<>();
        new EncodingReader("gzip,identity, deflate").addValues(list);
        assertEquals(2, list.size());
        assertEquals(Encoding.GZIP, list.getFirst());
        assertEquals(Encoding.DEFLATE, list.get(1));

        list = new ArrayList<>();
        new EncodingReader("identity").addValues(list);
        assertTrue(list.isEmpty());

        list = new ArrayList<>();
        new EncodingReader("identity,").addValues(list);
        assertTrue(list.isEmpty());

        list = new ArrayList<>();
        new EncodingReader("").addValues(list);
        assertTrue(list.isEmpty());

        list = new ArrayList<>();
        new EncodingReader(null).addValues(list);
        assertTrue(list.isEmpty());

        TokenReader tr = new TokenReader("bytes");
        List<String> l = tr.readValues();
        assertTrue(l.contains("bytes"));

        tr = new TokenReader("bytes,");
        l = tr.readValues();
        assertTrue(l.contains("bytes"));
        assertEquals(1, l.size());

        tr = new TokenReader("");
        l = tr.readValues();
        assertEquals(1, l.size());
    }

    @Test
    void testInvalidDate() {
        final String headerValue = "-1";
        final Date date = DateUtils.parse(headerValue, DateUtils.FORMAT_RFC_1123);
        assertNull(date);

        assertNull(DateUtils.unmodifiable(date));
    }

    /** Tests the parsing. */
    @Test
    void testParsing() {
        String header1 = "Accept-Encoding,User-Agent";
        String header2 = "Accept-Encoding , User-Agent";
        final String header3 = "Accept-Encoding,\r\tUser-Agent";
        final String header4 = "Accept-Encoding,\r User-Agent";
        final String header5 = "Accept-Encoding, \r \t User-Agent";
        String[] values = new String[] {"Accept-Encoding", "User-Agent"};
        testValues(header1, values);
        testValues(header2, values);
        testValues(header3, values);
        testValues(header4, values);
        testValues(header5, values);

        header1 = "Accept-Encoding, Accept-Language, Accept";
        header2 = "Accept-Encoding,Accept-Language,Accept";
        values = new String[] {"Accept-Encoding", "Accept-Language", "Accept"};
        testValues(header1, values);
        testValues(header2, values);

        // Test the parsing of an "Accept-encoding" header
        header1 = "gzip;q=1.0, identity;q=0.5 , *;q=0";
        ClientInfo clientInfo = new ClientInfo();
        PreferenceReader.addEncodings(header1, clientInfo);
        assertEquals(Encoding.GZIP, clientInfo.getAcceptedEncodings().getFirst().getMetadata());
        assertEquals(1.0F, clientInfo.getAcceptedEncodings().getFirst().getQuality());
        assertEquals(Encoding.IDENTITY, clientInfo.getAcceptedEncodings().get(1).getMetadata());
        assertEquals(0.5F, clientInfo.getAcceptedEncodings().get(1).getQuality());
        assertEquals(Encoding.ALL, clientInfo.getAcceptedEncodings().get(2).getMetadata());
        assertEquals(0F, clientInfo.getAcceptedEncodings().get(2).getQuality());

        // Test the parsing of an "Accept" header
        header1 = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
        clientInfo = new ClientInfo();
        PreferenceReader.addMediaTypes(header1, clientInfo);
        assertEquals(
                MediaType.TEXT_HTML, clientInfo.getAcceptedMediaTypes().getFirst().getMetadata());
        assertEquals(1.0F, clientInfo.getAcceptedMediaTypes().getFirst().getQuality());
        assertEquals(MediaType.IMAGE_GIF, clientInfo.getAcceptedMediaTypes().get(1).getMetadata());
        assertEquals(1.0F, clientInfo.getAcceptedMediaTypes().get(1).getQuality());
        assertEquals(MediaType.IMAGE_JPEG, clientInfo.getAcceptedMediaTypes().get(2).getMetadata());
        assertEquals(1.0F, clientInfo.getAcceptedMediaTypes().get(2).getQuality());
        assertEquals(new MediaType("*"), clientInfo.getAcceptedMediaTypes().get(3).getMetadata());
        assertEquals(0.2F, clientInfo.getAcceptedMediaTypes().get(3).getQuality());
        assertEquals(MediaType.ALL, clientInfo.getAcceptedMediaTypes().get(4).getMetadata());
        assertEquals(0.2F, clientInfo.getAcceptedMediaTypes().get(4).getQuality());

        // Test a more complex header
        header1 =
                "text/html, application/vnd.wap.xhtml+xml, "
                        + "application/xhtml+xml; profile=\"https://www.wapforum.org/xhtml\", "
                        + "image/gif, image/jpeg, image/pjpeg, audio/amr, */*";
        clientInfo = new ClientInfo();
        PreferenceReader.addMediaTypes(header1, clientInfo);
        assertEquals(
                MediaType.TEXT_HTML, clientInfo.getAcceptedMediaTypes().getFirst().getMetadata());
        assertEquals(1.0F, clientInfo.getAcceptedMediaTypes().getFirst().getQuality());
    }

    /**
     * Test that the parsing of a header returns the given array of values.
     *
     * @param header The header value to parse.
     * @param values The parsed values.
     */
    public void testValues(String header, String[] values) {
        HeaderReader<Object> hr = new HeaderReader<>(header);
        String value = hr.readRawValue();
        int index = 0;

        while (value != null) {
            assertEquals(value, values[index]);
            index++;
            value = hr.readRawValue();
        }
    }

    @Test
    void testEmptyValue() throws IOException {
        Header result = HeaderReader.readHeader("My-Header: ");
        assertNotNull(result);
        assertEquals("My-Header", result.getName());
        assertNull(result.getValue());

        assertThrows(IOException.class, () -> HeaderReader.readHeader("My-Header"));

        result = HeaderReader.readHeader("My-Header:");
        assertNotNull(result);
        assertEquals("My-Header", result.getName());
        assertNull(result.getValue());
    }
}
