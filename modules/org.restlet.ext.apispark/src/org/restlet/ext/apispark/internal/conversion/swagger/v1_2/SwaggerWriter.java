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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ApiDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ApiInfo;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.AuthorizationsDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.BasicAuthorizationDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ItemsDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ModelDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.OAuth2AuthorizationDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListing;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListingApi;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceOperationDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceOperationParameterDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResponseMessageDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.TypePropertyDeclaration;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;

/**
 * Tool library for converting Restlet Web API Definition to Swagger
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerWriter {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(SwaggerWriter.class
            .getName());

    /** Supported version of Swagger. */
    public static final String SWAGGER_VERSION = "1.2";

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SwaggerWriter() {
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
        PayLoad inputPayload = operation.getInputPayLoad();
        if (inputPayload != null && inputPayload.getType() != null) {
            Representation representation = contract.getRepresentation(inputPayload
                    .getType());

            ropd = new ResourceOperationParameterDeclaration();
            ropd.setParamType("body");
            ropd.setName("body");
            ropd.setRequired(true);

            if (inputPayload.isArray()) {
                ropd.setType("array");
                ItemsDeclaration items = new ItemsDeclaration();
                String itemsType = inputPayload.getType();
                if (Types.isPrimitiveType(itemsType)) {
                    SwaggerTypeFormat swaggerTypeFormat = SwaggerTypes
                            .toSwaggerType(itemsType);
                    items.setType(swaggerTypeFormat.getType());
                    items.setFormat(swaggerTypeFormat.getFormat());
                } else {
                    items.setRef(itemsType);
                }
                ropd.setItems(items);
            } else {
                ropd.setType(inputPayload.getType());
            }

            if (representation != null) {
                usedModels.add(inputPayload.getType());
            }
            rod.getParameters().add(ropd);
        }
    }

    /**
     * Fills Swagger API declaration main attributes from Restlet Web API
     * definition
     * 
     * @param definition
     *            The Restlet Web API definition
     * @param apiDeclaration
     *            The Swagger 1.2 API declaration
     */
    private static void fillApiDeclarationMainAttributes(Definition definition, ApiDeclaration apiDeclaration) {
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

        apiDeclaration.setSwaggerVersion(SWAGGER_VERSION);
        apiDeclaration.setResourcePath("");
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
            fillApiDeclarationOutRepresentation(operation, rod, usedModels);

            // fill the resource operation erorr response models
            fillApiDeclarationResponses(operation, usedModels, rod);

            rd.getOperations().add(rod);
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
     * @param usedModels
     *            The models specified by this API declaration
     */
    private static void fillApiDeclarationOutRepresentation(
            Operation operation, ResourceOperationDeclaration rod,
            Collection<String> usedModels) {
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
                    SwaggerTypeFormat swaggerTypeFormat = SwaggerTypes
                            .toSwaggerType(outRepr.getType());
                    rod.getItems().setType(swaggerTypeFormat.getType());
                    rod.getItems().setFormat(swaggerTypeFormat.getFormat());
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
            SwaggerTypeFormat swaggerTypeFormat = SwaggerTypes.toSwaggerType(pv
                    .getType());
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
            SwaggerTypeFormat swaggerTypeFormat = SwaggerTypes.toSwaggerType(qp
                    .getType());
            ropd.setType(swaggerTypeFormat.getType());
            ropd.setFormat(swaggerTypeFormat.getFormat());
            ropd.setName(qp.getName());
            ropd.setAllowMultiple(true);
            ropd.setDescription(qp.getDescription());
            ropd.setEnum_(qp.getEnumeration());
            ropd.setDefaultValue(qp.getDefaultValue());
            ropd.setRequired(qp.isRequired());
            rod.getParameters().add(ropd);
        }
    }


    /**
     * Fills Swagger ApiDeclaration's ModelDeclarations from Restlet Web API
     * definition
     * 
     * @param contract
     *            The Restlet Web API definition's {@link Contract}
     * @param apiDeclaration
     *            The Swagger {@link ApiDeclaration}
     * @param usedModels
     *            The models specified by this API declaration
     */
    private static void fillApiDeclarationRepresentations(
            Contract contract, ApiDeclaration apiDeclaration,
            Collection<String> usedModels) {
        apiDeclaration.setModels(new TreeMap<String, ModelDeclaration>());

        List<String> usedModelsList = new ArrayList<>(usedModels);
        for (int i = 0; i < usedModelsList.size(); i++) {
            String model = usedModelsList.get(i);
            Representation repr = contract.getRepresentation(model);
            if (repr == null || Types.isPrimitiveType(model)) {
                continue;
            }
            ModelDeclaration md = new ModelDeclaration();
            fillModel(apiDeclaration, usedModelsList, model,
                    repr.getDescription(), repr.getProperties(), md);
        }
    }

    /**
     * Fills a Swagger ApiDeclaration's {@link ResourceDeclaration}s from Restlet Web API
     * definition. Only includes the given resources in the given section.
     * 
     * @param contract
     *            The Restlet Web API definition's {@link Contract}
     * @param apiDeclaration
     *            The Swagger {@link ApiDeclaration}
     * @param resourcesToImport
     * @return
     */
    private static Collection<String> fillApiDeclarationResources(
            Contract contract, ApiDeclaration apiDeclaration,
            List<Resource> resourcesToImport) {
        Set<String> usedModels = new HashSet<>();

        // Get resources
        for (Resource resource : resourcesToImport) {
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
     * Fills a Swagger ApiDeclaration's {@link ResourceDeclaration}s from Restlet Web API
     * definition.
     * If <code>sectionName</code> parameter is defined, only resources from the given section will be included.
     * 
     * @param contract
     *            The Restlet Web API definition's {@link Contract}
     * @param apiDeclaration
     *            The Swagger {@link ApiDeclaration}
     * @param sectionName
     *            The name of the current section. If null no filter will be applied.
     *
     * @return The models specified by this API declaration
     */
    private static Collection<String> fillApiDeclarationResources(
            Contract contract, ApiDeclaration apiDeclaration,
            String sectionName) {

        // Get the resources corresponding to the sectionName
        List<Resource> resources;

        if (sectionName == null) {
            resources = contract.getResources();
        } else {
            resources = new ArrayList<>();
            for (Resource resource : contract.getResources()) {
                if (resource.getSections().contains(sectionName)) {
                    resources.add(resource);
                }
            }
        }

        return fillApiDeclarationResources(contract, apiDeclaration, resources);
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
            if (response.getOutputPayLoad() != null
                    && response.getOutputPayLoad().getType() != null) {
                rmd.setResponseModel(response.getOutputPayLoad().getType());
                usedModels.add(response.getOutputPayLoad().getType());
            }
            rod.getResponseMessages().add(rmd);
        }
    }

    private static void fillModel(ApiDeclaration apiDeclaration,
            List<String> usedModelsList, String model, String description,
            List<Property> properties, ModelDeclaration md) {
        md.setId(model);
        md.setDescription(description);
        for (Property prop : properties) {
            String type = prop.getType();

            boolean composite = Types.isCompositeType(type);
            if (composite) {
                type = model + StringUtils.firstUpper(prop.getName());
            }

            if (prop.isRequired()) {
                md.getRequired().add(prop.getName());
            }
            if (!Types.isPrimitiveType(type) && !usedModelsList.contains(type)) {
                usedModelsList.add(type);
            }
            TypePropertyDeclaration tpd = new TypePropertyDeclaration();
            tpd.setDescription(prop.getDescription());
            tpd.setEnum_(prop.getEnumeration());

            if (prop.isList()) {
                tpd.setType("array");
                tpd.setItems(new ItemsDeclaration());
                if (Types.isPrimitiveType(type)) {
                    SwaggerTypeFormat swaggerTypeFormat = SwaggerTypes
                            .toSwaggerType(type);
                    tpd.getItems().setType(swaggerTypeFormat.getType());
                    tpd.getItems().setFormat(swaggerTypeFormat.getFormat());
                } else {
                    tpd.getItems().setRef(type);
                    if (composite) {
                        ModelDeclaration m = new ModelDeclaration();
                        fillModel(apiDeclaration, usedModelsList, type, null, prop.getProperties(), m);
                    }
                }
            } else {
                if (Types.isPrimitiveType(type)) {
                    SwaggerTypeFormat swaggerTypeFormat = SwaggerTypes
                            .toSwaggerType(type);
                    tpd.setType(swaggerTypeFormat.getType());
                    tpd.setFormat(swaggerTypeFormat.getFormat());
                } else {
                    tpd.setRef(type);
                    if (composite) {
                        ModelDeclaration m = new ModelDeclaration();
                        fillModel(apiDeclaration, usedModelsList, type, null, prop.getProperties(), m);
                    }
                }
            }
            tpd.setMaximum(prop.getMax());
            tpd.setMinimum(prop.getMin());
            tpd.setUniqueItems(prop.isUniqueItems());

            md.getProperties().put(prop.getName(), tpd);
        }
        apiDeclaration.getModels().put(md.getId(), md);
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
        List<String> addedApis = new ArrayList<>();
        if (definition.getContract() != null && contract.getResources() != null) {
            listing.setApis(new ArrayList<ResourceListingApi>());

            for (Resource resource : contract.getResources()) {

                if (allResources) {
                    ResourceListingApi rd = new ResourceListingApi();
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
                        ResourceListingApi rd = new ResourceListingApi();
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
                new Comparator<ResourceListingApi>() {
                    @Override
                    public int compare(ResourceListingApi o1,
                            ResourceListingApi o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }

                });
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
            listing.getInfo().setLicense(definition.getLicense().getName());
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
     * Retrieves the Swagger API declaration corresponding to a category of the
     * given Restlet Web API {@link Definition}
     *
     * @param definition
     *            The Restlet Web API {@link Definition}
     * @param sectionName
     *            The category of the API declaration
     * @return The Swagger {@link ApiDeclaration} of the given category
     */
    public static ApiDeclaration getApiDeclaration(Definition definition, String sectionName) {
        ApiDeclaration result = new ApiDeclaration();
        Contract contract = definition.getContract();

        // fill API declaration main attributes
        fillApiDeclarationMainAttributes(definition, result);

        // fill API declaration resources
        Collection<String> usedModels = fillApiDeclarationResources(contract, result, sectionName);

        // fill API declaration representations
        fillApiDeclarationRepresentations(contract, result, usedModels);

        return result;
    }

    /**
     * Retrieves the Swagger API declaration corresponding to a category of the
     * given Restlet Web API {@link Definition}
     *
     * @param definition
     *            The Restlet Web API {@link Definition}
     * @return The Swagger {@link ApiDeclaration} of the given category
     */
    public static ApiDeclaration getApiDeclaration(Definition definition) {
        return getApiDeclaration(definition, null);
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
}