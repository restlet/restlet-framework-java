package org.restlet.example.book.restlet.ch08.sec5.server.website;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch08.sec5.server.webapi.common.MailRepresentation;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.InputRepresentation;
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

    @Override
    protected Representation put(Representation input) {
        try {
            // Create a factory for disk-based file items
            RestletFileUpload fileUpload = new RestletFileUpload(
                    new DiskFileItemFactory());
            List<FileItem> fileItems = fileUpload.parseRepresentation(input);

            for (FileItem fileItem : fileItems) {
                if (fileItem.isFormField()) {
                    System.out.println(fileItem.getFieldName() + "="
                            + fileItem.getString());
                } else {
                    Representation attachment = new InputRepresentation(
                            fileItem.getInputStream());
                    attachment.write(System.out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StringRepresentation("Mail updated!");
    }
}
