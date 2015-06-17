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

package org.restlet.ext.apispark.internal.conversion.swagger.v1_2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ApiDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.AuthorizationsDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ModelDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListing;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListingApi;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceOperationDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceOperationParameterDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResponseMessageDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.TypePropertyDeclaration;
import org.restlet.ext.apispark.internal.model.Contact;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.License;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;

/**
 * Tool library for converting Restlet Web API Definition from Swagger
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public class SwaggerReader {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(SwaggerReader.class
            .getName());

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SwaggerReader() {
    }

    /**
     * Fills Restlet Web API definition's Contract from Swagger 1.2 {@link ApiDeclaration}.
     * 
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param apiDeclaration
     *            The Swagger ApiDeclaration
     * @param declaredTypes
     *            The names of the representations already imported into the {@link Contract}.
     * @param sectionName
     *            Optional name of the section in which to import the {@link ApiDeclaration}.
     * @param sectionDescription
     *            Optional description of the section in which to import the {@link ApiDeclaration}.
     */
    private static void fillContract(Contract contract, ApiDeclaration apiDeclaration,
            List<String> declaredTypes, String sectionName, String sectionDescription) {
        Resource resource;
        Section section = null;

        if (!StringUtils.isNullOrEmpty(sectionName)) {
            section = new Section(sectionName);
            section.setDescription(sectionDescription);
            contract.getSections().add(section);
        }

        for (ResourceDeclaration api : apiDeclaration.getApis()) {
            resource = new Resource();
            resource.setResourcePath(api.getPath());

            List<String> declaredPathVariables = new ArrayList<>();
            fillOperations(resource, apiDeclaration, api, contract, section,
                    declaredPathVariables, declaredTypes);

            if (section != null) {
                resource.getSections().add(section.getName());
            }

            contract.getResources().add(resource);
            LOGGER.log(Level.FINE, "Resource " + api.getPath() + " added.");
        }
    }

    /**
     * Fills Restlet Web API definition's Contract from Swagger 1.2 definition
     * 
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param listing
     *            The Swagger ResourceListing
     * @param apiDeclarations
     *            The Swagger ApiDeclaration
     */
    private static void fillContract(Contract contract,
            ResourceListing listing, Map<String, ApiDeclaration> apiDeclarations) {

        List<String> declaredTypes = new ArrayList<>();
        for (Entry<String, ApiDeclaration> entry : apiDeclarations.entrySet()) {
            ApiDeclaration apiDeclaration = entry.getValue();

            String sectionName = entry.getKey();
            if (!StringUtils.isNullOrEmpty(sectionName)) {
                fillContract(contract, apiDeclaration, declaredTypes,
                        sectionName.startsWith("/") ? sectionName.substring(1) : sectionName,
                        listing.getApi(sectionName).getDescription());
            } else {
                fillContract(contract, apiDeclaration, declaredTypes, null, null);
            }
        }
    }

    /**
     * Fills Restlet Web API definition's main attributes from Swagger 1.2
     * definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param listing
     *            The Swagger 1.2 resource listing
     * @param basePath
     *            The basePath of the described Web API
     */
    private static void fillMainAttributes(Definition definition,
            ResourceListing listing, String basePath) {
        definition.setVersion(listing.getApiVersion());

        Contract contract = new Contract();
        if (listing.getInfo() != null) {
            Contact contact = new Contact();
            contact.setEmail(listing.getInfo().getContact());
            definition.setContact(contact);

            License license = new License();
            license.setName(listing.getInfo().getLicense());
            license.setUrl(listing.getInfo().getLicenseUrl());
            definition.setLicense(license);

            contract.setName(listing.getInfo().getTitle());
            contract.setDescription(listing.getInfo().getDescription());
        }

        LOGGER.log(Level.FINE, "Contract " + contract.getName() + " added.");
        definition.setContract(contract);

        if (definition.getEndpoints().isEmpty() && basePath != null) {
            // TODO verify how to deal with API key auth + oauth
            Endpoint endpoint = new Endpoint(basePath);
            definition.getEndpoints().add(endpoint);
            fillEndpointAuthorization(listing.getAuthorizations(), endpoint);
        }
    }

    private static void fillEndpointAuthorization(AuthorizationsDeclaration authorizations, Endpoint endpoint) {
        if (authorizations != null) {
            if (authorizations.getBasicAuth() != null) {
                endpoint.setAuthenticationProtocol(ChallengeScheme.HTTP_BASIC
                        .getName());
            } else if (authorizations.getOauth2() != null) {
                endpoint.setAuthenticationProtocol(ChallengeScheme.HTTP_OAUTH
                        .getName());
            } else if (authorizations.getApiKey() != null) {
                endpoint.setAuthenticationProtocol(ChallengeScheme.CUSTOM
                        .getName());
            }
        }
    }

    /**
     * Fills Restlet Web API definition's Operations from Swagger ApiDeclaration
     * 
     * @param resource
     *            The Restlet Web API definition's Resource
     * @param apiDeclaration
     *            The Swagger ApiDeclaration
     * @param api
     *            The Swagger ResourceDeclaration
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param section
     *            The Restlet Web API definition's current Section
     * @param declaredPathVariables
     *            The list of all declared path variables for the Resource
     * @param declaredTypes
     *            The list of all declared types for the Contract
     */
    private static void fillOperations(Resource resource,
            ApiDeclaration apiDeclaration, ResourceDeclaration api,
            Contract contract, Section section,
            List<String> declaredPathVariables, List<String> declaredTypes) {

        List<String> apiProduces = apiDeclaration.getProduces();
        List<String> apiConsumes = apiDeclaration.getConsumes();
        Map<String, List<String>> subtypes = new LinkedHashMap<>();
        Representation representation;

        // Operations listing
        Operation operation;
        for (ResourceOperationDeclaration swaggerOperation : api
                .getOperations()) {
            String methodName = swaggerOperation.getMethod();
            operation = new Operation();
            operation.setMethod(swaggerOperation.getMethod());
            operation.setName(swaggerOperation.getNickname());
            operation.setDescription(swaggerOperation.getSummary());

            // fill produced and consumed variants
            fillVariants(operation, swaggerOperation,
                    apiProduces, apiConsumes);

            // Extract success response message
            Response success = new Response();
            success.setCode(Status.SUCCESS_OK.getCode());
            success.setDescription("Success");
            success.setMessage(Status.SUCCESS_OK.getDescription());
            success.setName("Success");

            // fill output payload
            fillOutPayLoad(success, swaggerOperation);
            operation.getResponses().add(success);

            // fill parameters
            fillParameters(resource, operation, swaggerOperation,
                    declaredPathVariables);

            // fill responses
            fillResponseMessages(operation, swaggerOperation);

            resource.getOperations().add(operation);
            LOGGER.log(Level.FINE, "Method " + methodName + " added.");

            // fill representations
            fillRepresentations(contract, section, apiDeclaration, subtypes,
                    declaredTypes);

            // Deal with subtyping
            for (Entry<String, List<String>> subtypesPair : subtypes.entrySet()) {
                List<String> subtypesOf = subtypesPair.getValue();
                for (String subtypeOf : subtypesOf) {
                    representation = contract.getRepresentation(subtypeOf);
                    representation.setExtendedType(subtypesPair.getKey());
                }
            }
        }
    }

    /**
     * Fills Restlet Web API definition's operation output payload from Swagger
     * ResourceOperationDeclaration
     * 
     * @param success
     *            The Restlet Web API definition's operation success Response
     * @param swaggerOperation
     *            The Swagger ResourceOperationDeclaration
     */
    private static void fillOutPayLoad(Response success,
            ResourceOperationDeclaration swaggerOperation) {
        // Set response's entity
        PayLoad rwadOutRepr = new PayLoad();
        if ("array".equals(swaggerOperation.getType())) {
            LOGGER.log(Level.FINER,
                    "Operation: " + swaggerOperation.getNickname()
                            + " returns an array");
            rwadOutRepr.setArray(true);
            if (swaggerOperation.getItems().getType() != null) {
                rwadOutRepr.setType(swaggerOperation.getItems().getType());
            } else {
                rwadOutRepr.setType(swaggerOperation.getItems().getRef());
            }
        } else {
            LOGGER.log(Level.FINER,
                    "Operation: " + swaggerOperation.getNickname()
                            + " returns a single Representation");
            rwadOutRepr.setArray(false);
            if (swaggerOperation.getType() != null
                    && !"void".equals(swaggerOperation.getType())) {
                rwadOutRepr.setType(swaggerOperation.getType());
            } else {
                rwadOutRepr.setType(swaggerOperation.getRef());
            }
        }
        success.setOutputPayLoad(rwadOutRepr);
    }

    /**
     * Fills Restlet Web API definition's operation parameter from Swagger
     * ResourceOperationDeclaration
     * 
     * @param resource
     *            The Restlet Web API definition's Resource to which the
     *            operation is attached
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param swaggerOperation
     *            The Swagger ResourceOperationDeclaration
     * @param declaredPathVariables
     *            The list of declared pathVariable on the resource
     */
    private static void fillParameters(Resource resource, Operation operation,
            ResourceOperationDeclaration swaggerOperation,
            List<String> declaredPathVariables) {
        // Loop over Swagger parameters.
        for (ResourceOperationParameterDeclaration param : swaggerOperation
                .getParameters()) {
            if ("path".equals(param.getParamType())) {
                if (!declaredPathVariables.contains(param.getName())) {
                    declaredPathVariables.add(param.getName());
                    PathVariable pathVariable = toPathVariable(param);
                    resource.getPathVariables().add(pathVariable);
                }
            } else if ("body".equals(param.getParamType())) {
                if (operation.getInputPayLoad() == null) {
                    PayLoad rwadInRepr = toEntity(param);
                    operation.setInputPayLoad(rwadInRepr);
                }
            } else if ("query".equals(param.getParamType())) {
                QueryParameter rwadQueryParam = toQueryParameter(param);
                operation.getQueryParameters().add(rwadQueryParam);
            }
        }
    }

    /**
     * Fills Restlet Web API definition's Representations from Swagger
     * ApiDeclaration
     * 
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param section
     *            The Restlet Web API definition's current Section
     * @param apiDeclaration
     *            The Swagger ApiDeclaration
     * @param subtypes
     *            The list of this Representation's subtypes
     * @param declaredTypes
     *            The list of all declared types for the Contract
     */
    private static void fillRepresentations(Contract contract, Section section,
            ApiDeclaration apiDeclaration, Map<String, List<String>> subtypes,
            List<String> declaredTypes) {
        // Add representations
        Representation representation;
        for (Entry<String, ModelDeclaration> modelEntry : apiDeclaration
                .getModels().entrySet()) {
            ModelDeclaration model = modelEntry.getValue();
            if (model.getSubTypes() != null && !model.getSubTypes().isEmpty()) {
                subtypes.put(model.getId(), model.getSubTypes());
            }
            if (!declaredTypes.contains(modelEntry.getKey())) {
                declaredTypes.add(modelEntry.getKey());
                representation = toRepresentation(model, modelEntry.getKey());

                if (section != null) {
                    representation.addSection(section.getName());
                }

                contract.getRepresentations().add(representation);
                LOGGER.log(Level.FINE, "Representation " + modelEntry.getKey()
                        + " added.");
            }
        }
    }

    /**
     * Fills Restlet Web API definition's operation Responses from Swagger
     * ResourceOperationDeclaration
     * 
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param swaggerOperation
     *            The Swagger ResourceOperationDeclaration
     */
    private static void fillResponseMessages(Operation operation,
            ResourceOperationDeclaration swaggerOperation) {
        // Set error response messages
        if (swaggerOperation.getResponseMessages() != null) {
            for (ResponseMessageDeclaration swagResponse : swaggerOperation
                    .getResponseMessages()) {
                Response response = new Response();
                PayLoad outputPayLoad = new PayLoad();
                outputPayLoad.setType(swagResponse.getResponseModel());
                response.setOutputPayLoad(outputPayLoad);
                response.setName("Error " + swagResponse.getCode());
                response.setCode(swagResponse.getCode());
                response.setMessage(swagResponse.getMessage());
                operation.getResponses().add(response);
            }
        }
    }

    /**
     * Fills Restlet Web API definition's variants from Swagger 1.2 definition
     * 
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param swaggerOperation
     *            The Swagger ResourceOperationDeclaration
     * @param apiProduces
     *            The list of media types produced by the operation
     * @param apiConsumes
     *            The list of media types consumed by the operation
     */
    private static void fillVariants(Operation operation, ResourceOperationDeclaration swaggerOperation,
            List<String> apiProduces, List<String> apiConsumes) {
        // Set variants
        for (String produced : apiProduces.isEmpty() ? swaggerOperation
                .getProduces() : apiProduces) {
            operation.getProduces().add(produced);
        }

        for (String consumed : apiConsumes.isEmpty() ? swaggerOperation
                .getConsumes() : apiConsumes) {
            operation.getConsumes().add(consumed);
        }
    }

    /**
     * Converts a Swagger parameter to an instance of
     * {@link org.restlet.ext.apispark.internal.model.PayLoad}.
     * 
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of
     *         {@link org.restlet.ext.apispark.internal.model.PayLoad}.
     */
    private static PayLoad toEntity(
            ResourceOperationParameterDeclaration parameter) {
        PayLoad result = new PayLoad();
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
     * Converts a Swagger parameter to an instance of
     * {@link org.restlet.ext.apispark.internal.model.PathVariable}.
     * 
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of
     *         {@link org.restlet.ext.apispark.internal.model.PathVariable}.
     */
    private static PathVariable toPathVariable(
            ResourceOperationParameterDeclaration parameter) {
        PathVariable result = new PathVariable();
        result.setName(parameter.getName());
        result.setDescription(parameter.getDescription());
        result.setType(SwaggerTypes.toDefinitionType(new SwaggerTypeFormat(
                parameter.getType(), parameter.getFormat())));
        return result;
    }

    /**
     * Converts a Swagger parameter to an instance of
     * {@link org.restlet.ext.apispark.internal.model.QueryParameter}.
     * 
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of
     *         {@link org.restlet.ext.apispark.internal.model.QueryParameter}.
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
     * Converts a Swagger model to an instance of
     * {@link org.restlet.ext.apispark.internal.model.Representation}.
     * 
     * @param model
     *            The Swagger model.
     * @param name
     *            The name of the representation.
     * @return An instance of
     *         {@link org.restlet.ext.apispark.internal.model.Representation}.
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
                boolean required = model.getRequired().contains(swagProperties.getKey());
                property.setRequired(required);
            } else {
                property.setRequired(false);
            }
            property.setList(isArray);
            property.setDescription(swagProperty.getDescription());
            property.setUniqueItems(swagProperty.isUniqueItems());

            result.getProperties().add(property);
            LOGGER.log(Level.FINE, "Property " + property.getName() + " added.");
        }
        return result;
    }

    /**
     * Translates a Swagger API declaration to a Restlet Web API definition.
     * 
     * @param apiDeclaration
     *            The Swagger API declaration
     * @param sectionName
     *            Optional name of the section to add to the contract
     * @return the Restlet Web API definition
     * @throws TranslationException
     */
    public static Definition translate(ApiDeclaration apiDeclaration, String sectionName)
            throws TranslationException {
        try {
            Definition definition = new Definition();
            definition.setContract(new Contract());
            Endpoint endpoint = new Endpoint(apiDeclaration.getBasePath());
            definition.getEndpoints().add(endpoint);
            fillEndpointAuthorization(apiDeclaration.getAuthorizations(), endpoint);

            Contract contract = definition.getContract();
            fillContract(contract, apiDeclaration, new ArrayList<String>(), null, null);

            for (Representation representation : contract.getRepresentations()) {
                representation.addSectionsToProperties(contract);
            }

            LOGGER.log(Level.FINE,
                    "Definition successfully retrieved from Swagger definition");
            return definition;
        } catch (Exception e) {
            throw new TranslationException(
                    "compliance",
                    "Impossible to read your API definition, check your Swagger specs compliance",
                    e);
        }
    }

    /**
     * Translates a Swagger documentation to a Restlet definition.
     * 
     * @param listing
     *            The Swagger resource listing.
     * @param apiDeclarations
     *            The list of Swagger API declarations.
     * @return The Restlet definition.
     * @throws org.restlet.ext.apispark.internal.conversion.TranslationException
     */
    public static Definition translate(ResourceListing listing, Map<String, ApiDeclaration> apiDeclarations)
            throws TranslationException {

        validate(listing, apiDeclarations);

        try {
            Definition definition = new Definition();

            // fill main attributes of the Restlet Web API definition
            String basePath = null;

            List<ResourceListingApi> apis = listing.getApis();
            if (apis != null && !apis.isEmpty()) {
                String key = apis.get(0).getPath();
                ApiDeclaration firstApiDeclaration = apiDeclarations.get(key);
                basePath = firstApiDeclaration.getBasePath();
            }

            fillMainAttributes(definition, listing, basePath);

            Contract contract = definition.getContract();
            fillContract(contract, listing, apiDeclarations);

            for (Representation representation : contract.getRepresentations()) {
                representation.addSectionsToProperties(contract);
            }

            LOGGER.log(Level.FINE,
                    "Definition successfully retrieved from Swagger definition");
            return definition;
        } catch (Exception e) {
            throw new TranslationException(
                    "compliance",
                    "Impossible to read your API definition, check your Swagger specs compliance",
                    e);
        }
    }
    
    /**
     * Translates a Swagger Resource Listing to a Restlet definition.
     * 
     * @param listing
     *            The Swagger resource listing.
     * @return The Restlet definition.
     * @throws org.restlet.ext.apispark.internal.conversion.TranslationException
     */
    public static Definition translate(ResourceListing listing) {
        Definition definition = new Definition();
        fillMainAttributes(definition, listing, null);

        Contract contract = definition.getContract();
        fillSections(contract, listing);

        LOGGER.log(Level.FINE,
                "Main attributes successfully retrieved from Swagger resource listing.");
        return definition;
    }
            

    private static void fillSections(Contract contract, ResourceListing listing) {
        for (ResourceListingApi api : listing.getApis()) {
            Section section = new Section();
            String sectionName = SwaggerUtils.computeSectionName(api.getPath());
            section.setName(sectionName);
            section.setDescription(api.getDescription());

            contract.getSections().add(section);
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
     * @throws org.restlet.ext.apispark.internal.conversion.TranslationException
     */
    private static void validate(ResourceListing resourceListing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws TranslationException {
        int rlSize = resourceListing.getApis().size();
        int adSize = apiDeclarations.size();
        if (rlSize < adSize) {
            throw new TranslationException("file",
                    "Some API declarations are not mapped in your resource listing");
        } else if (rlSize > adSize) {
            throw new TranslationException("file",
                    "Some API declarations are missing");
        }
    }
}
