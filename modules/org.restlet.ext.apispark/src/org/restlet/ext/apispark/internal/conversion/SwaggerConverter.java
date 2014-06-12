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

package org.restlet.ext.apispark.internal.conversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.model.Body;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ServerResource;

import org.restlet.ext.apispark.internal.model.swagger.ApiDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ModelDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceListing;
import org.restlet.ext.apispark.internal.model.swagger.ResourceOperationDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceOperationParameterDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResponseMessageDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.TypePropertyDeclaration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Retrieves a Swagger definition and converts it to Restlet Web API Definition.
 * 
 * @author Cyprien Quilici
 */

public class SwaggerConverter extends ServerResource {

    /** Internal logger. */
    protected static Logger LOGGER = Context.getCurrentLogger();

    public Definition getDefinition(String address, String userName,
            String password) throws SwaggerConversionException {

        // Check that URL is non empty and well formed
        Pattern p = Pattern
                .compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        if (address == null) {
            throw new SwaggerConversionException("url",
                    "You did not provide any URL");
        }
        if (!p.matcher(address).matches()) {
            throw new SwaggerConversionException("url",
                    "You did not provide a valid URL");
        }
        boolean remote = address.startsWith("http");
        boolean apisparkAddress = apisparkAddress(address);
        ResourceListing resourceListing = new ResourceListing();
        Map<String, ApiDeclaration> apis = new HashMap<String, ApiDeclaration>();
        if (remote) {
            LOGGER.info("Reading file: " + address);
            resourceListing = createAuthenticatedClientResource(address,
                    userName, password, apisparkAddress).get(
                    ResourceListing.class);
            for (ResourceDeclaration api : resourceListing.getApis()) {
                LOGGER.info("Reading file: " + address + api.getPath());
                apis.put(
                        api.getPath().replaceAll("/", ""),
                        createAuthenticatedClientResource(
                                address + api.getPath(), userName, password,
                                apisparkAddress).get(ApiDeclaration.class));
            }
        } else {
            File resourceListingFile = new File(address);
            ObjectMapper om = new ObjectMapper();
            try {
                resourceListing = om.readValue(resourceListingFile,
                        ResourceListing.class);
                String basePath = resourceListingFile.getParent();
                LOGGER.info("Base path: " + basePath);
                for (ResourceDeclaration api : resourceListing.getApis()) {
                    LOGGER.info("Reading file " + basePath + api.getPath());
                    apis.put(api.getPath(), om.readValue(new File(basePath
                            + api.getPath()), ApiDeclaration.class));
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Cannot read file", e);
            }
        }
        return convert(resourceListing, apis);
    }

    private ClientResource createAuthenticatedClientResource(String url,
            String userName, String password, boolean apisparkAddress) {
        ClientResource cr = new ClientResource(url);
        cr.accept(MediaType.APPLICATION_JSON);
        if (apisparkAddress) {
            LOGGER.fine("Internal source: " + userName + " " + password);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, userName,
                    password);
        } else {
            LOGGER.fine("External source");
        }
        return cr;
    }

    private boolean apisparkAddress(String address) {
        Pattern p = Pattern
                .compile("http[s]?://[^/]+/apis/[0-9]+/versions/[0-9]+/swagger(/[a-z]+/?)?");
        Matcher m = p.matcher(address);
        return m.matches();
    }

    private void validateFiles(ResourceListing resourceListing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws SwaggerConversionException {
        List<String> listedApis = new ArrayList<String>();
        for (ResourceDeclaration resourceDeclaration : resourceListing
                .getApis()) {
            listedApis.add(resourceDeclaration.getPath().replaceAll("/", ""));
        }
        for (Entry<String, ApiDeclaration> entry : apiDeclarations.entrySet()) {
            if (!listedApis.contains(entry.getKey())) {
                throw new SwaggerConversionException("file", "The file "
                        + entry.getKey()
                        + " is not mapped in your resource listing");
            }
        }
        for (String listedApi : listedApis) {
            if (!apiDeclarations.containsKey(listedApi)) {
                throw new SwaggerConversionException("file", "The file "
                        + listedApi + " is missing");
            }
        }
    }

    public Definition convert(ResourceListing resourceListing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws SwaggerConversionException {

        validateFiles(resourceListing, apiDeclarations);

        List<String> declaredTypes = new ArrayList<String>();
        List<String> declaredPathVariables;
        Map<String, List<String>> subtypes = new HashMap<String, List<String>>();

        // Get the Swagger compliant JSON
        try {
            Definition rwadef = new Definition();
            rwadef.setVersion(resourceListing.getApiVersion());
            rwadef.setContact(resourceListing.getInfo().getContact());
            rwadef.setLicense(resourceListing.getInfo().getLicenseUrl());
            Contract rwadContract = new Contract();
            rwadContract.setName(resourceListing.getInfo().getTitle());
            LOGGER.info("Contract " + rwadContract.getName() + " added.");
            rwadContract.setDescription(resourceListing.getInfo()
                    .getDescription());
            rwadef.setContract(rwadContract);

            // Resource listing
            Resource rwadResource;
            for (Entry<String, ApiDeclaration> entry : apiDeclarations
                    .entrySet()) {
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
                        if (swagOperation.getType().equals("array")) {
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
                            if (swagBodyParam.getType().equals("array")) {
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
                                    Property rwadProperty = new Property();
                                    rwadProperty.setName(propertiesPair
                                            .getKey());

                                    // Set property's type
                                    if (swagProperty.getType() != null
                                            && !swagProperty.getType().equals(
                                                    "array")) {
                                        rwadProperty.setType(swagProperty
                                                .getType());
                                    } else if (swagProperty.getType() != null
                                            && swagProperty.getType().equals(
                                                    "array")) {
                                        rwadProperty
                                                .setType(swagProperty
                                                        .getItems().getType() != null ? swagProperty
                                                        .getItems().getType()
                                                        : swagProperty
                                                                .getItems()
                                                                .getRef());
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
                                    if (swagProperty.getType() != null) {
                                        rwadProperty.setMaxOccurs(swagProperty
                                                .getType().equals("array") ? -1
                                                : 1);
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
        } catch (Exception e) {
            LOGGER.severe("Impossible to read your API definition, check your Swagger specs compliance"
                    + e);
            throw new SwaggerConversionException("compliance",
                    "Impossible to read your API definition, check your Swagger specs compliance");
        }
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
