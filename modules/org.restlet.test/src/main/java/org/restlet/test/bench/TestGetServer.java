/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.test.bench;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.representation.FileRepresentation;

public class TestGetServer {

    public static void main(String[] args) throws Exception {
        ConnectorHelper<Server> helper;
        helper = new org.restlet.engine.connector.HttpServerHelper(null);
        Engine.getInstance().getRegisteredServers().add(0, helper);
        // [ifdef jse] instruction
        Engine.setLogLevel(Level.FINE);

        Server server = new Server(new Context(), Protocol.HTTP, 8554,
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        FileRepresentation fr = new FileRepresentation(
                                "file:///c:/RHDSetup.log", MediaType.TEXT_PLAIN);
                        System.out.println("Size sent: " + fr.getSize());
                        response.setEntity(fr);
                    }
                });

        server.start();
    }
}
