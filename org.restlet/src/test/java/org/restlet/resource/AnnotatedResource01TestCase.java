/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.StringRepresentation;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource01TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    private MyResource01 myResource;

    @Override
    protected void configureClientResource(ClientResource clientResource) {
        super.configureClientResource(clientResource);
        this.myResource = clientResource.wrap(MyResource01.class);
    }

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyServerResource01.class);
    }

    @Test
    void testDelete() {
        assertEquals("Done", myResource.remove());
    }

    @Test
    void testGet() throws IOException, ResourceException {
        MyBean myBean = myResource.represent();
        assertNotNull(myBean);
        assertEquals("myName", myBean.getName());
        assertEquals("myDescription", myBean.getDescription());

        ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED = true;
        String result = clientResource.get(MediaType.APPLICATION_JAVA_OBJECT_XML).getText();
        assertTrue(
                result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                        && result.contains("<java version=\""));
        ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED = false;
    }

    @Test
    void testOptions() {
        assertEquals("MyDescription", myResource.describe());
    }

    @Test
    void testPost() {
        MyBean myBean = new MyBean("myName", "myDescription");
        assertTrue(myResource.accept(myBean));
    }

    @Test
    void testPut() throws ResourceException {
        // Get current representation
        MyBean myBean = myResource.represent();
        assertNotNull(myBean);

        // Put new representation
        MyBean newBean = new MyBean("newName", "newDescription");
        String result = myResource.store(newBean);
        assertEquals("Done", result);

        // Attempt to send an unknown entity
        try {
            clientResource.put(new StringRepresentation("wxyz", MediaType.APPLICATION_GNU_ZIP));
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, re.getStatus());
        }
    }
}
