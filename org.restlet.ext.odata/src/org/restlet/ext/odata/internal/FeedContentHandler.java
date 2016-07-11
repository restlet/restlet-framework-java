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

package org.restlet.ext.odata.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.FeedReader;
import org.restlet.ext.atom.Link;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Generic Content handler for Atom Feed that takes care of OData specific
 * needs, such as parsing XML content from other namespaces than Atom. It
 * generates entities based on the values discovered in the feed.
 * 
 * @author Thierry Boileau
 * @param <T>
 *            The type of the parsed entities.
 */
public class FeedContentHandler<T> extends FeedReader {

    /** The value retrieved from the "count" tag. */
    private int count = -1;

    /** The list of entities to complete. */
    List<T> entities;

    /** The class of the entity targeted by this feed. */
    private Class<?> entityClass;

    /** The OData type of the parsed entities. */
    private EntityType entityType;

    /** Used to parsed Atom entry elements. */
    EntryContentHandler<T> entryHandler;

    /** This is true if we are inside our own 'feed' element. */
    private boolean isInFeed;

    /** Internal logger. */
    private Logger logger;

    /** The metadata of the OData service. */
    private Metadata metadata;

    /** Are we parsing the count tag? */
    private boolean parseCount;

    /** Are we parsing an entry? */
    private boolean parseEntry;

    /** Used to glean text content. */
    StringBuilder sb = null;

    /**
     * Constructor.
     * 
     * @param entityClass
     *            The class of the parsed entities.
     * @param entityType
     *            The entity type of the parsed entities.
     * @param metadata
     *            The metadata of the remote OData service.
     * @param logger
     *            The logger.
     */
    public FeedContentHandler(Class<?> entityClass, EntityType entityType,
            Metadata metadata, Logger logger) {
        this.entityClass = entityClass;
        this.entities = new ArrayList<T>();
        this.entityType = entityType;
        this.entryHandler = new EntryContentHandler<T>(entityClass, entityType,
                metadata, logger);
        this.logger = logger;
        this.metadata = metadata;
    }

    /**
     * Constructor.
     * 
     * @param entityClass
     *            The class of the parsed entities.
     * @param metadata
     *            The metadata of the remote OData service.
     * @param logger
     *            The logger.
     */
    public FeedContentHandler(Class<?> entityClass, Metadata metadata,
            Logger logger) {
        super();
        this.entityClass = entityClass;
        this.entities = new ArrayList<T>();
        this.entryHandler = new EntryContentHandler<T>(entityClass, metadata,
                logger);
        this.logger = logger;
        this.metadata = metadata;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (parseCount) {
            sb.append(ch, start, length);
        } else if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.characters(ch, start, length);
        }
    }

    /**
     * Handle the end of a "link" element. Doesn't close link that belongs to
     * inner associations!
     * 
     * @return Returns true if the caller should not pop state.
     * @author Emmanuel Liossis
     */
    public boolean closeLink() {
        if (parseEntry) {
            // Cascade to any expanded associations. If someone from inner
            // expanded associations has matched the close of the "link" and has
            // popped state, inhibit my callers from popping state.
            return entryHandler.closeLink();
        }

        // If we have passed 'feed' tag but haven't met any 'entry' tag, inhibit
        // callers from popping state when link closes, because in this case the
        // link pertains to the feed.
        return isInFeed;
    }

    @Override
    public void endContent(Content content) {
        if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.endContent(content);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (parseCount) {
            this.count = Integer.parseInt(sb.toString());
            parseCount = false;
        }
        // Allow also the "entry" end handling to cascade.
        else if (parseEntry
                || (entryHandler != null && localName.equals("entry"))) {
            // Delegate to the Entry reader
            entryHandler.endElement(uri, localName, qName);
        }

        if (!parseEntry && uri.equals("http://www.w3.org/2005/Atom")
                && localName.equals("feed")) {
            // Mark the end of 'feed' element pertaining to this handler.
            isInFeed = false;
        }

    }

    @Override
    public void endEntry(Entry entry) {
        // Only add the entity to the feed if it is our entry closing,
        // not an inner entry of an expanded association.
        if (entryHandler.closeEntry(entry)) {
            parseEntry = false;

            T entity = entryHandler.getEntity();

            if (entity != null) {
                entities.add(entity);
            } else {
                getLogger().warning("Can't add a null entity.");
            }
        }
    }

    @Override
    public void endLink(Link link) {
        if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.endLink(link);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.endPrefixMapping(prefix);
        }
    }

    /**
     * Returns the value of the "count" tag, that is to say the size of the
     * current entity set.
     * 
     * @return The size of the current entity set, as specified by the Atom
     *         document.
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the list of discovered entities.
     * 
     * @return The list of discovered entities.
     */
    public List<T> getEntities() {
        return entities;
    }

    /**
     * Returns the current logger.
     * 
     * @return The current logger.
     */
    private Logger getLogger() {
        if (logger == null) {
            logger = Context.getCurrentLogger();
        }
        return logger;
    }

    @Override
    public void startContent(Content content) {
        if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.startContent(content);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        if (!parseEntry
                && Service.WCF_DATASERVICES_METADATA_NAMESPACE.equals(uri)
                && "count".equals(localName)) {
            sb = new StringBuilder();
            parseCount = true;
        } else if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.startElement(uri, localName, qName, attrs);
        } else if (uri.equals("http://www.w3.org/2005/Atom")
                && localName.equals("feed")) {
            // Mark the start of the 'feed' element pertaining to this handler.
            isInFeed = true;
        }
    }

    @Override
    public void startEntry(Entry entry) {
        parseEntry = true;
        // Delegate to the Entry reader
        entryHandler.startEntry(entry);
    }

    @Override
    public void startFeed(Feed feed) {
        if (this.entityClass == null) {
            this.entityClass = ReflectUtils.getEntryClass(feed);
        }
        if (this.entityType == null && metadata != null) {
            entityType = metadata.getEntityType(entityClass);
        }
    }

    @Override
    public void startLink(Link link) {
        if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.startLink(link);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (parseEntry) {
            // Delegate to the Entry reader
            entryHandler.startPrefixMapping(prefix, uri);
        }
    }
}
