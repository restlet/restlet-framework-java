/*
 * Copyright 2005-2008 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.test.jaxrs.core;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import junit.framework.TestCase;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.core.JaxRsUriInfo;

/**
 * @author Stephan Koops
 *
 */
public class JaxRsUriInfoTest extends TestCase {
   
    private static final String BASE_REF_STR = "http://localhost/test/";
    private static final Reference BASE_REF = new Reference(BASE_REF_STR);
    private static final JaxRsUriInfo URI_INFO_3 = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR+"hfk;abc=def;ghi=jkl/hjh"));
    private static final JaxRsUriInfo URI_INFO_4 = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR+"hfk;ghi=jkl;abc=def/hjh"));

    private static final JaxRsUriInfo URI_INFO_5 = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR+"hfk;abc=%20def;ghi=jkl/hjh"));
    private static final JaxRsUriInfo URI_INFO_6 = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR+"hfk;ghi=jkl;abc= def/hjh"));

    private static final String RELATIV_1 = "relativ/a/b";
    private static final UriInfo URI_INFO_1 = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR+RELATIV_1)); 

    private static final String RELATIV_2 = "relativ/%20a%20/!b@%2C";
    private static final String RELATIV_2_DECODED = "relativ/ a /!b@,";
    private static final UriInfo URI_INFO_2 = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR+RELATIV_2)); 

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getPath()}.
     */
    public void testGetPath() {
        assertEquals(RELATIV_1, URI_INFO_1.getPath());
        assertEquals(RELATIV_2_DECODED, URI_INFO_2.getPath());
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getPath(boolean)}.
     */
    public void testGetPathBoolean() {
        assertEquals(RELATIV_1, URI_INFO_1.getPath(true));
        assertEquals(RELATIV_1, URI_INFO_1.getPath(false));
        assertEquals(RELATIV_2_DECODED, URI_INFO_2.getPath(true));
        assertEquals(RELATIV_2, URI_INFO_2.getPath(false));
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getPathSegments()}.
     */
    public void testGetPathSegments() {
        // must be alid for every UriInfo
        assertEquals(URI_INFO_1.getPathSegments(true), URI_INFO_1.getPathSegments());
        assertEquals(URI_INFO_2.getPathSegments(true), URI_INFO_2.getPathSegments());
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getPathSegments(boolean)}.
     */
    public void testGetPathSegmentsBoolean() {
        checkPathSegments(URI_INFO_1.getPathSegments(true), "relativ", 0, "a", 0, "b", 0);
        checkPathSegments(URI_INFO_1.getPathSegments(false), "relativ", 0, "a", 0, "b", 0);

        checkPathSegments(URI_INFO_2.getPathSegments(true), "relativ", 0, " a ", 0, "!b@,", 0);
        checkPathSegments(URI_INFO_2.getPathSegments(false), "relativ", 0, "%20a%20", 0, "!b@%2C", 0);
        
        UriInfo uriInfo = new JaxRsUriInfo(new Reference(BASE_REF, BASE_REF_STR+"abc/def;ghi=jkl;mno=pqr/stu;vwx=yz"));
        List<PathSegment> pathSegments = uriInfo.getPathSegments();
        checkPathSegments(pathSegments, "abc", 0, "def", 2, "stu", 1);

        MultivaluedMap<String, String> templateParameters1 = pathSegments.get(1).getMatrixParameters();
        assertEquals(2, templateParameters1.size());
        checkEntry("jkl", "ghi", templateParameters1);
        checkEntry("pqr", "mno", templateParameters1);

        MultivaluedMap<String, String> templateParameters2 = pathSegments.get(2).getMatrixParameters();
        assertEquals(1, templateParameters2.size());
        checkEntry("yz", "vwx", templateParameters2);
    }

    private void checkEntry(String expectedValue, String key,
            MultivaluedMap<String, String> templateParameters) {
        assertEquals(expectedValue, templateParameters.getFirst(key));
    }

    /**
     * @param pathSegments
     * @param path0
     * @param tpSize0 templatParamaterSize
     * @param path1
     * @param tpSize1
     * @param path2
     * @param tpSize2
     */
    private void checkPathSegments(List<PathSegment> pathSegments,
            String path0, int tpSize0, String path1, int tpSize1, String path2, int tpSize2) {
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
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getRequestUri()}.
     */
    public void testGetRequestUri() throws Exception {
        URI uri1 = URI_INFO_1.getRequestUri();
        assertEquals(new URI("http://localhost/test/relativ/a/b"), uri1);
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getRequestUriBuilder()}.
     */
    public void testEqualsObject() {
        assertTrue(URI_INFO_3.equals(URI_INFO_4));
    }

    public void _testGetRequestUriBuilder() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getAbsolutePath()}.
     */
    public void _testGetAbsolutePath() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getAbsolutePathBuilder()}.
     */
    public void _testGetAbsolutePathBuilder() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getBaseUri()}.
     */
    public void testGetBaseUri() {
        URI baseUri1 = URI_INFO_1.getBaseUri();
        assertEquals(BASE_REF_STR, baseUri1.toString());
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getBaseUriBuilder()}.
     */
    public void _testGetBaseUriBuilder() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getQueryParameters()}.
     */
    public void _testGetQueryParameters() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getQueryParameters(boolean)}.
     */
    public void _testGetQueryParametersBoolean() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.restlet.ext.jaxrs.core.JaxRsUriInfo#getTemplateParameters(boolean)}.
     */
    public void testGetTemplateParametersBoolean() {
        assertEquals(URI_INFO_3.getTemplateParameters(true), URI_INFO_4.getTemplateParameters(true));
        assertEquals(URI_INFO_3.getTemplateParameters(false), URI_INFO_4.getTemplateParameters(false));

        assertEquals(URI_INFO_5.getTemplateParameters(true), URI_INFO_6.getTemplateParameters(true));
        assertEquals(URI_INFO_5.getTemplateParameters(false), URI_INFO_6.getTemplateParameters(false));
}
}