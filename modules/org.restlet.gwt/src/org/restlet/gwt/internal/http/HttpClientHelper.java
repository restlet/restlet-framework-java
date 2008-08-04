/*
 * Copyright 2005-2008 Noelios Technologies.
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

package org.restlet.gwt.internal.http;

import org.restlet.gwt.Callback;
import org.restlet.gwt.Client;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.data.Status;
import org.restlet.gwt.internal.ClientHelper;

/**
 * Base HTTP client connector. Here is the list of parameters that are
 * supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>converter</td>
 * <td>String</td>
 * <td>com.noelios.restlet.http.HttpClientConverter</td>
 * <td>Class name of the converter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class HttpClientHelper extends ClientHelper {
    /** The converter from uniform calls to HTTP calls. */
    private volatile HttpClientConverter converter;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        this.converter = null;
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
     * Returns the converter from uniform calls to HTTP calls.
     * 
     * @return the converter from uniform calls to HTTP calls.
     */
    public HttpClientConverter getConverter() throws Exception {
        if (this.converter == null) {
            this.converter = new HttpClientConverter(getContext());
        }

        return this.converter;
    }

    @Override
    public void handle(Request request, Response response, Callback callback) {
        try {
            final HttpClientCall httpCall = getConverter().toSpecific(this,
                    request);
            getConverter().commit(httpCall, request, response, callback);
        } catch (final Exception e) {
            System.err.println("Error while handling an HTTP client call");
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        }
    }

    /**
     * Sets the converter from uniform calls to HTTP calls.
     * 
     * @param converter
     *            The converter to set.
     */
    public void setConverter(HttpClientConverter converter) {
        this.converter = converter;
    }
}
