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

import org.mortbay.util.InetAddrPort;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty AJP server connector.
 * 
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AjpServerHelper extends JettyServerHelper {
    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public AjpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.AJP);
    }

    /** Start hook. */
    public void start() throws Exception {
        AjpListener listener;

        if (getServer().getAddress() != null) {
            listener = new AjpListener(this, new InetAddrPort(getServer()
                    .getAddress(), getServer().getPort()));
        } else {
            listener = new AjpListener(this);
            listener.setPort(getServer().getPort());
        }

        // Configure the listener
        listener.setMinThreads(getMinThreads());
        listener.setMaxThreads(getMaxThreads());
        listener.setMaxIdleTimeMs(getMaxIdleTimeMs());

        setListener(listener);
        super.start();
    }

}
