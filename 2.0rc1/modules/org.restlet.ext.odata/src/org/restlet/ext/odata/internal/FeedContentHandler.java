/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.odata.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.FeedReader;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Person;
import org.restlet.ext.atom.Relation;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.internal.edm.AssociationEnd;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Mapping;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.edm.Property;
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

    /** The currently parsed association. */
    private AssociationEnd association;

    /** The value retrieved from the "count" tag. */
    private int count = -1;

    /** The path of the current XML element relatively to an Entry. */
    List<String> eltPath;

    /** The list of entities to complete. */
    List<T> entities;

    /** The current entity. */
    Object entity;

    /** The class of the entity targeted by this feed. */
    private Class<?> entityClass;

    /** The OData type of the parsed entities. */
    private EntityType entityType;

    /** Used to parsed Atom link elements that contains entries. */
    EntryContentHandler<T> extraEntryHandler;

    /** Used to parsed Atom link elements that contains feeds. */
    FeedContentHandler<T> extraFeedHandler;

    /** Internal logger. */
    private Logger logger;

    /** The currently parsed OData mapping. */
    private Mapping mapping;

    /** The metadata of the WCF service. */
    private Metadata metadata;

    /** Are we parsing an association? */
    private boolean parseAssociation;

    /** Are we parsing an entry content element? */
    private boolean parseContent;

    /** Are we parsing the count tag? */
    private boolean parseCount;

    /** Are we parsing an entry? */
    private boolean parseEntry;

    /** Are we parsing entity properties? */
    private boolean parseProperties;

    /** Are we parsing an entity property? */
    private boolean parseProperty;

    /** Must the current property be set to null? */
    private boolean parsePropertyNull;

    /** Used to handle property path. */
    private List<String> propertyPath;

    /** Used to handle property path. */
    private int propertyPathDeep = -1;

    /** Used to glean text content. */
    StringBuilder sb = null;

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
        this.entities = new ArrayList<T>();
        this.entityClass = entityClass;
        this.metadata = metadata;
        this.eltPath = new ArrayList<String>();
        this.logger = logger;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (parseAssociation) {
            if (extraFeedHandler != null) {
                extraFeedHandler.characters(ch, start, length);
            } else {
                extraEntryHandler.characters(ch, start, length);
            }
        } else if (parseCount || parseProperty || (mapping != null)) {
            sb.append(ch, start, length);
        }
    }

    @Override
    public void endContent(Content content) {
        parseContent = false;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (parseAssociation) {
            // Relay the events to the extra handlers
            if (extraFeedHandler != null) {
                extraFeedHandler.endElement(uri, localName, qName);
            } else {
                extraEntryHandler.endElement(uri, localName, qName);
            }
            if (localName.equals("entry")) {
                if (extraFeedHandler != null) {
                    extraFeedHandler.endEntry(null);
                } else {
                    extraEntryHandler.endEntry(null);
                }
            } else if (localName.equals("link")) {
                if (extraFeedHandler != null) {
                    extraFeedHandler.endLink(null);
                } else {
                    extraEntryHandler.endLink(null);
                }
            } else if (localName.equals("content")) {
                if (extraFeedHandler != null) {
                    extraFeedHandler.endContent(null);
                } else {
                    extraEntryHandler.endContent(null);
                }
            }
        } else if (parseCount) {
            this.count = Integer.parseInt(sb.toString());
            parseCount = false;
        } else if (parseProperty) {
            parseProperty = propertyPathDeep > 0;
            if (!parsePropertyNull) {
                Object obj = entity;
                if (propertyPath.size() > 1) {
                    for (int i = 0; i < propertyPath.size() - 1; i++) {
                        try {
                            Object o = ReflectUtils.invokeGetter(obj,
                                    propertyPath.get(i));
                            if (o == null) {
                                // Try to instantiate it
                                Field[] fields = obj.getClass()
                                        .getDeclaredFields();
                                for (Field field : fields) {
                                    if (field.getName().equalsIgnoreCase(
                                            propertyPath.get(i))) {
                                        o = field.getType().newInstance();
                                        break;
                                    }
                                }
                            }
                            ReflectUtils.invokeSetter(obj, propertyPath.get(i),
                                    o);
                            obj = o;
                        } catch (Exception e) {
                            obj = null;
                        }
                    }
                }
                Property property = metadata.getProperty(obj, localName);
                try {
                    if (property != null) {
                        ReflectUtils.setProperty(obj, property, sb.toString());
                    }
                } catch (Exception e) {
                    getLogger().warning(
                            "Cannot set " + localName + " property on " + obj
                                    + " with value " + sb.toString());
                }
            }
            propertyPathDeep--;
            if (propertyPath.size() > 0) {
                propertyPath.remove(propertyPath.size() - 1);
            }
        } else if (parseProperties) {
            if (Service.WCF_DATASERVICES_METADATA_NAMESPACE.equals(uri)
                    && "properties".equals(localName)) {
                parseProperties = false;
            }
        } else if (parseEntry) {
            if (mapping != null) {
                if (sb != null) {
                    try {
                        ReflectUtils.invokeSetter(entity, mapping
                                .getPropertyPath(), sb.toString());
                    } catch (Exception e) {
                        getLogger().warning(
                                "Cannot set the mapped property "
                                        + mapping.getPropertyPath() + " on "
                                        + entity + " with value "
                                        + sb.toString());
                    }
                }
                mapping = null;
            }

            if (!eltPath.isEmpty()) {
                eltPath.remove(eltPath.size() - 1);
            }
        }
    }

    @Override
    public void endEntry(Entry entry) {
        parseEntry = false;

        // Handle mapped values.
        for (Mapping m : metadata.getMappings()) {
            if (entityType != null && entityType.equals(m.getType())
                    && m.getNsUri() == null && m.getNsPrefix() == null) {
                // mapping atom
                Person author = (entry.getAuthors().isEmpty()) ? null : entry
                        .getAuthors().get(0);
                Person contributor = (entry.getContributors().isEmpty()) ? null
                        : entry.getContributors().get(0);
                Object value = null;
                if ("SyndicationAuthorEmail".equals(m.getValuePath())) {
                    value = (author != null) ? author.getEmail() : null;
                } else if ("SyndicationAuthorName".equals(m.getValuePath())) {
                    value = (author != null) ? author.getName() : null;
                } else if ("SyndicationAuthorUri".equals(m.getValuePath())) {
                    value = (author != null) ? author.getUri().toString()
                            : null;
                } else if ("SyndicationContributorEmail".equals(m
                        .getValuePath())) {
                    value = (contributor != null) ? contributor.getEmail()
                            : null;
                } else if ("SyndicationContributorName"
                        .equals(m.getValuePath())) {
                    value = (contributor != null) ? contributor.getName()
                            : null;
                } else if ("SyndicationContributorUri".equals(m.getValuePath())) {
                    value = (contributor != null) ? contributor.getUri()
                            .toString() : null;
                } else if ("SyndicationPublished".equals(m.getValuePath())) {
                    value = entry.getPublished();
                } else if ("SyndicationRights".equals(m.getValuePath())) {
                    value = (entry.getRights() != null) ? entry.getRights()
                            .getContent() : null;
                } else if ("SyndicationSummary".equals(m.getValuePath())) {
                    value = entry.getSummary();
                } else if ("SyndicationTitle".equals(m.getValuePath())) {
                    value = (entry.getTitle() != null) ? entry.getTitle()
                            .getContent() : null;
                } else if ("SyndicationUpdated".equals(m.getValuePath())) {
                    value = entry.getUpdated();
                }

                try {
                    if (value != null) {
                        ReflectUtils.invokeSetter(entity, m.getPropertyPath(),
                                value);
                    }
                } catch (Exception e) {
                    getLogger().warning(
                            "Cannot set the mapped property "
                                    + m.getPropertyPath() + " on " + entity
                                    + " with value " + value);
                }
            }
        }

        // If the entity is a blob, get the edit reference
        if (entityType.isBlob()
                && entityType.getBlobValueEditRefProperty() != null) {
            // Look for en entry with a "edit-media" relation value.
            Link link = entry.getLink(Relation.EDIT_MEDIA);
            String pty = entityType.getBlobValueEditRefProperty().getName();
            if (link != null) {
                try {
                    ReflectUtils.invokeSetter(entity, pty, link.getHref());
                } catch (Exception e) {
                    getLogger().warning(
                            "Cannot set the property " + pty + " on " + entity
                                    + " with value " + link.getHref());
                }
            }
        }

        entity = null;
    }

    @Override
    public void endLink(Link link) {
        if (parseAssociation) {
            parseAssociation = false;

            String propertyName = ReflectUtils.normalize(link.getTitle());
            if (extraFeedHandler != null) {
                try {
                    ReflectUtils.setProperty(entity, propertyName, association
                            .isToMany(), extraFeedHandler.getEntities()
                            .iterator(), ReflectUtils.getSimpleClass(entity,
                            propertyName));
                } catch (Exception e) {
                    getLogger().warning(
                            "Cannot set " + propertyName + " property on "
                                    + entity + " from link");
                }
            } else {
                try {
                    ReflectUtils.invokeSetter(entity, propertyName,
                            extraEntryHandler.getEntity());
                } catch (Exception e) {
                    getLogger().warning(
                            "Cannot set " + propertyName + " property on "
                                    + entity + " from link");
                }
            }
        }
        association = null;
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
        this.parseContent = true;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        if (parseAssociation) {
            // relays event to the extra handler
            if (extraFeedHandler != null) {
                extraFeedHandler.startElement(uri, localName, qName, attrs);
            } else {
                extraEntryHandler.startElement(uri, localName, qName, attrs);
            }

            if (localName.equals("feed")) {
                Feed feed = new Feed();
                if (extraFeedHandler != null) {
                    extraFeedHandler.startFeed(feed);
                }
            } else if (localName.equals("entry")) {
                Entry entry = new Entry();
                if (extraFeedHandler != null) {
                    extraFeedHandler.startEntry(entry);
                } else {
                    extraEntryHandler.startEntry(entry);
                }
            } else if (localName.equals("link")) {
                Link link = new Link();
                link.setHref(new Reference(attrs.getValue("", "href")));
                link.setRel(Relation.valueOf(attrs.getValue("", "rel")));
                String type = attrs.getValue("", "type");
                if (type != null && type.length() > 0) {
                    link.setType(new MediaType(type));
                }

                link.setHrefLang(new Language(attrs.getValue("", "hreflang")));
                link.setTitle(attrs.getValue("", "title"));
                final String attr = attrs.getValue("", "length");
                link.setLength((attr == null) ? -1L : Long.parseLong(attr));
                if (extraFeedHandler != null) {
                    extraFeedHandler.startLink(link);
                } else {
                    extraEntryHandler.startLink(link);
                }
            } else if (localName.equals("content")) {
                Content content = new Content();
                if (extraFeedHandler != null) {
                    extraFeedHandler.startContent(content);
                } else {
                    extraEntryHandler.startContent(content);
                }
            }
        } else if (parseProperties) {
            if (Service.WCF_DATASERVICES_NAMESPACE.equals(uri)) {
                sb = new StringBuilder();
                propertyPathDeep++;
                propertyPath.add(localName);
                parseProperty = true;
                parsePropertyNull = Boolean.parseBoolean(attrs.getValue(
                        Service.WCF_DATASERVICES_METADATA_NAMESPACE, "null"));
            }
        } else if (parseContent) {
            if (Service.WCF_DATASERVICES_METADATA_NAMESPACE.equals(uri)
                    && "properties".equals(localName)) {
                parseProperties = true;
                propertyPathDeep = 0;
                propertyPath = new ArrayList<String>();
            } else {
                if (entityType.isBlob()
                        && entityType.getBlobValueRefProperty() != null) {
                    String str = attrs.getValue("src");
                    if (str != null) {
                        try {
                            ReflectUtils.invokeSetter(entity, entityType
                                    .getBlobValueRefProperty().getName(),
                                    new Reference(str));
                        } catch (Exception e) {
                            getLogger().warning(
                                    "Cannot set "
                                            + entityType
                                                    .getBlobValueRefProperty()
                                                    .getName()
                                            + " property on " + entity
                                            + " with value " + str);
                        }
                    }
                }
            }
        } else if (Service.WCF_DATASERVICES_METADATA_NAMESPACE.equals(uri)
                && "count".equals(localName)) {
            sb = new StringBuilder();
            parseCount = true;
        } else if (parseEntry) {
            if (Service.WCF_DATASERVICES_METADATA_NAMESPACE.equals(uri)
                    && "properties".equals(localName) && entityType.isBlob()) {
                // in case of Media Link entries, the properties are directly
                // inside the entry.
                parseProperties = true;
                propertyPathDeep = 0;
                propertyPath = new ArrayList<String>();
            } else {
                // Could be mapped value
                eltPath.add(localName);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < eltPath.size(); i++) {
                    if (i > 0) {
                        sb.append("/");
                    }
                    sb.append(eltPath.get(i));
                }
                String str = sb.toString();

                // Check if this path is mapped.
                for (Mapping m : metadata.getMappings()) {
                    if (entityType != null && entityType.equals(m.getType())
                            && m.getNsUri() != null && m.getNsUri().equals(uri)
                            && str.equals(m.getValueNodePath())) {
                        if (m.isAttributeValue()) {
                            String value = attrs.getValue(uri, m
                                    .getValueAttributeName());
                            if (value != null) {
                                try {
                                    ReflectUtils.invokeSetter(entity, m
                                            .getPropertyPath(), value);
                                } catch (Exception e) {
                                    getLogger().warning(
                                            "Cannot set " + m.getPropertyPath()
                                                    + " property on " + entity
                                                    + " with value " + value);
                                }
                            }
                        } else {
                            this.sb = new StringBuilder();
                            mapping = m;
                        }
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startEntry(Entry entry) {
        parseEntry = true;
        eltPath = new ArrayList<String>();
        // Instantiate the entity
        try {
            entity = entityClass.newInstance();
            entities.add((T) entity);
        } catch (Exception e) {
            getLogger().warning(
                    "Error when instantiating  class " + entityClass);
        }
    }

    @Override
    public void startFeed(Feed feed) {
        if (this.entityClass == null) {
            this.entityClass = ReflectUtils.getEntryClass(feed);
        }
        entityType = metadata.getEntityType(entityClass);
    }

    @Override
    public void startLink(Link link) {
        if (link.getTitle() != null && entityType != null) {
            String propertyName = ReflectUtils.normalize(link.getTitle());
            // Get the associated entity
            association = metadata.getAssociation(entityType, propertyName);
            parseAssociation = association != null;
            if (parseAssociation) {
                if (association.isToMany()) {
                    extraFeedHandler = new FeedContentHandler<T>(ReflectUtils
                            .getSimpleClass(entity, propertyName), metadata,
                            getLogger());
                } else {
                    extraEntryHandler = new EntryContentHandler<T>(ReflectUtils
                            .getSimpleClass(entity, propertyName), metadata,
                            getLogger());
                }
            }
        }
    }
}
