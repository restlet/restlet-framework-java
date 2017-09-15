/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.engine.Engine;

/**
 * Method to execute when handling a call.
 * 
 * @author Jerome Louvel
 */
public final class Method implements Comparable<Method> {

    /** Map of registered methods. */
    private static final Map<String, Method> _methods = new ConcurrentHashMap<String, Method>();

    /**
     * Pseudo-method use to match all methods.
     */
    public static final Method ALL = new Method("*",
            "Pseudo-method use to match all methods.");

    private static final String BASE_HTTP = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html";

    private static final String BASE_WEBDAV = "http://www.webdav.org/specs/rfc2518.html";

    /**
     * Used with a proxy that can dynamically switch to being a tunnel.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.9">HTTP
     *      RFC - 9.9 CONNECT</a>
     */
    public static final Method CONNECT = new Method("CONNECT",
            "Used with a proxy that can dynamically switch to being a tunnel",
            BASE_HTTP + "#sec9.9", false, false);

    /**
     * Creates a duplicate of the source resource, identified by the
     * Request-URI, in the destination resource, identified by the URI in the
     * Destination header.
     * 
     * @see <a
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_COPY">WEBDAV
     *      RFC - 8.8 COPY Method</a>
     */
    public static final Method COPY = new Method(
            "COPY",
            "Creates a duplicate of the source resource, identified by the Request-URI, in the destination resource, identified by the URI in the Destination header",
            BASE_WEBDAV + "#METHOD_COPY", false, true);

    /**
     * Requests that the origin server deletes the resource identified by the
     * request URI.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7">HTTP
     *      RFC - 9.7 DELETE</a>
     */
    public static final Method DELETE = new Method(
            "DELETE",
            "Requests that the origin server deletes the resource identified by the request URI",
            BASE_HTTP + "#sec9.7", false, true);

    /**
     * Retrieves whatever information (in the form of an entity) that is
     * identified by the request URI.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      RFC - 9.3 GET</a>
     */
    public static final Method GET = new Method(
            "GET",
            "Retrieves whatever information (in the form of an entity) that is identified by the request URI",
            BASE_HTTP + "#sec9.3", true, true);

    /**
     * Identical to GET except that the server must not return a message body in
     * the response but only the message header.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      RFC - 9.4 HEAD</a>
     */
    public static final Method HEAD = new Method(
            "HEAD",
            "Identical to GET except that the server must not return a message body in the response",
            BASE_HTTP + "#sec9.4", true, true);

    /**
     * Used to take out a lock of any access type on the resource identified by
     * the request URI.
     * 
     * @see <a
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_LOCK">WEBDAV
     *      RFC - 8.10 LOCK Method</a>
     */
    public static final Method LOCK = new Method("LOCK",
            "Used to take out a lock of any access type (WebDAV)", BASE_WEBDAV
                    + "#METHOD_LOCK", false, false);

    /**
     * MKCOL creates a new collection resource at the location specified by the
     * Request URI.
     * 
     * @see <a
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_MKCOL">WEBDAV
     *      RFC - 8.3 MKCOL Method</a>
     */
    public static final Method MKCOL = new Method("MKCOL",
            "Used to create a new collection (WebDAV)", BASE_WEBDAV
                    + "#METHOD_MKCOL", false, true);

    /**
     * Logical equivalent of a copy, followed by consistency maintenance
     * processing, followed by a delete of the source where all three actions
     * are performed atomically.
     * 
     * @see <a
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_MOVE">WEBDAV
     *      RFC - 8.3 MKCOL Method</a>
     */
    public static final Method MOVE = new Method(
            "MOVE",
            "Logical equivalent of a copy, followed by consistency maintenance processing, followed by a delete of the source (WebDAV)",
            BASE_WEBDAV + "#METHOD_MOVE", false, false);

    /**
     * Requests for information about the communication options available on the
     * request/response chain identified by the URI.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      RFC - 9.2 OPTIONS</a>
     */
    public static final Method OPTIONS = new Method(
            "OPTIONS",
            "Requests for information about the communication options available on the request/response chain identified by the URI",
            BASE_HTTP + "#sec9.2", true, true);

    /**
     * Requests that the origin server applies partial modifications contained
     * in the entity enclosed in the request to the resource identified by the
     * request URI.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc5789">HTTP PATCH RFC 5789</a>
     */
    public static final Method PATCH = new Method(
            "PATCH",
            "Requests that the origin server applies partial modifications to the resource identified by the request URI",
            "http://tools.ietf.org/html/rfc5789", false, false);

    /**
     * Requests that the origin server accepts the entity enclosed in the
     * request as a new subordinate of the resource identified by the request
     * URI.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      RFC - 9.5 POST</a>
     */
    public static final Method POST = new Method(
            "POST",
            "Requests that the origin server accepts the entity enclosed in the request as a new subordinate of the resource identified by the request URI",
            BASE_HTTP + "#sec9.5", false, false);

    /**
     * Retrieves properties defined on the resource identified by the request
     * URI.
     * 
     * @see <a
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_PROPFIND">WEBDAV
     *      RFC - 8.1 PROPFIND</a>
     */
    public static final Method PROPFIND = new Method(
            "PROPFIND",
            "Retrieves properties defined on the resource identified by the request URI",
            BASE_WEBDAV + "#METHOD_PROPFIND", true, true);

    /**
     * Processes instructions specified in the request body to set and/or remove
     * properties defined on the resource identified by the request URI.
     * 
     * @see <a
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_PROPPATCH">WEBDAV
     *      RFC - 8.2 PROPPATCH</a>
     */
    public static final Method PROPPATCH = new Method(
            "PROPPATCH",
            "Processes instructions specified in the request body to set and/or remove properties defined on the resource identified by the request URI",
            BASE_WEBDAV + "#METHOD_PROPPATCH", false, true);

    /**
     * Requests that the enclosed entity be stored under the supplied request
     * URI.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6"
     *      HTTP RFC - 9.6 PUT</a>
     */
    public static final Method PUT = new Method(
            "PUT",
            "Requests that the enclosed entity be stored under the supplied request URI",
            BASE_HTTP + "#sec9.6", false, true);

    /**
     * Used to invoke a remote, application-layer loop-back of the request
     * message.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.8">HTTP
     *      RFC - 9.8 TRACE</a>
     */
    public static final Method TRACE = new Method(
            "TRACE",
            "Used to invoke a remote, application-layer loop-back of the request message",
            BASE_HTTP + "#sec9.8", true, true);

    /**
     * Removes the lock identified by the lock token from the request URI, and
     * all other resources included in the lock.
     * 
     * @see <a
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_UNLOCK">WEBDAV
     *      RFC - 8.11 UNLOCK Method</a>
     */
    public static final Method UNLOCK = new Method(
            "UNLOCK",
            "Removes the lock identified by the lock token from the request URI, and all other resources included in the lock",
            BASE_WEBDAV + "#METHOD_UNLOCK", false, false);

    /**
     * Adds a new Method to the list of registered methods.
     * 
     * @param method
     *            The method to register.
     */
    public static void register(Method method) {
        String name = (method == null) ? null : method.getName().toLowerCase();
        if ((name != null) && !name.equals("")) {
            _methods.put(name, method);
        }
    }

    /**
     * Sorts the given list of methods by name.
     * 
     * @param methods
     *            The methods to sort.
     */
    public static void sort(List<Method> methods) {
        Collections.sort(methods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
    }

    /**
     * Returns the method associated to a given method name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The method name.
     * @return The associated method.
     */
    public static Method valueOf(final String name) {
        Method result = null;

        if ((name != null) && !name.equals("")) {
            result = Method._methods.get(name.toLowerCase());
            if (result == null) {
                result = new Method(name);
            }
        }

        return result;
    }

    /** The description. */
    private final String description;

    /**
     * Indicates if the side-effects of several requests is the same as a single
     * request.
     */
    private volatile boolean idempotent;

    /** The name. */
    private volatile String name;

    /** Indicates if the method replies with a response. */
    private final boolean replying;

    /**
     * Indicates if it should have the significance of taking an action other
     * than retrieval.
     */
    private final boolean safe;

    /** The URI of the specification describing the method. */
    private volatile String uri;

    static {
        // Let the engine register all methods (the default ones and the ones to
        // be discovered) as soon as the Method class is loaded or at least
        // used.
        Engine.getInstance();
    }

    /**
     * Constructor for unsafe and non idempotent methods.
     * 
     * @param name
     *            The technical name of the method.
     * @see org.restlet.data.Method#valueOf(String)
     */
    public Method(final String name) {
        this(name, null);
    }

    /**
     * Constructor for unsafe and non idempotent methods.
     * 
     * @param name
     *            The technical name of the method.
     * @param description
     *            The description.
     * @see org.restlet.data.Method#valueOf(String)
     */
    public Method(String name, String description) {
        this(name, description, null, false, false);
    }

    /**
     * Constructor for unsafe and non idempotent methods.
     * 
     * @param name
     *            The technical name.
     * @param description
     *            The description.
     * @param uri
     *            The URI of the specification describing the method.
     * @see org.restlet.data.Method#valueOf(String)
     */
    public Method(String name, String description, String uri) {
        this(name, description, uri, false, false);
    }

    /**
     * Constructor for methods that reply to requests with responses.
     * 
     * @param name
     *            The technical name.
     * @param description
     *            The description.
     * @param uri
     *            The URI of the specification describing the method.
     * @param safe
     *            Indicates if the method is safe.
     * @param idempotent
     *            Indicates if the method is idempotent.
     * @see org.restlet.data.Method#valueOf(String)
     */
    public Method(String name, String description, String uri, boolean safe,
            boolean idempotent) {
        this(name, description, uri, safe, idempotent, true);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The technical name.
     * @param description
     *            The description.
     * @param uri
     *            The URI of the specification describing the method.
     * @param safe
     *            Indicates if the method is safe.
     * @param idempotent
     *            Indicates if the method is idempotent.
     * @param replying
     *            Indicates if the method replies with a response.
     * @see org.restlet.data.Method#valueOf(String)
     */
    public Method(String name, String description, String uri, boolean safe,
            boolean idempotent, boolean replying) {
        this.name = name;
        this.description = description;
        this.uri = uri;
        this.safe = safe;
        this.idempotent = idempotent;
        this.replying = replying;
    }

    /**
     * Compares this method to another. Based on the method name.
     * 
     * @param o
     *            The other method.
     */
    public int compareTo(Method o) {
        if (o != null) {
            return this.getName().compareTo(o.getName());
        }
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof Method)
                && ((Method) object).getName().equals(getName());
    }

    /**
     * Returns the description.
     * 
     * @return The description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the URI of the specification describing the method.
     * 
     * @return The URI of the specification describing the method.
     */
    public String getUri() {
        return this.uri;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().hashCode();
    }

    /**
     * Indicates if the side-effects of several requests is the same as a single
     * request.
     * 
     * @return True if the method is idempotent.
     */
    public boolean isIdempotent() {
        return idempotent;
    }

    /**
     * Indicates if the method replies with a response.
     * 
     * @return True if the method replies with a response.
     */
    public boolean isReplying() {
        return replying;
    }

    /**
     * Indicates if it should have the significance of taking an action other
     * than retrieval.
     * 
     * @return True if the method is safe.
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    @Override
    public String toString() {
        return getName();
    }
}
