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

import java.io.IOException;
import java.net.URL;

import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.conversion.swagger.v2_0.Swagger2Translator;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.FileRepresentation;

import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.util.SwaggerLoader;

public class Swagger2CompositeTranslatorTestCase extends Swagger2TestCase {

    public void testGetSwagger2() throws IOException {
        Definition savedDefinition = new JacksonRepresentation<>(
                new FileRepresentation(getClass().getResource(
                        "refImpl.composite.rwadef").getFile(),
                        MediaType.APPLICATION_JSON), Definition.class)
                .getObject();

        Swagger translatedSwagger = Swagger2Translator
                .getSwagger(savedDefinition);

        URL refImpl = getClass().getResource("refImpl.composite.swagger");
        Swagger savedSwagger = new SwaggerLoader().read(refImpl.getFile());

        compareSwaggerBeans(savedSwagger, translatedSwagger);
        compareSwaggerBeans(translatedSwagger, savedSwagger);
    }
}
