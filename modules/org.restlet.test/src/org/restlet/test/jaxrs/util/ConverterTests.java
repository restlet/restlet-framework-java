/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.util;

import static org.restlet.ext.jaxrs.internal.util.Converter.getMediaTypeWithoutParams;
import static org.restlet.ext.jaxrs.internal.util.Converter.toLanguage;
import static org.restlet.ext.jaxrs.internal.util.Converter.toLocale;

import java.util.Locale;

import junit.framework.TestCase;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;

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
        Form params = new Form();
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