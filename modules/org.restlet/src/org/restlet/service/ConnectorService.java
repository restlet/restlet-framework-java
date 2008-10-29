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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Protocol;
import org.restlet.resource.Representation;

/**
 * Service declaring client and server connectors. This is useful at deployment
 * time to know which connectors an application expects to be able to use.
 * 
 * Implementation note: the parent component will ensure that client connectors
 * won't automatically follow redirections. This will ensure a consistent
 * behavior and portability of applications.
 * 
 * @author Jerome Louvel
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
