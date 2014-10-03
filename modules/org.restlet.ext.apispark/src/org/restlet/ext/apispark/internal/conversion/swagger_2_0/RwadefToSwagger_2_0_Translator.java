package org.restlet.ext.apispark.internal.conversion.swagger_2_0;

import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Entity;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;

import com.wordnik.swagger.models.Contact;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.License;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.properties.AbstractNumericProperty;
import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.BooleanProperty;
import com.wordnik.swagger.models.properties.DateProperty;
import com.wordnik.swagger.models.properties.DoubleProperty;
import com.wordnik.swagger.models.properties.FloatProperty;
import com.wordnik.swagger.models.properties.IntegerProperty;
import com.wordnik.swagger.models.properties.LongProperty;
import com.wordnik.swagger.models.properties.RefProperty;
import com.wordnik.swagger.models.properties.StringProperty;

/**
 * Translator : RWADEF <-> Swagger 2.0.
 */
public class RwadefToSwagger_2_0_Translator {

	public static final Float SWAGGER_VERSION = Float.valueOf("2.0");

	/**
	 * Rwadef -> Swagger 2.0
	 * @param definition Rwadef
	 * @return Swagger 2.0
	 */
	public Swagger getSwaggerFromRoadef(Definition definition) {
		Swagger swagger = new Swagger();

		swagger.setSwagger(SWAGGER_VERSION);

		/* Info -> Swagger */
		Info infoSwagger = new Info();
		swagger.setInfo(infoSwagger);

		infoSwagger.setVersion(definition.getVersion());

		Contact contact = new Contact();
		contact.setName(definition.getContact());
		infoSwagger.setContact(contact);

		License license = new License();
		license.setUrl(definition.getLicense());
		infoSwagger.setLicense(license);

		if (definition.getContract() != null) {
			infoSwagger.setTitle(definition.getContract().getName());
			infoSwagger.setDescription(
					definition.getContract().getDescription());
		}

		// basePath
		if (!definition.getEndpoints().isEmpty()) {
			Endpoint endpoint = definition.getEndpoints().get(0);
			swagger.setBasePath(endpoint.computeUrl());
		} else {
			// TODO Log : no base path
		}

		/* Resource -> Path */
		if((definition.getContract() != null) && (definition.getContract().getResources() != null)) {
			for(Resource resourceRwadef : definition.getContract().getResources()) {

				Path pathSwagger = new Path();
				swagger.path(resourceRwadef.getResourcePath(), pathSwagger);

				/* Operation -> Operation */
				if(resourceRwadef.getOperations() != null) {
					for (Operation operationRwadef : resourceRwadef.getOperations()) {

						com.wordnik.swagger.models.Operation operationSwagger =
								new com.wordnik.swagger.models.Operation();
						pathSwagger.set(operationRwadef.getMethod(), operationSwagger);

						operationSwagger.setOperationId(operationRwadef.getName());
						operationSwagger.setProduces(operationRwadef.getProduces());
						operationSwagger.setConsumes(operationRwadef.getConsumes());
						operationSwagger.setSummary(operationRwadef.getDescription());
						operationSwagger.setDescription(operationRwadef.getDescription());
						operationSwagger.setProduces(operationRwadef.getProduces());

						/* PathVariable -> PathParameter */
						if(resourceRwadef.getPathVariables() != null) {
							for (PathVariable pathVariableRwadef : resourceRwadef.getPathVariables()) {
								PathParameter pathParameterSwagger = new PathParameter();
								operationSwagger.addParameter(pathParameterSwagger);

								pathParameterSwagger.setType(toSwaggerType(pathVariableRwadef.getType()));
								pathParameterSwagger.setRequired(true);
								pathParameterSwagger.setName(pathVariableRwadef.getName());
								pathParameterSwagger.setDescription(pathVariableRwadef.getDescription());
							}
						}

						/* InRepresentation -> BodyParameter */
						Entity inRepr = operationRwadef.getInRepresentation();
						if (inRepr != null) {
							BodyParameter bodyParameterSwagger = new BodyParameter();
							operationSwagger.addParameter(bodyParameterSwagger);

							ModelImpl schema = new ModelImpl();
							bodyParameterSwagger.setSchema(schema);
							if ("Representation".equals(inRepr.getType())) {
								schema.setType("file");
							} else {
								schema.setType(toSwaggerType(inRepr.getType()));
							}
						}

						/* QueryParameter -> QueryParameter */
						if(operationRwadef.getQueryParameters() != null) {
							for (QueryParameter queryParameterRwadef : operationRwadef.getQueryParameters()) {
								com.wordnik.swagger.models.parameters.QueryParameter queryParameterSwagger =
										new com.wordnik.swagger.models.parameters.QueryParameter();
								operationSwagger.addParameter(queryParameterSwagger);

								queryParameterSwagger.setRequired(false);
								queryParameterSwagger.setType(toSwaggerType(queryParameterRwadef.getType()));
								queryParameterSwagger.setName(queryParameterRwadef.getName());
								queryParameterSwagger.setDescription(queryParameterRwadef.getDescription());
							}
						}

						/* Response -> Response */
						if(operationRwadef.getResponses() != null) {
							for (Response responseRwadef : operationRwadef.getResponses()) {
								com.wordnik.swagger.models.Response responseSwagger =
										new com.wordnik.swagger.models.Response();
								operationSwagger.addResponse(
										String.valueOf(responseRwadef.getCode()),
										responseSwagger);

								responseSwagger.setDescription(responseRwadef.getDescription());

								// Response Schema
								if((responseRwadef.getEntity() != null) && (responseRwadef.getEntity().getType() != null) ) {
									Entity entity = responseRwadef.getEntity();
									if(entity.isArray()) {
										ArrayProperty arrayProperty = new ArrayProperty();
										arrayProperty.setItems(newPropertyForType(entity.getType()));
										responseSwagger.setSchema(arrayProperty);
									} else {
										responseSwagger.setSchema(newPropertyForType(entity.getType()));
									}
								}
							}
						}

					}
				}

			}
		}

		if((definition.getContract() != null) && (definition.getContract().getRepresentations() != null)) {
			for(Representation representationRwadef : definition.getContract().getRepresentations()) {
				if (isPrimitiveType(representationRwadef.getName())) {
					continue;
				}

				/* Representation -> Model */
				ModelImpl modelSwagger = new ModelImpl();
				modelSwagger.setName(representationRwadef.getName());
				swagger.addDefinition(modelSwagger.getName(), modelSwagger);

				/* Property -> Property */
				if(representationRwadef.getProperties() != null) {
					for (Property propertyRwadef : representationRwadef.getProperties()) {

						com.wordnik.swagger.models.properties.Property propertySwagger;

						// property type
						if ((propertyRwadef.getMaxOccurs() > 1) || (propertyRwadef.getMaxOccurs() == -1)) {
							ArrayProperty arrayProperty = new ArrayProperty();
							arrayProperty.setItems(newPropertyForType(propertyRwadef.getType()));
							propertySwagger = arrayProperty;
						} else {
							propertySwagger = newPropertyForType(propertyRwadef.getType());
						}

						// min and max
						if(propertySwagger instanceof AbstractNumericProperty) {
							AbstractNumericProperty abstractNumericProperty = (AbstractNumericProperty) propertySwagger;
							try {
								abstractNumericProperty.setMinimum(Double.valueOf(propertyRwadef.getMin()));
							} catch(NumberFormatException e) {
								// TODO Add log
							}
							try {
								abstractNumericProperty.setMaximum(Double.valueOf(propertyRwadef.getMax()));
							} catch(NumberFormatException e) {
								// TODO Add log
							}
						}

						propertySwagger.setDescription(propertyRwadef.getDescription());

						modelSwagger.addProperty(propertyRwadef.getName(), propertySwagger);
					}
				}

			}
		}

		/* Authorization -> SecurityDefinition */
		// TODO

		return swagger;
	}

	/**
	 * Indicates if the given type is a primitive type.
	 * 
	 * @param type
	 *            The type to be analysed
	 * @return A boolean of value true if the given type is primitive, false
	 *         otherwise.
	 */
	private boolean isPrimitiveType(String type) {
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
	 * Returns the primitive types as Swagger expects them
	 * 
	 * @param type
	 *            The type name to Swaggerize
	 * @return The Swaggerized type
	 */
	private String toSwaggerType(String type) {
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
	 * Get new property for Swagger 2.0 for the primitive type of Rwadef.
	 * @param type Type Rwadef
	 * @return Type Swagger
	 */
	private com.wordnik.swagger.models.properties.Property newPropertyForType(String type) {
		if ("string".equals(type.toLowerCase())) {
			return new StringProperty();
		} else if("int".equals(type.toLowerCase())) {
			return new IntegerProperty();
		} else if("integer".equals(type.toLowerCase())) {
			return new IntegerProperty();
		} else if("long".equals(type.toLowerCase())) {
			return new LongProperty();
		} else if("float".equals(type.toLowerCase())) {
			return new FloatProperty();
		} else if("double".equals(type.toLowerCase())) {
			return new DoubleProperty();
		} else if("date".equals(type.toLowerCase())) {
			return new DateProperty();
		} else if("boolean".equals(type.toLowerCase())) {
			return new BooleanProperty();
		} else if("bool".equals(type.toLowerCase())) {
			return new BooleanProperty();
		}
		// Reference to a representation
		return new RefProperty(type);
	}

}
