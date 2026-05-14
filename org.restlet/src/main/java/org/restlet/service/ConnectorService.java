/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.restlet.Application;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;

/**
 * Application service declaring client and server connectors. This is useful at deployment time to
 * know which connectors an application expects to be able to use.<br>
 * <br>
 * If you need to override the {@link #afterSend(Representation)} method for example, just create a
 * subclass and set it on your application with the {@link
 * Application#setConnectorService(ConnectorService)} method.<br>
 * <br>
 * Implementation note: the parent component will ensure that client connectors won't automatically
 * follow redirections. This will ensure a consistent behavior and portability of applications.
 *
 * @author Jerome Louvel
 */
public class ConnectorService extends Service {
    /** The list of required client protocols. */
    private final List<Protocol> clientProtocols;

    /** The list of required server protocols. */
    private final List<Protocol> serverProtocols;

    /** Constructor. */
    public ConnectorService() {
        this.clientProtocols = new CopyOnWriteArrayList<>();
        this.serverProtocols = new CopyOnWriteArrayList<>();
    }

    /**
     * Call-back method invoked by the client or server connectors just after sending the response
     * to the target component. The default implementation does nothing.
     *
     * @param entity The optional entity about to be committed.
     */
    public void afterSend(Representation entity) {
        // Do nothing by default.
    }

    /**
     * Call-back method invoked by the client or server connectors just before sending the response
     * to the target component. The default implementation does nothing.
     *
     * @param entity The optional entity about to be committed.
     */
    public void beforeSend(Representation entity) {
        // Do nothing by default.
    }

    /**
     * Returns the modifiable list of required client protocols. You need to update this list if you
     * need the parent component to provide additional client connectors.
     *
     * @return The list of required client protocols.
     */
    public List<Protocol> getClientProtocols() {
        return this.clientProtocols;
    }

    /**
     * Returns the modifiable list of required server protocols. An empty list means that all
     * protocols are potentially supported (default case). You should update this list to restrict
     * the actual protocols supported by your application.
     *
     * @return The list of required server protocols.
     */
    public List<Protocol> getServerProtocols() {
        return this.serverProtocols;
    }

    /**
     * Sets the modifiable list of required client protocols. This method clears the current list
     * and adds all entries in the parameter list.
     *
     * @param clientProtocols A list of required client protocols.
     */
    public void setClientProtocols(List<Protocol> clientProtocols) {
        synchronized (getClientProtocols()) {
            if (clientProtocols != getClientProtocols()) {
                getClientProtocols().clear();

                if (clientProtocols != null) {
                    getClientProtocols().addAll(clientProtocols);
                }
            }
        }
    }

    /**
     * Sets the modifiable list of required server protocols. This method clears the current list
     * and adds all entries in the parameter list.
     *
     * @param serverProtocols A list of required server protocols.
     */
    public void setServerProtocols(List<Protocol> serverProtocols) {
        synchronized (getServerProtocols()) {
            if (serverProtocols != getServerProtocols()) {
                getServerProtocols().clear();

                if (serverProtocols != null) {
                    getServerProtocols().addAll(serverProtocols);
                }
            }
        }
    }
}
