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

package org.restlet.ext.raml.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.parameter.UriParameter;
import org.restlet.data.Status;
import org.restlet.ext.apispark.model.Contract;
import org.restlet.ext.apispark.model.Definition;
import org.restlet.ext.apispark.model.Operation;
import org.restlet.ext.apispark.model.PathVariable;
import org.restlet.ext.apispark.model.Property;
import org.restlet.ext.apispark.model.QueryParameter;
import org.restlet.ext.apispark.model.Representation;
import org.restlet.ext.apispark.model.Resource;
import org.restlet.ext.apispark.model.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;

/**
 * Tools library for converting Restlet Web API Definition to and from RAML
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public abstract class RamlTranslator {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(RamlTranslator.class
            .getName());

    /**
     * Returns the {@link PathVariable} as described by the given
     * {@link UriParameter}.
     * 
     * @param paramName
     *            The name of the path variable.
     * @param uriParameter
     *            The uri parameter.
     * @return The {@link PathVariable} as described by the given
     *         {@link UriParameter}.
     */
    private static PathVariable getPathVariable(String paramName,
            UriParameter uriParameter) {
        PathVariable pathVariable = new PathVariable();
        pathVariable.setName(paramName);
        pathVariable.setDescription(uriParameter.getDescription());
        // pathVariable.setType(uriParameter.getType().toString().toLowerCase());
        pathVariable.setArray(uriParameter.isRepeat());
        return pathVariable;
    }

    /**
     * Returns the list of {@link PathVariable} as defined by the given
     * {@link org.raml.model.Resource}.
     * 
     * @param resource
     *            The given resource.
     * @return The list of {@link PathVariable} as defined by the given
     *         {@link org.raml.model.Resource}.
     */
    private static List<PathVariable> getPathVariables(
            org.raml.model.Resource resource) {
        List<PathVariable> pathVariables = new ArrayList<PathVariable>();
        for (Entry<String, UriParameter> entry : resource.getUriParameters()
                .entrySet()) {
            pathVariables
                    .add(getPathVariable(entry.getKey(), entry.getValue()));
        }
        if (resource.getParentResource() != null) {
            pathVariables
                    .addAll(getPathVariables(resource.getParentResource()));
        }
        return pathVariables;
    }

    /**
     * Retrieves the RAML API declaration corresponding to a category of the
     * given Restlet Web API Definition.
     * 
     * @param definition
     *            The Restlet Web API Definition.
     * @return The RAML API definition of the given category.
     */
    public static Raml getRaml(Definition definition) {
        Raml raml = new Raml();
        ObjectMapper m = new ObjectMapper();
        // TODO see how to translate it (1.0.0 to v1 ???)
        if (definition.getVersion() != null) {
            raml.setVersion(definition.getVersion());
        }
        if (definition.getEndpoint() != null) {
            raml.setBaseUri(definition.getEndpoint());
        }
        // raml.setBaseUriParameters(new HashMap<String, UriParameter>());
        // raml.getBaseUriParameters().put("version", new
        // UriParameter("version"));
        raml.setTitle(definition.getContract().getName());

        raml.setResources(new HashMap<String, org.raml.model.Resource>());
        org.raml.model.Resource ramlResource;
        List<String> paths = new ArrayList<String>();
        for (Resource resource : definition.getContract().getResources()) {
            ramlResource = new org.raml.model.Resource();
            if (resource.getName() != null) {
                ramlResource.setDisplayName(resource.getName());
            } else {
                ramlResource.setDisplayName(RamlUtils
                        .processResourceName(resource.getResourcePath()));
            }
            ramlResource.setDescription(resource.getDescription());

            ramlResource.setParentUri("");
            ramlResource.setRelativeUri(resource.getResourcePath());

            // Path variables
            UriParameter uiParam = new UriParameter();
            ramlResource.setUriParameters(new HashMap<String, UriParameter>());
            for (PathVariable pathVariable : resource.getPathVariables()) {
                uiParam.setDisplayName(pathVariable.getName());
                uiParam.setDescription(pathVariable.getDescription());
                uiParam.setRepeat(pathVariable.isArray());
                uiParam.setType(RamlUtils.getParamType(pathVariable.getType()));
                ramlResource.getUriParameters().put(pathVariable.getName(),
                        uiParam);
            }

            // Operations
            Action action;
            ramlResource.setActions(new HashMap<ActionType, Action>());
            MimeType ramlInRepresentation;
            for (Operation operation : resource.getOperations()) {
                action = new Action();
                action.setDescription(operation.getDescription());
                action.setResource(ramlResource);

                // In representation
                ramlInRepresentation = new MimeType();
                if (operation.getInRepresentation() != null) {
                    ramlInRepresentation.setType(operation
                            .getInRepresentation().getType());
                    if (RamlUtils.isPrimitiveType(operation
                            .getInRepresentation().getType())) {
                        Property inRepresentationPrimitive = new Property();
                        inRepresentationPrimitive.setName("");
                        inRepresentationPrimitive.setType(operation
                                .getInRepresentation().getType());
                        SimpleTypeSchema inRepresentationSchema = RamlUtils
                                .generatePrimitiveSchema(inRepresentationPrimitive);
                        try {
                            ramlInRepresentation
                                    .setSchema(m
                                            .writeValueAsString(inRepresentationSchema));
                        } catch (JsonProcessingException e) {
                            LOGGER.log(Level.WARNING,
                                    "Error when setting mime type schema.", e);
                        }
                    } else {
                        ramlInRepresentation.setSchema(operation
                                .getInRepresentation().getType());
                    }
                    action.setBody(new HashMap<String, MimeType>());
                    for (String mediaType : operation.getConsumes()) {
                        action.getBody().put(mediaType, ramlInRepresentation);
                    }
                }

                // Query parameters
                org.raml.model.parameter.QueryParameter ramlQueryParameter;
                action.setQueryParameters(new HashMap<String, org.raml.model.parameter.QueryParameter>());
                for (QueryParameter queryParameter : operation
                        .getQueryParameters()) {
                    ramlQueryParameter = new org.raml.model.parameter.QueryParameter();
                    ramlQueryParameter.setDisplayName(queryParameter.getName());
                    // ramlQueryParameter.setType(RamlUtils
                    // .getParamType(queryParameter.getType()));
                    ramlQueryParameter.setDescription(queryParameter
                            .getDescription());
                    ramlQueryParameter.setRequired(queryParameter.isRequired());
                    // TODO when enumerations have been added in RWADef
                    // ramlQueryParameter.setEnumeration(queryParameter.getEnumeration());
                    ramlQueryParameter.setDefaultValue(queryParameter
                            .getDefaultValue());
                    ramlQueryParameter.setRepeat(queryParameter
                            .isAllowMultiple());
                    action.getQueryParameters().put(queryParameter.getName(),
                            ramlQueryParameter);
                }

                // Responses + out representation
                MimeType ramlOutRepresentation;
                org.raml.model.Response ramlResponse = new org.raml.model.Response();
                action.setResponses(new HashMap<String, org.raml.model.Response>());
                for (Response response : operation.getResponses()) {
                    ramlResponse = new org.raml.model.Response();
                    ramlResponse.setDescription(response.getDescription());
                    ramlResponse.setBody(new HashMap<String, MimeType>());
                    ramlOutRepresentation = new MimeType();
                    if (Status.isSuccess(response.getCode())
                            && operation.getOutRepresentation() != null
                            && operation.getOutRepresentation().getType() != null) {
                        if (RamlUtils.isPrimitiveType(operation
                                .getOutRepresentation().getType())) {
                            Property outRepresentationPrimitive = new Property();
                            outRepresentationPrimitive.setName("");
                            outRepresentationPrimitive.setType(operation
                                    .getOutRepresentation().getType());
                            SimpleTypeSchema outRepresentationSchema = RamlUtils
                                    .generatePrimitiveSchema(outRepresentationPrimitive);
                            try {
                                ramlOutRepresentation
                                        .setSchema(m
                                                .writeValueAsString(outRepresentationSchema));
                            } catch (JsonProcessingException e) {
                                LOGGER.log(Level.WARNING,
                                        "Error when setting mime type schema.",
                                        e);
                            }
                        } else {
                            ramlOutRepresentation.setSchema(operation
                                    .getOutRepresentation().getType());
                        }
                    }
                    for (String mediaType : operation.getProduces()) {
                        ramlResponse.getBody().put(mediaType,
                                ramlOutRepresentation);
                    }
                    action.getResponses().put(
                            Integer.toString(response.getCode()), ramlResponse);
                }

                ramlResource.getActions().put(
                        RamlUtils.getActionType(operation.getMethod()), action);
            }
            paths.add(resource.getResourcePath());

            raml.getResources()
                    .put(ramlResource.getRelativeUri(), ramlResource);
        }

        // Representations
        raml.setSchemas(new ArrayList<Map<String, String>>());
        Map<String, String> schemas = new HashMap<String, String>();
        raml.getSchemas().add(schemas);
        for (Representation representation : definition.getContract()
                .getRepresentations()) {
            if (RamlUtils.isPrimitiveType(representation.getName())) {
                continue;
            }
            try {
                schemas.put(representation.getName(), m
                        .writeValueAsString(RamlUtils
                                .generateSchema(representation)));
            } catch (JsonProcessingException e) {
                LOGGER.log(Level.WARNING,
                        "Error when putting mime type schema for representation: "
                                + representation.getName(), e);
            }
        }
        return raml;
    }

    /**
     * Returns the representation given its name from the given list of
     * representations.
     * 
     * @param representations
     *            The list of representations.
     * @param name
     *            The name of the representation.
     * @return A representation.
     */
    public static Representation getRepresentationByName(
            List<Representation> representations, String name) {
        if (name != null) {
            for (Representation repr : representations) {
                if (name.equals(repr.getName())) {
                    return repr;
                }
            }
        }
        return null;
    }

    /**
     * Returns the list of Resources nested under a given Resource.
     * 
     * @param resourceName
     *            The name of the generated resource, extracted from its path.
     * @param resource
     *            The RAML Resource from which the list is extracted.
     * @param rootPathVariables
     *            The path variables contained in the base URI.
     * @return The list of Resources nested under resource.
     */
    private static List<Resource> getResource(String resourceName,
            org.raml.model.Resource resource,
            List<PathVariable> rootPathVariables) {
        List<Resource> rwadResources = new ArrayList<Resource>();

        // Create one resource
        Resource rwadResource = new Resource();
        rwadResource.setDescription(resource.getDescription());
        rwadResource.setName(resourceName);
        rwadResource.setResourcePath(resource.getUri());

        // Path Variables
        rwadResource.setPathVariables(getPathVariables(resource));
        rwadResource.getPathVariables().addAll(rootPathVariables);

        // Operations
        for (Entry<ActionType, Action> entry : resource.getActions().entrySet()) {
            Action action = entry.getValue();
            Operation operation = new Operation();
            operation.setDescription(action.getDescription());
            operation.setMethod(entry.getKey().name().toString());
        }

        rwadResources.add(rwadResource);

        // Nested resources
        for (Entry<String, org.raml.model.Resource> entry : resource
                .getResources().entrySet()) {
            rwadResources.addAll(getResource(
                    RamlUtils.processResourceName(entry.getValue().getUri()),
                    entry.getValue(), rootPathVariables));
        }

        return rwadResources;
    }

    /**
     * Translates a RAML documentation to a Restlet definition.
     * 
     * @param raml
     *            The RAML resource listing.
     * @return The Restlet definition.
     * @throws TranslationException
     */
    public static Definition translate(Raml raml) throws TranslationException {
        Definition def = new Definition();
        if (raml.getVersion() != null) {
            def.setVersion(raml.getVersion().substring(1));
            def.setEndpoint(raml.getBaseUri().replace("{version}",
                    raml.getVersion()));
        } else {
            def.setEndpoint(raml.getBaseUri());
        }
        def.setContract(new Contract());
        def.getContract().setName(raml.getTitle());
        // TODO String defaultMediaType = raml.getMediaType();
        List<PathVariable> rootPathVariables = new ArrayList<PathVariable>();
        for (Entry<String, UriParameter> entry : raml.getBaseUriParameters()
                .entrySet()) {
            rootPathVariables.add(getPathVariable(entry.getKey(),
                    entry.getValue()));
        }

        for (Map<String, String> schema : raml.getSchemas()) {
            for (Entry<String, String> entry : schema.entrySet()) {
                Representation representation = new Representation();
                representation.setName(entry.getKey());
                representation.setDescription(entry.getValue());
                // TODO get the schema !!!
                def.getContract().getRepresentations().add(representation);
            }
        }

        // Resources
        for (Entry<String, org.raml.model.Resource> entry : raml.getResources()
                .entrySet()) {
            org.raml.model.Resource resource = entry.getValue();
            def.getContract()
                    .getResources()
                    .addAll(getResource(
                            RamlUtils.processResourceName(resource.getUri()),
                            resource, rootPathVariables));
        }

        return def;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private RamlTranslator() {
    }
}