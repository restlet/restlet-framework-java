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

package org.restlet.example.book.restlet.ch04.sec2.sub7;

import java.io.IOException;

import javax.xml.transform.OutputKeys;

import org.restlet.data.LocalReference;
import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.TransformRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages XSLT transformation.
 */
public class MailServerResource extends ServerResource {

    @Get
    public Representation toXml() throws IOException {
        // Create a new DOM representation
        DomRepresentation rmepMail = new DomRepresentation();
        rmepMail.setIndenting(true);

        // Populate the DOM document
        Document doc = rmepMail.getDocument();

        Node mailElt = doc.createElement("mail");
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

        // Transform to another XML format using XSLT
        Representation transformSheet = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/Mail.xslt").get();

        TransformRepresentation result = new TransformRepresentation(rmepMail,
                transformSheet);
        result.getOutputProperties().put(OutputKeys.INDENT, "yes");
        return result;
    }

    @Put
    public void store(DomRepresentation mailRep) {
        // Retrieve the XML element using XPath expressions
        String subject = mailRep.getText("/email/head/subject");
        String content = mailRep.getText("/email/body");

        // Output the XML element values
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
    }
}
