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
package org.restlet.ext.jaxrs.internal.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.restlet.data.Parameter;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.internal.core.MultivaluedMapImpl;
import org.restlet.util.Series;

/**
 * This class wraps the request to get the headers from, if needed in a
 * {@link MessageBodyWriter}. The changing of the http headers is not supported
 * by this runtime environment, because it is not a good design and Restlet does
 * not support it.<br>
 * This class is not thread safe.
 * 
 * @author Stephan Koops
 */
public class WrappedRequestForHttpHeaders implements
        MultivaluedMap<String, Object> {
    /**
     * may be null, f content was not already copied from the
     * {@link #restletResponse}.
     */
    private Series<Parameter> headers;

    /** may be null */
    private MultivaluedMap<String, Object> jaxRsRespHeaders;

    private final Logger logger;

    /** null, if content was copied to the {@link #headers}. */
    private Response restletResponse;

    /**
     * 
     * @param restletResponse
     * @param jaxRsRespHeaders
     * @param logger
     */
    public WrappedRequestForHttpHeaders(Response restletResponse,
            MultivaluedMap<String, Object> jaxRsRespHeaders, Logger logger) {
        if (restletResponse == null)
            throw new IllegalArgumentException(
                    "The Restlet Response must not be null");
        this.restletResponse = restletResponse;
        this.jaxRsRespHeaders = jaxRsRespHeaders;
        this.logger = logger;
    }

    public void add(String headerName, Object headerValue) {
        unsupported();
    }

    private UnsupportedOperationException unsupported()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The changing of the http headers is not supported by this runtime environment.");
    }

    /**
     * @return
     */
    private MultivaluedMap<String, Object> allToJaxRsHeaders() {
        MultivaluedMap<String, Object> jaxRsRespHeaders = getJaxRsRespHeaders();
        Series<Parameter> headers = getHeaders();
        if (headers != null) {
            for (Parameter p : headers) {
                String name = p.getName();
                String value = p.getValue();
                List<Object> values = jaxRsRespHeaders.get(name);
                boolean contained = false;
                if (values != null) {
                    for (Object v : values)
                        if (v != null && v.toString().equals(value))
                            contained = true;
                }
                if (!contained)
                    jaxRsRespHeaders.add(name, value);
            }
            this.headers = null;
        }
        return jaxRsRespHeaders;
    }

    public void clear() {
        unsupported();
    }

    public boolean containsKey(Object headerName) {
        if (headerName == null)
            return false;
        if (jaxRsRespHeaders != null)
            if (jaxRsRespHeaders.containsKey(headerName))
                return true;
        Series<Parameter> headers = getHeaders();
        if (headers != null)
            for (Parameter p : headers)
                if (headerName.equals(p.getName()))
                    return true;
        return false;
    }

    public boolean containsValue(Object headerValue) {
        if (jaxRsRespHeaders != null)
            if (jaxRsRespHeaders.containsValue(headerValue))
                return true;
        Series<Parameter> headers = getHeaders();
        if (headers != null)
            for (Parameter p : headers)
                if (headerValue.equals(p.getValue()))
                    return true;
        return false;
    }

    public Set<java.util.Map.Entry<String, List<Object>>> entrySet() {
        MultivaluedMap<String, Object> jaxRsRespHeaders = allToJaxRsHeaders();
        return jaxRsRespHeaders.entrySet();
    }

    public List<Object> get(Object headerName) {
        return allToJaxRsHeaders().get(headerName);
    }

    public Object getFirst(String headerName) {
        if (jaxRsRespHeaders != null) {
            Object rt = jaxRsRespHeaders.getFirst(headerName);
            if (rt != null)
                return rt;
        }
        Series<Parameter> headers = getHeaders();
        if (headers != null) {
            Parameter first = headers.getFirst(headerName);
            if (first == null)
                return null;
            return first.getValue();
        }
        return null;
    }

    /**
     * gets the Restlet headers. If the Restlet headers are not available, but
     * the Restlet Response, the headers are copied from the Response headers.
     * If both is not available, null is returned.
     * 
     * @return
     */
    private Series<Parameter> getHeaders() {
        if (this.headers == null && restletResponse != null) {
            this.headers = Util.copyResponseHeaders(restletResponse, logger);
            this.restletResponse = null;
        }
        return this.headers;
    }

    private MultivaluedMap<String, Object> getJaxRsRespHeaders() {
        if (this.jaxRsRespHeaders == null)
            this.jaxRsRespHeaders = new MultivaluedMapImpl<String, Object>();
        return this.jaxRsRespHeaders;
    }

    public boolean isEmpty() {
        if (jaxRsRespHeaders != null && !jaxRsRespHeaders.isEmpty())
            return false;
        Series<Parameter> headers = getHeaders();
        if (headers != null)
            return headers.isEmpty();
        return true;
    }

    public Set<String> keySet() {
        return allToJaxRsHeaders().keySet();
    }

    public List<Object> put(String headerName, List<Object> headerValues) {
        throw unsupported();
    }

    public void putAll(Map<? extends String, ? extends List<Object>> t) {
        unsupported();
    }

    public void putSingle(String headerName, Object headerValue) {
        unsupported();
    }

    public List<Object> remove(Object headerName) {
        throw unsupported();
    }

    public int size() {
        int size = 0;
        if (jaxRsRespHeaders != null)
            size = jaxRsRespHeaders.size();
        Series<Parameter> headers = getHeaders();
        if (headers != null)
            size += headers.size();
        return size;
    }

    public Collection<List<Object>> values() {
        return allToJaxRsHeaders().values();
    }
}