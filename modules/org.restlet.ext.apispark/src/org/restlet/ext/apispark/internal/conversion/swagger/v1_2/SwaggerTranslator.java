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

package org.restlet.ext.apispark.internal.conversion.swagger.v1_2;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
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
import org.restlet.ext.apispark.internal.model.Types;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ApiDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ApiInfo;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.AuthorizationsDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.BasicAuthorizationDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ItemsDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ModelDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.OAuth2AuthorizationDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListing;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceOperationDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceOperationParameterDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResponseMessageDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.TypePropertyDeclaration;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tool library for converting Restlet Web API Definition to and from Swagger
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerTranslator {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(SwaggerTranslator.class
            .getName());

    /** Supported version of Swagger. */
    private static final String SWAGGER_VERSION = "1.2";

    /**
     * Fills Swagger resource listing main attributes from Restlet Web API
     * definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param listing
     *            The Swagger 1.2 resource listing
     */
    private static void fillResourceListingMainAttributes(
            Definition definition, ResourceListing listing) {
        // common properties
        listing.setApiVersion(definition.getVersion());
        // result.setBasePath(definition.getEndpoint());
        listing.setInfo(new ApiInfo());
        listing.setSwaggerVersion(SWAGGER_VERSION);
        if (definition.getContact() != null) {
            listing.getInfo().setContact(definition.getContact().getEmail());
        }
        if (definition.getLicense() != null) {
            listing.getInfo().setLicenseUrl(definition.getLicense().getUrl());
        }
        if (definition.getContract() != null) {
            listing.getInfo().setTitle(definition.getContract().getName());
            listing.getInfo().setDescription(
                    definition.getContract().getDescription());
        }

        if (!definition.getEndpoints().isEmpty()) {
            String authenticationProtocol = definition.getEndpoints().get(0)
                    .getAuthenticationProtocol();
            if (authenticationProtocol != null) {
                AuthorizationsDeclaration authorizations = new AuthorizationsDeclaration();
                // TODO add other authentication protocols
                if (ChallengeScheme.HTTP_BASIC.getName().equals(
                        authenticationProtocol)) {
                    authorizations
                            .setBasicAuth(new BasicAuthorizationDeclaration());
                    listing.setAuthorizations(authorizations);
                } else if (ChallengeScheme.HTTP_OAUTH.getName().equals(
                        authenticationProtocol)
                        || ChallengeScheme.HTTP_OAUTH_BEARER.getName().equals(
                                authenticationProtocol)
                        || ChallengeScheme.HTTP_OAUTH_MAC.getName().equals(
                                authenticationProtocol)) {
                    authorizations
                            .setOauth2(new OAuth2AuthorizationDeclaration());
                }
            }
        }
    }

    /**
     * Fills Swagger resource listing main attributes from Restlet Web API
     * definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param listing
     *            The Swagger 1.2 resource listing
     */
    private static void fillResourceListingApis(Definition definition,
            ResourceListing listing) {
        Contract contract = definition.getContract();
        boolean allResources = contract.getSections().isEmpty();

        // Resources
        List<String> addedApis = new ArrayList<String>();
        if (definition.getContract() != null && contract.getResources() != null) {
            listing.setApis(new ArrayList<ResourceDeclaration>());

            for (Resource resource : contract.getResources()) {
                ResourceDeclaration rd = new ResourceDeclaration();

                if (allResources) {
                    rd.setDescription(resource.getDescription());
                    rd.setPath(ReflectUtils.getFirstSegment(resource
                            .getResourcePath()));
                    if (!addedApis.contains(rd.getPath())) {
                        addedApis.add(rd.getPath());
                        listing.getApis().add(rd);
                    }
                } else {
                    for (String sectionName : resource.getSections()) {
                        Section section = contract.getSection(sectionName);
                        rd = new ResourceDeclaration();
                        rd.setDescription(section.getDescription());
                        rd.setPath("/" + sectionName);
                        if (!addedApis.contains(rd.getPath())) {
                            addedApis.add(rd.getPath());
                            listing.getApis().add(rd);
                        }
                    }
                }
            }
        }
        Collections.sort(listing.getApis(),
                new Comparator<ResourceDeclaration>() {
                    @Override
                    public int compare(ResourceDeclaration o1,
                            ResourceDeclaration o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }

                });
    }

    /**
     * Fills Swagger API declaration main attributes from Restlet Web API
     * definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param apiDeclaration
     *            The Swagger 1.2 API declaration
     * @param sectionName
     *            The name of the current section
     */
    private static void fillApiDeclarationMainAttributes(Definition definition,
            ApiDeclaration apiDeclaration, String sectionName) {
        apiDeclaration.setApiVersion(definition.getVersion());

        // No way to specify multiple endpoints in Swagger so we take the first
        // one
        Endpoint endpoint;
        if (!definition.getEndpoints().isEmpty()) {
            endpoint = definition.getEndpoints().get(0);
            apiDeclaration.setBasePath(endpoint.computeUrl());
        } else {
            endpoint = new Endpoint("http://example.com");
        }

        // Authentication
        // TODO deal with API key authentication
        AuthorizationsDeclaration authorizations = new AuthorizationsDeclaration();
        if (ChallengeScheme.HTTP_BASIC.getName().equals(
                (endpoint.getAuthenticationProtocol()))) {
            authorizations.setBasicAuth(new BasicAuthorizationDeclaration());
            apiDeclaration.setAuthorizations(authorizations);
        } else if (ChallengeScheme.HTTP_OAUTH.getName().equals(
                (endpoint.getAuthenticationProtocol()))
                || ChallengeScheme.HTTP_OAUTH_BEARER.getName().equals(
                        (endpoint.getAuthenticationProtocol()))
                || ChallengeScheme.HTTP_OAUTH_MAC.getName().equals(
                        (endpoint.getAuthenticationProtocol()))) {
            authorizations.setOauth2(new OAuth2AuthorizationDeclaration());
        }

        apiDeclaration.setInfo(new ApiInfo());
        apiDeclaration.setSwaggerVersion(SWAGGER_VERSION);
        apiDeclaration.setResourcePath("/" + sectionName);
    }

    /**
     * Fills Swagger ResourceOperationDeclaration's
     * ResourceOperationParameterDeclaration from Restlet Web API definition's
     * Resource
     * 
     * @param resource
     *            The Restlet Web API definition's Resource
     * @param rod
     *            The Swagger Swagger ResourceOperationDeclaration
     */
    private static void fillApiDeclarationPathVariables(Resource resource,
            ResourceOperationDeclaration rod) {
        // Get path variables
        ResourceOperationParameterDeclaration ropd;
        for (PathVariable pv : resource.getPathVariables()) {
            ropd = new ResourceOperationParameterDeclaration();
            ropd.setParamType("path");
            SwaggerTypeFormat swaggerTypeFormat =
                    SwaggerTypes.toSwaggerType(pv.getType());
            ropd.setType(swaggerTypeFormat.getType());
            ropd.setFormat(swaggerTypeFormat.getFormat());
            ropd.setRequired(true);
            ropd.setName(pv.getName());
            ropd.setAllowMultiple(false);
            ropd.setDescription(pv.getDescription());
            rod.getParameters().add(ropd);
        }
    }

    /**
     * Fills Swagger ResourceOperationDeclaration's
     * ResourceOperationParameterDeclaration from Restlet Web API definition's
     * Operation
     * 
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param rod
     *            The Swagger Swagger ResourceOperationDeclaration
     */
    private static void fillApiDeclarationQueryParameters(Operation operation,
            ResourceOperationDeclaration rod) {
        // Get query parameters
        ResourceOperationParameterDeclaration ropd;
        for (QueryParameter qp : operation.getQueryParameters()) {
            ropd = new ResourceOperationParameterDeclaration();
            ropd.setParamType("query");
            SwaggerTypeFormat swaggerTypeFormat =
                    SwaggerTypes.toSwaggerType(qp.getType());
            ropd.setType(swaggerTypeFormat.getType());
            ropd.setFormat(swaggerTypeFormat.getFormat());
            ropd.setName(qp.getName());
            ropd.setAllowMultiple(true);
            ropd.setDescription(qp.getDescription());
            ropd.setEnum_(qp.getEnumeration());
            ropd.setDefaultValue(qp.getDefaultValue());
            rod.getParameters().add(ropd);
        }
    }

    /**
     * Fills Swagger ResourceOperationDeclaration's type from Restlet Web API
     * definition's Operation
     * 
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param rod
     *            The Swagger Swagger ResourceOperationDeclaration
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param usedModels
     *            The models specified by this API declaration
     */
    private static void fillApiDeclarationInRepresentation(Operation operation,
            ResourceOperationDeclaration rod, Contract contract,
            Collection<String> usedModels) {
        // Get in representation
        ResourceOperationParameterDeclaration ropd;
        PayLoad inRepr = operation.getInputPayLoad();
        if (inRepr != null) {
            Representation representation = contract.getRepresentation(inRepr
                    .getType());

            ropd = new ResourceOperationParameterDeclaration();
            ropd.setParamType("body");
            ropd.setName("body");
            ropd.setRequired(true);

            ropd.setType(inRepr.getType());
            if (representation != null) {
                usedModels.add(inRepr.getType());
            }
            rod.getParameters().add(ropd);
        }
    }

    /**
     * Fills Swagger ResourceOperationDeclaration's returned type from Restlet
     * Web API definition's Operation
     * 
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param rod
     *            The Swagger Swagger ResourceOperationDeclaration
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param usedModels
     *            The models specified by this API declaration
     */
    private static void fillApiDeclarationOutRepresentation(
            Operation operation, ResourceOperationDeclaration rod,
            Contract contract, Collection<String> usedModels) {
        // Get out representation
        PayLoad outRepr = null;
        for (Response response : operation.getResponses()) {
            if (Status.isSuccess(response.getCode())) {
                outRepr = response.getOutputPayLoad();
            }
        }
        if (outRepr != null && outRepr.getType() != null) {
            if (outRepr.isArray()) {
                rod.setType("array");
                if (Types.isPrimitiveType(outRepr.getType())) {
                    // TODO how to display error ?
                } else {
                    rod.getItems().setRef(outRepr.getType());
                }
            } else {
                rod.setType(outRepr.getType());
            }
            usedModels.add(outRepr.getType());
        } else {
            rod.setType("void");
        }
    }

    /**
     * Fills Swagger ResourceOperationDeclaration's error responses from Restlet
     * Web API definition's Operation
     * 
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param usedModels
     *            The models specified by this API declaration
     * @param rod
     *            The Swagger Swagger ResourceOperationDeclaration
     */
    private static void fillApiDeclarationResponses(Operation operation,
            Collection<String> usedModels, ResourceOperationDeclaration rod) {
        // Get response messages
        for (Response response : operation.getResponses()) {
            if (Status.isSuccess(response.getCode())) {
                continue;
            }
            ResponseMessageDeclaration rmd = new ResponseMessageDeclaration();
            rmd.setCode(response.getCode());
            rmd.setMessage(response.getMessage());
            if (response.getOutputPayLoad() != null) {
                rmd.setResponseModel(response.getOutputPayLoad().getType());
                usedModels.add(response.getOutputPayLoad().getType());
            }
            rod.getResponseMessages().add(rmd);
        }
    }

    /**
     * Fills Swagger ResourceDeclaration's ResourceOperationDeclaration from
     * Restlet Web API definition's Resource
     * 
     * @param resource
     *            The Restlet Web API definition's Resource
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param usedModels
     *            The models specified by this API declaration
     * @param rd
     *            The Swagger Swagger ResourceDeclaration
     */
    private static void fillApiDeclarationOperations(Resource resource,
            Contract contract, Collection<String> usedModels,
            ResourceDeclaration rd) {
        // Get operations
        for (Operation operation : resource.getOperations()) {
            ResourceOperationDeclaration rod = new ResourceOperationDeclaration();
            rod.setMethod(operation.getMethod());
            rod.setSummary(operation.getDescription());
            rod.setNickname(operation.getName());
            rod.setProduces(operation.getProduces());
            rod.setConsumes(operation.getConsumes());

            // fill the resource operation parameters
            fillApiDeclarationPathVariables(resource, rod);
            fillApiDeclarationQueryParameters(operation, rod);

            // fill the resource operation in representation
            fillApiDeclarationInRepresentation(operation, rod, contract,
                    usedModels);

            // fill the resource operation out representation
            fillApiDeclarationOutRepresentation(operation, rod, contract,
                    usedModels);

            // fill the resource operation erorr response models
            fillApiDeclarationResponses(operation, usedModels, rod);

            rd.getOperations().add(rod);
        }
    }

    /**
     * Fills Swagger ApiDeclaration's ResourceDeclarations from Restlet Web API
     * definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param apiDeclaration
     *            The Swagger API declaration
     * @param sectionName
     *            The name of the current section
     * @return The models specified by this API declaration
     */
    private static Collection<String> fillApiDeclarationResources(
            Definition definition, ApiDeclaration apiDeclaration,
            String sectionName) {
        Set<String> usedModels = new HashSet<String>();
        Contract contract = definition.getContract();

        // Get the resources corresponding to the sectionName
        List<Resource> resources = new ArrayList<Resource>();
        boolean allResources = contract.getSections().isEmpty();
        for (Resource resource : contract.getResources()) {
            if (allResources) {
                resources.add(resource);
            } else {
                if (resource.getSections().contains(sectionName)) {
                    resources.add(resource);
                }
            }
        }

        // Get resources
        for (Resource resource : resources) {
            // Discriminate the resources of one category
            if (allResources
                    && !resource.getResourcePath()
                            .startsWith("/" + sectionName)) {
                continue;
            }
            ResourceDeclaration rd = new ResourceDeclaration();
            rd.setPath(resource.getResourcePath());
            rd.setDescription(resource.getDescription());

            // fill resource declaration
            fillApiDeclarationOperations(resource, contract, usedModels, rd);

            apiDeclaration.getApis().add(rd);
        }
        // Sort the API declarations according to their path.
        Collections.sort(apiDeclaration.getApis(),
                new Comparator<ResourceDeclaration>() {
                    @Override
                    public int compare(ResourceDeclaration o1,
                            ResourceDeclaration o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }
                });
        return usedModels;
    }

    /**
     * Fills Swagger ApiDeclaration's ModelDeclarations from Restlet Web API
     * definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param apiDeclaration
     *            The Swagger API declaration
     * @param usedModels
     *            The models specified by this API declaration
     */
    private static void fillApiDeclarationRepresentations(
            Definition definition, ApiDeclaration apiDeclaration,
            Collection<String> usedModels) {
        Contract contract = definition.getContract();
        apiDeclaration.setModels(new TreeMap<String, ModelDeclaration>());

        List<String> usedModelsList = new ArrayList<String>(usedModels);
        for (int i = 0; i < usedModelsList.size(); i++) {
            String model = usedModelsList.get(i);
            Representation repr = contract.getRepresentation(model);
            if (repr == null || Types.isPrimitiveType(model)) {
                continue;
            }
            ModelDeclaration md = new ModelDeclaration();
            md.setId(model);
            md.setDescription(repr.getDescription());
            for (Property prop : repr.getProperties()) {
                if (prop.getMinOccurs() > 0) {
                    md.getRequired().add(prop.getName());
                }
                if (!Types.isPrimitiveType(prop.getType())
                        && !usedModelsList.contains(prop.getType())) {
                    usedModelsList.add(prop.getType());
                }
                TypePropertyDeclaration tpd = new TypePropertyDeclaration();
                tpd.setDescription(prop.getDescription());
                tpd.setEnum_(prop.getEnumeration());

                if (prop.getMaxOccurs() > 1 || prop.getMaxOccurs() == -1) {
                    tpd.setType("array");
                    tpd.setItems(new ItemsDeclaration());
                    if (Types.isPrimitiveType(prop.getType())) {
                        SwaggerTypeFormat swaggerTypeFormat =
                                SwaggerTypes.toSwaggerType(prop.getType());
                        tpd.getItems().setType(
                                swaggerTypeFormat
                                        .getType());
                        tpd.setFormat(swaggerTypeFormat.getFormat());
                    } else {
                        tpd.getItems().setRef(prop.getType());
                    }
                } else {
                    if (Types.isPrimitiveType(prop.getType())) {
                        SwaggerTypeFormat swaggerTypeFormat =
                                SwaggerTypes.toSwaggerType(prop.getType());
                        tpd.setType(swaggerTypeFormat
                                .getType());
                        tpd.setFormat(swaggerTypeFormat.getFormat());
                    } else {
                        tpd.setRef(prop.getType());
                    }
                }
                tpd.setMaximum(prop.getMax());
                tpd.setMinimum(prop.getMin());
                tpd.setUniqueItems(prop.isUniqueItems());

                md.getProperties().put(prop.getName(), tpd);
            }
            apiDeclaration.getModels().put(md.getId(), md);
        }
    }

    /**
     * Retrieves the Swagger API declaration corresponding to a category of the
     * given Restlet Web API Definition
     * 
     * @param sectionName
     *            The category of the API declaration
     * @param definition
     *            The Restlet Web API Definition
     * @return The Swagger API definition of the given category
     */
    public static ApiDeclaration getApiDeclaration(String sectionName,
            Definition definition) {
        ApiDeclaration result = new ApiDeclaration();

        // fill API declaration main attributes
        fillApiDeclarationMainAttributes(definition, result, sectionName);

        // fill API declaration resources
        Collection<String> usedModels = fillApiDeclarationResources(definition,
                result, sectionName);

        // fill API declaration representations
        fillApiDeclarationRepresentations(definition, result, usedModels);

        return result;
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

        // fill resource listing main attributes
        fillResourceListingMainAttributes(definition, result);

        // fill resource listing API list
        fillResourceListingApis(definition, result);

        return result;
    }

    /**
     * Converts a Swagger parameter to an instance of {@link org.restlet.ext.apispark.internal.model.PayLoad}.
     *
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of {@link org.restlet.ext.apispark.internal.model.PayLoad}.
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
     * Converts a Swagger parameter to an instance of {@link org.restlet.ext.apispark.internal.model.PathVariable}.
     *
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of {@link org.restlet.ext.apispark.internal.model.PathVariable}.
     */
    private static PathVariable toPathVariable(
            ResourceOperationParameterDeclaration parameter) {
        PathVariable result = new PathVariable();
        result.setName(parameter.getName());
        result.setDescription(parameter.getDescription());
        result.setType(SwaggerTypes
                .toDefinitionType(new SwaggerTypeFormat(parameter
                        .getType(), parameter.getFormat())));
        return result;
    }

    /**
     * Converts a Swagger parameter to an instance of {@link org.restlet.ext.apispark.internal.model.QueryParameter}.
     *
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of {@link org.restlet.ext.apispark.internal.model.QueryParameter}.
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
     * Converts a Swagger model to an instance of {@link org.restlet.ext.apispark.internal.model.Representation}.
     *
     * @param model
     *            The Swagger model.
     * @param name
     *            The name of the representation.
     * @return An instance of {@link org.restlet.ext.apispark.internal.model.Representation}.
     */
    private static Representation toRepresentation(ModelDeclaration model,
            String name) {
        Representation result = new Representation();
        result.setIdentifier(name);
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
        Contact contact = new Contact();
        contact.setEmail(listing.getInfo().getContact());
        definition.setContact(contact);
        License license = new License();
        license.setUrl(listing.getInfo().getLicenseUrl());
        definition.setLicense(license);

        Contract contract = new Contract();
        contract.setName(listing.getInfo().getTitle());
        LOGGER.log(Level.FINE, "Contract " + contract.getName() + " added.");
        contract.setDescription(listing.getInfo().getDescription());
        definition.setContract(contract);

        if (definition.getEndpoints().isEmpty()) {
            // TODO verify how to deal with API key auth + oauth
            Endpoint endpoint = new Endpoint(basePath);
            definition.getEndpoints().add(endpoint);
            if (listing.getAuthorizations().getBasicAuth() != null) {
                endpoint.setAuthenticationProtocol(ChallengeScheme.HTTP_BASIC
                        .getName());
            } else if (listing.getAuthorizations().getOauth2() != null) {
                endpoint.setAuthenticationProtocol(ChallengeScheme.HTTP_OAUTH
                        .getName());
            } else if (listing.getAuthorizations().getApiKey() != null) {
                endpoint.setAuthenticationProtocol(ChallengeScheme.CUSTOM
                        .getName());
            }
        }
    }

    /**
     * Fills Restlet Web API definition's variants from Swagger 1.2 definition
     *
     * @param contract
     *            The Restlet Web API definition's Contract
     * @param section
     *            The current Section
     * @param operation
     *            The Restlet Web API definition's Operation
     * @param swaggerOperation
     *            The Swagger ResourceOperationDeclaration
     * @param apiProduces
     *            The list of media types produced by the operation
     * @param apiConsumes
     *            The list of media types consumed by the operation
     */
    private static void fillVariants(Contract contract, Section section,
            Operation operation, ResourceOperationDeclaration swaggerOperation,
            List<String> apiProduces, List<String> apiConsumes) {
        // Set variants
        Representation representation;
        boolean containsRawTypes = false;
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
            if (swaggerOperation.getType() != null) {
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
                representation.getSections().add(section.getName());
                contract.getRepresentations().add(representation);
                LOGGER.log(Level.FINE, "Representation " + modelEntry.getKey()
                        + " added.");
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
     */
    private static void fillOperations(Resource resource,
            ApiDeclaration apiDeclaration, ResourceDeclaration api,
            Contract contract, Section section,
            List<String> declaredPathVariables) {

        List<String> apiProduces = apiDeclaration.getProduces();
        List<String> apiConsumes = apiDeclaration.getConsumes();
        List<String> declaredTypes = new ArrayList<String>();
        Map<String, List<String>> subtypes = new HashMap<String, List<String>>();
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
            fillVariants(contract, section, operation, swaggerOperation,
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

        // Resource listing
        Resource resource;
        for (Entry<String, ApiDeclaration> entry : apiDeclarations.entrySet()) {
            ApiDeclaration apiDeclaration = entry.getValue();
            Section section = new Section();
            section.setName(entry.getKey());
            section.setDescription(listing.getApi(entry.getKey())
                    .getDescription());

            for (ResourceDeclaration api : apiDeclaration.getApis()) {
                resource = new Resource();
                resource.setResourcePath(api.getPath());

                List<String> declaredPathVariables = new ArrayList<String>();
                fillOperations(resource, apiDeclaration, api, contract,
                        section, declaredPathVariables);

                resource.getSections().add(section.getName());
                contract.getResources().add(resource);
                LOGGER.log(Level.FINE, "Resource " + api.getPath() + " added.");
            }
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
    public static Definition translate(ResourceListing listing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws TranslationException {

        validate(listing, apiDeclarations);

        try {
            Definition definition = new Definition();

            // fill main attributes of the Restlet Web API definition
            String basePath = apiDeclarations.get(
                    (listing.getApis().get(0).getPath())).getBasePath();
            fillMainAttributes(definition, listing, basePath);

            fillContract(definition.getContract(), listing, apiDeclarations);

            LOGGER.log(Level.FINE,
                    "Definition successfully retrieved from Swagger definition");
            return definition;
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                throw new TranslationException("file", e.getMessage(), e);
            } else {
                throw new TranslationException("compliance",
                        "Impossible to read your API definition, check your Swagger specs compliance", e);
            }
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
    private SwaggerTranslator() {
    }
}