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

package org.restlet.example.book.restlet.ch04.sec5.sub2;

import java.io.IOException;

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
@Deprecated
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

        try {
            if (MediaType.APPLICATION_XML.isCompatible(representation
                    .getMediaType())) {
                // Parse the XML representation to get the mail bean
                mail = new XstreamRepresentation<Mail>(representation,
                        Mail.class).getObject();
                System.out.println("XML representation received");
            } else if (MediaType.APPLICATION_JSON.isCompatible(representation
                    .getMediaType())) {
                // Parse the JSON representation to get the mail bean
                mail = new JacksonRepresentation<Mail>(representation,
                        Mail.class).getObject();
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
        } catch (IOException e) {
            throw new ResourceException(e);
        }

        return null;
    }
}
