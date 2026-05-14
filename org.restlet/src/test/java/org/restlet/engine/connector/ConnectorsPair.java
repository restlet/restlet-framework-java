/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.lang.String.format;

public class ConnectorsPair {
    final BaseConnectorsTestCase.HttpServer httpServer;
    final BaseConnectorsTestCase.HttpClient httpClient;

    public ConnectorsPair(
            BaseConnectorsTestCase.HttpServer httpServer,
            BaseConnectorsTestCase.HttpClient httpClient) {
        this.httpServer = httpServer;
        this.httpClient = httpClient;
    }

    public String getTestLabel() {
        return format("%s server and %s client", httpServer.name(), httpClient.name());
    }
}
