/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.engine.netty.NettyClientHelper;

/**
 * Netty HTTP and HTTPS client connector. See {@link NettyClientHelper} for the list of supported
 * parameters.
 *
 * @author Jerome Louvel
 */
public class HttpClientHelper extends NettyClientHelper {

    /**
     * Constructor.
     *
     * @param client The client connector to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
    }
}
