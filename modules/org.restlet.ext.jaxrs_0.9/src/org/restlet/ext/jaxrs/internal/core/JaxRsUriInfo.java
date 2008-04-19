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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Implementation of the JAX-RS interface {@link UriInfo}.<br>
 * <!--NICE--> This class may be refactored to a parent class which can be used
 * without the JAX-RS context.
 * 
 * @author Stephan Koops
 */
public class JaxRsUriInfo implements UriInfo {

    private static Logger unexpectedLogger = Logger
            .getLogger("JaxRsUriInfo.unexpected");

    private LinkedList<Object> ancestorResources = new LinkedList<Object>();

    private List<Object> ancestorResourcesUnomd = Collections
            .unmodifiableList(ancestorResources);

    private LinkedList<String> ancestorResourceURIs = new LinkedList<String>();

    private List<String> ancestorResourceURIsUnomd = Collections
            .unmodifiableList(ancestorResourceURIs);

    private String baseUri;

    private Object lastAncestorResource;

    private String lastAncestorResourceURI;

    private MultivaluedMap<String, String> pathParametersDecoded;

    /** is null, if no templateParameters given on creation */
    private MultivaluedMap<String, String> pathParametersEncoded;

    private List<PathSegment> pathSegmentsDecoded = null;

    private List<PathSegment> pathSegmentsEncoded = null;

    private MultivaluedMap<String, String> queryParametersDecoded;

    private MultivaluedMap<String, String> queryParametersEncoded;

    private boolean readOnly = false;

    private Reference referenceOriginal;

    private Reference referenceCut;

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
        this(reference, reference, true);
    }

    /**
     * Creates a new UriInfo. When using this constructor, the
     * templateParameters are not available.
     * 
     * @param referenceOriginal
     *                The original Restlet reference that will be wrapped. Must
     *                not be null and must have a base reference, see
     *                {@link Reference#getBaseRef()}.
     * @param referenceCut
     *                The Restlet reference with the cut extensions.
     * @param readOnly
     * @see #JaxRsUriInfo(Reference, MultivaluedMap)
     */
    protected JaxRsUriInfo(Reference referenceOriginal, Reference referenceCut,
            boolean readOnly) {
        if (referenceCut == null)
            throw new IllegalArgumentException("The reference must not be null");
        if (referenceCut.getBaseRef() == null)
            throw new IllegalArgumentException(
                    "The reference must contains a baseRef");
        this.referenceCut = referenceCut;
        this.referenceOriginal = referenceOriginal;
        this.readOnly = readOnly;
    }

    /**
     * also useable after {@link #setReadOnly()}
     * 
     * @param resourceObject
     * @param newUriPart
     * @throws URISyntaxException
     * @see UriInfo#getAncestorResources()
     * @see UriInfo#getAncestorResourceURIs()
     */
    public void addForAncestor(Object resourceObject, String newUriPart) {
        if (resourceObject == null)
            throw new IllegalArgumentException(
                    "The resource object must not be null");
        if (newUriPart == null)
            throw new IllegalArgumentException(
                    "The new URI part must not be null");

        StringBuilder newUri = new StringBuilder();

        if (lastAncestorResourceURI != null) {
            if (newUriPart.length() == 0)
                throw new IllegalArgumentException(
                        "The new URI part must not be empty");
            ancestorResources.addFirst(lastAncestorResource);
            ancestorResourceURIs.addFirst(lastAncestorResourceURI);
            newUri.append(lastAncestorResourceURI);
        }

        if (newUriPart.length() == 0 || newUriPart.charAt(0) != '/')
            newUri.append('/');
        newUri.append(newUriPart);

        this.lastAncestorResource = resourceObject;
        this.lastAncestorResourceURI = newUri.toString();
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
        List<String> segments = this.getReferenceOriginal().getRelativeRef()
                .getSegments();
        int l = segments.size();
        List<PathSegment> pathSegments = new ArrayList<PathSegment>(l);
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
        if (!Util.equals(this.getPathParameters(), other.getPathParameters()))
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
            return new URI(getReferenceOriginal().toString(false, false));
        } catch (URISyntaxException e) {
            throw wrapUriSyntaxExc(e, unexpectedLogger, "Could not create URI");
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
        return createUriBuilder(getReferenceOriginal());
    }

    /**
     * @param ref
     * @return
     * @throws IllegalArgumentException
     */
    private UriBuilder createUriBuilder(Reference ref) {
        // NICE what happens, if the Reference is invalid for the UriBuilder?
        UriBuilder b = new JaxRsUriBuilder();
        b.encode(false);
        b.scheme(ref.getScheme(false));
        b.userInfo(ref.getUserInfo(false));
        b.host(ref.getHostDomain(false));
        b.port(ref.getHostPort());
        b.path(ref.getPath(false));
        b.replaceQueryParams(ref.getQuery(false));
        b.fragment(ref.getFragment(false));
        b.encode(true);
        return b;
    }

    /**
     * @see javax.ws.rs.core.UriInfo#getAncestorResources()
     */
    public List<Object> getAncestorResources() {
        return ancestorResourcesUnomd;
    }

    /**
     * Get a list of URIs for ancestor resources. Each entry is a relative URI
     * that is a partial path that matched a resource class, a sub-resource
     * method or a sub-resource locator. The entries are ordered according to
     * request URI matching order, with the root resource URI first. E.g.:
     * 
     * <pre>
     * &#064;Path(&quot;foo&quot;)
     * public class FooResource {
     *  &#064;GET
     *  public String getFoo() {...}
     * 
     *  &#064;Path(&quot;bar&quot;)
     *  &#064;GET
     *  public String getFooBar() {...}
     * </pre>
     * 
     * <p>
     * A request <code>GET /foo</code> would return an empty list since
     * <code>FooResource</code> is a root resource.
     * </p>
     * 
     * <p>
     * A request <code>GET /foo/bar</code> would return a list with one entry:
     * "foo".
     * </p>
     * 
     * @return a list of URIs for ancestor resources.
     * @see javax.ws.rs.core.UriInfo#getAncestorResourceURIs()
     */
    public List<String> getAncestorResourceURIs() {
        Logger
                .getAnonymousLogger()
                .config(
                        "UriInfo.getAncestorResourceURIs() is not checked for coded or encoded.");
        return ancestorResourceURIsUnomd;
    }

    /**
     * Get a read-only list of URIs for ancestor resources. Each entry is a
     * relative URI that is a partial path that matched a resource class, a
     * sub-resource method or a sub-resource locator. Entries do not include
     * query parameters but do include matrix parameters if present in the
     * request URI. Entries are ordered in reverse request URI matching order,
     * with the root resource URI last. E.g.:
     * 
     * <pre>
     * &#064;Path(&quot;foo&quot;)
     * public class FooResource {
     *   &#064;GET
     *   public String getFoo() {...}
     * 
     *   &#064;Path(&quot;bar&quot;)
     *   &#064;GET
     *   public String getFooBar() {...}
     * </pre>
     * 
     * <p>
     * A request <code>GET /foo</code> would return an empty list since
     * <code>FooResource</code> is a root resource.
     * </p>
     * 
     * <p>
     * A request <code>GET /foo/bar</code> would return a list with one entry:
     * "foo".
     * </p>
     * 
     * @param decode
     *                controls whether sequences of escaped octets are decoded
     *                (true) or not (false).
     * @return a read-only list of URI paths for ancestor resources.
     * @see javax.ws.rs.core.UriInfo#getAncestorResourceURIs(boolean)
     */
    public List<String> getAncestorResourceURIs(boolean decode) {
        Logger
                .getAnonymousLogger()
                .config(
                        "UriInfo.getAncestorResourceURIs() is not checked for coded or encoded.");
        // LATER JaxRsUriBuilder.getAncestorResourceURIs(boolean decode).
        if (decode && !decode)
            decode = false;
        return ancestorResourceURIsUnomd;
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
            throw wrapUriSyntaxExc(e, unexpectedLogger, "Could not create URI");
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
            Reference baseRef = getReferenceCut().getBaseRef();
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
        String path = this.getReferenceOriginal().getRelativeRef().toString(
                true, true);
        if (!decode)
            return path;
        return Reference.decode(path);
    }

    /**
     * Get the request URI extension. The returned string includes any
     * extensions remove during request pre-processing for the purposes of
     * URI-based content negotiation. E.g. if the request URI was:
     * 
     * <pre>
     * http://example.com/resource.xml.en
     * </pre>
     * 
     * this method would return "xml.en" even if an applications implementation
     * of {@link ApplicationConfig#getMediaTypeMappings()} returned a map that
     * included "xml" as a key
     * 
     * @return the request URI extension
     * @see javax.ws.rs.core.UriInfo#getPathExtension()
     */
    public String getPathExtension() {
        return referenceOriginal.getExtensions();
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
     * @see UriInfo#getPathParameters()
     */
    public MultivaluedMap<String, String> getPathParameters() {
        if (this.pathParametersDecoded == null) {
            MultivaluedMapImpl<String, String> pathParamsDec = new MultivaluedMapImpl<String, String>();
            for (Map.Entry<String, List<String>> entryEnc : this
                    .interalGetPathParamsEncoded().entrySet()) {
                String keyDec = Reference.decode(entryEnc.getKey());
                List<String> valuesEnc = entryEnc.getValue();
                List<String> valuesDec = new ArrayList<String>(valuesEnc.size());
                for (String valueEnc : valuesEnc)
                    valuesDec.add(Reference.decode(valueEnc));
                pathParamsDec.put(keyDec, valuesDec);
            }
            UnmodifiableMultivaluedMap<String, String> ppd;
            ppd = UnmodifiableMultivaluedMap.get(pathParamsDec, false);
            if (isChangeable())
                return ppd;
            this.pathParametersDecoded = ppd;
        }
        return this.pathParametersDecoded;
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
     * @see UriInfo#getPathParameters(boolean)
     */
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        if (decode) {
            return getPathParameters();
        } else {
            return UnmodifiableMultivaluedMap
                    .get(interalGetPathParamsEncoded());
        }
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
     * Get the absolute platonic request URI in the form of a UriBuilder. The
     * platonic request URI is the request URI minus any extensions that were
     * removed during request pre-processing for the purposes of URI-based
     * content negotiation. E.g. if the request URI was:
     * 
     * <pre>
     * http://example.com/resource.xml
     * </pre>
     * 
     * and an applications implementation of
     * {@link ApplicationConfig#getMediaTypeMappings} returned a map that
     * included "xml" as a key then the platonic request URI would be:
     * 
     * <pre>
     * http://example.com/resource
     * </pre>
     * 
     * @return a UriBuilder initialized with the absolute platonic request URI
     * @throws java.lang.IllegalStateException
     *                 if called outside the scope of a request
     * @see javax.ws.rs.core.UriInfo#getPlatonicRequestUriBuilder()
     */
    public UriBuilder getPlatonicRequestUriBuilder() {
        return createUriBuilder(getReferenceCut());
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
                    getReferenceOriginal().getQueryAsForm(), false);
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
            Form queryForm = Converter.toFormEncoded(getReferenceOriginal()
                    .getQuery(), unexpectedLogger);
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
            return new URI(getReferenceOriginal().toString(true, true));
        } catch (URISyntaxException e) {
            throw wrapUriSyntaxExc(e, unexpectedLogger, "Could not create URI");
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
     * @deprecated Use {@link #getPathParameters()} instead
     */
    @Deprecated
    public MultivaluedMap<String, String> getTemplateParameters() {
        return getPathParameters();
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
     * @deprecated Use {@link #getPathParameters(boolean)} instead
     */
    @Deprecated
    public MultivaluedMap<String, String> getTemplateParameters(boolean decode) {
        return getPathParameters(decode);
    }

    @Override
    public int hashCode() {
        return this.getBaseUriStr().hashCode()
                ^ this.getPathSegments().hashCode()
                ^ this.getPathParameters().hashCode();
    }

    /**
     * @return the pathParametersEncoded
     */
    protected MultivaluedMap<String, String> interalGetPathParamsEncoded() {
        if (pathParametersEncoded == null)
            this.pathParametersEncoded = new MultivaluedMapImpl<String, String>();
        return pathParametersEncoded;
    }

    protected boolean isChangeable() {
        return !this.readOnly;
    }

    /**
     * @param pathParametersEncoded
     *                the pathParametersEncoded to set
     */
    protected void setPathParametersEncoded(
            MultivaluedMap<String, String> templateParametersEncoded) {
        this.pathParametersEncoded = templateParametersEncoded;
    }

    /**
     * Sets this JaxRsUriInfo to be read only. This is the default. This method
     * is intended to be used by {@link CallContext#setReadOnly()}. Ignored by
     * {@link #addForAncestor(Object, String)}.
     */
    protected void setReadOnly() {
        this.readOnly = true;
    }

    @Override
    public String toString() {
        return this.getReferenceOriginal().toString(true, false);
    }

    /**
     * This method throws an {@link WebApplicationException} for Exceptions
     * where is no planned handling. Logs the exception (warn {@link Level}).
     * 
     * @param exc
     *                the catched URISyntaxException
     * @param unexpectedLogger
     *                the unexpectedLogger to log the messade
     * @param logMessage
     *                the message to log.
     * @return Will never return anything, because the generated
     *         WebApplicationException will be thrown. You an formally throw the
     *         returned exception (e.g. in a catch block). So the compiler is
     *         sure, that the method will be left here.
     * @throws WebApplicationException
     *                 contains the given {@link Exception}
     */
    private WebApplicationException wrapUriSyntaxExc(URISyntaxException exc,
            Logger logger, String logMessage) throws WebApplicationException {
        logger.log(Level.WARNING, logMessage, exc);
        exc.printStackTrace();
        throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @return the referenceOriginal
     */
    protected Reference getReferenceOriginal() {
        return referenceOriginal;
    }

    /**
     * @return the referenceCut
     */
    private Reference getReferenceCut() {
        return this.referenceCut;
    }
}