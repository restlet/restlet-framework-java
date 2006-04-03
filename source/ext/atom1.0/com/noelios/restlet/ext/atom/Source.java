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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Source feed's metadata for entries copied from another feed. 
 */
public class Source
{
	protected List<Person> authors;
	protected List<Category> categories;
	protected List<Person> contributors;
	protected Generator generator;
	protected String icon;
	protected String id;
	protected List<Link> links;
	protected String logo;
	protected Text rights;
	protected Text subtitle;
	protected Text title;
	protected Date updated;
	
	
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

	public List<Link> getLinks()
	{
		if(this.links == null) this.links = new ArrayList<Link>();
		return this.links;
	}

	public Text getTitle()
	{
		return this.title;
	}

	public Date getUpdated()
	{
		return this.updated;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}

	public void setTitle(Text title)
	{
		this.title = title;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}
	
}
