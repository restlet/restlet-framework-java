package org.restlet.ext.apispark.internal.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.introspection.ApplicationIntrospector;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;

import com.wordnik.swagger.models.ArrayModel;
import com.wordnik.swagger.models.Contact;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.License;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.RefModel;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.HeaderParameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.properties.AbstractNumericProperty;
import com.wordnik.swagger.models.properties.AbstractProperty;
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
public class Swagger2Translator {

    public static final Float SWAGGER_2_0_VERSION = 2.0f;
    
    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(ApplicationIntrospector.class
            .getName());

    /**
     * Translates a Restlet Web API Definition to a Swagger definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @return Swagger The translated Swagger 2.0 definition
     */
    public static Swagger getSwagger(Definition definition) {

        // conversion
        Swagger swagger = new Swagger();
        swagger.setSwagger(SWAGGER_2_0_VERSION); // required

        // fill swagger main attributes
        fillMainAttributes(definition, swagger);

        // fill swagger.info
        fillInfo(definition, swagger); // required

        // fill swagger.paths
        fillPaths(definition, swagger); // required

        // fill swagger.definitions
        fillDefinitions(definition, swagger);

        // TODO add authorization attribute

        return swagger;
    }

    /**
     * Fills swagger main attributes from Restlet Web API definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param swagger
     *            The Swagger 2.0 definition
     */
    private static void fillMainAttributes(Definition definition,
            Swagger swagger) {
        // basePath
        Endpoint endpoint = definition.getEndpoints().get(0);
        swagger.setHost(endpoint.getDomain());
        swagger.setBasePath(endpoint.getBasePath());
        // TODO all schemes ?

        // Should be any of "http", "https", "ws", "wss"
        swagger.setSchemes(Arrays.asList(Scheme.forValue(endpoint.getProtocol())));

        // TODO check all
        // swagger.setConsumes();
        // swagger.setProduces();
        // swagger.parameters
        // swagger.responses
        // swagger.setSecurityDefinition();
    }

    /**
     * Fill swagger "Info" object from rwadef definition
     * 
     * @param definition
     *            rwadef definition
     * @param swagger
     *            swagger definition
     */
    private static void fillInfo(Definition definition, Swagger swagger) {
        Info infoSwagger = new Info();

        infoSwagger.setTitle(definition.getContract().getName()); // required
        infoSwagger.setDescription(definition.getContract().getDescription());
        infoSwagger.setVersion(definition.getVersion()); // required

        Contact contactSwagger = new Contact();
        if (definition.getContact() != null) {
            contactSwagger.setName(definition.getContact().getName());
            contactSwagger.setEmail(definition.getContact().getEmail());
            contactSwagger.setUrl(definition.getContact().getUrl());
        }
        infoSwagger.setContact(contactSwagger);

        License licenseSwagger = new License();
        if (definition.getLicense() != null) {
            if (!StringUtils.isNullOrEmpty(definition.getLicense().getName())) {
                org.restlet.ext.apispark.internal.model.License license = definition
                        .getLicense();
                licenseSwagger.setName(license.getName()); // required
                licenseSwagger.setUrl(license.getUrl());
                infoSwagger.setLicense(licenseSwagger);
            } else if (!StringUtils.isNullOrEmpty(definition.getLicense()
                    .getUrl())) {
                LOGGER.warning("You must specify a license name");
            }
        }

        swagger.setInfo(infoSwagger); // required
    }

    /**
     * Fill swagger "Paths" objects from rwadef definition
     * 
     * @param definition
     *            rwadef definition
     * @param swagger
     *            swagger definition
     */
    private static void fillPaths(Definition definition, Swagger swagger) {
        Map<String, Path> paths = new LinkedHashMap<String, Path>();

        for (Resource resource : definition.getContract().getResources()) {
            Path pathSwagger = new Path();

            fillPathOperations(definition, resource, pathSwagger);
            paths.put(resource.getResourcePath(), pathSwagger);
            // TODO path.parameters not in swagger bean - not in rwadef either
        }
        swagger.setPaths(paths);
    }

    /**
     * Fill swagger "Paths.Operations" objects from rwadef definition
     * 
     * @param definition
     *            rwadef definition
     * @param resource
     *            rwadef.resource definition
     * @param pathSwagger
     *            swagger.path definition
     */
    private static void fillPathOperations(Definition definition,
            Resource resource, Path pathSwagger) {
        for (Operation operation : resource.getOperations()) {

            com.wordnik.swagger.models.Operation operationSwagger = new com.wordnik.swagger.models.Operation();
            operationSwagger.setTags(new ArrayList<String>());
            operationSwagger.getTags().addAll(resource.getSections());

            String method = operation.getMethod().toLowerCase();
            Path setResult = pathSwagger.set(method, operationSwagger);
            if (setResult == null) {
                // TODO add error message in a conversion results bean
                return;
            }

            String description = operation.getDescription();

            if (description != null) {
                operationSwagger
                        .setSummary(description.length() > 120 ? description
                                .substring(0, 120) : description);
            }
            operationSwagger.setDescription(description);
            operationSwagger.setOperationId(operation.getName());
            operationSwagger.setConsumes(operation.getConsumes());
            operationSwagger.setProduces(operation.getProduces());
            // TODO add security
            // operationSwagger.setSecurity();

            fillOperationParameters(definition, resource, operation,
                    operationSwagger);

            fillOperationResponses(definition, operation, operationSwagger);
        }
    }

    private static void fillOperationParameters(Definition definition,
            Resource resource, Operation operation,
            com.wordnik.swagger.models.Operation operationSwagger) {

        // Path parameters
        for (PathVariable pathVariable : resource.getPathVariables()) {
            PathParameter pathParameterSwagger = new PathParameter();
            pathParameterSwagger.setType(SwaggerUtils.toSwaggerType(
                    pathVariable.getType()).getType()); // required
            pathParameterSwagger.setFormat(SwaggerUtils.toSwaggerType(
                    pathVariable.getType()).getFormat());
            pathParameterSwagger.setName(pathVariable.getName()); // required
            pathParameterSwagger.setDescription(pathVariable.getDescription());
            operationSwagger.addParameter(pathParameterSwagger);
        }

        // Body
        if (operation.getInputPayLoad() != null) {
            BodyParameter bodyParameterSwagger = new BodyParameter();
            bodyParameterSwagger.setName("body");

            // TODO no body parameter.description

            // TODO add Multipart FormData Parameters

            PayLoad inRepr = operation.getInputPayLoad();
            Representation representation = definition.getContract()
                    .getRepresentation(inRepr.getType());

            if (representation != null && representation.isRaw()) {
                ModelImpl modelImpl = new ModelImpl();
                modelImpl.setType("file");
                modelImpl.setDescription(representation.getDescription());
                bodyParameterSwagger.setSchema(modelImpl);
            } else {
                if (inRepr.isArray()) {
                    ArrayModel arrayModel = new ArrayModel();
                    arrayModel.setType("array");
                    // primitive or ref type
                    arrayModel.setItems(newPropertyForType(inRepr.getType()));
                    bodyParameterSwagger.setSchema(arrayModel);
                } else {
                    if (isPrimitiveType(inRepr.getType())) {
                        ModelImpl modelImpl = new ModelImpl();
                        modelImpl.setType(inRepr.getType());
                        bodyParameterSwagger.setSchema(modelImpl);
                    } else {
                        RefModel refModel = new RefModel();
                        refModel.set$ref(inRepr.getType());
                        bodyParameterSwagger.setSchema(refModel);
                    }
                }
            }
            operationSwagger.addParameter(bodyParameterSwagger);
        }

        // query parameters
        for (QueryParameter queryParameter : operation.getQueryParameters()) {
            com.wordnik.swagger.models.parameters.QueryParameter queryParameterSwagger = new com.wordnik.swagger.models.parameters.QueryParameter();
            queryParameterSwagger.setRequired(queryParameter.isRequired());
            if (queryParameter.isAllowMultiple()) {
                queryParameterSwagger.setType("array");
                queryParameterSwagger
                        .setItems(newPropertyForType(queryParameter.getType()));
                // do not set "csv" as it's the default format
                // queryParameterSwagger.setCollectionFormat("csv");
            } else {
                queryParameterSwagger.setType(SwaggerUtils.toSwaggerType(
                        queryParameter.getType()).getType());
                queryParameterSwagger.setFormat(SwaggerUtils.toSwaggerType(
                        queryParameter.getType()).getFormat());
            }
            queryParameterSwagger.setName(queryParameter.getName());
            queryParameterSwagger.setDescription(queryParameter
                    .getDescription());
            // TODO attributes above are not present in swagger bean
            // queryParameterSwagger.setDefaultValue(queryParameter.getDefault());
            // queryParameterSwagger.setEnumeration(queryParameter.getEnumeration());
            operationSwagger.addParameter(queryParameterSwagger);
        }

        for (Header header : operation.getHeaders()) {
            HeaderParameter headerParameterSwagger = new HeaderParameter();
            headerParameterSwagger.setRequired(header.isRequired());
            headerParameterSwagger.setType(SwaggerUtils.toSwaggerType(
                    header.getType()).getType());
            headerParameterSwagger.setFormat(SwaggerUtils.toSwaggerType(
                    header.getType()).getFormat());
            headerParameterSwagger.setName(header.getName());
            headerParameterSwagger.setDescription(header.getDescription());
            // TODO attributes above are not present in swagger bean
            // headerParameterSwagger.setDefaultValue(header.getDefault());
            // headerParameterSwagger.setEnumeration(header.getEnumeration());
            operationSwagger.addParameter(headerParameterSwagger);
        }
    }

    private static void fillOperationResponses(Definition definition,
            Operation operation,
            com.wordnik.swagger.models.Operation operationSwagger) {
        for (Response reponse : operation.getResponses()) {
            /* Response -> Response */
            com.wordnik.swagger.models.Response responseSwagger = new com.wordnik.swagger.models.Response();

            responseSwagger.setDescription(reponse.getDescription()); // required

            // Response Schema
            if (reponse.getOutputPayLoad() != null) {
                PayLoad entity = reponse.getOutputPayLoad();
                final Representation representation = definition.getContract()
                        .getRepresentation(entity.getType());

                if (representation != null && representation.isRaw()) {
                    // TODO wait for the swagger class
                    com.wordnik.swagger.models.properties.AbstractProperty fileProperty = new AbstractProperty() {
                        // anonymous class
                    };
                    fileProperty.setType("file");
                    fileProperty
                            .setDescription(representation.getDescription());
                    responseSwagger.setSchema(fileProperty);
                } else if (entity.isArray()) {
                    ArrayProperty arrayProperty = new ArrayProperty();
                    arrayProperty
                            .setItems(newPropertyForType(entity.getType()));
                    responseSwagger.setSchema(arrayProperty);
                } else {
                    responseSwagger.setSchema(newPropertyForType(entity
                            .getType()));
                }
            }

            operationSwagger.addResponse(String.valueOf(reponse.getCode()),
                    responseSwagger);

        }
        // TODO check that at least one success code is present
    }

    /**
     * Fill swagger "Definitions" objects from rwadef definition
     * 
     * @param definition
     *            rwadef definition
     * @param swagger
     *            swagger definition
     */
    private static void fillDefinitions(Definition definition, Swagger swagger) {
        for (Representation representation : definition.getContract()
                .getRepresentations()) {

            if (representation.isRaw()
                    || isPrimitiveType(representation.getName())) {
                continue;
            }

            /* Representation -> Model */
            ModelImpl modelSwagger = new ModelImpl();
            modelSwagger.setName(representation.getName());
            modelSwagger.setDescription(representation.getDescription());

            /* Property -> Property */
            for (Property property : representation.getProperties()) {

                com.wordnik.swagger.models.properties.Property propertySwagger;

                // property type
                if (property.getMaxOccurs() != null
                        && (property.getMaxOccurs() > 1 || property
                                .getMaxOccurs() == -1)) {
                    ArrayProperty arrayProperty = new ArrayProperty();
                    arrayProperty.setItems(newPropertyForType(property
                            .getType()));
                    propertySwagger = arrayProperty;
                } else {
                    propertySwagger = newPropertyForType(property.getType());
                }
                propertySwagger.setName(property.getName());
                propertySwagger.setDescription(property.getDescription());

                // min and max
                if (propertySwagger instanceof AbstractNumericProperty) {
                    AbstractNumericProperty abstractNumericProperty = (AbstractNumericProperty) propertySwagger;
                    try {
                        if (property.getMin() != null) {
                            abstractNumericProperty.setMinimum(Double
                                    .valueOf(property.getMin()));
                        }
                    } catch (NumberFormatException e) {
                        // TODO add error message in a conversion results bean
                    }
                    try {
                        if (property.getMax() != null) {
                            abstractNumericProperty.setMaximum(Double
                                    .valueOf(property.getMax()));
                        }
                    } catch (NumberFormatException e) {
                        // TODO add error message in a conversion results bean
                    }
                }
                modelSwagger.property(property.getName(), propertySwagger);
            }

            swagger.addDefinition(modelSwagger.getName(), modelSwagger);
        }
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
        return "string".equals(type.toLowerCase())
                || "int".equals(type.toLowerCase())
                || "integer".equals(type.toLowerCase())
                || "long".equals(type.toLowerCase())
                || "float".equals(type.toLowerCase())
                || "double".equals(type.toLowerCase())
                || "date".equals(type.toLowerCase())
                || "boolean".equals(type.toLowerCase())
                || "bool".equals(type.toLowerCase());
    }

    /**
     * Get new property for Swagger 2.0 for the primitive type of Rwadef.
     * 
     * @param type
     *            Type Rwadef
     * @return Type Swagger
     */
    private static com.wordnik.swagger.models.properties.Property newPropertyForType(
            String type) {
        if ("string".equals(type.toLowerCase())) {
            return new StringProperty();
        } else if ("int".equals(type.toLowerCase())) {
            return new IntegerProperty();
        } else if ("integer".equals(type.toLowerCase())) {
            return new IntegerProperty();
        } else if ("long".equals(type.toLowerCase())) {
            return new LongProperty();
        } else if ("float".equals(type.toLowerCase())) {
            return new FloatProperty();
        } else if ("double".equals(type.toLowerCase())) {
            return new DoubleProperty();
        } else if ("date".equals(type.toLowerCase())) {
            return new DateProperty();
        } else if ("boolean".equals(type.toLowerCase())) {
            return new BooleanProperty();
        } else if ("bool".equals(type.toLowerCase())) {
            return new BooleanProperty();
        }
        // Reference to a representation
        return new RefProperty(type);
    }

}
