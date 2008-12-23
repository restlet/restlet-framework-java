/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
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
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Atom Feed Document, acting as a component for metadata and data associated
 * with the feed.
 * 
 * @author Jerome Louvel
 */
public class Feed extends SaxRepresentation {
	// -------------------
	// Content reader part
	// -------------------
	private static class ContentReader extends DefaultHandler {
		public enum State {
			FEED, FEED_AUTHOR, FEED_AUTHOR_EMAIL, FEED_AUTHOR_NAME, FEED_AUTHOR_URI, FEED_CATEGORY, FEED_CONTRIBUTOR, FEED_CONTRIBUTOR_EMAIL, FEED_CONTRIBUTOR_NAME, FEED_CONTRIBUTOR_URI, FEED_ENTRY, FEED_ENTRY_AUTHOR, FEED_ENTRY_AUTHOR_EMAIL, FEED_ENTRY_AUTHOR_NAME, FEED_ENTRY_AUTHOR_URI, FEED_ENTRY_CATEGORY, FEED_ENTRY_CONTENT, FEED_ENTRY_CONTRIBUTOR, FEED_ENTRY_ID, FEED_ENTRY_LINK, FEED_ENTRY_PUBLISHED, FEED_ENTRY_RIGHTS, FEED_ENTRY_SOURCE, FEED_ENTRY_SOURCE_AUTHOR, FEED_ENTRY_SOURCE_AUTHOR_EMAIL, FEED_ENTRY_SOURCE_AUTHOR_NAME, FEED_ENTRY_SOURCE_AUTHOR_URI, FEED_ENTRY_SOURCE_CATEGORY, FEED_ENTRY_SOURCE_CONTRIBUTOR, FEED_ENTRY_SOURCE_GENERATOR, FEED_ENTRY_SOURCE_ICON, FEED_ENTRY_SOURCE_ID, FEED_ENTRY_SOURCE_LINK, FEED_ENTRY_SOURCE_LOGO, FEED_ENTRY_SOURCE_RIGHTS, FEED_ENTRY_SOURCE_SUBTITLE, FEED_ENTRY_SOURCE_TITLE, FEED_ENTRY_SOURCE_UPDATED, FEED_ENTRY_SUMMARY, FEED_ENTRY_TITLE, FEED_ENTRY_UPDATED, FEED_GENERATOR, FEED_ICON, FEED_ID, FEED_LINK, FEED_LOGO, FEED_RIGHTS, FEED_SUBTITLE, FEED_TITLE, FEED_UPDATED, NONE
		}

		/** Buffer for the current text content of the current tag. */
		private StringBuilder contentBuffer;
		/** The current parsed Category. */
		private Category currentCategory;
		/** The current date parsed from the current text content. */
		private Date currentDate;
		/** The current parsed Entry. */
		private Entry currentEntry;
		/** The current parsed Feed. */
		private final Feed currentFeed;
		/** The current parsed Link. */
		private Link currentLink;
		/** The current parsed Person. */
		private Person currentPerson;
		/** The current parsed Text. */
		private Text currentText;
		/** The current state. */
		private State state;

		/**
		 * Constructor.
		 * 
		 * @param feed
		 *            The feed object to update during the parsing.
		 */
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
			this.contentBuffer.append(ch, start, length);
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
			if (this.currentText != null) {
				this.currentText.setContent(this.contentBuffer.toString());
			}

			if (this.currentDate != null) {
				final String formattedDate = this.contentBuffer.toString();
				final Date parsedDate = DateUtils.parse(formattedDate,
						DateUtils.FORMAT_RFC_3339);

				if (parsedDate != null) {
					this.currentDate.setTime(parsedDate.getTime());
				} else {
					this.currentDate = null;
				}
			}

			if (uri.equalsIgnoreCase(ATOM_NAMESPACE)) {
				if (localName.equals("feed")) {
					this.state = State.NONE;
				} else if (localName.equals("title")) {
					if (this.state == State.FEED_TITLE) {
						this.currentFeed.setTitle(this.currentText);
						this.state = State.FEED;
					} else if (this.state == State.FEED_ENTRY_TITLE) {
						this.currentEntry.setTitle(this.currentText);
						this.state = State.FEED_ENTRY;
					} else if (this.state == State.FEED_ENTRY_SOURCE_TITLE) {
						this.currentEntry.getSource()
								.setTitle(this.currentText);
						this.state = State.FEED_ENTRY_SOURCE;
					}
				} else if (localName.equals("updated")) {
					if (this.state == State.FEED_UPDATED) {
						this.currentFeed.setUpdated(this.currentDate);
						this.state = State.FEED;
					} else if (this.state == State.FEED_ENTRY_UPDATED) {
						this.currentEntry.setUpdated(this.currentDate);
						this.state = State.FEED_ENTRY;
					} else if (this.state == State.FEED_ENTRY_SOURCE_UPDATED) {
						this.currentEntry.getSource().setUpdated(
								this.currentDate);
						this.state = State.FEED_ENTRY_SOURCE;
					}
				} else if (localName.equals("published")) {
					if (this.state == State.FEED_ENTRY_PUBLISHED) {
						this.currentEntry.setPublished(this.currentDate);
						this.state = State.FEED_ENTRY;
					}
				} else if (localName.equals("author")) {
					if (this.state == State.FEED_AUTHOR) {
						this.currentFeed.getAuthors().add(this.currentPerson);
						this.state = State.FEED;
					} else if (this.state == State.FEED_ENTRY_AUTHOR) {
						this.currentEntry.getAuthors().add(this.currentPerson);
						this.state = State.FEED_ENTRY;
					} else if (this.state == State.FEED_ENTRY_SOURCE_AUTHOR) {
						this.currentEntry.getSource().getAuthors().add(
								this.currentPerson);
						this.state = State.FEED_ENTRY_SOURCE;
					}
				} else if (localName.equals("name")) {
					this.currentPerson.setName(this.contentBuffer.toString());

					if (this.state == State.FEED_AUTHOR_NAME) {
						this.state = State.FEED_AUTHOR;
					} else if (this.state == State.FEED_ENTRY_AUTHOR_NAME) {
						this.state = State.FEED_ENTRY_AUTHOR;
					} else if (this.state == State.FEED_ENTRY_SOURCE_AUTHOR_NAME) {
						this.state = State.FEED_ENTRY_SOURCE_AUTHOR;
					}
				} else if (localName.equals("id")) {
					if (this.state == State.FEED_ID) {
						this.currentFeed.setId(this.contentBuffer.toString());
						this.state = State.FEED;
					} else if (this.state == State.FEED_ENTRY_ID) {
						this.currentEntry.setId(this.contentBuffer.toString());
						this.state = State.FEED_ENTRY;
					} else if (this.state == State.FEED_ENTRY_SOURCE_ID) {
						this.currentEntry.getSource().setId(
								this.contentBuffer.toString());
						this.state = State.FEED_ENTRY_SOURCE;
					}
				} else if (localName.equals("link")) {
					if (this.state == State.FEED_LINK) {
						this.currentFeed.getLinks().add(this.currentLink);
						this.state = State.FEED;
					} else if (this.state == State.FEED_ENTRY_LINK) {
						this.currentEntry.getLinks().add(this.currentLink);
						this.state = State.FEED_ENTRY;
					} else if (this.state == State.FEED_ENTRY_SOURCE_LINK) {
						this.currentEntry.getSource().getLinks().add(
								this.currentLink);
						this.state = State.FEED_ENTRY_SOURCE;
					}
				} else if (localName.equalsIgnoreCase("entry")) {
					if (this.state == State.FEED_ENTRY) {
						this.currentFeed.getEntries().add(this.currentEntry);
						this.state = State.FEED;
					}
				} else if (localName.equals("category")) {
					if (this.state == State.FEED_CATEGORY) {
						this.currentFeed.getCategories().add(
								this.currentCategory);
						this.state = State.FEED;
					} else if (this.state == State.FEED_ENTRY_CATEGORY) {
						this.currentEntry.getCategories().add(
								this.currentCategory);
						this.state = State.FEED_ENTRY;
					} else if (this.state == State.FEED_ENTRY_SOURCE_CATEGORY) {
						this.currentEntry.getSource().getCategories().add(
								this.currentCategory);
						this.state = State.FEED_ENTRY_SOURCE;
					}
				} else if (localName.equalsIgnoreCase("content")) {
					if (this.state == State.FEED_ENTRY_CONTENT) {
						if (this.currentEntry.getContent().isInline()) {
							final StringRepresentation sr = (StringRepresentation) this.currentEntry
									.getContent().getInlineContent();
							sr.setText(this.contentBuffer.toString());
						}

						this.state = State.FEED_ENTRY;
					}
				}
			}

			this.currentText = null;
			this.currentDate = null;
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
					this.state = State.FEED;
				} else if (localName.equals("title")) {
					startTextElement(attrs);

					if (this.state == State.FEED) {
						this.state = State.FEED_TITLE;
					} else if (this.state == State.FEED_ENTRY) {
						this.state = State.FEED_ENTRY_TITLE;
					} else if (this.state == State.FEED_ENTRY_SOURCE) {
						this.state = State.FEED_ENTRY_SOURCE_TITLE;
					}
				} else if (localName.equals("updated")) {
					this.currentDate = new Date();

					if (this.state == State.FEED) {
						this.state = State.FEED_UPDATED;
					} else if (this.state == State.FEED_ENTRY) {
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

					if (this.state == State.FEED) {
						this.state = State.FEED_AUTHOR;
					} else if (this.state == State.FEED_ENTRY) {
						this.state = State.FEED_ENTRY_AUTHOR;
					} else if (this.state == State.FEED_ENTRY_SOURCE) {
						this.state = State.FEED_ENTRY_SOURCE_AUTHOR;
					}
				} else if (localName.equals("name")) {
					if (this.state == State.FEED_AUTHOR) {
						this.state = State.FEED_AUTHOR_NAME;
					} else if (this.state == State.FEED_ENTRY_AUTHOR) {
						this.state = State.FEED_ENTRY_AUTHOR_NAME;
					} else if (this.state == State.FEED_ENTRY_SOURCE_AUTHOR) {
						this.state = State.FEED_ENTRY_SOURCE_AUTHOR_NAME;
					}
				} else if (localName.equals("id")) {
					if (this.state == State.FEED) {
						this.state = State.FEED_ID;
					} else if (this.state == State.FEED_ENTRY) {
						this.state = State.FEED_ENTRY_ID;
					} else if (this.state == State.FEED_ENTRY_SOURCE) {
						this.state = State.FEED_ENTRY_SOURCE_ID;
					}
				} else if (localName.equals("link")) {
					this.currentLink = new Link();
					this.currentLink.setHref(new Reference(attrs.getValue("",
							"href")));
					this.currentLink.setRel(Relation.parse(attrs.getValue("",
							"rel")));
					this.currentLink.setType(new MediaType(attrs.getValue("",
							"type")));
					this.currentLink.setHrefLang(new Language(attrs.getValue(
							"", "hreflang")));
					this.currentLink.setTitle(attrs.getValue("", "title"));
					final String attr = attrs.getValue("", "length");
					this.currentLink.setLength((attr == null) ? -1L : Long
							.parseLong(attr));

					if (this.state == State.FEED) {
						this.state = State.FEED_LINK;
					} else if (this.state == State.FEED_ENTRY) {
						this.state = State.FEED_ENTRY_LINK;
					} else if (this.state == State.FEED_ENTRY_SOURCE) {
						this.state = State.FEED_ENTRY_SOURCE_LINK;
					}
				} else if (localName.equalsIgnoreCase("entry")) {
					if (this.state == State.FEED) {
						this.currentEntry = new Entry();
						this.state = State.FEED_ENTRY;
					}
				} else if (localName.equals("category")) {
					this.currentCategory = new Category();
					this.currentCategory.setTerm(attrs.getValue("", "term"));
					this.currentCategory.setScheme(new Reference(attrs
							.getValue("", "scheme")));
					this.currentCategory.setLabel(attrs.getValue("", "label"));

					if (this.state == State.FEED) {
						this.state = State.FEED_CATEGORY;
					} else if (this.state == State.FEED_ENTRY) {
						this.state = State.FEED_ENTRY_CATEGORY;
					} else if (this.state == State.FEED_ENTRY_SOURCE) {
						this.state = State.FEED_ENTRY_SOURCE_CATEGORY;
					}
				} else if (localName.equalsIgnoreCase("content")) {
					if (this.state == State.FEED_ENTRY) {
						final MediaType type = getMediaType(attrs.getValue("",
								"type"));
						final String srcAttr = attrs.getValue("", "src");
						final Content currentContent = new Content();

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

						this.currentEntry.setContent(currentContent);
						this.state = State.FEED_ENTRY_CONTENT;
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
			this.currentText = new Text(
					getMediaType(attrs.getValue("", "type")));
		}
	}

	/** Atom Syndication Format namespace. */
	public final static String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

	/** XHTML namespace. */
	public final static String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

	/** The authors of the feed. */
	private volatile List<Person> authors;

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
	 * @param xmlFeed
	 *            The XML feed document.
	 * @throws IOException
	 */
	public Feed(Representation xmlFeed) throws IOException {
		super(xmlFeed);
		parse(new ContentReader(this));
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
			writer.setPrefix(ATOM_NAMESPACE, "atom");
			writer.setDataFormat(true);
			writer.setIndentStep(3);
			writer.startDocument();
			writeElement(writer);
			writer.endDocument();
		} catch (SAXException e) {
			e.printStackTrace();
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
