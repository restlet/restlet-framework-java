/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.restlet.Application;
import org.restlet.Connector;
import org.restlet.data.Protocol;
import org.restlet.engine.RestletHelper;
import org.restlet.service.ConnectorService;

/**
 * Base connector helper.
 *
 * @author Jerome Louvel
 */
public abstract class ConnectorHelper<T extends Connector> extends RestletHelper<T> {

    /**
     * Returns the connector service associated with a request.
     *
     * @return The connector service associated with a request.
     */
    public static ConnectorService getConnectorService() {
        final ConnectorService result;
        Application application = Application.getCurrent();

        if (application != null) {
            result = application.getConnectorService();
        } else {
            result = new ConnectorService();
        }

        return result;
    }

    /** The protocols simultaneously supported. */
    private final List<Protocol> protocols;

    /** Constructor. */
    protected ConnectorHelper(T connector) {
        super(connector);
        this.protocols = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns the protocols simultaneously supported.
     *
     * @return The protocols simultaneously supported.
     */
    public List<Protocol> getProtocols() {
        return this.protocols;
    }

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {}

    @Override
    public void update() throws Exception {}
}
