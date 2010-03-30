package org.restlet.example.book.restlet.ch05.sec5.sub4;

import org.restlet.data.Reference;
import org.restlet.resource.ServerResource;

/**
 * Mail server resource implementing the {@link MailResource} interface.
 */
public class MailServerResource extends ServerResource implements MailResource {

    public Mail retrieve() {
        Mail mail = new Mail();
        mail.setStatus("received");
        mail.setSubject("Message to self");
        mail.setContent("Doh!");
        mail.setAccountRef(new Reference(getReference(), "..").getTargetRef()
                .toString());
        return mail;
    }

    public void store(Mail mail) {
        System.out.println("Status: " + mail.getStatus());
        System.out.println("Subject: " + mail.getSubject());
        System.out.println("Content: " + mail.getContent());
        System.out.println("Account URI: " + mail.getAccountRef());
        System.out.println();
    }

}
