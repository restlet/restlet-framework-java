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

package org.restlet.engine.http;

import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.engine.ClientHelper;

/**
 * Base HTTP client connector. Here is the list of parameters that are
 * supported. They should be set in the Client's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>adapter</td>
 * <td>String</td>
 * <td>org.restlet.engine.http.HttpClientAdapter</td>
 * <td>Class name of the adapter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class HttpClientHelper extends ClientHelper {
    /** The adapter from uniform calls to HTTP calls. */
    private volatile HttpClientAdapter adapter;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        this.adapter = null;
    }

    /**
     * Creates a low-level HTTP client call from a high-level request.
     * 
     * @param request
     *            The high-level request.
     * @return A low-level HTTP client call.
     */
    public abstract HttpClientCall create(Request request);

    /**
     * Returns the adapter from uniform calls to HTTP calls.
     * 
     * @return the adapter from uniform calls to HTTP calls.
     */
    public HttpClientAdapter getAdapter() throws Exception {
        if (this.adapter == null) {
            // [ifndef gwt]
            final String adapterClass = getHelpedParameters().getFirstValue(
                    "adapter", "org.restlet.engine.http.HttpClientAdapter");
            this.adapter = (HttpClientAdapter) Class.forName(adapterClass)
                    .getConstructor(Context.class).newInstance(getContext());
            // [enddef]
            // [ifdef gwt] instruction uncomment
            // this.adapter = new HttpClientAdapter(getContext());
        }

        return this.adapter;
    }

    // [ifndef gwt] method
    @Override
    public void handle(Request request, Response response) {
        try {
            final HttpClientCall httpCall = getAdapter().toSpecific(this,
                    request);
            getAdapter().commit(httpCall, request, response);
        } catch (Exception e) {
            getLogger().log(Level.INFO,
                    "Error while handling an HTTP client call", e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        }
    }

    // [ifdef gwt] method uncomment
    // @Override
    // public void handle(Request request, Response response,
    // org.restlet.Uniform callback) {
    // try {
    // final HttpClientCall httpCall = getAdapter().toSpecific(this,
    // request);
    // getAdapter().commit(httpCall, request, response, callback);
    // } catch (Exception e) {
    // getLogger().log(Level.INFO,
    // "Error while handling an HTTP client call", e);
    // response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
    // }
    // }

    /**
     * Sets the adapter from uniform calls to HTTP calls.
     * 
     * @param adapter
     *            The adapter to set.
     */
    public void setAdapter(HttpClientAdapter adapter) {
        this.adapter = adapter;
    }
}
