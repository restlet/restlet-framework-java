/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.adapter;

import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_HEADERS;
import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_HTTPS_KEY_SIZE;
import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_HTTPS_SSL_SESSION_ID;
import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_VERSION;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.List;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Converter of low-level HTTP server calls into high-level uniform calls.
 *
 * @author Jerome Louvel
 */
public class ServerAdapter extends Adapter {

    /**
     * Constructor.
     *
     * @param context The client context.
     */
    public ServerAdapter(Context context) {
        super(context);
    }

    /**
     * Adds the entity headers for the handled uniform call.
     *
     * @param response The response returned.
     */
    protected void addEntityHeaders(HttpResponse response) {
        Series<Header> responseHeaders = response.getHttpCall().getResponseHeaders();
        Representation entity = response.getEntity();
        HeaderUtils.addEntityHeaders(entity, responseHeaders);
    }

    /**
     * Adds the response headers for the handled uniform call.
     *
     * @param response The response returned.
     */
    protected void addResponseHeaders(HttpResponse response) {
        try {
            // Add all the necessary headers
            HeaderUtils.addGeneralHeaders(response, response.getHttpCall().getResponseHeaders());
            HeaderUtils.addResponseHeaders(response, response.getHttpCall().getResponseHeaders());

            // Set the status code in the response
            if (response.getStatus() != null) {
                response.getHttpCall().setStatusCode(response.getStatus().getCode());
                response.getHttpCall().setReasonPhrase(response.getStatus().getReasonPhrase());
            }
        } catch (Exception e) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Exception intercepted while adding the response headers",
                            e);
            response.getHttpCall().setStatusCode(Status.SERVER_ERROR_INTERNAL.getCode());
            response.getHttpCall().setReasonPhrase(Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
        }
    }

    /**
     * Commits the changes to a handled uniform call back into the original HTTP call. The default
     * implementation first invokes the "addResponseHeaders" then asks the "htppCall" to send the
     * response back to the client.
     *
     * @param response The high-level response.
     */
    public void commit(HttpResponse response) {
        try {
            if (Method.HEAD.equals(response.getRequest().getMethod())) {
                addEntityHeaders(response);
                response.setEntity(null);
            } else if (Method.GET.equals(response.getRequest().getMethod())
                    && Status.SUCCESS_OK.equals(response.getStatus())
                    && (!response.isEntityAvailable())) {
                addEntityHeaders(response);
                getLogger()
                        .warning(
                                "A response with a 200 (Ok) status should have an entity. Make sure that resource \""
                                        + response.getRequest().getResourceRef()
                                        + "\" returns one or sets the status to 204 (No content).");
            } else if (response.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
                handleNoContentResponse(response);
            } else if (response.getStatus().equals(Status.SUCCESS_RESET_CONTENT)) {
                handleResetContent(response);
            } else if (response.getStatus().equals(Status.REDIRECTION_NOT_MODIFIED)) {
                handleRedirectionNotNodified(response);
            } else if (response.getStatus().isInformational()) {
                handleInformationalStatus(response);
            } else {
                addEntityHeaders(response);

                if (!response.isEntityAvailable()) {
                    if ((response.getEntity() != null) && (response.getEntity().getSize() != 0)) {
                        getLogger()
                                .warning(
                                        "A response with an unavailable and potentially non empty entity was returned. Ignoring the entity for resource \""
                                                + response.getRequest().getResourceRef()
                                                + "\".");
                    }

                    response.setEntity(null);
                }
            }

            // Add the response headers
            addResponseHeaders(response);

            // Send the response to the client
            response.getHttpCall().sendResponse(response);
        } catch (Exception exception) {
            handleException(response, exception);
        } finally {
            completeResponse(response);
        }
    }

    private static void completeResponse(final HttpResponse response) {
        response.getHttpCall().complete();

        if (response.getOnSent() != null) {
            response.getOnSent().handle(response.getRequest(), response);
        }
    }

    private void handleInformationalStatus(final HttpResponse response) {
        if (response.isEntityAvailable()) {
            getLogger()
                    .warning(
                            "Responses with an informational (1xx) status can't have an entity. Ignoring the entity for resource \""
                                    + response.getRequest().getResourceRef()
                                    + "\".");
            response.setEntity(null);
        }
    }

    private static void handleRedirectionNotNodified(final HttpResponse response) {
        if (response.getEntity() != null) {
            HeaderUtils.addNotModifiedEntityHeaders(
                    response.getEntity(), response.getHttpCall().getResponseHeaders());
            response.setEntity(null);
        }
    }

    private void handleResetContent(final HttpResponse response) {
        if (response.isEntityAvailable()) {
            getLogger()
                    .warning(
                            "Responses with a 205 (Reset content) status can't have an entity. Ignoring the entity for resource \""
                                    + response.getRequest().getResourceRef()
                                    + "\".");
            response.setEntity(null);
        }
    }

    private void handleNoContentResponse(final HttpResponse response) {
        addEntityHeaders(response);

        if (response.isEntityAvailable()) {
            getLogger()
                    .fine(
                            "Responses with a 204 (No content) status generally don't have an entity. Only adding entity headers for resource \""
                                    + response.getRequest().getResourceRef()
                                    + "\".");
            response.setEntity(null);
        }
    }

    private void handleException(final HttpResponse response, final Exception exception) {
        if (response.getHttpCall().isConnectionBroken(exception)) {
            // output a single log line for this common case to avoid filling server logs
            getLogger()
                    .log(
                            Level.INFO,
                            "The connection was broken. It was probably closed by the client. Reason: {0}",
                            exception.getMessage());
        } else {
            getLogger()
                    .log(
                            Level.SEVERE,
                            "An exception occurred writing the response entity",
                            exception);
            response.getHttpCall().setStatusCode(Status.SERVER_ERROR_INTERNAL.getCode());
            response.getHttpCall()
                    .setReasonPhrase("An exception occurred writing the response entity");
            response.setEntity(null);

            try {
                response.getHttpCall().sendResponse(response);
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING, "Unable to send error response", ioe);
            }
        }
    }

    /**
     * Converts a low-level HTTP call into a high-level uniform request.
     *
     * @param httpCall The low-level HTTP call.
     * @return A new high-level uniform request.
     */
    public HttpRequest toRequest(ServerCall httpCall) {
        HttpRequest result = new HttpRequest(getContext(), httpCall);
        result.getAttributes().put(ATTRIBUTE_HEADERS, httpCall.getRequestHeaders());

        if (httpCall.getVersion() != null) {
            result.getAttributes().put(ATTRIBUTE_VERSION, httpCall.getVersion());
        }

        if (httpCall.isConfidential()) {
            List<Certificate> clientCertificates = httpCall.getCertificates();

            if (clientCertificates != null) {
                result.getClientInfo().setCertificates(clientCertificates);
            }

            String cipherSuite = httpCall.getCipherSuite();

            if (cipherSuite != null) {
                result.getClientInfo().setCipherSuite(cipherSuite);
            }

            Integer keySize = httpCall.getSslKeySize();

            if (keySize != null) {
                result.getAttributes().put(ATTRIBUTE_HTTPS_KEY_SIZE, keySize);
            }

            String sslSessionId = httpCall.getSslSessionId();

            if (sslSessionId != null) {
                result.getAttributes().put(ATTRIBUTE_HTTPS_SSL_SESSION_ID, sslSessionId);
            }
        }

        return result;
    }
}
