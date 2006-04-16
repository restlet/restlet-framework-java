/*
 * Copyright 2005-2006 Jerome LOUVEL
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
	/** Reference of the generating agent. */
	protected Reference uri;
	
	/** Version of the generationg agent. */
	protected String version;
	
	/** Human-readable name for the generating agent. */
	protected String name;
	
	/**
	 * Constructor.
	 */
	public Generator()
	{
		this.uri = null;
		this.version = null;
		this.name = null;
	}

	/**
	 * Returns the reference of the generating agent.
	 * @return The reference of the generating agent.
	 */
	public Reference getUri()
	{
		return this.uri;
	}
	
	/**
	 * Sets the reference of the generating agent.
	 * @param uri The reference of the generating agent.
	 */
	public void setUri(Reference uri)
	{
		this.uri = uri;
	}
	
	/**
	 * Returns the version of the generating agent.
	 * @return The version of the generating agent.
	 */
	public String getVersion()
	{
		return this.version;
	}
	
	/**
	 * Sets the version of the generating agent.
	 * @param version The version of the generating agent.
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}
	
	/**
	 * Returns the human-readable name for the generating agent.
	 * @return The human-readable name for the generating agent.
	 */
	public String getName()
	{
		return this.name;
	}	
	
	/**
	 * Sets the human-readable name for the generating agent.
	 * @param name The human-readable name for the generating agent.
	 */
	public void setName(String name)
	{
		this.name = name;
	}	
	
}
