/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.RestletHelper;
import org.restlet.representation.Representation;

/**
 * Connector acting as a generic client. It internally uses one of the available
 * connector helpers registered with the Restlet engine.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class Client extends Connector {
    /**
     * The number of milliseconds the client should wait for a response before
     * aborting the request and setting its status to an error status.
     */
    private volatile int connectTimeout = 0;

    /** The helper provided by the implementation. */
    private final RestletHelper<Client> helper;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     */
    public Client(Context context, List<Protocol> protocols) {
        this(context, protocols, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     * @param helperClass
     *            Optional helper class name.
     */
    public Client(Context context, List<Protocol> protocols, String helperClass) {
        super(context, protocols);

        if ((protocols != null) && (protocols.size() > 0)) {
            if (Engine.getInstance() != null) {
                this.helper = Engine.getInstance().createHelper(this,
                        helperClass);
            } else {
                this.helper = null;
            }
        } else {
            this.helper = null;
        }
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     */
    public Client(Context context, Protocol protocol) {
        this(context, (protocol == null) ? null : Arrays.asList(protocol), null);
    }

    /**
     * Constructor.
     * 
     * @param protocols
     *            The connector protocols.
     */
    public Client(List<Protocol> protocols) {
        this(null, protocols, null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     */
    public Client(Protocol protocol) {
        this(null, protocol);
    }

    /**
     * Constructor.
     * 
     * @param protocolName
     *            The connector protocol.
     */
    public Client(String protocolName) {
        this(Protocol.valueOf(protocolName));
    }

    // [ifndef gwt] method
    /**
     * Deletes the resource and all its representations at the target URI
     * reference.
     * 
     * @param resourceRef
     *            The reference of the resource to delete.
     * @return The response.
     */
    public final Response delete(Reference resourceRef) {
        return handle(new Request(Method.DELETE, resourceRef));
    }

    // [ifdef gwt] method
    /**
     * Deletes the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to delete.
     * @param onReceivedCallback
     *            The callback invoked upon request reception.
     */
    public final void delete(Reference resourceRef, Uniform onReceivedCallback) {
        handle(new Request(Method.DELETE, resourceRef), onReceivedCallback);
    }

    // [ifndef gwt] method
    /**
     * Deletes the resource and all its representations at the target URI.
     * 
     * @param resourceUri
     *            The URI of the resource to delete.
     * @return The response.
     */
    public final Response delete(String resourceUri) {
        return handle(new Request(Method.DELETE, resourceUri));
    }

    // [ifndef gwt] method
    /**
     * Gets the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public final Response get(Reference resourceRef) {
        return handle(new Request(Method.GET, resourceRef));
    }

    // [ifndef gwt] method
    /**
     * Gets the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public final Response get(String resourceUri) {
        return handle(new Request(Method.GET, resourceUri));
    }

    /**
     * Returns the connection timeout in milliseconds. The default value is 0,
     * meaning an infinite timeout.
     * 
     * @return The connection timeout.
     */
    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private RestletHelper<Client> getHelper() {
        return this.helper;
    }

    // [ifndef gwt] method
    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @return The returned response.
     */
    public final Response handle(Request request) {
        Response response = new Response(request);
        handle(request, response);
        return response;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null) {
            getHelper().handle(request, response);
        } else {
            StringBuilder sb = new StringBuilder();
            sb
                    .append("No available client connector supports the required protocol: ");
            sb.append("'").append(request.getProtocol().getName()).append("'.");
            sb
                    .append(" Please add the JAR of a matching connector to your classpath.");
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, sb.toString());
        }
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param onResponseCallback
     *            The callback invoked upon response reception.
     */
    public void handle(Request request, Response response,
            Uniform onResponseCallback) {
        request.setOnResponse(onResponseCallback);
        handle(request, response);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param onReceivedCallback
     *            The callback invoked upon request reception.
     */
    public final void handle(Request request, Uniform onReceivedCallback) {
        Response response = new Response(request);
        handle(request, response, onReceivedCallback);
    }

    /**
     * Indicates the underlying connector helper is available.
     * 
     * @return True if the underlying connector helper is available.
     */
    @Override
    public boolean isAvailable() {
        return getHelper() != null;
    }

    // [ifndef gwt] method
    /**
     * Posts a representation to the resource at the target reference.
     * 
     * @param resourceRef
     *            The reference of the resource to post to.
     * @param entity
     *            The entity to post.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public final Response post(Reference resourceRef, Representation entity) {
        return handle(new Request(Method.POST, resourceRef, entity));
    }

    // [ifndef gwt] method
    /**
     * Posts a representation to the resource at the target URI.
     * 
     * @param resourceUri
     *            The URI of the resource to post to.
     * @param entity
     *            The entity to post.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public final Response post(String resourceUri, Representation entity) {
        return handle(new Request(Method.POST, resourceUri, entity));
    }

    // [ifndef gwt] method
    /**
     * Creates or updates a resource at the target URI reference with the given
     * representation as new state to be stored.
     * 
     * @param resourceRef
     *            The reference of the resource to modify.
     * @param representation
     *            The representation to store.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public final Response put(Reference resourceRef,
            Representation representation) {
        return handle(new Request(Method.PUT, resourceRef, representation));
    }

    // [ifndef gwt] method
    /**
     * Creates or updates a resource at the target URI with the given
     * representation as new state to be stored.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param representation
     *            The representation to store.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public final Response put(String resourceUri, Representation representation) {
        return handle(new Request(Method.PUT, resourceUri, representation));
    }

    /**
     * Sets the connection timeout in milliseconds. The default value is 0,
     * meaning an infinite timeout.
     * 
     * @param connectTimeout
     *            The connection timeout.
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            super.start();
            if (getHelper() != null) {
                getHelper().start();
            }
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            if (getHelper() != null) {
                getHelper().stop();
            }
            super.stop();
        }
    }

}
