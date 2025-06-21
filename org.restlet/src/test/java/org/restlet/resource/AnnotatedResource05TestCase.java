/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource05TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource05.class);
    }

    @Test
    public void testPost() throws IOException, ResourceException {
        Representation result = clientResource.post("[\"root\"]", MediaType.APPLICATION_JSON);
        assertNotNull(result);
        assertEquals("[\"root\"]", result.getText());
        assertEquals(MediaType.APPLICATION_JSON, result.getMediaType());

        result = clientResource.post("<root/>", MediaType.APPLICATION_XML);
        assertNotNull(result);
        assertEquals("<root/>", result.getText());
        assertEquals(MediaType.APPLICATION_XML, result.getMediaType());
    }

}
