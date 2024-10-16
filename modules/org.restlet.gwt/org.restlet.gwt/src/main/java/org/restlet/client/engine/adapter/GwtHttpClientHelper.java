/**
 * Copyright 2005-2020 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.adapter;

import org.restlet.client.Client;
import org.restlet.client.Request;
import org.restlet.client.data.ChallengeResponse;
import org.restlet.client.data.ChallengeScheme;
import org.restlet.client.data.CharacterSet;
import org.restlet.client.data.Protocol;
import org.restlet.client.engine.header.HeaderConstants;
import org.restlet.client.engine.util.Base64;

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
    public ClientCall create(Request request) {
        GwtClientCall result = null;

        try {
            result = new GwtClientCall(this, request.getMethod().toString(),
                    request.getResourceRef().toString(),
                    request.isEntityAvailable());

            // If a challenge response is provided, update the GWT request builder
            ChallengeResponse challenge = request.getChallengeResponse();
            if (challenge != null) {
                if (ChallengeScheme.HTTP_BASIC.equals(challenge.getScheme())) {
                    // Handle manually the generation of the "Authorization"
                    // header, since the browser does not cope with that.
                    byte[] bytes = (challenge.getIdentifier() + ":" + String.valueOf(challenge.getSecret()))
                                       .getBytes(CharacterSet.ISO_8859_1.getName());
                    result.getRequestBuilder().setHeader(
                            HeaderConstants.HEADER_AUTHORIZATION,
                            "Basic " + Base64.encode(bytes, false));
                } else if (challenge.getRawValue() != null && challenge.getScheme() != null) {
                    result.getRequestBuilder().setHeader(
                            HeaderConstants.HEADER_AUTHORIZATION,
                            challenge.getScheme().getTechnicalName() + " " + challenge.getRawValue());
                } else {
                    // In this case the login and password are simply concatenated to the URL.
                    result.getRequestBuilder().setUser(challenge.getIdentifier());
                    result.getRequestBuilder().setPassword(String.valueOf(challenge.getSecret()));
                }
            }

        } catch (Exception ioe) {
            System.err.println("Unable to create the HTTP client call");
        }

        return result;
    }

}
