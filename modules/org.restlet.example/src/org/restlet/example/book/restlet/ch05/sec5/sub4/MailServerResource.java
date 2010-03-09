package org.restlet.example.book.restlet.ch05.sec5.sub4;

import org.restlet.data.Reference;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.resource.ServerResource;

/**
 *  
 */
public class MailServerResource extends ServerResource implements MailResource {

    private Mail getMail() {
        Mail mail = new Mail();
        mail.setStatus("received");
        mail.setSubject("Message to self");
        mail.setContent("Doh!");
        mail.setAccountRef(new Reference(getReference(), "..").getTargetRef()
                .toString());
        return mail;
    }

    public JacksonRepresentation<Mail> toJson() {
        return new JacksonRepresentation<Mail>(getMail());
    }

    public XstreamRepresentation<Mail> toXml() {
        return new XstreamRepresentation<Mail>(getMail());
    }

    public void fromXml(XstreamRepresentation<Mail> representation) {
        System.out.println("XML representation received");
        displayMail(representation.getObject());
    }

    public void fromJson(JacksonRepresentation<Mail> representation) {
        System.out.println("JSON representation received");
        displayMail(representation.getObject());
    }

    private void displayMail(Mail mail) {
        System.out.println("Status: " + mail.getStatus());
        System.out.println("Subject: " + mail.getSubject());
        System.out.println("Content: " + mail.getContent());
        System.out.println("Account URI: " + mail.getAccountRef());
        System.out.println();
    }

}
