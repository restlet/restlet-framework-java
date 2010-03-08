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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.EntryReader;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Person;
import org.restlet.ext.atom.Relation;
import org.restlet.ext.odata.Service;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Mapping;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.edm.Property;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Content handler for Atom Feed that takes care of OData specific needs, such
 * as parsing XML content from other namespaces than Atom. It generates an
 * entity based on the values discovered in the entry.
 * 
 * @author Thierry Boileau
 * @param <T>
 *            The type of the parsed entity.
 */
public class EntryContentHandler<T> extends EntryReader {

    /** The path of the current XML element relatively to an Entry. */
    List<String> eltPath;

    /** The entity targeted by this entry. */
    private T entity;

    /** The class of the entity targeted by this entry. */
    private Class<?> entityClass;

    /** The OData type of the parsed entity. */
    private EntityType entityType;

    /** Internal logger. */
    private Logger logger;

    /** The currently parsed OData mapping. */
    private Mapping mapping;

    /** The metadata of the WCF service. */
    private Metadata metadata;

    /** Are we parsing an entry content element? */
    private boolean parseContent;

    /** Are we parsing an entry? */
    private boolean parseEntry;

    /** Are we parsing an entity property? */
    private boolean parseProperty;

    /** Must the current be set to null? */
    private boolean parsePropertyNull;

    /** Gleans text content. */
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
    public EntryContentHandler(Class<?> entityClass, Metadata metadata,
            Logger logger) {
        super();
        this.entityClass = entityClass;
        this.metadata = metadata;
        entityType = metadata.getEntityType(entityClass);
        this.logger = logger;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (parseProperty || mapping != null) {
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
        if (parseProperty) {
            parseProperty = false;
            if (!parsePropertyNull) {
                Property property = metadata.getProperty(entity, localName);
                try {
                    ReflectUtils.setProperty(entity, property, sb.toString());
                } catch (Exception e) {
                    getLogger().warning(
                            "Cannot set " + localName + " property on "
                                    + entity + " with value " + sb.toString());

                }
            }
        } else if (mapping != null) {
            if (sb != null) {
                try {
                    ReflectUtils.invokeSetter(entity,
                            mapping.getPropertyPath(), sb.toString());
                } catch (Exception e) {
                    getLogger().warning(
                            "Cannot set the mapped property "
                                    + mapping.getPropertyPath() + " on "
                                    + entity + " with value " + sb.toString());

                }
            }
            mapping = null;
        }

        if (!eltPath.isEmpty()) {
            eltPath.remove(eltPath.size() - 1);
        }
    }

    @Override
    public void endEntry(Entry entry) {
        parseEntry = false;

        // Handle Atom mapped values.
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
                            "Cannot set " + m.getPropertyPath()
                                    + " property on " + entity + " with value "
                                    + value);
                }
            }
        }
        
        // If the entity is a blob, get the edit reference
        if (entityType.isBlob()
                && entityType.getBlobValueEditRefProperty() != null) {
            // Look for en entry with a "edit-media" relation value.
            Link link = entry.getLink(new Relation("edit-media"));
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
    }

    public T getEntity() {
        return entity;
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
        if (parseContent) {
            if (Service.WCF_DATASERVICES_NAMESPACE.equals(uri)) {
                sb = new StringBuilder();
                parseProperty = true;
                parsePropertyNull = Boolean.parseBoolean(attrs.getValue(
                        Service.WCF_DATASERVICES_METADATA_NAMESPACE, "null"));
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
        } else if (parseEntry) {
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
                        String value = attrs
                                .getValue(m.getValueAttributeName());
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

    @SuppressWarnings("unchecked")
    @Override
    public void startEntry(Entry entry) {
        parseEntry = true;
        eltPath = new ArrayList<String>();
        // Instantiate the entity
        try {
            entity = (T) entityClass.newInstance();
        } catch (Exception e) {
            getLogger().warning(
                    "Error when instantiating  class " + entityClass);
        }
    }

}
