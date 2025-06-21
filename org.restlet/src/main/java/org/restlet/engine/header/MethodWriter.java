/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Method;

import java.util.Set;

/**
 * Method header writer.
 * 
 * @author Jerome Louvel
 */
public class MethodWriter extends HeaderWriter<Method> {

	/**
	 * Writes a set of methods with a comma separator.
	 * 
	 * @param methods The set of methods.
	 * @return The formatted set of methods.
	 */
	public static String write(Set<Method> methods) {
		return new MethodWriter().append(methods).toString();
	}

	@Override
	public MethodWriter append(Method method) {
		return (MethodWriter) appendToken(method.getName());
	}

}
