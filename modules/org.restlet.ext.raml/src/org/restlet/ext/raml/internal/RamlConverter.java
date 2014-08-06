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
import java.util.logging.Logger;

import org.raml.model.Raml;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.restlet.ext.raml.internal.model.Contract;
import org.restlet.ext.raml.internal.model.Definition;
import org.restlet.ext.raml.internal.model.Representation;

/**
 * Tool library for converting Restlet Web API Definition to and from Raml
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public abstract class RamlConverter {

	/** Internal logger. */
	protected static Logger LOGGER = Logger.getLogger(RamlConverter.class
			.getName());

	/** Supported version of Raml. */
	private static final String RAML_VERSION = "0.8";

	/**
	 * Converts a Raml documentation to a Restlet definition.
	 * 
	 * @param raml
	 *            The Raml resource listing.
	 * @return The Restlet definition.
	 * @throws RamlConversionException
	 */
	public static Definition convert(Raml raml) throws RamlConversionException {
		Definition def = new Definition();
		validate(raml);
		return def;
	}

	/**
	 * Retrieves the Raml API declaration corresponding to a category of the
	 * given Restlet Web API Definition
	 * 
	 * @param definition
	 *            The Restlet Web API Definition
	 * @return The Raml API definition of the given category
	 */
	public static Raml getRaml(Definition definition) {
		Raml raml = new Raml();
		raml = new RamlDocumentBuilder().build("file:///home/cyp/Bureau/hello_world2.raml");
		return raml;
	}

	/**
	 * Returns the representation given its name from the list of
	 * representations of the given contract.
	 * 
	 * @param contract
	 *            The contract.
	 * @param name
	 *            The name of the representation.
	 * @return A representation.
	 */
	private static Representation getRepresentationByName(Contract contract,
			String name) {
		if (name != null) {
			for (Representation repr : contract.getRepresentations()) {
				if (name.equals(repr.getName())) {
					return repr;
				}
			}
		}
		return null;
	}

	/**
	 * Indicates if the given type is a primitive type.
	 * 
	 * @param type
	 *            The type to be analysed
	 * @return A boolean of value true if the given type is primitive, false
	 *         otherwise.
	 */
	private static boolean isPrimitiveType(String type) {
		if ("string".equals(type.toLowerCase())
				|| "int".equals(type.toLowerCase())
				|| "integer".equals(type.toLowerCase())
				|| "long".equals(type.toLowerCase())
				|| "float".equals(type.toLowerCase())
				|| "double".equals(type.toLowerCase())
				|| "date".equals(type.toLowerCase())
				|| "boolean".equals(type.toLowerCase())
				|| "bool".equals(type.toLowerCase())) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the primitive types as Raml expects them
	 * 
	 * @param type
	 *            The type name to Ramlize
	 * @return The Ramlized type
	 */
	private static String toRamlType(String type) {
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

	/**
	 * Indicates if the given Raml definition is valid according to specs.
	 * 
	 * @param raml
	 *            The Raml definition.
	 * @throws RamlConversionException
	 */
	private static List<ValidationResult> validate(Raml raml)
			throws RamlConversionException {
		return RamlValidationService.createDefault().validate(
				new ClassPathResourceLoader().fetchResource("raml"));
	}

	/**
	 * Private constructor to ensure that the class acts as a true utility class
	 * i.e. it isn't instantiable and extensible.
	 */
	private RamlConverter() {
	}
}