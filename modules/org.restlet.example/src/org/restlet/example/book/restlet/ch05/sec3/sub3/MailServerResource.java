package org.restlet.example.book.restlet.ch05.sec3.sub3;

import org.restlet.data.Reference;
import org.restlet.ext.jackson.JacksonRepresentation;
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

        // Wraps the bean with a Jackson representation
        JacksonRepresentation<Mail> result = new JacksonRepresentation<Mail>(
                mail);
        return result;
    }

    @Override
    protected Representation put(Representation representation)
            throws ResourceException {
        // Parse the JSON representation to get the mail bean
        JacksonRepresentation<Mail> mailRep = new JacksonRepresentation<Mail>(
                representation, Mail.class);
        Mail mail = mailRep.getObject();

        // Output the XML element values
        System.out.println("Status: " + mail.getStatus());
        System.out.println("Subject: " + mail.getSubject());
        System.out.println("Content: " + mail.getContent());
        System.out.println("Account URI: " + mail.getAccountRef());

        return null;
    }
}
