/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.atom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.resource.Representation;
import org.restlet.resource.SaxRepresentation;
import org.restlet.resource.StringRepresentation;
import org.restlet.util.DateUtils;
import org.restlet.util.XmlWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Atom Feed Document, acting as a component for metadata and data associated
 * with the feed.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Feed extends SaxRepresentation {
    /** Atom Syndication Format namespace. */
    public final static String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

    /** XHTML namespace. */
    public final static String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

    /** The authors of the feed. */
    private List<Person> authors;

    /** The categories associated with the feed. */
    private List<Category> categories;

    /** The contributors to the feed. */
    private List<Person> contributors;

    /** The agent used to generate a feed. */
    private Generator generator;

    /** Image that provides iconic visual identification for a feed. */
    private Reference icon;

    /** Permanent, universally unique identifier for the feed. */
    private String id;

    /** The references from the entry to Web resources. */
    private List<Link> links;

    /** Image that provides visual identification for a feed. */
    private Reference logo;

    /** Information about rights held in and over an entry. */
    private Text rights;

    /** Short summary, abstract, or excerpt of an entry. */
    private Text subtitle;

    /** The human-readable title for the entry. */
    private Text title;

    /** Most recent moment when the entry was modified in a significant way. */
    private Date updated;

    /**
     * Individual entries, acting as a components for associated metadata and
     * data.
     */
    private List<Entry> entries;

    /**
     * Constructor.
     */
    public Feed() {
        super(MediaType.APPLICATION_ATOM_XML);
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
     * @param xmlFeed
     *            The XML feed document.
     * @throws IOException
     */
    public Feed(Representation xmlFeed) throws IOException {
        super(xmlFeed);
        parse(new ContentReader(this));
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
        // TODO
    }

    /**
     * Returns the authors of the entry.
     * 
     * @return The authors of the entry.
     */
    public List<Person> getAuthors() {
        if (this.authors == null)
            this.authors = new ArrayList<Person>();
        return this.authors;
    }

    /**
     * Returns the categories associated with the entry.
     * 
     * @return The categories associated with the entry.
     */
    public List<Category> getCategories() {
        if (this.categories == null)
            this.categories = new ArrayList<Category>();
        return this.categories;
    }

    /**
     * Returns the contributors to the entry.
     * 
     * @return The contributors to the entry.
     */
    public List<Person> getContributors() {
        if (this.contributors == null)
            this.contributors = new ArrayList<Person>();
        return this.contributors;
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
     * Sets the agent used to generate a feed.
     * 
     * @param generator
     *            The agent used to generate a feed.
     */
    public void setGenerator(Generator generator) {
        this.generator = generator;
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
     * Returns the permanent, universally unique identifier for the entry.
     * 
     * @return The permanent, universally unique identifier for the entry.
     */
    public String getId() {
        return this.id;
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
     * Returns the references from the entry to Web resources.
     * 
     * @return The references from the entry to Web resources.
     */
    public List<Link> getLinks() {
        if (this.links == null)
            this.links = new ArrayList<Link>();
        return this.links;
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
     * Sets the image that provides visual identification for a feed.
     * 
     * @param logo
     *            The image that provides visual identification for a feed.
     */
    public void setLogo(Reference logo) {
        this.logo = logo;
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
     * Sets the information about rights held in and over an entry.
     * 
     * @param rights
     *            The information about rights held in and over an entry.
     */
    public void setRights(Text rights) {
        this.rights = rights;
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
     * Sets the short summary, abstract, or excerpt of an entry.
     * 
     * @param subtitle
     *            The short summary, abstract, or excerpt of an entry.
     */
    public void setSubtitle(Text subtitle) {
        this.subtitle = subtitle;
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
     * Sets the human-readable title for the entry.
     * 
     * @param title
     *            The human-readable title for the entry.
     */
    public void setTitle(Text title) {
        this.title = title;
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
     * Returns the individual entries, acting as a components for associated
     * metadata and data.
     * 
     * @return The individual entries, acting as a components for associated
     *         metadata and data.
     */
    public List<Entry> getEntries() {
        if (this.entries == null)
            this.entries = new ArrayList<Entry>();
        return this.entries;
    }

    // -------------------
    // Content reader part
    // -------------------
    private static class ContentReader extends DefaultHandler {
        public enum State {
            NONE, FEED, FEED_AUTHOR, FEED_AUTHOR_NAME, FEED_AUTHOR_URI, FEED_AUTHOR_EMAIL, FEED_CATEGORY, FEED_CONTRIBUTOR, FEED_CONTRIBUTOR_NAME, FEED_CONTRIBUTOR_URI, FEED_CONTRIBUTOR_EMAIL, FEED_GENERATOR, FEED_ICON, FEED_ID, FEED_LINK, FEED_LOGO, FEED_RIGHTS, FEED_SUBTITLE, FEED_TITLE, FEED_UPDATED, FEED_ENTRY, FEED_ENTRY_AUTHOR, FEED_ENTRY_AUTHOR_NAME, FEED_ENTRY_AUTHOR_URI, FEED_ENTRY_AUTHOR_EMAIL, FEED_ENTRY_CATEGORY, FEED_ENTRY_CONTENT, FEED_ENTRY_CONTRIBUTOR, FEED_ENTRY_ID, FEED_ENTRY_LINK, FEED_ENTRY_PUBLISHED, FEED_ENTRY_RIGHTS, FEED_ENTRY_SOURCE, FEED_ENTRY_SOURCE_AUTHOR, FEED_ENTRY_SOURCE_AUTHOR_NAME, FEED_ENTRY_SOURCE_AUTHOR_URI, FEED_ENTRY_SOURCE_AUTHOR_EMAIL, FEED_ENTRY_SOURCE_CATEGORY, FEED_ENTRY_SOURCE_CONTRIBUTOR, FEED_ENTRY_SOURCE_GENERATOR, FEED_ENTRY_SOURCE_ICON, FEED_ENTRY_SOURCE_ID, FEED_ENTRY_SOURCE_LINK, FEED_ENTRY_SOURCE_LOGO, FEED_ENTRY_SOURCE_RIGHTS, FEED_ENTRY_SOURCE_SUBTITLE, FEED_ENTRY_SOURCE_TITLE, FEED_ENTRY_SOURCE_UPDATED, FEED_ENTRY_SUMMARY, FEED_ENTRY_TITLE, FEED_ENTRY_UPDATED
        }

        private State state;

        private Feed currentFeed;

        private Entry currentEntry;

        private Text currentText;

        private Date currentDate;

        private Link currentLink;

        private Person currentPerson;

        private Category currentCategory;

        private StringBuilder contentBuffer;

        public ContentReader(Feed feed) {
            this.state = State.NONE;
            this.currentFeed = feed;
            this.currentEntry = null;
            this.currentText = null;
            this.currentDate = null;
            this.currentLink = null;
            this.currentPerson = null;
            this.contentBuffer = null;
            this.currentCategory = null;
        }

        /**
         * Receive notification of the beginning of a document.
         */
        @Override
        public void startDocument() throws SAXException {
            this.contentBuffer = new StringBuilder();
        }

        /**
         * Receive notification of the beginning of an element.
         * 
         * @param uri
         *            The Namespace URI, or the empty string if the element has
         *            no Namespace URI or if Namespace processing is not being
         *            performed.
         * @param localName
         *            The local name (without prefix), or the empty string if
         *            Namespace processing is not being performed.
         * @param qName
         *            The qualified name (with prefix), or the empty string if
         *            qualified names are not available.
         * @param attrs
         *            The attributes attached to the element. If there are no
         *            attributes, it shall be an empty Attributes object. The
         *            value of this object after startElement returns is
         *            undefined.
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attrs) throws SAXException {
            this.contentBuffer.delete(0, this.contentBuffer.length() + 1);

            if (uri.equalsIgnoreCase(ATOM_NAMESPACE)) {
                if (localName.equals("feed")) {
                    state = State.FEED;
                } else if (localName.equals("title")) {
                    startTextElement(attrs);

                    if (state == State.FEED) {
                        state = State.FEED_TITLE;
                    } else if (state == State.FEED_ENTRY) {
                        state = State.FEED_ENTRY_TITLE;
                    } else if (state == State.FEED_ENTRY_SOURCE) {
                        state = State.FEED_ENTRY_SOURCE_TITLE;
                    }
                } else if (localName.equals("updated")) {
                    currentDate = new Date();

                    if (state == State.FEED) {
                        state = State.FEED_UPDATED;
                    } else if (state == State.FEED_ENTRY) {
                        state = State.FEED_ENTRY_UPDATED;
                    } else if (state == State.FEED_ENTRY_SOURCE) {
                        state = State.FEED_ENTRY_SOURCE_UPDATED;
                    }
                } else if (localName.equals("author")) {
                    currentPerson = new Person();

                    if (state == State.FEED) {
                        state = State.FEED_AUTHOR;
                    } else if (state == State.FEED_ENTRY) {
                        state = State.FEED_ENTRY_AUTHOR;
                    } else if (state == State.FEED_ENTRY_SOURCE) {
                        state = State.FEED_ENTRY_SOURCE_AUTHOR;
                    }
                } else if (localName.equals("name")) {
                    if (state == State.FEED_AUTHOR) {
                        state = State.FEED_AUTHOR_NAME;
                    } else if (state == State.FEED_ENTRY_AUTHOR) {
                        state = State.FEED_ENTRY_AUTHOR_NAME;
                    } else if (state == State.FEED_ENTRY_SOURCE_AUTHOR) {
                        state = State.FEED_ENTRY_SOURCE_AUTHOR_NAME;
                    }
                } else if (localName.equals("id")) {
                    if (state == State.FEED) {
                        state = State.FEED_ID;
                    } else if (state == State.FEED_ENTRY) {
                        state = State.FEED_ENTRY_ID;
                    } else if (state == State.FEED_ENTRY_SOURCE) {
                        state = State.FEED_ENTRY_SOURCE_ID;
                    }
                } else if (localName.equals("link")) {
                    currentLink = new Link();
                    currentLink.setHref(new Reference(attrs
                            .getValue("", "href")));
                    currentLink.setRel(Relation
                            .parse(attrs.getValue("", "rel")));
                    currentLink.setType(new MediaType(attrs
                            .getValue("", "type")));
                    currentLink.setHrefLang(new Language(attrs.getValue("",
                            "hreflang")));
                    currentLink.setTitle(attrs.getValue("", "title"));
                    String attr = attrs.getValue("", "length");
                    currentLink.setLength((attr == null) ? -1L : Long
                            .parseLong(attr));

                    if (state == State.FEED) {
                        state = State.FEED_LINK;
                    } else if (state == State.FEED_ENTRY) {
                        state = State.FEED_ENTRY_LINK;
                    } else if (state == State.FEED_ENTRY_SOURCE) {
                        state = State.FEED_ENTRY_SOURCE_LINK;
                    }
                } else if (localName.equalsIgnoreCase("entry")) {
                    if (state == State.FEED) {
                        currentEntry = new Entry();
                        state = State.FEED_ENTRY;
                    }
                } else if (localName.equals("category")) {
                    currentCategory = new Category();
                    currentCategory.setTerm(attrs.getValue("", "term"));
                    currentCategory.setScheme(new Reference(attrs.getValue("",
                            "scheme")));
                    currentCategory.setLabel(attrs.getValue("", "label"));

                    if (state == State.FEED) {
                        state = State.FEED_CATEGORY;
                    } else if (state == State.FEED_ENTRY) {
                        state = State.FEED_ENTRY_CATEGORY;
                    } else if (state == State.FEED_ENTRY_SOURCE) {
                        state = State.FEED_ENTRY_SOURCE_CATEGORY;
                    }
                } else if (localName.equalsIgnoreCase("content")) {
                    if (state == State.FEED_ENTRY) {
                        MediaType type = getMediaType(attrs
                                .getValue("", "type"));
                        String srcAttr = attrs.getValue("", "src");
                        Content currentContent = new Content();

                        if (srcAttr == null) {
                            // Content available inline
                            currentContent
                                    .setInlineContent(new StringRepresentation(
                                            null, type));
                        } else {
                            // Content available externally
                            currentContent
                                    .setExternalRef(new Reference(srcAttr));
                            currentContent.setExternalType(type);
                        }

                        currentEntry.setContent(currentContent);
                        state = State.FEED_ENTRY_CONTENT;
                    }
                }
            }
        }

        /**
         * Receive notification of the beginning of a text element.
         * 
         * @param attrs
         *            The attributes attached to the element.
         */
        public void startTextElement(Attributes attrs) {
            currentText = new Text(getMediaType(attrs.getValue("", "type")));
        }

        /**
         * Returns a media type from an Atom type attribute.
         * 
         * @param type
         *            The Atom type attribute.
         * @return The media type.
         */
        private MediaType getMediaType(String type) {
            MediaType result = null;

            if (type == null) {
                // No type defined
            } else if (type.equals("text")) {
                result = MediaType.TEXT_PLAIN;
            } else if (type.equals("html")) {
                result = MediaType.TEXT_HTML;
            } else if (type.equals("xhtml")) {
                result = MediaType.APPLICATION_XHTML_XML;
            } else {
                result = new MediaType(type);
            }

            return result;
        }

        /**
         * Receive notification of character data.
         * 
         * @param ch
         *            The characters from the XML document.
         * @param start
         *            The start position in the array.
         * @param length
         *            The number of characters to read from the array.
         */
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            contentBuffer.append(ch, start, length);
        }

        /**
         * Receive notification of the end of an element.
         * 
         * @param uri
         *            The Namespace URI, or the empty string if the element has
         *            no Namespace URI or if Namespace processing is not being
         *            performed.
         * @param localName
         *            The local name (without prefix), or the empty string if
         *            Namespace processing is not being performed.
         * @param qName
         *            The qualified XML name (with prefix), or the empty string
         *            if qualified names are not available.
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (currentText != null) {
                currentText.setContent(contentBuffer.toString());
            }

            if (currentDate != null) {
                String formattedDate = contentBuffer.toString();
                Date parsedDate = DateUtils.parse(formattedDate,
                        DateUtils.FORMAT_RFC_3339);

                if (parsedDate != null) {
                    currentDate.setTime(parsedDate.getTime());
                } else {
                    currentDate = null;
                }
            }

            if (uri.equalsIgnoreCase(ATOM_NAMESPACE)) {
                if (localName.equals("feed")) {
                    state = State.NONE;
                } else if (localName.equals("title")) {
                    if (state == State.FEED_TITLE) {
                        currentFeed.setTitle(currentText);
                        state = State.FEED;
                    } else if (state == State.FEED_ENTRY_TITLE) {
                        currentEntry.setTitle(currentText);
                        state = State.FEED_ENTRY;
                    } else if (state == State.FEED_ENTRY_SOURCE_TITLE) {
                        currentEntry.getSource().setTitle(currentText);
                        state = State.FEED_ENTRY_SOURCE;
                    }
                } else if (localName.equals("updated")) {
                    if (state == State.FEED_UPDATED) {
                        currentFeed.setUpdated(currentDate);
                        state = State.FEED;
                    } else if (state == State.FEED_ENTRY_UPDATED) {
                        currentEntry.setUpdated(currentDate);
                        state = State.FEED_ENTRY;
                    } else if (state == State.FEED_ENTRY_SOURCE_UPDATED) {
                        currentEntry.getSource().setUpdated(currentDate);
                        state = State.FEED_ENTRY_SOURCE;
                    }
                } else if (localName.equals("author")) {
                    if (state == State.FEED_AUTHOR) {
                        currentFeed.getAuthors().add(currentPerson);
                        state = State.FEED;
                    } else if (state == State.FEED_ENTRY_AUTHOR) {
                        currentEntry.getAuthors().add(currentPerson);
                        state = State.FEED_ENTRY;
                    } else if (state == State.FEED_ENTRY_SOURCE_AUTHOR) {
                        currentEntry.getSource().getAuthors()
                                .add(currentPerson);
                        state = State.FEED_ENTRY_SOURCE;
                    }
                } else if (localName.equals("name")) {
                    currentPerson.setName(contentBuffer.toString());

                    if (state == State.FEED_AUTHOR_NAME) {
                        state = State.FEED_AUTHOR;
                    } else if (state == State.FEED_ENTRY_AUTHOR_NAME) {
                        state = State.FEED_ENTRY_AUTHOR;
                    } else if (state == State.FEED_ENTRY_SOURCE_AUTHOR_NAME) {
                        state = State.FEED_ENTRY_SOURCE_AUTHOR;
                    }
                } else if (localName.equals("id")) {
                    if (state == State.FEED_ID) {
                        currentFeed.setId(contentBuffer.toString());
                        state = State.FEED;
                    } else if (state == State.FEED_ENTRY_ID) {
                        currentEntry.setId(contentBuffer.toString());
                        state = State.FEED_ENTRY;
                    } else if (state == State.FEED_ENTRY_SOURCE_ID) {
                        currentEntry.getSource()
                                .setId(contentBuffer.toString());
                        state = State.FEED_ENTRY_SOURCE;
                    }
                } else if (localName.equals("link")) {
                    if (state == State.FEED_LINK) {
                        currentFeed.getLinks().add(currentLink);
                        state = State.FEED;
                    } else if (state == State.FEED_ENTRY_LINK) {
                        currentEntry.getLinks().add(currentLink);
                        state = State.FEED_ENTRY;
                    } else if (state == State.FEED_ENTRY_SOURCE_LINK) {
                        currentEntry.getSource().getLinks().add(currentLink);
                        state = State.FEED_ENTRY_SOURCE;
                    }
                } else if (localName.equalsIgnoreCase("entry")) {
                    if (state == State.FEED_ENTRY) {
                        currentFeed.getEntries().add(currentEntry);
                        state = State.FEED;
                    }
                } else if (localName.equals("category")) {
                    if (state == State.FEED_CATEGORY) {
                        currentFeed.getCategories().add(currentCategory);
                        state = State.FEED;
                    } else if (state == State.FEED_ENTRY_CATEGORY) {
                        currentEntry.getCategories().add(currentCategory);
                        state = State.FEED_ENTRY;
                    } else if (state == State.FEED_ENTRY_SOURCE_CATEGORY) {
                        currentEntry.getSource().getCategories().add(
                                currentCategory);
                        state = State.FEED_ENTRY_SOURCE;
                    }
                } else if (localName.equalsIgnoreCase("content")) {
                    if (state == State.FEED_ENTRY_CONTENT) {
                        if (currentEntry.getContent().isInline()) {
                            StringRepresentation sr = (StringRepresentation) currentEntry
                                    .getContent().getInlineContent();
                            sr.setText(contentBuffer.toString());
                        }

                        state = State.FEED_ENTRY;
                    }
                }
            }

            currentText = null;
            currentDate = null;
        }

        /**
         * Receive notification of the end of a document.
         */
        @Override
        public void endDocument() throws SAXException {
            this.state = State.NONE;
            this.currentEntry = null;
            this.contentBuffer = null;
        }
    }

}
