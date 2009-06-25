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

package org.restlet;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.Helper;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Engine;

/**
 * Connector acting as a generic client. It internally uses one of the available
 * connectors registered with the current Restlet implementation.
 * 
 * @author Jerome Louvel
 */
public class Client extends Connector {
    /**
     * The number of milliseconds the client should wait for a response before
     * aborting the request and setting its status to an error status.
     */
    private int connectTimeout = 0;

    /** The helper provided by the implementation. */
    private Helper<Client> helper;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     */
    public Client(Context context, List<Protocol> protocols) {
        super(context, protocols);

        if ((protocols != null) && (protocols.size() > 0)) {
            if (Engine.getInstance() != null) {
                this.helper = Engine.getInstance().createHelper(this);
            }
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
        this(context, (protocol == null) ? null : Arrays.asList(protocol));
    }

    /**
     * Constructor.
     * 
     * @param protocols
     *            The connector protocols.
     */
    public Client(List<Protocol> protocols) {
        this(null, protocols);
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

    /**
     * Deletes the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to delete.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void delete(Reference resourceRef, Uniform callback) {
        handle(new Request(Method.DELETE, resourceRef), callback);
    }

    /**
     * Deletes the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to delete.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void delete(String resourceUri, Uniform callback) {
        handle(new Request(Method.DELETE, resourceUri), callback);
    }

    /**
     * Gets the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void get(Reference resourceRef, Uniform callback) {
        handle(new Request(Method.GET, resourceRef), callback);
    }

    /**
     * Gets the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void get(String resourceUri, Uniform callback) {
        handle(new Request(Method.GET, resourceUri), callback);
    }

    /**
     * Returns the connection timeout.
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
    private Helper<Client> getHelper() {
        return this.helper;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void handle(Request request, Uniform callback) {
        final Response response = new Response(request);
        handle(request, response, callback);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public void handle(Request request, Response response, Uniform callback) {
        super.handle(request, response, callback);

        if (getHelper() != null) {
            getHelper().handle(request, response, callback);
        }
    }

    /**
     * Gets the identified resource without its representation's content.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void head(Reference resourceRef, Uniform callback) {
        handle(new Request(Method.HEAD, resourceRef), callback);
    }

    /**
     * Gets the identified resource without its representation's content.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void head(String resourceUri, Uniform callback) {
        handle(new Request(Method.HEAD, resourceUri), callback);
    }

    /**
     * Gets the options for the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void options(Reference resourceRef, Uniform callback) {
        handle(new Request(Method.OPTIONS, resourceRef), callback);
    }

    /**
     * Gets the options for the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void options(String resourceUri, Uniform callback) {
        handle(new Request(Method.OPTIONS, resourceUri), callback);
    }

    /**
     * Posts a representation to the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to post to.
     * @param entity
     *            The entity to post.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void post(Reference resourceRef, Representation entity,
            Uniform callback) {
        handle(new Request(Method.POST, resourceRef, entity), callback);
    }

    /**
     * Posts a representation to the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to post to.
     * @param entity
     *            The entity to post.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void post(String resourceUri, Representation entity,
            Uniform callback) {
        handle(new Request(Method.POST, resourceUri, entity), callback);
    }

    /**
     * Posts a representation to the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param entity
     *            The entity to post.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void post(String resourceUri, String entity, Uniform callback) {
        post(resourceUri, new StringRepresentation(entity), callback);
    }

    /**
     * Puts a representation in the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to modify.
     * @param entity
     *            The entity to put.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void put(Reference resourceRef, Representation entity,
            Uniform callback) {
        handle(new Request(Method.PUT, resourceRef, entity), callback);
    }

    /**
     * Puts a representation in the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param entity
     *            The entity to put.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void put(String resourceUri, Representation entity,
            Uniform callback) {
        handle(new Request(Method.PUT, resourceUri, entity), callback);
    }

    /**
     * Puts a representation in the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param entity
     *            The entity to put.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void put(String resourceUri, String entity, Uniform callback) {
        put(resourceUri, new StringRepresentation(entity), callback);
    }

    /**
     * Sets the connection timeout.
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
