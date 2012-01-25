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

package org.restlet.engine.connector;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.util.Series;

/**
 * Client-side outbound way.
 * 
 * @author Jerome Louvel
 */
public abstract class ClientOutboundWay extends OutboundWay {

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
    public Request getActualMessage() {
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
                ReferenceUtils.format(request.getResourceRef(), getHelper()
                        .isProxying(), request));
        getLineBuilder().append(' ');
        getLineBuilder().append(getVersion(request));
        getLineBuilder().append("\r\n");
    }

}
