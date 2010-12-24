package org.restlet.test.ext.crypto;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.crypto.internal.AwsUtils;
import org.restlet.test.RestletTestCase;

/**
 * Unit test for {@link AwsUtils}. Test cases are taken from the examples
 * provided from <a href=
 * "http://docs.amazonwebservices.com/AmazonS3/latest/index.html?RESTAuthentication.html"
 * >Authenticating REST Requests</a>
 * 
 * @author Jean-Philippe Steinmetz <caskater47@gmail.com>
 */
public class HttpAwsS3UtilsTestCase extends RestletTestCase {
    private static final String ACCESS_KEY = "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o";

    private static final String ATTRIBUTES_HEADERS = "org.restlet.http.headers";

    private Request getRequest;

    private Request putRequest;

    private Request uploadRequest;

    @Before
    public void setUp() throws Exception {
        getRequest = new Request();
        Form headers = new Form();
        getRequest.getAttributes().put(ATTRIBUTES_HEADERS, headers);
        headers.add(HeaderConstants.HEADER_DATE,
                "Tue, 27 Mar 2007 19:36:42 +0000");
        getRequest.setMethod(Method.GET);
        getRequest
                .setResourceRef("http://johnsmith.s3.amazonaws.com/photos/puppy.jpg");

        putRequest = new Request();
        headers = new Form();
        putRequest.getAttributes().put(ATTRIBUTES_HEADERS, headers);
        headers.add(HeaderConstants.HEADER_CONTENT_LENGTH, "94328");
        headers.add(HeaderConstants.HEADER_CONTENT_TYPE, "image/jpeg");
        headers.add(HeaderConstants.HEADER_DATE,
                "Tue, 27 Mar 2007 21:15:45 +0000");
        putRequest.setMethod(Method.PUT);
        putRequest
                .setResourceRef("http://johnsmith.s3.amazonaws.com/photos/puppy.jpg");

        uploadRequest = new Request();
        headers = new Form();
        uploadRequest.getAttributes().put(ATTRIBUTES_HEADERS, headers);
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

    @Test
    public void testGetCanonicalizedAmzHeaders() {
        Form headers = (Form) getRequest.getAttributes()
                .get(ATTRIBUTES_HEADERS);
        String expected = "";
        String actual = AwsUtils.getCanonicalizedAmzHeaders(headers);
        Assert.assertEquals(expected, actual);

        headers = (Form) uploadRequest.getAttributes().get(ATTRIBUTES_HEADERS);
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
        String result = AwsUtils.getSignature(getRequest,
                ACCESS_KEY.toCharArray());
        Assert.assertEquals("xXjDGYUmKxnwqr5KXNPGldn5LbA=", result);

        result = AwsUtils.getSignature(putRequest,
                ACCESS_KEY.toCharArray());
        Assert.assertEquals("hcicpDDvL9SsO6AkvxqmIWkmOuQ=", result);

        result = AwsUtils.getSignature(uploadRequest,
                ACCESS_KEY.toCharArray());
        Assert.assertEquals("C0FlOtU8Ylb9KDTpZqYkZPX91iI=", result);
    }

    @Test
    public void testGetStringToSign() {
        String expected = "GET\n" + "\n" + "\n"
                + "Tue, 27 Mar 2007 19:36:42 +0000\n"
                + "/johnsmith/photos/puppy.jpg";
        String actual = AwsUtils.getStringToSign(getRequest);
        Assert.assertEquals(expected, actual);

        expected = "PUT\n" + "\n" + "image/jpeg\n"
                + "Tue, 27 Mar 2007 21:15:45 +0000\n"
                + "/johnsmith/photos/puppy.jpg";
        actual = AwsUtils.getStringToSign(putRequest);
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
        actual = AwsUtils.getStringToSign(uploadRequest);
        Assert.assertEquals(expected, actual);
    }
}
