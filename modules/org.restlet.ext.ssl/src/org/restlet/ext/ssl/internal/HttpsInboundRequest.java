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

package org.restlet.ext.ssl.internal;

import java.security.cert.Certificate;
import java.util.List;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.HttpInboundRequest;
import org.restlet.engine.header.HeaderConstants;

/**
 * Request wrapper for server HTTPS calls.
 * 
 * @author Jerome Louvel
 */
public class HttpsInboundRequest extends HttpInboundRequest {

    /**
     * Constructor.
     * 
     * @param context
     *            The context of the parent connector.
     * @param connection
     *            The associated network connection.
     * @param methodName
     *            The protocol method name.
     * @param resourceUri
     *            The target resource URI.
     * @param protocol
     *            The protocol name and version.
     */
    @SuppressWarnings("deprecation")
    public HttpsInboundRequest(Context context, Connection<Server> connection,
            String methodName, String resourceUri, String protocol) {
        super(context, connection, methodName, resourceUri, protocol);

        // Set the SSL certificates
        List<Certificate> clientCertificates = getConnection()
                .getSslClientCertificates();
        if (clientCertificates != null) {
            getAttributes().put(
                    HeaderConstants.ATTRIBUTE_HTTPS_CLIENT_CERTIFICATES,
                    clientCertificates);

            // TODO: To be moved in a ClientInfo subclass
            getClientInfo().setCertificates(clientCertificates);
        }

        String cipherSuite = getConnection().getSslCipherSuite();
        if (cipherSuite != null) {
            getAttributes().put(HeaderConstants.ATTRIBUTE_HTTPS_CIPHER_SUITE,
                    cipherSuite);

            // TODO: To be moved in a ClientInfo subclass
            getClientInfo().setCipherSuite(cipherSuite);
        }

        Integer keySize = getConnection().getSslKeySize();
        if (keySize != null) {
            getAttributes().put(HeaderConstants.ATTRIBUTE_HTTPS_KEY_SIZE,
                    keySize);
        }
    }

    @Override
    public SslConnection<Server> getConnection() {
        return (SslConnection<Server>) super.getConnection();
    }

}
