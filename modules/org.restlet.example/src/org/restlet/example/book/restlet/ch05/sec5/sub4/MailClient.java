package org.restlet.example.book.restlet.ch05.sec5.sub4;

import org.restlet.resource.ClientResource;

/**
 * Mail client retrieving a mail then storing it again on the same resource.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        MailResource mailClient = ClientResource.create(
                "http://localhost:8182/accounts/chunkylover53/mails/123",
                MailResource.class);
        mailClient.fromXml(mailClient.toXml());
        mailClient.fromJson(mailClient.toJson());
    }

}
