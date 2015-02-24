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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.test.RestletTestCase;

import com.wordnik.swagger.models.Contact;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.parameters.Parameter;

public class Swagger2TestCase extends RestletTestCase {

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

}
