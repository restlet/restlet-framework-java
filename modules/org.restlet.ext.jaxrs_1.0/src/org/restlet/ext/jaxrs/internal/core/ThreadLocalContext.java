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
package org.restlet.ext.jaxrs.internal.core;

import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * This class is used for thread local injection in providers and resources.
 * 
 * @author Stephan Koops
 * @see UriInfo
 * @see Request
 * @see HttpHeaders
 * @see SecurityContext
 * @see CallContext
 */
public class ThreadLocalContext implements UriInfo, Request, HttpHeaders,
        SecurityContext {
    
    private ThreadLocal<CallContext> callContexts = new ThreadLocal<CallContext>();
    // XXX immer über Context.getInstance()

    /**
     * @param lastModified
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#evaluatePreconditions(java.util.Date)
     * @see Request#evaluatePreconditions(Date)
     */
    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        return this.callContexts.get().evaluatePreconditions(lastModified);
    }

    /**
     * @param lastModified
     * @param entityTag
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#evaluatePreconditions(java.util.Date,
     *      javax.ws.rs.core.EntityTag)
     * @see Request#evaluatePreconditions(Date, EntityTag)
     */
    public ResponseBuilder evaluatePreconditions(Date lastModified,
            EntityTag entityTag) {
        return this.callContexts.get().evaluatePreconditions(lastModified,
                entityTag);
    }

    /**
     * @param entityTag
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#evaluatePreconditions(javax.ws.rs.core.EntityTag)
     * @see Request#evaluatePreconditions(EntityTag)
     */
    public ResponseBuilder evaluatePreconditions(EntityTag entityTag) {
        return this.callContexts.get().evaluatePreconditions(entityTag);
    }

    /**
     * Returns the wrapped CallContext for the current Thread.
     * 
     * @return the wrapped CallContext for the current Thread.
     */
    public CallContext get() {
        // XXX use org.restlet.Context.getContext();
        return callContexts.get();
    }
    
    /**
     * Sets the CallContext for the current thread
     * @param callContext
     */
    public void set(CallContext callContext) {
        // XXX use org.restlet.Context.getContext();
        callContexts.set(callContext);
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getAbsolutePath()
     * @see UriInfo#getAbsolutePath()
     */
    public URI getAbsolutePath() {
        return this.callContexts.get().getAbsolutePath();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getAbsolutePathBuilder()
     * @see UriInfo#getAbsolutePathBuilder()
     */
    public UriBuilder getAbsolutePathBuilder() {
        return this.callContexts.get().getAbsolutePathBuilder();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getAcceptableMediaTypes()
     * @see HttpHeaders#getAcceptableMediaTypes()
     */
    @SuppressWarnings("deprecation")
    public List<MediaType> getAcceptableMediaTypes() {
        return this.callContexts.get().getAcceptableMediaTypes();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getAncestorResources()
     * @see UriInfo#getAncestorResources()
     */
    public List<Object> getAncestorResources() {
        return this.callContexts.get().getAncestorResources();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getAncestorResourceURIs()
     * @see UriInfo#getAncestorResourceURIs()
     */
    public List<String> getAncestorResourceURIs() {
        return this.callContexts.get().getAncestorResourceURIs();
    }

    /**
     * @param decode
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getAncestorResourceURIs(boolean)
     * @see UriInfo#getAncestorResourceURIs(boolean)
     */
    public List<String> getAncestorResourceURIs(boolean decode) {
        return this.callContexts.get().getAncestorResourceURIs(decode);
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getAuthenticationScheme()
     * @see SecurityContext#getAuthenticationScheme()
     */
    public String getAuthenticationScheme() {
        return this.callContexts.get().getAuthenticationScheme();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getBaseUri()
     * @see UriInfo#getBaseUri()
     */
    public URI getBaseUri() {
        return this.callContexts.get().getBaseUri();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getBaseUriBuilder()
     * @see UriInfo#getBaseUriBuilder()
     */
    public UriBuilder getBaseUriBuilder() {
        return this.callContexts.get().getBaseUriBuilder();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getCookies()
     * @see HttpHeaders#getCookies()
     */
    public Map<String, Cookie> getCookies() {
        return this.callContexts.get().getCookies();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getLanguage()
     * @see HttpHeaders#getLanguage()
     */
    public String getLanguage() {
        return this.callContexts.get().getLanguage();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getMediaType()
     * @see HttpHeaders#getMediaType()
     */
    public MediaType getMediaType() {
        return this.callContexts.get().getMediaType();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPath()
     * @see UriInfo#getPath()
     */
    public String getPath() {
        return this.callContexts.get().getPath();
    }

    /**
     * @param decode
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPath(boolean)
     * @see UriInfo#getPath(boolean)
     */
    public String getPath(boolean decode) {
        return this.callContexts.get().getPath(decode);
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPathExtension()
     * @see UriInfo#getPathExtension()
     */
    public String getPathExtension() {
        return this.callContexts.get().getPathExtension();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPathParameters()
     * @see UriInfo#getPathParameters()
     */
    public MultivaluedMap<String, String> getPathParameters() {
        return this.callContexts.get().getPathParameters();
    }

    /**
     * @param decode
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPathParameters(boolean)
     * @see UriInfo#getPathParameters(boolean)
     */
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return this.callContexts.get().getPathParameters(decode);
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPathSegments()
     * @see UriInfo#getPathSegments()
     */
    public List<PathSegment> getPathSegments() {
        return this.callContexts.get().getPathSegments();
    }

    /**
     * @param decode
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPathSegments(boolean)
     * @see UriInfo#getPathSegments(boolean)
     */
    public List<PathSegment> getPathSegments(boolean decode) {
        return this.callContexts.get().getPathSegments(decode);
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getPlatonicRequestUriBuilder()
     * @see UriInfo#getPlatonicRequestUriBuilder()
     */
    public UriBuilder getPlatonicRequestUriBuilder() {
        return this.callContexts.get().getPlatonicRequestUriBuilder();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getQueryParameters()
     * @see UriInfo#getQueryParameters()
     */
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.callContexts.get().getQueryParameters();
    }

    /**
     * @param decode
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getQueryParameters(boolean)
     * @see UriInfo#getQueryParameters(boolean)
     */
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return this.callContexts.get().getQueryParameters(decode);
    }

    /**
     * @param name
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getRequestHeader(java.lang.String)
     * @see HttpHeaders#getRequestHeader(String)
     */
    public List<String> getRequestHeader(String name) {
        return this.callContexts.get().getRequestHeader(name);
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getRequestHeaders()
     * @see HttpHeaders#getRequestHeaders()
     */
    public MultivaluedMap<String, String> getRequestHeaders() {
        return this.callContexts.get().getRequestHeaders();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getRequestUri()
     * @see UriInfo#getRequestUri()
     */
    public URI getRequestUri() {
        return this.callContexts.get().getRequestUri();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.JaxRsUriInfo#getRequestUriBuilder()
     * @see UriInfo#getRequestUriBuilder()
     */
    public UriBuilder getRequestUriBuilder() {
        return this.callContexts.get().getRequestUriBuilder();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#getUserPrincipal()
     * @see SecurityContext#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return this.callContexts.get().getUserPrincipal();
    }

    /**
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#isSecure()
     * @see SecurityContext#isSecure()
     */
    public boolean isSecure() {
        return this.callContexts.get().isSecure();
    }

    /**
     * @param role
     * @return
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#isUserInRole(java.lang.String)
     * @see SecurityContext#isUserInRole(String)
     */
    public boolean isUserInRole(String role) {
        return this.callContexts.get().isUserInRole(role);
    }

    /**
     * @param variants
     * @return
     * @throws IllegalArgumentException
     * @see org.restlet.ext.jaxrs.internal.core.CallContext#selectVariant(java.util.List)
     * @see Request#selectVariant(List)
     */
    public Variant selectVariant(List<Variant> variants)
            throws IllegalArgumentException {
        return this.callContexts.get().selectVariant(variants);
    }

    @Override
    public String toString() {
        return "ThreadLocal: " + this.callContexts.get().toString();
    }
}