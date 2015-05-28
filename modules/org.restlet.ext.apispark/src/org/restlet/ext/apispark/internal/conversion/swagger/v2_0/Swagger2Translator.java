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
import org.restlet.data.Method;
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
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.RefModel;
import com.wordnik.swagger.models.Response;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.Tag;
import com.wordnik.swagger.models.auth.ApiKeyAuthDefinition;
import com.wordnik.swagger.models.auth.BasicAuthDefinition;
import com.wordnik.swagger.models.auth.OAuth2Definition;
import com.wordnik.swagger.models.auth.SecuritySchemeDefinition;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.HeaderParameter;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.parameters.QueryParameter;
import com.wordnik.swagger.models.parameters.RefParameter;
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
import com.wordnik.swagger.models.properties.Property;
import com.wordnik.swagger.models.properties.RefProperty;
import com.wordnik.swagger.models.properties.StringProperty;

/**
 * Translator : RWADef <-> Swagger 2.0.
 */
public class Swagger2Translator {

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
    protected static Logger LOGGER = Logger.getLogger(Swagger2Translator.class
            .getName());

    public static final String SWAGGER_VERSION = "2.0";

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
        if (operation.getInputPayLoad() != null) {
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
            String message = response.getMessage();
            responseSwagger.setDescription(message == null
                    ? ConversionUtils.generateResponseName(response.getCode())
                            : message); // required

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

    /**
     * Translates a Swagger definition to a Restlet Web API Definition
     * 
     * @param Swagger
     *            The translated Swagger 2.0 definition
     * 
     * @return The Restlet Web API definition
     */
    public static Definition translate(Swagger swagger) {

        // conversion
        Definition definition = new Definition();

        // fill RWADef main attributes
        fillRwadefEndpoints(swagger, definition);

        List<String> produces = new ArrayList<>();
        List<String> consumes = new ArrayList<>();
        fillRwadefGeneralInformation(swagger, definition, produces, consumes);

        // fill definition.sections
        Contract contract = definition.getContract();
        fillSections(swagger, contract);

        // Get declared parameters
        Map<String, Object> parameters = new LinkedHashMap<>();
        fillDeclaredParameters(swagger, definition, parameters);

        // fill definition.representations
        fillRepresentations(swagger, contract);

        // fill definition.resources
        fillResources(swagger, contract, produces, consumes, parameters);

        for (Representation representation : contract.getRepresentations()) {
            representation.addSectionsToProperties(contract);
        }

        return definition;
    }

    private static void fillDeclaredParameters(Swagger swagger, Definition definition,
            Map<String, Object> parameters) {
        if (swagger.getParameters() == null) {
            return;
        }

        for (String key : swagger.getParameters().keySet()) {
            Parameter swaggerParameter = swagger.getParameters().get(key);
            if (swaggerParameter instanceof QueryParameter) {
                org.restlet.ext.apispark.internal.model.QueryParameter queryParameter =
                        new org.restlet.ext.apispark.internal.model.QueryParameter();
                fillRwadefQueryParameter(queryParameter, (QueryParameter) swaggerParameter);
                parameters.put(key, queryParameter);

            } else if (swaggerParameter instanceof PathParameter) {
                org.restlet.ext.apispark.internal.model.PathVariable pathVariable =
                        new org.restlet.ext.apispark.internal.model.PathVariable();
                fillRwadefPathVariable(pathVariable, (PathParameter) swaggerParameter);
                parameters.put(key, pathVariable);

            } else if (swaggerParameter instanceof HeaderParameter) {
                Header header = new Header();
                fillRwadefHeader(header, (HeaderParameter) swaggerParameter);
                parameters.put(key, header);

            } else if (swaggerParameter instanceof BodyParameter) {
                PayLoad payload = new PayLoad();
                fillPayload((BodyParameter) swaggerParameter, payload);
                parameters.put(key, payload);
            } else {
                LOGGER.warning("The type of the parameter " + key + " was not recognized: "
                        + swaggerParameter.getClass().getName());
            }
        }
    }

    private static void fillRwadefHeader(Header header, HeaderParameter swaggerHeader) {
        header.setName(swaggerHeader.getName());
        header.setRequired(swaggerHeader.getRequired());
        header.setDescription(swaggerHeader.getDescription());
        header.setAllowMultiple(true);
        header.setDefaultValue(swaggerHeader.getDefaultValue());
        // TODO: example not implemented in Swagger 2.0

        SwaggerTypeFormat swaggerTypeFormat = new SwaggerTypeFormat(
                swaggerHeader.getType(),
                swaggerHeader.getFormat(),
                swaggerHeader.getItems());
        header.setType(SwaggerTypes.toDefinitionPrimitiveType(swaggerTypeFormat));
    }

    private static void fillRwadefPathVariable(PathVariable pathVariable, PathParameter swaggerPathVariable) {
        pathVariable.setName(swaggerPathVariable.getName());
        pathVariable.setRequired(swaggerPathVariable.getRequired());
        pathVariable.setDescription(swaggerPathVariable.getDescription());
        // TODO: example not implemented in Swagger 2.0

        SwaggerTypeFormat swaggerTypeFormat = new SwaggerTypeFormat(
                swaggerPathVariable.getType(),
                swaggerPathVariable.getFormat(),
                swaggerPathVariable.getItems());
        pathVariable.setType(SwaggerTypes.toDefinitionPrimitiveType(swaggerTypeFormat));
    }

    /**
     * Fills the given RWADef query parameter information from the given Swagger query parameter.
     * 
     * @param queryParameter
     *            The RWADef query parameter.
     * @param swaggerQueryParameter
     *            The Swagger query parameter.
     */
    private static void fillRwadefQueryParameter(org.restlet.ext.apispark.internal.model.QueryParameter queryParameter,
            QueryParameter swaggerQueryParameter) {

        // TODO: allowMultiple not implemented in Swagger 2.0
        queryParameter.setAllowMultiple(true);
        queryParameter.setDefaultValue(swaggerQueryParameter.getDefaultValue());
        queryParameter.setDescription(swaggerQueryParameter.getDescription());
        queryParameter.setEnumeration(swaggerQueryParameter.getEnum());
        // TODO: example not implemented in Swagger 2.0
        queryParameter.setName(swaggerQueryParameter.getName());
        queryParameter.setRequired(swaggerQueryParameter.getRequired());
        queryParameter.setSeparator(SwaggerUtils.getSeparator(swaggerQueryParameter.getCollectionFormat()));

        SwaggerTypeFormat swaggerTypeFormat = new SwaggerTypeFormat(
                swaggerQueryParameter.getType(),
                swaggerQueryParameter.getFormat(),
                swaggerQueryParameter.getItems());
        queryParameter.setType(SwaggerTypes.toDefinitionPrimitiveType(swaggerTypeFormat));
    }

    private static void fillSections(Swagger swagger, Contract contract) {
        if (swagger.getTags() == null) {
            return;
        }

        for (Tag tag : swagger.getTags()) {
           Section section = new Section();
            section.setName(tag.getName());
            section.setDescription(tag.getDescription());
            contract.getSections().add(section);
        }
    }

    private static void fillRwadefGeneralInformation(Swagger swagger, Definition definition,
            List<String> produces, List<String> consumes) {

        Info info = swagger.getInfo();

        // Contact
        if (info.getContact() != null) {
            org.restlet.ext.apispark.internal.model.Contact contact = new org.restlet.ext.apispark.internal.model.Contact();
            contact.setEmail(info.getContact().getEmail());
            contact.setName(info.getContact().getName());
            contact.setUrl(info.getContact().getUrl());
            definition.setContact(contact);
        }

        if (info.getLicense() != null) {
            // License
            org.restlet.ext.apispark.internal.model.License license = new org.restlet.ext.apispark.internal.model.License();
            license.setName(info.getLicense().getName());
            license.setUrl(info.getLicense().getUrl());
            definition.setLicense(license);
        }

        // Contract
        Contract contract = new Contract();
        contract.setDescription(info.getDescription());
        contract.setName(info.getTitle());
        definition.setContract(contract);

        // Media types
        if (swagger.getProduces() != null) {
            produces.addAll(swagger.getProduces());
        }
        if (swagger.getConsumes() != null) {
            consumes.addAll(swagger.getConsumes());
        }

        // General
        definition.setVersion(info.getVersion());
        definition.setTermsOfService(info.getTermsOfService());
        // TODO: attribution and keywords not implemented in Swagger 2.0
        definition.setAttribution(null);
    }

    private static void fillRepresentations(Swagger swagger, Contract contract) {
        if (swagger.getDefinitions() == null) {
            return;
        }

        for (String key : swagger.getDefinitions().keySet()) {
            Model model = swagger.getDefinitions().get(key);
            Representation representation = new Representation();
            representation.setDescription(model.getDescription());
            representation.setName(key);
            representation.setRaw(false);
            // TODO: example not implemented in RWADef (built from properties examples)
            fillRwadefProperties(model, representation);
            contract.getRepresentations().add(representation);
        }
    }

    private static void fillRwadefProperties(Model model, Representation representation) {
        if (model.getProperties() == null) {
            return;
        }

        for (String key : model.getProperties().keySet()) {
            org.restlet.ext.apispark.internal.model.Property property =
                    new org.restlet.ext.apispark.internal.model.Property();
            Property swaggerProperty = model.getProperties().get(key);

            property.setDefaultValue(swaggerProperty.getDefault());
            property.setDescription(swaggerProperty.getDescription());
            // TODO: enumeration not implemented in Swagger 2.0
            property.setExample(swaggerProperty.getExample());
            property.setRequired(swaggerProperty.getRequired());
            property.setList(swaggerProperty instanceof ArrayProperty);
            property.setName(key);
            // TODO: sub-properties not implemented in Swagger 2.0
            // TODO: uniqueItems not implemented in Swagger 2.0
            property.setUniqueItems(false);

            if (swaggerProperty instanceof ArrayProperty) {
                ArrayProperty arrayProperty = (ArrayProperty) swaggerProperty;
                property.setExample(arrayProperty.getItems().getExample());
            }

            property.setType(SwaggerTypes.toDefinitionType(swaggerProperty));

            representation.getProperties().add(property);
        }
    }

    private static void fillResources(Swagger swagger, Contract contract,
            List<String> produces, List<String> consumes,
            Map<String, Object> parameters) {
        if (swagger.getPaths() == null) {
            return;
        }

        for (String key : swagger.getPaths().keySet()) {
            Resource resource = new Resource();
            Path path = swagger.getPath(key);

            // TODO: description not implemented in Swagger 2.0
            resource.setName(ConversionUtils.processResourceName(key));
            resource.setResourcePath(key);
            for (Operation operation : path.getOperations()) {
                resource.addSections(operation.getTags());
            }

            if (path.getParameters() != null) {
                for (Parameter parameter : path.getParameters()) {
                    PathVariable pathVariable = null;
                    if (parameter instanceof PathParameter) {
                        pathVariable = new PathVariable();
                        fillRwadefPathVariable(pathVariable, (PathParameter) parameter);
                    } else if (parameter instanceof RefParameter) {
                        RefParameter refParameter = (RefParameter) parameter;
                        Object savedParameter = parameters.get(refParameter.getSimpleRef());
                        if (savedParameter instanceof PathVariable) {
                            pathVariable = (PathVariable) savedParameter;
                        }
                    }

                    if (pathVariable != null) {
                        resource.getPathVariables().add(pathVariable);
                    }
                }
            }
            fillRwadefOperations(path, resource, contract, produces, consumes, parameters);

            contract.getResources().add(resource);
        }

    }

    private static void fillRwadefOperations(Path path, Resource resource,
            Contract contract, List<String> produces, List<String> consumes,
            Map<String, Object> parameters) {

        fillRwadefOperation(path.getGet(), resource, contract, Method.GET.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getPost(), resource, contract, Method.POST.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getPut(), resource, contract, Method.PUT.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getDelete(), resource, contract, Method.DELETE.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getOptions(), resource, contract, Method.OPTIONS.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getPatch(), resource, contract, Method.PATCH.getName(),
                produces, consumes, parameters);
    }

    private static void fillRwadefOperation(Operation swaggerOperation, Resource resource,
            Contract contract, String methodName,
            List<String> produces, List<String> consumes,
            Map<String, Object> parameters) {
        if (swaggerOperation == null) {
            return;
        }

        org.restlet.ext.apispark.internal.model.Operation operation =
                new org.restlet.ext.apispark.internal.model.Operation();

        operation.addProduces(produces);
        operation.addProduces(swaggerOperation.getProduces());

        operation.addConsumes(consumes);
        operation.addConsumes(swaggerOperation.getConsumes());

        operation.setDescription(swaggerOperation.getDescription());
        operation.setMethod(methodName);
        operation.setName(swaggerOperation.getOperationId());

        fillRwadefParameters(swaggerOperation, operation, resource, parameters);
        fillRwadefResponses(swaggerOperation, operation, contract, parameters);
        fillInputPayload(swaggerOperation, operation, contract);

        resource.getOperations().add(operation);
    }

    private static void fillRwadefParameters(Operation swaggerOperation,
            org.restlet.ext.apispark.internal.model.Operation operation, Resource resource,
            Map<String, Object> parameters) {
        if (swaggerOperation.getParameters() == null) {
            return;
        }

        for (Parameter swaggerParameter : swaggerOperation.getParameters()) {
            if (swaggerParameter instanceof QueryParameter) {
                org.restlet.ext.apispark.internal.model.QueryParameter queryParameter =
                        new org.restlet.ext.apispark.internal.model.QueryParameter();
                QueryParameter swaggerQueryParameter = (QueryParameter) swaggerParameter;

                fillRwadefQueryParameter(queryParameter, swaggerQueryParameter);
                operation.getQueryParameters().add(queryParameter);
            } else if (swaggerParameter instanceof PathParameter) {
                org.restlet.ext.apispark.internal.model.PathVariable pathVariable =
                        new org.restlet.ext.apispark.internal.model.PathVariable();
                PathParameter swaggerPathVariable = (PathParameter) swaggerParameter;

                fillRwadefPathVariable(pathVariable, swaggerPathVariable);
                if (resource.getPathVariable(pathVariable.getName()) == null) {
                    resource.getPathVariables().add(pathVariable);
                }
            } else if (swaggerParameter instanceof HeaderParameter) {
                Header header = new Header();
                HeaderParameter swaggerHeader = new HeaderParameter();

                fillRwadefHeader(header, swaggerHeader);
                operation.getHeaders().add(header);
            } else {
                LOGGER.warning("Unsupported parameter type for " + swaggerParameter.getName() +
                        " of type " + swaggerParameter.getClass().getName());
            }
        }
    }

    private static void fillRwadefResponses(Operation swaggerOperation,
            org.restlet.ext.apispark.internal.model.Operation operation,
            Contract contract, Map<String, Object> parameters) {
        if (swaggerOperation == null) {
            return;
        }

        for (String key : swaggerOperation.getResponses().keySet()) {
            Response swaggerResponse = swaggerOperation.getResponses().get(key);
            org.restlet.ext.apispark.internal.model.Response response =
                    new org.restlet.ext.apispark.internal.model.Response();

            int statusCode;
            try {
                statusCode = Integer.parseInt(key);
                response.setCode(statusCode);
            } catch (Exception e) {
                // TODO: what to do with "Default" responses ?
                LOGGER.warning("Response " + key + " for operation " + swaggerOperation.getOperationId() +
                        " could not be retrieved because its key is not a valid status code.");
                continue;
            }

            response.setMessage(swaggerResponse.getDescription());
            response.setName(ConversionUtils.generateResponseName(statusCode));

            fillOutputPayload(swaggerResponse, response, swaggerOperation, contract, parameters);

            operation.getResponses().add(response);
        }
    }

    private static void fillOutputPayload(Response swaggerResponse,
            org.restlet.ext.apispark.internal.model.Response response,
            Operation swaggerOperation, Contract contract, 
            Map<String, Object> parameters) {
        Property property = swaggerResponse.getSchema();
        if (property == null) {
            return;
        }

        PayLoad payload = null;
        
        if (property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            Object declaredPayload = parameters.get(refProperty.get$ref());
            if (declaredPayload != null
                    && declaredPayload instanceof PayLoad) {
                payload = (PayLoad) declaredPayload;
            }

        }

        if (payload == null) {
            payload = new PayLoad();
            payload.setDescription(property.getDescription());
            payload.setArray(property instanceof ArrayProperty);
            payload.setType(SwaggerTypes.toDefinitionType(property));

        }

        Representation representation = contract.getRepresentation(payload.getType());
        if (representation != null) {
            representation.addSections(swaggerOperation.getTags());
        }

        response.setOutputPayLoad(payload);
    }

    private static void fillInputPayload(Operation swaggerOperation,
            org.restlet.ext.apispark.internal.model.Operation operation,
            Contract contract) {
        BodyParameter bodyParameter = SwaggerUtils.getInputPayload(swaggerOperation);

        if (bodyParameter != null) {
            PayLoad payload = new PayLoad();
            fillPayload(bodyParameter, payload);

            Representation representation = contract.getRepresentation(payload.getType());
            if (representation != null) {
                representation.addSections(swaggerOperation.getTags());
            }
            operation.setInputPayLoad(payload);
        }
    }

    private static void fillPayload(BodyParameter bodyParameter, PayLoad payload) {
        Model model = bodyParameter.getSchema();
        payload.setDescription(model.getDescription());

        if (model instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) model;
            payload.setArray(true);
            payload.setType(SwaggerTypes.toDefinitionType(arrayModel.getItems()));

        } else if (model instanceof RefModel) {
            RefModel refModel = (RefModel) model;
            payload.setType(refModel.getSimpleRef());

        } else {
            // FIXME: should we fail ?
            LOGGER.warning("Unsupported input payload type: " + model.getClass().getSimpleName());
        }
    }

    private static void fillRwadefEndpoints(Swagger swagger, Definition definition) {
        String authenticationProtocol = null;

        if (swagger.getSecurityDefinitions() != null) {
            for (String key : swagger.getSecurityDefinitions().keySet()) {
                SecuritySchemeDefinition securityDefinition = swagger.getSecurityDefinitions().get(key);
                if (securityDefinition instanceof BasicAuthDefinition) {
                    authenticationProtocol = ChallengeScheme.HTTP_BASIC.getName();
                } else if (securityDefinition instanceof OAuth2Definition) {
                    authenticationProtocol = ChallengeScheme.HTTP_OAUTH.getName();
                } else if (securityDefinition instanceof ApiKeyAuthDefinition) {
                    authenticationProtocol = ChallengeScheme.CUSTOM.getName();
                }
            }
        }

        if (swagger.getSchemes() != null
            && swagger.getHost() != null) {
            for (Scheme scheme : swagger.getSchemes()) {
                Endpoint endpoint = new Endpoint(scheme.toString().toLowerCase() + "://" + swagger.getHost()
                        + (swagger.getBasePath() == null ? "" : swagger.getBasePath()));
                endpoint.setAuthenticationProtocol(authenticationProtocol);
                definition.getEndpoints().add(endpoint);
            }
        }
    }
}
