/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

/**
 * Unit test case for the configuration of a component with an XML file.
 * 
 * @author Thierry Boileau
 */
public class ComponentXmlTestCase extends TestCase {

    private final int port = 8182;

    private final int port2 = 8183;

    public void testComponentXMLConfig() throws Exception {
        final StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\"?>");
        builder.append("<component>");
        builder.append("<server protocol=\"HTTP\" port=\"" + this.port
                + "\" />");
        builder.append("<server protocol=\"HTTP\" port=\"" + this.port2
                + "\" />");
        builder.append("<defaultHost hostPort=\"" + this.port2 + "\">");
        builder
                .append("<attach uriPattern=\"/abcd\" targetClass=\"org.restlet.test.HelloWorldApplication\" /> ");
        builder.append("</defaultHost>");
        builder.append("<host hostPort=\"" + this.port + "\">");
        builder
                .append("<attach uriPattern=\"/efgh\" targetClass=\"org.restlet.test.HelloWorldApplication\" /> ");
        builder.append("</host>");

        builder.append("</component>");

        final Component component = new Component(new StringRepresentation(
                builder.toString()));
        component.start();

        final Client client = new Client(Protocol.HTTP);

        Response response = client.get("http://localhost:" + this.port
                + "/efgh");
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.get("http://localhost:" + this.port + "/abcd");
        assertTrue(response.getStatus().isClientError());

        response = client.get("http://localhost:" + this.port2 + "/abcd");
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.get("http://localhost:" + this.port2 + "/efgh");
        assertTrue(response.getStatus().isClientError());

        component.stop();
    }

}
