/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty.resource;

import java.util.logging.Level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.jetty.HttpClientHelper;
import org.restlet.ext.jetty.HttpServerHelper;
import org.restlet.representation.ObjectRepresentation;

/**
 * All test cases relying on a client and a server should inherit from this
 * class.
 * 
 * @author Jerome Louvel
 */
public abstract class JettyConnectorTestCase {

    private Component c;

    private Client client;

    private String uri;

    public JettyConnectorTestCase() {
        super();
    }

    protected abstract Application createApplication(final String path);

    protected Request createRequest(Method method) {
        return new Request(method, getUri());
    }

    public Component getC() {
        return c;
    }

    public Client getClient() {
        return client;
    }

    public String getUri() {
        return uri;
    }

    protected Response handle(Request request) {
        return getClient().handle(request);
    }

    @BeforeEach
    void init() throws Exception {
        Engine.clearThreadLocalVariables();

        // Restore a clean engine
        org.restlet.engine.Engine.register(false);
        Engine.setLogLevel(Level.INFO);
        Engine.getInstance().registerDefaultConverters();
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));

        int serverPort = setUpServer(0, "/test");
        setUpClient(serverPort, "/test");

        // Enable object serialization
        ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED = true;
        ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = true;
    }

    protected void releaseResponse(Response response) {
        response.getEntity().release();
    }

    protected void setUpClient(int serverPort, String path) {
        this.client = new Client(Protocol.HTTP);
        this.uri = "http://localhost:" + serverPort + path;
    }

    protected int setUpServer(int suggestedPort, String path) throws Exception {
        c = new Component();
        final Server server = c.getServers().add(Protocol.HTTP, suggestedPort);
        c.getDefaultHost().attach(createApplication(path));
        c.start();
        return server.getActualPort();
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
        tearDownClient();
        tearDownServer();
    }

    protected void tearDownClient() throws Exception {
        client.stop();
        client = null;
    }

    protected void tearDownServer() throws Exception {
        c.stop();
        c = null;
    }

}