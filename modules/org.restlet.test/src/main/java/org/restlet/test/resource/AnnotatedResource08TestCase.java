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
import org.restlet.representation.Representation;
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
public class AnnotatedResource08TestCase extends RestletTestCase {

    private ClientResource clientResource;

    protected void setUp() throws Exception {
        super.setUp();
        Finder finder = new Finder();
        finder.setTargetClass(MyResource08.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.setNext(finder);
    }

    @Override
    protected void tearDown() throws Exception {
        clientResource = null;
        super.tearDown();
    }

    public void testPost() throws IOException, ResourceException {
        Representation input = new StringRepresentation("root",
                MediaType.APPLICATION_XML);
        Representation result = clientResource.post(input,
                MediaType.APPLICATION_XML);
        assertNotNull(result);
        assertEquals("root1", result.getText());
        assertEquals(MediaType.APPLICATION_XML, result.getMediaType());

        input = new StringRepresentation("root", MediaType.APPLICATION_XML);
        result = clientResource.post(input, MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("root1", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

        input = new StringRepresentation("root", MediaType.APPLICATION_JSON);
        result = clientResource.post(input, MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("root1", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

        input = new StringRepresentation("root", MediaType.APPLICATION_JSON);
        result = clientResource.post(input, MediaType.APPLICATION_XML);
        assertNotNull(result);
        assertEquals("root1", result.getText());
        assertEquals(MediaType.APPLICATION_XML, result.getMediaType());

        input = new StringRepresentation("root", MediaType.APPLICATION_WWW_FORM);
        result = clientResource.post(input, MediaType.APPLICATION_WWW_FORM);
        assertNotNull(result);
        assertEquals("root2", result.getText());
        assertEquals(MediaType.APPLICATION_WWW_FORM, result.getMediaType());

        input = new StringRepresentation("root", MediaType.APPLICATION_WWW_FORM);
        result = clientResource.post(input, MediaType.TEXT_HTML);
        assertNotNull(result);
        assertEquals("root2", result.getText());
        assertEquals(MediaType.TEXT_HTML, result.getMediaType());
    }

}
