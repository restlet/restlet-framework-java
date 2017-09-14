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

package org.restlet.test.ext.jaxrs.core;

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
import org.restlet.ext.jaxrs.ExtendedUriBuilder;
import org.restlet.ext.jaxrs.internal.core.UriBuilderImpl;
import org.restlet.test.ext.jaxrs.services.car.CarListResource;
import org.restlet.test.ext.jaxrs.services.car.CarResource;
import org.restlet.test.ext.jaxrs.services.resources.SimpleTrain;

/**
 * @author Stephan Koops
 * @see UriBuilder
 * @see UriBuilderImpl
 * @see ExtendedUriBuilder
 */
@SuppressWarnings("all")
public class UriBuilderImplTest extends TestCase {

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
        assertEquals(new URI(expectedUri).toString(), actualUri.toString());
    }

    static void assertEqualsURI(String expectedUri, UriBuilder actualBuilder)
            throws Exception {
        assertEqualsURI(expectedUri, actualBuilder, true);
    }

    static void assertEqualsURI(String expectedUri, UriBuilder actualBuilder,
            boolean encode) throws Exception {
        URI actual;
        if (encode)
            actual = actualBuilder.buildFromEncoded();
        else
            actual = actualBuilder.build();
        assertEqualsURI(expectedUri, actual);
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
     * @param compareResult
     *            if true, also the builded URIs are compared (this causes
     *            trouble sometimes, e.g. with "/").
     * @param expectedPathSegments
     */
    private static void assertEqualUriBuilder(String expectedScheme,
            String expectedUserInfo, String expectedHost, String expectedPort,
            String expectedPath, String expectedQuery,
            UriBuilder actualUriBuilder, boolean compareResult)
            throws Exception {
        if (actualUriBuilder.getClass().getPackage().getName()
                .startsWith("org.restlet.ext.jaxrs")) {
            assertEquals(expectedScheme, getScheme(actualUriBuilder));
            assertEquals(expectedUserInfo, getUserInfo(actualUriBuilder));
            assertEquals(expectedHost, getHost(actualUriBuilder));
            assertEquals(expectedPort, getPort(actualUriBuilder));
            final String actPath = getPath(actualUriBuilder);
            try {
                assertEquals(expectedPath, actPath);
            } catch (junit.framework.ComparisonFailure cf) {
                if (expectedPath == null)
                    assertEquals("", actPath);
            }
            CharSequence actualQuery = getQuery(actualUriBuilder);
            if (actualQuery != null) {
                actualQuery = actualQuery.toString();
            }
            assertEquals(expectedQuery, actualQuery);
        }
        if (compareResult) {
            UriBuilder expectedUriBuilder = RuntimeDelegate.getInstance()
                    .createUriBuilder();
            if (expectedScheme != null) {
                expectedUriBuilder.scheme(expectedScheme);
            }
            if (expectedUserInfo != null) {
                expectedUriBuilder.userInfo(expectedUserInfo);
            }
            if (expectedHost != null) {
                expectedUriBuilder.host(expectedHost);
            }
            if (expectedUriBuilder instanceof UriBuilderImpl) {
                ((UriBuilderImpl) expectedUriBuilder).port(expectedPort);
            } else if (expectedUriBuilder instanceof ExtendedUriBuilder) {
                ((ExtendedUriBuilder) expectedUriBuilder).port(expectedPort);
            } else {
                if (expectedPort == null || expectedPort.equals("")) {
                    expectedUriBuilder.port(-1);
                } else {
                    try {
                        int portInt = Integer.valueOf(expectedPort);
                    } catch (NumberFormatException e) {
                        System.out
                                .println("Sorry, could not do this test with an expected port \""
                                        + expectedPort
                                        + "\" and an UriBuilder of type "
                                        + expectedUriBuilder.getClass());
                    }
                }
            }
            expectedUriBuilder.path(expectedPath);
            if (expectedQuery != null) {
                expectedUriBuilder.replaceQuery(expectedQuery);
            }
            String expectedURI = expectedUriBuilder.build().toString();
            String atualURI = actualUriBuilder.build().toString();
            assertEquals(expectedURI, atualURI);
        }
    }

    /**
     * @param uriBuilder
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    static String getFieldValue(UriBuilder uriBuilder, String fieldName)
            throws Exception {
        Field queryField;
        try {
            queryField = uriBuilder.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            queryField = uriBuilder.getClass().getSuperclass()
                    .getDeclaredField(fieldName);
        }
        queryField.setAccessible(true);
        final Object value = queryField.get(uriBuilder);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * @param uriBuilderImpl
     * @return
     */
    private static String getHost(UriBuilder uriBuilder) throws Exception {
        return getFieldValue(uriBuilder, "host");
    }

    /**
     * @param uriBuilderImpl
     * @return
     */
    @SuppressWarnings("unchecked")
    private static String getPath(UriBuilder uriBuilder) throws Exception {
        final Object path = getFieldValue(uriBuilder, "path");
        if (path == null) {
            return null;
        }
        return path.toString();
    }

    /**
     * @param uriBuilderImpl
     * @return
     */
    private static String getPort(UriBuilder uriBuilder) throws Exception {
        return getFieldValue(uriBuilder, "port");
    }

    /**
     * @param uriBuilderImpl
     * @return
     */
    private static String getQuery(UriBuilder uriBuilder) throws Exception {
        return getFieldValue(uriBuilder, "query");
    }

    /**
     * @param uriBuilderImpl
     * @return
     */
    private static String getScheme(UriBuilder uriBuilder) throws Exception {
        return getFieldValue(uriBuilder, "scheme");
    }

    /**
     * @param uriBuilderImpl
     * @return
     */
    private static String getUserInfo(UriBuilder uriBuilder) throws Exception {
        return getFieldValue(uriBuilder, "userInfo");
    }

    public static void main(String[] args) {
        System.out.println(Reference.encode("%"));
    }

    private UriBuilder uriBuilder;

    /**
     * UriBuilder with variableNames.
     */
    private UriBuilder uriBuilderWithVars;

    private URI buildFromTemplVarsWithMap(UriBuilder uriBuilder) {
        final Map<String, Object> vars = new HashMap<String, Object>();
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
        return uriBuilder.buildFromMap(vars);
    }

    private URI buildFromTemplVarsWithStrings(UriBuilder uriBuilder) {
        return uriBuilder.build("abc", "username:password", "www.secure.org",
                "8080", "def", "ghi", "jkl", "mno", "pqr", "stu", "vwx", "ABC",
                "DEF", "GHI", "JKL", "MNO");
    }

    /**
     * @throws IllegalArgumentException
     */
    private void changeWithTemplVars(UriBuilderImpl uriBuilder) {
        uriBuilder.scheme("{scheme}");
        uriBuilder.userInfo("{userInfo}");
        uriBuilder.host("{host}");
        uriBuilder.port("{port}");
        uriBuilder.replacePath("{path1}");
        uriBuilder.path("{path2}");
        uriBuilder.replaceMatrix("{mp2Name}={mp2Value}");
        uriBuilder.path("{path3}");
        uriBuilder.matrixParam("{mp3Name}", "{mp3Value}");
        uriBuilder.replaceQuery("{qp1Name}={qp1Value}");
        uriBuilder.queryParam("{qp2Name}", "{qp2Value}");
        uriBuilder.fragment("{fragment}");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.uriBuilder = RuntimeDelegate.getInstance().createUriBuilder();
        this.uriBuilder.host("localhost");
        this.uriBuilder.segment("path1", "path2");
        this.uriBuilder.scheme("http");
        this.uriBuilderWithVars = RuntimeDelegate.getInstance()
                .createUriBuilder();
        this.uriBuilderWithVars.host("localhost");
        this.uriBuilderWithVars.scheme("http");
        this.uriBuilderWithVars.segment("abc", "{var1}", "def", "{var2}");
    }

    @Override
    protected void tearDown() throws Exception {
        this.uriBuilder = null;
        this.uriBuilderWithVars = null;
        super.tearDown();
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#build(java.lang.String[])}
     * .
     */
    public void testBuildFromArray() throws Exception {
        try {
            this.uriBuilderWithVars.build("123");
            fail("must fail, because there are not enough arguments");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        URI uri = this.uriBuilderWithVars.build("123", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uri);
        final UriBuilder uriBuilder2 = this.uriBuilderWithVars.clone();
        assertEqualsURI("http://localhost/abc/123/def/456",
                uriBuilder2.build("123", "456"));
        assertEquals(this.uriBuilderWithVars.toString(), uriBuilder2.toString());
        uriBuilder2.path("{var3}");
        uri = this.uriBuilderWithVars.build("123", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uri);
        try {
            uriBuilder2.build("123", "456");
            fail("must fail, because there are not enough arguments");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        final URI uri2 = uriBuilder2.build("123", "456", "789");
        assertEqualsURI("http://localhost/abc/123/def/456/789", uri2);
    }

    public void testBuildFromEncoded() throws Exception {
        assertEquals(URI_1, this.uriBuilder.buildFromEncoded("a", "b"));
        assertEquals(URI_1, this.uriBuilder.buildFromEncoded(" ", "b"));

        URI uri = this.uriBuilderWithVars.buildFromEncoded("a", "b");
        assertEqualsURI("http://localhost/abc/a/def/b", uri);

        uri = this.uriBuilderWithVars.buildFromEncoded("%20", "b");
        assertEqualsURI("http://localhost/abc/%20/def/b", uri);

        try {
            uri = this.uriBuilderWithVars.buildFromEncoded(" ", "b");
            fail("must fail");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#build(java.util.Map)} .
     */
    public void testBuildFromMap() throws Exception {
        final Map<String, Object> vars = new HashMap<String, Object>();
        try {
            this.uriBuilderWithVars.buildFromMap(vars);
            fail("must fail, because missing UriTemplate variables");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var1", "123");
        try {
            this.uriBuilderWithVars.buildFromMap(vars);
            fail("must fail, because missing UriTemplate variable");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var2", "456");
        assertEqualsURI("http://localhost/abc/123/def/456",
                this.uriBuilderWithVars.buildFromMap(vars));
        vars.put("var3", "789");
        assertEqualsURI("http://localhost/abc/123/def/456",
                this.uriBuilderWithVars.buildFromMap(vars));

        vars.put("var2", " ");
        assertEqualsURI("http://localhost/abc/123/def/%20",
                this.uriBuilderWithVars.buildFromMap(vars));
    }

    public void testBuildWithArgs() throws Exception {
        assertEquals(URI_1, this.uriBuilder.build("a", "b"));
        assertEquals(URI_1, this.uriBuilder.build(" ", "b"));

        URI uri = this.uriBuilderWithVars.build("a", "b");
        assertEqualsURI("http://localhost/abc/a/def/b", uri);

        uri = this.uriBuilderWithVars.build("%20", "b");
        assertEqualsURI("http://localhost/abc/%2520/def/b", uri);

        uri = this.uriBuilderWithVars.build(" ", "b");
        assertEqualsURI("http://localhost/abc/%20/def/b", uri);
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#build()}
     * .
     */
    public void testBuildWithoutArgs() throws Exception {
        assertEquals(URI_1, this.uriBuilder.build());

        try {
            this.uriBuilderWithVars.build();
            fail("must fail, because vars are required");
        } catch (IllegalArgumentException ube) {
            // wonderful
        }
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#clone()}
     * .
     */
    public void testClone() {
        assertEquals(this.uriBuilder.build(), this.uriBuilder.clone().build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#encode(boolean)} .
     */
    public void testEncode() throws Exception {
        final UriBuilder uriBuilder = RuntimeDelegate.getInstance()
                .createUriBuilder();
        uriBuilder.host("www.xyz.de");
        uriBuilder.scheme("http");
        uriBuilder.segment("path1", "path2");
        uriBuilder.path("hh ho");
        assertEqualsURI("http://www.xyz.de/path1/path2/hh%20ho", uriBuilder,
                true);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#fragment(java.lang.String)}
     * .
     */
    public void testFragmentEnc() throws Exception {
        this.uriBuilder.fragment(String.valueOf((char) 9));
        assertEqualsURI(URI_1 + "#%09", this.uriBuilder, true);

        this.uriBuilder.fragment("anker");
        assertEqualsURI(URI_1 + "#anker", this.uriBuilder, true);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#host(java.lang.String)}
     * .
     */
    public void testHostEnc() throws Exception {
        this.uriBuilder.host("test.domain.org");
        assertEqualsURI("http://test.domain.org/path1/path2", this.uriBuilder,
                true);

        try {
            this.uriBuilder.host("test.domain .org a");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://test.domain.org/path1/path2", this.uriBuilder,
                true);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#replaceMatrixParams(java.lang.String)}
     * . and
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#matrixParam(java.lang.String, java.lang.String)}
     * .
     */
    public void testMatrixParam() throws Exception {
        this.uriBuilder.matrixParam("mp1", "mv1");
        assertEqualsURI(URI_1 + ";mp1=mv1", this.uriBuilder, true);
        this.uriBuilder.matrixParam("mp1", "mv2");
        assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2", this.uriBuilder, true);
        this.uriBuilder.matrixParam("mp3", "mv3");
        try {
            assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2;mp3=mv3",
                    this.uriBuilder, true);
        } catch (AssertionFailedError afe) { // try another possibility
            assertEqualsURI(URI_1 + ";mp3=mv3;mp1=mv1;mp1=mv2",
                    this.uriBuilder, true);
        }
        this.uriBuilder.replaceMatrix("mp4=mv4");
        assertEqualsURI(URI_1 + ";mp4=mv4", this.uriBuilder, true);
        this.uriBuilder.replaceMatrix("");
        assertEquals(new URI(URI_1 + ";"), this.uriBuilder.build());

        this.uriBuilder.replaceMatrix(null);
        assertEquals(URI_1, this.uriBuilder.build());
        this.uriBuilder.matrixParam("jkj$sdf", "ij a%20");
        assertEqualsURI(URI_1 + ";jkj%24sdf=ij%20a%2520", this.uriBuilder, true);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#path(java.lang.Class)} .
     */
    public void testPathClass() throws Exception {
        this.uriBuilder.replacePath(null);
        this.uriBuilder.path(SimpleTrain.class);
        assertEqualsURI("http://localhost" + SimpleTrain.PATH, this.uriBuilder,
                true);
    }

    /**
     * Test method for {@link AbstractUriBuilder#path(Class, String).
     */
    public void testPathClassString() throws Exception {
        this.uriBuilder.replacePath(null);
        this.uriBuilder.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.OFFERS_PATH,
                this.uriBuilder, true);
    }

    /**
     * Test method for {@link AbstractUriBuilder#path(Class, String).
     */
    public void testPathClassStringEnc() throws Exception {
        this.uriBuilder.replacePath(null);
        this.uriBuilder.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.OFFERS_PATH,
                this.uriBuilder, true);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#path(java.lang.reflect.Method[])}
     * .
     */
    public void testPathMethodArray() throws Exception {
        this.uriBuilder.replacePath(null);
        final Method findCar = CarListResource.class.getMethod("findCar",
                Integer.TYPE);
        final Method engine = CarResource.class.getMethod("findEngine");
        this.uriBuilder.path(CarListResource.class);
        this.uriBuilder.path(findCar).path(engine);
        assertEqualsURI("http://localhost/" + CarListResource.PATH
                + "/5/engine", this.uriBuilder.build("5"));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#path(java.lang.String[])}
     * .
     */
    public void testPathStringArrayEnc() throws Exception {
        this.uriBuilder.segment("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", this.uriBuilder, true);
        this.uriBuilder.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", this.uriBuilder, true);

        this.uriBuilder.path(" ");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno/%20", this.uriBuilder, true);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#port(int)}.
     */
    public void testPort() throws Exception {
        this.uriBuilder.port(4711);
        assertEqualsURI("http://localhost:4711/path1/path2", this.uriBuilder,
                true);
        this.uriBuilder.port(-1);
        assertEqualsURI("http://localhost/path1/path2", this.uriBuilder, true);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#queryParam(java.lang.String, java.lang.String)}
     * .
     */
    public void testQueryEnc() throws Exception {
        this.uriBuilder.queryParam("qn", "qv");
        assertEqualsURI(URI_1 + "?qn=qv", this.uriBuilder, true);
        this.uriBuilder.queryParam("qn", "qv2");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2", this.uriBuilder, true);
        this.uriBuilder.queryParam("qn3", "qv3");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2&qn3=qv3", this.uriBuilder, true);
        this.uriBuilder.replaceQuery("qnNew=qvNew");
        assertEqualsURI(URI_1 + "?qnNew=qvNew", this.uriBuilder, true);

        this.uriBuilder.replaceQuery(null);
        this.uriBuilder.queryParam("na$me", "George U.");
        assertEqualsURI(URI_1 + "?na%24me=George%20U.", this.uriBuilder, true);
    }

    public void testreplaceMatrix() throws Exception {
        this.uriBuilder.matrixParam("a", "b");
        this.uriBuilder.matrixParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2;a=b;c=d", this.uriBuilder);

        this.uriBuilder.replaceMatrix("ksd hflk");
        assertEqualsURI("http://localhost/path1/path2;ksd%20hflk",
                this.uriBuilder);

        this.uriBuilder.replaceMatrix("e=f");
        assertEqualsURI("http://localhost/path1/path2;e=f", this.uriBuilder);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#replacePath(java.lang.String)}
     * .
     */
    public void testReplacePath() throws Exception {
        this.uriBuilder.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", this.uriBuilder);

        this.uriBuilder.replacePath("");
        assertEqualUriBuilder("http", null, "localhost", null, "", null,
                this.uriBuilder, true);
        assertEqualsUriSlashAllowed("http://localhost", this.uriBuilder);

        this.uriBuilder.replacePath("gh").path("r t");
        assertEqualsURI("http://localhost/gh/r%20t", this.uriBuilder);

        this.uriBuilder.replacePath("gh").path("r;t");
        assertEqualsURI("http://localhost/gh/r;t", this.uriBuilder);

        this.uriBuilder.replacePath("gh").path("r;t=6");
        assertEqualsURI("http://localhost/gh/r;t=6", this.uriBuilder);

        this.uriBuilder.replacePath("gh").path("r;t=");
        assertEqualsURI("http://localhost/gh/r;t=", this.uriBuilder);

        this.uriBuilder.replacePath("gh").path("r;t=6;g");
        assertEqualsURI("http://localhost/gh/r;t=6;g", this.uriBuilder);
    }

    public void testReplaceQueryParams() throws Exception {
        this.uriBuilder.queryParam("a", "b");
        this.uriBuilder.queryParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2?a=b&c=d", this.uriBuilder);

        this.uriBuilder.replaceQuery("ksd hflk");
        assertEqualsURI("http://localhost/path1/path2?ksd+hflk",
                this.uriBuilder);

        this.uriBuilder.replaceQuery("e=f");
        assertEqualsURI("http://localhost/path1/path2?e=f", this.uriBuilder);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#scheme(java.lang.String)}
     * .
     */
    public void testScheme() throws Exception {
        this.uriBuilder.scheme("ftp");
        assertEqualsURI("ftp://localhost/path1/path2", this.uriBuilder);
        this.uriBuilder.scheme("f4.-+tp");
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder);

        try {
            this.uriBuilder.scheme("44");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder);

        try {
            this.uriBuilder.scheme("f\0");
            fail();
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#schemeSpecificPart(java.lang.String)}
     * .
     */
    public void testSchemeSpecificPart() throws Exception {
        this.uriBuilder.schemeSpecificPart("//shkf");
        this.uriBuilder.replacePath("");
        assertEqualUriBuilder("http", null, "shkf", null, "", null,
                this.uriBuilder, true);

        this.uriBuilder.schemeSpecificPart("//shkf-host/akfshdf");
        assertEqualUriBuilder("http", null, "shkf-host", null, "/akfshdf",
                null, this.uriBuilder, true);

        this.uriBuilder.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder("http", "user", "shkf", null, "/akfshdf/akjhf",
                null, this.uriBuilder, true);

        this.uriBuilder.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder("http", null, "shkf", "4711", "/akjhf", "a=b",
                this.uriBuilder, true);

        this.uriBuilder.schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2", "a=b", this.uriBuilder, true);

        this.uriBuilder
                .schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2;3=4", "a=b", this.uriBuilder, true);

        this.uriBuilder.schemeSpecificPart("//www.domain.org/ ");
        assertEquals("http://www.domain.org/%20", this.uriBuilder.build()
                .toString());
    }

    /**
     * 
     * @throws Exception
     */
    public void testStaticFromPath() throws Exception {
        UriBuilder uriBuilder = UriBuilder.fromPath("path");
        if (uriBuilder instanceof UriBuilderImpl) {
            assertEqualUriBuilder(null, null, null, null, "path", null,
                    uriBuilder, true);
        }
        assertEqualsURI("path", uriBuilder);

        uriBuilder = UriBuilder.fromPath("path1/path2/abc.html");
        if (uriBuilder instanceof UriBuilderImpl) {
            assertEqualUriBuilder(null, null, null, null,
                    "path1/path2/abc.html", null, uriBuilder, true);
        }
        assertEqualsURI("path1/path2/abc.html", uriBuilder);

        uriBuilder = UriBuilder
                .fromPath("path1/path2;mp1=mv 1;mp2=mv2/abc.html");
        if (uriBuilder instanceof UriBuilderImpl) {
            assertEqualUriBuilder(null, null, null, null,
                    "path1/path2;mp1=mv%201;mp2=mv2/abc.html", null,
                    uriBuilder, false);
        }
        assertEquals("path1/path2;mp1=mv%201;mp2=mv2/abc.html", uriBuilder
                .build().toString());

        final String path = "path1/path2;mp1=mv1" + Reference.encode("?")
                + ";mp2=mv2/abc.html";
        uriBuilder = UriBuilder.fromPath(path);
        if (uriBuilder instanceof UriBuilderImpl) {
            assertEqualUriBuilder(null, null, null, null,
                    "path1/path2;mp1=mv1%3F;mp2=mv2/abc.html", null,
                    uriBuilder, false);
        }
    }

    public void testTemplateParams() throws Exception {
        changeWithTemplVars((UriBuilderImpl) this.uriBuilderWithVars);

        URI uri = buildFromTemplVarsWithMap(this.uriBuilderWithVars);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);

        uri = buildFromTemplVarsWithStrings(this.uriBuilderWithVars);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#toString()}.
     */
    public void testToString() {
        assertEquals("http://localhost/path1/path2", this.uriBuilder.toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#uri(java.net.URI)} .
     */
    public void testUri() throws Exception {
        URI u = new URI("ftp", "test.org", null, null, "fragment");
        this.uriBuilder.uri(u);
        assertEqualsURI("ftp://test.org/#fragment", this.uriBuilder);

        u = new URI("ftp", "test.org", "/path", "qu=ery", "fragment");
        this.uriBuilder.uri(u);
        assertEqualsURI("ftp://test.org/path?qu=ery#fragment", this.uriBuilder);

        final String id = "4711";
        final URI collectionUri = new URI(
                "http://localhost:8181/SecurityContextTestService");
        final URI location = UriBuilder.fromUri(collectionUri).path("{id}")
                .build(id);
        assertEqualsURI(collectionUri + "/4711", location);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.impl.UriBuilderImpl#userInfo(java.lang.String)}
     * .
     */
    public void testUserInfo() throws Exception {
        this.uriBuilder.userInfo("username");
        assertEqualsURI("http://username@localhost/path1/path2",
                this.uriBuilder);

        this.uriBuilder.replacePath((String) null);
        this.uriBuilder.host("abc");
        this.uriBuilder.userInfo("username:pw");
        assertEqualsUriSlashAllowed("http://username:pw@abc", this.uriBuilder);

        this.uriBuilder.userInfo("rkj;s78:&=+$,");
        assertEqualsUriSlashAllowed("http://rkj;s78:&=+$,@abc", this.uriBuilder);

        this.uriBuilder.userInfo(" ");
        assertEqualsUriSlashAllowed("http://%20@abc", this.uriBuilder);
    }
}
