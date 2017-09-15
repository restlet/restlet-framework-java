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

package org.restlet.ext.jaxrs.internal.resteasy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.SynchronousExecutionContext;
import org.jboss.resteasy.plugins.server.BaseHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.NotImplementedYetException;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.InputRepresentation;
import org.restlet.util.NamedValue;

/**
 * RESTEasy HTTP request wrapper for Restlet requests.
 * 
 * @author Jerome Louvel
 */
public class RestletHttpRequest extends BaseHttpRequest {

    private final ResteasyHttpHeaders httpHeaders;

    /** The wrapped Restlet request. */
    private final Request request;

    private final ResteasyUriInfo uriInfo;

    /**
     * Constructor.
     * 
     * @param request
     *            The wrapped Restlet request.
     * @throws URISyntaxException
     */
    public RestletHttpRequest(SynchronousDispatcher dispatcher, Request request)
            throws URISyntaxException {
        super(dispatcher);
        this.request = request;
        this.httpHeaders = createHttpHeaders();
        this.uriInfo = createUriInfo();
    }

    /**
     * Creates a RESTEasy HTTP headers object.
     * 
     * @return A RESTEasy HTTP headers object.
     */
    protected ResteasyHttpHeaders createHttpHeaders() {
        MultivaluedMap<String, String> requestHeaders = new Headers<String>();

        for (NamedValue<String> header : getRequest().getHeaders()) {
            requestHeaders.add(header.getName(), header.getValue());
        }

        return new ResteasyHttpHeaders(requestHeaders);
    }

    /**
     * Creates a RESTEasy URI info object.
     * 
     * @return A RESTEasy URI info object.
     */
    protected ResteasyUriInfo createUriInfo() {
        try {
            return new ResteasyUriInfo(getRequest().getResourceRef()
                    .getBaseRef().toUri(), new URI(getRequest()
                    .getResourceRef().getRelativeRef().toString()));
        } catch (URISyntaxException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to parse the URI.", e);
            return null;
        }
    }

    @Override
    public void forward(String path) {
        throw new NotImplementedYetException();
    }

    @Override
    public ResteasyAsynchronousContext getAsyncContext() {
        return new SynchronousExecutionContext(this.dispatcher, this,
                httpResponse);
    }

    @Override
    public Object getAttribute(String name) {
        return getRequest().getAttributes().get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(getRequest().getAttributes().keySet());
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        return this.httpHeaders;
    }

    @Override
    public String getHttpMethod() {
        return getRequest().getMethod().getName();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return getRequest().getEntity().getStream();
        } catch (IOException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to get the request entity input stream.", e);
            return null;
        }
    }

    @Override
    public MultivaluedMap<String, String> getMutableHeaders() {
        return httpHeaders.getMutableHeaders();
    }

    /**
     * Returns the wrapped Restlet request.
     * 
     * @return The wrapped Restlet request.
     */
    public Request getRequest() {
        return this.request;
    }

    @Override
    public ResteasyUriInfo getUri() {
        return this.uriInfo;
    }

    @Override
    public void removeAttribute(String name) {
        getRequest().getAttributes().remove(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        getRequest().getAttributes().put(name, value);
    }

    @Override
    public void setHttpMethod(String name) {
        getRequest().setMethod(Method.valueOf(name));
    }

    @Override
    public void setInputStream(InputStream stream) {
        getRequest().setEntity(new InputRepresentation(stream));
    }

    @Override
    public void setRequestUri(URI uri) throws IllegalStateException {
        getRequest().setResourceRef(new Reference(uri));
    }

    @Override
    public void setRequestUri(URI baseUri, URI requestUri)
            throws IllegalStateException {
        getRequest().setResourceRef(new Reference(baseUri, requestUri));
    }

    @Override
    public boolean wasForwarded() {
        return false;
    }

}
