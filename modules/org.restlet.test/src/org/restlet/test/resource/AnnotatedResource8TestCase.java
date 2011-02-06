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

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource8TestCase extends TestCase {

    private ClientResource clientResource;

    protected void setUp() throws Exception {
        Finder finder = new Finder();
        finder.setTargetClass(MyResource8.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.setNext(finder);
    }

    @Override
    protected void tearDown() throws Exception {
        clientResource = null;
        super.tearDown();
    }

    public void testGet() throws IOException, ResourceException {
        Representation input = new StringRepresentation("<root/>",
                MediaType.APPLICATION_XML);
        Representation result = clientResource.post(input,
                MediaType.APPLICATION_XML);
        assertNotNull(result);
        assertEquals("<root/>1", result.getText());
        assertEquals(MediaType.APPLICATION_XML, result.getMediaType());

        input = new StringRepresentation("<root/>", MediaType.APPLICATION_XML);
        result = clientResource.post(input, MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("<root/>2", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

        input = new StringRepresentation("root=true",
                MediaType.APPLICATION_WWW_FORM);
        result = clientResource.post(input, MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("root=true3", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

        Form inputForm = new Form();
        inputForm.add("root", "true");
        result = clientResource.post(inputForm, MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("root=true3", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

        input = new StringRepresentation("[root]", MediaType.APPLICATION_JSON);
        result = clientResource.post(input, MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("[root]2", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

    }

}
