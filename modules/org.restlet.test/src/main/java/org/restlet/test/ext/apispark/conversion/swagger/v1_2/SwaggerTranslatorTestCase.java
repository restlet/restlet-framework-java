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

package org.restlet.test.ext.apispark.conversion.swagger.v1_2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.DefaultConverter;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.SwaggerReader;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ApiDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListing;
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
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.test.RestletTestCase;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the
 * {@link org.restlet.ext.apispark.internal.conversion.swagger.v1_2.SwaggerTranslator}
 * class.
 * 
 * @author Cyprien Quilici
 */
public class SwaggerTranslatorTestCase extends RestletTestCase {

    private void comparePetstoreDefinition(Definition translatedDefinition)
            throws IOException {
        Definition savedDefinition = new JacksonRepresentation<Definition>(
                new FileRepresentation(getClass()
                        .getResource("Petstore.rwadef").getFile(),
                        MediaType.APPLICATION_JSON), Definition.class)
                .getObject();

        // Api Info
        assertEquals(savedDefinition.getContact().getEmail(),
                translatedDefinition.getContact().getEmail());

        Endpoint savedEndpoint = savedDefinition.getEndpoints().get(0);
        Endpoint translatedEndpoint = translatedDefinition.getEndpoints()
                .get(0);

        assertEquals(savedEndpoint.getPort(), translatedEndpoint.getPort());
        assertEquals(savedEndpoint.getProtocol(),
                translatedEndpoint.getProtocol());
        assertEquals(savedEndpoint.getDomain(), translatedEndpoint.getDomain());
        assertEquals(savedEndpoint.getBasePath(),
                translatedEndpoint.getBasePath());

        assertEquals(savedDefinition.getLicense().getUrl(),
                translatedDefinition.getLicense().getUrl());
        assertEquals(savedDefinition.getLicense().getName(),
                translatedDefinition.getLicense().getName());
        assertEquals(savedDefinition.getVersion(),
                translatedDefinition.getVersion());

        // Contract info
        Contract savedContract = savedDefinition.getContract();
        Contract translatedContract = translatedDefinition.getContract();
        assertEquals(savedContract.getDescription(),
                translatedContract.getDescription());
        assertEquals(savedContract.getName(), translatedContract.getName());

        // Representations
        Representation savedRepresentation;
        for (Representation translatedRepresentation : translatedDefinition
                .getContract().getRepresentations()) {
            savedRepresentation = savedDefinition.getContract()
                    .getRepresentation(translatedRepresentation.getName());
            assertEquals(true, savedRepresentation != null);

            if (savedRepresentation != null) {
                assertEquals(savedRepresentation.getDescription(),
                        translatedRepresentation.getDescription());
                assertEquals(savedRepresentation.getName(),
                        translatedRepresentation.getName());
                assertEquals(savedRepresentation.getExtendedType(),
                        translatedRepresentation.getExtendedType());
                assertEquals(savedRepresentation.isRaw(),
                        translatedRepresentation.isRaw());

                // Properties
                Property savedProperty;
                for (Property translatedProperty : translatedRepresentation
                        .getProperties()) {
                    savedProperty = savedRepresentation
                            .getProperty(translatedProperty.getName());
                    assertEquals(true, savedProperty != null);

                    if (savedProperty != null) {
                        assertEquals(savedProperty.getDefaultValue(),
                                translatedProperty.getDefaultValue());
                        assertEquals(savedProperty.getDescription(),
                                translatedProperty.getDescription());
                        assertEquals(savedProperty.getMax(),
                                translatedProperty.getMax());
                        assertEquals(savedProperty.getMaxOccurs(),
                                translatedProperty.getMaxOccurs());
                        assertEquals(savedProperty.getMin(),
                                translatedProperty.getMin());
                        assertEquals(savedProperty.getMinOccurs(),
                                translatedProperty.getMinOccurs());
                        assertEquals(savedProperty.getName(),
                                translatedProperty.getName());
                        assertEquals(savedProperty.getEnumeration(),
                                translatedProperty.getEnumeration());
                        assertEquals(savedProperty.isUniqueItems(),
                                translatedProperty.isUniqueItems());
                        assertEquals(savedProperty.getType(),
                                translatedProperty.getType());
                    }
                }
            }

        }

        // Resources
        Resource savedResource;
        for (Resource translatedResource : translatedDefinition.getContract()
                .getResources()) {
            savedResource = savedDefinition.getContract().getResource(
                    translatedResource.getResourcePath());
            assertEquals(true, savedResource != null);

            if (savedResource != null) {
                assertEquals(translatedResource.getDescription(),
                        savedResource.getDescription());
                assertEquals(translatedResource.getName(),
                        savedResource.getName());
                assertEquals(translatedResource.getResourcePath(),
                        savedResource.getResourcePath());

                // Path Variables
                PathVariable savedPathVariable;
                for (PathVariable translatedPathVariable : translatedResource
                        .getPathVariables()) {
                    savedPathVariable = savedResource
                            .getPathVariable(translatedPathVariable.getName());
                    assertEquals(true, savedPathVariable != null);

                    if (savedPathVariable != null) {
                        assertEquals(savedPathVariable.getName(),
                                translatedPathVariable.getName());
                        assertEquals(savedPathVariable.getDescription(),
                                translatedPathVariable.getDescription());
                    }
                }

                // Operations
                Operation savedOperation;
                for (Operation translatedOperation : translatedResource
                        .getOperations()) {
                    savedOperation = savedResource
                            .getOperation(translatedOperation.getName());
                    assertEquals(true, savedOperation != null);

                    if (savedOperation != null) {
                        assertEquals(savedOperation.getDescription(),
                                translatedOperation.getDescription());
                        assertEquals(savedOperation.getMethod(),
                                translatedOperation.getMethod());
                        assertEquals(savedOperation.getName(),
                                translatedOperation.getName());
                        assertEquals(savedOperation.getConsumes(),
                                translatedOperation.getConsumes());
                        assertEquals(savedOperation.getProduces(),
                                translatedOperation.getProduces());

                        // In representation
                        PayLoad savedInRepresentation = savedOperation
                                .getInputPayLoad();
                        PayLoad translatedInRepresentation = translatedOperation
                                .getInputPayLoad();
                        assertEquals(
                                true,
                                (savedInRepresentation == null) == (translatedInRepresentation == null));

                        if (translatedInRepresentation != null) {
                            assertEquals(savedInRepresentation.isArray(),
                                    translatedInRepresentation.isArray());
                            assertEquals(savedInRepresentation.getType(),
                                    translatedInRepresentation.getType());
                        }

                        // Out representation
                        PayLoad savedOutRepresentation = null;
                        if (savedOperation.getResponse(200) != null) {
                            savedOutRepresentation = savedOperation
                                    .getResponse(200).getOutputPayLoad();
                        }
                        PayLoad translatedOutRepresentation = null;
                        if (translatedOperation.getResponse(200) != null) {
                            translatedOutRepresentation = translatedOperation
                                    .getResponse(200).getOutputPayLoad();
                        }
                        assertEquals(
                                true,
                                (savedOutRepresentation == null) == (translatedOutRepresentation == null));

                        if (translatedOutRepresentation != null) {
                            assertEquals(savedOutRepresentation.isArray(),
                                    translatedOutRepresentation.isArray());
                            assertEquals(savedOutRepresentation.getType(),
                                    translatedOutRepresentation.getType());
                        }

                        // Responses
                        Response savedResponse;
                        for (Response translatedResponse : translatedOperation
                                .getResponses()) {
                            savedResponse = savedOperation
                                    .getResponse(translatedResponse.getCode());
                            assertEquals(true, savedResponse != null);

                            if (savedResponse != null) {
                                assertEquals(savedResponse.getCode(),
                                        translatedResponse.getCode());
                                assertEquals(savedResponse.getDescription(),
                                        translatedResponse.getDescription());
                                assertEquals(savedResponse.getMessage(),
                                        translatedResponse.getMessage());
                                assertEquals(savedResponse.getName(),
                                        translatedResponse.getName());

                                // Body
                                PayLoad savedResponseBody = savedResponse
                                        .getOutputPayLoad();
                                PayLoad translatedResponseBody = translatedResponse
                                        .getOutputPayLoad();
                                assertEquals(
                                        true,
                                        (savedResponseBody == null) == (translatedResponseBody == null));

                                if (translatedResponseBody != null) {
                                    assertEquals(savedResponseBody.isArray(),
                                            translatedResponseBody.isArray());
                                    assertEquals(savedResponseBody.getType(),
                                            translatedResponseBody.getType());
                                }
                            }

                        }

                        // Query Parameters
                        QueryParameter savedQueryParameter;
                        for (QueryParameter translatedQueryParameter : translatedOperation
                                .getQueryParameters()) {
                            savedQueryParameter = savedOperation
                                    .getQueryParameter(translatedQueryParameter
                                            .getName());
                            assertEquals(true, savedQueryParameter != null);

                            if (savedQueryParameter != null) {
                                assertEquals(
                                        savedQueryParameter.isAllowMultiple(),
                                        translatedQueryParameter
                                                .isAllowMultiple());
                                assertEquals(
                                        savedQueryParameter.getDefaultValue(),
                                        translatedQueryParameter
                                                .getDefaultValue());
                                assertEquals(
                                        savedQueryParameter.getDescription(),
                                        translatedQueryParameter
                                                .getDescription());
                                assertEquals(savedQueryParameter.getName(),
                                        translatedQueryParameter.getName());
                                assertEquals(savedQueryParameter.isRequired(),
                                        translatedQueryParameter.isRequired());
                                assertEquals(
                                        savedQueryParameter.getEnumeration(),
                                        translatedQueryParameter
                                                .getEnumeration());
                                assertEquals(savedQueryParameter.isRequired(),
                                        translatedQueryParameter.isRequired());
                            }
                        }
                    }
                }
            }
        }
    }

    protected void setUpEngine() {
        super.setUpEngine();
        // we control the available converters.
        Engine.getInstance().getRegisteredConverters().clear();
        Engine.getInstance().getRegisteredConverters()
                .add(new JacksonConverter());
        Engine.getInstance().getRegisteredConverters()
                .add(new DefaultConverter());
    }

    public void testPetstoreSwaggerJsonToRwadef() throws TranslationException,
            IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ResourceListing resourceListing = objectMapper.readValue(getClass()
                .getResource("api-docs.json"), ResourceListing.class);
        ApiDeclaration petApiDeclaration = objectMapper.readValue(getClass()
                .getResource("pet.json"), ApiDeclaration.class);
        ApiDeclaration storeApiDeclaration = objectMapper.readValue(getClass()
                .getResource("store.json"), ApiDeclaration.class);
        ApiDeclaration userApiDeclaration = objectMapper.readValue(getClass()
                .getResource("user.json"), ApiDeclaration.class);
        Map<String, ApiDeclaration> apiDeclarations = new HashMap<String, ApiDeclaration>();
        apiDeclarations.put("/pet", petApiDeclaration);
        apiDeclarations.put("/store", storeApiDeclaration);
        apiDeclarations.put("/user", userApiDeclaration);
        Definition translatedDefinition = SwaggerReader.translate(
                resourceListing, apiDeclarations);
        comparePetstoreDefinition(translatedDefinition);
    }
}