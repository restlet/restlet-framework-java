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

package com.noelios.restlet;

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Series;

/**
 * Server connector helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServerHelper extends ConnectorHelper {
    /** The server to help. */
    private Server server;

    /**
     * Constructor.
     * 
     * @param server
     *            The client to help.
     */
    public ServerHelper(Server server) {
        this.server = server;
    }

    /**
     * Returns the server to help.
     * 
     * @return The server to help.
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Returns the server parameters.
     * 
     * @return The server parameters.
     */
    public Series<Parameter> getParameters() {
        Series<Parameter> result = (getServer() != null) ? getServer()
                .getContext().getParameters() : null;
        if (result == null)
            result = new Form();
        return result;
    }

    /**
     * Returns the server logger.
     * 
     * @return The server logger.
     */
    public Logger getLogger() {
        return getServer().getLogger();
    }

    /**
     * Returns the server context.
     * 
     * @return The server context.
     */
    public Context getContext() {
        return getServer().getContext();
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
	@Override
    public void handle(Request request, Response response) {
        getServer().handle(request, response);
    }

    /**
     * Sets the server to help.
     * 
     * @param server
     *            The server to help.
     */
    public void setServer(Server server) {
        this.server = server;
    }

}
