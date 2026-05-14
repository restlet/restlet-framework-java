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

import org.junit.jupiter.api.Test;
import org.restlet.data.Status;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource03TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource03.class);
    }

    @Test
    void testGet() throws ResourceException {
        Status status = null;
        try {
            clientResource.get();
            status = clientResource.getStatus();
        } catch (ResourceException e) {
            status = e.getStatus();
        }
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, status);
    }
}
