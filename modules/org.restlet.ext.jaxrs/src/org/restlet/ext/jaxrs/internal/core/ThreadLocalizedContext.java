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

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

/**
 * This class is used for thread local injection into providers and resources.
 * 
 * @author Stephan Koops
 * @see Request
 * @see HttpHeaders
 * @see SecurityContext
 * @see Providers
 * @see ContextResolver
 * @see CallContext
 */
public class ThreadLocalizedContext implements Request, HttpHeaders,
        SecurityContext {

    /**
     * The key of the {@link CallContext} in the
     * {@link org.restlet.Request} attributes.
     */
    private static final String CALLCONTEXT_KEY = "org.restlet.ext.jaxrs.CallContext";

    /**
     * @param lastModified
     * @return null if the preconditions are met or a ResponseBuilder set with
     *         the appropriate status if the preconditions are not met.
     * @see CallContext#evaluatePreconditions(java.util.Date)
     * @see Request#evaluatePreconditions(Date)
     */
    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        return get().evaluatePreconditions(lastModified);
    }

    /**
     * @param lastModified
     * @param entityTag
     * @return {@inheritDoc}
     * @see CallContext#evaluatePreconditions(java.util.Date,
     *      javax.ws.rs.core.EntityTag)
     * @see Request#evaluatePreconditions(Date, EntityTag)
     */
    public ResponseBuilder evaluatePreconditions(Date lastModified,
            EntityTag entityTag) {
        return get().evaluatePreconditions(lastModified, entityTag);
    }

    /**
     * @param entityTag
     * @return null if the preconditions are met or a ResponseBuilder set with
     *         the appropriate status if the preconditions are not met. A
     *         returned ResponseBuilder will include an ETag header set with the
     *         value of eTag.
     * @see CallContext#evaluatePreconditions(javax.ws.rs.core.EntityTag)
     * @see Request#evaluatePreconditions(EntityTag)
     */
    public ResponseBuilder evaluatePreconditions(EntityTag entityTag) {
        return get().evaluatePreconditions(entityTag);
    }

    /**
     * Returns the wrapped CallContext for the current Thread.
     * 
     * @return the wrapped CallContext for the current Thread. Never returns
     *         null.
     * @throws IllegalStateException
     *             if no {@link CallContext} was given for the current thread.
     *             If this occurs, their is a bug in this JAX-RS implementation.
     * @see #set(CallContext)
     */
    public CallContext get() throws IllegalStateException {
        final Object callContext = getRequestAttributes().get(CALLCONTEXT_KEY);
        if (callContext == null) {
            throw new IllegalStateException("No CallContext given until now");
        }
        return (CallContext) callContext;
    }

    /**
     * @see HttpHeaders#getAcceptableLanguages()
     */
    public List<Locale> getAcceptableLanguages() {
        return get().getAcceptableLanguages();
    }

    /**
     * @see CallContext#getAcceptableMediaTypes()
     * @see HttpHeaders#getAcceptableMediaTypes()
     */
    public List<MediaType> getAcceptableMediaTypes() {
        return get().getAcceptableMediaTypes();
    }

    /**
     * @see CallContext#getAuthenticationScheme()
     * @see SecurityContext#getAuthenticationScheme()
     */
    public String getAuthenticationScheme() {
        return get().getAuthenticationScheme();
    }

    /**
     * @see CallContext#getCookies()
     * @see HttpHeaders#getCookies()
     */
    public Map<String, Cookie> getCookies() {
        return get().getCookies();
    }

    /**
     * @see CallContext#getLanguage()
     * @see HttpHeaders#getLanguage()
     */
    public Locale getLanguage() {
        return get().getLanguage();
    }

    /**
     * @see CallContext#getMediaType()
     * @see HttpHeaders#getMediaType()
     */
    public MediaType getMediaType() {
        return get().getMediaType();
    }

    /**
     * @see javax.ws.rs.core.Request#getFormParameters()
     */
    public String getMethod() {
        return get().getMethod();
    }

    /**
     * Returns the attributes of the current Restlet
     * {@link org.restlet.Request}.
     * 
     * @return the attributes of the current Restlet Request, but never null
     */
    private Map<String, Object> getRequestAttributes() {
        return org.restlet.Request.getCurrent().getAttributes();
    }

    /**
     * @see CallContext#getRequestHeader(java.lang.String)
     * @see HttpHeaders#getRequestHeader(String)
     */
    public List<String> getRequestHeader(String name) {
        return get().getRequestHeader(name);
    }

    /**
     * @see CallContext#getRequestHeaders()
     * @see HttpHeaders#getRequestHeaders()
     */
    public MultivaluedMap<String, String> getRequestHeaders() {
        return get().getRequestHeaders();
    }

    /**
     * @see CallContext#getUserPrincipal()
     * @see SecurityContext#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return get().getUserPrincipal();
    }

    /**
     * @see CallContext#isSecure()
     * @see SecurityContext#isSecure()
     */
    public boolean isSecure() {
        return get().isSecure();
    }

    /**
     * @param role
     * @see CallContext#isUserInRole(java.lang.String)
     * @see SecurityContext#isUserInRole(String)
     */
    public boolean isUserInRole(String role) {
        return get().isUserInRole(role);
    }

    /**
     * @param variants
     * @throws IllegalArgumentException
     * @see CallContext#selectVariant(java.util.List)
     * @see Request#selectVariant(List)
     */
    public Variant selectVariant(List<Variant> variants)
            throws IllegalArgumentException {
        return get().selectVariant(variants);
    }

    /**
     * Sets the CallContext for the current thread. You MUST set a CallContext
     * here before you can get it by {@link #get()}.
     * 
     * @param callContext
     *            The CallContext for the current request; must not be null.
     * @see #reset()
     * @see #get()
     * @throws IllegalArgumentException
     *             if null was given.
     */
    public void set(CallContext callContext) throws IllegalArgumentException {
        if (callContext == null) {
            throw new IllegalArgumentException(
                    "You must give a CallContext here. null is not allowed");
        }
        getRequestAttributes().put(CALLCONTEXT_KEY, callContext);
    }

    /**
     * @return .
     * @see UriInfo#getPathSegments()
     */
    public List<PathSegment> getPathSegments() {
        return get().getPathSegments();
    }

}