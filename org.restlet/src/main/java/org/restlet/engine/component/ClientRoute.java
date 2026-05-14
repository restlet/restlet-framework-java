/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.component;

import java.util.logging.Level;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

/**
 * Router scorer based on a target client connector.
 *
 * <p>Concurrency note: instances of this class or its subclasses can be invoked by several threads
 * at the same time and therefore must be thread-safe. You should be especially careful when storing
 * state in member variables.
 *
 * @author Jerome Louvel
 */
public class ClientRoute extends Route {
    /**
     * Constructor.
     *
     * @param router The parent router.
     * @param target The target client.
     */
    public ClientRoute(Router router, Client target) {
        super(router, target);
    }

    /**
     * Returns the target client.
     *
     * @return The target client.
     */
    public Client getClient() {
        return (Client) getNext();
    }

    /**
     * Returns the score for a given call (between 0 and 1.0).
     *
     * @param request The request to score.
     * @param response The response to score.
     * @return The score for a given call (between 0 and 1.0).
     */
    @Override
    public float score(Request request, Response response) {
        float result = 0F;

        // Add the protocol score
        final Protocol protocol = request.getProtocol();

        if (protocol == null) {
            getLogger().warning("Unable to determine the protocol to use for this call.");
        } else if (getClient().getProtocols().contains(protocol)) {
            result = 1.0F;
        }

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger()
                    .finer(
                            "Call score for the \""
                                    + getClient().getProtocols().toString()
                                    + "\" client: "
                                    + result);
        }

        return result;
    }
}
