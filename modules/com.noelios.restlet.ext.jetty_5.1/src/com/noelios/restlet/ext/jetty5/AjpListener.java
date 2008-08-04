/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.jetty5;

import java.io.IOException;
import java.net.Socket;

import org.mortbay.http.ajp.AJP13Connection;
import org.mortbay.http.ajp.AJP13Listener;
import org.mortbay.util.InetAddrPort;

/**
 * Jetty AJP listener.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AjpListener extends AJP13Listener {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The parent Jetty server helper. */
    private transient JettyServerHelper helper;

    /**
     * Constructor.
     * 
     * @param server
     *            The parent Jetty server.
     */
    public AjpListener(JettyServerHelper server) {
        this.helper = server;
    }

    /**
     * Constructor.
     * 
     * @param server
     *            The parent Jetty server.
     * @param address
     *            The listening address.
     */
    public AjpListener(JettyServerHelper server, InetAddrPort address) {
        super(address);
        this.helper = server;
    }

    /**
     * Returns the parent Jetty server helper.
     * 
     * @return The parent Jetty server helper.
     */
    public JettyServerHelper getHelper() {
        return this.helper;
    }

    /**
     * Creates an AJP13Connection instance. This method can be used to override
     * the connection instance.
     * 
     * @param socket
     *            The underlying socket.
     * @return The created connection.
     */
    protected AJP13Connection createConnection(Socket socket)
            throws IOException {
        return new AjpConnection(this, socket.getInputStream(), socket
                .getOutputStream(), socket, getBufferSize());
    }

}
