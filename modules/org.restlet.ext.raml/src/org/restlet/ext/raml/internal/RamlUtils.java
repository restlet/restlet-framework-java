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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.raml.model.ActionType;
import org.raml.model.ParamType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlValidationService;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

/**
 * Utility class for Raml java bean
 * 
 * @author Cyprien Quilici
 * 
 */
public class RamlUtils {
	/**
	 * The list of java types that correspond to RAML's number type
	 */
	static final ArrayList<String> numberTypesList = new ArrayList<String>() {
		private static final long serialVersionUID = -3474978102555721602L;

		{
			add("integer");
			add("int");
			add("double");
			add("double");
			add("long");
			add("long");
			add("float");
			add("float");
		}
	};

	/**
	 * The list of java types that correspond to RAML's integer type
	 */
	static final ArrayList<String> integerTypesList = new ArrayList<String>() {
		private static final long serialVersionUID = 7392597971276638683L;

		{
			add("integer");
			add("int");
		}
	};

	/**
	 * Indicates if the given Raml definition is valid according to specs.
	 * 
	 * @param raml
	 *            The Raml definition.
	 * @throws TranslationException
	 */
	public static List<ValidationResult> validate(String location)
			throws TranslationException {
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

	/**
	 * Gets the parent resource of a Resource given its path and the list of
	 * paths available on the API
	 * 
	 * @param paths
	 *            The list of paths available on the API.
	 * @param resourcePath
	 *            The path of the resource the parent resource is searched for
	 * @param raml
	 *            The Raml representing the API
	 * @return The parent resource
	 */
	public static Resource getParentResource(List<String> paths,
			String resourcePath, Raml raml) {
		List<String> parentPaths = new ArrayList<String>();
		parentPaths.addAll(paths);
		parentPaths.add(resourcePath);
		Collections.sort(parentPaths);
		int index = parentPaths.indexOf(resourcePath);
		String parentPath;
		if (index != 0) {
			parentPath = parentPaths.get(index - 1);
			if (resourcePath.startsWith(parentPath)) {
				return getResourceByCompletePath(raml, parentPath);
			}
		}
		return null;
	}

	/**
	 * Returns the RAML parameter type given a java primitive type
	 * 
	 * @param type
	 *            The java type
	 * @return The RAML parameter type
	 */
	public static ParamType getParamType(String type) {
		if (integerTypesList.contains(type)) {
			return ParamType.INTEGER;
		} else if (numberTypesList.contains(type)) {
			return ParamType.NUMBER;
		} else if ("boolean".equals(type)) {
			return ParamType.BOOLEAN;
		} else if ("date".equals(type)) {
			return ParamType.DATE;
		}

		// TODO add files
		// else if () {
		// return ParamType.FILE;
		// }
		return ParamType.STRING;
	}

	/**
	 * Returns the RAML ActionType given an HTTP method name
	 * 
	 * @param method
	 *            The HTTP method name as String
	 * @return The corresponding ActionType
	 */
	public static ActionType getActionType(String method) {
		if ("post".equals(method.toLowerCase())) {
			return ActionType.POST;
		} else if ("get".equals(method.toLowerCase())) {
			return ActionType.GET;
		} else if ("put".equals(method.toLowerCase())) {
			return ActionType.PUT;
		} else if ("patch".equals(method.toLowerCase())) {
			return ActionType.PATCH;
		} else if ("delete".equals(method.toLowerCase())) {
			return ActionType.DELETE;
		} else if ("head".equals(method.toLowerCase())) {
			return ActionType.HEAD;
		} else if ("options".equals(method.toLowerCase())) {
			return ActionType.OPTIONS;
		} else if ("trace".equals(method.toLowerCase())) {
			return ActionType.TRACE;
		}
		return null;
	}

	/**
	 * Returns a RAML Resource given its complete path
	 * 
	 * @param raml
	 *            The Raml in which the Resource is searched for
	 * @param path
	 *            The complete path of the resource
	 * @return The Resource
	 */
	private static Resource getResourceByCompletePath(Raml raml, String path) {
		for (Entry<String, Resource> entry : raml.getResources().entrySet()) {
			if (path.equals(entry.getValue().getParentUri()
					+ entry.getValue().getRelativeUri())) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Generates a name for a resource computed from its path. The name is
	 * composed of all alphanumeric characters in camel case.<br/>
	 * Ex: /contacts/{contactId} => ContactsContactId
	 * 
	 * @param uri
	 *            The URI of the Resource
	 * @return The Resource's name computed from the path
	 */
	public static String processResourceName(String uri) {
		String processedUri = "";
		String[] split = uri.replaceAll("\\{", "").replaceAll("\\}", "")
				.split("/");
		for (String str : split) {
			processedUri += RamlUtils.capFirst(str);
		}
		return processedUri;
	}

	/**
	 * Returns the relative path of a Resource given the base URI and its path
	 * 
	 * @param basePath
	 *            The base URI of the API
	 * @param path
	 *            The path of the Resource
	 * @return
	 */
	public static String cutBasePath(String basePath, String path) {
		return path.substring(basePath.length());
	}

	/**
	 * Indicates if the given type is a primitive type.
	 * 
	 * @param type
	 *            The type to be analysed
	 * @return A boolean of value true if the given type is primitive, false
	 *         otherwise.
	 */
	public static boolean isPrimitiveType(String type) {
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
	 * Generates the JsonSchema of a Representation
	 * 
	 * @param representation
	 *            The representation
	 * @return The JsonSchema of the Representation
	 */
	public static JsonSchema generateSchema(Representation representation) {
		SimpleTypeSchema propertySchema;
		ObjectSchema objectSchema;
		objectSchema = new ObjectSchema();
		objectSchema.setTitle(representation.getName());
		objectSchema.setDescription(representation.getDescription());
		if (!representation.isRaw()) {
			if (representation.getParentType() != null) {
				JsonSchema[] extended = new JsonSchema[1];
				SimpleTypeSchema typeExtended = new ObjectSchema();
				typeExtended.set$ref(representation.getParentType());
				extended[0] = typeExtended;
				objectSchema.setExtends(extended);
			}
			objectSchema.setProperties(new HashMap<String, JsonSchema>());
			for (Property property : representation.getProperties()) {
				if (property.getMaxOccurs() != 1) {
					ArraySchema array = new ArraySchema();
					array.setTitle(property.getName());
					if (property.getMinOccurs() > 0) {
						array.setRequired(true);
					}
					if (property.isUniqueItems()) {
						array.setUniqueItems(true);
					}
					if (isPrimitiveType(property.getType())) {
						Property prop = new Property();
						prop.setName(property.getName());
						prop.setType(property.getType());
						array.setItemsSchema(generatePrimitiveSchema(prop));
					} else {
						SimpleTypeSchema reference = new ObjectSchema();
						reference.set$ref("#/schemas/" + property.getType());
						array.setItemsSchema(reference);
						// array.setItemsSchema(generateSchema(RamlTranslater
						// .getRepresentationByName(representations,
						// property.getType()), representations));
					}
					objectSchema.getProperties().put(array.getTitle(), array);
				} else if (!isPrimitiveType(property.getType())) {
					propertySchema = new ObjectSchema();
					propertySchema.setTitle(property.getName());
					if (property.getMinOccurs() > 0) {
						propertySchema.setRequired(true);
					}
					objectSchema.getProperties().put(propertySchema.getTitle(),
							propertySchema);
				} else {
					SimpleTypeSchema primitive = generatePrimitiveSchema(property);
					if (property.getMinOccurs() > 0) {
						primitive.setRequired(true);
					}
					if (property.getDefaultValue() != null) {
						primitive.setDefault(property.getDefaultValue());
					}
					objectSchema.getProperties().put(property.getName(),
							primitive);
				}
			}

		}
		return objectSchema;
	}

	/**
	 * Generates the JsonSchema of a Representation's Property of primitive
	 * type.
	 * 
	 * @param property
	 *            The Property from which the JsonSchema is generated
	 * @return The JsonSchema ot the given Property
	 */
	public static SimpleTypeSchema generatePrimitiveSchema(Property property) {
		SimpleTypeSchema result = null;
		String name = property.getName();
		String type = property.getType();
		if (RamlUtils.integerTypesList.contains(type.toLowerCase())) {
			IntegerSchema integerSchema = new IntegerSchema();
			integerSchema.setTitle(name);
			if (property.getMin() != null) {
				integerSchema.setMinimum(Double.parseDouble(property.getMin()));
			}
			if (property.getMin() != null) {
				integerSchema.setMaximum(Double.parseDouble(property.getMax()));
			}
			result = integerSchema;
		} else if (RamlUtils.numberTypesList.contains(type.toLowerCase())) {
			NumberSchema numberSchema = new NumberSchema();
			numberSchema.setTitle(name);
			if (property.getMin() != null) {
				numberSchema.setMinimum(Double.parseDouble(property.getMin()));
			}
			if (property.getMin() != null) {
				numberSchema.setMaximum(Double.parseDouble(property.getMax()));
			}
			result = numberSchema;
		} else if ("boolean".equals(type.toLowerCase())) {
			BooleanSchema booleanSchema = new BooleanSchema();
			booleanSchema.setTitle(name);
			result = booleanSchema;
		} else if ("string".equals(type.toLowerCase())
				|| "date".equals(type.toLowerCase())) {
			StringSchema stringSchema = new StringSchema();
			stringSchema.setTitle(name);
			result = stringSchema;
		}
		return result;
	}
}
