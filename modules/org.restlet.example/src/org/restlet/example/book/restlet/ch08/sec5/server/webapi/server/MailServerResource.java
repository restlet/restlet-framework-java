package org.restlet.example.book.restlet.ch08.sec5.server.webapi.server;

import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch08.sec5.server.webapi.common.MailRepresentation;
import org.restlet.example.book.restlet.ch08.sec5.server.webapi.common.MailResource;
import org.restlet.ext.wadl.WadlServerResource;

/**
 * Mail server resource implementing the {@link MailResource} interface.
 */
public class MailServerResource extends WadlServerResource implements MailResource {

    public MailRepresentation retrieve() {
        MailRepresentation mail = new MailRepresentation();
        mail.setStatus("received");
        mail.setSubject("Message to self");
        mail.setContent("Doh!");
        mail.setAccountRef(new Reference(getReference(), "..").getTargetRef()
                .toString());
        return mail;
    }

    public void store(MailRepresentation mail) {
        System.out.println("Status: " + mail.getStatus());
        System.out.println("Subject: " + mail.getSubject());
        System.out.println("Content: " + mail.getContent());
        System.out.println("Account URI: " + mail.getAccountRef());
        System.out.println();
    }

}
