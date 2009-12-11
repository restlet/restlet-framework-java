/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.http;

import java.io.IOException;
import java.net.Socket;

import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.util.Series;

/**
 * An internal HTTP server connection.
 * 
 * @author Jerome Louvel
 */
public class InternalServerConnection extends Connection<Server> {

    public InternalServerConnection(ConnectorHelper<Server> helper,
            Socket socket) throws IOException {
        super(helper, socket);
    }

    public synchronized InternalRequest readNext() throws IOException {
        InternalRequest result = null;
        String requestMethod = null;
        String requestUri = null;
        String httpVersion = null;
        Series<Parameter> requestHeaders = null;

        setInboundBusy(true);

        StringBuilder sb = new StringBuilder();

        // Parse the request method
        int next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request method. End of stream reached too early.");
        }

        requestMethod = sb.toString();
        sb.delete(0, sb.length());

        // Parse the request URI
        next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the request URI. End of stream reached too early.");
        }

        requestUri = sb.toString();
        sb.delete(0, sb.length());

        // Parse the HTTP version
        next = getInboundStream().read();
        while ((next != -1) && !HeaderUtils.isCarriageReturn(next)) {
            sb.append((char) next);
            next = getInboundStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the HTTP version. End of stream reached too early.");
        }
        next = getInboundStream().read();

        if (HeaderUtils.isLineFeed(next)) {
            httpVersion = sb.toString();
            sb.delete(0, sb.length());

            // Parse the headers
            Parameter header = HeaderUtils.readHeader(getInboundStream(), sb);
            while (header != null) {
                if (requestHeaders == null) {
                    requestHeaders = new Form();
                }

                requestHeaders.add(header);
                header = HeaderUtils.readHeader(getInboundStream(), sb);
            }
        } else {
            throw new IOException(
                    "Unable to parse the HTTP version. The carriage return must be followed by a line feed.");
        }

        // Create the HTTP request
        result = new InternalRequest(getHelper().getContext(), this,
                requestMethod, requestUri, httpVersion, requestHeaders, null,
                false, null);

        return result;
    }

}
