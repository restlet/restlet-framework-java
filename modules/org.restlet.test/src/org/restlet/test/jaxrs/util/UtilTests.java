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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import junit.framework.TestCase;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.MultivaluedMapImpl;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * @author Stephan Koops
 * @see PathRegExp
 */
@SuppressWarnings("all")
public class UtilTests extends TestCase {

    private MultivaluedMap<String, Object> httpHeaders;

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

    public void testDoesImplements() {
        assertTrue(Util.doesImplements(String.class, CharSequence.class));
        assertFalse(Util.doesImplements(CharSequence.class, String.class));
        assertFalse(Util.doesImplements(Object.class, CharSequence.class));
    }
}