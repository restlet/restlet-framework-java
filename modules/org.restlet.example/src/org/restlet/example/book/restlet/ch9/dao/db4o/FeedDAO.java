package org.restlet.example.book.restlet.ch9.dao.db4o;

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
     * Get a feed by its identifiant.
     * 
     * @param feedId
     *                the feed's identifiant.
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

}
