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

package org.restlet.test.ext.swagger;

import java.io.File;
import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.conversion.SwaggerTranslater;
import org.restlet.ext.apispark.internal.conversion.SwaggerUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.model.Body;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Unit test for the {@link SwaggerTranslater} class.
 * 
 * @author Cyprien Quilici
 */
public class SwaggerTranslaterTestCase extends RestletTestCase {

	/**
	 * Retrieves the Petstore from {@linkplain http
	 * ://petstore.swagger.wordnik.com/api/api-docs}, and translates it to
	 * RWADef using SwaggerTranslater.
	 * 
	 * @throws TranslationException
	 * @throws IOException
	 */
	public void testPetstoreSwaggerToRwadef() throws TranslationException, IOException {
		Definition savedDefinition = new JacksonRepresentation<Definition>(
				new FileRepresentation(new File(
						"src/org/restlet/test/ext/raml/Petstore.rwadef"),
						MediaType.APPLICATION_JSON), Definition.class)
				.getObject();
		Definition translatedDefinition = SwaggerUtils.getDefinition(
				"http://petstore.swagger.wordnik.com/api/api-docs", "", "");

		// Api Info
		assertEquals(savedDefinition.getContact(),
				translatedDefinition.getContact());
		assertEquals(savedDefinition.getEndpoint(),
				translatedDefinition.getEndpoint());
		assertEquals(savedDefinition.getLicense(),
				translatedDefinition.getLicense());
		assertEquals(savedDefinition.getVersion(),
				translatedDefinition.getVersion());

		// Contract info
		assertEquals(savedDefinition.getContract().getDescription(),
				translatedDefinition.getContract().getDescription());
		assertEquals(savedDefinition.getContract().getName(),
				translatedDefinition.getContract().getName());

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
				 assertEquals(savedRepresentation.getParentType(),
				 translatedRepresentation.getParentType());
				 assertEquals(savedRepresentation.isRaw(),
				 translatedRepresentation.isRaw());
				 
				 // Properties
				Property savedProperty;
				for (Property translatedProperty: translatedRepresentation.getProperties()) {
					savedProperty = savedRepresentation.getProperty(translatedProperty.getName());
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
						 assertEquals(savedProperty.getPossibleValues(),
						 translatedProperty.getPossibleValues());
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
						assertEquals(savedPathVariable.isArray(),
								translatedPathVariable.isArray());
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
						Body savedInRepresentation = savedOperation
								.getInRepresentation();
						Body translatedInRepresentation = translatedOperation
								.getInRepresentation();
						assertEquals(
								true,
								(savedInRepresentation == null) == (translatedInRepresentation == null));

						if (translatedInRepresentation != null) {
							assertEquals(savedInRepresentation.isArray(),
									translatedInRepresentation.isArray());
							assertEquals(
									savedInRepresentation.getRepresentation(),
									translatedInRepresentation
											.getRepresentation());
						}

						// Out representation
						Body savedOutRepresentation = savedOperation
								.getOutRepresentation();
						Body translatedOutRepresentation = translatedOperation
								.getOutRepresentation();
						assertEquals(
								true,
								(savedOutRepresentation == null) == (translatedOutRepresentation == null));

						if (translatedOutRepresentation != null) {
							assertEquals(savedOutRepresentation.isArray(),
									translatedOutRepresentation.isArray());
							assertEquals(
									savedOutRepresentation.getRepresentation(),
									translatedOutRepresentation
											.getRepresentation());
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
								Body savedResponseBody = savedResponse
										.getBody();
								Body translatedResponseBody = translatedResponse
										.getBody();
								assertEquals(
										true,
										(savedResponseBody == null) == (translatedResponseBody == null));

								if (translatedResponseBody != null) {
									assertEquals(savedResponseBody.isArray(),
											translatedResponseBody.isArray());
									assertEquals(
											savedResponseBody
													.getRepresentation(),
											translatedResponseBody
													.getRepresentation());
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
								assertEquals(savedQueryParameter
										.getPossibleValues(),
										translatedQueryParameter
												.getPossibleValues());
								assertEquals(savedQueryParameter.isRequired(),
										translatedQueryParameter.isRequired());
							}
						}
					}
				}
			}
		}
	}
}
