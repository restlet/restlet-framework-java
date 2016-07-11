/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.engine.ssl.SslUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * Internal HTTPS server connector. Here is the list of additional parameters
 * that are supported. They should be set in the Server's context before it is
 * started:
 * <table>
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
 * SSL context setting.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link DefaultSslContextFactory} class.
 * 
 * @author Jerome Louvel
 */
public class HttpsServerHelper extends NetServerHelper {
    /** The underlying HTTPS server. */
    private volatile HttpsServer server;

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

    /** Starts the Restlet. */
    @Override
    public void start() throws Exception {
        // Initialize the SSL context
        SslContextFactory sslContextFactory = SslUtils
                .getSslContextFactory(this);
        SSLContext sslContext = sslContextFactory.createSslContext();
        String addr = getHelped().getAddress();

        if (addr != null) {
            // This call may throw UnknownHostException and otherwise always
            // returns an instance of INetAddress.
            // Note: textual representation of inet addresses are supported
            InetAddress iaddr = InetAddress.getByName(addr);

            // Note: the backlog of 50 is the default
            setAddress(new InetSocketAddress(iaddr, getHelped().getPort()));
        } else {
            int port = getHelped().getPort();

            // Use ephemeral port
            if (port > 0) {
                setAddress(new InetSocketAddress(getHelped().getPort()));
            }
        }

        // Complete initialization
        this.server = HttpsServer.create(new InetSocketAddress(getHelped()
                .getPort()), 0);
        final SSLParameters sslParams = sslContext.getDefaultSSLParameters();
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                params.setSSLParameters(sslParams);
            }
        });

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                HttpsServerHelper.this.handle(new HttpExchangeCall(getHelped(),
                        httpExchange, true));
            }
        });
        // creates a default executor
        server.setExecutor(createThreadPool());
        server.start();

        setConfidential(true);
        setEphemeralPort(server.getAddress().getPort());
        super.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        this.server.stop(0);
    }

}
