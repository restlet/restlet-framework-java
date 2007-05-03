/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Logger;

import org.restlet.Server;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.ReadableRepresentation;
import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;

/**
 * Abstract HTTP server connector call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class HttpServerCall extends HttpCall {
    /** The logger to use. */
    private Logger logger;

    /** Indicates if the "host" header was already parsed. */
    private boolean hostParsed;

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server connector.
     */
    public HttpServerCall(Server server) {
        setLogger(server.getLogger());
        setServerAddress(server.getAddress());
        setServerPort(server.getPort());
        this.hostParsed = false;
    }

    /**
     * Returns the request entity channel if it exists.
     * 
     * @return The request entity channel if it exists.
     */
    public abstract ReadableByteChannel getRequestChannel();

    /**
     * Returns the request entity stream if it exists.
     * 
     * @return The request entity stream if it exists.
     */
    public abstract InputStream getRequestStream();

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public abstract WritableByteChannel getResponseChannel();

    /**
     * Returns the response stream if it exists.
     * 
     * @return The response stream if it exists.
     */
    public abstract OutputStream getResponseStream();

    /**
     * Returns the request entity if available.
     * 
     * @return The request entity if available.
     */
    public Representation getRequestEntity() {
        Representation result = null;
        InputStream requestStream = getRequestStream();
        ReadableByteChannel requestChannel = getRequestChannel();

        if (((requestStream != null) || (requestChannel != null))) {
            // Extract the header values
            Encoding contentEncoding = null;
            Language contentLanguage = null;
            MediaType contentType = null;
            long contentLength = -1L;

            for (Parameter header : getRequestHeaders()) {
                if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_ENCODING)) {
                    contentEncoding = new Encoding(header.getValue());
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_LANGUAGE)) {
                    contentLanguage = new Language(header.getValue());
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_TYPE)) {
                    contentType = new MediaType(header.getValue());
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_LENGTH)) {
                    contentLength = Long.parseLong(header.getValue());
                }
            }

            if (requestStream != null) {
                result = new InputRepresentation(requestStream, contentType,
                        contentLength);
            } else if (requestChannel != null) {
                result = new ReadableRepresentation(requestChannel,
                        contentType, contentLength);
            }

            if (result != null) {
                result.setEncoding(contentEncoding);
                result.setLanguage(contentLanguage);
            }
        }

        return result;
    }

    /**
     * Returns the host domain name.
     * 
     * @return The host domain name.
     */
    public String getHostDomain() {
        if (!hostParsed)
            parseHost();
        return super.getHostDomain();
    }

    /**
     * Returns the host port.
     * 
     * @return The host port.
     */
    public Integer getHostPort() {
        if (!hostParsed)
            parseHost();
        return super.getHostPort();
    }

    /**
     * Parses the "host" header to set the server host and port properties.
     */
    private void parseHost() {
        String host = getRequestHeaders().getFirstValue(
                HttpConstants.HEADER_HOST, true);

        if (host != null) {
            int colonIndex = host.indexOf(':');

            if (colonIndex != -1) {
                super.setHostDomain(host.substring(0, colonIndex));
                super.setHostPort(Integer.valueOf(host
                        .substring(colonIndex + 1)));
            } else {
                super.setHostDomain(host);

                if (isConfidential()) {
                    super.setServerPort(Protocol.HTTPS.getDefaultPort());
                } else {
                    super.setServerPort(Protocol.HTTP.getDefaultPort());
                }
            }
        } else {
            this.logger
                    .warning("Couldn't find the mandatory \"Host\" HTTP header.");
        }

        this.hostParsed = true;
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them over the network. The default
     * implementation only writes the response entity on the reponse stream or
     * channel. Subclasses will probably also copy the response headers and
     * status.
     * 
     * @param response
     *            The high-level response.
     */
    public void sendResponse(Response response) throws IOException {
        if (response != null) {
            Representation entity = response.getEntity();

            if ((entity != null)
                    && !response.getRequest().getMethod().equals(Method.HEAD)
                    && !response.getStatus().equals(Status.SUCCESS_NO_CONTENT)
                    && !response.getStatus().equals(
                            Status.SUCCESS_RESET_CONTENT)) {
                // Get the connector service to callback
                ConnectorService connectorService = getConnectorService(response
                        .getRequest());
                if (connectorService != null)
                    connectorService.beforeSend(entity);

                // Send the entity to the client
                if (getResponseChannel() != null) {
                    entity.write(getResponseChannel());
                } else if (getResponseStream() != null) {
                    entity.write(getResponseStream());
                }

                if (connectorService != null)
                    connectorService.afterSend(entity);
            }

            if (getResponseStream() != null) {
                getResponseStream().flush();
            }
        }
    }

    /**
     * Returns the logger to use.
     * 
     * @return The logger to use.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Sets the logger to use.
     * 
     * @param logger
     *            The logger to use.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
