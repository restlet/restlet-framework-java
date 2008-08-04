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

package com.noelios.restlet.ext.jetty5;

import org.mortbay.util.InetAddrPort;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty HTTP server connector. Here is the list of additional parameters that
 * are supported: <table>
 * <tr>
 * <td>lowResourcePersistTimeMs</td>
 * <td>int</td>
 * <td>2000</td>
 * <td>Time in ms that connections will persist if listener is low on
 * resources.</td>
 * </tr>
 * </table>
 * 
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpServerHelper extends JettyServerHelper {
    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    /** Start hook. */
    public void start() throws Exception {
        HttpListener listener;

        if (getServer().getAddress() != null) {
            listener = new HttpListener(this, new InetAddrPort(getServer()
                    .getAddress(), getServer().getPort()));
        } else {
            listener = new HttpListener(this);
            listener.setPort(getServer().getPort());
        }

        // Configure the listener
        listener.setMinThreads(getMinThreads());
        listener.setMaxThreads(getMaxThreads());
        listener.setMaxIdleTimeMs(getMaxIdleTimeMs());
        listener.setLowResourcePersistTimeMs(getLowResourcePersistTimeMs());

        setListener(listener);
        super.start();
    }

    /**
     * Returns time in ms that connections will persist if listener is low on
     * resources.
     * 
     * @return Time in ms that connections will persist if listener is low on
     *         resources.
     */
    public int getLowResourcePersistTimeMs() {
        return Integer.parseInt(getParameters().getFirstValue(
                "lowResourcePersistTimeMs", "2000"));
    }

}
