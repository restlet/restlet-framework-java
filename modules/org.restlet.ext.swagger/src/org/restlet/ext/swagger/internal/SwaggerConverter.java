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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.restlet.ext.swagger.internal.model.swagger.ApiDeclaration;
import org.restlet.ext.swagger.internal.model.swagger.ApiInfo;
import org.restlet.ext.swagger.internal.model.swagger.ItemsDeclaration;
import org.restlet.ext.swagger.internal.model.swagger.ModelDeclaration;
import org.restlet.ext.swagger.internal.model.swagger.ResourceDeclaration;
import org.restlet.ext.swagger.internal.model.swagger.ResourceListing;
import org.restlet.ext.swagger.internal.model.swagger.ResourceOperationDeclaration;
import org.restlet.ext.swagger.internal.model.swagger.ResourceOperationParameterDeclaration;
import org.restlet.ext.swagger.internal.model.swagger.ResponseMessageDeclaration;
import org.restlet.ext.swagger.internal.model.swagger.TypePropertyDeclaration;

/**
 * Tool library for converting Restlet Web API Definition to and from Swagger
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerConverter {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(SwaggerConverter.class
            .getName());

    /** Supported version of Swagger. */
    private static final String SWAGGER_VERSION = "1.2";

    /**
     * Converts a Swagger documentation to a Restlet definition.
     * 
     * @param resourceListing
     *            The Swagger resource listing.
     * @param apiDeclarations
     *            The Swagger list of Swagger API declarations.
     * @return The Restlet definition.
     * @throws SwaggerConversionException
     */
    public static Definition convert(ResourceListing resourceListing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws SwaggerConversionException {

        validate(resourceListing, apiDeclarations);

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
                            LOGGER.log(Level.FINER, "Operation: "
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
                            LOGGER.log(Level.FINER, "Operation: "
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
                                        rwadContract, subtypeOf);
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

    /**
     * Retrieves the Swagger API declaration corresponding to a category of the
     * given Restlet Web API Definition
     * 
     * @param category
     *            The category of the API declaration
     * @param def
     *            The Restlet Web API Definition
     * @return The Swagger API definition of the given category
     */
    public static ApiDeclaration getApiDeclaration(String category,
            Definition def) {
        ApiDeclaration result = new ApiDeclaration();
        result.setApiVersion(def.getVersion());
        result.setBasePath(def.getEndpoint());
        result.setInfo(new ApiInfo());
        result.setSwaggerVersion(SWAGGER_VERSION);
        result.setResourcePath("/" + category);
        Set<String> usedModels = new HashSet<String>();

        // Get resources
        for (Resource resource : def.getContract().getResources()) {
            // Discriminate the resources of one category
            if (!resource.getResourcePath().startsWith("/" + category)) {
                continue;
            }
            ResourceDeclaration rd = new ResourceDeclaration();
            rd.setPath(resource.getResourcePath());
            rd.setDescription(resource.getDescription());

            // Get operations
            for (Operation operation : resource.getOperations()) {
                ResourceOperationDeclaration rod = new ResourceOperationDeclaration();
                rod.setMethod(operation.getMethod());
                rod.setSummary(operation.getDescription());
                rod.setNickname(operation.getName());
                rod.setProduces(operation.getProduces());
                rod.setConsumes(operation.getConsumes());

                // Get path variables
                ResourceOperationParameterDeclaration ropd;
                for (PathVariable pv : resource.getPathVariables()) {
                    ropd = new ResourceOperationParameterDeclaration();
                    ropd.setParamType("path");
                    ropd.setType("string");
                    ropd.setRequired(true);
                    ropd.setName(pv.getName());
                    ropd.setAllowMultiple(false);
                    ropd.setDescription(pv.getDescription());
                    rod.getParameters().add(ropd);
                }

                // Get in representation
                Body inRepr = operation.getInRepresentation();
                if (inRepr != null) {
                    ropd = new ResourceOperationParameterDeclaration();
                    ropd.setParamType("body");
                    ropd.setRequired(true);
                    if (inRepr.getRepresentation().equals("Representation")) {
                        ropd.setType("File");
                    } else {
                        ropd.setType(swaggerizeType(inRepr.getRepresentation()));
                    }
                    if (inRepr.getRepresentation() != null) {
                        usedModels.add(inRepr.getRepresentation());
                    }
                    rod.getParameters().add(ropd);
                }

                // Get out representation
                Body outRepr = operation.getOutRepresentation();
                if (outRepr != null && outRepr.getRepresentation() != null) {
                    if (outRepr.isArray()) {
                        rod.setType("array");
                        if (isPrimitiveType(outRepr.getRepresentation())) {
                            rod.getItems()
                                    .setType(
                                            swaggerizeType(outRepr
                                                    .getRepresentation()));
                        } else {
                            rod.getItems().setRef(outRepr.getRepresentation());
                        }
                    } else {
                        rod.setType(swaggerizeType(outRepr.getRepresentation()));
                    }
                    if (outRepr.getRepresentation() != null) {
                        usedModels.add(outRepr.getRepresentation());
                    }
                } else {
                    rod.setType("void");
                }

                // Get query parameters
                for (QueryParameter qp : operation.getQueryParameters()) {
                    ropd = new ResourceOperationParameterDeclaration();
                    ropd.setParamType("query");
                    ropd.setType("string");
                    ropd.setName(qp.getName());
                    ropd.setAllowMultiple(true);
                    ropd.setDescription(qp.getDescription());
                    ropd.setEnum_(qp.getPossibleValues());
                    ropd.setDefaultValue(qp.getDefaultValue());
                    rod.getParameters().add(ropd);
                }

                // Get response messages
                for (Response response : operation.getResponses()) {
                    ResponseMessageDeclaration rmd = new ResponseMessageDeclaration();
                    rmd.setCode(response.getCode());
                    rmd.setMessage(response.getMessage());
                    rmd.setResponseModel(response.getBody().getRepresentation());
                    rod.getResponseMessages().add(rmd);
                }

                rd.getOperations().add(rod);
            }
            result.getApis().add(rd);
        }

        result.setModels(new TreeMap<String, ModelDeclaration>());
        Iterator<String> iterator = usedModels.iterator();
        while (iterator.hasNext()) {
            String model = iterator.next();
            Representation repr = getRepresentationByName(def.getContract(),
                    model);
            if (repr == null || isPrimitiveType(model)) {
                continue;
            }
            ModelDeclaration md = new ModelDeclaration();
            md.setId(model);
            md.setDescription(repr.getDescription());
            for (Property prop : repr.getProperties()) {
                if (prop.isRequired()) {
                    md.getRequired().add(prop.getName());
                }
                if (!isPrimitiveType(prop.getType())
                        && !usedModels.contains(prop.getType())) {
                    usedModels.add(prop.getType());
                    iterator = usedModels.iterator();
                }
                TypePropertyDeclaration tpd = new TypePropertyDeclaration();
                tpd.setDescription(prop.getDescription());
                tpd.setEnum_(prop.getPossibleValues());

                if (prop.getMaxOccurs() > 1 || prop.getMaxOccurs() == -1) {
                    tpd.setType("array");
                    tpd.setItems(new ItemsDeclaration());
                    if (isPrimitiveType(prop.getType())) {
                        tpd.getItems().setType(swaggerizeType(prop.getType()));
                    } else {
                        tpd.getItems().setRef(prop.getType());
                    }
                } else {
                    if (isPrimitiveType(prop.getType())) {
                        tpd.setType(swaggerizeType(prop.getType()));
                    } else {
                        tpd.setRef(prop.getType());
                    }
                }
                tpd.setMaximum(prop.getMax());
                tpd.setMinimum(prop.getMin());
                tpd.setUniqueItems(prop.isUniqueItems());

                md.getProperties().put(prop.getName(), tpd);
            }
            result.getModels().put(md.getId(), md);
        }

        Collections.sort(result.getApis(),
                new Comparator<ResourceDeclaration>() {
                    @Override
                    public int compare(ResourceDeclaration o1,
                            ResourceDeclaration o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }
                });
        return result;
    }

    /**
     * Extracts the first segment of a path. Will retrieve "/pet" from
     * "/pet/{petId}" for example.
     * 
     * @param path
     *            The path of which the segment will be extracted
     * @return The first segment of the given path
     */
    private static String getFirstSegment(String path) {
        String segment = null;
        if (path != null) {
            segment = "/";
            for (String part : path.split("/")) {
                if (part != null && !part.isEmpty()) {
                    segment += part;
                    break;
                }
            }
        }
        return segment;
    }

    /**
     * Returns the list of parameters of a given Swagger operation according to
     * a given type, it returns an empty list when no parameters match the given
     * type.
     * 
     * @param operation
     *            The Swagger operation.
     * @param type
     *            The type of parameters to filter.
     * @return The list of parameters of a given Swagger operation according to
     *         a given type.
     */
    private static List<ResourceOperationParameterDeclaration> getParametersByType(
            ResourceOperationDeclaration operation, String type) {
        List<ResourceOperationParameterDeclaration> params = new ArrayList<ResourceOperationParameterDeclaration>();
        if (type != null) {
            for (ResourceOperationParameterDeclaration param : operation
                    .getParameters()) {
                if (type.equals(param.getParamType())) {
                    params.add(param);
                }
            }
        }
        return params;
    }

    /**
     * Returns the representation given its name from the list of
     * representations of the given contract.
     * 
     * @param contract
     *            The contract.
     * @param name
     *            The name of the representation.
     * @return A representation.
     */
    private static Representation getRepresentationByName(Contract contract,
            String name) {
        if (name != null) {
            for (Representation repr : contract.getRepresentations()) {
                if (name.equals(repr.getName())) {
                    return repr;
                }
            }
        }
        return null;
    }

    /**
     * Converts a Restlet Web API Definition to a Swagger resource listing.
     * 
     * @param def
     *            The Restlet Web API Definition.
     * @return The corresponding resource listing
     */
    public static ResourceListing getResourcelisting(Definition def) {
        ResourceListing result = new ResourceListing();

        // common properties
        result.setApiVersion(def.getVersion());
        result.setBasePath(def.getEndpoint());
        result.setInfo(new ApiInfo());
        result.setSwaggerVersion(SWAGGER_VERSION);
        if (def.getContact() != null) {
            result.getInfo().setContact(def.getContact());
        }
        if (def.getLicense() != null) {
            result.getInfo().setLicenseUrl(def.getLicense());
        }
        if (def.getContract() != null) {
            result.getInfo().setTitle(def.getContract().getName());
            result.getInfo().setDescription(def.getContract().getDescription());
        }
        // Resources
        List<String> addedApis = new ArrayList<String>();
        if (def.getContract() != null
                && def.getContract().getResources() != null) {
            result.setApis(new ArrayList<ResourceDeclaration>());

            for (Resource resource : def.getContract().getResources()) {
                ResourceDeclaration rd = new ResourceDeclaration();
                rd.setDescription(resource.getDescription());
                rd.setPath(getFirstSegment(resource.getResourcePath()));
                if (!addedApis.contains(rd.getPath())) {
                    addedApis.add(rd.getPath());
                    result.getApis().add(rd);
                }
            }
        }
        Collections.sort(result.getApis(),
                new Comparator<ResourceDeclaration>() {
                    @Override
                    public int compare(ResourceDeclaration o1,
                            ResourceDeclaration o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }

                });
        return result;
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
    private static String swaggerizeType(String type) {
        switch (type) {
        case "Integer":
            return "int";
        case "String":
            return "string";
        case "Boolean":
            return "boolean";
        default:
            return type;
        }
    }

    /**
     * Indicates of the given resource listing and list of API declarations
     * match.
     * 
     * @param resourceListing
     *            The Swagger resource listing.
     * @param apiDeclarations
     *            The list of Swagger API declarations.
     * @throws SwaggerConversionException
     */
    private static void validate(ResourceListing resourceListing,
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

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SwaggerConverter() {
    }
}