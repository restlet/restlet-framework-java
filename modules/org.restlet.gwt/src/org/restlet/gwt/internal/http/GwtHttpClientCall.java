/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.internal.http;

import org.restlet.gwt.Callback;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Reference;
import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.util.Series;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

/**
 * HTTP client connector call based on GWT's HTTP module.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class GwtHttpClientCall extends HttpClientCall {

    /** The wrapped HTTP request builder. */
    private final RequestBuilder requestBuilder;

    /** The GWT response. */
    private volatile Response response;

    /** Indicates if the response headers were added. */
    private volatile boolean responseHeadersAdded;

    /**
     * Special status code set when an error occurs and we don't have a Response
     * object.
     */
    private int errorStatusCode;

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
     */
    public GwtHttpClientCall(GwtHttpClientHelper helper, String method,
            String requestUri, boolean hasEntity) {
        super(helper, method, requestUri);

        if (requestUri.startsWith("http")) {
            this.requestBuilder = new RequestBuilder(method, requestUri) {
            };
            this.requestBuilder.setTimeoutMillis(getHelper().getTimeout());
            this.responseHeadersAdded = false;
        } else {
            throw new IllegalArgumentException(
                    "Only HTTP or HTTPS resource URIs are allowed here");
        }
    }

    /**
     * Returns the HTTP client helper.
     * 
     * @return The HTTP client helper.
     */
    @Override
    public GwtHttpClientHelper getHelper() {
        return (GwtHttpClientHelper) super.getHelper();
    }

    /**
     * Returns the response reason phrase.
     * 
     * @return The response reason phrase.
     */
    @Override
    public String getReasonPhrase() {
        return (getResponse() == null) ? null : getResponse().getStatusText();
    }

    /**
     * Returns the GWT request builder.
     * 
     * @return The GWT request builder.
     */
    public RequestBuilder getRequestBuilder() {
        return this.requestBuilder;
    }

    @Override
    public String getRequestEntityString() {
        return getRequestBuilder().getRequestData();
    }

    /**
     * Returns the GWT response.
     * 
     * @return The GWT response.
     */
    public Response getResponse() {
        return this.response;
    }

    @Override
    public String getResponseEntityString(long size) {
        return (getResponse() == null) ? null : getResponse().getText();
    }

    /**
     * Returns the modifiable list of response headers.
     * 
     * @return The modifiable list of response headers.
     */
    @Override
    public Series<Parameter> getResponseHeaders() {
        final Series<Parameter> result = super.getResponseHeaders();

        if (!this.responseHeadersAdded && (getResponse() != null)) {
            Header[] headers = getResponse().getHeaders();

            for (int i = 0; i < headers.length; i++) {
                if (headers[i] != null) {
                    result.add(headers[i].getName(), headers[i].getValue());
                }
            }

            this.responseHeadersAdded = true;
        }

        return result;
    }

    @Override
    public String getServerAddress() {
        return new Reference(getRequestBuilder().getUrl()).getHostIdentifier();
    }

    @Override
    public int getStatusCode() {
        return (getResponse() == null) ? this.errorStatusCode : getResponse()
                .getStatusCode();
    }

    @Override
    public void sendRequest(final org.restlet.gwt.data.Request request,
            final org.restlet.gwt.data.Response response,
            final Callback callback) throws Exception {
        final Representation entity = request.isEntityAvailable() ? request
                .getEntity() : null;
        if (entity != null) {
            getRequestBuilder().setRequestData(entity.getText());
        }

        // Set the request headers
        for (final Parameter header : getRequestHeaders()) {
            getRequestBuilder().setHeader(header.getName(),
                    getRequestHeaders().getValues(header.getName()));
        }

        // Set the current call as the callback handler
        getRequestBuilder().setCallback(new RequestCallback() {

            public void onError(com.google.gwt.http.client.Request gwtRequest,
                    Throwable exception) {
                callback.onEvent(request, response);
            }

            public void onResponseReceived(
                    com.google.gwt.http.client.Request gwtRequest,
                    Response gwtResponse) {
                setResponse(gwtResponse);
                callback.onEvent(request, response);
            }

        });

        // Send the request
        getRequestBuilder().send();
    }

    /**
     * Sets the GWT response.
     * 
     * @param response
     *            The GWT response.
     */
    public void setResponse(Response response) {
        this.response = response;
    }
}
