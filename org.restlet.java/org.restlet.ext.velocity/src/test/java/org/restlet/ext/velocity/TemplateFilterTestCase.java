/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.velocity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.restlet.*;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.resource.Directory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for template filters.
 *
 * @author Thierry Boileau
 */
public class TemplateFilterTestCase {

    @Test
    public void representationShouldBeUsedAsTemplate() throws Exception {
        Request request = new Request(Method.GET,"/template.txt.vm");
        Response response = testApplication.handle(request);
        assertEquals("Method=GET/Path=/template.txt.vm", response.getEntity().getText());
    }

    @Test
    public void representationShouldNotBeUsedAsTemplate() throws Exception {
        Request request = new Request(Method.GET, "/notATemplate.txt");
        Response response = testApplication.handle(request);

        assertEquals("Method=${m}/Path=${rp}", response.getEntity().getText());
    }

    /**
     * Internal class used for test purpose
     *
     * @author Thierry Boileau
     */
    private static class MyVelocityApplication extends Application {

        MyVelocityApplication() {
            setContext(new Context());
            getContext().setClientDispatcher(new Client(Protocol.CLAP));
        }

        @Override
        public Restlet createInboundRoot() {
            final Directory directory = new Directory(getContext(), LocalReference.createClapReference(TemplateFilterTestCase.class.getPackage()));

            // Create a Directory that manages a local directory
            return new TemplateFilter(getContext(), directory);
        }
    }

    @BeforeAll
    static void setUpTestCases() {
        Engine.clearThreadLocalVariables();

        // Create an application filtered with Velocity
        testApplication = new MyVelocityApplication();
    }

    private static Application testApplication;
}
