/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.nio.internal.way;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.connection.SslConnection;

/**
 * HTTPS client inbound way.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to favor lower-level network extensions allowing more control at the Restlet API level.
 */
@Deprecated
public class HttpsClientInboundWay extends HttpClientInboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public HttpsClientInboundWay(Connection<Client> connection, int bufferSize) {
        super(connection, bufferSize);
    }

    @Override
    public SslConnection<Client> getConnection() {
        return (SslConnection<Client>) super.getConnection();
    }

    @Override
    public void postProcess(int drained) throws IOException {
        getConnection().handleSslResult();
    }

}
