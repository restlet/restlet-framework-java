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

package org.restlet.ext.httpclient.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * HTTP client connector call based on Apache HTTP Client's HttpMethod class.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to favor lower-level network extensions allowing
 *             more control at the Restlet API level.
 */
@Deprecated
public class HttpMethodCall extends ClientCall {

    /** The associated HTTP client. */
    private volatile HttpClientHelper clientHelper;

    /** The wrapped HTTP request. */
    private volatile HttpUriRequest httpRequest;

    /** The wrapped HTTP response. */
    private volatile HttpResponse httpResponse;

    /** Indicates if the response headers were added. */
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
     * @param hasEntity
     *            Indicates if the call will have an entity to send to the
     *            server.
     * @throws IOException
     */
    public HttpMethodCall(HttpClientHelper helper, final String method,
            final String requestUri, boolean hasEntity) throws IOException {
        super(helper, method, requestUri);
        this.clientHelper = helper;

        if (requestUri.startsWith("http")) {
            if (method.equalsIgnoreCase(Method.GET.getName())) {
                this.httpRequest = new HttpGet(requestUri);
            } else if (method.equalsIgnoreCase(Method.POST.getName())) {
                this.httpRequest = new HttpPost(requestUri);
            } else if (method.equalsIgnoreCase(Method.PUT.getName())) {
                this.httpRequest = new HttpPut(requestUri);
            } else if (method.equalsIgnoreCase(Method.HEAD.getName())) {
                this.httpRequest = new HttpHead(requestUri);
            } else if (method.equalsIgnoreCase(Method.DELETE.getName())) {
                this.httpRequest = new HttpDelete(requestUri);
                if (hasEntity) {
                    getLogger()
                            .warning(
                                    "The current DELETE request provides an entity that may be not supported by the Apache HTTP Client library. If you face such issues, you can still move to another HTTP client connector.");
                }
            } else if (method.equalsIgnoreCase(Method.OPTIONS.getName())) {
                this.httpRequest = new HttpOptions(requestUri);
            } else if (method.equalsIgnoreCase(Method.TRACE.getName())) {
                this.httpRequest = new HttpTrace(requestUri);
            } else {
                this.httpRequest = new HttpEntityEnclosingRequestBase() {

                    @Override
                    public String getMethod() {
                        return method;
                    }

                    @Override
                    public URI getURI() {
                        try {
                            return new URI(requestUri);
                        } catch (URISyntaxException e) {
                            getLogger().log(Level.WARNING,
                                    "Invalid URI syntax", e);
                            return null;
                        }
                    }
                };
            }

            this.responseHeadersAdded = false;
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
    public HttpUriRequest getHttpRequest() {
        return this.httpRequest;
    }

    /**
     * Returns the HTTP response.
     * 
     * @return The HTTP response.
     */
    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    /**
     * Returns the response reason phrase.
     * 
     * @return The response reason phrase.
     */
    @Override
    public String getReasonPhrase() {
        if ((getHttpResponse() != null)
                && (getHttpResponse().getStatusLine() != null)) {
            return getHttpResponse().getStatusLine().getReasonPhrase();
        }
        return null;
    }

    @Override
    public WritableByteChannel getRequestEntityChannel() {
        return null;
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
    public ReadableByteChannel getResponseEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getResponseEntityStream(long size) {
        InputStream result = null;

        try {
            // Return a wrapper filter that will release the connection when
            // needed
            InputStream responseStream = (getHttpResponse() == null) ? null
                    : (getHttpResponse().getEntity() == null) ? null
                            : getHttpResponse().getEntity().getContent();
            if (responseStream != null) {
                result = new FilterInputStream(responseStream) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        getHttpResponse().getEntity().consumeContent();
                    }
                };
            }
        } catch (IOException ioe) {
        }

        return result;
    }

    /**
     * Returns the modifiable list of response headers.
     * 
     * @return The modifiable list of response headers.
     */
    @Override
    public Series<org.restlet.data.Header> getResponseHeaders() {
        Series<org.restlet.data.Header> result = super.getResponseHeaders();

        if (!this.responseHeadersAdded) {
            if ((getHttpResponse() != null)
                    && (getHttpResponse().getAllHeaders() != null)) {
                for (Header header : getHttpResponse().getAllHeaders()) {
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
        return getHttpRequest().getURI().getHost();
    }

    /**
     * Returns the response status code.
     * 
     * @return The response status code.
     */
    @Override
    public int getStatusCode() {
        if (getHttpResponse() != null
                && getHttpResponse().getStatusLine() != null) {
            return getHttpResponse().getStatusLine().getStatusCode();
        }
        return Status.CONNECTOR_ERROR_COMMUNICATION.getCode();
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

            // Set the request headers
            for (org.restlet.data.Header header : getRequestHeaders()) {
                if (!header.getName().equals(
                        HeaderConstants.HEADER_CONTENT_LENGTH)) {
                    getHttpRequest().addHeader(header.getName(),
                            header.getValue());
                }
            }

            // For those method that accept enclosing entities, provide it
            if ((entity != null)
                    && (getHttpRequest() instanceof HttpEntityEnclosingRequestBase)) {
                final HttpEntityEnclosingRequestBase eem = (HttpEntityEnclosingRequestBase) getHttpRequest();
                eem.setEntity(new AbstractHttpEntity() {
                    public InputStream getContent() throws IOException,
                            IllegalStateException {
                        return entity.getStream();
                    }

                    public long getContentLength() {
                        return entity.getSize();
                    }

                    public Header getContentType() {
                        return new BasicHeader(
                                HeaderConstants.HEADER_CONTENT_TYPE, (entity
                                        .getMediaType() != null) ? entity
                                        .getMediaType().toString() : null);
                    }

                    public boolean isRepeatable() {
                        return !entity.isTransient();
                    }

                    public boolean isStreaming() {
                        return (entity.getSize() == Representation.UNKNOWN_SIZE);
                    }

                    public void writeTo(OutputStream os) throws IOException {
                        entity.write(os);
                        os.flush();
                    }
                });
            }

            // Ensure that the connection is active
            this.httpResponse = this.clientHelper.getHttpClient().execute(
                    getHttpRequest());

            // Now we can access the status code, this MUST happen after closing
            // any open request stream.
            result = new Status(getStatusCode(), getReasonPhrase());
        } catch (IOException ioe) {
            this.clientHelper
                    .getLogger()
                    .log(Level.WARNING,
                            "An error occurred during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, ioe);

            // Release the connection
            getHttpRequest().abort();
        }

        return result;
    }

    @Override
    public void sendRequest(Request request, Response response, Uniform callback)
            throws Exception {
        sendRequest(request);

        if (request.getOnSent() != null) {
            request.getOnSent().handle(request, response);
        }

        if (callback != null) {
            // Transmit to the callback, if any.
            callback.handle(request, response);
        }
    }
}
