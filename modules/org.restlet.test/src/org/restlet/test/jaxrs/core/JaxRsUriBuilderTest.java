/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder;
import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.car.CarResource;
import org.restlet.test.jaxrs.services.resources.SimpleTrain;

/**
 * @author Stephan Koops
 * @see JaxRsUriBuilder
 * @see UriBuilder
 */
@SuppressWarnings("all")
public class JaxRsUriBuilderTest extends TestCase {

    private static final String TEMPL_VARS_EXPECTED = "abc://username:password@www.secure.org:8080/def/ghi;jkl=mno/pqr;stu=vwx?ABC=DEF&GHI=JKL#MNO";

    private static final URI URI_1;
    static {
        try {
            URI_1 = new URI("http://localhost/path1/path2");
        } catch (final URISyntaxException e) {
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
        } catch (final AssertionFailedError ife) {
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
            final JaxRsUriBuilder jaxRsUriBuilder = (JaxRsUriBuilder) actualUriBuilder;
            assertEquals(expectedScheme, getScheme(jaxRsUriBuilder));
            assertEquals(expectedUserInfo, getUserInfo(jaxRsUriBuilder));
            assertEquals(expectedHost, getHost(jaxRsUriBuilder));
            assertEquals(expectedPort, getPort(jaxRsUriBuilder));
            final String actPath = getPath(jaxRsUriBuilder);
            assertEquals(expectedPath, actPath);
            CharSequence actualQuery = getQuery(jaxRsUriBuilder);
            if (actualQuery != null) {
                actualQuery = actualQuery.toString();
            }
            assertEquals(expectedQuery, actualQuery);
        }
        final JaxRsUriBuilder expectedUriBuilder = (JaxRsUriBuilder) RuntimeDelegate
                .getInstance().createUriBuilder();
        expectedUriBuilder.encode(false);
        if (expectedScheme != null) {
            expectedUriBuilder.scheme(expectedScheme);
        }
        if (expectedUserInfo != null) {
            expectedUriBuilder.userInfo(expectedUserInfo);
        }
        if (expectedHost != null) {
            expectedUriBuilder.host(expectedHost);
        }
        expectedUriBuilder.port(expectedPort);
        expectedUriBuilder.path(expectedPath);
        if (expectedQuery != null) {
            expectedUriBuilder.replaceQueryParams(expectedQuery);
        }
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
        final Field queryField = jaxRsUriBuilder.getClass().getDeclaredField(
                fieldName);
        queryField.setAccessible(true);
        final Object value = queryField.get(jaxRsUriBuilder);
        if (value == null) {
            return null;
        }
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
        final Object path = getFieldValue(jaxRsUriBuilder, "path");
        if (path == null) {
            return null;
        }
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
        uriBuilder.port("{port}");
        uriBuilder.replacePath("{path1}");
        uriBuilder.path("{path2}");
        uriBuilder.replaceMatrixParams("{mp2Name}={mp2Value}");
        uriBuilder.path("{path3}");
        uriBuilder.matrixParam("{mp3Name}", "{mp3Value}");
        uriBuilder.replaceQueryParams("{qp1Name}={qp1Value}");
        uriBuilder.queryParam("{qp2Name}", "{qp2Value}");
        uriBuilder.fragment("{fragment}");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.uriBuilder1Enc = RuntimeDelegate.getInstance().createUriBuilder();
        this.uriBuilder1Enc.host("localhost");
        this.uriBuilder1Enc.path("path1", "path2");
        this.uriBuilder1Enc.scheme("http");
        this.uriBuilder1NoE = this.uriBuilder1Enc.clone();
        this.uriBuilder1NoE.encode(false);
        this.uriBuilderWithVarsEnc = RuntimeDelegate.getInstance()
                .createUriBuilder();
        this.uriBuilderWithVarsEnc.host("localhost");
        this.uriBuilderWithVarsEnc.scheme("http");
        this.uriBuilderWithVarsEnc.path("abc", "{var1}", "def", "{var2}");
        this.uriBuilderWithVarsNoE = this.uriBuilderWithVarsEnc.clone();
        this.uriBuilderWithVarsNoE.encode(false);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#build()}.
     */
    public void testBuild() throws Exception {
        assertEquals(URI_1, this.uriBuilder1Enc.build());
        assertEquals(URI_1, this.uriBuilder1NoE.build());

        try {
            this.uriBuilderWithVarsEnc.build();
            fail("must fail, because vars are required");
        } catch (final UriBuilderException ube) {
            // wonderful
        }
        try {
            this.uriBuilderWithVarsNoE.build();
            fail("must fail, because vars are required");
        } catch (final UriBuilderException ube) {
            // wonderful
        }
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#build(java.util.Map)}
     * .
     */
    public void testBuildMap() throws Exception {
        final Map<String, Object> vars = new HashMap<String, Object>();
        try {
            this.uriBuilderWithVarsEnc.build(vars);
            fail("must fail, because missing UriTemplate variables");
        } catch (final IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var1", "123");
        try {
            this.uriBuilderWithVarsEnc.build(vars);
            fail("must fail, because missing UriTemplate variable");
        } catch (final IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var2", "456");
        assertEqualsURI("http://localhost/abc/123/def/456",
                this.uriBuilderWithVarsEnc.build(vars));
        vars.put("var3", "789");
        assertEqualsURI("http://localhost/abc/123/def/456",
                this.uriBuilderWithVarsEnc.build(vars));

        vars.put("var2", " ");
        assertEqualsURI("http://localhost/abc/123/def/%20",
                this.uriBuilderWithVarsEnc.build(vars));

        try {
            this.uriBuilderWithVarsNoE.build(vars);
        } catch (final IllegalArgumentException iae) {
            // wonderful
        }
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#build(java.lang.String[])}
     * .
     */
    public void testBuildStringArray() throws Exception {
        try {
            this.uriBuilderWithVarsEnc.build("123");
            fail("must fail, because there are not enough arguments");
        } catch (final IllegalArgumentException e) {
            // wonderful
        }
        URI uri = this.uriBuilderWithVarsEnc.build("123", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uri);
        final UriBuilder uriBuilder2 = this.uriBuilderWithVarsEnc.clone();
        assertEqualsURI("http://localhost/abc/123/def/456", uriBuilder2.build(
                "123", "456"));
        assertEquals(this.uriBuilderWithVarsEnc.toString(), uriBuilder2
                .toString());
        uriBuilder2.path("{var3}");
        uri = this.uriBuilderWithVarsEnc.build("123", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uri);
        try {
            uriBuilder2.build("123", "456");
            fail("must fail, because there are not enough arguments");
        } catch (final IllegalArgumentException e) {
            // wonderful
        }
        final URI uri2 = uriBuilder2.build("123", "456", "789");
        assertEqualsURI("http://localhost/abc/123/def/456/789", uri2);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#clone()}.
     */
    public void testClone() {
        assertEquals(this.uriBuilder1Enc.build(), this.uriBuilder1Enc.clone()
                .build());
        final URI clonedEncWithVars = this.uriBuilder1NoE.clone().build();
        assertEquals(this.uriBuilder1NoE.build(), clonedEncWithVars);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#encode(boolean)}
     * .
     */
    public void testEncode() throws Exception {
        final UriBuilder uriBuilder = RuntimeDelegate.getInstance()
                .createUriBuilder();
        uriBuilder.encode(false);
        uriBuilder.host("www.xyz.de");
        uriBuilder.scheme("http");
        uriBuilder.path("path1", "path2");
        try {
            uriBuilder.path("hh ho");
            fail("must fail, because of invalid character");
        } catch (final IllegalArgumentException e) {
            // wonderful
        }
        uriBuilder.encode(true);
        uriBuilder.path("hh ho");
        assertEqualsURI("http://www.xyz.de/path1/path2/hh%20ho", uriBuilder);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#fragment(java.lang.String)}
     * .
     */
    public void testExtension() throws Exception {
        if (this.uriBuilder1Enc instanceof JaxRsUriBuilder) {
            final String path = getPath((JaxRsUriBuilder) this.uriBuilder1Enc);
            final String lastSegm = path.substring(path.lastIndexOf('/') + 1);
            assertFalse(lastSegm.contains("/")); // lastSegm is only last segmen
            assertFalse(lastSegm.contains(".")); // no "." in it
        }
        this.uriBuilder1Enc.extension("abc");
        assertEqualsURI(URI_1 + ".abc", this.uriBuilder1Enc);

        this.uriBuilder1Enc.extension(null);
        assertEqualsURI(URI_1.toString(), this.uriBuilder1Enc);

        this.uriBuilder1Enc.extension("");
        assertEqualsURI(URI_1 + ".", this.uriBuilder1Enc);

        this.uriBuilder1Enc.extension(null);
        assertEqualsURI(URI_1.toString(), this.uriBuilder1Enc);

        this.uriBuilder1Enc.extension("abc.def");
        assertEqualsURI(URI_1 + ".abc.def", this.uriBuilder1Enc);

        this.uriBuilder1Enc.extension(null);
        assertEqualsURI(URI_1.toString(), this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#fragment(java.lang.String)}
     * .
     */
    public void testFragmentEnc() throws Exception {
        this.uriBuilder1Enc.fragment(String.valueOf((char) 9));
        assertEqualsURI(URI_1 + "#%09", this.uriBuilder1Enc);

        this.uriBuilder1Enc.fragment("anker");
        assertEqualsURI(URI_1 + "#anker", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#fragment(java.lang.String)}
     * .
     */
    public void testFragmentNoE() throws Exception {
        try {
            this.uriBuilder1NoE.fragment(String.valueOf((char) 9));
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEquals(URI_1, this.uriBuilder1NoE.build());
        this.uriBuilder1NoE.fragment("anker");
        assertEqualsURI(URI_1 + "#anker", this.uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#host(java.lang.String)}
     * .
     */
    public void testHostEnc() throws Exception {
        this.uriBuilder1Enc.host("test.domain.org");
        assertEqualsURI("http://test.domain.org/path1/path2",
                this.uriBuilder1Enc);

        try {
            this.uriBuilder1Enc.host("test.domain .org a");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://test.domain.org/path1/path2",
                this.uriBuilder1Enc);
    }

    public void testHostNoE() throws Exception {
        this.uriBuilder1NoE.host("test.domain.org");
        assertEqualsURI("http://test.domain.org/path1/path2",
                this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.host("test.domain .org a");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://test.domain.org/path1/path2",
                this.uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replaceMatrixParams(java.lang.String)}
     * . and
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#matrixParam(java.lang.String, java.lang.String)}
     * .
     */
    public void testMatrixParamEnc() throws Exception {
        this.uriBuilder1Enc.matrixParam("mp1", "mv1");
        assertEqualsURI(URI_1 + ";mp1=mv1", this.uriBuilder1Enc);
        this.uriBuilder1Enc.matrixParam("mp1", "mv2");
        assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2", this.uriBuilder1Enc);
        this.uriBuilder1Enc.matrixParam("mp3", "mv3");
        try {
            assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2;mp3=mv3",
                    this.uriBuilder1Enc);
        } catch (final AssertionFailedError afe) { // try another possibility
            assertEqualsURI(URI_1 + ";mp3=mv3;mp1=mv1;mp1=mv2",
                    this.uriBuilder1Enc);
        }
        this.uriBuilder1Enc.replaceMatrixParams("mp4=mv4");
        assertEqualsURI(URI_1 + ";mp4=mv4", this.uriBuilder1Enc);
        this.uriBuilder1Enc.replaceMatrixParams("");
        assertEquals(new URI(URI_1 + ";"), this.uriBuilder1Enc.build());

        this.uriBuilder1Enc.replaceMatrixParams(null);
        assertEquals(URI_1, this.uriBuilder1Enc.build());
        this.uriBuilder1Enc.matrixParam("jkj$sdf", "ij a%20");
        assertEqualsURI(URI_1 + ";jkj%24sdf=ij%20a%20", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replaceMatrixParams(java.lang.String)}
     * . and
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#matrixParam(java.lang.String, java.lang.String)}
     * .
     */
    public void testMatrixParamNoE() throws Exception {
        this.uriBuilder1NoE.matrixParam("mp1", "mv1");
        assertEqualsURI(URI_1 + ";mp1=mv1", this.uriBuilder1NoE);
        this.uriBuilder1NoE.matrixParam("mp1", "mv2");
        assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2", this.uriBuilder1NoE);
        this.uriBuilder1NoE.matrixParam("mp3", "mv3");
        try {
            assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2;mp3=mv3",
                    this.uriBuilder1NoE);
        } catch (final AssertionFailedError afe) { // try another possibility
            assertEqualsURI(URI_1 + ";mp3=mv3;mp1=mv1;mp1=mv2",
                    this.uriBuilder1NoE);
        }
        this.uriBuilder1NoE.replaceMatrixParams("mp4=mv4");
        assertEqualsURI(URI_1 + ";mp4=mv4", this.uriBuilder1NoE);
        this.uriBuilder1NoE.replaceMatrixParams("");
        assertEquals(new URI(URI_1 + ";"), this.uriBuilder1NoE.build());

        this.uriBuilder1NoE.replaceMatrixParams(null);
        assertEquals(URI_1, this.uriBuilder1NoE.build());
        try {
            this.uriBuilder1NoE.matrixParam("jkj$sdf", "ij a%20");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEquals(URI_1, this.uriBuilder1NoE.build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.Class)}
     * .
     */
    public void testPathClass() throws Exception {
        this.uriBuilder1Enc.replacePath((String[]) null);
        this.uriBuilder1Enc.path(SimpleTrain.class);
        assertEqualsURI("http://localhost" + SimpleTrain.PATH,
                this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.Class, java.lang.String)}
     * .
     */
    public void testPathClassString() throws Exception {
        this.uriBuilder1Enc.replacePath((String[]) null);
        this.uriBuilder1Enc.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.PATH + "/"
                + CarListResource.OFFERS_PATH, this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.Class, java.lang.String)}
     * .
     */
    public void testPathClassStringEnc() throws Exception {
        this.uriBuilder1Enc.replacePath((String[]) null);
        this.uriBuilder1Enc.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.PATH + "/"
                + CarListResource.OFFERS_PATH, this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.reflect.Method[])}
     * .
     */
    public void testPathMethodArrayEnc() throws Exception {
        this.uriBuilder1Enc.replacePath((String[]) null);
        final Method findCar = CarListResource.class.getMethod("findCar",
                Integer.TYPE);
        final Method engine = CarResource.class.getMethod("findEngine");
        this.uriBuilder1Enc.path(CarListResource.class);
        this.uriBuilder1Enc.path(findCar, engine);
        assertEqualsURI("http://localhost/" + CarListResource.PATH
                + "/5/engine", this.uriBuilder1Enc.build("5"));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.String[])}
     * .
     */
    public void testPathStringArrayEnc() throws Exception {
        this.uriBuilder1Enc.path("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", this.uriBuilder1Enc);
        this.uriBuilder1Enc.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", this.uriBuilder1Enc);

        this.uriBuilder1Enc.path(" ");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno/%20", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#path(java.lang.String[])}
     * .
     */
    public void testPathStringArrayNoE() throws Exception {
        this.uriBuilder1NoE.path("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", this.uriBuilder1NoE);
        this.uriBuilder1NoE.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.path(" ");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", this.uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#port(int)}.
     */
    public void testPort() throws Exception {
        this.uriBuilder1Enc.port(4711);
        assertEqualsURI("http://localhost:4711/path1/path2",
                this.uriBuilder1Enc);
        this.uriBuilder1Enc.port(-1);
        assertEqualsURI("http://localhost/path1/path2", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#queryParam(java.lang.String, java.lang.String)}
     * .
     */
    public void testQueryEnc() throws Exception {
        this.uriBuilder1Enc.queryParam("qn", "qv");
        assertEqualsURI(URI_1 + "?qn=qv", this.uriBuilder1Enc);
        this.uriBuilder1Enc.queryParam("qn", "qv2");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2", this.uriBuilder1Enc);
        this.uriBuilder1Enc.queryParam("qn3", "qv3");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2&qn3=qv3", this.uriBuilder1Enc);
        this.uriBuilder1Enc.replaceQueryParams("qnNew=qvNew");
        assertEqualsURI(URI_1 + "?qnNew=qvNew", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replaceQueryParams(null);
        this.uriBuilder1Enc.queryParam("na$me", "George U.");
        assertEqualsURI(URI_1 + "?na%24me=George%20U.", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#queryParam(java.lang.String, java.lang.String)}
     * .
     */
    public void testQueryNoE() throws Exception {
        this.uriBuilder1NoE.queryParam("qn", "qv");
        assertEqualsURI(URI_1 + "?qn=qv", this.uriBuilder1NoE);
        this.uriBuilder1NoE.queryParam("qn", "qv2");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2", this.uriBuilder1NoE);
        this.uriBuilder1NoE.queryParam("qn3", "qv3");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2&qn3=qv3", this.uriBuilder1NoE);
        this.uriBuilder1NoE.replaceQueryParams("qnNew=qvNew");
        assertEqualsURI(URI_1 + "?qnNew=qvNew", this.uriBuilder1NoE);

        this.uriBuilder1NoE.replaceQueryParams(null);
        try {
            this.uriBuilder1NoE.queryParam("na$me", "George U.");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEquals(URI_1, this.uriBuilder1NoE.build());
    }

    public void testReplaceMatrixParamsEnc() throws Exception {
        this.uriBuilder1Enc.matrixParam("a", "b");
        this.uriBuilder1Enc.matrixParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2;a=b;c=d",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.replaceMatrixParams("ksd hflk");
        assertEqualsURI("http://localhost/path1/path2;ksd%20hflk",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.replaceMatrixParams("e=f");
        assertEqualsURI("http://localhost/path1/path2;e=f", this.uriBuilder1Enc);
    }

    public void testReplaceMatrixParamsNoE() throws Exception {
        this.uriBuilder1NoE.matrixParam("a", "b");
        this.uriBuilder1NoE.matrixParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2;a=b;c=d",
                this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.replaceMatrixParams("ksd hflk");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://localhost/path1/path2;a=b;c=d",
                this.uriBuilder1NoE);

        this.uriBuilder1NoE.replaceMatrixParams("e=f");
        assertEqualsURI("http://localhost/path1/path2;e=f", this.uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replacePath(java.lang.String)}
     * .
     */
    public void testReplacePathEnc() throws Exception {
        this.uriBuilder1Enc.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath((String[]) null);
        assertEqualUriBuilder("http", null, "localhost", null, "", null,
                this.uriBuilder1Enc);
        assertEqualsUriSlashAllowed("http://localhost", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r t");
        assertEqualsURI("http://localhost/gh/r%20t", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r;t");
        assertEqualsURI("http://localhost/gh/r;t", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r;t=6");
        assertEqualsURI("http://localhost/gh/r;t=6", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r;t=");
        assertEqualsURI("http://localhost/gh/r;t=", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r;t=6;g");
        assertEqualsURI("http://localhost/gh/r;t=6;g", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#replacePath(java.lang.String)}
     * .
     */
    public void testReplacePathNoE() throws Exception {
        this.uriBuilder1NoE.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", this.uriBuilder1NoE);

        this.uriBuilder1NoE.replacePath((String[]) null);
        assertEqualUriBuilder("http", null, "localhost", null, "", null,
                this.uriBuilder1NoE);

        assertEqualsUriSlashAllowed("http://localhost", this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.replacePath("gh", "r t");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsUriSlashAllowed("http://localhost", this.uriBuilder1NoE);

        this.uriBuilder1Enc.replacePath("gh", "r;t");
        assertEqualsURI("http://localhost/gh/r;t", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r;t=");
        assertEqualsURI("http://localhost/gh/r;t=", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r;t=6");
        assertEqualsURI("http://localhost/gh/r;t=6", this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath("gh", "r;t=6;g");
        assertEqualsURI("http://localhost/gh/r;t=6;g", this.uriBuilder1Enc);
    }

    public void testReplaceQueryParamsEnc() throws Exception {
        this.uriBuilder1Enc.queryParam("a", "b");
        this.uriBuilder1Enc.queryParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2?a=b&c=d",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.replaceQueryParams("ksd hflk");
        assertEqualsURI("http://localhost/path1/path2?ksd+hflk",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.replaceQueryParams("e=f");
        assertEqualsURI("http://localhost/path1/path2?e=f", this.uriBuilder1Enc);
    }

    public void testReplaceQueryParamsNoE() throws Exception {
        this.uriBuilder1NoE.queryParam("a", "b");
        this.uriBuilder1NoE.queryParam("c", "d");
        assertEqualsURI("http://localhost/path1/path2?a=b&c=d",
                this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.replaceQueryParams("ksd hflk");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("http://localhost/path1/path2?a=b&c=d",
                this.uriBuilder1NoE);

        this.uriBuilder1NoE.replaceQueryParams("e=f");
        assertEqualsURI("http://localhost/path1/path2?e=f", this.uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#scheme(java.lang.String)}
     * .
     */
    public void testSchemeEnc() throws Exception {
        this.uriBuilder1Enc.scheme("ftp");
        assertEqualsURI("ftp://localhost/path1/path2", this.uriBuilder1Enc);
        this.uriBuilder1Enc.scheme("f4.-+tp");
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder1Enc);

        try {
            this.uriBuilder1Enc.scheme("44");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder1Enc);

        try {
            this.uriBuilder1Enc.scheme("f\0");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#scheme(java.lang.String)}
     * .
     */
    public void testSchemeNoE() throws Exception {
        this.uriBuilder1NoE.scheme("ftp");
        assertEqualsURI("ftp://localhost/path1/path2", this.uriBuilder1NoE);
        this.uriBuilder1NoE.scheme("f4.-+tp");
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.scheme("44");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder1NoE);
        try {
            this.uriBuilder1NoE.scheme("f\0");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsURI("f4.-+tp://localhost/path1/path2", this.uriBuilder1NoE);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#schemeSpecificPart(java.lang.String)}
     * .
     */
    public void testSchemeSpecificPartEnc() throws Exception {
        this.uriBuilder1Enc.schemeSpecificPart("//shkf");
        assertEqualUriBuilder("http", null, "shkf", null, null, null,
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.schemeSpecificPart("//shkf/akfshdf");
        assertEqualUriBuilder("http", null, "shkf", null, "/akfshdf", null,
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder("http", "user", "shkf", null, "/akfshdf/akjhf",
                null, this.uriBuilder1Enc);

        this.uriBuilder1Enc.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder("http", null, "shkf", "4711", "/akjhf", "a=b",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc
                .schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2", "a=b", this.uriBuilder1Enc);

        this.uriBuilder1Enc
                .schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2;3=4", "a=b", this.uriBuilder1Enc);

        this.uriBuilder1Enc.schemeSpecificPart("//www.domain.org/ ");
        assertEqualUriBuilder("http", null, "www.domain.org", null, "/%20",
                null, this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#schemeSpecificPart(java.lang.String)}
     * .
     */
    public void testSchemeSpecificPartNoE() throws Exception {
        this.uriBuilder1NoE.schemeSpecificPart("//shkf");
        assertEqualUriBuilder("http", null, "shkf", null, null, null,
                this.uriBuilder1NoE);

        this.uriBuilder1NoE.schemeSpecificPart("//shkf/akfshdf");
        assertEqualUriBuilder("http", null, "shkf", null, "/akfshdf", null,
                this.uriBuilder1NoE);

        this.uriBuilder1NoE.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder("http", "user", "shkf", null, "/akfshdf/akjhf",
                null, this.uriBuilder1NoE);

        this.uriBuilder1NoE.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder("http", null, "shkf", "4711", "/akjhf", "a=b",
                this.uriBuilder1NoE);

        this.uriBuilder1NoE
                .schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2", "a=b", this.uriBuilder1NoE);

        this.uriBuilder1NoE
                .schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2;3=4", "a=b", this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.schemeSpecificPart("//www.domain.org/ ");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualUriBuilder("http", null, "www.domain.org", null,
                "/akjhf;1=2;3=4", "a=b", this.uriBuilder1NoE);
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

        final String path = "path1/path2;mp1=mv1" + Reference.encode("?")
                + ";mp2=mv2/abc.html";
        uriBuilder = UriBuilder.fromPath(path, false);
        if (uriBuilder instanceof JaxRsUriBuilder) {
            assertEqualUriBuilder(null, null, null, null,
                    "path1/path2;mp1=mv1%3F;mp2=mv2/abc.html", null, uriBuilder);
        }
        assertEqualsURI(path, uriBuilder);
    }

    public void testTemplateParamsEnc() throws Exception {
        changeWithTemplVars((JaxRsUriBuilder) this.uriBuilderWithVarsEnc);

        URI uri = buildFromTemplVarsWithMap(this.uriBuilderWithVarsEnc);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);

        uri = buildFromTemplVarsWithStrings(this.uriBuilderWithVarsEnc);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);
    }

    public void testTemplateParamsNoE() throws Exception {
        changeWithTemplVars((JaxRsUriBuilder) this.uriBuilderWithVarsNoE);

        URI uri = buildFromTemplVarsWithMap(this.uriBuilderWithVarsNoE);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);

        uri = buildFromTemplVarsWithStrings(this.uriBuilderWithVarsNoE);
        assertEqualsURI(TEMPL_VARS_EXPECTED, uri);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#toString()}.
     */
    public void testToString() {
        assertEquals("http://localhost/path1/path2", this.uriBuilder1Enc
                .toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#uri(java.net.URI)}
     * .
     */
    public void testUri() throws Exception {
        URI u = new URI("ftp", "test.org", null, null, "fragment");
        this.uriBuilder1Enc.uri(u);
        assertEqualsURI("ftp://test.org#fragment", this.uriBuilder1Enc);

        u = new URI("ftp", "test.org", "/path", "qu=ery", "fragment");
        this.uriBuilder1Enc.uri(u);
        assertEqualsURI("ftp://test.org/path?qu=ery#fragment",
                this.uriBuilder1Enc);

        final String id = "4711";
        final URI collectionUri = new URI(
                "http://localhost:8181/SecurityContextTestService");
        final URI location = UriBuilder.fromUri(collectionUri).path("{id}")
                .build(id);
        assertEqualsURI(collectionUri + "/4711", location);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#userInfo(java.lang.String)}
     * .
     */
    public void testUserInfoEnc() throws Exception {
        this.uriBuilder1Enc.userInfo("username");
        assertEqualsURI("http://username@localhost/path1/path2",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.replacePath((String) null);
        this.uriBuilder1Enc.host("abc");
        this.uriBuilder1Enc.userInfo("username:pw");
        assertEqualsUriSlashAllowed("http://username:pw@abc",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.userInfo("rkj;s78:&=+$,");
        assertEqualsUriSlashAllowed("http://rkj;s78:&=+$,@abc",
                this.uriBuilder1Enc);

        this.uriBuilder1Enc.userInfo(" ");
        assertEqualsUriSlashAllowed("http://%20@abc", this.uriBuilder1Enc);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriBuilder#userInfo(java.lang.String)}
     * .
     */
    public void testUserInfoNoE() throws Exception {
        this.uriBuilder1NoE.userInfo("username");
        assertEqualsURI("http://username@localhost/path1/path2",
                this.uriBuilder1NoE);

        this.uriBuilder1NoE.host("abc");
        this.uriBuilder1NoE.replacePath((String) null);
        this.uriBuilder1NoE.userInfo("username:pw");
        assertEqualsUriSlashAllowed("http://username:pw@abc",
                this.uriBuilder1NoE);

        this.uriBuilder1NoE.userInfo("rkj;s78:&=+$,");
        assertEqualsUriSlashAllowed("http://rkj;s78:&=+$,@abc",
                this.uriBuilder1NoE);

        try {
            this.uriBuilder1NoE.userInfo(" ");
            fail();
        } catch (final IllegalArgumentException iae) {
            // good
        }
        assertEqualsUriSlashAllowed("http://rkj;s78:&=+$,@abc",
                this.uriBuilder1NoE);
    }
}