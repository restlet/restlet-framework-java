/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.dataservices.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Link;
import org.restlet.ext.dataservices.internal.edm.AssociationEnd;
import org.restlet.ext.dataservices.internal.edm.EntityType;
import org.restlet.ext.dataservices.internal.edm.Metadata;
import org.restlet.ext.dataservices.internal.edm.Property;
import org.restlet.ext.dataservices.internal.reflect.ReflectUtils;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.NodeSet;
import org.restlet.representation.Representation;
import org.w3c.dom.Node;

/**
 * Parses a Feed representation and extract from its entries a list of embedded
 * entities.
 * 
 * @param <T>
 *            The type of the target entities.
 * 
 * @author Thierry Boileau
 */
public class FeedParser<T> {

    /** Class of the entity targeted by this feed. */
    private Class<?> entityClass;

    /** The underlying feed. */
    private Feed feed;

    /** The internal logger. */
    private Logger logger;

    /** The metadata of the ADO.NET service. */
    private Metadata metadata;

    /**
     * Constructor.
     * 
     * @param feed
     *            The feed to parse.
     * @param entityClass
     *            The class of the target entities.
     * @param metadata
     *            The metadata of the ADO.NET service.
     */
    public FeedParser(Feed feed, Class<?> entityClass, Metadata metadata) {
        super();
        this.feed = feed;
        this.entityClass = entityClass;
        this.metadata = metadata;
    }

    /**
     * Creates a new feed parser for a specific entity class.
     * 
     * @param <E>
     *            The class of the target entity.
     * @param subpath
     *            The path to this entity relatively to the service URI.
     * @param entityClass
     *            The target class of the entity.
     * @param metadata
     *            The metadata of the ADO.NET service.
     * @return A feed parser instance.
     */
    public <E> FeedParser<E> createFeedParser(Feed feed, Class<E> entityClass,
            Metadata metadata) {
        return new FeedParser<E>(feed, entityClass, metadata);
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

    /**
     * Parses the current feed and returns a list of objects or null if the feed
     * is null.
     * 
     * @return A list of objects or null if the feed is null.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Iterator<T> parse() {
        Iterator<T> result = null;

        if (this.entityClass == null) {
            this.entityClass = ReflectUtils.getEntryClass(feed);
        }

        if (feed == null || metadata == null || entityClass == null) {
            return result;
        }

        List<T> list = new ArrayList<T>();
        EntityType entityType = metadata.getEntityType(entityClass);

        for (Entry entry : feed.getEntries()) {
            try {
                Object entity = entityClass.newInstance();
                Representation content = entry.getContent().getInlineContent();

                // Recreate the bean
                DomRepresentation dr = new DomRepresentation(content);
                NodeSet propertyNodes = dr.getNodes("/properties/*");

                for (Node node : propertyNodes) {
                    String nodeName = node.getNodeName();
                    int index = nodeName.indexOf(":");
                    if (index != -1) {
                        nodeName = nodeName.substring(index + 1);
                    }

                    Property property = metadata.getProperty(entity, nodeName);
                    try {
                        ReflectUtils.setProperty(entity, property, node
                                .getTextContent());
                    } catch (Exception e) {
                        getLogger().log(
                                Level.WARNING,
                                "Can't set the property " + nodeName + " of "
                                        + entity.getClass(), e);
                    }
                }

                // Examines the links
                for (Link link : entry.getLinks()) {
                    // Try to get inline content denoting the full content of a
                    // property of the current entity
                    if (link.getContent() != null && link.getTitle() != null) {
                        String propertyName = ReflectUtils.normalize(link
                                .getTitle());
                        // Get the associated entity
                        AssociationEnd association = metadata.getAssociation(
                                entityType, propertyName);
                        if (association != null) {
                            try {
                                Feed linkFeed = null;
                                if (association.isToMany()) {
                                    linkFeed = new Feed(link.getContent()
                                            .getInlineContent());
                                } else {
                                    linkFeed = new Feed();
                                    linkFeed.getEntries().add(
                                            new Entry(link.getContent()
                                                    .getInlineContent()));
                                }

                                Class<?> linkClass = ReflectUtils
                                        .getEntryClass(linkFeed);
                                Iterator<?> iterator = createFeedParser(
                                        linkFeed, linkClass, metadata).parse();
                                ReflectUtils.setProperty(entity, propertyName,
                                        association.isToMany(), iterator,
                                        linkClass);
                            } catch (Exception e) {
                                getLogger().log(
                                        Level.WARNING,
                                        "Can't retrieve associated property "
                                                + propertyName, e);
                            }
                        }
                    }
                }

                list.add((T) entity);
            } catch (InstantiationException e) {
                getLogger().log(
                        Level.WARNING,
                        "Can't instantiate the constructor without arguments of the entity class: "
                                + entityClass, e);
            } catch (IllegalAccessException e) {
                getLogger().log(
                        Level.WARNING,
                        "Can't instantiate the constructor without arguments of the entity class: "
                                + entityClass, e);
            }
        }
        result = list.iterator();

        return result;
    }
}
