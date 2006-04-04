/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.atom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.Manager;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypes;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.noelios.restlet.data.SaxRepresentation;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.XmlWriter;

/**
 * Atom Feed Document, acting as a container for metadata and data associated with the feed.
 */
public class Feed extends SaxRepresentation
{
	public final static String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	public final static String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
	
	protected List<Person> authors;
	protected List<Category> categories;
	protected List<Person> contributors;
	protected Generator generator;
	protected String icon;
	protected String id;
	protected List<Link> links;
	protected Reference logo;
	protected Text rights;
	protected Text subtitle;
	protected Text title;
	protected Date updated;
	protected List<Entry> entries;

	public Feed()
	{
		super(MediaTypes.APPLICATION_ATOM_XML);
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
	 * @param xmlFeed The XML feed document.
	 * @throws IOException
	 */
	public Feed(Representation xmlFeed) throws IOException
	{
		super(xmlFeed);
		parse(new ContentReader(this));
	}

	/**
	 * Writes the representation to a XML writer. 
	 * @param writer The XML writer to write to.
	 * @throws IOException
	 */
	public void write(XmlWriter writer) throws IOException
	{
		
	}

	public List<Person> getAuthors()
	{
		if(this.authors == null) this.authors = new ArrayList<Person>();
		return this.authors;
	}
	
	public List<Category> getCategories()
	{
		if(this.categories == null) this.categories = new ArrayList<Category>();
		return this.categories;
	}

	public List<Person> getContributors()
	{
		if(this.contributors == null) this.contributors = new ArrayList<Person>();
		return this.contributors;
	}

	public List<Entry> getEntries()
	{
		if(this.entries == null) this.entries = new ArrayList<Entry>();
		return this.entries;
	}

	public Generator getGenerator()
	{
		return this.generator;
	}
	
	public String getIcon()
	{
		return this.icon;
	}
	
	public String getId()
	{
		return this.id;
	}

	public List<Link> getLinks()
	{
		if(this.links == null) this.links = new ArrayList<Link>();
		return this.links;
	}

	public Reference getLogo()
	{
		return this.logo;
	}
	
	public Text getRights()
	{
		return this.rights;
	}
	
	public Text getSubtitle()
	{
		return this.subtitle;
	}
	
	public Text getTitle()
	{
		return this.title;
	}

	public Date getUpdated()
	{
		return this.updated;
	}
	
	public void setGenerator(Generator generator)
	{
		this.generator = generator;
	}
	
	public void setIcon(String icon)
	{
		this.icon = icon;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}

	public void setLogo(Reference logo)
	{
		this.logo = logo;
	}
	
	public void setRights(Text rights)
	{
		this.rights = rights;
	}
	
	public void setSubtitle(Text subtitle)
	{
		this.subtitle = subtitle;
	}
	
	public void setTitle(Text title)
	{
		this.title = title;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}
	
	// -------------------
	// Content reader part
	// -------------------
	private static class ContentReader extends DefaultHandler
	{
		public enum State 
		{
			NONE,
			FEED,
			FEED_AUTHOR,
			FEED_AUTHOR_NAME,
			FEED_AUTHOR_URI,
			FEED_AUTHOR_EMAIL,
			FEED_CATEGORY,
			FEED_CONTRIBUTOR,
			FEED_CONTRIBUTOR_NAME,
			FEED_CONTRIBUTOR_URI,
			FEED_CONTRIBUTOR_EMAIL,
			FEED_GENERATOR,
			FEED_ICON,
			FEED_ID,
			FEED_LINK,
			FEED_LOGO,
			FEED_RIGHTS,
			FEED_SUBTITLE,
			FEED_TITLE,
			FEED_UPDATED,
			FEED_ENTRY,	
			FEED_ENTRY_AUTHOR,
			FEED_ENTRY_AUTHOR_NAME,
			FEED_ENTRY_AUTHOR_URI,
			FEED_ENTRY_AUTHOR_EMAIL,
			FEED_ENTRY_CATEGORY,
			FEED_ENTRY_CONTENT,
			FEED_ENTRY_CONTRIBUTOR,
			FEED_ENTRY_ID,
			FEED_ENTRY_LINK,
			FEED_ENTRY_PUBLISHED,
			FEED_ENTRY_RIGHTS,
			FEED_ENTRY_SOURCE,
			FEED_ENTRY_SOURCE_AUTHOR,
			FEED_ENTRY_SOURCE_AUTHOR_NAME,
			FEED_ENTRY_SOURCE_AUTHOR_URI,
			FEED_ENTRY_SOURCE_AUTHOR_EMAIL,
			FEED_ENTRY_SOURCE_CATEGORY,
			FEED_ENTRY_SOURCE_CONTRIBUTOR,
			FEED_ENTRY_SOURCE_GENERATOR,
			FEED_ENTRY_SOURCE_ICON,
			FEED_ENTRY_SOURCE_ID,
			FEED_ENTRY_SOURCE_LINK,
			FEED_ENTRY_SOURCE_LOGO,
			FEED_ENTRY_SOURCE_RIGHTS,
			FEED_ENTRY_SOURCE_SUBTITLE,
			FEED_ENTRY_SOURCE_TITLE,
			FEED_ENTRY_SOURCE_UPDATED,
			FEED_ENTRY_SUMMARY,
			FEED_ENTRY_TITLE,
			FEED_ENTRY_UPDATED
		};
		
		private State state;
		private Feed currentFeed; 
		private Entry currentEntry;
		private Text currentText;
		private Date currentDate;
		private Link currentLink;
		private Person currentPerson;
		private Category currentCategory;
		private StringBuilder contentBuffer;
	
		public ContentReader(Feed feed)
		{
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
		public void startDocument() throws SAXException
		{
			this.contentBuffer = new StringBuilder();
		}
	
		/**
		 * Receive notification of the beginning of an element.
		 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
		 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
		 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
		 * @param attrs The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object. The value of this object after startElement returns is undefined.
		 */
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException
		{
			this.contentBuffer.delete(0, this.contentBuffer.length() + 1);
			
			if(uri.equalsIgnoreCase(ATOM_NAMESPACE))
			{
				if(localName.equals("feed"))
				{
					state = State.FEED;
				}
				else if(localName.equals("title"))
				{
					startTextElement(attrs);

					if(state == State.FEED)
					{
						state = State.FEED_TITLE;
					}
					else if(state == State.FEED_ENTRY)
					{
						state = State.FEED_ENTRY_TITLE;
					}
					else if(state == State.FEED_ENTRY_SOURCE)
					{
						state = State.FEED_ENTRY_SOURCE_TITLE;
					}
				}
				else if(localName.equals("updated"))
				{
					currentDate = new Date();

					if(state == State.FEED)
					{
						state = State.FEED_UPDATED;
					}
					else if(state == State.FEED_ENTRY)
					{
						state = State.FEED_ENTRY_UPDATED;
					}
					else if(state == State.FEED_ENTRY_SOURCE)
					{
						state = State.FEED_ENTRY_SOURCE_UPDATED;
					}
				}
				else if(localName.equals("author"))
				{
					currentPerson = new Person();
					
					if(state == State.FEED)
					{
						state = State.FEED_AUTHOR;
					}
					else if(state == State.FEED_ENTRY)
					{
						state = State.FEED_ENTRY_AUTHOR;
					}
					else if(state == State.FEED_ENTRY_SOURCE)
					{
						state = State.FEED_ENTRY_SOURCE_AUTHOR;
					}
				}
				else if(localName.equals("name"))
				{
					if(state == State.FEED_AUTHOR)
					{
						state = State.FEED_AUTHOR_NAME;
					}
					else if(state == State.FEED_ENTRY_AUTHOR)
					{
						state = State.FEED_ENTRY_AUTHOR_NAME;
					}
					else if(state == State.FEED_ENTRY_SOURCE_AUTHOR)
					{
						state = State.FEED_ENTRY_SOURCE_AUTHOR_NAME;
					}
				}
				else if(localName.equals("id"))
				{
					if(state == State.FEED)
					{
						state = State.FEED_ID;
					}
					else if(state == State.FEED_ENTRY)
					{
						state = State.FEED_ENTRY_ID;
					}
					else if(state == State.FEED_ENTRY_SOURCE)
					{
						state = State.FEED_ENTRY_SOURCE_ID;
					}
				}
				else if(localName.equals("link"))
				{
					currentLink = new Link();
					currentLink.setHref(Manager.createReference(attrs.getValue("", "href")));
					currentLink.setRel(Relation.parse(attrs.getValue("", "rel")));
					currentLink.setType(Manager.createMediaType(attrs.getValue("", "type")));
					currentLink.setHrefLang(Manager.createLanguage(attrs.getValue("", "hreflang")));
					currentLink.setTitle(attrs.getValue("", "title"));
					String attr = attrs.getValue("", "length");
					currentLink.setLength((attr == null) ? -1L : Long.parseLong(attr));
					
					if(state == State.FEED)
					{
						state = State.FEED_LINK;
					}
					else if(state == State.FEED_ENTRY)
					{
						state = State.FEED_ENTRY_LINK;
					}
					else if(state == State.FEED_ENTRY_SOURCE)
					{
						state = State.FEED_ENTRY_SOURCE_LINK;
					}
				}
				else if(localName.equalsIgnoreCase("entry"))
				{
					if(state == State.FEED)
					{
						currentEntry = new Entry();
						state = State.FEED_ENTRY;
					}
				}
				else if(localName.equals("category"))
				{
					currentCategory = new Category();
					currentCategory.setTerm(attrs.getValue("", "term"));
					currentCategory.setScheme(Manager.createReference(attrs.getValue("", "scheme")));
					currentCategory.setLabel(attrs.getValue("", "label"));

					if(state == State.FEED)
					{
						state = State.FEED_CATEGORY;
					}
					else if(state == State.FEED_ENTRY)
					{
						state = State.FEED_ENTRY_CATEGORY;
					}
					else if(state == State.FEED_ENTRY_SOURCE)
					{
						state = State.FEED_ENTRY_SOURCE_CATEGORY;
					}
				}
			}
		}
		
		/**
		 * Receive notification of the beginning of a text element.
		 * @param attrs The attributes attached to the element. 
		 */
		public void startTextElement(Attributes attrs)
		{
			MediaType type = null;
			String typeAttr = attrs.getValue("", "type");
			
			if((typeAttr == null) || typeAttr.equals("text"))
			{
				type = MediaTypes.TEXT_PLAIN;
			}
			else if(typeAttr.equals("html"))
			{
				type = MediaTypes.TEXT_HTML;
			}
			else if(typeAttr.equals("xhtml"))
			{
				type = MediaTypes.APPLICATION_XHTML_XML;
			}
			else
			{
				type = Manager.createMediaType(typeAttr);
			}
			
			currentText = new Text(type);
		}
		
		/**
		 * Receive notification of character data.
		 * @param ch The characters from the XML document.
		 * @param start The start position in the array.
		 * @param length The number of characters to read from the array.
		 */
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			contentBuffer.append(ch, start, length);
		}
		
		/**
		 * Receive notification of the end of an element.
		 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
		 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
		 * @param qName The qualified XML name (with prefix), or the empty string if qualified names are not available.
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if(currentText != null)
			{
				currentText.setContent(contentBuffer.toString());
			}
			
			if(currentDate != null)
			{
				String formattedDate = contentBuffer.toString();
				Date parsedDate = DateUtils.parse(formattedDate, DateUtils.FORMAT_RFC_3339);
				
				if(parsedDate != null)
				{
					currentDate.setTime(parsedDate.getTime());
				}
				else
				{
					currentDate = null;
				}
			}
			
			if(uri.equalsIgnoreCase(ATOM_NAMESPACE))
			{
				if(localName.equals("feed"))
				{
					state = State.NONE;
				}
				else if(localName.equals("title"))
				{
					if(state == State.FEED_TITLE)
					{
						currentFeed.setTitle(currentText);
						state = State.FEED;
					}
					else if(state == State.FEED_ENTRY_TITLE)
					{
						currentEntry.setTitle(currentText);
						state = State.FEED_ENTRY;
					}
					else if(state == State.FEED_ENTRY_SOURCE_TITLE)
					{
						currentEntry.getSource().setTitle(currentText);
						state = State.FEED_ENTRY_SOURCE;
					}
				}
				else if(localName.equals("updated"))
				{
					if(state == State.FEED_UPDATED)
					{
						currentFeed.setUpdated(currentDate);
						state = State.FEED;
					}
					else if(state == State.FEED_ENTRY_UPDATED)
					{
						currentEntry.setUpdated(currentDate);
						state = State.FEED_ENTRY;
					}
					else if(state == State.FEED_ENTRY_SOURCE_UPDATED)
					{
						currentEntry.getSource().setUpdated(currentDate);
						state = State.FEED_ENTRY_SOURCE;
					}
				}
				else if(localName.equals("author"))
				{
					if(state == State.FEED_AUTHOR)
					{
						currentFeed.getAuthors().add(currentPerson);
						state = State.FEED;
					}
					else if(state == State.FEED_ENTRY_AUTHOR)
					{
						currentEntry.getAuthors().add(currentPerson);
						state = State.FEED_ENTRY;
					}
					else if(state == State.FEED_ENTRY_SOURCE_AUTHOR)
					{
						currentEntry.getSource().getAuthors().add(currentPerson);
						state = State.FEED_ENTRY_SOURCE;
					}
				}
				else if(localName.equals("name"))
				{
					currentPerson.setName(contentBuffer.toString());
	
					if(state == State.FEED_AUTHOR_NAME)
					{
						state = State.FEED_AUTHOR;
					}
					else if(state == State.FEED_ENTRY_AUTHOR_NAME)
					{
						state = State.FEED_ENTRY_AUTHOR;
					}
					else if(state == State.FEED_ENTRY_SOURCE_AUTHOR_NAME)
					{
						state = State.FEED_ENTRY_SOURCE_AUTHOR;
					}
				}
				else if(localName.equals("id"))
				{
					if(state == State.FEED_ID)
					{
						currentFeed.setId(contentBuffer.toString());
						state = State.FEED;
					}
					else if(state == State.FEED_ENTRY_ID)
					{
						currentEntry.setId(contentBuffer.toString());
						state = State.FEED_ENTRY;
					}
					else if(state == State.FEED_ENTRY_SOURCE_ID)
					{
						currentEntry.getSource().setId(contentBuffer.toString());
						state = State.FEED_ENTRY_SOURCE;
					}
				}
				else if(localName.equals("link"))
				{
					if(state == State.FEED_LINK)
					{
						currentFeed.getLinks().add(currentLink);
						state = State.FEED;
					}
					else if(state == State.FEED_ENTRY_LINK)
					{
						currentEntry.getLinks().add(currentLink);
						state = State.FEED_ENTRY;
					}
					else if(state == State.FEED_ENTRY_SOURCE_LINK)
					{
						currentEntry.getSource().getLinks().add(currentLink);
						state = State.FEED_ENTRY_SOURCE;
					}
				}
				else if(localName.equalsIgnoreCase("entry"))
				{
					if(state == State.FEED_ENTRY)
					{
						currentFeed.getEntries().add(currentEntry);
						state = State.FEED;
					}
				}
				else if(localName.equals("category"))
				{
					if(state == State.FEED_CATEGORY)
					{
						currentFeed.getCategories().add(currentCategory);
						state = State.FEED;
					}
					else if(state == State.FEED_ENTRY_CATEGORY)
					{
						currentEntry.getCategories().add(currentCategory);
						state = State.FEED_ENTRY;
					}
					else if(state == State.FEED_ENTRY_SOURCE_CATEGORY)
					{
						currentEntry.getSource().getCategories().add(currentCategory);
						state = State.FEED_ENTRY_SOURCE;
					}
				}
			}
			
			currentText = null;
			currentDate = null;
		}
	
		/**
		 * Receive notification of the end of a document.
		 */
		public void endDocument() throws SAXException
		{
			this.state = State.NONE;
			this.currentEntry = null;
			this.contentBuffer = null;
		}
	}
	
}
