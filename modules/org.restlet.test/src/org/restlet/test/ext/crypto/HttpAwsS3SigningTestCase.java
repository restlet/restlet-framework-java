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

package org.restlet.test.ext.crypto;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.crypto.internal.AwsUtils;
import org.restlet.test.RestletTestCase;
import org.restlet.util.Series;

/**
 * Unit test for {@link AwsUtils}. Test cases are taken from the examples
 * provided from <a href=
 * "http://docs.amazonwebservices.com/AmazonS3/latest/index.html?RESTAuthentication.html"
 * >Authenticating REST Requests</a>
 * 
 * @author Jean-Philippe Steinmetz <caskater47@gmail.com>
 */
public class HttpAwsS3SigningTestCase extends RestletTestCase {
    private static final String ACCESS_KEY = "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o";

    private Request getRequest;

    private Request putRequest;

    private Request uploadRequest;

    @Before
    public void setUp() throws Exception {
        getRequest = new Request();
        Series<Header> headers = new Series<Header>(Header.class);
        getRequest.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                headers);
        headers.add(HeaderConstants.HEADER_DATE,
                "Tue, 27 Mar 2007 19:36:42 +0000");
        getRequest.setMethod(Method.GET);
        getRequest
                .setResourceRef("http://johnsmith.s3.amazonaws.com/photos/puppy.jpg");

        putRequest = new Request();
        headers = new Series<Header>(Header.class);
        putRequest.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                headers);
        headers.add(HeaderConstants.HEADER_CONTENT_LENGTH, "94328");
        headers.add(HeaderConstants.HEADER_CONTENT_TYPE, "image/jpeg");
        headers.add(HeaderConstants.HEADER_DATE,
                "Tue, 27 Mar 2007 21:15:45 +0000");
        putRequest.setMethod(Method.PUT);
        putRequest
                .setResourceRef("http://johnsmith.s3.amazonaws.com/photos/puppy.jpg");

        uploadRequest = new Request();
        headers = new Series<Header>(Header.class);
        uploadRequest.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                headers);
        headers.add(HeaderConstants.HEADER_CONTENT_LENGTH, "5913339");
        headers.add(HeaderConstants.HEADER_CONTENT_MD5,
                "4gJE4saaMU4BqNR0kLY+lw==");
        headers.add(HeaderConstants.HEADER_CONTENT_TYPE,
                "application/x-download");
        headers.add(HeaderConstants.HEADER_DATE,
                "Tue, 27 Mar 2007 21:06:08 +0000");
        uploadRequest.setMethod(Method.PUT);
        uploadRequest
                .setResourceRef("http://static.johnsmith.net:8080/db-backup.dat.gz");
        headers.add("x-amz-acl", "public-read");
        headers.add("X-Amz-Meta-ReviewedBy", "joe@johnsmith.net");
        headers.add("X-Amz-Meta-ReviewedBy", "jane@johnsmith.net");
        headers.add("X-Amz-Meta-FileChecksum", "0x02661779");
        headers.add("X-Amz-Meta-ChecksumAlgorithm", "crc32");
    }

    @After
    public void tearDown() throws Exception {
        getRequest = null;
        putRequest = null;
        uploadRequest = null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCanonicalizedAmzHeaders() {
        Series<Header> headers = (Series<Header>) getRequest.getAttributes()
                .get(HeaderConstants.ATTRIBUTE_HEADERS);
        String expected = "";
        String actual = AwsUtils.getCanonicalizedAmzHeaders(headers);
        Assert.assertEquals(expected, actual);

        headers = (Series<Header>) uploadRequest.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        expected = "x-amz-acl:public-read\n"
                + "x-amz-meta-checksumalgorithm:crc32\n"
                + "x-amz-meta-filechecksum:0x02661779\n"
                + "x-amz-meta-reviewedby:joe@johnsmith.net,jane@johnsmith.net\n";
        actual = AwsUtils.getCanonicalizedAmzHeaders(headers);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetCanonicalizedResourceName() {
        String result = AwsUtils.getCanonicalizedResourceName(getRequest
                .getResourceRef());
        Assert.assertEquals("/johnsmith/photos/puppy.jpg", result);
    }

    @Test
    public void testGetSignature() {
        String result = AwsUtils.getS3Signature(getRequest,
                ACCESS_KEY.toCharArray());
        Assert.assertEquals("xXjDGYUmKxnwqr5KXNPGldn5LbA=", result);

        result = AwsUtils.getS3Signature(putRequest, ACCESS_KEY.toCharArray());
        Assert.assertEquals("hcicpDDvL9SsO6AkvxqmIWkmOuQ=", result);

        result = AwsUtils.getS3Signature(uploadRequest,
                ACCESS_KEY.toCharArray());
        Assert.assertEquals("C0FlOtU8Ylb9KDTpZqYkZPX91iI=", result);
    }

    @Test
    public void testGetStringToSign() {
        String expected = "GET\n" + "\n" + "\n"
                + "Tue, 27 Mar 2007 19:36:42 +0000\n"
                + "/johnsmith/photos/puppy.jpg";
        String actual = AwsUtils.getS3StringToSign(getRequest);
        Assert.assertEquals(expected, actual);

        expected = "PUT\n" + "\n" + "image/jpeg\n"
                + "Tue, 27 Mar 2007 21:15:45 +0000\n"
                + "/johnsmith/photos/puppy.jpg";
        actual = AwsUtils.getS3StringToSign(putRequest);
        Assert.assertEquals(expected, actual);

        expected = "PUT\n" + "4gJE4saaMU4BqNR0kLY+lw==\n"
                + "application/x-download\n"
                + "Tue, 27 Mar 2007 21:06:08 +0000\n"
                + "x-amz-acl:public-read\n"
                + "x-amz-meta-checksumalgorithm:crc32\n"
                + "x-amz-meta-filechecksum:0x02661779\n"
                + "x-amz-meta-reviewedby:"
                + "joe@johnsmith.net,jane@johnsmith.net\n"
                + "/static.johnsmith.net/db-backup.dat.gz";
        actual = AwsUtils.getS3StringToSign(uploadRequest);
        Assert.assertEquals(expected, actual);
    }
}
