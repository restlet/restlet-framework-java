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

import org.restlet.data.Reference;

/**
 * Identifies the agent used to generate a feed, for debugging and other purposes.
 */
public class Generator
{
	protected Reference uri;
	protected String version;
	protected String value;
	
	/**
	 * Constructor.
	 */
	public Generator()
	{
		this.uri = null;
		this.version = null;
		this.value = null;
	}
	
	public Reference getUri()
	{
		return this.uri;
	}
	
	public String getVersion()
	{
		return this.version;
	}
	
	public String getValue()
	{
		return this.value;
	}	
	
	public void setUri(Reference uri)
	{
		this.uri = uri;
	}
	
	public void setVersion(String version)
	{
		this.version = version;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}	
	
}
