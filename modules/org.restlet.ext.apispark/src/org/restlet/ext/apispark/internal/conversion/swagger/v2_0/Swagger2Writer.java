/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.ext.apispark.internal.conversion.swagger.v2_0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.conversion.ConversionUtils;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.utils.SampleUtils;

import com.wordnik.swagger.models.ArrayModel;
import com.wordnik.swagger.models.Contact;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.License;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.RefModel;
import com.wordnik.swagger.models.Response;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.Tag;
import com.wordnik.swagger.models.auth.BasicAuthDefinition;
import com.wordnik.swagger.models.auth.SecuritySchemeDefinition;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.HeaderParameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.properties.AbstractNumericProperty;
import com.wordnik.swagger.models.properties.AbstractProperty;
import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.BooleanProperty;
import com.wordnik.swagger.models.properties.DateProperty;
import com.wordnik.swagger.models.properties.DoubleProperty;
import com.wordnik.swagger.models.properties.FileProperty;
import com.wordnik.swagger.models.properties.FloatProperty;
import com.wordnik.swagger.models.properties.IntegerProperty;
import com.wordnik.swagger.models.properties.LongProperty;
import com.wordnik.swagger.models.properties.RefProperty;
import com.wordnik.swagger.models.properties.StringProperty;

/**
 * Translator : RWADef -> Swagger 2.0.
 */
public class Swagger2Writer {

    // TODO wait for Swagger class
    private static class ByteProperty extends AbstractProperty {
        private ByteProperty() {
            setType("string");
            setFormat("byte");
        }
    }

    // TODO wait for Swagger class
    private static class ShortProperty extends AbstractProperty {
        private ShortProperty() {
            setType("integer");
            setFormat("int32"); // int16 not supported
        }
    }

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(Swagger2Writer.class
            .getName());

    public static final String SWAGGER_VERSION = "2.0";

    /**
     * Fill Swagger "Definitions" objects from RWADef definition
     * 
     * @param definition
     *            RWADef definition
     * @param swagger
     *            Swagger definition
     */
    private static void fillDefinitions(Definition definition, Swagger swagger) {
        for (Representation representation : definition.getContract()
                .getRepresentations()) {

            if (representation.isRaw()
                    || Types.isPrimitiveType(representation.getName())) {
                continue;
            }

            if (StringUtils.isNullOrEmpty(representation.getName())) {
                LOGGER.warning("A representation should have an identifier:"
                        + representation.getName());
                continue;
            }

            /* Representation -> Model */
            ModelImpl modelSwagger = new ModelImpl();

            fillModel(representation.getName(),
                    representation.getDescription(),
                    representation.getProperties(), swagger, modelSwagger);
        }
    }

    /**
     * Fill Swagger "Model" objects from RWADef.
     * 
     * @param name
     *            The name of the Swagger model.
     * @param description
     *            the description of the Swagger model.
     * @param properties
     *            The list of RWADef properties of the Swagger model.
     * @param swagger
     *            The Swagger definition.
     * @param modelSwagger
     *            The Swagger model.
     */
    private static void fillModel(String name, String description,
            List<org.restlet.ext.apispark.internal.model.Property> properties,
            Swagger swagger, ModelImpl modelSwagger) {
        modelSwagger.setName(name);
        modelSwagger.setDescription(description);

        /* Property -> Property */
        for (org.restlet.ext.apispark.internal.model.Property property : properties) {

            com.wordnik.swagger.models.properties.Property propertySwagger;

            Object exampleObject = SampleUtils.getPropertyExampleValue(property);
            String example = exampleObject == null ? null : exampleObject
                    .toString();

            // property type
            if (property.isList()) {
                ArrayProperty arrayProperty = new ArrayProperty();
                com.wordnik.swagger.models.properties.Property itemProperty;
                if (Types.isCompositeType(property.getType())) {
                    String compositePropertyType = name + StringUtils.firstUpper(property.getName());
                    itemProperty = newPropertyForType(compositePropertyType);
                    // List of properties -> Model */
                    ModelImpl ms = new ModelImpl();
                    fillModel(
                            compositePropertyType,
                            null, property.getProperties(), swagger, ms);
                } else {
                    itemProperty = newPropertyForType(property.getType());
                }
                itemProperty.setExample(example);
                arrayProperty.setItems(itemProperty);
                propertySwagger = arrayProperty;
            } else {
                if (Types.isCompositeType(property.getType())) {
                    String compositePropertyType = name + StringUtils.firstUpper(property.getName());
                    propertySwagger = newPropertyForType(compositePropertyType);
                    // List of properties -> Model */
                    ModelImpl ms = new ModelImpl();
                    fillModel(
                            compositePropertyType,
                            null, property.getProperties(), swagger, ms);
                    propertySwagger.setExample(example);
                } else {
                    propertySwagger = newPropertyForType(property.getType());
                    propertySwagger.setExample(example);
                }
            }
            propertySwagger.setName(property.getName());
            propertySwagger.setDescription(property.getDescription());
            propertySwagger.setRequired(property.isRequired());

            // min and max
            if (propertySwagger instanceof AbstractNumericProperty) {
                AbstractNumericProperty abstractNumericProperty = (AbstractNumericProperty) propertySwagger;
                try {
                    if (property.getMin() != null) {
                        abstractNumericProperty.setMinimum(Double
                                .valueOf(property.getMin()));
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warning("Min property is not a number: "
                            + property.getMin());
                }
                try {
                    if (property.getMax() != null) {
                        abstractNumericProperty.setMaximum(Double
                                .valueOf(property.getMax()));
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warning("Max property is not a number: "
                            + property.getMax());
                }
            }
            modelSwagger.property(property.getName(), propertySwagger);
        }

        swagger.addDefinition(modelSwagger.getName(), modelSwagger);
    }

    private static void fillOperationParameters(Definition definition,
            Resource resource, org.restlet.ext.apispark.internal.model.Operation operation,
            Operation operationSwagger) {

        // Path parameters
        for (PathVariable pathVariable : resource.getPathVariables()) {
            PathParameter pathParameterSwagger = new PathParameter();
            SwaggerTypeFormat swaggerTypeFormat = SwaggerTypes
                    .toSwaggerType(pathVariable.getType());
            pathParameterSwagger.setType(swaggerTypeFormat.getType()); // required
            pathParameterSwagger.setFormat(swaggerTypeFormat.getFormat());
            pathParameterSwagger.setName(pathVariable.getName()); // required
            pathParameterSwagger.setDescription(pathVariable.getDescription());
            // TODO: add when implemented
            // pathParameterSwagger.setDefaultValue(pathVariable.getDefaultValue());
            operationSwagger.addParameter(pathParameterSwagger);
        }

        // Body
        if (operation.getInputPayLoad() != null
                && operation.getInputPayLoad().getType() != null) {
            BodyParameter bodyParameterSwagger = new BodyParameter();
            bodyParameterSwagger.setName("body");

            PayLoad inRepr = operation.getInputPayLoad();
            Representation representation = definition.getContract()
                    .getRepresentation(inRepr.getType());

            if (representation != null && representation.isRaw()) {
                ModelImpl modelImpl = new ModelImpl();
                modelImpl.setType(representation.getName());
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
                    if (Types.isPrimitiveType(inRepr.getType())) {
                        ModelImpl modelImpl = new ModelImpl();
                        modelImpl.setType(inRepr.getType());
                        bodyParameterSwagger.setSchema(modelImpl);
                    } else {
                        RefModel refModel = new RefModel();
                        refModel.asDefault(inRepr.getType());
                        bodyParameterSwagger.setSchema(refModel);
                    }
                }
            }
            operationSwagger.addParameter(bodyParameterSwagger);
        }

        // query parameters
        for (org.restlet.ext.apispark.internal.model.QueryParameter queryParameter : operation.getQueryParameters()) {
            com.wordnik.swagger.models.parameters.QueryParameter queryParameterSwagger = new com.wordnik.swagger.models.parameters.QueryParameter();
            queryParameterSwagger.setRequired(queryParameter.isRequired());
            queryParameterSwagger.setDefaultValue(queryParameter
                    .getDefaultValue());
            if (queryParameter.isAllowMultiple()) {
                queryParameterSwagger.setType("array");
                queryParameterSwagger
                        .setItems(newPropertyForType(queryParameter.getType()));
                // do not set "csv" as it's the default format
                // queryParameterSwagger.setCollectionFormat("csv");
            } else {
                queryParameterSwagger.setType(SwaggerTypes.toSwaggerType(
                        queryParameter.getType()).getType());
                queryParameterSwagger.setFormat(SwaggerTypes.toSwaggerType(
                        queryParameter.getType()).getFormat());
            }
            queryParameterSwagger.setName(queryParameter.getName());
            queryParameterSwagger.setDescription(queryParameter
                    .getDescription());
            operationSwagger.addParameter(queryParameterSwagger);
        }

        for (Header header : operation.getHeaders()) {
            HeaderParameter headerParameterSwagger = new HeaderParameter();
            headerParameterSwagger.setRequired(header.isRequired());
            headerParameterSwagger.setDefaultValue(header.getDefaultValue());
            headerParameterSwagger.setType(SwaggerTypes.toSwaggerType(
                    header.getType()).getType());
            headerParameterSwagger.setFormat(SwaggerTypes.toSwaggerType(
                    header.getType()).getFormat());
            headerParameterSwagger.setName(header.getName());
            headerParameterSwagger.setDescription(header.getDescription());
            operationSwagger.addParameter(headerParameterSwagger);
        }
    }

    private static void fillOperationResponses(Definition definition,
            org.restlet.ext.apispark.internal.model.Operation operation,
            Operation operationSwagger) {
        for (org.restlet.ext.apispark.internal.model.Response response : operation.getResponses()) {
            /* Response -> Response */
            Response responseSwagger = new Response();

            // may be null
            String description = response.getDescription();
            responseSwagger.setDescription(description == null
                    ? ConversionUtils.generateResponseName(response.getCode())
                            : description); // required

            // Response Schema
            if (response.getOutputPayLoad() != null
                    && response.getOutputPayLoad().getType() != null) {
                PayLoad entity = response.getOutputPayLoad();
                final Representation representation = definition.getContract()
                        .getRepresentation(entity.getType());

                if (representation != null && representation.isRaw()) {
                    FileProperty fileProperty = new FileProperty();
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

            operationSwagger.addResponse(String.valueOf(response.getCode()),
                    responseSwagger);

        }
        // TODO check that at least one success code is present
    }

    /**
     * Fill Swagger "Paths.Operations" objects from RWADef definition
     * 
     * @param definition
     *            RWADef definition
     * @param resource
     *            RWADef.resource definition
     * @param pathSwagger
     *            Swagger.path definition
     */
    private static void fillPathOperations(Definition definition,
            Resource resource, Path pathSwagger) {
        for (org.restlet.ext.apispark.internal.model.Operation operation : resource.getOperations()) {

            com.wordnik.swagger.models.Operation operationSwagger = new com.wordnik.swagger.models.Operation();
            operationSwagger.setTags(new ArrayList<String>());
            operationSwagger.getTags().addAll(resource.getSections());

            String method = operation.getMethod().toLowerCase();
            Path setResult = pathSwagger.set(method, operationSwagger);
            if (setResult == null) {
                LOGGER.warning("Method not supported:" + method);
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

    /**
     * Fill Swagger "Paths" objects from RWADef definition
     * 
     * @param definition
     *            RWADef definition
     * @param swagger
     *            Swagger definition
     */
    private static void fillPaths(Definition definition, Swagger swagger) {
        Map<String, Path> paths = new LinkedHashMap<>();

        for (Resource resource : definition.getContract().getResources()) {
            Path pathSwagger = new Path();

            fillPathOperations(definition, resource, pathSwagger);
            paths.put(resource.getResourcePath(), pathSwagger);
        }
        swagger.setPaths(paths);
    }

    /**
     * Fill Swagger "SecuritySchemeDefinition" objects from RWADef definition
     * 
     * @param definition
     *            RWADef definition
     * @param swagger
     *            Swagger definition
     */
    private static void fillSwaggerAuthentication(Definition definition, Swagger swagger) {
        Map<String, SecuritySchemeDefinition> securitySchemes = new LinkedHashMap<>();

        // Supported schemes
        String httpBasic = ChallengeScheme.HTTP_BASIC.getName();

        for (Endpoint endpoint : definition.getEndpoints()) {

            if (httpBasic.equals(endpoint.getAuthenticationProtocol())) {
                securitySchemes.put(httpBasic, new BasicAuthDefinition());
            }
        }
        swagger.setSecurityDefinitions(securitySchemes.isEmpty() ? null : securitySchemes);
    }

    /**
     * Fill Swagger "Info" object from RWADef definition
     * 
     * @param definition
     *            RWADef definition
     * @param swagger
     *            Swagger definition
     */
    private static void fillSwaggerInfo(Definition definition, Swagger swagger) {
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
     * Fills Swagger main attributes from Restlet Web API definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param swagger
     *            The Swagger 2.0 definition
     */
    private static void fillSwaggerMainAttributes(Definition definition,
            Swagger swagger) {
        // basePath
        if (definition.getEndpoints() != null
                && !definition.getEndpoints().isEmpty()) {
            Endpoint endpoint = definition.getEndpoints().get(0);
            swagger.setHost(endpoint.getDomain()
                    + (endpoint.getPort() == null ? "" : (":" + endpoint
                            .getPort())));
            swagger.setBasePath(endpoint.getBasePath());
            // Should be any of "http", "https", "ws", "wss"
            swagger.setSchemes(Arrays.asList(Scheme.forValue(endpoint
                    .getProtocol())));
        }
    }

    private static void fillTags(Contract contract, Swagger swagger) {
        if (contract.getSections() == null) {
            return;
        }
        swagger.setTags(new ArrayList<Tag>());

        for (Section section : contract.getSections()) {
            Tag tag = new Tag();
            tag.setName(section.getName());
            tag.setDescription(section.getDescription());
            swagger.getTags().add(tag);
        }
    }

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
        swagger.setSwagger(SWAGGER_VERSION); // required

        // fill Swagger main attributes
        fillSwaggerMainAttributes(definition, swagger);

        // fill authentication information
        fillSwaggerAuthentication(definition, swagger);

        // fill Swagger.info
        fillSwaggerInfo(definition, swagger); // required

        // fill Swagger.tags
        fillTags(definition.getContract(), swagger); // required

        // fill Swagger.paths
        fillPaths(definition, swagger); // required

        // fill Swagger.definitions
        fillDefinitions(definition, swagger);

        // TODO add authorization attribute

        return swagger;
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

        switch (type.toLowerCase()) {
        case "string":
            return new StringProperty();
        case "byte":
            return new ByteProperty();
        case "short":
            return new ShortProperty();
        case "integer":
            return new IntegerProperty();
        case "long":
            return new LongProperty();
        case "float":
            return new FloatProperty();
        case "double":
            return new DoubleProperty();
        case "date":
            return new DateProperty();
        case "boolean":
            return new BooleanProperty();
        }

        // Reference to a representation
        return new RefProperty().asDefault(type);
    }
}
