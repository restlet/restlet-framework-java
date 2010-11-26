package org.restlet.example.book.restlet.ch08.sec1.sub1;

import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages FreeMarker template engine.
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

        // Load the FreeMarker template
        Representation mailFtl = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/Mail.ftl").get();

        // Wraps the bean with a FreeMarker representation
        return new TemplateRepresentation(mailFtl,
                mail, MediaType.TEXT_HTML);
    }

    @Override
    protected Representation put(Representation input) {
        try {
            String inputText = input.getText();
            System.out.println(inputText);
            Form form = new Form(inputText);

            for (Parameter entry : form) {
                System.out.println(entry.getName() + "=" + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StringRepresentation("Mail updated!");
    }
}
