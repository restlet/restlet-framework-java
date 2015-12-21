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

package org.restlet.engine.adapter;

import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.engine.connector.ClientHelper;

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
 * <td>org.restlet.engine.adapter.ClientAdapter</td>
 * <td>Class name of the adapter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class HttpClientHelper extends ClientHelper {

    /** The adapter from uniform calls to HTTP calls. */
    private volatile ClientAdapter adapter;

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
    public abstract ClientCall create(Request request);

    /**
     * Returns the adapter from uniform calls to HTTP calls.
     * 
     * @return the adapter from uniform calls to HTTP calls.
     */
    public ClientAdapter getAdapter() throws Exception {
        if (this.adapter == null) {
            // [ifndef gwt]
            String adapterClass = getHelpedParameters().getFirstValue(
                    "adapter", "org.restlet.engine.adapter.ClientAdapter");
            this.adapter = (ClientAdapter) Class.forName(adapterClass)
                    .getConstructor(Context.class).newInstance(getContext());
            // [enddef]

            // [ifdef gwt] instruction uncomment
            // this.adapter = new ClientAdapter(getContext());
        }

        return this.adapter;
    }

    /**
     * Returns the connection timeout. Defaults to 15000.
     * 
     * @return The connection timeout.
     */
    public int getSocketConnectTimeoutMs() {
        int result = 0;

        if (getHelpedParameters().getNames().contains("socketConnectTimeoutMs")) {
            result = Integer.parseInt(getHelpedParameters().getFirstValue(
                    "socketConnectTimeoutMs", "15000"));
        }

        return result;
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            ClientCall clientCall = getAdapter().toSpecific(this, request);
            getAdapter().commit(clientCall, request, response);
        } catch (Exception e) {
            getLogger().log(Level.INFO,
                    "Error while handling an HTTP client call", e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        }
    }

    /**
     * Sets the adapter from uniform calls to HTTP calls.
     * 
     * @param adapter
     *            The adapter to set.
     */
    public void setAdapter(ClientAdapter adapter) {
        this.adapter = adapter;
    }
}
