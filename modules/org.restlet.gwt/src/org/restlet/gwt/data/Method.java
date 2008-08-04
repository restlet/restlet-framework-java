/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt.data;

/**
 * Method to execute when handling a call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class Method extends Metadata {
    private static final String BASE_HTTP = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html";

    private static final String BASE_WEBDAV = "http://www.webdav.org/specs/rfc2518.html";

    /**
     * Used with a proxy that can dynamically switch to being a tunnel.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.9"
     *      >HTTP * RFC - 9.9 CONNECT< /a>
     */
    public static final Method CONNECT = new Method("CONNECT",
            "Used with a proxy that can dynamically switch to being a tunnel",
            BASE_HTTP + "#sec9.9");

    /**
     * Creates a duplicate of the source resource, identified by the
     * Request-URI, in the destination resource, identified by the URI in the
     * Destination header.
     * 
     * @see <a *
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_COPY">WEBDAV *
     *      RFC - 8.8 COPY Method< /a>
     */
    public static final Method COPY = new Method(
            "COPY",
            "Creates a duplicate of the source resource, identified by the Request-URI, in the destination resource, identified by the URI in the Destination header",
            BASE_WEBDAV + "#METHOD_COPY");

    /**
     * Requests that the origin server deletes the resource identified by the
     * request URI.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7"
     *      >HTTP * RFC - 9.7 DELETE< /a>
     */
    public static final Method DELETE = new Method(
            "DELETE",
            "Requests that the origin server deletes the resource identified by the request URI",
            BASE_HTTP + "#sec9.7");

    /**
     * Retrieves whatever information (in the form of an entity) that is
     * identified by the request URI.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3"
     *      >HTTP * RFC - 9.3 GET< /a>
     */
    public static final Method GET = new Method(
            "GET",
            "Retrieves whatever information (in the form of an entity) that is identified by the request URI",
            BASE_HTTP + "#sec9.3");

    /**
     * Identical to GET except that the server must not return a message body in
     * the response but only the message header.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4"
     *      >HTTP * RFC - 9.4 GET< /a>
     */
    public static final Method HEAD = new Method(
            "HEAD",
            "Identical to GET except that the server must not return a message body in the response",
            BASE_HTTP + "#sec9.4");

    /**
     * Used to take out a lock of any access type on the resource identified by
     * the request URI.
     * 
     * @see <a *
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_LOCK">WEBDAV *
     *      RFC - 8.10 LOCK Method< /a>
     */
    public static final Method LOCK = new Method("LOCK",
            "Used to take out a lock of any access type (WebDAV)", BASE_WEBDAV
                    + "#METHOD_LOCK");

    /**
     * MKCOL creates a new collection resource at the location specified by the
     * Request URI.
     * 
     * @see <a *
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_MKCOL">WEBDAV
     *      * RFC - 8.3 MKCOL Method< /a>
     */
    public static final Method MKCOL = new Method("MKCOL",
            "Used to create a new collection (WebDAV)", BASE_WEBDAV
                    + "#METHOD_MKCOL");

    /**
     * Logical equivalent of a copy, followed by consistency maintenance
     * processing, followed by a delete of the source where all three actions
     * are performed atomically.
     * 
     * @see <a *
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_MOVE">WEBDAV *
     *      RFC - 8.3 MKCOL Method< /a>
     */
    public static final Method MOVE = new Method(
            "MOVE",
            "Logical equivalent of a copy, followed by consistency maintenance processing, followed by a delete of the source (WebDAV)",
            BASE_WEBDAV + "#METHOD_MOVE");

    /**
     * Requests for information about the communication options available on the
     * request/response chain identified by the URI.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2"
     *      >HTTP * RFC - 9.2 OPTIONS< /a>
     */
    public static final Method OPTIONS = new Method(
            "OPTIONS",
            "Requests for information about the communication options available on the request/response chain identified by the URI",
            BASE_HTTP + "#sec9.2");

    /**
     * Requests that the origin server accepts the entity enclosed in the
     * request as a new subordinate of the resource identified by the request
     * URI.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5"
     *      >HTTP * RFC - 9.5 POST< /a>
     */
    public static final Method POST = new Method(
            "POST",
            "Requests that the origin server accepts the entity enclosed in the request as a new subordinate of the resource identified by the request URI",
            BASE_HTTP + "#sec9.5");

    /**
     * Retrieves properties defined on the resource identified by the request
     * URI.
     * 
     * @see <a *
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_PROPFIND">
     *      WEBDAV * RFC - 8.1 PROPFIND< /a>
     */
    public static final Method PROPFIND = new Method(
            "PROPFIND",
            "Retrieves properties defined on the resource identified by the request URI",
            BASE_WEBDAV + "#METHOD_PROPFIND");

    /**
     * Processes instructions specified in the request body to set and/or remove
     * properties defined on the resource identified by the request URI.
     * 
     * @see <a *
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_PROPPATCH"
     *      >WEBDAV * RFC - 8.2 PROPPATCH< /a>
     */
    public static final Method PROPPATCH = new Method(
            "PROPPATCH",
            "Processes instructions specified in the request body to set and/or remove properties defined on the resource identified by the request URI",
            BASE_WEBDAV + "#METHOD_PROPPATCH");

    /**
     * Requests that the enclosed entity be stored under the supplied request
     * URI.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6"
     *      >HTTP * RFC - 9.6 PUT< /a>
     */
    public static final Method PUT = new Method(
            "PUT",
            "Requests that the enclosed entity be stored under the supplied request URI",
            BASE_HTTP + "#sec9.6");

    /**
     * Used to invoke a remote, application-layer loop-back of the request
     * message.
     * 
     * @see <a *
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.8"
     *      >HTTP * RFC - 9.8 TRACE< /a>
     */
    public static final Method TRACE = new Method(
            "TRACE",
            "Used to invoke a remote, application-layer loop-back of the request message",
            BASE_HTTP + "#sec9.8");

    /**
     * Removes the lock identified by the lock token from the request URI, and
     * all other resources included in the lock.
     * 
     * @see <a *
     *      href="http://www.webdav.org/specs/rfc2518.html#METHOD_UNLOCK">WEBDAV
     *      * RFC - 8.11 UNLOCK Method< /a>
     */
    public static final Method UNLOCK = new Method(
            "UNLOCK",
            "Removes the lock identified by the lock token from the request URI, and all other resources included in the lock",
            BASE_WEBDAV + "#METHOD_UNLOCK");

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

    /** The URI of the specification describing the method. */
    private volatile String uri;

    /**
     * Constructor.
     * 
     * @param name
     *            The technical name of the method.
     * @see #valueOf(String)
     */
    public Method(final String name) {
        this(name, null, null);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The technical name of the method.
     * @param description
     *            The description.
     * @see #valueOf(String)
     */
    public Method(final String name, final String description) {
        this(name, description, null);
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
     * @see #valueOf(String)
     */
    public Method(final String name, final String description, final String uri) {
        super(name, description);
        this.uri = uri;
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

    public void setUri(String uri) {
        this.uri = uri;
    }
}
