/*
 * Copyright 2005-2007 Noelios Technologies.
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

package com.noelios.restlet.ext.jetty5;

import java.io.IOException;
import java.net.Socket;

import org.mortbay.http.SslListener;
import org.mortbay.util.InetAddrPort;

/**
 * Jetty HTTPS listener.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpsListener extends SslListener {
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
    public HttpsListener(JettyServerHelper server) {
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
    public HttpsListener(JettyServerHelper server, InetAddrPort address) {
        super(address);
        this.helper = server;
    }

    /**
     * Returns the parent Jetty server.
     * 
     * @return The parent Jetty server.
     */
    public JettyServerHelper getHelper() {
        return this.helper;
    }

    /**
     * Creates a HTTP connection instance. This method can be used to override
     * the connection instance.
     * 
     * @param socket
     *            The underlying socket.
     * @return The created connection.
     */
    protected HttpsConnection createConnection(Socket socket)
            throws IOException {
        return new HttpsConnection(this, socket.getInetAddress(), socket
                .getInputStream(), socket.getOutputStream(), socket);
    }

}
