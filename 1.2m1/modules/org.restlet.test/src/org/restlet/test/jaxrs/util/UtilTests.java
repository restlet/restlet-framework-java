/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.util;

import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import junit.framework.TestCase;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.MultivaluedMapImpl;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * @author Stephan Koops
 * @see PathRegExp
 */
@SuppressWarnings("all")
public class UtilTests extends TestCase {

    /** test interface for test of {@link Util#doesImplement(Class, Class)}. */
    private static interface I1 { }
    /** test interface for test of {@link Util#doesImplement(Class, Class)}. */
    private static interface I2 extends I1 {}
    /** test interface for test of {@link Util#doesImplement(Class, Class)}. */
    private static class C1 { }
    /** test class for test of {@link Util#doesImplement(Class, Class)}. */
    private static class C2 extends C1 implements I1 {}
    /** test class for test of {@link Util#doesImplement(Class, Class)}. */
    private static class C3 implements I1 {}
    /** test class for test of {@link Util#doesImplement(Class, Class)}. */
    private static class C4 extends C3 {}
    /** test class for test of {@link Util#doesImplement(Class, Class)}. */
    private static class C5 extends C3 implements I2 {}

    private MultivaluedMap<String, Object> httpHeaders;

    private void checkPathTemplateWithoutRegExp(String expectedOut, String in)
            throws IllegalPathException {
        assertEquals(expectedOut, Util.getPathTemplateWithoutRegExps(in, null));
    }

    private void checkPathTemplateWithoutRegExpIllegal(String in) {
        try {
            Util.getPathTemplateWithoutRegExps(in, null);
            fail("\""+in+"\" must not be allowed");
        } catch (IllegalPathException e) {
            // wonderful
        }
    }

    /**
     * @return the {@link CharacterSet} as String
     */
    private String getCss() {
        return Util.getCharsetName(this.httpHeaders, null);
    }

    /**
     * @return the {@link MediaType} without any parameter
     */
    private MediaType getMt() {
        final MediaType mediaType = Util.getMediaType(this.httpHeaders);
        if (mediaType == null) {
            return null;
        }
        return Converter.getMediaTypeWithoutParams(mediaType);
    }

    /**
     * @return the {@link MediaType} without any parameter as String
     */
    private String getMts() {
        final MediaType mediaType = getMt();
        if (mediaType == null) {
            return null;
        }
        return mediaType.toString();
    }

    private void setContentType(MediaType mediaType, CharacterSet characterSet) {
        if (characterSet != null) {
            mediaType.getParameters().add("charset", characterSet.getName());
        }
        setContentType(mediaType.toString());
    }

    private void setContentType(String contentType) {
        this.httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
    }

    public void setUp() {
        this.httpHeaders = new MultivaluedMapImpl<String, Object>();
    }

    public void testDoesImplements() {
        assertTrue(Util.doesImplement(String.class, CharSequence.class));
        assertFalse(Util.doesImplement(CharSequence.class, String.class));
        assertFalse(Util.doesImplement(Object.class, CharSequence.class));
        assertTrue(Util.doesImplement(Integer.class, Comparable.class));
        
        assertFalse(Util.doesImplement(C1.class, I1.class));
        assertFalse(Util.doesImplement(C1.class, I2.class));
        assertTrue(Util.doesImplement(C2.class, I1.class));
        assertFalse(Util.doesImplement(C2.class, I2.class));
        assertTrue(Util.doesImplement(C3.class, I1.class));
        assertFalse(Util.doesImplement(C3.class, I2.class));
        assertTrue(Util.doesImplement(C4.class, I1.class));
        assertFalse(Util.doesImplement(C4.class, I2.class));
        assertTrue(Util.doesImplement(C5.class, I1.class));
        assertTrue(Util.doesImplement(C5.class, I2.class));
    }

    public void testGetOfContentType0() {
        assertEquals(null, getCss());
        assertEquals(null, getMts());
    }

    public void testGetOfContentType1() {
        setContentType("a/b;charset=CS");
        assertEquals("CS", getCss());
        assertEquals("a/b", getMts());
    }

    public void testGetOfContentType2() {
        setContentType(MediaType.TEXT_HTML, null);
        assertEquals(null, getCss());
        assertEquals(MediaType.TEXT_HTML, getMt());
    }

    public void testGetOfContentType3() {
        setContentType("a/b ;charset=CS");
        assertEquals("CS", getCss());
        assertEquals("a/b", getMts());
    }

    public void testGetOfContentType4() {
        setContentType("a/b;d=g;charset=CS");
        assertEquals("CS", getCss());
        assertEquals("a/b", getMts());
    }

    public void testGetPathTemplateWithoutRegExp() throws IllegalPathException {
        checkPathTemplateWithoutRegExp("abc", "abc");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{de}fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{de:sd}fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{ de}fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{ de }fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{de }fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{de :}fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{de : }fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{de : yx}fg");
        checkPathTemplateWithoutRegExp("abc{de}fg", "abc{de : yx }fg");
        checkPathTemplateWithoutRegExpIllegal("abc{}hjk");
        checkPathTemplateWithoutRegExpIllegal("abc{:}hjk");
        checkPathTemplateWithoutRegExpIllegal("abc{:sdf}hjk");
    }
}