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

package org.restlet.ext.rome;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * A syndicated feed representation (such as RSS or Atom) based on the ROME
 * library. Add feed entries to {@link SyndFeed#getEntries()}, which you can
 * access via {@link #getFeed()}.
 * 
 * @author Tal Liron
 * @see <a href="http://java.net/projects/rome/">ROME home</a>
 * @deprecated Not actively developed anymore.
 */
@Deprecated
public class SyndFeedRepresentation extends WriterRepresentation {

    /**
     * Converts a feed type to a media type.
     * 
     * @param feedType
     *            The source feed type.
     * @return The result media type or null.
     */
    public static MediaType getMediaType(String feedType) {
        MediaType result = null;

        if (feedType != null) {
            result = feedType.startsWith("atom") ? MediaType.APPLICATION_ATOM
                    : MediaType.APPLICATION_RSS;
        }

        return result;
    }

    /** The syndication feed. */
    private volatile SyndFeed feed;

    /**
     * Constructs a UTF8 RSS 2.0 feed.
     */
    public SyndFeedRepresentation() {
        this("rss_2.0");
    }

    /**
     * Constructor that parses the given feed representation.
     * 
     * @param feedRepresentation
     *            The feed representation to parse.
     * @throws IOException
     */
    public SyndFeedRepresentation(Representation feedRepresentation)
            throws IOException {
        super(null);

        InputSource source = new InputSource(feedRepresentation.getStream());

        try {
            this.feed = new SyndFeedInput().build(source);
        } catch (IllegalArgumentException e) {
            IOException ioe = new IOException(
                    "Couldn't read the feed representation. " + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (FeedException e) {
            IOException ioe = new IOException(
                    "Couldn't read the feed representation. " + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        setMediaType(getMediaType(this.feed.getFeedType()));
    }

    /**
     * Constructs a UTF8 feed.
     * 
     * @param feedType
     *            The feed type (see ROME documentation)
     */
    public SyndFeedRepresentation(String feedType) {
        this(feedType, CharacterSet.UTF_8);
    }

    /**
     * Constructs a feed.
     * 
     * @param feedType
     *            The feed type (see ROME documentation)
     * @param characterSet
     *            The character set
     */
    public SyndFeedRepresentation(String feedType, CharacterSet characterSet) {
        this(feedType, new ArrayList<Object>(), characterSet);
    }

    /**
     * Constructs a feed.
     * 
     * @param feedType
     *            The feed type (see ROME documentation)
     * @param entries
     *            The list of entries
     * @param characterSet
     *            The character set
     */
    public SyndFeedRepresentation(String feedType, List<?> entries,
            CharacterSet characterSet) {
        super(getMediaType(feedType));
        setCharacterSet(characterSet);
        this.feed = new SyndFeedImpl();
        this.feed.setFeedType(feedType);
        this.feed.setEntries(entries);
    }

    /**
     * Constructor around an existing feed.
     * 
     * @param feed
     *            The feed (must have a valid feedType!)
     */
    public SyndFeedRepresentation(SyndFeed feed) {
        super(getMediaType(feed.getFeedType()));
        this.feed = feed;
    }

    /**
     * The wrapped feed.
     * 
     * @return The feed
     */
    public SyndFeed getFeed() {
        return this.feed;
    }

    @Override
    public void write(Writer writer) throws IOException {
        try {
            SyndFeedOutput output = new SyndFeedOutput();

            if (this.feed.getFeedType() == null) {
                this.feed.setFeedType("atom_1.0");
            }

            output.output(this.feed, writer);
        } catch (FeedException e) {
            IOException ioe = new IOException("Feed exception");
            ioe.initCause(e);
            throw ioe;
        }
    }
}
