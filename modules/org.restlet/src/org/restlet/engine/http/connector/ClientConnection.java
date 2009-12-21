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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.restlet.Client;

/**
 * Generic HTTP client connection.
 * 
 * @author Jerome Louvel
 */
public class ClientConnection extends Connection<Client> {

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

    @Override
    public boolean canRead() throws IOException {
        return false;
    }

    @Override
    public boolean canWrite() throws IOException {
        return false;
    }

    @Override
    public InputStream getInboundStream() {
        return null;
    }

    @Override
    public OutputStream getOutboundStream() {
        return null;
    }

    @Override
    public void readMessages() {
    }

    @Override
    public void writeMessages() {
    }

}
