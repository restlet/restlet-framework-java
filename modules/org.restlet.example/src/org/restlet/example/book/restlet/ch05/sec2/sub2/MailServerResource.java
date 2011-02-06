/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.example.book.restlet.ch05.sec2.sub2;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages the DOM API.
 */
public class MailServerResource extends ServerResource {

    @Override
    protected Representation get() throws ResourceException {
        DomRepresentation result;

        try {
            // Create an empty DOM representation
            result = new DomRepresentation();

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
            accountRefElt.setTextContent(new Reference(getReference(), "..")
                    .getTargetRef().toString());
            mailElt.appendChild(accountRefElt);
        } catch (IOException e) {
            throw new ResourceException(e);
        }

        return result;
    }

    @Override
    protected Representation put(Representation representation)
            throws ResourceException {
        // Wraps the XML representation in a DOM representation
        DomRepresentation mailRep = new DomRepresentation(representation);

        // Parses and normalizes the DOM document
        Document doc;
        try {
            doc = mailRep.getDocument();

            Element mailElt = doc.getDocumentElement();
            Element statusElt = (Element) mailElt
                    .getElementsByTagName("status").item(0);
            Element subjectElt = (Element) mailElt.getElementsByTagName(
                    "subject").item(0);
            Element contentElt = (Element) mailElt.getElementsByTagName(
                    "content").item(0);
            Element accountRefElt = (Element) mailElt.getElementsByTagName(
                    "accountRef").item(0);

            // Output the XML element values
            System.out.println("Status: " + statusElt.getTextContent());
            System.out.println("Subject: " + subjectElt.getTextContent());
            System.out.println("Content: " + contentElt.getTextContent());
            System.out
                    .println("Account URI: " + accountRefElt.getTextContent());
        } catch (IOException e) {
            throw new ResourceException(e);
        }

        return null;
    }
}
