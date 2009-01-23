/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.ext.httpclient;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.http.HttpClientCall;
import org.restlet.resource.Representation;
import org.restlet.util.Series;


/**
 * HTTP client connector call based on Apache HTTP Client's HttpMethod class.
 * 
 * @author Jerome Louvel
 */
public class HttpMethodCall extends HttpClientCall {

    /** The associated HTTP client. */
    private volatile HttpClientHelper clientHelper;

    /** The wrapped HTTP method. */
    private volatile HttpMethod httpMethod;

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
            String requestUri, boolean hasEntity) throws IOException {
        super(helper, method, requestUri);
        this.clientHelper = helper;

        if (requestUri.startsWith("http")) {
            if (method.equalsIgnoreCase(Method.GET.getName())) {
                this.httpMethod = new GetMethod(requestUri);
            } else if (method.equalsIgnoreCase(Method.POST.getName())) {
                this.httpMethod = new PostMethod(requestUri);
            } else if (method.equalsIgnoreCase(Method.PUT.getName())) {
                this.httpMethod = new PutMethod(requestUri);
            } else if (method.equalsIgnoreCase(Method.HEAD.getName())) {
                this.httpMethod = new HeadMethod(requestUri);
            } else if (method.equalsIgnoreCase(Method.DELETE.getName())) {
                this.httpMethod = new DeleteMethod(requestUri);
            } else if (method.equalsIgnoreCase(Method.CONNECT.getName())) {
                final HostConfiguration host = new HostConfiguration();
                host.setHost(new URI(requestUri, false));
                this.httpMethod = new ConnectMethod(host);
            } else if (method.equalsIgnoreCase(Method.OPTIONS.getName())) {
                this.httpMethod = new OptionsMethod(requestUri);
            } else if (method.equalsIgnoreCase(Method.TRACE.getName())) {
                this.httpMethod = new TraceMethod(requestUri);
            } else {
                this.httpMethod = new EntityEnclosingMethod(requestUri) {
                    @Override
                    public String getName() {
                        return method;
                    }
                };
            }

            this.httpMethod.setFollowRedirects(this.clientHelper
                    .isFollowRedirects());
            this.httpMethod.setDoAuthentication(false);

            if (this.clientHelper.getRetryHandler() != null) {
                try {
                    this.httpMethod.getParams().setParameter(
                            HttpMethodParams.RETRY_HANDLER,
                            Engine.loadClass(
                                    this.clientHelper.getRetryHandler())
                                    .newInstance());
                } catch (Exception e) {
                    this.clientHelper
                            .getLogger()
                            .log(
                                    Level.WARNING,
                                    "An error occurred during the instantiation of the retry handler.",
                                    e);
                }
            }

            this.responseHeadersAdded = false;
            setConfidential(this.httpMethod.getURI().getScheme()
                    .equalsIgnoreCase(Protocol.HTTPS.getSchemeName()));
        } else {
            throw new IllegalArgumentException(
                    "Only HTTP or HTTPS resource URIs are allowed here");
        }
    }

    /**
     * Returns the HTTP method.
     * 
     * @return The HTTP method.
     */
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * Returns the response reason phrase.
     * 
     * @return The response reason phrase.
     */
    @Override
    public String getReasonPhrase() {
        return getHttpMethod().getStatusText();
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
            final InputStream responseBodyAsStream = getHttpMethod()
                    .getResponseBodyAsStream();
            if (responseBodyAsStream != null) {
                result = new FilterInputStream(responseBodyAsStream) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        getHttpMethod().releaseConnection();
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
    public Series<Parameter> getResponseHeaders() {
        final Series<Parameter> result = super.getResponseHeaders();

        if (!this.responseHeadersAdded) {
            for (final Header header : getHttpMethod().getResponseHeaders()) {
                result.add(header.getName(), header.getValue());
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
        try {
            return getHttpMethod().getURI().getHost();
        } catch (URIException e) {
            return null;
        }
    }

    /**
     * Returns the response status code.
     * 
     * @return The response status code.
     */
    @Override
    public int getStatusCode() {
        return getHttpMethod().getStatusCode();
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
            for (final Parameter header : getRequestHeaders()) {
                getHttpMethod().addRequestHeader(header.getName(),
                        header.getValue());
            }

            // For those method that accept enclosing entites, provide it
            if ((entity != null)
                    && (getHttpMethod() instanceof EntityEnclosingMethod)) {
                final EntityEnclosingMethod eem = (EntityEnclosingMethod) getHttpMethod();
                eem.setRequestEntity(new RequestEntity() {
                    public long getContentLength() {
                        return entity.getSize();
                    }

                    public String getContentType() {
                        return (entity.getMediaType() != null) ? entity
                                .getMediaType().toString() : null;
                    }

                    public boolean isRepeatable() {
                        return !entity.isTransient();
                    }

                    public void writeRequest(OutputStream os)
                            throws IOException {
                        entity.write(os);
                    }
                });
            }

            // Ensure that the connection is active
            this.clientHelper.getHttpClient().executeMethod(getHttpMethod());

            // Now we can access the status code, this MUST happen after closing
            // any open request stream.
            result = new Status(getStatusCode(), null, getReasonPhrase(), null);

            // If there is no response body, immediately release the connection
            if (getHttpMethod().getResponseBodyAsStream() == null) {
                getHttpMethod().releaseConnection();
            }
        } catch (IOException ioe) {
            this.clientHelper
                    .getLogger()
                    .log(
                            Level.WARNING,
                            "An error occurred during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, ioe);

            // Release the connection
            getHttpMethod().releaseConnection();
        }

        return result;
    }
}
