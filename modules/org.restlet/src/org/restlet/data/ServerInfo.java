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

package org.restlet.data;

/**
 * Server specific data related to a call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ServerInfo {
    /** The IP address. */
    private volatile String address;

    /** The agent name. */
    private volatile String agent;

    /** The port number. */
    private volatile int port;

    /**
     * Constructor.
     */
    public ServerInfo() {
        this.address = null;
        this.agent = null;
        this.port = -1;
    }

    /**
     * Returns the IP address.
     * 
     * @return The IP address.
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Returns the agent name (ex: "Noelios Restlet Engine/1.1").
     * 
     * @return The agent name.
     */
    public String getAgent() {
        return this.agent;
    }

    /**
     * Returns the port number which received the call. If no port is specified,
     * -1 is returned.
     * 
     * @return The port number which received the call.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Sets the IP address which received the call.
     * 
     * @param address
     *            The IP address which received the call.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets the agent name (ex: "Noelios Restlet Engine/1.1").
     * 
     * @param agent
     *            The agent name.
     */
    public void setAgent(String agent) {
        this.agent = agent;
    }

    /**
     * Sets the port number which received the call.
     * 
     * @param port
     *            The port number which received the call.
     */
    public void setPort(int port) {
        this.port = port;
    }

}
