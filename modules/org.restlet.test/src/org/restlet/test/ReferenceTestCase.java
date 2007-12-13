/*
 * Copyright 2005-2007 Noelios Consulting.
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
package org.restlet.test;

import java.io.IOException;

import org.restlet.data.Reference;

/**
 * Test {@link org.restlet.data.Reference}.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://www.semagia.com/">Semagia</a>
 */
public class ReferenceTestCase extends RestletTestCase {
    protected final static String DEFAULT_SCHEME = "http";

    protected final static String DEFAULT_SCHEMEPART = "//";

    /**
     * Tests the URI parsing.
     */
    public void testParsing() throws IOException {
        String base = "http://a/b/c/d;p?q";

        String uri01 = "g:h";
        String uri02 = "g";
        String uri03 = "./g";
        String uri04 = "g/";
        String uri05 = "/g";
        String uri06 = "//g";
        String uri07 = "?y";
        String uri08 = "g?y";
        String uri09 = "#s";
        String uri10 = "g#s";
        String uri11 = "g?y#s";
        String uri12 = ";x";
        String uri13 = "g;x";
        String uri14 = "g;x?y#s";
        String uri15 = "";
        String uri16 = ".";
        String uri17 = "./";
        String uri18 = "..";
        String uri19 = "../";
        String uri20 = "../g";
        String uri21 = "../..";
        String uri22 = "../../";
        String uri23 = "../../g";
        String uri24 = "../../../g";
        String uri25 = "../../../../g";
        String uri26 = "/./g";
        String uri27 = "/../g";
        String uri28 = "g.";
        String uri29 = ".g";
        String uri30 = "g..";
        String uri31 = "..g";
        String uri32 = "./../g";
        String uri33 = "./g/.";
        String uri34 = "g/./h";
        String uri35 = "g/../h";
        String uri36 = "g;x=1/./y";
        String uri37 = "g;x=1/../y";

        String uri101 = "g:h";
        String uri102 = "http://a/b/c/g";
        String uri103 = "http://a/b/c/g";
        String uri104 = "http://a/b/c/g/";
        String uri105 = "http://a/g";
        String uri106 = "http://g";
        String uri107 = "http://a/b/c/d;p?y";
        String uri108 = "http://a/b/c/g?y";
        String uri109 = "http://a/b/c/d;p?q#s";
        String uri110 = "http://a/b/c/g#s";
        String uri111 = "http://a/b/c/g?y#s";
        String uri112 = "http://a/b/c/;x";
        String uri113 = "http://a/b/c/g;x";
        String uri114 = "http://a/b/c/g;x?y#s";
        String uri115 = "http://a/b/c/d;p?q";
        String uri116 = "http://a/b/c/";
        String uri117 = "http://a/b/c/";
        String uri118 = "http://a/b/";
        String uri119 = "http://a/b/";
        String uri120 = "http://a/b/g";
        String uri121 = "http://a/";
        String uri122 = "http://a/";
        String uri123 = "http://a/g";
        String uri124 = "http://a/g";
        String uri125 = "http://a/g";
        String uri126 = "http://a/g";
        String uri127 = "http://a/g";
        String uri128 = "http://a/b/c/g.";
        String uri129 = "http://a/b/c/.g";
        String uri130 = "http://a/b/c/g..";
        String uri131 = "http://a/b/c/..g";
        String uri132 = "http://a/b/g";
        String uri133 = "http://a/b/c/g/";
        String uri134 = "http://a/b/c/g/h";
        String uri135 = "http://a/b/c/h";
        String uri136 = "http://a/b/c/g;x=1/y";
        String uri137 = "http://a/b/c/y";

        Reference host = new Reference("http://host.com");
        Reference slashdir = new Reference(host, "/dir");
        Reference dir = new Reference(host, "dir");
        Reference dirslash = new Reference(host, "dir/");
        Reference fulldir = new Reference("http://host.com/dir");
        Reference fulldirsub = new Reference(fulldir, "sub");
        Reference fulldirslashsub = new Reference(fulldir, "/sub");
        Reference slashdirsub = new Reference(slashdir, "sub");
        Reference slashdirslashsub = new Reference(slashdir, "/sub");
        Reference dirslashsub = new Reference(dirslash, "sub");
        Reference fullsub = new Reference("http://host.com/dir/sub");

        // Test the parsing of references into its components
        testRef0("foo://example.com:8042/over/there?name=ferret#nose", "foo",
                "example.com:8042", "/over/there", "name=ferret", "nose");
        testRef0("urn:example:animal:ferret:nose", "urn", null,
                "example:animal:ferret:nose", null, null);
        testRef0("mailto:fred@example.com", "mailto", null, "fred@example.com",
                null, null);
        testRef0("foo://info.example.com?fred", "foo", "info.example.com",
                null, "fred", null);
        testRef0("*", null, null, "*", null, null);
        testRef0("http://localhost?query", "http", "localhost", null, "query",
                null);
        testRef0("http://localhost#?query", "http", "localhost", null, null,
                "?query");
        testRef0("http://localhost/?query", "http", "localhost", "/", "query",
                null);
        testRef0("http://localhost/#?query", "http", "localhost", "/", null,
                "?query");
        testRef0("http://localhost/path#frag/ment", "http", "localhost",
                "/path", null, "frag/ment");
        testRef0("http://localhost/path?qu/ery", "http", "localhost", "/path",
                "qu/ery", null);

        // Test the resolution of relative references
        testRef1(base, uri01, uri101);
        testRef1(base, uri02, uri102);
        testRef1(base, uri03, uri103);
        testRef1(base, uri04, uri104);
        testRef1(base, uri05, uri105);
        testRef1(base, uri06, uri106);
        testRef1(base, uri07, uri107);
        testRef1(base, uri08, uri108);
        testRef1(base, uri09, uri109);
        testRef1(base, uri10, uri110);
        testRef1(base, uri11, uri111);
        testRef1(base, uri12, uri112);
        testRef1(base, uri13, uri113);
        testRef1(base, uri14, uri114);
        testRef1(base, uri15, uri115);
        testRef1(base, uri16, uri116);
        testRef1(base, uri17, uri117);
        testRef1(base, uri18, uri118);
        testRef1(base, uri19, uri119);
        testRef1(base, uri20, uri120);
        testRef1(base, uri21, uri121);
        testRef1(base, uri22, uri122);
        testRef1(base, uri23, uri123);
        testRef1(base, uri24, uri124);
        testRef1(base, uri25, uri125);
        testRef1(base, uri26, uri126);
        testRef1(base, uri27, uri127);
        testRef1(base, uri28, uri128);
        testRef1(base, uri29, uri129);
        testRef1(base, uri30, uri130);
        testRef1(base, uri31, uri131);
        testRef1(base, uri32, uri132);
        testRef1(base, uri33, uri133);
        testRef1(base, uri34, uri134);
        testRef1(base, uri35, uri135);
        testRef1(base, uri36, uri136);
        testRef1(base, uri37, uri137);

        // Test the relativization of absolute references
        testRef2(base, uri102, uri02);
        testRef2(base, uri104, uri04);
        testRef2(base, uri107, uri07);
        testRef2(base, uri108, uri08);
        testRef2(base, uri109, uri09);
        testRef2(base, uri110, uri10);
        testRef2(base, uri111, uri11);
        testRef2(base, uri112, uri12);
        testRef2(base, uri113, uri13);
        testRef2(base, uri114, uri14);
        testRef2(base, uri116, uri16);
        testRef2(base, uri118, uri18);
        testRef2(base, uri120, uri20);
        testRef2(base, uri121, uri21);
        testRef2(base, uri123, uri23);

        // Test the toString method with or without query/fragment
        testRef3("http://localhost/path#fragment", true, true,
                "http://localhost/path#fragment");
        testRef3("http://localhost/path#fragment", true, false,
                "http://localhost/path");
        testRef3("http://localhost/path#fragment", false, true,
                "http://localhost/path#fragment");
        testRef3("http://localhost/path#fragment", false, false,
                "http://localhost/path");

        testRef3("http://localhost/path?query", true, true,
                "http://localhost/path?query");
        testRef3("http://localhost/path?query", true, false,
                "http://localhost/path?query");
        testRef3("http://localhost/path?query", false, true,
                "http://localhost/path");
        testRef3("http://localhost/path?query", false, false,
                "http://localhost/path");

        testRef3("http://localhost/path?query#fragment", true, true,
                "http://localhost/path?query#fragment");
        testRef3("http://localhost/path?query#fragment", true, false,
                "http://localhost/path?query");
        testRef3("http://localhost/path?query#fragment", false, true,
                "http://localhost/path#fragment");
        testRef3("http://localhost/path?query#fragment", false, false,
                "http://localhost/path");

        testRef3("http://localhost/path#fragment?query", true, true,
                "http://localhost/path#fragment?query");
        testRef3("http://localhost/path#fragment?query", true, false,
                "http://localhost/path");
        testRef3("http://localhost/path#fragment?query", false, true,
                "http://localhost/path#fragment?query");
        testRef3("http://localhost/path#fragment?query", false, false,
                "http://localhost/path");

        testRef4(host, "http", "host.com", null, "http://host.com",
                "http://host.com", "http://host.com", null, null);
        testRef4(slashdir, null, null, "/dir", null, "/dir",
                "http://host.com/dir", null, "/dir");
        testRef4(dir, null, null, "dir", null, "dir", "http://host.com/dir",
                null, "dir");
        testRef4(dirslash, null, null, "dir/", null, "dir/",
                "http://host.com/dir/", null, "dir/");
        testRef4(fulldir, "http", "host.com", "/dir", "http://host.com/dir",
                "http://host.com/dir", "http://host.com/dir", null, null);

        testRef4(fulldirsub, null, null, "sub", null, "sub",
                "http://host.com/sub", null, "sub");
        testRef4(fulldirslashsub, null, null, "/sub", null, "/sub",
                "http://host.com/sub", null, "/sub");
        testRef4(slashdirsub, null, null, "sub", null, "sub",
                "http://host.com/sub", null, "sub");
        testRef4(slashdirslashsub, null, null, "/sub", null, "/sub",
                "http://host.com/sub", null, "/sub");
        testRef4(dirslashsub, null, null, "sub", null, "sub",
                "http://host.com/dir/sub", null, "sub");
        testRef4(fullsub, "http", "host.com", "/dir/sub",
                "http://host.com/dir/sub", "http://host.com/dir/sub",
                "http://host.com/dir/sub", null, null);
    }

    /**
     * Tests the parsing of a reference into its components
     * 
     * @param reference
     * @param scheme
     * @param authority
     * @param path
     * @param query
     * @param fragment
     */
    private void testRef0(String reference, String scheme, String authority,
            String path, String query, String fragment) {
        Reference ref = new Reference(reference);
        assertEquals(scheme, ref.getScheme());
        assertEquals(authority, ref.getAuthority());
        assertEquals(path, ref.getPath());
        assertEquals(query, ref.getQuery());
        assertEquals(fragment, ref.getFragment());
    }

    /**
     * Test the resolution of relative references.
     * 
     * @param baseUri
     * @param relativeUri
     * @param expectedAbsoluteUri
     */
    private void testRef1(String baseUri, String relativeUri,
            String expectedAbsoluteUri) {
        Reference baseRef = new Reference(baseUri);
        Reference relativeRef = new Reference(baseRef, relativeUri);
        Reference absoluteRef = relativeRef.getTargetRef();
        assertEquals(expectedAbsoluteUri, absoluteRef.toString());
    }

    /**
     * Test the relativization of absolute references
     * 
     * @param baseUri
     * @param absoluteUri
     * @param expectedRelativeUri
     */
    private void testRef2(String baseUri, String absoluteUri,
            String expectedRelativeUri) {
        Reference baseRef = new Reference(baseUri);
        Reference absoluteRef = new Reference(absoluteUri);
        Reference relativeRef = absoluteRef.getRelativeRef(baseRef);
        assertEquals(expectedRelativeUri, relativeRef.toString());
    }

    /**
     * Test the toString method with or without query/fragment
     * 
     * @param reference
     * @param query
     * @param fragment
     * @param toString
     */
    private void testRef3(String reference, boolean query, boolean fragment,
            String toString) {
        Reference ref = new Reference(reference);
        assertEquals(ref.toString(query, fragment), toString);
    }

    /**
     * Test the behaviour of several getters upon a Reference object.
     * 
     * @param reference
     * @param query
     * @param fragment
     * @param toString
     */
    private void testRef4(Reference reference, String scheme, String authority,
            String path, String remainingPart, String toString,
            String targetRef, String query, String relativePart) {
        assertEquals(reference.getScheme(), scheme);
        assertEquals(reference.getAuthority(), authority);
        assertEquals(reference.getPath(), path);
        assertEquals(reference.getRemainingPart(), remainingPart);
        assertEquals(reference.toString(), toString);
        assertEquals(reference.getTargetRef().toString(), targetRef);
        assertEquals(reference.getQuery(), query);
        assertEquals(reference.getRelativePart(), relativePart);
    }

    /**
     * Returns a reference with uri == http://
     * 
     * @return Reference instance.
     */
    protected Reference getReference() {
        Reference ref = new Reference();
        ref.setScheme(DEFAULT_SCHEME);
        ref.setSchemeSpecificPart(DEFAULT_SCHEMEPART);
        return ref;
    }

    /**
     * Returns a reference that is initialized with http://www.restlet.org.
     * 
     * @return Reference instance.
     */
    protected Reference getDefaultReference() {
        Reference ref = getReference();
        ref.setHostDomain("www.restlet.org");
        return ref;
    }

    /**
     * Equality tests.
     */
    public void testEquals() throws Exception {
        Reference ref1 = getDefaultReference();
        Reference ref2 = getDefaultReference();
        assertTrue(ref1.equals(ref2));
        assertEquals(ref1, ref2);
    }

    /**
     * Test references that are unequal.
     */
    public void testUnEquals() throws Exception {
        String uri1 = "http://www.restlet.org/";
        String uri2 = "http://www.restlet.net/";
        Reference ref1 = new Reference(uri1);
        Reference ref2 = new Reference(uri2);
        assertFalse(ref1.equals(ref2));
        assertFalse(ref1.equals(null));
    }

    /**
     * Test hostname getting/setting.
     */
    public void testHostName() throws Exception {
        Reference ref = getReference();
        String host = "www.restlet.org";
        ref.setHostDomain(host);
        assertEquals(host, ref.getHostDomain());
        host = "restlet.org";
        ref.setHostDomain(host);
        assertEquals(host, ref.getHostDomain());
    }

    /**
     * Test port getting/setting.
     */
    public void testPort() throws Exception {
        Reference ref = getDefaultReference();
        int port = 8080;
        ref.setHostPort(port);
        assertEquals(port, ref.getHostPort());
        port = 9090;
        ref.setHostPort(port);
        assertEquals(port, ref.getHostPort());
    }

    /**
     * Test scheme getting/setting.
     */
    public void testScheme() throws Exception {
        Reference ref = getDefaultReference();
        assertEquals(DEFAULT_SCHEME, ref.getScheme());
        String scheme = "https";
        ref.setScheme(scheme);
        assertEquals(scheme, ref.getScheme());
        ref.setScheme(DEFAULT_SCHEME);
        assertEquals(DEFAULT_SCHEME, ref.getScheme());
    }

    /**
     * Test scheme specific part getting/setting.
     */
    public void testSchemeSpecificPart() throws Exception {
        Reference ref = getDefaultReference();
        String part = "//www.restlet.org";
        assertEquals(part, ref.getSchemeSpecificPart());
        part = "//www.restlet.net";
        ref.setSchemeSpecificPart(part);
        assertEquals(part, ref.getSchemeSpecificPart());
    }

    /**
     * Test addition methods.
     */
    public void testAdditions() throws Exception {
        Reference ref = new Reference("http://www.restlet.org");
        ref.addQueryParameter("abc", "123");
        assertEquals("http://www.restlet.org?abc=123", ref.toString());
        ref.addQueryParameter("def", null);
        assertEquals("http://www.restlet.org?abc=123&def", ref.toString());
        ref.addSegment("root");
        assertEquals("http://www.restlet.org/root?abc=123&def", ref.toString());
        ref.addSegment("dir");
        assertEquals("http://www.restlet.org/root/dir?abc=123&def", ref
                .toString());
    }
}
