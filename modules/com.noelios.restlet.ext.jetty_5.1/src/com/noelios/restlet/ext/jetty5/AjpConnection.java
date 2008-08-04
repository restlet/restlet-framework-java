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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.ajp.AJP13Connection;

/**
 * Jetty AJP connection.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AjpConnection extends AJP13Connection {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param listener
     *            The parent AJP listener.
     * @param in
     *            Input stream to read the request from.
     * @param out
     *            Output stream to write the response to.
     * @param socket
     *            The listening socket.
     * @param bufferSize
     *            The buffer size.
     */
    public AjpConnection(AjpListener listener, InputStream in,
            OutputStream out, Socket socket, int bufferSize) throws IOException {
        super(listener, in, out, socket, bufferSize);
    }

    /**
     * Handle Jetty HTTP calls.
     * 
     * @param request
     *            The HttpRequest request.
     * @param response
     *            The HttpResponse response.
     * @return The HttpContext that completed handling of the request or null.
     * @exception HttpException
     * @exception IOException
     */
    protected HttpContext service(HttpRequest request, HttpResponse response)
            throws HttpException, IOException {
        getJettyServer().handle(
                new JettyCall(getJettyServer().getServer(), request, response));

        // Commit the response and ensures that all data is flushed out to the
        // caller
        response.commit();

        // Indicates that the request fully handled
        request.setHandled(true);

        return null;
    }

    /**
     * Returns the Jetty connector.
     * 
     * @return The Jetty connector.
     */
    private JettyServerHelper getJettyServer() {
        return ((AjpListener) getListener()).getHelper();
    }

}
