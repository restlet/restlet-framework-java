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

package org.restlet.ext.apispark.internal.conversion.raml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.SecurityScheme;
import org.raml.model.parameter.UriParameter;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.conversion.ConversionUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
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
import org.restlet.ext.apispark.internal.utils.SampleUtils;

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
     * Returns the {@link org.restlet.ext.apispark.internal.model.PathVariable}
     * as described by the given {@link org.raml.model.parameter.UriParameter}.
     * 
     * @param paramName
     *            The name of the path variable.
     * @param uriParameter
     *            The uri parameter.
     * @return The {@link org.restlet.ext.apispark.internal.model.PathVariable}
     *         as described by the given
     *         {@link org.raml.model.parameter.UriParameter}.
     */
    private static PathVariable getPathVariable(String paramName,
            UriParameter uriParameter) {
        PathVariable pathVariable = new PathVariable();
        pathVariable.setName(paramName);
        pathVariable.setDescription(uriParameter.getDescription());
        // pathVariable.setType(uriParameter.getType().toString().toLowerCase());
        return pathVariable;
    }

    /**
     * Returns the list of
     * {@link org.restlet.ext.apispark.internal.model.PathVariable} as defined
     * by the given {@link org.raml.model.Resource}.
     * 
     * @param resource
     *            The given resource.
     * @return The list of
     *         {@link org.restlet.ext.apispark.internal.model.PathVariable} as
     *         defined by the given {@link org.raml.model.Resource}.
     */
    private static List<PathVariable> getPathVariables(
            org.raml.model.Resource resource) {
        List<PathVariable> pathVariables = new ArrayList<>();
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
     * Builds a sample map for each Representation of the Contract
     * 
     * @param contract
     *            The Restlet Web API Contract
     * @return A map of representations' names and sample maps
     */
    private static Map<String, Map<String, Object>> getSamples(Contract contract) {
        Map<String, Map<String, Object>> samples = new LinkedHashMap<>();
        for (Representation representation : contract.getRepresentations()) {
            samples.put(representation.getName(),
                    SampleUtils.getRepresentationSample(representation));
        }
        return samples;
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
        Contract contract = definition.getContract();

        Map<String, Map<String, Object>> representationSamples = getSamples(contract);

        // No way to specify multiple endpoints in RAML so we take the first one
        Endpoint endpoint = null;
        if (!definition.getEndpoints().isEmpty()) {
            endpoint = definition.getEndpoints().get(0);
            raml.setBaseUri(endpoint.computeUrl());
        } else {
            raml.setBaseUri("http://example.com/v1");
        }

        // Authentication
        raml.setSecuritySchemes(getSecuritySchemes(endpoint));

        // raml.setBaseUriParameters(new HashMap<String, UriParameter>());
        // raml.getBaseUriParameters().put("version", new
        // UriParameter("version"));
        raml.setTitle(contract.getName());

        raml.setResources(new LinkedHashMap<String, org.raml.model.Resource>());
        fillResources(raml.getResources(), m, contract, representationSamples);

        // Representations
        raml.setSchemas(new ArrayList<Map<String, String>>());
        Map<String, String> schemas = new LinkedHashMap<>();
        raml.getSchemas().add(schemas);
        for (Representation representation : contract.getRepresentations()) {
            if (RamlUtils.isPrimitiveType(representation.getName())) {
                continue;
            }
            try {
                RamlUtils.fillSchemas(representation, schemas, m);
            } catch (JsonProcessingException e) {
                LOGGER.log(Level.WARNING,
                        "Error when putting mime type schema for representation: "
                                + representation.getName(), e);
            }
        }
        return raml;
    }

    private static void fillResources(
            Map<String, org.raml.model.Resource> resources, ObjectMapper m,
            Contract contract,
            Map<String, Map<String, Object>> representationSamples) {

        org.raml.model.Resource ramlResource;
        List<String> paths = new ArrayList<>();

        // Resources
        for (Resource resource : contract.getResources()) {
            ramlResource = new org.raml.model.Resource();
            if (resource.getName() != null) {
                ramlResource.setDisplayName(resource.getName());
            } else {
                ramlResource.setDisplayName(ConversionUtils
                        .processResourceName(resource.getResourcePath()));
            }
            ramlResource.setDescription(resource.getDescription());

            ramlResource.setParentUri("");
            ramlResource.setRelativeUri(resource.getResourcePath());

            // Path variables
            UriParameter uiParam = new UriParameter();
            ramlResource.setUriParameters(new LinkedHashMap<String, UriParameter>());
            for (PathVariable pathVariable : resource.getPathVariables()) {
                uiParam.setDisplayName(pathVariable.getName());
                uiParam.setDescription(pathVariable.getDescription());
                uiParam.setType(RamlUtils.getParamType(pathVariable.getType()));
                uiParam.setExample(pathVariable.getExample());
                ramlResource.getUriParameters().put(pathVariable.getName(),
                        uiParam);
            }

            // Operations
            Action action;
            ramlResource.setActions(new LinkedHashMap<ActionType, Action>());
            for (Operation operation : resource.getOperations()) {
                action = new Action();
                action.setDescription(operation.getDescription());
                action.setResource(ramlResource);

                // In representation

                if (operation.getInputPayLoad() != null) {
                    MimeType ramlInRepresentation = new MimeType();
                    fillInputRepresentation(m, representationSamples, action,
                            operation, ramlInRepresentation);
                }

                // Query parameters

                action.setQueryParameters(new LinkedHashMap<String, org.raml.model.parameter.QueryParameter>());
                for (QueryParameter queryParameter : operation
                        .getQueryParameters()) {
                    org.raml.model.parameter.QueryParameter ramlQueryParameter = new org.raml.model.parameter.QueryParameter();
                    ramlQueryParameter.setDisplayName(queryParameter.getName());
                    // ramlQueryParameter.setType(RamlUtils
                    // .getParamType(queryParameter.getType()));
                    ramlQueryParameter.setDescription(queryParameter
                            .getDescription());
                    ramlQueryParameter.setRequired(queryParameter.isRequired());
                    ramlQueryParameter.setExample(queryParameter.getExample());
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
                action.setResponses(new LinkedHashMap<String, org.raml.model.Response>());
                for (Response response : operation.getResponses()) {
                    org.raml.model.Response ramlResponse = new org.raml.model.Response();
                    ramlResponse.setDescription(response.getDescription());
                    ramlResponse.setBody(new LinkedHashMap<String, MimeType>());
                    ramlOutRepresentation = new MimeType();
                    if (Status.isSuccess(response.getCode())
                            && response.getOutputPayLoad() != null
                            && response.getOutputPayLoad().getType() != null) {
                        if (RamlUtils.isPrimitiveType(response
                                .getOutputPayLoad().getType())) {
                            Property outRepresentationPrimitive = new Property();
                            outRepresentationPrimitive.setName("");
                            outRepresentationPrimitive.setType(response
                                    .getOutputPayLoad().getType());
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
                            ramlOutRepresentation.setSchema(response
                                    .getOutputPayLoad().getType());
                        }
                    }
                    if (response.getOutputPayLoad() != null) {
                        MimeType ramlOutRepresentationWithMediaType;
                        for (String mediaType : operation.getProduces()) {
                            ramlOutRepresentationWithMediaType = new MimeType();
                            ramlOutRepresentationWithMediaType
                                    .setSchema(ramlOutRepresentation
                                            .getSchema());
                            try {
                                ramlOutRepresentationWithMediaType
                                        .setExample(getExampleFromPayLoad(
                                                response.getOutputPayLoad(),
                                                representationSamples,
                                                mediaType));
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING,
                                        "Error when writting sample.", e);
                            }
                            ramlResponse.getBody().put(mediaType,
                                    ramlOutRepresentationWithMediaType);
                        }
                    }
                    action.getResponses().put(
                            Integer.toString(response.getCode()), ramlResponse);
                }

                ramlResource.getActions().put(
                        RamlUtils.getActionType(operation.getMethod()), action);
            }
            paths.add(resource.getResourcePath());

            resources.put(ramlResource.getRelativeUri(), ramlResource);
        }
    }

    private static void fillInputRepresentation(ObjectMapper m,
            Map<String, Map<String, Object>> representationSamples,
            Action action, Operation operation, MimeType ramlInRepresentation) {
        ramlInRepresentation.setType(operation.getInputPayLoad().getType());
        if (RamlUtils.isPrimitiveType(operation.getInputPayLoad().getType())) {
            Property inRepresentationPrimitive = new Property();
            inRepresentationPrimitive.setName("");
            inRepresentationPrimitive.setType(operation.getInputPayLoad()
                    .getType());
            SimpleTypeSchema inRepresentationSchema = RamlUtils
                    .generatePrimitiveSchema(inRepresentationPrimitive);
            try {
                ramlInRepresentation.setSchema(m
                        .writeValueAsString(inRepresentationSchema));
            } catch (JsonProcessingException e) {
                LOGGER.log(Level.WARNING,
                        "Error when setting mime type schema.", e);
            }
        } else {
            ramlInRepresentation.setSchema(operation.getInputPayLoad()
                    .getType());
        }
        action.setBody(new LinkedHashMap<String, MimeType>());
        MimeType ramlInRepresentationWithMediaType;
        for (String mediaType : operation.getConsumes()) {
            ramlInRepresentationWithMediaType = new MimeType();
            ramlInRepresentationWithMediaType.setSchema(ramlInRepresentation
                    .getSchema());
            try {
                ramlInRepresentationWithMediaType
                        .setExample(getExampleFromPayLoad(
                                operation.getInputPayLoad(),
                                representationSamples, mediaType));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error when writting sample.", e);
            }
            action.getBody().put(mediaType, ramlInRepresentationWithMediaType);
        }
    }

    private static List<Map<String, SecurityScheme>> getSecuritySchemes(Endpoint endpoint) {
        ArrayList<Map<String, SecurityScheme>> securitySchemesList = new ArrayList<>();
        Map<String, SecurityScheme> securitySchemes = new LinkedHashMap<>();
        SecurityScheme securityScheme = new SecurityScheme();
        if (endpoint != null) {
            if (ChallengeScheme.HTTP_BASIC.equals(endpoint
                    .getAuthenticationProtocol())) {
                securityScheme.setType(ChallengeScheme.HTTP_BASIC.getName());
                securitySchemes.put(ChallengeScheme.HTTP_BASIC.getName(),
                        securityScheme);
            } else if (ChallengeScheme.HTTP_OAUTH.equals(endpoint
                    .getAuthenticationProtocol())
                    || ChallengeScheme.HTTP_OAUTH_BEARER.equals(endpoint
                            .getAuthenticationProtocol())
                    || ChallengeScheme.HTTP_OAUTH_MAC.equals(endpoint
                            .getAuthenticationProtocol())) {
                securityScheme.setType("Oauth 2.0");
                securitySchemes.put("oauth_2_0", securityScheme);
            } else if (ChallengeScheme.HTTP_DIGEST.equals(endpoint
                    .getAuthenticationProtocol())) {
                securityScheme.setType(ChallengeScheme.HTTP_DIGEST.getName());
                securitySchemes.put(ChallengeScheme.HTTP_DIGEST.getName(),
                        securityScheme);
            } else if (ChallengeScheme.CUSTOM.equals(endpoint
                    .getAuthenticationProtocol())) {
                securityScheme.setType(ChallengeScheme.CUSTOM.getName());
                securitySchemes.put(ChallengeScheme.CUSTOM.getName(),
                        securityScheme);
            }
            if (!securitySchemes.isEmpty()) {
                securitySchemesList.add(securitySchemes);
                return securitySchemesList;
            }
        }
        return null;
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
        List<Resource> rwadResources = new ArrayList<>();

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
                    ConversionUtils.processResourceName(entry.getValue().getUri()),
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
        Definition definition = new Definition();
        if (raml.getVersion() != null) {
            definition.setVersion(raml.getVersion().substring(1));
            // def.setEndpoint(raml.getBaseUri().replace("{version}",
            // raml.getVersion()));
        } else {
            // def.setEndpoint(raml.getBaseUri());
        }
        Contract contract = new Contract();
        definition.setContract(contract);
        contract.setName(raml.getTitle());

        // TODO add section sorting strategies

        // TODO String defaultMediaType = raml.getMediaType();
        List<PathVariable> rootPathVariables = new ArrayList<>();
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

                // TODO set representations's sections
                // representation.getSections().add(section.getName());
                contract.getRepresentations().add(representation);
            }
        }

        // Resources
        for (Entry<String, org.raml.model.Resource> entry : raml.getResources()
                .entrySet()) {
            org.raml.model.Resource resource = entry.getValue();
            contract.getResources().addAll(
                    getResource(
                            ConversionUtils.processResourceName(resource.getUri()),
                            resource, rootPathVariables));
        }

        return definition;
    }

    /**
     * Returns an example in provided media Type of the entity in the given
     * PayLoad.
     * 
     * @param payLoad
     *            The PayLoad.
     * @param representationSamples
     *            The map of samples by representations.
     * @param mediaType
     *            The media type as String.
     * @return An example in provided media Type of the entity in the given
     *         PayLoad.
     */
    private static String getExampleFromPayLoad(PayLoad payLoad,
            Map<String, Map<String, Object>> representationSamples,
            String mediaType) {
        Object sample = (Types.isPrimitiveType(payLoad.getType())) ?
            SampleUtils.getPropertyDefaultExampleValue(payLoad.getType(), "value") :
            representationSamples.get(payLoad.getType());

        if (payLoad.isArray()) {
            sample = Arrays.asList(sample);
        }
        return SampleUtils.convertSampleAccordingToMediaType(sample, mediaType, payLoad.getType());
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private RamlTranslator() {
    }
}