/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.ext.jaxrs.internal.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.restlet.Response;
import org.restlet.data.Parameter;
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

    /** null, if content was copied to the {@link #headers}. */
    private Response restletResponse;

    /**
     * 
     * @param restletResponse
     * @param jaxRsRespHeaders
     */
    public WrappedRequestForHttpHeaders(Response restletResponse,
            MultivaluedMap<String, Object> jaxRsRespHeaders) {
        if (restletResponse == null) {
            throw new IllegalArgumentException(
                    "The Restlet Response must not be null");
        }
        this.restletResponse = restletResponse;
        this.jaxRsRespHeaders = jaxRsRespHeaders;
    }

    public void add(String headerName, Object headerValue) {
        unsupported();
    }

    /**
     * @return
     */
    private MultivaluedMap<String, Object> allToJaxRsHeaders() {
        final MultivaluedMap<String, Object> jaxRsRespHeaders = getJaxRsRespHeaders();
        final Series<Parameter> headers = getHeaders();
        if (headers != null) {
            for (final Parameter p : headers) {
                final String name = p.getName();
                final String value = p.getValue();
                final List<Object> values = jaxRsRespHeaders.get(name);
                boolean contained = false;
                if (values != null) {
                    for (final Object v : values) {
                        if ((v != null) && v.toString().equals(value)) {
                            contained = true;
                        }
                    }
                }
                if (!contained) {
                    jaxRsRespHeaders.add(name, value);
                }
            }
            this.headers = null;
        }
        return jaxRsRespHeaders;
    }

    public void clear() {
        unsupported();
    }

    public boolean containsKey(Object headerName) {
        if (headerName == null) {
            return false;
        }
        if (this.jaxRsRespHeaders != null) {
            if (this.jaxRsRespHeaders.containsKey(headerName)) {
                return true;
            }
        }
        final Series<Parameter> headers = getHeaders();
        if (headers != null) {
            for (final Parameter p : headers) {
                if (headerName.equals(p.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsValue(Object headerValue) {
        if (this.jaxRsRespHeaders != null) {
            if (this.jaxRsRespHeaders.containsValue(headerValue)) {
                return true;
            }
        }
        final Series<Parameter> headers = getHeaders();
        if (headers != null) {
            for (final Parameter p : headers) {
                if (headerValue.equals(p.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<java.util.Map.Entry<String, List<Object>>> entrySet() {
        final MultivaluedMap<String, Object> jaxRsRespHeaders = allToJaxRsHeaders();
        return jaxRsRespHeaders.entrySet();
    }

    public List<Object> get(Object headerName) {
        return allToJaxRsHeaders().get(headerName);
    }

    public Object getFirst(String headerName) {
        if (this.jaxRsRespHeaders != null) {
            final Object rt = this.jaxRsRespHeaders.getFirst(headerName);
            if (rt != null) {
                return rt;
            }
        }
        final Series<Parameter> headers = getHeaders();
        if (headers != null) {
            final Parameter first = headers.getFirst(headerName);
            if (first == null) {
                return null;
            }
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
        if ((this.headers == null) && (this.restletResponse != null)) {
            this.headers = Util.copyResponseHeaders(this.restletResponse);
            this.restletResponse = null;
        }
        return this.headers;
    }

    private MultivaluedMap<String, Object> getJaxRsRespHeaders() {
        if (this.jaxRsRespHeaders == null) {
            this.jaxRsRespHeaders = new MultivaluedMapImpl<String, Object>();
        }
        return this.jaxRsRespHeaders;
    }

    public boolean isEmpty() {
        if ((this.jaxRsRespHeaders != null) && !this.jaxRsRespHeaders.isEmpty()) {
            return false;
        }
        final Series<Parameter> headers = getHeaders();
        if (headers != null) {
            return headers.isEmpty();
        }
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
        if (this.jaxRsRespHeaders != null) {
            size = this.jaxRsRespHeaders.size();
        }
        final Series<Parameter> headers = getHeaders();
        if (headers != null) {
            size += headers.size();
        }
        return size;
    }

    private UnsupportedOperationException unsupported()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The changing of the http headers is not supported by this runtime environment.");
    }

    public Collection<List<Object>> values() {
        return allToJaxRsHeaders().values();
    }
}