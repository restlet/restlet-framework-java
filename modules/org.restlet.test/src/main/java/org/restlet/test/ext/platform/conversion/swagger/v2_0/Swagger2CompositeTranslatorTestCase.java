/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.test.ext.platform.conversion.swagger.v2_0;

import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.properties.RefProperty;
import org.restlet.ext.platform.internal.conversion.swagger.v2_0.Swagger2Writer;
import org.restlet.ext.platform.internal.introspection.util.Types;
import org.restlet.ext.platform.internal.model.Definition;
import org.restlet.ext.platform.internal.model.Property;
import org.restlet.ext.platform.internal.model.Representation;

import java.io.IOException;
import java.net.URL;

public class Swagger2CompositeTranslatorTestCase extends Swagger2TestCase {

    /**
     * Conversion Rwadef -> Swagger 2.0.
     */
    public void testGetSwagger1() {
        //prepare
        Definition definition = createDefinition();

        //add a new composite property to representation 'nameRepresentation1'
        Representation rep = definition.getContract().getRepresentation("nameRepresentation1");
        Property compositeProperty = new Property();
        compositeProperty.setName("myCompositeType");
        compositeProperty.setDescription("description");
        compositeProperty.setType(Types.compositeType);
        rep.getProperties().add(compositeProperty);

        Property nameProperty = new Property();
        nameProperty.setName("name");
        nameProperty.setDescription("description");
        nameProperty.setType("string");

        compositeProperty.getProperties().add(nameProperty);

        //execute
        Swagger swagger = Swagger2Writer.getSwagger(definition);

        //verify
        Model model1 = swagger.getDefinitions().get("nameRepresentation1");
        assertNotNull(model1);
        RefProperty swaggerProperty = (RefProperty) model1.getProperties().get("myCompositeType");
        assertNotNull(swaggerProperty);
        assertEquals("#/definitions/nameRepresentation1MyCompositeType", swaggerProperty.get$ref());

        Model model2 = swagger.getDefinitions().get("nameRepresentation1MyCompositeType");
        assertNotNull(model2);
        assertEquals(1, model2.getProperties().size());
        assertNotNull(model2.getProperties().get("name"));
    }

    public void testGetSwagger2() throws IOException {
        Definition savedDefinition = parseDefinition(getClass().getResource("refImpl.composite.rwadef"));

        Swagger translatedSwagger = Swagger2Writer.getSwagger(savedDefinition);

        URL refImpl = getClass().getResource("refImpl.composite.swagger");
        Swagger savedSwagger = SwaggerLoader.readJson(refImpl);

        compareSwaggerBeans(savedSwagger, translatedSwagger);
    }
}
