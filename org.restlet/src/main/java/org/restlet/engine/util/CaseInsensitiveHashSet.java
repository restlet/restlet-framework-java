/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * Set implementation that is case insensitive.
 */
public class CaseInsensitiveHashSet extends HashSet<String> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor initializing the set with the given collection.
	 * 
	 * @param source The source collection to use for initialization.
	 */
	public CaseInsensitiveHashSet(Collection<? extends String> source) {
		super(source);
	}

	@Override
	public boolean add(String element) {
		return super.add(element.toLowerCase());
	}

	/**
	 * Verify containment by ignoring case.
	 */
	public boolean contains(String element) {
		return super.contains(element.toLowerCase());
	}

	@Override
	public boolean contains(Object o) {
		return contains(o.toString());
	}
}
