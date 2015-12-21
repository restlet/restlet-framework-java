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

package org.restlet.engine.adapter;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Edition;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.util.Series;

/**
 * Converter of high-level uniform calls into low-level HTTP client calls.
 * 
 * @author Jerome Louvel
 */
public class ClientAdapter extends Adapter {

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use.
     */
    public ClientAdapter(Context context) {
        super(context);
    }

    /**
     * Commits the changes to a handled HTTP client call back into the original
     * uniform call. The default implementation first invokes the
     * "addResponseHeaders" then asks the "htppCall" to send the response back
     * to the client.
     * 
     * @param httpCall
     *            The original HTTP call.
     * @param request
     *            The high-level request.
     * @param response
     *            The high-level response.
     * @throws Exception
     */
    public void commit(final ClientCall httpCall, Request request,
            Response response) throws Exception {
        if (httpCall != null) {
            // Check if the call is asynchronous
            if (request.isAsynchronous()) {
                final Uniform userCallback = request.getOnResponse();

                // Send the request to the client
                httpCall.sendRequest(request, response, new Uniform() {
                    public void handle(Request request, Response response) {
                        try {
                            updateResponse(response,
                                    new Status(httpCall.getStatusCode(),
                                            httpCall.getReasonPhrase()),
                                    httpCall);

                            if (userCallback != null) {
                                userCallback.handle(request, response);
                            }
                        } catch (Throwable t) {
                            getLogger()
                                    .log(Level.WARNING,
                                            "Unexpected error or exception inside the user call back",
                                            t);
                        }
                    }
                });
            } else {
                if (Edition.CURRENT == Edition.GWT) {
                    System.err
                            .println("HTTP client calls must have a callback in the GWT edition");
                } else {
                    // [ifndef gwt]
                    updateResponse(response, httpCall.sendRequest(request),
                            httpCall);
                    // [enddef]
                }
            }
        }
    }

    /**
     * Reads the response headers of a handled HTTP client call to update the
     * original uniform call.
     * 
     * @param httpCall
     *            The handled HTTP client call.
     * @param response
     *            The high-level response to update.
     */
    protected void readResponseHeaders(ClientCall httpCall, Response response) {
        try {
            Series<Header> responseHeaders = httpCall.getResponseHeaders();

            // Put the response headers in the call's attributes map
            response.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                    responseHeaders);

            HeaderUtils.copyResponseTransportHeaders(responseHeaders, response);
        } catch (Exception e) {
            getLogger()
                    .log(Level.FINE,
                            "An error occurred during the processing of the HTTP response.",
                            e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        }
    }

    /**
     * Converts a low-level HTTP call into a high-level uniform call.
     * 
     * @param client
     *            The HTTP client that will handle the call.
     * @param request
     *            The high-level request.
     * @return A new high-level uniform call.
     */
    public ClientCall toSpecific(HttpClientHelper client, Request request) {
        // Create the low-level HTTP client call
        ClientCall result = client.create(request);

        // Add the headers
        if (result != null) {
            HeaderUtils.addGeneralHeaders(request, result.getRequestHeaders());

            if (request.getEntity() != null) {
                HeaderUtils.addEntityHeaders(request.getEntity(),
                        result.getRequestHeaders());
            }

            // NOTE: This must stay at the end because the AWS challenge
            // scheme requires access to all HTTP headers
            HeaderUtils.addRequestHeaders(request, result.getRequestHeaders());
        }

        return result;
    }

    /**
     * Updates the response with information from the lower-level HTTP client
     * call.
     * 
     * @param response
     *            The response to update.
     * @param status
     *            The response status to apply.
     * @param httpCall
     *            The source HTTP client call.
     * @throws IOException
     */
    public void updateResponse(Response response, Status status,
            ClientCall httpCall) {
        // Send the request to the client
        response.setStatus(status);

        // Get the server address
        response.getServerInfo().setAddress(httpCall.getServerAddress());
        response.getServerInfo().setPort(httpCall.getServerPort());

        // Read the response headers
        readResponseHeaders(httpCall, response);

        // Set the entity
        response.setEntity(httpCall.getResponseEntity(response));

        // Release the representation's content for some obvious cases
        if (response.getEntity() != null) {
            if (response.getEntity().isEmpty()) {
                response.getEntity().release();
            } else if (response.getRequest().getMethod().equals(Method.HEAD)) {
                response.getEntity().release();
            } else if (response.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
                response.getEntity().release();
            } else if (response.getStatus()
                    .equals(Status.SUCCESS_RESET_CONTENT)) {
                response.getEntity().release();
                response.setEntity(null);
            } else if (response.getStatus().equals(
                    Status.REDIRECTION_NOT_MODIFIED)) {
                response.getEntity().release();
            } else if (response.getStatus().isInformational()) {
                response.getEntity().release();
                response.setEntity(null);
            }
        }
    }
}
