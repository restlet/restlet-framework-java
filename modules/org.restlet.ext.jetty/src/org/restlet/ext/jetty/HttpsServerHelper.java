/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jetty;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.ssl.DefaultSslContextFactory;
import org.restlet.ext.ssl.SslContextFactory;
import org.restlet.ext.ssl.internal.SslUtils;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that
 * are supported. They should be set in the Server's context before it is
 * started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>int</td>
 * <td>2</td>
 * <td>The type of Jetty connector to use.<br>
 * 1 : Selecting NIO connector (Jetty's SslSelectChannelConnector class).<br>
 * 2 : Blocking BIO connector (Jetty's SslSocketConnector class)</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.ext.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a
 * parameter, or an instance as an attribute for a more complete and flexible
 * SSL context setting.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link DefaultSslContextFactory} class.
 * 
 * @see <a
 *      href="http://docs.codehaus.org/display/JETTY/How+to+configure+SSL">How
 *      to configure SSL for Jetty</a>
 * @author Jerome Louvel
 */
public class HttpsServerHelper extends JettyServerHelper {
    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpsServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTPS);
    }

    /**
     * Creates a new internal Jetty connector.
     * 
     * @return A new internal Jetty connector.
     */
    @Override
    protected AbstractConnector createConnector() {
        AbstractConnector result = null;
        final SslContextFactory sslContextFactory = SslUtils
                .getSslContextFactory(this);
        final String[] excludedCipherSuites = SslUtils
                .getDisabledCipherSuites(this);

        // Create and configure the Jetty HTTP connector
        switch (getType()) {
        case 1:
            // Selecting NIO connector
            SslSelectChannelConnector nioResult;
            nioResult = new SslSelectChannelConnector() {
                @Override
                protected SSLContext createSSLContext() throws Exception {
                    return sslContextFactory.createSslContext();
                }
            };

            if (excludedCipherSuites != null) {
                nioResult.setExcludeCipherSuites(excludedCipherSuites);
            }

            result = nioResult;
            break;
        case 2:
            // Blocking BIO connector
            SslSocketConnector bioResult = new SslSocketConnector() {
                @Override
                protected SSLServerSocketFactory createFactory()
                        throws Exception {
                    final SSLContext sslContext = sslContextFactory
                            .createSslContext();
                    return sslContext.getServerSocketFactory();
                }
            };

            if (excludedCipherSuites != null) {
                bioResult.setExcludeCipherSuites(excludedCipherSuites);
            }

            result = bioResult;
            break;
        }

        return result;
    }

    /**
     * Returns the type of Jetty connector to use.
     * 
     * @return The type of Jetty connector to use.
     */
    public int getType() {
        return Integer.parseInt(getHelpedParameters()
                .getFirstValue("type", "2"));
    }

}
