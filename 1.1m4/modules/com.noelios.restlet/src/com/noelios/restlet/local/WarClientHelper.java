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

package com.noelios.restlet.local;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Connector to resources packaged as a WAR container. The WAR container can be
 * under the form of a single compressed file or as a uncompressed directory.
 * Here is a sample URI:<br>
 * 
 * <pre>
 * war:///path/to/my/resource/entry.txt
 * </pre>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class WarClientHelper extends FileClientHelper {

    /**
     * Constructor. Note that the common list of metadata associations based on
     * extensions is added, see the addCommonExtensions() method.
     * 
     * @param client
     *                The client to help.
     */
    public WarClientHelper(Client client) {
        super(client);
        getProtocols().clear();
        getProtocols().add(Protocol.WAR);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        String scheme = request.getResourceRef().getScheme();

        // Ensure that all ".." and "." are normalized into the path
        // to prevent unauthorized access to user directories.
        request.getResourceRef().normalize();

        if (scheme.equalsIgnoreCase("war")) {
            handleWar(request, response);
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only WAR is supported.");
        }
    }

    /**
     * Handles a call using the current Web Application.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    protected abstract void handleWar(Request request, Response response);

}
