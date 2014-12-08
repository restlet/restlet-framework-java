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

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource01TestCase extends RestletTestCase {

    private ClientResource clientResource;

    private MyResource01 myResource;

    protected void setUp() throws Exception {
        super.setUp();
        Engine.getInstance().getRegisteredConverters().clear();
        Engine.getInstance().getRegisteredConverters()
                .add(new JacksonConverter());
        Engine.getInstance().registerDefaultConverters();
        Finder finder = new Finder();
        finder.setTargetClass(MyServerResource01.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.setNext(finder);
        this.myResource = clientResource.wrap(MyResource01.class);
    }

    @Override
    protected void tearDown() throws Exception {
        clientResource = null;
        myResource = null;
        super.tearDown();
    }

    public void testDelete() {
        assertEquals("Done", myResource.remove());
    }

    public void testGet() throws IOException, ResourceException {
        MyBean myBean = myResource.represent();
        assertNotNull(myBean);
        assertEquals("myName", myBean.getName());
        assertEquals("myDescription", myBean.getDescription());

        String result = clientResource.get(MediaType.TEXT_XML).getText();
        assertEquals(
                "<MyBean><description>myDescription</description><name>myName</name></MyBean>",
                result);

        result = clientResource.get(MediaType.APPLICATION_XML).getText();
        assertEquals(
                "<MyBean><description>myDescription</description><name>myName</name></MyBean>",
                result);

        result = clientResource.get(MediaType.APPLICATION_ALL_XML).getText();
        assertEquals(
                "<MyBean><description>myDescription</description><name>myName</name></MyBean>",
                result);

        result = clientResource.get(MediaType.APPLICATION_JSON).getText();
        assertEquals("{\"description\":\"myDescription\",\"name\":\"myName\"}",
                result);

        ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED = true;
        result = clientResource.get(MediaType.APPLICATION_JAVA_OBJECT_XML)
                .getText();
        assertTrue(result
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                && result.contains("<java version=\""));
        ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED = false;
    }

    public void testOptions() {
        assertEquals("MyDescription", myResource.describe());
    }

    public void testPost() {
        MyBean myBean = new MyBean("myName", "myDescription");
        assertTrue(myResource.accept(myBean));
    }

    public void testPut() throws ResourceException {
        // Get current representation
        MyBean myBean = myResource.represent();
        assertNotNull(myBean);

        // Put new representation
        MyBean newBean = new MyBean("newName", "newDescription");
        String result = myResource.store(newBean);
        assertEquals("Done", result);

        // Attempt to send an unknown entity
        try {
            clientResource.put(new StringRepresentation("wxyz",
                    MediaType.APPLICATION_GNU_ZIP));
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
                    re.getStatus());
        }
    }

}
