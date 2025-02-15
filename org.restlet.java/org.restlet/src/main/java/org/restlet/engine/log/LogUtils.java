/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.log;

/**
 * Logging related utility methods.
 * 
 * @author Jerome Louvel
 */
public class LogUtils {

	/**
	 * Prevent instantiation of the class.
	 */
	private LogUtils() {
	}

	/**
	 * Return the best class name. If the class is anonymous, then it returns the
	 * super class name.
	 * 
	 * @param clazz The class to name.
	 * @return The class name.
	 */
	public static String getBestClassName(Class<?> clazz) {
		String result = clazz.getSimpleName();

		if (result.isEmpty()) {
			result = getBestClassName(clazz.getSuperclass());
		}

		return result;
	}

	/**
	 * Returns a non-null logger name. It is composed by the canonical class name of
	 * the owner object suffixed by the owner's hash code.
	 * 
	 * @param baseName The base logger name to prepend, without a trailing dot.
	 * @param owner    The context owner.
	 * @return The logger name.
	 */
	public static String getLoggerName(String baseName, Object owner) {
		String result = baseName;

		if ((owner != null) && (owner.getClass().getSimpleName() != null)) {
			result += "." + getBestClassName(owner.getClass());
		}

		return result;
	}

}
