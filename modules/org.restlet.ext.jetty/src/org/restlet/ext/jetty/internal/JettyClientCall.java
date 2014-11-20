/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jetty.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Header;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.jetty.HttpClientHelper;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * HTTP client connector call based on Jetty's HttpRequest class.
 * 
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class JettyClientCall extends ClientCall {

    /**
     * The associated HTTP client.
     */
    private final HttpClientHelper clientHelper;

    /**
     * The wrapped HTTP request.
     */
    private final HttpRequest httpRequest;

    /**
     * The wrapped HTTP response.
     */
    private volatile org.eclipse.jetty.client.api.Response httpResponse;

    /**
     * The wrapped input stream response listener.
     */
    private volatile InputStreamResponseListener inputStreamResponseListener;

    /**
     * Indicates if the response headers were added.
     */
    private volatile boolean responseHeadersAdded;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent HTTP client helper.
     * @param method
     *            The method name.
     * @param requestUri
     *            The request URI.
     * @throws IOException
     */
    public JettyClientCall(HttpClientHelper helper, final String method,
            final String requestUri) throws IOException {
        super(helper, method, requestUri);
        this.clientHelper = helper;

        if (requestUri.startsWith("http")) {
            this.httpRequest = (HttpRequest) helper.getHttpClient().newRequest(
                    requestUri);
            this.httpRequest.method(method);

            setConfidential(this.httpRequest.getURI().getScheme()
                    .equalsIgnoreCase(Protocol.HTTPS.getSchemeName()));
        } else {
            throw new IllegalArgumentException(
                    "Only HTTP or HTTPS resource URIs are allowed here");
        }
    }

    /**
     * Returns the HTTP request.
     * 
     * @return The HTTP request.
     */
    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    /**
     * Returns the HTTP response.
     * 
     * @return The HTTP response.
     */
    public org.eclipse.jetty.client.api.Response getHttpResponse() {
        return this.httpResponse;
    }

    /**
     * Returns the input stream response listener.
     * 
     * @return The input stream response listener.
     */
    public InputStreamResponseListener getInputStreamResponseListener() {
        return this.inputStreamResponseListener;
    }

    /**
     * Returns the response reason phrase.
     * 
     * @return The response reason phrase.
     */
    @Override
    public String getReasonPhrase() {
        final org.eclipse.jetty.client.api.Response httpResponse = getHttpResponse();
        return httpResponse == null ? null : httpResponse.getReason();
    }

    public WritableByteChannel getRequestEntityChannel() {
        return null;
    }

    public OutputStream getRequestEntityStream() {
        return null;
    }

    public OutputStream getRequestHeadStream() {
        return null;
    }

    public ReadableByteChannel getResponseEntityChannel(long size) {
        return null;
    }

    public InputStream getResponseEntityStream(long size) {
        final InputStreamResponseListener inputStreamResponseListener = getInputStreamResponseListener();
        return inputStreamResponseListener == null ? null
                : inputStreamResponseListener.getInputStream();
    }

    /**
     * Returns the response entity if available. Note that no metadata is
     * associated by default, you have to manually set them from your headers.
     *
     * As jetty client decode the input stream on the fly in
     * {@link org.eclipse.jetty.client.HttpReceiver#responseContent(org.eclipse.jetty.client.HttpExchange, java.nio.ByteBuffer, org.eclipse.jetty.util.Callback)}
     * we have to clear the {@link org.restlet.representation.Representation#getEncodings()}
     * to avoid decoding the input stream another time.

     * @param response
     *            the Response to get the entity from
     * @return The response entity if available.
     */
    @Override
    public Representation getResponseEntity(Response response) {
        Representation responseEntity = super.getResponseEntity(response);
        if (responseEntity != null && !responseEntity.getEncodings().isEmpty()) {
            responseEntity.getEncodings().clear();
        }
        return responseEntity;
    }

    /**
     * Returns the modifiable list of response headers.
     * 
     * @return The modifiable list of response headers.
     */
    @Override
    public Series<Header> getResponseHeaders() {
        final Series<Header> result = super.getResponseHeaders();

        if (!this.responseHeadersAdded) {
            final org.eclipse.jetty.client.api.Response httpResponse = getHttpResponse();
            if (httpResponse != null) {
                final HttpFields headers = httpResponse.getHeaders();
                if (headers != null) {
                    for (HttpField header : headers)
                        result.add(header.getName(), header.getValue());
                }
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
        return this.httpRequest.getURI().getHost();
    }

    /**
     * Returns the response status code.
     * 
     * @return The response status code.
     */
    @Override
    public int getStatusCode() {
        final org.eclipse.jetty.client.api.Response httpResponse = getHttpResponse();
        return httpResponse == null ? null : httpResponse.getStatus();
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

        try {
            final Representation entity = request.getEntity();

            // Request entity
            if (entity != null && entity.isAvailable())
                this.httpRequest.content(new InputStreamContentProvider(entity
                        .getStream()));

            // Set the request headers
            for (Header header : getRequestHeaders()) {
                final String name = header.getName();
                if (!name.equals(HeaderConstants.HEADER_CONTENT_LENGTH))
                    this.httpRequest.header(name, header.getValue());
            }

            // Ensure that the connection is active
            this.inputStreamResponseListener = new InputStreamResponseListener();
            this.httpRequest.send(this.inputStreamResponseListener);
            this.httpResponse = this.inputStreamResponseListener
                    .get(clientHelper.getTimeout(), TimeUnit.MILLISECONDS);

            result = new Status(getStatusCode(), getReasonPhrase());
        } catch (IOException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "An error occurred while reading the request entity.", e);
            result = new Status(Status.CONNECTOR_ERROR_INTERNAL, e);

            // Release the connection
            getHttpRequest().abort(e);
        } catch (TimeoutException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "The HTTP request timed out.", e);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, e);

            // Release the connection
            getHttpRequest().abort(e);
        } catch (InterruptedException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "The HTTP request thread was interrupted.", e);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, e);

            // Release the connection
            getHttpRequest().abort(e);
        } catch (ExecutionException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "An error occurred while processing the HTTP request.", e);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, e);

            // Release the connection
            getHttpRequest().abort(e);
        }

        return result;
    }

    @Override
    public void sendRequest(Request request, Response response, Uniform callback)
            throws Exception {
        sendRequest(request);

        final Uniform getOnSent = request.getOnSent();
        if (getOnSent != null)
            getOnSent.handle(request, response);

        if (callback != null)
            // Transmit to the callback, if any
            callback.handle(request, response);
    }
}
