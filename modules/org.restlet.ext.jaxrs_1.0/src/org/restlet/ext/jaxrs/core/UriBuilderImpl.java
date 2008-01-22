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

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.ext.jaxrs.wrappers.ResourceClass;
import org.restlet.util.Template;

/**
 * @author Stephan Koops
 * 
 */
public class UriBuilderImpl extends UriBuilder {

    private boolean encode = true;

    private Reference reference = new Reference();

    /**
     * 
     */
    public UriBuilderImpl() {
    }

    /**
     * Build a URI, any URI template parameters will be replaced by the empty
     * string. The <code>build</code> method does not change the state of the
     * <code>UriBuilder</code> and it may be called multiple times on the same
     * builder instance.
     * 
     * @return the URI built from the UriBuilder
     * @throws UriBuilderException
     *                 if there are any URI template parameters, or if a URI
     *                 cannot be constructed based on the current state of the
     *                 builder.
     * @see javax.ws.rs.core.UriBuilder#build()
     */
    @Override
    public URI build() throws UriBuilderException {
        return buildUri(toString());
    }

    /**
     * @param refAsString
     * @return
     * @throws UriBuilderException
     */
    private URI buildUri(String refAsString) throws UriBuilderException {
        try {
            return new URI(refAsString);
        } catch (URISyntaxException e) {
            throw new UriBuilderException("Could not build the URI", e);
        }
    }

    /**
     * Build a URI, any URI template parameters will be replaced by the value in
     * the supplied map. The <code>build</code> method does not change the
     * state of the <code>UriBuilder</code> and it may be called multiple
     * times on the same builder instance.
     * 
     * @param values
     *                a map of URI template parameter names and values
     * @return the URI built from the UriBuilder
     * @throws IllegalArgumentException
     *                 if automatic encoding is disabled and a supplied value
     *                 contains illegal characters, or if there are any URI
     *                 template parameters without a supplied value
     * @throws UriBuilderException
     *                 if a URI cannot be constructed based on the current state
     *                 of the builder.
     * @see javax.ws.rs.core.UriBuilder#build(java.util.Map)
     */
    @Override
    @SuppressWarnings("unchecked")
    public URI build(Map<String, String> values)
            throws IllegalArgumentException, UriBuilderException {
        Template template = new Template(toString());
        return buildUri(template.format((Map) values));
    }

    /**
     * Build a URI, using the supplied values in order to replace any URI
     * template parameters. The <code>build</code> method does not change the
     * state of the <code>UriBuilder</code> and it may be called multiple
     * times on the same builder instance.
     * <p>
     * All instances of the same template parameter will be replaced by the same
     * value that corresponds to the position of the first instance of the
     * template parameter. e.g. the template "{a}/{b}/{a}" with values {"x",
     * "y", "z"} will result in the the URI "x/y/x", <i>not</i> "x/y/z".
     * 
     * @param values
     *                a list of URI template parameter values
     * @return the URI built from the UriBuilder
     * @throws IllegalArgumentException
     *                 if automatic encoding is disabled and a supplied value
     *                 contains illegal characters, or if there are any URI
     *                 template parameters without a supplied value
     * @throws UriBuilderException
     *                 if a URI cannot be constructed based on the current state
     *                 of the builder.
     * @see javax.ws.rs.core.UriBuilder#build(java.lang.String[])
     */
    @Override
    public URI build(String... values) throws IllegalArgumentException,
            UriBuilderException {
        // TODO UriBuilderImpl.build(String... values)
        throw new NotYetImplementedException();
    }

    /**
     * Create a copy of the UriBuilder preserving its state. This is a more
     * efficient means of creating a copy than constructing a new UriBuilder
     * from a URI returned by the {@link #build} method.
     * 
     * @return a copy of the UriBuilder
     * @see javax.ws.rs.core.UriBuilder#clone()
     */
    @Override
    public UriBuilder clone() {
        UriBuilderImpl uriBuilder = new UriBuilderImpl();
        uriBuilder.encode = this.encode;
        uriBuilder.reference = this.reference.clone();
        return uriBuilder;
    }

    /**
     * Controls whether the UriBuilder will automatically encode URI components
     * added by subsequent operations or not. Defaut value is true
     * 
     * @param enable
     *                automatic encoding (true) or disable it (false). If false,
     *                subsequent components added must be valid with all illegal
     *                characters already escaped.
     * @return the updated UriBuilder
     * @see javax.ws.rs.core.UriBuilder#encode(boolean)
     * @see UriBuilder
     */
    @Override
    public UriBuilder encode(boolean enable) {
        this.encode = enable;
        return this;
    }

    /**
     * Encodes the given string, if encoding is enabled. If encoding is
     * disabled, the methods checks the validaty of the containing characters.
     * 
     * @param string
     *                the string to encode or check. Must not be null; result
     *                are not defined.
     * @param errMessName
     *                The name for the message
     * @return
     * @throws IllegalArgumentException
     *                 if the char is invalid.
     */
    private String encode(String string, String errMessName)
            throws IllegalArgumentException {
        if (string == null)
            throw new IllegalArgumentException("The " + errMessName
                    + " must not be null");
        if (this.encode)
            return Reference.encode(string);
        if (Reference.encode(string).length() != string.length())
            throw new IllegalArgumentException("The " + errMessName
                    + " contains illegal characters");
        return string;
    }

    /**
     * Set the URI fragment using an unencoded value.
     * 
     * @param fragment
     *                the URI fragment, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if fragment is null, or if automatic encoding is disabled
     *                 and fragment contains illegal characters
     * @see javax.ws.rs.core.UriBuilder#fragment(java.lang.String)
     */
    @Override
    public UriBuilder fragment(String fragment) throws IllegalArgumentException {
        this.reference.setFragment(encode(fragment, "fragment"));
        return this;
    }

    /**
     * Set the URI host.
     * 
     * @return the updated UriBuilder
     * @param host
     *                the URI host, may contain URI template parameters
     * @throws IllegalArgumentException
     *                 if host is invalid or is null
     * @see javax.ws.rs.core.UriBuilder#host(java.lang.String)
     */
    @Override
    public UriBuilder host(String host) throws IllegalArgumentException {
        if (host == null)
            throw new IllegalArgumentException("The host must not be null");
        // LATER check host name for invalid characters
        reference.setHostDomain(host);
        return this;
    }

    /**
     * Append a matrix parameter to the existing set of matrix parameters of the
     * current final segment of the URI path. Note that the matrix parameters
     * are tied to a particular path segment; subsequent addition of path
     * segments will not affect their position in the URI path.
     * 
     * @param name
     *                the matrix parameter name, may contain URI template
     *                parameters
     * @param value
     *                the matrix parameter value, may contain URI template
     *                parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if name or value is null, or if automatic encoding is
     *                 disabled and name or value contains illegal characters
     * @see javax.ws.rs.core.UriBuilder#matrixParam(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public UriBuilder matrixParam(String name, String value)
            throws IllegalArgumentException {
        String oldPath = reference.getPath();
        reference.setPath(oldPath + ";" + encode(name, "matrix parameter name")
                + "=" + encode(value, "matrix parameter value"));
        return this;
    }

    /**
     * Append path segments from a Path-annotated class to the existing list of
     * segments. When constructing the final path, each segment will be
     * separated by '/' if necessary. The value of the encode property of the
     * Path annotation will be used when processing the value of the
     * 
     * @Path but it will not be used to modify the state of automaic encoding
     *       for the builder.
     * 
     * @param resource
     *                a resource whose
     * @Path value will be used to obtain the path segment.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if resource is null, or if resource.encode is false and
     *                 resource.value contains illegal characters, or if
     *                 resource is not annotated with UrPath
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public UriBuilder path(Class resource) throws IllegalArgumentException {
        if (resource == null)
            throw new IllegalArgumentException(
                    "The root resource class must not be null");
        Path path = ResourceClass.getPathAnnotation(resource);
        if (path == null)
            throw new IllegalArgumentException(
                    "The given class is no root resource class, because it is not annotated with @Path");
        this.path(encode(path.value(), "root resource class path"));
        // LATER encode Path
        return this;
    }

    /**
     * Append path segments from a Path-annotated method to the existing list of
     * segments. When constructing the final path, each segment will be
     * separated by '/' if necessary. This method is a convenience shortcut to
     * <code>path(Method)</code>, it can only be used in cases where there is
     * a single method with the specified name that is annotated with
     * 
     * @Path.
     * 
     * @param resource
     *                the resource containing the method
     * @param method
     *                the name of the method whose
     * @UPathvalue will be used to obtain the path segment
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if resource or method is null, or if the specified method
     *                 does not exist, or there is more than or less than one
     *                 variant of the method annotated with UriPath
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.Class, java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public UriBuilder path(Class resource, String method)
            throws IllegalArgumentException {
        // TODO UriBuilderImpl.path(Class resource, String method)
        throw new NotYetImplementedException();
    }

    /**
     * Append path segments from a list of Path-annotated methods to the
     * existing list of segments. When constructing the final path, each segment
     * will be separated by '/' if necessary. The value of the encode property
     * of the Path annotation will be used when processing the value of the
     * 
     * @Path but it will not be used to modify the state of automaic encoding
     *       for the builder.
     * 
     * @param methods
     *                a list of methods whose
     * @Path values will be used to obtain the path segments
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if any element of methods is null or is not annotated
     *                 with a UrPath
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.reflect.Method[])
     */
    @Override
    public UriBuilder path(Method... methods) throws IllegalArgumentException {
        // TODO UriBuilder.path(Method...)
        throw new NotYetImplementedException();
    }

    /**
     * Append path segments to the existing list of segments. When constructing
     * the final path, each segment will be separated by '/' if necessary.
     * Existing '/' characters are preserved thus a single segment value can
     * represent multiple URI path segments.
     * 
     * @param segments
     *                the path segments, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if any element of segments is null, or if automatic
     *                 encoding is disabled and any element of segments contains
     *                 illegal characters
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.String[])
     */
    @Override
    public UriBuilder path(String... segments) throws IllegalArgumentException {
        if (segments == null)
            throw new IllegalArgumentException("The segments must not be null");
        // first check preconditions
        for (int i = 0; i < segments.length; i++)
            segments[i] = encode(segments[i], i
                    + ". segment of the path (index started with 0)");
        // than add segments
        for (String segment : segments)
            reference.addSegment(segment);
        return this;
    }

    /**
     * Set the URI port.
     * 
     * @param port
     *                the URI port, a value of -1 will unset an explicit port.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if port is invalid
     * @see javax.ws.rs.core.UriBuilder#port(int)
     */
    @Override
    public UriBuilder port(int port) throws IllegalArgumentException {
        if (port < 0)
            this.reference.setHostPort(null);
        else
            this.reference.setHostPort(port);
        return this;
    }

    /**
     * Append a query parameter to the existing set of query parameters.
     * 
     * @param name
     *                the query parameter name, may contain URI template
     *                parameters
     * @param value
     *                the query parameter value, may contain URI template
     *                parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if name or value is null, or if automatic encoding is
     *                 disabled and name or value contains illegal characters
     * @see javax.ws.rs.core.UriBuilder#queryParam(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public UriBuilder queryParam(String name, String value)
            throws IllegalArgumentException {
        name = encode(name, "query parameter name");
        value = encode(name, "query parameter value");
        String oldQuery = reference.getQuery();
        StringBuilder newQuery;
        if (oldQuery == null) {
            newQuery = new StringBuilder();
        } else {
            newQuery = new StringBuilder(oldQuery);
            newQuery.append('&');
        }
        newQuery.append(name);
        newQuery.append('=');
        newQuery.append(value);
        reference.setQuery(newQuery.toString());
        return this;
    }

    /**
     * Set the matrix parameters of the current final segment of the current URI
     * path. This method will overwrite any existing matrix parameters on the
     * current final segment of the current URI path. Note that the matrix
     * parameters are tied to a particular path segment; subsequent addition of
     * path segments will not affect their position in the URI path.
     * 
     * @param matrix
     *                the matrix parameters, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if matrix cannot be parsed or is null, or if automatic
     *                 encoding is disabled and any matrix parameter name or
     *                 value contains illegal characters
     * @see javax.ws.rs.core.UriBuilder#replaceMatrixParams(java.lang.String)
     */
    @Override
    public UriBuilder replaceMatrixParams(String matrix)
            throws IllegalArgumentException {
        // TODO UriBuilder.replaceMatrixParams(String matrix)
        throw new NotYetImplementedException();
    }

    /**
     * Set the URI path. This method will overwrite any existing path segments
     * and associated matrix parameters.
     * 
     * @param path
     *                the URI path, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if automatic encoding is disabled and path contains
     *                 illegal characters, or if path is null
     * @see javax.ws.rs.core.UriBuilder#replacePath(java.lang.String)
     */
    @Override
    public UriBuilder replacePath(String path) throws IllegalArgumentException {
        reference.setPath(encode(path, "path"));
        return this;
    }

    /**
     * Set the URI query string. This method will overwrite any existing query
     * parameters.
     * 
     * @param query
     *                the URI query string, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if query cannot be parsed or is null, or if automatic
     *                 encoding is disabled and any query parameter name or
     *                 value contains illegal characters
     * @see javax.ws.rs.core.UriBuilder#replaceQueryParams(java.lang.String)
     */
    @Override
    public UriBuilder replaceQueryParams(String query)
            throws IllegalArgumentException {
        if (query == null)
            throw new IllegalArgumentException("The query must not be null");
        // LATER check query param
        reference.setQuery(query);
        return this;
    }

    /**
     * Set the URI scheme.
     * 
     * @param scheme
     *                the URI scheme, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if scheme is invalid or is null
     * @see javax.ws.rs.core.UriBuilder#scheme(java.lang.String)
     */
    @Override
    public UriBuilder scheme(String scheme) throws IllegalArgumentException {
        if (scheme == null)
            throw new IllegalArgumentException("The scheme must not be null");
        Util.checkValidScheme(scheme);
        reference.setScheme(scheme);
        return this;
    }

    /**
     * Set the URI scheme-specific-part (see {@link java.net.URI}). This method
     * will overwrite any existing values for authority, user-info, host, port
     * and path.
     * 
     * @param ssp
     *                the URI scheme-specific-part, may contain URI template
     *                parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if ssp cannot be parsed or is null
     * @see javax.ws.rs.core.UriBuilder#schemeSpecificPart(java.lang.String)
     */
    @Override
    public UriBuilder schemeSpecificPart(String ssp)
            throws IllegalArgumentException {
        if (ssp == null)
            throw new IllegalArgumentException(
                    "The scheme specific part must not be null");
        // TODO check for invalid chars.
        reference.setSchemeSpecificPart(ssp);
        return this;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        return reference.toString(true, true);
    }

    /**
     * Copies the non-null components of the supplied URI to the UriBuilder
     * replacing any existing values for those components.
     * 
     * @param uri
     *                the URI to copy components from
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if uri is null
     * @see javax.ws.rs.core.UriBuilder#uri(java.net.URI)
     */
    @Override
    public UriBuilder uri(URI uri) throws IllegalArgumentException {
        Util.copyUriToReference(uri, this.reference);
        return this;
    }

    /**
     * Set the URI user-info.
     * 
     * @param ui
     *                the URI user-info, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *                 if automatic encoding is disabled and ui contains illegal
     *                 characters, or if ui is null
     * @see javax.ws.rs.core.UriBuilder#userInfo(java.lang.String)
     */
    @Override
    public UriBuilder userInfo(String ui) throws IllegalArgumentException {
        reference.setUserInfo(encode(ui, "userInfo"));
        return this;
    }
}