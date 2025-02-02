/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

import org.restlet.data.Reference;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Allows to sort the list of references set by the resource.
 * 
 * @author Jerome Louvel
 */
public class AlphabeticalComparator implements Comparator<Reference>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Compares two references.
	 * 
	 * @param ref0 The first reference.
	 * @param ref1 The second reference.
	 * @return The comparison result.
	 * @see Comparator
	 */
	public int compare(Reference ref0, Reference ref1) {
		final boolean bRep0Null = (ref0.getIdentifier() == null);
		final boolean bRep1Null = (ref1.getIdentifier() == null);

		if (bRep0Null && bRep1Null) {
			return 0;
		}
		if (bRep0Null) {
			return -1;
		}
		if (bRep1Null) {
			return 1;
		}
		return compare(ref0.toString(false, false), ref1.toString(false, false));
	}

	/**
	 * Compares two strings.
	 * 
	 * @param str0 The first string.
	 * @param str1 The second string.
	 * @return The comparison result.
	 * @see Comparator
	 */
	public int compare(final String str0, final String str1) {
		return str0.compareTo(str1);
	}
}
