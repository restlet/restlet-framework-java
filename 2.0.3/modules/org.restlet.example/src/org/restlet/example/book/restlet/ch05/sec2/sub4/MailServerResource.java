package org.restlet.example.book.restlet.ch05.sec2.sub4;

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
 * account. Leverages the XPath language.
 */
public class MailServerResource extends ServerResource {

    @Override
    protected Representation get() throws ResourceException {
        DomRepresentation result;

        try {
            // Create a new DOM representation
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

        // Retrieve the XML element using XPath expressions
        String status = mailRep.getText("/mail/status");
        String subject = mailRep.getText("/mail/subject");
        String content = mailRep.getText("/mail/content");
        String accountRef = mailRep.getText("/mail/accountRef");

        // Output the XML element values
        System.out.println("Status: " + status);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
        System.out.println("Account URI: " + accountRef);

        return null;
    }
}
