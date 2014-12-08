/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.resource;

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.test.RestletTestCase;

/**
 * All test cases relying on a client and a server should inherit from this
 * class.
 * 
 * @author Jerome Louvel
 */
public abstract class InternalConnectorTestCase extends RestletTestCase {

    private Component c;

    private Client client;

    private String uri;

    public InternalConnectorTestCase() {
        super();
    }

    protected abstract Application createApplication(final String path);

    protected Request createRequest(Method method) {
        Request request = new Request(method, getUri());
        return request;
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
        Response response;
        response = getClient().handle(request);
        return response;
    }

    public void initClient() throws Exception {
        setUpEngine();
        setUpCommon();
        setUpClient(8888, "/test");
    }

    public void initServer() throws Exception {
        setUpEngine();
        setUpCommon();
        setUpServer(8888, "/test");
    }

    protected void releaseResponse(Response response) {
        response.getEntity().release();
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpCommon();
        int serverPort = setUpServer(0, "/test");
        setUpClient(serverPort, "/test");
    }

    protected void setUpClient(int serverPort, String path) throws Exception {
        this.client = new Client(Protocol.HTTP);
        this.uri = "http://localhost:" + serverPort + path;
        this.client = new Client(Protocol.HTTP);
    }

    protected void setUpCommon() throws Exception {
        Engine.setLogLevel(Level.INFO);
        Engine.getInstance().getRegisteredConverters().clear();
        Engine.getInstance().registerDefaultConverters();
    }

    protected int setUpServer(int suggestedPort, String path) throws Exception {
        c = new Component();
        final Server server = c.getServers().add(Protocol.HTTP, suggestedPort);
        c.getDefaultHost().attach(createApplication(path));
        c.start();
        return server.getActualPort();
    }

    @Override
    protected void tearDown() throws Exception {
        tearDownClient();
        tearDownServer();
        super.tearDown();
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