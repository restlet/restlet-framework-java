package org.restlet.example.book.restlet.ch9.dao.db4o;

import java.util.Date;

import org.restlet.example.book.restlet.ch9.objects.Feed;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * DAO that manages the persistence of Feed objects.
 * 
 */
public class FeedDAO extends Db4oDAO {

    public FeedDAO(ObjectContainer objectContainer) {
        super(objectContainer);
    }

    /**
     * Add a new Feed object in the database.
     * 
     * @param feed
     *                new Feed object to be added.
     * @return the feed object completed with its identfiant.
     */
    public Feed createFeed(Feed feed) {
        feed.setId(Long.toString(new Date().getTime()));
        objectContainer.store(feed);
        objectContainer.commit();

        return feed;
    }

    /**
     * Get a feed by its id.
     * 
     * @param feedId
     *                the feed's id.
     * @return a Feed object or null if no feed has been found.
     */
    public Feed getFeedById(String feedId) {
        Feed prototype = new Feed();
        prototype.setId(feedId);

        return getFeed(prototype);
    }

    /**
     * Get a feed according to a prototype.
     * 
     * @param prototype
     *                the prototype.
     * @return a Feed object or null if no feed has been found.
     */
    private Feed getFeed(Feed prototype) {
        Feed feed = null;
        ObjectSet<Feed> result = objectContainer.queryByExample(prototype);

        if (!result.isEmpty()) {
            feed = result.get(0);
        }

        return feed;
    }

    /**
     * Update a feed.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param feed
     *                the feed to be updated.
     */
    public void updateFeed(Feed feed) {
        objectContainer.store(feed);
        objectContainer.commit();
    }

    /**
     * Delete a feed.
     * 
     * @param feed
     *                the feed to be deleted.
     */
    public void deleteFeed(Feed feed) {
        objectContainer.delete(feed);
        objectContainer.commit();
    }

}
