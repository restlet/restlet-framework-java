/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities for manipulation of {@link Set}.
 * 
 * @author Manuel Boillod
 */
public class SetUtils {

	/**
	 * Returns a new {@link java.util.HashSet} with the given elements
	 * 
	 * @param elements The elements
	 * @return A new {@link java.util.HashSet} with the given elements
	 */
	@SafeVarargs
	public static <E> Set<E> newHashSet(E... elements) {
		HashSet<E> set = new HashSet<>(elements.length);
		Collections.addAll(set, elements);
		return set;
	}

}
