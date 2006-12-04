/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.data;

/**
 * Server specific data related to a call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServerInfo {
    /** The IP address. */
    private String address;

    /** The agent name. */
    private String agent;

    /** The port number. */
    private Integer port;

    /**
     * Constructor.
     */
    public ServerInfo() {
        this.address = null;
        this.agent = null;
        this.port = null;
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
     * Returns the agent name (ex: "Noelios Restlet Engine/1.0").
     * 
     * @return The agent name.
     */
    public String getAgent() {
        return this.agent;
    }

    /**
     * Returns the port number which received the call.
     * 
     * @return The port number which received the call.
     */
    public Integer getPort() {
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
     * Sets the agent name (ex: "Noelios Restlet Engine/1.0").
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
    public void setPort(Integer port) {
        this.port = port;
    }

}
