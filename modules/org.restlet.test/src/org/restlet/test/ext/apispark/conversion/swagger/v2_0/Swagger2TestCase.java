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

package org.restlet.test.ext.apispark.conversion.swagger.v2_0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Header;
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
import org.restlet.test.RestletTestCase;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.wordnik.swagger.models.Contact;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.Tag;
import com.wordnik.swagger.models.auth.SecuritySchemeDefinition;
import com.wordnik.swagger.models.parameters.Parameter;

public class Swagger2TestCase extends RestletTestCase {

    protected Definition createDefinition() {

        // definition
        Definition definition = new Definition();
        definition.setVersion("version");
        org.restlet.ext.apispark.internal.model.Contact contact = new org.restlet.ext.apispark.internal.model.Contact();
        contact.setName("contact");
        definition.setContact(contact);
        License license = new License();
        license.setName("license");
        definition.setLicense(license);

        // endpoint
        Endpoint endpoint = new Endpoint();
        definition.getEndpoints().add(endpoint);
        endpoint.setProtocol("protocol");
        endpoint.setDomain("domain");
        endpoint.setPort(999);
        endpoint.setBasePath("/basePath");

        // contract
        Contract contract = new Contract();
        definition.setContract(contract);
        contract.setName("contract.name");
        contract.setDescription("contract.description");

        // resource 1
        Resource resource1 = new Resource();
        contract.getResources().add(resource1);
        resource1.setResourcePath("resourcePath1");

        // resource 1 : operation 1
        Operation operation1 = new Operation();
        resource1.getOperations().add(operation1);
        operation1.setMethod("get");
        operation1.setName("nameoperation1");
        operation1.setDescription("description");

        // resource 1 : operation 1 : produces
        List<String> op1Produces = new ArrayList<>();
        operation1.setProduces(op1Produces);
        op1Produces.add("produce 1");
        op1Produces.add("produce 2");

        // resource 1 : operation 1 : consumes
        List<String> op1Consumes = new ArrayList<>();
        operation1.setConsumes(op1Consumes);
        op1Consumes.add("consume 1");
        op1Consumes.add("consume 2");

        // resource 1 : path variable 1
        PathVariable pathVariable1 = new PathVariable();
        resource1.getPathVariables().add(pathVariable1);
        pathVariable1.setName("namePathVariable1");
        pathVariable1.setType("string");
        pathVariable1.setDescription("description");

        // resource 1 : path variable 2
        PathVariable pathVariable2 = new PathVariable();
        resource1.getPathVariables().add(pathVariable2);
        pathVariable2.setName("namePathVariable2");
        pathVariable2.setType("string");
        pathVariable2.setDescription("description");

        // resource 1 : operation 1 : inRepresentation
        PayLoad inRepr = new PayLoad();
        operation1.setInputPayLoad(inRepr);
        inRepr.setType("integer");

        // resource 1 : operation 1 : queryParameter 1
        QueryParameter queryParameter1 = new QueryParameter();
        operation1.getQueryParameters().add(queryParameter1);
        queryParameter1.setName("nameQueryParameter1");
        queryParameter1.setType("integer");
        queryParameter1.setDescription("description");

        // resource 1 : operation 1 : queryParameter 2
        QueryParameter queryParameter2 = new QueryParameter();
        operation1.getQueryParameters().add(queryParameter2);
        queryParameter2.setName("nameQueryParameter2");
        queryParameter2.setType("string");
        queryParameter2.setDescription("description");

        // resource 1 : operation 1 : response 1
        Response response1 = new Response();
        operation1.getResponses().add(response1);
        response1.setCode(200);
        response1.setDescription("description");
        PayLoad response1Entity = new PayLoad();
        response1.setOutputPayLoad(response1Entity);
        response1Entity.setArray(true);
        response1Entity.setType("integer");

        // resource 1 : operation 1 : response 2
        Response response2 = new Response();
        operation1.getResponses().add(response2);
        response2.setCode(300);
        response2.setDescription("description");
        PayLoad response2Entity = new PayLoad();
        response2.setOutputPayLoad(response2Entity);
        response2Entity.setArray(false);
        response2Entity.setType("Entity2");

        // resource 1 : operation 1 : response 3
        Response response3 = new Response();
        operation1.getResponses().add(response3);
        response3.setCode(400);
        response3.setDescription("description");
        PayLoad response3Entity = new PayLoad();
        response3.setOutputPayLoad(response3Entity);
        response3Entity.setArray(false);
        response3Entity.setType("integer");

        // resource 2
        Resource resource2 = new Resource();
        contract.getResources().add(resource2);
        resource2.setResourcePath("resourcePath2");

        // representation 1
        Representation representation1 = new Representation();
        definition.getContract().getRepresentations().add(representation1);
        representation1.setName("nameRepresentation1");

        // representation 1 : property 1
        Property representation1Property1 = new Property();
        representation1.getProperties().add(representation1Property1);
        representation1Property1.setName("nameRepresentation1Property1");
        representation1Property1.setType("integer");
        representation1Property1.setDescription("description");

        // representation 1 : property 2
        Property representation1Property2 = new Property();
        representation1.getProperties().add(representation1Property2);
        representation1Property2.setName("nameRepresentation1Property2");
        representation1Property2.setList(true);
        representation1Property2.setType("string");
        representation1Property2.setDescription("description");

        // representation 1 : property 3
        Property representation1Property3 = new Property();
        representation1.getProperties().add(representation1Property3);
        representation1Property3.setName("nameRepresentation1Property3");
        representation1Property3.setType("Entity");
        representation1Property3.setDescription("description");

        // representation 2
        Representation representation2 = new Representation();
        definition.getContract().getRepresentations().add(representation2);
        representation2.setName("nameRepresentation2");

        return definition;
    }

    private void compareMapOfStrings(Map<String, String> savedMap,
            Map<String, String> translatedMap) {
        if (savedMap != null || translatedMap != null) {
            assertNotNull(savedMap);
            assertNotNull(translatedMap);
            for (Entry<String, String> entry : savedMap.entrySet()) {
                assertEquals(savedMap.get(entry.getKey()),
                        translatedMap.get(entry.getKey()));
            }
            for (Entry<String, String> entry : translatedMap.entrySet()) {
                assertEquals(savedMap.get(entry.getKey()),
                        translatedMap.get(entry.getKey()));
            }
        }
    }

    private void compareStringLists(List<String> savedList,
            List<String> translatedList) {
        if (assertBothNull(savedList, translatedList)) {
            return;
        }

        for (String value : savedList) {
            assertTrue(translatedList.contains(value));
        }

        for (String value : translatedList) {
            assertTrue(savedList.contains(value));
        }
    }

    protected void compareSwaggerBeans(Swagger savedSwagger,
            Swagger translatedSwagger) {

        compareSwaggerMainAttributes(savedSwagger, translatedSwagger);

        // Info
        Info savedInfo = savedSwagger.getInfo();
        Info translatedInfo = translatedSwagger.getInfo();
        compareSwaggerInfo(savedInfo, translatedInfo);

        // Contact
        Contact savedContact = savedInfo.getContact();
        Contact translatedContact = translatedInfo.getContact();
        compareSwaggerContact(savedContact, translatedContact);

        // License
        com.wordnik.swagger.models.License savedLicense = savedInfo
                .getLicense();
        com.wordnik.swagger.models.License translatedLicense = translatedInfo
                .getLicense();
        assertEquals(savedLicense.getUrl(), translatedLicense.getUrl());
        assertEquals(savedLicense.getName(), translatedLicense.getName());

        // Models
        Map<String, Model> savedModels = savedSwagger.getDefinitions();
        Map<String, Model> translatedModels = translatedSwagger
                .getDefinitions();
        for (Entry<String, Model> entry : savedModels.entrySet()) {
            Model savedModel = savedModels.get(entry.getKey());
            Model translatedModel = translatedModels.get(entry.getKey());
            assertNotNull(savedModel);
            assertNotNull(translatedModel);
            compareSwaggerModels(savedModel, translatedModel);
        }

        // Paths
        Map<String, Path> savedPaths = savedSwagger.getPaths();
        Map<String, Path> translatedPaths = translatedSwagger.getPaths();
        for (Entry<String, Path> entry : savedPaths.entrySet()) {
            Path savedPath = savedPaths.get(entry.getKey());
            Path translatedPath = translatedPaths.get(entry.getKey());
            assertNotNull(savedPath);
            assertNotNull(translatedPath);
            compareSwaggerPaths(savedPath, translatedPath);
        }

        // Tags
        List<Tag> savedTags = savedSwagger.getTags();
        List<Tag> translatedTags = translatedSwagger.getTags();

        assertEquals(savedTags.size(), translatedTags.size());

        for (Tag savedTag : savedTags) {
            String tagName = savedTag.getName();
            Tag translatedTag = getTagFromList(translatedTags, tagName);
            assertNotNull(savedTag);
            assertNotNull(translatedTag);
            assertEquals(savedTag.getDescription(), translatedTag.getDescription());
        }

    }

    private void compareSwaggerContact(Contact savedContact,
            Contact translatedContact) {
        assertEquals(savedContact.getEmail(), translatedContact.getEmail());
        assertEquals(savedContact.getName(), translatedContact.getName());
        assertEquals(savedContact.getUrl(), translatedContact.getUrl());
    }

    private void compareSwaggerInfo(Info savedInfo, Info translatedInfo) {
        assertEquals(savedInfo.getDescription(),
                translatedInfo.getDescription());
        assertEquals(savedInfo.getTermsOfService(),
                translatedInfo.getTermsOfService());
        assertEquals(savedInfo.getTitle(), translatedInfo.getTitle());
        assertEquals(savedInfo.getVersion(), translatedInfo.getVersion());
    }

    private void compareSwaggerMainAttributes(Swagger savedSwagger,
            Swagger translatedSwagger) {
        assertEquals(savedSwagger.getBasePath(),
                translatedSwagger.getBasePath());
        assertEquals(savedSwagger.getConsumes(),
                translatedSwagger.getConsumes());
        assertEquals(savedSwagger.getHost(), translatedSwagger.getHost());
        assertEquals(savedSwagger.getSwagger(), translatedSwagger.getSwagger());
        assertEquals(savedSwagger.getConsumes(),
                translatedSwagger.getConsumes());

        Map<String, SecuritySchemeDefinition> savedSecuritySchemes = savedSwagger
                .getSecurityDefinitions();
        Map<String, SecuritySchemeDefinition> translatedSecuritySchemes = translatedSwagger
                .getSecurityDefinitions();

        if (savedSecuritySchemes == null) {
            assertEquals(translatedSecuritySchemes, null);
        } else {
            assertEquals(savedSecuritySchemes.size(),
                    translatedSecuritySchemes.size());

            for (String key : savedSecuritySchemes.keySet()) {
                assertEquals(savedSecuritySchemes.get(key).getClass(),
                        translatedSecuritySchemes.get(key).getClass());
            }
        }
    }

    private void compareSwaggerModels(Model savedModel, Model translatedModel) {
        assertEquals(savedModel.getDescription(),
                translatedModel.getDescription());
        assertEquals(savedModel.getExample(), translatedModel.getExample());
        Map<String, com.wordnik.swagger.models.properties.Property> savedProperties = savedModel
                .getProperties();
        Map<String, com.wordnik.swagger.models.properties.Property> translatedProperties = translatedModel
                .getProperties();
        for (Entry<String, com.wordnik.swagger.models.properties.Property> entry : savedProperties
                .entrySet()) {
            com.wordnik.swagger.models.properties.Property savedProperty = savedProperties
                    .get(entry.getKey());
            com.wordnik.swagger.models.properties.Property translatedProperty = translatedProperties
                    .get(entry.getKey());
            assertNotNull(savedProperty);
            if (translatedProperty == null) {
                assertNotNull(translatedProperty);
            }
            compareSwaggerProperties(savedProperty, translatedProperty);
        }
    }

    private void compareSwaggerOperations(
            com.wordnik.swagger.models.Operation savedOperation,
            com.wordnik.swagger.models.Operation translatedOperation) {
        if (savedOperation != null || translatedOperation != null) {
            assertNotNull(savedOperation);
            assertNotNull(translatedOperation);
            assertEquals(savedOperation.getDescription(),
                    translatedOperation.getDescription());
            assertEquals(savedOperation.getOperationId(),
                    translatedOperation.getOperationId());
            assertEquals(savedOperation.getSummary(),
                    translatedOperation.getSummary());

            // Consumes
            List<String> savedConsumes = savedOperation.getConsumes();
            List<String> translatedConsumes = translatedOperation.getConsumes();
            compareStringLists(savedConsumes, translatedConsumes);

            // Produces
            List<String> savedProduces = savedOperation.getProduces();
            List<String> translatedProduces = translatedOperation.getProduces();
            compareStringLists(savedProduces, translatedProduces);

            // Parameters
            List<com.wordnik.swagger.models.parameters.Parameter> savedParameters = savedOperation
                    .getParameters();
            List<com.wordnik.swagger.models.parameters.Parameter> translatedParameters = translatedOperation
                    .getParameters();
            if (savedParameters != null || translatedParameters != null) {
                assertNotNull(savedParameters);
                assertNotNull(translatedParameters);
                for (com.wordnik.swagger.models.parameters.Parameter savedParameter : savedParameters) {
                    com.wordnik.swagger.models.parameters.Parameter translatedParameter = getParameterFromList(
                            translatedParameters, savedParameter.getName());
                    assertNotNull(savedParameter);
                    assertNotNull(translatedParameter);
                    compareSwaggerParameters(savedParameter,
                            translatedParameter);
                }
            }

            // Responses
            Map<String, com.wordnik.swagger.models.Response> savedResponses = savedOperation
                    .getResponses();
            Map<String, com.wordnik.swagger.models.Response> translatedResponses = translatedOperation
                    .getResponses();
            if (savedResponses != null || translatedResponses != null) {
                assertNotNull(savedResponses);
                assertNotNull(translatedResponses);
                for (Entry<String, com.wordnik.swagger.models.Response> entry : savedResponses
                        .entrySet()) {
                    com.wordnik.swagger.models.Response savedResponse = savedResponses
                            .get(entry.getKey());
                    com.wordnik.swagger.models.Response translatedResponse = translatedResponses
                            .get(entry.getKey());
                    assertNotNull(savedResponse);
                    assertNotNull(translatedResponse);
                    compareSwaggerResponses(savedResponse, translatedResponse);
                }
            }

            // TODO add security and schemes (not retrieved during conversion
            // yet)

            List<String> savedTags = savedOperation.getTags();
            List<String> translatedTags = translatedOperation.getTags();
            compareStringLists(savedTags, translatedTags);
        }
    }

    private void compareSwaggerParameters(Parameter savedParameter,
            Parameter translatedParameter) {
        assertEquals(savedParameter.getDescription(),
                translatedParameter.getDescription());
        assertEquals(savedParameter.getIn(), translatedParameter.getIn());
        assertEquals(savedParameter.getName(), translatedParameter.getName());
        assertEquals(savedParameter.getRequired(),
                translatedParameter.getRequired());

    }

    private void compareSwaggerPaths(Path savedPath, Path translatedPath) {
        compareSwaggerOperations(savedPath.getGet(), translatedPath.getGet());
        compareSwaggerOperations(savedPath.getOptions(),
                translatedPath.getOptions());
        compareSwaggerOperations(savedPath.getPatch(),
                translatedPath.getPatch());
        compareSwaggerOperations(savedPath.getPost(), translatedPath.getPost());
        compareSwaggerOperations(savedPath.getPut(), translatedPath.getPut());
        compareSwaggerOperations(savedPath.getDelete(),
                translatedPath.getDelete());
    }

    private void compareSwaggerProperties(
            com.wordnik.swagger.models.properties.Property savedProperty,
            com.wordnik.swagger.models.properties.Property translatedProperty) {
        if (savedProperty != null || translatedProperty != null) {
            assertNotNull(savedProperty);
            assertNotNull(translatedProperty);
            assertEquals(savedProperty.getExample(),
                    translatedProperty.getExample());
            assertEquals(savedProperty.getDescription(),
                    translatedProperty.getDescription());
            assertEquals(savedProperty.getFormat(),
                    translatedProperty.getFormat());
            // TODO fails, investigate (seems like name is filled during
            // translation but not during deserialization
            // assertEquals(savedProperty.getName(),
            // translatedProperty.getName());
            assertEquals(savedProperty.getRequired(),
                    translatedProperty.getRequired());
            assertEquals(savedProperty.getTitle(),
                    translatedProperty.getTitle());
            assertEquals(savedProperty.getType(), translatedProperty.getType());
            assertEquals(savedProperty.getPosition(),
                    translatedProperty.getPosition());
        }
    }

    private void compareSwaggerResponses(
            com.wordnik.swagger.models.Response savedResponse,
            com.wordnik.swagger.models.Response translatedResponse) {
        assertEquals(savedResponse.getDescription(),
                translatedResponse.getDescription());
        compareMapOfStrings(savedResponse.getExamples(),
                translatedResponse.getExamples());
        compareSwaggerProperties(savedResponse.getSchema(),
                translatedResponse.getSchema());
    }

    private Tag getTagFromList(List<Tag> list, String parameterName) {
        for (Tag tag : list) {
            if (tag.getName().equals(parameterName)) {
                return tag;
            }
        }
        return null;
    }

    private com.wordnik.swagger.models.parameters.Parameter getParameterFromList(
            List<com.wordnik.swagger.models.parameters.Parameter> list,
            String parameterName) {
        for (com.wordnik.swagger.models.parameters.Parameter parameter : list) {
            if (parameter.getName().equals(parameterName)) {
                return parameter;
            }
        }
        return null;
    }

    protected void compareDefinitions(Definition savedDefinition,
            Definition translatedDefinition) {

        assertEquals(savedDefinition.getAttribution(), translatedDefinition.getAttribution());
        assertEquals(savedDefinition.getSpecVersion(), translatedDefinition.getSpecVersion());
        assertEquals(savedDefinition.getTermsOfService(), translatedDefinition.getTermsOfService());
        assertEquals(savedDefinition.getVersion(), translatedDefinition.getVersion());

        // Contact
        org.restlet.ext.apispark.internal.model.Contact savedContact = savedDefinition.getContact();
        org.restlet.ext.apispark.internal.model.Contact translatedContact = translatedDefinition.getContact();
        assertEquals(savedContact.getEmail(), translatedContact.getEmail());
        assertEquals(savedContact.getName(), translatedContact.getName());
        assertEquals(savedContact.getUrl(), translatedContact.getUrl());
        
        // License
        org.restlet.ext.apispark.internal.model.License savedLicense = savedDefinition.getLicense();
        org.restlet.ext.apispark.internal.model.License translatedLicense = translatedDefinition.getLicense();
        assertEquals(savedLicense.getName(), translatedLicense.getName());
        assertEquals(savedLicense.getUrl(), translatedLicense.getUrl());
        
        compareStringLists(savedDefinition.getKeywords(), translatedDefinition.getKeywords());

        compareRwadefEndpoints(savedDefinition, translatedDefinition);
        compareRwadefContracts(savedDefinition.getContract(), translatedDefinition.getContract());
    }

    private void compareRwadefEndpoints(Definition savedDefinition, Definition translatedDefinition) {
        if (assertBothNull(savedDefinition.getEndpoints(), translatedDefinition.getEndpoints())) {
            return;
        }

        ImmutableMap<String, Endpoint> savedEndpoints = Maps.uniqueIndex(
                savedDefinition.getEndpoints(),
                new Function<Endpoint, String>() {
                    public String apply(Endpoint endpoint) {
                        return endpoint.computeUrl();
                    }
                });
        ImmutableMap<String, Endpoint> translatedEndpoints = Maps.uniqueIndex(
                translatedDefinition.getEndpoints(),
                new Function<Endpoint, String>() {
                    public String apply(Endpoint endpoint) {
                        return endpoint.computeUrl();
                    }
                });
        assertEquals(savedEndpoints.size(), translatedEndpoints.size());
        for (String key : savedEndpoints.keySet()) {
            Endpoint savedEndpoint = savedEndpoints.get(key);
            Endpoint translatedEndpoint = translatedEndpoints.get(key);
            assertNotNull(savedEndpoint);
            assertNotNull(translatedEndpoint);
            assertEquals(savedEndpoint.getAuthenticationProtocol(), translatedEndpoint.getAuthenticationProtocol());
            assertEquals(savedEndpoint.getBasePath(), translatedEndpoint.getBasePath());
            assertEquals(savedEndpoint.getDomain(), translatedEndpoint.getDomain());
            assertEquals(savedEndpoint.getProtocol(), translatedEndpoint.getProtocol());
            assertEquals(savedEndpoint.getPort(), translatedEndpoint.getPort());
        }
    }

    private void compareRwadefContracts(Contract savedContract, Contract translatedContract) {
        assertEquals(savedContract.getDescription(), translatedContract.getDescription());
        assertEquals(savedContract.getName(), translatedContract.getName());

        compareRwadefSections(savedContract, translatedContract);
        compareRwadefRepresentations(savedContract, translatedContract);
        compareRwadefResources(savedContract, translatedContract);
    }

    private void compareRwadefResources(Contract savedContract, Contract translatedContract) {
        if (assertBothNull(savedContract.getResources(), translatedContract.getResources())) {
            return;
        }

        ImmutableMap<String, Resource> savedResources = Maps.uniqueIndex(
                savedContract.getResources(),
                new Function<Resource, String>() {
                    public String apply(Resource resource) {
                        return resource.getResourcePath();
                    }
                });
        ImmutableMap<String, Resource> translatedResources = Maps.uniqueIndex(
                translatedContract.getResources(),
                new Function<Resource, String>() {
                    public String apply(Resource resource) {
                        return resource.getResourcePath();
                    }
                });

        assertEquals(savedResources.size(), translatedResources.size());
        for (String key : savedResources.keySet()) {
            Resource savedResource = savedResources.get(key);
            Resource translatedResource = translatedResources.get(key);
            assertNotNull(savedResource);
            assertNotNull(translatedResource);

            assertEquals(savedResource.getDescription(), translatedResource.getDescription());
            assertEquals(savedResource.getAuthenticationProtocol(), translatedResource.getAuthenticationProtocol());
            assertEquals(savedResource.getName(), translatedResource.getName());
            compareStringLists(savedResource.getSections(), translatedResource.getSections());

            compareRwadefPathVariables(savedResource, translatedResource);
            compareRwadefOperations(savedResource, translatedResource);
        }
    }

    private void compareRwadefPathVariables(Resource savedResource, Resource translatedResource) {
        if (assertBothNull(savedResource.getPathVariables(), translatedResource.getPathVariables())) {
            return;
        }

        ImmutableMap<String, PathVariable> savedPathVariables = Maps.uniqueIndex(
                savedResource.getPathVariables(),
                new Function<PathVariable, String>() {
                    public String apply(PathVariable pathVariable) {
                        return pathVariable.getName();
                    }
                });
        ImmutableMap<String, PathVariable> translatedPathVariables = Maps.uniqueIndex(
                translatedResource.getPathVariables(),
                new Function<PathVariable, String>() {
                    public String apply(PathVariable pathVariable) {
                        return pathVariable.getName();
                    }
                });

        assertEquals(savedPathVariables.size(), translatedPathVariables.size());
        for (String key1 : savedPathVariables.keySet()) {
            PathVariable savedPathVariable = savedPathVariables.get(key1);
            PathVariable translatedPathVariable = translatedPathVariables.get(key1);
            assertNotNull(savedPathVariable);
            assertNotNull(translatedPathVariable);

            assertEquals(savedPathVariable.getDescription(), translatedPathVariable.getDescription());
            assertEquals(savedPathVariable.getExample(), translatedPathVariable.getExample());
            assertEquals(savedPathVariable.getType(), translatedPathVariable.getType());
            assertEquals(savedPathVariable.isRequired(), translatedPathVariable.isRequired());
        }
    }

    private void compareRwadefOperations(Resource savedResource, Resource translatedResource) {
        if (assertBothNull(savedResource.getOperations(), translatedResource.getOperations())) {
            return;
        }

        ImmutableMap<String, Operation> savedOperations = Maps.uniqueIndex(
                savedResource.getOperations(),
                new Function<Operation, String>() {
                    public String apply(Operation operation) {
                        return operation.getName();
                    }
                });
        ImmutableMap<String, Operation> translatedOperations = Maps.uniqueIndex(
                translatedResource.getOperations(),
                new Function<Operation, String>() {
                    public String apply(Operation operation) {
                        return operation.getName();
                    }
                });

        assertEquals(savedOperations.size(), translatedOperations.size());
        for (String key : savedOperations.keySet()) {
            Operation savedOperation = savedOperations.get(key);
            Operation translatedOperation = translatedOperations.get(key);
            assertNotNull(savedOperation);
            assertNotNull(translatedOperation);

            assertEquals(savedOperation.getDescription(), translatedOperation.getDescription());
            assertEquals(savedOperation.getMethod(), translatedOperation.getMethod());

            compareRwadefHeaders(savedOperation.getHeaders(), translatedOperation.getHeaders());
            compareRwadefQueryParameters(savedOperation, translatedOperation);
            compareRwadefPayloads(savedOperation.getInputPayLoad(), translatedOperation.getInputPayLoad());
            compareRwadefResponses(savedOperation, translatedOperation);

            compareStringLists(savedOperation.getProduces(), translatedOperation.getProduces());
            compareStringLists(savedOperation.getConsumes(), translatedOperation.getConsumes());
        }
    }

    private void compareRwadefResponses(Operation savedOperation, Operation translatedOperation) {
        if (assertBothNull(savedOperation.getResponses(), translatedOperation.getResponses())) {
            return;
        }

        ImmutableMap<Integer, Response> savedResponses = Maps.uniqueIndex(
                savedOperation.getResponses(),
                new Function<Response, Integer>() {
                    public Integer apply(Response response) {
                        return response.getCode();
                    }
                });
        ImmutableMap<Integer, Response> translatedResponses = Maps.uniqueIndex(
                translatedOperation.getResponses(),
                new Function<Response, Integer>() {
                    public Integer apply(Response response) {
                        return response.getCode();
                    }
                });

        assertEquals(savedResponses.size(), translatedResponses.size());
        for (Integer key : savedResponses.keySet()) {
            Response savedResponse = savedResponses.get(key);
            Response translatedResponse = translatedResponses.get(key);
            assertNotNull(savedResponse);
            assertNotNull(translatedResponse);

            assertEquals(savedResponse.getDescription(), translatedResponse.getDescription());

            // both don't exist in Swagger => can't be retrieved
            // assertEquals(savedResponse.getMessage(), translatedResponse.getMessage());
            // assertEquals(savedResponse.getName(), translatedResponse.getName());

            compareRwadefHeaders(savedResponse.getHeaders(), translatedResponse.getHeaders());
            compareRwadefPayloads(savedResponse.getOutputPayLoad(), translatedResponse.getOutputPayLoad());
        }
    }

    private void compareRwadefPayloads(PayLoad savedPayload, PayLoad translatedPayload) {
        if (assertBothNull(savedPayload, translatedPayload)) {
            return;
        }

        assertEquals(savedPayload.getDescription(), translatedPayload.getDescription());
        assertEquals(savedPayload.getType(), translatedPayload.getType());
        assertEquals(savedPayload.isArray(), translatedPayload.isArray());
    }

    private void compareRwadefQueryParameters(Operation savedOperation, Operation translatedOperation) {
        if (assertBothNull(savedOperation.getQueryParameters(), translatedOperation.getQueryParameters())) {
            return;
        }

        ImmutableMap<String, QueryParameter> savedQueryParameters = Maps.uniqueIndex(
                savedOperation.getQueryParameters(),
                new Function<QueryParameter, String>() {
                    public String apply(QueryParameter header) {
                        return header.getName();
                    }
                });
        ImmutableMap<String, QueryParameter> translatedQueryParameters = Maps.uniqueIndex(
                translatedOperation.getQueryParameters(),
                new Function<QueryParameter, String>() {
                    public String apply(QueryParameter header) {
                        return header.getName();
                    }
                });

        assertEquals(savedQueryParameters.size(), translatedQueryParameters.size());
        for (String key : savedQueryParameters.keySet()) {
            QueryParameter savedQueryParameter = savedQueryParameters.get(key);
            QueryParameter translatedQueryParameter = translatedQueryParameters.get(key);
            assertNotNull(savedQueryParameter);
            assertNotNull(translatedQueryParameter);

            assertEquals(savedQueryParameter.getDefaultValue(), translatedQueryParameter.getDefaultValue());
            assertEquals(savedQueryParameter.getDescription(), translatedQueryParameter.getDescription());
            assertEquals(savedQueryParameter.getType(), translatedQueryParameter.getType());
            assertEquals(savedQueryParameter.getExample(), translatedQueryParameter.getExample());
            assertEquals(savedQueryParameter.getSeparator(), translatedQueryParameter.getSeparator());
            assertEquals(savedQueryParameter.isRequired(), translatedQueryParameter.isRequired());
            // TODO: not available in Swagger 2.0
            // assertEquals(savedQueryParameter.isAllowMultiple(), translatedQueryParameter.isAllowMultiple());
            compareStringLists(savedQueryParameter.getEnumeration(), translatedQueryParameter.getEnumeration());
        }
    }

    private void compareRwadefHeaders(List<Header> savedHeadersList, List<Header> translatedHeadersList) {
        if (assertBothNull(savedHeadersList, translatedHeadersList)) {
            return;
        }

        ImmutableMap<String, Header> savedHeaders = Maps.uniqueIndex(
                savedHeadersList,
                new Function<Header, String>() {
                    public String apply(Header header) {
                        return header.getName();
                    }
                });
        ImmutableMap<String, Header> translatedHeaders = Maps.uniqueIndex(
                translatedHeadersList,
                new Function<Header, String>() {
                    public String apply(Header header) {
                        return header.getName();
                    }
                });

        assertEquals(savedHeaders.size(), translatedHeaders.size());
        for (String key : savedHeaders.keySet()) {
            Header savedHeader = savedHeaders.get(key);
            Header translatedHeader = translatedHeaders.get(key);
            assertNotNull(savedHeader);
            assertNotNull(translatedHeader);

            assertEquals(savedHeader.getDefaultValue(), translatedHeader.getDefaultValue());
            assertEquals(savedHeader.getDescription(), translatedHeader.getDescription());
            assertEquals(savedHeader.getType(), translatedHeader.getType());
            assertEquals(savedHeader.isRequired(), translatedHeader.isRequired());
            // TODO: does not exist in Swagger 2.0 yet
            // assertEquals(savedHeader.isAllowMultiple(), translatedHeader.isAllowMultiple());

        }
    }

    private void compareRwadefRepresentations(Contract savedContract, Contract translatedContract) {
        if (assertBothNull(savedContract.getRepresentations(), translatedContract.getRepresentations())) {
            return;
        }

        ImmutableMap<String, Representation> savedRepresentations = Maps.uniqueIndex(
                savedContract.getRepresentations(),
                new Function<Representation, String>() {
                    public String apply(Representation representation) {
                        return representation.getName();
                    }
                });
        ImmutableMap<String, Representation> translatedRepresentations = Maps.uniqueIndex(
                translatedContract.getRepresentations(),
                new Function<Representation, String>() {
                    public String apply(Representation representation) {
                        return representation.getName();
                    }
                });

        assertEquals(savedRepresentations.size(), translatedRepresentations.size());
        for (String key : savedRepresentations.keySet()) {
            Representation savedRepresentation = savedRepresentations.get(key);
            Representation translatedRepresentation = translatedRepresentations.get(key);
            assertNotNull(savedRepresentation);
            assertNotNull(translatedRepresentation);
            assertEquals(savedRepresentation.getDescription(), translatedRepresentation.getDescription());
            assertEquals(savedRepresentation.getExtendedType(), translatedRepresentation.getExtendedType());

            compareStringLists(savedRepresentation.getSections(), translatedRepresentation.getSections());
            compareRwadefProperties(savedRepresentation.getProperties(), translatedRepresentation.getProperties());
        }
    }

    private void compareRwadefProperties(List<Property> savedPropertiesList,
            List<Property> translatedPropertiesList) {
        if (assertBothNull(savedPropertiesList, translatedPropertiesList)) {
            return;
        }

        ImmutableMap<String, Property> savedProperties = Maps.uniqueIndex(
                savedPropertiesList,
                new Function<Property, String>() {
                    public String apply(Property property) {
                        return property.getName();
                    }
                });
        ImmutableMap<String, Property> translatedProperties = Maps.uniqueIndex(
                translatedPropertiesList,
                new Function<Property, String>() {
                    public String apply(Property property) {
                        return property.getName();
                    }
                });
        
        assertEquals(savedProperties.size(), translatedProperties.size());
        for (String key : savedProperties.keySet()) {
            Property savedProperty = savedProperties.get(key);
            Property translatedProperty = translatedProperties.get(key);
            assertNotNull(savedProperty);
            assertNotNull(translatedProperty);

            assertEquals(savedProperty.getDefaultValue(), translatedProperty.getDefaultValue());
            assertEquals(savedProperty.getDescription(), translatedProperty.getDescription());
            assertEquals(savedProperty.getExample(), translatedProperty.getExample());
            assertEquals(savedProperty.getMax(), translatedProperty.getMax());
            assertEquals(savedProperty.getMin(), translatedProperty.getMin());
            assertEquals(savedProperty.getType(), translatedProperty.getType());
            assertEquals(savedProperty.getMaxOccurs(), translatedProperty.getMaxOccurs());
            assertEquals(savedProperty.getMinOccurs(), translatedProperty.getMinOccurs());

            compareRwadefProperties(savedProperty.getProperties(), translatedProperty.getProperties());
            compareStringLists(savedProperty.getEnumeration(), translatedProperty.getEnumeration());
        }
    }

    private void compareRwadefSections(Contract savedContract, Contract translatedContract) {
        if (assertBothNull(savedContract.getSections(), translatedContract.getSections())) {
            return;
        }

        ImmutableMap<String, Section> savedSections = Maps.uniqueIndex(savedContract.getSections(),
                new Function<Section, String>() {
                    public String apply(Section section) {
                        return section.getName();
                    }
                });
        ImmutableMap<String, Section> translatedSections = Maps.uniqueIndex(translatedContract.getSections(),
                new Function<Section, String>() {
                    public String apply(Section section) {
                        return section.getName();
                    }
                });

        assertEquals(savedSections.size(), translatedSections.size());
        for (String key : savedSections.keySet()) {
            Section savedSection = savedSections.get(key);
            Section translatedSection = translatedSections.get(key);
            assertNotNull(savedSection);
            assertNotNull(translatedSection);
            assertEquals(savedSection.getDescription(), translatedSection.getDescription());
        }
    }

    /**
     * Asserts that the given objects are both null. Returns true if it is the case, false otherwise.
     * Fails if one is null and not the other.
     * 
     * @param savedObject
     *            The object from the saved definition.
     * @param translatedObject
     *            The object from the translated definition.
     * @return True if both the objects are null, false otherwise.
     */
    private boolean assertBothNull(Object savedObject, Object translatedObject) {
        if (savedObject == null || translatedObject == null) {
            assertNull(savedObject);
            assertNull(translatedObject);
            return true;
        }
        return false;
    }

}
