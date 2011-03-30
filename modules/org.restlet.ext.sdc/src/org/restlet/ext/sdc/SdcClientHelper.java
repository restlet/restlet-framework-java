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

package org.restlet.ext.sdc;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.adapter.HttpClientHelper;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.engine.security.SslUtils;
import org.restlet.ext.sdc.internal.SdcClientCall;
import org.restlet.ext.sdc.internal.SdcServerConnection;

/**
 * SDC tunnel connector. This is a client connector from the Restlet application
 * developer point of view, but internally it launches an SDC tunnel server to
 * allow SDC agents located inside intranet to establish SDC tunnels.<br>
 * <br>
 * Note that currently all SDC tunnel connections are accepted and are matched
 * with SDC client requests based on the SDC user name, domain and password.
 * Here is a usage example:<br>
 * 
 * <pre>
 * Request request = new Request(Method.GET, &quot;http://www.restlet.org&quot;);
 * request.setProtocol(Protocol.valueOf(&quot;SDC&quot;));
 * request.setProxyChallengeResponse(new ChallengeResponse(ChallengeScheme
 *         .valueOf(&quot;SDC&quot;), &quot;myUser@example.com&quot;, &quot;myPassword&quot;));
 * Response response = sdcClient.handle(request);
 * response.getEntity().write(System.out);
 * </pre>
 * 
 * Here is the list of additional parameters that are supported. They should be
 * set in the Server's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>serverPort</td>
 * <td>int</td>
 * <td>4433</td>
 * <td>The port number of the SDC tunnels server.</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Let you specify a {@link SslContextFactory} instance for a more complete
 * and flexible SSL context setting. If this parameter is set, it takes
 * Precedence over the other SSL parameters below.</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Let you specify a {@link SslContextFactory} class name as a parameter, or
 * an instance as an attribute for a more complete and flexible SSL context
 * setting. If set, it takes precedence over the other SSL parameters below.</td>
 * </tr>
 * <tr>
 * <tr>
 * <td>keystorePath</td>
 * <td>String</td>
 * <td>${user.home}/.keystore</td>
 * <td>SSL keystore path.</td>
 * </tr>
 * <tr>
 * <td>keystorePassword</td>
 * <td>String</td>
 * <td></td>
 * <td>SSL keystore password.</td>
 * </tr>
 * <tr>
 * <td>keystoreType</td>
 * <td>String</td>
 * <td>JKS</td>
 * <td>SSL keystore type</td>
 * </tr>
 * <tr>
 * <td>keyPassword</td>
 * <td>String</td>
 * <td>${keystorePassword}</td>
 * <td>SSL key password.</td>
 * </tr>
 * <tr>
 * <td>certAlgorithm</td>
 * <td>String</td>
 * <td>SunX509</td>
 * <td>SSL certificate algorithm.</td>
 * </tr>
 * <tr>
 * <td>enabledCipherSuites</td>
 * <td>String</td>
 * <td>TLS_RSA_WITH_AES_128_CBC_SHA</td>
 * <td>Whitespace-separated list of enabled cipher suites and/or can be
 * specified multiple times.</td>
 * </tr>
 * <tr>
 * <td>disabledCipherSuites</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Whitespace-separated list of disabled cipher suites and/or can be
 * specified multiple times. It affects the cipher suites manually enabled or
 * the default ones.</td>
 * </tr>
 * <tr>
 * <td>needClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we require client certificate authentication.</td>
 * </tr>
 * <tr>
 * <td>sslProtocol</td>
 * <td>String</td>
 * <td>TLS</td>
 * <td>SSL protocol.</td>
 * </tr>
 * <tr>
 * <td>wantClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we would like client certificate authentication (only for
 * the BIO connector type).</td>
 * </tr>
 * </table>
 * 
 * 
 * @author Jerome Louvel
 */
public class SdcClientHelper extends HttpClientHelper {

    /** The map of SDC tunnel connections. */
    private final Map<String, SdcServerConnection> connections;

    /**
     * The latch that can be used to block until the connector is ready to
     * process requests.
     */
    private final CountDownLatch latch;

    /** The connection worker service. */
    private final ExecutorService workerService;

    /**
     * Constructor.
     * 
     * @param client
     *            The parent client.
     */
    public SdcClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.valueOf("SDC"));
        this.connections = new TreeMap<String, SdcServerConnection>();
        this.workerService = Executors.newCachedThreadPool();
        this.latch = new CountDownLatch(1);
    }

    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            Reference targetRef = request.getResourceRef().getBaseRef() == null ? request
                    .getResourceRef() : request.getResourceRef().getTargetRef();
            ChallengeResponse cr = request.getProxyChallengeResponse();

            if (cr != null) {
                if (cr.getScheme().equals(ChallengeScheme.valueOf("SDC"))) {
                    String key = cr.getIdentifier() + ":"
                            + String.valueOf(cr.getSecret());
                    int retryAttempts = 3;
                    int retryDelay = 3000;
                    SdcServerConnection ssc = null;

                    for (int i = 0; (ssc == null) && (i < retryAttempts); i++) {
                        ssc = getConnections().get(key);

                        if (ssc == null) {
                            try {
                                Thread.sleep(retryDelay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (ssc == null) {
                        getLogger()
                                .log(Level.WARNING,
                                        "Unable to find an established SDC tunnel for this request: ",
                                        request.getResourceRef());
                    } else {
                        result = new SdcClientCall(this, ssc, request
                                .getMethod().toString(), targetRef.toString());
                    }
                }
            }
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING,
                    "Unable to create the HTTP client call", ioe);
        }

        return result;
    }

    /**
     * Returns the map of SDC tunnel connections.
     * 
     * @return The map of SDC tunnel connections.
     */
    public Map<String, SdcServerConnection> getConnections() {
        return connections;
    }

    /**
     * Returns the list of enabled cipher suites. By default, this suite is
     * returned: "TLS_RSA_WITH_AES_128_CBC_SHA".
     * 
     * @return The list of enabled cipher suites.
     */
    public String[] getEnabledCipherSuites() {
        return getHelpedParameters().getValuesArray("enabledCipherSuites",
                "TLS_RSA_WITH_AES_128_CBC_SHA");
    }

    /**
     * Returns the latch that can be used to block until the connector is ready
     * to process requests.
     * 
     * @return The latch that can be used to block until the connector is ready
     *         to process requests.
     */
    public CountDownLatch getLatch() {
        return latch;
    }

    /**
     * Returns the port number of the SDC tunnels server.
     * 
     * @return The port number of the SDC tunnels server.
     */
    public int getServerPort() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "serverPort", "4433"));
    }

    /**
     * Returns the connection worker service.
     * 
     * @return The connection worker service.
     */
    public ExecutorService getWorkerService() {
        return workerService;
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info(
                "Starting the SDC client and its tunnel server on port "
                        + getServerPort());

        new Thread() {
            @Override
            public void run() {
                try {
                    // Creates the server socket
                    SslContextFactory contextFactory = SslUtils
                            .getSslContextFactory(SdcClientHelper.this);
                    SSLServerSocketFactory ssf = contextFactory
                            .createSslContext().getServerSocketFactory();
                    SSLServerSocket serverSocket = (SSLServerSocket) ssf
                            .createServerSocket(getServerPort());

                    // Accept the next socket
                    boolean loop = true;
                    SSLSocket socket = null;

                    // Let the SDC tunnel creator continue
                    getLatch().countDown();

                    while (loop) {
                        try {
                            socket = (SSLSocket) serverSocket.accept();
                            socket.setEnabledCipherSuites(getEnabledCipherSuites());
                            SdcServerConnection ssc = new SdcServerConnection(
                                    SdcClientHelper.this, socket);
                            ssc.connect();
                            if (ssc.getKey() != null) {
                                getConnections().put(ssc.getKey(), ssc);
                            } else {
                                getLogger().warning(
                                        "Detected wrong connection.");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();

        // Wait for the listener to start up and count down the latch
        // This blocks until the server is ready to receive connections
        try {
            if (!getLatch().await(IoUtils.TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                // Timeout detected
                getLogger()
                        .warning(
                                "The calling thread timed out while waiting for the connector to be ready to accept connections.");
            }
        } catch (InterruptedException ex) {
            getLogger()
                    .log(Level.WARNING,
                            "Interrupted while waiting for starting latch. Stopping...",
                            ex);
            stop();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the SDC client and its tunnel server");
    }

}
