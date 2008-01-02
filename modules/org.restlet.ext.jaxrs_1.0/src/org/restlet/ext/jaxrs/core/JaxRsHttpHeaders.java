/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.ext.jaxrs.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.util.Series;

/**
 * Implemetation of the JAX-RS interface {@link HttpHeaders}
 * @author Stephan Koops
 *
 */
public class JaxRsHttpHeaders implements HttpHeaders {
    // TODO TESTEN: soll Case-insensitiv sein
    private org.restlet.data.Request request;

    private List<MediaType> acceptedMediaTypes;

    private List<Cookie> cookies;

    private String language;

    private MediaType mediaType;

    private AllHttpHeaders requestHeaders;

    /**
     * 
     * @param request The Restlet request to wrap.
     */
    public JaxRsHttpHeaders(org.restlet.data.Request request) {
        this.request = request;
    }

    public List<MediaType> getAcceptableMediaTypes() {
        if (this.acceptedMediaTypes == null) {
            List<Preference<org.restlet.data.MediaType>> restletAccMediaTypes = request
                    .getClientInfo().getAcceptedMediaTypes();
            List<MediaType> accMediaTypes = new ArrayList<MediaType>(
                    restletAccMediaTypes.size());
            for (Preference<org.restlet.data.MediaType> mediaTypePref : restletAccMediaTypes)
                accMediaTypes.add(createJaxRsMediaType(mediaTypePref));
            this.acceptedMediaTypes = accMediaTypes;
        }
        return this.acceptedMediaTypes;
    }

    /**
     * @param mediaTypePref
     * @return
     */
    private MediaType createJaxRsMediaType(
            Preference<org.restlet.data.MediaType> mediaTypePref) {
        org.restlet.data.MediaType restletMediaType = mediaTypePref
                .getMetadata();
        Series<Parameter> rlMediaTypeParams = restletMediaType.getParameters();
        Map<String, String> parameters = null;
        if (!rlMediaTypeParams.isEmpty()) {
            parameters = new HashMap<String, String>();
            for (Parameter p : rlMediaTypeParams)
                parameters.put(p.getName(), p.getValue());
        }
        return new MediaType(restletMediaType.getMainType(), restletMediaType
                .getSubType());
    }

    public List<Cookie> getCookies() {
        if (this.cookies == null) {
            List<Cookie> c = new ArrayList<Cookie>();
            if (!request.getCookies().isEmpty())
                throw new NotYetImplementedException();
            this.cookies = c;
        }
        return this.cookies;
    }

    public String getLanguage() {
        return language;
    }

    public MediaType getMediaType() {
        return this.mediaType;
    }

    public MultivaluedMap<String, String> getRequestHeaders() {
        if (this.requestHeaders == null) {
            this.requestHeaders = new AllHttpHeaders(Util
                    .getHttpHeaders(request));
        }
        return this.requestHeaders;
    }

    class AllHttpHeaders implements MultivaluedMap<String, String> {
        AllHttpHeaders(Form httpHeaders) {
            this.httpHeaders = httpHeaders;
        }

        private Form httpHeaders;

        public String getFirst(String key) {
            return httpHeaders.getFirstValue(key, true);
        }

        public boolean containsKey(Object key) {
            if (!(key instanceof String))
                return false;
            return httpHeaders.getFirst((String) key, true) != null;
        }

        public boolean containsValue(Object value) {
            for (Parameter p : httpHeaders)
                if (Util.equals(p.getValue(), value))
                    return true;
            return false;
        }

        public Set<java.util.Map.Entry<String, List<String>>> entrySet() {
            throw new NotYetImplementedException();
        }

        public List<String> get(Object key) {
            if (!(key instanceof String))
                return null;
            throw new NotYetImplementedException(
                    "Änderung in Klasse Series: getValuesAsList((String)key, true)");
            // return httpHeaders.getValuesAsList((String)key, true);
        }

        public boolean isEmpty() {
            return httpHeaders.isEmpty();
        }

        public Set<String> keySet() {
            throw new NotYetImplementedException();
        }

        public int size() {
            return httpHeaders.size(); // LATER stimmt vielleicht nicht, wenn
                                        // es Elemente mehrfach gibt
        }

        public Collection<List<String>> values() {
            throw new NotYetImplementedException();
        }

        @Deprecated
        @SuppressWarnings("unused")
        public void add(String key, String value)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "The HTTP headers are immutable");
        }

        @Deprecated
        @SuppressWarnings("unused")
        public void putSingle(String key, String value)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "The HTTP headers are immutable");
        }

        @Deprecated
        @SuppressWarnings("unused")
        public void clear() throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "The HTTP headers are immutable");
        }

        @Deprecated
        @SuppressWarnings("unused")
        public List<String> put(String key, List<String> value)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "The HTTP headers are immutable");
        }

        @Deprecated
        @SuppressWarnings("unused")
        public void putAll(Map<? extends String, ? extends List<String>> t)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "The HTTP headers are immutable");
        }

        @Deprecated
        @SuppressWarnings("unused")
        public List<String> remove(Object key)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "The HTTP headers are immutable");
        }
    }
}