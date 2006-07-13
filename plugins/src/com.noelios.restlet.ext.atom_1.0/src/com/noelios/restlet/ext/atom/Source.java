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

package com.noelios.restlet.ext.atom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.data.Reference;

/**
 * Source feed's metadata for entries copied from another feed. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Source
{
	/** The authors of the entry. */
	protected List<Person> authors;

	/** The categories associated with the entry. */
	protected List<Category> categories;

	/** The contributors to the entry. */
	protected List<Person> contributors;
	
	/** The agent used to generate a feed. */
	protected Generator generator;
	
	/** Image that provides iconic visual identification for a feed. */
	protected Reference icon;

	/** Permanent, universally unique identifier for the entry. */
	protected String id;

	/** The references from the entry to Web resources. */
	protected List<Link> links;
	
	/** Image that provides visual identification for a feed. */
	protected Reference logo;
	
	/** Information about rights held in and over an entry. */
	protected Text rights;
	
	/** Short summary, abstract, or excerpt of an entry. */
	protected Text subtitle;
	
	/** The human-readable title for the entry. */
	protected Text title;
	
	/** Most recent moment when the entry was modified in a significant way. */
	protected Date updated;
	
	/**
	 * Constructor.
	 */
	public Source()
	{
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
	}
	
	/** 
	 * Returns the authors of the entry.
	 * @return The authors of the entry.
	 */
	public List<Person> getAuthors()
	{
		if(this.authors == null) this.authors = new ArrayList<Person>();
		return this.authors;
	}

	/** 
	 * Returns the categories associated with the entry.
	 * @return The categories associated with the entry.
	 */
	public List<Category> getCategories()
	{
		if(this.categories == null) this.categories = new ArrayList<Category>();
		return this.categories;
	}
	
	/** 
	 * Returns the contributors to the entry.
	 * @return The contributors to the entry.
	 */
	public List<Person> getContributors()
	{
		if(this.contributors == null) this.contributors = new ArrayList<Person>();
		return this.contributors;
	}

	/** 
	 * Returns the agent used to generate a feed.
	 * @return The agent used to generate a feed.
	 */
	public Generator getGenerator()
	{
		return this.generator;
	}

	/** 
	 * Sets the agent used to generate a feed.
	 * @param generator The agent used to generate a feed.
	 */
	public void setGenerator(Generator generator)
	{
		this.generator = generator;
	}

	/** 
	 * Returns the image that provides iconic visual identification for a feed.
	 * @return The image that provides iconic visual identification for a feed.
	 */
	public Reference getIcon()
	{
		return this.icon;
	}

	/** 
	 * Sets the image that provides iconic visual identification for a feed.
	 * @param icon The image that provides iconic visual identification for a feed.
	 */
	public void setIcon(Reference icon)
	{
		this.icon = icon;
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
		if(this.links == null) this.links = new ArrayList<Link>();
		return this.links;
	}

	/**
	 * Returns the image that provides visual identification for a feed.
	 * @return The image that provides visual identification for a feed.
	 */
	public Reference getLogo()
	{
		return this.logo;
	}
	
	/**
	 * Sets the image that provides visual identification for a feed.
	 * @param logo The image that provides visual identification for a feed.
	 */
	public void setLogo(Reference logo)
	{
		this.logo = logo;
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
	 * Returns the short summary, abstract, or excerpt of an entry.
	 * @return The short summary, abstract, or excerpt of an entry.
	 */
	public Text getSubtitle()
	{
		return this.subtitle;
	}

	/** 
	 * Sets the short summary, abstract, or excerpt of an entry.
	 * @param subtitle The short summary, abstract, or excerpt of an entry.
	 */
	public void setSubtitle(Text subtitle)
	{
		this.subtitle = subtitle;
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
