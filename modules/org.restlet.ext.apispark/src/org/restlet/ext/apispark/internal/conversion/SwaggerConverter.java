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
import java.io.FileNotFoundException;
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
import org.restlet.ext.apispark.internal.model.swagger.ApiDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ModelDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceListing;
import org.restlet.ext.apispark.internal.model.swagger.ResourceOperationDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceOperationParameterDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResponseMessageDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.TypePropertyDeclaration;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ServerResource;

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
        if (address == null) {
            throw new SwaggerConversionException("url",
                    "You did not provide any URL");
        }
        Pattern p = Pattern
                .compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        boolean remote = p.matcher(address).matches();
        boolean apisparkAddress = apisparkAddress(address);
        ResourceListing resourceListing = new ResourceListing();
        Map<String, ApiDeclaration> apis = new HashMap<String, ApiDeclaration>();
        if (remote) {
            LOGGER.log(Level.FINE, "Reading file: " + address);
            resourceListing = createAuthenticatedClientResource(address,
                    userName, password, apisparkAddress).get(
                    ResourceListing.class);
            for (ResourceDeclaration api : resourceListing.getApis()) {
                LOGGER.log(Level.FINE,
                        "Reading file: " + address + api.getPath());
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
                LOGGER.log(Level.FINE, "Base path: " + basePath);
                for (ResourceDeclaration api : resourceListing.getApis()) {
                    LOGGER.log(Level.FINE,
                            "Reading file " + basePath + api.getPath());
                    apis.put(api.getPath(), om.readValue(new File(basePath
                            + api.getPath()), ApiDeclaration.class));
                }
            } catch (IOException e) {
                throw new SwaggerConversionException("file", e.getMessage());
            }
        }
        return convert(resourceListing, apis);
    }

    private ClientResource createAuthenticatedClientResource(String url,
            String userName, String password, boolean apisparkAddress) {
        ClientResource cr = new ClientResource(url);
        cr.accept(MediaType.APPLICATION_JSON);
        if (apisparkAddress) {
            LOGGER.log(Level.FINE, "Internal source: " + userName + " "
                    + password);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, userName,
                    password);
        } else {
            LOGGER.log(Level.FINE, "External source");
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
        int mappedFiles = resourceListing.getApis().size();
        if (mappedFiles < apiDeclarations.size()) {
            throw new SwaggerConversionException("file",
                    "One of your API declarations is not mapped in your resource listing");
        }
        if (mappedFiles > apiDeclarations.size()) {
            throw new SwaggerConversionException("file",
                    "Some API declarations are missing");
        }
    }

    public Definition convert(ResourceListing resourceListing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws SwaggerConversionException {

        validateFiles(resourceListing, apiDeclarations);

        boolean containsRawTypes = false;
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
            LOGGER.log(Level.FINE, "Contract " + rwadContract.getName()
                    + " added.");
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
                            LOGGER.log(Level.FINE, "Method " + methodName
                                    + " ignored.");
                            continue;
                        }
                        rwadOperation = new Operation();
                        rwadOperation.setMethod(swagOperation.getMethod());
                        rwadOperation.setName(swagOperation.getNickname());

                        // Set variants
                        Representation rwadFile;
                        for (String produced : apiProduces.isEmpty() ? swagOperation
                                .getProduces() : apiProduces) {
                            if (!containsRawTypes
                                    && produced.equals("multipart/form-data")) {
                                rwadFile = new Representation();
                                rwadFile.setName("File");
                                rwadFile.setRaw(true);
                                containsRawTypes = true;
                                rwadContract.getRepresentations().add(rwadFile);
                            }
                            rwadOperation.getProduces().add(produced);
                        }
                        for (String consumed : apiConsumes.isEmpty() ? swagOperation
                                .getConsumes() : apiConsumes) {
                            if (!containsRawTypes
                                    && consumed.equals("multipart/form-data")) {
                                rwadFile = new Representation();
                                rwadFile.setName("File");
                                rwadFile.setRaw(true);
                                containsRawTypes = true;
                                rwadContract.getRepresentations().add(rwadFile);
                            }
                            rwadOperation.getConsumes().add(consumed);
                        }

                        // Set outrepresentation
                        Body rwadOutRepr = new Body();
                        if (swagOperation.getType().equals("array")) {
                            LOGGER.log(Level.FINE, "Operation: "
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
                            LOGGER.log(Level.FINE, "Operation: "
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
                                if (swagBodyParam.getItems() != null
                                        && swagBodyParam.getItems().getType() != null) {
                                    rwadInRepr.setRepresentation(swagBodyParam
                                            .getItems().getType());
                                } else {
                                    rwadInRepr.setRepresentation(swagBodyParam
                                            .getItems().getRef());
                                }
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
                        LOGGER.log(Level.FINE, "Method " + methodName
                                + " added.");

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
                                    LOGGER.log(Level.FINE, "Property "
                                            + rwadProperty.getName()
                                            + " added.");
                                }

                                rwadContract.getRepresentations().add(rwadRepr);
                                LOGGER.log(Level.FINE, "Representation "
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
                    LOGGER.log(Level.FINE, "Resource " + api.getPath()
                            + " added.");
                }

                if (rwadef.getEndpoint() == null) {
                    rwadef.setEndpoint(swagApiResourceDec.getBasePath());
                }
            }
            LOGGER.log(Level.FINE,
                    "Definition successfully retrieved from Swagger definition");
            return rwadef;
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                throw new SwaggerConversionException("file",
                        ((FileNotFoundException) e).getMessage());
            } else {
                throw new SwaggerConversionException("compliance",
                        "Impossible to read your API definition, check your Swagger specs compliance");
            }
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