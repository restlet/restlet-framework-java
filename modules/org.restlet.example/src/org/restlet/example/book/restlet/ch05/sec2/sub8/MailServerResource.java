/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

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
