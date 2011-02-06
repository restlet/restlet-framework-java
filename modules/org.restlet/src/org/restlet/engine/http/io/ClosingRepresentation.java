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

package org.restlet.engine.http.io;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

/**
 * Wrapper representation to close the associated socket when the representation
 * is released.
 * 
 * @author Jerome Louvel
 */
public class ClosingRepresentation extends WrapperRepresentation {

    /** The associated logger. */
    private final Logger logger;

    /** The associated socket. */
    private final Socket socket;

    /**
     * Constructor.
     * 
     * @param wrappedRepresentation
     *            The wrapped representation.
     * @param socket
     *            The associated socket.
     * @param logger
     *            The associated logger.
     */
    public ClosingRepresentation(Representation wrappedRepresentation,
            Socket socket, Logger logger) {
        super(wrappedRepresentation);
        this.socket = socket;
        this.logger = logger;
    }

    /**
     * Closes the socket if necessary.
     */
    @Override
    public void release() {
        try {
            if (!this.socket.isClosed()) {
                if (!(this.socket instanceof SSLSocket)) {
                    this.socket.shutdownOutput();
                }
                this.socket.close();
            }
        } catch (IOException ex) {
            this.logger.log(Level.WARNING,
                    "An error occured closing the client socket", ex);
        }

        super.release();
    }
}