/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.resource;

import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource1TestCase extends TestCase {

    private ClientResource clientResource;

    private MyResource1 myResource;

    protected void setUp() throws Exception {
        Finder finder = new Finder();
        finder.setTargetClass(MyServerResource1.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.setNext(finder);
        this.myResource = clientResource.wrap(MyResource1.class);
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
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<org.restlet.test.resource.MyBean>\n  <name>myName</name>\n  <description>myDescription</description>\n</org.restlet.test.resource.MyBean>",
                result);

        result = clientResource.get(MediaType.APPLICATION_XML).getText();
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<org.restlet.test.resource.MyBean>\n  <name>myName</name>\n  <description>myDescription</description>\n</org.restlet.test.resource.MyBean>",
                result);

        result = clientResource.get(MediaType.APPLICATION_ALL_XML).getText();
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<org.restlet.test.resource.MyBean>\n  <name>myName</name>\n  <description>myDescription</description>\n</org.restlet.test.resource.MyBean>",
                result);

        result = clientResource.get(MediaType.APPLICATION_JSON).getText();
        assertEquals("{\"name\":\"myName\",\"description\":\"myDescription\"}",
                result);

        result = clientResource.get(MediaType.APPLICATION_JAVA_OBJECT_XML)
                .getText();
        assertTrue(result
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n<java version=\""));
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
            assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, re.getStatus());
        }
    }

}
