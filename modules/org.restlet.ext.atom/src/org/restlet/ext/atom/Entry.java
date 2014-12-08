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

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.atom.internal.EntryContentReader;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Representation;
import org.xml.sax.SAXException;

/**
 * Represents an individual entry, acting as a component for metadata and data
 * associated with the entry.
 * 
 * @author Jerome Louvel
 */
public class Entry extends SaxRepresentation {

    /** The authors of the entry. */
    private volatile List<Person> authors;

    /** The categories associated with the entry. */
    private volatile List<Category> categories;

    /** Contains or links to the content of the entry. */
    private volatile Content content;

    /** The contributors to the entry. */
    private volatile List<Person> contributors;

    /** Permanent, universally unique identifier for the entry. */
    private volatile String id;

    /** The references from the entry to Web resources. */
    private volatile List<Link> links;

    /** Moment associated with an event early in the life cycle of the entry. */
    private volatile Date published;

    /** Information about rights held in and over an entry. */
    private volatile Text rights;

    /** Source feed's metadata if the entry was copied from another feed. */
    private volatile Source source;

    /** Short summary, abstract, or excerpt of the entry. */
    private volatile String summary;

    /** The human-readable title for the entry. */
    private volatile Text title;

    /** Most recent moment when the entry was modified in a significant way. */
    private volatile Date updated;

    /**
     * Constructor.
     */
    public Entry() {
        super(MediaType.APPLICATION_ATOM);
        setNamespaceAware(true);
        this.authors = null;
        this.categories = null;
        this.content = null;
        this.contributors = null;
        this.id = null;
        this.links = null;
        this.published = null;
        this.rights = null;
        this.source = null;
        this.summary = null;
        this.title = null;
        this.updated = null;
    }

    /**
     * Constructor.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     * @param entryUri
     *            The entry URI.
     * @throws IOException
     */
    public Entry(Client clientDispatcher, String entryUri) throws IOException {
        this(clientDispatcher.handle(new Request(Method.GET, entryUri))
                .getEntity());
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context from which the client dispatcher will be
     *            retrieved.
     * @param entryUri
     *            The entry URI.
     * @throws IOException
     */
    public Entry(Context context, String entryUri) throws IOException {
        this(context.getClientDispatcher()
                .handle(new Request(Method.GET, entryUri)).getEntity());
    }

    /**
     * Constructor.
     * 
     * @param xmlEntry
     *            The XML entry document.
     * @throws IOException
     */
    public Entry(Representation xmlEntry) throws IOException {
        super(xmlEntry);
        setNamespaceAware(true);
        parse(new EntryContentReader(this));
    }

    /**
     * Constructor.
     * 
     * @param xmlEntry
     *            The XML entry document.
     * @param entryReader
     *            Custom entry reader.
     * @throws IOException
     */
    public Entry(Representation xmlEntry, EntryReader entryReader)
            throws IOException {
        super(xmlEntry);
        setNamespaceAware(true);
        parse(new EntryContentReader(this, entryReader));
    }

    /**
     * Constructor.
     * 
     * @param entryUri
     *            The entry URI.
     * @throws IOException
     */
    public Entry(String entryUri) throws IOException {
        this(new Client(new Reference(entryUri).getSchemeProtocol()), entryUri);
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
     * Returns the content of the entry or links to it.
     * 
     * @return The content of the entry or links to it.
     */
    public Content getContent() {
        return this.content;
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
     * Returns the permanent, universally unique identifier for the entry.
     * 
     * @return The permanent, universally unique identifier for the entry.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the first available link with a given relation type.
     * 
     * @param rel
     *            The relation type to match.
     * @return The first available link with a given relation type.
     */
    public Link getLink(Relation rel) {
        Link result = null;
        Link current = null;

        for (final Iterator<Link> iter = getLinks().iterator(); (result == null)
                && iter.hasNext();) {
            current = iter.next();

            if (current.getRel() == rel) {
                result = current;
            }
        }

        return result;
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
     * Returns the moment associated with an event early in the life cycle of
     * the entry.
     * 
     * @return The moment associated with an event early in the life cycle of
     *         the entry.
     */
    public Date getPublished() {
        return this.published;
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
     * Returns the source feed's metadata if the entry was copied from another
     * feed.
     * 
     * @return The source feed's metadata if the entry was copied from another
     *         feed.
     */
    public Source getSource() {
        return this.source;
    }

    /**
     * Returns the short summary, abstract, or excerpt of the entry.
     * 
     * @return The short summary, abstract, or excerpt of the entry.
     */
    public String getSummary() {
        return this.summary;
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
     * Sets the content of the entry or links to it.
     * 
     * @param content
     *            The content of the entry or links to it.
     */
    public void setContent(Content content) {
        this.content = content;
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
     * Sets the moment associated with an event early in the life cycle of the
     * entry.
     * 
     * @param published
     *            The moment associated with an event early in the life cycle of
     *            the entry.
     */
    public void setPublished(Date published) {
        this.published = DateUtils.unmodifiable(published);
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
     * Sets the source feed's metadata if the entry was copied from another
     * feed.
     * 
     * @param source
     *            The source feed's metadata if the entry was copied from
     *            another feed.
     */
    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * Sets the short summary, abstract, or excerpt of the entry.
     * 
     * @param summary
     *            The short summary, abstract, or excerpt of the entry.
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Sets the human-readable title for the entry.
     * 
     * @param title
     *            The human-readable title for the entry.
     */
    public void setTitle(String title) {
        setTitle(new Text(title));
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
                    "Unable to write the Atom entry document.");
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
        writer.startElement(ATOM_NAMESPACE, "entry");

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

        if (getContent() != null) {
            getContent().writeElement(writer);
        }

        if (getContributors() != null) {
            for (final Person person : getContributors()) {
                person.writeElement(writer, "contributor");
            }
        }

        if (getId() != null) {
            writer.dataElement(ATOM_NAMESPACE, "id", getId());
        }

        if (getLinks() != null) {
            for (final Link link : getLinks()) {
                link.writeElement(writer);
            }
        }
        if (getPublished() != null) {
            Text.writeElement(writer, getPublished(), ATOM_NAMESPACE,
                    "published");
        }

        if (getRights() != null) {
            getRights().writeElement(writer, "rights");
        }

        if (getSource() != null) {
            getSource().writeElement(writer);
        }

        if (getSummary() != null) {
            writer.dataElement(ATOM_NAMESPACE, "summary", getSummary());
        }

        if (getTitle() != null) {
            getTitle().writeElement(writer, "title");
        }

        if (getUpdated() != null) {
            Text.writeElement(writer, getUpdated(), ATOM_NAMESPACE, "updated");
        }

        writeInlineContent(writer);

        writer.endElement(ATOM_NAMESPACE, "entry");
    }

    /**
     * Allow to write extra content inside the entry. The default implementation
     * does nothing and is intended to be overridden.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeInlineContent(XmlWriter writer) throws SAXException {
        // Do nothing by default.
    }
}
