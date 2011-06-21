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
