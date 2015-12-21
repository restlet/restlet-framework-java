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

package org.restlet.example.book.restlet.ch07.sec5.website;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.example.book.restlet.ch07.sec5.webapi.common.MailRepresentation;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages FreeMarker template engine.
 */
public class MailServerResource extends ServerResource {

    @Get
    Representation retrieve() {
        // Create the mail URI inside the API application
        String accountId = getAttribute("accountId");
        String mailId = getAttribute("mailId");
        String mailApiUri = "riap://component/api/accounts/" + accountId
                + "/mails/" + mailId;

        // Optimal internal call using the server dispatcher
        ClientResource cr = new ClientResource(mailApiUri);
        cr.setNext(getContext().getServerDispatcher());
        MailRepresentation mail = cr.get(MailRepresentation.class);

        // Load the FreeMarker template
        Representation mailFtl = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/Mail.ftl").get();

        // Wraps the bean with a FreeMarker representation
        return new TemplateRepresentation(mailFtl, mail, MediaType.TEXT_HTML);
    }

    @Put
    public String store(Representation input) throws Exception {
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

        return "Mail updated!";
    }
}
