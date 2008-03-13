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
 * {@link MessageBodyWriter}.
 * 
 * @author Stephan Koops
 */
public class WrappedRequestForHttpHeaders implements
        MultivaluedMap<String, Object> {
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

    /** may be null */
    private MultivaluedMap<String, Object> jaxRsRespHeaders;

    /** null, if content was copied to the {@link #headers}. */
    private Response restletResponse;

    /**
     * may be null, f content was not already copied from the
     * {@link #restletResponse}.
     */
    private Series<Parameter> headers;

    private Logger logger;

    private MultivaluedMap<String, Object> getJaxRsRespHeaders() {
        if (this.jaxRsRespHeaders == null)
            this.jaxRsRespHeaders = new MultivaluedMapImpl<String, Object>();
        return this.jaxRsRespHeaders;
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

    public void add(String headerName, Object headerValue) {
        getJaxRsRespHeaders().add(headerName, headerValue);
    }

    public Object getFirst(String headerName) {
        if (jaxRsRespHeaders != null) {
            Object rt = jaxRsRespHeaders.getFirst(headerName);
            if (rt != null)
                return rt;
        }
        Series<Parameter> headers = getHeaders();
        if (headers != null)
            return headers.getFirst(headerName).getValue();
        return null;
    }

    public void putSingle(String headerName, Object headerValue) {
        getJaxRsRespHeaders().putSingle(headerName, headerValue);
        Series<Parameter> headers = getHeaders();
        if (headers != null)
            headers.removeAll(headerName);
    }

    public void clear() {
        if (jaxRsRespHeaders != null)
            jaxRsRespHeaders.clear();
        this.headers = null;
        this.restletResponse = null;
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

    public List<Object> get(Object headerName) {
        return allToJaxRsHeaders().get(headerName);
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
        return allToJaxRsHeaders().put(headerName, headerValues);
    }

    public void putAll(Map<? extends String, ? extends List<Object>> t) {
        allToJaxRsHeaders().putAll(t);
    }

    public List<Object> remove(Object headerName) {
        return allToJaxRsHeaders().remove(headerName);
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
