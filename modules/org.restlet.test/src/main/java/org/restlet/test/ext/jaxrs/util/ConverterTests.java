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

package org.restlet.test.ext.jaxrs.util;

import static org.restlet.ext.jaxrs.internal.util.Converter.getMediaTypeWithoutParams;
import static org.restlet.ext.jaxrs.internal.util.Converter.toLanguage;
import static org.restlet.ext.jaxrs.internal.util.Converter.toLocale;

import java.util.Locale;

import junit.framework.TestCase;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.util.Series;

/**
 * @author Stephan Koops
 * @see PathRegExp
 */
@SuppressWarnings("all")
public class ConverterTests extends TestCase {

    private void checkToLanguageToLocale(Locale locale) {
        assertEquals(locale, toLocale(toLanguage(locale)));
    }

    public void testGetMediaTypeWitoutParams1() {
        assertEquals(null, getMediaTypeWithoutParams((MediaType) null));
        assertEquals(MediaType.TEXT_HTML,
                getMediaTypeWithoutParams(MediaType.TEXT_HTML));
    }

    public void testGetMediaTypeWitoutParams2() {
        Series<Parameter> params = new Series<Parameter>(Parameter.class);
        params.add("abc", "def");
        final MediaType mt = new MediaType("a/b", params);
        assertEquals("a/b", getMediaTypeWithoutParams(mt).toString());
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
