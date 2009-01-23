/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt.engine.http;

import org.restlet.gwt.Client;
import org.restlet.gwt.data.Protocol;
import org.restlet.gwt.data.Request;

/**
 * HTTP client connector using the GWT's HTTP module. Here is the list of
 * parameters that are supported:
 * <table>
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
 * @author Jerome Louvel
 */
public class GwtHttpClientHelper extends HttpClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
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
     *            The high-level request.
     * @return A low-level HTTP client call.
     */
    @Override
    public HttpClientCall create(Request request) {
        GwtHttpClientCall result = null;

        try {
            result = new GwtHttpClientCall(this,
                    request.getMethod().toString(), request.getResourceRef()
                            .toString(), request.isEntityAvailable());

            // If a challenge response is provided,
            // update the GWT request builder
            if (request.getChallengeResponse() != null) {
                result.getRequestBuilder().setUser(
                        request.getChallengeResponse().getIdentifier());
                result.getRequestBuilder().setPassword(
                        String.valueOf(request.getChallengeResponse()
                                .getSecret()));
            }

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
