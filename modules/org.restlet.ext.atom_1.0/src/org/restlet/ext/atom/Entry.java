/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.restlet.util.DateUtils;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Represents an individual entry, acting as a component for metadata and data
 * associated with the entry.
 * 
 * @author Jerome Louvel
 */
public class Entry {

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

        writer.endElement(ATOM_NAMESPACE, "entry");
    }

}
