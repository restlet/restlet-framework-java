package org.restlet.example.book.restlet.ch05.sec5.sub2;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Mail client retrieving a mail then storing it again on the same resource.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        ClientResource mailClient = new ClientResource(
                "http://localhost:8182/accounts/chunkylover53/mails/123");

        Representation mailRepresentation = mailClient
                .get(MediaType.APPLICATION_XML);
        mailClient.put(mailRepresentation);

        mailRepresentation = mailClient.get(MediaType.APPLICATION_JSON);
        mailClient.put(mailRepresentation);
    }

}
