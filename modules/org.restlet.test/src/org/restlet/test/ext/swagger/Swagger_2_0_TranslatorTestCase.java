package org.restlet.test.ext.swagger;

import org.restlet.ext.apispark.internal.conversion.swagger_2_0.RwadefToSwagger_2_0_Translator;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.test.RestletTestCase;

import com.wordnik.swagger.models.Swagger;

public class Swagger_2_0_TranslatorTestCase extends RestletTestCase {

	private RwadefToSwagger_2_0_Translator rwadefToSwagger_2_0_Translator = new RwadefToSwagger_2_0_Translator();

	/**
	 * Conversion Rwadef -> Swagger 2.0.
	 */
	public void testGetSwagger() {
		// Given
		Definition definition = new Definition();

		// When
		Swagger swagger = rwadefToSwagger_2_0_Translator.getSwaggerFromRoadef(definition);

		// Then
		assertEquals(Float.valueOf("2.0f"), swagger.getSwagger());
	}

}
