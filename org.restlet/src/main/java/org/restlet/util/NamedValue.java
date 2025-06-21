/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.util;

/**
 * String couple between a name and a value.
 * 
 * @author Jerome Louvel
 */
public interface NamedValue<V> {

	/**
	 * Returns the name of this parameter.
	 * 
	 * @return The name of this parameter.
	 */
	abstract String getName();

	/**
	 * Returns the value.
	 * 
	 * @return The value.
	 */
	abstract V getValue();

	/**
	 * Sets the value.
	 * 
	 * @param value The value.
	 */
	abstract void setValue(V value);

}
