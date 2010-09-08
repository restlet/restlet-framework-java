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

package org.restlet.engine.nio;

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.util.Series;

/**
 * Server-side outbound way.
 * 
 * @author Jerome Louvel
 */
public class ServerOutboundWay extends OutboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public ServerOutboundWay(Connection<Server> connection) {
        super(connection);
    }

    @Override
    protected void addHeaders(Series<Parameter> headers) {
        Response response = getMessage();
        Request request = response.getRequest();

        // Initial verifications
        if (Status.SUCCESS_RESET_CONTENT.equals(response.getStatus())
                && response.isEntityAvailable()) {
            getLogger()
                    .warning(
                            "Responses with a 205 (Reset content) status can't have an entity. Ignoring the entity for resource \""
                                    + request.getResourceRef() + "\".");
            response.setEntity(null);
        } else if (Status.REDIRECTION_NOT_MODIFIED.equals(response.getStatus())
                && (request.getEntity() != null)) {
            HeaderUtils.addNotModifiedEntityHeaders(response.getEntity(),
                    headers);
            response.setEntity(null);
        } else if (response.getStatus().isInformational()
                && response.isEntityAvailable()) {
            getLogger()
                    .warning(
                            "Responses with an informational (1xx) status can't have an entity. Ignoring the entity for resource \""
                                    + request.getResourceRef() + "\".");
            response.setEntity(null);

        }

        // Effectively add the headers
        addGeneralHeaders(headers);
        addResponseHeaders(headers);
        addEntityHeaders(response.getEntity(), headers);

        // Additional verifications
        if (!response.isEntityAvailable()) {
            if ((response.getEntity() != null)
                    && response.getEntity().getSize() != 0) {
                getLogger()
                        .warning(
                                "A response with an unavailable and potentially non empty entity was returned. Ignoring the entity for resource \""
                                        + response.getRequest()
                                                .getResourceRef() + "\".");
            }

            response.setEntity(null);
        }

        if (Method.GET.equals(request.getMethod())
                && Status.SUCCESS_OK.equals(response.getStatus())
                && (!response.isEntityAvailable())) {
            getLogger()
                    .warning(
                            "A response with a 200 (Ok) status should have an entity. Make sure that resource \""
                                    + request.getResourceRef()
                                    + "\" returns one or set the status to 204 (No content).");
        } else if (Status.SUCCESS_NO_CONTENT.equals(response.getStatus())
                && response.isEntityAvailable()) {
            getLogger()
                    .fine("Responses with a 204 (No content) status generally don't have an entity. Only adding entity headers for resource \""
                            + request.getResourceRef() + "\".");
            response.setEntity(null);
        }

        if (Method.HEAD.equals(request.getMethod())) {
            response.setEntity(null);
        }
    }

    /**
     * Adds the response headers.
     * 
     * @param headers
     *            The headers series to update.
     */
    protected void addResponseHeaders(Series<Parameter> headers) {
        HeaderUtils.addResponseHeaders(getMessage(), headers);
    }

    @Override
    protected void onCompleted() {
        getMessages().remove(getMessage());

        if (!getMessage().getStatus().isInformational()) {
            // Attempt to read additional inbound messages
            getConnection().getInboundWay().getMessages().remove(getMessage());
        }

        if (!getConnection().isPersistent()) {
            getConnection().close(true);
        }

        super.onCompleted();
    }

    @Override
    protected void writeStartLine() throws IOException {
        getLineBuilder().append(getVersion(getMessage().getRequest()));
        getLineBuilder().append(' ');
        getLineBuilder().append(getMessage().getStatus().getCode());
        getLineBuilder().append(' ');

        if (getMessage().getStatus().getName() != null) {
            getLineBuilder().append(getMessage().getStatus().getName());
        } else {
            getLineBuilder().append(
                    "Status " + getMessage().getStatus().getCode());
        }

        getLineBuilder().append("\r\n");
    }

}
