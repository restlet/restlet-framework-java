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

package org.restlet.example.book.restlet.ch04.sec2.sub2;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages the DOM API.
 */
public class MailServerResource extends ServerResource {

    @Get
    public Representation toXml() throws IOException {
        // Create an empty DOM representation
        DomRepresentation result = new DomRepresentation();

        // Ensure pretty printing
        result.setIndenting(true);

        // Retrieve the DOM document to populate
        Document doc = result.getDocument();

        // Append the root node
        Node mailElt = doc.createElement("mail");
        doc.appendChild(mailElt);

        // Append the child nodes and set their text content
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

        // Computes the parent URI including slash
        accountRefElt.setTextContent(new Reference(getReference(), "..")
                .getTargetRef().toString());
        mailElt.appendChild(accountRefElt);

        return result;
    }

    @Put
    public void store(DomRepresentation mailRep) throws IOException {
        // Parses and normalizes the DOM document
        Document doc = mailRep.getDocument();
        Element mailElt = doc.getDocumentElement();
        Element statusElt = (Element) mailElt.getElementsByTagName("status")
                .item(0);
        Element subjectElt = (Element) mailElt.getElementsByTagName("subject")
                .item(0);
        Element contentElt = (Element) mailElt.getElementsByTagName("content")
                .item(0);
        Element accountRefElt = (Element) mailElt.getElementsByTagName(
                "accountRef").item(0);

        // Output the XML element values
        System.out.println("Status: " + statusElt.getTextContent());
        System.out.println("Subject: " + subjectElt.getTextContent());
        System.out.println("Content: " + contentElt.getTextContent());
        System.out.println("Account URI: " + accountRefElt.getTextContent());
    }
}
