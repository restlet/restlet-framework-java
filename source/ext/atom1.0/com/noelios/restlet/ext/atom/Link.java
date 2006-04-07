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
	/** Contains the link's IRI. */
	protected Reference href;

	/** Indicates the link's relation type */
	protected Relation rel;
	
	/** Advisory media type. */
	protected MediaType type;
	
	/** Language of the resource pointed to by the href attribute. */
	protected Language hrefLang;
	
	/** Human-readable information about the link. */
	protected String title;
	
	/** Advisory length of the linked content in octets. */
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

	/** 
	 * Returns the link's IRI.
	 * @return The link's IRI. 
	 */
	public Reference getHref()
	{
		return this.href;
	}
	
	/** 
	 * Sets the link's IRI.
	 * @param href The link's IRI. 
	 */
	public void setHref(Reference href)
	{
		this.href = href;
	}

	/** 
	 * Returns the link's relation type.
	 * @return The link's relation type.
	 */
	public Relation getRel()
	{
		return this.rel;
	}

	/** 
	 * Sets the link's relation type.
	 * @param rel The link's relation type.
	 */
	public void setRel(Relation rel)
	{
		this.rel = rel;
	}
	
	/**
	 * Returns the advisoty media type.
	 * @return The advisoty media type.
	 */
	public MediaType getType()
	{
		return this.type;
	}
	
	/**
	 * Sets the advisoty media type.
	 * @param type The advisoty media type.
	 */
	public void setType(MediaType type)
	{
		this.type = type;
	}
	
	/**
	 * Returns the language of the resource pointed to by the href attribute.
	 * @return The language of the resource pointed to by the href attribute.
	 */
	public Language getHrefLang()
	{
		return this.hrefLang;
	}

	/**
	 * Sets the language of the resource pointed to by the href attribute.
	 * @param hrefLang The language of the resource pointed to by the href attribute.
	 */
	public void setHrefLang(Language hrefLang)
	{
		this.hrefLang = hrefLang;
	}
	
	/**
	 * Returns the human-readable information about the link.
	 * @return The human-readable information about the link.
	 */
	public String getTitle()
	{
		return this.title;
	}
	
	/**
	 * Sets the human-readable information about the link.
	 * @param title The human-readable information about the link.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns the advisory length of the linked content in octets.
	 * @return The advisory length of the linked content in octets.
	 */
	public long getLength()
	{
		return this.length;
	}
	
	/**
	 * Sets the advisory length of the linked content in octets.
	 * @param length The advisory length of the linked content in octets.
	 */
	public void setLength(long length)
	{
		this.length = length;
	}
	
}
