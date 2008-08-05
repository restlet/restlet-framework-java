/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.http;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;

import com.noelios.restlet.Engine;

/**
 * HTTP client helper based on BIO sockets.
 * 
 * @author Jerome Louvel
 */
public class StreamClientHelper extends HttpClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public StreamClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
    }

    @Override
    public HttpClientCall create(Request request) {
        request.getClientInfo().setAgent(Engine.VERSION_HEADER);
        return new StreamClientCall(this, request);
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info("Starting the HTTP client");
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the HTTP client");
    }
}
