/**
 * Copyright 2005-2024 Qlik
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
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource04TestCase extends RestletTestCase {

    private ClientResource clientResource;

    @BeforeEach
    protected void setUpEach() throws Exception {
        Finder finder = new Finder();
        finder.setTargetClass(MyResource04.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.setNext(finder);
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
        clientResource = null;
    }

    @Test
    public void testGet() throws IOException, ResourceException {
        Representation result = clientResource.get(MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("[\"root\"]", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

        result = clientResource.get(MediaType.APPLICATION_XML);
        assertNotNull(result);
        assertEquals("<root/>", result.getText());
        assertEquals(MediaType.APPLICATION_XML, result.getMediaType());

        result = clientResource.get(MediaType.TEXT_HTML);
        assertNotNull(result);
        assertEquals("<html><body>root</body></html>", result.getText());
        assertEquals(MediaType.TEXT_HTML, result.getMediaType());

    }

}
