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
import org.restlet.data.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
public class AnnotatedResource03TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource03.class);
    }

    @Test
    public void testGet() throws ResourceException {
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
