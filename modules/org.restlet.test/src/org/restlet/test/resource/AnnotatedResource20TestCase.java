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

package org.restlet.test.resource;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource20TestCase extends RestletTestCase {

    private ClientResource clientResource;

    private MyResource20 myResource;

    protected void setUp() throws Exception {
        super.setUp();
        Engine.getInstance().getRegisteredConverters().clear();
        Engine.getInstance().getRegisteredConverters()
                .add(new JacksonConverter());
        Engine.getInstance().registerDefaultConverters();

        // Hosts resources into an Application because we need some services for
        // handling content negotiation, conversion of exceptions, etc.
        Application application = new Application();
        application.setInboundRoot(MyServerResource20.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.accept(MediaType.APPLICATION_JSON);
        this.clientResource.setNext(application);
        this.myResource = clientResource.wrap(MyResource20.class);
    }

    @Override
    protected void tearDown() throws Exception {
        clientResource = null;
        myResource = null;
        super.tearDown();
    }

    public void testGet() throws IOException, ResourceException {
        try {
            myResource.represent();
            fail("Should fail");
        } catch (MyException01 e) {
            assertEquals(400, clientResource.getStatus().getCode());
        }
    }

    public void testGetAndSerializeException() throws IOException,
            ResourceException {
        try {
            myResource.representAndSerializeException();
            fail("Should fail");
        } catch (MyException02 e) {
            assertEquals(400, clientResource.getStatus().getCode());
            assertEquals("my custom error", e.getCustomProperty());
        }
    }

    public void testClientExceptions() {
        ClientResource cr = new ClientResource("http://test");
        cr.setNext(new Finder(Context.getCurrent(), MyServerResource20.class));
        MyResource20 ai = cr.wrap(MyResource20.class);

        try {
            ai.represent();
        } catch (MyException01 e) {
            e.printStackTrace();
        }

        try {
            ai.representAndSerializeException();
        } catch (MyException02 e) {
            e.printStackTrace();
        }

    }

}
