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

package org.restlet.ext.atom.internal;

import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.atom.Category;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.EntryReader;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Person;
import org.restlet.ext.atom.Relation;
import org.restlet.ext.atom.Text;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.StringRepresentation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Content reader for entries.
 * 
 * @author Jerome Louvel
 */
public class EntryContentReader extends EntryReader {
    private enum State {
        FEED_ENTRY, FEED_ENTRY_AUTHOR, FEED_ENTRY_AUTHOR_EMAIL, FEED_ENTRY_AUTHOR_NAME, FEED_ENTRY_AUTHOR_URI, FEED_ENTRY_CATEGORY, FEED_ENTRY_CONTENT, FEED_ENTRY_CONTRIBUTOR, FEED_ENTRY_ID, FEED_ENTRY_LINK, FEED_ENTRY_PUBLISHED, FEED_ENTRY_RIGHTS, FEED_ENTRY_SOURCE, FEED_ENTRY_SOURCE_AUTHOR, FEED_ENTRY_SOURCE_AUTHOR_EMAIL, FEED_ENTRY_SOURCE_AUTHOR_NAME, FEED_ENTRY_SOURCE_AUTHOR_URI, FEED_ENTRY_SOURCE_CATEGORY, FEED_ENTRY_SOURCE_CONTRIBUTOR, FEED_ENTRY_SOURCE_GENERATOR, FEED_ENTRY_SOURCE_ICON, FEED_ENTRY_SOURCE_ID, FEED_ENTRY_SOURCE_LINK, FEED_ENTRY_SOURCE_LOGO, FEED_ENTRY_SOURCE_RIGHTS, FEED_ENTRY_SOURCE_SUBTITLE, FEED_ENTRY_SOURCE_TITLE, FEED_ENTRY_SOURCE_UPDATED, FEED_ENTRY_SUMMARY, FEED_ENTRY_TITLE, FEED_ENTRY_UPDATED, NONE
    }

    /** Buffer for the current text content of the current tag. */
    private StringBuilder contentBuffer;

    /** Mark the Content depth. */
    private int contentDepth;

    /** The media type of the Content (for inline cases). */
    private MediaType contentType;

    /** The currently parsed Category. */
    private Category currentCategory;

    /** The currently parsed Content. */
    private Content currentContent;

    /** The currently parsed XML content writer. */
    private XmlWriter currentContentWriter;

    /** The currently date parsed from the current text content. */
    private Date currentDate;

    /** The currently parsed Entry. */
    private Entry currentEntry;

    /** The currently parsed Link. */
    private Link currentLink;

    /** The currently parsed Person. */
    private Person currentPerson;

    /** The currently parsed Text. */
    private Text currentText;

    /** The current list of prefix mappings. */
    private Map<String, String> prefixMappings;

    /** The currently state. */
    private EntryContentReader.State state;

    /**
     * Constructor.
     * 
     * @param entry
     *            The entry object to update during the parsing.
     */
    public EntryContentReader(Entry entry) {
        this(entry, null);
    }

    /**
     * Constructor.
     * 
     * @param entry
     *            The entry object to update during the parsing.
     * @param extraEntryHandler
     *            Custom handler of all events.
     */
    public EntryContentReader(Entry entry, EntryReader extraEntryHandler) {
        super(extraEntryHandler);
        this.state = State.NONE;
        this.contentDepth = -1;
        this.currentEntry = entry;
        this.currentText = null;
        this.currentDate = null;
        this.currentLink = null;
        this.currentPerson = null;
        this.contentBuffer = null;
        this.currentCategory = null;
        this.currentContent = null;
        this.prefixMappings = new ConcurrentHashMap<String, String>();
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (this.contentDepth >= 0) {
            // The content might embed XML elements from various namespaces
            if (this.currentContentWriter != null) {
                this.currentContentWriter.characters(ch, start, length);
            }
        } else {
            this.contentBuffer.append(ch, start, length);
        }

        super.characters(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        this.state = State.NONE;
        this.contentBuffer = null;

        super.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (this.currentText != null) {
            this.currentText.setContent(this.contentBuffer.toString());
        }

        if (this.currentDate != null) {
            final String formattedDate = this.contentBuffer.toString();
            final Date parsedDate = DateUtils.parse(formattedDate.trim(),
                    DateUtils.FORMAT_RFC_3339);

            if (parsedDate != null) {
                this.currentDate.setTime(parsedDate.getTime());
            } else {
                this.currentDate = null;
            }
        }

        if (contentDepth > 0) {
            // The content might embed XML elements from various namespaces
            if (this.currentContentWriter != null) {
                this.currentContentWriter.endElement(uri, localName, qName);
            }
            contentDepth--;
        } else if (uri.equalsIgnoreCase(Feed.ATOM_NAMESPACE)) {
            if (localName.equals("feed")) {
                this.state = State.NONE;
            } else if (localName.equals("title")) {
                if (this.state == State.FEED_ENTRY_TITLE) {
                    this.currentEntry.setTitle(this.currentText);
                    this.state = State.FEED_ENTRY;
                } else if (this.state == State.FEED_ENTRY_SOURCE_TITLE) {
                    this.currentEntry.getSource().setTitle(this.currentText);
                    this.state = State.FEED_ENTRY_SOURCE;
                }
            } else if (localName.equals("updated")) {
                if (this.state == State.FEED_ENTRY_UPDATED) {
                    this.currentEntry.setUpdated(this.currentDate);
                    this.state = State.FEED_ENTRY;
                } else if (this.state == State.FEED_ENTRY_SOURCE_UPDATED) {
                    this.currentEntry.getSource().setUpdated(this.currentDate);
                    this.state = State.FEED_ENTRY_SOURCE;
                }
            } else if (localName.equals("published")) {
                if (this.state == State.FEED_ENTRY_PUBLISHED) {
                    this.currentEntry.setPublished(this.currentDate);
                    this.state = State.FEED_ENTRY;
                }
            } else if (localName.equals("author")) {
                if (this.state == State.FEED_ENTRY_AUTHOR) {
                    this.currentEntry.getAuthors().add(this.currentPerson);
                    this.state = State.FEED_ENTRY;
                } else if (this.state == State.FEED_ENTRY_SOURCE_AUTHOR) {
                    this.currentEntry.getSource().getAuthors()
                            .add(this.currentPerson);
                    this.state = State.FEED_ENTRY_SOURCE;
                }
            } else if (localName.equals("name")) {
                this.currentPerson.setName(this.contentBuffer.toString());

                if (this.state == State.FEED_ENTRY_AUTHOR_NAME) {
                    this.state = State.FEED_ENTRY_AUTHOR;
                } else if (this.state == State.FEED_ENTRY_SOURCE_AUTHOR_NAME) {
                    this.state = State.FEED_ENTRY_SOURCE_AUTHOR;
                }
            } else if (localName.equals("id")) {
                if (this.state == State.FEED_ENTRY_ID) {
                    this.currentEntry.setId(this.contentBuffer.toString());
                    this.state = State.FEED_ENTRY;
                } else if (this.state == State.FEED_ENTRY_SOURCE_ID) {
                    this.currentEntry.getSource().setId(
                            this.contentBuffer.toString());
                    this.state = State.FEED_ENTRY_SOURCE;
                }
            } else if (localName.equals("link")) {
                if (this.state == State.FEED_ENTRY_LINK) {
                    this.currentEntry.getLinks().add(this.currentLink);
                    this.state = State.FEED_ENTRY;
                } else if (this.state == State.FEED_ENTRY_SOURCE_LINK) {
                    this.currentEntry.getSource().getLinks()
                            .add(this.currentLink);
                    this.state = State.FEED_ENTRY_SOURCE;
                }
                // Set the inline content, if any
                if (this.currentContentWriter != null) {
                    String content = this.currentContentWriter.getWriter()
                            .toString().trim();
                    contentDepth = -1;

                    if ("".equals(content)) {
                        this.currentLink.setContent(null);
                    } else {
                        if (this.currentLink.getType() != null) {
                            currentContent
                                    .setInlineContent(new StringRepresentation(
                                            content, this.currentLink.getType()));
                        } else {
                            currentContent
                                    .setInlineContent(new StringRepresentation(
                                            content, contentType));
                        }
                    }
                    this.currentContentWriter = null;
                }
                endLink(this.currentLink);
            } else if (localName.equalsIgnoreCase("entry")) {
                this.state = State.NONE;
                endEntry(this.currentEntry);
            } else if (localName.equals("category")) {
                if (this.state == State.FEED_ENTRY_CATEGORY) {
                    this.currentEntry.getCategories().add(this.currentCategory);
                    this.state = State.FEED_ENTRY;
                } else if (this.state == State.FEED_ENTRY_SOURCE_CATEGORY) {
                    this.currentEntry.getSource().getCategories()
                            .add(this.currentCategory);
                    this.state = State.FEED_ENTRY_SOURCE;
                }
            } else if (localName.equalsIgnoreCase("content")) {
                if (this.state == State.FEED_ENTRY_CONTENT) {
                    if (!this.currentEntry.getContent().isExternal()) {
                        String content = this.currentContentWriter.getWriter()
                                .toString().trim();
                        contentDepth = -1;

                        if ("".equals(content)) {
                            this.currentEntry.setContent(null);
                        } else {
                            currentContent
                                    .setInlineContent(new StringRepresentation(
                                            content));
                        }
                    }

                    this.state = State.FEED_ENTRY;
                }

                this.currentContentWriter = null;
                endContent(this.currentContent);
            }
        }

        this.currentText = null;
        this.currentDate = null;
        super.endElement(uri, localName, qName);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.prefixMappings.remove(prefix);
        super.endPrefixMapping(prefix);
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
            result = MediaType.APPLICATION_XHTML;
        } else {
            result = new MediaType(type);
        }

        return result;
    }

    /**
     * Initiates the parsing of a mixed content part of the current document.
     */
    private void initiateInlineMixedContent() {
        this.contentDepth = 0;
        StringWriter sw = new StringWriter();
        currentContentWriter = new XmlWriter(sw);

        for (String prefix : this.prefixMappings.keySet()) {
            currentContentWriter.forceNSDecl(this.prefixMappings.get(prefix),
                    prefix);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        this.contentBuffer = new StringBuilder();
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        this.contentBuffer.delete(0, this.contentBuffer.length() + 1);

        if (this.contentDepth >= 0) {
            // The content might embed XML elements from various namespaces
            if (this.currentContentWriter != null) {
                this.currentContentWriter.startElement(uri, localName, qName,
                        attrs);
            }
            this.contentDepth++;
        } else if (uri.equalsIgnoreCase(Feed.ATOM_NAMESPACE)) {
            if (localName.equals("title")) {
                startTextElement(attrs);

                if (this.state == State.FEED_ENTRY) {
                    this.state = State.FEED_ENTRY_TITLE;
                } else if (this.state == State.FEED_ENTRY_SOURCE) {
                    this.state = State.FEED_ENTRY_SOURCE_TITLE;
                }
            } else if (localName.equals("updated")) {
                this.currentDate = new Date();

                if (this.state == State.FEED_ENTRY) {
                    this.state = State.FEED_ENTRY_UPDATED;
                } else if (this.state == State.FEED_ENTRY_SOURCE) {
                    this.state = State.FEED_ENTRY_SOURCE_UPDATED;
                }
            } else if (localName.equals("published")) {
                this.currentDate = new Date();

                if (this.state == State.FEED_ENTRY) {
                    this.state = State.FEED_ENTRY_PUBLISHED;
                }
            } else if (localName.equals("author")) {
                this.currentPerson = new Person();

                if (this.state == State.FEED_ENTRY) {
                    this.state = State.FEED_ENTRY_AUTHOR;
                } else if (this.state == State.FEED_ENTRY_SOURCE) {
                    this.state = State.FEED_ENTRY_SOURCE_AUTHOR;
                }
            } else if (localName.equals("name")) {
                if (this.state == State.FEED_ENTRY_AUTHOR) {
                    this.state = State.FEED_ENTRY_AUTHOR_NAME;
                } else if (this.state == State.FEED_ENTRY_SOURCE_AUTHOR) {
                    this.state = State.FEED_ENTRY_SOURCE_AUTHOR_NAME;
                }
            } else if (localName.equals("id")) {
                if (this.state == State.FEED_ENTRY) {
                    this.state = State.FEED_ENTRY_ID;
                } else if (this.state == State.FEED_ENTRY_SOURCE) {
                    this.state = State.FEED_ENTRY_SOURCE_ID;
                }
            } else if (localName.equals("link")) {
                this.currentLink = new Link();
                this.currentLink.setHref(new Reference(attrs.getValue("",
                        "href")));
                this.currentLink.setRel(Relation.valueOf(attrs.getValue("",
                        "rel")));
                String type = attrs.getValue("", "type");
                if (type != null && type.length() > 0) {
                    this.currentLink.setType(new MediaType(type));
                }
                this.currentLink.setHrefLang(new Language(attrs.getValue("",
                        "hreflang")));
                this.currentLink.setTitle(attrs.getValue("", "title"));
                final String attr = attrs.getValue("", "length");
                this.currentLink.setLength((attr == null) ? -1L : Long
                        .parseLong(attr));

                if (this.state == State.FEED_ENTRY) {
                    this.state = State.FEED_ENTRY_LINK;
                } else if (this.state == State.FEED_ENTRY_SOURCE) {
                    this.state = State.FEED_ENTRY_SOURCE_LINK;
                }
                // Glean the content
                this.currentContent = new Content();
                // Content available inline
                initiateInlineMixedContent();
                this.currentLink.setContent(currentContent);
                startLink(this.currentLink);
            } else if (localName.equalsIgnoreCase("entry")) {
                this.state = State.FEED_ENTRY;
                startEntry(this.currentEntry);
            } else if (localName.equals("category")) {
                this.currentCategory = new Category();
                this.currentCategory.setTerm(attrs.getValue("", "term"));
                this.currentCategory.setScheme(new Reference(attrs.getValue("",
                        "scheme")));
                this.currentCategory.setLabel(attrs.getValue("", "label"));

                if (this.state == State.FEED_ENTRY) {
                    this.state = State.FEED_ENTRY_CATEGORY;
                } else if (this.state == State.FEED_ENTRY_SOURCE) {
                    this.state = State.FEED_ENTRY_SOURCE_CATEGORY;
                }
            } else if (localName.equalsIgnoreCase("content")) {
                if (this.state == State.FEED_ENTRY) {
                    contentType = getMediaType(attrs.getValue("", "type"));
                    String srcAttr = attrs.getValue("", "src");
                    this.currentContent = new Content();

                    if (srcAttr == null) {
                        // Content available inline
                        initiateInlineMixedContent();
                    } else {
                        // Content available externally
                        this.currentContent.setExternalRef(new Reference(
                                srcAttr));
                        this.currentContent.setExternalType(contentType);
                    }

                    this.currentEntry.setContent(currentContent);
                    this.state = State.FEED_ENTRY_CONTENT;
                }

                startContent(this.currentContent);
            }
        }

        super.startElement(uri, localName, qName, attrs);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        this.prefixMappings.put(prefix, uri);

        super.startPrefixMapping(prefix, uri);
    }

    /**
     * Receive notification of the beginning of a text element.
     * 
     * @param attrs
     *            The attributes attached to the element.
     */
    public void startTextElement(Attributes attrs) {
        this.currentText = new Text(getMediaType(attrs.getValue("", "type")));
    }
}
