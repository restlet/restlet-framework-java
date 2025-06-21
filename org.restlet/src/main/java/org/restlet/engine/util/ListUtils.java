/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Emulate List functions missing from GWT port of List
 * 
 * @author Rob Heittman
 */
public class ListUtils {
	/**
	 * Unlike List.subList(), which returns a live view of a set of List elements,
	 * this method returns a new copy of the list. List.subList() is not available
	 * in GWT 1.5 and was removed on purpose.
	 * 
	 * @param list      The source List
	 * @param fromIndex Starting index in the source List
	 * @param toIndex   Ending index in the source List
	 * @throws IndexOutOfBoundsException Call exceeds the bounds of the source List
	 * @throws IllegalArgumentException  fromIndex and toIndex are not in sequence
	 * @return a copy of the selected range
	 */
	public static <T> List<T> copySubList(List<T> list, int fromIndex, int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > list.size())
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		ArrayList<T> subList = new ArrayList<T>();
		for (int i = fromIndex; i <= toIndex; i++) {
			subList.add(list.get(i));
		}
		return subList;
	}

}
