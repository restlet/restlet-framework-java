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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.Guard;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Conditions;
import org.restlet.data.Dimension;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.jaxrs.AccessControl;
import org.restlet.ext.jaxrs.ThrowExcAccessControl;
import org.restlet.ext.jaxrs.util.Converter;
import org.restlet.ext.jaxrs.util.SortedMetadata;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.resource.Representation;
import org.restlet.util.Series;

/**
 * Implemetation of the JAX-RS interfaces {@link HttpHeaders}, {@link UriInfo}
 * and {@link Request}
 * 
 * @author Stephan Koops
 * 
 */
public class CallContext extends JaxRsUriInfo implements UriInfo, Request,
        HttpHeaders, SecurityContext {

    // TODO throw IllegalStateException if called outside the scope of a request

    private static final int STATUS_PREC_FAILED = Status.CLIENT_ERROR_PRECONDITION_FAILED
            .getCode();

    private List<MediaType> acceptedMediaTypes;

    private SortedMetadata<org.restlet.data.MediaType> accMediaTypes;

    private AccessControl accessControl;

    private Map<String, Cookie> cookies;

    private String language;

    private MediaType mediaType;

    private org.restlet.data.Request request;

    private UnmodifiableMultivaluedMap<String, String> requestHeaders;

    private org.restlet.data.Response response;

    /**
     * 
     * @param request
     *                The Restlet request to wrap. Must not be null.
     * @param templateParametersEncoded
     *                The template parameters. Must not be null.
     * @param response
     *                The Restlet response
     * @param accessControl
     *                The accessControl is needed to check, if a user is in a
     *                role, see {@link #isUserInRole(String)}. If null was
     *                given here and
     *                {@link SecurityContext#isUserInRole(String)} is called,
     *                the HTTP client will get an Internal Server Error as
     *                response.
     */
    public CallContext(org.restlet.data.Request request,
            org.restlet.data.Response response, AccessControl accessControl) {
        super((request == null) ? null : request.getResourceRef(), false);

        if (response == null)
            throw new IllegalArgumentException(
                    "The Restlet Response must not be null");
        if (request == null)
            throw new IllegalArgumentException(
                    "The Restlet Request must not be null");
        if (accessControl == null)
            throw new IllegalArgumentException(
                    "The AccessControl must not be null.");
        this.request = request;
        this.response = response;
        this.accessControl = accessControl;
        this.accMediaTypes = new SortedMetadata<org.restlet.data.MediaType>(
                request.getClientInfo().getAcceptedMediaTypes());
    }

    // HttpHeaders methods

    private boolean checkIfOneMatch(List<Tag> requestETags, Tag entityTag) {
        if (entityTag.isWeak())
            return false;
        for (Tag requestETag : requestETags) {
            if (entityTag.equals(requestETag))
                return true;
        }
        return false;
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
     * Evaluate request preconditions based on the passed in value.
     * 
     * @param lastModified
     *                a date that specifies the modification date of the
     *                resource
     * @return null if the preconditions are met or a ResponseBuilder set with
     *         the appropriate status if the preconditions are not met.
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * 
     * @see javax.ws.rs.core.Request#evaluatePreconditions(java.util.Date)
     * @see #evaluatePreconditions(Date, EntityTag)
     */
    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        return evaluatePreconditions(lastModified, null);
    }

    /**
     * Evaluate request preconditions based on the passed in value.
     * 
     * @param lastModified
     *                a date that specifies the modification date of the
     *                resource
     * @param entityTag
     *                an ETag for the current state of the resource
     * @return null if the preconditions are met or a ResponseBuilder set with
     *         the appropriate status if the preconditions are not met. A
     *         returned ResponseBuilder will include an ETag header set with the
     *         value of eTag.
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * 
     * @see javax.ws.rs.core.Request#evaluatePreconditions(java.util.Date,
     *      javax.ws.rs.core.EntityTag)
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.5">RFC
     *      2616, section 10.3.5: Status 304: Not Modiied</a>
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.13">RFC
     *      2616, section 10.4.13: Status 412: Precondition Failed</a>
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-13.3">RFC 2616,
     *      section 13.3: (Caching) Validation Model</a>
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.24">RFC 2616,
     *      section 14.24: Header "If-Match"</a>
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.25">RFC 2616,
     *      section 14.25: Header "If-Modified-Since"</a>
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.26">RFC 2616,
     *      section 14.26: Header "If-None-Match"</a>
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.28">RFC 2616,
     *      section 14.28: Header "If-Unmodified-Since"</a>
     */
    public ResponseBuilder evaluatePreconditions(Date lastModified,
            EntityTag entityTag) {
        if (lastModified == null && entityTag == null)
            return null;
        ResponseBuilder rb = null;
        Method requestMethod = request.getMethod();
        Conditions conditions = this.request.getConditions();
        if (lastModified != null) {
            // Header "If-Modified-Since"
            Date modSinceCond = conditions.getModifiedSince();
            if (modSinceCond != null) {
                if (modSinceCond.after(lastModified)) {
                    // the Entity was not changed
                    boolean readRequest = requestMethod.equals(Method.GET)
                            || requestMethod.equals(Method.HEAD);
                    if (readRequest) {
                        rb = Response.notModified();
                        rb.lastModified(lastModified);
                        rb.tag(entityTag);
                    } else {
                        return precFailed("The entity was not modified since "
                                + Util.formatDate(modSinceCond, false));
                    }
                } else {
                    // entity was changed -> check for other precoditions
                }
            }
            // Header "If-Unmodified-Since"
            Date unmodSinceCond = conditions.getUnmodifiedSince();
            if (unmodSinceCond != null) {
                if (unmodSinceCond.after(lastModified)) {
                    // entity was not changed -> Web Service must recalculate it
                    return null;
                } else {
                    // the Entity was changed
                    return precFailed("The entity was modified since "
                            + Util.formatDate(unmodSinceCond, false));
                }
            }
        }
        if (entityTag != null) {
            Tag actualEntityTag = Converter.toRestletTag(entityTag);
            // Header "If-Match"
            List<Tag> requestMatchETags = conditions.getMatch();
            if (!requestMatchETags.isEmpty()) {
                boolean match = checkIfOneMatch(requestMatchETags,
                        actualEntityTag);
                if (!match) {
                    return precFailed("The entity does not match Entity Tag "
                            + entityTag);
                }
            } else {
                // default answer to the request
            }
            // Header "If-None-Match"
            List<Tag> requestNoneMatchETags = conditions.getNoneMatch();
            if (!requestNoneMatchETags.isEmpty()) {
                boolean match = checkIfOneMatch(requestNoneMatchETags,
                        actualEntityTag);
                if (match) {
                    return precFailed("The entity matches Entity Tag "
                            + entityTag);
                }
            } else {
                // default answer to the request
            }
        }
        return rb;
    }

    /**
     * Evaluate request preconditions based on the passed in value.
     * 
     * @param entityTag
     *                an ETag for the current state of the resource
     * @return null if the preconditions are met or a ResponseBuilder set with
     *         the appropriate status if the preconditions are not met. A
     *         returned ResponseBuilder will include an ETag header set with the
     *         value of eTag.
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see javax.ws.rs.core.Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)
     * @see #evaluatePreconditions(Date, EntityTag)
     */
    public ResponseBuilder evaluatePreconditions(EntityTag entityTag) {
        return evaluatePreconditions(null, entityTag);
    }

    /**
     * For use from JAX-RS interface.
     * 
     * @see HttpHeaders#getAcceptableMediaTypes()
     * @see
     */
    @Deprecated
    public List<MediaType> getAcceptableMediaTypes() {
        if (this.acceptedMediaTypes == null) {
            List<Preference<org.restlet.data.MediaType>> restletAccMediaTypes = request
                    .getClientInfo().getAcceptedMediaTypes();
            List<MediaType> accMediaTypes = new ArrayList<MediaType>(
                    restletAccMediaTypes.size());
            for (Preference<org.restlet.data.MediaType> mediaTypePref : restletAccMediaTypes)
                accMediaTypes.add(createJaxRsMediaType(mediaTypePref));
            this.acceptedMediaTypes = Collections
                    .unmodifiableList(accMediaTypes);
        }
        return this.acceptedMediaTypes;
    }

    /**
     * Returns the accepted media types as Restlet
     * {@link org.restlet.data.MediaType}s.
     * 
     * @return the accepted {@link org.restlet.data.MediaType}s.
     */
    public SortedMetadata<org.restlet.data.MediaType> getAccMediaTypes() {
        return this.accMediaTypes;
    }

    /**
     * Returns the string value of the authentication scheme used to protect the
     * resource. If the resource is not authenticated, null is returned.
     * 
     * Values are the same as the CGI variable AUTH_TYPE
     * 
     * @return one of the static members BASIC_AUTH, FORM_AUTH,
     *         CLIENT_CERT_AUTH, DIGEST_AUTH (suitable for == comparison) or the
     *         container-specific string indicating the authentication scheme,
     *         or null if the request was not authenticated.
     * @see SecurityContext#getAuthenticationScheme()
     */
    public String getAuthenticationScheme() {
        Principal principal = Guard.getPrincipal(request);
        if (principal == null)
            return null;
        ChallengeResponse challengeResponse = request.getChallengeResponse();
        if (challengeResponse == null)
            return null;
        ChallengeScheme authScheme = challengeResponse.getScheme();
        if (authScheme == null)
            return null;
        if (authScheme.equals(ChallengeScheme.HTTP_BASIC))
            return SecurityContext.BASIC_AUTH;
        if (authScheme.equals(ChallengeScheme.HTTP_DIGEST))
            return SecurityContext.DIGEST_AUTH;
        return authScheme.getName();
        // LATER is SecurityContext.CLIENT_CERT_AUTH supported?
        // LATER FORM_AUTH wird wohl auch nicht unterstutzt.
    }

    /**
     * Get any cookies that accompanied the request.
     * 
     * @return a map of cookie name (String) to Cookie.
     * @see HttpHeaders#getCookies()
     */
    public Map<String, Cookie> getCookies() {
        if (this.cookies == null) {
            Map<String, Cookie> cookies = new HashMap<String, Cookie>();
            for (org.restlet.data.Cookie rc : request.getCookies()) {
                Cookie cookie = Converter.toJaxRsCookie(rc);
                cookies.put(cookie.getName(), cookie);
            }
            this.cookies = Collections.unmodifiableMap(cookies);
        }
        return this.cookies;
    }

    /**
     * @see HttpHeaders#getLanguage()
     */
    public String getLanguage() {
        if (this.language == null) {
            Representation entity = request.getEntity();
            if (entity == null)
                return null;
            List<Language> languages = entity.getLanguages();
            if (languages.isEmpty())
                return null;
            this.language = Util.getFirstElement(languages).getName();
        }
        return this.language;
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
            this.requestHeaders = UnmodifiableMultivaluedMap.getFromForm(Util
                    .getHttpHeaders(request), false);
        }
        return this.requestHeaders;
    }

    /**
     * Returns a <code>java.security.Principal</code> object containing the
     * name of the current authenticated user. If the user has not been
     * authenticated, the method returns null.
     * 
     * @return a <code>java.security.Principal</code> containing the name of
     *         the user making this request; null if the user has not been
     *         authenticated
     * @see SecurityContext#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return Guard.getPrincipal(request);
    }

    /**
     * Returns a boolean indicating whether this request was made using a secure
     * channel, such as HTTPS.
     * 
     * @return <code>true</code> if the request was made using a secure
     *         channel, <code>false</code> otherwise
     * @see SecurityContext#isSecure()
     */
    public boolean isSecure() {
        return this.request.isConfidential();
    }

    /**
     * Returns a boolean indicating whether the authenticated user is included
     * in the specified logical "role". If the user has not been authenticated,
     * the method returns <code>false</code>.
     * 
     * @param role
     *                a <code>String</code> specifying the name of the role
     * @return a <code>boolean</code> indicating whether the user making the
     *         request belongs to a given role; <code>false</code> if the user
     *         has not been authenticated
     * @see SecurityContext#isUserInRole(String)
     */
    public boolean isUserInRole(String role) {
        Principal principal = Guard.getPrincipal(request);
        if (this.accessControl == null)
            this.accessControl = ThrowExcAccessControl.getInstance();
        return accessControl.isUserInRole(principal, role);
    }

    /**
     * Creates a response with status 412 (Precondition Failed).
     * 
     * @param message
     *                Plain Text error message. Will be returned as entity.
     * @return Returns a response with status 412 (Precondition Failed) and the
     *         given message as entity.
     */
    private ResponseBuilder precFailed(String message) {
        ResponseBuilder rb = Response.status(STATUS_PREC_FAILED);
        rb.entity(message);
        rb.language(Language.ENGLISH.getName());
        rb.type(Converter.toJaxRsMediaType(
                org.restlet.data.MediaType.TEXT_PLAIN, null));
        return rb;
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
     *                 if variants is null or empty.
     * @see Request#selectVariant(List)
     */
    public Variant selectVariant(List<Variant> variants)
            throws IllegalArgumentException {
        if (variants == null || variants.isEmpty())
            throw new IllegalArgumentException();
        List<org.restlet.resource.Variant> restletVariants = Converter
                .toRestletVariants(variants);
        org.restlet.resource.Variant bestRestlVar = request.getClientInfo()
                .getPreferredVariant(restletVariants, null);
        Variant bestVariant = Converter.toJaxRsVariant(bestRestlVar);
        Set<Dimension> dimensions = response.getDimensions();
        if (bestRestlVar.getCharacterSet() != null)
            dimensions.add(Dimension.CHARACTER_SET);
        if (bestRestlVar.getEncodings() != null)
            dimensions.add(Dimension.ENCODING);
        if (bestRestlVar.getLanguages() != null)
            dimensions.add(Dimension.LANGUAGE);
        if (bestRestlVar.getMediaType() != null)
            dimensions.add(Dimension.MEDIA_TYPE);
        return bestVariant;
    }

    /**
     * Returns the Restlet {@link org.restlet.data.Request}
     * 
     * @return the Restlet {@link org.restlet.data.Request}
     */
    public org.restlet.data.Request getRequest() {
        return request;
    }

    /**
     * Returns the Restlet {@link org.restlet.data.Response}
     * 
     * @return the Restlet {@link org.restlet.data.Response}
     */
    public org.restlet.data.Response getResponse() {
        return response;
    }

    /**
     * @param annotation
     * @return
     */
    public String getLastTemplParamEnc(PathParam annotation) {
        String key = annotation.value();
        MultivaluedMap<String, String> templParamsEncoded = interalGetTemplateParametersEncoded();
        List<String> values = templParamsEncoded.get(key);
        if (values == null || values.isEmpty())
            return null;
        return Util.getLastElement(values);
    }

    /**
     * @param key
     * @param value
     */
    public void addTemplParamsEnc(String key, String value) {
        checkChangeable();
        interalGetTemplateParametersEncoded().add(key, value);
    }

    /**
     * @param matrixParamAnnot
     * @return
     */
    public String getLastMatrixParamEnc(MatrixParam matrixParamAnnot) {
        String mpName = matrixParamAnnot.value();
        List<PathSegment> pathSegments = getPathSegments(false);
        for (int i = pathSegments.size() - 1; i >= 0; i--) {
            PathSegment pathSegment = pathSegments.get(i);
            List<String> mpValues = pathSegment.getMatrixParameters().get(
                    mpName);
            if (mpValues != null && !mpValues.isEmpty()) {
                String result = Util.getLastElement(mpValues);
                if(result == null)
                    return "";
                return result;
            }
        }
        return null;
    }

    /**
     * Sets the Context to be read only. As from now changes are not allowed.
     */
    @Override
    public void setReadOnly() {
        super.setReadOnly();
    }
}