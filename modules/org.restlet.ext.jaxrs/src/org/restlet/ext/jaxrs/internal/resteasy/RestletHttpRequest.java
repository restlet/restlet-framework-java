/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.InputRepresentation;

/**
 * RESTEasy HTTP request wrapper for Restlet requests.
 * 
 * @author Jerome Louvel
 */
public class RestletHttpRequest implements HttpRequest {

    /** The wrapped Restlet request. */
    private final Request request;

    /**
     * Constructor.
     * 
     * @param request
     *            The wrapped Restlet request.
     */
    public RestletHttpRequest(Request request) {
        this.request = request;
    }

    @Override
    public void forward(String path) {

    }

    @Override
    public ResteasyAsynchronousContext getAsyncContext() {

        return null;
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
    public MultivaluedMap<String, String> getDecodedFormParameters() {

        return null;
    }

    @Override
    public MultivaluedMap<String, String> getFormParameters() {

        return null;
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        // TODO
        return null;
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
        // TODO
        return null;
    }

    /**
     * Returns the wrapped Restlet request.
     * 
     * @return The wrapped Restlet request.
     */
    public Request getRequest() {
        return request;
    }

    @Override
    public ResteasyUriInfo getUri() {
        // TODO
        return null;
    }

    @Override
    public boolean isInitial() {
        // TODO
        return false;
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
        // TODO
        return false;
    }

}
