/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.sdc.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.io.IoUtils;
import org.restlet.ext.sdc.SdcClientHelper;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import com.google.dataconnector.protocol.proto.SdcFrame;
import com.google.dataconnector.protocol.proto.SdcFrame.FetchReply;
import com.google.dataconnector.protocol.proto.SdcFrame.FetchRequest;
import com.google.dataconnector.protocol.proto.SdcFrame.MessageHeader;
import com.google.protobuf.ByteString;

/**
 * SDC client call wrapping a HTTP call. This call will be tunneled through the
 * matched SDC server connection previously established with a remote SDC agent.
 * 
 * @author Jerome Louvel
 */
public class SdcClientCall extends ClientCall {

    /** The matching SDC server connection to use for tunneling. */
    private final SdcServerConnection connection;

    /** The SDC HTTP response. */
    private volatile FetchReply fetchReply;

    /** The SDC HTTP request. */
    private volatile FetchRequest fetchRequest;

    /** Efficiently blocks the calling thread while the call is tunneled. */
    private final CountDownLatch latch;

    /** The request entity stream convertible to a byte array. */
    private final ByteArrayOutputStream requestEntityStream;

    /** Indicates if the response headers were added. */
    private volatile boolean responseHeadersAdded;

    /**
     * Constructor.
     * 
     * @param sdcClientHelper
     *            The parent HTTP client helper.
     * @param connection
     *            The associated SDC tunnel connection.
     * @param method
     *            The method name.
     * @param requestUri
     *            The request URI.
     * @throws IOException
     */
    public SdcClientCall(SdcClientHelper sdcClientHelper,
            SdcServerConnection connection, String method, String requestUri)
            throws IOException {
        super(sdcClientHelper, method, requestUri);
        this.connection = connection;
        this.latch = new CountDownLatch(1);
        this.requestEntityStream = new ByteArrayOutputStream();
    }

    /**
     * Returns the connection.
     * 
     * @return The connection.
     */
    public SdcServerConnection getConnection() {
        return this.connection;
    }

    /**
     * Returns the SDC HTTP response.
     * 
     * @return The SDC HTTP response.
     */
    public FetchReply getFetchReply() {
        return fetchReply;
    }

    /**
     * Returns the SDC HTTP request.
     * 
     * @return The SDC HTTP request.
     */
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

    /**
     * Returns the latch that efficiently blocks the calling thread while the
     * call is tunneled.
     * 
     * @return The latch that efficiently blocks the calling thread while the
     *         call is tunneled.
     */
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
    public WritableByteChannel getRequestEntityChannel() {
        return null;
    }

    /**
     * Returns the request entity stream convertible to a byte array.
     * 
     * @return The request entity stream convertible to a byte array.
     */
    @Override
    public OutputStream getRequestEntityStream() {
        return requestEntityStream;
    }

    @Override
    public OutputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public ReadableByteChannel getResponseEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getResponseEntityStream(long size) {
        return getFetchReply().getContents().newInput();
    }

    /**
     * Returns the modifiable list of response headers.
     * 
     * @return The modifiable list of response headers.
     */
    @Override
    public Series<Header> getResponseHeaders() {
        Series<Header> result = super.getResponseHeaders();

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
        Representation entity = request.isEntityAvailable() ? request
                .getEntity() : null;

        // Get the connector service to callback
        org.restlet.service.ConnectorService connectorService = ConnectorHelper
                .getConnectorService();
        if (connectorService != null) {
            connectorService.beforeSend(entity);
        }

        try {
            try {
                // Set the request headers
                List<MessageHeader> headers = new CopyOnWriteArrayList<SdcFrame.MessageHeader>();

                for (Header header : getRequestHeaders()) {
                    if (!header.getName().equals(
                            HeaderConstants.HEADER_CONTENT_LENGTH)
                            && !header.getName().equals(
                                    HeaderConstants.HEADER_PROXY_AUTHORIZATION)) {
                        headers.add(MessageHeader.newBuilder()
                                .setKey(header.getName())
                                .setValue(header.getValue()).build());
                    }
                }

                if (!Method.GET.equals(request.getMethod())) {
                    headers.add(MessageHeader.newBuilder()
                            .setKey("x-sdc-http-method")
                            .setValue(request.getMethod().getName()).build());
                }

                if (entity != null) {
                    OutputStream requestStream = getRequestEntityStream();

                    if (requestStream != null) {
                        entity.write(requestStream);
                        requestStream.flush();
                        requestStream.close();

                        // Build the fetch request
                        setFetchRequest(FetchRequest
                                .newBuilder()
                                .setId(UUID.randomUUID().toString())
                                .setResource(
                                        request.getResourceRef().toString())
                                .setStrategy("HTTPClient")
                                .addAllHeaders(headers)
                                .setContents(
                                        ByteString
                                                .copyFrom(this.requestEntityStream
                                                        .toByteArray()))
                                .build());
                    }
                } else {
                    // Build the fetch request
                    setFetchRequest(FetchRequest.newBuilder()
                            .setId(UUID.randomUUID().toString())
                            .setResource(request.getResourceRef().toString())
                            .setStrategy("HTTPClient").addAllHeaders(headers)
                            .build());
                }

                getConnection().sendRequest(this);

                // Block the thread until we receive the response or a
                // timeout occurs
                if (!getLatch()
                        .await(IoUtils.TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    // Timeout detected
                    result = new Status(Status.CONNECTOR_ERROR_INTERNAL,
                            "The calling thread timed out while waiting for a response to unblock it.");
                } else {
                    result = Status.valueOf(getFetchReply().getStatus());
                }
            } catch (Exception e) {
                getHelper()
                        .getLogger()
                        .log(Level.FINE,
                                "An unexpected error occurred during the sending of the HTTP request.",
                                e);
                result = new Status(Status.CONNECTOR_ERROR_INTERNAL, e);
            }

            // Now we can access the status code, this MUST happen after closing
            // any open request stream.
            result = new Status(getStatusCode(), null, getReasonPhrase(), null);
        } catch (IOException ioe) {
            getHelper()
                    .getLogger()
                    .log(Level.FINE,
                            "An error occured during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, ioe);
        } finally {
            if (entity != null) {
                entity.release();
            }

            // Call-back after writing
            if (connectorService != null) {
                connectorService.afterSend(entity);
            }
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

    /**
     * Sets the SDC HTTP response.
     * 
     * @param fetchReply
     *            The SDC HTTP response.
     */
    public void setFetchReply(FetchReply fetchReply) {
        this.fetchReply = fetchReply;
    }

    /**
     * Sets the SDC HTTP request.
     * 
     * @param fetchRequest
     *            The SDC HTTP request.
     */
    public void setFetchRequest(FetchRequest fetchRequest) {
        this.fetchRequest = fetchRequest;
    }

}
