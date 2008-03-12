/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.test.jaxrs.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.core.JaxRsPathSegment;
import org.restlet.ext.jaxrs.core.JaxRsUriBuilder;
import org.restlet.ext.jaxrs.core.MultivaluedMapImpl;
import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.car.CarResource;
import org.restlet.test.jaxrs.services.resources.SimpleTrain;

/**
 * @author Stephan
 * 
 */
public class JaxRsUriBuilderTest extends TestCase {

    private static final URI URI_1;
    static {
        try {
            URI_1 = new URI("http://localhost/path1/path2");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can not initialize JaxRsUriBuilderTest");
        }
    }

    /**
     * @param expectedUri
     * @param actualUri
     * @throws URISyntaxException
     */
    static void assertEqualsURI(String expectedUri, URI actualUri)
            throws URISyntaxException {
        assertEquals(new URI(expectedUri), actualUri);
        assertEquals(expectedUri, actualUri.toString());
    }

    static void assertEqualsURI(String expectedUri, UriBuilder actual)
            throws Exception {
        assertEqualsURI(expectedUri, actual.build());
    }

    static void assertEqualsUriSlashAllowed(String expectedUri,
            UriBuilder actual) throws URISyntaxException {
        try {
            assertEqualsURI(expectedUri, actual.build());
        } catch (AssertionFailedError ife) {
            assertEqualsURI(expectedUri + "/", actual.build());
        }
    }

    /**
     * Note, that the actual value is at the beginning, because of the
     * expectedPathSegents must be the last parameter.
     * 
     * @param actualUriBuilder
     * @param expectedScheme
     * @param expectedUserInfo
     * @param expectedHost
     * @param expectedPort
     * @param expectedQuery
     * @param expectedPathSegments
     */
    private static void assertEqualUriBuilder(UriBuilder actualUriBuilder,
            String expectedScheme, String expectedUserInfo,
            String expectedHost, int expectedPort, String expectedQuery,
            JaxRsPathSegment... expectedPathSegments) throws Exception {
        if (actualUriBuilder instanceof JaxRsUriBuilder) {
            JaxRsUriBuilder jaxRsUriBuilder = (JaxRsUriBuilder) actualUriBuilder;
            assertEquals(expectedScheme, getScheme(jaxRsUriBuilder));
            assertEquals(expectedUserInfo, getUserInfo(jaxRsUriBuilder));
            assertEquals(expectedHost, getHost(jaxRsUriBuilder));
            assertEquals(expectedPort, getPort(jaxRsUriBuilder));
            List<PathSegment> actPathSegms = getPathSegments(jaxRsUriBuilder);
            for (int i = 0; i < expectedPathSegments.length; i++) {
                PathSegment expectedPathSegment = expectedPathSegments[i];
                PathSegment actPathSegm = actPathSegms.get(i);
                assertEquals(i + ". path segm:", expectedPathSegment,
                        actPathSegm);
            }
            assertEquals(expectedPathSegments.length, actPathSegms.size());
            CharSequence actualQuery = getQuery(jaxRsUriBuilder);
            if (actualQuery != null)
                actualQuery = actualQuery.toString();
            assertEquals(expectedQuery, actualQuery);
        }
        UriBuilder expectedUriBuilder = RuntimeDelegate.getInstance()
                .createUriBuilder();
        expectedUriBuilder.encode(false);
        if (expectedScheme != null)
            expectedUriBuilder.scheme(expectedScheme);
        if (expectedUserInfo != null)
            expectedUriBuilder.userInfo(expectedUserInfo);
        if (expectedHost != null)
            expectedUriBuilder.host(expectedHost);
        expectedUriBuilder.port(expectedPort);
        for (JaxRsPathSegment pathSegment : expectedPathSegments) {
            expectedUriBuilder.path(pathSegment.getPath());
            for (Map.Entry<String, List<String>> mpEntry : pathSegment
                    .getMatrixParameters().entrySet())
                for (String mpValue : mpEntry.getValue())
                    expectedUriBuilder.matrixParam(mpEntry.getKey(), mpValue);
        }
        if (expectedQuery != null)
            expectedUriBuilder.replaceQueryParams(expectedQuery);
        assertEquals(expectedUriBuilder.build(), actualUriBuilder.build());
    }

    /**
     * @param jaxRsUriBuilder
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static Object getFieldValue(JaxRsUriBuilder jaxRsUriBuilder,
            String fieldName) throws Exception {
        Field queryField = jaxRsUriBuilder.getClass().getDeclaredField(
                fieldName);
        queryField.setAccessible(true);
        return queryField.get(jaxRsUriBuilder);
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getHost(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return (String) getFieldValue(jaxRsUriBuilder, "host");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    @SuppressWarnings("unchecked")
    private static List<PathSegment> getPathSegments(
            JaxRsUriBuilder jaxRsUriBuilder) throws Exception {
        return (List) getFieldValue(jaxRsUriBuilder, "pathSegments");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static int getPort(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return ((Number) getFieldValue(jaxRsUriBuilder, "port")).intValue();
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getQuery(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return (String) getFieldValue(jaxRsUriBuilder, "query");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getScheme(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return (String) getFieldValue(jaxRsUriBuilder, "scheme");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getUserInfo(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return (String) getFieldValue(jaxRsUriBuilder, "userInfo");
    }

    public static void main(String[] args) {
        System.out.println(Reference.encode("%"));
    }

    private UriBuilder uriBuilder1Enc;

    private UriBuilder uriBuilder1NoE;

    /**
     * UriBuilder with variableNames.
     */
    private UriBuilder uriBuilderWithVarsEnc;

    private UriBuilder uriBuilderWithVarsNoE;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uriBuilder1Enc = RuntimeDelegate.getInstance().createUriBuilder();
        uriBuilder1Enc.host("localhost");
        uriBuilder1Enc.path("path1", "path2");
        uriBuilder1Enc.scheme("http");
        uriBuilder1NoE = uriBuilder1Enc.clone();
        uriBuilder1NoE.encode(false);
        uriBuilderWithVarsEnc = RuntimeDelegate.getInstance()
                .createUriBuilder();
        uriBuilderWithVarsEnc.host("localhost");
        uriBuilderWithVarsEnc.scheme("http");
        uriBuilderWithVarsEnc.path("abc", "{var1}", "def", "{var2}");
        uriBuilderWithVarsNoE = uriBuilderWithVarsEnc.clone();
        uriBuilderWithVarsNoE.encode(false);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#build()}.
     */
    public void testBuild() throws Exception {
        assertEquals(URI_1, uriBuilder1Enc.build());
        assertEquals(URI_1, uriBuilder1NoE.build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#build(java.util.Map)}.
     */
    public void testBuildMap() throws Exception {
        assertEqualsURI("http://localhost/abc/%7Bvar1%7D/def/%7Bvar2%7D",
                uriBuilderWithVarsEnc);
        Map<String, String> vars = new HashMap<String, String>();
        try {
            uriBuilderWithVarsEnc.build(vars);
            fail("must fail, because missing UriTemplate variables");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var1", "123");
        try {
            uriBuilderWithVarsEnc.build(vars);
            fail("must fail, because missing UriTemplate variable");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var2", "456");
        assertEqualsURI("http://localhost/abc/123/def/456",
                uriBuilderWithVarsEnc.build(vars));
        vars.put("var3", "789");
        assertEqualsURI("http://localhost/abc/123/def/456",
                uriBuilderWithVarsEnc.build(vars));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#build(java.lang.String[])}.
     */
    public void testBuildStringArray() throws Exception {
        try {
            uriBuilderWithVarsEnc.build("123");
            fail("must fail, because there are not enough arguments");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        URI uri = uriBuilderWithVarsEnc.build("123", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uri);
        UriBuilder uriBuilder2 = uriBuilderWithVarsEnc.clone();
        assertEqualsURI("http://localhost/abc/123/def/456", uriBuilder2.build(
                "123", "456"));
        assertEquals(uriBuilderWithVarsEnc.toString(), uriBuilder2.toString());
        uriBuilder2.path("{var3}");
        uri = uriBuilderWithVarsEnc.build("123", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uri);
        try {
            uriBuilder2.build("123", "456");
            fail("must fail, because there are not enough arguments");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        URI uri2 = uriBuilder2.build("123", "456", "789");
        assertEqualsURI("http://localhost/abc/123/def/456/789", uri2);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#clone()}.
     */
    public void testClone() {
        assertEquals(uriBuilder1Enc.build(), uriBuilder1Enc.clone().build());
        URI clonedEncWithVars = uriBuilderWithVarsEnc.clone().build();
        assertEquals(uriBuilderWithVarsEnc.build(), clonedEncWithVars);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#encode(boolean)}.
     */
    public void testEncode() throws Exception {
        UriBuilder uriBuilder = RuntimeDelegate.getInstance()
                .createUriBuilder();
        uriBuilder.encode(false);
        uriBuilder.host("www.xyz.de");
        uriBuilder.scheme("http");
        uriBuilder.path("path1", "path2");
        try {
            uriBuilder.path("hh:ho");
            fail("must fail, because of invalid character");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        uriBuilder.encode(true);
        uriBuilder.path("hh:ho");
        assertEqualsURI("http://www.xyz.de/path1/path2/hh%3Aho", uriBuilder);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#fragment(java.lang.String)}.
     */
    public void testFragmentEnc() throws Exception {
        uriBuilder1Enc.fragment(String.valueOf((char) 9));
        assertEqualsURI(URI_1 + "#%09", uriBuilder1Enc);

        uriBuilder1Enc.fragment("anker");
        assertEqualsURI(URI_1 + "#anker", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#fragment(java.lang.String)}.
     */
    public void testFragmentNoE() throws Exception {
        try {
            uriBuilder1NoE.fragment(String.valueOf((char) 9));
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEquals(URI_1, uriBuilder1NoE.build());
        uriBuilder1NoE.fragment("anker");
        assertEqualsURI(URI_1 + "#anker", uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#host(java.lang.String)}.
     */
    public void testHostEnc() throws Exception {
        uriBuilder1Enc.host("test.domain.org");
        assertEqualsURI("http://test.domain.org/path1/path2", uriBuilder1Enc);

        try {
            uriBuilder1Enc.host("test.domain .org ä");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://test.domain.org/path1/path2", uriBuilder1Enc);
    }

    public void testHostNoE() throws Exception {
        uriBuilder1NoE.host("test.domain.org");
        assertEqualsURI("http://test.domain.org/path1/path2", uriBuilder1NoE);

        try {
            uriBuilder1NoE.host("test.domain .org ä");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://test.domain.org/path1/path2", uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#replaceMatrixParams(java.lang.String)}.
     * and
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#matrixParam(java.lang.String, java.lang.String)}.
     */
    public void testMatrixParamEnc() throws Exception {
        uriBuilder1Enc.matrixParam("mp1", "mv1");
        assertEqualsURI(URI_1 + ";mp1=mv1", uriBuilder1Enc);
        uriBuilder1Enc.matrixParam("mp1", "mv2");
        assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2", uriBuilder1Enc);
        uriBuilder1Enc.matrixParam("mp3", "mv3");
        try {
            assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2;mp3=mv3", uriBuilder1Enc);
        } catch (AssertionFailedError afe) { // try another possibility
            assertEqualsURI(URI_1 + ";mp3=mv3;mp1=mv1;mp1=mv2", uriBuilder1Enc);
        }
        uriBuilder1Enc.replaceMatrixParams("mp4=mv4");
        assertEqualsURI(URI_1 + ";mp4=mv4", uriBuilder1Enc);
        uriBuilder1Enc.replaceMatrixParams("");
        assertEquals(URI_1, uriBuilder1Enc.build());

        uriBuilder1Enc.replaceMatrixParams(null);
        uriBuilder1Enc.matrixParam("jkj$sdf", "ij a%20");
        assertEqualsURI(URI_1 + ";jkj%24sdf=ij%20a%2520", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#replaceMatrixParams(java.lang.String)}.
     * and
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#matrixParam(java.lang.String, java.lang.String)}.
     */
    public void testMatrixParamNoE() throws Exception {
        uriBuilder1NoE.matrixParam("mp1", "mv1");
        assertEqualsURI(URI_1 + ";mp1=mv1", uriBuilder1NoE);
        uriBuilder1NoE.matrixParam("mp1", "mv2");
        assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2", uriBuilder1NoE);
        uriBuilder1NoE.matrixParam("mp3", "mv3");
        try {
            assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2;mp3=mv3", uriBuilder1NoE);
        } catch (AssertionFailedError afe) { // try another possibility
            assertEqualsURI(URI_1 + ";mp3=mv3;mp1=mv1;mp1=mv2", uriBuilder1NoE);
        }
        uriBuilder1NoE.replaceMatrixParams("mp4=mv4");
        assertEqualsURI(URI_1 + ";mp4=mv4", uriBuilder1NoE);
        uriBuilder1NoE.replaceMatrixParams("");
        assertEquals(URI_1, uriBuilder1NoE.build());

        uriBuilder1NoE.replaceMatrixParams(null);
        try {
            uriBuilder1NoE.matrixParam("jkj$sdf", "ij a%20");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEquals(URI_1, uriBuilder1NoE.build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.Class)}.
     */
    public void testPathClass() throws Exception {
        uriBuilder1Enc.replacePath((String[]) null);
        uriBuilder1Enc.path(SimpleTrain.class);
        assertEqualsURI("http://localhost" + SimpleTrain.PATH, uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.Class, java.lang.String)}.
     */
    public void testPathClassString() throws Exception {
        uriBuilder1Enc.replacePath((String[]) null);
        uriBuilder1Enc.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.PATH + "/"
                + CarListResource.OFFERS_PATH, uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.Class, java.lang.String)}.
     */
    public void testPathClassStringEnc() throws Exception {
        uriBuilder1Enc.replacePath((String[]) null);
        uriBuilder1Enc.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.PATH + "/"
                + CarListResource.OFFERS_PATH, uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.reflect.Method[])}.
     */
    public void testPathMethodArrayEnc() throws Exception {
        uriBuilder1Enc.replacePath((String[]) null);
        Method findCar = CarListResource.class.getMethod("findCar",
                Integer.TYPE);
        Method engine = CarResource.class.getMethod("findEngine");
        uriBuilder1Enc.path(CarListResource.class);
        uriBuilder1Enc.path(findCar, engine);
        assertEqualsURI("http://localhost/" + CarListResource.PATH
                + "/5/engine", uriBuilder1Enc.build("5"));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.String[])}.
     */
    public void testPathStringArrayEnc() throws Exception {
        uriBuilder1Enc.path("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", uriBuilder1Enc);
        uriBuilder1Enc.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", uriBuilder1Enc);

        uriBuilder1Enc.path("$");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno/%24", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.String[])}.
     */
    public void testPathStringArrayNoE() throws Exception {
        uriBuilder1NoE.path("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", uriBuilder1NoE);
        uriBuilder1NoE.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", uriBuilder1NoE);

        try {
            uriBuilder1NoE.path("$");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#port(int)}.
     */
    public void testPort() throws Exception {
        uriBuilder1Enc.port(4711);
        assertEqualsURI("http://localhost:4711/path1/path2", uriBuilder1Enc);
        uriBuilder1Enc.port(-1);
        assertEqualsURI("http://localhost/path1/path2", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#queryParam(java.lang.String, java.lang.String)}.
     */
    public void testQueryEnc() throws Exception {
        uriBuilder1Enc.queryParam("qn", "qv");
        assertEqualsURI(URI_1 + "?qn=qv", uriBuilder1Enc);
        uriBuilder1Enc.queryParam("qn", "qv2");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2", uriBuilder1Enc);
        uriBuilder1Enc.queryParam("qn3", "qv3");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2&qn3=qv3", uriBuilder1Enc);
        uriBuilder1Enc.replaceQueryParams("qnNew=qvNew");
        assertEqualsURI(URI_1 + "?qnNew=qvNew", uriBuilder1Enc);

        uriBuilder1Enc.replaceQueryParams(null);
        uriBuilder1Enc.queryParam("na$me", "George U.");
        assertEqualsURI(URI_1 + "?na%24me=George%20U.", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#queryParam(java.lang.String, java.lang.String)}.
     */
    public void testQueryNoE() throws Exception {
        uriBuilder1NoE.queryParam("qn", "qv");
        assertEqualsURI(URI_1 + "?qn=qv", uriBuilder1NoE);
        uriBuilder1NoE.queryParam("qn", "qv2");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2", uriBuilder1NoE);
        uriBuilder1NoE.queryParam("qn3", "qv3");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2&qn3=qv3", uriBuilder1NoE);
        uriBuilder1NoE.replaceQueryParams("qnNew=qvNew");
        assertEqualsURI(URI_1 + "?qnNew=qvNew", uriBuilder1NoE);

        uriBuilder1NoE.replaceQueryParams(null);
        try {
            uriBuilder1NoE.queryParam("na$me", "George U.");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEquals(URI_1, uriBuilder1NoE.build());
    }

    public void _testReplaceMatrixParamsEnc() throws Exception {
        fail("not yet impemented");
    }

    public void _testReplaceMatrixParamsNoE() throws Exception {
        fail("not yet impemented");
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#replacePath(java.lang.String)}.
     */
    public void _testReplacePathEnc() throws Exception {
        uriBuilder1Enc.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", uriBuilder1Enc);

        uriBuilder1Enc.replacePath((String[]) null);
        assertEqualUriBuilder(uriBuilder1Enc, "http", null, "localhost", -1,
                null);
        assertEqualsUriSlashAllowed("http://localhost", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r$t");
        assertEqualsURI("http://localhost/gh/r%24t", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r;t");
        assertEqualsURI("http://localhost/gh/r;t", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r;t=6");
        assertEqualsURI("http://localhost/gh/r;t=6", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r;t=");
        assertEqualsURI("http://localhost/gh/r;t=", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r;t=6;g");
        assertEqualsURI("http://localhost/gh/r;t=6;g", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#replacePath(java.lang.String)}.
     */
    public void _testReplacePathNoE() throws Exception {
        uriBuilder1NoE.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", uriBuilder1NoE);

        uriBuilder1NoE.replacePath((String[]) null);
        assertEqualUriBuilder(uriBuilder1NoE, "http", null, "localhost", -1,
                null);

        assertEqualsUriSlashAllowed("http://localhost", uriBuilder1NoE);

        try {
            uriBuilder1NoE.replacePath("gh", "r$t");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsUriSlashAllowed("http://localhost", uriBuilder1NoE);

        uriBuilder1Enc.replacePath("gh", "r;t");
        assertEqualsURI("http://localhost/gh/r;t", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r;t=");
        assertEqualsURI("http://localhost/gh/r;t=", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r;t=6");
        assertEqualsURI("http://localhost/gh/r;t=6", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r;t=6;g");
        assertEqualsURI("http://localhost/gh/r;t=6;g", uriBuilder1Enc);
    }

    public void _testReplaceQueryParamsEnc() throws Exception {
        fail("not yet impemented");
    }

    public void _testReplaceQueryParamsNoE() throws Exception {
        fail("not yet impemented");
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#scheme(java.lang.String)}.
     */
    public void testSchemeEnc() throws Exception {
        uriBuilder1Enc.scheme("ftp");
        assertEqualsURI("ftp://localhost/path1/path2", uriBuilder1Enc);
        uriBuilder1Enc.scheme("f4.-+tp");
        assertEqualsURI("f4.-+tp://localhost/path1/path2", uriBuilder1Enc);

        try {
            uriBuilder1Enc.scheme("44");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", uriBuilder1Enc);

        try {
            uriBuilder1Enc.scheme("f\0");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#scheme(java.lang.String)}.
     */
    public void testSchemeNoE() throws Exception {
        uriBuilder1NoE.scheme("ftp");
        assertEqualsURI("ftp://localhost/path1/path2", uriBuilder1NoE);
        uriBuilder1NoE.scheme("f4.-+tp");
        assertEqualsURI("f4.-+tp://localhost/path1/path2", uriBuilder1NoE);

        try {
            uriBuilder1NoE.scheme("44");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", uriBuilder1NoE);
        try {
            uriBuilder1NoE.scheme("f\0");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#schemeSpecificPart(java.lang.String)}.
     */
    public void testSchemeSpecificPartEnc() throws Exception {
        uriBuilder1Enc.schemeSpecificPart("//shkf");
        assertEqualUriBuilder(uriBuilder1Enc, "http", null, "shkf", -1, null);

        uriBuilder1Enc.schemeSpecificPart("//shkf/akfshdf");
        assertEqualUriBuilder(uriBuilder1Enc, "http", null, "shkf", -1, null,
                new JaxRsPathSegment("akfshdf", false, null));

        uriBuilder1Enc.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder(uriBuilder1Enc, "http", "user", "shkf", -1, null,
                new JaxRsPathSegment("akfshdf", false, null),
                new JaxRsPathSegment("akjhf", false, null));

        uriBuilder1Enc.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder(uriBuilder1Enc, "http", null, "shkf", 4711,
                "a=b", new JaxRsPathSegment("akjhf", false, null));

        uriBuilder1Enc.schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        MultivaluedMapImpl<String, String> mp = new MultivaluedMapImpl<String, String>();
        mp.putSingle("1", "2");
        assertEqualUriBuilder(uriBuilder1Enc, "http", null, "www.domain.org",
                -1, "a=b", new JaxRsPathSegment("akjhf", false, mp));

        uriBuilder1Enc.schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        mp = new MultivaluedMapImpl<String, String>();
        mp.putSingle("1", "2");
        mp.putSingle("3", "4");
        assertEqualUriBuilder(uriBuilder1Enc, "http", null, "www.domain.org",
                -1, "a=b", new JaxRsPathSegment("akjhf", false, mp));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#schemeSpecificPart(java.lang.String)}.
     */
    public void testSchemeSpecificPartNoE() throws Exception {
        uriBuilder1NoE.schemeSpecificPart("//shkf");
        assertEqualUriBuilder(uriBuilder1NoE, "http", null, "shkf", -1, null);

        uriBuilder1NoE.schemeSpecificPart("//shkf/akfshdf");
        assertEqualUriBuilder(uriBuilder1NoE, "http", null, "shkf", -1, null,
                new JaxRsPathSegment("akfshdf", false, null));

        uriBuilder1NoE.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder(uriBuilder1NoE, "http", "user", "shkf", -1, null,
                new JaxRsPathSegment("akfshdf", false, null),
                new JaxRsPathSegment("akjhf", false, null));

        uriBuilder1NoE.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder(uriBuilder1NoE, "http", null, "shkf", 4711,
                "a=b", new JaxRsPathSegment("akjhf", false, null));

        uriBuilder1NoE.schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        MultivaluedMapImpl<String, String> mp = new MultivaluedMapImpl<String, String>();
        mp.putSingle("1", "2");
        assertEqualUriBuilder(uriBuilder1NoE, "http", null, "www.domain.org",
                -1, "a=b", new JaxRsPathSegment("akjhf", false, mp));

        uriBuilder1NoE.schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        mp = new MultivaluedMapImpl<String, String>();
        mp.putSingle("1", "2");
        mp.putSingle("3", "4");
        assertEqualUriBuilder(uriBuilder1NoE, "http", null, "www.domain.org",
                -1, "a=b", new JaxRsPathSegment("akjhf", false, mp));
    }

    /**
     * 
     * @throws Exception
     */
    public void testStaticFromPath() throws Exception {
        UriBuilder uriBuilder = UriBuilder.fromPath("path");
        if (uriBuilder instanceof JaxRsUriBuilder) {
            assertEqualUriBuilder((JaxRsUriBuilder) uriBuilder, null, null,
                    null, -1, null, new JaxRsPathSegment("path", false, null));
        }
        assertEqualsURI("path", uriBuilder);

        uriBuilder = UriBuilder.fromPath("path1/path2/abc.html");
        if (uriBuilder instanceof JaxRsUriBuilder) {
            assertEqualUriBuilder(uriBuilder, null, null, null, -1, null,
                    new JaxRsPathSegment("path1", false, null),
                    new JaxRsPathSegment("path2", false, null),
                    new JaxRsPathSegment("abc.html", false, null));
        }
        assertEqualsURI("path1/path2/abc.html", uriBuilder);

        uriBuilder = UriBuilder.fromPath(
                "path1/path2;mp1=mv?1;mp2=mv2/abc.html", true);
        if (uriBuilder instanceof JaxRsUriBuilder) {
            MultivaluedMapImpl<String, String> map2 = new MultivaluedMapImpl<String, String>();
            map2.add("mp1", "mv%3F1");
            map2.add("mp2", "mv2");
            assertEqualUriBuilder(uriBuilder, null, null, null, -1, null,
                    new JaxRsPathSegment("path1", false, null),
                    new JaxRsPathSegment("path2", false, map2),
                    new JaxRsPathSegment("abc.html", false, null));
        }
        assertEquals("path1/path2;mp1=mv%3F1;mp2=mv2/abc.html", uriBuilder
                .build().toString());

        String path = "path1/path2;mp1=mv1" + Reference.encode("?")
                + ";mp2=mv2/abc.html";
        uriBuilder = UriBuilder.fromPath(path, false);
        if (uriBuilder instanceof JaxRsUriBuilder) {
            MultivaluedMapImpl<String, String> map2 = new MultivaluedMapImpl<String, String>();
            map2.add("mp1", "mv1%3F");
            map2.add("mp2", "mv2");
            assertEqualUriBuilder(uriBuilder, null, null, null, -1, null,
                    new JaxRsPathSegment("path1", false, null),
                    new JaxRsPathSegment("path2", false, map2),
                    new JaxRsPathSegment("abc.html", false, null));
        }
        assertEqualsURI(path, uriBuilder);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#toString()}.
     */
    public void testToString() {
        assertEquals("http://localhost/path1/path2", uriBuilder1Enc.toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#uri(java.net.URI)}.
     */
    public void testUri() throws Exception {
        // TODO test again Jersey Implementation
        URI u = new URI("ftp", "test.org", null, null, "fragment");
        uriBuilder1Enc.uri(u);
        assertEqualsURI("ftp://test.org#fragment", uriBuilder1Enc);

        u = new URI("ftp", "test.org", "/path", "qu=ery", "fragment");
        uriBuilder1Enc.uri(u);
        assertEqualsURI("ftp://test.org/path?qu=ery#fragment", uriBuilder1Enc);

        String id = "4711";
        URI collectionUri = new URI(
                "http://localhost:8181/SecurityContextTestService");
        URI location = UriBuilder.fromUri(collectionUri).path("{id}").build(id);
        assertEqualsURI(collectionUri + "/4711", location);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#userInfo(java.lang.String)}.
     */
    public void testUserInfoEnc() throws Exception {
        uriBuilder1Enc.userInfo("username");
        assertEqualsURI("http://username@localhost/path1/path2", uriBuilder1Enc);

        uriBuilder1Enc.replacePath((String) null);
        uriBuilder1Enc.host("abc");
        uriBuilder1Enc.userInfo("username:pw");
        assertEqualsUriSlashAllowed("http://username:pw@abc", uriBuilder1Enc);

        uriBuilder1Enc.userInfo("rkj;s78:&=+$,");
        assertEqualsUriSlashAllowed("http://rkj;s78:&=+$,@abc", uriBuilder1Enc);

        uriBuilder1Enc.userInfo(" ");
        assertEqualsUriSlashAllowed("http://%20@abc", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#userInfo(java.lang.String)}.
     */
    public void testUserInfoNoE() throws Exception {
        uriBuilder1NoE.userInfo("username");
        assertEqualsURI("http://username@localhost/path1/path2", uriBuilder1NoE);

        uriBuilder1NoE.host("abc");
        uriBuilder1NoE.replacePath((String) null);
        uriBuilder1NoE.userInfo("username:pw");
        assertEqualsUriSlashAllowed("http://username:pw@abc", uriBuilder1NoE);

        uriBuilder1NoE.userInfo("rkj;s78:&=+$,");
        assertEqualsUriSlashAllowed("http://rkj;s78:&=+$,@abc", uriBuilder1NoE);

        try {
            uriBuilder1NoE.userInfo(" ");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsUriSlashAllowed("http://rkj;s78:&=+$,@abc", uriBuilder1NoE);
    }
}