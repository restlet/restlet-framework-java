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

package org.restlet.service;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Protocol;
import org.restlet.resource.Representation;

/**
 * Service providing client and server connectors.
 * 
 * Implementation note: the parent component will ensure that client connectors
 * won't automatically follow redirections. This will ensure a consistent
 * behavior and portability of applications.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ConnectorService {
    /** The list of required client protocols. */
    private List<Protocol> clientProtocols;

    /** The list of required server protocols. */
    private List<Protocol> serverProtocols;

    /**
     * Constructor.
     */
    public ConnectorService() {
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
     * Returns the list of required client protocols. You need to update
     * this list if you need the parent component to provide additional client
     * connectors.
     * 
     * @return The list of required client protocols.
     */
    public List<Protocol> getClientProtocols() {
        if (this.clientProtocols == null)
            this.clientProtocols = new ArrayList<Protocol>();
        return this.clientProtocols;
    }

    /**
     * Returns the list of required server protocols. An empty list means that
     * all protocols are potentially supported (default case). You should update
     * this list to restrict the actual protocols supported by your application.
     * 
     * @return The list of required server protocols.
     */
    public List<Protocol> getServerProtocols() {
        if (this.serverProtocols == null)
            this.serverProtocols = new ArrayList<Protocol>();
        return this.serverProtocols;
    }

}
