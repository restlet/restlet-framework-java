/**
 * Copyright 2005-2010 Noelios Technologies.
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
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.http.ClientCall;
import org.restlet.engine.http.HttpClientHelper;
import org.restlet.engine.io.NioUtils;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.engine.security.SslUtils;
import org.restlet.ext.sdc.internal.SdcClientCall;
import org.restlet.ext.sdc.internal.SdcServerConnection;

/**
 * SDC tunnel connector. Here is the list of parameters that are supported. They
 * should be set in the Client's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>adapter</td>
 * <td>String</td>
 * <td>org.restlet.engine.http.ClientAdapter</td>
 * <td>Class name of the adapter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public class SdcClientHelper extends HttpClientHelper {

    private final Map<String, SdcServerConnection> connections;

    /** The connection worker service. */
    private final ExecutorService workerService;

    public SdcClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.valueOf("SDC"));
        this.connections = new TreeMap<String, SdcServerConnection>();
        this.workerService = Executors.newCachedThreadPool();
    }

    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            Reference targetRef = request.getResourceRef().getBaseRef() == null ? request
                    .getResourceRef() : request.getResourceRef().getTargetRef();

            if (!request.getMethod().equals(Method.GET)) {
                throw new IOException("Only GET methods are allowed.");
            }

            if (request.getChallengeResponse() != null) {
                if (request.getChallengeResponse().getScheme()
                        .equals(ChallengeScheme.valueOf("SDC"))) {
                    String key = request.getChallengeResponse().getIdentifier()
                            + ":"
                            + String.valueOf(request.getChallengeResponse()
                                    .getSecret());
                    SdcServerConnection ssc = getConnections().get(key);

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

    public Map<String, SdcServerConnection> getConnections() {
        return connections;
    }

    public String[] getEnabledCipherSuites() {
        return new String[] { "TLS_RSA_WITH_AES_128_CBC_SHA" };
    }

    public ExecutorService getWorkerService() {
        return workerService;
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();

        getLogger().info("Starting the SDC tunnel on port 4433.");
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                try {
                    SslContextFactory contextFactory = SslUtils
                            .getSslContextFactory(SdcClientHelper.this);
                    SSLServerSocketFactory ssf = contextFactory
                            .createSslContext().getServerSocketFactory();
                    SSLServerSocket serverSocket = (SSLServerSocket) ssf
                            .createServerSocket(4433);

                    // Accept the next socket
                    boolean loop = true;
                    SSLSocket socket = null;

                    // Let the SDC tunnel creator continue
                    latch.countDown();

                    while (loop) {
                        try {
                            socket = (SSLSocket) serverSocket.accept();
                            socket.setEnabledCipherSuites(getEnabledCipherSuites());
                            SdcServerConnection ssc;
                            ssc = new SdcServerConnection(SdcClientHelper.this,
                                    socket);
                            ssc.connect();
                            getConnections().put(ssc.getKey(), ssc);
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
            if (!latch.await(NioUtils.NIO_TIMEOUT, TimeUnit.MILLISECONDS)) {
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
        getLogger().info("Stopping the SDC tunnel");
    }

}
