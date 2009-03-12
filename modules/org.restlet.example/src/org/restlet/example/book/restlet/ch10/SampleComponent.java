/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch10;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.DomRepresentation;
import org.restlet.representation.SaxRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.NodeSet;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Creates a component.
 */
public class SampleComponent {

    public static void main(String[] args) throws Exception {
        final Component component = new Component();
        // Add a new HTTP server connector
        component.getServers().add(Protocol.HTTP, 8182);
        // Add a new FILE client connector
        component.getClients().add(Protocol.FILE);

        // Attach the application to the component and start it
        component.getDefaultHost().attach("/directoryApplication",
                new DirectoryApplication());
        component.getDefaultHost().attach("/dynamicApplication",
                new DynamicApplication());
        component.getDefaultHost().attach("/xmlApplication",
                new XmlApplication());
        component.getDefaultHost().attach("/cookiesRestlet",
                new CookiesRestlet());
        component.getDefaultHost().attach("/tunnelApplication",
                new TunnelApplication());
        component.getDefaultHost().attach("/nonStandardMethodsApplication",
                new NonStandardMethodsApplication());

        component.start();

        // Request the XML file
        final Client client = new Client(Protocol.HTTP);
        Response response = client
                .get("http://localhost:8182/xmlApplication/xml/mail.xml");
        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            // Get the entity as a DOM representation.
            final DomRepresentation domRep = response.getEntityAsDom();
            // Evaluate an XPath expression and get the list of nodes
            final NodeSet nodes = domRep.getNodes("/mail/recipients/to/text()");
            for (final Node node : nodes) {
                System.out.println(node.getNodeValue());
            }
            // Get the schema
            response = client
                    .get("http://localhost:8182/xmlApplication/xml/mail.xsd");
            if (response.getStatus().isSuccess()
                    && response.isEntityAvailable()) {
                domRep.validate(response.getEntity());
            }
        }

        // Request the XML file
        response = client
                .get("http://localhost:8182/xmlApplication/xml/mail.xml");

        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            // Get the entity as a SAX representation.
            final SaxRepresentation saxRep = response.getEntityAsSax();
            // In-line simple content handler.
            final ContentHandler contentHandler = new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName,
                        String name, Attributes attributes) throws SAXException {
                    System.out.println("processing element : " + name);
                }
            };
            // Parse the XML document
            saxRep.parse(contentHandler);
        }

        response = client.get("http://localhost:8182/cookiesRestlet");
        // Get the cookies sent by the server
        for (final CookieSetting cookieSetting : response.getCookieSettings()) {
            System.out.print("[");
            System.out.print(cookieSetting.getName());
            System.out.print("/");
            System.out.print(cookieSetting.getValue());
            System.out.print("]");
        }

        // Add them to each new request to the server
        Request request = new Request(Method.GET,
                "http://localhost:8182/cookiesRestlet");
        request.getCookies().addAll(response.getCookieSettings());
        // Add a new one
        request.getCookies().add(new Cookie("test", "value"));
        response = client.handle(request);
        response.getEntity().write(System.out);

        request = new Request(new Method("TEST"),
                "http://localhost:8182/nonStandardMethodsApplication");
        request
                .setEntity(new StringRepresentation("Test non standard method."));
        response = client.handle(request);
        System.out.println(response.getStatus());
        response.getEntity().write(System.out);

        // component.stop();
    }

}
