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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.restlet.ext.raml.internal.model.Contract;
import org.restlet.ext.raml.internal.model.Definition;
import org.restlet.ext.raml.internal.model.Operation;
import org.restlet.ext.raml.internal.model.PathVariable;
import org.restlet.ext.raml.internal.model.Representation;
import org.restlet.ext.raml.internal.model.Resource;

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
		if (raml.getVersion() != null) {
			def.setVersion(raml.getVersion().substring(1));
			def.setEndpoint(raml.getBaseUri().replace("{version}",
					raml.getVersion()));
		} else {
			def.setEndpoint(raml.getBaseUri());
		}
		def.setContract(new Contract());
		def.getContract().setName(raml.getTitle());
		// TODO String defaultMediaType = raml.getMediaType();
		List<PathVariable> rootPathVariables = new ArrayList<PathVariable>();
		for (Entry<String, UriParameter> entry : raml.getBaseUriParameters()
				.entrySet()) {
			rootPathVariables.add(getPathVariable(entry.getKey(),
					entry.getValue()));
		}

		for (Map<String, String> schema : raml.getSchemas()) {
			for (Entry<String, String> entry : schema.entrySet()) {
				Representation representation = new Representation();
				representation.setName(entry.getKey());
				representation.setDescription(entry.getValue());
				// TODO get the schema !!!
				def.getContract().getRepresentations().add(representation);
			}
		}

		// Resources
		for (Entry<String, org.raml.model.Resource> entry : raml.getResources()
				.entrySet()) {
			org.raml.model.Resource resource = entry.getValue();
			def.getContract()
					.getResources()
					.addAll(getResource(processResourceName(resource.getUri()),
							resource, rootPathVariables));
		}

		return def;
	}

	private static List<Resource> getResource(String resourceName,
			org.raml.model.Resource resource,
			List<PathVariable> rootPathVariables) {
		List<Resource> rwadResources = new ArrayList<Resource>();

		// Create one resource
		Resource rwadResource = new Resource();
		rwadResource.setDescription(resource.getDescription());
		rwadResource.setName(resourceName);
		rwadResource.setResourcePath(resource.getUri());

		// Path Variables
		rwadResource.setPathVariables(getPathVariables(resource));
		rwadResource.getPathVariables().addAll(rootPathVariables);

		// Operations
		for (Entry<ActionType, Action> entry : resource.getActions().entrySet()) {
			Action action = entry.getValue();
			Operation operation = new Operation();
			operation.setDescription(action.getDescription());
			operation.setMethod(entry.getKey().name().toString());
		}

		rwadResources.add(rwadResource);

		// Nested resources
		for (Entry<String, org.raml.model.Resource> entry : resource
				.getResources().entrySet()) {
			rwadResources
					.addAll(getResource(processResourceName(entry.getValue()
							.getUri()), entry.getValue(), rootPathVariables));
		}

		return rwadResources;
	}

	private static String processResourceName(String uri) {
		String processedUri = "";
		String[] split = uri.replaceAll("\\{", "").replaceAll("\\}", "").split("/");
		for (String str : split) {
			processedUri.concat(RamlUtils.capFirst(str));
		}
		return processedUri;
	}

	private static PathVariable getPathVariable(String paramName,
			UriParameter uriParameter) {
		PathVariable pathVariable = new PathVariable();
		pathVariable.setName(paramName);
		pathVariable.setDescription(uriParameter.getDescription());
		pathVariable.setType(uriParameter.getType().toString().toLowerCase());
		pathVariable.setArray(uriParameter.isRepeat());
		return pathVariable;
	}

	private static List<PathVariable> getPathVariables(
			org.raml.model.Resource resource) {
		List<PathVariable> pathVariables = new ArrayList<PathVariable>();
		for (Entry<String, UriParameter> entry : resource.getUriParameters()
				.entrySet()) {
			pathVariables
					.add(getPathVariable(entry.getKey(), entry.getValue()));
		}
		if (resource.getParentResource() != null) {
			pathVariables
					.addAll(getPathVariables(resource.getParentResource()));
		}
		return pathVariables;
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
		raml = new RamlDocumentBuilder()
				.build("file:///home/cyp/Bureau/hello_world.raml");
		org.raml.model.Resource resource = raml.getResource("/products");
		System.out.println(resource.getActions().get(ActionType.POST));
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
	 * Private constructor to ensure that the class acts as a true utility class
	 * i.e. it isn't instantiable and extensible.
	 */
	private RamlConverter() {
	}
}