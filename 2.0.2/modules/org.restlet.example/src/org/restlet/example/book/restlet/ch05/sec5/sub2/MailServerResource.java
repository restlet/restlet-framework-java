package org.restlet.example.book.restlet.ch05.sec5.sub2;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages Jackson extension.
 */
public class MailServerResource extends ServerResource {

    @Override
    protected void doInit() throws ResourceException {
        // Declares the two variants supported
        getVariants().add(new Variant(MediaType.APPLICATION_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException {
        Representation result = null;

        // Create the mail bean
        Mail mail = new Mail();
        mail.setStatus("received");
        mail.setSubject("Message to self");
        mail.setContent("Doh!");
        mail.setAccountRef(new Reference(getReference(), "..").getTargetRef()
                .toString());

        if (MediaType.APPLICATION_XML.isCompatible(variant.getMediaType())) {
            // Wraps the bean with an XStream representation
            result = new XstreamRepresentation<Mail>(mail);
        } else if (MediaType.APPLICATION_JSON.isCompatible(variant
                .getMediaType())) {
            // Wraps the bean with a Jackson representation
            result = new JacksonRepresentation<Mail>(mail);
        }

        return result;
    }

    @Override
    protected Representation put(Representation representation, Variant variant)
            throws ResourceException {
        Mail mail = null;

        if (MediaType.APPLICATION_XML.isCompatible(representation
                .getMediaType())) {
            // Parse the XML representation to get the mail bean
            mail = new XstreamRepresentation<Mail>(representation).getObject();
            System.out.println("XML representation received");
        } else if (MediaType.APPLICATION_JSON.isCompatible(representation
                .getMediaType())) {
            // Parse the JSON representation to get the mail bean
            mail = new JacksonRepresentation<Mail>(representation, Mail.class)
                    .getObject();
            System.out.println("JSON representation received");
        }

        if (mail != null) {
            // Output the mail bean
            System.out.println("Status: " + mail.getStatus());
            System.out.println("Subject: " + mail.getSubject());
            System.out.println("Content: " + mail.getContent());
            System.out.println("Account URI: " + mail.getAccountRef());
            System.out.println();
        }

        return null;
    }
}
