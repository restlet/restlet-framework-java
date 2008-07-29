/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Protocol;
import org.restlet.resource.Representation;

/**
 * Service declaring client and server connectors.
 * 
 * Implementation note: the parent component will ensure that client connectors
 * won't automatically follow redirections. This will ensure a consistent
 * behavior and portability of applications.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ConnectorService extends Service {
    /** The list of required client protocols. */
    private volatile List<Protocol> clientProtocols;

    /** The list of required server protocols. */
    private volatile List<Protocol> serverProtocols;

    /**
     * Constructor.
     */
    public ConnectorService() {
        this.clientProtocols = new CopyOnWriteArrayList<Protocol>();
        this.serverProtocols = new CopyOnWriteArrayList<Protocol>();
    }

    /**
     * Call-back method invoked by the client or server connectors just after
     * sending the entity to the target component. The default implementation
     * does nothing.
     * 
     * @param entity
     *            The entity about to be committed.
     */
    public void afterSend(Representation entity) {
        // Do nothing by default.
    }

    /**
     * Call-back method invoked by the client or server connectors just before
     * sending the entity to the target component. The default implementation
     * does nothing.
     * 
     * @param entity
     *            The entity about to be committed.
     */
    public void beforeSend(Representation entity) {
        // Do nothing by default.
    }

    /**
     * Returns the modifiable list of required client protocols. You need to
     * update this list if you need the parent component to provide additional
     * client connectors.
     * 
     * @return The list of required client protocols.
     */
    public List<Protocol> getClientProtocols() {
        return this.clientProtocols;
    }

    /**
     * Returns the modifiable list of required server protocols. An empty list
     * means that all protocols are potentially supported (default case). You
     * should update this list to restrict the actual protocols supported by
     * your application.
     * 
     * @return The list of required server protocols.
     */
    public List<Protocol> getServerProtocols() {
        return this.serverProtocols;
    }

    /**
     * Sets the list of required client protocols.
     * 
     * @param clientProtocols
     *            The list of required client protocols.
     */
    public void setClientProtocols(List<Protocol> clientProtocols) {
        this.clientProtocols = clientProtocols;
    }

    /**
     * Sets he list of required server protocols.
     * 
     * @param serverProtocols
     *            The list of required server protocols.
     */
    public void setServerProtocols(List<Protocol> serverProtocols) {
        this.serverProtocols = serverProtocols;
    }

}
