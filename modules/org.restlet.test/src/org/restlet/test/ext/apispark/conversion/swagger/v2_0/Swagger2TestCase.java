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

import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.apispark.conversion.DefinitionComparator;

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
        io.swagger.models.License savedLicense = savedInfo.getLicense();
        io.swagger.models.License translatedLicense = translatedInfo
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
        Map<String, io.swagger.models.properties.Property> savedProperties = savedModel
                .getProperties();
        Map<String, io.swagger.models.properties.Property> translatedProperties = translatedModel
                .getProperties();
        for (Entry<String, io.swagger.models.properties.Property> entry : savedProperties
                .entrySet()) {
            io.swagger.models.properties.Property savedProperty = savedProperties
                    .get(entry.getKey());
            io.swagger.models.properties.Property translatedProperty = translatedProperties
                    .get(entry.getKey());
            assertNotNull(savedProperty);
            if (translatedProperty == null) {
                assertNotNull(translatedProperty);
            }
            compareSwaggerProperties(savedProperty, translatedProperty);
        }
    }

    private void compareSwaggerOperations(
            io.swagger.models.Operation savedOperation,
            io.swagger.models.Operation translatedOperation) {
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
            DefinitionComparator.compareStringLists(savedConsumes, translatedConsumes);

            // Produces
            List<String> savedProduces = savedOperation.getProduces();
            List<String> translatedProduces = translatedOperation.getProduces();
            DefinitionComparator.compareStringLists(savedProduces, translatedProduces);

            // Parameters
            List<io.swagger.models.parameters.Parameter> savedParameters = savedOperation
                    .getParameters();
            List<io.swagger.models.parameters.Parameter> translatedParameters = translatedOperation
                    .getParameters();
            if (savedParameters != null || translatedParameters != null) {
                assertNotNull(savedParameters);
                assertNotNull(translatedParameters);
                for (io.swagger.models.parameters.Parameter savedParameter : savedParameters) {
                    io.swagger.models.parameters.Parameter translatedParameter = getParameterFromList(
                            translatedParameters, savedParameter.getName());
                    assertNotNull(savedParameter);
                    assertNotNull(translatedParameter);
                    compareSwaggerParameters(savedParameter,
                            translatedParameter);
                }
            }

            // Responses
            Map<String, io.swagger.models.Response> savedResponses = savedOperation
                    .getResponses();
            Map<String, io.swagger.models.Response> translatedResponses = translatedOperation
                    .getResponses();
            if (savedResponses != null || translatedResponses != null) {
                assertNotNull(savedResponses);
                assertNotNull(translatedResponses);
                for (Entry<String, io.swagger.models.Response> entry : savedResponses
                        .entrySet()) {
                    io.swagger.models.Response savedResponse = savedResponses
                            .get(entry.getKey());
                    io.swagger.models.Response translatedResponse = translatedResponses
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
            DefinitionComparator.compareStringLists(savedTags, translatedTags);
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
            io.swagger.models.properties.Property savedProperty,
            io.swagger.models.properties.Property translatedProperty) {
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
            io.swagger.models.Response savedResponse,
            io.swagger.models.Response translatedResponse) {
        assertEquals(savedResponse.getDescription(),
                translatedResponse.getDescription());
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

    private io.swagger.models.parameters.Parameter getParameterFromList(
            List<io.swagger.models.parameters.Parameter> list,
            String parameterName) {
        for (io.swagger.models.parameters.Parameter parameter : list) {
            if (parameter.getName().equals(parameterName)) {
                return parameter;
            }
        }
        return null;
    }

}
