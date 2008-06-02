/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.gwt.internal.http.gwt;

import org.restlet.gwt.Client;
import org.restlet.gwt.data.Protocol;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.internal.http.HttpClientCall;
import org.restlet.gwt.internal.http.HttpClientHelper;

/**
 * HTTP client connector using the GWT's HTTP module. Here is the list of
 * parameters that are supported: <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>timeout</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Sets the request timeout to a specified timeout, in milliseconds. A
 * timeout of zero is interpreted as an infinite timeout.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class GwtHttpClientHelper extends HttpClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *                The client to help.
     */
    public GwtHttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
    }

    /**
     * Creates a low-level HTTP client call from a high-level uniform call.
     * 
     * @param request
     *                The high-level request.
     * @return A low-level HTTP client call.
     */
    @Override
    public HttpClientCall create(Request request) {
        HttpClientCall result = null;

        try {
            result = new GwtHttpClientCall(this,
                    request.getMethod().toString(), request.getResourceRef()
                            .toString(), request.isEntityAvailable());
        } catch (Exception ioe) {
            System.err.println("Unable to create the HTTP client call");
        }

        return result;
    }

    /**
     * Returns the timeout value. A timeout of zero is interpreted as an
     * infinite timeout.
     * 
     * @return The timeout value.
     */
    public int getTimeout() {
        return Integer.parseInt(getParameters().getFirstValue("timeout", "0"));
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        System.out.println("Starting the HTTP client");
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        System.out.println("Stopping the HTTP client");
    }

}
