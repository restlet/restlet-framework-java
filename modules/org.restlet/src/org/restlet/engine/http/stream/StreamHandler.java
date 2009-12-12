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

package org.restlet.engine.http.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;

/**
 * Class that handles an incoming socket.
 * 
 * @author Jerome Louvel
 */
public class StreamHandler implements Runnable {

    /** The target server helper. */
    private final StreamServerHelper helper;

    /** The socket connection to handle. */
    private final Socket socket;

    /**
     * Constructor.
     * 
     * @param helper
     *            The target server helper.
     * @param socket
     *            The socket connection to handle.
     */
    public StreamHandler(StreamServerHelper helper, Socket socket) {
        this.helper = helper;
        this.socket = socket;
    }

    /**
     * Handles the given socket connection.
     */
    public void run() {
        try {
            this.helper.handle(new StreamServerCall(this.helper.getHelped(),
                    new BufferedInputStream(this.socket.getInputStream()),
                    new BufferedOutputStream(this.socket.getOutputStream()),
                    this.socket));
        } catch (IOException ex) {
            this.helper.getLogger().log(Level.WARNING,
                    "Unexpected error while handling a call", ex);
        }
    }
}