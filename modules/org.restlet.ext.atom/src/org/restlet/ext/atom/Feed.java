/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.atom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.atom.internal.FeedContentReader;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Representation;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Atom Feed Document, acting as a component for metadata and data associated
 * with the feed.
 * 
 * @author Jerome Louvel
 */
public class Feed extends SaxRepresentation {
    /** Atom Syndication Format namespace. */
    public final static String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

    /** XHTML namespace. */
    public final static String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

    /** The authors of the feed. */
    private volatile List<Person> authors;

    /**
     * The base reference used to resolve relative references found within the
     * scope of the xml:base attribute.
     */
    private volatile Reference baseReference;

    /** The categories associated with the feed. */
    private volatile List<Category> categories;

    /** The contributors to the feed. */
    private volatile List<Person> contributors;

    /**
     * Individual entries, acting as a components for associated metadata and
     * data.
     */
    private List<Entry> entries;

    /** The agent used to generate a feed. */
    private volatile Generator generator;

    /** Image that provides iconic visual identification for a feed. */
    private volatile Reference icon;

    /** Permanent, universally unique identifier for the feed. */
    private volatile String id;

    /** The references from the entry to Web resources. */
    private volatile List<Link> links;

    /** Image that provides visual identification for a feed. */
    private volatile Reference logo;

    /** Information about rights held in and over an entry. */
    private volatile Text rights;

    /** Short summary, abstract, or excerpt of an entry. */
    private volatile Text subtitle;

    /** The human-readable title for the entry. */
    private volatile Text title;

    /** Most recent moment when the entry was modified in a significant way. */
    private volatile Date updated;

    /**
     * Constructor.
     */
    public Feed() {
        super(MediaType.APPLICATION_ATOM);
        this.authors = null;
        this.categories = null;
        this.contributors = null;
        this.generator = null;
        this.icon = null;
        this.id = null;
        this.links = null;
        this.logo = null;
        this.rights = null;
        this.subtitle = null;
        this.title = null;
        this.updated = null;
        this.entries = null;
    }

    /**
     * Constructor.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     * @param feedUri
     *            The feed URI.
     * @throws IOException
     */
    public Feed(Client clientDispatcher, String feedUri) throws IOException {
        this(clientDispatcher.handle(new Request(Method.GET, feedUri))
                .getEntity());
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context from which the client dispatcher will be
     *            retrieved.
     * @param feedUri
     *            The feed URI.
     * @throws IOException
     */
    public Feed(Context context, String feedUri) throws IOException {
        this(context.getClientDispatcher().handle(
                new Request(Method.GET, feedUri)).getEntity());
    }

    /**
     * Constructor.
     * 
     * @param xmlFeed
     *            The XML feed document.
     * @throws IOException
     */
    public Feed(Representation xmlFeed) throws IOException {
        super(xmlFeed);
        parse(new FeedContentReader(this));
    }

    /**
     * Constructor.
     * 
     * @param xmlFeed
     *            The XML feed document.
     * @param feedReader
     *            Custom feed reader.
     * @throws IOException
     */
    public Feed(Representation xmlFeed, FeedReader feedReader)
            throws IOException {
        super(xmlFeed);
        parse(new FeedContentReader(this, feedReader));
    }

    /**
     * Constructor.
     * 
     * @param feedUri
     *            The feed URI.
     * @throws IOException
     */
    public Feed(String feedUri) throws IOException {
        this(new Client(new Reference(feedUri).getSchemeProtocol()), feedUri);
    }

    /**
     * Returns the authors of the entry.
     * 
     * @return The authors of the entry.
     */
    public List<Person> getAuthors() {
        // Lazy initialization with double-check.
        List<Person> a = this.authors;
        if (a == null) {
            synchronized (this) {
                a = this.authors;
                if (a == null) {
                    this.authors = a = new ArrayList<Person>();
                }
            }
        }
        return a;
    }

    /**
     * Returns the base reference used to resolve relative references found
     * within the scope of the xml:base attribute.
     * 
     * @return The base reference used to resolve relative references found
     *         within the scope of the xml:base attribute.
     */
    public Reference getBaseReference() {
        return baseReference;
    }

    /**
     * Returns the categories associated with the entry.
     * 
     * @return The categories associated with the entry.
     */
    public List<Category> getCategories() {
        // Lazy initialization with double-check.
        List<Category> c = this.categories;
        if (c == null) {
            synchronized (this) {
                c = this.categories;
                if (c == null) {
                    this.categories = c = new ArrayList<Category>();
                }
            }
        }
        return c;
    }

    /**
     * Returns the contributors to the entry.
     * 
     * @return The contributors to the entry.
     */
    public List<Person> getContributors() {
        // Lazy initialization with double-check.
        List<Person> c = this.contributors;
        if (c == null) {
            synchronized (this) {
                c = this.contributors;
                if (c == null) {
                    this.contributors = c = new ArrayList<Person>();
                }
            }
        }
        return c;
    }

    /**
     * Returns the individual entries, acting as a components for associated
     * metadata and data.
     * 
     * @return The individual entries, acting as a components for associated
     *         metadata and data.
     */
    public List<Entry> getEntries() {
        // Lazy initialization with double-check.
        List<Entry> e = this.entries;
        if (e == null) {
            synchronized (this) {
                e = this.entries;
                if (e == null) {
                    this.entries = e = new ArrayList<Entry>();
                }
            }
        }
        return e;
    }

    /**
     * Returns the agent used to generate a feed.
     * 
     * @return The agent used to generate a feed.
     */
    public Generator getGenerator() {
        return this.generator;
    }

    /**
     * Returns the image that provides iconic visual identification for a feed.
     * 
     * @return The image that provides iconic visual identification for a feed.
     */
    public Reference getIcon() {
        return this.icon;
    }

    /**
     * Returns the permanent, universally unique identifier for the entry.
     * 
     * @return The permanent, universally unique identifier for the entry.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the references from the entry to Web resources.
     * 
     * @return The references from the entry to Web resources.
     */
    public List<Link> getLinks() {
        // Lazy initialization with double-check.
        List<Link> l = this.links;
        if (l == null) {
            synchronized (this) {
                l = this.links;
                if (l == null) {
                    this.links = l = new ArrayList<Link>();
                }
            }
        }
        return l;
    }

    /**
     * Returns the image that provides visual identification for a feed.
     * 
     * @return The image that provides visual identification for a feed.
     */
    public Reference getLogo() {
        return this.logo;
    }

    /**
     * Returns the information about rights held in and over an entry.
     * 
     * @return The information about rights held in and over an entry.
     */
    public Text getRights() {
        return this.rights;
    }

    /**
     * Returns the short summary, abstract, or excerpt of an entry.
     * 
     * @return The short summary, abstract, or excerpt of an entry.
     */
    public Text getSubtitle() {
        return this.subtitle;
    }

    /**
     * Returns the human-readable title for the entry.
     * 
     * @return The human-readable title for the entry.
     */
    public Text getTitle() {
        return this.title;
    }

    /**
     * Returns the most recent moment when the entry was modified in a
     * significant way.
     * 
     * @return The most recent moment when the entry was modified in a
     *         significant way.
     */
    public Date getUpdated() {
        return this.updated;
    }

    /**
     * Sets the base reference used to resolve relative references found within
     * the scope of the xml:base attribute.
     * 
     * @param baseReference
     *            The base reference used to resolve relative references found
     *            within the scope of the xml:base attribute.
     */
    public void setBaseReference(Reference baseReference) {
        this.baseReference = baseReference;
    }

    /**
     * Sets the base URI used to resolve relative references found within the
     * scope of the xml:base attribute.
     * 
     * @param baseUri
     *            The base URI used to resolve relative references found within
     *            the scope of the xml:base attribute.
     */
    public void setBaseReference(String baseUri) {
        setBaseReference(new Reference(baseUri));
    }

    /**
     * Sets the agent used to generate a feed.
     * 
     * @param generator
     *            The agent used to generate a feed.
     */
    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    /**
     * Sets the image that provides iconic visual identification for a feed.
     * 
     * @param icon
     *            The image that provides iconic visual identification for a
     *            feed.
     */
    public void setIcon(Reference icon) {
        this.icon = icon;
    }

    /**
     * Sets the permanent, universally unique identifier for the entry.
     * 
     * @param id
     *            The permanent, universally unique identifier for the entry.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the image that provides visual identification for a feed.
     * 
     * @param logo
     *            The image that provides visual identification for a feed.
     */
    public void setLogo(Reference logo) {
        this.logo = logo;
    }

    /**
     * Sets the information about rights held in and over an entry.
     * 
     * @param rights
     *            The information about rights held in and over an entry.
     */
    public void setRights(Text rights) {
        this.rights = rights;
    }

    /**
     * Sets the short summary, abstract, or excerpt of an entry.
     * 
     * @param subtitle
     *            The short summary, abstract, or excerpt of an entry.
     */
    public void setSubtitle(Text subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Sets the human-readable title for the entry.
     * 
     * @param title
     *            The human-readable title for the entry.
     */
    public void setTitle(Text title) {
        this.title = title;
    }

    /**
     * Sets the most recent moment when the entry was modified in a significant
     * way.
     * 
     * @param updated
     *            The most recent moment when the entry was modified in a
     *            significant way.
     */
    public void setUpdated(Date updated) {
        this.updated = DateUtils.unmodifiable(updated);
    }

    /**
     * Writes the representation to a XML writer.
     * 
     * @param writer
     *            The XML writer to write to.
     * @throws IOException
     */
    @Override
    public void write(XmlWriter writer) throws IOException {
        try {
            writer.setPrefix(ATOM_NAMESPACE, "");
            writer.setDataFormat(true);
            writer.setIndentStep(3);
            writer.startDocument();
            writeElement(writer);
            writer.endDocument();
        } catch (SAXException e) {
            IOException ioe = new IOException(
                    "Unable to write the Atom feed document.");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        writer.startElement(ATOM_NAMESPACE, "feed");

        if (getAuthors() != null) {
            for (final Person person : getAuthors()) {
                person.writeElement(writer, "author");
            }
        }

        if (getCategories() != null) {
            for (final Category category : getCategories()) {
                category.writeElement(writer);
            }
        }
        if (getContributors() != null) {
            for (final Person person : getContributors()) {
                person.writeElement(writer, "contributor");
            }
        }

        if (getGenerator() != null) {
            getGenerator().writeElement(writer);
        }

        if (getIcon() != null) {
            writer.dataElement(ATOM_NAMESPACE, "icon", getIcon().toString());
        }

        if (getId() != null) {
            writer.dataElement(ATOM_NAMESPACE, "id", null,
                    new AttributesImpl(), getId());
        }

        if (getLinks() != null) {
            for (final Link link : getLinks()) {
                link.writeElement(writer);
            }
        }

        if ((getLogo() != null) && (getLogo().toString() != null)) {
            writer.dataElement(ATOM_NAMESPACE, "logo", getLogo().toString());
        }

        if (getRights() != null) {
            getRights().writeElement(writer, "rights");
        }

        if (getSubtitle() != null) {
            getSubtitle().writeElement(writer, "subtitle");
        }

        if (getTitle() != null) {
            getTitle().writeElement(writer, "title");
        }

        if (getUpdated() != null) {
            Text.writeElement(writer, getUpdated(), ATOM_NAMESPACE, "updated");
        }

        if (getEntries() != null) {
            for (final Entry entry : getEntries()) {
                entry.writeElement(writer);
            }
        }

        writer.endElement(ATOM_NAMESPACE, "feed");
    }

}
