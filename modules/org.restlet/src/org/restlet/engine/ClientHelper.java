/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine;

import org.restlet.Client;

/**
 * Client connector helper. Base client helper based on NIO non blocking
 * sockets. Here is the list of parameters that are supported. They should be
 * set in the Client's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>socketConnectTimeoutMs</td>
 * <td>int</td>
 * <td>0</td>
 * <td>The socket connection timeout or 0 for unlimited wait.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public class ClientHelper extends ConnectorHelper<Client> {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public ClientHelper(Client client) {
        super(client);
    }

    /**
     * Returns the connection timeout.
     * 
     * @return The connection timeout.
     */
    @SuppressWarnings("deprecation")
    public int getSocketConnectTimeoutMs() {
        int result = getHelped().getConnectTimeout();

        if (getHelpedParameters().getNames().contains("socketConnectTimeoutMs")) {
            result = Integer.parseInt(getHelpedParameters().getFirstValue(
                    "socketConnectTimeoutMs", "0"));
        }

        return result;
    }

}
