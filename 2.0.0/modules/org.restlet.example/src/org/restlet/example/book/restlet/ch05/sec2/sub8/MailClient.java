package org.restlet.example.book.restlet.ch05.sec2.sub8;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Mail client retrieving a mail then storing it again on the same resource.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        ClientResource mailClient = new ClientResource(
                "http://localhost:8182/accounts/chunkylover53/mails/123");
        Representation mailRepresentation = mailClient.get();
        mailClient.put(mailRepresentation);
    }

}
