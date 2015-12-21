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

package org.restlet.example.book.restlet.ch04.sec2.sub6;

import java.io.IOException;

import org.restlet.data.LocalReference;
import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages XML Schema validation.
 */
public class MailServerResource extends ServerResource {

    @Get
    public Representation toXml() throws IOException {
        // Create a new DOM representation
        DomRepresentation result = new DomRepresentation();
        result.setIndenting(true);

        // XML namespace configuration
        result.setNamespaceAware(true);

        // Populate the DOM document
        Document doc = result.getDocument();

        Node mailElt = doc.createElementNS(
                "http://www.rmep.org/namespaces/1.0", "mail");
        doc.appendChild(mailElt);

        Node statusElt = doc.createElement("status");
        statusElt.setTextContent("received");
        mailElt.appendChild(statusElt);

        Node subjectElt = doc.createElement("subject");
        subjectElt.setTextContent("Message to self");
        mailElt.appendChild(subjectElt);

        Node contentElt = doc.createElement("content");
        contentElt.setTextContent("Doh!");
        mailElt.appendChild(contentElt);

        Node accountRefElt = doc.createElement("accountRef");
        accountRefElt.setTextContent(new Reference(getReference(), "..")
                .getTargetRef().toString());
        mailElt.appendChild(accountRefElt);
        return result;
    }

    @Put
    public void store(DomRepresentation mailRep) throws IOException {
        // Configure the XML Schema used for validation
        Representation mailXsd = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/Mail.xsd").get();
        mailRep.setSchema(mailXsd);
        mailRep.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                throw new ResourceException(exception);
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
                throw new ResourceException(exception);
            }

            public void warning(SAXParseException exception)
                    throws SAXException {
                throw new ResourceException(exception);
            }
        });

        // XML namespace configuration
        String rmepNs = "http://www.rmep.org/namespaces/1.0";
        mailRep.setNamespaceAware(true);
        mailRep.getNamespaces().put("", rmepNs);
        mailRep.getNamespaces().put("rmep", rmepNs);

        // Retrieve the XML element using XPath expressions
        String status = mailRep.getText("/:mail/:status");
        String subject = mailRep.getText("/rmep:mail/:subject");
        String content = mailRep.getText("/rmep:mail/rmep:content");
        String accountRef = mailRep.getText("/:mail/rmep:accountRef");

        // Output the XML element values
        System.out.println("Status: " + status);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        System.out.println("Account URI: " + accountRef);
    }
}
