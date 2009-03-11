/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.ClientHelper;


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
 * <td>org.restlet.engine.http.HttpClientConverter</td>
 * <td>Class name of the converter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
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
            final String converterClass = getHelpedParameters()
                    .getFirstValue("converter",
                            "org.restlet.engine.http.HttpClientConverter");
            this.converter = (HttpClientConverter) Class
                    .forName(converterClass).getConstructor(Context.class)
                    .newInstance(getContext());
        }

        return this.converter;
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            final HttpClientCall httpCall = getConverter().toSpecific(this,
                    request);
            getConverter().commit(httpCall, request, response);
        } catch (Exception e) {
            getLogger().log(Level.INFO,
                    "Error while handling an HTTP client call", e);
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
