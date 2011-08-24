/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.connector;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.util.Series;

/**
 * Client-side outbound way.
 * 
 * @author Jerome Louvel
 */
public abstract class ClientOutboundWay extends OutboundWay {

    /**
     * Returns the request URI.
     * 
     * @param resourceRef
     *            The resource reference.
     * @param isProxied
     *            Indicates if the request goes through a proxy and requires an
     *            absolute URI.
     * @return The absolute request URI.
     */
    private static String getRequestUri(Reference resourceRef, boolean isProxied) {
        String result = null;
        Reference requestRef = resourceRef.isAbsolute() ? resourceRef
                : resourceRef.getTargetRef();

        if (isProxied) {
            result = requestRef.getIdentifier();
        } else {
            if (requestRef.hasQuery()) {
                result = requestRef.getPath() + "?" + requestRef.getQuery();
            } else {
                result = requestRef.getPath();
            }

            if ((result == null) || (result.equals(""))) {
                result = "/";
            }
        }

        return result;
    }

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public ClientOutboundWay(Connection<?> connection, int bufferSize) {
        super(connection, bufferSize);
    }

    @Override
    protected void addHeaders(Series<Header> headers) {
        Request request = getMessage().getRequest();
        addGeneralHeaders(headers);
        addEntityHeaders(request.getEntity(), headers);

        // NOTE: This must stay at the end because the AWS challenge
        // scheme requires access to all HTTP headers
        addRequestHeaders(headers);
    }

    /**
     * Adds the request headers.
     * 
     * @param headers
     *            The headers series to update.
     */
    protected void addRequestHeaders(Series<Header> headers) {
        HeaderUtils.addRequestHeaders(getMessage().getRequest(), headers);
    }

    @Override
    public Message getActualMessage() {
        return (getMessage() == null) ? null : getMessage().getRequest();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Connection<Client> getConnection() {
        return (Connection<Client>) super.getConnection();
    }

    @Override
    public void onCompleted(boolean endDetected) {
        Response message = getMessage();

        if (message != null) {
            Request request = message.getRequest();

            if (request.getOnSent() != null) {
                request.getOnSent().handle(request, message);
            }
        }

        super.onCompleted(endDetected);
    }

    @Override
    protected void writeStartLine() throws IOException {
        Request request = getMessage().getRequest();
        getLineBuilder().append(request.getMethod().getName());
        getLineBuilder().append(' ');
        getLineBuilder().append(
                getRequestUri(request.getResourceRef(), getHelper()
                        .isProxying()));
        getLineBuilder().append(' ');
        getLineBuilder().append(getVersion(request));
        getLineBuilder().append("\r\n");
    }
}
