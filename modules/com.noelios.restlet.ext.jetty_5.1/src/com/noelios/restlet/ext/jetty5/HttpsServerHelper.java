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

import java.io.File;

import org.mortbay.util.InetAddrPort;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that
 * are supported: <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>lowResourcePersistTimeMs</td>
 * <td>int</td>
 * <td>2000</td>
 * <td>Time in ms that connections will persist if listener is low on
 * resources.</td>
 * </tr>
 * <tr>
 * <td>keystorePath</td>
 * <td>String</td>
 * <td>${user.home}/.keystore</td>
 * <td>The SSL keystore path.</td>
 * </tr>
 * <tr>
 * <td>keystorePassword</td>
 * <td>String</td>
 * <td></td>
 * <td>The SSL keystore password.</td>
 * </tr>
 * <tr>
 * <td>keyPassword</td>
 * <td>String</td>
 * <td></td>
 * <td>The SSL key password.</td>
 * </tr>
 * </table>
 * 
 * @see <a href="http://jetty.mortbay.org/jetty/faq?s=400-Security&t=ssl">FAQ -
 *      Configuring SSL for Jetty</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpsServerHelper extends JettyServerHelper {
    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpsServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTPS);
    }

    /** Start hook. */
    public void start() throws Exception {
        HttpsListener listener = null;
        if (getServer().getAddress() != null) {
            listener = new HttpsListener(this, new InetAddrPort(getServer()
                    .getAddress(), getServer().getPort()));
        } else {
            listener = new HttpsListener(this);
            listener.setPort(getServer().getPort());
        }

        // Configure the listener
        listener.setMinThreads(getMinThreads());
        listener.setMaxThreads(getMaxThreads());
        listener.setMaxIdleTimeMs(getMaxIdleTimeMs());
        listener.setLowResourcePersistTimeMs(getLowResourcePersistTimeMs());
        listener.setKeystore(getKeystorePath());
        listener.setPassword(getKeystorePassword());
        listener.setKeyPassword(getKeyPassword());
        setListener(listener);

        super.start();
    }

    /**
     * Returns the SSL keystore path.
     * 
     * @return The SSL keystore path.
     */
    public String getKeystorePath() {
        return getParameters().getFirstValue("keystorePath",
                System.getProperty("user.home") + File.separator + ".keystore");
    }

    /**
     * Returns the SSL keystore password.
     * 
     * @return The SSL keystore password.
     */
    public String getKeystorePassword() {
        return getParameters().getFirstValue("keystorePassword", "");
    }

    /**
     * Returns the SSL key password.
     * 
     * @return The SSL key password.
     */
    public String getKeyPassword() {
        return getParameters().getFirstValue("keyPassword", "");
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
