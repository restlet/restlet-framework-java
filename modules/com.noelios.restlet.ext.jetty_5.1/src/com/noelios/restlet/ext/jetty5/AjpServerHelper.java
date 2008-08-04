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
