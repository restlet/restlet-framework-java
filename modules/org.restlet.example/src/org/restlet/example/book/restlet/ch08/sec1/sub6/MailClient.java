package org.restlet.example.book.restlet.ch08.sec1.sub6;

import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.resource.ClientResource;

/**
 * Mail client updating a mail by submitting a form.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        ClientResource mailClient = new ClientResource(
                "http://localhost:8182/accounts/123/feeds/xyz");
        Feed feed = mailClient.get(Feed.class);

        // Display the retrieved feed and entries
        System.out.println("Title: " + feed.getTitle());

        for (Entry entry : feed.getEntries()) {
            System.out.println("Entry title: " + entry.getTitle());
            System.out.println("Entry summary: " + entry.getSummary());
        }

    }

}
