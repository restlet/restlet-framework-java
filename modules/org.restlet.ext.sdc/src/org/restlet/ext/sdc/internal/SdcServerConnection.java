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

package org.restlet.ext.sdc.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import org.restlet.ext.sdc.SdcClientHelper;

import com.google.dataconnector.protocol.Dispatchable;
import com.google.dataconnector.protocol.FrameReceiver;
import com.google.dataconnector.protocol.FrameSender;
import com.google.dataconnector.protocol.FramingException;
import com.google.dataconnector.protocol.proto.SdcFrame;
import com.google.dataconnector.protocol.proto.SdcFrame.AuthorizationInfo;
import com.google.dataconnector.protocol.proto.SdcFrame.AuthorizationInfo.ResultCode;
import com.google.dataconnector.protocol.proto.SdcFrame.FetchReply;
import com.google.dataconnector.protocol.proto.SdcFrame.FrameInfo;
import com.google.dataconnector.protocol.proto.SdcFrame.FrameInfo.Type;
import com.google.dataconnector.protocol.proto.SdcFrame.HealthCheckInfo;
import com.google.dataconnector.protocol.proto.SdcFrame.HealthCheckInfo.Source;
import com.google.dataconnector.protocol.proto.SdcFrame.RegistrationRequestV4;
import com.google.dataconnector.protocol.proto.SdcFrame.RegistrationResponseV4;
import com.google.dataconnector.protocol.proto.SdcFrame.ServerSuppliedConf;
import com.google.dataconnector.util.ShutdownManager;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * The SDC server connection established between this SDC client connector,
 * acting as the tunnel server and a remote SDC agent.
 * 
 * @author Jerome Louvel
 */
public class SdcServerConnection implements Dispatchable {

    /** The map of pending SDC/HTTP client calls, keyed by the unique call ID. */
    private final Map<String, SdcClientCall> calls;

    /** The receiver for SDC Frame protocol. */
    private final FrameReceiver frameReceiver;

    /** The sender for SDC Frame protocol. */
    private final FrameSender frameSender;

    /** The parent SDC client helper. */
    private final SdcClientHelper helper;

    /** The socket input stream. */
    private final InputStream inputStream;

    /** The authorization key composed of the email address and the password. */
    private volatile String key;

    /** The socket output stream. */
    private final OutputStream outputStream;

    /** The SSL connection socket. */
    private final SSLSocket socket;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent SDC client helper.
     * @param socket
     *            The SSL connection socket.
     * @throws IOException
     */
    public SdcServerConnection(SdcClientHelper helper, SSLSocket socket)
            throws IOException {
        this.helper = helper;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.frameReceiver = new FrameReceiver();
        this.frameReceiver.setInputStream(getInputStream());
        this.calls = new ConcurrentHashMap<String, SdcClientCall>();

        BlockingQueue<FrameInfo> sendQueue = new LinkedBlockingQueue<SdcFrame.FrameInfo>();
        ShutdownManager shutdownManager = new ShutdownManager();
        this.frameSender = new FrameSender(sendQueue, shutdownManager);
        this.frameSender.setOutputStream(getOutputStream());
    }

    /**
     * Connects this SDC tunnel with one remote SDC agent.
     * 
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            // Initial handshakecom.google.dataconnector.util.ShutdownManager
            readHandshake();

            // Authorization step
            FrameInfo frameInfo = getFrameReceiver().readOneFrame();

            if (frameInfo.getType() == FrameInfo.Type.AUTHORIZATION) {
                AuthorizationInfo authorizationRequest = AuthorizationInfo
                        .parseFrom(frameInfo.getPayload());
                setKey(authorizationRequest.getEmail() + ":"
                        + authorizationRequest.getPassword());

                AuthorizationInfo authorizationResponse = AuthorizationInfo
                        .newBuilder().setResult(ResultCode.OK).build();

                getFrameSender().sendFrame(FrameInfo.Type.AUTHORIZATION,
                        authorizationResponse.toByteString());

                // Register frame dispatchers
                getFrameReceiver().registerDispatcher(Type.FETCH_REQUEST, this);
                getFrameReceiver().registerDispatcher(Type.REGISTRATION, this);
                getFrameReceiver().registerDispatcher(Type.HEALTH_CHECK, this);

                // Launch a thread to asynchronously receive incoming frames
                getHelper().getWorkerService().execute(new Runnable() {
                    public void run() {
                        try {
                            getFrameReceiver().startDispatching();
                        } catch (FramingException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Launch a thread to asynchronously send outgoing frames
                getHelper().getWorkerService().execute(new Runnable() {
                    public void run() {
                        getFrameSender().run();
                    }
                });
            } else {
                getLogger().log(
                        Level.WARNING,
                        "Unable to authorize the connection. Wrong frame type received: "
                                + frameInfo);
            }
        } catch (FramingException e) {
            getLogger().log(Level.WARNING,
                    "Unable to authorize the connection.", e);
        }
    }

    /**
     * Asynchronously process the response frames received from the SDC agent.
     * 
     * @param frameInfo
     *            The SDC frame to parse.
     */
    public void dispatch(FrameInfo frameInfo) throws FramingException {
        try {
            if (frameInfo.getType() == Type.FETCH_REQUEST) {
                FetchReply fetchReply = FetchReply.parseFrom(frameInfo
                        .getPayload());

                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().log(Level.FINE,
                            "SDC response received: " + fetchReply.toString());
                }

                // Lookup the associated SDC request
                SdcClientCall call = getCalls().get(fetchReply.getId());

                if (call != null) {
                    call.setFetchReply(fetchReply);

                    // Unblock the client thread
                    call.getLatch().countDown();
                } else if (getLogger().isLoggable(Level.WARNING)) {
                    getLogger()
                            .log(Level.WARNING,
                                    "Unable to find the SDC request associated to the received response");
                }
            } else if (frameInfo.getType() == FrameInfo.Type.REGISTRATION) {
                RegistrationRequestV4 registrationRequest = RegistrationRequestV4
                        .parseFrom(frameInfo.getPayload());

                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().log(
                            Level.FINE,
                            "SDC tunnel registration received: "
                                    + registrationRequest);
                }

                RegistrationResponseV4 registrationResponse = RegistrationResponseV4
                        .newBuilder()
                        .setResult(
                                com.google.dataconnector.protocol.proto.SdcFrame.RegistrationResponseV4.ResultCode.OK)
                        .setServerSuppliedConf(
                                ServerSuppliedConf.newBuilder()
                                        .setHealthCheckTimeout(5000)
                                        .setHealthCheckWakeUpInterval(5000)
                                        .build()).build();

                getFrameSender().sendFrame(FrameInfo.Type.REGISTRATION,
                        registrationResponse.toByteString());
            } else if (frameInfo.getType() == Type.HEALTH_CHECK) {
                HealthCheckInfo healthCheckResponse = HealthCheckInfo
                        .newBuilder()
                        .setSource(Source.SERVER)
                        .setTimeStamp(System.currentTimeMillis())
                        .setType(
                                com.google.dataconnector.protocol.proto.SdcFrame.HealthCheckInfo.Type.RESPONSE)
                        .build();

                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger()
                            .log(Level.FINE,
                                    "SDC health check received: "
                                            + healthCheckResponse);
                }

                // Reply to the check
                getFrameSender().sendFrame(Type.HEALTH_CHECK,
                        healthCheckResponse.toByteString());
            } else if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE,
                        "Unexpected SDC frame received: " + frameInfo);
            }
        } catch (InvalidProtocolBufferException e) {
            getLogger().log(Level.WARNING, "Invalid SDC frame received", e);
        }
    }

    /**
     * Returns the map of pending SDC/HTTP client calls, keyed by the unique
     * call ID.
     * 
     * @return The map of pending SDC/HTTP client calls, keyed by the unique
     *         call ID.
     */
    public Map<String, SdcClientCall> getCalls() {
        return calls;
    }

    /**
     * Returns the receiver for SDC Frame protocol.
     * 
     * @return The receiver for SDC Frame protocol.
     */
    public FrameReceiver getFrameReceiver() {
        return frameReceiver;
    }

    /**
     * Returns the sender for SDC Frame protocol.
     * 
     * @return The sender for SDC Frame protocol.
     */
    public FrameSender getFrameSender() {
        return frameSender;
    }

    /**
     * Returns the parent SDC client helper.
     * 
     * @return The parent SDC client helper.
     */
    public SdcClientHelper getHelper() {
        return helper;
    }

    /**
     * Returns the socket input stream.
     * 
     * @return The socket input stream.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the authorization key composed of the email address and the
     * password separated by a colon character.
     * 
     * @return The authorization key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the current logger.
     * 
     * @return The current logger.
     */
    public Logger getLogger() {
        return getHelper().getLogger();
    }

    /**
     * Returns the socket output stream.
     * 
     * @return The socket output stream.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Returns the SSL connection socket.
     * 
     * @return The SSL connection socket.
     */
    public SSLSocket getSocket() {
        return socket;
    }

    /**
     * Reads the SDC initial handshake message from the socket input stream.
     * 
     * @return The SDC initial handshake message from the socket input stream.
     * @throws IOException
     */
    protected boolean readHandshake() throws IOException {
        boolean result = true;
        byte[] hsm = ("v5.0 "
                + FrameReceiver.class.getPackage().getImplementationVersion() + "\n")
                .getBytes();
        int c;

        for (int i = 0; result && (i < hsm.length); i++) {
            c = getInputStream().read();
            result = (c == hsm[i]);
        }

        return result;
    }

    /**
     * Effectively send the requests using the SDC frame protocol. It also
     * stores the call in the map to be later be able to associate it with its
     * response.
     * 
     * @param call
     *            The SDC client call to be sent.
     */
    public void sendRequest(SdcClientCall call) {
        getCalls().put(call.getFetchRequest().getId(), call);
        getFrameSender().sendFrame(FrameInfo.Type.FETCH_REQUEST,
                call.getFetchRequest().toByteString());

    }

    /**
     * Sets the authorization key composed of the email address and the password
     * separated by a colon character.
     * 
     * @param key
     *            The authorization key.
     */
    public void setKey(String key) {
        this.key = key;
    }

}
