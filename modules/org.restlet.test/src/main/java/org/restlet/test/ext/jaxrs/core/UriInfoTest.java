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

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import junit.framework.TestCase;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 * @see ThreadLocalizedUriInfo
 * @see UriInfo
 */
@SuppressWarnings("all")
public class UriInfoTest extends TestCase {

    private static final String BASE_REF_STR = "http://localhost/test/";

    private static final Reference BASE_REF = new Reference(BASE_REF_STR);

    private static final String RELATIV_1 = "relativ/a/b";

    private static final String RELATIV_2 = "relativ/%20a%20/%21b%40%2C";

    private static final String RELATIV_2_DECODED = "relativ/ a /!b@,";

    private static final Reference REFERENCE_1 = new Reference(BASE_REF,
            BASE_REF_STR + RELATIV_1);

    private static final Reference REFERENCE_2 = new Reference(BASE_REF,
            BASE_REF_STR + RELATIV_2);

    protected static void assertUnmodifiable(
            MultivaluedMap<String, String> multivaluedMap) {
        try {
            multivaluedMap.add("jh,", "hkj");
        } catch (NotYetImplementedException usoe) {
            throw usoe;
        } catch (UnsupportedOperationException usoe) {
            // must be thrown, because it should be unmodifiable
        }
    };

    private static final UriInfo createUriInfo1() {
        return newUriInfo(REFERENCE_1, BASE_REF);
    };

    private static final UriInfo createUriInfo2() {
        return newUriInfo(REFERENCE_2, BASE_REF);
    };

    private static final ThreadLocalizedUriInfo createUriInfo5() {
        return newUriInfo(new Reference(BASE_REF, BASE_REF_STR
                + "hfk;abc=%20def;ghi=jkl"), BASE_REF);
    };

    private static final ThreadLocalizedUriInfo createUriInfo7() {
        return newUriInfo(new Reference(BASE_REF, BASE_REF_STR
                + "abc?def=123&ghi=456"), BASE_REF);
    };

    private static final ThreadLocalizedUriInfo createUriInfo8() {
        return newUriInfo(new Reference(BASE_REF, BASE_REF_STR
                + "abc?def=1+23&gh%20i=45%206"), BASE_REF);
    }

    /**
     * creates a new UriInfo object.
     * <p>
     * <b>You could only use one of these UriInfos at the same time !!!</b>
     * 
     * @param resourceRef
     * @param rootRef
     * @return
     */
    static ThreadLocalizedUriInfo newUriInfo(Reference resourceRef,
            Reference rootRef) {
        Request request = new Request();
        request.setResourceRef(resourceRef);
        request.setOriginalRef(resourceRef);
        request.setRootRef(rootRef);
        Response response = new Response(request);
        Response.setCurrent(response);
        CallContext callContext = new CallContext(request, response);
        ThreadLocalizedContext tlContext = new ThreadLocalizedContext();
        tlContext.set(callContext);
        return new ThreadLocalizedUriInfo(tlContext);
    }

    // the Template parameters are tested in SimpleTrain and SimpleTrainTest

    private void checkEntry(String expectedValue, String key,
            MultivaluedMap<String, String> templateParameters) {
        assertEquals(expectedValue, templateParameters.getFirst(key));
    }

    /**
     * @param pathSegments
     * @param path0
     * @param tpSize0
     *            templatParamaterSize
     * @param path1
     * @param tpSize1
     * @param path2
     * @param tpSize2
     */
    private void checkPathSegments(List<PathSegment> pathSegments,
            String path0, int tpSize0, String path1, int tpSize1, String path2,
            int tpSize2) {
        assertEquals(3, pathSegments.size());
        final PathSegment pathSegment0 = pathSegments.get(0);
        final PathSegment pathSegment1 = pathSegments.get(1);
        final PathSegment pathSegment2 = pathSegments.get(2);
        assertEquals(path0, pathSegment0.getPath());
        assertEquals(tpSize0, pathSegment0.getMatrixParameters().size());
        assertEquals(path1, pathSegment1.getPath());
        assertEquals(tpSize1, pathSegment1.getMatrixParameters().size());
        assertEquals(path2, pathSegment2.getPath());
        assertEquals(tpSize2, pathSegment2.getMatrixParameters().size());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getAbsolutePath()}
     * .
     */
    public void testGetAbsolutePath() throws Exception {
        UriBuilderImplTest.assertEqualsURI(BASE_REF_STR + RELATIV_1,
                createUriInfo1().getAbsolutePath());
        UriBuilderImplTest.assertEqualsURI(BASE_REF_STR + RELATIV_2,
                createUriInfo2().getAbsolutePath());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getAbsolutePathBuilder()}
     * .
     */
    public void testGetAbsolutePathBuilder() throws Exception {
        UriBuilderImplTest.assertEqualsURI(BASE_REF_STR + RELATIV_1,
                createUriInfo1().getAbsolutePathBuilder());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getBaseUri()}
     * .
     */
    public void testGetBaseUri() {
        final URI baseUri1 = createUriInfo1().getBaseUri();
        assertEquals(BASE_REF_STR, baseUri1.toString());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getBaseUriBuilder()}
     * .
     */
    public void testGetBaseUriBuilder() throws Exception {
        final URI uri = createUriInfo1().getBaseUri();
        UriBuilderImplTest.assertEqualsURI(BASE_REF_STR, uri);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getPath()}
     * .
     */
    public void testGetPath() {
        assertEquals(RELATIV_1, createUriInfo1().getPath());
        assertEquals(RELATIV_2_DECODED, createUriInfo2().getPath());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getPath(boolean)}
     * .
     */
    public void testGetPathBoolean() {
        assertEquals(RELATIV_1, createUriInfo1().getPath(true));
        assertEquals(RELATIV_1, createUriInfo1().getPath(false));
        assertEquals(RELATIV_2_DECODED, createUriInfo2().getPath(true));
        assertEquals(RELATIV_2, createUriInfo2().getPath(false));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getPathSegments()}
     * .
     */
    public void testGetPathSegments() {
        // must be alid for every UriInfo
        assertEquals(createUriInfo1().getPathSegments(true), createUriInfo1()
                .getPathSegments());
        assertEquals(createUriInfo2().getPathSegments(true), createUriInfo2()
                .getPathSegments());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getPathSegments(boolean)}
     * .
     */
    public void testGetPathSegmentsBoolean() {
        checkPathSegments(createUriInfo1().getPathSegments(true), "relativ", 0,
                "a", 0, "b", 0);
        checkPathSegments(createUriInfo1().getPathSegments(false), "relativ",
                0, "a", 0, "b", 0);

        checkPathSegments(createUriInfo2().getPathSegments(true), "relativ", 0,
                " a ", 0, "!b@,", 0);
        checkPathSegments(createUriInfo2().getPathSegments(false), "relativ",
                0, "%20a%20", 0, "%21b%40%2C", 0);

        final UriInfo uriInfo = newUriInfo(new Reference(BASE_REF, BASE_REF_STR
                + "abc/def;ghi=jkl;mno=pqr/stu;vwx=yz"), BASE_REF);
        final List<PathSegment> pathSegments = uriInfo.getPathSegments();
        checkPathSegments(pathSegments, "abc", 0, "def", 2, "stu", 1);

        final MultivaluedMap<String, String> templateParameters1 = pathSegments
                .get(1).getMatrixParameters();
        assertEquals(2, templateParameters1.size());
        checkEntry("jkl", "ghi", templateParameters1);
        checkEntry("pqr", "mno", templateParameters1);

        final MultivaluedMap<String, String> templateParameters2 = pathSegments
                .get(2).getMatrixParameters();
        assertEquals(1, templateParameters2.size());
        checkEntry("yz", "vwx", templateParameters2);
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getQueryParameters(boolean)}
     * .
     */
    public void testGetQueryParametersDecoded() {
        assertEquals("123",
                createUriInfo7().getQueryParameters(true).getFirst("def"));
        assertEquals("456",
                createUriInfo7().getQueryParameters(true).getFirst("ghi"));
        assertEquals(2, createUriInfo7().getQueryParameters(true).size());

        assertEquals("1 23", createUriInfo8().getQueryParameters(true)
                .getFirst("def"));
        assertEquals("45 6", createUriInfo8().getQueryParameters(true)
                .getFirst("gh i"));
        assertEquals(2, createUriInfo8().getQueryParameters(true).size());
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getQueryParameters()}
     * .
     */
    public void testGetQueryParametersEncoded() {
        assertEquals("123", createUriInfo7().getQueryParameters(false)
                .getFirst("def"));
        assertEquals("456", createUriInfo7().getQueryParameters(false)
                .getFirst("ghi"));
        assertEquals(2, createUriInfo7().getQueryParameters(false).size());

        assertEquals("1+23", createUriInfo8().getQueryParameters(false)
                .getFirst("def"));
        assertEquals("45%206", createUriInfo8().getQueryParameters(false)
                .getFirst("gh%20i"));
        assertEquals(2, createUriInfo8().getQueryParameters(false).size());
    }

    public void testGetQueryParametersUnmodifiable() {
        assertUnmodifiable(createUriInfo5().getQueryParameters(true));
        assertUnmodifiable(createUriInfo5().getQueryParameters(false));
    }

    /**
     * Test method for
     * {@link org.restlet.ext.jaxrs.internal.core.ThreadLocalizedUriInfo#getRequestUri()}
     * .
     */
    public void testGetRequestUri() throws Exception {
        final URI uri1 = createUriInfo1().getRequestUri();
        assertEquals(new URI("http://localhost/test/relativ/a/b"), uri1);
    }

    public void testGetRequestUriBuilder() throws Exception {
        final UriBuilder uriBuilder1 = createUriInfo1().getRequestUriBuilder();
        UriBuilderImplTest.assertEqualsURI("http://localhost/test/relativ/a/b",
                uriBuilder1);
    }
}
