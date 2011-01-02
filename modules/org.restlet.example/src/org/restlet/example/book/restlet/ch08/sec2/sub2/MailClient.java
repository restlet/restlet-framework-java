package org.restlet.example.book.restlet.ch08.sec2.sub2;

import java.util.List;

import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.resource.ClientResource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Mail client updating a mail by submitting a form.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        ClientResource mailClient = new ClientResource(
                "http://localhost:8111/accounts/123/feeds/xyz");

        // Display the retrieved Atom feed and entries
        Feed atomFeed = mailClient.get(Feed.class);
        System.out.println("\nAtom feed: " + atomFeed.getTitle() + "\n");

        for (Entry entry : atomFeed.getEntries()) {
            System.out.println("Title  : " + entry.getTitle());
            System.out.println("Summary: " + entry.getSummary());
        }

        // Display the retrieved RSS feed and entries
        SyndFeed rssFeed = mailClient.get(SyndFeed.class);
        System.out.println("\nRSS feed: " + rssFeed.getTitle() + "\n");

        @SuppressWarnings("unchecked")
        List<SyndEntry> entries = (List<SyndEntry>) rssFeed.getEntries();

        for (SyndEntry entry : entries) {
            System.out.println("Title  : " + entry.getTitle());
            System.out.println("Summary: " + entry.getDescription().getValue());
        }
    }
}
