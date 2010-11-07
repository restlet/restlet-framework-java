package org.restlet.example.book.restlet.ch05.sec2.sub8;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages XML Schema validation.
 */
public class MailServerResource extends ServerResource {

    @Override
    protected Representation get() throws ResourceException {
        // Create the mail bean
        Mail mail = new Mail();
        mail.setStatus("received");
        mail.setSubject("Message to self");
        mail.setContent("Doh!");
        mail.setAccountRef(new Reference(getReference(), "..").getTargetRef()
                .toString());

        // Wraps the bean with a JAXB representation
        JaxbRepresentation<Mail> result = new JaxbRepresentation<Mail>(mail);
        result.setFormattedOutput(true);
        return result;
    }

    @Override
    protected Representation put(Representation representation)
            throws ResourceException {
        try {
            // Parse the XML representation to get the mail bean
            JaxbRepresentation<Mail> mailRep = new JaxbRepresentation<Mail>(
                    representation, Mail.class);
            Mail mail = mailRep.getObject();

            // Output the XML element values
            System.out.println("Status: " + mail.getStatus());
            System.out.println("Subject: " + mail.getSubject());
            System.out.println("Content: " + mail.getContent());
            System.out.println("Account URI: " + mail.getAccountRef());
        } catch (IOException e) {
            throw new ResourceException(e);
        }

        return null;
    }
}
