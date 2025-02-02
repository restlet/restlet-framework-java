package org.restlet.ext.jetty.connectors;

import static java.lang.String.format;

public class ConnectorTestCase {
    final BaseConnectorsTestCase.HttpServer httpServer;
    final BaseConnectorsTestCase.HttpClient httpClient;

    public ConnectorTestCase(BaseConnectorsTestCase.HttpServer httpServer, BaseConnectorsTestCase.HttpClient httpClient) {
        this.httpServer = httpServer;
        this.httpClient = httpClient;
    }

    public String getTestLabel() {
        return format("%s server and %s client", httpServer.name(), httpClient.name());
    }
}
