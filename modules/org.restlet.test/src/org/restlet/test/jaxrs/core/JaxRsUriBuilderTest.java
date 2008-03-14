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
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder;
import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.car.CarResource;
import org.restlet.test.jaxrs.services.resources.SimpleTrain;

/**
 * @author Stephan
 * 
 */
@SuppressWarnings("all")
public class JaxRsUriBuilderTest extends TestCase {

    /**
     * 
     */
    private static final String TEMPL_VARS_EXPECTED = "abc://username:password@www.secure.org:8080/def/ghi;jkl=mno/pqr;stu=vwx?ABC=DEF&GHI=JKL#MNO";

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
        assertEquals(expectedUri, actualUri.toString());
        assertEquals(new URI(expectedUri), actualUri);
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
     * @param expectedScheme
     * @param expectedUserInfo
     * @param expectedHost
     * @param expectedPort
     * @param expectedQuery
     * @param actualUriBuilder
     * @param expectedPathSegments
     */
    private static void assertEqualUriBuilder(String expectedScheme,
            String expectedUserInfo, String expectedHost, String expectedPort,
            String expectedPath, String expectedQuery,
            UriBuilder actualUriBuilder) throws Exception {
        if (actualUriBuilder instanceof JaxRsUriBuilder) {
            JaxRsUriBuilder jaxRsUriBuilder = (JaxRsUriBuilder) actualUriBuilder;
            assertEquals(expectedScheme, getScheme(jaxRsUriBuilder));
            assertEquals(expectedUserInfo, getUserInfo(jaxRsUriBuilder));
            assertEquals(expectedHost, getHost(jaxRsUriBuilder));
            assertEquals(expectedPort, getPort(jaxRsUriBuilder));
            String actPath = getPath(jaxRsUriBuilder);
            assertEquals(expectedPath, actPath);
            CharSequence actualQuery = getQuery(jaxRsUriBuilder);
            if (actualQuery != null)
                actualQuery = actualQuery.toString();
            assertEquals(expectedQuery, actualQuery);
        }
        JaxRsUriBuilder expectedUriBuilder = (JaxRsUriBuilder) RuntimeDelegate
                .getInstance().createUriBuilder();
        expectedUriBuilder.encode(false);
        if (expectedScheme != null)
            expectedUriBuilder.scheme(expectedScheme);
        if (expectedUserInfo != null)
            expectedUriBuilder.userInfo(expectedUserInfo);
        if (expectedHost != null)
            expectedUriBuilder.host(expectedHost);
        expectedUriBuilder.port(expectedPort);
        expectedUriBuilder.path(expectedPath);
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
    private static String getFieldValue(JaxRsUriBuilder jaxRsUriBuilder,
            String fieldName) throws Exception {
        Field queryField = jaxRsUriBuilder.getClass().getDeclaredField(
                fieldName);
        queryField.setAccessible(true);
        Object value = queryField.get(jaxRsUriBuilder);
        if (value == null)
            return null;
        return value.toString();
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getHost(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return getFieldValue(jaxRsUriBuilder, "host");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    @SuppressWarnings("unchecked")
    private static String getPath(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        Object path = getFieldValue(jaxRsUriBuilder, "path");
        if (path == null)
            return null;
        return path.toString();
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getPort(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return getFieldValue(jaxRsUriBuilder, "port");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getQuery(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return getFieldValue(jaxRsUriBuilder, "query");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getScheme(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return getFieldValue(jaxRsUriBuilder, "scheme");
    }

    /**
     * @param jaxRsUriBuilder
     * @return
     */
    private static String getUserInfo(JaxRsUriBuilder jaxRsUriBuilder)
            throws Exception {
        return getFieldValue(jaxRsUriBuilder, "userInfo");
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#build()}.
     */
    public void testBuild() throws Exception {
        assertEquals(URI_1, uriBuilder1Enc.build());
        assertEquals(URI_1, uriBuilder1NoE.build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#build(java.util.Map)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#build(java.lang.String[])}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#clone()}.
     */
    public void testClone() {
        assertEquals(uriBuilder1Enc.build(), uriBuilder1Enc.clone().build());
        URI clonedEncWithVars = uriBuilderWithVarsEnc.clone().build();
        assertEquals(uriBuilderWithVarsEnc.build(), clonedEncWithVars);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#encode(boolean)}.
     */
    public void testEncode() throws Exception {
        UriBuilder uriBuilder = RuntimeDelegate.getInstance()
                .createUriBuilder();
        uriBuilder.encode(false);
        uriBuilder.host("www.xyz.de");
        uriBuilder.scheme("http");
        uriBuilder.path("path1", "path2");
        try {
            uriBuilder.path("hh ho");
            fail("must fail, because of invalid character");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        uriBuilder.encode(true);
        uriBuilder.path("hh ho");
        assertEqualsURI("http://www.xyz.de/path1/path2/hh%20ho", uriBuilder);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#fragment(java.lang.String)}.
     */
    public void testFragmentEnc() throws Exception {
        uriBuilder1Enc.fragment(String.valueOf((char) 9));
        assertEqualsURI(URI_1 + "#%09", uriBuilder1Enc);

        uriBuilder1Enc.fragment("anker");
        assertEqualsURI(URI_1 + "#anker", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#fragment(java.lang.String)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#host(java.lang.String)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replaceMatrixParams(java.lang.String)}.
     * and
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#matrixParam(java.lang.String, java.lang.String)}.
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
        assertEquals(new URI(URI_1 + ";"), uriBuilder1Enc.build());

        uriBuilder1Enc.replaceMatrixParams(null);
        assertEquals(URI_1, uriBuilder1Enc.build());
        uriBuilder1Enc.matrixParam("jkj$sdf", "ij a%20");
        assertEqualsURI(URI_1 + ";jkj%24sdf=ij%20a%2520", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replaceMatrixParams(java.lang.String)}.
     * and
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#matrixParam(java.lang.String, java.lang.String)}.
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
        assertEquals(new URI(URI_1 + ";"), uriBuilder1NoE.build());

        uriBuilder1NoE.replaceMatrixParams(null);
        assertEquals(URI_1, uriBuilder1NoE.build());
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.Class)}.
     */
    public void testPathClass() throws Exception {
        uriBuilder1Enc.replacePath((String[]) null);
        uriBuilder1Enc.path(SimpleTrain.class);
        assertEqualsURI("http://localhost" + SimpleTrain.PATH, uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.Class, java.lang.String)}.
     */
    public void testPathClassString() throws Exception {
        uriBuilder1Enc.replacePath((String[]) null);
        uriBuilder1Enc.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.PATH + "/"
                + CarListResource.OFFERS_PATH, uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.Class, java.lang.String)}.
     */
    public void testPathClassStringEnc() throws Exception {
        uriBuilder1Enc.replacePath((String[]) null);
        uriBuilder1Enc.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.PATH + "/"
                + CarListResource.OFFERS_PATH, uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.reflect.Method[])}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.String[])}.
     */
    public void testPathStringArrayEnc() throws Exception {
        uriBuilder1Enc.path("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", uriBuilder1Enc);
        uriBuilder1Enc.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", uriBuilder1Enc);

        uriBuilder1Enc.path(" ");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno/%20", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.String[])}.
     */
    public void testPathStringArrayNoE() throws Exception {
        uriBuilder1NoE.path("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", uriBuilder1NoE);
        uriBuilder1NoE.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", uriBuilder1NoE);

        try {
            uriBuilder1NoE.path(" ");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#port(int)}.
     */
    public void testPort() throws Exception {
        uriBuilder1Enc.port(4711);
        assertEqualsURI("http://localhost:4711/path1/path2", uriBuilder1Enc);
        uriBuilder1Enc.port(-1);
        assertEqualsURI("http://localhost/path1/path2", uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#queryParam(java.lang.String, java.lang.String)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#queryParam(java.lang.String, java.lang.String)}.
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

    public void testReplaceMatrixParamsEnc() throws Exception {
        uriBuilder1Enc.matrixParam("a", "b");
        uriBuilder1Enc.matrixParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2;a=b;c=d", uriBuilder1Enc);

        uriBuilder1Enc.replaceMatrixParams("ksd hflk");
        assertEqualsURI("http://localhost/path1/path2;ksd%20hflk",
                uriBuilder1Enc);

        uriBuilder1Enc.replaceMatrixParams("e=f");
        assertEqualsURI("http://localhost/path1/path2;e=f", uriBuilder1Enc);
    }

    public void testReplaceMatrixParamsNoE() throws Exception {
        uriBuilder1NoE.matrixParam("a", "b");
        uriBuilder1NoE.matrixParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2;a=b;c=d", uriBuilder1NoE);

        try {
            uriBuilder1NoE.replaceMatrixParams("ksd hflk");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://localhost/path1/path2;a=b;c=d", uriBuilder1NoE);

        uriBuilder1NoE.replaceMatrixParams("e=f");
        assertEqualsURI("http://localhost/path1/path2;e=f", uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replacePath(java.lang.String)}.
     */
    public void testReplacePathEnc() throws Exception {
        uriBuilder1Enc.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", uriBuilder1Enc);

        uriBuilder1Enc.replacePath((String[]) null);
        assertEqualUriBuilder("http", null, "localhost", null, "", null,
                uriBuilder1Enc);
        assertEqualsUriSlashAllowed("http://localhost", uriBuilder1Enc);

        uriBuilder1Enc.replacePath("gh", "r t");
        assertEqualsURI("http://localhost/gh/r%20t", uriBuilder1Enc);

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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replacePath(java.lang.String)}.
     */
    public void testReplacePathNoE() throws Exception {
        uriBuilder1NoE.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", uriBuilder1NoE);

        uriBuilder1NoE.replacePath((String[]) null);
        assertEqualUriBuilder("http", null, "localhost", null, "", null,
                uriBuilder1NoE);

        assertEqualsUriSlashAllowed("http://localhost", uriBuilder1NoE);

        try {
            uriBuilder1NoE.replacePath("gh", "r t");
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

    public void testReplaceQueryParamsEnc() throws Exception {
        uriBuilder1Enc.queryParam("a", "b");
        uriBuilder1Enc.queryParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2?a=b&c=d", uriBuilder1Enc);

        uriBuilder1Enc.replaceQueryParams("ksd hflk");
        assertEqualsURI("http://localhost/path1/path2?ksd%20hflk",
                uriBuilder1Enc);

        uriBuilder1Enc.replaceQueryParams("e=f");
        assertEqualsURI("http://localhost/path1/path2?e=f", uriBuilder1Enc);
    }

    public void testReplaceQueryParamsNoE() throws Exception {
        uriBuilder1NoE.queryParam("a", "b");
        uriBuilder1NoE.queryParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2?a=b&c=d", uriBuilder1NoE);

        try {
            uriBuilder1NoE.replaceQueryParams("ksd hflk");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://localhost/path1/path2?a=b&c=d", uriBuilder1NoE);

        uriBuilder1NoE.replaceQueryParams("e=f");
        assertEqualsURI("http://localhost/path1/path2?e=f", uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#scheme(java.lang.String)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#scheme(java.lang.String)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#schemeSpecificPart(java.lang.String)}.
     */
    public void testSchemeSpecificPartEnc() throws Exception {
        uriBuilder1Enc.schemeSpecificPart("//shkf");
        assertEqualUriBuilder("http", null, "shkf", null, null, null,
                uriBuilder1Enc);

        uriBuilder1Enc.schemeSpecificPart("//shkf/akfshdf");
        assertEqualUriBuilder("http", null, "shkf", null, "/akfshdf", null,
                uriBuilder1Enc);

        uriBuilder1Enc.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder("http", "user", "shkf", null, "/akfshdf/akjhf",
                null, uriBuilder1Enc);

        uriBuilder1Enc.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder("http", null, "shkf", "4711", "/akjhf", "a=b",
                uriBuilder1Enc);

        uriBuilder1Enc.schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2", "a=b", uriBuilder1Enc);

        uriBuilder1Enc.schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2;3=4", "a=b", uriBuilder1Enc);

        uriBuilder1Enc.schemeSpecificPart("//www.domain.org/ ");
        assertEqualUriBuilder("http", null, "www.domain.org", null, "/%20",
                null, uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#schemeSpecificPart(java.lang.String)}.
     */
    public void testSchemeSpecificPartNoE() throws Exception {
        uriBuilder1NoE.schemeSpecificPart("//shkf");
        assertEqualUriBuilder("http", null, "shkf", null, null, null,
                uriBuilder1NoE);

        uriBuilder1NoE.schemeSpecificPart("//shkf/akfshdf");
        assertEqualUriBuilder("http", null, "shkf", null, "/akfshdf", null,
                uriBuilder1NoE);

        uriBuilder1NoE.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder("http", "user", "shkf", null, "/akfshdf/akjhf",
                null, uriBuilder1NoE);

        uriBuilder1NoE.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder("http", null, "shkf", "4711", "/akjhf", "a=b",
                uriBuilder1NoE);

        uriBuilder1NoE.schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2", "a=b", uriBuilder1NoE);

        uriBuilder1NoE.schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2;3=4", "a=b", uriBuilder1NoE);

        try {
            uriBuilder1NoE.schemeSpecificPart("//www.domain.org/ ");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2;3=4", "a=b", uriBuilder1NoE);
    }

    /**
     * 
     * @throws Exception
     */
    public void testStaticFromPath() throws Exception {
        UriBuilder uriBuilder = UriBuilder.fromPath("path");
        if (uriBuilder instanceof JaxRsUriBuilder) {
            assertEqualUriBuilder(null, null, null, null, "path", null,
                    uriBuilder);
        }
        assertEqualsURI("path", uriBuilder);

        uriBuilder = UriBuilder.fromPath("path1/path2/abc.html");
        if (uriBuilder instanceof JaxRsUriBuilder) {
            assertEqualUriBuilder(null, null, null, null,
                    "path1/path2/abc.html", null, uriBuilder);
        }
        assertEqualsURI("path1/path2/abc.html", uriBuilder);

        uriBuilder = UriBuilder.fromPath(
                "path1/path2;mp1=mv 1;mp2=mv2/abc.html", true);
        if (uriBuilder instanceof JaxRsUriBuilder) {
            assertEqualUriBuilder(null, null, null, null,
                    "path1/path2;mp1=mv%201;mp2=mv2/abc.html", null, uriBuilder);
        }
        assertEquals("path1/path2;mp1=mv%201;mp2=mv2/abc.html", uriBuilder
                .build().toString());

        String path = "path1/path2;mp1=mv1" + Reference.encode("?")
                + ";mp2=mv2/abc.html";
        uriBuilder = UriBuilder.fromPath(path, false);
        if (uriBuilder instanceof JaxRsUriBuilder) {
            assertEqualUriBuilder(null, null, null, null,
                    "path1/path2;mp1=mv1%3F;mp2=mv2/abc.html", null, uriBuilder);
        }
        assertEqualsURI(path, uriBuilder);
    }

    public void testTemplateParamsEnc() throws Exception {
        changeWithTemplVars((JaxRsUriBuilder) uriBuilderWithVarsEnc);

        URI uri = buildFromTemplVarsWithMap(uriBuilderWithVarsEnc);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);

        uri = buildFromTemplVarsWithStrings(uriBuilderWithVarsEnc);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);
    }

    public void testTemplateParamsNoE() throws Exception {
        changeWithTemplVars((JaxRsUriBuilder) uriBuilderWithVarsNoE);

        URI uri = buildFromTemplVarsWithMap(uriBuilderWithVarsNoE);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);

        uri = buildFromTemplVarsWithStrings(uriBuilderWithVarsNoE);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);
    }

    private URI buildFromTemplVarsWithMap(UriBuilder uriBuilder) {
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("scheme", "abc");
        vars.put("userInfo", "username:password");
        vars.put("host", "www.secure.org");
        vars.put("port", "8080");
        vars.put("path1", "def");
        vars.put("path2", "ghi");
        vars.put("mp2Name", "jkl");
        vars.put("mp2Value", "mno");
        vars.put("path3", "pqr");
        vars.put("mp3Name", "stu");
        vars.put("mp3Value", "vwx");
        vars.put("qp1Name", "ABC");
        vars.put("qp1Value", "DEF");
        vars.put("qp2Name", "GHI");
        vars.put("qp2Value", "JKL");
        vars.put("fragment", "MNO");
        return uriBuilder.build(vars);
    }

    private URI buildFromTemplVarsWithStrings(UriBuilder uriBuilder) {
        return uriBuilder.build("abc", "username:password", "www.secure.org",
                "8080", "def", "ghi", "jkl", "mno", "pqr", "stu", "vwx", "ABC",
                "DEF", "GHI", "JKL", "MNO");
    }

    /**
     * @throws IllegalArgumentException
     */
    private void changeWithTemplVars(JaxRsUriBuilder uriBuilder) {
        uriBuilder.scheme("{scheme}");
        uriBuilder.userInfo("{userInfo}");
        uriBuilder.host("{host}");
        uriBuilder.port("{port}"); // REQUESTED uriBuilder.port(String) missing
        uriBuilder.replacePath("{path1}");
        uriBuilder.path("{path2}");
        uriBuilder.replaceMatrixParams("{mp2Name}={mp2Value}");
        uriBuilder.path("{path3}");
        uriBuilder.matrixParam("{mp3Name}", "{mp3Value}");
        uriBuilder.replaceQueryParams("{qp1Name}={qp1Value}");
        uriBuilder.queryParam("{qp2Name}", "{qp2Value}");
        uriBuilder.fragment("{fragment}");
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#toString()}.
     */
    public void testToString() {
        assertEquals("http://localhost/path1/path2", uriBuilder1Enc.toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#uri(java.net.URI)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#userInfo(java.lang.String)}.
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
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#userInfo(java.lang.String)}.
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