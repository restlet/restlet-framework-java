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

package org.restlet.ext.sdc.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLSocket;

import org.restlet.ext.sdc.SdcClientHelper;

import com.google.dataconnector.client.SdcConnection;
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

    private volatile String key;

    private final SSLSocket socket;

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private final FrameReceiver frameReceiver;

    private final FrameSender frameSender;

    /** Map of pending HTTP/SDC client calls, keyed by the unique call ID. */
    private final Map<String, SdcClientCall> calls;

    private final SdcClientHelper helper;

    /**
     * Constructor.
     * 
     * @param socket
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
        this.calls = new TreeMap<String, SdcClientCall>();

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
            // Initial handshake
            readHandshake();

            // Authorization step
            FrameInfo frameInfo = getFrameReceiver().readOneFrame();

            if (frameInfo.getType() == FrameInfo.Type.AUTHORIZATION) {
                AuthorizationInfo authorizationRequest = AuthorizationInfo
                        .parseFrom(frameInfo.getPayload());
                setKey(authorizationRequest.getEmail() + ":"
                        + authorizationRequest.getPassword());
                System.out.println(authorizationRequest);

                AuthorizationInfo authorizationResponse = AuthorizationInfo
                        .newBuilder().setResult(ResultCode.OK).build();

                getFrameSender().sendFrame(FrameInfo.Type.AUTHORIZATION,
                        authorizationResponse.toByteString());

                // Register frame dispatchers
                getFrameReceiver().registerDispatcher(Type.FETCH_REQUEST, this);
                getFrameReceiver().registerDispatcher(Type.AUTHORIZATION, this);
                getFrameReceiver().registerDispatcher(Type.REGISTRATION, this);
                getFrameReceiver().registerDispatcher(Type.HEALTH_CHECK, this);

                // Launch a thread to asynchronously receive incoming frames
                getHelper().getWorkerService().execute(new Runnable() {
                    @Override
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
                    @Override
                    public void run() {
                        getFrameSender().run();
                    }
                });
            } else {
                System.out
                        .println("Unable to authorize the connection. Wrong frame type received: "
                                + frameInfo);
            }
        } catch (FramingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asynchronously process the response frames received from the SDC agent.
     */
    @Override
    public void dispatch(FrameInfo frameInfo) throws FramingException {
        if (frameInfo.getType() == Type.FETCH_REQUEST) {
            System.out.println(frameInfo);

            try {
                FetchReply fetchReply = FetchReply.parseFrom(frameInfo
                        .getPayload());

                SdcClientCall call = getCalls().get(fetchReply.getId());

                if (call != null) {
                    call.setFetchReply(fetchReply);
                    call.getLatch().countDown();
                } else {
                    System.out
                            .println("Unable to find the client call associated to the received response");
                }

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        } else if (frameInfo.getType() == FrameInfo.Type.REGISTRATION) {
            RegistrationRequestV4 registrationRequest;

            try {
                registrationRequest = RegistrationRequestV4.parseFrom(frameInfo
                        .getPayload());
                System.out.println(registrationRequest);

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
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        } else if (frameInfo.getType() == Type.HEALTH_CHECK) {
            System.out.println(frameInfo);

            HealthCheckInfo checkResponse = HealthCheckInfo
                    .newBuilder()
                    .setSource(Source.SERVER)
                    .setTimeStamp(System.currentTimeMillis())
                    .setType(
                            com.google.dataconnector.protocol.proto.SdcFrame.HealthCheckInfo.Type.RESPONSE)
                    .build();
            getFrameSender().sendFrame(Type.HEALTH_CHECK,
                    checkResponse.toByteString());
        } else {
            System.out.println("Unexpected frame:" + frameInfo);
        }
    }

    public Map<String, SdcClientCall> getCalls() {
        return calls;
    }

    public FrameReceiver getFrameReceiver() {
        return frameReceiver;
    }

    public FrameSender getFrameSender() {
        return frameSender;
    }

    public SdcClientHelper getHelper() {
        return helper;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getKey() {
        return key;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public SSLSocket getSocket() {
        return socket;
    }

    protected boolean readHandshake() throws IOException {
        boolean result = true;
        byte[] hsm = SdcConnection.INITIAL_HANDSHAKE_MSG.getBytes();
        int c;

        for (int i = 0; result && (i < hsm.length); i++) {
            c = getInputStream().read();
            result = (c == hsm[i]);
        }

        return result;
    }

    public void sendRequest(SdcClientCall call) {
        getCalls().put(call.getFetchRequest().getId(), call);
        getFrameSender().sendFrame(FrameInfo.Type.FETCH_REQUEST,
                call.getFetchRequest().toByteString());

    }

    public void setKey(String key) {
        this.key = key;
    }

}
