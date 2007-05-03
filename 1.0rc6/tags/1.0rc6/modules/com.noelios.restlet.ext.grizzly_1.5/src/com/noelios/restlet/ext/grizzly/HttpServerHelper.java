/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.ext.grizzly;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty HTTP server connector. Here is the list of additional parameters that
 * are supported: <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>int</td>
 * <td>1</td>
 * <td>The type of Jetty connector to use.<br/> 1 : Selecting NIO connector
 * (Jetty's SelectChannelConnector class).<br/> 2 : Blocking NIO connector
 * (Jetty's BlockingChannelConnector class).<br/> 3 : Blocking BIO connector
 * (Jetty's SocketConnector class).</td>
 * </tr>
 * </table>
 * 
 * @see <a href="http://jetty.mortbay.org/jetty6/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpServerHelper extends GrizzlyServerHelper {
    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    /**
     * Returns the type of Jetty connector to use.
     * 
     * @return The type of Jetty connector to use.
     */
    public int getType() {
        return Integer.parseInt(getParameters().getFirstValue("type", "1"));
    }

}
