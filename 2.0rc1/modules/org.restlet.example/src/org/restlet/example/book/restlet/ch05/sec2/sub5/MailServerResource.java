package org.restlet.example.book.restlet.ch05.sec2.sub5;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages XML namespaces.
 */
public class MailServerResource extends ServerResource {

    @Override
    protected Representation get() throws ResourceException {
        DomRepresentation result;

        try {
            // Create a new DOM representation
            result = new DomRepresentation();
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
        } catch (IOException e) {
            throw new ResourceException(e);
        }

        return result;
    }

    @Override
    protected Representation put(Representation representation)
            throws ResourceException {
        DomRepresentation mailRep = new DomRepresentation(representation);

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

        return null;
    }
}
