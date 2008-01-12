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
package org.restlet.ext.jaxrs.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.util.Series;

/**
 * Implemetation of the JAX-RS interfaces {@link HttpHeaders}, {@link UriInfo}
 * and {@link Request}
 * 
 * @author Stephan Koops
 * 
 */
public class HttpContextImpl extends JaxRsUriInfo implements UriInfo, Request,
        HttpHeaders {

    private org.restlet.data.Request restletRequest;

    private List<MediaType> acceptedMediaTypes;

    private Map<String, Cookie> cookies;

    private String language;

    private MediaType mediaType;

    private FormMulvaltivaluedMap requestHeaders;

    /**
     * 
     * @param restletRequest
     *                The Restlet request to wrap. Must not be null.
     * @param templateParametersEncoded
     *                The template parameters. Must not be null.
     */
    public HttpContextImpl(org.restlet.data.Request restletRequest,
            MultivaluedMap<String, String> templateParametersEncoded) {
        super(restletRequest.getResourceRef(), templateParametersEncoded);
        if (templateParametersEncoded == null)
            throw new IllegalArgumentException(
                    "The templateParameter must not be null");
        this.restletRequest = restletRequest;
    }

    // HttpHeaders methods

    /**
     * @see HttpHeaders#getAcceptableMediaTypes()
     */
    public List<MediaType> getAcceptableMediaTypes() {
        if (this.acceptedMediaTypes == null) {
            List<Preference<org.restlet.data.MediaType>> restletAccMediaTypes = restletRequest
                    .getClientInfo().getAcceptedMediaTypes();
            List<MediaType> accMediaTypes = new ArrayList<MediaType>(
                    restletAccMediaTypes.size());
            for (Preference<org.restlet.data.MediaType> mediaTypePref : restletAccMediaTypes)
                accMediaTypes.add(createJaxRsMediaType(mediaTypePref));
            this.acceptedMediaTypes = accMediaTypes;
        }
        return this.acceptedMediaTypes;
    }

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

    /**
     * Get any cookies that accompanied the request.
     * @return a map of cookie name (String) to Cookie.
     * @see HttpHeaders#getCookies()
     */
    public Map<String, Cookie> getCookies() {
        if (this.cookies == null) {
        	Map<String, Cookie> c = new HashMap<String, Cookie>();
        	for(org.restlet.data.Cookie rc : restletRequest.getCookies())
        	{
            	Cookie cookie = Util.convertCookie(rc);
            	c.put(cookie.getName(), cookie);
        	}
            this.cookies = c;
        }
        return this.cookies;
    }

    /**
     * @see HttpHeaders#getLanguage()
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @see HttpHeaders#getMediaType()
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }

    /**
     * @see HttpHeaders#getRequestHeaders()
     */
    public MultivaluedMap<String, String> getRequestHeaders() {
        if (this.requestHeaders == null) {
            this.requestHeaders = new FormMulvaltivaluedMap(Util
                    .getHttpHeaders(restletRequest));
        }
        return this.requestHeaders;
    }

    /**
     * Evaluate request preconditions based on the passed in value.
     * 
     * @param eTag
     *                an ETag for the current state of the resource
     * @return null if the preconditions are met or a Response that should be
     *         returned if the preconditions are not met.
     * 
     * @see javax.ws.rs.core.Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)
     */
    public Response evaluatePreconditions(EntityTag tag) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * Evaluate request preconditions based on the passed in value.
     * 
     * @param lastModified
     *                a date that specifies the modification date of the
     *                resource
     * @return null if the preconditions are met or a Response that should be
     *         returned if the preconditions are not met.
     * @see javax.ws.rs.core.Request#evaluatePreconditions(java.util.Date)
     */
    public Response evaluatePreconditions(Date lastModified) {
        Date modSinceCond = this.restletRequest.getConditions().getModifiedSince();
        if(modSinceCond == null)
            return null;
        if(modSinceCond.after(lastModified))   // if(2007.after(2008))
        {
            Method requestMethod = restletRequest.getMethod();
            if(requestMethod.equals(Method.GET) || requestMethod.equals(Method.HEAD))
            {
                ResponseBuilder rb = Response.notModified();
                return rb.build();
            }
            else
            {
                // wenn GET, dann 304, bei anderen Methoden andere ergebnisse (Precondition failed)
                throw new NotYetImplementedException("Only implemented for GET and HEAD");
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Evaluate request preconditions based on the passed in value.
     * 
     * @param lastModified
     *                a date that specifies the modification date of the
     *                resource
     * @param eTag
     *                an ETag for the current state of the resource
     * @return null if the preconditions are met or a Response that should be
     *         returned if the preconditions are not met.
     * @see javax.ws.rs.core.Request#evaluatePreconditions(java.util.Date,
     *      javax.ws.rs.core.EntityTag)
     */
    public Response evaluatePreconditions(Date lastModified, EntityTag tag) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * Select the representation variant that best matches the request. More
     * explicit variants are chosen ahead of less explicit ones. A vary header
     * is computed from the supplied list and automatically added to the
     * response.
     * 
     * @param variants
     *                a list of Variant that describe all of the available
     *                representation variants.
     * @return the variant that best matches the request.
     * @see Variant.VariantListBuilder
     * @throws IllegalArgumentException
     *                 if variants is empty
     * @see Request#selectVariant(List)
     */
    public Variant selectVariant(List<Variant> variants)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }
}