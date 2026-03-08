/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.util.Series;

public class ReferenceTestCase {

    protected final static String DEFAULT_SCHEME = "http";

    protected final static String DEFAULT_SCHEMEPART = "//";

    /**
     * Returns a reference initialized with http://restlet.org.
     *
     * @return Reference instance.
     */
    protected Reference getDefaultReference() {
        final Reference ref = getReference();
        ref.setHostDomain("restlet.org");
        return ref;
    }

    /**
     * Returns a reference with uri == http://
     *
     * @return Reference instance.
     */
    protected Reference getReference() {
        final Reference ref = new Reference();
        ref.setScheme(DEFAULT_SCHEME);
        ref.setSchemeSpecificPart(DEFAULT_SCHEMEPART);
        return ref;
    }

    /**
     * Test addition methods.
     */
    @Test
    void testAdditions() {
        final Reference ref = new Reference("http://restlet.org");
        ref.addQueryParameter("abc", "123");
        assertEquals("http://restlet.org?abc=123", ref.toString());
        ref.addQueryParameter("def", null);
        assertEquals("http://restlet.org?abc=123&def", ref.toString());
        ref.addSegment("root");
        assertEquals("http://restlet.org/root?abc=123&def", ref.toString());
        ref.addSegment("dir");
        assertEquals("http://restlet.org/root/dir?abc=123&def", ref.toString());
    }

    @Test
    void testEmptyRef() {
        Reference reference = new Reference();
        reference.setAuthority("testAuthority"); // must not produce NPE

        reference = new Reference();
        reference.setBaseRef("http://localhost"); // must not produce NPE

        reference = new Reference();
        reference.setFragment("fragment"); // must not produce NPE

        reference = new Reference();
        reference.setHostDomain("localhost"); // must not produce NPE
        assertEquals("localhost", reference.getAuthority());
        reference.setHostPort(4711); // must not produce NPE
        assertEquals("localhost:4711", reference.getAuthority());
        reference.setUserInfo("sdgj:skdfj"); // must not produce NPE
        assertEquals("sdgj:skdfj@localhost:4711", reference.getAuthority());

        reference = new Reference();
        reference.setIdentifier("http://host/abc/wkj"); // must not produce NPE

        reference = new Reference();
        reference.setPath("loc/alhost"); // must not produce NPE

        reference = new Reference();
        reference.setProtocol(Protocol.HTTPS); // must not produce NPE

        reference = new Reference();
        reference.setQuery("a=b&c=&g=1"); // must not produce NPE

        reference = new Reference();
        reference.setRelativePart("http://localhost"); // must not produce NPE

        reference = new Reference();
        reference.setScheme("skjf"); // must not produce NPE

        reference = new Reference();
        reference.setSchemeSpecificPart("host/afjhsd"); // must not produce NPE

        reference = new Reference();
        final List<String> segments = new ArrayList<String>();
        segments.add("skhf");
        segments.add("sgdfg");
        segments.add("xiz");
        reference.setSegments(segments); // must not produce NPE
    }

    @ParameterizedTest
    @CsvSource({
            "http://localhost/abc,/def",
            "http://localhost/abc/,/def",
            "http://localhost/abc?query,/def",
            "http://localhost/abc#fragment,/def",
             "http://localhost/abc?query#fragment,/def",
             "http://localhost/abc#fragment?query,/def",
             "http://localhost#fragment/abc?query,/def",
             "http://localhost?query/abc,/def"
    })
    void testSetPath(String reference, String path) {
        final Reference ref = new Reference(reference);
        ref.setPath(path);
        assertEquals(path, ref.getPath());
    }

    /**
     * Equality tests.
     */
    @Test
    public void testEquals() {
        final Reference ref1 = getDefaultReference();
        final Reference ref2 = getDefaultReference();
        assertEquals(ref1, ref2);
    }

    @Test
    void testGetLastSegment() {
        Reference reference = new Reference("http://hostname");
        assertNull(reference.getLastSegment());

        reference = new Reference("http://hostname/");
        assertNull(reference.getLastSegment());

        reference = new Reference("http://hostname/abc");
        assertEquals("abc", reference.getLastSegment());

        reference = new Reference("http://hostname/abc/");
        assertEquals("abc", reference.getLastSegment());

        reference = new Reference("http://hostname/123/abc/");
        assertEquals("abc", reference.getLastSegment());

        reference = new Reference("http://hostname/123/abc");
        assertEquals("abc", reference.getLastSegment());
    }

    /**
     * Test hostname getting/setting.
     */
    @Test
    void testHostName() {
        final Reference ref = getReference();
        String host = "restlet.org";
        ref.setHostDomain(host);
        assertEquals(host, ref.getHostDomain());
        host = "restlet.org";
        ref.setHostDomain(host);
        assertEquals(host, ref.getHostDomain());
        Reference ref2 = new Reference("http://[::1]:8182");
        assertEquals("[::1]", ref2.getHostDomain());
    }

    @Test
    void testMatrix() {
        final Reference ref1 = new Reference(
                "http://domain.tld/whatever/a=1;b=2;c=4?x=a&y=b");
        final Reference ref2 = new Reference(
                "http://domain.tld/whatever/a=1/foo;b=2;c=4;d?x=a&y=b");
        final Reference ref3 = new Reference(
                "http://domain.tld/whatever/a=1;b=2;c=4/foo?x=a&y=b");

        assertTrue(ref1.hasMatrix());
        assertTrue(ref2.hasMatrix());
        assertFalse(ref3.hasMatrix());

        assertEquals("b=2;c=4", ref1.getMatrix());
        assertEquals("b=2;c=4;d", ref2.getMatrix());

        final Form form1 = ref1.getMatrixAsForm();
        assertEquals("2", form1.getFirstValue("b"));
        assertEquals("4", form1.getFirstValue("c"));

        final Form form2 = ref1.getMatrixAsForm();
        assertEquals("2", form2.getFirstValue("b"));
        assertEquals("4", form2.getFirstValue("c"));
        assertNull(form2.getFirstValue("d"));

        final Form newForm = new Form();
        newForm.add("a", "1");
        newForm.add("b", "2");
        newForm.add("c", "4");
        assertEquals("a=1;b=2;c=4", newForm.getMatrixString());
    }

    @Test
    void testOriginalRef() {
        Reference ref = new Reference("http://localhost/test");
        Series<Header> headers = new Series<>(Header.class);
        headers.add(HeaderConstants.HEADER_X_FORWARDED_PROTO, "HTTPS");
        headers.add(HeaderConstants.HEADER_X_FORWARDED_PORT, "123");

        Reference originalRef = ReferenceUtils.getOriginalRef(ref, headers);
        assertEquals(Protocol.HTTPS, originalRef.getSchemeProtocol());
        assertEquals(123, originalRef.getHostPort());
    }

    /**
     * Test the computation of parent references, for absolute and relative
     * URIs.
     */
    @Test
    void testParentRef() {
        Reference baseRef = new Reference("http://test.com/foo/bar");
        Reference parentRef = baseRef.getParentRef();
        assertEquals("http://test.com/foo/", parentRef.toString());

        baseRef = new Reference("/foo/bar");
        parentRef = baseRef.getParentRef();
        assertEquals("/foo/", parentRef.toString());
    }

    @Nested
    class TestParsing {

        @ParameterizedTest
        @CsvSource({
                "urn:example:animal:ferret:nose,urn,,example:animal:ferret:nose,,",
                "foo://example.com:8042/over/there?name=ferret#nose,foo,example.com:8042,/over/there,name=ferret,nose",
                "mailto:fred@example.com,mailto,,fred@example.com,,",
                "foo://info.example.com?fred,foo,info.example.com,,fred,",
                "http://localhost?query,http,localhost,,query,",
                "http://localhost#?query,http,localhost,,,?query",
                "http://localhost/?query,http,localhost,/,query,",
                "http://localhost/#?query,http,localhost,/,,?query",
                "http://localhost/path#frag/ment,http,localhost,/path,,frag/ment",
                "http://localhost/path?qu/ery,http,localhost,/path,qu/ery,"
        } )
        void testComponentsParsing(String reference,
                                         String scheme, String authority, String path, String query, String fragment) {
            final Reference ref = new Reference(reference);
            assertEquals(scheme, ref.getScheme());
            assertEquals(authority, ref.getAuthority());
            assertEquals(path, ref.getPath());
            assertEquals(query, ref.getQuery());
            assertEquals(fragment, ref.getFragment());
        }

        @ParameterizedTest
        @CsvSource({
                "http://localhost/path#fragment,true,true,http://localhost/path#fragment",
                "http://localhost/path#fragment,true,false,http://localhost/path",
                "http://localhost/path#fragment,false,true,http://localhost/path#fragment",
                "http://localhost/path#fragment,false,false,http://localhost/path",
                "http://localhost/path?query,true,true,http://localhost/path?query",
                "http://localhost/path?query,true,false,http://localhost/path?query",
                "http://localhost/path?query,false,true,http://localhost/path",
                "http://localhost/path?query,false,false,http://localhost/path",
                "http://localhost/path?query#fragment,true,true,http://localhost/path?query#fragment",
                "http://localhost/path?query#fragment,true,false,http://localhost/path?query",
                "http://localhost/path?query#fragment,false,true,http://localhost/path#fragment",
                "http://localhost/path?query#fragment,false,false,http://localhost/path",
                "http://localhost/path#fragment?query,true,true,http://localhost/path#fragment?query",
                "http://localhost/path#fragment?query,true,false,http://localhost/path",
                "http://localhost/path#fragment?query,false,true,http://localhost/path#fragment?query",
                "http://localhost/path#fragment?query,false,false,http://localhost/path"
        } )
        void testParsingOfQueryAndFragment(String reference, boolean query, boolean fragment, String toString) {
            final Reference ref = new Reference(reference);
            assertEquals(ref.toString(query, fragment), toString);
        }

        @Test
        void testGetters() {
            final Reference host = new Reference("http://host.com");
            final Reference slashdir = new Reference(host, "/dir");
            final Reference dir = new Reference(host, "dir");
            final Reference dirslash = new Reference(host, "dir/");
            final Reference fulldir = new Reference("http://host.com/dir");
            final Reference fulldirsub = new Reference(fulldir, "sub");
            final Reference fulldirslashsub = new Reference(fulldir, "/sub");
            final Reference slashdirsub = new Reference(slashdir, "sub");
            final Reference slashdirslashsub = new Reference(slashdir, "/sub");
            final Reference dirslashsub = new Reference(dirslash, "sub");
            final Reference fullsub = new Reference("http://host.com/dir/sub");
            final Reference fullsubQuery = new Reference("http://host.com/dir/sub?query");

            testGetters(host, "http", "host.com", null,
                    "http://host.com", "http://host.com",
                    "http://host.com", null, null);
            testGetters(slashdir, null, null, "/dir",
                    null, "/dir",
                    "http://host.com/dir", null, "/dir");
            testGetters(dir, null, null, "dir",
                    null, "dir",
                    "http://host.com/dir", null, "dir");
            testGetters(dirslash, null, null, "dir/",
                    null, "dir/",
                    "http://host.com/dir/", null, "dir/");
            testGetters(fulldir, "http", "host.com", "/dir",
                    "http://host.com/dir", "http://host.com/dir",
                    "http://host.com/dir", null, null);
            testGetters(fulldirsub, null, null, "sub",
                    null, "sub",
                    "http://host.com/sub", null, "sub");
            testGetters(fulldirslashsub, null, null, "/sub",
                    null, "/sub",
                    "http://host.com/sub", null, "/sub");
            testGetters(slashdirsub, null, null, "sub",
                    null, "sub",
                    "http://host.com/sub", null, "sub");
            testGetters(slashdirslashsub, null, null, "/sub",
                    null, "/sub",
                    "http://host.com/sub", null, "/sub");
            testGetters(dirslashsub, null, null, "sub",
                    null, "sub",
                    "http://host.com/dir/sub", null, "sub");
            testGetters(fullsub, "http", "host.com", "/dir/sub",
                    "http://host.com/dir/sub", "http://host.com/dir/sub",
                    "http://host.com/dir/sub", null, null);
            testGetters(fullsubQuery, "http", "host.com", "/dir/sub",
                    "http://host.com/dir/sub?query", "http://host.com/dir/sub?query",
                    "http://host.com/dir/sub?query", "query", null);
        }

        private void testGetters(Reference reference, String scheme, String authority,
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

        @ParameterizedTest
        @CsvSource({
                "http://a/b/c/d;p?q,g:h,g:h",
                "http://a/b/c/d;p?q,g,http://a/b/c/g",
                "http://a/b/c/d;p?q,./g,http://a/b/c/g",
                "http://a/b/c/d;p?q,g/,http://a/b/c/g/",
                "http://a/b/c/d;p?q,/g,http://a/g",
                "http://a/b/c/d;p?q,//g,http://g",
                "http://a/b/c/d;p?q,?y,http://a/b/c/d;p?y",
                "http://a/b/c/d;p?q,g?y,http://a/b/c/g?y",
                "http://a/b/c/d;p?q,#s,http://a/b/c/d;p?q#s",
                "http://a/b/c/d;p?q,g#s,http://a/b/c/g#s",
                "http://a/b/c/d;p?q,g?y#s,http://a/b/c/g?y#s",
                "http://a/b/c/d;p?q,;x,http://a/b/c/;x",
                "http://a/b/c/d;p?q,g;x,http://a/b/c/g;x",
                "http://a/b/c/d;p?q,g;x?y#s,http://a/b/c/g;x?y#s",
                "http://a/b/c/d;p?q,,http://a/b/c/d;p?q",
                "http://a/b/c/d;p?q,.,http://a/b/c/",
                "http://a/b/c/d;p?q,./,http://a/b/c/",
                "http://a/b/c/d;p?q,..,http://a/b/",
                "http://a/b/c/d;p?q,../,http://a/b/",
                "http://a/b/c/d;p?q,../g,http://a/b/g",
                "http://a/b/c/d;p?q,../..,http://a/",
                "http://a/b/c/d;p?q,../../,http://a/",
                "http://a/b/c/d;p?q,../../g,http://a/g",
                "http://a/b/c/d;p?q,../../../g,http://a/g",
                "http://a/b/c/d;p?q,../../../../g,http://a/g",
                "http://a/b/c/d;p?q,/./g,http://a/g",
                "http://a/b/c/d;p?q,/../g,http://a/g",
                "http://a/b/c/d;p?q,g.,http://a/b/c/g.",
                "http://a/b/c/d;p?q,.g,http://a/b/c/.g",
                "http://a/b/c/d;p?q,g..,http://a/b/c/g..",
                "http://a/b/c/d;p?q,..g,http://a/b/c/..g",
                "http://a/b/c/d;p?q,./../g,http://a/b/g",
                "http://a/b/c/d;p?q,./g/.,http://a/b/c/g/",
                "http://a/b/c/d;p?q,g/./h,http://a/b/c/g/h",
                "http://a/b/c/d;p?q,g/../h,http://a/b/c/h",
                "http://a/b/c/d;p?q,g;x=1/./y,http://a/b/c/g;x=1/y",
                "http://a/b/c/d;p?q,g;x=1/../y,http://a/b/c/y"
        })
        void testResolutionRelativeReference(String baseUri, String relativeUri,
                                                     String expectedAbsoluteUri) {
            final Reference baseRef = new Reference(baseUri);
            final Reference relativeRef = new Reference(baseRef, relativeUri);
            final Reference absoluteRef = relativeRef.getTargetRef();
            assertEquals(expectedAbsoluteUri, absoluteRef.toString());
        }

        @ParameterizedTest
        @CsvSource({
                "http://a/b/c/d;p?q,http://a/b/c/g,g",
                "http://a/b/c/d;p?q,http://a/b/c/g/,g/",
                "http://a/b/c/d;p?q,http://a/b/c/d;p?y,?y",
                "http://a/b/c/d;p?q,http://a/b/c/g?y,g?y",
                "http://a/b/c/d;p?q,http://a/b/c/d;p?q#s,#s",
                "http://a/b/c/d;p?q,http://a/b/c/g#s,g#s",
                "http://a/b/c/d;p?q,http://a/b/c/g?y#s,g?y#s",
                "http://a/b/c/d;p?q,http://a/b/c/;x,;x",
                "http://a/b/c/d;p?q,http://a/b/c/g;x,g;x",
                "http://a/b/c/d;p?q,http://a/b/c/g;x?y#s,g;x?y#s",
                "http://a/b/c/d;p?q,http://a/b/c/,.",
                "http://a/b/c/d;p?q,http://a/b/,..",
                "http://a/b/c/d;p?q,http://a/b/g,../g",
                "http://a/b/c/d;p?q,http://a/,../..",
                "http://a/b/c/d;p?q,http://a/g,../../g",
                "http://a/b/c/g/,http://a/b/c/,..",
                "http://a/b/c/g/,http://a/b/,../.."
        })
        void testRelativizeAbsoluteReference(String baseUri, String absoluteUri,
                                                         String expectedRelativeUri) {
            final Reference baseRef = new Reference(baseUri);
            final Reference absoluteRef = new Reference(absoluteUri);
            final Reference relativeRef = absoluteRef.getRelativeRef(baseRef);
            assertEquals(expectedRelativeUri, relativeRef.toString());
        }
    }

    /**
     * Test port getting/setting.
     */
    @ParameterizedTest
    @ValueSource(ints = {8080, 9090})
    void testPort(int port) {
        Reference ref = getDefaultReference();
        ref.setHostPort(port);
        assertEquals(port, ref.getHostPort());
    }

    @Test
    void testPortIPv6() {
        Reference ref = new Reference("http://[::1]:8182");
        assertEquals(8182, ref.getHostPort());
    }

    @Test
    void testProtocolConstructors() {
        assertEquals("http://restlet.org", new Reference(Protocol.HTTP,
                "restlet.org").toString());
        assertEquals("https://restlet.org:8443", new Reference(Protocol.HTTPS,
                "restlet.org", 8443).toString());

        final Reference ref = new Reference(Protocol.HTTP, "restlet.org");
        ref.addQueryParameter("abc", "123");
        assertEquals("http://restlet.org?abc=123", ref.toString());
    }

    @Test
    void testQuery() {

        Reference ref1 = new Reference(
                "http://localhost/search?q=anythingelse%");
        String query = ref1.getQuery();
        assertEquals("q=anythingelse%25", query);

        Form queryForm = ref1.getQueryAsForm();
        assertEquals("anythingelse%", queryForm.getFirstValue("q"));

        Form extJsQuery = new Form("&_dc=1244741620627&callback=stcCallback1001");
        assertEquals("1244741620627", extJsQuery.getFirstValue("_dc"));
        assertEquals("stcCallback1001", extJsQuery.getFirstValue("callback"));

        Reference ref = new Reference("http://localhost/v1/projects/13404");
        ref.addQueryParameter("dyn", "true");
        assertEquals("http://localhost/v1/projects/13404?dyn=true", ref.toString());
    }

    @Test
    void testQueryWithUri() {
        Reference ref = new Reference(new Reference("http://localhost:8111/"),
                "http://localhost:8111/contrats/123?srvgwt=localhost:9997");
        assertEquals("contrats/123?srvgwt=localhost:9997", ref.getRelativeRef().toString());
    }

    @Test
    void testRiap() {
        Reference baseRef = new Reference("riap://component/exist/db/");
        Reference ref = new Reference(baseRef, "something.xq");
        assertEquals("riap://component/exist/db/something.xq", ref
                .getTargetRef().toString());
    }

    /**
     * Test scheme getting/setting.
     */
    @Test
    void testScheme() {
        final Reference ref = getDefaultReference();
        assertEquals(DEFAULT_SCHEME, ref.getScheme());
        final String scheme = "https";
        ref.setScheme(scheme);
        assertEquals(scheme, ref.getScheme());
        ref.setScheme(DEFAULT_SCHEME);
        assertEquals(DEFAULT_SCHEME, ref.getScheme());
    }

    /**
     * Test scheme specific part getting/setting.
     */
    @Test
    void testSchemeSpecificPart() {
        final Reference ref = getDefaultReference();
        String part = "//restlet.org";
        assertEquals(part, ref.getSchemeSpecificPart());
        part = "//restlet.net";
        ref.setSchemeSpecificPart(part);
        assertEquals(part, ref.getSchemeSpecificPart());
    }

    /**
     * Test setting of the last segment.
     */
    @Test
    void testSetLastSegment() {
        Reference ref = new Reference("http://localhost:1234");
        ref.addSegment("test");
        assertEquals("http://localhost:1234/test", ref.toString());

        ref.setLastSegment("last");
        assertEquals("http://localhost:1234/last", ref.toString());

        ref = new Reference("http://localhost:1234");
        ref.setLastSegment("last");
        assertEquals("http://localhost:1234/last", ref.toString());

        ref.setLastSegment("test");
        assertEquals("http://localhost:1234/test", ref.toString());

        ref.addSegment("last");
        assertEquals("http://localhost:1234/test/last", ref.toString());
    }


    @ParameterizedTest
    @CsvSource({
            "http://localhost:81,//localhost:81",
            "http://localhost:81?query,//localhost:81?query",
            "http://localhost:81?query#fragment,//localhost:81?query",
            "http://localhost:81#fragment,//localhost:81",
            "http://localhost:81/#fragment,//localhost:81/",
            "http://localhost:81/?query,//localhost:81/?query",
            "http://localhost:81/?query=https://perdu.com,//localhost:81/?query=https://perdu.com",
    })
    void testSchemeSpecificPart(final String uri, final String expected) {
        Reference ref = new Reference(uri);
        assertEquals(expected, ref.getSchemeSpecificPart());
    }

    @ParameterizedTest
    @CsvSource({
            "http://localhost:81,localhost:81",
            "http://localhost:81?query,localhost:81",
            "http://localhost:81?query#fragment,localhost:81",
            "http://localhost:81#fragment,localhost:81",
            "http://localhost:81/#fragment,localhost:81",
            "http://localhost:81/?query,localhost:81",
            "http://localhost:81/?query=https://perdu.com,localhost:81",
            "http://localhost:81?query=https://perdu.com,localhost:81",
    })
    void testAuthority(final String uri, final String expected) {
        Reference ref = new Reference(uri);
        assertEquals(expected, ref.getAuthority());
    }

    @ParameterizedTest
    @CsvSource({
            "http://localhost:81,",
            "http://localhost:81/a,/a",
            "http://localhost:81?query,",
            "http://localhost:81/a?query,/a",
            "http://localhost:81?query#fragment,",
            "http://localhost:81#fragment/1234,",
            "http://localhost:81/a#fragment/1234,/a",
            "http://localhost:81/?query,/",
            "http://localhost:81/?query=https://perdu.com/1234,/",
            "http://localhost:81?query=https://perdu.com/1234,"
    })
    void testPath(final String uri, final String expected) {
        Reference ref = new Reference(uri);
        assertEquals(expected, ref.getPath());
    }

    @Test
    void testTargetRef() {
        Reference ref = new Reference(
                "http://twitter.com?status=RT @gamasutra:  Devil May Cry : Born Again http://www.gamasutra.com/view/feature/177267/");
        Reference targetRef = new Reference(
                new Reference(
                        "http://www.gamasutra.com/view/feature/177267/devil_may_cry_born_again.php"),
                ref).getTargetRef();
        assertEquals(
                "http://twitter.com?status=RT%20@gamasutra:%20%20Devil%20May%20Cry%20:%20Born%20Again%20http://www.gamasutra.com/view/feature/177267/",
                targetRef.toString());
    }

    /**
     * Test references that are unequal.
     */
    @Test
    void testUnEquals() {
        final String uri1 = "http://restlet.org/";
        final String uri2 = "http://restlet.net/";
        final Reference ref1 = new Reference(uri1);
        final Reference ref2 = new Reference(uri2);
        assertNotEquals(ref1, ref2);
    }

    @Test
    void testUserinfo() {
        final Reference reference = new Reference("http://localhost:81");
        // This format is deprecated; however, we may prevent failures.
        reference.setUserInfo("login:password");
        assertEquals("login:password@localhost:81", reference.getAuthority());
        assertEquals("localhost", reference.getHostDomain());
        assertEquals(81, reference.getHostPort());
        assertEquals("login:password", reference.getUserInfo());

        reference.setHostDomain("[::1]");
        assertEquals("login:password@[::1]:81", reference.getAuthority());
        assertEquals("[::1]", reference.getHostDomain());
        assertEquals(81, reference.getHostPort());
        assertEquals("login:password", reference.getUserInfo());

        reference.setHostDomain("www.example.com");
        assertEquals("login:password@www.example.com:81", reference.getAuthority());
        assertEquals("www.example.com", reference.getHostDomain());
        assertEquals(81, reference.getHostPort());
        assertEquals("login:password", reference.getUserInfo());

        reference.setHostPort(82);
        assertEquals("login:password@www.example.com:82", reference.getAuthority());
        assertEquals("www.example.com", reference.getHostDomain());
        assertEquals(82, reference.getHostPort());
        assertEquals("login:password", reference.getUserInfo());

        reference.setUserInfo("login");
        assertEquals("login@www.example.com:82", reference.getAuthority());
        assertEquals("www.example.com", reference.getHostDomain());
        assertEquals(82, reference.getHostPort());
        assertEquals("login", reference.getUserInfo());
    }

    @Test
    void testValidity() {
        String uri = "http ://domain.tld/whatever/";
        Reference ref = new Reference(uri);
        assertEquals("http%20://domain.tld/whatever/", ref.toString());

        uri = "file:///C|/wherever\\whatever.swf";
        ref = new Reference(uri);
        assertEquals("file:///C%7C/wherever%5Cwhatever.swf", ref.toString());
    }

    @Nested
    class TestFailures {
        @ParameterizedTest
        @ValueSource(strings = {
                "https://[192.168.0.1]127.0.0.1/",
                "https://[192.168.0.1]vulndetector.com/",
                "https://[normal.com@]vulndetector.com/",
                "https://normal.com[user@vulndetector].com/",
                "https://normal.com[@]vulndetector.com/",
                "https://user:pwd@a[1:2:3:4]/",
                "https://[1:2:3:4:5:6:7:8:9]",
                "https://[1::1::1]",
                "https://[1:2:3:]",
                "https://[ffff::127.0.0.4000]",
                "https://[0:0::vulndetector.com]:80",
                "https://[2001:db8::vulndetector.com]",
                "http://localhost:18:19",
                "http://localhost:18ab"
        })
        void shouldFailWhenParsingIncorrectHosts(String url) {
            final Reference reference = new Reference(url);
            assertThrows(IllegalArgumentException.class, reference::getAuthority);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https>://vulndetector.com/path",
                "https%25://vulndetector.com/path"})
        void shouldFailWhenParsingIncorrectScheme(String url) {
            final Reference reference = new Reference(url);
            assertThrows(IllegalArgumentException.class, reference::getScheme);
        }
    }
}
