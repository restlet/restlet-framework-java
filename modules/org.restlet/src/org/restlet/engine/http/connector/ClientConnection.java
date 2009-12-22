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

package org.restlet.engine.http.connector;

import java.io.IOException;
import java.net.Socket;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Generic HTTP client connection.
 * 
 * @author Jerome Louvel
 */
public abstract class ClientConnection extends Connection<Client> {

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     * @param socket
     *            The underlying socket.
     * @throws IOException
     */
    public ClientConnection(BaseHelper<Client> helper, Socket socket)
            throws IOException {
        super(helper, socket);
    }

    /**
     * Adds the request headers.
     * 
     * @param request
     *            The request to inspect.
     * @param headers
     *            The headers series to update.
     */
    protected void addRequestHeaders(Request request, Series<Parameter> headers) {
        HeaderUtils.addRequestHeaders(request, headers);
    }

    /**
     * Creates a new response.
     * 
     * @param context
     *            The current context.
     * @param connection
     *            The associated connection.
     * @param headers
     *            The response headers.
     * @param entity
     *            The response entity.
     * @return The created response.
     */
    protected abstract ConnectedResponse createResponse(Context context,
            ClientConnection connection, Series<Parameter> headers,
            Representation entity);

    @Override
    protected void handleNextMessage() {
    }

    @Override
    protected Message readMessage() throws IOException {
        return null;
    }

    @Override
    protected void writeMessage(Message message) {
    }

}
