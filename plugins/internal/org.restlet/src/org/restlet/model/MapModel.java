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

package org.restlet.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * Simple model based on a map.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class MapModel implements Model
{
	/** 
	 * The map used to store model variables.
	 */
	private Map<String, Object> map;

	/**
	 * Constructor. An internal map is automatically created.
	 */
	public MapModel()
	{
		this(new TreeMap<String, Object>());
	}

	/**
	 * Constructor.
	 * @param map The map to use as model storage.
	 */
	public MapModel(Map<String, Object> map)
	{
		this.map = map;
	}

	/**
	 * Returns the model value for a given name. 
	 * @param name The name to look-up.
	 * @return The model value for the given name.
	 */
	public String get(String name)
	{
		Object result = this.map.get(name);

		if (result instanceof String)
			return (String) result;
		else
			return result.toString();
	}

	/**
	 * Indicates if the model contains a value for a given name.
	 * @param name The name to look-up.
	 * @return True if the model contains a value for the given name.
	 */
	public boolean contains(String name)
	{
		return this.map.containsKey(name);
	}

	/**
	 * Puts the model value for a given name.
	 * @param name The name to look-up.
	 * @param value The value to put.
	 */
	public void put(String name, String value)
	{
		this.map.put(name, value);
	}

	/**
	 * Removes the model value for a given name.
	 * @param name The name to look-up.
	 */
	public void remove(String name)
	{
		this.map.remove(name);
	}

}
