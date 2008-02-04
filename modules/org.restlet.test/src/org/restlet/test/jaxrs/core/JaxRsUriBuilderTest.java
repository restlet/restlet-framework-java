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

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.restlet.test.jaxrs.services.SimpleTrain;
import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.car.CarResource;

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

    private UriBuilder uriBuilder1;

    /** UriBuilder with variableNames */
    private UriBuilder uriBuilderWithVars;

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
    private void assertEqualUriBuilder(UriBuilder actualUriBuilder,
            String expectedScheme, String expectedUserInfo,
            String expectedHost, int expectedPort, String expectedQuery,
            JaxRsPathSegment... expectedPathSegments) {
        if (actualUriBuilder instanceof JaxRsUriBuilder) {
            JaxRsUriBuilder jaxRsUriBuilder = (JaxRsUriBuilder) actualUriBuilder;
            assertEquals(expectedScheme, jaxRsUriBuilder.getScheme());
            assertEquals(expectedUserInfo, jaxRsUriBuilder.getUserInfo());
            assertEquals(expectedHost, jaxRsUriBuilder.getHost());
            assertEquals(expectedPort, jaxRsUriBuilder.getPort());
            LinkedList<JaxRsPathSegment> actPathSegms = jaxRsUriBuilder
                    .getPathSegments();
            for (int i = 0; i < expectedPathSegments.length; i++) {
                PathSegment expectedPathSegment = expectedPathSegments[i];
                JaxRsPathSegment actPathSegm = actPathSegms.get(i);
                assertEquals(i + ". path segm:", expectedPathSegment,
                        actPathSegm);
            }
            assertEquals(expectedPathSegments.length, actPathSegms.size());
            CharSequence actualQuery = jaxRsUriBuilder.getQuery();
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uriBuilder1 = RuntimeDelegate.getInstance().createUriBuilder();
        uriBuilder1.host("localhost");
        uriBuilder1.path("path1", "path2");
        uriBuilder1.scheme("http");
        uriBuilderWithVars = RuntimeDelegate.getInstance().createUriBuilder();
        uriBuilderWithVars.encode(false);
        uriBuilderWithVars.host("localhost");
        uriBuilderWithVars.scheme("http");
        uriBuilderWithVars.path("abc", "{var1}", "def", "{var2}");
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#build()}.
     */
    public void testBuild() throws Exception {
        assertEquals(URI_1, uriBuilder1.build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#build(java.util.Map)}.
     */
    public void testBuildMap() throws Exception {
        assertEqualsURI("http://localhost/abc/%7Bvar1%7D/def/%7Bvar2%7D",
                uriBuilderWithVars);
        Map<String, String> vars = new HashMap<String, String>();
        try {
            uriBuilderWithVars.build(vars);
            fail("must fail, because missing UriTemplate variables");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var1", "123");
        try {
            uriBuilderWithVars.build(vars);
            fail("must fail, because missing UriTemplate variable");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        vars.put("var2", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uriBuilderWithVars
                .build(vars));
        vars.put("var3", "789");
        assertEqualsURI("http://localhost/abc/123/def/456", uriBuilderWithVars
                .build(vars));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#build(java.lang.String[])}.
     */
    public void testBuildStringArray() throws Exception {
        try {
            uriBuilderWithVars.build("123");
            fail("must fail, because there are not enough arguments");
        } catch (IllegalArgumentException e) {
            // wonderful
        }
        URI uri = uriBuilderWithVars.build("123", "456");
        assertEqualsURI("http://localhost/abc/123/def/456", uri);
        UriBuilder uriBuilder2 = uriBuilderWithVars.clone();
        assertEqualsURI("http://localhost/abc/123/def/456", uriBuilder2.build(
                "123", "456"));
        assertEquals(uriBuilderWithVars.toString(), uriBuilder2.toString());
        uriBuilder2.path("{var3}");
        uri = uriBuilderWithVars.build("123", "456");
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
        assertEquals(uriBuilder1.build(), uriBuilder1.clone().build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#fragment(java.lang.String)}.
     */
    public void testFragment() throws Exception {
        uriBuilder1.fragment("anker");
        assertEqualsURI(URI_1 + "#anker", uriBuilder1);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#host(java.lang.String)}.
     */
    public void testHost() throws Exception {
        uriBuilder1.host("test.domain.org");
        assertEqualsURI("http://test.domain.org/path1/path2", uriBuilder1);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#replaceMatrixParams(java.lang.String)}.
     * and
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#matrixParam(java.lang.String, java.lang.String)}.
     */
    public void testMatrixParam() throws Exception {
        uriBuilder1.matrixParam("mp1", "mv1");
        assertEqualsURI(URI_1 + ";mp1=mv1", uriBuilder1);
        uriBuilder1.matrixParam("mp1", "mv2");
        assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2", uriBuilder1);
        uriBuilder1.matrixParam("mp3", "mv3");
        try {
            assertEqualsURI(URI_1 + ";mp1=mv1;mp1=mv2;mp3=mv3", uriBuilder1);
        } catch (AssertionFailedError afe) { // try another possibility
            assertEqualsURI(URI_1 + ";mp3=mv3;mp1=mv1;mp1=mv2", uriBuilder1);
        }
        uriBuilder1.replaceMatrixParams("mp4=mv4");
        assertEqualsURI(URI_1 + ";mp4=mv4", uriBuilder1);
        uriBuilder1.replaceMatrixParams("");
        assertEquals(URI_1, uriBuilder1.build());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.Class)}.
     */
    public void testPathClass() throws Exception {
        uriBuilder1.replacePath((String[]) null);
        uriBuilder1.path(SimpleTrain.class);
        assertEqualsURI("http://localhost" + SimpleTrain.PATH, uriBuilder1);
        // LATER gucken, dass er @Path.encode richtig auswertet
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.reflect.Method[])}.
     */
    public void testPathMethodArray() throws Exception {
        uriBuilder1.replacePath((String[]) null);
        Method findCar = CarListResource.class.getMethod("findCar",
                String.class);
        Method engine = CarResource.class.getMethod("findEngine");
        uriBuilder1.path(CarListResource.class);
        uriBuilder1.path(findCar, engine);
        assertEqualsURI("http://localhost/" + CarListResource.PATH
                + "/5/engine", uriBuilder1.build("5"));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.Class, java.lang.String)}.
     */
    public void testPathClassString() throws Exception {
        uriBuilder1.replacePath((String[]) null);
        uriBuilder1.path(CarListResource.class, "getOffers");
        assertEqualsURI("http://localhost/" + CarListResource.PATH + "/"
                + CarListResource.OFFERS_PATH, uriBuilder1);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#path(java.lang.String[])}.
     */
    public void testPathStringArray() throws Exception {
        uriBuilder1.path("jjj", "kkk", "ll");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll", uriBuilder1);
        uriBuilder1.path("mno");
        assertEqualsURI(URI_1 + "/jjj/kkk/ll/mno", uriBuilder1);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#port(int)}.
     */
    public void testPort() throws Exception {
        uriBuilder1.port(4711);
        assertEqualsURI("http://localhost:4711/path1/path2", uriBuilder1);
        uriBuilder1.port(-1);
        assertEqualsURI("http://localhost/path1/path2", uriBuilder1);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#queryParam(java.lang.String, java.lang.String)}.
     */
    public void testQuery() throws Exception {
        uriBuilder1.queryParam("qn", "qv");
        assertEqualsURI(URI_1 + "?qn=qv", uriBuilder1);
        uriBuilder1.queryParam("qn", "qv2");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2", uriBuilder1);
        uriBuilder1.queryParam("qn3", "qv3");
        assertEqualsURI(URI_1 + "?qn=qv&qn=qv2&qn3=qv3", uriBuilder1);
        uriBuilder1.replaceQueryParams("qnNew=qvNew");
        assertEqualsURI(URI_1 + "?qnNew=qvNew", uriBuilder1);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#replacePath(java.lang.String)}.
     */
    public void testReplacePath() throws Exception {
        uriBuilder1.replacePath("newPath");
        assertEqualsURI("http://localhost/newPath", uriBuilder1);

        uriBuilder1.replacePath((String[]) null);
        assertEqualUriBuilder(uriBuilder1, "http", null, "localhost", -1, null);
        try {
            assertEqualsURI("http://localhost/", uriBuilder1);
        } catch (AssertionFailedError afe) {
            assertEqualsURI("http://localhost", uriBuilder1);
        }
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#scheme(java.lang.String)}.
     */
    public void testScheme() throws Exception {
        uriBuilder1.scheme("ftp");
        assertEqualsURI("ftp://localhost/path1/path2", uriBuilder1);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#schemeSpecificPart(java.lang.String)}.
     */
    public void testSchemeSpecificPart() throws Exception {
        uriBuilder1.schemeSpecificPart("//shkf");
        assertEqualUriBuilder(uriBuilder1, "http", null, "shkf", -1, null);

        uriBuilder1.schemeSpecificPart("//shkf/akfshdf");
        assertEqualUriBuilder(uriBuilder1, "http", null, "shkf", -1, null,
                new JaxRsPathSegment("akfshdf", false, null));

        uriBuilder1.schemeSpecificPart("//user@shkf/akfshdf/akjhf");
        assertEqualUriBuilder(uriBuilder1, "http", "user", "shkf", -1, null,
                new JaxRsPathSegment("akfshdf", false, null),
                new JaxRsPathSegment("akjhf", false, null));

        uriBuilder1.schemeSpecificPart("//shkf:4711/akjhf?a=b");
        assertEqualUriBuilder(uriBuilder1, "http", null, "shkf", 4711, "a=b",
                new JaxRsPathSegment("akjhf", false, null));

        uriBuilder1.schemeSpecificPart("//www.domain.org/akjhf;1=2?a=b");
        MultivaluedMapImpl<String, String> mp = new MultivaluedMapImpl<String, String>();
        mp.putSingle("1", "2");
        assertEqualUriBuilder(uriBuilder1, "http", null, "www.domain.org", -1,
                "a=b", new JaxRsPathSegment("akjhf", false, mp));

        uriBuilder1.schemeSpecificPart("//www.domain.org/akjhf;1=2;3=4?a=b");
        mp = new MultivaluedMapImpl<String, String>();
        mp.putSingle("1", "2");
        mp.putSingle("3", "4");
        assertEqualUriBuilder(uriBuilder1, "http", null, "www.domain.org", -1,
                "a=b", new JaxRsPathSegment("akjhf", false, mp));
    }

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
        assertEquals("http://localhost/path1/path2", uriBuilder1.toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.core.JaxRsUriBuilder#uri(java.net.URI)}.
     */
    public void testUri() throws Exception {
        // TODO test again Jersey Implementation
        URI u = new URI("ftp", "test.org", null, null, "fragment");
        uriBuilder1.uri(u);
        assertEqualsURI("ftp://test.org#fragment", uriBuilder1);

        u = new URI("ftp", "test.org", "/path", "qu=ery", "fragment");
        uriBuilder1.uri(u);
        assertEqualsURI("ftp://test.org/path?qu=ery#fragment", uriBuilder1);

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
    public void testUserInfo() throws Exception {
        uriBuilder1.encode(false);
        uriBuilder1.userInfo("username");
        assertEqualsURI("http://username@localhost/path1/path2", uriBuilder1);
        // LATER test: uriBuilder1.userInfo("username:password");
        // assertEquals("http://username:password@localhost/path1/path2",uriBuilder1.build().toString());
    }
}
