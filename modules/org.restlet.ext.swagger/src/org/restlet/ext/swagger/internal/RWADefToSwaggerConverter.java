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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.restlet.Context;
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
import org.restlet.resource.ServerResource;

/**
 * Retrieves Swagger Resource Listing or API Declaration given a Restlet Web API
 * Definition.
 * 
 * @author Cyprien Quilici
 */

public class RWADefToSwaggerConverter extends ServerResource {

    private final String SwaggerVersion = "1.2";

    /** Internal logger. */
    protected static Logger LOGGER = Context.getCurrentLogger();

    /**
     * Retrieves the Swagger resource listing from a Restlet Web API Definition
     * 
     * @param def
     *            The Restlet Web API Definition
     * @return The corresponding resource listing
     */
    public ResourceListing getResourcelisting(Definition def) {
        ResourceListing result = new ResourceListing();

        // common properties
        result.setApiVersion(def.getVersion() == null ? "1.0" : def
                .getVersion());
        result.setBasePath(def.getEndpoint() == null ? "http://localhost:9000/v1"
                : def.getEndpoint());
        result.setInfo(new ApiInfo());
        result.setSwaggerVersion(SwaggerVersion);
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
     * Retrieves the Swagger API declaration corresponding to a category of the
     * given Restlet Web API Definition
     * 
     * @param category
     *            The category of the API declaration
     * @param def
     *            The Restlet Web API Definition
     * @return The Swagger API definition of the given category
     */
    public ApiDeclaration getApiDeclaration(String category, Definition def) {
        ApiDeclaration result = new ApiDeclaration();
        result.setApiVersion(def.getVersion() == null ? "1.0" : def
                .getVersion());
        result.setBasePath(def.getEndpoint() == null ? "http://localhost:9000/v1"
                : def.getEndpoint());
        result.setInfo(new ApiInfo());
        result.setSwaggerVersion(SwaggerVersion);
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
            Representation repr = getRepresentationByName(model,
                    def.getContract());
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
     * Retrieves a representation from a Restlet Web API Definition given its
     * name
     * 
     * @param name
     *            The name of the representation
     * @param contract
     *            The contract, extracted from the Restlet Web API Definition
     * @return The representation of the given name
     */
    private Representation getRepresentationByName(String name,
            Contract contract) {
        for (Representation repr : contract.getRepresentations()) {
            if (repr.getName().equals(name)) {
                return repr;
            }
        }
        return null;
    }

    /**
     * Extracts the first segment of a path. Will retrieve "/pet" from
     * "/pet/{petId}" for example.
     * 
     * @param path
     *            The path of which the segment will be extracted
     * @return The first segment of the given path
     */
    private String getFirstSegment(String path) {
        String segment = null;
        if (path != null) {
            segment = "/";
            for (String part : path.split("/")) {
                if (part != null && !"".equals(part)) {
                    segment += part;
                    break;
                }
            }
        }
        return segment;
    }

    /**
     * Returns true if the given type is a primitive type, false otherwise.
     * 
     * @param type
     *            The type to be analysed
     * @return A boolean of value true if the given type is primitive, false
     *         otherwise.
     */
    private boolean isPrimitiveType(String type) {
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
        } else {
            return false;
        }
    }

    /**
     * Returns the primitive types as Swagger expects them
     * 
     * @param type
     *            The type name to Swaggerize
     * @return The Swaggerized type
     */
    private String swaggerizeType(String type) {
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
}
