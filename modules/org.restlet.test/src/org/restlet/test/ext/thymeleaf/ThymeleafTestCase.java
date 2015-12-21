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

package org.restlet.test.ext.thymeleaf;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.data.MediaType;
import org.restlet.ext.thymeleaf.TemplateRepresentation;
import org.restlet.test.RestletTestCase;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Unit test for the Thymeleaf extension.
 * 
 * @author Thierry Boileau
 */
public class ThymeleafTestCase extends RestletTestCase {

    public void testTemplate() throws Exception {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("org/restlet/test/ext/thymeleaf/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        final Map<String, Object> map = new TreeMap<String, Object>();
        map.put("welcome", "Hello, world");

        final String result = new TemplateRepresentation("test",
                TemplateRepresentation.createTemplateEngine(templateResolver),
                Locale.getDefault(), map, MediaType.TEXT_PLAIN).getText();
        assertTrue(result.contains("Hello, world"));
    }

}
