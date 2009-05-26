/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.data;

/**
 * Method to execute when handling a call.
 * 
 * @author Jerome Louvel
 */
public final class Method extends Metadata implements Comparable<Method> {
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
     *      RFC - 9.4 GET</a>
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
                    + "#METHOD_LOCK", true, false);

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
            BASE_WEBDAV + "#METHOD_UNLOCK", true, false);

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
            if (name.equalsIgnoreCase(GET.getName())) {
                result = GET;
            } else if (name.equalsIgnoreCase(POST.getName())) {
                result = POST;
            } else if (name.equalsIgnoreCase(HEAD.getName())) {
                result = HEAD;
            } else if (name.equalsIgnoreCase(OPTIONS.getName())) {
                result = OPTIONS;
            } else if (name.equalsIgnoreCase(PUT.getName())) {
                result = PUT;
            } else if (name.equalsIgnoreCase(DELETE.getName())) {
                result = DELETE;
            } else if (name.equalsIgnoreCase(CONNECT.getName())) {
                result = CONNECT;
            } else if (name.equalsIgnoreCase(COPY.getName())) {
                result = COPY;
            } else if (name.equalsIgnoreCase(LOCK.getName())) {
                result = LOCK;
            } else if (name.equalsIgnoreCase(MKCOL.getName())) {
                result = MKCOL;
            } else if (name.equalsIgnoreCase(MOVE.getName())) {
                result = MOVE;
            } else if (name.equalsIgnoreCase(PROPFIND.getName())) {
                result = PROPFIND;
            } else if (name.equalsIgnoreCase(PROPPATCH.getName())) {
                result = PROPPATCH;
            } else if (name.equalsIgnoreCase(TRACE.getName())) {
                result = TRACE;
            } else if (name.equalsIgnoreCase(UNLOCK.getName())) {
                result = UNLOCK;
            } else {
                result = new Method(name);
            }
        }

        return result;
    }

    /**
     * Indicates if the side-effects of several requests is the same as a single
     * request.
     */
    private boolean idempotent;

    /**
     * Indicates if it should have the significance of taking an action other
     * than retrieval.
     */
    private final boolean safe;

    /** The URI of the specification describing the method. */
    private volatile String uri;

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
     * @see org.restlet.data.Method#valueOf(String)
     */
    public Method(String name, String description, String uri, boolean safe,
            boolean idempotent) {
        super(name, description);
        this.uri = uri;
        this.safe = safe;
        this.idempotent = idempotent;
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
        } else {
            return 1;
        }

    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof Method)
                && ((Method) object).getName().equals(getName());
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
     * Indicates if it should have the significance of taking an action other
     * than retrieval.
     * 
     * @return True if the method is safe.
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * Sets the URI of the specification describing the method.
     * 
     * @param uri
     *            The URI of the specification describing the method.
     * 
     * @deprecated Method instances are shared by all Restlet applications and
     *             shouldn't be modifiable.
     */
    @Deprecated
    public void setUri(String uri) {
        this.uri = uri;
    }
}
