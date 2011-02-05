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

package org.restlet.ext.jaxrs.internal.core;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.restlet.ext.jaxrs.ExtendedUriInfo;

/**
 * A ThreadLocalizedUriInfo is used to inject, if a {@link UriInfo} is required
 * to inject. It must be new instantiated for every place to inject.
 * 
 * @author Stephan Koops
 * @see UriInfo
 * @see ThreadLocalizedContext
 */
public class ThreadLocalizedUriInfo implements UriInfo {

    private final ThreadLocal<MatchedInfo> matchedInfos = new ThreadLocal<MatchedInfo>();

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
     * @return the MatchedInfo with the current data from the
     *         {@link CallContext}.
     * @throws IllegalStateException
     *             if no CallContext could be loaded.
     */
    private MatchedInfo createAncestorInfo() throws IllegalStateException {
        final CallContext callContext = getCallContext();
        return new MatchedInfo(callContext.getMatchedURIs(),
                callContext.getMatchedResources());
    }

    private MatchedInfo get() throws IllegalStateException {
        MatchedInfo matchedInfo = this.matchedInfos.get();
        if (matchedInfo != null) {
            return matchedInfo;
        }
        matchedInfo = createAncestorInfo();
        this.matchedInfos.set(matchedInfo);
        return matchedInfo;
    }

    /**
     * @see JaxRsUriInfo#getAbsolutePath()
     * @see UriInfo#getAbsolutePath()
     */
    public URI getAbsolutePath() {
        return getCallContext().getAbsolutePath();
    }

    /**
     * @see JaxRsUriInfo#getAbsolutePathBuilder()
     * @see UriInfo#getAbsolutePathBuilder()
     * @see ExtendedUriInfo#getAbsolutePathBuilder()
     */
    public UriBuilder getAbsolutePathBuilder() {
        return getCallContext().getAbsolutePathBuilder();
    }

    /**
     * @see JaxRsUriInfo#getMatchedResources()
     * @see UriInfo#getMatchedResources()
     */
    public List<Object> getMatchedResources() {
        return get().getResources();
    }

    /**
     * @see UriInfo#getMatchedURIs()
     */
    public List<String> getMatchedURIs() {
        return get().getUris(true);
    }

    /**
     * @see JaxRsUriInfo#getAncestorResourceURIs(boolean)
     * @see UriInfo#getAncestorResourceURIs(boolean)
     */
    public List<String> getMatchedURIs(boolean decode) {
        return get().getUris(decode);
    }

    /**
     * @see JaxRsUriInfo#getBaseUri()
     * @see UriInfo#getBaseUri()
     */
    public URI getBaseUri() {
        return getCallContext().getBaseUri();
    }

    /**
     * @see JaxRsUriInfo#getBaseUriBuilder()
     * @see UriInfo#getBaseUriBuilder()
     */
    public UriBuilder getBaseUriBuilder() {
        return getCallContext().getBaseUriBuilder();
    }

    protected CallContext getCallContext() throws IllegalStateException {
        return this.tlContext.get();
    }

    /**
     * @see JaxRsUriInfo#getPath()
     * @see UriInfo#getPath()
     */
    public String getPath() {
        return getCallContext().getPath();
    }

    /**
     * @see JaxRsUriInfo#getPath(boolean)
     * @see UriInfo#getPath(boolean)
     */
    public String getPath(boolean decode) {
        return getCallContext().getPath(decode);
    }

    /**
     * @see JaxRsUriInfo#getPathParameters()
     * @see UriInfo#getPathParameters()
     */
    public MultivaluedMap<String, String> getPathParameters() {
        return getCallContext().getPathParameters();
    }

    /**
     * @see JaxRsUriInfo#getPathParameters(boolean)
     * @see UriInfo#getPathParameters(boolean)
     */
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return getCallContext().getPathParameters(decode);
    }

    /**
     * @see JaxRsUriInfo#getPathSegments()
     * @see UriInfo#getPathSegments()
     */
    public List<PathSegment> getPathSegments() {
        return getCallContext().getPathSegments();
    }

    /**
     * @see JaxRsUriInfo#getPathSegments(boolean)
     * @see UriInfo#getPathSegments(boolean)
     */
    public List<PathSegment> getPathSegments(boolean decode) {
        return getCallContext().getPathSegments(decode);
    }

    /**
     * @see JaxRsUriInfo#getQueryParameters()
     * @see UriInfo#getQueryParameters()
     */
    public MultivaluedMap<String, String> getQueryParameters() {
        return getCallContext().getQueryParameters();
    }

    /**
     * @see JaxRsUriInfo#getQueryParameters(boolean)
     * @see UriInfo#getQueryParameters(boolean)
     */
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return getCallContext().getQueryParameters(decode);
    }

    /**
     * @see JaxRsUriInfo#getRequestUri()
     * @see UriInfo#getRequestUri()
     */
    public URI getRequestUri() {
        return getCallContext().getRequestUri();
    }

    /**
     * @see JaxRsUriInfo#getRequestUriBuilder()
     * @see UriInfo#getRequestUriBuilder()
     */
    public UriBuilder getRequestUriBuilder() {
        return getCallContext().getRequestUriBuilder();
    }

    /**
     * Removes the MatchedInfo for the current thread.
     */
    public void reset() {
        this.matchedInfos.remove();
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
            this.matchedInfos.set(createAncestorInfo());
        } else {
            reset();
        }
    }
}