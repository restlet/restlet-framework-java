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

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource07TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource07.class);
    }

    @Test
    void testPost() throws IOException, ResourceException {
        Representation input = new StringRepresentation("[\"root\"]", MediaType.APPLICATION_JSON);
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
