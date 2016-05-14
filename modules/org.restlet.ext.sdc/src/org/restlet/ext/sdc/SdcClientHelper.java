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

package org.restlet.ext.sdc;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.adapter.HttpClientHelper;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.engine.ssl.SslUtils;
import org.restlet.engine.util.ReferenceUtils;
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
 * Request request = new Request(Method.GET, &quot;http://restlet.org&quot;);
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
 * <td>enabledCipherSuites</td>
 * <td>String</td>
 * <td>TLS_RSA_WITH_AES_128_CBC_SHA</td>
 * <td>Whitespace-separated list of enabled cipher suites and/or can be specified multiple times.</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.engine.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a parameter, or an instance as an attribute
 * for a more complete and flexible SSL context setting.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the {@link DefaultSslContextFactory} class.
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
        this.connections = new ConcurrentHashMap<String, SdcServerConnection>();
        this.workerService = Executors.newCachedThreadPool();
        this.latch = new CountDownLatch(1);
    }

    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            ChallengeResponse cr = request.getProxyChallengeResponse();

            if (cr != null) {
                if (cr.getScheme().equals(ChallengeScheme.valueOf("SDC"))) {
                    String key = cr.getIdentifier() + ":" + String.valueOf(cr.getSecret());
                    int retryAttempts = 3;
                    int retryDelay = 3000;
                    SdcServerConnection ssc = null;

                    for (int i = 0; (ssc == null) && (i < retryAttempts); i++) {
                        ssc = getConnections().get(key);

                        if (ssc == null) {
                            try {
                                Thread.sleep(retryDelay);
                            } catch (InterruptedException e) {
                                // MITRE, CWE-391 - Unchecked Error Condition
                                Thread.currentThread().interrupt();
                            }
                        }
                    }

                    if (ssc == null) {
                        getLogger()
                                .log(Level.WARNING,
                                        "Unable to find an established SDC tunnel for this request: ",
                                        request.getResourceRef());
                    } else {
                        result = new SdcClientCall(this,
                                ssc,
                                request.getMethod().toString(),
                                ReferenceUtils.update(request.getResourceRef(), request).toString());
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
            // MITRE, CWE-391 - Unchecked Error Condition
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the SDC client and its tunnel server");
    }

}
