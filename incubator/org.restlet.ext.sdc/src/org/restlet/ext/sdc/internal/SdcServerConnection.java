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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLSocket;

import org.restlet.ext.sdc.SdcClientHelper;

import com.google.dataconnector.client.SdcConnection;
import com.google.dataconnector.protocol.FrameReceiver;
import com.google.dataconnector.protocol.FrameSender;
import com.google.dataconnector.protocol.FramingException;
import com.google.dataconnector.protocol.proto.SdcFrame;
import com.google.dataconnector.protocol.proto.SdcFrame.AuthorizationInfo;
import com.google.dataconnector.protocol.proto.SdcFrame.AuthorizationInfo.ResultCode;
import com.google.dataconnector.protocol.proto.SdcFrame.FrameInfo;
import com.google.dataconnector.protocol.proto.SdcFrame.RegistrationRequestV4;
import com.google.dataconnector.protocol.proto.SdcFrame.RegistrationResponseV4;
import com.google.dataconnector.util.ShutdownManager;

/**
 * The SDC server connection established between this SDC client connector,
 * acting as the tunnel server and a remote SDC agent.
 * 
 * @author Jerome Louvel
 */
public class SdcServerConnection {

    private volatile String key;

    private final SSLSocket socket;

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private final FrameReceiver frameReceiver;

    private final FrameSender frameSender;

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

        BlockingQueue<FrameInfo> sendQueue = new LinkedBlockingQueue<SdcFrame.FrameInfo>();
        ShutdownManager shutdownManager = new ShutdownManager();
        this.frameSender = new FrameSender(sendQueue, shutdownManager);
        this.frameSender.setOutputStream(getOutputStream());

        getHelper().getWorkerService().execute(new Runnable() {

            @Override
            public void run() {
                getFrameSender().run();
            }
        });
    }

    public void connect() throws IOException {
        readHandshake();

        try {
            // 1) Authorization step
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
            } else {
                System.out.println(frameInfo);
            }

            // 2) Registration step
            frameInfo = getFrameReceiver().readOneFrame();

            if (frameInfo.getType() == FrameInfo.Type.REGISTRATION) {
                RegistrationRequestV4 registrationRequest = RegistrationRequestV4
                        .parseFrom(frameInfo.getPayload());
                System.out.println(registrationRequest);

                RegistrationResponseV4 registrationResponse = RegistrationResponseV4
                        .newBuilder()
                        .setResult(
                                com.google.dataconnector.protocol.proto.SdcFrame.RegistrationResponseV4.ResultCode.OK)
                        .build();

                getFrameSender().sendFrame(FrameInfo.Type.REGISTRATION,
                        registrationResponse.toByteString());
            }
        } catch (FramingException e) {
            e.printStackTrace();
        }
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

    public void setKey(String key) {
        this.key = key;
    }

}
