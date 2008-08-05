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
package org.restlet.test.jaxrs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.core.MultivaluedMapImpl;
import org.restlet.ext.jaxrs.internal.exceptions.JaxRsRuntimeException;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;
import org.restlet.test.jaxrs.services.providers.IllegalArgExcMapper;

/**
 * @author Stephan Koops
 * @see ExceptionMappers
 * @see IllegalArgExcMapper
 */
@SuppressWarnings("all")
public class ExceptionMappersTest extends TestCase {

    private static final class TestHttpHeaders implements HttpHeaders {

        private final List<MediaType> accMediaTypes = new ArrayList<MediaType>();

        /**
         * @see javax.ws.rs.core.HttpHeaders#getAcceptableLanguages()
         */
        public List<Locale> getAcceptableLanguages() {
            return new ArrayList<Locale>();
        }

        public List<MediaType> getAcceptableMediaTypes() {
            return this.accMediaTypes;
        }

        public Map<String, Cookie> getCookies() {
            return Collections.emptyMap();
        }

        public Locale getLanguage() {
            return null;
        }

        public MediaType getMediaType() {
            return null;
        }

        public List<String> getRequestHeader(String name) {
            return Collections.emptyList();
        }

        public MultivaluedMap<String, String> getRequestHeaders() {
            return new MultivaluedMapImpl<String, String>();
        }
    }

    private static final int INTERNAL_SERVER_ERROR = Status.INTERNAL_SERVER_ERROR
            .getStatusCode();

    private JaxRsProviders exceptionMappers;

    /**
     * @param exc
     * @return
     */
    private Response convert(Throwable exc) {
        return this.exceptionMappers.convert(exc);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final IllegalArgExcMapper illegalArgExcMapper = new IllegalArgExcMapper();
        illegalArgExcMapper.httpHeaders = new TestHttpHeaders();
        this.exceptionMappers = new JaxRsProviders();
        this.exceptionMappers.add(illegalArgExcMapper);
    }

    public void testIae() throws Exception {
        final Response r = convert(new IllegalArgumentException());
        assertNotNull(r);
        assertEquals(IllegalArgExcMapper.STATUS, r.getStatus());
    }

    public void testIoe() throws Exception {
        IOException ioException = new IOException(
                "This exception is planned for testing !");
        try {
            convert(ioException);
            fail("must throw an wrapper exception");
        } catch (JaxRsRuntimeException e) {
            assertEquals(ioException, e.getCause());
        }
    }

    public void testNfe() throws Exception {
        final Response r = convert(new NumberFormatException());
        assertNotNull(r);
        assertEquals(IllegalArgExcMapper.STATUS, r.getStatus());
    }
}