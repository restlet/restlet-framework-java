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

package org.restlet.example.book.restlet.ch04.sec2.sub8;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account. Leverages JAXB extension.
 */
public class MailServerResource extends ServerResource {

    @Get
    public Representation toXml() {
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

    @Put
    public void store(Representation rep) throws IOException {
        // Parse XML representation to get the mail bean
        JaxbRepresentation<Mail> mailRep = new JaxbRepresentation<Mail>(rep,
                Mail.class);
        Mail mail = mailRep.getObject();

        // Output the XML element values
        System.out.println("Status: " + mail.getStatus());
        System.out.println("Subject: " + mail.getSubject());
        System.out.println("Content: " + mail.getContent());
        System.out.println("Account URI: " + mail.getAccountRef());
    }
}
