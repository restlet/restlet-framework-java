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

package org.restlet.example.book.restlet.ch04.sec2.sub3;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages the SAX API.
 */
public class MailServerResource extends ServerResource {

    @Get
    public Representation toXml() {
        // Create a new SAX representation
        SaxRepresentation result = new SaxRepresentation() {

            public void write(org.restlet.ext.xml.XmlWriter writer)
                    throws IOException {
                try {
                    // Start document
                    writer.startDocument();

                    // Append the root node
                    writer.startElement("mail");

                    // Append the child nodes and set their text content
                    writer.startElement("status");
                    writer.characters("received");
                    writer.endElement("status");

                    writer.startElement("subject");
                    writer.characters("Message to self");
                    writer.endElement("subject");

                    writer.startElement("content");
                    writer.characters("Doh!");
                    writer.endElement("content");

                    writer.startElement("accountRef");
                    writer.characters(new Reference(getReference(), "..")
                            .getTargetRef().toString());
                    writer.endElement("accountRef");

                    // End the root node
                    writer.endElement("mail");

                    // End the document
                    writer.endDocument();
                } catch (SAXException e) {
                    throw new IOException(e.getMessage());
                }
            };
        };
        result.setNamespaceAware(true);

        return result;
    }

    @Put
    public void store(SaxRepresentation mailRep) throws IOException {
        mailRep.parse(new DefaultHandler() {

            @Override
            public void startElement(String uri, String localName,
                    String qName, Attributes attributes) throws SAXException {
                // Output the XML element names
                if ("status".equals(localName)) {
                    System.out.print("Status: ");
                } else if ("subject".equals(localName)) {
                    System.out.print("Subject: ");
                } else if ("content".equals(localName)) {
                    System.out.print("Content: ");
                } else if ("accountRef".equals(localName)) {
                    System.out.print("Account URI: ");
                }
            }

            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {
                // Output the XML element values
                System.out.print(new String(ch, start, length));
            }

            @Override
            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                // Output a new line
                System.out.println();
            }

        });
    }
}
