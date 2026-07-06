/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

/** Tests cases for the tunnel filter. */
class TunnelFilterTestCase {

    /** . */
    private static final String EFFECTED = "https://example.com/adf.asdf/af.html";

    /** . */
    private static final String QUERY = "https://example.com/?start=2013-11-26T03%3A45%2B1300";

    /** . */
    private static final String QUERY_PREF =
            "https://example.com/?start=2013-11-26T03%3A45%2B1300&media=txt";

    /** . */
    private static final String START_REF_FOR_PATH_TEST = "https://www.example.com/abc/def/";

    /** . */
    private static final String UNEFFECTED = "https://example.com/abc.def/af.ab";

    private List<Preference<CharacterSet>> accCharsets;

    private List<Preference<Encoding>> accEncodings;

    private List<Preference<Language>> accLanguages;

    private List<Preference<MediaType>> accMediaTypes;

    private String lastCreatedReference;

    private Request request;

    private Response response;

    private TunnelFilter tunnelFilter;

    private String userAgent;

    void assertCharSets(CharacterSet... characterSets) {
        assertEqualSet(this.accCharsets, characterSets);
    }

    void assertEncodings(Encoding... encodings) {
        assertEqualSet(this.accEncodings, encodings);
    }

    @SafeVarargs
    final <A extends Metadata> void assertEqualSet(
            List<? extends Preference<A>> actual, A... expected) {
        if (actual.size() != expected.length) {
            System.out.println("Is:     " + actual);
            System.out.println("Should: " + Arrays.asList(expected));
        }
        assertEquals(actual.size(), expected.length);
        boolean contained = false;
        for (final Metadata exp : expected) {
            for (final Preference<? extends Metadata> act : actual) {
                if (exp.equals(act.getMetadata())) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                final String message = exp + " should be in, but is missing in " + actual;
                fail(message);
            }
        }
    }

    void assertLanguages(Language... languages) {
        assertEqualSet(this.accLanguages, languages);
    }

    void assertMediaTypes(MediaType... mediaTypes) {
        assertEqualSet(this.accMediaTypes, mediaTypes);
    }

    void assertMethod(Method method) {
        assertEquals(this.request.getMethod(), method);
    }

    void assertNotSameMethod(Method method) {
        assertNotSame(this.request.getMethod(), method);
    }

    private void check(String expectedCut, String expectedExtensions) {
        final Reference resourceRef = this.request.getResourceRef();
        assertEquals(expectedCut, resourceRef.toString());

        final Reference originalRef = this.request.getOriginalRef();
        assertEquals(originalRef, new Reference(this.lastCreatedReference));
        assertEquals(expectedCut, resourceRef.toString());
        assertEquals(expectedExtensions, resourceRef.getExtensions());
    }

    /**
     * @param expectedSubPathCut if null, the same as subPathOrig
     * @param expectedExtension if null, then the same as "" for this test
     */
    private void checkFromPath(String expectedSubPathCut, String expectedExtension) {
        if (expectedSubPathCut == null) {
            check(this.lastCreatedReference, expectedExtension);
        } else {
            check(START_REF_FOR_PATH_TEST + expectedSubPathCut, expectedExtension);
        }
    }

    /**
     * @see #createGetFromPath(String)
     * @see #createRequest(Method, String)
     */
    void createGet(String reference) {
        createRequest(Method.GET, reference);
    }

    /**
     * @see #createGet(String)
     * @see #createRequest(Method, String)
     */
    private void createGetFromPath(String subPathToCheck) {
        createGet(START_REF_FOR_PATH_TEST + subPathToCheck);
    }

    /** */
    void createPost(String reference) {
        createRequest(Method.POST, reference);
    }

    /**
     * Creates a {@link Request} and put it into {@link #request}.<br>
     * To use the methods provided by the test case class, use ever the provided create methods to
     * create a request.
     *
     * @see #createPost(String)
     * @see #createGet(String)
     * @see #createGetFromPath(String)
     */
    private void createRequest(Method method, String reference) {
        this.request = new Request(method, reference);
        this.request.setOriginalRef(new Reference(reference));
        this.response = new Response(this.request);
        this.lastCreatedReference = reference;
        setPrefs();
        this.request.getClientInfo().setAgent(this.userAgent);
    }

    private void extensionTunnelOff() {
        final Application application = this.tunnelFilter.getApplication();
        application.getTunnelService().setExtensionsTunnel(false);
    }

    /** Call this method to filter the current request */
    private void filter() {
        this.tunnelFilter.beforeHandle(this.request, this.response);
        setPrefs();
    }

    private void setPrefs() {
        this.accMediaTypes = this.request.getClientInfo().getAcceptedMediaTypes();
        this.accLanguages = this.request.getClientInfo().getAcceptedLanguages();
        this.accCharsets = this.request.getClientInfo().getAcceptedCharacterSets();
        this.accEncodings = this.request.getClientInfo().getAcceptedEncodings();
    }

    @BeforeEach
    void setUpEach() {
        Application app = new Application(new Context());
        Application.setCurrent(app);
        this.tunnelFilter = new TunnelFilter(app.getContext());
        this.tunnelFilter.getApplication().getTunnelService().setExtensionsTunnel(true);
    }

    @AfterEach
    void tearDownEach() {
        this.tunnelFilter = null;
        this.request = null;
        this.response = null;
    }

    @ParameterizedTest
    @ValueSource(strings = {EFFECTED, UNEFFECTED})
    void testExtensionsMappingOff(String reference) {
        extensionTunnelOff();
        createGet(reference);
        this.accLanguages.add(new Preference<>(Language.valueOf("ajh")));
        this.accMediaTypes.add(new Preference<>(MediaType.APPLICATION_STUFFIT));
        filter();
        assertEquals(reference, this.request.getResourceRef().toString());
        assertLanguages(Language.valueOf("ajh"));
        assertMediaTypes(MediaType.APPLICATION_STUFFIT);
        assertCharSets();
        assertEncodings();
    }

    @Test
    void shouldDetectExtensionWithoutMediaType() {
        createGet(UNEFFECTED);
        filter();
        check(UNEFFECTED, "ab");
        assertLanguages();
        assertCharSets();
        assertMediaTypes();
    }

    @Test
    void shouldDetectMediaType() {
        createGet(EFFECTED);
        filter();
        check("https://example.com/adf.asdf/af", null);
        assertMediaTypes(MediaType.TEXT_HTML);
        assertLanguages();
        assertCharSets();
    }

    @Test
    void shouldCutPathAndExtension() {
        createGetFromPath("afhhh");
        filter();
        checkFromPath(null, null);
        assertEqualSet(this.accMediaTypes);
        assertLanguages();
        assertEncodings();
        assertCharSets();
    }

    @Test
    void shouldCutPathAndDetectExtension() {
        createGetFromPath("hksf.afsdf");
        filter();
        checkFromPath(null, "afsdf");
        assertMediaTypes();
        assertLanguages();
        assertEncodings();
        assertCharSets();
    }

    @Test
    void shouldKeepPathAndDetectExtensionAndMediaType() {
        createGetFromPath("hksf.afsdf.html");
        filter();
        checkFromPath("hksf.afsdf", "afsdf");
        assertMediaTypes(MediaType.TEXT_HTML);
        assertLanguages();
        assertEncodings();
        assertCharSets();
    }

    @ParameterizedTest
    @CsvSource({
        "hksf.afsdf.html.txt,hksf.afsdf.html,afsdf.html",
        "hksf.html.afsdf.txt,hksf.html.afsdf,html.afsdf"
    })
    void shouldKeepPathAndDetectExtensionAndKeepLastMediaType(
            String path, String expectedSubPathCut, String expectedExtension) {
        createGetFromPath(path);
        filter();
        checkFromPath(expectedSubPathCut, expectedExtension);
        assertMediaTypes(MediaType.TEXT_PLAIN);
        assertLanguages();
        assertEncodings();
        assertCharSets();
    }

    @Test
    void shouldKeepPathAndDetectExtensionAndKeepLastMediaTypeAndLastLanguage() {
        createGetFromPath("hksf.html.afsdf.txt.en.fr");
        filter();
        checkFromPath("hksf.html.afsdf.txt.en", "html.afsdf.txt.en");
        // Take care about the fact that only one extension per metadata "type"
        // is allowed: ie only one Language, one encoding, one media type, etc.
        assertMediaTypes();
        assertLanguages(Language.FRENCH);
        assertEncodings();
        assertCharSets();

        createGetFromPath("hksf.html.afsdf.txt.en");
        filter();
        checkFromPath("hksf.html.afsdf", "html.afsdf");
        assertMediaTypes(MediaType.TEXT_PLAIN);
        assertLanguages(Language.ENGLISH);
        assertEncodings();
        assertCharSets();
    }

    @Test
    void shouldDetectNoExtension() {
        createGet(START_REF_FOR_PATH_TEST);
        filter();
        checkFromPath(null, null);
        assertMediaTypes();
        assertLanguages();
        assertEncodings();
        assertCharSets();
    }

    @Test
    void testMethodTunnelingViaHeader() {
        tunnelFilter.getTunnelService().setMethodTunnel(true);
        Map<String, Object> attributesHeader = new HashMap<>();
        Series<Header> headers = new Series<>(Header.class);
        headers.add(HeaderConstants.HEADER_X_HTTP_METHOD_OVERRIDE, Method.GET.getName());
        headers.add(HeaderConstants.HEADER_X_FORWARDED_FOR, "TEST");
        attributesHeader.put(HeaderConstants.ATTRIBUTE_HEADERS, headers);

        createGet(UNEFFECTED);
        this.request.setAttributes(attributesHeader);
        filter();
        assertMethod(Method.GET);

        createPost(UNEFFECTED);
        filter();
        assertMethod(Method.POST);

        createPost(UNEFFECTED);
        tunnelFilter.getTunnelService().setMethodHeader(HeaderConstants.HEADER_X_FORWARDED_FOR);
        this.request.setAttributes(attributesHeader);
        filter();
        assertNotSameMethod(Method.PUT);

        createPost(UNEFFECTED);
        tunnelFilter.getTunnelService().setMethodHeader(HeaderConstants.HEADER_X_FORWARDED_FOR);
        tunnelFilter.getTunnelService().setHeadersTunnel(false);
        this.request.setAttributes(attributesHeader);
        filter();
        assertMethod(Method.POST);
    }

    @Test
    void testWithMatrixParam() {
        createGet(EFFECTED + ";abcdef");
        filter();
        check("https://example.com/adf.asdf/af;abcdef", null);
        assertMediaTypes(MediaType.TEXT_HTML);
        assertLanguages();
        assertCharSets();
        assertCharSets();
    }

    @Test
    void testMethodTunnelingViaUserAgent() {
        tunnelFilter.getTunnelService().setExtensionsTunnel(false);
        tunnelFilter.getTunnelService().setHeadersTunnel(false);
        tunnelFilter.getTunnelService().setMethodTunnel(false);
        tunnelFilter.getTunnelService().setPreferencesTunnel(false);
        tunnelFilter.getTunnelService().setQueryTunnel(false);
        tunnelFilter.getTunnelService().setUserAgentTunnel(true);

        createGet(UNEFFECTED);
        this.accMediaTypes.add(new Preference<>(MediaType.APPLICATION_ZIP));
        filter();
        assertEquals(UNEFFECTED, this.request.getResourceRef().toString());
        assertMediaTypes(MediaType.APPLICATION_ZIP);
        assertCharSets();
        assertEncodings();

        this.userAgent =
                "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)";
        createGet(UNEFFECTED);
        this.accMediaTypes.add(new Preference<>(MediaType.APPLICATION_ZIP));
        filter();
        assertEquals(UNEFFECTED, this.request.getResourceRef().toString());
        assertMediaTypes(
                MediaType.TEXT_HTML,
                MediaType.APPLICATION_XHTML,
                MediaType.APPLICATION_XML,
                MediaType.ALL);
    }

    @Test
    void testMethodTunnelingViaQuery() {
        tunnelFilter.getTunnelService().setExtensionsTunnel(false);
        tunnelFilter.getTunnelService().setHeadersTunnel(false);
        tunnelFilter.getTunnelService().setMethodTunnel(false);
        tunnelFilter.getTunnelService().setPreferencesTunnel(true);
        tunnelFilter.getTunnelService().setQueryTunnel(true);
        tunnelFilter.getTunnelService().setUserAgentTunnel(false);

        createGet(QUERY);
        this.tunnelFilter.beforeHandle(this.request, this.response);

        assertEquals(QUERY, this.request.getResourceRef().toString());

        createGet(QUERY_PREF);
        this.tunnelFilter.beforeHandle(this.request, this.response);
        assertEquals(QUERY, this.request.getResourceRef().toString());
        assertMediaTypes(MediaType.TEXT_PLAIN);
    }
}
