package org.restlet.example.book.restlet.ch08.sec5.server.webapi.client;

import java.util.List;

import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.resource.ClientResource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Mail client.
 */
public class MailClient {

    /**
     * Mail client interacting with the RESTful mail server.
     * 
     * @param args
     *            The optional arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("\n1) Set-up the service client resource\n");
        ClientResource service = new ClientResource("http://localhost:8111");

        System.out.println("\n2) Describe the application\n");
        System.out.println(service.options().getText());

        System.out.println("\n3) Describe the accounts resource\n");
        System.out.println(service.getChild("/accounts/").options().getText());

        // FORM SUBMIT
        ClientResource mailClient = new ClientResource(
                "http://localhost:8111/accounts/123/mails/abc");
        mailClient.getRequest().getCookies()
                .add(new Cookie("Credentials", "scott=tiger"));

        Form form = new Form();
        form.add("subject", "Message to Jérôme");
        form.add("content", "Doh!\n\nAllo?");
        mailClient.put(form).write(System.out);

        // WEB FEEDS
        mailClient = new ClientResource(
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
