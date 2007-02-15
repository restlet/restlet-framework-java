/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.component;

import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Router scorer based on a target VirtualHost.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ClientRoute extends Route {
    /**
     * Constructor.
     * 
     * @param router
     *            The parent router.
     * @param target
     *            The target client.
     */
    public ClientRoute(Router router, Client target) {
        super(router, "", target);
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
     * Sets the next client.
     * 
     * @param next
     *            The next client.
     */
    public void setNext(Client next) {
        super.setNext(next);
    }

    /**
     * Returns the score for a given call (between 0 and 1.0).
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @return The score for a given call (between 0 and 1.0).
     */
    public float score(Request request, Response response) {
        float result = 0F;

        // Add the protocol score
        Protocol protocol = request.getProtocol();

        if (protocol == null) {
            getLogger().warning(
                    "Unable to determine the protocol to use for this call.");
        } else if (getClient().getProtocols().contains(protocol)) {
            result = 1.0F;
        }

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer(
                    "Call score for the \""
                            + getClient().getProtocols().toString()
                            + "\" client: " + result);
        }

        return result;
    }
}
