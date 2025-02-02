/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

import org.restlet.util.Resolver;

import java.util.Map;

/**
 * Resolves variable values based on a map.
 * 
 * @author Jerome Louvel
 */
public class MapResolver extends Resolver<Object> {

	/** The variables to use when formatting. */
	private final Map<String, ?> map;

	/**
	 * Constructor.
	 * 
	 * @param map The variables to use when formatting.
	 */
	public MapResolver(Map<String, ?> map) {
		this.map = map;
	}

	@Override
	public Object resolve(String variableName) {
		return this.map.get(variableName);
	}
}
