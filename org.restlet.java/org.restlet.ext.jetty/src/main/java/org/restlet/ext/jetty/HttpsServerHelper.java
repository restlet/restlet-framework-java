/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import java.util.logging.Level;

import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.ext.jetty.internal.RestletSslContextFactoryServer;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that
 * are supported. They should be set in the Server's context before it is
 * started:
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.engine.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a
 * parameter, or an instance as an attribute for a more complete and flexible
 * SSL context setting</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link DefaultSslContextFactory} class.
 * 
 * @see <a href=
 *      "https://jetty.org/docs/jetty/12/operations-guide/keystore/index.html">How
 *      to configure SSL for Jetty</a>
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class HttpsServerHelper extends JettyServerHelper {

    /**
     * Constructor.
     * 
     * @param server The server to help.
     */
    public HttpsServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTPS);
    }

    @Override
    protected ConnectionFactory[] createConnectionFactories(
            HttpConfiguration configuration) {
        ConnectionFactory[] result;

        try {
            SslContextFactory.Server sslContextFactory = new RestletSslContextFactoryServer(
                    org.restlet.engine.ssl.SslUtils.getSslContextFactory(this));
            result = AbstractConnectionFactory.getFactories(sslContextFactory,
                    new HttpConnectionFactory(configuration));
        } catch (Exception e) {
            result = null;
            getLogger().log(Level.WARNING,
                    "Unable to create the Jetty SSL context factory", e);
        }

        return result;
    }
}
