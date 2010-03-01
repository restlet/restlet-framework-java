package org.restlet.example.book.restlet.ch05.sec2;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * XXX.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        ClientResource mailClient = new ClientResource(
                "http://localhost:8182/accounts/chunkylover53/mails/123");
        Representation mailRepresentation = mailClient.get();
        mailClient.put(mailRepresentation);
    }

}
