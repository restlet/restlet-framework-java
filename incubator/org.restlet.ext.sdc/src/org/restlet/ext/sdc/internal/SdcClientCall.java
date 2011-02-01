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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.http.ClientCall;
import org.restlet.engine.io.NioUtils;
import org.restlet.ext.sdc.SdcClientHelper;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import com.google.dataconnector.protocol.proto.SdcFrame.FetchReply;
import com.google.dataconnector.protocol.proto.SdcFrame.FetchRequest;
import com.google.dataconnector.protocol.proto.SdcFrame.FrameInfo;
import com.google.dataconnector.protocol.proto.SdcFrame.MessageHeader;

/**
 * SDC client call wrapping a HTTP call. This call will be tunneled through the
 * matched SDC server connection previously established with a remote SDC agent.
 * 
 * @author Jerome Louvel
 */
public class SdcClientCall extends ClientCall {

    /** */
    private final CountDownLatch latch;

    /** The matching SDC server connection to use for tunnelling. */
    private final SdcServerConnection connection;

    /** Indicates if the response headers were added. */
    private volatile boolean responseHeadersAdded;

    /** */
    private volatile FetchRequest fetchRequest;

    /** */
    private volatile FetchReply fetchReply;

    /**
     * Constructor.
     * 
     * @param sdcClientHelper
     *            The parent HTTP client helper.
     * @param method
     *            The method name.
     * @param requestUri
     *            The request URI.
     * @param hasEntity
     *            Indicates if the call will have an entity to send to the
     *            server.
     * @throws IOException
     */
    public SdcClientCall(SdcClientHelper sdcClientHelper,
            SdcServerConnection connection, String method, String requestUri)
            throws IOException {
        super(sdcClientHelper, method, requestUri);
        this.connection = connection;
        this.latch = new CountDownLatch(1);
    }

    /**
     * Returns the connection.
     * 
     * @return The connection.
     */
    public SdcServerConnection getConnection() {
        return this.connection;
    }

    public FetchReply getFetchReply() {
        return fetchReply;
    }

    public FetchRequest getFetchRequest() {
        return fetchRequest;
    }

    /**
     * Returns the HTTP client helper.
     * 
     * @return The HTTP client helper.
     */
    @Override
    public SdcClientHelper getHelper() {
        return (SdcClientHelper) super.getHelper();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    /**
     * Returns the response reason phrase.
     * 
     * @return The response reason phrase.
     */
    @Override
    public String getReasonPhrase() {
        try {
            return Status.valueOf(getStatusCode()).getDescription();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected Representation getRepresentation(InputStream stream) {
        return null;
    }

    @Override
    public WritableByteChannel getRequestEntityChannel() {
        return null;
    }

    @Override
    public OutputStream getRequestEntityStream() {
        return getRequestStream();
    }

    @Override
    public OutputStream getRequestHeadStream() {
        return getRequestStream();
    }

    /**
     * Returns the request entity stream if it exists.
     * 
     * @return The request entity stream if it exists.
     */
    public OutputStream getRequestStream() {
        return null;
    }

    @Override
    public ReadableByteChannel getResponseEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getResponseEntityStream(long size) {
        return null;
    }

    /**
     * Returns the modifiable list of response headers.
     * 
     * @return The modifiable list of response headers.
     */
    @Override
    public Series<Parameter> getResponseHeaders() {
        Series<Parameter> result = super.getResponseHeaders();

        if (!this.responseHeadersAdded) {
            for (MessageHeader mh : getFetchReply().getHeadersList()) {
                result.add(mh.getKey(), mh.getValue());
            }

            this.responseHeadersAdded = true;
        }

        return result;
    }

    /**
     * Returns the response address.<br>
     * Corresponds to the IP address of the responding server.
     * 
     * @return The response address.
     */
    @Override
    public String getServerAddress() {
        return null;
    }

    /**
     * Returns the response status code.
     * 
     * @return The response status code.
     * @throws IOException
     * @throws IOException
     */
    @Override
    public int getStatusCode() throws IOException {
        return getFetchReply().getStatus();
    }

    /**
     * Sends the request to the client. Commits the request line, headers and
     * optional entity and send them over the network.
     * 
     * @param request
     *            The high-level request.
     * @return The result status.
     */
    @Override
    public Status sendRequest(Request request) {
        Status result = null;

        if (Protocol.HTTP.equals(request.getResourceRef().getSchemeProtocol())) {
            // Set the request URI
            setFetchRequest(FetchRequest.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setResource(request.getResourceRef().toString())
                    .setStrategy("URLConnection").build());

            // Set the request headers
            for (Parameter header : getRequestHeaders()) {
                getFetchRequest().getHeadersList().add(
                        MessageHeader.newBuilder().setKey(header.getName())
                                .setValue(header.getValue()).build());
            }
        } else {
            throw new IllegalArgumentException(
                    "Only HTTP or HTTPS resource URIs are allowed here");
        }

        try {
            getConnection().getFrameSender().sendFrame(
                    FrameInfo.Type.FETCH_REQUEST,
                    getFetchRequest().toByteString());

            // Block the thread until we receive the response or a timeout
            // occurs
            if (!getLatch().await(NioUtils.NIO_TIMEOUT, TimeUnit.MILLISECONDS)) {
                // Timeout detected
                result = new Status(Status.CONNECTOR_ERROR_INTERNAL,
                        "The calling thread timed out while waiting for a response to unblock it.");
            }
        } catch (Exception e) {
            getHelper()
                    .getLogger()
                    .log(Level.FINE,
                            "An unexpected error occurred during the sending of the HTTP request.",
                            e);
            result = new Status(Status.CONNECTOR_ERROR_INTERNAL, e);
        }

        return result;
    }

    @Override
    public void sendRequest(Request request, Response response, Uniform callback)
            throws Exception {
        // Send the request
        sendRequest(request);

        if (request.getOnSent() != null) {
            request.getOnSent().handle(request, response);
        }

        if (callback != null) {
            // Transmit to the callback, if any.
            callback.handle(request, response);
        }
    }

    public void setFetchReply(FetchReply fetchReply) {
        this.fetchReply = fetchReply;
    }

    public void setFetchRequest(FetchRequest fetchRequest) {
        this.fetchRequest = fetchRequest;
    }

}
