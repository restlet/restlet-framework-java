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

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource02TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource02.class);
    }

    @Test
    void testGet() throws IOException, ResourceException {
        Representation result = clientResource.get(MediaType.APPLICATION_ATOM);
        assertNotNull(result);
        assertEquals("<content/>", result.getText());
        assertEquals(MediaType.TEXT_XML, result.getMediaType());
    }
}
