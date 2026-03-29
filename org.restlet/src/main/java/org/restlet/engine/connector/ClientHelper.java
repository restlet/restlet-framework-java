/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import org.restlet.Client;

/**
 * Client connector helper. Base client helper based on NIO non-blocking sockets. Here is the list
 * of parameters that are supported. They should be set in the Client's context before it is
 * started:
 *
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * </table>
 *
 * @author Jerome Louvel
 */
public class ClientHelper extends ConnectorHelper<Client> {

    /**
     * Constructor.
     *
     * @param client The client to help.
     */
    public ClientHelper(Client client) {
        super(client);
    }
}
