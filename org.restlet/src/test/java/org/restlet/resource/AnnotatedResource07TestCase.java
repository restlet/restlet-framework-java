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
import org.restlet.representation.StringRepresentation;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource07TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource07.class);
    }

    @Test
    public void testPost() throws IOException, ResourceException {
        Representation input = new StringRepresentation("[\"root\"]",
                MediaType.APPLICATION_JSON);
        Representation result = clientResource.post(input);
        assertNotNull(result);
        assertEquals("[\"root\"]1", result.getText());
        assertEquals(MediaType.APPLICATION_XML, result.getMediaType());

        input = new StringRepresentation("<root/>", MediaType.APPLICATION_XML);
        result = clientResource.post(input);
        assertNotNull(result);
        assertEquals("<root/>2", result.getText());
        assertEquals(MediaType.APPLICATION_XML, result.getMediaType());
    }

}
