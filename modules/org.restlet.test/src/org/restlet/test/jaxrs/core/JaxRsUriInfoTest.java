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

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import junit.framework.TestCase;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 * 
 */
public class JaxRsUriInfoTest extends TestCase {

    private static final String BASE_REF_STR = "http://localhost/test/";

    private static final Reference BASE_REF = new Reference(BASE_REF_STR);

    private static final String RELATIV_1 = "relativ/a/b";

    private static final String RELATIV_2 = "relativ/%20a%20/%21b%40%2C";

    private static final Reference REFERENCE_1 = new Reference(BASE_REF,
            BASE_REF_STR + RELATIV_1);

    private static final Reference REFERENCE_2 = new Reference(BASE_REF,
            BASE_REF_STR + RELATIV_2);

    private static final String RELATIV_2_DECODED = "relativ/ a /!b@,";

    private static final UriInfo URI_INFO_1 = new JaxRsUriInfo(REFERENCE_1);

    private static final UriInfo URI_INFO_2 = new JaxRsUriInfo(REFERENCE_2);

    private static final JaxRsUriInfo URI_INFO_3 = new JaxRsUriInfo(
            new Reference(BASE_REF, BASE_REF_STR + "hfk;abc=def;ghi=jkl/hjh"));

    private static final JaxRsUriInfo URI_INFO_4 = new JaxRsUriInfo(
            new Reference(BASE_REF, BASE_REF_STR + "hfk;ghi=jkl;abc=def/hjh"));

    private static final JaxRsUriInfo URI_INFO_5 = new JaxRsUriInfo(
            new Reference(BASE_REF, BASE_REF_STR + "hfk;abc=%20def;ghi=jkl"));

    private static final JaxRsUriInfo URI_INFO_7 = new JaxRsUriInfo(
            new Reference(BASE_REF, BASE_REF_STR + "abc?def=123&ghi=456"));

    private static final JaxRsUriInfo URI_INFO_8 = new JaxRsUriInfo(
            new Reference(BASE_REF, BASE_REF_STR + "abc?def=1+23&gh%20i=45%206"));

    private void checkEntry(String expectedValue, String key,
            MultivaluedMap<String, String> templateParameters) {
        assertEquals(expectedValue, templateParameters.getFirst(key));
    }

    // the Template parameters are tested in SimpleTrain and SimpleTrainTest

    /**
     * @param pathSegments
     * @param path0
     * @param tpSize0
     *                templatParamaterSize
     * @param path1
     * @param tpSize1
     * @param path2
     * @param tpSize2
     */
    private void checkPathSegments(List<PathSegment> pathSegments,
            String path0, int tpSize0, String path1, int tpSize1, String path2,
            int tpSize2) {
        assertEquals(3, pathSegments.size());
        PathSegment pathSegment0 = pathSegments.get(0);
        PathSegment pathSegment1 = pathSegments.get(1);
        PathSegment pathSegment2 = pathSegments.get(2);
        assertEquals(path0, pathSegment0.getPath());
        assertEquals(tpSize0, pathSegment0.getMatrixParameters().size());
        assertEquals(path1, pathSegment1.getPath());
        assertEquals(tpSize1, pathSegment1.getMatrixParameters().size());
        assertEquals(path2, pathSegment2.getPath());
        assertEquals(tpSize2, pathSegment2.getMatrixParameters().size());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getRequestUriBuilder()}.
     */
    public void testEqualsObject() {
        assertEquals("URI_INFO_3 and URI_INFO_4 must be equals", URI_INFO_3,
                URI_INFO_4);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getAbsolutePath()}.
     */
    public void testGetAbsolutePath() throws Exception {
        JaxRsUriBuilderTest.assertEqualsURI(BASE_REF_STR + RELATIV_1,
                URI_INFO_1.getAbsolutePath());
        JaxRsUriBuilderTest.assertEqualsURI(BASE_REF_STR + RELATIV_2,
                URI_INFO_2.getAbsolutePath());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getAbsolutePathBuilder()}.
     */
    public void testGetAbsolutePathBuilder() throws Exception {
        JaxRsUriBuilderTest.assertEqualsURI(BASE_REF_STR + RELATIV_1,
                URI_INFO_1.getAbsolutePathBuilder());
        
        String expectedUri = BASE_REF_STR + RELATIV_2;
        URI actualUri = URI_INFO_2.getAbsolutePathBuilder().build();
        JaxRsUriBuilderTest.assertEquals(new URI(expectedUri), actualUri);
        JaxRsUriBuilderTest.assertEquals(expectedUri, actualUri.toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getBaseUri()}.
     */
    public void testGetBaseUri() {
        URI baseUri1 = URI_INFO_1.getBaseUri();
        assertEquals(BASE_REF_STR, baseUri1.toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getBaseUriBuilder()}.
     */
    public void testGetBaseUriBuilder() throws Exception {
        URI uri = URI_INFO_1.getBaseUri();
        JaxRsUriBuilderTest.assertEqualsURI(BASE_REF_STR, uri);
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPath()}.
     */
    public void testGetPath() {
        assertEquals(RELATIV_1, URI_INFO_1.getPath());
        assertEquals(RELATIV_2_DECODED, URI_INFO_2.getPath());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPath(boolean)}.
     */
    public void testGetPathBoolean() {
        assertEquals(RELATIV_1, URI_INFO_1.getPath(true));
        assertEquals(RELATIV_1, URI_INFO_1.getPath(false));
        assertEquals(RELATIV_2_DECODED, URI_INFO_2.getPath(true));
        assertEquals(RELATIV_2, URI_INFO_2.getPath(false));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPathSegments()}.
     */
    public void testGetPathSegments() {
        // must be alid for every UriInfo
        assertEquals(URI_INFO_1.getPathSegments(true), URI_INFO_1
                .getPathSegments());
        assertEquals(URI_INFO_2.getPathSegments(true), URI_INFO_2
                .getPathSegments());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPathSegments(boolean)}.
     */
    public void testGetPathSegmentsBoolean() {
        checkPathSegments(URI_INFO_1.getPathSegments(true), "relativ", 0, "a",
                0, "b", 0);
        checkPathSegments(URI_INFO_1.getPathSegments(false), "relativ", 0, "a",
                0, "b", 0);

        checkPathSegments(URI_INFO_2.getPathSegments(true), "relativ", 0,
                " a ", 0, "!b@,", 0);
        checkPathSegments(URI_INFO_2.getPathSegments(false), "relativ", 0,
                "%20a%20", 0, "%21b%40%2C", 0);

        UriInfo uriInfo = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR
                + "abc/def;ghi=jkl;mno=pqr/stu;vwx=yz"));
        List<PathSegment> pathSegments = uriInfo.getPathSegments();
        checkPathSegments(pathSegments, "abc", 0, "def", 2, "stu", 1);

        MultivaluedMap<String, String> templateParameters1 = pathSegments
                .get(1).getMatrixParameters();
        assertEquals(2, templateParameters1.size());
        checkEntry("jkl", "ghi", templateParameters1);
        checkEntry("pqr", "mno", templateParameters1);

        MultivaluedMap<String, String> templateParameters2 = pathSegments
                .get(2).getMatrixParameters();
        assertEquals(1, templateParameters2.size());
        checkEntry("yz", "vwx", templateParameters2);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getQueryParameters(boolean)}.
     */
    public void testGetQueryParametersDecoded() {
        assertEquals("123", URI_INFO_7.getQueryParameters(true).getFirst("def"));
        assertEquals("456", URI_INFO_7.getQueryParameters(true).getFirst("ghi"));
        assertEquals(2, URI_INFO_7.getQueryParameters(true).size());

        assertEquals("1 23", URI_INFO_8.getQueryParameters(true)
                .getFirst("def"));
        assertEquals("45 6", URI_INFO_8.getQueryParameters(true).getFirst(
                "gh i"));
        assertEquals(2, URI_INFO_7.getQueryParameters(true).size());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getQueryParameters()}.
     */
    public void testGetQueryParametersEncoded() {
        assertEquals("123", URI_INFO_7.getQueryParameters(false)
                .getFirst("def"));
        assertEquals("456", URI_INFO_7.getQueryParameters(false)
                .getFirst("ghi"));
        assertEquals(2, URI_INFO_7.getQueryParameters(false).size());

        assertEquals("1+23", URI_INFO_8.getQueryParameters(false).getFirst(
                "def"));
        assertEquals("45%206", URI_INFO_8.getQueryParameters(false).getFirst(
                "gh%20i"));
        assertEquals(2, URI_INFO_8.getQueryParameters(false).size());
    }

    public void testGetQueryParametersUnmodifiable() {
        assertUnmodifiable(URI_INFO_5.getQueryParameters(true));
        assertUnmodifiable(URI_INFO_5.getQueryParameters(false));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getRequestUri()}.
     */
    public void testGetRequestUri() throws Exception {
        URI uri1 = URI_INFO_1.getRequestUri();
        assertEquals(new URI("http://localhost/test/relativ/a/b"), uri1);
    }

    public void testGetRequestUriBuilder() throws Exception {
        UriBuilder uriBuilder1 = URI_INFO_1.getRequestUriBuilder();
        JaxRsUriBuilderTest.assertEqualsURI(
                "http://localhost/test/relativ/a/b", uriBuilder1);
    }

    protected static void assertUnmodifiable(
            MultivaluedMap<String, String> multivaluedMap) {
        try {
            multivaluedMap.add("jh,", "hkj");
        } catch (NotYetImplementedException usoe) {
            throw usoe;
        } catch (UnsupportedOperationException usoe) {
            // must be thrown, because it should be unmodifiable
        }
    }
}
