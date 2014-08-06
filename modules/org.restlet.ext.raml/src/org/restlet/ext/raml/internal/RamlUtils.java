/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.raml.internal;

import java.util.List;

import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlValidationService;

/**
 * Utility class for Raml java bean
 * 
 * @author Cyprien Quilici
 * 
 */
public class RamlUtils {

	/**
	 * Indicates if the given Raml definition is valid according to specs.
	 * 
	 * @param raml
	 *            The Raml definition.
	 * @throws RamlConversionException
	 */
	public static List<ValidationResult> validate(String location)
			throws RamlConversionException {
		// requires lots of dependencies => see if needed
		return RamlValidationService.createDefault().validate(location);
	}

	/**
	 * Returns the String passed as a parameter with a capital first letter.
	 * Used to generate resource names in camel case
	 * 
	 * @param str
	 *            The string to process
	 * @return The String with a capital first letter
	 */
	public static String capFirst(String str) {
		if (str == null) {
			return null;
		}
		if ("".equals(str)) {
			return "";
		}
		return ("" + str.charAt(0)).toUpperCase() + str.substring(1);
	}

	/**
	 * Returns the primitive types as Raml expects them
	 * 
	 * @param type
	 *            The type name to Ramlize
	 * @return The Ramlized type
	 */
	public static String toRamlType(String type) {
		if ("Integer".equals(type)) {
			return "int";
		} else if ("String".equals(type)) {
			return "string";
		} else if ("Boolean".equals(type)) {
			return "boolean";
		} else {
			return type;
		}
	}
}
