/**
 * Copyright 2005-2010 Noelios Technologies.
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

import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.util.Series;

/**
 * Client-side outbound way.
 * 
 * @author Jerome Louvel
 */
public class ClientOutboundWay extends OutboundWay {

    /**
     * Returns the absolute request URI.
     * 
     * @param resourceRef
     *            The resource reference.
     * @return The absolute request URI.
     */
    private static String getRequestUri(Reference resourceRef) {
        String result = null;
        Reference absoluteRef = resourceRef.isAbsolute() ? resourceRef
                : resourceRef.getTargetRef();
        if (absoluteRef.hasQuery()) {
            result = absoluteRef.getPath() + "?" + absoluteRef.getQuery();
        } else {
            result = absoluteRef.getPath();
        }

        if ((result == null) || (result.equals(""))) {
            result = "/";
        }

        return result;
    }

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public ClientOutboundWay(Connection<?> connection) {
        super(connection);
    }

    @Override
    protected void addHeaders(Series<Parameter> headers) {
        Request request = getMessage().getRequest();
        addGeneralHeaders(headers);
        addRequestHeaders(headers);
        addEntityHeaders(request.getEntity(), headers);
    }

    /**
     * Adds the request headers.
     * 
     * @param headers
     *            The headers series to update.
     */
    protected void addRequestHeaders(Series<Parameter> headers) {
        HeaderUtils.addRequestHeaders(getMessage().getRequest(), headers);
    }

    @Override
    protected void onCompleted() {
        Request request = getMessage().getRequest();

        if (request.getOnSent() != null) {
            request.getOnSent().handle(request, getMessage());
        }

        // The request has been written
        getMessages().remove(getMessage());

        if (request.isExpectingResponse()) {
            getConnection().getInboundWay().getMessages().add(getMessage());
        }

        super.onCompleted();
    }

    @Override
    protected void writeStartLine() throws IOException {
        Request request = getMessage().getRequest();
        getLineBuilder().append(request.getMethod().getName());
        getLineBuilder().append(' ');
        getLineBuilder().append(getRequestUri(request.getResourceRef()));
        getLineBuilder().append(' ');
        getLineBuilder().append(getVersion(request));
        getLineBuilder().append("\r\n");
    }
}
