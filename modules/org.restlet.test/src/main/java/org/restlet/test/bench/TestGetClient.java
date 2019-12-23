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

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class TestGetClient {

    public static void main(String[] args) throws IOException {
        ConnectorHelper<Client> helper;
        helper = new org.restlet.engine.connector.HttpClientHelper(null);
        Engine.getInstance().getRegisteredClients().add(0, helper);
        // [ifdef jse] instruction
        Engine.setLogLevel(Level.FINE);
        long startTime = System.currentTimeMillis();

        ClientResource resource = new ClientResource("http://localhost:8554/");
        try {
            Representation entity = resource.get();
            System.out.println("Status: " + resource.getStatus());

            long expectedSize = entity.getSize();
            long receivedSize = entity.exhaust();

            System.out.println("Size expected: " + expectedSize);
            System.out.println("Size consumed: " + receivedSize);

            if ((expectedSize != -1) && (expectedSize != receivedSize)) {
                System.out.println("ERROR: SOME BYTES WERE LOST!");
            }
        } catch (ResourceException e) {
            System.out.println("Status: " + resource.getStatus());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Duration: " + (endTime - startTime) + " ms");
    }

}
