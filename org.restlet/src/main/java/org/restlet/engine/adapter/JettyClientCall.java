/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.eclipse.jetty.client.*;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpFields.Mutable;
import org.restlet.Uniform;
import org.restlet.data.Header;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.engine.header.HeaderConstants;
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
    private final Request request;

    /**
     * The wrapped HTTP response.
     */
    private volatile Response response;

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
     * @param helper     The parent HTTP client helper.
     * @param method     The method name.
     * @param requestUri The request URI.
     * @throws IOException
     */
    public JettyClientCall(HttpClientHelper helper, final String method,
            final String requestUri) throws IOException {
        super(helper, method, requestUri);
        this.clientHelper = helper;

        if (requestUri.startsWith("http")) {
            this.request = helper.getHttpClient().newRequest(requestUri);
            this.request.method(method);

            setConfidential(this.request.getURI().getScheme()
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
    public Request getRequest() {
        return this.request;
    }

    /**
     * Returns the HTTP response.
     * 
     * @return The HTTP response.
     */
    public Response getResponse() {
        return this.response;
    }

    /**
     * Returns the input stream response listener.
     * 
     * @return The input stream response listener.
     */
    public InputStreamResponseListener getInputStreamResponseListener() {
        return this.inputStreamResponseListener;
    }

    @Override
    public String getReasonPhrase() {
        final Response httpResponse = getResponse();
        return httpResponse == null ? null : httpResponse.getReason();
    }

    @Override
    public OutputStream getRequestEntityStream() {
        return null;
    }

    @Override
    public OutputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public InputStream getResponseEntityStream(long size) {
        final InputStreamResponseListener inputStreamResponseListener = getInputStreamResponseListener();
        return inputStreamResponseListener == null ? null
                : inputStreamResponseListener.getInputStream();
    }

    /**
     * Returns the response entity if available. Note that no metadata is
     * associated by default, you have to manually set them from your headers.
     * 
     * As a jetty client decodes the input stream on the fly, we have to clear the
     * {@link org.restlet.representation.Representation#getEncodings()} to avoid
     * decoding the input stream another time.
     * 
     * @param response the Response to get the entity from
     * @return The response entity if available.
     */
    @Override
    public Representation getResponseEntity(org.restlet.Response response) {
        Representation responseEntity = super.getResponseEntity(response);
        if (responseEntity != null
                && !responseEntity.getEncodings().isEmpty()) {
            responseEntity.getEncodings().clear();
            // Entity size is reset accordingly.
            responseEntity.setSize(Representation.UNKNOWN_SIZE);
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
            final Response httpResponse = getResponse();
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
        return this.request.getURI().getHost();
    }

    /**
     * Returns the response status code.
     * 
     * @return The response status code.
     */
    @Override
    public int getStatusCode() {
        return getResponse().getStatus();
    }

    /**
     * Sends the request to the client. Commits the request line, headers, and
     * optional entity and send them over the network.
     * 
     * @param request The high-level request.
     * @return The result status.
     */
    @Override
    public Status sendRequest(org.restlet.Request request) {
        Status result = null;

        try {
            final Representation entity = request.getEntity();

            // Request entity
            if (entity != null && entity.isAvailable())
                this.request.body(new InputStreamRequestContent(entity.getStream()));

            // Set the request headers
            for (Header header : getRequestHeaders()) {
                final String name = header.getName();
                switch (name) {
                case HeaderConstants.HEADER_CONTENT_LENGTH:
                    // skip this header
                    break;
                case HeaderConstants.HEADER_USER_AGENT:
                    this.request.agent(header.getValue());
                    break;
                default:
                    ((Mutable)this.request.getHeaders()).add(name, header.getValue());
                    break;
                }
            }

            // Ensure that the connection is active
            this.inputStreamResponseListener = new InputStreamResponseListener();
            this.request.send(this.inputStreamResponseListener);
            this.response = this.inputStreamResponseListener
                    .get(clientHelper.getIdleTimeout(), TimeUnit.MILLISECONDS);

            result = new Status(getStatusCode(), getReasonPhrase());
        } catch (IOException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "An error occurred while reading the request entity.", e);
            result = new Status(Status.CONNECTOR_ERROR_INTERNAL, e);

            // Release the connection
            getRequest().abort(e);
        } catch (TimeoutException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "The HTTP request timed out.", e);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, e);

            // Release the connection
            getRequest().abort(e);
        } catch (InterruptedException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "The HTTP request thread was interrupted.", e);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, e);

            // Release the connection
            getRequest().abort(e);
        } catch (ExecutionException e) {
            this.clientHelper.getLogger().log(Level.WARNING,
                    "An error occurred while processing the HTTP request.", e);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, e);

            // Release the connection
            getRequest().abort(e);
        }

        return result;
    }

    @Override
    public void sendRequest(org.restlet.Request request,
            org.restlet.Response response, Uniform callback) throws Exception {
        sendRequest(request);

        final Uniform getOnSent = request.getOnSent();
        if (getOnSent != null)
            getOnSent.handle(request, response);

        if (callback != null)
            // Transmit to the callback, if any
            callback.handle(request, response);
    }
}
