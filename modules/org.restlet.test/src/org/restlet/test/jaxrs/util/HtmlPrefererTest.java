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
package org.restlet.test.jaxrs.util;

import java.util.List;

import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.ext.jaxrs.internal.util.HtmlPreferer;

import junit.framework.TestCase;

/**
 * This TextCase checks the {@link HtmlPreferer}.
 * 
 * @author Stephan Koops
 * @see HtmlPreferer
 */
public class HtmlPrefererTest extends TestCase {

    private static final HtmlPreferer HTML_PREFERER = new HtmlPreferer(null,
            new Restlet());

    /**
     * @param accMediaTypes
     * @param mediaType
     * @param quality
     */
    private void addMediaTypePref(List<Preference<MediaType>> accMediaTypes,
            MediaType mediaType, float quality) {
        accMediaTypes.add(new Preference<MediaType>(mediaType, quality));
    }

    /**
     * @param accMediaTypes
     * @param q0
     * @param q1
     * @param q2
     * @param q3
     * @param q4
     */
    private void check(List<Preference<MediaType>> accMediaTypes, float q0,
            float q1, float q2, float q3, float q4) {
        assertEquals(5, accMediaTypes.size());
        Preference<MediaType> amt0 = accMediaTypes.get(0);
        Preference<MediaType> amt1 = accMediaTypes.get(1);
        Preference<MediaType> amt2 = accMediaTypes.get(2);
        Preference<MediaType> amt3 = accMediaTypes.get(3);
        Preference<MediaType> amt4 = accMediaTypes.get(4);
        assertEquals(q0, amt0.getQuality());
        assertEquals(q1, amt1.getQuality());
        assertEquals(q2, amt2.getQuality());
        assertEquals(q3, amt3.getQuality());
        assertEquals(q4, amt4.getQuality());
    }

    public void test1() {
        Request request = new Request();
        List<Preference<MediaType>> accMediaTypes = request.getClientInfo()
                .getAcceptedMediaTypes();
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_ALL, 0.2f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_HTML, 1f);
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_XHTML_XML, 0.9f);
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_XML, 0.8f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_XML, 0.7f);
        HTML_PREFERER.handle(request);
        check(accMediaTypes, 0.2f, 1f, 0.9f, 0.8f, 0.7f);
    }

    public void test2() {
        Request request = new Request();
        List<Preference<MediaType>> accMediaTypes = request.getClientInfo()
                .getAcceptedMediaTypes();
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_ALL, 0.2f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_HTML, 0.6f);
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_XHTML_XML, 0.9f);
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_XML, 0.8f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_XML, 0.7f);
        HTML_PREFERER.handle(request);
        check(accMediaTypes, 0.2f, 0.801f, 0.9f, 0.8f, 0.7f);
    }

    public void test3() {
        Request request = new Request();
        List<Preference<MediaType>> accMediaTypes = request.getClientInfo()
                .getAcceptedMediaTypes();
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_ALL, 0.2f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_HTML, 0.6f);
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_XHTML_XML, 1f);
        addMediaTypePref(accMediaTypes, MediaType.IMAGE_BMP, 0.8f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_PLAIN, 0.7f);
        HTML_PREFERER.handle(request);
        check(accMediaTypes, 0.2f, 0.6f, 1f, 0.8f, 0.7f);
    }

    public void test4() {
        Request request = new Request();
        List<Preference<MediaType>> accMediaTypes = request.getClientInfo()
                .getAcceptedMediaTypes();
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_ALL, 0.2f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_HTML, 0.6f);
        addMediaTypePref(accMediaTypes, MediaType.APPLICATION_XML, 1f);
        addMediaTypePref(accMediaTypes, MediaType.IMAGE_BMP, 0.999f);
        addMediaTypePref(accMediaTypes, MediaType.TEXT_PLAIN, 0.7f);
        HTML_PREFERER.handle(request);
        check(accMediaTypes, 0.2f, 1f, 0.999f, 0.998f, 0.7f);
    }
}