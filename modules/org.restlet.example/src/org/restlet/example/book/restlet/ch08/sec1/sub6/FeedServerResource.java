package org.restlet.example.book.restlet.ch08.sec1.sub6;

import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Text;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Resource corresponding to an account feed associated to tags.
 */
public class FeedServerResource extends ServerResource {

    @Get
    public Feed toAtom() throws ResourceException {
        Feed result = new Feed();
        result.setTitle(new Text("Homer's feed"));
        Entry entry;

        for (int i = 1; i < 11; i++) {
            entry = new Entry();
            entry.setTitle(new Text("Mail n°" + i));
            entry.setSummary("Doh! This is the content of mail n°" + i);
            result.getEntries().add(entry);
        }

        return result;
    }

    @Get
    protected SyndFeed toRss() throws ResourceException {
        SyndFeed result = null;

        return result;
    }
}
