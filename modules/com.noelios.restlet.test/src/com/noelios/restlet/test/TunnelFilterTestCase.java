/*
 * Copyright 2005-2008 Noelios Technologies.
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

package com.noelios.restlet.test;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.application.TunnelFilter;

/**
 * Tests cases for the tunnel filter.
 */
public class TunnelFilterTestCase extends TestCase {

    /** . */
    private static final String EFFECTED = "http://example.org/adf.asdf/af.html";

    /** . */
    private static final String START_REF_FOR_PATH_TEST = "http://www.example.com/abc/def/";

    /** . */
    private static final String UNEFFECTED = "http://example.org/abc.def/af.ab";

    private List<Preference<CharacterSet>> accCharsets;

    private List<Preference<Encoding>> accEncodings;

    private List<Preference<Language>> accLanguages;

    private List<Preference<MediaType>> accMediaTypes;

    private String lastCreatedReference;

    private Request request;

    private Response response;

    private TunnelFilter tunnelFilter;

    void assertCharSets(CharacterSet... characterSets) {
        assertEqualSet(this.accCharsets, characterSets);
    }

    void assertEncodings(Encoding... encodings) {
        assertEqualSet(this.accEncodings, encodings);
    }

    <A extends Metadata> A assertEqualSet(List<? extends Preference<A>> actual,
            A... expected) {
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
                final String message = exp
                        + " should be in, but is missing in " + actual;
                fail(message);
            }
        }
        return null;
    }

    void assertLanguages(Language... languages) {
        assertEqualSet(this.accLanguages, languages);
    }

    void assertMediaTypes(MediaType... mediaTypes) {
        assertEqualSet(this.accMediaTypes, mediaTypes);
    }

    /**
     * @param expectedCut
     * @param expectedExtensions
     */
    private void check(String expectedCut, String expectedExtensions) {
        final Reference resourceRef = this.request.getResourceRef();
        assertEquals(expectedCut, resourceRef.toString());

        final Reference originalRef = this.request.getOriginalRef();
        assertEquals(originalRef, new Reference(this.lastCreatedReference));
        assertEquals(expectedCut, resourceRef.toString());
        assertEquals(expectedExtensions, resourceRef.getExtensions());
    }

    /**
     * 
     * @param expectedSubPathCut
     *            if null, the same as subPathOrig
     * @param expectedExtension
     *            if null, then same as "" for this test
     */
    private void checkFromPath(String expectedSubPathCut,
            String expectedExtension) {
        if (expectedSubPathCut == null) {
            check(this.lastCreatedReference, expectedExtension);
        } else {
            check(START_REF_FOR_PATH_TEST + expectedSubPathCut,
                    expectedExtension);
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
     * 
     * @param subPathToCheck
     * @see #createGet(String)
     * @see #createRequest(Method, String)
     */
    private void createGetFromPath(String subPathToCheck) {
        createGet(START_REF_FOR_PATH_TEST + subPathToCheck);
    }

    /**
     * Creates a {@link Request} and put it into {@link #request}.<br>
     * To use the methods provided by the test case class use ever the provided
     * create methods to create a request.
     * 
     * @param method
     * @param reference
     * @see #createGet(String)
     * @see #createGetFromPath(String)
     */
    void createRequest(Method method, String reference) {
        this.request = new Request(method, reference);
        this.request.setOriginalRef(new Reference(reference));
        this.response = new Response(this.request);
        this.lastCreatedReference = reference;
        setPrefs();
    }

    private void extensionTunnelOff() {
        final Application application = this.tunnelFilter.getApplication();
        application.getTunnelService().setExtensionsTunnel(false);
    }

    /**
     * Call this method to filter the current request
     */
    private void filter() {
        this.tunnelFilter.beforeHandle(this.request, this.response);
        setPrefs();
    }

    private void setPrefs() {
        this.accMediaTypes = this.request.getClientInfo()
                .getAcceptedMediaTypes();
        this.accLanguages = this.request.getClientInfo().getAcceptedLanguages();
        this.accCharsets = this.request.getClientInfo()
                .getAcceptedCharacterSets();
        this.accEncodings = this.request.getClientInfo().getAcceptedEncodings();
    }

    @Override
    public void setUp() {
        final Application app = new Application(new Context());
        Application.setCurrent(app);
        this.tunnelFilter = new TunnelFilter(app.getContext());
        this.tunnelFilter.getApplication().getTunnelService()
                .setExtensionsTunnel(true);
    }

    public void testExtMappingOff1() {
        extensionTunnelOff();
        createGet(UNEFFECTED);
        this.accLanguages
                .add(new Preference<Language>(Language.valueOf("ajh")));
        this.accMediaTypes.add(new Preference<MediaType>(
                MediaType.APPLICATION_STUFFIT));
        filter();
        assertEquals(UNEFFECTED, this.request.getResourceRef().toString());
        assertLanguages(Language.valueOf("ajh"));
        assertMediaTypes(MediaType.APPLICATION_STUFFIT);
        assertCharSets();
        assertEncodings();
    }

    public void testExtMappingOff2() {
        extensionTunnelOff();
        createGet(EFFECTED);
        this.accLanguages
                .add(new Preference<Language>(Language.valueOf("ajh")));
        this.accMediaTypes.add(new Preference<MediaType>(
                MediaType.APPLICATION_STUFFIT));
        filter();
        assertEquals(EFFECTED, this.request.getResourceRef().toString());
        assertLanguages(Language.valueOf("ajh"));
        assertMediaTypes(MediaType.APPLICATION_STUFFIT);
        assertCharSets();
        assertEncodings();
    }

    public void testExtMappingOn() {
        createGet(UNEFFECTED);
        filter();
        check(UNEFFECTED, "ab");
        assertLanguages();
        assertCharSets();
        assertCharSets();
        assertMediaTypes();

        createGet(EFFECTED);
        filter();
        check("http://example.org/adf.asdf/af", null);
        assertMediaTypes(MediaType.TEXT_HTML);
        assertLanguages();
        assertCharSets();
        assertCharSets();

        createGetFromPath("afhhh");
        filter();
        checkFromPath(null, null);
        assertEqualSet(this.accMediaTypes);
        assertLanguages();
        assertEncodings();
        assertCharSets();

        createGetFromPath("hksf.afsdf");
        filter();
        checkFromPath(null, "afsdf");
        assertMediaTypes();
        assertLanguages();
        assertEncodings();
        assertCharSets();

        createGetFromPath("hksf.afsdf.html");
        filter();
        checkFromPath("hksf.afsdf", "afsdf");
        assertMediaTypes(MediaType.TEXT_HTML);
        assertLanguages();
        assertEncodings();
        assertCharSets();

        createGetFromPath("hksf.afsdf.html.txt");
        filter();
        checkFromPath("hksf.afsdf.html", "afsdf.html");
        assertMediaTypes(MediaType.TEXT_PLAIN);
        assertLanguages();
        assertEncodings();
        assertCharSets();

        createGetFromPath("hksf.html.afsdf.txt");
        filter();
        checkFromPath("hksf.html.afsdf", "html.afsdf");
        assertMediaTypes(MediaType.TEXT_PLAIN);
        assertLanguages();
        assertEncodings();
        assertCharSets();

        createGetFromPath("hksf.html.afsdf.txt.en.fr");
        filter();
        checkFromPath("hksf.html.afsdf.txt.en", "html.afsdf.txt.en");
        // Take care about the fact that only one extension per metadata "type"
        // is allowed: ie only one Language, one encoding, one media type, etc.
        // assertMediaTypes(MediaType.TEXT_PLAIN);
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

        createGet(START_REF_FOR_PATH_TEST);
        filter();
        checkFromPath(null, null);
        assertMediaTypes();
        assertLanguages();
        assertEncodings();
        assertCharSets();
    }

    public void testWithMatrixParam() {
        createGet(EFFECTED + ";abcdef");
        filter();
        check("http://example.org/adf.asdf/af;abcdef", null);
        assertMediaTypes(MediaType.TEXT_HTML);
        assertLanguages();
        assertCharSets();
        assertCharSets();
    }
}
