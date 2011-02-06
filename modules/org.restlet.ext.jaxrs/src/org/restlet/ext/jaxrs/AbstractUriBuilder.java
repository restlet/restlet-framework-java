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

package org.restlet.ext.jaxrs;

import static org.restlet.ext.jaxrs.internal.util.Util.append;
import static org.restlet.ext.jaxrs.internal.util.Util.getPathTemplateWithoutRegExps;
import static org.restlet.ext.jaxrs.internal.util.Util.indexBeginMatrixOfLastSegment;
import static org.restlet.ext.jaxrs.internal.util.Util.notEndsWith;
import static org.restlet.ext.jaxrs.internal.util.Util.notStartsWith;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.EncodeOrCheck;
import org.restlet.routing.Template;
import org.restlet.util.Resolver;

/**
 * Abstract implementation of interface {@link UriBuilder}. Not intended for
 * public use.
 * 
 * @author Stephan Koops
 * @see UriBuilder
 */
public abstract class AbstractUriBuilder extends UriBuilder {

    private class ArrayVariableResolver extends Resolver<String> {
        private final boolean encoding;

        private int i = 0;

        private final Map<String, String> retrievedValues = new HashMap<String, String>();

        private final Object[] values;

        ArrayVariableResolver(Object[] values, boolean encoding) {
            this.values = values;
            this.encoding = encoding;
        }

        @Override
        public String resolve(String variableName) {
            String varValue = this.retrievedValues.get(variableName);
            if (varValue == null) {
                if (this.i >= this.values.length) {
                    throw new IllegalArgumentException(
                            "The value given array contains not enough elements (contains "
                                    + this.values.length
                                    + ", but need at least " + (this.i + 1)
                                    + ")");
                }
                final Object value = this.values[this.i];
                if (value == null) {
                    throw new IllegalArgumentException(
                            "The given array contains null value at position ("
                                    + this.i + ")");
                }
                varValue = value.toString();
                varValue = EncodeOrCheck.all(varValue, this.encoding);
                this.i++;
                this.retrievedValues.put(variableName, varValue);
            }
            return varValue;
        }
    }

    /**
     * {@link String} or {@link StringBuilder}. It is flexible to avoid
     * unnecessary converting.
     */
    private CharSequence fragment;

    /**
     * no converting or appending necessary.
     */
    private String host;

    /**
     * {@link String} or {@link StringBuilder}. It is flexible to avoid
     * unnecessary converting.
     */
    private CharSequence path;

    private String port = null;

    /**
     * {@link String} or {@link StringBuilder}. It is flexible to avoid
     * unnecessary converting.
     */
    private CharSequence query;

    /**
     * no converting or appending necessary.
     */
    private String scheme;

    /**
     * {@link String} or {@link StringBuilder}. It is flexible to avoid
     * unnecessary converting.
     */
    private CharSequence userInfo;

    protected AbstractUriBuilder() {
    }

    /**
     * adds a valid (encoded or checked) path segment. The path may start with
     * "/" or not, but must not be null.
     * 
     * @param path
     * @param newPathSegment
     */
    private void addValidPathSegment(CharSequence newPathSegment) {
        final StringBuilder path = getPath();
        if ((this.host != null) && notEndsWith(path, '/')
                && notStartsWith(newPathSegment, '/')) {
            path.append('/');
        }
        path.append(newPathSegment);
    }

    /**
     * adds a valid (encoded or checked) path segment. The elements may start
     * with "/" or not, but must not be null.
     * 
     * @param newPathSegments
     */
    private void addValidPathSegments(List<CharSequence> newPathSegments) {
        for (final CharSequence newPathSegment : newPathSegments) {
            addValidPathSegment(newPathSegment);
        }
    }

    /**
     * Build a URI, using the supplied values in order to replace any URI
     * template parameters. Values are converted to <code>String</code> using
     * their <code>toString</code> method and are then encoded to match the
     * rules of the URI component to which they pertain. All '%' characters in
     * the stringified values will be encoded. The state of the builder is
     * unaffected; this method may be called multiple times on the same builder
     * instance.
     * <p>
     * All instances of the same template parameter will be replaced by the same
     * value that corresponds to the position of the first instance of the
     * template parameter. e.g. the template "{a}/{b}/{a}" with values {"x",
     * "y", "z"} will result in the the URI "x/y/x", <i>not</i> "x/y/z".
     * 
     * @param values
     *            a list of URI template parameter values
     * @return the URI built from the UriBuilder
     * @throws IllegalArgumentException
     *             if there are any URI template parameters without a supplied
     *             value, or if a value is null.
     * @throws UriBuilderException
     *             if a URI cannot be constructed based on the current state of
     *             the builder.
     * @see javax.ws.rs.core.UriBuilder#build(java.lang.Object[])
     */
    @Override
    public URI build(Object... values) throws IllegalArgumentException,
            UriBuilderException {
        final Template template = new Template(toStringWithCheck(false));
        return buildUri(template
                .format(new ArrayVariableResolver(values, true)));
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#buildFromEncoded(java.lang.Object[])
     */
    @Override
    public URI buildFromEncoded(Object... values)
            throws IllegalArgumentException, UriBuilderException {
        final Template template = new Template(toStringWithCheck(false));
        return buildUri(template
                .format(new ArrayVariableResolver(values, false)));
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#buildFromEncodedMap(java.util.Map)
     */
    @Override
    public URI buildFromEncodedMap(Map<String, ? extends Object> values)
            throws IllegalArgumentException, UriBuilderException {
        return this.buildFromMap(values, false);
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#buildFromMap(java.util.Map)
     */
    @Override
    public URI buildFromMap(Map<String, ? extends Object> values)
            throws IllegalArgumentException, UriBuilderException {
        return this.buildFromMap(values, true);
    }

    /**
     * Build a URI, any URI template parameters will be replaced by the value in
     * the supplied map. The <code>build</code> method does not change the state
     * of the <code>UriBuilder</code> and it may be called multiple times on the
     * same builder instance.
     * 
     * @param values
     *            a map of URI template parameter names and values
     * @param encode
     *            true, if the value should be encoded, or false if not.
     * @return the URI built from the UriBuilder
     * @throws IllegalArgumentException
     *             if automatic encoding is disabled and a supplied value
     *             contains illegal characters, or if there are any URI template
     *             parameters without a supplied value
     * @throws UriBuilderException
     *             if a URI cannot be constructed based on the current state of
     *             the builder.
     * @see javax.ws.rs.core.UriBuilder#build(java.util.Map)
     */
    private URI buildFromMap(final Map<String, ? extends Object> values,
            final boolean encode) throws IllegalArgumentException,
            UriBuilderException {
        final Template template = new Template(toStringWithCheck(false));
        return buildUri(template.format(new Resolver<String>() {
            @Override
            public String resolve(String variableName) {
                final Object varValue = values.get(variableName);
                if (varValue == null) {
                    throw new IllegalArgumentException(
                            "The value Map must contain a value for all given Templet variables. The value for variable "
                                    + variableName + " is missing");
                }
                return EncodeOrCheck.all(varValue.toString(), encode);
            }
        }));
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
            throw new UriBuilderException(
                    "Could not build the URI from String " + refAsString, e);
        }
    }

    /**
     * @param uriBuilder
     */
    protected void copyInto(AbstractUriBuilder uriBuilder) {
        if (this.fragment != null) {
            uriBuilder.fragment = this.fragment;
            this.fragment = this.fragment.toString();
        }
        uriBuilder.host = this.host;
        uriBuilder.port = this.port;
        uriBuilder.scheme = this.scheme;
        if (this.userInfo != null) {
            uriBuilder.userInfo = this.userInfo;
            this.userInfo = this.userInfo.toString();
        }
        if (this.path != null) {
            uriBuilder.path = this.path;
            this.path = this.path.toString();
        }
        if (this.query != null) {
            uriBuilder.query = this.query;
            this.query = this.query.toString();
            // copy this.query to new query, because typically the clone will
            // be changed and not the orignal.
        }
    }

    /**
     * Set the URI fragment using an unencoded value.
     * 
     * @param fragment
     *            the URI fragment, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if fragment is null, or if automatic encoding is disabled and
     *             fragment contains illegal characters
     * @see javax.ws.rs.core.UriBuilder#fragment(java.lang.String)
     */
    @Override
    public UriBuilder fragment(String fragment) throws IllegalArgumentException {
        if (fragment == null) {
            this.fragment = null;
        } else {
            this.fragment = EncodeOrCheck.fragment(fragment);
        }
        return this;
    }

    /**
     * @return the extension of the current state, with a "." at beginning. Is
     *         intended to be overriden in subclass ExtendedUriBuilder. Must not
     *         return null, "" or ".".
     */
    protected String getExtension() {
        return null;
    }

    /**
     * @return The path as StringBuilder. Ensures, that the returned
     *         StringBuilder is available in the instance variable.
     */
    private StringBuilder getPath() {
        StringBuilder path;
        if (this.path instanceof StringBuilder) {
            return (StringBuilder) this.path;
        }
        if (this.path == null) {
            path = new StringBuilder();
            this.path = path;
        } else {
            path = new StringBuilder(this.path);
            this.path = path;
        }
        return path;
    }

    /**
     * Set the URI host.
     * 
     * @return the updated UriBuilder
     * @param host
     *            the URI host, may contain URI template parameters
     * @throws IllegalArgumentException
     *             if host is invalid or is null
     * @see javax.ws.rs.core.UriBuilder#host(java.lang.String)
     */
    @Override
    public UriBuilder host(String host) throws IllegalArgumentException {
        if (host == null) {
            this.host = null;
        } else {
            this.host = EncodeOrCheck.host(host);
        }
        return this;
    }

    /**
     * Append a matrix parameter to the existing set of matrix parameters of the
     * current final segment of the URI path. If multiple values are supplied
     * the parameter will be added once per value. Note that the matrix
     * parameters are tied to a particular path segment; subsequent addition of
     * path segments will not affect their position in the URI path.
     * 
     * @param name
     *            the matrix parameter name, may contain URI template parameters
     * @param values
     *            the matrix parameter value(s), each object will be converted
     *            to a {@code String} using its {@code toString()} method.
     *            Stringified values may contain URI template parameters.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if name or value is null, or if automatic encoding is
     *             disabled and the name or any stringified value contains
     *             illegal characters
     * @see <a href="http://www.w3.org/DesignIssues/MatrixURIs.html">Matrix
     *      URIs</a>
     * @see javax.ws.rs.core.UriBuilder#matrixParam(String, Object...)
     */
    @Override
    public UriBuilder matrixParam(String name, Object... values)
            throws IllegalArgumentException {
        if (values == null) {
            throw new IllegalArgumentException("The values must not be null");
        }
        CharSequence ncs;
        ncs = EncodeOrCheck.nameOrValue(name, true, "matrix parameter name");
        final List<String> valuesStr = new ArrayList<String>();
        for (final Object value : values) {
            final String vcs = EncodeOrCheck.nameOrValue(value, true,
                    "matrix parameter value");
            valuesStr.add(vcs);
        }
        final StringBuilder path = getPath();
        for (final String vcs : valuesStr) {
            path.append(';');
            path.append(ncs);
            path.append('=');
            path.append(vcs);
        }
        return this;
    }

    /**
     * Appends the given path to the current path.S
     * 
     * @param pathToAppend
     * @return the current UriBuilder
     * @throws IllegalArgumentException
     */
    protected UriBuilder path(CharSequence pathToAppend)
            throws IllegalArgumentException {
        if (pathToAppend == null)
            throw new IllegalArgumentException(
                    "the path to append must not be null");
        CharSequence validPathSegment = EncodeOrCheck.pathSegmentsWithMatrix(
                pathToAppend, true);
        addValidPathSegment(validPathSegment);
        return this;
    }

    /**
     * Append path segments from a Path-annotated class to the existing list of
     * segments. When constructing the final path, each segment will be
     * separated by '/' if necessary. The value of the encode property of the
     * Path annotation will be used when processing the value of the
     * &#64;<!---->{@link Path} but it will not be used to modify the state of
     * automatic encoding for the builder.
     * 
     * @param rootResource
     *            a resource whose &#64;Path value will be used to obtain the
     *            path segment.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if resource is null, or if resource.encode is false and
     *             resource.value contains illegal characters, or if resource is
     *             not annotated with UrPath
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.Class)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public UriBuilder path(Class rootResource) throws IllegalArgumentException {
        if (rootResource == null) {
            throw new IllegalArgumentException(
                    "The root resource class must not be null");
        }
        String newPathSegment;
        try {
            newPathSegment = getPathTemplateWithoutRegExps(rootResource);
        } catch (IllegalPathOnClassException e) {
            throw e.getCause();
        } catch (MissingAnnotationException e) {
            throw new IllegalArgumentException("The class "
                    + rootResource.getName()
                    + " is not a valid root resource class. "
                    + "A root resource class must be annotated with @Path");
        }
        addValidPathSegment(newPathSegment);
        return this;
    }

    /**
     * Append path segments from a Path-annotated method to the existing list of
     * segments. When constructing the final path, each segment will be
     * separated by '/' if necessary. This method is a convenience shortcut to
     * <code>path(Method)</code>, it can only be used in cases where there is a
     * single method with the specified name that is annotated with &#64;<!---->
     * {@link Path}.
     * 
     * @param rootResource
     *            the root resource class containing the method.
     * @param methodName
     *            the name of the method whose &#64;{@link Path} value will be
     *            used to obtain the path segment.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if resource or method is null, or if the specified method
     *             does not exist, or there is more than or less than one
     *             variant of the method annotated with UriPath
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public UriBuilder path(Class rootResource, String methodName)
            throws IllegalArgumentException {
        if (methodName == null) {
            throw new IllegalArgumentException(
                    "The method name must not be null");
        }
        String resMethodPath = null;
        for (final Method method : rootResource.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            String path;
            try {
                path = getPathTemplateWithoutRegExps(method);
            } catch (IllegalPathOnMethodException e) {
                throw e.getCause();
            } catch (MissingAnnotationException e) {
                throw new IllegalArgumentException(e);
            }
            if (path == null) {
                continue;
            }
            if ((resMethodPath != null) && !resMethodPath.equals(path)) {
                throw new IllegalArgumentException("The class " + rootResource
                        + " has more than one methods with the name "
                        + methodName + " annotated with @Path");
            }
            resMethodPath = path;
        }
        if (resMethodPath == null) {
            throw new IllegalArgumentException("The class " + rootResource
                    + " has no method with the name " + methodName
                    + " annotated with @Path");
        }
        addValidPathSegment(resMethodPath);
        return this;
    }

    /**
     * Append the path from a {@link javax.ws.rs.Path}-annotated method to the
     * existing path. When constructing the final path, a '/' separator will be
     * inserted between the existing path and the supplied path if necessary.
     * 
     * @param method
     *            a method whose {@link javax.ws.rs.Path} value will be used to
     *            obtain the path to append to the existing path
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if any element of methods is null or is not annotated with a
     *             {@link javax.ws.rs.Path}
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.reflect.Method)
     */
    @Override
    public UriBuilder path(Method method) throws IllegalArgumentException {
        if (method == null) {
            return this;
        }
        String validSegment;
        try {
            validSegment = getPathTemplateWithoutRegExps(method);
        } catch (MissingAnnotationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalPathException e) {
            throw e.getCause();
        }
        addValidPathSegment(validSegment);
        return this;
    }

    /**
     * Append path to the existing path. When constructing the final path, a '/'
     * separator will be inserted between the existing path and the supplied
     * path if necessary. Existing '/' characters are preserved thus a single
     * value can represent multiple URI path segments.
     * 
     * @param pathToAppend
     *            the path to append to the current path, may contain URI
     *            template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if path is null
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.String)
     */
    @Override
    public UriBuilder path(String pathToAppend) throws IllegalArgumentException {
        path((CharSequence) pathToAppend);
        return this;
    }

    /**
     * append the given path segments to the current path
     * 
     * @param segments
     * @return
     */
    private List<CharSequence> pathSegmentsWithMatrix(String[] segments) {
        if (segments == null) {
            return new ArrayList<CharSequence>(0);
        }
        final int length = segments.length;
        final List<CharSequence> r = new ArrayList<CharSequence>(length);
        if (length == 0) {
            return r;
        }
        final String s = segments[0];
        if ((s == null) || (s.length() == 0)) {
            return r;
        }
        r.add(EncodeOrCheck.pathSegmentWithMatrix(s, true));
        for (int i = 1; i < length; i++) {
            r.add(EncodeOrCheck.pathSegmentWithMatrix(segments[i], true));
        }
        return r;
    }

    /**
     * Set the URI port.
     * 
     * @param port
     *            the URI port, a value of -1 will unset an explicit port.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if port is invalid
     * @see javax.ws.rs.core.UriBuilder#port(int)
     */
    @Override
    public UriBuilder port(int port) throws IllegalArgumentException {
        if (port == -1) {
            this.port = null;
        } else if (port > 65535 || port < -1) {
            throw new IllegalArgumentException(
                    "The port must between zero and 65535 or -1 to unset the explizit port");
        } else {
            this.port = String.valueOf(port);
        }
        return this;
    }

    /**
     * Set the URI port. Only integers or a variable template is allowed
     * 
     * @param port
     *            the URI port (null will unset an explicit port) or a template
     *            variable.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if given value is invalid
     * @see #port(int)
     */
    public UriBuilder port(String port) throws IllegalArgumentException {
        if (port == null) {
            this.port = null;
        } else if (port.startsWith("{") && port.endsWith("}")) {
            this.port = port;
        } else {
            this.port(Integer.parseInt(port));
        }
        return this;
    }

    /**
     * Append a query parameter to the existing set of query parameters. If
     * multiple values are supplied the parameter will be added once per value.
     * 
     * @param name
     *            the query parameter name, may contain URI template parameters
     * @param values
     *            the query parameter value(s), each object will be converted to
     *            a {@code String} using its {@code toString()} method.
     *            Stringified values may contain URI template parameters.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if name or value is null, or if automatic encoding is
     *             disabled and name or value contains illegal characters
     * @see javax.ws.rs.core.UriBuilder#queryParam(String, Object...)
     */
    @Override
    public UriBuilder queryParam(String name, Object... values)
            throws IllegalArgumentException {
        if (values == null) {
            throw new IllegalArgumentException("The values must not be null");
        }
        CharSequence ncs;
        ncs = EncodeOrCheck.nameOrValue(name, true, "query parameter name");
        final List<String> valuesStr = new ArrayList<String>();
        for (final Object value : values) {
            final String vcs = EncodeOrCheck.nameOrValue(value, true,
                    "query parameter value");
            valuesStr.add(vcs);
        }
        final Iterator<String> valueIter = valuesStr.iterator();
        StringBuilder query;
        if (this.query == null) {
            query = new StringBuilder();
            this.query = query;
        } else if (this.query instanceof StringBuilder) {
            query = (StringBuilder) this.query;
            query.append('&');
        } else {
            query = new StringBuilder(this.query.toString());
            query.append('&');
        }
        query.append(ncs);
        query.append('=');
        query.append(valueIter.next());
        while (valueIter.hasNext()) {
            query.append('&');
            query.append(ncs);
            query.append('=');
            query.append(valueIter.next());
        }
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
     *            the matrix parameters, may contain URI template parameters. A
     *            null value will remove all matrix parameters of the current
     *            final segment of the current URI path.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if matrix cannot be parsed, or if automatic encoding is
     *             disabled and any matrix parameter name or value contains
     *             illegal characters
     * @see javax.ws.rs.core.UriBuilder#replaceMatrix(String)
     */
    @Override
    public UriBuilder replaceMatrix(String matrix)
            throws IllegalArgumentException {
        CharSequence mpcs = null;
        if (matrix != null) {
            mpcs = EncodeOrCheck.fullMatrix(matrix);
        }
        final StringBuilder path = getPath();
        // remove matrix params from last path segment
        final int beginLastPs = path.lastIndexOf("/");
        if (beginLastPs >= 0) {
            final int beginMp = path.indexOf(";", beginLastPs);
            if (beginMp >= 0) {
                path.delete(beginMp, path.length());
            }
        }
        // add new matrix parameters
        if (mpcs == null) {
            return this;
        }
        if ((mpcs.length() == 0) || (mpcs.charAt(0) != ';')) {
            path.append(";");
        }
        path.append(mpcs);
        return this;
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#replaceMatrixParam(java.lang.String,
     *      java.lang.Object[])
     */
    @Override
    public UriBuilder replaceMatrixParam(String name, Object... values)
            throws IllegalArgumentException {
        // TODO replace matrix parameters
        throw new NotYetImplementedException(
                "Sorry, the relacement of matrix parameters is not supported yet");
    }

    /**
     * Replaces the current path with the given path. This method checks, if
     * there is an extension in, if needed by this class.
     * 
     * @param newPath
     *            the new path.
     */
    protected UriBuilder replacePath(CharSequence newPath)
            throws IllegalArgumentException {
        // TODO check for extension in subclass
        if (newPath == null
                || (newPath.length() > 0 && newPath.charAt(0) == '/')) {
            this.path = newPath;
        } else {
            this.path = null;
            path(newPath);
        }
        return this;
    }

    /**
     * Set the URI path. This method will overwrite any existing path and
     * associated matrix parameters. Existing '/' characters are preserved thus
     * a single value can represent multiple URI path segments.
     * 
     * @param newPath
     *            the path to replace the old path with, may contain URI
     *            template parameters. A null value will unset the path
     *            component of the URI.
     * @return the updated UriBuilder
     * @see javax.ws.rs.core.UriBuilder#replacePath(java.lang.String)
     */
    @Override
    public UriBuilder replacePath(String newPath)
            throws IllegalArgumentException {
        return replacePath((CharSequence) newPath);
    }

    /**
     * Set the URI query string. This method will overwrite any existing query
     * parameters.
     * 
     * @param query
     *            the URI query string, may contain URI template parameters. A
     *            null value will remove all query parameters.
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if query cannot be parsed
     * @see javax.ws.rs.core.UriBuilder#replaceQuery(java.lang.String)
     */
    @Override
    public UriBuilder replaceQuery(String query)
            throws IllegalArgumentException {
        if ((query == null) || (query.length() == 0)) {
            this.query = null;
        } else {
            this.query = EncodeOrCheck.fullQuery(query, true);
        }
        return this;
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#replaceQueryParam(String, Object[])
     */
    @Override
    public UriBuilder replaceQueryParam(String name, Object... values)
            throws IllegalArgumentException {
        throw new NotYetImplementedException(
                "Sorry, the relacement of query parameters is not supported yet");
    }

    /**
     * Set the URI scheme.
     * 
     * @param scheme
     *            the URI scheme, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if scheme is invalid or is null
     * @see javax.ws.rs.core.UriBuilder#scheme(java.lang.String)
     */
    @Override
    public UriBuilder scheme(String scheme) throws IllegalArgumentException {
        this.scheme = EncodeOrCheck.scheme(scheme);
        return this;
    }

    /**
     * Set the URI scheme-specific-part (see {@link java.net.URI}). This method
     * will overwrite any existing values for authority, user-info, host, port
     * and path.
     * 
     * @param ssp
     *            the URI scheme-specific-part, may contain URI template
     *            parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if ssp cannot be parsed or is null
     * @see javax.ws.rs.core.UriBuilder#schemeSpecificPart(java.lang.String)
     */
    @Override
    public UriBuilder schemeSpecificPart(String ssp)
            throws IllegalArgumentException {
        if (ssp == null) {
            throw new IllegalArgumentException(
                    "The scheme specific part must not be null");
        }
        if (ssp.startsWith("//")) {
            ssp = ssp.substring(2);
        }
        // split schemeSpecificPart
        final int firstSlashPos = ssp.indexOf('/');
        String authority;
        CharSequence path;
        CharSequence query;
        CharSequence fragment;
        if (firstSlashPos >= 0) {
            authority = ssp.substring(0, firstSlashPos);
            final String pathQueryFragment = ssp.substring(firstSlashPos);
            String pathQuery;
            final int firstCrosshatchPos = pathQueryFragment.indexOf('#');
            if (firstCrosshatchPos >= 0) {
                pathQuery = pathQueryFragment.substring(0, firstCrosshatchPos);
                fragment = pathQueryFragment.substring(firstCrosshatchPos + 1);
            } else {
                pathQuery = pathQueryFragment;
                fragment = null;
            }
            final int firstQmPos = pathQuery.indexOf('?');
            if (firstQmPos >= 0) {
                path = pathQuery.substring(0, firstQmPos);
                query = pathQuery.substring(firstQmPos + 1);
            } else {
                path = pathQuery;
                query = null;
            }
        } else {
            authority = ssp;
            path = null;
            query = null;
            fragment = null;
        }
        CharSequence userInfo;
        String host;
        String port;
        final int atSignPos = authority.lastIndexOf('@');
        if (atSignPos >= 0) {
            userInfo = authority.substring(0, atSignPos);
            authority = authority.substring(atSignPos + 1);
        } else {
            userInfo = null;
        }
        final int colonPos = authority.lastIndexOf(':');
        if (colonPos >= 0) {
            host = authority.substring(0, colonPos);
            port = authority.substring(colonPos + 1);
        } else {
            host = authority;
            port = null;
        }
        // check / convert values
        if (userInfo != null) {
            userInfo = EncodeOrCheck.userInfo(userInfo, true);
        }
        if (host != null) {
            host = EncodeOrCheck.host(host);
        }
        if (path != null) {
            path = EncodeOrCheck.pathSegmentsWithMatrix(path, true);
        }
        if (query != null) {
            query = EncodeOrCheck.fullQuery(query, true);
        }
        if (fragment != null) {
            fragment = EncodeOrCheck.fragment(fragment);
        }
        // check and set max. one: the port
        port(port);
        // set checked / converted
        this.userInfo = userInfo;
        this.host = host;
        this.replacePath(path);
        this.query = query;
        this.fragment = fragment;
        return this;
    }

    /**
     * Append path segments to the existing path. When constructing the final
     * path, a '/' separator will be inserted between the existing path and the
     * first path segment if necessary and each supplied segment will also be
     * separated by '/'. Existing '/' characters are encoded thus a single value
     * can only represent a single URI path segment.
     * 
     * @param segments
     *            the path segment values, each may contain URI template
     *            parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if segments or any element of segments is null
     * @see javax.ws.rs.core.UriBuilder#segment(java.lang.String[])
     */
    @Override
    public UriBuilder segment(String... segments)
            throws IllegalArgumentException {
        if (segments == null) {
            throw new IllegalArgumentException("The segments must not be null");
        }
        // first check preconditions
        final List<CharSequence> newPathSegments = pathSegmentsWithMatrix(segments);
        // than add
        addValidPathSegments(newPathSegments);
        return this;
    }

    /**
     * Returns the actual URI as String.
     * 
     * @return the actual URI as String.
     * @see #toStringWithCheck(boolean)
     */
    @Override
    public String toString() {
        return this.toString(false);
    }

    /**
     * Returns the actual URI as String.
     * 
     * @param convertBraces
     *            if true, all braces are converted, if false then not.
     * 
     * @return the actual URI as {@link String}. Never returns null.
     * @see #toStringWithCheck(boolean)
     */
    private String toString(boolean convertBraces) {
        try {
            final StringBuilder stb = new StringBuilder();
            if (this.scheme != null) {
                append(stb, this.scheme, convertBraces);
                stb.append("://");
            }
            if (this.userInfo != null) {
                append(stb, this.userInfo, convertBraces);
                stb.append('@');
            }
            if (this.host != null) {
                append(stb, this.host, convertBraces);
            }
            if (this.port != null) {
                stb.append(':');
                stb.append(this.port);
            }
            String extension = getExtension();
            CharSequence thisPath = this.path;
            if (thisPath != null || extension != null) {
                if (stb.length() > 0) {
                    if (notStartsWith(thisPath, '/')
                            || ((thisPath == null) && (extension != null))) {
                        stb.append('/');
                    }
                }
                if (extension == null) {
                    append(stb, thisPath, convertBraces);
                } else {
                    int begLastMatrix = indexBeginMatrixOfLastSegment(thisPath);
                    if (begLastMatrix >= 0) {
                        append(stb, thisPath, convertBraces, 0, begLastMatrix);
                        append(stb, extension, convertBraces);
                        append(stb, thisPath, convertBraces, begLastMatrix);
                    } else { // no matrix parameters found
                        append(stb, thisPath, convertBraces);
                        append(stb, extension, convertBraces);
                    }
                }
            }
            if (this.query != null) {
                stb.append('?');
                append(stb, this.query, convertBraces);
            }
            if (this.fragment != null) {
                stb.append('#');
                append(stb, this.fragment, convertBraces);
            }
            return stb.toString();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not write the UriBuilder to a String; but this Exception could not occur normally",
                    e);
        }
    }

    /**
     * Returns the actual URI as String. Check for valid scheme and host before
     * 
     * @param convertBraces
     *            if true, all braces are converted, if false then not.
     * 
     * @return the actual URI as String.
     * @see #toString()
     */
    private String toStringWithCheck(boolean convertBraces) {
        if (this.host == null) {
            if (this.port != null) {
                throw new UriBuilderException(
                        "You must set a host, if you set a port");
            }
            if ((this.userInfo != null) && (this.userInfo.length() >= 0)) {
                throw new UriBuilderException(
                        "You must set a host, if you set a userInfo");
            }
        }
        return toString(convertBraces);
    }

    /**
     * Copies the non-null components of the supplied URI to the UriBuilder
     * replacing any existing values for those components.
     * 
     * @param uri
     *            the URI to copy components from
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if uri is null
     * @see javax.ws.rs.core.UriBuilder#uri(java.net.URI)
     */
    @Override
    public UriBuilder uri(URI uri) throws IllegalArgumentException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (uri.getScheme() != null) {
            this.scheme = uri.getScheme();
        }
        if (uri.getHost() != null) {
            this.host = uri.getHost();
        }
        this.port(uri.getPort());
        if (uri.getRawUserInfo() != null) {
            this.userInfo = uri.getRawUserInfo();
        }
        if (uri.getRawPath() != null) {
            this.replacePath(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            this.query = uri.getRawQuery();
        }
        if (uri.getRawFragment() != null) {
            this.fragment = uri.getRawFragment();
        }
        return this;
    }

    /**
     * Set the URI user-info.
     * 
     * @param userInfo
     *            the URI user-info, may contain URI template parameters
     * @return the updated UriBuilder
     * @throws IllegalArgumentException
     *             if automatic encoding is disabled and the userInfo contains
     *             illegal characters, or if the userInfo is null.
     * @see javax.ws.rs.core.UriBuilder#userInfo(java.lang.String)
     */
    @Override
    public UriBuilder userInfo(String userInfo) throws IllegalArgumentException {
        if (userInfo == null) {
            this.userInfo = null;
        } else {
            this.userInfo = EncodeOrCheck.userInfo(userInfo, true);
        }
        return this;
    }
}
