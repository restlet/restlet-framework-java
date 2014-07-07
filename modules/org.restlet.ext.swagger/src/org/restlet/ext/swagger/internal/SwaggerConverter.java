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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.Status;
import org.restlet.ext.swagger.internal.model.Body;
import org.restlet.ext.swagger.internal.model.Contract;
import org.restlet.ext.swagger.internal.model.Definition;
import org.restlet.ext.swagger.internal.model.Operation;
import org.restlet.ext.swagger.internal.model.PathVariable;
import org.restlet.ext.swagger.internal.model.Property;
import org.restlet.ext.swagger.internal.model.QueryParameter;
import org.restlet.ext.swagger.internal.model.Representation;
import org.restlet.ext.swagger.internal.model.Resource;
import org.restlet.ext.swagger.internal.model.Response;
import org.restlet.ext.swagger.internal.swagger.ApiDeclaration;
import org.restlet.ext.swagger.internal.swagger.ApiInfo;
import org.restlet.ext.swagger.internal.swagger.ItemsDeclaration;
import org.restlet.ext.swagger.internal.swagger.ModelDeclaration;
import org.restlet.ext.swagger.internal.swagger.ResourceDeclaration;
import org.restlet.ext.swagger.internal.swagger.ResourceListing;
import org.restlet.ext.swagger.internal.swagger.ResourceOperationDeclaration;
import org.restlet.ext.swagger.internal.swagger.ResourceOperationParameterDeclaration;
import org.restlet.ext.swagger.internal.swagger.ResponseMessageDeclaration;
import org.restlet.ext.swagger.internal.swagger.TypePropertyDeclaration;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Retrieves a Swagger definition and converts it to Restlet Web API Definition.
 * 
 * @author Cyprien Quilici
 */

public class SwaggerConverter extends ServerResource {

    /** Internal logger. */
    protected static Logger LOGGER = Context.getCurrentLogger();

    public ResourceListing getResourcelisting(Definition definition) {
        ResourceListing result = new ResourceListing();

        // common properties
        result.setApiVersion(definition.getVersion());
        result.setInfo(new ApiInfo());
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
        result.setBasePath(definition.getEndpoint());
        // operations
        if (definition.getContract() != null
                && definition.getContract().getResources() != null) {
            result.setApis(new ArrayList<ResourceDeclaration>());

            for (Resource resource : definition.getContract().getResources()) {
                ResourceDeclaration rd = new ResourceDeclaration();
                rd.setDescription(resource.getDescription());
                rd.setPath(resource.getResourcePath());
                rd.setOperations(new ArrayList<ResourceOperationDeclaration>());

                if (resource.getOperations() != null) {
                    for (Operation operation : resource.getOperations()) {
                        ResourceOperationDeclaration rod = new ResourceOperationDeclaration();
                        // rod.setAuthorizations(authorizations);
                        rod.setConsumes(operation.getConsumes());
                        // rod.setDeprecated(deprecated);
                        rod.setMethod(operation.getMethod());
                        rod.setNickname(operation.getName());
                        // rod.setNotes(notes);
                        rod.setParameters(parameters);
                        rod.setProduces(operation.getProduces());
                        rod.setResponseMessages(responseMessages);
                        rod.setSummary(operation.getDescription());

                        if (operation.getOutRepresentation() != null) {
                            if (operation.getOutRepresentation().isArray()) {
                                rod.setType("array");
                                if (operation.getOutRepresentation()
                                        .getRepresentation() != null) {
                                    // TODO how to set either type or ref?
                                    rod.setItems(new ItemsDeclaration());
                                    rod.getItems().setType(
                                            operation.getOutRepresentation()
                                                    .getRepresentation());
                                    rod.getItems().setRef(
                                            operation.getOutRepresentation()
                                                    .getRepresentation());
                                    // rod.getItems().setFormat(format);;
                                }
                            } else {
                                // TODO how to set either type or ref?
                                rod.setType(operation.getOutRepresentation()
                                        .getRepresentation());
                                rod.setRef(operation.getOutRepresentation()
                                        .getRepresentation());
                            }
                        } else if (operation.getResponses() != null) {
                            for (Response response : operation.getResponses()) {
                                if (Status.isSuccess(response.getCode())) {
                                    
                                    
                                }
                            }
                        }
                        
                        rd.getOperations().add(rod);
                    }
                }

                result.getApis().add(rd);
            }
            definition.getContract().getRepresentations();
            definition.getContract().getResources();

        }

        return result;

    }

    public ApiDeclaration getApiDeclaration(String resource) {
        ApiDeclaration result = null;

        return result;
    }

    public Definition convert(ResourceListing swagDec,
            Map<String, ApiDeclaration> apis) {
        List<String> declaredTypes = new ArrayList<String>();
        List<String> declaredPathVariables;
        Map<String, List<String>> subtypes = new HashMap<String, List<String>>();

        // Get the Swagger compliant JSON
        try {
            Definition rwadef = new Definition();
            rwadef.setVersion(swagDec.getApiVersion());
            rwadef.setContact(swagDec.getInfo().getContact());
            rwadef.setLicense(swagDec.getInfo().getLicenseUrl());
            Contract rwadContract = new Contract();
            rwadContract.setName(swagDec.getInfo().getTitle());
            LOGGER.info("Contract " + rwadContract.getName() + " added.");
            rwadContract.setDescription(swagDec.getInfo().getDescription());
            rwadef.setContract(rwadContract);

            // Resource listing
            Resource rwadResource;
            for (Entry<String, ApiDeclaration> entry : apis.entrySet()) {
                ApiDeclaration swagApiResourceDec = entry.getValue();
                List<String> apiProduces = new ArrayList<String>();
                List<String> apiConsumes = new ArrayList<String>();
                if (swagApiResourceDec.getProduces() != null) {
                    apiProduces = swagApiResourceDec.getProduces();
                }
                if (swagApiResourceDec.getConsumes() != null) {
                    apiConsumes = swagApiResourceDec.getConsumes();
                }

                for (ResourceDeclaration api : swagApiResourceDec.getApis()) {
                    declaredPathVariables = new ArrayList<String>();
                    rwadResource = new Resource();
                    rwadResource.setResourcePath(api.getPath());

                    // Operations listing
                    Operation rwadOperation;
                    for (ResourceOperationDeclaration swagOperation : api
                            .getOperations()) {
                        String methodName = swagOperation.getMethod();
                        if ("OPTIONS".equals(methodName)
                                || "PATCH".equals(methodName)) {
                            LOGGER.info("Method " + methodName + " ignored.");
                            continue;
                        }
                        rwadOperation = new Operation();
                        rwadOperation.setMethod(swagOperation.getMethod());
                        rwadOperation.setName(swagOperation.getNickname());

                        // Set variants
                        for (String produced : apiProduces.isEmpty() ? swagOperation
                                .getProduces() : apiProduces) {
                            rwadOperation.getProduces().add(produced);
                        }
                        for (String consumed : apiConsumes.isEmpty() ? swagOperation
                                .getConsumes() : apiConsumes) {
                            rwadOperation.getConsumes().add(consumed);
                        }

                        // Set outrepresentation
                        Body rwadOutRepr = new Body();
                        if ("array".equals(swagOperation.getType())) {
                            LOGGER.fine("Operation: "
                                    + swagOperation.getNickname()
                                    + " returns an array");
                            rwadOutRepr.setArray(true);
                            if (swagOperation.getItems() != null
                                    && swagOperation.getItems().getType() != null) {
                                rwadOutRepr.setRepresentation(swagOperation
                                        .getItems().getType());
                            } else {
                                rwadOutRepr.setRepresentation(swagOperation
                                        .getItems().getRef());
                            }
                        } else {
                            LOGGER.fine("Operation: "
                                    + swagOperation.getNickname()
                                    + " returns a single Representation");
                            rwadOutRepr.setArray(false);
                            if (swagOperation.getType() != null) {
                                rwadOutRepr.setRepresentation(swagOperation
                                        .getType());
                            } else {
                                rwadOutRepr.setRepresentation(swagOperation
                                        .getRef());
                            }
                        }
                        rwadOperation.setOutRepresentation(rwadOutRepr);

                        // Extract success response message
                        Response success = new Response();
                        success.setCode(200);
                        success.setBody(rwadOutRepr);
                        success.setDescription("Success");
                        success.setMessage("The request has succeeded");
                        success.setName("Success");
                        rwadOperation.getResponses().add(success);

                        // Set path variables
                        for (ResourceOperationParameterDeclaration swagPathVariable : getParametersByType(
                                swagOperation, "path")) {
                            if (!declaredPathVariables
                                    .contains(swagPathVariable.getName())) {
                                declaredPathVariables.add(swagPathVariable
                                        .getName());
                                PathVariable rwadPathVariable = new PathVariable();
                                rwadPathVariable.setName(swagPathVariable
                                        .getName());
                                rwadPathVariable
                                        .setDescription(swagPathVariable
                                                .getDescription());
                                rwadPathVariable.setArray(swagPathVariable
                                        .isAllowMultiple());
                                rwadResource.getPathVariables().add(
                                        rwadPathVariable);
                            }
                        }

                        // Set inRepresentation
                        List<ResourceOperationParameterDeclaration> swagBodyParams;
                        if (!(swagBodyParams = getParametersByType(
                                swagOperation, "body")).isEmpty()) {
                            Body rwadInRepr = new Body();
                            ResourceOperationParameterDeclaration swagBodyParam = swagBodyParams
                                    .get(0);
                            if ("array".equals(swagBodyParam.getType())) {
                                rwadInRepr.setArray(true);
                                rwadInRepr.setRepresentation(swagBodyParam
                                        .getItems().getType());
                            } else {
                                rwadInRepr.setArray(false);
                                rwadInRepr.setRepresentation(swagBodyParam
                                        .getType());
                            }
                            rwadOperation.setInRepresentation(rwadInRepr);
                        }

                        // Set query parameters
                        for (ResourceOperationParameterDeclaration swagQueryParam : getParametersByType(
                                swagOperation, "query")) {
                            QueryParameter rwadQueryParam = new QueryParameter();
                            rwadQueryParam.setName(swagQueryParam.getName());
                            rwadQueryParam.setDescription(swagQueryParam
                                    .getDescription());
                            rwadQueryParam.setRequired(swagQueryParam
                                    .isRequired());
                            rwadQueryParam.setAllowMultiple(swagQueryParam
                                    .isAllowMultiple());
                            rwadQueryParam.setDefaultValue(swagQueryParam
                                    .getDefaultValue());
                            if (swagQueryParam.getEnum_() != null
                                    && !swagQueryParam.getEnum_().isEmpty()) {
                                rwadQueryParam
                                        .setPossibleValues(new ArrayList<String>());
                                for (String value : swagQueryParam.getEnum_()) {
                                    rwadQueryParam.getPossibleValues().add(
                                            value);
                                }
                            }
                            rwadOperation.getQueryParameters().add(
                                    rwadQueryParam);
                        }

                        // Set response messages
                        if (swagOperation.getResponseMessages() != null) {
                            for (ResponseMessageDeclaration swagResponse : swagOperation
                                    .getResponseMessages()) {
                                Response rwadResponse = new Response();
                                Body body = new Body();
                                body.setRepresentation(swagResponse
                                        .getResponseModel());
                                rwadResponse.setBody(body);
                                rwadResponse.setName("Error "
                                        + swagResponse.getCode());
                                rwadResponse.setCode(swagResponse.getCode());
                                rwadResponse.setMessage(swagResponse
                                        .getMessage());
                                rwadOperation.getResponses().add(rwadResponse);
                            }
                        }

                        rwadOperation
                                .setDescription(swagOperation.getSummary());
                        rwadResource.getOperations().add(rwadOperation);
                        LOGGER.info("Method " + methodName + " added.");

                        // Add representations
                        Iterator<Entry<String, ModelDeclaration>> representationsIt = swagApiResourceDec
                                .getModels().entrySet().iterator();
                        while (representationsIt.hasNext()) {
                            Entry<String, ModelDeclaration> representationPair = representationsIt
                                    .next();
                            ModelDeclaration swagRepresentation = representationPair
                                    .getValue();
                            if (swagRepresentation.getSubTypes() != null
                                    && !swagRepresentation.getSubTypes()
                                            .isEmpty()) {
                                subtypes.put(swagRepresentation.getId(),
                                        swagRepresentation.getSubTypes());
                            }
                            if (!declaredTypes.contains(representationPair
                                    .getKey())) {
                                declaredTypes.add(representationPair.getKey());
                                Representation rwadRepr = new Representation();
                                rwadRepr.setName(representationPair.getKey());
                                rwadRepr.setDescription(swagRepresentation
                                        .getDescription());

                                // Set properties
                                Iterator<Entry<String, TypePropertyDeclaration>> propertiesIt = representationPair
                                        .getValue().getProperties().entrySet()
                                        .iterator();
                                while (propertiesIt.hasNext()) {
                                    Entry<String, TypePropertyDeclaration> propertiesPair = propertiesIt
                                            .next();
                                    TypePropertyDeclaration swagProperty = propertiesPair
                                            .getValue();
                                    boolean isArray = "array"
                                            .equals(swagProperty.getType());

                                    Property rwadProperty = new Property();
                                    rwadProperty.setName(propertiesPair
                                            .getKey());

                                    // Set property's type
                                    if (isArray) {
                                        rwadProperty
                                                .setType(swagProperty
                                                        .getItems().getType() != null ? swagProperty
                                                        .getItems().getType()
                                                        : swagProperty
                                                                .getItems()
                                                                .getRef());
                                    } else if (swagProperty.getType() != null) {
                                        rwadProperty.setType(swagProperty
                                                .getType());
                                    } else if (swagProperty.getRef() != null) {
                                        rwadProperty.setType(swagProperty
                                                .getRef());
                                    }
                                    if (swagRepresentation.getRequired() != null) {
                                        rwadProperty
                                                .setMinOccurs(swagRepresentation
                                                        .getRequired()
                                                        .contains(
                                                                propertiesPair
                                                                        .getKey()) ? 1
                                                        : 0);
                                    } else {
                                        rwadProperty.setMinOccurs(0);
                                    }
                                    if (isArray) {
                                        rwadProperty.setMaxOccurs(-1);
                                    } else {
                                        rwadProperty.setMaxOccurs(1);
                                    }
                                    rwadProperty.setDescription(swagProperty
                                            .getDescription());
                                    rwadProperty.setMin(swagProperty
                                            .getMinimum());
                                    rwadProperty.setMax(swagProperty
                                            .getMinimum());
                                    rwadProperty.setUniqueItems(swagProperty
                                            .isUniqueItems());

                                    rwadRepr.getProperties().add(rwadProperty);
                                    LOGGER.info("Property "
                                            + rwadProperty.getName()
                                            + " added.");
                                }

                                rwadContract.getRepresentations().add(rwadRepr);
                                LOGGER.info("Representation "
                                        + rwadRepr.getName() + " added.");
                            }
                        }

                        // Deal with subtyping
                        Iterator<Entry<String, List<String>>> subtypesIt = subtypes
                                .entrySet().iterator();
                        while (subtypesIt.hasNext()) {
                            Entry<String, List<String>> subtypesPair = subtypesIt
                                    .next();
                            List<String> subtypesOf = subtypesPair.getValue();
                            for (String subtypeOf : subtypesOf) {
                                Representation repr = getRepresentationByName(
                                        subtypeOf, rwadContract);
                                repr.setParentType(subtypesPair.getKey());
                            }
                        }
                    }

                    rwadef.getContract().getResources().add(rwadResource);
                    LOGGER.info("Resource " + api.getPath() + " added.");
                }

                if (rwadef.getEndpoint() == null) {
                    rwadef.setEndpoint(swagApiResourceDec.getBasePath());
                }
            }
            LOGGER.info("Definition successfully retrieved from Swagger definition");
            return rwadef;
        } catch (ResourceException e) {
            LOGGER.severe("Impossible to read your API definition, check your Swagger file compliance"
                    + e);
        }
        return null;
    }

    private Representation getRepresentationByName(String name,
            Contract contract) {
        for (Representation repr : contract.getRepresentations()) {
            if (repr.getName().equals(name)) {
                return repr;
            }
        }
        return null;
    }

    private List<ResourceOperationParameterDeclaration> getParametersByType(
            ResourceOperationDeclaration swagOperation, String type) {
        List<ResourceOperationParameterDeclaration> params = new ArrayList<ResourceOperationParameterDeclaration>();
        for (ResourceOperationParameterDeclaration param : swagOperation
                .getParameters()) {
            if (param.getParamType().equals(type)) {
                params.add(param);
            }
        }
        return params;
    }
}
