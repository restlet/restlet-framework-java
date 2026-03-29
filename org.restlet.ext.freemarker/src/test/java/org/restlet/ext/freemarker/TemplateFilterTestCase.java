/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.freemarker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.resource.Directory;

/**
 * Test case for template filters.
 *
 * @author Thierry Boileau
 */
class TemplateFilterTestCase {

    @Test
    void representationShouldBeUsedAsTemplate() throws Exception {
        Request request = new Request(Method.GET, "/template.txt.fmt");
        Response response = testApplication.handle(request);
        assertEquals("Method=GET/Path=/template.txt.fmt", response.getEntity().getText());
    }

    @Test
    void representationShouldNotBeUsedAsTemplate() throws Exception {
        Request request = new Request(Method.GET, "/notATemplate.txt");
        Response response = testApplication.handle(request);

        assertEquals("Method=${m}/Path=${rp}", response.getEntity().getText());
    }

    @BeforeAll
    static void setUpTestCases() {
        Engine.clearThreadLocalVariables();

        // Create an application filtered with Velocity
        testApplication = new MyFreemakerApplication();
    }

    private static Application testApplication;

    /**
     * Internal class used for test purpose
     *
     * @author Thierry Boileau
     */
    private static class MyFreemakerApplication extends Application {

        MyFreemakerApplication() {
            setContext(new Context());
            getContext().setClientDispatcher(new Client(Protocol.CLAP));
        }

        @Override
        public Restlet createInboundRoot() {
            final Directory directory =
                    new Directory(
                            getContext(),
                            LocalReference.createClapReference(
                                    TemplateFilterTestCase.class.getPackage()));
            return new org.restlet.ext.freemarker.TemplateFilter(getContext(), directory);
        }
    }
}
