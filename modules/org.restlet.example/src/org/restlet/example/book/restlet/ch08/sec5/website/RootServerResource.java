package org.restlet.example.book.restlet.ch08.sec5.website;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch08.sec5.webapi.common.MailRepresentation;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Root resource implementation.
 */
public class RootServerResource extends ServerResource {

    @Override
    protected Representation get() throws ResourceException {
        // Create the mail bean
        MailRepresentation mail = new MailRepresentation();
        mail.setStatus("received");
        mail.setSubject("Message to self");
        mail.setContent("Doh!");
        mail.setAccountRef(new Reference(getReference(), "..").getTargetRef()
                .toString());

        // Load the FreeMarker template
        Representation mailFtl = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/Mail.ftl").get();

        // Wraps the bean with a FreeMarker representation
        return new TemplateRepresentation(mailFtl, mail, MediaType.TEXT_HTML);
    }

    public String represent() {
        return "Welcome to the " + getApplication().getName() + " !";
    }

}
