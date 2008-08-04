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

import org.restlet.Server;

import com.noelios.restlet.http.HttpServerHelper;

/**
 * Abstract Jetty Web server connector. Here is the list of parameters that are
 * supported: <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>minThreads</td>
 * <td>int</td>
 * <td>2</td>
 * <td>Minimum threads waiting to service requests.</td>
 * </tr>
 * <tr>
 * <td>maxThread</td>
 * <td>int</td>
 * <td>256</td>
 * <td>Maximum threads that will service requests.</td>
 * </tr>
 * <tr>
 * <td>maxIdleTimeMs</td>
 * <td>int</td>
 * <td>10000</td>
 * <td>Time for an idle thread to wait for a request or read.</td>
 * </tr>
 * <tr>
 * <td>converter</td>
 * <td>String</td>
 * <td>com.noelios.restlet.http.HttpServerConverter</td>
 * <td>Class name of the converter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * <tr>
 * <td>useForwardedForHeader</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Lookup the "X-Forwarded-For" header supported by popular proxies and
 * caches and uses it to populate the Request.getClientAddresses() method
 * result. This information is only safe for intermediary components within your
 * local network. Other addresses could easily be changed by setting a fake
 * header and should not be trusted for serious security checks.</td>
 * </tr>
 * </table>
 * 
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class JettyServerHelper extends HttpServerHelper {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The Jetty listener (keep package prefixing). */
    private org.mortbay.http.HttpListener listener;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public JettyServerHelper(Server server) {
        super(server);
    }

    /**
     * Returns the Jetty listener.
     * 
     * @return The Jetty listener.
     */
    public org.mortbay.http.HttpListener getListener() {
        return this.listener;
    }

    /**
     * Sets the Jetty listener.
     * 
     * @param listener
     *            The Jetty listener.
     */
    public void setListener(org.mortbay.http.HttpListener listener) {
        this.listener = listener;
    }

    /** Start connector. */
    public void start() throws Exception {
        getListener().start();
    }

    /** Stop connector. */
    public void stop() throws Exception {
        getListener().stop();
    }

    /**
     * Returns the minimum threads waiting to service requests.
     * 
     * @return The minimum threads waiting to service requests.
     */
    public int getMinThreads() {
        return Integer.parseInt(getParameters()
                .getFirstValue("minThreads", "2"));
    }

    /**
     * Returns the maximum threads that will service requests.
     * 
     * @return The maximum threads that will service requests.
     */
    public int getMaxThreads() {
        return Integer.parseInt(getParameters().getFirstValue("maxThreads",
                "256"));
    }

    /**
     * Returns the time for an idle thread to wait for a request or read.
     * 
     * @return The time for an idle thread to wait for a request or read.
     */
    public int getMaxIdleTimeMs() {
        return Integer.parseInt(getParameters().getFirstValue("maxIdleTimeMs",
                "10000"));
    }

}
