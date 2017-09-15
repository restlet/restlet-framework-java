/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.restlet.ch07.sec2;

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
                "http://localhost:8111/accounts/chunkylover53/feeds/xyz");

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
