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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Converter;
import org.restlet.ext.jaxrs.util.Util;

/**
 * Implementation of the JAX-RS interface {@link UriInfo}.<br>
 * LATER This class may be refactored to a parent class which can be used
 * without the JAX-RS context
 * 
 * @author Stephan Koops
 * 
 */
public class JaxRsUriInfo implements UriInfo {

    // TODO throw IllegalStateException if called outside the scope of a request

    private static Logger logger = Logger.getLogger("JaxRsUriInfo.unexpected");

    private String baseUri;

    private List<PathSegment> pathSegmentsDecoded = null;

    private List<PathSegment> pathSegmentsEncoded = null;

    private MultivaluedMap<String, String> queryParametersDecoded;

    private MultivaluedMap<String, String> queryParametersEncoded;

    private boolean readOnly = false;

    protected Reference reference;

    private MultivaluedMap<String, String> templateParametersDecoded;

    /** is null, if no templateParameters given on creation */
    private MultivaluedMap<String, String> templateParametersEncoded;

    /**
     * Creates a new UriInfo. When using this constructor, the
     * templateParameters are not available.
     * 
     * @param reference
     *                The Restlet reference that will be wrapped. Must not be
     *                null and must have a base reference, see
     *                {@link Reference#getBaseRef()}.
     * 
     * @see #JaxRsUriInfo(Reference, MultivaluedMap)
     */
    public JaxRsUriInfo(Reference reference) {
        this(reference, true);
    }

    /**
     * Creates a new UriInfo. When using this constructor, the
     * templateParameters are not available.
     * 
     * @param reference
     *                The Restlet reference that will be wrapped. Must not be
     *                null and must have a base reference, see
     *                {@link Reference#getBaseRef()}.
     * @param readOnly
     * 
     * @see #JaxRsUriInfo(Reference, MultivaluedMap)
     */
    protected JaxRsUriInfo(Reference reference, boolean readOnly) {
        if (reference == null)
            throw new IllegalArgumentException("The reference must not be null");
        if (reference.getBaseRef() == null)
            throw new IllegalArgumentException(
                    "The reference must contains a baseRef");
        this.reference = reference;
        this.readOnly = readOnly;
    }

    /**
     * Checks, if this object is changeable. If not, a
     * {@link IllegalStateException} is thrown.
     * 
     * @throws IllegalStateException
     */
    protected void checkChangeable() throws IllegalStateException {
        if (!isChangeable())
            throw new IllegalStateException(
                    "The CallContext is no longer changeable");
    }

    private List<PathSegment> createPathSegments(boolean decode) {
        List<String> segments = this.reference.getRelativeRef().getSegments();
        List<PathSegment> pathSegments = new ArrayList<PathSegment>(segments
                .size());
        int l = segments.size();
        for (int i = 0; i < l; i++)
            pathSegments.add(new JaxRsPathSegment(segments.get(i), true,
                    decode, false, false, i));
        return Collections.unmodifiableList(pathSegments);
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof UriInfo))
            return false;
        UriInfo other = (UriInfo) anotherObject;
        if (!this.getBaseUri().equals(other.getBaseUri()))
            return false;
        if (!this.getPathSegments().equals(other.getPathSegments()))
            return false;
        if (!Util.equals(this.getTemplateParameters(), other
                .getTemplateParameters()))
            return false;
        return true;
    }

    /**
     * Get the absolute path of the request. This includes everything preceding
     * the path (host, port etc) but excludes query parameters and fragment.
     * This is a shortcut for
     * <code>uriInfo.getBase().resolve(uriInfo.getPath()).</code>
     * 
     * @return the absolute path of the request
     * @see UriInfo#getAbsolutePath()
     */
    public URI getAbsolutePath() {
        try {
            return new URI(reference.toString(false, false));
        } catch (URISyntaxException e) {
            throw Util.handleException(e, logger, "Could not create URI");
        }
    }

    /**
     * Get the absolute path of the request in the form of a UriBuilder. This
     * includes everything preceding the path (host, port etc) but excludes
     * query parameters and fragment.
     * 
     * @return a UriBuilder initialized with the absolute path of the request.
     * @see UriInfo#getAbsolutePathBuilder()
     */
    public UriBuilder getAbsolutePathBuilder() {
    	// LATER what happens, if the Reference is invalid for the UriBuilder?
        UriBuilder b = new JaxRsUriBuilder();
        b.encode(false);
        b.scheme(reference.getScheme(false));
        b.userInfo(reference.getUserInfo(false));
        b.host(reference.getHostDomain(false));
        b.port(reference.getHostPort());
        b.path(reference.getPath(false));
        b.replaceQueryParams(reference.getQuery(false));
        b.fragment(reference.getFragment(false));
        b.encode(true);
        return b;
    }

    /**
     * @see javax.ws.rs.core.UriInfo#getAncestorResources()
     */
    public List<Object> getAncestorResources() {
        // TODO UriInfo.getAncestorResources()
        throw new NotYetImplementedException();
    }

    /**
     * Get a list of URIs for ancestor resources. Each entry is a relative URI
     * that is a partial path that matched a resource class, a sub-resource 
     * method or a sub-resource locator. The entries are ordered according to 
     * request URI matching order, with the root resource URI first. E.g.:
     * 
     * <pre>&#064;Path("foo")
     *public class FooResource {
     *  &#064;GET
     *  public String getFoo() {...}
     * 
     *  &#064;Path("bar")
     *  &#064;GET
     *  public String getFooBar() {...}
     *}</pre>
     * 
     * <p>A request <code>GET /foo</code> would return an empty list since
     * <code>FooResource</code> is a root resource.</p>
     * 
     * <p>A request <code>GET /foo/bar</code> would return a list with one
     * entry: "foo".</p>
     * 
     * @return a list of URIs for ancestor resources.
     * @see javax.ws.rs.core.UriInfo#getAncestorResourceURIs()
     */
    public List<URI> getAncestorResourceURIs() {
        // TODO UriInfo.getAncestorResourceURIs()
        // REQUEST return last resource as first?
        // REQUEST UriInfo.getAncestorResourceURIs() and getAnchestorResources()
        // list should be unmodifiable
        throw new NotYetImplementedException();
    }

    /**
     * Get the base URI of the application. URIs of resource beans are all
     * relative to this base URI.
     * 
     * @return the base URI of the application
     * @see UriInfo#getBaseUri()
     */
    public URI getBaseUri() {
        try {
            return new URI(getBaseUriStr());
        } catch (URISyntaxException e) {
            throw Util.handleException(e, logger, "Could not create URI");
        }
    }

    /**
     * Get the absolute path of the request in the form of a UriBuilder. This
     * includes everything preceding the path (host, port etc) but excludes
     * query parameters and fragment.
     * 
     * @return a UriBuilder initialized with the absolute path of the request.
     * @see UriInfo#getAbsolutePathBuilder()
     * @see UriInfo#getBaseUriBuilder()
     */
    public UriBuilder getBaseUriBuilder() {
        return UriBuilder.fromUri(getBaseUriStr());
    }

    private String getBaseUriStr() {
        if (this.baseUri == null) {
            Reference baseRef = reference.getBaseRef();
            if (baseRef != null)
                this.baseUri = baseRef.toString(false, false);
        }
        return baseUri;
    }

    /**
     * Get the path of the current request relative to the base URI as a string.
     * All sequences of escaped octets are decoded, equivalent to
     * <code>getPath(true)</code>.
     * 
     * @return the relative URI path.
     * @see UriInfo#getPath()
     */
    public String getPath() {
        return getPath(true);
    }

    /**
     * Get the path of the current request relative to the base URI as a string.
     * 
     * @param decode
     *                controls whether sequences of escaped octets are decoded
     *                (true) or not (false).
     * @return the relative URI path.
     * @see UriInfo#getPath(boolean)
     */
    public String getPath(boolean decode) {
        String path = this.reference.getRelativeRef().toString(true, true);
        if (!decode)
            return path;
        return Reference.decode(path);
    }

    /**
     * Get the path of the current request relative to the base URI as a list of
     * {@link PathSegment}. This method is useful when the path needs to be
     * parsed, particularly when matrix parameters may be present in the path.
     * All sequences of escaped octets are decoded, equivalent to
     * <code>getPathSegments(true)</code>.
     * 
     * @return an unmodifiable list of {@link PathSegment}. The matrix
     *         parameter map of each path segment is also unmodifiable.
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see PathSegment
     * @see UriInfo#getPathSegments()
     */
    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    /**
     * Get the path of the current request relative to the base URI as a list of
     * {@link PathSegment}. This method is useful when the path needs to be
     * parsed, particularly when matrix parameters may be present in the path.
     * 
     * @param decode
     *                controls whether sequences of escaped octets are decoded
     *                (true) or not (false).
     * @return an unmodifiable list of {@link PathSegment}. The matrix
     *         parameter map of each path segment is also unmodifiable.
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see PathSegment
     * @see UriInfo#getPathSegments(boolean)
     */
    public List<PathSegment> getPathSegments(boolean decode) {
        if (decode) {
            if (this.pathSegmentsDecoded == null)
                this.pathSegmentsDecoded = createPathSegments(decode);
            return pathSegmentsDecoded;
        } else {
            if (this.pathSegmentsEncoded == null)
                this.pathSegmentsEncoded = createPathSegments(decode);
            return pathSegmentsEncoded;
        }
    }

    /**
     * Get the URI query parameters of the current request. All sequences of
     * escaped octets are decoded, equivalent to
     * <code>getQueryParameters(true)</code>.
     * 
     * @return an unmodifiable map of query parameter names and values
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see UriInfo#getQueryParameters()
     */
    public MultivaluedMap<String, String> getQueryParameters() {
        if (queryParametersDecoded == null)
            queryParametersDecoded = UnmodifiableMultivaluedMap.getFromForm(
                    reference.getQueryAsForm(), false);
        return queryParametersDecoded;
    }

    /**
     * @return an unmodifiable map of query parameter names and values
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see UriInfo#getQueryParameters(boolean)
     */
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        if (decode)
            return getQueryParameters();
        if (queryParametersEncoded == null) {
            Form queryForm = Converter.toFormEncoded(reference.getQuery(),
                    logger);
            queryParametersEncoded = UnmodifiableMultivaluedMap.getFromForm(
                    queryForm, false);
        }
        return queryParametersEncoded;
    }

    /**
     * @see UriInfo#getRequestUri()
     */
    public URI getRequestUri() {
        try {
            return new URI(reference.toString(true, true));
        } catch (URISyntaxException e) {
            throw Util.handleException(e, logger, "Could not create URI");
        }
    }

    /**
     * Get the absolute request URI in the form of a UriBuilder.
     * 
     * @return a UriBuilder initialized with the absolute request URI.
     * @see UriInfo#getRequestUriBuilder()
     */
    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(getRequestUri());
    }

    /**
     * Get the values of any embedded URI template parameters. All sequences of
     * escaped octets are decoded, equivalent to
     * <code>getTemplateParameters(true)</code>.
     * 
     * @return an unmodifiable map of parameter names and values
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see javax.ws.rs.Path
     * @see UriInfo#getTemplateParameters()
     */
    public MultivaluedMap<String, String> getTemplateParameters() {
        if (this.templateParametersDecoded == null) {
            MultivaluedMapImpl<String, String> templParamsDec = new MultivaluedMapImpl<String, String>();
            for (Map.Entry<String, List<String>> entryEnc : this
                    .interalGetTemplateParametersEncoded().entrySet()) {
                String keyDec = Reference.decode(entryEnc.getKey());
                List<String> valuesEnc = entryEnc.getValue();
                List<String> valuesDec = new ArrayList<String>(valuesEnc.size());
                for (String valueEnc : valuesEnc)
                    valuesDec.add(Reference.decode(valueEnc));
                templParamsDec.put(keyDec, valuesDec);
            }
            UnmodifiableMultivaluedMap<String, String> tpd;
            tpd = UnmodifiableMultivaluedMap.get(templParamsDec, false);
            if (isChangeable())
                return tpd;
            this.templateParametersDecoded = tpd;
        }
        return this.templateParametersDecoded;
    }

    /**
     * Get the values of any embedded URI template parameters.
     * 
     * @param decode
     *                controls whether sequences of escaped octets are decoded
     *                (true) or not (false).
     * @return an unmodifiable map of parameter names and values
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see javax.ws.rs.Path
     * @see UriInfo#getTemplateParameters(boolean)
     */
    public MultivaluedMap<String, String> getTemplateParameters(boolean decode) {
        if (decode) {
            return getTemplateParameters();
        } else {
            return UnmodifiableMultivaluedMap
                    .get(interalGetTemplateParametersEncoded());
        }
    }

    @Override
    public int hashCode() {
        return this.getBaseUriStr().hashCode()
                ^ this.getPathSegments().hashCode()
                ^ this.getTemplateParameters().hashCode();
    }

    /**
     * @return the templateParametersEncoded
     */
    protected MultivaluedMap<String, String> interalGetTemplateParametersEncoded() {
        if (templateParametersEncoded == null)
            this.templateParametersEncoded = new MultivaluedMapImpl<String, String>();
        return templateParametersEncoded;
    }

    protected boolean isChangeable() {
        return !this.readOnly;
    }

    /**
     * Sets this JaxRsUriInfo to be read only. This is the default. This method
     * is intended to be used by {@link CallContext#setReadOnly()}.
     */
    protected void setReadOnly() {
        this.readOnly = true;
    }

    /**
     * @param templateParametersEncoded
     *                the templateParametersEncoded to set
     */
    protected void setTemplateParametersEncoded(
            MultivaluedMap<String, String> templateParametersEncoded) {
        this.templateParametersEncoded = templateParametersEncoded;
    }

    @Override
    public String toString() {
        return this.reference.toString(true, false);
    }
}