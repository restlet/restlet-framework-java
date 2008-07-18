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
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExceptionMappers;
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

    private ExceptionMappers exceptionMappers;

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
        this.exceptionMappers = new ExceptionMappers();
        this.exceptionMappers.add(illegalArgExcMapper);
    }

    public void testIae() throws Exception {
        final Response r = convert(new IllegalArgumentException());
        assertNotNull(r);
        assertEquals(IllegalArgExcMapper.STATUS, r.getStatus());
    }

    public void testIoe() throws Exception {
        final Response r = convert(new IOException(
                "This exception is planned for testing !"));
        assertNotNull(r);
        assertEquals(INTERNAL_SERVER_ERROR, r.getStatus());
    }

    public void testNfe() throws Exception {
        final Response r = convert(new NumberFormatException());
        assertNotNull(r);
        assertEquals(IllegalArgExcMapper.STATUS, r.getStatus());
    }
}