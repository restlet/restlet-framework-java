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

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;

/**
 * Defines a reference from an entry or feed to a Web resource.
 */
public class Link
{
	protected Reference href;
	protected Relation rel;
	protected MediaType type;
	protected Language hrefLang;
	protected String title;
	protected long length;
	
	/**
	 * Constructor.
	 */
	public Link()
	{
		this.href = null;
		this.rel = null;
		this.type = null;
		this.hrefLang = null;
		this.title = null;
		this.length = -1;
	}

	public Relation getRel()
	{
		return this.rel;
	}
	
	public void setHref(Reference href)
	{
		this.href = href;
	}
	
	public void setHrefLang(Language hrefLang)
	{
		this.hrefLang = hrefLang;
	}
	
	public void setLength(long length)
	{
		this.length = length;
	}

	public void setRel(Relation rel)
	{
		this.rel = rel;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public void setType(MediaType type)
	{
		this.type = type;
	}
	
}
