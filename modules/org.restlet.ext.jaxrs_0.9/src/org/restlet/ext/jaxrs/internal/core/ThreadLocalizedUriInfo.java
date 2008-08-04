/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.ext.jaxrs.internal.core;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * A ThreadLocalizedUriInfo is used to inject, if a {@link UriInfo} is required
 * to inject. It must be new instantiated for every place to inject.
 * 
 * @author Stephan Koops
 * @see UriInfo
 * @see ThreadLocalizedContext
 */
public class ThreadLocalizedUriInfo implements UriInfo {

    private final ThreadLocal<AncestorInfo> ancestorInfos = new ThreadLocal<AncestorInfo>();

    private final ThreadLocalizedContext tlContext;

    /**
     * Creates a new ThreadLocalizedUriInfo
     * 
     * @param tlContext
     */
    public ThreadLocalizedUriInfo(ThreadLocalizedContext tlContext) {
        this.tlContext = tlContext;
    }

    /**
     * @return the AncestorInfo with the current data from the
     *         {@link CallContext}.
     * @throws IllegalStateException
     *             if no CallContext could be loaded.
     */
    private AncestorInfo createAncestorInfo() throws IllegalStateException {
        final CallContext callContext = getCallContext();
        return new AncestorInfo(callContext.getAncestorResourceURIs(),
                callContext.getAncestorResources());
    }

    private AncestorInfo get() throws IllegalStateException {
        AncestorInfo ancestorInfo = this.ancestorInfos.get();
        if (ancestorInfo != null) {
            return ancestorInfo;
        }
        ancestorInfo = createAncestorInfo();
        this.ancestorInfos.set(ancestorInfo);
        return ancestorInfo;
    }

    /**
     * @return
     * @see JaxRsUriInfo#getAbsolutePath()
     * @see UriInfo#getAbsolutePath()
     */
    public URI getAbsolutePath() {
        return getCallContext().getAbsolutePath();
    }

    /**
     * @return
     * @see JaxRsUriInfo#getAbsolutePathBuilder()
     * @see UriInfo#getAbsolutePathBuilder()
     */
    public UriBuilder getAbsolutePathBuilder() {
        return getCallContext().getAbsolutePathBuilder();
    }

    /**
     * @return
     * @see JaxRsUriInfo#getAncestorResources()
     * @see UriInfo#getAncestorResources()
     */
    public List<Object> getAncestorResources() {
        return get().getResources();
    }

    /**
     * @return
     * @see UriInfo#getAncestorResourceURIs()
     */
    public List<String> getAncestorResourceURIs() {
        return get().getUris(true);
    }

    /**
     * @param decode
     * @return
     * @see JaxRsUriInfo#getAncestorResourceURIs(boolean)
     * @see UriInfo#getAncestorResourceURIs(boolean)
     */
    public List<String> getAncestorResourceURIs(boolean decode) {
        return get().getUris(decode);
    }

    /**
     * @return
     * @see JaxRsUriInfo#getBaseUri()
     * @see UriInfo#getBaseUri()
     */
    public URI getBaseUri() {
        return getCallContext().getBaseUri();
    }

    /**
     * @return
     * @see JaxRsUriInfo#getBaseUriBuilder()
     * @see UriInfo#getBaseUriBuilder()
     */
    public UriBuilder getBaseUriBuilder() {
        return getCallContext().getBaseUriBuilder();
    }

    private CallContext getCallContext() throws IllegalStateException {
        return this.tlContext.get();
    }

    /**
     * @return
     * @see JaxRsUriInfo#getConnegExtension()
     * @see UriInfo#getConnegExtension()
     */
    public String getConnegExtension() {
        return getCallContext().getConnegExtension();
    }

    /**
     * @return
     * @see JaxRsUriInfo#getPath()
     * @see UriInfo#getPath()
     */
    public String getPath() {
        return getCallContext().getPath();
    }

    /**
     * @param decode
     * @return
     * @see JaxRsUriInfo#getPath(boolean)
     * @see UriInfo#getPath(boolean)
     */
    public String getPath(boolean decode) {
        return getCallContext().getPath(decode);
    }

    /**
     * @return
     * @see JaxRsUriInfo#getPathParameters()
     * @see UriInfo#getPathParameters()
     */
    public MultivaluedMap<String, String> getPathParameters() {
        return getCallContext().getPathParameters();
    }

    /**
     * @param decode
     * @return
     * @see JaxRsUriInfo#getPathParameters(boolean)
     * @see UriInfo#getPathParameters(boolean)
     */
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return getCallContext().getPathParameters(decode);
    }

    /**
     * @return
     * @see JaxRsUriInfo#getPathSegments()
     * @see UriInfo#getPathSegments()
     */
    public List<PathSegment> getPathSegments() {
        return getCallContext().getPathSegments();
    }

    /**
     * @param decode
     * @return
     * @see JaxRsUriInfo#getPathSegments(boolean)
     * @see UriInfo#getPathSegments(boolean)
     */
    public List<PathSegment> getPathSegments(boolean decode) {
        return getCallContext().getPathSegments(decode);
    }

    /**
     * @return
     * @see JaxRsUriInfo#getQueryParameters()
     * @see UriInfo#getQueryParameters()
     */
    public MultivaluedMap<String, String> getQueryParameters() {
        return getCallContext().getQueryParameters();
    }

    /**
     * @param decode
     * @return
     * @see JaxRsUriInfo#getQueryParameters(boolean)
     * @see UriInfo#getQueryParameters(boolean)
     */
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return getCallContext().getQueryParameters(decode);
    }

    /**
     * @return
     * @see JaxRsUriInfo#getRequestUri()
     * @see UriInfo#getRequestUri()
     */
    public URI getRequestUri() {
        return getCallContext().getRequestUri();
    }

    /**
     * @return
     * @see JaxRsUriInfo#getRequestUriBuilder()
     * @see UriInfo#getRequestUriBuilder()
     */
    public UriBuilder getRequestUriBuilder() {
        return getCallContext().getRequestUriBuilder();
    }

    /**
     * Removes the AncestorInfo for the current thread.
     */
    public void reset() {
        this.ancestorInfos.remove();
    }

    /**
     * Saves the current state of the ancestorResource(URI)s for the current
     * thread.
     * 
     * @param saveState
     *            if true, the current state is save for the current thread from
     *            the current {@link CallContext}; if false (for singeltons),
     *            the saved object for the current thread is removed.
     */
    public void saveStateForCurrentThread(boolean saveState) {
        if (saveState) {
            this.ancestorInfos.set(createAncestorInfo());
        } else {
            reset();
        }
    }
}