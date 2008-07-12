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

import static org.restlet.ext.jaxrs.internal.util.Converter.*;

import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import junit.framework.TestCase;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.MultivaluedMapImpl;
import org.restlet.ext.jaxrs.internal.core.ResponseBuilderImpl;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.MatchingResult;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.util.Util;

import com.noelios.restlet.util.StringUtils;

/**
 * @author Stephan Koops
 * @see PathRegExp
 */
@SuppressWarnings("all")
public class ConverterTests extends TestCase {

    public void testGetMediaTypeWitoutParams1() {
        assertEquals(null, getMediaTypeWithoutParams((MediaType) null));
        assertEquals(MediaType.TEXT_HTML,
                getMediaTypeWithoutParams(MediaType.TEXT_HTML));
    }

    public void testGetMediaTypeWitoutParams2() {
        MediaType mt = new MediaType("a/b");
        mt.getParameters().add("abc", "def");
        assertEquals("a/b", getMediaTypeWithoutParams(mt).toString());
    }

    private void checkToLanguageToLocale(Locale locale) {
        assertEquals(locale, toLocale(toLanguage(locale)));
    }

    public void testToLanguageToLocale() {
        checkToLanguageToLocale(Locale.CANADA);
        checkToLanguageToLocale(Locale.CANADA_FRENCH);
        checkToLanguageToLocale(Locale.CHINA);
        checkToLanguageToLocale(Locale.CHINESE);
        checkToLanguageToLocale(Locale.ENGLISH);
        checkToLanguageToLocale(Locale.FRANCE);
        checkToLanguageToLocale(Locale.FRENCH);
        checkToLanguageToLocale(Locale.GERMAN);
        checkToLanguageToLocale(Locale.GERMANY);
        checkToLanguageToLocale(Locale.ITALIAN);
        checkToLanguageToLocale(Locale.ITALY);
        checkToLanguageToLocale(Locale.JAPAN);
        checkToLanguageToLocale(Locale.JAPANESE);
        checkToLanguageToLocale(Locale.KOREA);
        checkToLanguageToLocale(Locale.KOREAN);
        checkToLanguageToLocale(Locale.PRC);
        checkToLanguageToLocale(Locale.SIMPLIFIED_CHINESE);
        checkToLanguageToLocale(Locale.TAIWAN);
        checkToLanguageToLocale(Locale.TRADITIONAL_CHINESE);
        checkToLanguageToLocale(Locale.UK);
        checkToLanguageToLocale(Locale.US);
    }

    public void testToLocale() {
        assertEquals(new Locale("abc"), toLocale("abc"));
        assertEquals(new Locale("abc", "", "def"), toLocale("abc__def"));
        assertEquals(new Locale("abc", "def"), toLocale("abc_def"));
    }
}