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

package org.restlet.test.component;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the configuration of a component with an XML file.
 * 
 * @author Thierry Boileau
 */
public class ComponentXmlTestCase extends RestletTestCase {

    private final int port = TEST_PORT;

    private final int port2 = port + 1;

    public void testComponentXMLConfig() throws Exception {
        AppendableRepresentation config = new AppendableRepresentation();
        config.append("<?xml version=\"1.0\"?>");
        config.append("<component>");
        config
                .append("<server protocol=\"HTTP\" port=\"" + this.port
                        + "\" />");
        config.append("<server protocol=\"HTTP\" port=\"" + this.port2
                + "\" />");
        config.append("<defaultHost hostPort=\"" + this.port2 + "\">");
        config
                .append("<attach uriPattern=\"/abcd\" targetClass=\"org.restlet.test.component.HelloWorldApplication\" /> ");
        config.append("</defaultHost>");
        config.append("<host hostPort=\"" + this.port + "\">");
        config
                .append("<attach uriPattern=\"/efgh\" targetClass=\"org.restlet.test.component.HelloWorldApplication\" /> ");
        config.append("</host>");

        config.append("</component>");

        final Component component = new Component(config);
        component.start();

        final Client client = new Client(Protocol.HTTP);

        Response response = client.handle(new Request(Method.GET,
                "http://localhost:" + this.port + "/efgh"));
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.handle(new Request(Method.GET, "http://localhost:"
                + this.port + "/abcd"));
        assertTrue(response.getStatus().isClientError());

        response = client.handle(new Request(Method.GET, "http://localhost:"
                + this.port2 + "/abcd"));
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.handle(new Request(Method.GET, "http://localhost:"
                + this.port2 + "/efgh"));
        assertTrue(response.getStatus().isClientError());

        component.stop();
        client.stop();
    }
}
