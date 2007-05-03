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
import java.util.List;

/**
 * Workspace containing collections of members entries.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Workspace
{
	/**
	 * The parent service.
	 */
	private Service service;

	/**
	 * The title.
	 */
	private String title;

	/**
	 * The list of collections.
	 */
	private List<Collection> collections;

	/**
	 * Constructor.
	 * @param service The parent service.
	 */
	public Workspace(Service service)
	{
		this(service, null);
	}

	/**
	 * Constructor.
	 * @param service The parent service.
	 * @param title The title.
	 */
	public Workspace(Service service, String title)
	{
		this.service = service;
		this.title = title;
	}

	/**
	 * Returns the parent service.
	 * @return The parent service.
	 */
	public Service getService()
	{
		return this.service;
	}

	/**
	 * Sets the parent service.
	 * @param service The parent service.
	 */
	public void setService(Service service)
	{
		this.service = service;
	}

	/**
	 * Returns the title.
	 * @return The title.
	 */
	public String getTitle()
	{
		return this.title;
	}

	/**
	 * Sets the title.
	 * @param title The title.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns the list of collections.
	 * @return The list of collections.
	 */
	public List<Collection> getCollections()
	{
		if (this.collections == null)
		{
			this.collections = new ArrayList<Collection>();
		}

		return this.collections;
	}

}
