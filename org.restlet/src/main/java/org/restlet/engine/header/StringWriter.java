/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import java.util.Set;

/**
 * String header writer.
 * 
 * @author Manuel Boillod
 */
public class StringWriter extends HeaderWriter<String> {

	/**
	 * Writes a set of values with a comma separator.
	 * 
	 * @param values The set of values.
	 * @return The formatted set of values.
	 */
	public static String write(Set<String> values) {
		return new StringWriter().append(values).toString();
	}

	@Override
	public StringWriter append(String value) {
		return (StringWriter) appendToken(value);
	}

}
