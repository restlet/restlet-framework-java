/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.ext.atom;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Represents an individual entry, acting as a container for metadata and data associated with the entry.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Entry
{
	/** The authors of the entry. */
	private List<Person> authors;

	/** The categories associated with the entry. */
	private List<Category> categories;

	/** Contains or links to the content of the entry. */
	private Content content;

	/** The contributors to the entry. */
	private List<Person> contributors;

	/** Permanent, universally unique identifier for the entry. */
	private String id;

	/** The references from the entry to Web resources. */
	private List<Link> links;

	/** Moment associated with an event early in the life cycle of the entry. */
	private Date published;

	/** Information about rights held in and over an entry. */
	private Text rights;

	/** Source feed's metadata if the entry was copied from another feed. */
	private Source source;

	/** Short summary, abstract, or excerpt of the entry. */
	private String summary;

	/** The human-readable title for the entry. */
	private Text title;

	/** Most recent moment when the entry was modified in a significant way. */
	private Date updated;

	/**
	 * Constructor.
	 */
	public Entry()
	{
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
	 * @return The authors of the entry.
	 */
	public List<Person> getAuthors()
	{
		if (this.authors == null) this.authors = new ArrayList<Person>();
		return this.authors;
	}

	/** 
	 * Returns the categories associated with the entry.
	 * @return The categories associated with the entry.
	 */
	public List<Category> getCategories()
	{
		if (this.categories == null) this.categories = new ArrayList<Category>();
		return this.categories;
	}

	/** 
	 * Returns the content of the entry or links to it.
	 * @return The content of the entry or links to it.
	 */
	public Content getContent()
	{
		return this.content;
	}

	/** 
	 * Sets the content of the entry or links to it.
	 * @param content The content of the entry or links to it.
	 */
	public void setContent(Content content)
	{
		this.content = content;
	}

	/** 
	 * Returns the contributors to the entry.
	 * @return The contributors to the entry.
	 */
	public List<Person> getContributors()
	{
		if (this.contributors == null) this.contributors = new ArrayList<Person>();
		return this.contributors;
	}

	/** 
	 * Returns the permanent, universally unique identifier for the entry.
	 * @return The permanent, universally unique identifier for the entry.
	 */
	public String getId()
	{
		return this.id;
	}

	/** 
	 * Sets the permanent, universally unique identifier for the entry.
	 * @param id The permanent, universally unique identifier for the entry.
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/** 
	 * Returns the references from the entry to Web resources.
	 * @return The references from the entry to Web resources.
	 */
	public List<Link> getLinks()
	{
		if (this.links == null) this.links = new ArrayList<Link>();
		return this.links;
	}

	/**
	 * Returns the first available link with a given relation type.
	 * @param rel The relation type to match.
	 * @return The first available link with a given relation type.
	 */
	public Link getLink(Relation rel)
	{
		Link result = null;
		Link current = null;

		for (Iterator<Link> iter = getLinks().iterator(); (result == null)
				&& iter.hasNext();)
		{
			current = iter.next();

			if (current.getRel() == rel)
			{
				result = current;
			}
		}

		return result;
	}

	/** 
	 * Returns the moment associated with an event early in the life cycle of the entry.
	 * @return The moment associated with an event early in the life cycle of the entry.
	 */
	public Date getPublished()
	{
		return this.published;
	}

	/** 
	 * Sets the moment associated with an event early in the life cycle of the entry.
	 * @param published The moment associated with an event early in the life cycle of the entry.
	 */
	public void setPublished(Date published)
	{
		this.published = published;
	}

	/** 
	 * Returns the information about rights held in and over an entry.
	 * @return The information about rights held in and over an entry.
	 */
	public Text getRights()
	{
		return this.rights;
	}

	/** 
	 * Sets the information about rights held in and over an entry.
	 * @param rights The information about rights held in and over an entry.
	 */
	public void setRights(Text rights)
	{
		this.rights = rights;
	}

	/** 
	 * Returns the source feed's metadata if the entry was copied from another feed.
	 * @return The source feed's metadata if the entry was copied from another feed.
	 */
	public Source getSource()
	{
		return this.source;
	}

	/** 
	 * Sets the source feed's metadata if the entry was copied from another feed.
	 * @param source The source feed's metadata if the entry was copied from another feed.
	 */
	public void setSource(Source source)
	{
		this.source = source;
	}

	/** 
	 * Returns the short summary, abstract, or excerpt of the entry.
	 * @return The short summary, abstract, or excerpt of the entry.
	 */
	public String getSummary()
	{
		return this.summary;
	}

	/** 
	 * Sets the short summary, abstract, or excerpt of the entry.
	 * @param summary The short summary, abstract, or excerpt of the entry.
	 */
	public void setSummary(String summary)
	{
		this.summary = summary;
	}

	/** 
	 * Returns the human-readable title for the entry.
	 * @return The human-readable title for the entry.
	 */
	public Text getTitle()
	{
		return this.title;
	}

	/** 
	 * Sets the human-readable title for the entry.
	 * @param title The human-readable title for the entry.
	 */
	public void setTitle(Text title)
	{
		this.title = title;
	}

	/** 
	 * Returns the most recent moment when the entry was modified in a significant way.
	 * @return The most recent moment when the entry was modified in a significant way.
	 */
	public Date getUpdated()
	{
		return this.updated;
	}

	/** 
	 * Sets the most recent moment when the entry was modified in a significant way.
	 * @param updated The most recent moment when the entry was modified in a significant way.
	 */
	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

}
