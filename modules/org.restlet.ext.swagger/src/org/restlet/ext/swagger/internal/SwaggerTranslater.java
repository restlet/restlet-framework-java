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

package org.restlet.ext.swagger.internal;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.model.Contract;
import org.restlet.ext.apispark.model.Definition;
import org.restlet.ext.apispark.model.Entity;
import org.restlet.ext.apispark.model.Operation;
import org.restlet.ext.apispark.model.PathVariable;
import org.restlet.ext.apispark.model.Property;
import org.restlet.ext.apispark.model.QueryParameter;
import org.restlet.ext.apispark.model.Representation;
import org.restlet.ext.apispark.model.Resource;
import org.restlet.ext.apispark.model.Response;
import org.restlet.ext.swagger.internal.model.ApiDeclaration;
import org.restlet.ext.swagger.internal.model.ApiInfo;
import org.restlet.ext.swagger.internal.model.ItemsDeclaration;
import org.restlet.ext.swagger.internal.model.ModelDeclaration;
import org.restlet.ext.swagger.internal.model.ResourceDeclaration;
import org.restlet.ext.swagger.internal.model.ResourceListing;
import org.restlet.ext.swagger.internal.model.ResourceOperationDeclaration;
import org.restlet.ext.swagger.internal.model.ResourceOperationParameterDeclaration;
import org.restlet.ext.swagger.internal.model.ResponseMessageDeclaration;
import org.restlet.ext.swagger.internal.model.TypePropertyDeclaration;
import org.restlet.ext.swagger.internal.reflect.ReflectUtils;

/**
 * Tool library for converting Restlet Web API Definition to and from Swagger
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerTranslater {

	/** Internal logger. */
	protected static Logger LOGGER = Logger.getLogger(SwaggerTranslater.class
			.getName());

	/** Supported version of Swagger. */
	private static final String SWAGGER_VERSION = "1.2";

	/**
	 * Translates a Swagger documentation to a Restlet definition.
	 * 
	 * @param resourceListing
	 *            The Swagger resource listing.
	 * @param apiDeclarations
	 *            The list of Swagger API declarations.
	 * @return The Restlet definition.
	 * @throws TranslationException
	 */
	public static Definition translate(ResourceListing resourceListing,
			Map<String, ApiDeclaration> apiDeclarations)
			throws TranslationException {

		validate(resourceListing, apiDeclarations);

		boolean containsRawTypes = false;
		List<String> declaredTypes = new ArrayList<String>();
		List<String> declaredPathVariables;
		Map<String, List<String>> subtypes = new HashMap<String, List<String>>();

		try {
			Definition definition = new Definition();
			definition.setVersion(resourceListing.getApiVersion());
			definition.setContact(resourceListing.getInfo().getContact());
			definition.setLicense(resourceListing.getInfo().getLicenseUrl());
			Contract contract = new Contract();
			contract.setName(resourceListing.getInfo().getTitle());
			LOGGER.log(Level.FINE, "Contract " + contract.getName() + " added.");
			contract.setDescription(resourceListing.getInfo().getDescription());
			definition.setContract(contract);

			// Resource listing
			Resource resource;
			for (Entry<String, ApiDeclaration> entry : apiDeclarations
					.entrySet()) {
				ApiDeclaration swagApiDeclaration = entry.getValue();
				List<String> apiProduces = swagApiDeclaration.getProduces();
				List<String> apiConsumes = swagApiDeclaration.getConsumes();

				for (ResourceDeclaration api : swagApiDeclaration.getApis()) {
					declaredPathVariables = new ArrayList<String>();
					resource = new Resource();
					resource.setResourcePath(api.getPath());

					// Operations listing
					Operation operation;
					for (ResourceOperationDeclaration swagOperation : api
							.getOperations()) {
						String methodName = swagOperation.getMethod();
						operation = new Operation();
						operation.setMethod(swagOperation.getMethod());
						operation.setName(swagOperation.getNickname());
						operation.setDescription(swagOperation.getSummary());

						// Set variants
						Representation representation;
						for (String produced : apiProduces.isEmpty() ? swagOperation
								.getProduces() : apiProduces) {
							if (!containsRawTypes
									&& MediaType.MULTIPART_FORM_DATA.getName()
											.equals(produced)) {
								representation = new Representation();
								representation.setName("File");
								representation.setRaw(true);
								containsRawTypes = true;
								contract.getRepresentations().add(
										representation);
							}
							operation.getProduces().add(produced);
						}
						for (String consumed : apiConsumes.isEmpty() ? swagOperation
								.getConsumes() : apiConsumes) {
							if (!containsRawTypes
									&& MediaType.MULTIPART_FORM_DATA.getName()
											.equals(consumed)) {
								representation = new Representation();
								representation.setName("File");
								representation.setRaw(true);
								containsRawTypes = true;
								contract.getRepresentations().add(
										representation);
							}
							operation.getConsumes().add(consumed);
						}

						// Set response's entity
						Entity rwadOutRepr = new Entity();
						if ("array".equals(swagOperation.getType())) {
							LOGGER.log(Level.FINER, "Operation: "
									+ swagOperation.getNickname()
									+ " returns an array");
							rwadOutRepr.setArray(true);
							if (swagOperation.getItems().getType() != null) {
								rwadOutRepr.setType(swagOperation.getItems()
										.getType());
							} else {
								rwadOutRepr.setType(swagOperation.getItems()
										.getRef());
							}
						} else {
							LOGGER.log(Level.FINER, "Operation: "
									+ swagOperation.getNickname()
									+ " returns a single Representation");
							rwadOutRepr.setArray(false);
							if (swagOperation.getType() != null) {
								rwadOutRepr.setType(swagOperation.getType());
							} else {
								rwadOutRepr.setType(swagOperation.getRef());
							}
						}
						operation.setOutRepresentation(rwadOutRepr);

						// Extract success response message
						Response success = new Response();
						success.setCode(Status.SUCCESS_OK.getCode());
						success.setEntity(rwadOutRepr);
						success.setDescription("Success");
						success.setMessage(Status.SUCCESS_OK.getDescription());
						success.setName("Success");
						operation.getResponses().add(success);

						// Loop over Swagger parameters.
						for (ResourceOperationParameterDeclaration param : swagOperation
								.getParameters()) {
							if ("path".equals(param.getParamType())) {
								if (!declaredPathVariables.contains(param
										.getName())) {
									declaredPathVariables.add(param.getName());
									PathVariable pathVariable = toPathVariable(param);
									resource.getPathVariables().add(
											pathVariable);
								}
							} else if ("body".equals(param.getParamType())) {
								if (operation.getInRepresentation() == null) {
									Entity rwadInRepr = toEntity(param);
									operation.setInRepresentation(rwadInRepr);
								}
							} else if ("query".equals(param.getParamType())) {
								QueryParameter rwadQueryParam = toQueryParameter(param);
								operation.getQueryParameters().add(
										rwadQueryParam);
							}
						}

						// Set error response messages
						if (swagOperation.getResponseMessages() != null) {
							for (ResponseMessageDeclaration swagResponse : swagOperation
									.getResponseMessages()) {
								Response response = new Response();
								Entity entity = new Entity();
								entity.setType(swagResponse.getResponseModel());
								response.setEntity(entity);
								response.setName("Error "
										+ swagResponse.getCode());
								response.setCode(swagResponse.getCode());
								response.setMessage(swagResponse.getMessage());
								operation.getResponses().add(response);
							}
						}

						resource.getOperations().add(operation);
						LOGGER.log(Level.FINE, "Method " + methodName
								+ " added.");

						// Add representations
						for (Entry<String, ModelDeclaration> modelEntry : swagApiDeclaration
								.getModels().entrySet()) {
							ModelDeclaration model = modelEntry.getValue();
							if (model.getSubTypes() != null
									&& !model.getSubTypes().isEmpty()) {
								subtypes.put(model.getId(), model.getSubTypes());
							}
							if (!declaredTypes.contains(modelEntry.getKey())) {
								declaredTypes.add(modelEntry.getKey());
								Representation rwadRepr = toRepresentation(
										model, modelEntry.getKey());
								contract.getRepresentations().add(rwadRepr);
								LOGGER.log(Level.FINE, "Representation "
										+ modelEntry.getKey() + " added.");
							}
						}

						// Deal with subtyping
						for (Entry<String, List<String>> subtypesPair : subtypes
								.entrySet()) {
							List<String> subtypesOf = subtypesPair.getValue();
							for (String subtypeOf : subtypesOf) {
								Representation repr = getRepresentationByName(
										contract, subtypeOf);
								repr.setExtendedType(subtypesPair.getKey());
							}
						}
					}

					definition.getContract().getResources().add(resource);
					LOGGER.log(Level.FINE, "Resource " + api.getPath()
							+ " added.");
				}

				if (definition.getEndpoint() == null) {
					definition.setEndpoint(swagApiDeclaration.getBasePath());
				}
			}
			LOGGER.log(Level.FINE,
					"Definition successfully retrieved from Swagger definition");
			return definition;
		} catch (Exception e) {
			if (e instanceof FileNotFoundException) {
				throw new TranslationException("file",
						((FileNotFoundException) e).getMessage());
			} else {
				throw new TranslationException("compliance",
						"Impossible to read your API definition, check your Swagger specs compliance");
			}
		}
	}

	/**
	 * Retrieves the Swagger API declaration corresponding to a category of the
	 * given Restlet Web API Definition
	 * 
	 * @param category
	 *            The category of the API declaration
	 * @param definition
	 *            The Restlet Web API Definition
	 * @return The Swagger API definition of the given category
	 */
	public static ApiDeclaration getApiDeclaration(String category,
			Definition definition) {
		ApiDeclaration result = new ApiDeclaration();
		result.setApiVersion(definition.getVersion());
		result.setBasePath(definition.getEndpoint());
		result.setInfo(new ApiInfo());
		result.setSwaggerVersion(SWAGGER_VERSION);
		result.setResourcePath("/" + category);
		Set<String> usedModels = new HashSet<String>();

		// Get resources
		for (Resource resource : definition.getContract().getResources()) {
			// Discriminate the resources of one category
			if (!resource.getResourcePath().startsWith("/" + category)) {
				continue;
			}
			ResourceDeclaration rd = new ResourceDeclaration();
			rd.setPath(resource.getResourcePath());
			rd.setDescription(resource.getDescription());

			// Get operations
			for (Operation operation : resource.getOperations()) {
				ResourceOperationDeclaration rod = new ResourceOperationDeclaration();
				rod.setMethod(operation.getMethod());
				rod.setSummary(operation.getDescription());
				rod.setNickname(operation.getName());
				rod.setProduces(operation.getProduces());
				rod.setConsumes(operation.getConsumes());

				// Get path variables
				ResourceOperationParameterDeclaration ropd;
				for (PathVariable pv : resource.getPathVariables()) {
					ropd = new ResourceOperationParameterDeclaration();
					ropd.setParamType("path");
					ropd.setType(toSwaggerType(pv.getType()));
					ropd.setRequired(true);
					ropd.setName(pv.getName());
					ropd.setAllowMultiple(false);
					ropd.setDescription(pv.getDescription());
					rod.getParameters().add(ropd);
				}

				// Get in representation
				Entity inRepr = operation.getInRepresentation();
				if (inRepr != null) {
					ropd = new ResourceOperationParameterDeclaration();
					ropd.setParamType("body");
					ropd.setRequired(true);
					if ("Representation".equals(inRepr.getType())) {
						ropd.setType("File");
					} else {
						ropd.setType(toSwaggerType(inRepr.getType()));
					}
					if (inRepr.getType() != null) {
						usedModels.add(inRepr.getType());
					}
					rod.getParameters().add(ropd);
				}

				// Get out representation
				Entity outRepr = operation.getOutRepresentation();
				if (outRepr != null && outRepr.getType() != null) {
					if (outRepr.isArray()) {
						rod.setType("array");
						if (isPrimitiveType(outRepr.getType())) {
							rod.getItems().setType(
									toSwaggerType(outRepr.getType()));
						} else {
							rod.getItems().setRef(outRepr.getType());
						}
					} else {
						rod.setType(toSwaggerType(outRepr.getType()));
					}
					usedModels.add(outRepr.getType());
				} else {
					rod.setType("void");
				}

				// Get query parameters
				for (QueryParameter qp : operation.getQueryParameters()) {
					ropd = new ResourceOperationParameterDeclaration();
					ropd.setParamType("query");
					ropd.setType(toSwaggerType(qp.getType()));
					ropd.setName(qp.getName());
					ropd.setAllowMultiple(true);
					ropd.setDescription(qp.getDescription());
					ropd.setEnum_(qp.getEnumeration());
					ropd.setDefaultValue(qp.getDefaultValue());
					rod.getParameters().add(ropd);
				}

				// Get response messages
				for (Response response : operation.getResponses()) {
					if (Status.isSuccess(response.getCode())) {
						continue;
					}
					ResponseMessageDeclaration rmd = new ResponseMessageDeclaration();
					rmd.setCode(response.getCode());
					rmd.setMessage(response.getMessage());
					if (response.getEntity() != null) {
						rmd.setResponseModel(response.getEntity().getType());
					}
					rod.getResponseMessages().add(rmd);
				}

				rd.getOperations().add(rod);
			}
			result.getApis().add(rd);
		}

		result.setModels(new TreeMap<String, ModelDeclaration>());
		Iterator<String> iterator = usedModels.iterator();
		while (iterator.hasNext()) {
			String model = iterator.next();
			Representation repr = getRepresentationByName(
					definition.getContract(), model);
			if (repr == null || isPrimitiveType(model)) {
				continue;
			}
			ModelDeclaration md = new ModelDeclaration();
			md.setId(model);
			md.setDescription(repr.getDescription());
			for (Property prop : repr.getProperties()) {
				if (prop.getMinOccurs() > 0) {
					md.getRequired().add(prop.getName());
				}
				if (!isPrimitiveType(prop.getType())
						&& !usedModels.contains(prop.getType())) {
					usedModels.add(prop.getType());
					iterator = usedModels.iterator();
				}
				TypePropertyDeclaration tpd = new TypePropertyDeclaration();
				tpd.setDescription(prop.getDescription());
				tpd.setEnum_(prop.getEnumeration());

				if (prop.getMaxOccurs() > 1 || prop.getMaxOccurs() == -1) {
					tpd.setType("array");
					tpd.setItems(new ItemsDeclaration());
					if (isPrimitiveType(prop.getType())) {
						tpd.getItems().setType(toSwaggerType(prop.getType()));
					} else {
						tpd.getItems().setRef(prop.getType());
					}
				} else {
					if (isPrimitiveType(prop.getType())) {
						tpd.setType(toSwaggerType(prop.getType()));
					} else {
						tpd.setRef(prop.getType());
					}
				}
				tpd.setMaximum(prop.getMax());
				tpd.setMinimum(prop.getMin());
				tpd.setUniqueItems(prop.isUniqueItems());

				md.getProperties().put(prop.getName(), tpd);
			}
			result.getModels().put(md.getId(), md);
		}

		// Sort the API declarations according to their path.
		Collections.sort(result.getApis(),
				new Comparator<ResourceDeclaration>() {
					@Override
					public int compare(ResourceDeclaration o1,
							ResourceDeclaration o2) {
						return o1.getPath().compareTo(o2.getPath());
					}
				});
		return result;
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
	 * Translates a Restlet Web API Definition to a Swagger resource listing.
	 * 
	 * @param definition
	 *            The Restlet Web API Definition.
	 * @return The corresponding resource listing
	 */
	public static ResourceListing getResourcelisting(Definition definition) {
		ResourceListing result = new ResourceListing();

		// common properties
		result.setApiVersion(definition.getVersion());
		// result.setBasePath(definition.getEndpoint());
		result.setInfo(new ApiInfo());
		result.setSwaggerVersion(SWAGGER_VERSION);
		if (definition.getContact() != null) {
			result.getInfo().setContact(definition.getContact());
		}
		if (definition.getLicense() != null) {
			result.getInfo().setLicenseUrl(definition.getLicense());
		}
		if (definition.getContract() != null) {
			result.getInfo().setTitle(definition.getContract().getName());
			result.getInfo().setDescription(
					definition.getContract().getDescription());
		}
		// Resources
		List<String> addedApis = new ArrayList<String>();
		if (definition.getContract() != null
				&& definition.getContract().getResources() != null) {
			result.setApis(new ArrayList<ResourceDeclaration>());

			for (Resource resource : definition.getContract().getResources()) {
				ResourceDeclaration rd = new ResourceDeclaration();
				rd.setDescription(resource.getDescription());
				rd.setPath(ReflectUtils.getFirstSegment(resource
						.getResourcePath()));
				if (!addedApis.contains(rd.getPath())) {
					addedApis.add(rd.getPath());
					result.getApis().add(rd);
				}
			}
		}
		Collections.sort(result.getApis(),
				new Comparator<ResourceDeclaration>() {
					@Override
					public int compare(ResourceDeclaration o1,
							ResourceDeclaration o2) {
						return o1.getPath().compareTo(o2.getPath());
					}

				});
		return result;
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
	 * Converts a Swagger parameter to an instance of {@link Entity}.
	 * 
	 * @param parameter
	 *            The Swagger parameter.
	 * @return An instance of {@link Entity}.
	 */
	private static Entity toEntity(
			ResourceOperationParameterDeclaration parameter) {
		Entity result = new Entity();
		if ("array".equals(parameter.getType())) {
			result.setArray(true);
			if (parameter.getItems() != null
					&& parameter.getItems().getType() != null) {
				result.setType(parameter.getItems().getType());
			} else if (parameter.getItems() != null) {
				result.setType(parameter.getItems().getRef());
			}
		} else {
			result.setArray(false);
			result.setType(parameter.getType());
		}
		return result;
	}

	/**
	 * Converts a Swagger parameter to an instance of {@link PathVariable}.
	 * 
	 * @param parameter
	 *            The Swagger parameter.
	 * @return An instance of {@link PathVariable}.
	 */
	private static PathVariable toPathVariable(
			ResourceOperationParameterDeclaration parameter) {
		PathVariable result = new PathVariable();
		result.setName(parameter.getName());
		result.setDescription(parameter.getDescription());
		result.setType(toRwadefType(parameter.getType()));
		result.setArray(parameter.isAllowMultiple());
		return result;
	}

	/**
	 * Converts a Swagger parameter to an instance of {@link QueryParameter}.
	 * 
	 * @param parameter
	 *            The Swagger parameter.
	 * @return An instance of {@link QueryParameter}.
	 */
	private static QueryParameter toQueryParameter(
			ResourceOperationParameterDeclaration parameter) {
		QueryParameter result = new QueryParameter();
		result.setName(parameter.getName());
		result.setDescription(parameter.getDescription());
		result.setRequired(parameter.isRequired());
		result.setAllowMultiple(parameter.isAllowMultiple());
		result.setDefaultValue(parameter.getDefaultValue());
		if (parameter.getEnum_() != null && !parameter.getEnum_().isEmpty()) {
			result.setEnumeration(new ArrayList<String>());
			for (String value : parameter.getEnum_()) {
				result.getEnumeration().add(value);
			}
		}
		return result;
	}

	/**
	 * Converts a Swagger model to an instance of {@link Representation}.
	 * 
	 * @param model
	 *            The Swagger model.
	 * @param name
	 *            The name of the representation.
	 * @return An instance of {@link Representation}.
	 */
	private static Representation toRepresentation(ModelDeclaration model,
			String name) {
		Representation result = new Representation();
		result.setName(name);
		result.setDescription(model.getDescription());

		// Set properties
		for (Entry<String, TypePropertyDeclaration> swagProperties : model
				.getProperties().entrySet()) {
			TypePropertyDeclaration swagProperty = swagProperties.getValue();
			Property property = new Property();
			property.setName(swagProperties.getKey());

			// Set property's type
			boolean isArray = "array".equals(swagProperty.getType());
			if (isArray) {
				property.setType(swagProperty.getItems().getType() != null ? swagProperty
						.getItems().getType() : swagProperty.getItems()
						.getRef());
			} else if (swagProperty.getType() != null) {
				property.setType(swagProperty.getType());
			} else if (swagProperty.getRef() != null) {
				property.setType(swagProperty.getRef());
			}

			if (model.getRequired() != null) {
				property.setMinOccurs(model.getRequired().contains(
						swagProperties.getKey()) ? 1 : 0);
			} else {
				property.setMinOccurs(0);
			}
			property.setMaxOccurs(isArray ? -1 : 1);
			property.setDescription(swagProperty.getDescription());
			property.setMin(swagProperty.getMinimum());
			property.setMax(swagProperty.getMaximum());
			property.setUniqueItems(swagProperty.isUniqueItems());

			result.getProperties().add(property);
			LOGGER.log(Level.FINE, "Property " + property.getName() + " added.");
		}
		return result;
	}

	/**
	 * Returns the primitive types as Swagger expects them
	 * 
	 * @param type
	 *            The type name to Swaggerize
	 * @return The Swaggerized type
	 */
	private static String toSwaggerType(String type) {
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
	 * Returns the primitive types as RWADef expects them
	 * 
	 * @param type
	 *            The type name to Swaggerize
	 * @return The Swaggerized type
	 */
	private static String toRwadefType(String type) {
		if ("int".equals(type)) {
			return "Integer";
		} else if ("string".equals(type)) {
			return "String";
		} else if ("boolean".equals(type)) {
			return "Boolean";
		} else {
			return type;
		}
	}

	/**
	 * Indicates if the given resource listing and list of API declarations
	 * match.
	 * 
	 * @param resourceListing
	 *            The Swagger resource listing.
	 * @param apiDeclarations
	 *            The list of Swagger API declarations.
	 * @throws TranslationException
	 */
	private static void validate(ResourceListing resourceListing,
			Map<String, ApiDeclaration> apiDeclarations)
			throws TranslationException {
		int rlSize = resourceListing.getApis().size();
		int adSize = apiDeclarations.size();
		if (rlSize < adSize) {
			throw new TranslationException("file",
					"One of your API declarations is not mapped in your resource listing");
		} else if (rlSize > adSize) {
			throw new TranslationException("file",
					"Some API declarations are missing");
		}
	}

	/**
	 * Private constructor to ensure that the class acts as a true utility class
	 * i.e. it isn't instantiable and extensible.
	 */
	private SwaggerTranslater() {
	}
}