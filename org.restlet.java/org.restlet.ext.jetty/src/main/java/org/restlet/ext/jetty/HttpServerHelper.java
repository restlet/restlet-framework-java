/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty HTTP server connector.
 * 
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class HttpServerHelper extends JettyServerHelper {

    /**
     * Constructor.
     * 
     * @param server The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    /**
     * Create and configure the Jetty HTTP connector
     * 
     * @param configuration The HTTP configuration.
     */
    @Override
    protected ConnectionFactory[] createConnectionFactories(
            final HttpConfiguration configuration) {
        return new ConnectionFactory[] {
                new HttpConnectionFactory(configuration) };
    }

}
