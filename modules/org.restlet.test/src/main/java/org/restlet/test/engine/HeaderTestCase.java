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

package org.restlet.test.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.engine.header.EncodingReader;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.header.PreferenceReader;
import org.restlet.engine.header.TokenReader;
import org.restlet.engine.util.Base64;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.Representation;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the header.
 * 
 * @author Jerome Louvel
 */
public class HeaderTestCase extends RestletTestCase {
    /**
     * Test the {@link HeaderReader#addValues(java.util.Collection)} method.
     */
    public void testAddValues() {
        List<Encoding> list = new ArrayList<Encoding>();
        new EncodingReader("gzip,deflate").addValues(list);
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), Encoding.GZIP);
        assertEquals(list.get(1), Encoding.DEFLATE);

        list = new ArrayList<Encoding>();
        new EncodingReader("gzip,identity, deflate").addValues(list);
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), Encoding.GZIP);
        assertEquals(list.get(1), Encoding.DEFLATE);

        list = new ArrayList<Encoding>();
        new EncodingReader("identity").addValues(list);
        assertTrue(list.isEmpty());

        list = new ArrayList<Encoding>();
        new EncodingReader("identity,").addValues(list);
        assertTrue(list.isEmpty());

        list = new ArrayList<Encoding>();
        new EncodingReader("").addValues(list);
        assertTrue(list.isEmpty());

        list = new ArrayList<Encoding>();
        new EncodingReader(null).addValues(list);
        assertTrue(list.isEmpty());

        TokenReader tr = new TokenReader("bytes");
        List<String> l = tr.readValues();
        assertTrue(l.contains("bytes"));

        tr = new TokenReader("bytes,");
        l = tr.readValues();
        assertTrue(l.contains("bytes"));
        assertEquals(l.size(), 1);

        tr = new TokenReader("");
        l = tr.readValues();
        assertEquals(l.size(), 1);
    }

    public void testExtracting() {
        ArrayList<Header> headers = new ArrayList<Header>();
        String md5hash = "aaaaaaaaaaaaaaaa";
        // encodes to "YWFhYWFhYWFhYWFhYWFhYQ==", the "==" at the end is padding
        String encodedWithPadding = Base64.encode(md5hash.getBytes(), false);
        String encodedNoPadding = encodedWithPadding.substring(0, 22);

        Header header = new Header(HeaderConstants.HEADER_CONTENT_MD5,
                encodedWithPadding);
        headers.add(header);

        // extract Content-MD5 header with padded Base64 encoding, make sure it
        // decodes to original hash
        Representation rep = HeaderUtils.extractEntityHeaders(headers, null);
        assertEquals(rep.getDigest().getAlgorithm(),
                org.restlet.data.Digest.ALGORITHM_MD5);
        assertEquals(new String(rep.getDigest().getValue()), md5hash);

        // extract header with UNpadded encoding, make sure it also decodes to
        // original hash
        header.setValue(encodedNoPadding);
        rep = HeaderUtils.extractEntityHeaders(headers, null);
        assertEquals(rep.getDigest().getAlgorithm(),
                org.restlet.data.Digest.ALGORITHM_MD5);
        assertEquals(new String(rep.getDigest().getValue()), md5hash);
    }

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
        PreferenceReader.addEncodings(header1, clientInfo);
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
        PreferenceReader.addMediaTypes(header1, clientInfo);
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
        PreferenceReader.addMediaTypes(header1, clientInfo);
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
        HeaderReader<Object> hr = new HeaderReader<Object>(header);
        String value = hr.readRawValue();
        int index = 0;

        while (value != null) {
            assertEquals(value, values[index]);
            index++;
            value = hr.readRawValue();
        }
    }

    public void testEmptyValue() throws IOException {
        Header result = HeaderReader.readHeader("My-Header: ");
        assertNotNull(result);
        assertEquals("My-Header", result.getName());
        assertNull(result.getValue());
        try {
            result = HeaderReader.readHeader("My-Header");
            fail("Not allowed");
        } catch (IOException e) {           
        }
        
        result = HeaderReader.readHeader("My-Header:");
        assertNotNull(result);
        assertEquals("My-Header", result.getName());
        assertNull(result.getValue());
    }
}
