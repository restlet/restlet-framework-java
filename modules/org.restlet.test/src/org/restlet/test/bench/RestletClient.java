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
