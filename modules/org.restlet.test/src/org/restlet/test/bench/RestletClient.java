package org.restlet.test.bench;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class RestletClient {

    /**
     * @param args
     * @throws IOException
     * @throws ResourceException
     */
    public static void main(String[] args) throws ResourceException,
            IOException {
        ConnectorHelper<Client> helper;
        helper = new org.restlet.engine.connector.HttpClientHelper(null);
        Engine.getInstance().getRegisteredClients().add(0, helper);

        // helper = new org.restlet.ext.httpclient.HttpClientHelper(null);
        helper = new org.restlet.ext.jetty.HttpClientHelper(null);
        // helper = new org.restlet.ext.nio.HttpsClientHelper(null);
        Engine.getInstance().getRegisteredClients().add(0, helper);
        // [ifdef jse] instruction
        Engine.setLogLevel(Level.FINE);

        for (int i = 0; i < 1; i++) {
            ClientResource cr = new ClientResource("http://restlet.org"
            // "https://www.amazon.com/gp/css/homepage.html"
            // "https://github.com/restlet/restlet-framework-java"
            );
            cr.get().write(System.out);
        }
    }
}
